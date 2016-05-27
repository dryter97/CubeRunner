package me.poutineqc.cuberunner.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import me.poutineqc.cuberunner.CubeRunner;
import me.poutineqc.cuberunner.Language;
import me.poutineqc.cuberunner.Permissions;
import me.poutineqc.cuberunner.games.Arena;

public class SignPlace implements Listener {

	private static Language local;

	public SignPlace(CubeRunner plugin) {
		SignPlace.local = plugin.getPlayerData().getLanguage("");
	}

	@EventHandler
	public void onSignPlace(SignChangeEvent event) {

		Player player = event.getPlayer();
		if (ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', event.getLine(0))).toLowerCase()
				.contains("[cr]")) {

			if (!Permissions.hasPermission(Permissions.createSign, player, false)) {
				setNoPermissionsSign(event);
				return;
			}

			if (event.getLine(1).equalsIgnoreCase("play"))
				if (Arena.getArena(event.getLine(2)) != null) {
					event.setLine(3, event.getLine(2));
					setPlaySign(event);
					return;
				}

			if (event.getLine(1).equalsIgnoreCase("join"))
				if (Arena.getArena(event.getLine(2)) != null) {
					event.setLine(3, event.getLine(2));
					setJoinSign(event);
					return;
				}

			if (event.getLine(1).equalsIgnoreCase("quit")) {
				setQuitSign(event);
				return;
			}

			if (event.getLine(1).equalsIgnoreCase("start")) {
				setStartSign(event);
				return;
			}

			if (event.getLine(1).equalsIgnoreCase("stats")) {
				setStatsSign(event);
				return;
			}

			setNoValidSign(event);

		} else if (ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', event.getLine(1))).toLowerCase()
				.contains("[cr]")) {

			if (!Permissions.hasPermission(Permissions.createSign, player, false)) {
				setNoPermissionsSign(event);
				return;
			}

			if (event.getLine(2).equalsIgnoreCase("play"))
				if (Arena.getArena(event.getLine(3)) == null)
					setPlaySign(event);

			if (event.getLine(2).equalsIgnoreCase("join"))
				if (Arena.getArena(event.getLine(3)) == null)
					setJoinSign(event);

			if (event.getLine(2).equalsIgnoreCase("quit"))
				setQuitSign(event);

			if (event.getLine(2).equalsIgnoreCase("start"))
				setStartSign(event);

			if (event.getLine(2).equalsIgnoreCase("stats"))
				setStatsSign(event);

			setNoValidSign(event);

		} else if (ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', event.getLine(2))).toLowerCase()
				.contains("[cr]")
				|| ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', event.getLine(3))).toLowerCase()
						.contains("[cr]")) {

			if (!Permissions.hasPermission(Permissions.createSign, player, false)) {
				setNoPermissionsSign(event);
				return;
			}

			setNoValidSign(event);
		}
	}

	private void setNoPermissionsSign(SignChangeEvent e) {
		e.setLine(0, ChatColor.translateAlternateColorCodes('&', local.signNoPermission0));
		e.setLine(1, ChatColor.translateAlternateColorCodes('&', local.signNoPermission1));
		e.setLine(2, ChatColor.translateAlternateColorCodes('&', local.signNoPermission2));
		e.setLine(3, ChatColor.translateAlternateColorCodes('&', local.signNoPermission3));
	}

	private void setNoValidSign(SignChangeEvent e) {
		e.setLine(0, ChatColor.translateAlternateColorCodes('&', local.prefixLong));
		e.setLine(1, ChatColor.translateAlternateColorCodes('&', local.signNotValid1));
		e.setLine(2, ChatColor.translateAlternateColorCodes('&', local.signNotValid2));
		e.setLine(3, ChatColor.translateAlternateColorCodes('&', local.signNotValid3));
	}

	private void setStartSign(SignChangeEvent e) {
		e.setLine(0, "");
		e.setLine(1, ChatColor.translateAlternateColorCodes('&', local.prefixLong));
		e.setLine(2, ChatColor.translateAlternateColorCodes('&', local.signStartGame));
		e.setLine(3, "");
	}

	private void setStatsSign(SignChangeEvent e) {
		e.setLine(0, "");
		e.setLine(1, ChatColor.translateAlternateColorCodes('&', local.prefixLong));
		e.setLine(2, ChatColor.translateAlternateColorCodes('&', local.signOpenStats));
		e.setLine(3, "");
	}

	private void setQuitSign(SignChangeEvent e) {
		e.setLine(0, "");
		e.setLine(1, ChatColor.translateAlternateColorCodes('&', local.prefixLong));
		e.setLine(2, ChatColor.translateAlternateColorCodes('&', local.signQuit));
		e.setLine(3, "");
	}

	private void setJoinSign(SignChangeEvent e) {
		e.setLine(0, "");
		e.setLine(1, ChatColor.translateAlternateColorCodes('&', local.prefixLong));
		e.setLine(2, ChatColor.translateAlternateColorCodes('&', local.signJoin));
	}

	private void setPlaySign(SignChangeEvent e) {
		e.setLine(0, "");
		e.setLine(1, ChatColor.translateAlternateColorCodes('&', local.prefixLong));
		e.setLine(2, ChatColor.translateAlternateColorCodes('&', local.signPlay));
	}

}
