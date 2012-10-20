package org.bdgp.somviewer.server;

import java.sql.ResultSet;
import java.util.Vector;

public class InsituDatabase extends DBMySQL {

	private final String st_qmaps = "select distinct(name) from somtitle";
	
	
	public InsituDatabase(String host, String db, String user, String password) {
		super(host, db, user, password);
	}

	
	// non complex queries
	// queries with multiple data points are in server.data
	
	public Vector<String> somMaps() throws Exception {
		
		Vector<String> maps = new Vector<String>(2);
		
		try {
			ResultSet rs = query(st_qmaps);

			if ( rs == null ) {
				logEvent(this, DBbase.WARN, "No result set returned");
				return null;
			}
			
			while (rs.next()) {	
				maps.add(rs.getString(1));
				logEvent(this, DBbase.INFO, "maps=" + rs.getString(1));								
			}
		}
		catch (Exception e) {
			logEvent(this, DBbase.ERROR, "Exception: " + e.getMessage());
			throw e;
		}

		return maps;
	}
	
	public Vector<String> somOverlays()	{
		return null;
	}
	
	public Vector<String> somVariants() {
		return null;
	}
	
}
