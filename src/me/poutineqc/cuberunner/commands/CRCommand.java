package me.poutineqc.cuberunner.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import me.poutineqc.cuberunner.CubeRunner;
import me.poutineqc.cuberunner.Language;
import me.poutineqc.cuberunner.Language.Messages;
import me.poutineqc.cuberunner.commands.inventories.CRInventoryColor;
import me.poutineqc.cuberunner.commands.inventories.CRInventoryJoin;
import me.poutineqc.cuberunner.commands.inventories.CRInventoryStats;
import me.poutineqc.cuberunner.game.Arena;
import me.poutineqc.cuberunner.game.GameState;
import me.poutineqc.cuberunner.game.Arena.LeavingReason;
import me.poutineqc.cuberunner.utils.Permissions;
import me.poutineqc.cuberunner.utils.Utils;

public enum CRCommand {

	HELP("help", Messages.COMMAND_HELP, "cuberunner.player.help", "/%command% help [category] [page]",
			CRCommandType.GENERAL) {
		@Override
		public void execute(CubeRunner plugin, Player player, String[] args, Object... extra) {
			Language local = CubeRunner.get().getLang(player);
			String cmdValue = extra.length > 0 ? ((extra[0] instanceof String) ? (String) extra[0] : "cr") : "cr";
			String header = Utils.color("&8&m" + StringUtils.repeat(" ", 15) + "&r&8| &5CubeRunner &d"
					+ local.get(Messages.KEYWORD_HELP) + "&8&m|" + StringUtils.repeat(" ", 35));

			if (args.length == 1) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', header));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&5/" + cmdValue + " help general &8- " + local.get(Messages.HELP_DESCRIPTION_GENERALL)));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&5/" + cmdValue + " help game &8- " + local.get(Messages.HELP_DESCRIPTION_GAME)));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&5/" + cmdValue + " help arena &8- " + local.get(Messages.HELP_DESCRIPTION_ARENA)));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&5/" + cmdValue + " help admin &8- " + local.get(Messages.HELP_DESCRIPTION_ADMIN)));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&5/" + cmdValue + " help all &8- " + local.get(Messages.HELP_DESCRIPTION_ALL)));
				player.sendMessage("\n");
				return;
			}

			int pageNumber = 1;
			CRCommandType commandType = null;
			List<CRCommand> requestedCommands;

			try {
				pageNumber = Integer.parseInt(args[1]);
				if (pageNumber < 1)
					pageNumber = 1;

				requestedCommands = CRCommand.getRequiredCommands(player, commandType);
				if (pageNumber > Math.ceil((double) requestedCommands.size() / 3))
					pageNumber = (int) Math.ceil((double) requestedCommands.size() / 3);

			} catch (NumberFormatException e) {
				switch (args[1].toLowerCase()) {
				case "game":
					commandType = CRCommandType.GAME;
					break;
				case "arena":
					commandType = CRCommandType.ARENA;
					break;
				case "admin":
					commandType = CRCommandType.ADMIN;
					break;
				case "general":
					commandType = CRCommandType.GENERAL;
					break;
				}

				requestedCommands = CRCommand.getRequiredCommands(player, commandType);

				if (args.length > 2) {
					try {
						pageNumber = Integer.parseInt(args[2]);
						if (pageNumber < 1)
							pageNumber = 1;

						if (pageNumber > Math.ceil((double) requestedCommands.size() / 3))
							pageNumber = (int) Math.ceil((double) requestedCommands.size() / 3);

					} catch (NumberFormatException ex) {
					}
				}
			}

			if (requestedCommands.size() == 0)
				pageNumber = 0;

			player.sendMessage(ChatColor.translateAlternateColorCodes('&', header));
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&5" + local.get(Messages.KEYWORD_HELP_CATEGORY) + ": &7"
							+ (commandType == null ? "ALL" : commandType.toString()) + ", &5"
							+ local.get(Messages.KEYWORD_HELP_PAGE) + ": &7" + String.valueOf(pageNumber) + "&8/&7"
							+ (int) (Math.ceil((double) requestedCommands.size() / 3))));

			if (pageNumber == 0) {
				local.sendMsg(player, local.get(Messages.HELP_ERROR_PERMISSION));
				return;
			}

			for (int i = 3 * (pageNumber - 1); i < requestedCommands.size() && i < (3 * (pageNumber - 1)) + 3; i++) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&5" + requestedCommands.get(i).getUsage().replace("%command%", cmdValue)));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						" &8- &7" + local.get(requestedCommands.get(i).getDescription())));
			}
			player.sendMessage("\n");
		}

		@Override
		public void complete(List<String> tabCompletion, String[] args) {
			if (args.length == 2)
				for (CRCommandType category : CRCommandType.values())
					if (category.name().toLowerCase().startsWith(args[1].toLowerCase()))
						tabCompletion.add(category.name().toLowerCase());
		}
	},
	LANGUAGE("lang", Messages.COMMAND_LANGUAGE, "cuberunner.player.language", "/%command% language <language>",
			CRCommandType.GENERAL) {
		@Override
		public void execute(CubeRunner plugin, Player player, String[] args, Object... extra) {
			Language local = CubeRunner.get().getLang(player);
			if (args.length == 1) {
				local.sendMsg(player, local.get(Messages.LANGUAGE_LIST));
				for (Entry<String, Language> language : Language.getLanguages().entrySet())
					player.sendMessage("- " + language.getValue().getLanguageName());

				return;
			}

			Language language = Language.getLanguage(args[1]);
			if (language == null) {
				String cmdValue = extra.length > 0 ? ((extra[0] instanceof String) ? (String) extra[0] : "cr") : "cr";
				local.sendMsg(player, local.get(Messages.LANGUAGE_NOT_FOUND).replace("%cmd%", cmdValue));
				return;
			}

			CubeRunner.get().getCRPlayer(player).setLanguage(language);

			language.sendMsg(player, language.get(Messages.LANGUAGE_CHANGED));
		}

		@Override
		public void complete(List<String> tabCompletion, String[] args) {
			if (args.length == 2)
				for (Entry<String, Language> lang : Language.getLanguages().entrySet())
					if (lang.getValue().getLanguageName().toLowerCase().startsWith(args[1].toLowerCase()))
						tabCompletion.add(lang.getValue().getLanguageName());
		}
	},
	STATS("stats", Messages.COMMAND_STATS, "cuberunner.player.stats", "/%command% stats", CRCommandType.GENERAL) {
		@Override
		public void execute(CubeRunner plugin, Player player, String[] args, Object... extra) {
			new CRInventoryStats(plugin.getCRPlayer(player));
		}

		@Override
		public void complete(List<String> tabCompletion, String[] args) {
			return;
		}
	},

	LIST("list", Messages.COMMAND_LIST, "cuberunner.player.list", "/%command% list", CRCommandType.GAME) {
		@Override
		public void execute(CubeRunner plugin, Player player, String[] args, Object... extra) {
			JOIN.execute(plugin, player, new String[0]);
		}

		@Override
		public void complete(List<String> tabCompletion, String[] args) {
			return;
		}
	},
	INFO("info", Messages.COMMAND_INFO, "cuberunner.player.info", "/%command% info <arena>", CRCommandType.GAME) {
		@Override
		public void execute(CubeRunner plugin, Player player, String[] args, Object... extra) {
			Language local = CubeRunner.get().getLang(player);

			Arena arena = args.length > 1 ? Arena.getArena(args[1]) : null;
			if (arena == null) {
				local.sendMsg(player, local.get(Messages.ERROR_MISSING_ARENA));
				return;
			}

			String cmdValue = extra.length > 0 ? ((extra[0] instanceof String) ? (String) extra[0] : "cr") : "cr";

			if (args.length == 1) {
				local.sendMsg(player, local.get(Messages.ERROR_MISSING_ARENA));
				local.sendMsg(player, local.get(Messages.INFO_TIP).replace("%cmd%", cmdValue));
				return;
			}

			arena = Arena.getArena(args[1]);
			if (arena == null) {
				local.sendMsg(player, local.get(Messages.ERROR_MISSING_ARENA).replace("%arena%", args[1]));
				local.sendMsg(player, local.get(Messages.INFO_TIP).replace("%cmd%", cmdValue));
				return;
			}

			arena.displayInformation(player);

		}

		@Override
		public void complete(List<String> tabCompletion, String[] args) {
			if (args.length == 2)
				for (Arena arena : Arena.getArenas())
					if (arena.getName().startsWith(args[1].toLowerCase()))
						tabCompletion.add(arena.getName());
		}
	},
	JOIN("join", Messages.COMMAND_JOIN, "cuberunner.player.play.join", "/%command% join [arena]", CRCommandType.GAME) {
		@Override
		public void execute(CubeRunner plugin, Player player, String[] args, Object... extra) {
			Language local = CubeRunner.get().getLang(player);

			Arena arena = Arena.getArenaFromPlayer(player);
			if (arena != null) {
				local.sendMsg(player, local.get(Messages.ERROR_ALREADY_IN_GAME));
				return;
			}

			arena = args.length > 1 ? Arena.getArena(args[1]) : null;
			if (arena == null) {
				new CRInventoryJoin(CubeRunner.get().getCRPlayer(player), 1);
				return;
			}

			boolean teleport = extra.length > 0 ? ((extra[0] instanceof Boolean) ? (boolean) extra[0] : true) : false;
			arena.addPlayer(player, teleport);

		}

		@Override
		public void complete(List<String> tabCompletion, String[] args) {
			if (args.length == 2)
				for (Arena arena : Arena.getArenas())
					if (arena.getName().toLowerCase().startsWith(args[1].toLowerCase()))
						tabCompletion.add(arena.getName());
		}
	},
	QUIT("quit", Messages.COMMAND_QUIT, "cuberunner.player.play.quit", "/%command% quit", CRCommandType.GAME) {
		@Override
		public void execute(CubeRunner plugin, Player player, String[] args, Object... extra) {
			Language local = CubeRunner.get().getLang(player);

			Arena arena = Arena.getArenaFromPlayer(player);
			if (arena == null) {
				local.sendMsg(player, local.get(Messages.ERROR_NOT_IN_GAME));
				return;
			}

			arena.removePlayer(player, LeavingReason.COMMAND);
		}

		@Override
		public void complete(List<String> tabCompletion, String[] args) {
			return;
		}
	},
	START("start", Messages.COMMAND_START, "cuberunner.player.play.start", "/%command% start", CRCommandType.GAME) {
		@Override
		public void execute(CubeRunner plugin, Player player, String[] args, Object... extra) {
			Language local = CubeRunner.get().getLang(player);

			Arena arena = Arena.getArenaFromPlayer(player);
			if (arena == null) {
				local.sendMsg(player, local.get(Messages.ERROR_NOT_IN_GAME));
				return;
			}

			if (arena.getGameState() != GameState.READY) {
				local.sendMsg(player, local.get(Messages.START_NOT_READY));
				return;
			}

			arena.initiateGame(player);
		}

		@Override
		public void complete(List<String> tabCompletion, String[] args) {
			return;
		}
	},

	NEW("new", Messages.COMMAND_NEW, "cuberunner.admin.edit.new", "/%command% new <arenaName>", CRCommandType.ARENA) {
		@Override
		public void execute(CubeRunner plugin, Player player, String[] args, Object... extra) {
			Language local = CubeRunner.get().getLang(player);

			if (args.length <= 1) {
				local.sendMsg(player, local.get(Messages.EDIT_CREATE_NONAME));
				return;
			}

			if (Arena.getArena(args[1]) != null) {
				local.sendMsg(player, local.get(Messages.EDIT_NEW_EXISTS).replace("%arena%", args[1]));
				return;
			}

			if (args[1].length() > 12) {
				local.sendMsg(player, local.get(Messages.EDIT_NEW_LONG_NAME));
				return;
			}

			new Arena(args[1], player);
			local.sendMsg(player, local.get(Messages.EDIT_NEW_SUCCESS).replace("%arena%", args[1]));
		}

		@Override
		public void complete(List<String> tabCompletion, String[] args) {
			return;
		}
	},
	DELETE("delete", Messages.COMMAND_DELETE, "cuberunner.admin.edit.delete", "/%command% delete <arenaName>",
			CRCommandType.ARENA) {
		@Override
		public void execute(CubeRunner plugin, Player player, String[] args, Object... extra) {
			Language local = CubeRunner.get().getLang(player);

			Arena arena = args.length > 1 ? Arena.getArena(args[1]) : null;
			if (arena == null) {
				local.sendMsg(player, local.get(Messages.ERROR_MISSING_ARENA));
				return;
			}

			arena.delete(player);
		}

		@Override
		public void complete(List<String> tabCompletion, String[] args) {
			if (args.length == 2)
				for (Arena arena : Arena.getArenas())
					if (arena.getName().toLowerCase().startsWith(args[1].toLowerCase()))
						tabCompletion.add(arena.getName());
		}
	},
	SETZONE("setzone", Messages.COMMAND_SETZONE, "cuberunner.admin.edit.zone", "/%command% setzone <arenaName>",
			CRCommandType.ARENA) {
		@Override
		public void execute(CubeRunner plugin, Player player, String[] args, Object... extra) {
			Language local = CubeRunner.get().getLang(player);

			Arena arena = args.length > 1 ? Arena.getArena(args[1]) : null;
			if (arena == null) {
				local.sendMsg(player, local.get(Messages.ERROR_MISSING_ARENA));
				return;
			}

			arena.setArena(player);
		}

		@Override
		public void complete(List<String> tabCompletion, String[] args) {
			if (args.length == 2)
				for (Arena arena : Arena.getArenas())
					if (arena.getName().toLowerCase().startsWith(args[1].toLowerCase()))
						tabCompletion.add(arena.getName());
		}
	},
	SETLOBBY("setlobby", Messages.COMMAND_SETLOBBY, "cuberunner.admin.edit.lobby", "/%command% setlobby <arenaName>",
			CRCommandType.ARENA) {
		@Override
		public void execute(CubeRunner plugin, Player player, String[] args, Object... extra) {
			Language local = CubeRunner.get().getLang(player);

			Arena arena = args.length > 1 ? Arena.getArena(args[1]) : null;
			if (arena == null) {
				local.sendMsg(player, local.get(Messages.ERROR_MISSING_ARENA));
				return;
			}

			arena.setLobby(player);
		}

		@Override
		public void complete(List<String> tabCompletion, String[] args) {
			if (args.length == 2)
				for (Arena arena : Arena.getArenas())
					if (arena.getName().toLowerCase().startsWith(args[1].toLowerCase()))
						tabCompletion.add(arena.getName());
		}
	},
	SETSTARTPOINT("setstartpoint", Messages.COMMAND_SETSTARTPOINT, "cuberunner.admin.edit.startpoint",
			"/%command% setstartpoint <arenaName>", CRCommandType.ARENA) {
		@Override
		public void execute(CubeRunner plugin, Player player, String[] args, Object... extra) {
			Language local = CubeRunner.get().getLang(player);

			Arena arena = args.length > 1 ? Arena.getArena(args[1]) : null;
			if (arena == null) {
				local.sendMsg(player, local.get(Messages.ERROR_MISSING_ARENA));
				return;
			}

			arena.setStartPoint(player);
		}

		@Override
		public void complete(List<String> tabCompletion, String[] args) {
			if (args.length == 2)
				for (Arena arena : Arena.getArenas())
					if (arena.getName().toLowerCase().startsWith(args[1].toLowerCase()))
						tabCompletion.add(arena.getName());
		}
	},
	SETMINPLAYER("setminplayer", Messages.COMMAND_SETMINPLAYER, "cuberunner.admin.edit.amountplayer.minplayer",
			"/%command% setminplayer <arenaName> <amount>", CRCommandType.ARENA) {
		@Override
		public void execute(CubeRunner plugin, Player player, String[] args, Object... extra) {
			Language local = CubeRunner.get().getLang(player);

			Arena arena = args.length > 1 ? Arena.getArena(args[1]) : null;
			if (arena == null) {
				local.sendMsg(player, local.get(Messages.ERROR_MISSING_ARENA));
				return;
			}

			if (args.length < 3) {
				local.sendMsg(player, local.get(Messages.EDIT_PLAYERS_MISSING));
			}

			try {
				arena.setMinPlayer(Integer.parseInt(args[2]), player);
			} catch (NumberFormatException e) {
				local.sendMsg(player, local.get(Messages.EDIT_PLAYERS_ERROR).replace("%error%",
						local.get(Messages.EDIT_PLAYERS_NAN)));
				return;
			}

		}

		@Override
		public void complete(List<String> tabCompletion, String[] args) {
			if (args.length == 2)
				for (Arena arena : Arena.getArenas())
					if (arena.getName().toLowerCase().startsWith(args[1].toLowerCase()))
						tabCompletion.add(arena.getName());
		}
	},
	SETMAXPLAYER("setmaxplayer", Messages.COMMAND_SETMAXPLAYER, "cuberunner.admin.edit.amountplayer.maxplayer",
			"/%command% setmaxplayer <arenaName> <amount>", CRCommandType.ARENA) {
		@Override
		public void execute(CubeRunner plugin, Player player, String[] args, Object... extra) {
			Language local = CubeRunner.get().getLang(player);

			Arena arena = args.length > 1 ? Arena.getArena(args[1]) : null;
			if (arena == null) {
				local.sendMsg(player, local.get(Messages.ERROR_MISSING_ARENA));
				return;
			}

			if (args.length < 3) {
				local.sendMsg(player, local.get(Messages.EDIT_PLAYERS_MISSING));
			}

			try {
				arena.setMaxPlayer(Integer.parseInt(args[2]), player);
			} catch (NumberFormatException e) {
				local.sendMsg(player, local.get(Messages.EDIT_PLAYERS_ERROR).replace("%error%",
						local.get(Messages.EDIT_PLAYERS_NAN)));
				return;
			}

		}

		@Override
		public void complete(List<String> tabCompletion, String[] args) {
			if (args.length == 2)
				for (Arena arena : Arena.getArenas())
					if (arena.getName().toLowerCase().startsWith(args[1].toLowerCase()))
						tabCompletion.add(arena.getName());

		}
	},
	SETCOLOR("setcolor", Messages.COMMAND_SETCOLOR, "cuberunner.admin.edit.color", "/%command% setcolor <arenaName>",
			CRCommandType.ARENA) {
		@Override
		public void execute(CubeRunner plugin, Player player, String[] args, Object... extra) {
			Language local = CubeRunner.get().getLang(player);

			Arena arena = args.length > 1 ? Arena.getArena(args[1]) : null;
			if (arena == null) {
				local.sendMsg(player, local.get(Messages.ERROR_MISSING_ARENA));
				return;
			}

			if (arena.getGameState() == GameState.ACTIVE || arena.getGameState() == GameState.ENDING) {
				local.sendMsg(player, local.get(Messages.EDIT_COLOR_ERROR));
				return;
			}

			new CRInventoryColor(plugin.getCRPlayer(player), arena);
		}

		@Override
		public void complete(List<String> tabCompletion, String[] args) {
			if (args.length == 2)
				for (Arena arena : Arena.getArenas())
					if (arena.getName().toLowerCase().startsWith(args[1].toLowerCase()))
						tabCompletion.add(arena.getName());
		}
	},

	RELOAD("reload", Messages.COMMAND_RELOAD, "cuberunner.admin.reload", "/%command% reload", CRCommandType.ADMIN) {
		@Override
		public void execute(CubeRunner plugin, Player player, String[] args, Object... extra) {
			CubeRunner.get().reload();

			Language local = CubeRunner.get().getLang(player);
			local.sendMsg(player, local.get(Messages.ADMIN_RELOAD));
		}

		@Override
		public void complete(List<String> tabCompletion, String[] args) {
			return;
		}
	};

	private final String commandName;
	private final Messages description;
	private final String permission;
	private final String usage;
	private final CRCommandType type;

	public abstract void execute(CubeRunner plugin, Player player, String[] args, Object... extra);

	public abstract void complete(List<String> tabCompletion, String[] args);

	private CRCommand(String commandName, Messages description, String permission, String usage, CRCommandType type) {
		this.commandName = commandName;
		this.description = description;
		this.permission = permission;
		this.usage = usage;
		this.type = type;
	}

	public String getCommandName() {
		return commandName;
	}

	public Messages getDescription() {
		return description;
	}

	public String getPermission() {
		return permission;
	}

	public String getUsage() {
		return usage;
	}

	public static CRCommand getCommand(String argument) {
		for (CRCommand command : CRCommand.values()) {
			if (command.commandName.equalsIgnoreCase(argument))
				return command;
		}
		return null;
	}

	public static List<CRCommand> getRequiredCommands(Player player, CRCommandType commandType) {
		List<CRCommand> requestedCommands = new ArrayList<CRCommand>();

		for (CRCommand command : CRCommand.values())
			if (command.type == commandType || commandType == null)
				if (Permissions.hasPermission(command.permission, player, false))
					requestedCommands.add(command);

		return requestedCommands;
	}

	public enum CRCommandType {
		GENERAL, GAME, ARENA, ADMIN;
	}

}
