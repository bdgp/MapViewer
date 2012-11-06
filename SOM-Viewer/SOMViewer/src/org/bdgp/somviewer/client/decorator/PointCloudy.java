package org.bdgp.somviewer.client.decorator;

import java.util.HashMap;
import java.util.Vector;

import org.vaadin.gwtgraphics.client.VectorObject;
import org.vaadin.gwtgraphics.client.shape.Circle;

import com.google.gwt.event.dom.client.ClickHandler;

public class PointCloudy extends PointBasic {

	protected static final int BIGCIRC_RAD = 10;
	
	public PointCloudy(HashMap<String, String> colormap) {
		super(colormap);
		// TODO Auto-generated constructor stub
	}

	public VectorObject drawLabel(String label, ClickHandler onclick) {
		return null;
	}
	
	public VectorObject drawMarker(boolean showMarker, Vector<String> colormap_ids, ClickHandler onclick) {
		
		if ( colormap_ids == null ) {
			return null;
		}

		Circle c = null;
		for ( String col : colormap_ids ) {

			if ( ! colormap.containsKey(col) )
				continue;
		
			c = drawCircle(BIGCIRC_RAD, 0.15f, "#" + colormap.get(col));
			if ( onclick != null) 
				c.addClickHandler(onclick);
			break;
		}
		
		return c;
	}
}
