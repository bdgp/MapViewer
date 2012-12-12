package org.bdgp.somviewer.server;


import java.util.Vector;

import org.bdgp.somviewer.client.GreetingService;
import org.bdgp.somviewer.rpc.SOMDataService;
import org.bdgp.somviewer.rpc.data.SOMDataOverlay;
import org.bdgp.somviewer.rpc.data.SOMDataPts;
import org.bdgp.somviewer.rpc.data.SOMList;
import org.bdgp.somviewer.rpc.data.SOMPtInfo;
import org.bdgp.somviewer.server.DBbase.LogSeverity;
import org.bdgp.somviewer.server.data.InsituDatabase;
import org.bdgp.somviewer.server.data.SOMinfo;
import org.bdgp.somviewer.server.data.SOMoverlay;
import org.bdgp.somviewer.server.data.SOMstructure;
import org.bdgp.somviewer.shared.FieldVerifier;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class SOMServiceImpl extends RemoteServiceServlet implements
		GreetingService, SOMDataService {

	DBMySQL db = new DBMySQL("localhost", "insitu", "erwin", "", DBbase.Connectivity.BONECP);
	
	Vector<String> sommaps = null;
	SOMstructure somstruct = null; // Could be a hashtable for saved queries
	SOMoverlay overlay = null;
	SOMinfo info = null;
	
	static boolean queryInProgress = false;
	
	public SOMServiceImpl() {
		
		info = new InsituDatabase(db);
		
	}
	
	
	private boolean manageQuery(boolean start) {
		
		// Check if db allows for concurrent connections - if yes, no need to hold
		if ( db.concurrentConnections() ) {
			return true;
		}
		
		if ( start ) {
			// make sure we're only running one simultaneous query
			try {
				while ( queryInProgress == true ) {
					Thread.sleep(10);
				}
				queryInProgress = true;
				db.logClear(); // clear log from previous query - doesn't apply now
			}
			catch (Exception e) {
				queryInProgress = false;
				return false;
			}
		} else {
			queryInProgress = false;
		}
		return true;
	}
	
	
	public SOMDataPts getSOMDataPts(String datasrc) {
		
		SOMDataPts sd = new SOMDataPts();
		sd.requested = datasrc;
		
		if ( somstruct == null ) {
			somstruct = new SOMstructure(db);
		}

		// Avoid injection attacks by making sure name exists in database
		String map = verifyMapname(datasrc);
		
		manageQuery(true);
		
		try {
						
			if ( map != null ) {
				somstruct.runQuery(map);
				if ( overlay == null || overlay.getTitle().compareTo(map) != 0 ) {
					overlay = new SOMoverlay(db, map);
					overlay.queryAvailable();
				}
			}
			else
				sd.queryResult = "SOM map \"" + datasrc + "\" doesn't exist in database";
		}
		catch (Exception e) {
			sd.queryResult = "Exception: " + e.getMessage();
		}
		
		manageQuery(false);
		
		if ( sd.queryResult == null ) {
			sd.id = somstruct.getId();
			sd.x = somstruct.getX();
			sd.y = somstruct.getY();
			sd.names = somstruct.getNames();
			sd.map = new String(datasrc);
			
			overlay.availData(sd);
		} else {
			sd.queryResult += db.flatLog(LogSeverity.ALL);
		}
		
		return sd;
	}
	
	
	public SOMList getSOMmaps() {
		
		SOMList sl = new SOMList();
		
		manageQuery(true);
		
		try {
			sommaps = info.somMaps();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			sl.queryResult = "Exception: " + e.getMessage();
		}
		
		manageQuery(false);
		
		sl.entries = sommaps;
		
		return sl;
		
	}

	
	public SOMDataOverlay getDataOverlay(String datasrc, String name, int variant) {
		SOMDataOverlay so = new SOMDataOverlay();
		
		// Avoid injection attacks by making sure name exists in database
		String map = verifyMapname(datasrc);
		
		manageQuery(true);
		
		try {
			if ( overlay == null || overlay.getTitle().compareTo(map) != 0 ) {
				overlay = new SOMoverlay(db, map);
			}
			
			so.id = overlay.queryOverlay(name, variant);
			so.values = overlay.queryOverlayValues(name, variant);
			if ( so.values != null ) {
				so.val_min = overlay.queryOverlayMinValue(name, variant);
				so.val_max = overlay.queryOverlayMaxValue(name, variant);
			}
			
		}
		catch (Exception e) {
			so.queryResult = "Exception: " + e.getMessage();
		}
		
		manageQuery(false);
		
		if ( so.id != null ) {
			so.name = name;
			so.variant = variant;
		} else {
			so.queryResult += db.flatLog(LogSeverity.ALL);
		}
		
		
		return so;
	}
	
	
	public SOMPtInfo getPtInfo(int id, int variant) {
		SOMPtInfo pti = new SOMPtInfo();
		String html = new String();
		int info_width = 0;
		
		// Avoid injection attacks by making sure name exists in database
		// String map = verifyMapname(datasrc);
		
		manageQuery(true);
		
		try {
			html = info.shortInfo(id, variant);
			info_width = info.getShortInfoWidth();
		}
		catch (Exception e) {
			pti.queryResult = "Exception: " + e.getMessage() + db.flatLog(LogSeverity.ALL);
		}
		
		manageQuery(false);
		
		pti.html_Sinfo = html;
		pti.width_Sinfo = info_width;
		pti.req_id = id;
		pti.req_variant = variant;
		
		return pti;
	}
	
	
	protected String verifyMapname(String map) {
		if ( sommaps == null )
			getSOMmaps();

		if ( sommaps == null ) {
			return null;
		}
		
		for ( String m : sommaps ) {
			if ( m.compareTo(map) == 0 ) {
				return m;
			}
		}
		
		return null;
	}
	

	public String greetServer(String input) throws IllegalArgumentException {
		// Verify that the input is valid. 
		if (!FieldVerifier.isValidName(input)) {
			// If the input is not valid, throw an IllegalArgumentException back to
			// the client.
			throw new IllegalArgumentException(
					"Name must be at least 4 characters long");
		}

		String serverInfo = getServletContext().getServerInfo();
		String userAgent = getThreadLocalRequest().getHeader("User-Agent");

		// Escape data from the client to avoid cross-site script vulnerabilities.
		input = escapeHtml(input);
		userAgent = escapeHtml(userAgent);

		return "Hello, " + input + "!<br><br>I am running " + serverInfo
				+ ".<br><br>It looks like you are using:<br>" + userAgent;
	}

	/**
	 * Escape an html string. Escaping data received from the client helps to
	 * prevent cross-site script vulnerabilities.
	 * 
	 * @param html the html string to escape
	 * @return the escaped string
	 */
	private String escapeHtml(String html) {
		if (html == null) {
			return null;
		}
		return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;");
	}
}
