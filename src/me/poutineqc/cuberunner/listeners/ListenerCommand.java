package me.poutineqc.cuberunner.listeners;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.poutineqc.cuberunner.CubeRunner;
import me.poutineqc.cuberunner.Language;
import me.poutineqc.cuberunner.Language.Messages;
import me.poutineqc.cuberunner.commands.CRCommand;
import me.poutineqc.cuberunner.utils.Permissions;
import me.poutineqc.cuberunner.utils.Utils;

public class ListenerCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdValue, String[] args) {

		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players can use CubeRunner's commands");
			return true;
		}

		CubeRunner plugin = CubeRunner.get();
		Player player = (Player) sender;
		Language local = CubeRunner.get().getLang(player);

		if (args.length == 0) {
			player.sendMessage(Utils.color("&8&m" + StringUtils.repeat(" ", 15) + "&r&8| &5CubeRunner " + "&8&m|" + StringUtils.repeat(" ", 40)));
			player.sendMessage(Utils
					.color(local.get(Messages.DEVELOPPER).replace("%developper%", plugin.getDescription().getAuthors().toString())));
			player.sendMessage(Utils.color(local.get(Messages.VERSION).replace("%version%", plugin.getDescription().getVersion())));
			player.sendMessage(Utils.color(local.get(Messages.DESCRIPTION).replace("%command%", cmdValue)));
			player.sendMessage("\n");
			return true;
		}

		CRCommand crCommand = CRCommand.getCommand(args[0]);
		if (crCommand == null) {
			local.sendMsg(player, local.get(Messages.ERROR_COMMAND).replace("%cmd%", cmdValue));
			return true;
		}

		if (!Permissions.hasPermission(crCommand.getPermission(), player, true))
			return true;

		switch (crCommand) {
		case HELP:
		case LANGUAGE:
		case INFO:
			crCommand.execute(plugin, player, args, cmdValue);
			break;
		case JOIN:
			crCommand.execute(plugin, player, args, "false");
			break;
		default:
			crCommand.execute(plugin, player, args);
		}
		return true;
	}
}
