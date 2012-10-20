package org.bdgp.somviewer.rpc;

import org.bdgp.somviewer.rpc.data.SOMDataOverlay;
import org.bdgp.somviewer.rpc.data.SOMDataPts;
import org.bdgp.somviewer.rpc.data.SOMList;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SOMDataServiceAsync {

	void getSOMmaps(AsyncCallback callback);
	void getSOMDataPts(String datasrc, AsyncCallback callback);
	void getDataOverlay(String datasrc, String name, int variant, AsyncCallback callback);
	
}
