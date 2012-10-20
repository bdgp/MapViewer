/**
 * 
 */
package org.bdgp.somviewer.rpc;


import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * @author erwin
 *
 */
public class ServerService {
	
	private static ServerService instance;
	private SOMDataServiceAsync proxy;
	
	private ServerService() {
		proxy = (SOMDataServiceAsync) GWT.create(SOMDataService.class);

		// directory "server-status" specified in ServerStatus.gwt.xml
		// This creates the URL for the RPC
		// May be necessary to change for deployment - see p.369 of GWT book!
		((ServiceDefTarget) proxy).setServiceEntryPoint(GWT.getModuleBaseURL() + "som-server");
	}
	
	public static ServerService getInstance() {
		if ( instance == null ) {
			instance = new ServerService();
		}
		
		return instance;
	}
	
	public void getSOMDataPts(String datasrc, AsyncCallback callback) {
		proxy.getSOMDataPts(datasrc, callback);
	}

	public void getSOMmaps(AsyncCallback callback) {
		proxy.getSOMmaps(callback);
	}
	
	public void	getDataOverlay(String datasrc, String overlay, int variant, AsyncCallback callback) {
		proxy.getDataOverlay(datasrc, overlay, variant, callback);
	}

}
