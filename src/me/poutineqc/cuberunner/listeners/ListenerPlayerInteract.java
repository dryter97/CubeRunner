package me.poutineqc.cuberunner.listeners;

import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import me.poutineqc.cuberunner.commands.signs.CRSign;

public class ListenerPlayerInteract implements Listener {

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
			return;

		if (!(event.getClickedBlock().getState() instanceof Sign))
			return;

		Sign s = (Sign) event.getClickedBlock().getState();
		CRSign crSign = CRSign.getCrSign(s.getLocation());
		if (crSign == null)
			return;

		event.setCancelled(true);
		crSign.onInteract(event.getPlayer());
	}
}
