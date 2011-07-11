package com.webkonsept.bukkit.konseptgate;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;

public class KGBlockListener extends BlockListener {
	KG plugin;
	KGBlockListener (KG instance){
		plugin = instance;
	}
	
	public void onBlockBreak(BlockBreakEvent event){
		Block block = event.getBlock();
		Block above = block.getFace(BlockFace.UP);
		boolean message = false;
		Player player = event.getPlayer();
		
		if (player != null){
			message = true;
		}
		
		if (plugin.gates.gateLocation.containsKey(block.getLocation())){
			event.setCancelled(true);
			if (message){
				player.sendMessage(ChatColor.GOLD+"You can't destroy a KonseptGate this way.  Use /kg delete <gatename>");
			}
		}
		else if (plugin.gates.gateLocation.containsKey(above.getLocation())){
			event.setCancelled(true);
			if (message){
				player.sendMessage(ChatColor.GOLD+"You can't destroy this block because a KonseptGate rests on it.");
			}
		}
	}
}
