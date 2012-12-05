package org.bdgp.somviewer.client.decorator;

import java.util.Vector;

public class ColorRankBasicCSS implements ColorRank {

	protected Vector<ColorShifts> col_shifts = new Vector<ColorShifts>(10);
	
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

	
	public String getColor(int rank) {
		if ( rank >= col_shifts.size() )
			rank = 0;
		
		// TODO: add simple algorithm to figure out color tone and use different one
		return col_shifts.get(rank).css_name;
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
