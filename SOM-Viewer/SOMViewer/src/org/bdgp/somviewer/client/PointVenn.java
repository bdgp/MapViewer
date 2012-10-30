package org.bdgp.somviewer.client;

import java.util.Vector;

import org.vaadin.gwtgraphics.client.Group;
import org.vaadin.gwtgraphics.client.VectorObject;

import com.google.gwt.event.dom.client.ClickHandler;

/**
 * @author erwin
 * Generates a Venn diagram like point for multiple overlapping colors
 */
public class PointVenn extends PointBasic {
	
	protected static int uuid = 12346;
	protected static final int ShiftVal = 4;
	protected Vector<PtShifts> shifts = new Vector<PtShifts>(10);
		
	public PointVenn() {
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
	
	
	public VectorObject drawMarker(boolean showMarker, Vector<String> colors, ClickHandler onclick) {

		VectorObject vo = null;
		
		if ( colors == null ) {
			if ( showMarker == true ) {
				vo = drawCircle(MARKER_CIRC_RAD, 0.5f, "fuchsia");
				if ( onclick != null) 
					vo.addClickHandler(onclick);
				return vo;
			} else
				return null;
		}

		if ( colors.size() == 1 ) {
			vo = drawCircle(OVERLAY_CIRC_RAD, 0.9f, "#" + colors.get(0));
			if ( onclick != null) 
				vo.addClickHandler(onclick);
			return vo;
		}
		
		int x_save, y_save;
		int ct = 0;
		
		Group g = new Group();
		for ( String col : colors ) {
			if ( ct >= shifts.size() )
				ct = 0;
			
			x_save = x; y_save = y;
			x += shifts.get(ct).xs;
			y += shifts.get(ct).ys;
			vo = drawCircle(OVERLAY_CIRC_RAD, 0.9f, "#" + col);
			g.add(vo);
			if ( onclick != null )
				vo.addClickHandler(onclick);
			x = x_save;
			y = y_save;
			
			ct++;
		}

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
