package org.bdgp.somviewer.client.decorator;

import java.util.HashMap;
import java.util.Vector;

import org.vaadin.gwtgraphics.client.VectorObject;

import com.google.gwt.event.dom.client.ClickHandler;

public interface PointDecorator {

	void setPoint(int x, int y);
	
	void setViewPortSize(int w, int h);
	
	void setInfo(Integer id, String name, HashMap<Integer,String> others);

	VectorObject drawLabel(String label, ClickHandler onclick);
	
	VectorObject drawMarker(boolean showMarker, Vector<String> colors, ClickHandler onclick);
	
	void infoQuick(String title, Integer id, int variant, int x, int y);
	
	void infoLong(Integer id);
	
	boolean isDraw();
	
	boolean isInfo();
	
	int uuid();
	
}
