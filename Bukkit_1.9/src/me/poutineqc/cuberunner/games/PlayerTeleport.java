package me.poutineqc.cuberunner.games;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import me.poutineqc.cuberunner.CubeRunner;
import me.poutineqc.cuberunner.Language;
import me.poutineqc.cuberunner.PlayerData;

public class PlayerTeleport implements Listener {

	private PlayerData playerData;

	public PlayerTeleport(CubeRunner plugin) {
		this.playerData = plugin.getPlayerData();
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		Arena arena = Arena.getArenaFromPlayer(player);

		if (arena == null)
			return;

		if (teleportNearPoint(event.getTo(), arena.getLobby())
				|| teleportNearPoint(event.getTo(), arena.getStartPoint()) || arena.isInsideArena(event.getTo()))
			return;

		User user = arena.getUser(player);
		if (user.hasLeft())
			return;

		Language local = playerData.getLanguageOfPlayer(player);
		local.sendMsg(player, local.errorTeleport);

		event.setCancelled(true);

	}

	private boolean teleportNearPoint(Location to, Location point) {
		if (to.getWorld() != point.getWorld())
			return false;

		if (to.distance(point) > 1)
			return false;

		return true;
	}

}
