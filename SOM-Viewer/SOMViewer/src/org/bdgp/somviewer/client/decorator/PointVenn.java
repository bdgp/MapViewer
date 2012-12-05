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
		
	public PointVenn(OverlayDrawMap colormap, ColorRank col_rank) {
		super(colormap, col_rank);
		
		// Add "Venn" like shifts of the point into 8 possible directions
		add(-1,0);
		add(1,0);
		add(0,-1);
		add(0,1);
		add(-1,-1);
		add(1,-1);
		add(-1,1);
		add(1,1);
		
	}
	
	
	protected void add(int xs, int ys) {				
		shifts.add(new PtShifts(xs,ys));
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
		if ( col_rank != 0 && colrank != null ) {
			c.setStrokeWidth(3);
			c.setStrokeColor(colrank.getColor(col_rank));			
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
}
