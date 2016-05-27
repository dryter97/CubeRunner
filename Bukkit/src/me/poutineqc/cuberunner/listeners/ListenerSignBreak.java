package me.poutineqc.cuberunner.listeners;

import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import me.poutineqc.cuberunner.commands.signs.CRSign;

public class ListenerSignBreak implements Listener {
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if (!(event.getBlock().getState() instanceof Sign))
			return;
		
		if (event.isCancelled())
			return;
		
		Sign sign = (Sign) event.getBlock().getState();
		CRSign dacSign = CRSign.getCrSign(sign.getLocation());
		if (dacSign != null)
			dacSign.removeSign();
	}

}