package me.poutineqc.cuberunner.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import me.poutineqc.cuberunner.CRStats;
import me.poutineqc.cuberunner.CRPlayer.PlayerStatsException;
import me.poutineqc.cuberunner.game.Arena;
import me.poutineqc.cuberunner.game.GameState;
import me.poutineqc.cuberunner.game.Arena.LeavingReason;

public class ListenerPlayerDisconnect implements Listener {
	
	@EventHandler
	public void onPlayerDisconnect(PlayerQuitEvent event) throws PlayerStatsException {
		
		Player player = event.getPlayer();
		Arena arena = Arena.getArenaFromPlayer(player);

		if (arena == null)
			return;
		
		if (arena.getGameState() == GameState.STARTUP)
			arena.getUser(player).getCRPlayer().doneChallenge(CRStats.THE_RAGE_QUIT);
			
		arena.removePlayer(player, LeavingReason.DISCONNECT);

	}
}
