package me.poutineqc.cuberunner.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import me.poutineqc.cuberunner.CRStats;
import me.poutineqc.cuberunner.CRPlayer.PlayerStatsException;
import me.poutineqc.cuberunner.game.Arena;
import me.poutineqc.cuberunner.game.GameState;
import me.poutineqc.cuberunner.game.User;

public class ListenerPlayerMove implements Listener {

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) throws PlayerStatsException {
		Player player = event.getPlayer();
		Arena arena = Arena.getArenaFromPlayer(player);

		if (arena == null)
			return;

		User user = arena.getUser(player);
		if (user.isEliminated())
			return;

		if (arena.getGameState() != GameState.ACTIVE && arena.getGameState() != GameState.STARTUP)
			return;

		if (!arena.isInsideArena(event.getTo())) {
			event.setCancelled(true);
			return;
		}
		
		if (arena.getGameState() != GameState.ACTIVE)
			return;

		user.addToDistanceRan(event.getTo().toVector().subtract(event.getFrom().toVector()));
		
		double toLocation = event.getTo().getY();
		double fromLocation = event.getFrom().getY();
		
		if (toLocation > arena.getMinPoint().getY() + 10)
			user.getCRPlayer().doneChallenge(CRStats.REACH_HEIGHT_10);

		if (user.isJumping()) {
			if (toLocation < fromLocation)
				user.setJumping(false);
		} else {
			if (toLocation > fromLocation) {
				user.setJumping(true);
				user.jump();
				
				if (user.getJump() == 50)
					user.getCRPlayer().doneChallenge(CRStats.THE_KILLER_BUNNY);
			}
		}
	}

}
