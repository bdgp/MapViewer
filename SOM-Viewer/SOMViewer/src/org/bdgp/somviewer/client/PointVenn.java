package org.bdgp.somviewer.client;

import java.util.HashMap;
import java.util.Vector;

import org.vaadin.gwtgraphics.client.Group;
import org.vaadin.gwtgraphics.client.VectorObject;

import com.google.gwt.event.dom.client.ClickHandler;

/**
 * @author erwin
 * Generates a Venn diagram like point for multiple overlapping colors
 */
public class PointVenn extends PointBasic {
	
	protected int uuid = 12346;
	protected static final int ShiftVal = 4;
	protected Vector<PtShifts> shifts = new Vector<PtShifts>(10);
		
	public PointVenn(HashMap<String,String> colormap) {
		super(colormap);
		
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
		int x, y;
		
		x = xs != 0 ? ShiftVal * xs : 0;
		y = ys != 0 ? ShiftVal * ys : 0;
				
		shifts.add(new PtShifts(x,y));
	}
	
	
	/* (non-Javadoc)
	 * @see org.bdgp.somviewer.client.PointBasic#drawLabel(java.lang.String, com.google.gwt.event.dom.client.ClickHandler)
	 * We don't draw any text in this class
	 */
	public VectorObject drawLabel(String label, ClickHandler onclick) {
		return null;
	}
	
	public VectorObject drawMarker(boolean showMarker, Vector<String> colormap_ids, ClickHandler onclick) {

		VectorObject vo = null;
		
		// TODO: This will be handled by PointMarker
		if ( colormap_ids == null ) {
			return null;
//			if ( showMarker == true ) {
//				vo = drawCircle(MARKER_CIRC_RAD, 0.5f, "fuchsia");
//				if ( onclick != null) 
//					vo.addClickHandler(onclick);
//				return vo;
//			} else
//				return null;
		}

		
		if ( colormap_ids.size() == 1 ) {
			if ( ! colormap.containsKey(colormap_ids.get(0)) )
				return null;
			vo = drawCircle(OVERLAY_CIRC_RAD, 0.9f, "#" + colormap.get(colormap_ids.get(0)));
			if ( onclick != null) 
				vo.addClickHandler(onclick);
			return vo;
		}
		
		int x_save, y_save;
		int ct = 0;
		
		Group g = new Group();
		for ( String col : colormap_ids ) {
			if ( ct >= shifts.size() )
				ct = 0;

			if ( ! colormap.containsKey(col) )
				continue;
			
			x_save = x; y_save = y;
			x += shifts.get(ct).xs;
			y += shifts.get(ct).ys;
			vo = drawCircle(OVERLAY_CIRC_RAD, 0.9f, "#" + colormap.get(col));
			g.add(vo);
			if ( onclick != null )
				vo.addClickHandler(onclick);
			x = x_save;
			y = y_save;
			
			ct++;
		}

		if ( g.getVectorObjectCount() == 0 )
			g = null;
		
		return g;
	}

	
	protected class PtShifts {
		public int xs;
		public int ys;
		
		public PtShifts(int xs, int ys) {
			this.xs = xs;
			this.ys = ys;
		}
	}

}
