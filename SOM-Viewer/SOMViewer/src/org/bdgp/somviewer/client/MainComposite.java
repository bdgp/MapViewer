package org.bdgp.somviewer.client;

import java.util.Iterator;
import java.util.Vector;

import org.bdgp.somviewer.client.decorator.ColorRank;
import org.bdgp.somviewer.client.decorator.ColorRankBasicCSS;
import org.bdgp.somviewer.client.decorator.DecoratorFactory;
import org.bdgp.somviewer.client.decorator.PointDecorator;
import org.bdgp.somviewer.client.decorator.PointInfo;
import org.bdgp.somviewer.client.decorator.PointMarker;
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
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
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
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Image;

public class MainComposite extends ResizeComposite {

	protected int win_w = 400;
	protected int win_h = 400;
	protected boolean initialScaling = true;
	
	protected String som_src = "test";
	protected DrawingArea canvas;
	protected float canv_x, canv_y;
	
	protected SOMData som = null;

	protected PushButton but_zplus, but_zone, but_zminus;
	protected ListBox avail_somBox;
	protected VerticalPanel catPanel;
	protected VariationSelectWidget varMain;
	protected IntegerBox intMain;
	protected Vector<CheckBox> overlay_cb;
	
	protected CanvasComposite canvPanel;
	
	protected boolean showCircles = true, showLabels = true;
	protected float zoom = 1;
	protected float base_zoom = 1;
	
	protected final int ctrlPanelSize = 200;
	protected final int titlePanelSize = 25;
	protected int canv_gapx = 0, canv_gapy = 0;
	
	protected final static int GRP_TXT = 0;
	protected final static int GRP_CIRC = 1;

	protected final static boolean TEST_CANV = true;
	private HorizontalPanel findPanel;
	private TextBox findBox;
	// private Button btnFind;
	// private Button btnClear;
	private HorizontalPanel horizontalPanel;
	private HorizontalPanel horizontalPanel_1;
	private Grid grid;
	private HTML titleHtml;
	private HTML statusHtml;
	private HTML infoHtml;
	private PushButton btnFind;
	private PushButton btnClear;
	private PushButton btnPrint;
	
	public MainComposite(RootLayoutPanel parentPanel) {
		
		DockLayoutPanel mainPanel = new DockLayoutPanel(Unit.PX);
		// mainPanel.setSize("100%", "100%");
		initWidget(mainPanel);
		
		grid = new Grid(1, 5);
		grid.setStyleName("titleBar");
		mainPanel.addNorth(grid, titlePanelSize);
		grid.setWidth("100%");
		
		titleHtml = new HTML("Map viewer:", true);
		grid.setWidget(0, 0, titleHtml);
		
		avail_somBox = new ListBox();
		avail_somBox.addItem("Select map from list...");
		grid.setWidget(0, 1, avail_somBox);
		avail_somBox.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				int selected_map = avail_somBox.getSelectedIndex();
				if ( selected_map > 0 )
					getSOM(avail_somBox.getValue(selected_map));
			}
		});
		
		btnPrint = new PushButton(new Image("images/print_map-normal.png"));
		btnPrint.setStyleName("imageButton");
		btnPrint.getDownFace().setImage(new Image("images/print_map-pressed.png"));
		btnPrint.getUpHoveringFace().setImage(new Image("images/print_map-pressed.png"));
		btnPrint.getUpFace().setImage(new Image("images/print_map-normal.png"));
		grid.setWidget(0, 2, btnPrint);
		btnPrint.addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent sender) {
				Print.it(canvPanel);
			}
		});
		
		
		statusHtml = new HTML();
		grid.setWidget(0, 3, statusHtml);
		statusHtml.setWidth("200px");
		
		infoHtml = new HTML();
		grid.setWidget(0, 4, infoHtml);
		infoHtml.setWidth("200px");
		
		// FlowPanel flowPanel = new FlowPanel();
		// mainPanel.addNorth(flowPanel, 20);

		
		ScrollPanel scrollCtrlPanel = new ScrollPanel();
		mainPanel.addWest(scrollCtrlPanel,ctrlPanelSize);
		
		VerticalPanel ctrlPanel = new VerticalPanel();
		// mainPanel.addWest(ctrlPanel,ctrlPanelSize);
		ctrlPanel.setWidth("100%");
		scrollCtrlPanel.add(ctrlPanel);
		
		//Label lblNewLabel_2 = new Label("Select:");
		//ctrlPanel.add(lblNewLabel_2);
		HTML navTitle = new HTML("<b>Navigation</b>");
		navTitle.setStyleName("ctrlTitle");
		ctrlPanel.add(navTitle);
		// ctrlPanel.add(new HTML("<b>Navigation</b>"));
	
		
		//
		// Search field
		findPanel = new HorizontalPanel();
		findPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		ctrlPanel.add(findPanel);
		findPanel.setWidth("100%");
		
		findBox = new TextBox();
		findBox.setStyleName("find-TextBox");
		findPanel.add(findBox);
		findBox.setSize("60px", "12px");
		
		horizontalPanel = new HorizontalPanel();
		horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		horizontalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		findPanel.add(horizontalPanel);
		horizontalPanel.setHeight("24px");
		findPanel.setCellHorizontalAlignment(horizontalPanel, HasHorizontalAlignment.ALIGN_RIGHT);
		
//		btnFind = new Button("Find");
//		horizontalPanel.add(btnFind);
//		btnClear = new Button("Clear");
//		horizontalPanel.add(btnClear);

		
		btnFind = new PushButton(new Image("images/find-normal.png"));
		btnFind.setStyleName("imageButton");
		btnFind.getDownFace().setImage(new Image("images/find-pressed.png"));
		btnFind.getUpHoveringFace().setImage(new Image("images/find-pressed.png"));
		btnFind.getUpFace().setImage(new Image("images/find-normal.png"));
		horizontalPanel.add(btnFind);
//		btnFind.setSize("45px", "24px");
		
		btnClear = new PushButton(new Image("images/clear-normal.png"));
		btnClear.setStyleName("imageButton");
		btnClear.getDownFace().setImage(new Image("images/clear-pressed.png"));
		btnClear.getUpHoveringFace().setImage(new Image("images/clear-pressed.png"));
		btnClear.getUpFace().setImage(new Image("images/clear-normal.png"));
		horizontalPanel.add(btnClear);
//		btnClear.setSize("45px", "24px");
		
		btnClear.addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent sender) {
				findBox.setText("");
				canvPanel.unsetMark();
			}
		});
		
		btnFind.addClickHandler( new ClickHandler() {
			public void onClick(ClickEvent sender) {
				setFromSearchBox();
			}
		});
		
		findBox.addKeyUpHandler(new KeyUpHandler() {
		      public void onKeyUp(KeyUpEvent event) {
		        if ( event.getNativeKeyCode() == KeyCodes.KEY_ENTER ) {
		        	setFromSearchBox();
		        }
		      }
		} );

		
		// avail_somBox.addItem("test");
		
		HorizontalPanel zoomPanel = new HorizontalPanel();
		zoomPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		ctrlPanel.add(zoomPanel);
		zoomPanel.setWidth("100%");

		Label lblNewLabel_1 = new Label("Zoom:");
		zoomPanel.add(lblNewLabel_1);
		
		horizontalPanel_1 = new HorizontalPanel();
		horizontalPanel_1.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		horizontalPanel_1.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		zoomPanel.add(horizontalPanel_1);
		zoomPanel.setCellVerticalAlignment(horizontalPanel_1, HasVerticalAlignment.ALIGN_MIDDLE);
		zoomPanel.setCellHorizontalAlignment(horizontalPanel_1, HasHorizontalAlignment.ALIGN_RIGHT);
		
//		but_zplus = new Button("+");				
//		but_zone = new Button("1:1");
//		but_zminus = new Button("-");		

		but_zplus = new PushButton(new Image("images/plus-normal.png"));
		but_zplus.setStyleName("imageButton");
		but_zplus.getDownFace().setImage(new Image("images/plus-pressed.png"));
		but_zplus.getUpHoveringFace().setImage(new Image("images/plus-pressed.png"));
		but_zplus.getUpFace().setImage(new Image("images/plus-normal.png"));
		but_zone = new PushButton(new Image("images/one-normal.png"));
		but_zone.setStyleName("imageButton");
		but_zone.getDownFace().setImage(new Image("images/one-pressed.png"));
		but_zone.getUpHoveringFace().setImage(new Image("images/one-pressed.png"));
		but_zone.getUpFace().setImage(new Image("images/one-normal.png"));
		but_zminus = new PushButton(new Image("images/minus-normal.png"));
		but_zminus.setStyleName("imageButton");
		but_zminus.getDownFace().setImage(new Image("images/minus-pressed.png"));
		but_zminus.getUpHoveringFace().setImage(new Image("images/minus-pressed.png"));
		but_zminus.getUpFace().setImage(new Image("images/minus-normal.png"));

		
		horizontalPanel_1.add(but_zplus);
		horizontalPanel_1.add(but_zone);		
		horizontalPanel_1.add(but_zminus);
		but_zminus.addClickHandler(new ZoomHandler());
		but_zone.addClickHandler(new ZoomHandler());
		but_zplus.addClickHandler(new ZoomHandler());
		
//		ScrollPanel scrollCatPanel = new ScrollPanel();
//		ctrlPanel.add(scrollCatPanel);
		
		catPanel = new VerticalPanel();
		ctrlPanel.add(catPanel);
//		scrollCatPanel.setWidget(catPanel);
		//catPanel.setSize("100%", "100%");
		
		canvPanel = new CanvasComposite(ctrlPanelSize, titlePanelSize);
		mainPanel.add(canvPanel);
		
		Feedback.getInstance().setInfoWidget(infoHtml);
		Feedback.getInstance().setStatusWidget(statusHtml);
		
		getSOMlist();
	}

	
	protected void getSOMlist() {
		Feedback.getInstance().rpcRequested(null);
		ServerService.getInstance().getSOMmaps(new SomMapUpdater());
	}
	
	
	protected void getSOM(String src) {
		som_src = src;
		
		Feedback.getInstance().rpcRequested(null);
		
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

		// som.setDecorators(df);
		
		canvPanel.updateCanvas(false);
		canvPanel.setSOM(som);
		
		// Add all available Overlays to PointMarker
		OverlayDrawMap om_all = new OverlayDrawMap(som);
		om_all.addAll();
		ColorRank rank = new ColorRankBasicCSS();

		canvPanel.addDecorator(new PointMarker(om_all, rank));

		// Use DecoratorFactory for the decorators
		DecoratorFactory df = new DecoratorFactory(som, rank);
		Iterator<PointDecorator> it_df = df.getDecorators();
		while ( it_df.hasNext() )
			canvPanel.addDecorator(it_df.next());
		canvPanel.setDecorator(new PointInfo());
		
				
		CategoryComposite cat = new CategoryComposite(canvPanel,som);
		catPanel.clear();
		catPanel.add(cat);
		cat.setColorRank(rank);

		canvPanel.updateCanvas(true);
	}
	
	
	protected void setFromSearchBox() {
		String search_text = findBox.getText();
		
		if ( som == null )
			return;
		
		if ( search_text.length() == 0 ) {
			canvPanel.unsetMark();
			return;
		}
		
		SOMpt pt = som.search(search_text);
		if ( pt != null ) {
			canvPanel.unsetMark();
			canvPanel.setMark(pt);
		}
	}

	
	
	public class SomUpdater extends AbstractLoggingAsyncHandler {
		
		public void handleFailure(Throwable caught) {
			
		}
		
		public void handleSuccess(Object result) {
			SOMDataPts data = (SOMDataPts) result;
						
			if ( data.queryResult != null ) {
				Feedback.getInstance().rpcError(data.queryResult);
				setLogEntry("Requested " + data.requested + ", ERROR " + data.queryResult);
				set(null);
			} else {
				Feedback.getInstance().rpcReceived("Got " + data.map + " with " + data.id.length + " entries");
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
				Feedback.getInstance().rpcError(data.queryResult);
				setLogEntry("Available SOM maps: ERROR " + data.queryResult);
			} else {
				Feedback.getInstance().rpcReceived("Received " + data.entries.size());
				setLogEntry("Available SOM maps: Received " + data.entries.size());
				setMaps(data.entries);
			}
			
			
		}
		
	}
	

	public class ZoomHandler implements ClickHandler {
	      public void onClick(ClickEvent event) {
	          PushButton bzoom = ((PushButton) event.getSource());
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
