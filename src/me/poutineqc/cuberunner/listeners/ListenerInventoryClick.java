package me.poutineqc.cuberunner.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import me.poutineqc.cuberunner.CRPlayer;
import me.poutineqc.cuberunner.CubeRunner;
import me.poutineqc.cuberunner.commands.inventories.CRInventory;

public class ListenerInventoryClick implements Listener {
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.NOTHING || event.getAction() == InventoryAction.UNKNOWN)
			return;
		
		if (!(event.getWhoClicked() instanceof Player))
			return;
		
		ItemStack item = event.getCurrentItem();
		CRPlayer player = CubeRunner.get().getCRPlayer((Player) event.getWhoClicked());
		
		CRInventory inv = player.getCurrentInventory();
		if (inv == null)
			return;
		
		event.setCancelled(true);
		inv.update(item, event.getAction());
	}
	
}
