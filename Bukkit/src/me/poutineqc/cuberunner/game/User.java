package me.poutineqc.cuberunner.game;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import me.poutineqc.cuberunner.CRPlayer;
import me.poutineqc.cuberunner.Configuration;

public class User {

	private CRPlayer player;
	private String displayName;
	private OriginalPlayerStats originalStats;

	private boolean allowTeleport = false;
	private boolean eliminated = false;
	private long startTime;
	private int score = 0;
	private int jump = 0;
	private boolean jumping = false;
	private double distanceRan = 0;

	private HashMap<Long, Double> totalDistance = new HashMap<Long, Double>();

	public User(Configuration config, CRPlayer player, Arena arena, boolean eliminated, boolean tpAuto) {
		this.player = player;
		displayName = player.getPlayer().getDisplayName();
		this.eliminated = eliminated;

		originalStats = new OriginalPlayerStats(config, player.getPlayer());

		if (tpAuto)
			player.getPlayer().teleport(arena.getLobby());

		originalStats.ifillOtherStats(player.getPlayer());
		imaxStats();
	}

	public User(int time) {
		this.score = time;
	}

	protected void setStartTime() {
		startTime = System.currentTimeMillis();
	}

	public Player getPlayer() {
		return player.getPlayer();
	}

	public CRPlayer getCRPlayer() {
		return player;
	}

	protected void addToScore() {
		score++;
	}

	protected int getScore() {
		return score;
	}

	public String getDisplayName() {
		return displayName;
	}

	protected UUID getUUID() {
		return player.getUUID();
	}

	protected int getGameLenght() {
		return (int) (System.currentTimeMillis() - startTime);
	}

	protected void ireturnStats() {
		player.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
		allowTeleport = true;
		originalStats.ireturnStats(player.getPlayer());
	}

	protected void imaxStats() {
		originalStats.imaxStats(player.getPlayer());
	}

	public boolean isEliminated() {
		return eliminated;
	}

	public void setEliminated(boolean eliminated) {
		this.eliminated = eliminated;
	}

	public void allowTeleport() {
		allowTeleport = true;
	}

	public boolean hasAllowTeleport() {
		return allowTeleport;
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

	public void addToDistanceRan(Vector vector) {
		distanceRan += vector.length();
		totalDistance.put(System.currentTimeMillis(),
				Math.sqrt(Math.pow(vector.getX(), 2) + Math.pow(vector.getZ(), 2)));
	}

	public double getDistanceRan() {
		return distanceRan;
	}

	public String getName() {
		return player.getName();
	}

	public double getLastTreeSecondsDistance() {
		long currentTime = System.currentTimeMillis();
		double distanceDone = 0;
		
		for (long i = currentTime - 2000; i <= currentTime; i++)
			distanceDone += totalDistance.getOrDefault(i, 0.0);
		
		return distanceDone;
	}
}
