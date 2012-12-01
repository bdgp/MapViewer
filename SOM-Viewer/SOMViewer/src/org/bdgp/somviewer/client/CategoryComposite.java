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
import com.google.gwt.user.client.ui.HasVerticalAlignment;
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
	protected DrawSync dsync;

	protected boolean showCircles = true, showLabels = true;

	
	public CategoryComposite(CanvasComposite canvasOwner, SOMData som) {
		this.som = som;
		this.canvasOwner = canvasOwner;
		
		initWidget(catPanel);
		
		dsync = new DrawSync(canvasOwner);
		populatePanel();	
	}
	
	
	protected void populatePanel() {

		// catPanel.clear();

		HTML titleText = new HTML("Overlays");
		titleText.setStyleName("ctrlTitle");
		catPanel.add(titleText);
		
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
		
		for ( String t : som.getOverlayTypes() ) {
			populateCatGroup(t);
		}
		
	}

	
	protected void populateCatGroup(String type) {
		int max_variant = som.getMaxVariantByType(type);
	
		Vector<String> ovnames = som.getOverlayNames(type);				

		Label lblNewLabel = new Label(type);
		lblNewLabel.setStyleName("ctrlSubTitle");
		catPanel.add(lblNewLabel);
		
		CatVariantHandler globalCatHandler = null;
		String [] globalVarNames = null;

		if ( max_variant > 1 && ovnames.size() > 1 ) {
			HorizontalPanel global_varPanel = new HorizontalPanel();
			global_varPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
			catPanel.add(global_varPanel);

			VerticalPanel globalVPan = new VerticalPanel();
			Label globalLabel = new Label("Stage 13-16");
			globalLabel.setStyleName("verboseLabel");
			globalVPan.add(globalLabel);
			VariationSelectWidget globalSlider = new VariationSelectWidget(max_variant,"60px",true);
			globalSlider.setValue(som.getMaxVariant());
			globalVPan.add(globalSlider.asWidget());
			global_varPanel.add(globalVPan);
			
			IntegerBox globalInt = new IntegerBox();
			global_varPanel.add(globalInt);
			globalInt.setValue(som.getMaxVariant());
			globalInt.setMaxLength(2);
			globalInt.setWidth("10px");

			globalCatHandler = new CatVariantHandler(null, null, globalInt, globalSlider, globalLabel);
			globalSlider.addBarValueChangedHandler(globalCatHandler);
			globalInt.addChangeHandler(globalCatHandler);
		}
		
		Grid osGrid = new Grid(ovnames.size(), 3);
		catPanel.add(osGrid);
		
		Vector<CatVariantHandler> os_handlers = new Vector<CatVariantHandler>(ovnames.size());
		int row = 0;
		for ( String ovn : ovnames ) {

			// overlay Checkbox - always there
			CheckBox osCheckBox = new CheckBox();
			osGrid.setWidget(row, 0, osCheckBox);
			osCheckBox.setText(ovn);
			String col = som.getOverlayColor(ovn);
			if ( col != null)
				osCheckBox.getElement().getStyle().setProperty("backgroundColor", "#" + col);
			
			
			if ( max_variant > 1 ) {
				// Create sliders and value box and link them to each other
				
				// overlay Slider & Label
				VerticalPanel vPan = new VerticalPanel();
				Label osLab = new Label("Stage 13-16");
				osLab.setStyleName("verboseLabel");
				vPan.add(osLab);
				VariationSelectWidget osVarSlider = new VariationSelectWidget(max_variant,"60px",true);
				osVarSlider.setValue(max_variant);				
				vPan.add(osVarSlider.asWidget());				
				osGrid.setWidget(row, 1, vPan);
				

				// overlay numeric box
				IntegerBox osInt = new IntegerBox();
				osGrid.setWidget(row, 2, osInt);
				osInt.setValue(max_variant);
				osInt.setMaxLength(2);
				osInt.setWidth("10px");

				// Handlers, link them together
				osCheckBox.addClickHandler(new CatHandler(ovn, osInt));
				CatVariantHandler catVHandler = new CatVariantHandler(ovn, osCheckBox, osInt, osVarSlider, osLab);
				osVarSlider.addBarValueChangedHandler(catVHandler);
				osInt.addChangeHandler(catVHandler);
				catVHandler.setVariantNames(som.getVariantNames(ovn));
				// Set global variant names to first one
				if ( globalVarNames == null )
					globalVarNames = som.getVariantNames(ovn);

				os_handlers.add(catVHandler);
			}
			else
			{
				// Just leave it with the checkbox, add handler
				osCheckBox.addClickHandler(new CatHandler(ovn, max_variant));
			}
			
			row++;

		}
		
		if ( globalCatHandler != null ) {
			globalCatHandler.setChildHandlers(os_handlers);
			globalCatHandler.setVariantNames(globalVarNames);
		}
		
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
		
		Feedback.getInstance().rpcRequested(name + " - " + variant);
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
				Feedback.getInstance().rpcError(data.queryResult);
			} else {
				setLogEntry("SOM overlay: Received " + data.id.length);
				Feedback.getInstance().rpcReceived("Overlay: " + data.id.length);
				if ( som != null ) {
					som.addOverlay(data, null);
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
		int const_value;
		
		public CatHandler(String name, IntegerBox box) {
			this.name = name;
			this.varBox = box;
		}
		
		public CatHandler(String name, int const_value) {
			this.name = name;
			this.const_value = const_value;
			varBox = null;
		}
		
		
		public void onClick(ClickEvent event) {
			// updateOverlays();
			
			boolean checked = ((CheckBox) event.getSource()).getValue();
			if ( checked == true ) {
				if ( varBox != null )
					activateOverlay(name, varBox.getValue());
				else
					activateOverlay(name, const_value);
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
	public class CatVariantHandler implements BarValueChangedHandler, ChangeHandler {
		protected DrawSync sync;
		private String name;
		private CheckBox cb;
		protected IntegerBox numBox;
		private VariationSelectWidget var;
		private Label lab;
		private Vector<CatVariantHandler> others = null;
		private String [] variant_names = null;
		protected boolean change_req = false;
		
		public CatVariantHandler(String name, CheckBox cb, IntegerBox box, VariationSelectWidget var, Label lab) {
			this.name = name;
			this.cb = cb;
			this.numBox = box;
			this.var = var;
			this.lab = lab;
			this.sync = dsync;
		}
		
		
		public void setVariantNames(String [] variant_names) {
			this.variant_names = variant_names;
			if ( variant_names.length >= numBox.getValue() )
				lab.setText(variant_names[numBox.getValue()]);
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
		
		public void setChildHandlers(Vector<CatVariantHandler> others) {
			this.others = others;
		}
				
		protected void change(int variant) {
			
			// Synchronize the two
			// The if statements shouldn't be necessary but GWT goes into infinite loop otherwise
			// The infinite loop happens because changing the value apparently triggers an event
			if ( numBox.getValue() != variant ) {
				numBox.setValue(variant);

				if ( variant_names != null ) {
					if ( variant_names.length >= numBox.getValue() )
						lab.setText(variant_names[numBox.getValue()]);
				}
			}
			if ( var.getValue() != variant )
				var.setValue(variant);
			
			// if name == null, change all of them
			if ( others != null ) {
				canvasOwner.setCommonVariant(variant);
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
