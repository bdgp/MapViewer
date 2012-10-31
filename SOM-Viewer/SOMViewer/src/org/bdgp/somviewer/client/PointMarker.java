package org.bdgp.somviewer.client;

import java.util.Vector;

import org.vaadin.gwtgraphics.client.VectorObject;
import org.vaadin.gwtgraphics.client.shape.Text;

import com.google.gwt.event.dom.client.ClickHandler;

public class PointMarker extends PointBasic {

	public PointMarker() {
		
	}

	public VectorObject drawLabel(String label, ClickHandler onclick) {

		Text t = new Text(x, y, label);
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

	public VectorObject drawMarker(boolean showMarker, Vector<String> colors, ClickHandler onclick) {

		VectorObject vo = null;
		
		if ( colors == null ) {
			if ( showMarker == true ) {
				vo = drawCircle(MARKER_CIRC_RAD, 0.5f, "fuchsia");
				if ( onclick != null) 
					vo.addClickHandler(onclick);
				return vo;
			} 
		}

		return null;
	}
}
