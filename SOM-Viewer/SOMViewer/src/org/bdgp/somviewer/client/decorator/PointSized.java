package org.bdgp.somviewer.client.decorator;

import java.util.HashMap;
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
		
		if ( colors == null ) {
			if ( showMarker == true )
				return drawCircle(MARKER_CIRC_RAD, 0.5f, "fuchsia");
			else
				return null;
		}

		if ( colors.size() == 1 ) {
			return drawCircle(radius, 0.9f, "#" + colors.get(1));
		}
		
		Group g = new Group();
		for ( String col : colors ) {
			g.add(drawCircle(radius, 0.9f, "#" + col));
		}

		return g;		
	}

}
