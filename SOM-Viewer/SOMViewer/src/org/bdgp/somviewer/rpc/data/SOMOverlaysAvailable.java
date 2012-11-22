package org.bdgp.somviewer.rpc.data;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SOMOverlaysAvailable implements IsSerializable {

	public String name;
	public int variant;
	public String [] variant_names;
	public String color;
	public String [] color_similar;
	public String type;
	public String decorator;

}
