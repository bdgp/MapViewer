package org.bdgp.somviewer.client;

import java.util.HashMap;
import java.util.Vector;

import org.bdgp.somviewer.client.CategoryComposite.SomOverlayUpdater;
import org.bdgp.somviewer.rpc.AbstractLoggingAsyncHandler;
import org.bdgp.somviewer.rpc.ServerService;
import org.bdgp.somviewer.rpc.data.SOMDataOverlay;
import org.bdgp.somviewer.rpc.data.SOMPtInfo;
import org.vaadin.gwtgraphics.client.VectorObject;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PointInfo implements PointDecorator {

	protected final static int uuid = 12347;
	int x,y;

	HashMap<Integer,String> infoCache = new HashMap<Integer,String>();
	
	DialogBox dialogBox = null;
	VerticalPanel dialogVPanel = null;
	
	public PointInfo() {
		// TODO Auto-generated constructor stub
	}

	public void setPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public VectorObject drawLabel(String label) {
		return null;
	}

	public VectorObject drawMarker(boolean showMarker, Vector<String> colors) {
		return null;
	}

	public void infoQuick(Integer id, int variant) {
		
		if ( infoCache.containsKey(id) )
			infoDialog(infoCache.get(id));
	
		getSInfo(id.intValue(), variant);
	}

	public void infoLong(Integer id) {
	}

	public int uuid() {
		return uuid;
	}


	protected void infoDialog(String html) {
		// Create the popup dialog box
		
		if ( dialogBox == null ) {
			dialogBox = new DialogBox();
			dialogBox.setText("Information");
			dialogBox.setAnimationEnabled(true);
			dialogVPanel = new VerticalPanel();
		} else {
			dialogVPanel.clear();
		}

		final Button closeButton = new Button("Close");
		// We can set the id of a widget by accessing its Element
		closeButton.getElement().setId("closeButton");
		dialogVPanel.addStyleName("dialogVPanel");
		dialogVPanel.add(new HTML(html));
		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		dialogVPanel.add(closeButton);
		dialogBox.setWidget(dialogVPanel);
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
				infoCache.put(data.req_id, data.html_Sinfo);
				infoDialog(data.html_Sinfo);
			}
		}
		
	}

	public boolean isDraw() {
		return false;
	}

	public boolean isInfo() {
		return true;
	}

	
}
