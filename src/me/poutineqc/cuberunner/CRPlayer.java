package me.poutineqc.cuberunner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import me.poutineqc.cuberunner.commands.inventories.CRInventory;
import me.poutineqc.cuberunner.utils.MinecraftConfiguration;

public class CRPlayer {

	private UUID uuid;
	private HashMap<CRStats, Object> stats = new HashMap<CRStats, Object>();
	private CRInventory crInventory;

	public CRPlayer(UUID uuid, String name) {
		this.uuid = uuid;

		if (CubeRunner.get().getMySQL().hasConnection())
			CubeRunner.get().getMySQL().newPlayer(uuid, name);
		else {
			MinecraftConfiguration config = CubeRunner.get().getPlayerConfig();
			for (CRStats key : CRStats.values()) {
				if (key == CRStats.NAME)
					config.get().set("players." + uuid.toString() + "." + key.getNameFlatFile(), name);
				else
					config.get().set("players." + uuid.toString() + "." + key.getNameFlatFile(), key.getDefaultValue());

				config.save();
			}
			
			CubeRunner.get().updateAll(this);
		}

		for (CRStats stats : CRStats.values()) {
			this.stats.put(stats, stats.getDefault());
			if (stats == CRStats.NAME)
				this.stats.put(stats, name);
		}
	}

	public CRPlayer(String uuid, ConfigurationSection config) {
		this.uuid = UUID.fromString(uuid);

		for (CRStats stats : CRStats.values())
			this.stats.put(stats, stats.getValue(config));
		
		CubeRunner.get().updateAll(this);
	}

	public CRPlayer(ResultSet query) {
		try {
			this.uuid = UUID.fromString(query.getString("UUID"));

			for (CRStats stats : CRStats.values())
				this.stats.put(stats, stats.getValue(query));

		} catch (SQLException e) {
			Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could read some informations from playerData.ylm.");
		}
		
		CubeRunner.get().updateAll(this);
	}

	private void updateInformation(CRStats key, String value) {
		if (CubeRunner.get().getMySQL().hasConnection())
			CubeRunner.get().getMySQL().updatePlayer(uuid, key, value);
		else {
			CubeRunner.get().getPlayerConfig().get().set("players." + uuid.toString() + "." + key.getNameFlatFile(),
					value);
			CubeRunner.get().getPlayerConfig().save();
		}
	}

	private void updateInformation(CRStats key, double value) {
		if (CubeRunner.get().getMySQL().hasConnection())
			CubeRunner.get().getMySQL().updatePlayer(uuid, key, String.valueOf(value));
		else {
			CubeRunner.get().getPlayerConfig().get().set("players." + uuid.toString() + "." + key.getNameFlatFile(),
					value);
			CubeRunner.get().getPlayerConfig().save();
		}
	}

	private void updateInformation(CRStats key, int value) {
		if (CubeRunner.get().getMySQL().hasConnection())
			CubeRunner.get().getMySQL().updatePlayer(uuid, key, String.valueOf(value));
		else {
			CubeRunner.get().getPlayerConfig().get().set("players." + uuid.toString() + "." + key.getNameFlatFile(),
					value);
			CubeRunner.get().getPlayerConfig().save();
		}
	}

	private void updateInformation(CRStats key, boolean value) {
		if (CubeRunner.get().getMySQL().hasConnection())
			CubeRunner.get().getMySQL().updatePlayer(uuid, key, value ? "1" : "0");
		else {
			CubeRunner.get().getPlayerConfig().get().set("players." + uuid.toString() + "." + key.getNameFlatFile(),
					value);
			CubeRunner.get().getPlayerConfig().save();
		}
	} 

	public void setCurrentInventory(CRInventory crInventory) {
		this.crInventory = crInventory;
	}

	public CRInventory getCurrentInventory() {
		return crInventory;
	}

	public UUID getUUID() {
		return uuid;
	}

	public Player getPlayer() {
		return Bukkit.getOfflinePlayer(uuid).getPlayer();
	}

	public void setName(String name) {
		stats.put(CRStats.NAME, name);
		updateInformation(CRStats.NAME, name);
	}

	public String getName() {
		return (String) stats.get(CRStats.NAME);
	}

	public void setLanguage(Language local) {
		stats.put(CRStats.LANGUAGE, local);
		updateInformation(CRStats.LANGUAGE, local.getFileName());
	}

	public Language getLanguage() {
		return (Language) stats.get(CRStats.LANGUAGE);
	}

	public void updateAverageScorePerGame() {
		int distance = (int) stats.get(CRStats.TOTAL_SCORE);
		int games = (int) stats.get(CRStats.GAMES_PLAYED);
		stats.put(CRStats.AVERAGE_SCORE, (double) distance / games);
		updateInformation(CRStats.AVERAGE_SCORE, distance / games);
	}

	public void addInt(CRStats crStats, int amount) throws PlayerStatsException {
		Object value = stats.get(crStats);
		if (!(value instanceof Integer))
			throw new PlayerStatsException("Stat [" + crStats.name() + "] is not an Integer.");

		stats.put(crStats, (int) value + amount);
		updateInformation(crStats, (int) value + amount);
	}

	public void addDouble(CRStats crStats, double amount) throws PlayerStatsException {
		Object value = stats.get(crStats);
		if (!(value instanceof Double))
			throw new PlayerStatsException("Stat [" + crStats.name() + "] is not a Double.");

		stats.put(crStats, (double) value + amount);
		updateInformation(crStats, (double) value + amount);
	}

	public void increment(CRStats crStats, boolean save) throws PlayerStatsException {
		Object value = stats.get(crStats);
		if (!(value instanceof Integer))
			throw new PlayerStatsException("Stat [" + crStats.name() + "] is not an Incrementor.");

		stats.put(crStats, ((int) value) + 1);
		CubeRunner.get().getAchievementManager().complete(this, crStats, ((int) value) + 1);

		if (save)
			saveIncrementor(crStats);
	}

	public void saveIncrementor(CRStats crStats) throws PlayerStatsException {
		Object value = stats.get(crStats);
		if (!(value instanceof Integer))
			throw new PlayerStatsException("Stat [" + crStats.name() + "] is not an Incrementor.");

		updateInformation(crStats, (int) value);
	}

	public void doneChallenge(CRStats challenge) throws PlayerStatsException {
		Object didIt = stats.get(challenge);
		if (!(didIt instanceof Boolean))
			throw new PlayerStatsException("Stat [" + challenge.name() + "] is not a challenge.");

		if (!((boolean) didIt)) {
			stats.put(challenge, true);
			updateInformation(challenge, true);
			CubeRunner.get().getAchievementManager().complete(this, challenge);
		}
	}

	public int getInt(CRStats crStats) throws PlayerStatsException {
		Object value = stats.get(crStats);
		if (!(value instanceof Integer))
			throw new PlayerStatsException("Stat [" + crStats.name() + "] is not an Integer.");

		return (int) value;
	}

	public double getDouble(CRStats crStats) throws PlayerStatsException {
		Object value = stats.get(crStats);
		if (!(value instanceof Double) && !(value instanceof Integer))
			throw new PlayerStatsException("Stat [" + crStats.name() + "] is not a Double.");

		return (double) value;
	}

	public boolean hasChallenge(CRStats challenge) throws PlayerStatsException {
		Object didIt = stats.get(challenge);
		if (!(didIt instanceof Boolean))
			throw new PlayerStatsException("Stat [" + challenge.name() + "] is not a challenge.");

		return (boolean) didIt;
	}

	public class PlayerStatsException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4844283181470739524L;

		public PlayerStatsException(String message) {
			super(message);
		}

	}

	public Object get(CRStats stat) {
		return stats.get(stat);
	}

}
