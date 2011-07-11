package com.webkonsept.bukkit.konseptgate;

import java.util.HashMap;
import org.bukkit.entity.Player;

public class KGPlayerInTransit implements Runnable {
	private HashMap<Player,Long> inTransit;
	private Player player;
	KGPlayerInTransit (Player player,HashMap<Player, Long> inTransit){
		this.inTransit = inTransit;
		this.player = player;
		Long waitFor = System.currentTimeMillis()+1000;
		inTransit.put(player,waitFor);
	}
	
	@Override
	public void run() {
		inTransit.remove(player);
	}

}
