/**
 * 
 */
package org.bdgp.somviewer.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Vector;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

/**
 * @author erwin
 *
 */
public abstract class DBbase {
	
	public enum Connectivity {DIRECT, BONECP};
	
	protected Connection con;
	protected Statement stmt;
	protected String url, user, password;
	protected Hashtable<String, PreparedStatement> pstmt = new Hashtable<String, PreparedStatement>();
	protected Vector<DBLog> query_log = new Vector<DBLog>();
	public boolean active = false;
	public String status;
	
	protected BoneCP connectionPool = null;
	protected Connectivity db_connect = Connectivity.DIRECT;
	
	public enum LogSeverity {INFO, WARN, ERROR, ALL};
	
	public DBbase() {
		
		initConn();
		connect();
	}
	
	
	public DBbase(String url, String user, String password) {
		
		this.url = url;
		this.user = user;
		this.password = password;
		
		initConn();
		connect();
		
	}

	
	public DBbase(String url, String user, String password, Connectivity con) {
		
		this.url = url;
		this.user = user;
		this.password = password;
		this.db_connect = con;
		
		initConn();
		
		if ( db_connect == Connectivity.DIRECT )
			connect();
		else if ( db_connect == Connectivity.BONECP ) {
			connectBoneJ();
		}
		
	}

	
	
	public abstract void initConn();
	
	
	public boolean connect() {
		try {
			con = DriverManager.getConnection(url, user, password);
			stmt = con.createStatement();
		}
		catch(Exception e) {
			status = e.getMessage() + " for " + url;
			return false;
		}
		
		status = "Successful connection";
		active = true;
		
		return true;
	}
	
	
	public boolean connectBoneJ() {
		
		try {
			BoneCPConfig config = new BoneCPConfig();
			config.setJdbcUrl(this.url); // jdbc url specific to your database, eg jdbc:mysql://127.0.0.1/yourdb
			config.setUsername(user); 
			config.setPassword(password);
			config.setMinConnectionsPerPartition(5);
			config.setMaxConnectionsPerPartition(10);
			config.setPartitionCount(1);
			connectionPool = new BoneCP(config); // setup the connection pool			
		} catch (SQLException e) {
			status = e.getMessage() + " for " + url;
			return false;
		}

		status = "Successful connection";
		active = true;
		
		return true;
	}
	
	
	public boolean concurrentConnections() {
		
		if ( db_connect == Connectivity.BONECP ) {
			return true;
		}
		
		return false;
	}
	
	
	public PreparedStatement prepStatement(String id, String qstmt) throws SQLException {
		
		PreparedStatement pst;
		String qId;
		
		if ( id == null ) {
			qId = qstmt;
		} else {
			qId = id;
		}
		
		if ( ! pstmt.containsKey(qId) ) {
			pst = con.prepareStatement(qstmt);
			pstmt.put(qId, pst);
		}
		else {
			pst = (PreparedStatement) pstmt.get(qId);
		}
		
		return pst;
	}
	
	
//	public PreparedStatement prepStatement(String qstmt) throws Exception {
//		return con.prepareStatement(qstmt);
//	}
	
		
	public ResultSet query(String qstmt) throws SQLException {
		
		if ( active == true ) {
		
			if ( db_connect == Connectivity.DIRECT ) {
				ResultSet rs = stmt.executeQuery(qstmt);
				return rs;
			} 
			else if ( db_connect == Connectivity.BONECP ) {
				Connection connection = connectionPool.getConnection(); // fetch a connection from BoneJ pool
				Statement bonej_stmt = connection.createStatement();
				ResultSet rs = bonej_stmt.executeQuery(qstmt);
				connection.close();
				return rs;
			}
			else
				return null;
			
		} else {
			throw new SQLException("No connection: " + status);
		}
	}
	
	public String stringQuery (String qstmt) throws Exception {
		String result = new String("");
		
		ResultSet rs = query(qstmt);
		
		while (rs.next()) {
			result = rs.getString(1);
			break;
		}
		
		return result;
	}

	
	public int intQuery (String qstmt) throws Exception {
		int result = -1;
		
		ResultSet rs = query(qstmt);
		
		while (rs.next()) {
			result = rs.getInt(1);
			break;
		}
		
		return result;
	}
	
	// toggle logging on/off
	public void logStatus(boolean log_on) {
		
	}
	
	
	public void logEvent(Object source, LogSeverity severity, String entry) {
		query_log.add( new DBLog(source.getClass().getName(), severity, entry) );
	}
	
	public void logClear() {
		query_log.clear();
	}
	
	public String flatLog(LogSeverity severity) {
		String log_entries = new String("Database query log:\n");
		
		for (int i=0; i < query_log.size(); i++ ) {
			DBLog log = (DBLog) query_log.elementAt(i);
			log_entries = log_entries.concat( log.source + ": " + log.entry  + ", \n" );
		}
		
		return log_entries;
	}
	
	
	protected class DBLog {
		public String source;
		public LogSeverity severity;
		public String entry;
		
		public DBLog() {
			source = new String();
			severity = null;
			entry = new String();
		
		}
		
		public DBLog(String source, LogSeverity severity, String entry) {
			this.source = source;
			this.severity = severity;
			this.entry = entry;
		}
				
	}
	
	
}
