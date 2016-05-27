package me.poutineqc.cuberunner.commands.signs;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

import me.poutineqc.cuberunner.Language;
import me.poutineqc.cuberunner.Language.Messages;
import me.poutineqc.cuberunner.commands.CRCommand;
import me.poutineqc.cuberunner.game.Arena;
import me.poutineqc.cuberunner.utils.Permissions;

public class CRSignPlay extends CRSignPlayers {

	public CRSignPlay(SignChangeEvent event, Arena arena) {
		super(event.getBlock().getLocation(), SignType.PLAY);
		this.arena = arena;

		Language local = Language.getDefault();

		event.setLine(0, ChatColor.translateAlternateColorCodes('&', local.get(Messages.PREFIX_LONG)));
		event.setLine(1, ChatColor.translateAlternateColorCodes('&', local.get(Messages.KEYWORD_SIGN_PLAY)));
		switch (arena.getGameState()) {
		case ACTIVE:
		case ENDING:
			event.setLine(3, ChatColor.translateAlternateColorCodes('&', local.get(Messages.KEYWORD_GAMESTATE_ACTIVE)));
			break;
		case READY:
		case STARTUP:
			event.setLine(3,
					ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.get(Messages.KEYWORD_SCOREBOARD_PLAYERS)))
							+ " : " + String.valueOf(arena.getAmountOfPlayerInGame()) + "/"
							+ String.valueOf(arena.getMaxPlayer()));
			break;
		case UNREADY:
			event.setLine(3, ChatColor.translateAlternateColorCodes('&', local.get(Messages.KEYWORD_GAMESTATE_UNSET)));
			break;
		}

		signs.add(this);
		updateSigns(arena);

	}

	public CRSignPlay(UUID uuid, Location location) {
		super(uuid, location, SignType.PLAY);

		boolean delete = false;
		BlockState block = null;
		try {
			block = location.getBlock().getState();
			if (!(block instanceof Sign)) {
				delete = true;
			} else {
				Sign sign = (Sign) block;
				arena = Arena.getArena(sign.getLine(2));
				if (arena == null) {
					delete = true;
				} else if (arena.getWorld() != location.getWorld()) {
					delete = true;
				}
			}
		} catch (NullPointerException e) {
			delete = true;
		}

		if (delete) {
			removeSign();
			return;
		}

		signs.add(this);
		updateSigns(arena);
	}

	@Override
	protected boolean updateSign(Language local, Sign sign) {
		sign.setLine(0, ChatColor.translateAlternateColorCodes('&', local.get(Messages.PREFIX_LONG)));
		sign.setLine(1, ChatColor.translateAlternateColorCodes('&', local.get(Messages.KEYWORD_SIGN_PLAY)));
		sign.update();

		if (arena == null)
			return false;

		updateDisplay(local, sign);
		return true;
	}

	@Override
	public void onInteract(Player player) {
		if (Permissions.hasPermission(CRCommand.JOIN.getPermission(), player, true))
			CRCommand.JOIN.execute(plugin, player, new String[] { "join", arena.getName() }, false);
	}

}
