package me.poutineqc.cuberunner.achievements;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import me.poutineqc.cuberunner.CubeRunner;
import me.poutineqc.cuberunner.games.Arena;
import me.poutineqc.cuberunner.games.GameState;
import me.poutineqc.cuberunner.games.User;

public class PlayerMovement implements Listener {

	private Achievements achievements;

	public PlayerMovement(CubeRunner plugin) {
		this.achievements = plugin.getAchievements();
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
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

		user.addToDistanceRan(event.getFrom().distance(event.getTo()));
		
		double toLocation = event.getTo().getY();
		double fromLocation = event.getFrom().getY();
		
		if (toLocation > arena.getMinPoint().getY() + 10)
			achievements.checkAchievement(AchievementType.REACH_HEIGHT_10, player);

		if (user.isJumping()) {
			if (toLocation < fromLocation)
				user.setJumping(false);
		} else {
			if (toLocation > fromLocation) {
				user.setJumping(true);
				user.jump();
				
				if (user.getJump() == 50)
					achievements.checkAchievement(AchievementType.JUMP_50_TIMES, player);
			}
		}
	}

}
