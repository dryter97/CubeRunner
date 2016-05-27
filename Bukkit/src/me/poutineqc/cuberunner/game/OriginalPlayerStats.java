package me.poutineqc.cuberunner.game;

import java.util.Collection;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

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
	private boolean flying;
	private boolean allowFlight;

	private ItemStack[] inventoryItems;

	public OriginalPlayerStats(Configuration config, Player player) {
		this.config = config;
		this.location = player.getLocation();
	}

	public void ireturnStats(Player player) {
		for (PotionEffect effect : player.getActivePotionEffects())
			player.removePotionEffect(effect.getType());

		player.setAllowFlight(allowFlight);
		if (flying) {
			player.setAllowFlight(true);
			player.setFlying(flying);
		}
		player.setFallDistance(0);
		player.setVelocity(new Vector());
		player.setLevel(level);
		player.setExp(experience);
		player.setGameMode(gameMode);
		player.setHealth(health);
		player.setFoodLevel(foodLevel);
		player.setSaturation(saturation);
		player.addPotionEffects(effects);
		
		if (config.saveAndClearInventory) {
			player.getInventory().setContents(inventoryItems);
			player.updateInventory();
		}
			
		if (config.teleportAfterEnding)
			player.teleport(location);
		
	}

	public void imaxStats(Player player) {
		player.setFallDistance(0);
		player.setVelocity(new Vector());
		player.setFlying(false);
		player.setAllowFlight(false);
		player.setLevel(0);
		player.setExp(0);
		player.setGameMode(GameMode.ADVENTURE);
		player.setHealth(20);
		player.setFoodLevel(20);
		player.setSaturation(20);
		for (PotionEffect effect : player.getActivePotionEffects())
			player.removePotionEffect(effect.getType());
		
		if (config.saveAndClearInventory) {
			player.getInventory().clear();;
			player.updateInventory();
		}
	}

	public void ifillOtherStats(Player player) {
		this.flying = player.isFlying();
		this.allowFlight = player.getAllowFlight();
		this.level = player.getLevel();
		this.experience = player.getExp();
		this.gameMode = player.getGameMode();
		this.health = player.getHealth();
		this.foodLevel = player.getFoodLevel();
		this.saturation = player.getSaturation();
		this.effects = player.getActivePotionEffects();
		this.inventoryItems = player.getInventory().getContents().clone();
	}
}