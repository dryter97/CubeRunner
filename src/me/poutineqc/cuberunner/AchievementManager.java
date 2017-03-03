package me.poutineqc.cuberunner;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.poutineqc.cuberunner.CRPlayer.PlayerStatsException;
import me.poutineqc.cuberunner.Language.Messages;
import me.poutineqc.cuberunner.utils.MinecraftConfiguration;
import net.milkbowl.vault.economy.Economy;

public class AchievementManager {

	private HashMap<Achievement, LinkedHashMap<Integer, Double>> achievements;
	private HashMap<Challenge, Double> challenges;

	public AchievementManager(CubeRunner plugin) {
		MinecraftConfiguration config = new MinecraftConfiguration(null, "achievements", true);

		generateAchievements(config);
		generateChallenges(config);
	}

	private void generateAchievements(MinecraftConfiguration config) {
		achievements = new HashMap<Achievement, LinkedHashMap<Integer, Double>>();

		for (Achievement achievement : Achievement.values()) {
			achievements.put(achievement, new LinkedHashMap<Integer, Double>());

			for (String line : config.get().getStringList(achievement.configName)) {

				AchievementReader reader = new AchievementReader(line);
				if (!reader.ok) {
					Bukkit.getLogger().warning("Could not read line [" + line + "] from achievements.yml.. Ignoring");
					continue;
				}

				achievements.get(achievement).put(reader.level, reader.reward);
			}
		}
	}

	private void generateChallenges(MinecraftConfiguration config) {
		challenges = new HashMap<Challenge, Double>();

		for (Challenge challenge : Challenge.values())
			challenges.put(challenge, config.get().getDouble(challenge.configName));
	}

	public void complete(CRPlayer player, CRStats crStats, int value) {
		Achievement achievement = Achievement.association.get(crStats);
		if (achievement == null)
			return;

		if (!achievements.get(achievement).containsKey(value))
			return;

		congradulate(player, achievement, value);

		if (CubeRunner.get().isEconomyEnabled()) {
			double reward = achievements.get(achievement).get(value);
			Economy economy = CubeRunner.get().getEconomy();
			economy.depositPlayer(player.getPlayer(), reward);

			Language local = player.getLanguage();
			local.sendMsg(player.getPlayer(), local.get(Messages.ACHIEVEMENT_REWARD)
					.replace("%amount%", String.valueOf(reward)).replace("%currency%", economy.currencyNamePlural()));

			try {
				player.addDouble(CRStats.MONEY, reward);
			} catch (PlayerStatsException e) {
				e.printStackTrace();
			}
		}
	}

	private void congradulate(CRPlayer player, Achievement achievement, int amount) {
		if (CubeRunner.get().getConfiguration().broadcastAchievement)
			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				Language local = CubeRunner.get().getLang(onlinePlayer);
				local.sendMsg(onlinePlayer, local.get(Messages.ACHIEVEMENT_BROADCAST)
						.replace("%player%", player.getPlayer().getDisplayName()).replace("%achievementName%",
								local.get(achievement.achievementMessage).replace("%amount%", String.valueOf(amount))));
			}
	}

	public void complete(CRPlayer player, CRStats crStats) {
		Challenge challenge = Challenge.association.get(crStats);
		if (challenge == null)
			return;

		congradulate(player, challenge);

		if (CubeRunner.get().isEconomyEnabled()) {
			double reward = challenges.get(challenge);
			Economy economy = CubeRunner.get().getEconomy();
			economy.depositPlayer(player.getPlayer(), reward);

			Language local = player.getLanguage();
			local.sendMsg(player.getPlayer(), local.get(Messages.ACHIEVEMENT_REWARD)
					.replace("%amount%", String.valueOf(reward)).replace("%currency%", economy.currencyNamePlural()));

			try {
				player.addDouble(CRStats.MONEY, reward);
			} catch (PlayerStatsException e) {
				e.printStackTrace();
			}
		}
	}

	private void congradulate(CRPlayer player, Challenge challenge) {
		if (CubeRunner.get().getConfiguration().broadcastAchievement)
			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				Language local = CubeRunner.get().getLang(onlinePlayer);
				local.sendMsg(onlinePlayer,
						local.get(Messages.ACHIEVEMENT_BROADCAST)
								.replace("%player%", player.getPlayer().getDisplayName())
								.replace("%achievementName%", local.get(challenge.message)));
			}
	}

	private class AchievementReader {
		private boolean ok = true;
		private int level;
		private double reward;

		private AchievementReader(String line) {
			String[] lineArgs = line.split(";");

			try {
				level = Integer.parseInt(lineArgs[0]);
				reward = Double.parseDouble(lineArgs[1]);

			} catch (NumberFormatException | IndexOutOfBoundsException e) {
				ok = false;
			}
		}
	}

	public enum Achievement {
		GAMES_PLAYED(CRStats.GAMES_PLAYED, "amountOfGamesPlayed", Messages.ACHIEVEMENT_GAMES, Messages.STATS_INFO_GAMES),
		TOTAL_SCORE(CRStats.TOTAL_SCORE, "totalScore", Messages.ACHIEVEMENT_TOTAL_SCORE, Messages.STATS_INFO_GAMES),
		KILLS(CRStats.KILLS, "amountPlayerKills", Messages.ACHIEVEMENT_KILLS, Messages.STATS_INFO_GAMES),
		MULTIPLAYER_WON(CRStats.MULTIPLAYER_WON, "multiplayerGamesWon", Messages.ACHIEVEMENT_MULTIPLAYER_WON, Messages.STATS_INFO_GAMES);

		private final CRStats stats;
		private final String configName;
		private final Messages achievementMessage;
		private final Messages statsMessage;

		private static HashMap<CRStats, Achievement> association;

		static {
			association = new HashMap<CRStats, Achievement>();
			for (Achievement achievement : Achievement.values())
				association.put(achievement.stats, achievement);
		}

		private Achievement(CRStats stats, String configName, Messages achievementMessage, Messages statsMessage) {
			this.stats = stats;
			this.configName = configName;
			this.achievementMessage = achievementMessage;
			this.statsMessage = statsMessage;
		}

		public CRStats getCrStats() {
			return stats;
		}

		public Messages getMessage() {
			return statsMessage;
		}

		public Messages getAchievementMessage() {
			return achievementMessage;
		}
	}

	public enum Challenge {
		SURVIVE_5_MINUTES(CRStats.SURVIVE_5_MINUTES, "rewardSurvive5Minutes", Messages.ACHIEVEMENT_SURVIVE_5_MINUTES),
		FILL_THE_ARENA(CRStats.FILL_THE_ARENA, "rewardFillTheArenasFloor", Messages.ACHIEVEMENT_FILL_THE_ARENA),
		REACH_HEIGHT_10(CRStats.REACH_HEIGHT_10, "rewardReachHeight10", Messages.ACHIEVEMENT_REACH_HEIGHT_10),
		THE_RAGE_QUIT(CRStats.THE_RAGE_QUIT, "rewardTheRageQuit", Messages.ACHIEVEMENT_RAGE_QUIT),
		THE_KILLER_BUNNY(CRStats.THE_KILLER_BUNNY, "rewardTheKillerBunny", Messages.ACHIEVEMENT_KILLER_BUNNY),
		THE_ANSWER_TO_LIFE(CRStats.THE_ANSWER_TO_LIFE, "rewardTheAnswerToLife", Messages.ACHIEVEMENT_ANSWER_TO_LIFE);

		private final CRStats stats;
		private final String configName;
		private final Messages message;

		private static HashMap<CRStats, Challenge> association;

		static {
			association = new HashMap<CRStats, Challenge>();
			for (Challenge challenge : Challenge.values())
				association.put(challenge.stats, challenge);
		}

		private Challenge(CRStats stats, String configName, Messages message) {
			this.stats = stats;
			this.configName = configName;
			this.message = message;
		}

		public CRStats getCrStats() {
			return stats;
		}

		public Messages getMessage() {
			return message;
		}
	}

	public HashMap<Achievement, LinkedHashMap<Integer, Double>> getAchievements() {
		return achievements;
	}

	public HashMap<Challenge, Double> getChallenges() {
		return challenges;
	}
}
