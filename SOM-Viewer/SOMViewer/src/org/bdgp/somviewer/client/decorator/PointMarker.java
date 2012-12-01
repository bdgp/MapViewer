package org.bdgp.somviewer.client.decorator;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.bdgp.somviewer.client.OverlayDrawMap;
import org.vaadin.gwtgraphics.client.VectorObject;
import org.vaadin.gwtgraphics.client.shape.Text;

import com.google.gwt.event.dom.client.ClickHandler;

public class PointMarker extends PointBasic {

	protected int uuid = 12348;
	
	public PointMarker(OverlayDrawMap colormap) {
		super(colormap);
	}

	public VectorObject drawLabel(String label, ClickHandler onclick) {
		
		String draw = new String(label);
		
		if ( contents != null ) {
			draw += "+";
//			for (Map.Entry<Integer, String> entry : contents.entrySet()) {
//				draw += "\n" + entry.getValue();
//			}
		}
		
		Text t = new Text(x, y, draw);
		t.setFontFamily("Arial");
		t.setFontSize(10);
		t.setStrokeWidth(0);
		t.setFillColor("black");
		int fw = t.getTextWidth() / 2;
		int fh = t.getTextHeight() / 2;
		t.setX(x - fw);
		t.setY(y + fh);
		
		if (onclick != null)
			t.addClickHandler(onclick);
		
		return t;
	}

	public VectorObject drawMarker(boolean showMarker, Vector<String> colors, OverlayDrawMap overlay_map, ClickHandler onclick) {

		VectorObject vo = null;
		
		if ( colors == null && overlay_map == null ) {
			if ( showMarker == true ) {
				vo = drawCircle(markerSize(), 0.5f, "fuchsia");
				if ( onclick != null) 
					vo.addClickHandler(onclick);
				return vo;
			} 
		}

		return null;
	}
	
	protected int markerSize() {
		int s = 0;
		
		if ( contents != null ) {
			s = contents.size() / 2;
			s = s == 0 ? 1 : s;
		}

		s += MARKER_CIRC_RAD;
		return s;
	}
	
}
