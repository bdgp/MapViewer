package org.bdgp.somviewer.server.data;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Vector;

import org.bdgp.somviewer.rpc.data.SOMDataPts;
import org.bdgp.somviewer.rpc.data.SOMOverlaysAvailable;
import org.bdgp.somviewer.server.DBbase;
import org.bdgp.somviewer.server.DBbase.LogSeverity;

public class SOMoverlay {
	
	private final String st_title_id = "select id from somtitle where name = ";
	private final String st_availoverlays = "select distinct(name), max(variant), color, type, decorator from somoverlay_info where somtitle_id = __ID  group by name";	
	private final String st_availvariants = "select distinct(variant), variant_name, type from somoverlay_info where somtitle_id = __ID order by variant";
	private final String st_overlay = "select somstruct_id, value from somoverlay so, somoverlay_info si where so.somoverlay_info_id = si.id and si.name = '__NAME' and si.variant = __VAR and si.somtitle_id = __ID";
	
	protected DBbase db;
	
	protected String somtitle = null;
	protected int som_id = 0;
	protected Vector<Available> available = null;
	protected HashMap<String, Vector<String>> variants = null;
	
	public SOMoverlay(DBbase db, String title) throws Exception {
		this.db = db;
		somtitle = title;
		getTitleID();
	}
	
	protected void getTitleID() throws Exception {
		
		boolean success = true;
		String fquery = st_title_id + "'" + somtitle + "'";
		ResultSet rs = db.query(fquery);
		
		if ( rs == null ) {
			db.logEvent(this, LogSeverity.WARN, "No result set returned");
			success = false;
		}
		else {
			rs.next();
			som_id = rs.getInt(1);
			if ( som_id == 0 ) {
				db.logEvent(this, LogSeverity.WARN, "No id returned");
				success = false;
			}
		}
		
		if ( success == false )
			throw new Exception("SOM map doesn't exist. Name given " + somtitle);
	}
	
	public String getTitle() {
		return somtitle;
	}
	
	
	
	public void queryAvailable() throws Exception {		
		
		// Check if we queried the data already
		if ( available != null ) {
			return;
		}
		
		getVariants();
		
		String fquery = st_availoverlays.replace("__ID", new Integer(som_id).toString());
		db.logEvent(this, LogSeverity.INFO, fquery);
		
		available = new Vector<Available>(10);
		
		try {
			ResultSet rs = db.query(fquery);

			if ( rs == null ) {
				db.logEvent(this, LogSeverity.WARN, "No result set returned");
				return;
			}
			
			while (rs.next()) {
				
				Available av = new Available();
				
				av.name = rs.getString(1);
				av.variant = rs.getInt(2);
				av.color = rs.getString(3);
				av.type = rs.getString(4);
				av.decorator = rs.getString(5);
				
				if ( variants.containsKey(av.type) ) {
					av.variant_names = variants.get(av.type);
					if ( av.variant_names.size() == 0 ) {
						av.variant_names = null;
					}
				}
				
				db.logEvent(this, LogSeverity.INFO, "Name=" + av.name + ", Variant=" + av.variant + ", Colormap=" + av.color);
				
				if ( av.name != null ) {
					available.add(av);
				}
				
			}
		}
		catch (Exception e) {
			db.logEvent(this, LogSeverity.ERROR, "Exception: " + e.getMessage());
			available = null;
			throw e;
		}
		
		return;
	}
	
	
	private void getVariants() throws Exception {

		String fquery = st_availvariants.replace("__ID", new Integer(som_id).toString());
		db.logEvent(this, LogSeverity.INFO, fquery);

		try {
			ResultSet rs = db.query(fquery);

			if ( rs == null ) {
				db.logEvent(this, LogSeverity.WARN, "No result set returned");
				return;
			}
			
			variants = new HashMap<String, Vector<String>>();
			
			while (rs.next()) {
				String type = rs.getString(3);
				Integer order = rs.getInt(1);
				String desc = rs.getString(2);
				
				Vector<String> var_desc = null;
				if ( variants.containsKey(type)) {
					var_desc = variants.get(type);
				} else {
					var_desc = new Vector<String>(order+10);
					variants.put(type, var_desc);
				}
				if ( order >= var_desc.size() ) {
					var_desc.setSize(order+10);
				}
				var_desc.add(order, desc);				
			}
		}
		catch (Exception e) {
			db.logEvent(this, LogSeverity.ERROR, "Exception: " + e.getMessage());
			available = null;
			throw e;
		}
		
	}
	
	
	public boolean overlayExists(String name) throws Exception {
		if ( available == null ) {
			queryAvailable();
		}
		
		if ( available == null )
			return false;
		
		for ( Available av : available ) {
			if ( av.name.compareTo(name) == 0 )
				return true;
		}
		
		return false;
	}
	
	
	
	public int [] queryOverlay(String name, int variant) throws Exception {
		
		if ( overlayExists(name) == false ) {
			throw new Exception("Overlay doesn't exist: " + name);
		}
		
		String fquery = st_overlay.replace("__NAME", name);
		fquery = fquery.replace("__VAR", new Integer(variant).toString());
		fquery = fquery.replace("__ID", new Integer(som_id).toString());
		
		ResultSet rs = db.query(fquery);

		if ( rs == null ) {
			db.logEvent(this, LogSeverity.WARN, "No result set returned");
			return null;
		}
		
		Vector<Integer> ov_id = new Vector<Integer>(50);
		
		while (rs.next()) {
			
			ov_id.add(new Integer(rs.getInt(1)));
						
		}
		
		db.logEvent(this, LogSeverity.INFO, "Received " + ov_id.size() + " overlays");
		
		int [] ov = new int[ov_id.size()];
		
		for ( int i = 0; i < ov_id.size(); i++ ) {
			ov[i] = ov_id.get(i);
		}
		
		return ov;
		
	}
	

	
	public SOMDataPts availData(SOMDataPts pts) {
		if ( available == null || pts == null )
			return null;
		
		pts.available = new Vector<SOMOverlaysAvailable>(available.size());
		
		for ( int i=0; i < available.size(); i++ ) {
			String [] var_array  =  new String[ available.get(i).variant_names.size() ];
			for ( int j = 0; j < available.get(i).variant_names.size(); j++ )
					var_array[j] = available.get(i).variant_names.get(j);
			pts.available.add(pts.CreateAvailable(available.get(i).name, available.get(i).variant, var_array, available.get(i).color, available.get(i).type, available.get(i).decorator));
		}
		
		return pts;
	}
	
	
	protected class Available {
		String name;
		int variant;
		Vector<String> variant_names;
		String color;
		String type;
		String decorator;
	}
	
}
