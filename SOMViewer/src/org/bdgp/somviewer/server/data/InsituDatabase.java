package org.bdgp.somviewer.server.data;

import java.sql.ResultSet;
import java.util.Vector;

import org.bdgp.somviewer.server.DBbase;
import org.bdgp.somviewer.server.DBbase.LogSeverity;

public class InsituDatabase extends SOMInfoGeneric implements SOMinfo {

	private final String st_im_info = "select image.image_path from som2main, annot, image where annot.main_id = som2main.main_id and image.annot_id = annot.id and annot.stage = __STAGE and som2main.somstruct_id = __ID";
	private final String st_term_info = "select term.go_term from som2main, annot, annot_term, term where annot.main_id = som2main.main_id and annot_term.annot_id = annot.id and annot.stage = __STAGE and annot_term.term_id = term.id and som2main.somstruct_id = __ID";
	private final String st_id2name = "select main.flybase_name, main.flybase_id, main.est_id, main.cgname from main, som2main where som2main.main_id = main.id and som2main.somstruct_id = __ID";
	
	private final String im_url = "http://insitu.fruitfly.org/insitu_image_storage/thumbnails/";
	private final String bdgp_url = "http://insitu.fruitfly.org/cgi-bin/ex/report.pl?ftype=1&ftext=";

	private final int THUMBNAIL_WIDTH = 250;
	
	
	public InsituDatabase(DBbase db) {
		super(db);
	}		

	
	public String shortInfo(int id, int variant) throws Exception {
		Vector<String> impaths = new Vector<String>(5);
		Vector<String> terms = new Vector<String>(5);
		
		String sym, fbgn, est_id, cgname;
		
		String html = new String();
		
		// Adjust for variant numbering
		variant++;
		
		String nquery = st_id2name.replace("__ID", new Integer(id).toString());
		
		String imquery = st_im_info.replace("__ID", new Integer(id).toString());
		imquery = imquery.replace("__STAGE", new Integer(variant).toString());
		
		String tquery = st_term_info.replace("__ID", new Integer(id).toString());
		tquery = tquery.replace("__STAGE", new Integer(variant).toString());
		
		try {
			ResultSet rs;
			
			rs = db.query(nquery);			
			if ( rs == null ) {
				sym = new String();
				fbgn = new String();
				est_id = new String();
				cgname = new String();
				db.logEvent(this, LogSeverity.WARN, "No gene information result set returned");
			}
			else
			{
				rs.next();
				sym = rs.getString(1);
				fbgn = rs.getString(2);
				est_id = rs.getString(3);
				cgname = rs.getString(4);
				db.logEvent(this, LogSeverity.INFO, "sym=" + rs.getString(1));								
						
			}
		
			if ( variant > 0 ) {
				rs = db.query(imquery);
				if ( rs == null ) {
					db.logEvent(this, LogSeverity.WARN, "No image result set returned");
				}
				else
				{
					while (rs.next()) {	
						impaths.add(rs.getString(1));
						db.logEvent(this, LogSeverity.INFO, "impath=" + rs.getString(1));								
					}				
				}

				rs = db.query(tquery);			
				if ( rs == null ) {
					db.logEvent(this, LogSeverity.WARN, "No term result set returned");
				}
				else
				{
					while (rs.next()) {	
						terms.add(rs.getString(1));
						db.logEvent(this, LogSeverity.INFO, "term=" + rs.getString(1));								
					}				
				}
			}

		}
		catch (Exception e) {
			db.logEvent(this, LogSeverity.ERROR, "Exception: " + e.getMessage());
			throw e;
		}
		
		if ( sym.length() == 0 ) {
			html += "<h3>Error: Not found in database</h3>";
			return html;
		}
		
		
		// html += sym + " (Stage " + resolveStage(variant) + ")<BR>";
		html += "<a href=\"" + bdgp_url + cgname + "\" target=\"_blank\">" + cgname + ":insitu</a> ";
		html += "<a href=\"http://flybase.org/reports/" + fbgn + ".html\" target=\"_blank\">" + fbgn + "</a><br>";
		html += "Stage " + resolveStage(variant);
		html += "<P>";
		if ( variant > 0 ) {
			for ( String im : impaths )
				html += "<img src=\"" + im_url + im + "\" alt=\"" + est_id + "\"/><BR>";
			html += "<P>";
			for ( String t : terms )
				html += t + "<BR>";
		} else {
			html += "<B>No stage selected!</B>";
			html += "Quick view supports only one stage.";
		}
		
		return html;
	}

	public int getShortInfoWidth(int id, int variant) throws Exception {
		// id & variant are ignored here - width is pretty much consistent
		return THUMBNAIL_WIDTH;
	}
	
	
	public String longInfo(int id, int variant) {
		return null;
	}
	
	private String resolveStage(int stage) {
		String stString = "unknown";
		
		switch (stage) {
		case 1:
			stString="0-3";
			break;
		case 2:
			stString="4-6";
			break;
		case 3:
			stString="7-8";
			break;
		case 4:
			stString="9-10";
			break;
		case 5:
			stString="11-12";
			break;
		case 6:
			stString="13-16";
			break;
		}
		
		return stString;
	}

}
