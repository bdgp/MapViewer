package org.bdgp.somviewer.client;

/**
 *PUBLIC SOFTWARE
 *
 *This source code has been placed in the public domain. You can use, modify, and distribute
 *the source code and executable programs based on the source code.
 *
 *However, note the following:
 *
 *DISCLAIMER OF WARRANTY
 *
 * This source code is provided "as is" and without warranties as to performance
 * or merchantability. The author and/or distributors of this source code may
 * have made statements about this source code. Any such statements do not constitute
 * warranties and shall not be relied on by the user in deciding whether to use
 * this source code.This source code is provided without any express or implied
 * warranties whatsoever. Because of the diversity of conditions and hardware
 * under which this source code may be used, no warranty of fitness for a
 * particular purpose is offered. The user is advised to test the source code
 * thoroughly before relying on it. The user must assume the entire risk of
 * using the source code.
 * 
 */


import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;

/**
 * @author amal
 * @version 1.0
 * From
 * http://zone817.blogspot.com/2010/08/close-button-in-caption-bar-of-gwt.html
 */


public class DialogBoxClosable extends DialogBox {


	    HTML close = new HTML("[X]");
	    HTML title = new HTML("");
	    HorizontalPanel captionPanel = new HorizontalPanel();

		public DialogBoxClosable(boolean autoHide, boolean modal)
	      {
	        super(autoHide, modal);
	        Element td = getCellElement(0, 1);
	        DOM.removeChild(td, (Element) td.getFirstChildElement());
	        DOM.appendChild(td, captionPanel.getElement());
	        captionPanel.setStyleName("Caption");//width-100%
	        captionPanel.setWidth("100%");
	        captionPanel.add(title);
	        close.addStyleName("CloseButton");//float:right
	        captionPanel.add(close);
	        super.setGlassEnabled(true);
	        super.setAnimationEnabled(true);
	      }
		
	    public DialogBoxClosable(boolean autoHide)
	      {
	        this(autoHide, true);
	      }
	    public DialogBoxClosable()
	      {
	        this(false);
	      }

	     @Override
	    public String getHTML()
	      {
	        return this.title.getHTML();
	      }

	    @Override
	    public String getText()
	      {
	        return this.title.getText();
	      }

	    @Override
	    public void setHTML(String html)
	      {
	        this.title.setHTML(html);
	      }

	    @Override
	    public void setText(String text)
	      {
	        this.title.setText(text);
	      }

	    @Override
	    protected void onPreviewNativeEvent(NativePreviewEvent event)
	      {
	        NativeEvent nativeEvent = event.getNativeEvent();

	        if (!event.isCanceled()
	          && (event.getTypeInt() == Event.ONCLICK)
	          && isCloseEvent(nativeEvent))
	          {
	            this.hide();
	          }
	        super.onPreviewNativeEvent(event);
	      }

	    private boolean isCloseEvent(NativeEvent event)
	      {
	        return event.getEventTarget().equals(close.getElement());//compares equality of the underlying DOM elements
	      }
	    }

