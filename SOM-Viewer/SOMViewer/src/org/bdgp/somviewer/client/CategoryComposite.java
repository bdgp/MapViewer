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
	protected SOMComposite canvasOwner;
	protected VerticalPanel catPanel  = new VerticalPanel();
	protected int redraw = -1;

	protected boolean showCircles = true, showLabels = true;

	
	public CategoryComposite(SOMComposite canvasOwner, SOMData som) {
		this.som = som;
		this.canvasOwner = canvasOwner;
		
		initWidget(catPanel);
		populatePanel();	
	}

	public void activate(boolean act) {
		if ( act == true )
			redraw = 0;
		else
			redraw = -1;
	}
	
	protected void toggleDraw(boolean noredraw) {
		if ( redraw < 0 )
			return;
		if ( noredraw == true )
			redraw++;
		else if ( redraw > 0 )
			redraw--;
	}
	
	
	protected void draw() {
		if ( redraw == 0 )
			canvasOwner.draw();
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
				if ( som != null )
					som.invalidateCanvasGroup();
				draw();
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
				if ( som != null )
					som.invalidateCanvasGroup();
				draw();
			}
		});				

		Label lblNewLabel = new Label("Organ system");
		catPanel.add(lblNewLabel);
		
		
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
		
		VarBarHandler globalSliderHandler = new VarBarHandler(null, null, globalInt, globalSlider);
		VarBoxHandler globalBoxHandler = new VarBoxHandler(null, null, globalInt, globalSlider);
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
			osVarSlider.addBarValueChangedHandler(new VarBarHandler(ovn, osCheckBox, osInt, osVarSlider));
			VarBoxHandler osBoxHandler = new VarBoxHandler(ovn, osCheckBox, osInt, osVarSlider);
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

	
	protected void getOverlay(String name, int variant) {
		if ( som == null )
			return;
		
		ServerService.getInstance().getDataOverlay(som.getMapName(), name, variant, new SomOverlayUpdater());
	}

	
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
					draw();
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
			draw();
		}
	}
	
	
	public class CatVariantHandler {
		private String name;
		private CheckBox cb;
		protected IntegerBox numBox;
		private VariationSelectWidget var;
		private Vector<CatVariantHandler> others = null;
		
		public CatVariantHandler(String name, CheckBox cb, IntegerBox box, VariationSelectWidget var) {
			this.name = name;
			this.cb = cb;
			this.numBox = box;
			this.var = var;
		}
				
		public void setChildHandlers(Vector<CatVariantHandler> others) {
			this.others = others;
		}
				
		protected void change(int variant) {
			
			toggleDraw(true);
			
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
			
			toggleDraw(false);
		}		
	}
		
	public class VarBarHandler extends CatVariantHandler implements BarValueChangedHandler {
		
		public VarBarHandler(String name, CheckBox cb, IntegerBox box, VariationSelectWidget var) {
			super(name, cb, box, var);
		}
		
        public void onBarValueChanged(BarValueChangedEvent event) {
            change(event.getValue());
            draw();
         }
	}
	
	public class VarBoxHandler extends CatVariantHandler implements ChangeHandler {
		
		public VarBoxHandler(String name, CheckBox cb, IntegerBox box, VariationSelectWidget var) {
			super(name, cb, box, var);
		}

		public void onChange(ChangeEvent event) {
			change(numBox.getValue());
			draw();
		}
	}

}
