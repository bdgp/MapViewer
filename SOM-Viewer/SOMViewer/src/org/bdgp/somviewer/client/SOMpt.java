package org.bdgp.somviewer.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class SOMpt {

	protected Integer id;
	protected float x;
	protected float y;
	protected String name;
	protected int variant;
	protected Vector<DrawHints> draw = new Vector<DrawHints>(2);
	protected Vector<String> overlay_names = new Vector<String>(2);
	protected HashMap<String,Integer> overlay_unique = new HashMap<String,Integer>();
	protected OverlayDrawMap overlay_map = null;
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
	
		
	public OverlayDrawMap getDrawMap() {
		return overlay_map;
	}
		
	public void setVariant(int var) {
		variant = var;
	}
	
	
	public int getVariant() {
		//TODO: This shouldn't return a fixed value!!!
		return variant;
		
//		if ( draw.size() == 0 ) {
//			return 5;
//		}
//
//		return draw.get(0).variant;
	}
	
	
	public void addDrawDescription(int var, String color, int color_rank, String shape) {
		DrawHints dh = new DrawHints();
		dh.variant = var;
		dh.color = color;
		dh.color_rank = color_rank;
		dh.shape = shape;
		draw.add(dh);
	}
	
	// Colormaps, by definition, have to be unique
//	public void addColorMapName(String n,  int value) {
//		//overlay_names.add(n);
//		overlay_unique.put(n, dummy);
//	}
	
	
	public void addDrawMap(SOMData som, String n, int value) {
		if ( overlay_map == null )
			overlay_map = new OverlayDrawMap(som);
		overlay_map.add(n);
		if ( value != 0 )
			overlay_map.addValue(value);
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
		public int color_rank;
		public String shape;
	}
	
}
