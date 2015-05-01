package org.bdgp.somviewer.server.data;

import java.sql.ResultSet;
import java.util.Vector;

import org.bdgp.somviewer.server.DBbase;
import org.bdgp.somviewer.server.DBbase.LogSeverity;

public class SOMInfoGeneric implements SOMinfo {

	private final String st_qmaps = "select distinct(name) from somtitle";
	protected final String st_qssinfo = "select html, width from sominfo_short, somstruct, somtitle where sominfo_short.somstruct_id = __ID and (sominfo_short.variant = 0 or sominfo_short.variant = __VAR)";

	protected DBbase db;
	
	protected int last_id, last_variant;
	protected String html;
	protected int width;
	
	public SOMInfoGeneric(DBbase db) {
		this.db = db;
	}

	public Vector<String> somMaps() throws Exception {
		
		Vector<String> maps = new Vector<String>(2);
		
		try {
			ResultSet rs = db.query(st_qmaps);

			if ( rs == null ) {
				db.logEvent(this, LogSeverity.WARN, "No result set returned");
				return null;
			}
			
			while (rs.next()) {	
				maps.add(rs.getString(1));
				db.logEvent(this, LogSeverity.INFO, "maps=" + rs.getString(1));								
			}
		}
		catch (Exception e) {
			db.logEvent(this, LogSeverity.ERROR, "Exception: " + e.getMessage());
			throw e;
		}

		return maps;
	}
	

	public String shortInfo(int id, int variant) throws Exception {

		// See if we ran this query already
		if ( id == last_id || variant == last_variant )
			return html;
		else
			html = null;
		
		String fquery = st_qssinfo.replace("__ID", new Integer(id).toString());
		fquery = fquery.replace("__VAR", new Integer(id).toString());
		
		try {
			ResultSet rs = db.query(fquery);
			if ( rs == null ) {
				db.logEvent(this, LogSeverity.WARN, "No information returned");
				return null;
			}
			
			rs.next();	
			html = rs.getString(1);
			width = rs.getInt(2);

			db.logEvent(this, LogSeverity.INFO, "maps=" + rs.getString(1));								
			
		}
		catch (Exception e) {
			db.logEvent(this, LogSeverity.ERROR, "Exception: " + e.getMessage());
			throw e;
		}
		
		last_id = id;
		last_variant = variant;
		
		return html;
	}

	public String longInfo(int id, int variant) throws Exception {
		// Not implemented in client at this point
		return null;
	}

	public int getShortInfoWidth(int id, int variant) throws Exception {
		if ( id != last_id || variant != last_variant )
			html = null;
		if ( html == null )
			shortInfo(id, variant);
		return width;
	}

}
