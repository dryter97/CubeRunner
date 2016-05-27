package me.poutineqc.cuberunner.games;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import me.poutineqc.cuberunner.Configuration;
import me.poutineqc.cuberunner.CubeRunner;
import me.poutineqc.cuberunner.MySQL;
import me.poutineqc.cuberunner.PlayerData;
import me.poutineqc.cuberunner.achievements.AchievementType;
import me.poutineqc.cuberunner.achievements.Achievements;

public class PlayerDamage implements Listener {

	private MySQL mySQL;
	private Configuration config;
	private PlayerData playerData;
	private Achievements achievements;
	
	public PlayerDamage(CubeRunner plugin) {
		this.mySQL = plugin.getMySQL();
		this.config = plugin.getConfiguration();
		this.playerData = plugin.getPlayerData();
		this.achievements = plugin.getAchievements();
	}

	@EventHandler
	public void onPlayerDamageByEntity(EntityDamageByEntityEvent event) {

		if (!(event.getEntity() instanceof Player))
			return;
		
		Player player = (Player) event.getEntity();
		Arena arena = Arena.getArenaFromPlayer(player);

		if (arena == null)
			return;

		if (event.getCause() != DamageCause.FALLING_BLOCK) {
			event.setCancelled(true);
			return;
		}

		if (arena.getGameState() != GameState.ACTIVE) {
			event.setCancelled(true);
			return;
		}
		
		User user = arena.getUser(player);
		if (user.isEliminated()) {
			event.setCancelled(true);
			return;
		}

		String dammagerUUID = event.getDamager().getCustomName();
		if (!dammagerUUID.equalsIgnoreCase(player.getUniqueId().toString())) {
			int kills = 0;
			if (mySQL.hasConnection()) {
				ResultSet query = mySQL.query("SELECT kills FROM " + config.tablePrefix
										+ "PLAYERS WHERE UUID='" + dammagerUUID + "';");
				try {
					if (query.next())
						kills = query.getInt("kills");
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				mySQL.update("UPDATE " + config.tablePrefix + "PLAYERS SET kills='" + ++kills
							+ "' WHERE UUID='" + dammagerUUID + "';");
			} else {
				kills = playerData.getData().getInt("players." + dammagerUUID + ".kills", 0);
				playerData.getData().set("players." + dammagerUUID + ".kills", ++kills);
				playerData.savePlayerData();
			}
			
			achievements.checkAchievement(AchievementType.AMOUNT_KILLS, arena.getPlayerFromUUID(dammagerUUID));
		}

		event.setDamage(0);
		arena.eliminateUser(arena.getUser(player), false);
	}
	
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent event) {

		if (!(event.getEntity() instanceof Player))
			return;
		
		Player player = (Player) event.getEntity();
		Arena arena = Arena.getArenaFromPlayer(player);

		if (arena == null)
			return;

		if (event.getCause() != DamageCause.FALLING_BLOCK) {
			event.setCancelled(true);
			return;
		}
		
		User user = arena.getUser(player);
		if (user.isEliminated()) {
			event.setCancelled(true);
			return;
		}
	}

}
