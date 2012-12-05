package org.bdgp.somviewer.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.bdgp.somviewer.client.decorator.DecoratorFactory;
import org.bdgp.somviewer.rpc.data.SOMDataOverlay;
import org.bdgp.somviewer.rpc.data.SOMDataPts;
import org.bdgp.somviewer.rpc.data.SOMOverlaysAvailable;
import org.vaadin.gwtgraphics.client.Group;

public class SOMData {
	
	protected static final int MAX_OVERLAY = 30; // max. number of overlays cached
	protected static final float ZOOM_INC = 100; // Increment for zoom
	protected static final int VALUE_RANGE = 100; // Integer range for point values
	
	protected String map_name;
	protected HashMap<String,Overlay> overlay;
	protected HashMap<Integer,SOMstruct> all_data; // all data 
	protected HashMap<Integer,SOMstruct> data; // visible data
	protected HashMap<Integer,Integer> hid_data; // mappings from hidden to visible
	protected HashMap<String,Library> library;
	protected Vector<String> types, decorators;
	protected SOMDataPts pts;
	protected HashMap<Integer,Group> data_grp = null;
	protected float zoom;
	protected float zoom_range = 1000;
	
	public SOMData(SOMDataPts pts) {
		this.pts = pts;
		
		map_name = pts.map;
		all_data = new HashMap<Integer,SOMstruct>(100);
		data = new HashMap<Integer, SOMstruct>(100);
		hid_data = new HashMap<Integer,Integer>();
		library = new HashMap<String, Library>();
		
		float hid_res = 1 / zoom_range;
		
		for ( int i = 0; i < pts.id.length; i++ ) {
			SOMstruct sd = new SOMstruct();
			sd.id = new Integer(pts.id[i]);
			sd.x = pts.x[i];
			sd.y = pts.y[i];
			sd.name = pts.names[i];
			all_data.put(sd.id, sd);
			categorizeVisibility(sd, hid_res);
			// data.put(new Integer(pts.id[i]), sd);
		}
		
		
		Unique un_type = new Unique();
		Unique un_decorator = new Unique();
		
		Vector<SOMOverlaysAvailable> av = pts.available;
		for ( SOMOverlaysAvailable a : av ) {
			// Fill in library
			Library l = new Library();
			l.color = a.color;
			l.color_similar = a.color_similar;
			l.max = a.variant;
			l.variant_names = a.variant_names;
			l.type = a.type;
			un_type.addTerm(a.type);
			l.decorator = a.decorator;
			un_decorator.addTerm(a.decorator);
			library.put(a.name,l);
		}
		
		types = un_type.getUnique();
		decorators = un_decorator.getUnique();
	}

	
	/**
	 * This function categorizes the data if they are visible or not (i.e. on the same point, depending on the zoom)
	 * Normally this weeds out identical points but this version is zoom dependent.
	 * Thus points with very small differences can become visible if zoomed enough.
	 * @param id
	 * @param eval
	 * @param resolution
	 */
	protected void categorizeVisibility(SOMstruct eval, float resolution) {
		
		boolean invisible = false;
		
		for (Map.Entry<Integer, SOMstruct> entry : data.entrySet()) {
			SOMstruct s = entry.getValue();
			
			if ( Math.abs(s.x - eval.x) <= resolution && Math.abs(s.y - eval.y) <= resolution ) {
				hid_data.put(eval.id, entry.getKey());
				invisible = true;
				break;
			}			
		}
		
		if ( invisible == false )
			data.put(eval.id, eval);
		
	}
	
	
	public static String overlay2uuid(String name, int variant) {
		String uuid;
		if ( variant > 0 )
			uuid = name + "_" + variant;
		else
			uuid = name;
		return uuid;
	}
	
	
	public String getMapName() {
		return map_name;
	}
	
	
	public Vector<String> getOverlayTypes() {
		return types;
	}
	
	public Vector<String> getOverlayDecorators() {
		return decorators;
	}

	
	
	public Vector<String> getOverlayNames() {
		
		Vector<String> names = new Vector<String>(library.size());
		
		for (Map.Entry<String, Library> entry : library.entrySet()) {
		    names.add(entry.getKey());
		}

		return names;
	}
	
	
	public Vector<String> getOverlayNames(String type) {
		
		Vector<String> names = new Vector<String>(library.size());
		
		for (Map.Entry<String, Library> entry : library.entrySet()) {
			if ( entry.getValue().type.compareTo(type) == 0)
				names.add(entry.getKey());
		}

		return names;
	}

	
	public HashMap<String,String> getColorMap() {
		HashMap<String,String> colormap = new HashMap<String,String>();
		
		for (Map.Entry<String, Library> entry : library.entrySet()) {
			colormap.put(entry.getKey(), entry.getValue().color);
		}
		
		return colormap;
	}
	
	
	public String getOverlayColor(String name) {
		
		if ( library.containsKey(name) )
			return library.get(name).color;
		
		return null;
		
	}
	
	
	public int getColorRank(String id) {
		if ( library.containsKey(id) )
			return library.get(id).color_rank;
		return 0;
	}
	
	
	public int getMaxVariant () {
		int max = 0;
		
		for (Map.Entry<String, Library> entry : library.entrySet()) {
			int lmax = entry.getValue().max;
		    if ( lmax > max )
		    	max = lmax;
		}
		
		return max;
	}
	
	public int getMaxVariantByName(String name) {
		Library l = library.get(name);
		if ( l == null ) {
			throw new NoSuchElementException();
		}
		
		return l.max;
	}
	
	public int getMaxVariantByType(String type) {
		int max = 0;
		
		for (Map.Entry<String, Library> entry : library.entrySet()) {
			Library l = entry.getValue();			
			if ( l.type.compareTo(type) == 0) {			
				int lmax = entry.getValue().max;
				if ( lmax > max )
					max = lmax;
			}
		}
		
		return max;
	}
	
	
	public String [] getVariantNames(String name) {
		Library l = library.get(name);
		if ( l == null ) {
			throw new NoSuchElementException();
		}
		return l.variant_names;
	}
	
	
	public void setZoom(float zoom) {
		if ( this.zoom != zoom ) {
			data_grp = null;
		}
		this.zoom = zoom;
		
		if ( zoom > zoom_range ) {
			// re-categorize the visibility
			zoom_range *= ZOOM_INC;
			
			data = new HashMap<Integer,SOMstruct>();
			hid_data = new HashMap<Integer, Integer>();
			float hid_res = 1 /zoom_range;
			for ( Map.Entry<Integer, SOMstruct> entry : all_data.entrySet() ) {
				SOMstruct s = entry.getValue();
				categorizeVisibility(s, hid_res);
			}
		}
		
	}
	
	
	public SOMpt search(String name) {
		
		SOMpt pt = null;
		
		for ( Map.Entry<Integer, SOMstruct> entry : all_data.entrySet() ) {
			SOMstruct s = entry.getValue();
			if ( s.name.compareTo(name) == 0 ) {
				pt = new SOMpt(s.id, s.x, s.y, s.name);
				return pt;
			}
		}
		
		return null;
	}
	

	public Iterator<SOMpt> getData() {
		IterateCoordinates ic = new IterateCoordinates();
		return ic;
	}
	
	public Iterator<SOMpt> getAllData() {
		IterateCoordinates ic = new IterateCoordinates(true);
		return ic;
	}		
	
	
	public void setDataCanvasGroup(Group grp, int id, float zoom) {
		if ( data_grp == null )
			data_grp = new HashMap<Integer,Group>();
		data_grp.put(new Integer(id), grp);
		this.zoom = zoom;
	}
	
	
	public Group getDataCanvasGroup(int id) {
		if ( data_grp == null )
			return null;
		if ( data_grp.containsKey(new Integer(id)) ) 
			return data_grp.get(id);
		else
			return null;
	}
	
	public boolean existsCanvasGroup() {
		if ( data_grp == null )
			return false;
		else
			return true;
	}
	
	
	public void invalidateCanvasGroup() {
		data_grp = null;
	}
	
	
	public OverlayIterator getOverlays() {
		OverlayIterator oi = new IterateOverlays();
		return oi;
	}
	
	public void addOverlay(SOMDataOverlay data, String col) {
		if ( overlay == null ) {
			overlay = new HashMap<String,Overlay>();
		}
		
		String uuid = overlay2uuid(data.name, data.variant);
		
		if ( overlay.containsKey(uuid) ) {
			overlay.remove(uuid);
		}
		
		Library l = library.get(data.name);
		if ( l == null ) {
			throw new NoSuchElementException();
		}
		
		Overlay ovn = new Overlay();
		ovn.active = true;
		ovn.name = data.name;
		ovn.variant = data.variant;
		ovn.ids = data.id;
		ovn.vectorGroup = null;
		ovn.color = col != null? col : l.color;
		ovn.zoom = zoom;
		
		// TODO: Deal with values
		// Find min/max value and calculate int for each value
		if ( data.values != null ) {
			ovn.values = new int[data.values.length];
			float val_scaled;
			float scale = Math.abs(data.val_max - data.val_min);
			for ( int i= 0; i < data.values.length; i++ ) {
				float val = data.values[i];
				// TODO:
				val_scaled = val - data.val_min + Math.abs(val) * VALUE_RANGE / scale;
				ovn.values[i] = (int) val_scaled;
			}
		}
		
		overlay.put(uuid, ovn);
		
		if ( overlay.size() > MAX_OVERLAY ) {
			pruneOverlays();
		}
	}

	
	public boolean existsOverlay(String name, int variant) {
		
		if ( overlay == null )
			return false;
		
		return overlay.containsKey(overlay2uuid(name, variant));
	}
	
	/**
	 * Set overlay with specified name & variant active
	 * @param name
	 * @param variant
	 */
	public void setOverlayActive(String name, int variant) {		
		if ( overlay == null )
			return;
		
		setOverlayActive(overlay.get(overlay2uuid(name, variant)));
	}

	
	/**
	 * Set all overlays with specified variant active
	 * @param variant
	 */
	public void setOverlayActive(int variant) {
		if ( overlay == null )
			return;
		
		Iterator<Map.Entry<String, Overlay>> alloverlays = overlay.entrySet().iterator();
		
		while (alloverlays.hasNext()) {
			Map.Entry<String, Overlay> entry = (Map.Entry<String, Overlay>) alloverlays.next();
			Overlay ov = (Overlay) entry.getValue();
			
			if ( ov.variant == variant )
				setOverlayActive(ov);
		}

	}
	
	/**
	 * Set all overlays with specified name active
	 * @param name
	 */
	public void setOverlayActive(String name) {
		if ( overlay == null )
			return;
		
		Iterator<Map.Entry<String, Overlay>> alloverlays = overlay.entrySet().iterator();
		
		while (alloverlays.hasNext()) {
			Map.Entry<String, Overlay> entry = (Map.Entry<String, Overlay>) alloverlays.next();
			Overlay ov = (Overlay) entry.getValue();
			
			if ( ov.name.compareTo(name) == 0 )
				setOverlayActive(ov);
		}

	}

	
	protected void setOverlayActive(Overlay ov) {
		ov.active = true;
		setColorRank(ov.name,false);
	}
	
	
	public void setOverlayInactive(String name) {
		Library l = library.get(name);
		if ( l == null ) {
			return;
		}
		
		for ( int var=0; var <= l.max; var++ ) {
			if ( existsOverlay(name, var) ) {
				setOverlayInactive(name,var);
			}
		}
		
	}
	
	public void setOverlayInactive(String name, int variant) {
		if ( overlay == null )
			return;

		Overlay ov = overlay.get(overlay2uuid(name, variant));
		ov.active = false;
		setColorRank(ov.name,true);
	}
	
	
	protected void setColorRank(String name, boolean rm) {
		int rank = 0;
		Library l = library.get(name);
		HashMap<String, Integer> similar = new HashMap<String,Integer>(l.color_similar.length);
		HashMap<String, Library> adjust = new HashMap<String, Library>(library.size());
		
		for ( String ss : l.color_similar ) {
			similar.put(ss, 1);
		}
		
		Iterator<Map.Entry<String, Overlay>> alloverlays = overlay.entrySet().iterator();
		while (alloverlays.hasNext()) {
			Map.Entry<String, Overlay> entry = (Map.Entry<String, Overlay>) alloverlays.next();
			Overlay ovc = (Overlay) entry.getValue();
			
			if ( ovc.active == true && similar.containsKey(ovc.name) ) {
				Library la = library.get(ovc.name);
				if ( l == la )
					continue;
				if ( ! adjust.containsKey(ovc.name) ) {
					adjust.put(ovc.name, la);
					rank++;
				}
			}
		}
		
		
		if ( rm == true) {
			if ( adjust.size() == 0 )
				return;
			Iterator<Map.Entry<String, Library>> adj_libs = adjust.entrySet().iterator();
			while (adj_libs.hasNext()) {
				Map.Entry<String, Library> entry = (Map.Entry<String, Library>) adj_libs.next();
				Library la = entry.getValue();
				if ( la.color_rank > l.color_rank )
					la.color_rank--;
			}
		} else {
			l.color_rank = rank;
		}
	}
	
	
	// Get rid of all inactive overlays
	protected void pruneOverlays() {
		if ( overlay == null )
			return;
		
		Vector<Overlay> killme = new Vector<Overlay>(30);
		Iterator<Map.Entry<String, Overlay>> alloverlays = overlay.entrySet().iterator();
		
		while (alloverlays.hasNext()) {
			Map.Entry<String, Overlay> entry = (Map.Entry<String, Overlay>) alloverlays.next();
			Overlay ov = entry.getValue();
			if ( ov.active == false )
				killme.add(ov);
		}
		
		for ( Overlay ov : killme ) {
			overlay.remove(ov);
		}
	}
	
	
	public class IterateCoordinates implements Iterator<SOMpt> {
		protected HashMap<Integer,SOMpt> seldata;
		protected Iterator<Map.Entry<Integer, SOMpt>> sel_it;
		protected Iterator<Map.Entry<Integer, SOMstruct>> all_it;
		
		public IterateCoordinates() {
			sel_it = null;
			all_it = data.entrySet().iterator();
		}
		
		public IterateCoordinates(String uuid) {
			if ( overlay == null )
				throw new NoSuchElementException();
			
			Overlay ov = overlay.get(uuid);
			if ( ov == null )
				throw new NoSuchElementException();			
			
			all_it = null;
			genData(ov);
			sel_it = seldata.entrySet().iterator();
		}
		
		
		/**
		 * Generate an coordinate iterator that either 
		 * - contains all datapoints only (all == false)
		 * - contains all datapoints and overlays (all == true)
		 * @param all
		 */
		public IterateCoordinates(boolean all) {
			
			all_it = data.entrySet().iterator();

			if ( all == false ) {
				// Use all_it
				sel_it = null;
				return;
			}
			else
			{
				seldata = new HashMap<Integer,SOMpt>();
				
				// copy everything to seldata
				// First generate the visible data
				while (all_it.hasNext()) {
				    Map.Entry<Integer, SOMstruct> entry = (Map.Entry<Integer, SOMstruct>) all_it.next();
				    SOMstruct value = (SOMstruct) entry.getValue();
				    SOMpt pt = new SOMpt(entry.getKey(), value.x, value.y, value.name);
					seldata.put(entry.getKey(), pt);
				}
				all_it = null;
				
				// ... then add the hidden data
				Iterator<Map.Entry<Integer, Integer>> hid_it = hid_data.entrySet().iterator();
				while (hid_it.hasNext()) {
				    Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) hid_it.next();
				    Integer secondary_id = (Integer) entry.getKey();
				    Integer primary_id = (Integer) entry.getValue();
				    SOMpt pt = seldata.get(primary_id);
				    pt.addIdenticalPt(secondary_id, all_data.get(secondary_id).name);
				}
			}
			
			// if there are overlays, add them to sel_it
			if ( overlay != null ){
				for ( String key : overlay.keySet() ) {
					Overlay ov = overlay.get(key);
					if ( ov.active == true ) {
						genData(ov);
					}
				}
			}
			sel_it = seldata.entrySet().iterator();
		}
		
		
		protected void genData(Overlay ov) {
			
			if ( ov == null ) {
				return;
			}
			
			if ( seldata == null ) {
				seldata = new HashMap<Integer,SOMpt>();
			}
			
			for ( int i = 0; i < ov.ids.length; i++ ) {
				SOMpt pt;
				int ov_value = 0;
								
				if ( seldata.containsKey(ov.ids[i]) ) {
					pt = seldata.get(ov.ids[i]);
				}
				else if ( hid_data.containsKey(ov.ids[i])) {
					// The overlay is in the hidden set, fetch the primary data
					Integer primary_id = hid_data.get(ov.ids[i]);
					if ( seldata.containsKey(primary_id) ) {
						pt = seldata.get(primary_id);
					} else {
						SOMstruct value = data.get(primary_id);
						pt = new SOMpt(ov.ids[i], value.x, value.y, value.name);
						seldata.put(ov.ids[i], pt);
					}
				}
				else {
					SOMstruct value = data.get(ov.ids[i]);
					pt = new SOMpt(ov.ids[i], value.x, value.y, value.name);
					seldata.put(ov.ids[i], pt);
				}
				if ( ov.values != null )
					ov_value = ov.values[i];
				// pt.addDrawDescription(ov.variant, ov.color, library.get(ov.name).color_rank, null); // Draw description is not really used right now?
				pt.addDrawMap(SOMData.this, ov.name, ov_value);
				//pt.addColorMapName(ov.name, ov_value);
			}
		}
		
		
		public boolean hasNext() {
			
			if ( sel_it == null ) {
				return all_it.hasNext();
			} else {
				return sel_it.hasNext();
			}
		}

		public SOMpt next() {
			SOMpt pt = null;
			
			if ( all_it != null ) {
			    Map.Entry<Integer, SOMstruct> entry = (Map.Entry<Integer, SOMstruct>) all_it.next();
			    SOMstruct value = (SOMstruct) entry.getValue();
			    pt = new SOMpt(entry.getKey(), value.x, value.y, value.name);
			}
			else if ( sel_it != null ) {
			    Map.Entry<Integer, SOMpt> entry = (Map.Entry<Integer, SOMpt>) sel_it.next();
			    pt = (SOMpt) entry.getValue();
			}
			
			return pt;
			
		}

		public void remove() {
		}
	}
	
	
	public class IterateOverlays implements OverlayIterator {

		protected Vector<String> ov_active;
		protected int idx;
		
		public IterateOverlays() {
			idx = 0;
			ov_active = new Vector<String>();
			
			if ( overlay != null )			
				for ( String key : overlay.keySet() ) {
					Overlay ov = overlay.get(key);
					if ( ov.active == true ) {
						ov_active.add(key);
					}
				}
		}
		
		
		public boolean hasNext() {
			
			if ( idx < ov_active.size() )
				return true;
			
			return false;
		}

		public IterateCoordinates next() {
			
			if ( idx >= ov_active.size() )
				throw new NoSuchElementException();

			IterateCoordinates ic = new IterateCoordinates(ov_active.get(idx));
			idx++;
			
			return ic;
		}

		public Group getGroup() {
			Overlay ov = getCurrent();
			if ( ov.vectorGroup != null && ov.zoom == zoom ) {
				return ov.vectorGroup;
			}
			
			return null;
		}
		
		public void setGroup(Group grp) {
			Overlay ov = getCurrent();
			// Don't update Group if we're not active - we don't want to have Group with empty graphics
			if (ov.active == false)
				return;
			ov.vectorGroup = grp;
			ov.zoom = zoom;
		}
		
		public boolean isActive() {
			Overlay ov = getCurrent();
			return ov.active;
		}
		
		
		public String color() {
			Overlay ov = getCurrent();
			return ov.color;
		}
		
		// takes into account that idx is increased after calling next
		protected Overlay getCurrent() {
			Overlay ov = overlay.get(ov_active.get(idx == 0 ? 0 : idx - 1));
			
			return ov;
		}
				
		public void remove() {
		}
		
	}
	
	
	
	protected class Unique {
		protected HashMap<String,Integer> unique = new HashMap<String,Integer>();
		protected Integer dummy = new Integer(1);
		
		public void addTerm(String term) {
			if ( unique.containsKey(term) == false ) {
				unique.put(term, dummy);
			}
		}
		
		public Vector<String> getUnique() {
			if ( unique.size() == 0 ) {
				return null;
			}
			
			Vector<String> un = new Vector<String>(unique.size());
			for ( String key : unique.keySet() ) {
				un.add(key);
			}
			return un;
		}
		
	}
	
	
	
	public class SOMstruct {
		public Integer id;
		public float x;
		public float y;
		public String name;
	}
	
	
	protected class Library {
		int max;
		String [] variant_names;
		String color;
		int color_rank;
		String [] color_similar;
		String type;
		String decorator;
	}
	
	protected class Overlay {
		boolean active;
		// boolean pending; // RPC request pending
		String name;
		int variant;
		int [] ids;
		int [] values;
		String color;
		Group vectorGroup;
		float zoom; 
	}
	
	
	
}
