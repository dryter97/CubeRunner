package me.poutineqc.cuberunner.games;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import me.poutineqc.cuberunner.CubeRunner;
import me.poutineqc.cuberunner.achievements.AchievementType;
import me.poutineqc.cuberunner.achievements.Achievements;

public class PlayerDisconnect implements Listener {

	private Achievements achievements;
	
	public PlayerDisconnect(CubeRunner plugin) {
		this.achievements = plugin.getAchievements();
	}	
	
	@EventHandler
	public void onPlayerDisconnect(PlayerQuitEvent event) {
		
		Player player = event.getPlayer();
		Arena arena = Arena.getArenaFromPlayer(player);

		if (arena == null)
			return;
		
		if (arena.getGameState() == GameState.STARTUP)
			achievements.checkAchievement(AchievementType.DISCONNECT_IN_STARTUP, player);
			
		arena.removePlayer(player, true);

	}
}
