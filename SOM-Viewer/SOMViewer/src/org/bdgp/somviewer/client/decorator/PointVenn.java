package org.bdgp.somviewer.client.decorator;

import java.util.Iterator;
import java.util.Vector;

import org.bdgp.somviewer.client.OverlayDrawMap;
import org.vaadin.gwtgraphics.client.Group;
import org.vaadin.gwtgraphics.client.VectorObject;
import org.vaadin.gwtgraphics.client.shape.Circle;

import com.google.gwt.event.dom.client.ClickHandler;

/**
 * @author erwin
 * Generates a Venn diagram like point for multiple overlapping colors
 */
public class PointVenn extends PointBasic {
	
	protected int uuid = 12346;
	protected static final int ShiftVal = 4;
	protected Vector<PtShifts> shifts = new Vector<PtShifts>(10);
	protected Vector<ColorShifts> col_shifts = new Vector<ColorShifts>(10);
		
	public PointVenn(OverlayDrawMap colormap) {
		super(colormap);
		
		// Add "Venn" like shifts of the point into 8 possible directions
		add(-1,0);
		add(1,0);
		add(0,-1);
		add(0,1);
		add(-1,-1);
		add(1,-1);
		add(-1,1);
		add(1,1);
		
		// Add colors if ranks are too similar
		addcolor("DarkGreen", "006400");
		addcolor("DarkKhaki", "BDB76B");
		addcolor("DarkMagenta","8B008B");
		addcolor("DarkOliveGreen", "556B2F");
		addcolor("Darkorange","FF8C00");
		addcolor("DarkOrchid","9932CC");
		addcolor("DarkRed","8B0000");
		addcolor("DarkSalmon", "E9967A");
		addcolor("DarkSeaGreen", "8FBC8F");
		addcolor("DarkSlateBlue","483D8B");
	}
	
	
	protected void add(int xs, int ys) {				
		shifts.add(new PtShifts(xs,ys));
	}
	
	
	protected void addcolor(String name, String value) {
		col_shifts.add(new ColorShifts(name, value));
	}
	
	
	/* (non-Javadoc)
	 * @see org.bdgp.somviewer.client.PointBasic#drawLabel(java.lang.String, com.google.gwt.event.dom.client.ClickHandler)
	 * We don't draw any text in this class
	 */
	public VectorObject drawLabel(String label, ClickHandler onclick) {
		return null;
	}
	
	public VectorObject drawMarker(boolean showMarker, Vector<String> colormap_ids, OverlayDrawMap overlay_map, ClickHandler onclick) {

//		VectorObject vo = null;
		
		if ( overlay_map == null ) {
			return null;
		}

				
		int x_save, y_save;
		int ct = 0;
		int max_dec = 0;
		
		Circle c = null;
		Group g = new Group();
		Iterator<String> ov_it = overlay_map.mapIterator();
		while ( ov_it.hasNext() ) {
			String col = ov_it.next();
			
			if ( ct >= shifts.size() )
				ct = 0;

			if ( ! colormap.containsKey(col) )
				continue;
			
			x_save = x; y_save = y;
			int [] values = overlay_map.getValues(col);
			if ( values == null ) {
				x += shifts.get(ct).xsc;
				y += shifts.get(ct).ysc;
				// c = drawCircle(OVERLAY_CIRC_RAD, 0.9f, "#" + colormap.getColor(col));
				c = drawDistinctCircle(OVERLAY_CIRC_RAD, 0.9f, "#" + colormap.getColor(col), overlay_map.getColorRank(col));
			} else {
				max_dec = values.length - 1;
				x += shifts.get(ct).xs * values[max_dec];
				y += shifts.get(ct).ys * values[max_dec];
				c = drawCircle(values[max_dec], 0.9f, "#" + colormap.getColor(col));
			}
			g.add(c);
			if ( onclick != null )
				c.addClickHandler(onclick);
			x = x_save;
			y = y_save;
			
			ct++;
		}

		if ( g.getVectorObjectCount() == 0 )
			g = null;
		else if ( g.getVectorObjectCount() == 1 && c != null ) {
			g.remove(c);
			c.setX(x);
			c.setY(y);
			return c;
		}
		
		return g;
	}

	
	protected Circle drawDistinctCircle(int radius, float opacy, String color, int col_rank) {
		Circle c = new Circle(x, y, radius);
		c.setFillOpacity(opacy);
		if ( col_rank != 0 ) {
			if ( col_rank >= col_shifts.size() )
				col_rank = 0;
			c.setStrokeWidth(2);
			c.setStrokeColor(col_shifts.get(col_rank).getColor());			
		} else {
			c.setStrokeWidth(0);
			c.setStrokeColor("white");
		}
		c.setFillColor(color);
		return(c);
	}
	
	protected class PtShifts {
		// original factors
		public int xs;
		public int ys;
		// factors multiplied by const value
		public int xsc;
		public int ysc;
		
		public PtShifts(int xs, int ys) {
			this.xs = xs;
			this.ys = ys;
			this.xsc = xs != 0 ? ShiftVal * xs : 0;
			this.ysc = ys != 0 ? ShiftVal * ys : 0;
		}
		
	}
	
	protected class ColorShifts {
		public String css_name;
		public String hex_value;
		
		public ColorShifts(String n, String v) {
			css_name = n;
			hex_value = v;
		}
		
		public String getColor() {
			return css_name;
		}
	}

}
