package me.poutineqc.cuberunner;

import java.util.logging.Logger;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.poutineqc.cuberunner.achievements.AchievementGUI;
import me.poutineqc.cuberunner.achievements.Achievements;
import me.poutineqc.cuberunner.achievements.PlayerMovement;
import me.poutineqc.cuberunner.achievements.TopManager;
import me.poutineqc.cuberunner.commands.CubeRunnerCommand;
import me.poutineqc.cuberunner.commands.PlayerCommands;
import me.poutineqc.cuberunner.commands.PlayerInteract;
import me.poutineqc.cuberunner.commands.SignPlace;
import me.poutineqc.cuberunner.games.Arena;
import me.poutineqc.cuberunner.games.ColorGUI;
import me.poutineqc.cuberunner.games.PlayerDamage;
import me.poutineqc.cuberunner.games.PlayerDisconnect;
import me.poutineqc.cuberunner.games.PlayerTeleport;
import me.poutineqc.cuberunner.tools.JoinGUI;
import net.milkbowl.vault.economy.Economy;

public class CubeRunner extends JavaPlugin {

	private Configuration config;
	private MySQL mysql = new MySQL();
	private ArenaData arenaData;
	private PlayerData playerData;
	private TopManager topManager;
	private Achievements achievements;
	private AchievementGUI achievementsGui;
	private JoinGUI joinGui;
	private ColorGUI colorGUI;
	private PlayerCommands playerCommands;
	
	private static Economy economy;

	public void onEnable() {
		PluginDescriptionFile pdfFile = getDescription();
		final Logger logger = getLogger();

		config = new Configuration(this);
		if (!initialiseEconomy())
			return;
		new CubeRunnerCommand(this);
		loadLanguages();
		connectMySQL();
		arenaData = new ArenaData(this);
		playerData = new PlayerData(this);
		topManager = new TopManager(this);
		achievements = new Achievements(this);
		achievementsGui = new AchievementGUI(this);
		joinGui = new JoinGUI(this);
		colorGUI = new ColorGUI(this);
		new Arena(this);
		new Permissions(this);

		playerCommands = new PlayerCommands(this);
		enableListeners();
		getCommand("cuberunner").setExecutor(playerCommands);

		logger.info(pdfFile.getName() + " has been enabled (v" + pdfFile.getVersion() + ")");
	}

	public void onDisable() {
		PluginDescriptionFile pdfFile = getDescription();
		Logger logger = getLogger();

		if (mysql.hasConnection())
			mysql.close();

		logger.info(pdfFile.getName() + " has been diabled");
	}

	public void connectMySQL() {
		if (config.mysql) {
			mysql = new MySQL(this, config.host, config.port, config.database, config.user, config.password);

			if (mysql.hasConnection())
				createMySQLTables();
		} else {
			mysql = new MySQL(this);
		}
	}

	private void createMySQLTables() {
		mysql.update("CREATE TABLE IF NOT EXISTS " + config.tablePrefix + "ARENAS (name varchar(32),world varchar(32),"
				+ "minAmountPlayer INT DEFAULT 1, maxAmountPlayer INT DEFAULT 8, highestScore INT DEFAULT 0,"
				+ "colorIndice LONG, highestPlayer varchar(32) DEFAULT 'null',"
				+ "minPointX INT DEFAULT 0,minPointY INT DEFAULT 0,minPointZ INT DEFAULT 0,"
				+ "maxPointX INT DEFAULT 0, maxPointY INT DEFAULT 0,maxPointZ INT DEFAULT 0,"
				+ "lobbyX DOUBLE DEFAULT 0,lobbyY DOUBLE DEFAULT 0,lobbyZ DOUBLE DEFAULT 0,"
				+ "lobbyPitch FLOAT DEFAULT 0,lobbyYaw FLOAT DEFAULT 0,"
				+ "startPointX DOUBLE DEFAULT 0,startPointY DOUBLE DEFAULT 0,startPointZ DOUBLE DEFAULT 0,"
				+ "startPointPitch FLOAT DEFAULT 0,startPointYaw FLOAT DEFAULT 0);");
		mysql.update("ALTER TABLE " + config.tablePrefix + "ARENAS CONVERT TO CHARACTER SET utf8;");
		mysql.update("CREATE TABLE IF NOT EXISTS " + config.tablePrefix
				+ "PLAYERS (UUID varchar(64), name varchar(64), language varchar(32), timePlayed INT DEFAULT 0,"
				+ "money DOUBLE DEFAULT 0, averageDistancePerGame DOUBLE DEFAULT 0, totalDistance DOUBLE DEFAULT 0,"
				+ "games INT DEFAULT 0, totalScore INT DEFAULT 0, kills INT DEFAULT 0, multiplayerWon INT DEFAULT 0,"
				+ "survive5Minutes BOOLEAN DEFAULT FALSE, reachHeight10 BOOLEAN DEFAULT FALSE,"
				+ "fillTheArena BOOLEAN DEFAULT FALSE, theAnswerToLife BOOLEAN DEFAULT FALSE,"
				+ "theRageQuit BOOLEAN DEFAULT FALSE, theKillerBunny BOOLEAN DEFAULT FALSE);");
		mysql.update("ALTER TABLE " + config.tablePrefix + "PLAYERS CONVERT TO CHARACTER SET utf8;");
		mysql.update("CREATE OR REPLACE VIEW " + config.tablePrefix
				+ "AVERAGESCORE AS SELECT name, averageDistancePerGame FROM " + config.tablePrefix
				+ "PLAYERS ORDER BY averageDistancePerGame DESC LIMIT 10");
		mysql.update(
				"CREATE OR REPLACE VIEW " + config.tablePrefix + "TOTALDISTANCE AS SELECT name, totalDistance FROM "
						+ config.tablePrefix + "PLAYERS ORDER BY totalDistance DESC LIMIT 10");
		mysql.update("CREATE OR REPLACE VIEW " + config.tablePrefix + "MOSTGAMES AS SELECT name, games FROM "
				+ config.tablePrefix + "PLAYERS ORDER BY games DESC LIMIT 10");
		mysql.update("CREATE OR REPLACE VIEW " + config.tablePrefix + "TOTALSCORE AS SELECT name, totalScore FROM "
				+ config.tablePrefix + "PLAYERS ORDER BY totalScore DESC LIMIT 10");
		mysql.update("CREATE OR REPLACE VIEW " + config.tablePrefix + "KILLS AS SELECT name, kills FROM "
				+ config.tablePrefix + "PLAYERS ORDER BY kills DESC LIMIT 10");
		mysql.update(
				"CREATE OR REPLACE VIEW " + config.tablePrefix + "MULTIPLAYERWON AS SELECT name, multiplayerWon FROM "
						+ config.tablePrefix + "PLAYERS ORDER BY multiplayerWon DESC LIMIT 10");
	}

	public void loadLanguages() {
		Language.clearLanguages();

		// A CHANGER AVANT LA RELEASE
		new Language(this);
		new Language("en-US", false);
		new Language("fr-FR", false);
		new Language(config.language, false);
	}

	public boolean initialiseEconomy() {
		if (config.economyRewards)
			if (!setupEconomy()) {
				getLogger().warning("Vault not found.");
				getLogger().warning("Add Vault to your plugins or disable monetary rewards in the config.");
				getLogger().info("Disabling DeACoudre...");
				getServer().getPluginManager().disablePlugin(this);
				return false;
			}
		return true;
	}

	private boolean setupEconomy() {
		economy = null;
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		economy = rsp.getProvider();
		return economy != null;
	}

	public static boolean isEconomyEnabled() {
		return economy != null;
	}

	public static Economy getEconomy() {
		return economy;
	}

	private void enableListeners() {
		PluginManager pm = getServer().getPluginManager();

		pm.registerEvents(new PlayerData(this), this);
		pm.registerEvents(new PlayerDamage(this), this);
		pm.registerEvents(new PlayerTeleport(this), this);
		pm.registerEvents(new PlayerDisconnect(this), this);
		pm.registerEvents(new SignPlace(this), this);
		pm.registerEvents(new PlayerInteract(this), this);
		pm.registerEvents(new PlayerMovement(this), this);
		pm.registerEvents(new AchievementGUI(this), this);
		pm.registerEvents(new JoinGUI(this), this);
		pm.registerEvents(new ColorGUI(this), this);
	}

	public Configuration getConfiguration() {
		return config;
	}

	public MySQL getMySQL() {
		return mysql;
	}

	public ArenaData getArenaData() {
		return arenaData;
	}

	public PlayerData getPlayerData() {
		return playerData;
	}

	public JoinGUI getJoinGui() {
		return joinGui;
	}

	public AchievementGUI getAchievementsGui() {
		return achievementsGui;
	}

	public PlayerCommands getPlayerCommands() {
		return playerCommands;
	}

	public Achievements getAchievements() {
		return achievements;
	}
	
	public TopManager getTopManager() {
		return topManager;
	}

	public ColorGUI getColorGUI() {
		return colorGUI;
	}

}
