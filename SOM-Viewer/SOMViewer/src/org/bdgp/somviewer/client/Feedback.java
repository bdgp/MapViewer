package org.bdgp.somviewer.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class Feedback {

	private static Feedback feedback = null;

	protected DialogBox dialogBox = null;
	protected VerticalPanel dialogVPanel = null;
	
	protected HTML status = null;
	protected HTML info = null;
	
	protected String req = "Requested data from server";
	protected HTML req_title = new HTML(req);
	protected HTML req_progress = new HTML();
	protected int counter = 0;
	
	protected Timer discardDelayTimer = null;

	private Feedback() {
	}

	// Singleton class!!
	public static Feedback getInstance() {
		if ( feedback == null ) {
			feedback = new Feedback();
		}
		
		return feedback;
	}

	
	public void setStatusWidget(HTML status) {
		this.status = status;
	}
	

	public void setInfoWidget(HTML info) {
		this.info = info;
	}

	
	
	public void rpcRequested(String requested) {
		startOp(requested);
	}
	
	
	public void rpcReceived() {
		finishOp();
	}

	public void rpcReceived(String result) {
		finishOp();
	}

	public void rpcError(String error) {
		finishOp();
	}

	
	public void startDraw() {
		startOp(null);
	}
	
	public void finishDraw() {
		finishOp();
	}
	
	protected void startOp(String info) {
		
		counter++;

		setProgress(100/(counter + 1));
		status.setText(req);
		
	}
	
	
	protected void finishOp() {
		if ( --counter > 0 ) {
			setProgress(100/(counter + 1) + 25);
		} else {
			status.setText("Done");
			setProgress(100);
			
			if ( discardDelayTimer != null ) {
				return;
			}
			
		    discardDelayTimer = new Timer() {
		        public void run() {
		        	discard();
		        	//Window.alert("Nifty, eh?");
		        }
		      };

		      // Schedule the timer to run once in 5 seconds.
		      discardDelayTimer.schedule(5000);

			
		}		
	}
	
	
	protected void discard() {
		status.setText("");
		info.setText("");
		discardDelayTimer = null;
	}
	
	
	// From
	// http://code.google.com/p/upload4gwt/source/browse/trunk/src/com/siderakis/upload4gwt/client/ui/SimpleProgressBar.java
	
    public interface StatusCellSafeHTMLTemplate extends SafeHtmlTemplates {
        @Template("<div><div style=\"font-size:medium;height:1.2em;width:100%;cursor:default;border:thin #7ba5d5 solid;\">"
                        + "<div style=\"height:1.2em;width:{0}%; background:#8cb6e6; background-image: url('progress_background.png');\">"
						//+ "<div style=\"height:1.2em;width:{0}%; background:#8cb6e6;\">"
                        + "</div><div style=\"height:1.2em; margin:-1.2em;font-weight:bold;color:#4e7fba;\">"
                        + "<center>{0}%</center></div></div></div>")
                        SafeHtml status(int percentage);
    }
    
    final private StatusCellSafeHTMLTemplate statusCellSafeHTMLTemplate = (StatusCellSafeHTMLTemplate) GWT.create(StatusCellSafeHTMLTemplate.class);

    
	
    public void setProgress(final int uploadStatus) {
    	if ( status == null )
    		return;
        info.setHTML(statusCellSafeHTMLTemplate.status(uploadStatus));
    }

	
}
