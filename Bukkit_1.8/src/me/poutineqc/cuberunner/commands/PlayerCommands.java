package me.poutineqc.cuberunner.commands;

import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.poutineqc.cuberunner.ArenaData;
import me.poutineqc.cuberunner.Configuration;
import me.poutineqc.cuberunner.CubeRunner;
import me.poutineqc.cuberunner.Language;
import me.poutineqc.cuberunner.MySQL;
import me.poutineqc.cuberunner.Permissions;
import me.poutineqc.cuberunner.PlayerData;
import me.poutineqc.cuberunner.achievements.AchievementGUI;
import me.poutineqc.cuberunner.achievements.Achievements;
import me.poutineqc.cuberunner.games.Arena;
import me.poutineqc.cuberunner.games.ColorGUI;
import me.poutineqc.cuberunner.games.GameState;
import me.poutineqc.cuberunner.tools.JoinGUI;

public class PlayerCommands implements CommandExecutor {
	private CubeRunner plugin;
	private PlayerData playerData;
	private JoinGUI joinGui;
	private AchievementGUI achievementsGUI;
	private ColorGUI colorGUI;
	private ArenaData arenaData;
	private MySQL mysql;
	private Configuration config;
	private Achievements achievements;

	public PlayerCommands(CubeRunner plugin) {
		this.plugin = plugin;
		arenaData = plugin.getArenaData();
		playerData = plugin.getPlayerData();
		joinGui = plugin.getJoinGui();
		achievementsGUI = plugin.getAchievementsGui();
		this.colorGUI = plugin.getColorGUI();
		this.mysql = plugin.getMySQL();
		this.config = plugin.getConfiguration();
		this.achievements = plugin.getAchievements();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdValue, String[] args) {

		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players can use CubeRunner's commands");
			return true;
		}

		Player player = (Player) sender;
		Language local = playerData.getLanguageOfPlayer(player);

		if (args.length == 0) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8&m" + StringUtils.repeat(" ", 90)));
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					local.developper.replace("%developper%", plugin.getDescription().getAuthors().toString())));
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					local.version.replace("%version%", plugin.getDescription().getVersion())));
			local.sendMsg(player, local.description.replace("%command%", cmdValue));
			player.sendMessage("\n");
			return true;
		}

		if (args[0].equalsIgnoreCase("help")) {
			String header = "&8&m" + StringUtils.repeat(" ", 30) + "&r &6CubeRunner &e" + local.keyWordHelp + " &8&m"
					+ StringUtils.repeat(" ", 30);

			if (args.length == 1) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', header));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&5/" + cmdValue + " help general &8- " + local.helpDescriptionGeneral));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&5/" + cmdValue + " help game &8- " + local.helpDescriptionGame));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&5/" + cmdValue + " help arena &8- " + local.helpDescriptionArena));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&5/" + cmdValue + " help admin &8- " + local.helpDescriptionAdmin));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&5/" + cmdValue + " help all &8- " + local.helpDescriptionAll));
				player.sendMessage("\n");
				return true;
			}

			int pageNumber = 1;
			CommandType commandType;
			List<CubeRunnerCommand> requestedCommands;

			try {
				pageNumber = Integer.parseInt(args[1]);
				if (pageNumber < 1)
					pageNumber = 1;

				commandType = CommandType.ALL;

				requestedCommands = CubeRunnerCommand.getRequiredCommands(player, commandType);
				if (pageNumber > Math.ceil((double) requestedCommands.size() / 3))
					pageNumber = (int) Math.ceil((double) requestedCommands.size() / 3);

			} catch (NumberFormatException e) {
				switch (args[1].toLowerCase()) {
				case "game":
					commandType = CommandType.GAME_COMMANDS;
					break;
				case "arena":
					commandType = CommandType.ARENA_COMMANDS;
					break;
				case "admin":
					commandType = CommandType.ADMIN_COMMANDS;
					break;
				case "general":
					commandType = CommandType.GENERAL;
					break;
				default:
					commandType = CommandType.ALL;
				}

				requestedCommands = CubeRunnerCommand.getRequiredCommands(player, commandType);

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
					"&5" + local.keyWordCategory + ": &7" + commandType.toString() + ", &5" + local.keyWordPage + ": &7"
							+ String.valueOf(pageNumber) + "&8/&7"
							+ (int) (Math.ceil((double) requestedCommands.size() / 3))));

			if (pageNumber == 0) {
				local.sendMsg(player, local.helpNoPermission);
				return true;
			}

			for (int i = 3 * (pageNumber - 1); i < requestedCommands.size() && i < (3 * (pageNumber - 1)) + 3; i++) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&5" + requestedCommands.get(i).getUsage().replace("%command%", cmdValue)));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						" &8- &7" + local.getCommandsDescription().get(requestedCommands.get(i).getDescription())));
			}
			player.sendMessage("\n");
			return true;
		}

		Arena arena;
		CubeRunnerCommand command = CubeRunnerCommand.getCommand(args[0]);
		if (command != null) {

			if (command.getCommandName().equalsIgnoreCase("language")) {
				if (!Permissions.hasPermission(command, player, true))
					return true;

				if (args.length == 1) {
					local.sendMsg(player, local.languageList);
					for (Entry<String, Language> language : Language.getLanguages().entrySet())
						player.sendMessage("- " + language.getValue().languageName);

					return true;
				}

				Entry<String, Language> entrySet = Language.getLanguage(args[1]);
				if (entrySet == null) {
					local.sendMsg(player, local.languageNotFound.replace("%cmd%", cmdValue));
					return true;
				}

				playerData.setLanguage(player, entrySet.getKey());
				local = playerData.getLanguageOfPlayer(player);

				local.sendMsg(player, local.languageChangeSuccess);
				return true;
			}

			if (command.getCommandName().equalsIgnoreCase("info")) {
				if (!Permissions.hasPermission(command, player, true))
					return true;

				if (args.length == 1) {
					local.sendMsg(player, local.toolInfoMissingName);
					local.sendMsg(player, local.toolInfoTip.replace("%cmd%", cmdValue));
					return true;
				}

				arena = Arena.getArena(args[1]);
				if (arena == null) {
					local.sendMsg(player, local.arenaNotFound.replace("%arena%", args[1]));
					local.sendMsg(player, local.toolInfoTip.replace("%cmd%", cmdValue));
					return true;
				}

				local.sendMsg(player, local.toolInfoTip.replace("%cmd%", cmdValue));
				arena.displayInformation(player);
				return true;
			}

			if (command.getCommandName().equalsIgnoreCase("join") || command.getCommandName().equalsIgnoreCase("play")
					|| command.getCommandName().equalsIgnoreCase("list")) {
				if (args.length > 1)
					commandJoin(command, player, args.length, args[1], true);
				else
					commandJoin(command, player, args.length, "", true);
			}

			if (command.getCommandName().equalsIgnoreCase("quit"))
				commandQuit(command, player);

			if (command.getCommandName().equalsIgnoreCase("start"))
				commandStart(command, player);

			if (command.getCommandName().equalsIgnoreCase("stats"))
				commandStats(command, player);

			if (command.getCommandName().equalsIgnoreCase("reload")) {
				if (!Permissions.hasPermission(command, player, true))
					return true;

				if (mysql.hasConnection())
					mysql.close();

				config.loadConfiguration(plugin);
				if (config.mysql)
					mysql.updateInfo(plugin);

				plugin.initialiseEconomy();
				plugin.loadLanguages();

				if (!mysql.hasConnection()) {
					playerData.loadPlayerData();
					arenaData.loadArenaData();
				}

				achievements.setupAchievements();
				new SignPlace(plugin);
				new PlayerInteract(plugin);

				Arena.loadExistingArenas();

				local = playerData.getLanguageOfPlayer(player);
				local.sendMsg(player, local.reloadSuccess);
				return true;
			}

			if (command.getCommandName().equalsIgnoreCase("new")) {
				if (!Permissions.hasPermission(command, player, true))
					return true;

				if (args.length == 1) {
					local.sendMsg(player, local.missingArenaName);
					return true;
				}

				if (Arena.getArena(args[1]) != null) {
					local.sendMsg(player, local.arenaAlreadyExist.replace("%arena%", args[1]));
					return true;
				}

				new Arena(args[1], player);
				local.sendMsg(player, local.arenaCreated.replace("%arena%", args[1]));
				return true;
			}
			return true;
		}

		arena = Arena.getArena(args[0]);

		if (arena == null) {
			local.sendMsg(player, local.errorCommand.replace("%cmd%", cmdValue));
			return true;
		}

		if (args.length == 1) {
			local.sendMsg(player, local.missingEditArgument.replace("%cmd%", cmdValue));
			return true;
		}

		command = CubeRunnerCommand.getCommand(args[1]);
		if (command == null) {
			local.sendMsg(player, local.errorCommand.replace("%cmd%", cmdValue));
			return true;
		}

		if (command.getCommandName().equalsIgnoreCase("delete")) {
			if (!Permissions.hasPermission(command, player, true))
				return true;

			arena.delete(player);
			return true;
		}

		if (command.getCommandName().equalsIgnoreCase("setzone")) {
			if (!Permissions.hasPermission(command, player, true))
				return true;

			arena.setArena(player);
			return true;
		}

		if (command.getCommandName().equalsIgnoreCase("setlobby")) {
			if (!Permissions.hasPermission(command, player, true))
				return true;

			arena.setLobby(player);
			return true;
		}

		if (command.getCommandName().equalsIgnoreCase("setstartpoint")) {
			if (!Permissions.hasPermission(command, player, true))
				return true;

			arena.setStartPoint(player);
			return true;
		}

		if (command.getCommandName().equalsIgnoreCase("setcolor")) {
			if (!Permissions.hasPermission(command, player, true))
				return true;

			if (arena.getGameState() == GameState.ACTIVE || arena.getGameState() == GameState.ENDING) {
				local.sendMsg(player, local.guiColorEditWhileActive);
				return true;
			}

			colorGUI.openColorGUI(player, arena);
			return true;
		}

		if (command.getCommandName().equalsIgnoreCase("setminplayer")) {
			if (!Permissions.hasPermission(command, player, true))
				return true;

			if (args.length < 3) {
				local.sendMsg(player, local.arenaAmountPlayerMissingArgument);
				return true;
			}

			int amount = 0;
			try {
				amount = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				local.sendMsg(player,
						local.arenaAmountPlayerInvalidArgument.replace("%error%", local.arenaAmountPlayerNotANumber));
				return true;
			}

			arena.setMinPlayer(amount, player);
			return true;
		}

		if (command.getCommandName().equalsIgnoreCase("setmaxplayer")) {
			if (!Permissions.hasPermission(command, player, true))
				return true;

			if (args.length < 3) {
				local.sendMsg(player, local.arenaAmountPlayerMissingArgument);
			}

			int amount = 0;
			try {
				amount = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				local.sendMsg(player,
						local.arenaAmountPlayerInvalidArgument.replace("%error%", local.arenaAmountPlayerNotANumber));
				return true;
			}

			arena.setMaxPlayer(amount, player);
			return true;
		}

		local.sendMsg(player, local.errorCommand);
		return true;
	}

	protected void commandJoin(CubeRunnerCommand command, Player player, int argsLength, String arenaName,
			boolean teleport) {
		if (!Permissions.hasPermission(command, player, true))
			return;

		Language local = playerData.getLanguageOfPlayer(player);
		Arena arena = Arena.getArenaFromPlayer(player);
		if (arena != null) {
			local.sendMsg(player, local.playerAlreadyInGame);
			return;
		}

		if (argsLength == 1 || command.getCommandName().equalsIgnoreCase("list")) {
			joinGui.openJoinGui(player, 1);
			return;
		}

		arena = Arena.getArena(arenaName);
		if (arena == null) {
			local.sendMsg(player, local.arenaNotFound.replace("%arena%", arenaName));
			return;
		}

		arena.addPlayer(player, teleport);
	}

	protected void commandQuit(CubeRunnerCommand command, Player player) {
		if (!Permissions.hasPermission(command, player, true))
			return;

		Language local = playerData.getLanguageOfPlayer(player);
		Arena arena = Arena.getArenaFromPlayer(player);
		if (arena == null) {
			local.sendMsg(player, local.playerNotInGame);
			return;
		}

		arena.removePlayer(player, true);
	}

	protected void commandStats(CubeRunnerCommand command, Player player) {
		if (!Permissions.hasPermission(command, player, true))
			return;

		achievementsGUI.openAchievementInventory(player);
	}

	protected void commandStart(CubeRunnerCommand command, Player player) {
		if (!Permissions.hasPermission(command, player, true))
			return;

		Language local = playerData.getLanguageOfPlayer(player);
		Arena arena = Arena.getArenaFromPlayer(player);
		if (arena == null) {
			local.sendMsg(player, local.playerNotInGame);
			return;
		}

		if (arena.getGameState() != GameState.READY) {
			local.sendMsg(player, local.gameNotReady);
			return;
		}

		arena.initiateGame(player);
	}
}
