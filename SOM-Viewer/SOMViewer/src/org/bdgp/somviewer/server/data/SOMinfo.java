package org.bdgp.somviewer.server.data;

import java.util.Vector;

public interface SOMinfo {

	Vector<String> somMaps() throws Exception;
	
	String shortInfo(int id, int variant) throws Exception;
	
	String longInfo(int id, int variant) throws Exception;
	
	int getShortInfoWidth();
	
}
