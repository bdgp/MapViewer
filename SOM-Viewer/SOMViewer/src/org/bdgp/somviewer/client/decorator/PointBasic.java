package org.bdgp.somviewer.client.decorator;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.bdgp.somviewer.client.OverlayDrawMap;
import org.vaadin.gwtgraphics.client.Group;
import org.vaadin.gwtgraphics.client.VectorObject;
import org.vaadin.gwtgraphics.client.shape.Circle;
import org.vaadin.gwtgraphics.client.shape.Text;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author erwin
 * This one is the base class for many PointDecorators
 * It does everything but not very well, should be overridden when possible
 */
public class PointBasic implements PointDecorator {

	protected int uuid = 12345;
	protected final static int MARKER_CIRC_RAD = 2;
	protected final static int OVERLAY_CIRC_RAD = 7;
	
	protected ExtendedInfoHandler info_handler;
	protected OverlayDrawMap colormap;
	protected ColorRank colrank;
	protected HashMap<Integer,String> contents;
	protected String label;
	protected Integer id;
	protected int x,y;
	protected int view_w, view_h;
	protected boolean label_drawn = false;
	
	private Timer cleanupDelayTimer = null;
	private final static int CLEANUP_DELAY = 10000;
	
	public PointBasic(OverlayDrawMap colormap, ColorRank col_rank) {
		this.colormap = colormap;
		this.colrank = col_rank;
	}

	public void setPoint(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	
	public void setInfo(Integer id, String name, HashMap<Integer,String> others) {
		this.id = id;
		this.label = name;
		this.contents = others;
	}
	
	
	public void setViewPortSize(int w, int h) {
		this.view_h = h;
		this.view_w = w;
	}
	

	public VectorObject drawLabel(String label, ClickHandler onclick) {

		Text t = new Text(x, y, label);
		t.setFontFamily("Arial");
		t.setFontSize(10);
		t.setStrokeWidth(0);
		t.setFillColor("black");
		int fw = t.getTextWidth() / 2;
		int fh = t.getTextHeight() / 2;
		t.setX(x - fw);
		t.setY(y + fh);
		
		if (onclick != null)
			t.addClickHandler(onclick);
		
		return t;
	}

	public VectorObject drawMarker(boolean showMarker, Vector<String> colors, OverlayDrawMap overlay_map, ClickHandler onclick) {

		if ( colors == null ) {
			if ( showMarker == true )
				return drawCircle(MARKER_CIRC_RAD, 0.5f, "fuchsia");
			else
				return null;
		}

		if ( colors.size() == 1 ) {
			return drawCircle(OVERLAY_CIRC_RAD, 0.9f, "#" + colors.get(1));
		}
		
		Group g = new Group();
		for ( String col : colors ) {
			g.add(drawCircle(OVERLAY_CIRC_RAD, 0.9f, "#" + col));
		}

		return g;		
	}

	
	protected Circle drawCircle(int radius, float opacy, String color) {
		Circle c = new Circle(x, y, radius);
		c.setFillOpacity(opacy);
		c.setStrokeWidth(0);
		c.setStrokeColor("white");	
		c.setFillColor(color);
		return(c);		
	}
	
	
	public void infoQuick(String title, Integer id, int variant ,int x, int y) {
	}

	public void infoLong(Integer id) {
	}

	public int uuid() {
		int ret_uuid = this.uuid;
		return ret_uuid;
	}

	public boolean isDraw() {
		return true;
	}

	public boolean isInfo() {
		return false;
	}
	
	
	protected void addExtendedInfoPruned(VectorObject vo) {
		label_drawn = true;
		if ( vo != null )
			processExtendedInfo(vo);
	}
	
	
	protected void addExtendedInfo(VectorObject vo) {
		if ( label_drawn == true) {
			label_drawn = false;
		}
		processExtendedInfo(vo);
	}
	
	protected void processExtendedInfo(VectorObject vo) {
		
		// Label has been drawn - skip info if that happened
		if ( label_drawn == true ) {
			if ( contents == null || contents.size() == 0 )
				return;
		}
		
		int req_size = 1;
		if ( contents != null ) {
			req_size += contents.size();
		}
				
		ExtendedInfoHandler eih = null;
		
		if ( info_handler != null )
			if ( info_handler.isSame(x, y) ) {
				eih = info_handler;
			} 
		
		if ( eih == null ) {
			eih = new ExtendedInfoHandler(x, y, req_size);
			eih.addItem(label);
			info_handler = eih;
		}
				
		if ( contents != null && contents.size() > 0 ) {
			for ( Map.Entry<Integer, String> entry : contents.entrySet() ) {
				String c = entry.getValue();
				eih.addItem(c);
			}
		}
		vo.addMouseOverHandler(eih);
		vo.addMouseOutHandler(eih);
	}
	
	
	protected class ExtendedInfoHandler implements MouseOverHandler, MouseOutHandler {
		protected Vector<String> ext_info;
		protected HTML html_info;
		protected PopupPanel popup;
		protected int x, y;
		
		public ExtendedInfoHandler(int x, int y, int infoSize) {
			ext_info = new Vector<String>(infoSize);
			this.x = x;
			this.y = y;
		}
		
		public void addItem(String item) {
			ext_info.add(item);
			html_info = null;
		}
		
		
		public boolean isSame(int xc, int yc) {
			if ( xc == x && yc == y )
				return true;
			return false;
		}
		
		
		public void onMouseOver(MouseOverEvent event) {
			if ( ext_info.size() == 0 )
				return;
			
            if ( html_info == null ) {
            	String info = new String();
            	info += ext_info.get(0);
            	for ( int i=1; i < ext_info.size()/2+1; i++ ) {
            		info += "<BR>" + ext_info.get(i);
            	}
            	html_info = new HTML(info);
            }
			if ( popup == null ) {
				popup = new PopupPanel();
				popup.setStyleName("info-PopupPanel");
	            popup.add(html_info); 
			}
            Widget source = (Widget) event.getSource();
            int xc = source.getAbsoluteLeft() + 5;
            int yc = source.getAbsoluteTop() + 5;
            popup.setPopupPosition(xc, yc);
            popup.show();
            
            // Schedule a timer to make sure we won't have any leftover popups
		    cleanupDelayTimer = new Timer() {
		        public void run() {
		        	popup.hide();
		        }
		      };
		      // Schedule the timer to run once in 30 seconds.
		      cleanupDelayTimer.schedule(CLEANUP_DELAY);

		}

		public void onMouseOut(MouseOutEvent event) {
			if ( popup != null ) {
				popup.hide();
			}
		}

		
	}
	

}
