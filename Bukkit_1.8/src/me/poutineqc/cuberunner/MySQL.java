package me.poutineqc.cuberunner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQL {

	private CubeRunner plugin;
	private String host;
	private int port;
	private String database;
	private String user;
	private String password;

	private Connection connection;

	public MySQL(CubeRunner plugin) {
		this.plugin = plugin;
		connection = null;
	}

	public MySQL(CubeRunner plugin, String host, int port, String database, String user, String password) {
		this.plugin = plugin;
		this.host = host;
		this.port = port;
		this.database = database;
		this.user = user;
		this.password = password;

		connect();
	}
	
	public MySQL() {
		connection = null;
	}

	public void updateInfo(CubeRunner plugin) {
		Configuration config = plugin.getConfiguration();
		this.plugin = plugin;
		this.host = config.host;
		this.port = config.port;
		this.database = config.database;
		this.user = config.user;
		this.password = config.password;
		
		connect();
	}

	public void connect() {
		try {
			connection = DriverManager.getConnection(
					"jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true", user, password);
			plugin.getLogger().info("[MySQL] The connection to MySQL is made!");
		} catch (SQLException e) {
			plugin.getLogger().info("[MySQL] The connection to MySQL couldn't be made! reason: " + e.getMessage());
		}
	}

	public void close() {
		try {
			if (connection != null) {
				connection.close();
				plugin.getLogger().info("[MySQL] The connection to MySQL is ended successfully!");
			}
		} catch (SQLException e) {
			plugin.getLogger().info("[MySQL] The connection couldn't be closed! reason: " + e.getMessage());
		}
	}

	public void update(String qry) {
		try {
			PreparedStatement st = connection.prepareStatement(qry);
			st.execute();
			st.close();
		} catch (SQLException e) {
			connect();
			System.err.println(e);
		}
	}

	public boolean hasConnection() {
		return connection != null;
	}

	public ResultSet query(String qry) {
		ResultSet rs = null;
		try {
			PreparedStatement st = connection.prepareStatement(qry);
			rs = st.executeQuery();
		} catch (SQLException e) {
			connect();
			System.err.println(e);
		}
		return rs;
	}
}
