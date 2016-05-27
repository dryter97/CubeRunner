package me.poutineqc.cuberunner.games;

import org.bukkit.entity.Player;

import me.poutineqc.cuberunner.Configuration;
import me.poutineqc.cuberunner.tools.OriginalPlayerStats;

public class User {

	private Player player;
	private String uuid;
	private String displayName;
	private OriginalPlayerStats originalStats;
	
	private boolean left = false;
	private boolean eliminated = false;
	private long startTime;
	private int score = 0;
	private int jump = 0;
	private boolean jumping = false;
	private double distanceRan = 0;
	
	public User(Configuration config, Player player, boolean eliminated) {
		this.player = player;
		uuid = player.getUniqueId().toString();
		displayName = player.getDisplayName();
		originalStats = new OriginalPlayerStats(config, player);
		this.eliminated = eliminated;
	}
	
	public User(int time) {
		this.score = time;
	}

	protected void setStartTime() {
		startTime = System.currentTimeMillis();
	}
	
	public Player getPlayer() {
		return player;
	}
	
	protected void addToScore() {
		score++;
	}
	
	protected int getScore() {
		return score;
	}
	
	protected String getDisplayName() {
		return displayName;
	}
	
	protected String getUUID() {
		return uuid;
	}
	
	protected int getGameLenght() {
		return (int) (System.currentTimeMillis() - startTime);
	}
	
	protected void returnStats() {
		originalStats.returnStats(getPlayer());
	}
	
	protected void maxStats() {
		originalStats.maxStats(getPlayer());
	}

	public boolean isEliminated() {
		return eliminated;
	}
	
	public void setEliminated(boolean eliminated) {
		this.eliminated = eliminated;
	}

	public void returnStats(Player player) {
		originalStats.returnStats(player);
		
	}

	public void quit() {
		left = true;
	}
	
	public boolean hasLeft() {
		return left;
	}
	
	public void jump() {
		jump++;
	}
	
	public int getJump() {
		return jump;
	}
	
	public void setJumping(boolean jumping) {
		this.jumping = jumping;
	}
	
	public boolean isJumping() {
		return jumping;
	}
	
	public void addToDistanceRan(double distance) {
		distanceRan += distance;
	}
	
	public double getDistanceRan() {
		return distanceRan;
	}
}
