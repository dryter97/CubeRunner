package me.poutineqc.cuberunner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.poutineqc.cuberunner.CRPlayer.PlayerStatsException;
import me.poutineqc.cuberunner.utils.MinecraftConfiguration;

public final class PlayerData implements Listener {

	private MinecraftConfiguration playerData;
	private HashMap<UUID, CRPlayer> players;
	private HashMap<CRStats, View> views;

	public PlayerData(CubeRunner plugin) {
		players = new HashMap<UUID, CRPlayer>();
		loadViews();
	}

	public void loadViews() {
		views = new HashMap<CRStats, View>();

		try {
			views.put(CRStats.AVERAGE_SCORE, new View(CRStats.AVERAGE_SCORE));
			views.put(CRStats.GAMES_PLAYED, new View(CRStats.GAMES_PLAYED));
			views.put(CRStats.MULTIPLAYER_WON, new View(CRStats.MULTIPLAYER_WON));
			views.put(CRStats.TOTAL_SCORE, new View(CRStats.TOTAL_SCORE));
			views.put(CRStats.TOTAL_DISTANCE, new View(CRStats.TOTAL_DISTANCE));
			views.put(CRStats.KILLS, new View(CRStats.KILLS));
		} catch (PlayerStatsException e) {
			e.printStackTrace();
		}
	}

	public void loadPlayers(CubeRunner plugin) {
		loadExistingPlayers(plugin);

		for (Player player : Bukkit.getOnlinePlayers())
			loadPlayer(player);
	}

	private void loadExistingPlayers(CubeRunner plugin) {
		if (plugin.getMySQL().hasConnection()) {
			ResultSet query = plugin.getMySQL().queryAll();

			try {
				while (query.next()) {
					CRPlayer player = new CRPlayer(query);
					players.put(player.getUUID(), player);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

		} else {
			playerData = new MinecraftConfiguration(null, "playerData", false);
			if (playerData.get().contains("players"))
				for (String uuid : playerData.get().getConfigurationSection("players").getKeys(false)) {
					CRPlayer player = new CRPlayer(uuid, playerData.get().getConfigurationSection("players." + uuid));
					players.put(player.getUUID(), player);
				}
		}
	}

	private void loadPlayer(Player player) {
		UUID uuid = player.getUniqueId();
		CRPlayer crPlayer = players.get(uuid);

		if (crPlayer == null)
			players.put(uuid, new CRPlayer(uuid, player.getName()));

		else if (!crPlayer.getName().equalsIgnoreCase(player.getName()))
			crPlayer.setName(player.getName());
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		loadPlayer(event.getPlayer());
	}

	public CRPlayer getCRPlayer(Player player) {
		return players.get(player.getUniqueId());
	}

	public MinecraftConfiguration getConfig() {
		return playerData;
	}

	public class View {
		CRStats stats;
		List<CRPlayer> list = new ArrayList<>(10);

		public View(CRStats stats) throws PlayerStatsException {
			this.stats = stats;

			for (Entry<UUID, CRPlayer> player : players.entrySet())
				update(player.getValue());
		}

		public void update(CRPlayer crPlayer) throws PlayerStatsException {
			if (list.contains(crPlayer))
				list.remove(crPlayer);

			boolean over = false;
			int originalSize = list.size();
			list.add(crPlayer);
			
			for (int i = originalSize; i > 0 && !over; i--) {
				Object value = list.get(i).get(stats);
				if (value instanceof Integer) {
					if ((int) value > list.get(i - 1).getInt(stats)) {
						CRPlayer temp = list.get(i);
						list.set(i, list.get(i - 1));
						list.set(i - 1, temp);
					} else
						over = true;
					
				} else if (value instanceof Double) {
					if ((double) value > list.get(i - 1).getDouble(stats)) {
						CRPlayer temp = list.get(i);
						list.set(i, list.get(i - 1));
						list.set(i - 1, temp);
					} else
						over = true;
				}
			}

			if (originalSize == 10)
				list.remove(originalSize);
		}

		public List<CRPlayer> getList() {
			return list;
		}
	}

	public void updateAll(CRPlayer crPlayer) throws PlayerStatsException {
		for (Entry<CRStats, View> set : views.entrySet())
			set.getValue().update(crPlayer);
	}

	public HashMap<CRStats, View> getViews() {
		return views;
	}

	public CRPlayer getCRPlayer(UUID uuid) {
		return players.get(uuid);
	}

	public void clear() {
		players = new HashMap<UUID, CRPlayer>();
	}
}
