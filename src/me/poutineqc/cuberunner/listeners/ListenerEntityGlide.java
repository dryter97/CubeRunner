package me.poutineqc.cuberunner.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;

import me.poutineqc.cuberunner.game.Arena;

public class ListenerEntityGlide implements Listener {

	@EventHandler
	public void onEntityToggleGlide(EntityToggleGlideEvent event) {
		if (!event.isGliding())
			return;
		
		if (!(event.getEntity() instanceof Player))
			return;
		
		Player player = (Player) event.getEntity();
		Arena arena = Arena.getArenaFromPlayer(player);
		if (arena == null)
			return;
		
		event.setCancelled(true);
		
	}

}
