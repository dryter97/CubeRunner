package me.poutineqc.cuberunner.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import me.poutineqc.cuberunner.CRPlayer;
import me.poutineqc.cuberunner.CubeRunner;

public class ListenerInventoryClose implements Listener {

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (!(event.getPlayer() instanceof Player))
			return;
		
		CRPlayer player = CubeRunner.get().getCRPlayer((Player) event.getPlayer());
		player.setCurrentInventory(null);
		
		
	}
	
}
