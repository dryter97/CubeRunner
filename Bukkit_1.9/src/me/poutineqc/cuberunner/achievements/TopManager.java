package me.poutineqc.cuberunner.achievements;

import java.util.ArrayList;
import java.util.List;

import me.poutineqc.cuberunner.CubeRunner;
import me.poutineqc.cuberunner.PlayerData;

public class TopManager {

	private PlayerData playerData;

	private String player;
	private double score;

	private static List<TopManager> ratio = new ArrayList<TopManager>();
	private static List<TopManager> distanceRan = new ArrayList<TopManager>();
	private static List<TopManager> games = new ArrayList<TopManager>();
	private static List<TopManager> totalScore = new ArrayList<TopManager>();
	private static List<TopManager> kills = new ArrayList<TopManager>();
	private static List<TopManager> multiplayerWon = new ArrayList<TopManager>();

	public TopManager(CubeRunner plugin) {
		this.playerData = plugin.getPlayerData();
		updateAll();
	}

	public TopManager(String name, double score) {
		this.player = name;
		this.score = score;
	}

	public void updateAll() {
		ratio = updateTop("averageDistancePerGame");
		distanceRan = updateTop("totalDistance");
		games = updateTop("games");
		totalScore = updateTop("totalScore");
		kills = updateTop("kills");
		multiplayerWon = updateTop("multiplayerWon");
	}

	private List<TopManager> updateTop(String lookup) {
		List<TopManager> tempList = new ArrayList<TopManager>();

		if (playerData.getData().contains("players")) {
			for (String uuid : playerData.getData().getConfigurationSection("players").getKeys(false)) {
				String name = playerData.getData().getString("players." + uuid + ".name", "unknown");
				double score = playerData.getData().getDouble("players." + uuid + "." + lookup, 0);
				tempList.add(0, new TopManager(name, score));

				for (int i = 0; i < 10 && i < tempList.size() - 1; i++) {
					if (tempList.get(i).score < tempList.get(i + 1).score) {
						TopManager tempValue = tempList.get(i);
						tempList.set(i, tempList.get(i + 1));
						tempList.set(i + 1, tempValue);
					}
				}
			}
		}

		return tempList;
	}

	protected String getPlayer() {
		return player;
	}

	protected double getScore() {
		return score;
	}

	public static List<TopManager> getRatio() {
		return ratio;
	}

	public static List<TopManager> getDistanceRan() {
		return distanceRan;
	}

	public static List<TopManager> getGames() {
		return games;
	}

	public static List<TopManager> getTotalScore() {
		return totalScore;
	}

	public static List<TopManager> getKills() {
		return kills;
	}

	public static List<TopManager> getMultiplayerWon() {
		return multiplayerWon;
	}

}
