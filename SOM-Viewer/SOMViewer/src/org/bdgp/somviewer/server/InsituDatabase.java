package org.bdgp.somviewer.server;

import java.sql.ResultSet;
import java.util.Vector;

public class InsituDatabase extends DBMySQL {

	private final String st_qmaps = "select distinct(name) from somtitle";
	
	private final String st_im_info = "select image.image_path from som2main, annot, image where annot.main_id = som2main.main_id and image.annot_id = annot.id and annot.stage = __STAGE and som2main.somstruct_id = __ID";
	private final String st_term_info = "select term.go_term from som2main, annot, annot_term, term where annot.main_id = som2main.main_id and annot_term.annot_id = annot.id and annot.stage = __STAGE and annot_term.term_id = term.id and som2main.somstruct_id = __ID";
	private final String st_id2name = "select main.flybase_name, main.flybase_id, main.est_id from main, som2main where som2main.main_id = main.id and som2main.somstruct_id = __ID";
	
	private final String im_url = "http://insitu.fruitfly.org/insitu_image_storage/thumbnails/";

	
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
	

	public String shortInfo(int id, int variant) throws Exception {
		Vector<String> impaths = new Vector<String>(5);
		Vector<String> terms = new Vector<String>(5);
		
		String sym, fbgn, est_id;
		
		String html = new String();
		
		String nquery = st_id2name.replace("__ID", new Integer(id).toString());
		
		String imquery = st_im_info.replace("__ID", new Integer(id).toString());
		imquery = imquery.replace("__STAGE", new Integer(variant).toString());
		
		String tquery = st_term_info.replace("__ID", new Integer(id).toString());
		tquery = tquery.replace("__STAGE", new Integer(variant).toString());
		
		try {
			ResultSet rs;
			
			rs = query(nquery);			
			if ( rs == null ) {
				sym = new String();
				fbgn = new String();
				est_id = new String();
				logEvent(this, DBbase.WARN, "No image result set returned");
			}
			else
			{
				rs.next();
				sym = rs.getString(1);
				fbgn = rs.getString(2);
				est_id = rs.getString(3);
				logEvent(this, DBbase.INFO, "impath=" + rs.getString(1));								
						
			}
		
			
			rs = query(imquery);
			if ( rs == null ) {
				logEvent(this, DBbase.WARN, "No image result set returned");
			}
			else
			{
				while (rs.next()) {	
					impaths.add(rs.getString(1));
					logEvent(this, DBbase.INFO, "impath=" + rs.getString(1));								
				}				
			}

			rs = query(tquery);			
			if ( rs == null ) {
				logEvent(this, DBbase.WARN, "No image result set returned");
			}
			else
			{
				while (rs.next()) {	
					terms.add(rs.getString(1));
					logEvent(this, DBbase.INFO, "term=" + rs.getString(1));								
				}				
			}

		}
		catch (Exception e) {
			logEvent(this, DBbase.ERROR, "Exception: " + e.getMessage());
			throw e;
		}
		
		if ( sym.length() == 0 ) {
			html += "<h3>Error: Not found in database</h3>";
			return html;
		}
		
		
		html += "<h3>" + sym + "</h3>";
		for ( String im : impaths )
			html += "<img src=\"" + im_url + im + "\" alt=\"" + est_id + "\"/><BR>";
		html += "<P>";
		for ( String t : terms )
			html += t + "<BR>";
		
		return html;
	}

	
	public String longInfo(int id, int variant) {
		return null;
	}
	
}
