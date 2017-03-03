package me.poutineqc.cuberunner.listeners;

import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class ListenerEntityChangeBlock implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		if ((event.getEntity() instanceof FallingBlock)) {
			FallingBlock fallingBlock = (FallingBlock) event.getEntity();
			if ((fallingBlock.getMaterial() == Material.WOOL) && (event.getBlock().getType() == Material.AIR)) {
				if (fallingBlock.getCustomName()
						.matches("[a-f0-9]{8}-[a-f0-9]{4}-4[0-9]{3}-[89ab][a-f0-9]{3}-[0-9a-f]{12}"))
					event.setCancelled(false);
			}
		}
	}

}
