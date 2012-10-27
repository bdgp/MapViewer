package org.bdgp.somviewer.client;

import java.util.Vector;

public class SOMpt {

	public float x;
	public float y;
	public String name;
	public Vector<DrawHints> draw = new Vector<DrawHints>(2);
	
	public SOMpt() {
		// TODO Auto-generated constructor stub
	}

	public SOMpt(float x, float y, String name) {
		this.x = x;
		this.y = y;
		this.name = name;
	}

	public float getX() {
		return x;
	}


	public void setX(float x) {
		this.x = x;
	}


	public float getY() {
		return y;
	}

	
	public void setY(float y) {
		this.y = y;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}

	public Vector<String> getColors() {
		if ( draw.size() == 0 ) {
			return null;
		}
		Vector<String> col = new Vector<String>(draw.size());
		for ( DrawHints dh : draw )
			col.add(dh.color);
		
		return col;
	}
	
	public void addDrawDescription(String color, String shape) {
		DrawHints dh = new DrawHints();
		dh.color = color;
		dh.shape = shape;
		draw.add(dh);
	}
	

	protected class DrawHints {
		public String color;
		public String shape;
	}
	
}
