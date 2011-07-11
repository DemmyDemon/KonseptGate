package com.webkonsept.bukkit.konseptgate;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class KGPlayerTeleport implements Runnable {
	Player player;
	Location destination;
	HashSet<Player> frozen;

	KGPlayerTeleport (Player player,Location destination,HashSet<Player> frozen){
		this.frozen = frozen;
		this.player = player;
		this.destination = destination;
		player.setVelocity(new Vector(0,0,0));
		frozen.add(player);
	}
	@Override
	public void run() {
		player.teleport(destination);
		player.setVelocity(new Vector(0,0,0));
		frozen.remove(player);
	}

}
