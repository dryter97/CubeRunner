package me.poutineqc.cuberunner;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;


public class Configuration {

	private FileConfiguration config;
	private File configFile;
	
	public String version;
	public String language;
	
	public boolean mysql;
	public String host;
	public int port;
	public String user;
	public String password;
	public String database;
	public String tablePrefix;
	
	public boolean prefixInFrontOfEveryMessages;
	public int countdownTime;
	public boolean teleportAfterEnding;
	
	public boolean economyRewards;
	public double pricePerScore;
	public boolean achievementsRewards;
	public double rewardSurvive5Minutes;
	public double rewardFillTheArenasFloor;
	public double rewardReachHeight10;
	public double rewardTheRageQuit;
	public double rewardTheKillerBunny;
	public double rewardTheAnswerToLife;

	public Configuration(CubeRunner plugin) {
		configFile = new File(plugin.getDataFolder(), "config.yml");
			
		if (!configFile.exists())
			plugin.saveDefaultConfig();

		loadConfiguration(plugin);
	}
	
	public void loadConfiguration(Plugin plugin) {
		config = YamlConfiguration.loadConfiguration(configFile);

		version = config.getString("version");
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
		
		prefixInFrontOfEveryMessages = config.getBoolean("prefixInFrontOfEveryMessages", true);
		countdownTime = config.getInt("countdownTime", 15);
		teleportAfterEnding = config.getBoolean("teleportAfterEnding", true);

		economyRewards = config.getBoolean("economyRewards", false);
		
		pricePerScore = config.getDouble("pricePerScore", 0.05);
		achievementsRewards = config.getBoolean("achievementsRewards");
		rewardSurvive5Minutes = config.getDouble("rewardSurvive5Minutes", 25.0);
		rewardFillTheArenasFloor = config.getDouble("rewardFillTheArenasFloor", 50.0);
		rewardReachHeight10 = config.getDouble("rewardReachHeight10", 100.0);
		rewardTheRageQuit = config.getDouble("rewardTheRageQuit", 15.0);
		rewardTheKillerBunny = config.getDouble("rewardTheKillerBunny", 15.0);
		rewardTheAnswerToLife = config.getDouble("rewardTheAnswerToLife", 15.0);
	}

}
