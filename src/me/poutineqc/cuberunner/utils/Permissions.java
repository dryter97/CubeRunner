package me.poutineqc.cuberunner.utils;

import org.bukkit.entity.Player;

import me.poutineqc.cuberunner.CubeRunner;
import me.poutineqc.cuberunner.Language;
import me.poutineqc.cuberunner.Language.Messages;

public class Permissions {
	public static final String createSign = "cuberunner.admin.edit.sign";
	public static final String advancedInfo = "cuberunner.admin.info";

	public static boolean hasPermission(String permission, Player player, boolean warning) {
		if (player.hasPermission(permission))
			return true;

		if (warning) {
			Language local = CubeRunner.get().getCRPlayer(player).getLanguage();
			local.sendMsg(player, local.get(Messages.ERROR_PERMISSION));
		}

		return false;
	}

}
