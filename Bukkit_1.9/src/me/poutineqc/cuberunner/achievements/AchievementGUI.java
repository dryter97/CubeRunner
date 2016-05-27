package me.poutineqc.cuberunner.achievements;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import me.poutineqc.cuberunner.Configuration;
import me.poutineqc.cuberunner.CubeRunner;
import me.poutineqc.cuberunner.Language;
import me.poutineqc.cuberunner.MySQL;
import me.poutineqc.cuberunner.PlayerData;
import me.poutineqc.cuberunner.tools.ItemStackManager;
import net.milkbowl.vault.economy.Economy;

public class AchievementGUI implements Listener {

	private Configuration config;
	private MySQL mysql;
	private PlayerData playerData;
	private Achievements achievements;
	private Economy economy;

	public AchievementGUI(CubeRunner plugin) {
		this.config = plugin.getConfiguration();
		this.mysql = plugin.getMySQL();
		this.playerData = plugin.getPlayerData();
		this.achievements = plugin.getAchievements();
		this.economy = CubeRunner.getEconomy();
	}

	@EventHandler
	public void onInventotyClick(InventoryClickEvent event) {

		if (!(event.getWhoClicked() instanceof Player))
			return;

		Player player = (Player) event.getWhoClicked();
		Language local = playerData.getLanguageOfPlayer(player);

		if (!ChatColor.stripColor(event.getInventory().getName())
				.equalsIgnoreCase(ChatColor.stripColor(
						ChatColor.translateAlternateColorCodes('&', local.guiStatsName + " &0: &5CubeRunner")))
				&& !ChatColor.stripColor(event.getInventory().getName()).equalsIgnoreCase(ChatColor.stripColor(
						ChatColor.translateAlternateColorCodes('&', local.guiChallengeName + " &0: &5CubeRunner"))))
			return;

		event.setCancelled(true);

		if (event.getAction() == InventoryAction.NOTHING || event.getAction() == InventoryAction.UNKNOWN)
			return;

		String itemName = event.getCurrentItem().getItemMeta().getDisplayName();

		if (isEqualOnColorStrip(itemName, local.guiChallengeName)) {
			openChallengeInventory(player);
			return;
		}

		if (isEqualOnColorStrip(itemName, local.guiStatsName)) {
			openAchievementInventory(player);
			return;
		}

	}

	private boolean isEqualOnColorStrip(String toCheck, String original) {
		return ChatColor.stripColor(toCheck)
				.equalsIgnoreCase(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', original)));
	}

	public void openAchievementInventory(Player player) {

		String uuid = player.getUniqueId().toString();
		Language local = playerData.getLanguageOfPlayer(player);
		Inventory inv = Bukkit.createInventory(null, 54,
				ChatColor.translateAlternateColorCodes('&', local.guiStatsName + " &0: &5CubeRunner"));
		ItemStackManager icon;
		int location;

		double averageDistancePerGame = 0;
		double totalDistance = 0;
		int gamesPlayed = 0;
		int totalPoints = 0;
		int kills = 0;
		int multiplayerWon = 0;
		int timePlayed = 0;
		double money = 0;

		if (mysql.hasConnection()) {
			ResultSet query = mysql.query("SELECT * FROM " + config.tablePrefix + "PLAYERS WHERE UUID='" + uuid + "';");
			try {
				if (query.next()) {
					averageDistancePerGame = query.getDouble("averageDistancePerGame");
					totalDistance = query.getDouble("totalDistance");
					gamesPlayed = query.getInt("games");
					totalPoints = query.getInt("totalScore");
					kills = query.getInt("kills");
					multiplayerWon = query.getInt("multiplayerWon");
					timePlayed = query.getInt("timePlayed");
					money = query.getDouble("money");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			averageDistancePerGame = playerData.getData().getDouble("players." + uuid + ".averageDistancePerGame", 0);
			totalDistance = playerData.getData().getDouble("players." + uuid + ".totalDistance", 0);
			gamesPlayed = playerData.getData().getInt("players." + uuid + ".games", 0);
			totalPoints = playerData.getData().getInt("players." + uuid + ".totalScore", 0);
			kills = playerData.getData().getInt("players." + uuid + ".kills", 0);
			multiplayerWon = playerData.getData().getInt("players." + uuid + ".multiplayerWon", 0);
			timePlayed = playerData.getData().getInt("players." + uuid + ".timePlayed", 0);
			money = playerData.getData().getDouble("players." + uuid + ".money", 0);
		}

		averageDistancePerGame = ((int) (averageDistancePerGame * 100)) / 100.0;
		totalDistance = (int) totalDistance / 1000.0;
		money = (int) (money * 100) / 100.0;

		/***************************************************
		 * Glass Spacer
		 ***************************************************/

		icon = new ItemStackManager(Material.STAINED_GLASS_PANE);
		icon.setData((short) 10);
		icon.setTitle(" ");

		for (int i = 0; i < inv.getSize(); i++)
			switch (i) {
			case 1:
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:
			case 15:
			case 16:
			case 17:
			case 19:
			case 28:
			case 37:
			case 46:
				icon.setPosition(i);
				icon.addToInventory(inv);
			}

		/***************************************************
		 * Stats
		 ***************************************************/

		icon = new ItemStackManager(Material.PAPER);
		icon.setTitle(ChatColor.translateAlternateColorCodes('&', local.guiStatsName + " : CubeRunner"));

		icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");
		icon.addToLore(ChatColor.translateAlternateColorCodes('&',
				local.guiStatsAverageDistancePerGame + " : &e" + String.valueOf(averageDistancePerGame)));
		icon.addToLore(ChatColor.translateAlternateColorCodes('&', local.guiStatsTotalDistance + " : &e"
				+ String.valueOf(totalDistance) + " " + ChatColor.GREEN + local.keyWordDistance));
		icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");
		icon.addToLore(ChatColor.translateAlternateColorCodes('&',
				local.guiStatsGamePlayed + " : &e" + String.valueOf(gamesPlayed)));
		icon.addToLore(ChatColor.translateAlternateColorCodes('&',
				local.guiStatsTotalPoints + " : &e" + String.valueOf(totalPoints)));
		icon.addToLore(
				ChatColor.translateAlternateColorCodes('&', local.guiStatsKills + " : &e" + String.valueOf(kills)));
		icon.addToLore(ChatColor.translateAlternateColorCodes('&',
				local.guiStatsMultiplayer + " : &e" + String.valueOf(multiplayerWon)));
		icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");
		icon.addToLore(ChatColor.LIGHT_PURPLE
				+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.guiStatsTimePlayed)) + ": "
				+ getTimePLayed(local, timePlayed));
		if (CubeRunner.isEconomyEnabled())
			icon.addToLore(ChatColor.LIGHT_PURPLE
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.guiStatsMoney)) + ": &e"
					+ String.valueOf(money) + ChatColor.GREEN + economy.currencyNamePlural());

		icon.setPosition(2);
		icon.addToInventory(inv);

		/***************************************************
		 * Top Ratio
		 ***************************************************/

		icon = new ItemStackManager(Material.EMPTY_MAP);
		icon.setTitle(ChatColor.GOLD + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
				local.keyWordTop10 + " : " + local.guiStatsAverageDistancePerGame)));

		icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");

		if (mysql.hasConnection()) {
			ResultSet query = mysql.query("SELECT * FROM " + config.tablePrefix + "AVERAGESCORE;");
			try {
				while (query.next()) {
					icon.addToLore(ChatColor.LIGHT_PURPLE + query.getString("name") + " : " + ChatColor.YELLOW
							+ getAverage(query.getDouble("averageDistancePerGame"), 100));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			for (int i = 0; i < 10 && i < TopManager.getRatio().size(); i++) {
				icon.addToLore(ChatColor.LIGHT_PURPLE + TopManager.getRatio().get(i).getPlayer() + " : "
						+ ChatColor.YELLOW + getAverage(TopManager.getRatio().get(i).getScore(), 100));
			}
		}

		icon.setPosition(0);
		icon.addToInventory(inv);

		/***************************************************
		 * Top Distance Ran
		 ***************************************************/

		icon = new ItemStackManager(Material.EMPTY_MAP);
		icon.setTitle(ChatColor.GOLD + ChatColor.stripColor(
				ChatColor.translateAlternateColorCodes('&', local.keyWordTop10 + " : " + local.guiStatsTotalDistance)));

		icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");

		if (mysql.hasConnection()) {
			ResultSet query = mysql.query("SELECT * FROM " + config.tablePrefix + "TOTALDISTANCE;");
			try {
				while (query.next()) {
					icon.addToLore(ChatColor.LIGHT_PURPLE + query.getString("name") + " : " + ChatColor.YELLOW
							+ getAverage(query.getDouble("totalDistance"), 1) / 1000 + " " + ChatColor.GREEN
							+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordDistance)));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			for (int i = 0; i < 10 && i < TopManager.getDistanceRan().size(); i++) {
				icon.addToLore(ChatColor.LIGHT_PURPLE + TopManager.getDistanceRan().get(i).getPlayer() + " : "
						+ ChatColor.YELLOW + getAverage(TopManager.getDistanceRan().get(i).getScore(), 1) / 1000);
			}
		}

		icon.setPosition(9);
		icon.addToInventory(inv);

		/***************************************************
		 * Top games
		 ***************************************************/

		icon = new ItemStackManager(Material.EMPTY_MAP);
		icon.setTitle(ChatColor.GOLD + ChatColor.stripColor(
				ChatColor.translateAlternateColorCodes('&', local.keyWordTop10 + " : " + local.guiStatsGamePlayed)));

		icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");

		if (mysql.hasConnection()) {
			ResultSet query = mysql.query("SELECT * FROM " + config.tablePrefix + "MOSTGAMES;");
			try {
				while (query.next()) {
					icon.addToLore(ChatColor.LIGHT_PURPLE + query.getString("name") + " : " + ChatColor.YELLOW
							+ query.getInt("games"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			for (int i = 0; i < 10 && i < TopManager.getGames().size(); i++) {
				icon.addToLore(ChatColor.LIGHT_PURPLE + TopManager.getGames().get(i).getPlayer() + " : "
						+ ChatColor.YELLOW + TopManager.getGames().get(i).getScore());
			}
		}

		icon.setPosition(18);
		icon.addToInventory(inv);

		/***************************************************
		 * Top Total Score
		 ***************************************************/

		icon = new ItemStackManager(Material.EMPTY_MAP);
		icon.setTitle(ChatColor.GOLD + ChatColor.stripColor(
				ChatColor.translateAlternateColorCodes('&', local.keyWordTop10 + " : " + local.guiStatsTotalPoints)));

		icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");

		if (mysql.hasConnection()) {
			ResultSet query = mysql.query("SELECT * FROM " + config.tablePrefix + "TOTALSCORE;");
			try {
				while (query.next()) {
					icon.addToLore(ChatColor.LIGHT_PURPLE + query.getString("name") + " : " + ChatColor.YELLOW
							+ query.getInt("totalScore"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			for (int i = 0; i < 10 && i < TopManager.getTotalScore().size(); i++) {
				icon.addToLore(ChatColor.LIGHT_PURPLE + TopManager.getTotalScore().get(i).getPlayer() + " : "
						+ ChatColor.YELLOW + TopManager.getTotalScore().get(i).getScore());
			}
		}

		icon.setPosition(27);
		icon.addToInventory(inv);

		/***************************************************
		 * Top Kills
		 ***************************************************/

		icon = new ItemStackManager(Material.EMPTY_MAP);
		icon.setTitle(ChatColor.GOLD + ChatColor.stripColor(
				ChatColor.translateAlternateColorCodes('&', local.keyWordTop10 + " : " + local.guiStatsKills)));

		icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");

		if (mysql.hasConnection()) {
			ResultSet query = mysql.query("SELECT * FROM " + config.tablePrefix + "KILLS;");
			try {
				while (query.next()) {
					icon.addToLore(ChatColor.LIGHT_PURPLE + query.getString("name") + " : " + ChatColor.YELLOW
							+ query.getInt("kills"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			for (int i = 0; i < 10 && i < TopManager.getKills().size(); i++) {
				icon.addToLore(ChatColor.LIGHT_PURPLE + TopManager.getKills().get(i).getPlayer() + " : "
						+ ChatColor.YELLOW + (int) TopManager.getKills().get(i).getScore());
			}
		}

		icon.setPosition(36);
		icon.addToInventory(inv);

		/***************************************************
		 * Top Multiplayer Won
		 ***************************************************/

		icon = new ItemStackManager(Material.EMPTY_MAP);
		icon.setTitle(ChatColor.GOLD + ChatColor.stripColor(
				ChatColor.translateAlternateColorCodes('&', local.keyWordTop10 + " : " + local.guiStatsMultiplayer)));

		icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");

		if (mysql.hasConnection()) {
			ResultSet query = mysql.query("SELECT * FROM " + config.tablePrefix + "MULTIPLAYERWON;");
			try {
				while (query.next()) {
					icon.addToLore(ChatColor.LIGHT_PURPLE + query.getString("name") + " : " + ChatColor.YELLOW
							+ query.getInt("multiplayerWon"));
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			for (int i = 0; i < 10 && i < TopManager.getMultiplayerWon().size(); i++) {
				icon.addToLore(ChatColor.LIGHT_PURPLE + TopManager.getMultiplayerWon().get(i).getPlayer() + " : "
						+ ChatColor.YELLOW + (int) TopManager.getMultiplayerWon().get(i).getScore());
			}
		}

		icon.setPosition(45);
		icon.addToInventory(inv);

		/***************************************************
		 * Achievements games
		 ***************************************************/
		location = 20;
		for (AchievementsObject ao : achievements.getachievements().get(0)) {

			icon = new ItemStackManager(Material.WOOL);

			if (ao.get_level() <= gamesPlayed) {
				icon.setData((short) 5);
				icon.setTitle(ChatColor.GREEN + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
						local.achievementAmountGame.replace("%amount%", String.valueOf(ao.get_level())))));
				icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");
				icon.addToLore(ChatColor.AQUA
						+ ChatColor.stripColor(
								ChatColor.translateAlternateColorCodes('&', local.keyWordProgression + ": "))
						+ ChatColor.GREEN
						+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordCompleted)));
			} else {
				icon.setData((short) 8);
				icon.setTitle(ChatColor.RED + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
						local.achievementAmountGame.replace("%amount%", String.valueOf(ao.get_level())))));
				icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");
				icon.addToLore(ChatColor.AQUA
						+ ChatColor.stripColor(
								ChatColor.translateAlternateColorCodes('&', local.keyWordProgression + ": "))
						+ ChatColor.YELLOW + String.valueOf(gamesPlayed) + "/" + String.valueOf(ao.get_level()));
				if (CubeRunner.isEconomyEnabled()) {
					if (config.achievementsRewards)
						icon.addToLore(ChatColor.AQUA
								+ ChatColor.stripColor(
										ChatColor.translateAlternateColorCodes('&', local.keyWordReward + ": "))
								+ ChatColor.YELLOW + String.valueOf(ao.get_reward()) + economy.currencyNamePlural());
				}
			}

			icon.setPosition(location++);
			icon.addToInventory(inv);

		}

		/***************************************************
		 * Achievements totalScore
		 ***************************************************/
		location = 29;
		for (AchievementsObject ao : achievements.getachievements().get(1)) {

			icon = new ItemStackManager(Material.WOOL);

			if (ao.get_level() <= totalPoints) {
				icon.setData((short) 5);
				icon.setTitle(ChatColor.GREEN + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
						local.achievementTotalScore.replace("%amount%", String.valueOf(ao.get_level())))));
				icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");
				icon.addToLore(ChatColor.AQUA
						+ ChatColor.stripColor(
								ChatColor.translateAlternateColorCodes('&', local.keyWordProgression + ": "))
						+ ChatColor.GREEN
						+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordCompleted)));
			} else {
				icon.setData((short) 8);
				icon.setTitle(ChatColor.RED + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
						local.achievementTotalScore.replace("%amount%", String.valueOf(ao.get_level())))));
				icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");
				icon.addToLore(ChatColor.AQUA
						+ ChatColor.stripColor(
								ChatColor.translateAlternateColorCodes('&', local.keyWordProgression + ": "))
						+ ChatColor.YELLOW + String.valueOf(totalPoints) + "/" + String.valueOf(ao.get_level()));
				if (CubeRunner.isEconomyEnabled()) {
					if (config.achievementsRewards)
						icon.addToLore(ChatColor.AQUA
								+ ChatColor.stripColor(
										ChatColor.translateAlternateColorCodes('&', local.keyWordReward + ": "))
								+ ChatColor.YELLOW + String.valueOf(ao.get_reward()) + economy.currencyNamePlural());
				}
			}

			icon.setPosition(location++);
			icon.addToInventory(inv);

		}

		/***************************************************
		 * Achievements kills
		 ***************************************************/
		location = 38;
		for (AchievementsObject ao : achievements.getachievements().get(2)) {

			icon = new ItemStackManager(Material.WOOL);

			if (ao.get_level() <= kills) {
				icon.setData((short) 5);
				icon.setTitle(ChatColor.GREEN + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
						local.achievementKills.replace("%amount%", String.valueOf(ao.get_level())))));
				icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");
				icon.addToLore(ChatColor.AQUA
						+ ChatColor.stripColor(
								ChatColor.translateAlternateColorCodes('&', local.keyWordProgression + ": "))
						+ ChatColor.GREEN
						+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordCompleted)));
			} else {
				icon.setData((short) 8);
				icon.setTitle(ChatColor.RED + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
						local.achievementKills.replace("%amount%", String.valueOf(ao.get_level())))));
				icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");
				icon.addToLore(ChatColor.AQUA
						+ ChatColor.stripColor(
								ChatColor.translateAlternateColorCodes('&', local.keyWordProgression + ": "))
						+ ChatColor.YELLOW + String.valueOf(kills) + "/" + String.valueOf(ao.get_level()));
				if (CubeRunner.isEconomyEnabled()) {
					if (config.achievementsRewards)
						icon.addToLore(ChatColor.AQUA
								+ ChatColor.stripColor(
										ChatColor.translateAlternateColorCodes('&', local.keyWordReward + ": "))
								+ ChatColor.YELLOW + String.valueOf(ao.get_reward()) + economy.currencyNamePlural());
				}
			}

			icon.setPosition(location++);
			icon.addToInventory(inv);

		}

		/***************************************************
		 * Achievements Multiplayer Won
		 ***************************************************/
		location = 47;
		for (AchievementsObject ao : achievements.getachievements().get(3)) {

			icon = new ItemStackManager(Material.WOOL);

			if (ao.get_level() <= multiplayerWon) {
				icon.setData((short) 5);
				icon.setTitle(ChatColor.GREEN + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
						local.achievementMultiplayerWon.replace("%amount%", String.valueOf(ao.get_level())))));
				icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");
				icon.addToLore(ChatColor.AQUA
						+ ChatColor.stripColor(
								ChatColor.translateAlternateColorCodes('&', local.keyWordProgression + ": "))
						+ ChatColor.GREEN
						+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordCompleted)));
			} else {
				icon.setData((short) 8);
				icon.setTitle(ChatColor.RED + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
						local.achievementMultiplayerWon.replace("%amount%", String.valueOf(ao.get_level())))));
				icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");
				icon.addToLore(ChatColor.AQUA
						+ ChatColor.stripColor(
								ChatColor.translateAlternateColorCodes('&', local.keyWordProgression + ": "))
						+ ChatColor.YELLOW + String.valueOf(multiplayerWon) + "/" + String.valueOf(ao.get_level()));
				if (CubeRunner.isEconomyEnabled()) {
					if (config.achievementsRewards)
						icon.addToLore(ChatColor.AQUA
								+ ChatColor.stripColor(
										ChatColor.translateAlternateColorCodes('&', local.keyWordReward + ": "))
								+ ChatColor.YELLOW + String.valueOf(ao.get_reward()) + economy.currencyNamePlural());
				}
			}

			icon.setPosition(location++);
			icon.addToInventory(inv);

		}

		/***************************************************
		 * Arrow
		 ***************************************************/

		icon = new ItemStackManager(Material.ARROW);

		icon.setTitle(local.guiChallengeName);

		icon.setPosition(8);
		icon.addToInventory(inv);

		/***************************************************
		 * Display
		 ***************************************************/

		player.openInventory(inv);
	}

	public void openChallengeInventory(Player player) {

		String uuid = player.getUniqueId().toString();
		Language local = playerData.getLanguageOfPlayer(player);
		Inventory inv = Bukkit.createInventory(null, 27,
				ChatColor.translateAlternateColorCodes('&', local.guiChallengeName + " &0: &5CubeRunner"));
		ItemStackManager icon;

		double averageDistancePerGame = 0;
		double totalDistance = 0;
		int gamesPlayed = 0;
		int totalPoints = 0;
		int kills = 0;
		int multiplayerWon = 0;
		int timePlayed = 0;
		double money = 0;
		boolean survive5Minutes = false;
		boolean reachHeight10 = false;
		boolean fillTheArena = false;
		boolean theAnswerToLife = false;
		boolean theRageQuit = false;
		boolean theKillerBunny = false;

		if (mysql.hasConnection()) {
			ResultSet query = mysql.query("SELECT * FROM " + config.tablePrefix + "PLAYERS WHERE UUID='" + uuid + "';");
			try {
				if (query.next()) {
					averageDistancePerGame = query.getDouble("averageDistancePerGame");
					totalDistance = query.getDouble("totalDistance");
					gamesPlayed = query.getInt("games");
					totalPoints = query.getInt("totalScore");
					kills = query.getInt("kills");
					multiplayerWon = query.getInt("multiplayerWon");
					timePlayed = query.getInt("timePlayed");
					money = query.getDouble("money");
					survive5Minutes = query.getBoolean("survive5Minutes");
					reachHeight10 = query.getBoolean("reachHeight10");
					fillTheArena = query.getBoolean("fillTheArena");
					theAnswerToLife = query.getBoolean("theAnswerToLife");
					theRageQuit = query.getBoolean("theRageQuit");
					theKillerBunny = query.getBoolean("theKillerBunny");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			averageDistancePerGame = playerData.getData().getDouble("players." + uuid + ".averageDistancePerGame", 0);
			totalDistance = playerData.getData().getDouble("players." + uuid + ".totalDistance", 0);
			gamesPlayed = playerData.getData().getInt("players." + uuid + ".games", 0);
			totalPoints = playerData.getData().getInt("players." + uuid + ".totalScore", 0);
			kills = playerData.getData().getInt("players." + uuid + ".kills", 0);
			multiplayerWon = playerData.getData().getInt("players." + uuid + ".multiplayerWon", 0);
			timePlayed = playerData.getData().getInt("players." + uuid + ".timePlayed", 0);
			money = playerData.getData().getDouble("players." + uuid + ".money", 0);
			survive5Minutes = playerData.getData().getBoolean("players." + uuid + ".achievement.survive5Minutes",
					false);
			reachHeight10 = playerData.getData().getBoolean("players." + uuid + ".achievement.reachHeight10", false);
			fillTheArena = playerData.getData().getBoolean("players." + uuid + ".achievement.fillTheArena", false);
			theAnswerToLife = playerData.getData().getBoolean("players." + uuid + ".achievement.theAnswerToLife",
					false);
			theRageQuit = playerData.getData().getBoolean("players." + uuid + ".achievement.theRageQuit", false);
			theKillerBunny = playerData.getData().getBoolean("players." + uuid + ".achievement.theKillerBunny", false);
		}

		averageDistancePerGame = ((int) (averageDistancePerGame * 100)) / 100.0;
		totalDistance = (int) totalDistance / 1000.0;
		money = (int) (money * 100) / 100.0;

		/***************************************************
		 * Glass Spacer
		 ***************************************************/

		icon = new ItemStackManager(Material.STAINED_GLASS_PANE);
		icon.setData((short) 10);
		icon.setTitle(" ");

		for (int i = 0; i < inv.getSize(); i++)
			switch (i) {
			case 9:
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:
			case 15:
			case 16:
			case 17:
				icon.setPosition(i);
				icon.addToInventory(inv);
			}

		/***************************************************
		 * Stats
		 ***************************************************/

		icon = new ItemStackManager(Material.PAPER);
		icon.setTitle(ChatColor.translateAlternateColorCodes('&', local.guiStatsName + " : CubeRunner"));

		icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");
		icon.addToLore(ChatColor.translateAlternateColorCodes('&',
				local.guiStatsAverageDistancePerGame + " : &e" + String.valueOf(averageDistancePerGame)));
		icon.addToLore(ChatColor.translateAlternateColorCodes('&', local.guiStatsTotalDistance + " : &e"
				+ String.valueOf(totalDistance) + " " + ChatColor.GREEN + local.keyWordDistance));
		icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");
		icon.addToLore(ChatColor.translateAlternateColorCodes('&',
				local.guiStatsGamePlayed + " : &e" + String.valueOf(gamesPlayed)));
		icon.addToLore(ChatColor.translateAlternateColorCodes('&',
				local.guiStatsTotalPoints + " : &e" + String.valueOf(totalPoints)));
		icon.addToLore(
				ChatColor.translateAlternateColorCodes('&', local.guiStatsKills + " : &e" + String.valueOf(kills)));
		icon.addToLore(ChatColor.translateAlternateColorCodes('&',
				local.guiStatsMultiplayer + " : &e" + String.valueOf(multiplayerWon)));
		icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");
		icon.addToLore(ChatColor.LIGHT_PURPLE
				+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.guiStatsTimePlayed)) + ": "
				+ getTimePLayed(local, timePlayed));
		if (CubeRunner.isEconomyEnabled())
			icon.addToLore(ChatColor.LIGHT_PURPLE
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.guiStatsMoney)) + ": &e"
					+ String.valueOf(money) + ChatColor.GREEN + economy.currencyNamePlural());

		icon.setPosition(4);
		icon.addToInventory(inv);

		/***************************************************
		 * Challenge : survive 5 minutes
		 ***************************************************/

		icon = new ItemStackManager(Material.INK_SACK);
		if (survive5Minutes) {
			icon.setData((short) 10);
			icon.setTitle(ChatColor.GREEN + ChatColor
					.stripColor(ChatColor.translateAlternateColorCodes('&', local.achievementSurvive5Minutes)));
			icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");
			icon.addToLore(ChatColor.AQUA
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordProgression)) + ": "
					+ ChatColor.GREEN
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordCompleted)));
		} else {
			icon.setData((short) 8);
			icon.setTitle(ChatColor.RED + ChatColor
					.stripColor(ChatColor.translateAlternateColorCodes('&', local.achievementSurvive5Minutes)));
			icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");
			icon.addToLore(ChatColor.AQUA
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordProgression)) + ": "
					+ ChatColor.RED
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordNotCompleted)));
			if (CubeRunner.isEconomyEnabled()) {
				if (config.achievementsRewards)
					icon.addToLore(ChatColor.AQUA
							+ ChatColor
									.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordReward + ": "))
							+ ChatColor.YELLOW + String.valueOf(config.rewardSurvive5Minutes)
							+ economy.currencyNamePlural());
			}
		}

		icon.setPosition(19);
		icon.addToInventory(inv);

		/***************************************************
		 * Challenge : Fill the arena
		 ***************************************************/

		icon = new ItemStackManager(Material.INK_SACK);
		if (fillTheArena) {
			icon.setData((short) 10);
			icon.setTitle(ChatColor.GREEN
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.achievementFillTheArena)));
			icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");
			icon.addToLore(ChatColor.AQUA
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordProgression)) + ": "
					+ ChatColor.GREEN
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordCompleted)));
		} else {
			icon.setData((short) 8);
			icon.setTitle(ChatColor.RED
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.achievementFillTheArena)));
			icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");
			icon.addToLore(ChatColor.AQUA
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordProgression)) + ": "
					+ ChatColor.RED
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordNotCompleted)));
			if (CubeRunner.isEconomyEnabled()) {
				if (config.achievementsRewards)
					icon.addToLore(ChatColor.AQUA
							+ ChatColor
									.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordReward + ": "))
							+ ChatColor.YELLOW + String.valueOf(config.rewardFillTheArenasFloor)
							+ economy.currencyNamePlural());
			}
		}

		icon.setPosition(20);
		icon.addToInventory(inv);

		/***************************************************
		 * Challenge : Reach Height 10
		 ***************************************************/

		icon = new ItemStackManager(Material.INK_SACK);
		if (reachHeight10) {
			icon.setData((short) 10);
			icon.setTitle(ChatColor.GREEN + ChatColor
					.stripColor(ChatColor.translateAlternateColorCodes('&', local.achievementReachHeight10)));
			icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");
			icon.addToLore(ChatColor.AQUA
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordProgression)) + ": "
					+ ChatColor.GREEN
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordCompleted)));
		} else {
			icon.setData((short) 8);
			icon.setTitle(ChatColor.RED + ChatColor
					.stripColor(ChatColor.translateAlternateColorCodes('&', local.achievementReachHeight10)));
			icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");
			icon.addToLore(ChatColor.AQUA
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordProgression)) + ": "
					+ ChatColor.RED
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordNotCompleted)));
			if (CubeRunner.isEconomyEnabled()) {
				if (config.achievementsRewards)
					icon.addToLore(ChatColor.AQUA
							+ ChatColor
									.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordReward + ": "))
							+ ChatColor.YELLOW + String.valueOf(config.rewardReachHeight10)
							+ economy.currencyNamePlural());
			}
		}

		icon.setPosition(21);
		icon.addToInventory(inv);

		/***************************************************
		 * Challenge : RageQuit
		 ***************************************************/

		icon = new ItemStackManager(Material.INK_SACK);
		if (theRageQuit) {
			icon.setData((short) 10);
			icon.setTitle(ChatColor.GREEN
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.achievementTheRageQuit)));
			icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");
			icon.addToLore(ChatColor.AQUA
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordProgression)) + ": "
					+ ChatColor.GREEN
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordCompleted)));
		} else {
			icon.setData((short) 8);
			icon.setTitle(ChatColor.RED
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.achievementTheRageQuit)));
			icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");
			icon.addToLore(ChatColor.AQUA
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordProgression)) + ": "
					+ ChatColor.RED
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordNotCompleted)));
			if (CubeRunner.isEconomyEnabled()) {
				if (config.achievementsRewards)
					icon.addToLore(ChatColor.AQUA
							+ ChatColor
									.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordReward + ": "))
							+ ChatColor.YELLOW + String.valueOf(config.rewardTheRageQuit)
							+ economy.currencyNamePlural());
			}
		}

		icon.setPosition(23);
		icon.addToInventory(inv);

		/***************************************************
		 * Challenge : Killer Bunny
		 ***************************************************/

		icon = new ItemStackManager(Material.INK_SACK);
		if (theKillerBunny) {
			icon.setData((short) 10);
			icon.setTitle(ChatColor.GREEN + ChatColor
					.stripColor(ChatColor.translateAlternateColorCodes('&', local.achievementTheKillerBunny)));
			icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");
			icon.addToLore(ChatColor.AQUA
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordProgression)) + ": "
					+ ChatColor.GREEN
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordCompleted)));
		} else {
			icon.setData((short) 8);
			icon.setTitle(ChatColor.RED + ChatColor
					.stripColor(ChatColor.translateAlternateColorCodes('&', local.achievementTheKillerBunny)));
			icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");
			icon.addToLore(ChatColor.AQUA
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordProgression)) + ": "
					+ ChatColor.RED
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordNotCompleted)));
			if (CubeRunner.isEconomyEnabled()) {
				if (config.achievementsRewards)
					icon.addToLore(ChatColor.AQUA
							+ ChatColor
									.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordReward + ": "))
							+ ChatColor.YELLOW + String.valueOf(config.rewardTheKillerBunny)
							+ economy.currencyNamePlural());
			}
		}

		icon.setPosition(24);
		icon.addToInventory(inv);

		/***************************************************
		 * Challenge : answer to life
		 ***************************************************/

		icon = new ItemStackManager(Material.INK_SACK);
		if (theAnswerToLife) {
			icon.setData((short) 10);
			icon.setTitle(ChatColor.GREEN + ChatColor
					.stripColor(ChatColor.translateAlternateColorCodes('&', local.achievementTheAnswerToLife)));
			icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");
			icon.addToLore(ChatColor.AQUA
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordProgression)) + ": "
					+ ChatColor.GREEN
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordCompleted)));
		} else {
			icon.setData((short) 8);
			icon.setTitle(ChatColor.RED + ChatColor
					.stripColor(ChatColor.translateAlternateColorCodes('&', local.achievementTheAnswerToLife)));
			icon.addToLore(ChatColor.STRIKETHROUGH + "" + ChatColor.YELLOW + "----------------------------");
			icon.addToLore(ChatColor.AQUA
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordProgression)) + ": "
					+ ChatColor.RED
					+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordNotCompleted)));
			if (CubeRunner.isEconomyEnabled()) {
				if (config.achievementsRewards)
					icon.addToLore(ChatColor.AQUA
							+ ChatColor
									.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordReward + ": "))
							+ ChatColor.YELLOW + String.valueOf(config.rewardTheAnswerToLife)
							+ economy.currencyNamePlural());
			}
		}

		icon.setPosition(25);
		icon.addToInventory(inv);

		/***************************************************
		 * Arrow
		 ***************************************************/

		icon = new ItemStackManager(Material.ARROW);

		icon.setTitle(local.guiStatsName);

		icon.setPosition(8);
		icon.addToInventory(inv);

		/***************************************************
		 * Display
		 ***************************************************/

		player.openInventory(inv);
	}

	private double getAverage(double value, int degree) {
		return (int) (value * degree) / (double) (degree);
	}

	private String getTimePLayed(Language local, int timePlayed) {
		long hours = 0;

		timePlayed /= 60000;
		while (timePlayed > 60) {
			timePlayed -= 60;
			hours++;
		}

		return ChatColor.YELLOW + String.valueOf(hours) + ChatColor.GREEN + " "
				+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordHours))
				+ ChatColor.YELLOW + " " + String.valueOf(timePlayed) + ChatColor.GREEN + " "
				+ ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.keyWordMinutes));
	}

}
