package org.bdgp.somviewer.client.decorator;

public class ColorSimpleCalc {

	protected static final int MIN_DIF = 50;
	
	
	public static int colorDiff(final String col1, final String col2) {
		int diff = 0;
		
		int [] rgb1 = colorInt(col1);
		int [] rgb2 = colorInt(col2);
		
		diff = Math.abs(rgb1[0]-rgb2[0]) + Math.abs(rgb1[1]-rgb2[1]) + Math.abs(rgb1[2]-rgb2[2]);
		
		return diff;
	}
	
	public static int colorDiff(final String col1, int r, int g, int b) {
		int diff = 0;
		
		int [] rgb1 = colorInt(col1);
		
		diff = Math.abs(rgb1[0]-r) + Math.abs(rgb1[1]-g) + Math.abs(rgb1[2]-b);
		
		return diff;
	}

	
	public static boolean isDiff(final String col1, final String col2) {
		
		// Exceptions
		if ( col1.compareTo("000000") == 0 ) {
			if ( col2.compareTo("0000FF") == 0 ) {
				return false;
			}
		}
		
		// Normal stuff
		int d = colorDiff(col1, col2);
		
		if ( d < MIN_DIF )
			return false;
		return true;
	}
	
	
	protected static int [] colorInt(final String colorStr) {
		int [] rgb = new int[3];
		if ( colorStr.startsWith("#") ) {			
			rgb[0] = Integer.valueOf( colorStr.substring( 1, 3 ), 16 );
			rgb[1] = Integer.valueOf( colorStr.substring( 3, 5 ), 16 );
			rgb[2] = Integer.valueOf( colorStr.substring( 5, 7 ), 16 );
		} else {
			rgb[0] = Integer.valueOf( colorStr.substring( 0, 2 ), 16 );
			rgb[1] = Integer.valueOf( colorStr.substring( 2, 4 ), 16 );
			rgb[2] = Integer.valueOf( colorStr.substring( 4, 6 ), 16 );				
		}
		return rgb;
	}

}
