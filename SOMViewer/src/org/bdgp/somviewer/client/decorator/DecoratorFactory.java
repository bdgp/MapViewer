package org.bdgp.somviewer.client.decorator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.bdgp.somviewer.client.OverlayDrawMap;
import org.bdgp.somviewer.client.SOMData;

public class DecoratorFactory {
	
	SOMData som;
	OverlayDrawMap overlay_map;
	ColorRank col_rank;
	Vector<Decorator> content = new Vector<Decorator>(2);

	public DecoratorFactory(SOMData som, ColorRank col_rank) {
		this.som = som;
		this.col_rank = col_rank;
		overlay_map = new OverlayDrawMap(som);
		addOverlays();
	}
	
	protected PointDecorator toPointDecorator(Decorator d) {
		
		if ( d.decorator.compareTo("Basic") == 0 )
			return new PointBasic(d.colormap, col_rank);
		else if ( d.decorator.compareTo("Venn") == 0 )
			return new PointVenn(d.colormap, col_rank);
		else if ( d.decorator.compareTo("Cloudy") == 0 )
			return new PointCloudy(d.colormap, col_rank);
		
		return null;
	}
	
	
	protected void addOverlays() {
		Iterator<String> map_it = overlay_map.libraryIterator();
		
		while (map_it.hasNext()) {
			addOverlayType(map_it.next());
		}
	}
	
	
	public void addOverlayType(String name) {
		
		String decorator = overlay_map.getDecorator(name);
		
		for ( Decorator d : content ) {
			if ( d.decorator.compareTo(decorator) == 0 ) {
				 d.colormap.add(name);
				 return;
			}
		}
		
		Decorator d = new Decorator();
		d.decorator = decorator;
		d.colormap = new OverlayDrawMap(som);
		d.colormap.add(name);
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
		public OverlayDrawMap colormap;
	}

}
