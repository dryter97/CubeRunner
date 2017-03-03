package me.poutineqc.cuberunner.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import me.poutineqc.cuberunner.CubeRunner;
import me.poutineqc.cuberunner.Language;
import me.poutineqc.cuberunner.Language.Messages;
import me.poutineqc.cuberunner.commands.signs.CRSignJoin;
import me.poutineqc.cuberunner.commands.signs.CRSignPlay;
import me.poutineqc.cuberunner.commands.signs.CRSignQuit;
import me.poutineqc.cuberunner.commands.signs.CRSignStart;
import me.poutineqc.cuberunner.commands.signs.CRSignStats;
import me.poutineqc.cuberunner.commands.signs.CRSignTop;
import me.poutineqc.cuberunner.game.Arena;
import me.poutineqc.cuberunner.utils.Permissions;

public class ListenerSignUpdate implements Listener {

	@EventHandler
	public void onSignPlace(SignChangeEvent event) {

		Language local = CubeRunner.get().getLang(event.getPlayer());

		if (isPrefixInLine(event.getLine(0))) {
			if (!Permissions.hasPermission(Permissions.createSign, event.getPlayer(), false)) {
				setNoPermissionsSign(event, local);
				return;
			}

			if (event.getLine(1).equalsIgnoreCase("join")) {
				Arena arena = Arena.getArena(event.getLine(2));
				if (arena != null) {
					new CRSignJoin(event, arena);
				} else {
					setNoValidSign(event, local);
				}
			} else if (event.getLine(1).equalsIgnoreCase("play")) {
				Arena arena = Arena.getArena(event.getLine(2));
				if (arena != null) {
					if (arena.getWorld() == null) {
						setNoValidSign(event, local);
					} else if (arena.getWorld() != event.getBlock().getWorld()) {
						setNoValidSign(event, local);
					} else {
						new CRSignPlay(event, arena);
					}
				} else {
					setNoValidSign(event, local);
				}
			} else if (event.getLine(1).equalsIgnoreCase("quit")) {
				new CRSignQuit(event);

			} else if (event.getLine(1).equalsIgnoreCase("top")) {
				Arena arena = Arena.getArena(event.getLine(2));
				if (arena != null) {
					new CRSignTop(event, arena);
				} else {
					setNoValidSign(event, local);
				}
			} else if (event.getLine(1).equalsIgnoreCase("start")) {
				new CRSignStart(event);

			} else if (event.getLine(1).equalsIgnoreCase("stats")) {
				new CRSignStats(event);

			} else {
				setNoValidSign(event, local);
			}
		} else if (isPrefixInLine(event.getLine(1)) || isPrefixInLine(event.getLine(2))
				|| isPrefixInLine(event.getLine(3))) {

			if (Permissions.hasPermission(Permissions.createSign, event.getPlayer(), false))
				setNoValidSign(event, local);
			else
				setNoPermissionsSign(event, local);
		}
	}

	private void setNoPermissionsSign(SignChangeEvent e, Language local) {
		e.setLine(0, ChatColor.translateAlternateColorCodes('&', local.get(Messages.SIGN_PERM_0)));
		e.setLine(1, ChatColor.translateAlternateColorCodes('&', local.get(Messages.SIGN_PERM_1)));
		e.setLine(2, ChatColor.translateAlternateColorCodes('&', local.get(Messages.SIGN_PERM_2)));
		e.setLine(3, ChatColor.translateAlternateColorCodes('&', local.get(Messages.SIGN_PERM_3)));
	}

	private void setNoValidSign(SignChangeEvent e, Language local) {
		e.setLine(0, ChatColor.translateAlternateColorCodes('&', local.get(Messages.PREFIX_LONG)));
		e.setLine(1, ChatColor.translateAlternateColorCodes('&', local.get(Messages.SIGN_VALID_1)));
		e.setLine(2, ChatColor.translateAlternateColorCodes('&', local.get(Messages.SIGN_VALID_2)));
		e.setLine(3, ChatColor.translateAlternateColorCodes('&', local.get(Messages.SIGN_VALID_3)));
	}

	private boolean isPrefixInLine(String line) {
		Language local = Language.getDefault();
		String stipedLine = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', line)).toLowerCase();
		return stipedLine.contains("[cr]")
				|| stipedLine.contains(ChatColor
						.stripColor(ChatColor.translateAlternateColorCodes('&', local.get(Messages.PREFIX_LONG).toLowerCase().trim())))
				|| stipedLine.contains(ChatColor.stripColor(
						ChatColor.translateAlternateColorCodes('&', local.get(Messages.PREFIX_SHORT).toLowerCase().trim())));
	}

}
