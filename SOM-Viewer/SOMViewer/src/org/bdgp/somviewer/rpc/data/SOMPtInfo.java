package org.bdgp.somviewer.rpc.data;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SOMPtInfo implements IsSerializable {

	public String queryResult = null;
	public int req_id;
	public int req_variant;
	
	public String title;
	public String html_Sinfo;
	public String html_Linfo;

}
