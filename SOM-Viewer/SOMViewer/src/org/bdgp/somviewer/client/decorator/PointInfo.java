package org.bdgp.somviewer.client.decorator;

import java.util.HashMap;
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
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PointInfo implements PointDecorator {

	protected final static int DLG_MINH = 200;
	protected final static int DLG_BUFFER = 50;
	
	protected final static int uuid = 12347;
	int x,y;
	int view_h, view_w;
	int dialog_w = -1;
	int click_x, click_y; // Last click position
	String title;

	protected HashMap<Integer,String> contents;
	protected String label;
	protected Integer id;

	HashMap<Integer,InfoData> infoCache = new HashMap<Integer,InfoData>();
	
	DialogBox dialogBox = null;
	VerticalPanel dialogVPanel = null;
	ScrollPanel scrollDlgPanel = null;
	
	public PointInfo() {
		// TODO Auto-generated constructor stub
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
		
		click_x = x; click_y = y;
		this.title = title;
		
		if ( infoCache.containsKey(id) )
			infoDialog(infoCache.get(id));
	
		getSInfo(id.intValue(), variant);
	}

	public void infoLong(Integer id) {
	}

	public int uuid() {
		return uuid;
	}


	protected void infoDialog(InfoData info_data) {
		// Create the popup dialog box
		
		String html = info_data.sinfo;
		
		int pos_x, pos_y;
		
		pos_x = click_x; pos_y = click_y;
		
		int win_w = Window.getClientWidth();
		int win_h = Window.getClientHeight();
		
		if ( dialogBox == null ) {
			dialogBox = new DialogBoxClosable();
			//dialogBox.setText("Information");
			// dialogBox.setText(label);
			dialogBox.setAnimationEnabled(true);
			dialogVPanel = new VerticalPanel();
			scrollDlgPanel = new ScrollPanel();
			dialogBox.setWidget(scrollDlgPanel);
			scrollDlgPanel.add(dialogVPanel);
		} else {
			dialogVPanel.clear();
		}

		dialogBox.setText(title);

		final Button closeButton = new Button("Close");
		// We can set the id of a widget by accessing its Element
		closeButton.getElement().setId("closeButton");
		dialogVPanel.addStyleName("dialogVPanel");
		dialogVPanel.add(new HTML(html));
		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_LEFT);
		dialogVPanel.add(closeButton);
		// dialogBox.setWidget(dialogVPanel);

		// Set sizes
		int height = win_h - pos_y - DLG_BUFFER;
		height = height < DLG_MINH ? DLG_MINH : height;
		dialogBox.setWidth(info_data.sinfo_w + "px");
		scrollDlgPanel.setHeight(height + "px");
		
		if ( pos_x > win_w / 2 ) {
			pos_x -= info_data.sinfo_w;
		}		
		if ( pos_y + height > win_h ) {
			pos_y -= height;
		}
		dialogBox.setPopupPosition(pos_x, pos_y);
		
		dialogBox.show();

		// Add a handler to close the DialogBox
		closeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				dialogBox.hide();
			}
		});

	}
	
	
	protected void getSInfo(int id, int variant) {
		ServerService.getInstance().getPtInfo(id, variant, new SomPtInfoUpdater());
	}
	
	/**
	 * @author erwin
	 * RPC incoming data for the overlays
	 */
	public class SomPtInfoUpdater extends AbstractLoggingAsyncHandler {
		
		public void handleFailure(Throwable caught) {
			
		}
		
		public void handleSuccess(Object result) {
			SOMPtInfo data = (SOMPtInfo) result;
			
			if ( data.queryResult != null ) {
				setLogEntry("Pt Info: ERROR " + data.queryResult);
			} else {
				setLogEntry("Pt Info: Received " + data.req_id);
				InfoData id = new InfoData();
				id.sinfo = data.html_Sinfo;
				id.sinfo_w = data.width_Sinfo;
				id.linfo = data.html_Linfo;
				infoCache.put(data.req_id, id);
				infoDialog(id);
			}
		}
		
	}

	public boolean isDraw() {
		return false;
	}

	public boolean isInfo() {
		return true;
	}

	
	public class InfoData {
		public String sinfo;
		public int sinfo_w;
		public String linfo;
	}
	
	
}
