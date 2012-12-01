package org.bdgp.somviewer.rpc.data;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SOMDataOverlay implements IsSerializable {
	public int [] id;
	public float [] values;
	public float val_min, val_max;
	public String name;
	public int variant;
	public String queryResult;
}
