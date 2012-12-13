package org.bdgp.somviewer.client.decorator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.bdgp.somviewer.client.OverlayDrawMap;
import org.vaadin.gwtgraphics.client.VectorObject;
import org.vaadin.gwtgraphics.client.shape.Circle;

import com.google.gwt.event.dom.client.ClickHandler;

public class PointCloudy extends PointBasic {

	protected static final int BIGCIRC_RAD = 12;
	
	public PointCloudy(OverlayDrawMap colormap, ColorRank col_rank) {
		super(colormap, col_rank);
		// TODO Auto-generated constructor stub
	}

	public VectorObject drawLabel(String label, ClickHandler onclick) {
		return null;
	}
	
	public VectorObject drawMarker(boolean showMarker, Vector<String> colormap_ids, OverlayDrawMap overlay_map, ClickHandler onclick) {
		
		if ( overlay_map == null ) {
			return null;
		}

		Circle c = null;
		Iterator<String> ov_it = overlay_map.mapIterator();
		while ( ov_it.hasNext() ) {
			String col = ov_it.next();

			if ( ! colormap.containsKey(col) )
				continue;
		
			c = drawCircle(BIGCIRC_RAD, 0.15f, "#" + colormap.getColor(col));
			if ( onclick != null) 
				c.addClickHandler(onclick);
			break;
		}
		
		return c;
	}
}
