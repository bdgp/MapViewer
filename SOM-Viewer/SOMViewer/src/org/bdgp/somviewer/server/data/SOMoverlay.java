package org.bdgp.somviewer.server.data;

import java.sql.ResultSet;
import java.util.Vector;

import org.bdgp.somviewer.rpc.data.SOMDataPts;
import org.bdgp.somviewer.rpc.data.SOMOverlaysAvailable;
import org.bdgp.somviewer.server.DBbase;

public class SOMoverlay {
	
	private final String st_title_id = "select id from somtitle where name = ";
	private final String st_availoverlays = "select distinct(name), max(variant), color from somoverlay_info where somtitle_id = __ID  group by name";	
	private final String st_overlay = "select somstruct_id from somoverlay so, somoverlay_info si where so.somoverlay_info_id = si.id and si.name = '__NAME' and si.variant = __VAR and si.somtitle_id = __ID";
	
	protected DBbase db;
	
	protected String somtitle = null;
	protected int som_id = 0;
	protected Vector<Available> available = null;
	
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
			db.logEvent(this, DBbase.WARN, "No result set returned");
			success = false;
		}
		else {
			rs.next();
			som_id = rs.getInt(1);
			if ( som_id == 0 ) {
				db.logEvent(this, DBbase.WARN, "No id returned");
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
		
		String fquery = st_availoverlays.replace("__ID", new Integer(som_id).toString());
		db.logEvent(this, DBbase.INFO, fquery);
		
		available = new Vector<Available>(10);
		
		try {
			ResultSet rs = db.query(fquery);

			if ( rs == null ) {
				db.logEvent(this, DBbase.WARN, "No result set returned");
				return;
			}
			
			while (rs.next()) {
				
				Available av = new Available();
				
				av.name = rs.getString(1);
				av.variant = rs.getInt(2);
				av.color = rs.getString(3);
				
				db.logEvent(this, DBbase.INFO, "Name=" + av.name + ", Variant=" + av.variant + ", Colormap=" + av.color);
				
				if ( av.name != null ) {
					available.add(av);
				}
				
			}
		}
		catch (Exception e) {
			db.logEvent(this, DBbase.ERROR, "Exception: " + e.getMessage());
			available = null;
			throw e;
		}
		
		return;
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
			db.logEvent(this, DBbase.WARN, "No result set returned");
			return null;
		}
		
		Vector<Integer> ov_id = new Vector<Integer>(50);
		
		while (rs.next()) {
			
			ov_id.add(new Integer(rs.getInt(1)));
						
		}
		
		db.logEvent(this, DBbase.INFO, "Received " + ov_id.size() + " overlays");
		
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
			pts.available.add(pts.CreateAvailable(available.get(i).name, available.get(i).variant, available.get(i).color));
		}
		
		return pts;
	}
	
	
	protected class Available {
		String name;
		int variant;
		String color;
	}
	
}
