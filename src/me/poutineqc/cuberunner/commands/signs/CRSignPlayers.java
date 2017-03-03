package me.poutineqc.cuberunner.commands.signs;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;

import me.poutineqc.cuberunner.Language;
import me.poutineqc.cuberunner.Language.Messages;

public abstract class CRSignPlayers extends CRSignDisplay {

	public CRSignPlayers(Location location, SignType type) {
		super(location, type);
	}

	public CRSignPlayers(UUID uuid, Location location, SignType type) {
		super(uuid, location, type);
	}

	@Override
	protected void updateDisplay(Language local, Sign sign) {
		switch (arena.getGameState()) {
		case ACTIVE:
		case ENDING:
			sign.setLine(3, ChatColor.translateAlternateColorCodes('&', local.get(Messages.KEYWORD_GAMESTATE_ACTIVE)));
			break;
		case READY:
		case STARTUP:
			sign.setLine(3,
					ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', local.get(Messages.KEYWORD_SCOREBOARD_PLAYERS)))
							+ " : " + String.valueOf(arena.getAmountOfPlayerInGame()) + "/"
							+ String.valueOf(arena.getMaxPlayer()));
			break;
		case UNREADY:
			sign.setLine(3, ChatColor.translateAlternateColorCodes('&', local.get(Messages.KEYWORD_GAMESTATE_UNSET)));
			break;
		}

		sign.update();
	}

}
