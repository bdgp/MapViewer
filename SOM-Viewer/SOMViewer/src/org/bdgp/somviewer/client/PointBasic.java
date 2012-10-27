package org.bdgp.somviewer.client;

import java.util.Vector;

import org.vaadin.gwtgraphics.client.Group;
import org.vaadin.gwtgraphics.client.VectorObject;
import org.vaadin.gwtgraphics.client.shape.Circle;
import org.vaadin.gwtgraphics.client.shape.Text;

public class PointBasic implements PointDecorator {

	protected final static int uuid = 12345;
	protected final static int MARKER_CIRC_RAD = 2;
	protected final static int OVERLAY_CIRC_RAD = 7;
	int x,y;
	
	public PointBasic() {
	}

	public void setPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public VectorObject drawLabel(String label) {

		Text t = new Text(x, y, label);
		t.setFontFamily("Arial");
		t.setFontSize(10);
		t.setStrokeWidth(0);
		t.setFillColor("black");
		int fw = t.getTextWidth() / 2;
		int fh = t.getTextHeight() / 2;
		t.setX(x - fw);
		t.setY(y + fh);
		
		return t;
	}

	public VectorObject drawMarker(boolean showMarker, Vector<String> colors) {

		if ( colors == null ) {
			if ( showMarker == true )
				return drawCircle(MARKER_CIRC_RAD, 0.5f, "fuchsia");
			else
				return null;
		}

		if ( colors.size() == 1 ) {
			return drawCircle(OVERLAY_CIRC_RAD, 0.9f, "#" + colors.get(1));
		}
		
		Group g = new Group();
		for ( String col : colors ) {
			g.add(drawCircle(OVERLAY_CIRC_RAD, 0.9f, "#" + col));
		}

		return g;		
	}

	
	protected Circle drawCircle(int radius, float opacy, String color) {
		Circle c = new Circle(x, y, radius);
		c.setFillOpacity(opacy);
		c.setStrokeWidth(0);
		c.setStrokeColor("white");	
		c.setFillColor(color);
		return(c);		
	}
	
	
	public void infoQuick() {
	}

	public void infoLong() {
	}

	public int uuid() {
		return uuid;
	}

}
