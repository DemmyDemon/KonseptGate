package com.webkonsept.bukkit.konseptgate;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;

public class KGEntityListener extends EntityListener {
	private KG plugin;
	
	KGEntityListener (KG instance){
		plugin = instance;
	}
	
	public void onEntityExplode(EntityExplodeEvent event){
		if (!plugin.isEnabled()) return;
		for (Block block : event.blockList()){
			Block above = block.getRelative(BlockFace.UP);
			if (plugin.gates.gateLocation.containsKey(block.getLocation())){
				event.setCancelled(true);
				break;
			}
			else if (plugin.gates.gateLocation.containsKey(above.getLocation())){
				event.setCancelled(true);
				break;
			}
		}
	}
}
