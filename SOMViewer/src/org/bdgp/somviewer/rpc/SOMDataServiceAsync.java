package org.bdgp.somviewer.rpc;

import org.bdgp.somviewer.rpc.data.SOMDataOverlay;
import org.bdgp.somviewer.rpc.data.SOMDataPts;
import org.bdgp.somviewer.rpc.data.SOMList;
import org.bdgp.somviewer.rpc.data.SOMPtInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SOMDataServiceAsync {

	void getSOMmaps(AsyncCallback<SOMList> callback);
	void getSOMDataPts(String datasrc, AsyncCallback<SOMDataPts> callback);
	void getDataOverlay(String datasrc, String name, int variant, AsyncCallback<SOMDataOverlay> callback);
	void getPtInfo(int id, int variant, AsyncCallback<SOMPtInfo> callback);
	
}
