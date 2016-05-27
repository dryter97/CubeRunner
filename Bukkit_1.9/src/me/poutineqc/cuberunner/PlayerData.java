package me.poutineqc.cuberunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.poutineqc.cuberunner.commands.CubeRunnerCommand;

public class PlayerData implements Listener {

	private Configuration config;
	private MySQL mysql;

	private File playerFile;
	private FileConfiguration playerData;
	private boolean lastVersion;
	private String latestVersion;

	public PlayerData(CubeRunner plugin) {
		config = plugin.getConfiguration();
		mysql = plugin.getMySQL();

		playerFile = new File(plugin.getDataFolder(), "playerData.yml");
		if (!playerFile.exists()) {
			try {
				playerFile.createNewFile();
			} catch (IOException e) {
				Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create playerData.ylm.");
			}
		}

		lastVersion = isLastVersion(plugin);

		loadPlayerData();
	}

	private boolean isLastVersion(CubeRunner plugin) {
		boolean next = false;
		URL url;
		try {
			url = new URL("http://www.poutineqc.ca/pluginVersion.txt");
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

			while ((latestVersion = in.readLine()) != null) {
				if (next)
					break;
				if (latestVersion.equalsIgnoreCase("CubeRunner"))
					next = true;
			}
			
			in.close();
		} catch (IOException e) {
			plugin.getLogger().warning("Could not find the latest version available.");
		}

		if (next)
			return latestVersion.equalsIgnoreCase(plugin.getDescription().getVersion());

		return true;
	}

	public void loadPlayerData() {
		playerData = YamlConfiguration.loadConfiguration(playerFile);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		addOnFileIfNotExist(player);

		if (!lastVersion && Permissions.hasPermission(CubeRunnerCommand.getCommand("reload"), player, false)) {
			Language local = getLanguageOfPlayer(player);
			local.sendMsg(player,
					"&5A new CubeRunner version is available &d(v%version%)&5.\n".replace("%version%", latestVersion)
							+ "&5Get it now : &dhttps://www.spigotmc.org/resources/cuberunner.19715/");
		}
	}

	public void addOnFileIfNotExist(Player player) {
		String uuid = player.getUniqueId().toString();

		if (mysql.hasConnection()) {
			ResultSet query = mysql.query("SELECT * FROM " + config.tablePrefix + "PLAYERS WHERE UUID='" + uuid + "';");
			try {
				if (!query.next()) {
					mysql.update("INSERT INTO " + config.tablePrefix + "PLAYERS (UUID, name) VALUES ('" + uuid + "','"
							+ player.getName() + "');");
				} else {
					mysql.update("UPDATE " + config.tablePrefix + "PLAYERS SET name='" + player.getName()
							+ "' WHERE UUID='" + uuid + "';");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			if (!playerData.contains("players." + uuid)) {
				playerData.set("players." + uuid + ".name", player.getName());
				playerData.set("players." + uuid + ".language", "default");
				playerData.set("players." + uuid + ".money", 0);
				playerData.set("players." + uuid + ".averageDistancePerGame", 0);
				playerData.set("players." + uuid + ".totalDistance", 0);
				playerData.set("players." + uuid + ".games", 0);
				playerData.set("players." + uuid + ".totalScore", 0);
				playerData.set("players." + uuid + ".kills", 0);
				playerData.set("players." + uuid + ".multiplayerWon", 0);
				playerData.set("players." + uuid + ".timePlayed", 0);
				playerData.set("players." + uuid + ".achievement.survive5Minutes", false);
				playerData.set("players." + uuid + ".achievement.reachHeight10", false);
				playerData.set("players." + uuid + ".achievement.fillTheArena", false);
				playerData.set("players." + uuid + ".achievement.theAnswerToLife", false);
				playerData.set("players." + uuid + ".achievement.theRageQuit", false);
				playerData.set("players." + uuid + ".achievement.theKillerBunny", false);
				savePlayerData();
			} else {
				playerData.set("players." + uuid + ".name", player.getName());
				savePlayerData();
			}
		}
	}

	public FileConfiguration getData() {
		return playerData;
	}

	public void savePlayerData() {
		try {
			playerData.save(playerFile);
		} catch (IOException e) {
			Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not save playerData.yml!");
		}
	}

	public void newUser(Player player) {
		if (mysql.hasConnection()) {
			mysql.update("INSERT INTO " + config.tablePrefix + "PLAYERS (language) VALUES ('default');");
		} else {

		}
	}

	public Language getLanguageOfPlayer(Player player) {
		String fileName = null;
		if (mysql.hasConnection()) {
			try {
				ResultSet query = mysql.query("SELECT language FROM " + config.tablePrefix + "PLAYERS WHERE UUID='"
						+ player.getUniqueId() + "';");

				if (query.next()) {
					fileName = query.getString("language");
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			fileName = playerData.getString("players." + player.getUniqueId().toString() + ".language", null);
		}

		if (fileName == null)
			return Language.getLanguages().get(config.language);

		return getLanguage(fileName);
	}

	public Language getLanguage(String fileName) {
		for (Entry<String, Language> local : Language.getLanguages().entrySet())
			if (local.getKey().equalsIgnoreCase(fileName))
				return local.getValue();

		if (Language.getLanguages().containsKey(config.language))
			return Language.getLanguages().get(config.language);

		return Language.getLanguages().get("en-US");

	}

	public void setLanguage(Player player, String key) {
		if (mysql.hasConnection()) {
			mysql.update("UPDATE " + config.tablePrefix + "PLAYERS SET language='" + key + "' WHERE UUID='"
					+ player.getUniqueId().toString() + "';");
		} else {
			playerData.set("players." + player.getUniqueId().toString() + ".language", key);
			savePlayerData();
		}
	}

	public boolean isLatestVersion() {
		return lastVersion;
	}

	public String getLatestVersion() {
		return latestVersion;
	}
}
