package org.bdgp.somviewer.client.decorator;

import java.util.HashMap;
import java.util.Vector;

public class ColorRankBasicCSS implements ColorRank {

	protected Vector<ColorShifts> col_shifts = new Vector<ColorShifts>(10);
	protected HashMap<String, Vector<ColorShifts>> ref_map = new HashMap<String, Vector<ColorShifts>>(); // Cache of sorted map
	
	public ColorRankBasicCSS() {
		// Add colors if ranks are too similar
		addcolor("White", "FFFFFF"); // 0 = no color
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

	
	protected void addcolor(String name, String value) {
		col_shifts.add(new ColorShifts(name, value));
	}

	// Returns ranked color without any considerations
	public String getColor(int rank) {
		if ( rank >= col_shifts.size() )
			rank = 0;
		
		return col_shifts.get(rank).css_name;
	}
	
	// Returns ranked color with distance consideration
	public String getColor(int rank, String ref_color) {
		if ( rank >= col_shifts.size() )
			rank = 0;
		
		Vector<ColorShifts> ref;
		if ( ref_map.containsKey(ref_color) ) {
			ref = ref_map.get(ref_color);
		} else {
			ref = rankBySimilarity(ref_color);
			ref_map.put(ref_color, ref);
		}
		
		return ref.get(rank).css_name;
	}

	
	protected Vector<ColorShifts> rankBySimilarity(String color) {
		Vector<ColorShifts> cs = new Vector<ColorShifts>(col_shifts.size());
		Vector<Integer> cs_val = new Vector<Integer>(col_shifts.size());
		int diff;
		
		ColorShifts c = col_shifts.get(0);
		diff = ColorSimpleCalc.colorDiff(color, c.r, c.g, c.b);
		cs.add(c);
		cs_val.add(diff);
		
		for ( int i = 1; i < col_shifts.size(); i++ ) {
			int j;
			c = col_shifts.get(i);
			diff = ColorSimpleCalc.colorDiff(color, c.r, c.g, c.b);
			for ( j = 0; j < cs_val.size(); j++ ) {
				if ( diff <= cs_val.get(j) ) {
					break;
				}					
			}
			cs_val.insertElementAt(diff, j);
			cs.insertElementAt(c, j);
		}
		
		return cs;
	}
	
	
	protected class ColorShifts {
		public String css_name;
		public String hex_value;
		public int r, g, b;
		
		public ColorShifts(String n, String v) {
			css_name = n;
			hex_value = v;
			
			int [] rgb = ColorSimpleCalc.colorInt(v);
			r = rgb[0];
			g = rgb[1];
			b = rgb[2];
		}
		
		public String getColor() {
			return css_name;
		}
	}
}
