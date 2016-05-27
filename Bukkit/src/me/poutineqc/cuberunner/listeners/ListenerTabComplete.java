package me.poutineqc.cuberunner.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import me.poutineqc.cuberunner.commands.CRCommand;

public class ListenerTabComplete implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		List<String> tabCompletion = new ArrayList<String>();
		
		if (!(sender instanceof Player))
			return tabCompletion;
		
		Player player = (Player) sender;
		
		if (args.length == 1) {
			for (CRCommand command : CRCommand.getRequiredCommands(player, null))
				if (command.getCommandName().startsWith(args[0].toLowerCase()))
					tabCompletion.add(command.getCommandName());
			
			return tabCompletion;
		}

		CRCommand command = CRCommand.getCommand(args[0].toLowerCase());
		if (command == null)
			return tabCompletion;
		
		command.complete(tabCompletion, args);
		return tabCompletion;
	}
}
