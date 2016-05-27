package me.poutineqc.cuberunner.achievements;

import java.io.File;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.poutineqc.cuberunner.Configuration;
import me.poutineqc.cuberunner.CubeRunner;
import me.poutineqc.cuberunner.Language;
import me.poutineqc.cuberunner.MySQL;
import me.poutineqc.cuberunner.PlayerData;
import net.milkbowl.vault.economy.Economy;

public class Achievements {

	private CubeRunner plugin;
	private MySQL mysql;
	private Configuration config;
	private PlayerData playerData;

	private File achievementFile;
	private FileConfiguration achievementData;

	private ArrayList<ArrayList<AchievementsObject>> achievements = new ArrayList<ArrayList<AchievementsObject>>();
	private Economy economy;

	public Achievements(CubeRunner plugin) {
		this.plugin = plugin;
		this.mysql = plugin.getMySQL();
		this.config = plugin.getConfiguration();
		this.playerData = plugin.getPlayerData();
		this.economy = CubeRunner.getEconomy();

		achievementFile = new File(plugin.getDataFolder(), "achievements.yml");
		if (!achievementFile.exists()) {
			InputStream local = plugin.getResource("achievements.yml");
			if (local != null) {
				plugin.saveResource("achievements.yml", false);
			} else
				plugin.getLogger().info("Could not find achievements.yml");
		}

		setupAchievements();
	}

	public void setupAchievements() {

		achievementData = YamlConfiguration.loadConfiguration(achievementFile);

		achievements.clear();

		String[] configNames = new String[] { "amountOfGamesPlayed", "totalScore", "amountPlayerKills",
				"multiplayerGamesWon" };
		for (int i = 0; i < configNames.length; i++) {
			achievements.add(new ArrayList<AchievementsObject>());

			final int MAX_ACHIEVEMENT_PER_ROW = 7;
			List<String> readData = achievementData.getStringList(configNames[i]);

			for (int j = 0; j < readData.size() && achievements.get(i).size() < MAX_ACHIEVEMENT_PER_ROW; j++) {
				String[] individualData = readData.get(j).split(";");

				if (individualData.length < 2) {
					plugin.getLogger().info(
							"Could not load the " + j + "'th data from the " + i + "'th achievement type. (Reading)");
					continue;
				}

				try {
					int level = Integer.parseInt(individualData[0]);
					double reward = Double.parseDouble(individualData[1]);
					achievements.get(i).add(new AchievementsObject(level, reward));
				} catch (NumberFormatException e) {
					plugin.getLogger().info("Could not load the " + j + "'th data from the " + i
							+ "'th achievement type. (Conversion)");
					continue;
				}
			}
		}

	}

	public void checkAchievement(AchievementType type, Player player) {
		if (player == null)
			return;

		AchievementsObject ao;
		boolean completed;

		switch (type) {
		case AMOUNT_GAMES:
			ao = checkNumberAchievement(player, 0, "games");
			if (ao == null)
				break;

			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				Language local = playerData.getLanguageOfPlayer(p);
				local.sendMsg(p,
						local.achievementCongrats.replace("%player%", player.getDisplayName()).replace(
								"%achievementName%",
								local.achievementAmountGame.replace("%amount%", String.valueOf(ao.get_level()))));
			}

			checkForMoneyReward(player, ao.get_reward());
			break;

		case TOTAL_SCORE:
			ao = checkNumberAchievement(player, 1, "totalScore");
			if (ao == null)
				break;

			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				Language local = playerData.getLanguageOfPlayer(p);
				local.sendMsg(p,
						local.achievementCongrats.replace("%player%", player.getDisplayName()).replace(
								"%achievementName%",
								local.achievementTotalScore.replace("%amount%", String.valueOf(ao.get_level()))));
			}

			checkForMoneyReward(player, ao.get_reward());
			break;

		case AMOUNT_KILLS:
			ao = checkNumberAchievement(player, 2, "kills");
			if (ao == null)
				break;

			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				Language local = playerData.getLanguageOfPlayer(p);
				local.sendMsg(p,
						local.achievementCongrats.replace("%player%", player.getDisplayName()).replace(
								"%achievementName%",
								local.achievementKills.replace("%amount%", String.valueOf(ao.get_level()))));
			}

			checkForMoneyReward(player, ao.get_reward());
			break;

		case MULTIPLAYER_GAMES_WON:
			ao = checkNumberAchievement(player, 3, "multiplayerWon");
			if (ao == null)
				break;

			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				Language local = playerData.getLanguageOfPlayer(p);
				local.sendMsg(p,
						local.achievementCongrats.replace("%player%", player.getDisplayName()).replace(
								"%achievementName%",
								local.achievementMultiplayerWon.replace("%amount%", String.valueOf(ao.get_level()))));
			}

			checkForMoneyReward(player, ao.get_reward());
			break;

		case SURVIVE_5_MINUTES:
			completed = checkSingleAchievement(player, "survive5Minutes");
			if (!completed)
				break;

			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				Language local = playerData.getLanguageOfPlayer(p);
				local.sendMsg(p, local.achievementCongrats.replace("%player%", player.getDisplayName())
						.replace("%achievementName%", local.achievementSurvive5Minutes));
			}

			checkForMoneyReward(player, config.rewardSurvive5Minutes);
			break;

		case REACH_HEIGHT_10:
			completed = checkSingleAchievement(player, "reachHeight10");
			if (!completed)
				break;

			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				Language local = playerData.getLanguageOfPlayer(p);
				local.sendMsg(p, local.achievementCongrats.replace("%player%", player.getDisplayName())
						.replace("%achievementName%", local.achievementReachHeight10));
			}
			
			checkForMoneyReward(player, config.rewardReachHeight10);
			break;

		case FILL_THE_ARENA:
			completed = checkSingleAchievement(player, "fillTheArena");
			if (!completed)
				break;

			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				Language local = playerData.getLanguageOfPlayer(p);
				local.sendMsg(p, local.achievementCongrats.replace("%player%", player.getDisplayName())
						.replace("%achievementName%", local.achievementFillTheArena));
			}

			checkForMoneyReward(player, config.rewardFillTheArenasFloor);
			break;

		case DIE_SECONDS_42:
			completed = checkSingleAchievement(player, "theAnswerToLife");
			if (!completed)
				break;

			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				Language local = playerData.getLanguageOfPlayer(p);
				local.sendMsg(p, local.achievementCongrats.replace("%player%", player.getDisplayName())
						.replace("%achievementName%", local.achievementTheAnswerToLife));
			}

			checkForMoneyReward(player, config.rewardTheAnswerToLife);
			break;

		case DISCONNECT_IN_STARTUP:
			completed = checkSingleAchievement(player, "theRageQuit");
			if (!completed)
				break;

			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				Language local = playerData.getLanguageOfPlayer(p);
				local.sendMsg(p, local.achievementCongrats.replace("%player%", player.getDisplayName())
						.replace("%achievementName%", local.achievementTheRageQuit));
			}

			checkForMoneyReward(player, config.rewardTheRageQuit);
			break;

		case JUMP_50_TIMES:
			completed = checkSingleAchievement(player, "theKillerBunny");
			if (!completed)
				break;

			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				Language local = playerData.getLanguageOfPlayer(p);
				local.sendMsg(p, local.achievementCongrats.replace("%player%", player.getDisplayName())
						.replace("%achievementName%", local.achievementTheKillerBunny));
			}

			checkForMoneyReward(player, config.rewardTheKillerBunny);
			break;

		}
	}

	private AchievementsObject checkNumberAchievement(Player player, int arrayNumber, String configName) {
		int amount = 0;
		if (mysql.hasConnection()) {
			ResultSet query = mysql.query("SELECT " + configName + " FROM " + config.tablePrefix
					+ "PLAYERS WHERE UUID='" + player.getUniqueId().toString() + "';");
			try {
				if (query.next())
					amount = query.getInt(configName);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			amount = playerData.getData().getInt("players." + player.getUniqueId().toString() + "." + configName, 0);
		}

		for (AchievementsObject ao : achievements.get(arrayNumber))
			if (ao.get_level() == amount)
				return ao;

		return null;
	}

	private boolean checkSingleAchievement(Player player, String configName) {
		boolean achieved = true;
		if (mysql.hasConnection()) {
			ResultSet query = mysql.query("SELECT " + configName + " FROM " + config.tablePrefix
					+ "PLAYERS WHERE UUID='" + player.getUniqueId().toString() + "';");
			try {
				if (query.next()) {
					achieved = query.getBoolean(configName);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			achieved = playerData.getData()
					.getBoolean("players." + player.getUniqueId().toString() + ".achievement." + configName, false);
		}

		if (mysql.hasConnection()) {
			mysql.update("UPDATE " + config.tablePrefix + "PLAYERS SET " + configName + "='1' WHERE UUID='"
					+ player.getUniqueId().toString() + "';");
		} else {
			playerData.getData().set("players." + player.getUniqueId().toString() + ".achievement." + configName, true);
			playerData.savePlayerData();
		}

		return !achieved;
	}

	protected ArrayList<ArrayList<AchievementsObject>> getachievements() {
		return achievements;
	}
	
	private void checkForMoneyReward(Player player, double amount) {

		if (!CubeRunner.isEconomyEnabled())
			return;

		if (!config.achievementsRewards)
			return;

		economy.depositPlayer(player, amount);
		Language l = playerData.getLanguageOfPlayer(player);
		l.sendMsg(player, l.achievementMoneyReward.replace("%amount%", String.valueOf(amount))
				.replace("%currency%", economy.currencyNamePlural()));
		
		double original = 0;
		if (mysql.hasConnection()) {
			ResultSet query = mysql.query("SELECT money FROM " + config.tablePrefix
					+ "PLAYERS WHERE UUID='" + player.getUniqueId().toString() + "';");
			try {
				if (query.next())
					original = query.getDouble("money");
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			mysql.update("UPDATE " + config.tablePrefix + "PLAYERS SET money='" + (original + amount)
							+ "' WHERE UUID='" + player.getUniqueId().toString() + "';");
		} else {
			original = playerData.getData().getDouble("players." + player.getUniqueId().toString() + ".money", 0);
			playerData.getData().set("players." + player.getUniqueId().toString() + ".money", (original + amount));
			playerData.savePlayerData();
		}
		
	}

}
