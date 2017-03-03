package me.poutineqc.cuberunner.commands.signs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import me.poutineqc.cuberunner.Configuration;
import me.poutineqc.cuberunner.CubeRunner;
import me.poutineqc.cuberunner.Language;
import me.poutineqc.cuberunner.MySQL;
import me.poutineqc.cuberunner.PlayerData;
import me.poutineqc.cuberunner.game.Arena;
import me.poutineqc.cuberunner.utils.MinecraftConfiguration;

public abstract class CRSign {

	protected static CubeRunner plugin;
	protected static Configuration config;
	private static MySQL mysql;
	protected static PlayerData playerData;

	private static MinecraftConfiguration signData;

	protected static List<CRSign> signs = new ArrayList<CRSign>();

	private UUID uuid;
	protected Location location;

	public static void setVariables(CubeRunner plugin) {
		CRSign.plugin = plugin;
		CRSign.config = plugin.getConfiguration();
		CRSign.mysql = plugin.getMySQL();
		CRSign.playerData = plugin.getPlayerData();

		signData = new MinecraftConfiguration(null, "signData", false);
	}

	public static void loadAllSigns() {
		signs.clear();

		if (mysql.hasConnection()) {
			ResultSet query = mysql.query("SELECT * FROM " + config.tablePrefix + "SIGNS;");
			try {
				while (query.next()) {
					UUID uuid = UUID.fromString(query.getString("uuid"));
					Location location = new Location(Bukkit.getWorld(query.getString("locationWorld")),
							query.getInt("locationX"), query.getInt("locationY"), query.getInt("locationZ"));

					try {
						switch (SignType.valueOf(query.getString("type"))) {
						case JOIN:
							new CRSignJoin(uuid, location);
							break;
						case PLAY:
							new CRSignPlay(uuid, location);
							break;
						case QUIT:
							new CRSignQuit(uuid, location);
							break;
						case START:
							new CRSignStart(uuid, location);
							break;
						case STATS:
							new CRSignStats(uuid, location);
							break;
						case TOP:
							new CRSignTop(uuid, location);
							break;

						}
					} catch (IllegalArgumentException e) {
						CRSign.removeSign(uuid, location);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			if (!signData.get().contains("signs"))
				return;

			for (String uuid : signData.get().getConfigurationSection("signs").getKeys(false)) {
				ConfigurationSection cs = signData.get().getConfigurationSection("signs." + uuid);
				Location location = new Location(Bukkit.getWorld(cs.getString("location.world")),
						cs.getInt("location.X", 0), cs.getInt("location.Y", 0), cs.getInt("location.Z"));

				try {
					switch (SignType.valueOf(cs.getString("type", UUID.randomUUID().toString()))) {
					case JOIN:
						new CRSignJoin(UUID.fromString(uuid), location);
						break;
					case PLAY:
						new CRSignPlay(UUID.fromString(uuid), location);
						break;
					case QUIT:
						new CRSignQuit(UUID.fromString(uuid), location);
						break;
					case START:
						new CRSignStart(UUID.fromString(uuid), location);
						break;
					case STATS:
						new CRSignStats(UUID.fromString(uuid), location);
						break;
					case TOP:
						new CRSignTop(UUID.fromString(uuid), location);
						break;
					}
				} catch (IllegalArgumentException e) {
					CRSign.removeSign(UUID.fromString(uuid), location);
				}
			}
		}

		updateSigns();
	}

	public CRSign(UUID uuid, Location location, SignType type) {
		this.uuid = uuid;
		this.location = location;
	}

	public CRSign(Location location, SignType type) {
		this.uuid = UUID.randomUUID();
		this.location = location;

		if (mysql.hasConnection()) {
			mysql.update("INSERT INTO " + config.tablePrefix
					+ "SIGNS (uuid, type ,locationWorld, locationX, locationY, locationZ) " + "VALUES ('" + uuid + "','"
					+ type.name() + "','" + location.getWorld().getName() + "','" + location.getBlockX() + "','"
					+ location.getBlockY() + "','" + location.getBlockZ() + "');");

		} else {
			signData.get().set("signs." + uuid.toString() + ".type", type.name());
			signData.get().set("signs." + uuid.toString() + ".location.world", location.getWorld().getName());
			signData.get().set("signs." + uuid.toString() + ".location.X", location.getBlockX());
			signData.get().set("signs." + uuid.toString() + ".location.Y", location.getBlockY());
			signData.get().set("signs." + uuid.toString() + ".location.Z", location.getBlockZ());
			signData.save();
		}
	}

	private static void removeSign(UUID uuid, Location location) {
		if (location.getBlock().getState() instanceof Sign) {
			Sign sign = (Sign) location.getBlock().getState();
			sign.setLine(0, " ");
			sign.setLine(1, " ");
			sign.setLine(2, " ");
			sign.setLine(3, " ");
			sign.getLocation().getChunk().load();
			sign.update();
		}

		if (mysql.hasConnection()) {
			mysql.update("DELETE FROM " + config.tablePrefix + "SIGNS WHERE uuid='" + uuid.toString() + "';");
		} else {
			signData.get().set("signs." + uuid.toString(), null);
			signData.save();
		}
	}

	public void removeSign() {
		removeSign(uuid, location);
		signs.remove(this);
	}

	public abstract void onInteract(Player player);

	protected abstract boolean updateSign(Language local, Sign sign);

	public static void updateSigns() {
		List<CRSign> errors = new ArrayList<CRSign>();

		for (CRSign dacsign : signs) {

			BlockState block = dacsign.location.getBlock().getState();
			if (!(block instanceof Sign)) {
				errors.add(dacsign);
				continue;
			}

			if (!dacsign.updateSign(Language.getDefault(), (Sign) block))
				errors.add(dacsign);
		}

		for (CRSign toRemove : errors)
			toRemove.removeSign();
	}

	public static void updateSigns(Arena arena) {
		if (arena == null)
			return;

		List<CRSign> errors = new ArrayList<CRSign>();

		for (CRSign dacsign : signs) {
			if (!(dacsign instanceof CRSignDisplay))
				continue;

			CRSignDisplay displaySign = (CRSignDisplay) dacsign;
			if (displaySign.arena != arena)
				continue;

			BlockState block = dacsign.location.getBlock().getState();
			if (!(block instanceof Sign)) {
				errors.add(dacsign);
				continue;
			}

			displaySign.updateDisplay(Language.getDefault(), (Sign) block);
		}

		for (CRSign toRemove : errors)
			toRemove.removeSign();
	}

	public static void arenaDelete(Arena arena) {
		if (arena == null)
			return;

		List<CRSign> errors = new ArrayList<CRSign>();

		for (CRSign dacsign : signs) {
			if (!(dacsign instanceof CRSignDisplay))
				continue;

			CRSignDisplay displaySign = (CRSignDisplay) dacsign;
			if (displaySign.arena != arena)
				continue;

			if (!arena.getName().equalsIgnoreCase(((Sign) dacsign.location.getBlock().getState()).getLine(2)))
				continue;

			errors.add(dacsign);
		}

		for (CRSign toRemove : errors)
			toRemove.removeSign();
	}

	public static CRSign getCrSign(Location location) {
		for (CRSign dacsign : signs) {
			if (dacsign.location.equals(location))
				return dacsign;
		}

		return null;
	}

	public enum SignType {
		JOIN, PLAY, START, QUIT, STATS, TOP;
	}
}
