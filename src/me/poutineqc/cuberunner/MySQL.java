package me.poutineqc.cuberunner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MySQL {

	private CubeRunner plugin;
	private String prefix;

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

	public MySQL(CubeRunner plugin, String host, int port, String database, String user, String password,
			String prefix) {
		this.plugin = plugin;
		this.prefix = prefix;

		this.host = host;
		this.port = port;
		this.database = database;
		this.user = user;
		this.password = password;

		connect();
		
		if (hasConnection())
			initializeDatabases();
	}

	private void initializeDatabases() {
		update("CREATE TABLE IF NOT EXISTS " + prefix + "SIGNS ("
				+ "uuid varchar(64), type varchar(32),"
				+ "locationWorld varchar(32), locationX INT DEFAULT 0, locationY INT DEFAULT 0, locationZ INT DEFAULT 0);");
		update("ALTER TABLE " + prefix + "SIGNS CONVERT TO CHARACTER SET utf8;");
		
		update("CREATE TABLE IF NOT EXISTS " + prefix + "ARENAS (name varchar(32),world varchar(32),"
				+ "minAmountPlayer INT DEFAULT 1, maxAmountPlayer INT DEFAULT 8, highestScore INT DEFAULT 0,"
				+ "colorIndice LONG, highestPlayer varchar(32) DEFAULT 'null',"
				+ "minPointX INT DEFAULT 0,minPointY INT DEFAULT 0,minPointZ INT DEFAULT 0,"
				+ "maxPointX INT DEFAULT 0, maxPointY INT DEFAULT 0,maxPointZ INT DEFAULT 0,"
				+ "lobbyX DOUBLE DEFAULT 0,lobbyY DOUBLE DEFAULT 0,lobbyZ DOUBLE DEFAULT 0,"
				+ "lobbyPitch FLOAT DEFAULT 0,lobbyYaw FLOAT DEFAULT 0,"
				+ "startPointX DOUBLE DEFAULT 0,startPointY DOUBLE DEFAULT 0,startPointZ DOUBLE DEFAULT 0,"
				+ "startPointPitch FLOAT DEFAULT 0,startPointYaw FLOAT DEFAULT 0);");
		update("ALTER TABLE " + prefix + "ARENAS CONVERT TO CHARACTER SET utf8;");
		
		update("CREATE TABLE IF NOT EXISTS " + prefix
				+ "PLAYERS (UUID varchar(64), name varchar(64), language varchar(32), timePlayed INT DEFAULT 0,"
				+ "money DOUBLE DEFAULT 0, averageDistancePerGame DOUBLE DEFAULT 0, totalDistance DOUBLE DEFAULT 0,"
				+ "games INT DEFAULT 0, totalScore INT DEFAULT 0, kills INT DEFAULT 0, multiplayerWon INT DEFAULT 0,"
				+ "survive5Minutes BOOLEAN DEFAULT FALSE, reachHeight10 BOOLEAN DEFAULT FALSE,"
				+ "fillTheArena BOOLEAN DEFAULT FALSE, theAnswerToLife BOOLEAN DEFAULT FALSE,"
				+ "theRageQuit BOOLEAN DEFAULT FALSE, theKillerBunny BOOLEAN DEFAULT FALSE);");
		update("ALTER TABLE " + prefix + "PLAYERS CONVERT TO CHARACTER SET utf8;");
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
		} catch (SQLException e) {
			plugin.getLogger().info("[MySQL] The connection to MySQL couldn't be made! reason: " + e.getMessage());
		}
	}

	public void close() {
		try {
			if (connection != null) {
				connection.close();
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
			System.err.println(qry);
			System.err.println(e);
		}
	}

	public void newPlayer(UUID uuid, String name) {
		String qry = String.format("INSERT INTO %1$sPLAYERS (UUID, name) VALUES ('%2$s','%3$s');", prefix, uuid.toString(), name);
		update(qry);
	}

	public void updatePlayer(UUID uuid, CRStats key, String value) {
		String qry = String.format("UPDATE %1$sPLAYERS SET %2$s='%3$s' WHERE UUID='%4$s';", prefix,
				key.getNameMySQL(), value, uuid.toString());
		update(qry);
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
			System.err.println(qry);
			System.err.println(e);
		}
		return rs;
	}

	public ResultSet queryAll() {
		String qry = String.format("SELECT * FROM %1$sPLAYERS", prefix);
		return query(qry);
	}

	public ResultSet queryView(String viewName) {
		return query("SELECT * FROM " + prefix + viewName + ";");
	}
}
