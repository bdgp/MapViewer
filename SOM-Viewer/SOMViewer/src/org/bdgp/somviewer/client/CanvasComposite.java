package org.bdgp.somviewer.client;

import java.util.Iterator;
import java.util.Vector;

import org.bdgp.somviewer.client.decorator.PointDecorator;
import org.vaadin.gwtgraphics.client.DrawingArea;
import org.vaadin.gwtgraphics.client.Group;
import org.vaadin.gwtgraphics.client.VectorObject;
import org.vaadin.gwtgraphics.client.shape.Circle;
import org.vaadin.gwtgraphics.client.shape.Rectangle;
import org.vaadin.gwtgraphics.client.shape.Text;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ScrollPanel;

public class CanvasComposite extends Composite {

	protected final static int DEFAULT_CIRC_RAD = 5;
	protected final static float DEFAULT_ZOOM_FACTOR = 1.5f;
	protected final static int DEFAULT_TREASURE_AREA = 10;

	protected int xPanelSize = 180;
	protected int yPanelSize = 0;
	protected int canv_gapx = 0, canv_gapy = 0;

	protected int win_w = 400;
	protected int win_h = 400;
	protected boolean initialScaling = true;
	
	protected DrawingArea canvas;

	protected Vector<PointDecorator> pt_draw = new Vector<PointDecorator>(3);
	protected PointDecorator pt_info = null;
	
	protected SOMpt treasure = null;
	protected Rectangle rec_treasure = null;
	
	protected float canv_x, canv_y;
	protected float zoom = 1;
	protected float base_zoom = 1;
	protected boolean updCanvas = true;

	protected boolean showCircles = true, showLabels = true;
	
	protected int commonVariant = 5;
	
	protected final static int GRP_TXT = 0;
	protected final static int GRP_CIRC = 1;

	Circle c1;

	protected SOMData som = null;

	
	public CanvasComposite(int reduce_x, int reduce_y) {
		
		xPanelSize = reduce_x;
		yPanelSize = 0;
		
		ScrollPanel scrollCanvasPanel = new ScrollPanel();
		scrollCanvasPanel.setAlwaysShowScrollBars(false);

		initWidget(scrollCanvasPanel);

		win_w = Window.getClientWidth() - xPanelSize;
		win_h = Window.getClientHeight()- yPanelSize;
		
		// minx = mainPanel.
		
		canvas = new DrawingArea(win_w, win_h);
		scrollCanvasPanel.add(canvas);
		
		// Calculate spaces at boundary with some dummy text
		Text t = new Text(win_w/2, win_h/2, "CG888888");
		t.setFontFamily("Arial");
		t.setFontSize(10);
		t.setStrokeWidth(0);
		t.setFillColor("black");
		int fw = t.getTextWidth() / 2;
		int fh = t.getTextHeight() / 2;
		canv_gapx = fw;
		canv_gapy = fh > DEFAULT_CIRC_RAD ? fh : DEFAULT_CIRC_RAD;
		
		t = new Text(win_w/2, win_h/2, "Select map from list");
		t.setFontSize(36);
		fw = t.getTextWidth() / 2;
		fh = t.getTextHeight() / 2;
		t.setX(win_w/2 - fw);
		t.setY(win_h/2 + fh);
		canvas.add(t);
		
	}

	
	public void setSOM(SOMData som) {

		this.som = som;
		
		resetDecorators();
//		addDecorator(new PointMarker(som.getColorMap()));
//		addDecorator(new PointVenn(som.getColorMap()));
//		setDecorator(new PointInfo());

		win_w = Window.getClientWidth() - xPanelSize;
		win_h = Window.getClientHeight()- yPanelSize;
				
		float maxx = -1, maxy = -1;
		
		// find max/min x/y values for setting zoom & canvas
		
		Iterator<SOMpt> som_data = som.getData();
		while ( som_data.hasNext() ) {
			SOMpt som_xy = som_data.next();
			float xf = som_xy.getX();
			float yf = som_xy.getY();
			
			if ( xf > maxx )
				maxx = xf;
			if ( yf > maxy )
				maxy = yf;
		}
			
				
		float zx = (win_w - 2 * canv_gapx) / maxx;
		float zy = (win_h - 2 * canv_gapy) / maxy;
		base_zoom = zx > zy ? zy : zx;
		zoom = base_zoom;

		canv_x = maxx + 2 * canv_gapx / zoom;
		canv_y = maxy + 2 * canv_gapy / zoom;
	}

	
	public void setDecorator(PointDecorator pd) {
		if ( pd == null )
			return;
		
		pd.setViewPortSize(win_w, win_h);
		
		if ( pd.isInfo() )
			pt_info = pd;
		else {
			pt_draw.clear();
			pt_draw.add(pd);
		}
	}
	
	
	public void addDecorator(PointDecorator pd) {
		
		// This class should add the pd and remove any Decorator with the same uuid
		// The current uuid design doesn't appear to work in GWT
		// -> just add them without checking and make sure externally that everything is cleared
		
		if ( pd == null )
			return;
		
		pd.setViewPortSize(win_w, win_h);
		
		if ( pd.isInfo() ) {
			// at this time we can only have one info
			// cannot see how having more than one is useful
			pt_info = pd;
		} else {
			pt_draw.add(pd);
		}
		
		//  old uuid checking code:
//		Vector<PointDecorator> rm = null;		
//		// Make sure list contains only one object with uuid
//		if ( pt_draw.size() > 0 ) {
//			for ( PointDecorator p : pt_draw ) {
//				if ( p.uuid() == pd.uuid() ) {
//					if ( rm == null )
//						rm = new Vector<PointDecorator>();
//					rm.add(p);
//				}
//			}
//		}
//		
//		pt_draw.add(pd);
//		
//		if ( rm != null )
//			for (PointDecorator p : rm) {
//				pt_draw.remove(p);
//			}
		
	}
	
	// TODO: This will crash
	public void rmDecorator(int uuid) {
		if ( pt_draw.size() > 0 ) {
			for ( PointDecorator p : pt_draw ) {
				if ( p.uuid() == uuid ) {
					pt_draw.remove(p);
				}
			}
		}		
	}
	
	
	public void resetDecorators() {
		pt_draw.clear();
	}
	
	
	public void setMarkers(boolean mk) {
		showCircles = mk;
	}

	
	public void setLabels(boolean lab) {
		showLabels = lab;
	}

	public void updateCanvas(boolean act) {
		updCanvas = act;
		if ( updCanvas == true )
			draw();
	}
	
	
	public void setCommonVariant(int var) {
		commonVariant = var;
	}
	
	
	public void setMark(SOMpt pt) {
		this.treasure = pt;
		drawTreasure();
	}
	
	public void unsetMark() {
		if ( this.rec_treasure != null ) {
			canvas.remove(rec_treasure);
			rec_treasure = null;
		}
		this.treasure = null;
	}
	
	
	protected void drawTreasure() {
		if ( treasure != null ) {
			
			float xf = treasure.getX() * zoom - DEFAULT_TREASURE_AREA;
			float yf = treasure.getY() * zoom - DEFAULT_TREASURE_AREA;
			
			rec_treasure = new Rectangle((int) xf, (int) yf, DEFAULT_TREASURE_AREA*2, DEFAULT_TREASURE_AREA*2);
			rec_treasure.setFillOpacity(0);
			rec_treasure.setStrokeWidth(3);
			rec_treasure.setStrokeColor("red");	
			rec_treasure.setFillColor("white");

			canvas.add(rec_treasure);
		}
	}
	
	
	public void zoomReset() {
		doZoom(true,0);
	}
	
	public void zoomIncrease() {
		doZoom(false,DEFAULT_ZOOM_FACTOR);
	}
	
	public void zoomDecrease() {
		doZoom(false,-DEFAULT_ZOOM_FACTOR);
	}
	
	
	protected void doZoom(boolean one, float factor) {
		if ( one == true )
			zoom = base_zoom;
		else if ( factor < 0 )
			zoom /= Math.abs(factor);
		else
			zoom *= factor;
		
		if ( som == null )
			return;
		
		som.setZoom(zoom);		
		// Text tz = new Text(100, 100, "Zoom = " + zoom);
		// canvas.add(tz);
		
		float fcanvx = canv_x * zoom;
		float fcanvy = canv_y * zoom;
		int acanvx = (int) (fcanvx > win_w ? fcanvx : win_w);
		int acanvy = (int) (fcanvy > win_h ? fcanvy : win_h);

		canvas.setHeight(acanvy);
		canvas.setWidth(acanvx);
		
		draw();
	}
	
	
	
	
	public void draw() {
		
		if ( updCanvas == false )
			return;
		
		if (som == null) {
			return;
		}
		
		boolean text_tofg = false;
		Group grp_txt = som.getDataCanvasGroup(GRP_TXT);
		Group grp_circ = null;
		
		// Text is really slow to draw, everything else is quick
		// Thus, if we encounter text, keep it on the canvas instead of clearing the whole thing.
		// Note: this only works if all the other markers are kept in overlay groups or a single group
		if ( grp_txt != null && showLabels ) {
			for ( int i=0; i < canvas.getVectorObjectCount(); i++ ) {
				VectorObject canv_obj = canvas.getVectorObject(i);
				if ( canv_obj == grp_txt ) {
					text_tofg = true;
				} else {
					canvas.remove(canv_obj);
				}
			}
		}
		// Text object not found or not valid - clear it and continue normally. 
		if ( text_tofg == false ) {
			canvas.clear();
			grp_txt = null;
		}
				
		if ( pt_draw.size() == 0 ) {
			// old fallback routine - depreciated
			draw_fallback(grp_txt,grp_circ,text_tofg);
			return;
		}
		
		Feedback.getInstance().startDraw();
		
		int x,y;
		float xf, yf;
		boolean doLabels = false;

		if ( grp_txt == null && showLabels == true ) {
			grp_txt = new Group();
			doLabels = true;
		}

		// TODO: Make sure there is anything to draw 
		Iterator<SOMpt> som_data = som.getAllData();
		// this makes sure there is one group to add all the overlays
		Group overlay_markers = new Group();
		
		while ( som_data.hasNext() ) {
			SOMpt som_pt = som_data.next();
			xf = som_pt.getX() * zoom;
			yf = som_pt.getY() * zoom;
			x = (int) xf;
			y = (int) yf;

			for ( PointDecorator p : pt_draw ) {
				p.setPoint(x, y);
				p.setInfo(som_pt.getId(), som_pt.getName(), som_pt.getIdenticalPt());
				ClickHandler onclick = null;
				if ( pt_info != null ) {
					onclick = new InfoClick(som_pt.getName(), som_pt.getId(), som_pt.getVariant());
				}
				if ( doLabels == true) {
					VectorObject txt = p.drawLabel(som_pt.getName(), onclick);
					if ( txt != null )
						grp_txt.add(txt);

				}
				VectorObject m = p.drawMarker(showCircles, som_pt.getColorMapNames(), onclick);
				if ( m != null ) {
					overlay_markers.add(m);
				}
			}
		}

		if ( overlay_markers.getVectorObjectCount() > 0 ) {
			canvas.add(overlay_markers);
		}
		
		if ( doLabels == true ) {
			som.setDataCanvasGroup(grp_txt, GRP_TXT, zoom);
		}

		if ( grp_txt != null ) {
			if ( text_tofg == true )
				canvas.bringToFront(grp_txt);
			else
				canvas.add(grp_txt);
		}
		drawTreasure();
		
		Feedback.getInstance().finishDraw();
		
	}
	
	
	protected void draw_fallback(Group grp_txt, Group grp_circ, boolean text_tofg) {
		if ( showCircles || showLabels ) {
			
			if ( som.existsCanvasGroup() == true ) {
				grp_txt = som.getDataCanvasGroup(GRP_TXT);
				grp_circ = som.getDataCanvasGroup(GRP_CIRC);
			}
			
			
			// See if we have a cached group
			if ( (grp_txt == null && showLabels) || (grp_circ == null && showCircles) ) {
				
				int x,y;
				Group ngrp_txt = new Group();
				Group ngrp_circ = new Group();
				
				Iterator<SOMpt> som_data = som.getData();

				while ( som_data.hasNext() ) {
					SOMpt som_xy = som_data.next();
					float xf = som_xy.getX() * zoom;
					float yf = som_xy.getY() * zoom;
					x = (int) xf;
					y = (int) yf;

					if ( showCircles == true && grp_circ == null ) {
						ngrp_circ.add(drawPtMarker(x,y));
					}
					if ( showLabels == true && grp_txt == null ) {
						ngrp_txt.add(drawPtText(x,y,som_xy.name));
					}

				}
				if ( showCircles == true && grp_circ == null ) {
					som.setDataCanvasGroup(ngrp_circ, GRP_CIRC, zoom);
					grp_circ = ngrp_circ;
				}
				if ( showLabels == true && grp_txt == null ) {
					som.setDataCanvasGroup(grp_txt, GRP_TXT, zoom);
					grp_txt = ngrp_txt;
				}
			}
			
		}
		
		
		OverlayIterator som_overlay = som.getOverlays();
		
		while (som_overlay.hasNext()) {
		
			Iterator<SOMpt> som_odata = som_overlay.next();
			
			// Only overwrite groups if not active; we don't want to have an empty/stale group
			if ( som_overlay.isActive() == false )
				continue;
			
			Group grp_overlay = som_overlay.getGroup();
			
			if ( grp_overlay == null ) {
				int x, y;
				String color = som_overlay.color();
				grp_overlay = new Group();
				
				while ( som_odata.hasNext() ) {
					SOMpt som_xy = som_odata.next();
					float xf = som_xy.getX() * zoom;
					float yf = som_xy.getY() * zoom;
					x = (int) xf;
					y = (int) yf;
					
					Circle c = new Circle(x, y, DEFAULT_CIRC_RAD);
					c.setFillColor("#" + color);
					c.setFillOpacity(0.9);
					c.setStrokeWidth(0);
					c.setStrokeColor("white");
					grp_overlay.add(c);				
				}
				som_overlay.setGroup(grp_overlay);
			}
			canvas.add(grp_overlay);
		}
		
		
		// Order: 
		// 1) Overlays
		// 2) Circles
		// 3) Text
		if ( grp_circ != null )
			canvas.add(grp_circ);
		if ( grp_txt != null ) {
			if ( text_tofg == true )
				canvas.bringToFront(grp_txt);
			else
				canvas.add(grp_txt);
		}

		
//		canvas.addMouseMoveHandler(new MouseMoveHandler() {
//			  public void onMouseMove(MouseMoveEvent event) {
//				  if ( c1 != null ) {
//					  c1.setX(event.getX());
//					  c1.setY(event.getY());
//				  }
//			  }
//			});
		
		
	}
	
	
	protected VectorObject drawPtText(int x, int y, String text) {

		Text t = new Text(x, y, text);
		t.setFontFamily("Arial");
		t.setFontSize(10);
		t.setStrokeWidth(0);
		t.setFillColor("black");
		int fw = t.getTextWidth() / 2;
		int fh = t.getTextHeight() / 2;
		t.setX(x - fw);
		t.setY(y + fh);
		
		return t;
	}
	
	protected VectorObject drawPtMarker(int x, int y) {
		Circle c = new Circle(x, y, DEFAULT_CIRC_RAD);
		c.setFillColor("fuchsia");
		c.setFillOpacity(0.5);
		c.setStrokeWidth(0);
		c.setStrokeColor("white");	
		
		return(c);
	}

	
	protected class InfoClick implements ClickHandler {
		protected Integer id;
		protected int variant;
		protected String title;
		
		public InfoClick(String title, Integer id, int variant) {
			this.title = title;
			this.id = id;
			this.variant = variant;
		}
		
		public void onClick(ClickEvent event) {
			if ( pt_info != null ) {
				pt_info.infoQuick(title, id, variant, event.getClientX(), event.getClientY());
			}
			
		}
		
	}
	
	
}
