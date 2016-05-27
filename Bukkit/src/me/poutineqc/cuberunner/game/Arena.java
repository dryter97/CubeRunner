package me.poutineqc.cuberunner.game;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import com.google.gson.JsonObject;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

import me.poutineqc.cuberunner.ArenaData;
import me.poutineqc.cuberunner.CRPlayer.PlayerStatsException;
import me.poutineqc.cuberunner.CRStats;
import me.poutineqc.cuberunner.CubeRunner;
import me.poutineqc.cuberunner.Language;
import me.poutineqc.cuberunner.Language.Messages;
import me.poutineqc.cuberunner.MySQL;
import me.poutineqc.cuberunner.commands.signs.CRSign;
import me.poutineqc.cuberunner.utils.ItemStackManager;
import me.poutineqc.cuberunner.utils.Permissions;
import me.poutineqc.cuberunner.utils.Utils;

public class Arena {

	private static CubeRunner plugin;
	private static MySQL mysql;
	private static ArenaData arenaData;

	private static List<Arena> arenas = new ArrayList<Arena>();

	private String name;
	private World world;
	private Location minPoint;
	private Location maxPoint;
	private Location lobby;
	private Location startPoint;

	private int highestScore;
	private String highestPlayer;
	private int minAmountPlayer;
	private int maxAmountPlayer;
	private ColorManager colorManager;
	private Objective objective;
	private Scoreboard scoreboard;

	private List<User> users = new ArrayList<User>();
	private GameState gameState = GameState.UNREADY;
	private boolean multiplayerGame = false;
	private boolean filled;

	/*******************************************
	 * Constructors and else
	 *********************************************/

	public Arena(CubeRunner plugin) {
		Arena.plugin = plugin;
		Arena.mysql = plugin.getMySQL();
		Arena.arenaData = plugin.getArenaData();
	}

	public static void loadExistingArenas() {
		Arena.mysql = plugin.getMySQL();
		arenas = new ArrayList<Arena>();

		if (mysql.hasConnection()) {
			try {
				ResultSet arenas = mysql.query("SELECT * FROM " + CubeRunner.get().getConfiguration().tablePrefix + "ARENAS;");
				while (arenas.next()) {
					String name = arenas.getString("name");
					Long colorIndice = arenas.getLong("colorIndice");
					World world = Bukkit.getServer().getWorld(arenas.getString("world"));
					Location minPoint = new Location(world, arenas.getInt("minPointX"), arenas.getInt("minPointY"),
							arenas.getInt("minPointZ"));
					Location maxPoint = new Location(world, arenas.getInt("maxPointX"), arenas.getInt("maxPointY"),
							arenas.getInt("maxPointZ"));

					Location lobby = new Location(world, arenas.getDouble("lobbyX"), arenas.getDouble("lobbyY"),
							arenas.getDouble("lobbyZ"));
					lobby.setPitch(arenas.getFloat("lobbyPitch"));
					lobby.setYaw(arenas.getFloat("lobbyYaw"));

					Location startPoint = new Location(world, arenas.getDouble("startPointX"),
							arenas.getDouble("startPointY"), arenas.getDouble("startPointZ"));
					startPoint.setPitch(arenas.getFloat("startPointPitch"));
					startPoint.setYaw(arenas.getFloat("startPointYaw"));

					int minAmountPlayer = arenas.getInt("minAmountPlayer");
					int maxAmountPlayer = arenas.getInt("maxAmountPlayer");
					int highestScore = arenas.getInt("highestScore");
					String highestPlayer = arenas.getString("highestPlayer");
					new Arena(name, world, minPoint, maxPoint, lobby, startPoint, minAmountPlayer, maxAmountPlayer,
							highestScore, highestPlayer, colorIndice);
				}
			} catch (SQLException e) {
				plugin.getLogger().info("[MySQL] Error while loading arenas.");
			}

		} else {
			if (!arenaData.getData().contains("arenas"))
				return;

			for (String arenaName : arenaData.getData().getConfigurationSection("arenas").getKeys(false)) {
				World world = Bukkit.getServer()
						.getWorld(arenaData.getData().getString("arenas." + arenaName + ".world"));
				Long colorIndice = arenaData.getData().getLong("arenas." + arenaName + ".colorIndice", 1);
				Location minPoint = new Location(world,
						arenaData.getData().getInt("arenas." + arenaName + ".minPoint.X", 0),
						arenaData.getData().getInt("arenas." + arenaName + ".minPoint.Y", 0),
						arenaData.getData().getInt("arenas." + arenaName + ".minPoint.Z", 0));
				Location maxPoint = new Location(world,
						arenaData.getData().getInt("arenas." + arenaName + ".maxPoint.X", 0),
						arenaData.getData().getInt("arenas." + arenaName + ".maxPoint.Y", 0),
						arenaData.getData().getInt("arenas." + arenaName + ".maxPoint.Z", 0));

				Location lobby = new Location(world,
						arenaData.getData().getDouble("arenas." + arenaName + ".lobby.X", 0),
						arenaData.getData().getDouble("arenas." + arenaName + ".lobby.Y", 0),
						arenaData.getData().getDouble("arenas." + arenaName + ".lobby.Z", 0));
				lobby.setPitch((float) arenaData.getData().getDouble("arenas." + arenaName + ".lobby.Pitch", 0));
				lobby.setYaw((float) arenaData.getData().getDouble("arenas." + arenaName + ".lobby.Yaw", 0));

				Location startPoint = new Location(world,
						arenaData.getData().getDouble("arenas." + arenaName + ".startPoint.X", 0),
						arenaData.getData().getDouble("arenas." + arenaName + ".startPoint.Y", 0),
						arenaData.getData().getDouble("arenas." + arenaName + ".startPoint.Z", 0));
				startPoint.setPitch(
						(float) arenaData.getData().getDouble("arenas." + arenaName + ".startPoint.Pitch", 0));
				startPoint.setYaw((float) arenaData.getData().getDouble("arenas." + arenaName + ".startPoint.Yaw", 0));

				int minAmountPlayer = arenaData.getData().getInt("arenas." + arenaName + ".minAmountPlayer", 1);
				int maxAmountPlayer = arenaData.getData().getInt("arenas." + arenaName + ".maxAmountPlayer", 8);
				int highestScore = arenaData.getData().getInt("arenas." + arenaName + ".highestScore.score", 0);
				String highestPlayer = arenaData.getData().getString("arenas." + arenaName + ".highestScore.player",
						"null");
				new Arena(arenaName, world, minPoint, maxPoint, lobby, startPoint, minAmountPlayer, maxAmountPlayer,
						highestScore, highestPlayer, colorIndice);
			}
		}
	}

	private Arena(String name, World world, Location minPoint, Location maxPoint, Location lobby, Location startPoint,
			int minAmountPlayer, int maxAmountPlayer, int highestScore, String highestPlayer, Long colorIndice) {
		this.name = name;
		this.world = world;
		this.minPoint = minPoint;
		this.maxPoint = maxPoint;
		this.lobby = lobby;
		this.startPoint = startPoint;
		this.minAmountPlayer = minAmountPlayer;
		this.maxAmountPlayer = maxAmountPlayer;
		this.highestScore = highestScore;
		this.highestPlayer = highestPlayer;
		this.colorManager = new ColorManager(colorIndice, plugin, this);

		setNullIfDefault();
		arenas.add(this);

		resetScoreboard();
	}

	private void resetScoreboard() {
		Language local = Language.getDefault();
		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		objective = scoreboard.registerNewObjective(name, "dummy");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(ChatColor.LIGHT_PURPLE + name + ChatColor.WHITE + " : " + ChatColor.GREEN
				+ local.get(Messages.KEYWORD_SCOREBOARD_PLAYERS));
		objective.getScore(ChatColor.GREEN + "-------------------").setScore(1);
		objective.getScore(ChatColor.GREEN + local.get(Messages.KEYWORD_INFO_BEST_SCORE) + " = "
				+ ChatColor.LIGHT_PURPLE + String.valueOf(highestScore)).setScore(highestScore);
		objective.getScore(ChatColor.GREEN + local.get(Messages.KEYWORD_INFO_MINIMUM) + " "
				+ local.get(Messages.KEYWORD_SCOREBOARD_PLAYERS) + " = " + ChatColor.LIGHT_PURPLE
				+ String.valueOf(minAmountPlayer)).setScore(2);
		objective.getScore(ChatColor.GREEN + local.get(Messages.KEYWORD_INFO_MAXIMUM) + " "
				+ local.get(Messages.KEYWORD_SCOREBOARD_PLAYERS) + " = " + ChatColor.LIGHT_PURPLE
				+ String.valueOf(maxAmountPlayer)).setScore(3);

	}

	public Arena(String name, Player player) {
		this.name = name;
		this.world = player.getWorld();
		arenas.add(this);
		this.colorManager = new ColorManager((long) 1, plugin, this);
		this.highestPlayer = "null";
		this.highestScore = 0;
		this.minAmountPlayer = 1;
		this.maxAmountPlayer = 8;

		resetScoreboard();

		if (mysql.hasConnection()) {
			mysql.update("INSERT INTO " + CubeRunner.get().getConfiguration().tablePrefix + "ARENAS (name, world) " + "VALUES ('" + name + "','"
					+ world.getName() + "');");
			mysql.update("UPDATE " + CubeRunner.get().getConfiguration().tablePrefix + "ARENAS SET colorIndice=" + (long) 1 + " WHERE name='" + name
					+ "';");
		} else {
			arenaData.getData().set("arenas." + name + ".world", world.getName());
			arenaData.getData().set("arenas." + name + ".colorIndice", (long) 1);
			arenaData.saveArenaData();
		}
	}

	private void setNullIfDefault() {
		if ((0 == minPoint.getX()) && (0 == minPoint.getY()) && (0 == minPoint.getZ()))
			minPoint = null;

		if ((0 == maxPoint.getX()) && (0 == maxPoint.getY()) && (0 == maxPoint.getZ()))
			maxPoint = null;

		if ((0 == lobby.getX()) && (0 == lobby.getY()) && (0 == lobby.getZ()))
			lobby = null;

		if ((0 == startPoint.getX()) && (0 == startPoint.getY()) && (0 == startPoint.getZ()))
			startPoint = null;

		if (isReady())
			gameState = GameState.READY;
	}

	public void delete(Player player) {
		Language local = CubeRunner.get().getCRPlayer(player).getLanguage();
		local.sendMsg(player, local.get(Messages.EDIT_DELETE));
		arenas.remove(this);

		if (mysql.hasConnection()) {
			mysql.update("DELETE FROM " + CubeRunner.get().getConfiguration().tablePrefix + "ARENAS WHERE name='" + name + "';");
		} else {
			arenaData.getData().set("arenas." + name, null);
			arenaData.saveArenaData();
		}
	}

	/*******************************************
	 * Arena Setup
	 *********************************************/

	public void setArena(Player player) {
		Language local = CubeRunner.get().getCRPlayer(player).getLanguage();

		Selection s = getWorldEdit().getSelection(player);
		if (s == null) {
			local.sendMsg(player, local.get(Messages.EDIT_REGION_WORLDEDIT));
			return;
		}

		gameState = GameState.UNREADY;
		world = s.getWorld();
		minPoint = s.getMinimumPoint();
		maxPoint = s.getMaximumPoint();
		local.sendMsg(player, local.get(Messages.EDIT_REGION).replace("%arena%", name));

		if (mysql.hasConnection()) {
			mysql.update("UPDATE " + CubeRunner.get().getConfiguration().tablePrefix + "ARENAS SET world='" + world.getName() + "',minPointX="
					+ minPoint.getBlockX() + ",minPointY=" + minPoint.getBlockY() + ",minPointZ=" + minPoint.getBlockZ()
					+ ",maxPointX=" + maxPoint.getBlockX() + ",maxPointY=" + maxPoint.getBlockY() + ",maxPointZ="
					+ maxPoint.getBlockZ() + " WHERE name='" + name + "';");
		} else {
			arenaData.getData().set("arenas." + name + ".world", world.getName());
			arenaData.getData().set("arenas." + name + ".minPoint.X", minPoint.getBlockX());
			arenaData.getData().set("arenas." + name + ".minPoint.Y", minPoint.getBlockY());
			arenaData.getData().set("arenas." + name + ".minPoint.Z", minPoint.getBlockZ());
			arenaData.getData().set("arenas." + name + ".maxPoint.X", maxPoint.getBlockX());
			arenaData.getData().set("arenas." + name + ".maxPoint.Y", maxPoint.getBlockY());
			arenaData.getData().set("arenas." + name + ".maxPoint.Z", maxPoint.getBlockZ());
			arenaData.saveArenaData();
		}

		if (isReady())
			gameState = GameState.READY;
	}

	public void setLobby(Player player) {
		Language local = CubeRunner.get().getCRPlayer(player).getLanguage();

		gameState = GameState.UNREADY;
		world = player.getWorld();
		lobby = player.getLocation();
		lobby.add(new Vector(0, 0.5, 0));
		local.sendMsg(player, local.get(Messages.EDIT_LOBBY).replace("%arena%", name));

		if (mysql.hasConnection()) {
			mysql.update("UPDATE " + CubeRunner.get().getConfiguration().tablePrefix + "ARENAS SET world='" + world.getName() + "',lobbyX="
					+ lobby.getX() + ",lobbyY=" + lobby.getY() + ",lobbyZ=" + lobby.getZ() + ",lobbyPitch="
					+ lobby.getPitch() + ",lobbyYaw=" + lobby.getYaw() + " WHERE name='" + name + "';");
		} else {
			arenaData.getData().set("arenas." + name + ".world", world.getName());
			arenaData.getData().set("arenas." + name + ".lobby.X", lobby.getX());
			arenaData.getData().set("arenas." + name + ".lobby.Y", lobby.getY());
			arenaData.getData().set("arenas." + name + ".lobby.Z", lobby.getZ());
			arenaData.getData().set("arenas." + name + ".lobby.Pitch", lobby.getPitch());
			arenaData.getData().set("arenas." + name + ".lobby.Yaw", lobby.getYaw());
			arenaData.saveArenaData();
		}

		if (isReady())
			gameState = GameState.READY;
	}

	public void setStartPoint(Player player) {
		Language local = CubeRunner.get().getCRPlayer(player).getLanguage();

		gameState = GameState.UNREADY;
		world = player.getWorld();
		startPoint = player.getLocation();
		startPoint.add(new Vector(0, 0.5, 0));
		local.sendMsg(player, local.get(Messages.EDIT_STARTPOINT).replace("%arena%", name));

		if (mysql.hasConnection()) {
			mysql.update("UPDATE " + CubeRunner.get().getConfiguration().tablePrefix + "ARENAS SET world='" + world.getName() + "',startPointX="
					+ startPoint.getX() + ",startPointY=" + startPoint.getY() + ",startPointZ=" + startPoint.getZ()
					+ ",startPointPitch=" + startPoint.getPitch() + ",startPointYaw=" + startPoint.getYaw()
					+ " WHERE name='" + name + "';");
		} else {
			arenaData.getData().set("arenas." + name + ".world", world.getName());
			arenaData.getData().set("arenas." + name + ".startPoint.X", startPoint.getX());
			arenaData.getData().set("arenas." + name + ".startPoint.Y", startPoint.getY());
			arenaData.getData().set("arenas." + name + ".startPoint.Z", startPoint.getZ());
			arenaData.getData().set("arenas." + name + ".startPoint.Pitch", startPoint.getPitch());
			arenaData.getData().set("arenas." + name + ".startPoint.Yaw", startPoint.getYaw());
			arenaData.saveArenaData();
		}

		if (isReady())
			gameState = GameState.READY;
	}

	private WorldEditPlugin getWorldEdit() {
		Plugin p = Bukkit.getPluginManager().getPlugin("WorldEdit");
		if (p instanceof WorldEditPlugin)
			return (WorldEditPlugin) p;
		else
			return null;
	}

	public void setMinPlayer(int amount, Player player) {
		Language local = CubeRunner.get().getCRPlayer(player).getLanguage();

		if (amount < 1) {
			local.sendMsg(player,
					local.get(Messages.EDIT_PLAYERS_ERROR).replace("%error%", local.get(Messages.EDIT_PLAYERS_ERROR)));
			return;
		}

		if (amount > maxAmountPlayer) {
			local.sendMsg(player, local.get(Messages.EDIT_PLAYERS_ERROR).replace("%error%",
					local.get(Messages.EDIT_PLAYERS_MINIMUM_MAXIMUM)));
			return;
		}

		if (mysql.hasConnection()) {
			mysql.update("UPDATE " + CubeRunner.get().getConfiguration().tablePrefix + "ARENAS SET minAmountPlayer=" + amount + " WHERE name='"
					+ name + "';");
		} else {
			arenaData.getData().set("arenas." + name + ".minAmountPlayer", amount);
			arenaData.saveArenaData();
		}

		scoreboard.resetScores(ChatColor.GREEN + local.get(Messages.KEYWORD_INFO_MINIMUM) + " = "
				+ ChatColor.LIGHT_PURPLE + String.valueOf(minAmountPlayer));
		minAmountPlayer = amount;
		objective.getScore(ChatColor.GREEN + local.get(Messages.KEYWORD_INFO_MINIMUM) + " = " + ChatColor.LIGHT_PURPLE
				+ String.valueOf(minAmountPlayer)).setScore(3);

		local.sendMsg(player, local.get(Messages.COMMAND_SETMINPLAYER).replace("%arena%", name));
	}

	public void setMaxPlayer(int amount, Player player) {
		Language local = CubeRunner.get().getCRPlayer(player).getLanguage();

		if (amount > 12) {
			local.sendMsg(player, local.get(Messages.EDIT_PLAYERS_ERROR).replace("%error%",
					local.get(Messages.EDIT_PLAYERS_MAXIMUM_10)));
			return;
		}

		if (amount < minAmountPlayer) {
			local.sendMsg(player, local.get(Messages.EDIT_PLAYERS_ERROR).replace("%error%",
					local.get(Messages.EDIT_PLAYERS_MAXIMUM_MINIMUM)));
			return;
		}

		if (mysql.hasConnection()) {
			mysql.update("UPDATE " + CubeRunner.get().getConfiguration().tablePrefix + "ARENAS SET maxAmountPlayer=" + amount + " WHERE name='"
					+ name + "';");
		} else {
			arenaData.getData().set("arenas." + name + ".maxAmountPlayer", amount);
			arenaData.saveArenaData();
		}

		scoreboard.resetScores(ChatColor.GREEN + local.get(Messages.KEYWORD_INFO_MAXIMUM) + " = "
				+ ChatColor.LIGHT_PURPLE + String.valueOf(maxAmountPlayer));
		maxAmountPlayer = amount;
		objective.getScore(ChatColor.GREEN + local.get(Messages.KEYWORD_INFO_MAXIMUM) + " = " + ChatColor.LIGHT_PURPLE
				+ String.valueOf(maxAmountPlayer)).setScore(3);

		local.sendMsg(player, local.get(Messages.COMMAND_SETMAXPLAYER).replace("%arena%", name));
	}

	private boolean isReady() {
		return isOutsideArena(lobby) && isInsideArena(startPoint);
	}

	/*******************************************
	 * Tools
	 *********************************************/

	public void displayInformation(Player player) {
		Language local = CubeRunner.get().getCRPlayer(player).getLanguage();

		String stringGameState;
		switch (gameState) {
		case ACTIVE:
			stringGameState = local.get(Messages.KEYWORD_GAMESTATE_ACTIVE);
			break;
		case READY:
			stringGameState = local.get(Messages.KEYWORD_GAMESTATE_READY);
			break;
		case STARTUP:
			stringGameState = local.get(Messages.KEYWORD_GAMESTATE_STARTUP);
			break;
		case ENDING:
		case UNREADY:
		default:
			stringGameState = local.get(Messages.KEYWORD_GAMESTATE_UNSET);
		}

		player.sendMessage(
				ChatColor.translateAlternateColorCodes('&', "&8&m" + StringUtils.repeat(" ", 13) + "&r &5CubeRunner &d"
						+ local.get(Messages.KEYWORD_INFO) + " &5: &d" + name + " &8&m" + StringUtils.repeat(" ", 13)));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5" + local.get(Messages.KEYWORD_INFO_CURRENT)
				+ " " + local.get(Messages.KEYWORD_INFO_GAME_STATE) + ": &7" + stringGameState));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5" + local.get(Messages.KEYWORD_INFO_CURRENT)
				+ " " + local.get(Messages.KEYWORD_INFO_AMOUNT_OF_PLAYER) + ": &7" + users.size()));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5" + local.get(Messages.KEYWORD_INFO_MINIMUM)
				+ " " + local.get(Messages.KEYWORD_INFO_AMOUNT_OF_PLAYER) + ": &7" + minAmountPlayer));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5" + local.get(Messages.KEYWORD_INFO_MAXIMUM)
				+ " " + local.get(Messages.KEYWORD_INFO_AMOUNT_OF_PLAYER) + ": &7" + maxAmountPlayer));
		player.sendMessage(
				ChatColor.translateAlternateColorCodes('&', "&5" + local.get(Messages.KEYWORD_INFO_BEST_SCORE) + ": &7"
						+ highestScore + " &5" + local.get(Messages.KEYWORD_GENERAL_BY) + " &7" + highestPlayer));
		player.sendMessage("\n");

		if (!Permissions.hasPermission(Permissions.advancedInfo, player, false))
			return;

		player.sendMessage(
				ChatColor.translateAlternateColorCodes('&',
						"&8&m" + StringUtils.repeat(" ", 5) + "&r &5CubeRunner &d"
								+ local.get(Messages.KEYWORD_INFO_ADVANCED) + " &5: &d" + name + " &8&m"
								+ StringUtils.repeat(" ", 5)));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&',
				"&5" + local.get(Messages.KEYWORD_INFO_WORLD) + ": &7" + world.getName()));
		if (lobby == null) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&5" + local.get(Messages.KEYWORD_INFO_LOBBY) + ": &7null"));
		} else {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&5" + local.get(Messages.KEYWORD_INFO_LOBBY) + ": &7{"
							+ ((int) (lobby.getX() * 100)) / (double) 100 + ", "
							+ ((int) (lobby.getY() * 100)) / (double) 100 + ", "
							+ ((int) (lobby.getZ() * 100)) / (double) 100 + "}"));
		}
		if (startPoint == null) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&5" + local.get(Messages.KEYWORD_INFO_START_POINT) + ": &7null"));
		} else {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&5" + local.get(Messages.KEYWORD_INFO_START_POINT) + ": &7{"
							+ ((int) (startPoint.getX() * 100)) / (double) 100 + ", "
							+ ((int) (startPoint.getY() * 100)) / (double) 100 + ", "
							+ ((int) (startPoint.getZ() * 100)) / (double) 100 + "}"));
		}
		if (minPoint == null) {
			player.sendMessage(
					ChatColor.translateAlternateColorCodes('&', "&5" + local.get(Messages.KEYWORD_INFO_MINIMUM)
							+ local.get(Messages.KEYWORD_INFO_ZONE_COORDINATE) + ": &7null"));
		} else {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&5" + local.get(Messages.KEYWORD_INFO_MINIMUM) + local.get(Messages.KEYWORD_INFO_ZONE_COORDINATE)
							+ ": &7{" + minPoint.getBlockX() + ", " + minPoint.getBlockY() + ", " + minPoint.getBlockZ()
							+ "}"));
		}
		if (maxPoint == null) {
			player.sendMessage(
					ChatColor.translateAlternateColorCodes('&', "&5" + local.get(Messages.KEYWORD_INFO_MAXIMUM)
							+ local.get(Messages.KEYWORD_INFO_ZONE_COORDINATE) + ": &7null"));
		} else {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&5" + local.get(Messages.KEYWORD_INFO_MAXIMUM) + local.get(Messages.KEYWORD_INFO_ZONE_COORDINATE)
							+ ": &7{" + maxPoint.getBlockX() + ", " + maxPoint.getBlockY() + ", " + maxPoint.getBlockZ()
							+ "}"));
		}
		player.sendMessage("\n");
	}

	/*******************************************
	 * Big Game commands
	 *********************************************/

	public void addPlayer(Player player, boolean teleport) {
		Language local = CubeRunner.get().getCRPlayer(player).getLanguage();

		if (getArenaFromPlayer(player) != null) {
			local.sendMsg(player, local.get(Messages.ERROR_ALREADY_IN_GAME));
			return;
		}

		if (gameState == GameState.UNREADY || gameState == GameState.ENDING) {
			local.sendMsg(player, local.get(Messages.JOIN_UNREADY));
			return;
		}

		player.setScoreboard(scoreboard);

		User user = null;
		if (teleport || gameState != GameState.ACTIVE) {
			user = new User(CubeRunner.get().getConfiguration(), CubeRunner.get().getCRPlayer(player), this, gameState == GameState.ACTIVE,
					teleport);
			users.add(user);
			CRSign.updateSigns(this);
		}

		if (gameState == GameState.ACTIVE) {
			local.sendMsg(player, local.get(Messages.JOIN_ACTIVE));

			if (user != null)
				local.sendMsg(player, local.get(Messages.JOIN_SPECTATOR));

			return;
		} else {
			objective.getScore(user.getName()).setScore(0);
		}

		if (gameState == GameState.STARTUP)
			player.teleport(startPoint);

		local.sendMsg(player, local.get(Messages.JOIN_PLAYER).replace("%arena%", name));
		for (User u : users) {
			if (u == user)
				continue;

			Language localAlt = u.getCRPlayer().getLanguage();
			localAlt.sendMsg(u, localAlt.get(Messages.JOIN_OTHERS).replace("%player%", player.getDisplayName()));
		}

		if (gameState == GameState.READY && CubeRunner.get().getConfiguration().autostart && getAmountOfPlayerInGame() >= minAmountPlayer)
			initiateGame(null);
	}

	public void removePlayer(Player player, LeavingReason reason) {
		Language local = CubeRunner.get().getCRPlayer(player).getLanguage();

		User user = getUser(player);
		if (user == null)
			return;

		if (!user.isEliminated()) {
			local.sendMsg(user, local.get(Messages.QUIT_PLAYER).replace("%arena%", name));
			for (User u : users) {
				if (u == user)
					continue;

				Language l = u.getCRPlayer().getLanguage();
				l.sendMsg(u, l.get(Messages.QUIT_OTHERS).replace("%player%", user.getDisplayName()));
			}

			if (gameState == GameState.ACTIVE)
				eliminateUser(user, reason);
		}

		if (gameState == GameState.STARTUP) {
			user.getPlayer().teleport(lobby);
		}

		if (gameState == GameState.STARTUP || gameState == GameState.READY)
			objective.getScoreboard().resetScores(user.getName());

		user.ireturnStats();
		users.remove(user);
		CRSign.updateSigns(this);

		if (gameState == GameState.STARTUP)
			if (getAmountOfPlayerInGame() < minAmountPlayer) {
				gameState = GameState.READY;
				for (User u : users)
					u.getPlayer().teleport(lobby);
			}
	}

	public void initiateGame(Player player) {
		Language local = player == null ? null : CubeRunner.get().getCRPlayer(player).getLanguage();

		if (users.size() < minAmountPlayer) {
			if (local != null)
				local.sendMsg(player,
						local.get(Messages.START_MINIMUM).replace("%amount%", String.valueOf(minAmountPlayer)));

			return;
		}

		if (users.size() > maxAmountPlayer) {
			if (local != null)
				local.sendMsg(player,
						local.get(Messages.START_MAXIMUM).replace("%amount%", String.valueOf(maxAmountPlayer)));

			return;
		}

		if (CubeRunner.get().getConfiguration().broadcastStartup)
			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				Language localTemp = CubeRunner.get().getLang(p);
				localTemp.sendMsg(p, localTemp.get(Messages.START_BROADCAST).replace("%arena%", name));
			}

		resetArena();
		gameState = GameState.STARTUP;

		for (User user : users) {
			user.getPlayer().teleport(startPoint);
			user.imaxStats();
		}

		countdown(this, CubeRunner.get().getConfiguration().countdownTime * 20);
	}

	private void countdown(final Arena arena, final int cooldownTimer) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {

				int level = (int) Math.floor(cooldownTimer / 20);

				for (User user : arena.users) {
					if (cooldownTimer != 0)
						user.getPlayer().setLevel(level + 1);
					else
						user.getPlayer().setLevel(0);

					user.getPlayer().setExp((float) ((cooldownTimer % 20) / 20.0));
				}

				if (gameState == GameState.STARTUP) {
					switch (cooldownTimer / 20) {
					case 30:
					case 10:
					case 5:
					case 4:
					case 3:
					case 2:
					case 1:
						for (User user : arena.users) {
							if (cooldownTimer % 20.0 == 0) {

								Sound sound = CubeRunner.oneNine ? Sound.valueOf("UI_BUTTON_CLICK")
										: Sound.valueOf("CLICK");

								user.getPlayer().playSound(user.getPlayer().getLocation(), sound, 1, 1);

								Language local = CubeRunner.get().getCRPlayer(user.getPlayer()).getLanguage();

								JsonObject title = new JsonObject();
								title.addProperty("text", String.valueOf((int) cooldownTimer / 20));
								title.addProperty("bold", true);
								title.addProperty("color", "gold");

								JsonObject subtitle = new JsonObject();
								subtitle.addProperty("text", local.get(Messages.KEYWORD_GENERAL_SECONDS));
								subtitle.addProperty("italic", true);
								subtitle.addProperty("color", "gray");

								Utils.sendTitle(user.getPlayer(), title.toString(), subtitle.toString(), 5, 10, 5);
							}
						}
						break;
					case 0:
						if (cooldownTimer % 20.0 == 0) {

							arena.startGame();
							return;
						}
					}

					arena.countdown(arena, cooldownTimer - 1);

				} else {
					for (User user : arena.users) {
						Player p = user.getPlayer();
						p.setLevel(0);
						p.setExp(0);
						Language local = user.getCRPlayer().getLanguage();
						local.sendMsg(user.getPlayer(), local.get(Messages.START_STOP));
					}
				}
			}
		}, 1L);

	}

	protected void startGame() {
		Language local = Language.getDefault();
		gameState = GameState.ACTIVE;
		CRSign.updateSigns(this);
		multiplayerGame = getAmountOfPlayerInGame() > 1;

		objective.setDisplayName(ChatColor.AQUA + name + ChatColor.WHITE + " : " + ChatColor.GREEN
				+ local.get(Messages.KEYWORD_SCOREBOARD_SCORE));
		objective.getScoreboard().resetScores(ChatColor.GREEN + "-------------------");
		objective.getScoreboard()
				.resetScores(ChatColor.GREEN + local.get(Messages.KEYWORD_INFO_MINIMUM) + " "
						+ local.get(Messages.KEYWORD_SCOREBOARD_PLAYERS) + " = " + ChatColor.LIGHT_PURPLE
						+ String.valueOf(minAmountPlayer));
		objective.getScoreboard()
				.resetScores(ChatColor.GREEN + local.get(Messages.KEYWORD_INFO_MAXIMUM) + " "
						+ local.get(Messages.KEYWORD_SCOREBOARD_PLAYERS) + " = " + ChatColor.LIGHT_PURPLE
						+ String.valueOf(maxAmountPlayer));

		blockShower(1, this);

		for (User user : users) {
			Player player = user.getPlayer();
			player.setGameMode(GameMode.ADVENTURE);
			player.setFlying(false);

			user.setStartTime();
		}

	}

	@SuppressWarnings("deprecation")
	private void resetArena() {
		for (int x = minPoint.getBlockX(); x <= maxPoint.getBlockX(); x++)
			for (int y = maxPoint.getBlockY(); y >= minPoint.getBlockY(); y--)
				nextBlock: for (int z = minPoint.getBlockZ(); z <= maxPoint.getBlockZ(); z++) {
					Location location = new Location(world, x, y, z);
					Block block = location.getBlock();
					if (block.getType() != Material.STAINED_CLAY && block.getType() != Material.WOOL)
						continue;

					for (ItemStackManager item : colorManager.getOnlyChoosenBlocks())
						if (item.getMaterial() == block.getType())
							if (item.getItem().getDurability() == block.getData()) {
								block.setType(Material.AIR);
								continue nextBlock;
							}

				}
	}

	@SuppressWarnings("deprecation")
	public void resetArena(ItemStack item) {
		for (int x = minPoint.getBlockX(); x <= maxPoint.getBlockX(); x++)
			for (int y = maxPoint.getBlockY(); y >= minPoint.getBlockY(); y--)
				nextBlock: for (int z = minPoint.getBlockZ(); z <= maxPoint.getBlockZ(); z++) {
					Location location = new Location(world, x, y, z);
					Block block = location.getBlock();
					if (block.getType() != Material.STAINED_CLAY && block.getType() != Material.WOOL)
						continue;

					if (item.getType() == block.getType())
						if (item.getDurability() == block.getData()) {
							block.setType(Material.AIR);
							continue nextBlock;
						}

				}
	}

	private static void blockShower(final long number, final Arena arena) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {

				if (arena.getAmountOfPlayerInGame() == 0)
					return;

				if (!arena.filled)
					if (arena.isArenaFull()) {
						arena.filled = true;
						for (User u : arena.users)
							if (!u.isEliminated())
								try {
									u.getCRPlayer().doneChallenge(CRStats.FILL_THE_ARENA);
								} catch (PlayerStatsException e) {
									e.printStackTrace();
								}
					}

				if (number == 6000)
					for (User u : arena.users)
						if (!u.isEliminated())
							try {
								u.getCRPlayer().doneChallenge(CRStats.SURVIVE_5_MINUTES);
							} catch (PlayerStatsException e) {
								e.printStackTrace();
							}

				for (User user : arena.users) {
					if (user.isEliminated())
						continue;
					
					if (user.getLastTreeSecondsDistance() < 1 && user.getScore() >= 2)
						arena.eliminateUser(user, LeavingReason.HIDING);

					Player player = user.getPlayer();

					Location l;
					if (arena.maxPoint.getBlockY() > player.getLocation().getBlockY() + 10)
						l = new Location(player.getWorld(), player.getLocation().getX(),
								player.getLocation().getY() + 10, player.getLocation().getZ());
					else
						l = new Location(player.getWorld(), player.getLocation().getX(), arena.maxPoint.getY(),
								player.getLocation().getZ());

					NumberFormat formater = new DecimalFormat("#.#####");
					double xOffset = Double
							.parseDouble(formater.format(l.getX() > 0 ? l.getX() % 1 : 1 + (l.getX() % 1)));
					double zOffset = Double
							.parseDouble(formater.format(l.getZ() > 0 ? l.getZ() % 1 : 1 + (l.getZ() % 1)));

					if (xOffset <= 0.7 && xOffset >= 0.3)
						l.setX(Math.floor(l.getX()) + 0.5D);

					if (zOffset <= 0.7 && zOffset >= 0.3)
						l.setZ(Math.floor(l.getZ()) + 0.5D);

					player.setSaturation(20);
					if (number % 20 == 0) {
						player.setLevel((int) number / 20);
						player.setSaturation(20);
						user.addToScore();
						arena.objective.getScore(user.getName()).setScore(user.getScore());

						try {
							user.getCRPlayer().increment(CRStats.TOTAL_SCORE, false);
						} catch (PlayerStatsException e) {
							e.printStackTrace();
						}
					}

					ItemStackManager itemStack = arena.colorManager.getRandomAvailableBlock();

					try {

						Object craftWorld = Class
								.forName("org.bukkit.craftbukkit." + CubeRunner.NMS_VERSION + ".CraftWorld")
								.cast(player.getWorld());
						Object world = craftWorld.getClass().getMethod("getHandle").invoke(craftWorld);

						@SuppressWarnings("deprecation")
						Object entityBlock = Utils.getNMSClass("Block").getMethod("getById", int.class).invoke(null,
								itemStack.getMaterial().getId());
						Object iBlockData = entityBlock.getClass().getMethod("fromLegacyData", int.class)
								.invoke(entityBlock, (int) itemStack.getDurability());
						Object entityFallingBlock = Utils.getNMSClass("EntityFallingBlock")
								.getConstructor(Utils.getNMSClass("World"), double.class, double.class, double.class,
										Utils.getNMSClass("IBlockData"))
								.newInstance(world, l.getX(), l.getY(), l.getZ(), iBlockData);

						entityFallingBlock.getClass().getField("ticksLived").set(entityFallingBlock, (int) 1);
						entityFallingBlock.getClass().getSuperclass().getMethod("setCustomName", String.class)
								.invoke(entityFallingBlock, user.getUUID().toString());
						entityFallingBlock.getClass().getSuperclass().getMethod("setCustomNameVisible", boolean.class)
								.invoke(entityFallingBlock, false);

						entityFallingBlock.getClass().getMethod("a", boolean.class).invoke(entityFallingBlock, true);
						entityFallingBlock.getClass().getField("dropItem").set(entityFallingBlock, false);

						world.getClass().getMethod("addEntity", Utils.getNMSClass("Entity"), SpawnReason.class)
								.invoke(world, entityFallingBlock, SpawnReason.CUSTOM);

						if (number % 2 == 0) {
							entityFallingBlock.getClass().getSuperclass().getField("motX").set(entityFallingBlock,
									(1 - (Math.random() * 2)) / 10.0);
							entityFallingBlock.getClass().getSuperclass().getField("motY").set(entityFallingBlock, 0);
							entityFallingBlock.getClass().getSuperclass().getField("motZ").set(entityFallingBlock,
									(1 - (Math.random() * 2)) / 10.0);
							entityFallingBlock.getClass().getSuperclass().getField("velocityChanged")
									.set(entityFallingBlock, true);
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				Arena.blockShower(number + 1, arena);

			}
		}, 1L);
	}

	private boolean isArenaFull() {
		for (int i = minPoint.getBlockX(); i < maxPoint.getBlockX(); i++)
			for (int j = minPoint.getBlockZ(); j < maxPoint.getBlockZ(); j++)
				if (new Location(world, i, minPoint.getBlockY(), j).getBlock().getType() == Material.AIR)
					return false;

		return true;
	}

	public void eliminateUser(User user, LeavingReason reason) {

		user.getPlayer().teleport(lobby);
		user.setEliminated(true);

		if (reason == LeavingReason.CRUSHED) {
			Language local = user.getCRPlayer().getLanguage();
			local.sendMsg(user,
					local.get(Messages.END_CRUSH_PLAYER).replace("%score%", String.valueOf(user.getScore())));
			for (User u : users) {
				if (u == user)
					continue;

				Language l = u.getCRPlayer().getLanguage();
				l.sendMsg(u, l.get(Messages.END_CRUSH_OTHERS).replace("%player%", user.getDisplayName())
						.replace("%score%", String.valueOf(user.getScore())));
			}
		}

		if (reason == LeavingReason.HIDING) {
			Language local = user.getCRPlayer().getLanguage();
			local.sendMsg(user, local.get(Messages.END_HIDE_PLAYER));
			for (User u : users) {
				if (u == user)
					continue;

				Language l = u.getCRPlayer().getLanguage();
				l.sendMsg(u, l.get(Messages.END_HIDE_OTHERS).replace("%player%", user.getDisplayName()));
			}
		}

		try {
			user.getCRPlayer().increment(CRStats.GAMES_PLAYED, true);
			user.getCRPlayer().addInt(CRStats.TIME_PLAYED, user.getGameLenght());
			user.getCRPlayer().addDouble(CRStats.TOTAL_DISTANCE, user.getDistanceRan());
			user.getCRPlayer().saveIncrementor(CRStats.TOTAL_SCORE);
		} catch (PlayerStatsException e) {
			e.printStackTrace();
		}
		user.getCRPlayer().updateAverageScorePerGame();

		if (CubeRunner.get().isEconomyEnabled()) {
			Player player = user.getPlayer();
			double amount = (CubeRunner.get().getConfiguration().pricePerScore * user.getScore());
			CubeRunner.get().getEconomy().depositPlayer(player, amount);
			try {
				user.getCRPlayer().addDouble(CRStats.MONEY, amount);
			} catch (PlayerStatsException e) {
				e.printStackTrace();
			}

			Language l = user.getCRPlayer().getLanguage();
			l.sendMsg(player,
					l.get(Messages.END_REWARD).replace("%amount2%", String.valueOf(user.getScore()))
							.replace("%amount%", String.valueOf((int) (amount * 100) / 100.0))
							.replace("%currency%", CubeRunner.get().getEconomy().currencyNamePlural()));
		}

		if (user.getScore() == 42)
			try {
				user.getCRPlayer().doneChallenge(CRStats.THE_ANSWER_TO_LIFE);
			} catch (PlayerStatsException e) {
				e.printStackTrace();
			}

		if (getAmountOfPlayerInGame() == 0)
			endingSequence();

		commandReward(user, getAmountOfPlayerInGame() == 0);

		try {
			CubeRunner.get().getPlayerData().updateAll(user.getCRPlayer());
		} catch (PlayerStatsException e) {
			e.printStackTrace();
		}
	}

	private void commandReward(User user, boolean lastPlayer) {
		for (String command : CubeRunner.get().getConfiguration().playerCommands)
			if (command.contains("%all%"))
				for (User u : users)
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
							command.replace("%all%", u.getName()).replace("%player%", user.getName())
									.replace("%arena%", name)
									.replace("%prefix%", u.getCRPlayer().getLanguage().get(Messages.PREFIX_SHORT)));
			else
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
						command.replace("%player%", user.getName()).replace("%arena%", name).replace("%prefix%",
								user.getCRPlayer().getLanguage().get(Messages.PREFIX_SHORT)));

		if (lastPlayer) {
			if (multiplayerGame)
				for (String command : CubeRunner.get().getConfiguration().winnerCommands)
					if (command.contains("%all%"))
						for (User u : users)
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
									command.replace("%all%", u.getName()).replace("%winner%", user.getName())
											.replace("%arena%", name).replace("%prefix%",
													u.getCRPlayer().getLanguage().get(Messages.PREFIX_SHORT)));
					else
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
								command.replace("%winner%", user.getName()).replace("%arena%", name).replace("%prefix%",
										user.getCRPlayer().getLanguage().get(Messages.PREFIX_SHORT)));

			for (String command : CubeRunner.get().getConfiguration().endingCommands)
				if (command.contains("%all%"))
					for (User u : users)
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
								command.replace("%all%", u.getName()).replace("%arena%", name).replace("%prefix%",
										u.getCRPlayer().getLanguage().get(Messages.PREFIX_SHORT)));
				else
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%arena%", name)
							.replace("%prefix%", user.getCRPlayer().getLanguage().get(Messages.PREFIX_SHORT)));

			if (multiplayerGame) {
				if (CubeRunner.get().getConfiguration().broadcastEndingMulti)
					for (Player player : Bukkit.getOnlinePlayers()) {
						Language local = CubeRunner.get().getLang(player);
						local.sendMsg(player,
								local.get(Messages.END_BROADCAST_MULTIPLAYER).replace("%player%", user.getDisplayName())
										.replace("%arena%", name).replace("%score%", String.valueOf(user.getScore())));
					}
			} else if (CubeRunner.get().getConfiguration().broadcastEndingSingle)
				for (Player player : Bukkit.getOnlinePlayers()) {
					Language local = CubeRunner.get().getLang(player);
					local.sendMsg(player,
							local.get(Messages.END_BROADCAST_SINGLEPLAYER).replace("%player%", user.getDisplayName())
									.replace("%arena%", name).replace("%score%", String.valueOf(user.getScore())));
				}
		}
	}

	private void endingSequence() {
		gameState = GameState.ENDING;
		CRSign.updateSigns(this);

		User user = getHighestScore();

		if (multiplayerGame) {
			try {
				user.getCRPlayer().increment(CRStats.MULTIPLAYER_WON, true);
			} catch (PlayerStatsException e) {
				e.printStackTrace();
			}
		}

		if (user.getScore() > highestScore) {
			highestScore = user.getScore();
			highestPlayer = user.getPlayer().getName();

			for (Player player : Bukkit.getServer().getOnlinePlayers()) {
				Language local = CubeRunner.get().getCRPlayer(player).getLanguage();
				local.sendMsg(player, local.get(Messages.END_BEST).replace("%player%", user.getDisplayName())
						.replace("%score%", String.valueOf(user.getScore())).replace("%arena%", name));
			}

			if (mysql.hasConnection()) {
				mysql.update("UPDATE " + CubeRunner.get().getConfiguration().tablePrefix + "ARENAS SET highestScore='" + user.getScore()
						+ "', highestPlayer='" + user.getPlayer().getName() + "' WHERE name='" + name + "';");
			} else {
				arenaData.getData().set("arenas." + name + ".highestScore.score", highestScore);
				arenaData.getData().set("arenas." + name + ".highestScore.player", user.getPlayer().getName());
				arenaData.saveArenaData();
			}
		}

		resetScoreboard();

		if (CubeRunner.get().getConfiguration().teleportAfterEnding) {
			for (User u : users) {
				Language local = u.getCRPlayer().getLanguage();
				local.sendMsg(u, local.get(Messages.END_TELEPORT));
			}

			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					kickUsers(false);
				}
			}, 100L);
		} else
			kickUsers(true);
	}

	public User getHighestScore() {
		User user = new User(0);
		for (User u : users)
			if (user.getScore() <= u.getScore())
				user = u;
		return user;
	}

	private void kickUsers(boolean wait) {
		for (User user : users) {
			user.allowTeleport();
			user.ireturnStats();
		}

		users.clear();

		final Arena a = this;
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				gameState = GameState.READY;
				CRSign.updateSigns(a);
			}
		}, wait ? 20L : 0L);
	}

	public boolean isInsideArena(Location location) {
		if (minPoint == null)
			return false;
		if (maxPoint == null)
			return false;
		if (location == null)
			return false;

		if (location.getBlockX() < minPoint.getBlockX() || location.getBlockX() > maxPoint.getBlockX())
			return false;

		if (location.getBlockY() < minPoint.getBlockY() || location.getBlockY() > maxPoint.getBlockY())
			return false;

		if (location.getBlockZ() < minPoint.getBlockZ() || location.getBlockZ() > maxPoint.getBlockZ())
			return false;

		return true;
	}

	private boolean isOutsideArena(Location location) {
		if (minPoint == null)
			return false;
		if (maxPoint == null)
			return false;
		if (location == null)
			return false;

		if (location.getBlockX() < minPoint.getBlockX() || location.getBlockX() > maxPoint.getBlockX())
			return true;

		if (location.getBlockY() < minPoint.getBlockY() || location.getBlockY() > maxPoint.getBlockY())
			return true;

		if (location.getBlockZ() < minPoint.getBlockZ() || location.getBlockZ() > maxPoint.getBlockZ())
			return true;

		return false;
	}

	/*******************************************
	 * Getters and setters
	 *********************************************/

	public static List<Arena> getArenas() {
		return arenas;
	}

	public String getName() {
		return name;
	}

	public GameState getGameState() {
		return gameState;
	}

	public static Arena getArena(String name) {
		for (Arena arena : arenas)
			if (arena.name.toLowerCase().equalsIgnoreCase(name.toLowerCase()))
				return arena;
		return null;
	}

	public static Arena getArenaFromPlayer(Player player) {
		for (Arena arena : arenas)
			for (User user : arena.users)
				if (user.getPlayer() == player)
					return arena;
		return null;
	}

	public User getUser(Player player) {
		for (User user : users)
			if (user.getPlayer() == player)
				return user;
		return null;
	}

	public int getAmountOfPlayerInGame() {
		int i = 0;
		for (User user : users)
			if (!user.isEliminated())
				i++;

		return i;
	}

	public Location getLobby() {
		return lobby;
	}

	public Location getStartPoint() {
		return startPoint;
	}

	public String getHighestPlayer() {
		return highestPlayer;
	}

	public int getHighestPlayerScore() {
		return highestScore;
	}

	public Location getMinPoint() {
		return minPoint;
	}

	public ColorManager getColorManager() {
		return colorManager;
	}

	public enum LeavingReason {
		DISCONNECT, HIDING, CRUSHED, COMMAND
	}

	public World getWorld() {
		return world;
	}

	public int getMaxPlayer() {
		return maxAmountPlayer;
	}

}
