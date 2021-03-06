package org.bdgp.somviewer.server.data;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Vector;

import org.bdgp.somviewer.server.DBbase;
import org.bdgp.somviewer.server.DBbase.LogSeverity;

public class SOMstructure {

	protected final String st_som = "select somstruct.id, somstruct.x, somstruct.y, somstruct.name from somstruct, somtitle where somstruct.somtitle_id = somtitle.id and somtitle.name = ";
	
	protected DBbase db;
	
	protected String somtitle = null;
	protected Vector<SOMData> somdata = null;
	
	
	public SOMstructure(DBbase db) {
		this.db = db;
	}
		
	public void runQuery(String q_name) throws Exception {		
		
		// Check if we queried the data already
		if ( somtitle != null ) {
			if ( somtitle.compareTo(q_name) == 0 && somdata != null )
				return;
		}
		
		String fquery = st_som + "'" + q_name + "'";
		db.logEvent(this, LogSeverity.INFO, fquery);
		
		somdata = new Vector<SOMData>(10);
		
		try {
			ResultSet rs = db.query(fquery);

			if ( rs == null ) {
				db.logEvent(this, LogSeverity.WARN, "No result set returned");
				somdata = null;
				return;
			}
			
			while (rs.next()) {
				
				SOMData sd = new SOMData();
				
				sd.id = rs.getInt(1);
				sd.x = rs.getFloat(2);
				sd.y = rs.getFloat(3);
				sd.name = rs.getString(4);
				db.logEvent(this, LogSeverity.INFO, "id=" + sd.id + ": x=" + sd.x + ",y=" + sd.y + ", name=" + sd.name);
				
				if ( sd.name != null ) {
					somdata.add(sd);
				}
				
			}
		}
		catch (Exception e) {
			db.logEvent(this, LogSeverity.ERROR, "Exception: " + e.getMessage());
			somdata = null;
			throw e;
		}
		
		return;
	}
	

	public int [] getId() {
		if ( somdata == null )
			return null;
		
		int [] id = new int[somdata.size()];
		
		for ( int i=0; i < somdata.size(); i++ ) {
			id[i] = somdata.get(i).id;
		}
		
		return id;
	}

	
	public float [] getX() {
		if ( somdata == null )
			return null;
		
		float [] x = new float[somdata.size()];
		
		for ( int i=0; i < somdata.size(); i++ ) {
			x[i] = somdata.get(i).x;
		}
		
		return x;
	}
	
	public float [] getY() {
		if ( somdata == null )
			return null;
		
		float [] y = new float[somdata.size()];
		
		for ( int i=0; i < somdata.size(); i++ ) {
			y[i] = somdata.get(i).y;
		}
		
		return y;
	}

	public String [] getNames() {
		if ( somdata == null )
			return null;
		
		String [] name = new String[somdata.size()];
		
		for ( int i=0; i < somdata.size(); i++ ) {
			name[i] = new String(somdata.get(i).name);
		}
		
		return name;
	}


	protected class SOMData {
		Integer id;
		float x, y;
		String name;
	}
	
	
}
