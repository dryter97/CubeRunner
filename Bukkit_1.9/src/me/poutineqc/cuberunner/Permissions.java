package me.poutineqc.cuberunner;

import org.bukkit.entity.Player;

import me.poutineqc.cuberunner.commands.CubeRunnerCommand;

public class Permissions {

	private static PlayerData playerData;
	
	public static final String createSign = "cuberunner.admin.edit.sign";
	public static final String advancedInfo = "cuberunner.admin.info";

	public Permissions(CubeRunner plugin) {
		playerData = plugin.getPlayerData();
	}

	public static boolean hasPermission(CubeRunnerCommand command, Player player, boolean warning) {
		return hasPermission(command.getPermission(), player, warning);
	}

	public static boolean hasPermission(String permission, Player player, boolean warning) {
		if (player.hasPermission(permission))
			return true;

		if (warning) {
			Language local = playerData.getLanguageOfPlayer(player);
			local.sendMsg(player, local.errorPermission);
		}

		return false;
	}

}
