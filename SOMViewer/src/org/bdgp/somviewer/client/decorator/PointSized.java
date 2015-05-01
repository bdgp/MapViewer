package org.bdgp.somviewer.client.decorator;

import java.util.Iterator;
import java.util.Vector;

import org.bdgp.somviewer.client.OverlayDrawMap;
import org.vaadin.gwtgraphics.client.Group;
import org.vaadin.gwtgraphics.client.VectorObject;

import com.google.gwt.event.dom.client.ClickHandler;

public class PointSized extends PointBasic {

	public PointSized(OverlayDrawMap colormap, ColorRank col_rank) {
		super(colormap, col_rank);
		// TODO Auto-generated constructor stub
	}

	public VectorObject drawMarker(boolean showMarker, Vector<String> colors, OverlayDrawMap overlay_map, ClickHandler onclick) {

		// TODO draw a circle for each dec_sizes
		
		int radius = MARKER_CIRC_RAD;
		
		if ( overlay_map != null ) {
			// radius = overlay_map[0];
		}
		
		Group g = new Group();		
		Iterator<String> ov_it = overlay_map.mapIterator();
		while ( ov_it.hasNext() ) {
			String col = ov_it.next();
			
			if ( ! colormap.containsKey(col) )
				continue;

			Vector<Integer> values = overlay_map.getValues(col);
			
			if ( values.size() == 0 )
				return null;
			else if ( values.size() == 1 ) {
				g.add(drawCircle(values.get(0), 0.2f, "#" + colormap.getColor(col)));
			} else {
				for ( Integer v : values ) {
					g.add(drawCircle(v, 0.2f, "#" + colormap.getColor(col)));
				}
			}
		}
		
		return g;
		
	}

}
