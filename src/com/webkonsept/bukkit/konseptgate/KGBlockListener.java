package com.webkonsept.bukkit.konseptgate;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.inventory.ItemStack;

public class KGBlockListener extends BlockListener {
	KG plugin;
	KGBlockListener (KG instance){
		plugin = instance;
	}
	
	public void onBlockBreak(BlockBreakEvent event){
		Block block = event.getBlock();
		Block above = block.getRelative(BlockFace.UP);
		boolean message = false;
		Player player = event.getPlayer();
		
		if (player != null){
			message = true;
		}
		
		if (plugin.gates.gateLocation.containsKey(block.getLocation())){
			event.setCancelled(true);
			if (message){
				player.sendMessage(ChatColor.GOLD+"You can't destroy a KonseptGate this way.  Delete or move it.");
			}
		}
		else if (plugin.gates.gateLocation.containsKey(above.getLocation())){
			event.setCancelled(true);
			if (message){
				player.sendMessage(ChatColor.GOLD+"You can't destroy this block: A KonseptGate rests on it.");
			}
		}
	}
	public void onBlockPhysics(BlockPhysicsEvent event){
		if (plugin.gates.gateLocation.containsKey(event.getBlock().getLocation())){
			event.setCancelled(true);  // Allowing belowBlock to be glowstone, glass etc etc
		}
		else if (plugin.gates.gateLocation.containsKey(event.getBlock().getRelative(BlockFace.DOWN).getLocation())){
			event.setCancelled(true);  // Again, to allow fancy materials.
		}
	}
	public void onBlockPistonRetract(BlockPistonRetractEvent event){
		if (event.isSticky()){ // If it's not sticky it can't affect any Gate.
			Block piston = event.getBlock();
			Block affected = piston.getRelative(event.getDirection()).getRelative(event.getDirection());
			// Direction twice, because retraction affects TWO blocks away due to being extended, right-oh?
			
			if (plugin.gates.gateLocation.containsKey(affected.getRelative(BlockFace.UP).getLocation())){
				plugin.babble("Piston tried to mess with the block below a KonseptGate! BOO!!");
				event.setCancelled(true);  // Moving the block below a plate with a piston is bad.
				
				byte b = piston.getData();
				piston.setType(Material.PISTON_BASE); // The slime ball is "dropped"
				piston.setData(b); // To maintain the facing direction etc.
				
				ItemStack slime = new ItemStack(Material.SLIME_BALL);
				slime.setAmount(1);
				piston.getWorld().dropItemNaturally(piston.getLocation(),slime);  // To give back the slime lost when resolving this.
			}
		}
	}
	public void onBlockPistonExtend(BlockPistonExtendEvent event){
		BlockFace facing = event.getDirection();
		
		if (plugin.gates.isGate(event.getBlock().getRelative(facing))){
			plugin.babble("Piston tried to mess with a KonseptGate by pushing it! BOO!!");
			event.setCancelled(true);
		}
		else {
			for (Block affected : event.getBlocks()){
				if (plugin.gates.isGate(affected)){
					// Yeah, this doesn't work because of a bug.
					// I'll leave it in here as it will "take over" when that bug is fixed.
					plugin.babble("Piston tried to mess with a KonseptGate! BOO!!");
					event.setCancelled(true);
					break;
				}
				else if (plugin.gates.isGate(affected.getRelative(facing))){
					plugin.babble("Piston tried to mess with a KonseptGate by proxy! BOO!!");
					event.setCancelled(true);
					break;
				}
				else if (plugin.gates.isGate(affected.getRelative(BlockFace.UP))){
					plugin.babble("Piston tried to mess with the block below a KonseptGate! BOO!!");
					event.setCancelled(true);
					break;
				}
			}
		}
	}
}
