package me.poutineqc.cuberunner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;


public class Configuration {

	private FileConfiguration config;
	private File configFile;
	
	public String language;
	
	public boolean mysql;
	public String host;
	public int port;
	public String user;
	public String password;
	public String database;
	public String tablePrefix;
	
	public boolean prefixInFrontOfEveryMessages;
	public boolean autostart;
	public int countdownTime;
	public boolean saveAndClearInventory;
	public boolean teleportAfterEnding;
	
	public boolean broadcastStartup;
	public boolean broadcastAchievement;
	public boolean broadcastEndingSingle;
	public boolean broadcastEndingMulti;
	
	public boolean lookForUpdates;
	public boolean economyRewards;
	public double pricePerScore;
	public boolean achievementsRewards;
	
	public List<String> winnerCommands; 
	public List<String> playerCommands; 
	public List<String> endingCommands; 

	public Configuration(CubeRunner plugin) {
		configFile = new File(plugin.getDataFolder(), "config.yml");
			
		if (!configFile.exists())
			plugin.saveDefaultConfig();

		loadConfiguration(plugin);
	}
	
	public void loadConfiguration(Plugin plugin) {
		config = YamlConfiguration.loadConfiguration(configFile);

		language = config.getString("language", "en-US");
		
		mysql = config.getBoolean("mysql", false);
		if (mysql) {
			host = config.getString("host", "127.0.0.1");
			if (host.equalsIgnoreCase("localhost"))
				host = "127.0.0.1";
			
			port = config.getInt("host", 3306);
			user = config.getString("user");
			password = config.getString("password");
			database = config.getString("database");
			tablePrefix = config.getString("tablePrefix", "cuberunner_");
		}
		
		lookForUpdates = config.getBoolean("checkForUpdates", true);
		prefixInFrontOfEveryMessages = config.getBoolean("prefixInFrontOfEveryMessages", true);
		countdownTime = config.getInt("countdownTime", 15);
		autostart = config.getBoolean("autostart", true);
		saveAndClearInventory = config.getBoolean("saveAndClearInventory", true);
		teleportAfterEnding = config.getBoolean("teleportAfterEnding", true);
		
		broadcastStartup = config.getBoolean("broadcasts.startup", true);
		broadcastAchievement = config.getBoolean("broadcasts.achievement", true);
		broadcastEndingSingle = config.getBoolean("broadcasts.ending.singleplayer", true);
		broadcastEndingMulti = config.getBoolean("broadcasts.ending.multiplayer", true);

		economyRewards = config.getBoolean("economyRewards", false);
		
		pricePerScore = config.getDouble("pricePerScore", 0.05);
		achievementsRewards = config.getBoolean("achievementsRewards");
		
		winnerCommands = new ArrayList<String>();
		playerCommands = new ArrayList<String>();
		endingCommands = new ArrayList<String>();
		for (String command : config.getStringList("commands")) {
			if (command.contains("%winner%"))
				winnerCommands.add(command);
			else if (command.contains("%player%"))
				playerCommands.add(command);
			else
				endingCommands.add(command);
		}
	}
	
	public enum CommandType {
		WINNER, PLAYER, NOTHING
	}
}
