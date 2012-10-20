package org.bdgp.somviewer.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.bdgp.somviewer.rpc.data.SOMDataPts;
import org.bdgp.somviewer.rpc.data.SOMOverlaysAvailable;
import org.vaadin.gwtgraphics.client.Group;

public class SOMData {
	
	protected static final int MAX_OVERLAY = 30;
	
	protected String map_name;
	protected HashMap<String,Overlay> overlay;
	protected HashMap<Integer,SOMstruct> data;
	protected HashMap<String,Library> library;
	protected SOMDataPts pts;
	protected Group data_grp = null;
	protected float zoom;
	
	public SOMData(SOMDataPts pts) {
		this.pts = pts;
		
		map_name = pts.map;
		data = new HashMap<Integer, SOMstruct>();
		library = new HashMap<String, Library>();
		
		for ( int i = 0; i < pts.id.length; i++ ) {
			SOMstruct sd = new SOMstruct();
			sd.x = pts.x[i];
			sd.y = pts.y[i];
			sd.name = pts.names[i];
			data.put(new Integer(pts.id[i]), sd);
		}
		
		Vector<SOMOverlaysAvailable> av = pts.available;
		for ( SOMOverlaysAvailable a : av ) {
			Library l = new Library();
			l.color = a.color;
			l.max = a.variant;
			library.put(a.name,l);
		}
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
	
	public Vector<String> getOverlayNames() {
		
		Vector<String> names = new Vector<String>(library.size());
		
		for (Map.Entry<String, Library> entry : library.entrySet()) {
		    names.add(entry.getKey());
		}

		return names;
	}
	
	
	public void setZoom(float zoom) {
		if ( this.zoom != zoom ) {
			data_grp = null;
		}
		this.zoom = zoom;
	}
	

	public Iterator<SOMstruct> getData() {
		IterateCoordinates ic = new IterateCoordinates();
		return ic;
	}
	
	
	public void setDataCanvasGroup(Group grp, float zoom) {
		data_grp = grp;
		this.zoom = zoom;
	}
	
	
	public Group getDataCanvasGroup() {
		return data_grp;
	}
	
	public void invalidateCanvasGroup() {
		data_grp = null;
	}
	
	
	public OverlayIterator getOverlays() {
		OverlayIterator oi = new IterateOverlays();
		return oi;
	}
	
	public void addOverlay(String name, int variant, int [] id, String col) {
		if ( overlay == null ) {
			overlay = new HashMap<String,Overlay>();
		}
		
		String uuid = overlay2uuid(name, variant);
		
		if ( overlay.containsKey(uuid))
			return;
		
		Library l = library.get(name);
		if ( l == null ) {
			throw new NoSuchElementException();
		}
		
		Overlay ovn = new Overlay();
		ovn.active = true;
		ovn.ids = id;
		ovn.vectorGroup = null;
		ovn.color = col != null? col : l.color;
		ovn.zoom = zoom;
		
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
	
	public void setOverlayActive(String name, int variant) {		
		if ( overlay == null )
			return;
		
		overlay.get(overlay2uuid(name, variant)).active = true;
	}
	
	public void setOverlayInactive(String name) {
		Library l = library.get(name);
		if ( l == null ) {
			throw new NoSuchElementException();
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

		overlay.get(overlay2uuid(name, variant)).active = false;
	}
	
	// Get rid of all inactive overlays
	protected void pruneOverlays() {
		if ( overlay == null )
			return;
		
		Iterator<Map.Entry<String, Overlay>> alloverlays = overlay.entrySet().iterator();
		
		while (alloverlays.hasNext()) {
			Map.Entry<String, Overlay> entry = (Map.Entry<String, Overlay>) alloverlays.next();
			Overlay ov = (Overlay) entry.getValue();
			if ( ov.active == false )
				alloverlays.remove();
		}
	}
	
	
	public class IterateCoordinates implements Iterator<SOMstruct> {
		protected Overlay ov;
		protected int idx;
		protected Iterator<Map.Entry<Integer, SOMstruct>> alldata;
		
		public IterateCoordinates() {
			ov = null;
			alldata = data.entrySet().iterator();
		}
		
		public IterateCoordinates(String uuid) {
			if ( overlay == null )
				throw new NoSuchElementException();
			
			ov = overlay.get(uuid);
			if ( ov == null )
				throw new NoSuchElementException();			
			
			alldata = null;
			idx = 0;
		}
		
		
		public boolean hasNext() {
			
			if ( ov == null ) {
				return alldata.hasNext();
			}
			if ( idx < ov.ids.length ) {
				return true;
			}
			return false;
		}

		public SOMstruct next() {
			
			if ( ov == null ) {
			    Map.Entry<Integer, SOMstruct> entry = (Map.Entry<Integer, SOMstruct>) alldata.next();
			    SOMstruct value = (SOMstruct) entry.getValue();
				return value;
			}
			
			if ( idx >= ov.ids.length )
				throw new NoSuchElementException();
			
			SOMstruct value = data.get(ov.ids[idx]);
			idx++;
			
			return value;
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
	
	
	public class SOMstruct {
		public float x;
		public float y;
		public String name;
	}
	
	
	protected class Library {
		int max;
		String color;
	}
	
	protected class Overlay {
		boolean active;
		int [] ids;
		String color;
		Group vectorGroup;
		float zoom; 
	}
	
	
	
}
