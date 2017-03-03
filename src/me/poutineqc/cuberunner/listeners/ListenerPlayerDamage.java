package me.poutineqc.cuberunner.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import me.poutineqc.cuberunner.CRStats;
import me.poutineqc.cuberunner.CubeRunner;
import me.poutineqc.cuberunner.CRPlayer.PlayerStatsException;
import me.poutineqc.cuberunner.game.Arena;
import me.poutineqc.cuberunner.game.GameState;
import me.poutineqc.cuberunner.game.User;
import me.poutineqc.cuberunner.game.Arena.LeavingReason;

public class ListenerPlayerDamage implements Listener {

	@EventHandler
	public void onPlayerDamageByEntity(EntityDamageByEntityEvent event) throws PlayerStatsException {

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

		event.setDamage(0);
		arena.eliminateUser(arena.getUser(player), LeavingReason.CRUSHED);

		String dammagerUUID = event.getDamager().getCustomName();
		if (!dammagerUUID.equalsIgnoreCase(player.getUniqueId().toString()))
			CubeRunner.get().getCRPlayer(Bukkit.getPlayer(UUID.fromString(dammagerUUID))).increment(CRStats.KILLS,
					true);
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
