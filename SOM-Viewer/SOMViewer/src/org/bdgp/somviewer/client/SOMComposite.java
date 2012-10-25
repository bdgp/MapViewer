package org.bdgp.somviewer.client;

import java.util.Iterator;
import java.util.Vector;

import org.bdgp.somviewer.rpc.ServerService;
import org.bdgp.somviewer.rpc.AbstractLoggingAsyncHandler;
import org.bdgp.somviewer.rpc.data.SOMDataOverlay;
import org.bdgp.somviewer.rpc.data.SOMDataPts;
import org.bdgp.somviewer.rpc.data.SOMList;
import org.bdgp.somviewer.rpc.data.SOMOverlaysAvailable;
import org.vaadin.gwtgraphics.client.DrawingArea;
import org.vaadin.gwtgraphics.client.Group;
import org.vaadin.gwtgraphics.client.VectorObject;
import org.vaadin.gwtgraphics.client.shape.Circle;
import org.vaadin.gwtgraphics.client.shape.Text;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ValueBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.SimpleCheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.dom.client.Element ;
import com.google.gwt.user.client.ui.FlowPanel;
import com.kiouri.sliderbar.client.event.BarValueChangedEvent;
import com.kiouri.sliderbar.client.event.BarValueChangedHandler;

public class SOMComposite extends ResizeComposite {

	protected final static int DEFAULT_CIRC_RAD = 5;

	protected int win_w = 400;
	protected int win_h = 400;
	protected boolean initialScaling = true;
	
	protected String som_src = "test";
	protected DrawingArea canvas;
	protected float canv_x, canv_y;
	
	protected SOMData som = null;
//	protected SOMDataPts pts = null;

	protected Button but_zplus, but_zone, but_zminus;
	protected ListBox avail_somBox;
	protected VerticalPanel catPanel;
	protected VariationSelectWidget varMain;
	protected IntegerBox intMain;
	protected Vector<CheckBox> overlay_cb;
	
	protected boolean showCircles = true, showLabels = true;
	protected float zoom = 1;
	protected float base_zoom = 1;
	
	protected final int ctrlPanelSize = 180;
	protected int canv_gapx = 0, canv_gapy = 0;
	
	protected final static int GRP_TXT = 0;
	protected final static int GRP_CIRC = 1;

	Circle c1;

	
	public SOMComposite(RootLayoutPanel parentPanel) {
		
		DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.PX);
		// mainPanel.setSize("100%", "100%");
		initWidget(mainPanel);
		
		// FlowPanel flowPanel = new FlowPanel();
		// mainPanel.addNorth(flowPanel, 20);

		VerticalPanel ctrlPanel = new VerticalPanel();
		mainPanel.addWest(ctrlPanel,ctrlPanelSize);
		
		Label lblNewLabel_2 = new Label("Select:");
		ctrlPanel.add(lblNewLabel_2);
		
		avail_somBox = new ListBox();
		ctrlPanel.add(avail_somBox);
		avail_somBox.setVisibleItemCount(3);
		avail_somBox.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				int selected_map = avail_somBox.getSelectedIndex();
				getSOM(avail_somBox.getValue(selected_map));
			}
		});
		// avail_somBox.addItem("test");
		
		HorizontalPanel zoomPanel = new HorizontalPanel();
		ctrlPanel.add(zoomPanel);

		Label lblNewLabel_1 = new Label("Zoom:");
		zoomPanel.add(lblNewLabel_1);
		
		but_zplus = new Button("+");
		zoomPanel.add(but_zplus);
		but_zplus.addClickHandler(new ZoomHandler());
		
		but_zone = new Button("1:1");
		zoomPanel.add(but_zone);
		but_zone.addClickHandler(new ZoomHandler());
		
		but_zminus = new Button("-");
		zoomPanel.add(but_zminus);
		but_zminus.addClickHandler(new ZoomHandler());
		
		//ScrollPanel scrollCatPanel = new ScrollPanel();
		ScrollPanel scrollCatPanel = new ScrollPanel();
		ctrlPanel.add(scrollCatPanel);
		
		catPanel = new VerticalPanel();
		scrollCatPanel.setWidget(catPanel);
		catPanel.setSize("100%", "100%");
						
		ScrollPanel scrollCanvasPanel = new ScrollPanel();
		scrollCanvasPanel.setAlwaysShowScrollBars(false);
		// scrollCanvasPanel.setSize("428px", "417px");
		mainPanel.add(scrollCanvasPanel);
		//Element container = mainPanel.getWidgetContainerElement(scrollCanvasPanel);
		//minx = container.getOffsetWidth();
		//miny = container.getOffsetHeight();
		// minx = mainPanel.getOffsetWidth() - 100;
		// miny = mainPanel.getOffsetHeight();
		Window.enableScrolling(false);
		win_w = Window.getClientWidth() - ctrlPanelSize;
		win_h = Window.getClientHeight();
		
		// minx = mainPanel.
		
		canvas = new DrawingArea(win_w, win_h);
		scrollCanvasPanel.add(canvas);
		
		// Calculate spaces at boundary with some dummy text
		Text t = new Text(win_w/2, win_h/2, "CG888888");
		int fw = t.getTextWidth() / 2;
		int fh = t.getTextHeight() / 2;
		canv_gapx = fw;
		canv_gapy = fh > DEFAULT_CIRC_RAD ? fh : DEFAULT_CIRC_RAD;
		
		t = new Text(win_w/2, win_h/2, "Select SOM from list");
		canvas.add(t);

		getSOMlist();
	}

	
	protected void getSOMlist() {
		ServerService.getInstance().getSOMmaps(new SomMapUpdater());
	}
	
	
	protected void getSOM(String src) {
		som_src = src;
		
		ServerService.getInstance().getSOMDataPts(som_src, new SomUpdater());
	}
	
	
	protected void setMaps(Vector<String> maps) {
		for ( String m : maps ) {
			avail_somBox.addItem(m);
		}
	}
	
	
	protected void set(SOMDataPts data) {
		SOMDataPts pts = data;

		if (pts == null) {
			return;
		}

		som = new SOMData(pts);
		
		// Adjust to current window size
		win_w = Window.getClientWidth() - ctrlPanelSize;
		win_h = Window.getClientHeight();

		// find max/min x/y values for setting zoom & canvas
		float x = -1, y = -1;
		for ( int i = 0; i < pts.id.length; i++ ) {
			if ( pts.x[i] > x )
				x = pts.x[i];
			if ( pts.y[i] > y )
				y = pts.y[i];
		}
				
		float zx = (win_w - 2 * canv_gapx) / x;
		float zy = (win_h - 2 * canv_gapy) / y;
		base_zoom = zx > zy ? zy : zx;
		zoom = base_zoom;

		canv_x = x + 2 * canv_gapx / zoom;
		canv_y = y + 2 * canv_gapy / zoom;
		
		CategoryComposite cat = new CategoryComposite(this,som);
		catPanel.clear();
		catPanel.add(cat);
		cat.activate(true);

		draw();
	}
	
	
	protected void draw() {
		
		boolean text_tofg = false;
		
		if (som == null) {
			return;
		}
		
		som.setZoom(zoom);		
		Text tz = new Text(100, 100, "Zoom = " + zoom);
		canvas.add(tz);
		
		float fcanvx = canv_x * zoom;
		float fcanvy = canv_y * zoom;
		int acanvx = (int) (fcanvx > win_w ? fcanvx : win_w);
		int acanvy = (int) (fcanvy > win_h ? fcanvy : win_h);

		canvas.setHeight(acanvy);
		canvas.setWidth(acanvx);
		
		Group grp_txt = som.getDataCanvasGroup(GRP_TXT);
		Group grp_circ = null;
		
		// Text is really slow to draw, everything else is quick
		// Thus, if we encounter text, keep it on the canvas instead of clearing the whole thing.
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
						Circle c = new Circle(x, y, DEFAULT_CIRC_RAD);
						c.setFillColor("fuchsia");
						c.setFillOpacity(0.5);
						c.setStrokeWidth(0);
						c.setStrokeColor("white");
						ngrp_circ.add(c);
					}
					if ( showLabels == true && grp_txt == null ) {
						Text t = new Text(x, y, som_xy.name);
						t.setFontFamily("Arial");
						t.setFontSize(10);
						t.setStrokeWidth(0);
						t.setFillColor("black");
						int fw = t.getTextWidth() / 2;
						int fh = t.getTextHeight() / 2;
						t.setX(x - fw);
						t.setY(y + fh);
						ngrp_txt.add(t);
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
	
//	public void onResize() {
//		super.onResize();
//		
//		win_w = Window.getClientWidth() - ctrlPanelSize;
//		win_h = Window.getClientHeight();
//		draw();
//	}
	
	
	
	
	
	public class SomUpdater extends AbstractLoggingAsyncHandler {
		
		public void handleFailure(Throwable caught) {
			
		}
		
		public void handleSuccess(Object result) {
			SOMDataPts data = (SOMDataPts) result;
			
			if ( data.queryResult != null ) {
				setLogEntry("Requested " + data.requested + ", ERROR " + data.queryResult);
				set(null);
			} else {
				setLogEntry("Requested " + data.requested + ", received " + data.map + " with " + data.id.length + " entries");
				set(data);
			}
			
			
		}
	}
	
	
	public class SomMapUpdater extends AbstractLoggingAsyncHandler {
		
		public void handleFailure(Throwable caught) {
			
		}
		
		public void handleSuccess(Object result) {
			SOMList data = (SOMList) result;
			
			if ( data.queryResult != null ) {
				setLogEntry("Available SOM maps: ERROR " + data.queryResult);
			} else {
				setLogEntry("Available SOM maps: Received " + data.entries.size());
				setMaps(data.entries);
			}
			
			
		}
		
	}
	

	public class ZoomHandler implements ClickHandler {
	      public void onClick(ClickEvent event) {
	          Button bzoom = ((Button) event.getSource());
	          if ( bzoom == but_zplus ) {
	        	  zoom *= 1.5;
	          }
	          else if (bzoom == but_zminus ) {
	        	  zoom *= 0.75;
	          }
	          else {
	        	  zoom = base_zoom;
	          }
	          if ( som != null )
	        	  som.setZoom(zoom);
	          draw();
	        }
	}
		
}
