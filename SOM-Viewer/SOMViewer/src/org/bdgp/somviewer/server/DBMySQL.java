/**
 * 
 */
package org.bdgp.somviewer.server;

/**
 * @author erwin
 *
 */
public class DBMySQL extends DBbase {

	public DBMySQL(String host, String db, String user, String password) {
		
		this.url = "jdbc:mysql://" + host + ":3306/" + db;
		this.user = user;
		this.password = password;
		
		initConn();
		
		super.connect();
	}

	public void initConn() {
		try {
			// Class.forName("com.mysql.jdbc");
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		}
		catch (Exception e) {
			status = "Driver loading failed";
			return;
		}
	}
	
}
