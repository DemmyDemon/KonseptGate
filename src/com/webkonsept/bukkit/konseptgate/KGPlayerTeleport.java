package com.webkonsept.bukkit.konseptgate;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class KGPlayerTeleport implements Runnable {
	Player player;
	Location destination;
	Block fromBlock;
	HashSet<Player> frozen;

	KGPlayerTeleport (Player player,Block plate,Location destination,HashSet<Player> frozen,boolean showFireEffect){
		this.frozen = frozen;
		this.player = player;
		this.destination = destination;
		this.fromBlock = plate;

		player.setVelocity(new Vector(0,0,0));
		frozen.add(player);
		if(showFireEffect){
		    player.setNoDamageTicks(25);
			player.setFireTicks(20);
		}
	}
	@Override
	public void run() {
		player.teleport(destination);
		player.setVelocity(new Vector(0,0,0));
		frozen.remove(player);
		if (fromBlock != null && fromBlock.getType().equals(Material.STONE_PLATE)){
		    fromBlock.setData((byte)0);
        }
	}

}
