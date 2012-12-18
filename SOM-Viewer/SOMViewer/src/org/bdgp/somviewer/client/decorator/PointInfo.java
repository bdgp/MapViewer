package org.bdgp.somviewer.client.decorator;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.bdgp.somviewer.client.DialogBoxClosable;
import org.bdgp.somviewer.client.OverlayDrawMap;
import org.bdgp.somviewer.rpc.AbstractLoggingAsyncHandler;
import org.bdgp.somviewer.rpc.ServerService;
import org.bdgp.somviewer.rpc.data.SOMPtInfo;
import org.vaadin.gwtgraphics.client.VectorObject;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PointInfo implements PointDecorator {

	protected final static int DLG_MINH = 200;
	protected final static int DLG_BUFFER = 50;
	protected final static String COLOR_BUTTON_ACTIVE = "#66FF99";
	protected final static String COLOR_BUTTON_INACTIVE = "#CCCCCC";
	
	protected final static int uuid = 12347;
	protected int x,y;
	protected int view_h, view_w;
	// protected int dialog_w = -1;
	protected int click_x, click_y; // Last click position
	protected String title;
	protected int variant;

	protected HashMap<Integer,String> contents;
	protected String label;
	protected Integer id;

	protected HashMap<Integer,InfoData> infoCache = new HashMap<Integer,InfoData>();
	
	// Button management
	protected HashMap<Integer,Button> buttonList = null;
	protected Integer buttonActive = null;
	
	protected DialogBox dialogBox = null;
	protected VerticalPanel dialogVPanel = null;
	protected ScrollPanel scrollDlgPanel = null;
	
	public PointInfo() {
	}

	public void setPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setViewPortSize(int w, int h) {
		this.view_h = h;
		this.view_w = w;
	}

	
	public void setInfo(Integer id, String name, HashMap<Integer,String> others) {
		this.id = id;
		this.label = name;
		this.contents = others;
	}
	
	
	public VectorObject drawLabel(String label, ClickHandler onclick) {
		return null;
	}

	public VectorObject drawMarker(boolean showMarker, Vector<String> colors, OverlayDrawMap overlay_map, ClickHandler onclick) {
		return null;
	}

	public void infoQuick(String title, Integer id, int variant, int x, int y) {
		
		// If the variant changes, invalidate the cache
		if ( this.variant != variant )
			infoCache = new HashMap<Integer,InfoData>();
		
		click_x = x; click_y = y;
		this.title = title;
		this.variant = variant;
		
		if ( infoCache.containsKey(id) )
			infoDialog(infoCache.get(id));
		else
			getSInfo(id.intValue(), variant);
	}

	public void infoLong(Integer id) {
	}


	protected void infoDialog(InfoData info_data) {
		// Create the popup dialog box
		
		int pos_x, pos_y;
		int width = 0, height = 0;
		
		pos_x = click_x; pos_y = click_y;
		
		int win_w = Window.getClientWidth();
		int win_h = Window.getClientHeight();
		
		if ( dialogBox == null ) {
			dialogBox = new DialogBoxClosable();
			dialogBox.setAnimationEnabled(true);
			dialogVPanel = new VerticalPanel();
			scrollDlgPanel = new ScrollPanel();
			dialogBox.setWidget(scrollDlgPanel);
		} else {
			dialogVPanel.clear();
			scrollDlgPanel.clear();
		}

		dialogBox.setText(title);

		populateInfoPanel(info_data.sinfo);
		
		if ( contents == null || contents.size() == 0 ) {
			scrollDlgPanel.add(dialogVPanel);
		} else {
			HorizontalPanel dialogExtPanel = new HorizontalPanel();
			dialogExtPanel.setWidth("100%");
			dialogExtPanel.add(dialogVPanel);
			
			VerticalPanel othersPanel = new VerticalPanel();
			
			Button b;
			buttonList = new HashMap<Integer, Button>(contents.size() + 1);
			b = altButton(id, label, COLOR_BUTTON_ACTIVE);
			othersPanel.add(b);
			buttonActive = id;
			
			for ( Map.Entry<Integer, String> entry : contents.entrySet() ) {
				String c = entry.getValue();
				// othersPanel.add(new HTML(c));
				b = altButton(entry.getKey(), c, COLOR_BUTTON_INACTIVE);
				othersPanel.add(b);
			}
			dialogExtPanel.add(othersPanel);
			
			scrollDlgPanel.add(dialogExtPanel);
			width += 100;
		}

		// Set sizes
		// Note: Width setting may interfere with setting in .css file - currently commented out
		width += info_data.sinfo_w;
		height = win_h - pos_y - DLG_BUFFER;
		height = height < DLG_MINH ? DLG_MINH : height;
		dialogBox.setWidth(width + "px");
		scrollDlgPanel.setHeight(height + "px");
		
		if ( pos_x > win_w / 2 ) {
			pos_x -= width;
		}		
		if ( pos_y + height > win_h ) {
			pos_y -= height;
		}
		dialogBox.setPopupPosition(pos_x, pos_y);
		
		dialogBox.show();

	}
	
	
	/**
	 * Same as infoDialog but just updates the dialogVPanel
	 * @param info_data
	 */
	protected void updateDialog(InfoData info_data) {
		populateInfoPanel(info_data.sinfo);
		
		Button b;
		b = buttonList.get(buttonActive);
		b.getElement().getStyle().setBackgroundColor(COLOR_BUTTON_INACTIVE);
		b = buttonList.get(info_data.id);
		b.getElement().getStyle().setBackgroundColor(COLOR_BUTTON_ACTIVE);
		buttonActive = info_data.id;
	}
	
	
	protected void populateInfoPanel(String html) {
		dialogVPanel.clear();

		final Button closeButton = new Button("Close");
		// We can set the id of a widget by accessing its Element
		closeButton.getElement().setId("closeButton");
		dialogVPanel.addStyleName("dialogVPanel");

		dialogVPanel.add(new HTML(html));
		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_LEFT);
		dialogVPanel.add(closeButton);

		// Add a handler to close the DialogBox
		closeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				dialogBox.hide();
			}
		});

	}
	
	
	protected Button altButton(Integer ptid, String name, String color) {
		Button b = new Button(name);
		b.setStyleName("dlgOtherGeneButton");
		b.getElement().getStyle().setBackgroundColor(color);
		buttonList.put(ptid, b);
		b.addClickHandler(new AltButtonHandler(ptid));

		return b;
	}
	
	
	protected void getSInfo(int id, int variant) {
		ServerService.getInstance().getPtInfo(id, variant, new SomPtInfoUpdater());
	}

	protected void getSAltInfo(int id, int variant) {
		ServerService.getInstance().getPtInfo(id, variant, new SomAltInfoUpdater());
	}

	
	/**
	 * @author erwin
	 * RPC incoming data for the overlays
	 */
	public class SomPtInfoUpdater extends AbstractLoggingAsyncHandler<SOMPtInfo> {
		
		public void handleFailure(Throwable caught) {
			
		}
		
		public void handleSuccess(SOMPtInfo result) {
			SOMPtInfo data = (SOMPtInfo) result;
			
			if ( data.queryResult != null ) {
				setLogEntry("Pt Info: ERROR " + data.queryResult);
			} else {
				setLogEntry("Pt Info: Received " + data.req_id);
				InfoData id = new InfoData();
				id.id = data.req_id;
				id.sinfo = data.html_Sinfo;
				id.sinfo_w = data.width_Sinfo;
				id.linfo = data.html_Linfo;
				infoCache.put(data.req_id, id);
				infoDialog(id);
			}
		}
		
	}

	/**
	 * @author erwin
	 * RPC incoming data for the overlays
	 */
	public class SomAltInfoUpdater extends AbstractLoggingAsyncHandler<SOMPtInfo> {
		
		public void handleFailure(Throwable caught) {
			
		}
		
		public void handleSuccess(SOMPtInfo result) {
			SOMPtInfo data = result;
			
			if ( data.queryResult != null ) {
				setLogEntry("Pt Info: ERROR " + data.queryResult);
			} else {
				setLogEntry("Pt Info: Received " + data.req_id);
				InfoData id = new InfoData();
				id.id = data.req_id;
				id.sinfo = data.html_Sinfo;
				id.sinfo_w = data.width_Sinfo;
				id.linfo = data.html_Linfo;
				infoCache.put(data.req_id, id);
				updateDialog(id);
			}
		}
		
	}

	
	public class AltButtonHandler implements ClickHandler {
		Integer ptid;
		
		public AltButtonHandler(Integer ptid) {
			this.ptid = ptid;
		}
		
		public void onClick(ClickEvent event) {
			if ( infoCache.containsKey(ptid) )
				updateDialog(infoCache.get(ptid));
			else
				getSAltInfo(ptid.intValue(), variant);
		}
	}
	
	
	public boolean isDraw() {
		return false;
	}

	public boolean isInfo() {
		return true;
	}

	
	public class InfoData {
		public Integer id;
		public String sinfo;
		public int sinfo_w;
		public String linfo;
	}
	
	
}
