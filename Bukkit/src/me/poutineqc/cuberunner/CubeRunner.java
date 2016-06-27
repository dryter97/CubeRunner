package me.poutineqc.cuberunner;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.poutineqc.cuberunner.CRPlayer.PlayerStatsException;
import me.poutineqc.cuberunner.commands.signs.CRSign;
import me.poutineqc.cuberunner.game.Arena;
import me.poutineqc.cuberunner.listeners.ListenerCommand;
import me.poutineqc.cuberunner.listeners.ListenerEntityChangeBlock;
import me.poutineqc.cuberunner.listeners.ListenerEntityGlide;
import me.poutineqc.cuberunner.listeners.ListenerInventoryClick;
import me.poutineqc.cuberunner.listeners.ListenerInventoryClose;
import me.poutineqc.cuberunner.listeners.ListenerPlayerDamage;
import me.poutineqc.cuberunner.listeners.ListenerPlayerDisconnect;
import me.poutineqc.cuberunner.listeners.ListenerPlayerInteract;
import me.poutineqc.cuberunner.listeners.ListenerPlayerMove;
import me.poutineqc.cuberunner.listeners.ListenerPlayerTeleport;
import me.poutineqc.cuberunner.listeners.ListenerSignBreak;
import me.poutineqc.cuberunner.listeners.ListenerSignUpdate;
import me.poutineqc.cuberunner.listeners.ListenerTabComplete;
import me.poutineqc.cuberunner.utils.MinecraftConfiguration;
import net.milkbowl.vault.economy.Economy;

public class CubeRunner extends JavaPlugin {

	private static CubeRunner plugin;
	public static final String name = "CubeRunner";

	private Updater updater;
	private Configuration config;
	private MySQL mysql = new MySQL();
	private ArenaData arenaData;
	private PlayerData playerData;
	private AchievementManager achievementManager;

	private Economy economy;
	public static String NMS_VERSION;
	public static boolean aboveOneNine;

	public void onEnable() {
		plugin = this;
		NMS_VERSION = getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
		aboveOneNine = NMS_VERSION.startsWith("v1_9") || NMS_VERSION.startsWith("v1_1") || NMS_VERSION.startsWith("v2");

		config = new Configuration(this);
		if (config.lookForUpdates)
			updater = new Updater(this);

		if (!initialiseEconomy())
			return;

		loadLanguages();
		connectMySQL();
		arenaData = new ArenaData(this);
		playerData = new PlayerData(this);
		playerData.loadPlayers(plugin);
		achievementManager = new AchievementManager(this);
		CRSign.setVariables(this);
		new Arena(this);

		getCommand("cuberunner").setExecutor(new ListenerCommand());
		getCommand("cuberunner").setTabCompleter(new ListenerTabComplete());

		enableListeners();

		try {
			Metrics metrics;
			metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				Arena.loadExistingArenas();
				CRSign.loadAllSigns();
			}
		}, 0);

		getLogger().info(getDescription().getName() + " has been enabled (v" + getDescription().getVersion() + ")");
	}

	public void onDisable() {
		PluginDescriptionFile pdfFile = getDescription();
		Logger logger = getLogger();

		if (config.lookForUpdates)
			updater.stop();

		if (mysql.hasConnection())
			mysql.close();
		else
			arenaData.loadArenaData();

		logger.info(pdfFile.getName() + " has been diabled");
	}

	public void reload() {
		updater.stop();
		Language.clear();

		config.loadConfiguration(this);
		if (config.lookForUpdates)
			updater = new Updater(this);

		if (!initialiseEconomy())
			return;

		loadLanguages();

		playerData.clear();
		playerData.loadViews();
		playerData.loadPlayers(this);
		achievementManager = new AchievementManager(this);
		CRSign.setVariables(this);

		Arena.loadExistingArenas();
		CRSign.loadAllSigns();
	}

	public void connectMySQL() {
		if (config.mysql) {
			mysql = new MySQL(this, config.host, config.port, config.database, config.user, config.password,
					config.tablePrefix);
		} else {
			mysql = new MySQL(this);
		}
	}

	public void loadLanguages() {
		new Language(this, "en");
		new Language(this, "fr");
		new Language(this, "de");
		new Language(this, config.language);
	}

	public boolean initialiseEconomy() {
		if (config.economyRewards)
			if (!setupEconomy()) {
				getLogger().warning("Vault not found.");
				getLogger().warning("Add Vault to your plugins or disable monetary rewards in the config.");
				getLogger().info("Disabling CubeRunner...");
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

	public boolean isEconomyEnabled() {
		return economy != null;
	}

	public Economy getEconomy() {
		return economy;
	}

	private void enableListeners() {
		PluginManager pm = getServer().getPluginManager();

		pm.registerEvents(updater, this);
		pm.registerEvents(playerData, this);
		pm.registerEvents(new ListenerPlayerDamage(), this);
		pm.registerEvents(new ListenerPlayerTeleport(), this);
		pm.registerEvents(new ListenerPlayerDisconnect(), this);
		pm.registerEvents(new ListenerSignUpdate(), this);
		pm.registerEvents(new ListenerPlayerInteract(), this);
		pm.registerEvents(new ListenerInventoryClose(), this);
		pm.registerEvents(new ListenerPlayerMove(), this);
		pm.registerEvents(new ListenerSignBreak(), this);
		pm.registerEvents(new ListenerInventoryClick(), this);
		pm.registerEvents(new ListenerEntityChangeBlock(), this);

		if (aboveOneNine)
			pm.registerEvents(new ListenerEntityGlide(), this);
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

	public static CubeRunner get() {
		return plugin;
	}

	public CRPlayer getCRPlayer(Player player) {
		return playerData.getCRPlayer(player);
	}

	public MinecraftConfiguration getPlayerConfig() {
		return playerData.getConfig();
	}

	public Language getLang(Player player) {
		return getCRPlayer(player).getLanguage();
	}

	public AchievementManager getAchievementManager() {
		return achievementManager;
	}

	public void updateAll(CRPlayer crPlayer) {
		try {
			playerData.updateAll(crPlayer);
		} catch (PlayerStatsException e) {
			e.printStackTrace();
		}
	}

	public CRPlayer getCRPlayer(UUID uuid) {
		return playerData.getCRPlayer(uuid);
	}
}
