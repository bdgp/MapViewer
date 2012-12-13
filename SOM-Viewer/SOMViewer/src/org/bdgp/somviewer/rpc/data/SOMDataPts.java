package org.bdgp.somviewer.rpc.data;

import java.util.Vector;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SOMDataPts implements IsSerializable {

	public String queryResult = null;
	public String requested = null;
	public String map = null;
	
	public int [] id;
	public float [] x, y;
	public String [] names;
	
	public SOMOverlaysAvailable [] available;
	
	public SOMOverlaysAvailable CreateAvailable(String name, int variant, String [] variant_names, String color, String[] color_similar, String type, String decorator) {
		SOMOverlaysAvailable oa = new SOMOverlaysAvailable();
		oa.name = name;
		oa.variant = variant;
		oa.variant_names = variant_names;
		oa.color = color;
		oa.color_similar = color_similar;
		oa.type = type;
		oa.decorator = decorator;
		return oa;
	}
		
}
