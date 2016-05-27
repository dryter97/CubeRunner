package me.poutineqc.cuberunner.games;

import java.sql.ResultSet;
import java.sql.SQLException;
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
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

import me.poutineqc.cuberunner.ArenaData;
import me.poutineqc.cuberunner.Configuration;
import me.poutineqc.cuberunner.CubeRunner;
import me.poutineqc.cuberunner.Language;
import me.poutineqc.cuberunner.MySQL;
import me.poutineqc.cuberunner.Permissions;
import me.poutineqc.cuberunner.PlayerData;
import me.poutineqc.cuberunner.achievements.AchievementType;
import me.poutineqc.cuberunner.achievements.Achievements;
import me.poutineqc.cuberunner.achievements.TopManager;
import me.poutineqc.cuberunner.tools.ColorManager;
import me.poutineqc.cuberunner.tools.ItemStackManager;
import net.milkbowl.vault.economy.Economy;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle.EnumTitleAction;
import net.minecraft.server.v1_8_R3.PlayerConnection;

public class Arena {

	private static CubeRunner plugin;
	private static Configuration config;
	private static MySQL mysql;
	private static PlayerData playerData;
	private static ArenaData arenaData;
	private static Achievements achievements;
	private static TopManager topManager;

	private static List<Arena> arenas = new ArrayList<Arena>();
	private static Economy economy;

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

	private List<User> users = new ArrayList<User>();
	private GameState gameState = GameState.UNREADY;
	private int originalAmountPlayer;
	private boolean filled;

	/*******************************************
	 * Constructors and else
	 *********************************************/

	public Arena(CubeRunner plugin) {
		Arena.plugin = plugin;
		Arena.mysql = plugin.getMySQL();
		Arena.config = plugin.getConfiguration();
		Arena.arenaData = plugin.getArenaData();
		Arena.playerData = plugin.getPlayerData();
		Arena.achievements = plugin.getAchievements();
		Arena.topManager = plugin.getTopManager();
		Arena.economy = CubeRunner.getEconomy();
	}

	public static void loadExistingArenas() {
		Arena.mysql = plugin.getMySQL();
		arenas = new ArrayList<Arena>();

		if (mysql.hasConnection()) {
			try {
				ResultSet arenas = mysql.query("SELECT * FROM " + config.tablePrefix + "ARENAS;");
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

	public Arena(String name, World world, Location minPoint, Location maxPoint, Location lobby, Location startPoint,
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
	}

	public Arena(String name, Player player) {
		this.name = name;
		world = player.getWorld();
		arenas.add(this);
		colorManager = new ColorManager((long) 1, plugin, this);
		this.highestPlayer = "null";
		this.highestScore = 0;
		this.minAmountPlayer = 1;
		this.maxAmountPlayer = 8;

		if (mysql.hasConnection()) {
			mysql.update("INSERT INTO " + config.tablePrefix + "ARENAS (name, world) " + "VALUES ('" + name + "','"
					+ world.getName() + "');");
			mysql.update("UPDATE " + config.tablePrefix + "ARENAS SET colorIndice=" + (long) 1 + " WHERE name='" + name
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
		Language local = playerData.getLanguageOfPlayer(player);
		local.sendMsg(player, local.arenaDeleted);
		arenas.remove(this);

		if (mysql.hasConnection()) {
			mysql.update("DELETE FROM " + config.tablePrefix + "ARENAS WHERE name='" + name + "';");
		} else {
			arenaData.getData().set("arenas." + name, null);
			arenaData.saveArenaData();
		}
	}

	/*******************************************
	 * Arena Setup
	 *********************************************/

	public void setArena(Player player) {
		Language local = playerData.getLanguageOfPlayer(player);

		Selection s = getWorldEdit().getSelection(player);
		if (s == null) {
			local.sendMsg(player, local.missingWorldEditRegion);
			return;
		}

		gameState = GameState.UNREADY;
		world = s.getWorld();
		minPoint = s.getMinimumPoint();
		maxPoint = s.getMaximumPoint();
		local.sendMsg(player, local.arenaSetArena.replace("%arena%", name));

		if (mysql.hasConnection()) {
			mysql.update("UPDATE " + config.tablePrefix + "ARENAS SET world=" + world.getName() + ",minPointX="
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
		Language local = playerData.getLanguageOfPlayer(player);

		gameState = GameState.UNREADY;
		world = player.getWorld();
		lobby = player.getLocation();
		lobby.add(new Vector(0, 0.5, 0));
		local.sendMsg(player, local.arenaSetLobby.replace("%arena%", name));

		if (mysql.hasConnection()) {
			mysql.update("UPDATE " + config.tablePrefix + "ARENAS SET world=" + world.getName() + ",lobbyX="
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
		Language local = playerData.getLanguageOfPlayer(player);

		gameState = GameState.UNREADY;
		world = player.getWorld();
		startPoint = player.getLocation();
		startPoint.add(new Vector(0, 0.5, 0));
		local.sendMsg(player, local.arenaSetStartPoint.replace("%arena%", name));

		if (mysql.hasConnection()) {
			mysql.update("UPDATE " + config.tablePrefix + "ARENAS SET world=" + world.getName() + ",startPointX="
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
		Language local = playerData.getLanguageOfPlayer(player);

		if (amount < 1) {
			local.sendMsg(player,
					local.arenaAmountPlayerInvalidArgument.replace("%error%", local.arenaAmountPlayerMinEqualZero));
			return;
		}

		if (amount > maxAmountPlayer) {
			local.sendMsg(player,
					local.arenaAmountPlayerInvalidArgument.replace("%error%", local.arenaAmountPlayerMinBiggerMax));
			return;
		}

		if (mysql.hasConnection()) {
			mysql.update("UPDATE " + config.tablePrefix + "ARENAS SET minAmountPlayer=" + amount + " WHERE name='"
					+ name + "';");
		} else {
			arenaData.getData().set("arenas." + name + ".minAmountPlayer", amount);
			arenaData.saveArenaData();
		}

		minAmountPlayer = amount;
		local.sendMsg(player, local.arenaAmountPlayerSuccess.replace("%arena%", name));
	}

	public void setMaxPlayer(int amount, Player player) {
		Language local = playerData.getLanguageOfPlayer(player);

		if (amount < minAmountPlayer) {
			local.sendMsg(player,
					local.arenaAmountPlayerInvalidArgument.replace("%error%", local.arenaAmountPlayerMaxBiggerMin));
			return;
		}

		if (mysql.hasConnection()) {
			mysql.update("UPDATE " + config.tablePrefix + "ARENAS SET maxAmountPlayer=" + amount + " WHERE name='"
					+ name + "';");
		} else {
			arenaData.getData().set("arenas." + name + ".maxAmountPlayer", amount);
			arenaData.saveArenaData();
		}

		maxAmountPlayer = amount;
		local.sendMsg(player, local.arenaAmountPlayerSuccess.replace("%arena%", name));
	}

	private boolean isReady() {
		return isOutsideArena(lobby) && isInsideArena(startPoint);
	}

	/*******************************************
	 * Tools
	 *********************************************/

	public void displayInformation(Player player) {
		Language local = playerData.getLanguageOfPlayer(player);

		String stringGameState;
		switch (gameState) {
		case ACTIVE:
			stringGameState = local.keyWordActive;
			break;
		case READY:
			stringGameState = local.keyWordReady;
			break;
		case STARTUP:
			stringGameState = local.keyWordStartup;
			break;
		case ENDING:
		case UNREADY:
		default:
			stringGameState = local.keyWordUnset;
		}

		player.sendMessage(
				ChatColor.translateAlternateColorCodes('&', "&8&m" + StringUtils.repeat(" ", 13) + "&r &5CubeRunner &d"
						+ local.keyWordInformation + " &5: &d" + name + " &8&m" + StringUtils.repeat(" ", 13)));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&',
				"&5" + local.keyWordCurrent + " " + local.keyWordGameState + ": &7" + stringGameState));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&',
				"&5" + local.keyWordCurrent + " " + local.keyWordAmountPlayer + ": &7" + users.size()));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&',
				"&5" + local.keyWordMinimum + " " + local.keyWordAmountPlayer + ": &7" + minAmountPlayer));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&',
				"&5" + local.keyWordMaximum + " " + local.keyWordAmountPlayer + ": &7" + maxAmountPlayer));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5" + local.keyWordBestScore + ": &7"
				+ highestScore + " &5" + local.keyWordBy + " &7" + highestPlayer));
		player.sendMessage("\n");

		if (!Permissions.hasPermission(Permissions.advancedInfo, player, false))
			return;

		player.sendMessage(
				ChatColor.translateAlternateColorCodes('&', "&8&m" + StringUtils.repeat(" ", 5) + "&r &5CubeRunner &d"
						+ local.keyWordAdvanced + " &5: &d" + name + " &8&m" + StringUtils.repeat(" ", 5)));
		player.sendMessage(
				ChatColor.translateAlternateColorCodes('&', "&5" + local.keyWordWorld + ": &7" + world.getName()));
		if (lobby == null) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&5" + local.keyWordLobby + ": &7null"));
		} else {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&5" + local.keyWordLobby + ": &7{" + ((int) (lobby.getX() * 100)) / (double) 100 + ", "
							+ ((int) (lobby.getY() * 100)) / (double) 100 + ", "
							+ ((int) (lobby.getZ() * 100)) / (double) 100 + "}"));
		}
		if (startPoint == null) {
			player.sendMessage(
					ChatColor.translateAlternateColorCodes('&', "&5" + local.keyWordStartPoint + ": &7null"));
		} else {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&5" + local.keyWordStartPoint + ": &7{" + ((int) (startPoint.getX() * 100)) / (double) 100 + ", "
							+ ((int) (startPoint.getY() * 100)) / (double) 100 + ", "
							+ ((int) (startPoint.getZ() * 100)) / (double) 100 + "}"));
		}
		if (minPoint == null) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&5" + local.keyWordMinimum + local.keyWordZone + ": &7null"));
		} else {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&5" + local.keyWordMinimum + local.keyWordZone + ": &7{" + minPoint.getBlockX() + ", "
							+ minPoint.getBlockY() + ", " + minPoint.getBlockZ() + "}"));
		}
		if (maxPoint == null) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&5" + local.keyWordMaximum + local.keyWordZone + ": &7null"));
		} else {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&',
					"&5" + local.keyWordMaximum + local.keyWordZone + ": &7{" + maxPoint.getBlockX() + ", "
							+ maxPoint.getBlockY() + ", " + maxPoint.getBlockZ() + "}"));
		}
		player.sendMessage("\n");
	}

	/*******************************************
	 * Big Game commands
	 *********************************************/

	public void addPlayer(Player player, boolean teleport) {
		Language local = playerData.getLanguageOfPlayer(player);
		playerData.addOnFileIfNotExist(player);

		if (getArenaFromPlayer(player) != null) {
			local.sendMsg(player, local.playerAlreadyInGame);
			return;
		}

		if (gameState == GameState.UNREADY || gameState == GameState.ENDING) {
			local.sendMsg(player, local.playerJoinUnready);
			return;
		}

		addUser(player, gameState == GameState.ACTIVE);

		if (teleport)
			player.teleport(lobby);

		if (gameState == GameState.ACTIVE) {
			local.sendMsg(player, local.playerJoinActive);

			if (teleport)
				local.sendMsg(player, local.playerJoinSpectator);

			return;
		}

		if (gameState == GameState.STARTUP)
			player.teleport(startPoint);

		local.sendMsg(player, local.playerJoinSuccess.replace("%arena%", name));
		for (User u : users) {
			Player p = u.getPlayer();
			if (p == player)
				return;

			Language localAlt = playerData.getLanguageOfPlayer(p);
			localAlt.sendMsg(p, localAlt.playerJoinOthers.replace("%player%", player.getDisplayName()));
		}
	}

	private void addUser(Player player, boolean eliminated) {
		User user = new User(config, player, gameState == GameState.ACTIVE);
		users.add(user);
		user.maxStats();
		user.setEliminated(eliminated);
	}

	public void removePlayer(Player player, boolean disconnect) {
		Language local = playerData.getLanguageOfPlayer(player);

		User user = getUser(player);

		user.quit();

		local.sendMsg(player, local.playerQuitSuccess.replace("%arena%", name));
		for (User u : users) {
			Player p = u.getPlayer();
			if (p == player)
				continue;

			Language l = playerData.getLanguageOfPlayer(p);
			l.sendMsg(p, l.playerQuitOthers.replace("%player%", player.getDisplayName()));
		}

		if (getGameState() == GameState.ACTIVE)
			eliminateUser(user, disconnect);

		if (getGameState() == GameState.STARTUP)
			player.teleport(lobby);

		user.returnStats();
		removeUser(player);
	}

	public void initiateGame(Player player) {
		Language local = playerData.getLanguageOfPlayer(player);

		if (users.size() < minAmountPlayer) {
			local.sendMsg(player, local.gameStartLessMin.replace("%amount%", String.valueOf(minAmountPlayer)));
			return;
		}

		if (users.size() > maxAmountPlayer) {
			local.sendMsg(player, local.gameStartLessMax.replace("%amount%", String.valueOf(maxAmountPlayer)));
			return;
		}

		if (users.size() < maxAmountPlayer)
			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				Language localTemp = playerData.getLanguageOfPlayer(p);
				p.sendMessage(ChatColor.translateAlternateColorCodes('&',
						localTemp.prefixShort + localTemp.gameCountdownStarted.replace("%arena%", name)));
			}

		resetArena();
		gameState = GameState.STARTUP;

		for (User user : users) {
			user.getPlayer().teleport(startPoint);
		}

		countdown(this, config.countdownTime * 20);
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
								user.getPlayer().playSound(user.getPlayer().getLocation(), Sound.CLICK, 1, 1);

								Language local = playerData.getLanguageOfPlayer(user.getPlayer());

								PacketPlayOutTitle countNumberTitle = new PacketPlayOutTitle(EnumTitleAction.TITLE,
										ChatSerializer.a("{\"text\":\"" + cooldownTimer / 20
												+ "\",\"color\":\"gold\",\"bold\":true}"),
										1, 20, 5);
								PacketPlayOutTitle countNumberSubtitle = new PacketPlayOutTitle(
										EnumTitleAction.SUBTITLE, ChatSerializer.a("{\"text\":\"" + local.keyWordSeconds
												+ "\",\"color\":\"dark_gray\",\"italic\":true}"),
										1, 20, 5);

								PlayerConnection connection = ((CraftPlayer) user.getPlayer())
										.getHandle().playerConnection;
								connection.sendPacket(countNumberTitle);
								connection.sendPacket(countNumberSubtitle);

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
						Language local = playerData.getLanguageOfPlayer(p);
						local.sendMsg(user.getPlayer(), local.gameCountdownStopped);
					}
				}
			}
		}, 1L);

	}

	protected void startGame() {
		gameState = GameState.ACTIVE;
		originalAmountPlayer = getAmountInGame(this);
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

				if (getAmountInGame(arena) == 0)
					return;

				if (!arena.filled)
					if (arena.isArenaFull()) {
						arena.filled = true;
						for (User u : arena.users)
							if (!u.isEliminated())
								achievements.checkAchievement(AchievementType.FILL_THE_ARENA, u.getPlayer());
					}

				if (number == 6000)
					for (User u : arena.users)
						if (!u.isEliminated())
							achievements.checkAchievement(AchievementType.SURVIVE_5_MINUTES, u.getPlayer());

				for (User user : arena.users) {

					if (user.isEliminated())
						continue;

					Player player = user.getPlayer();

					Location l;
					if (arena.maxPoint.getBlockY() > player.getLocation().getBlockY() + 10)
						l = new Location(player.getWorld(), player.getLocation().getBlockX(),
								player.getLocation().getBlockY() + 10, player.getLocation().getBlockZ());
					else
						l = new Location(player.getWorld(), player.getLocation().getBlockX(),
								arena.maxPoint.getBlockY(), player.getLocation().getBlockZ());

					player.setSaturation(20);
					if (number % 20 == 0) {
						player.setLevel((int) number / 20);
						user.addToScore();

						int totalScore = 0;
						if (mysql.hasConnection()) {
							ResultSet query = mysql.query("SELECT totalScore FROM " + config.tablePrefix
									+ "PLAYERS WHERE UUID='" + user.getUUID() + "';");
							try {
								if (query.next())
									totalScore = query.getInt("totalScore");
							} catch (SQLException e) {
								e.printStackTrace();
							}

							mysql.update("UPDATE " + config.tablePrefix + "PLAYERS SET totalScore='" + ++totalScore
									+ "' WHERE UUID='" + user.getUUID() + "';");
						} else {
							totalScore = playerData.getData().getInt("players." + user.getUUID() + ".totalScore", 0);
							playerData.getData().set("players." + user.getUUID() + ".totalScore", ++totalScore);
							playerData.savePlayerData();
						}

						achievements.checkAchievement(AchievementType.TOTAL_SCORE, player);
					}

					ItemStackManager fallingBlock = arena.colorManager.getRandomAvailableBlock();

					@SuppressWarnings("deprecation")
					FallingBlock clay = (FallingBlock) player.getWorld().spawnFallingBlock(l,
							fallingBlock.getMaterial(), (byte) fallingBlock.getData());

					clay.setDropItem(false);
					clay.setCustomName(user.getUUID());
					clay.setCustomNameVisible(false);

					if (number % 2 == 0) {
						clay.setVelocity(
								new Vector((1 - (Math.random() * 2)) / 10.0, 0, (1 - (Math.random() * 2)) / 10.0));

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

	protected void eliminateUser(User user, boolean disconnection) {

		user.getPlayer().teleport(lobby);
		user.setEliminated(true);

		if (!disconnection) {
			Language local = playerData.getLanguageOfPlayer(user.getPlayer());
			local.sendMsg(user, local.gameCrushedPlayer.replace("%score%", String.valueOf(user.getScore())));
			for (User u : users) {
				if (u == user)
					continue;

				Language l = playerData.getLanguageOfPlayer(u.getPlayer());
				l.sendMsg(u, l.gameCrushedOthers.replace("%player%", user.getDisplayName()).replace("%score%",
						String.valueOf(user.getScore())));
			}
		}

		int games = 0;
		int timePlayed = 0;
		double totalDistance = 0;
		int totalScore = 0;
		double money = 0;

		if (mysql.hasConnection()) {
			ResultSet query = mysql.query("SELECT games, timePlayed, totalDistance, totalScore, money FROM "
					+ config.tablePrefix + "PLAYERS WHERE UUID='" + user.getUUID() + "';");

			try {
				if (query.next()) {
					games = query.getInt("games");
					timePlayed = query.getInt("timePlayed");
					totalDistance = query.getDouble("totalDistance");
					totalScore = query.getInt("totalScore");
					money = query.getDouble("money");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			games = playerData.getData().getInt("players." + user.getUUID() + ".games", 0);
			timePlayed = playerData.getData().getInt("players." + user.getUUID() + ".timePlayed", 0);
			totalDistance = playerData.getData().getDouble("players." + user.getUUID() + ".totalDistance", 0);
			totalScore = playerData.getData().getInt("players." + user.getUUID() + ".totalScore", 0);
			money = playerData.getData().getDouble("players." + user.getUUID() + ".money", 0);
		}

		games++;
		timePlayed += user.getGameLenght();
		totalDistance += user.getDistanceRan();
		double ratio = (double) totalScore / games;

		if (CubeRunner.isEconomyEnabled()) {
			Player player = user.getPlayer();
			double amount = (config.pricePerScore * user.getScore());
			economy.depositPlayer(player, amount);
			Language l = playerData.getLanguageOfPlayer(player);
			l.sendMsg(player,
					l.achievementMoneyGame.replace("%amount2%", String.valueOf(user.getScore()))
							.replace("%amount%", String.valueOf((int) (amount * 100) / 100.0))
							.replace("%currency%", economy.currencyNamePlural()));
			money += amount;
		}

		if (mysql.hasConnection()) {
			mysql.update("UPDATE " + config.tablePrefix + "PLAYERS SET money='" + money + "', timePlayed='" + timePlayed
					+ "', games='" + games + "', totalDistance='" + totalDistance + "', averageDistancePerGame='"
					+ ratio + "' WHERE UUID='" + user.getUUID() + "';");
		} else {
			playerData.getData().set("players." + user.getUUID() + ".timePlayed", timePlayed);
			playerData.getData().set("players." + user.getUUID() + ".games", games);
			playerData.getData().set("players." + user.getUUID() + ".totalDistance", totalDistance);
			playerData.getData().set("players." + user.getUUID() + ".averageDistancePerGame", ratio);
			playerData.getData().set("players." + user.getUUID() + ".money", money);
			playerData.savePlayerData();
		}

		achievements.checkAchievement(AchievementType.AMOUNT_GAMES, user.getPlayer());

		if (user.getScore() == 42)
			achievements.checkAchievement(AchievementType.DIE_SECONDS_42, user.getPlayer());

		if (getAmountInGame(this) == 0)
			endingSequence();

		if (!mysql.hasConnection())
			topManager.updateAll();
	}

	private void endingSequence() {
		gameState = GameState.ENDING;

		User user = getHighestScore();

		if (originalAmountPlayer > 1) {
			int multiWins = 0;
			if (mysql.hasConnection()) {
				ResultSet query = mysql.query("SELECT multiplayerWon FROM " + config.tablePrefix
						+ "PLAYERS WHERE UUID='" + user.getUUID() + "';");
				try {
					if (query.next())
						multiWins = query.getInt("multiplayerWon");

					mysql.update("UPDATE " + config.tablePrefix + "PLAYERS SET multiplayerWon='" + ++multiWins
							+ "' WHERE UUID='" + user.getUUID() + "';");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			} else {
				multiWins = playerData.getData().getInt("players." + user.getUUID() + ".multiplayerWon", 0);
				playerData.getData().set("players." + user.getUUID() + ".multiplayerWon", ++multiWins);
				playerData.savePlayerData();
			}

			achievements.checkAchievement(AchievementType.MULTIPLAYER_GAMES_WON, user.getPlayer());
		}

		if (user.getScore() > highestScore) {
			highestScore = user.getScore();
			highestPlayer = user.getPlayer().getName();

			for (Player player : Bukkit.getServer().getOnlinePlayers()) {
				Language local = playerData.getLanguageOfPlayer(player);
				local.sendMsg(player, local.endingBest.replace("%player%", user.getDisplayName())
						.replace("%score%", String.valueOf(user.getScore())).replace("%arena%", name));
			}

			if (mysql.hasConnection()) {
				mysql.update("UPDATE " + config.tablePrefix + "ARENAS SET highestScore='" + user.getScore()
						+ "', highestPlayer='" + user.getPlayer().getName() + "' WHERE name='" + name + "';");
			} else {
				arenaData.getData().set("arenas." + name + ".highestScore.score", highestScore);
				arenaData.getData().set("arenas." + name + ".highestScore.player", user.getPlayer().getName());
				arenaData.saveArenaData();
			}
		}

		if (config.teleportAfterEnding) {
			for (User u : users) {
				Language local = playerData.getLanguageOfPlayer(u.getPlayer());
				local.sendMsg(u, local.endingTeleport);
			}

			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					kickUsers();
				}
			}, 100L);
		} else
			kickUsers();

	}

	private User getHighestScore() {
		User user = new User(0);
		for (User u : users)
			if (user.getScore() <= u.getScore())
				user = u;
		return user;
	}

	private static int getAmountInGame(Arena arena) {
		int i = 0;
		for (User user : arena.users)
			if (!user.isEliminated())
				i++;

		return i;
	}

	private void kickUsers() {
		for (User user : users) {
			user.quit();
			user.returnStats();
		}

		users.clear();
		gameState = GameState.READY;
	}

	public void removeUser(Player player) {
		for (User user : users) {
			if (user.getUUID().equalsIgnoreCase(player.getUniqueId().toString())) {
				users.remove(user);

				if (getGameState() == GameState.STARTUP)
					checkIfStillActive();

				return;
			}
		}
	}

	private void checkIfStillActive() {
		if (getAmountInGame(this) < minAmountPlayer) {
			gameState = GameState.READY;
			for (User user : users)
				user.getPlayer().teleport(lobby);
		}
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

	public boolean isOutsideArena(Location location) {
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

	public static Arena getArenaFromUser(User user) {
		for (Arena arena : arenas)
			for (User u : arena.users)
				if (user == u)
					return arena;
		return null;
	}

	public User getUser(Player player) {
		for (User user : users)
			if (user.getPlayer() == player)
				return user;
		return null;
	}

	protected int getAmountOfPlayerInGame() {
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

	public List<User> getUsers() {
		return users;
	}

	protected String getHighestPlayer() {
		return highestPlayer;
	}

	public Player getPlayerFromUUID(String dammagerUUID) {
		for (User user : users)
			if (user.getUUID().equalsIgnoreCase(dammagerUUID))
				return user.getPlayer();
		return null;
	}

	public Location getMinPoint() {
		return minPoint;
	}

	public ColorManager getColorManager() {
		return colorManager;
	}

	public Location getMaxPoint() {
		return maxPoint;
	}

}
