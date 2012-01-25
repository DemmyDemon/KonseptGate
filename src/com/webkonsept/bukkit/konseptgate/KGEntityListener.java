package com.webkonsept.bukkit.konseptgate;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class KGEntityListener implements Listener {
	private KG plugin;
	
	KGEntityListener (KG instance){
		plugin = instance;
	}
	
	@EventHandler
	public void onEntityExplode(final EntityExplodeEvent event){
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
