package org.bdgp.somviewer.rpc;

import com.google.gwt.core.client.GWT;

public abstract class AbstractLoggingAsyncHandler extends AbstractBaseAsyncHandler {
	
	String log = null;
	
	public void onFailure(Throwable caught) {
		
		GWT.log("RPC Failure [" + this.getClass().getName() + "]", caught);
		super.onFailure(caught);
		printLogEntry();
	}

	public void onSuccess(Object result) {
		
		GWT.log("RPC Success [" + this.getClass().getName() + "]", null);
		super.onSuccess(result);		
		printLogEntry();
	}

	protected void setLogEntry(String log) {
		this.log = log;
	}
	
	private void printLogEntry() {
		if ( log != null ) {
			GWT.log("RPC log (" + log + ")", null);
		}
		log = null;
	}
	
}
