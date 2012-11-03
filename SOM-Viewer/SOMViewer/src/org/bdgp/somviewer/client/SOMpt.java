package org.bdgp.somviewer.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class SOMpt {

	public Integer id;
	public float x;
	public float y;
	public String name;
	public Vector<DrawHints> draw = new Vector<DrawHints>(2);
	public Vector<String> overlay_names = new Vector<String>(2);
	protected HashMap<String,Integer> overlay_unique = new HashMap<String,Integer>();
	protected Integer dummy  = new Integer(1);
	protected HashMap<Integer,String> identical_pts;
	
	public SOMpt() {
		// TODO Auto-generated constructor stub
	}

	public SOMpt(Integer id, float x, float y, String name) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.name = name;
	}

	public Integer getId() {
		return id;
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
	
	
	public Vector<String> getColorMapNames() {
		if ( overlay_unique.size() == 0 )
			return null;
		
		if ( overlay_names.size() == 0 ) {
			for (Map.Entry<String, Integer> entry : overlay_unique.entrySet()) {
				overlay_names.add(entry.getKey());
			}
		}
		return overlay_names;
	}
	
	
	public int getVariant() {
		//TODO: This shouldn't return a fixed value!!!
		if ( draw.size() == 0 ) {
			return 5;
		}

		return draw.get(0).variant;
	}
	
	
	public void addDrawDescription(int var, String color, String shape) {
		DrawHints dh = new DrawHints();
		dh.variant = var;
		dh.color = color;
		dh.shape = shape;
		draw.add(dh);
	}
	
	// Colormaps, by definition, have to be unique
	public void addColorMapName(String n) {
		//overlay_names.add(n);
		overlay_unique.put(n, dummy);
	}
	

	public void addIdenticalPt(Integer id, String name) {
		if ( identical_pts == null )
			identical_pts = new HashMap<Integer,String>();
		identical_pts.put(id, name);
	}
	
	
	public int sizeIdenticalPt() {
		return identical_pts.size();
	}
	
	
	public HashMap<Integer,String> getIdenticalPt() {
		return identical_pts;
	}
	
	
	protected class DrawHints {
		public int variant;
		public String color;
		public String shape;
	}
	
}
