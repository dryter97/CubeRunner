package me.poutineqc.cuberunner.commands.signs;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Sign;

import me.poutineqc.cuberunner.Language;
import me.poutineqc.cuberunner.game.Arena;

public abstract class CRSignDisplay extends CRSign {

	protected Arena arena;
	
	public CRSignDisplay(Location location, SignType type) {
		super(location, type);
	}

	public CRSignDisplay(UUID uuid, Location location, SignType type) {
		super(uuid, location, type);
	}
	
	protected abstract void updateDisplay(Language local, Sign sign);

}
