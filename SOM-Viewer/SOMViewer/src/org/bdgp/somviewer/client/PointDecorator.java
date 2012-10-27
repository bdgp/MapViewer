package org.bdgp.somviewer.client;

import java.util.Vector;

import org.vaadin.gwtgraphics.client.VectorObject;

public interface PointDecorator {

	void setPoint(int x, int y);

	VectorObject drawLabel(String label);
	
	VectorObject drawMarker(boolean showMarker, Vector<String> colors);
	
	void infoQuick();
	
	void infoLong();
	
	int uuid();
	
}
