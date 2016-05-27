package me.poutineqc.cuberunner.tools;

import java.util.Collection;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import me.poutineqc.cuberunner.Configuration;

public class OriginalPlayerStats {

	private Configuration config;

	private int level;
	private float experience;
	private GameMode gameMode;
	private double health;
	private int foodLevel;
	private float saturation;
	private Collection<PotionEffect> effects;
	private Location location;

	public OriginalPlayerStats(Configuration config, Player player) {
		this.config = config;
		this.level = player.getLevel();
		this.experience = player.getExp();
		this.gameMode = player.getGameMode();
		this.health = player.getHealth();
		this.foodLevel = player.getFoodLevel();
		this.saturation = player.getSaturation();
		this.effects = player.getActivePotionEffects();
		this.location = player.getLocation();
	}

	public void returnStats(Player player) {
		if (config.teleportAfterEnding)
			player.teleport(location);

		player.setLevel(level);
		player.setExp(experience);
		player.setGameMode(gameMode);
		player.setHealth(health);
		player.setFoodLevel(foodLevel);
		player.setSaturation(saturation);
		player.addPotionEffects(effects);
	}

	public void maxStats(Player player) {
		player.setLevel(0);
		player.setExp(0);
		player.setGameMode(GameMode.ADVENTURE);
		player.setHealth(20);
		player.setFoodLevel(20);
		player.setSaturation(20);
		for (PotionEffect effect : player.getActivePotionEffects())
			player.removePotionEffect(effect.getType());

	}
}
