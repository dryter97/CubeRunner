package me.poutineqc.cuberunner.commands.signs;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

import me.poutineqc.cuberunner.CubeRunner;
import me.poutineqc.cuberunner.Language;
import me.poutineqc.cuberunner.Language.Messages;
import me.poutineqc.cuberunner.game.Arena;

public class CRSignTop extends CRSignDisplay {

	public CRSignTop(SignChangeEvent event, Arena arena) {
		super(event.getBlock().getLocation(), SignType.TOP);
		this.arena = arena;

		Language local = Language.getDefault();
		event.setLine(0, ChatColor.translateAlternateColorCodes('&', local.get(Messages.PREFIX_LONG)));
		event.setLine(1, ChatColor.translateAlternateColorCodes('&', local.get(Messages.KEYWORD_SIGN_TOP)));
		event.setLine(3, arena.getHighestPlayer());

		signs.add(this);

	}

	public CRSignTop(UUID uuid, Location location) {
		super(uuid, location, SignType.TOP);

		boolean delete = false;
		BlockState block = null;
		try {
			block = location.getBlock().getState();
			if (!(block instanceof Sign))
				delete = true;
			else {
				Sign sign = (Sign) block;
				arena = Arena.getArena(sign.getLine(2));
				if (arena == null)
					delete = true;
			}
		} catch (NullPointerException e) {
			delete = true;
		}

		if (delete) {
			removeSign();
			return;
		}

		signs.add(this);
	}

	@Override
	protected boolean updateSign(Language local, Sign sign) {
		sign.setLine(0, ChatColor.translateAlternateColorCodes('&', local.get(Messages.PREFIX_LONG)));
		sign.setLine(1, ChatColor.translateAlternateColorCodes('&', local.get(Messages.KEYWORD_SIGN_TOP)));
		sign.update();

		if (arena == null)
			return false;

		updateDisplay(local, sign);
		return true;
	}

	@Override
	protected void updateDisplay(Language local, Sign sign) {
		sign.setLine(3, arena.getHighestPlayer());
		sign.update();
	}

	@Override
	public void onInteract(Player player) {
		Language local = CubeRunner.get().getLang(player);
		local.sendMsg(player, local.get(Messages.KEYWORD_SIGN_TOP) + ChatColor.WHITE + " : " + String.valueOf(arena.getHighestPlayerScore())
				+ " " + local.get(Messages.KEYWORD_GENERAL_BY) + " " + ChatColor.DARK_AQUA + arena.getHighestPlayer());
	}

}