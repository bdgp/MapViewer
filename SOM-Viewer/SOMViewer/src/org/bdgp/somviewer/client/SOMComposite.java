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

	protected Button but_zplus, but_zone, but_zminus;
	protected ListBox avail_somBox;
	protected VerticalPanel catPanel;
	protected VariationSelectWidget varMain;
	protected IntegerBox intMain;
	protected Vector<CheckBox> overlay_cb;
	
	protected CanvasComposite canvPanel;
	
	protected boolean showCircles = true, showLabels = true;
	protected float zoom = 1;
	protected float base_zoom = 1;
	
	protected final int ctrlPanelSize = 180;
	protected int canv_gapx = 0, canv_gapy = 0;
	
	protected final static int GRP_TXT = 0;
	protected final static int GRP_CIRC = 1;

	Circle c1;

	protected final static boolean TEST_CANV = true;
	
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
		
		canvPanel = new CanvasComposite(ctrlPanelSize,0);
		mainPanel.add(canvPanel);
		
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
		
		canvPanel.updateCanvas(false);
		canvPanel.setSOM(som);
				
		CategoryComposite cat = new CategoryComposite(canvPanel,som);
		catPanel.clear();
		catPanel.add(cat);

		canvPanel.updateCanvas(true);
	}
	
	
	
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
	        	  canvPanel.zoomIncrease();
	        	  zoom *= 1.5;
	          }
	          else if (bzoom == but_zminus ) {
	        	  canvPanel.zoomDecrease();
	        	  zoom *= 0.75;
	          }
	          else {
	        	  canvPanel.zoomReset();
	        	  zoom = base_zoom;
	          }
	          if ( som != null )
	        	  som.setZoom(zoom);
	        }
	}
		
}
