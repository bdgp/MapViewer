package org.bdgp.somviewer.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.bdgp.somviewer.client.SOMData.Library;

public class OverlayDrawMap {
	private Vector<Integer> values;
	private HashMap<String,SOMData.Library> entries;
	final private HashMap<String,SOMData.Library> library;
	
	public OverlayDrawMap(final SOMData som) {
		library = som.library;
		entries = new HashMap<String,SOMData.Library>(library.size());
	}
	
	public void add(String id) {
		entries.put(id,library.get(id));
	}
	
	public void addValue(int val) {
		if ( values == null )
			values = new Vector<Integer>(2);
		values.add(val);
	}
	
	
	public void addAll() {
		// essentially just duplicate the entire map
		for (Map.Entry<String, Library> entry : library.entrySet()) {
			entries.put(entry.getKey(), entry.getValue());
		}
	}
	
	public boolean containsKey(String id) {
		return entries.containsKey(id);
	}
	
	public String getColor(String id) {
		return library.get(id).color;
	}
	

	public int getColorRank(String id) {
		return library.get(id).color_rank;
	}
	
	
	public String getDecorator(String id) {
		return library.get(id).decorator;
	}
	
	
	public Vector<Integer> getValues(String id) {
		return values;
	}
	
	public Iterator<String> libraryIterator() {
		return new IterateMaps();
	}
	
	
	public Iterator<String> mapIterator() {
		return new IterateMaps(false);
	}
	
	
	public class IterateMaps implements Iterator<String> {
		protected Iterator<Map.Entry<String, SOMData.Library>> lib_it;
		
		public IterateMaps() {
			lib_it = library.entrySet().iterator();
		}

		public IterateMaps(boolean all) {
			if ( all == true )
				lib_it = library.entrySet().iterator();
			else
				lib_it = entries.entrySet().iterator();
		}

		public boolean hasNext() {
			return lib_it.hasNext();
		}

		public String next() {
			Map.Entry<String, Library> entry = lib_it.next();
			return entry.getKey();
		}

		public void remove() {
		}
		
	}
	
}
