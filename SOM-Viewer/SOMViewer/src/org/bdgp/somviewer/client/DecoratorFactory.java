package org.bdgp.somviewer.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

public class DecoratorFactory {
	
	Vector<Decorator> content = new Vector<Decorator>(2);

	
	protected PointDecorator toPointDecorator(Decorator d) {
		
		if ( d.decorator.compareTo("Venn") == 0 )
			return new PointVenn(d.colormap);
		else if ( d.decorator.compareTo("Cloud") == 0 )
			return null;
		
		return null;
	}
	
	
	public void addOverlayType(String name, String decorator, String color) {
		
		for ( Decorator d : content ) {
			if ( d.decorator.compareTo(decorator) == 0 ) {
				 d.colormap.put(name, color);
				 return;
			}
		}
		
		Decorator d = new Decorator();
		d.decorator = decorator;
		d.colormap = new HashMap<String,String>();
		d.colormap.put(name, color);
		content.add(d);
	}

	
	public Iterator<PointDecorator> getDecorators() {
		return new IterateDecorators();
	}
	
	
	protected class IterateDecorators implements Iterator<PointDecorator> {
	
		int pos;
		
		public IterateDecorators() {
			pos = 0;
		}
	
		public boolean hasNext() {
			if ( pos < content.size() )
				return true;
			return false;
		}

		public PointDecorator next() {
			if ( pos >= content.size())
				throw new NoSuchElementException();
			
			Decorator d = content.get(pos++);
			return toPointDecorator(d);
		}

		public void remove() {
		}
	}
	
	protected class Decorator {
		public String decorator;
		public HashMap<String,String> colormap;
	}

}
