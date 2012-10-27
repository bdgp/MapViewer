package org.bdgp.somviewer.client;

import java.util.Vector;

import org.bdgp.somviewer.rpc.AbstractLoggingAsyncHandler;
import org.bdgp.somviewer.rpc.ServerService;
import org.bdgp.somviewer.rpc.data.SOMDataOverlay;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.kiouri.sliderbar.client.event.BarValueChangedEvent;
import com.kiouri.sliderbar.client.event.BarValueChangedHandler;

public class CategoryComposite extends Composite {

	protected SOMData som;
	protected CanvasComposite canvasOwner;
	protected VerticalPanel catPanel  = new VerticalPanel();

	protected boolean showCircles = true, showLabels = true;

	
	public CategoryComposite(CanvasComposite canvasOwner, SOMData som) {
		this.som = som;
		this.canvasOwner = canvasOwner;
		
		initWidget(catPanel);
		populatePanel();	
	}
	
	
	protected void populatePanel() {

		// catPanel.clear();

		catPanel.add(new HTML("<b>Overlays:</b>"));
		
		CheckBox circCheckBox = new CheckBox();
		catPanel.add(circCheckBox);
		circCheckBox.setText("Show positions");
		circCheckBox.setValue(showCircles);
		circCheckBox.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				boolean checked = ((CheckBox) event.getSource()).getValue();
				showCircles = checked;
				canvasOwner.setMarkers(checked);
				if ( som != null )
					som.invalidateCanvasGroup();
				canvasOwner.draw();
			}
		});
		
		
		CheckBox labelsCheckBox = new CheckBox();
		catPanel.add(labelsCheckBox);
		labelsCheckBox.setText("Show names");
		labelsCheckBox.setValue(showLabels);
		labelsCheckBox.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				boolean checked = ((CheckBox) event.getSource()).getValue();
				showLabels = checked;
				canvasOwner.setLabels(checked);
				if ( som != null )
					som.invalidateCanvasGroup();
				canvasOwner.draw();
			}
		});				

		Label lblNewLabel = new Label("Organ system");
		catPanel.add(lblNewLabel);

		DrawSync dsync = new DrawSync(canvasOwner);
		
		HorizontalPanel global_varPanel = new HorizontalPanel();
		catPanel.add(global_varPanel);
		
		VariationSelectWidget globalSlider = new VariationSelectWidget(som.getMaxVariant(),"60px",true);
		global_varPanel.add(globalSlider.asWidget());
		globalSlider.setValue(som.getMaxVariant());
		
		IntegerBox globalInt = new IntegerBox();
		global_varPanel.add(globalInt);
		globalInt.setValue(som.getMaxVariant());
		globalInt.setMaxLength(2);
		globalInt.setWidth("10px");
		
		VarBarHandler globalSliderHandler = new VarBarHandler(null, dsync, null, globalInt, globalSlider);
		VarBoxHandler globalBoxHandler = new VarBoxHandler(null, dsync, null, globalInt, globalSlider);
		globalSlider.addBarValueChangedHandler(globalSliderHandler);
		globalInt.addChangeHandler(globalBoxHandler);
		
		Vector<String> ovnames = som.getOverlayNames();				
		Grid osGrid = new Grid(ovnames.size(), 3);
		catPanel.add(osGrid);
		
		Vector<CatVariantHandler> os_handlers = new Vector<CatVariantHandler>(ovnames.size());
		int row = 0;
		for ( String ovn : ovnames ) {

			// overlay Checkbox 
			CheckBox osCheckBox = new CheckBox();
			osGrid.setWidget(row, 0, osCheckBox);
			osCheckBox.setText(ovn);
			
			// overlay Slider
			VariationSelectWidget osVarSlider = new VariationSelectWidget(som.getMaxVariant(),"60px",true);
			osGrid.setWidget(row, 1, osVarSlider);
			osVarSlider.setValue(som.getMaxVariant());
			
			// overlay numeric box
			IntegerBox osInt = new IntegerBox();
			osGrid.setWidget(row, 2, osInt);
			osInt.setValue(som.getMaxVariant());
			osInt.setMaxLength(2);
			osInt.setWidth("10px");
			
			row++;
			
			// Handlers, link them together
			osCheckBox.addClickHandler(new CatHandler(ovn, osInt));
			osVarSlider.addBarValueChangedHandler(new VarBarHandler(ovn, dsync, osCheckBox, osInt, osVarSlider));
			VarBoxHandler osBoxHandler = new VarBoxHandler(ovn, dsync, osCheckBox, osInt, osVarSlider);
			osInt.addChangeHandler(osBoxHandler);
			
			os_handlers.add(osBoxHandler);
		}
		
		globalSliderHandler.setChildHandlers(os_handlers);
		globalBoxHandler.setChildHandlers(os_handlers);
		
	}

	protected void activateOverlay(String name, int variant) {
		
		if ( variant < 0 ) {
			variant = som.getMaxVariant();
		}
		
		if ( ! som.existsOverlay(name, variant) ) {
			getOverlay(name, variant); // launch RPC request
		} else {
			som.setOverlayActive(name, variant);
		}
	}
	
	protected void inactivateOverlay(String name) {
		// inactivate all overlays with name
		som.setOverlayInactive(name);
	}

	
	/**
	 * Request the overlay data from the server
	 * @param name
	 * @param variant
	 */
	protected void getOverlay(String name, int variant) {
		if ( som == null )
			return;
		
		ServerService.getInstance().getDataOverlay(som.getMapName(), name, variant, new SomOverlayUpdater());
	}

	
	/**
	 * @author erwin
	 * RPC incoming data for the overlays
	 */
	public class SomOverlayUpdater extends AbstractLoggingAsyncHandler {
		
		public void handleFailure(Throwable caught) {
			
		}
		
		public void handleSuccess(Object result) {
			SOMDataOverlay data = (SOMDataOverlay) result;
			
			if ( data.queryResult != null ) {
				setLogEntry("SOM overlay: ERROR " + data.queryResult);
			} else {
				setLogEntry("SOM overlay: Received " + data.id.length);
				if ( som != null ) {
					som.addOverlay(data.name, data.variant, data.id, null);
					// Activate all newly received overlays - should be by request from slider/checkbox only
					activateOverlay(data.name, data.variant);
					canvasOwner.draw();
				}
			}
		}
		
	}

	public class CatHandler implements ClickHandler {
		String name;
		IntegerBox varBox;
		
		public CatHandler(String name, IntegerBox box) {
			this.name = name;
			this.varBox = box;
		}
		
		public void onClick(ClickEvent event) {
			// updateOverlays();
			
			boolean checked = ((CheckBox) event.getSource()).getValue();
			if ( checked == true ) {
				activateOverlay(name, varBox.getValue());
			} else {
				inactivateOverlay(name);
			}
			canvasOwner.draw();
		}
	}
	
	
	/**
	 * @author erwin
	 * Keeps checkbox, slider and textbox synchronized
	 * Updates all sliders/textboxes with the master slider/textbox is changed
	 */
	public class CatVariantHandler {
		protected DrawSync sync;
		private String name;
		private CheckBox cb;
		protected IntegerBox numBox;
		private VariationSelectWidget var;
		private Vector<CatVariantHandler> others = null;
		protected boolean change_req = false;
		
		public CatVariantHandler(String name, DrawSync sync, CheckBox cb, IntegerBox box, VariationSelectWidget var) {
			this.name = name;
			this.cb = cb;
			this.numBox = box;
			this.var = var;
			this.sync = sync;
		}
				
		public void setChildHandlers(Vector<CatVariantHandler> others) {
			this.others = others;
		}
				
		protected void change(int variant) {
			
			// Synchronize the two
			// The if statements shouldn't be necessary but GWT goes into infinite loop otherwise
			// The infinite loop happens because changing the value apparently triggers an event
			if ( numBox.getValue() != variant ) 
				numBox.setValue(variant);
			if ( var.getValue() != variant )
				var.setValue(variant);
			
			// if name == null, change all of them
			if ( others != null ) {
				for ( CatVariantHandler cvh : others ) {
					cvh.change(variant);
				}
			}
			
			if ( name == null ) {
				return;
			}
			
			if ( cb != null && cb.getValue() != null && cb.getValue() == true ) {
				som.setOverlayInactive(name);
				activateOverlay(name, variant);
			}
		}		
	}
		
	public class VarBarHandler extends CatVariantHandler implements BarValueChangedHandler {
		
		public VarBarHandler(String name, DrawSync sync, CheckBox cb, IntegerBox box, VariationSelectWidget var) {
			super(name, sync, cb, box, var);
		}
		
        public void onBarValueChanged(BarValueChangedEvent event) {
        	sync.noDraw();
        	// This prevents the handler from running two simultaneous RPC requests/redraws
        	if ( change_req == false ) {
        		change_req = true;
        		change(event.getValue());
        		change_req = false;
        	}
            sync.draw();
         }
	}
	
	public class VarBoxHandler extends CatVariantHandler implements ChangeHandler {
		
		public VarBoxHandler(String name, DrawSync sync, CheckBox cb, IntegerBox box, VariationSelectWidget var) {
			super(name, sync, cb, box, var);
		}

		public void onChange(ChangeEvent event) {
			sync.noDraw();
			// This prevents the handler from running two simultaneous RPC requests/redraws
        	if ( change_req == false ) {
        		change_req = true;
        		change(numBox.getValue());
        		change_req = false;
        	}
			sync.draw();
		}
	}
	
	/**
	 * @author erwin
	 * This class keeps track of multiple draw requests
	 * Used for the CatVariantHandler
	 * This keeps the updates of the synchronized widgets from redrawing the canvas before finished.
	 * Class would be very slow without that. 
	 */
	protected class DrawSync {
		private int redraw;
		private CanvasComposite canvas;
		
		public DrawSync(CanvasComposite canvas) {
			redraw = 0;
			this.canvas = canvas;
		}
		
		public void noDraw() {
			redraw++;
		}
		
		public void draw() {
			if ( --redraw <= 0 ) {
				redraw = 0;
				canvas.draw();
			}
		}
		
	}
	

}
