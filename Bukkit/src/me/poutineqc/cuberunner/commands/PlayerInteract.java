package me.poutineqc.cuberunner.commands;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import me.poutineqc.cuberunner.CubeRunner;
import me.poutineqc.cuberunner.Language;

public class PlayerInteract implements Listener {

	private static Language local;
	private PlayerCommands playerCommands;

	public PlayerInteract(CubeRunner plugin) {
		PlayerInteract.local = plugin.getPlayerData().getLanguage("");
		this.playerCommands = plugin.getPlayerCommands();
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
			return;

		if (!(event.getClickedBlock().getState() instanceof Sign))
			return;

		Sign s = (Sign) event.getClickedBlock().getState();

		if (!ChatColor.stripColor(s.getLine(1).toLowerCase().replace(" ", "")).equalsIgnoreCase(
				ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.prefixLong).replace(" ", ""))))
			return;

		event.setCancelled(true);
		
		Player player = event.getPlayer();
		
		if (ChatColor.stripColor(s.getLine(2))
				.equalsIgnoreCase(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.signPlay))))
			playerCommands.commandJoin(CubeRunnerCommand.getCommand("join"), player, 0, s.getLine(3), false);
		
		if (ChatColor.stripColor(s.getLine(2))
				.equalsIgnoreCase(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.signJoin))))
			playerCommands.commandJoin(CubeRunnerCommand.getCommand("join"), player, 0, s.getLine(3), true);
		
		if (ChatColor.stripColor(s.getLine(2))
				.equalsIgnoreCase(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.signQuit))))
			playerCommands.commandQuit(CubeRunnerCommand.getCommand("quit"), player);
		
		if (ChatColor.stripColor(s.getLine(2))
				.equalsIgnoreCase(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.signStartGame))))
			playerCommands.commandStart(CubeRunnerCommand.getCommand("start"), player);

		if (ChatColor.stripColor(s.getLine(2))
				.equalsIgnoreCase(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.signOpenStats))))
			playerCommands.commandStats(CubeRunnerCommand.getCommand("stats"), player);

	}
}
