package org.bdgp.somviewer.client;

import com.kiouri.sliderbar.client.solution.simplehorizontal.SliderBarSimpleHorizontal;

public class VariationSelectWidget extends SliderBarSimpleHorizontal {

	public VariationSelectWidget(int maxValue, String width, boolean showRows) {
		super(maxValue, width, showRows);
		
		init();
	}

	
	public void init() {
	
		drawMarks("white",1);
		setMinMarkStep(2);
	}
	
}
