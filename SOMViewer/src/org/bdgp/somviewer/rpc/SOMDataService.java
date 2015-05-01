/**
 * 
 */
package org.bdgp.somviewer.rpc;


import org.bdgp.somviewer.rpc.data.SOMDataOverlay;
import org.bdgp.somviewer.rpc.data.SOMDataPts;
import org.bdgp.somviewer.rpc.data.SOMList;
import org.bdgp.somviewer.rpc.data.SOMPtInfo;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * @author erwin
 *
 */
public interface SOMDataService extends RemoteService {

	SOMList getSOMmaps();
	SOMDataPts getSOMDataPts(String datasrc);
	SOMDataOverlay getDataOverlay(String datasrc, String name, int variant);
	SOMPtInfo getPtInfo(int id, int variant);

}
