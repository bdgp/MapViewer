package org.bdgp.somviewer.client.decorator;

import java.util.HashMap;
import java.util.Vector;

import org.bdgp.somviewer.client.OverlayDrawMap;
import org.vaadin.gwtgraphics.client.Group;
import org.vaadin.gwtgraphics.client.VectorObject;
import org.vaadin.gwtgraphics.client.shape.Circle;
import org.vaadin.gwtgraphics.client.shape.Text;

import com.google.gwt.event.dom.client.ClickHandler;

/**
 * @author erwin
 * This one is the base class for many PointDecorators
 * It does everything but not very well, should be overridden when possible
 */
public class PointBasic implements PointDecorator {

	protected int uuid = 12345;
	protected final static int MARKER_CIRC_RAD = 2;
	protected final static int OVERLAY_CIRC_RAD = 7;
	
	protected OverlayDrawMap colormap;
	protected HashMap<Integer,String> contents;
	protected String label;
	protected Integer id;
	int x,y;
	int view_w, view_h;
	
	
	public PointBasic(OverlayDrawMap colormap) {
		this.colormap = colormap;
	}

	public void setPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	
	public void setInfo(Integer id, String name, HashMap<Integer,String> others) {
		this.id = id;
		this.label = name;
		this.contents = others;
	}
	
	
	public void setViewPortSize(int w, int h) {
		this.view_h = h;
		this.view_w = w;
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

	public VectorObject drawMarker(boolean showMarker, Vector<String> colors, OverlayDrawMap overlay_map, ClickHandler onclick) {

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
	
	
	public void infoQuick(String title, Integer id, int variant ,int x, int y) {
	}

	public void infoLong(Integer id) {
	}

	public int uuid() {
		int ret_uuid = this.uuid;
		return ret_uuid;
	}

	public boolean isDraw() {
		return true;
	}

	public boolean isInfo() {
		return false;
	}

}
