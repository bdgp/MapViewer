package org.bdgp.somviewer.rpc.data;

import java.util.Vector;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SOMList implements IsSerializable {
	public String queryResult = null;
	public Vector<String> entries;
}
