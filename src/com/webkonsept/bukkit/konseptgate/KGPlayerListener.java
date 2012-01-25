package com.webkonsept.bukkit.konseptgate;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitScheduler;

public class KGPlayerListener implements Listener {
	private KG plugin = null;
	public HashMap<Player,Long> inTransit = new HashMap<Player,Long>();
	public HashSet<Player> frozen = new HashSet<Player>();
	
	KGPlayerListener (KG instance){
		plugin = instance;
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onPlayerInteract(final PlayerInteractEvent event){
		if (!plugin.isEnabled()) return;
		if (event.isCancelled()) return;
		Player player = event.getPlayer();
		if (event.getAction().equals(Action.PHYSICAL) && event.getClickedBlock().getType().equals(Material.STONE_PLATE)){
			if (inTransit.containsKey(player) && inTransit.get(player) > System.currentTimeMillis()){
				plugin.babble("Still in transit from the last TP, won't TP again.");
			}
			else {
				Location saneOriginLocation = KGate.saneLocation(event.getClickedBlock().getLocation());
				if (plugin.gates.gateLocation.containsKey(saneOriginLocation)){
					if (plugin.permit(player, "konseptgate.teleport")){
						KGate origin = plugin.gates.gateLocation.get(saneOriginLocation);
						if (plugin.gates.gateName.containsKey(origin.getTargetName())){
							KGate destinationGate = plugin.gates.gateName.get(origin.getTargetName());
							
							Location destination = destinationGate.getLocationForTeleport();
							if (destination != null){
								plugin.babble("Teleporting "+player.getName()+" to "+origin.getTargetName());
								BukkitScheduler scheduler = plugin.getServer().getScheduler();
								inTransit.put(player,System.currentTimeMillis()+2000);
								scheduler.scheduleSyncDelayedTask(plugin, new KGPlayerTeleport(event.getPlayer(),destination,frozen,plugin.fireEffect),1);
							}
							else {
								plugin.babble("Target world isn't loaded yet!");
								player.sendMessage(ChatColor.RED+"Sorry, the target world is not loaded yet!");
							}
						}
						else if (origin.getTargetName().equals("")){
							player.sendMessage("No destination is set for this gate");
						}
						else{
							player.sendMessage("Destination gate, '"+origin.getTargetName()+"', is unknown.");
						}
					}
					else {
						player.sendMessage(ChatColor.RED+"Sorry, teleport permission denied!");
					}
				}
			}
		}
		else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock().getType().equals(Material.STONE_PLATE)){
			Location saneGateLocation = KGate.saneLocation(event.getClickedBlock().getLocation());
			if (plugin.gates.gateLocation.containsKey(saneGateLocation)){
				if (plugin.permit(player, "konseptgate.info")){
					KGate gate = plugin.gates.gateLocation.get(saneGateLocation);
					if (gate.getTargetName().length() > 0){
						player.sendMessage(ChatColor.GRAY+"This gate is called "+gate.getName()+", it goes to "+gate.getTargetName()+".");
					}
					else {
						player.sendMessage("This gate is called "+gate.getName()+", it is not linked and goes nowhere.");
					}
					event.setCancelled(true);
				}
			}
		}
	}
	@EventHandler
	public void onPlayerMove (final PlayerMoveEvent event){
		if (!plugin.isEnabled()) return;
		if (event.isCancelled()) return;
		if (frozen.contains(event.getPlayer())){
			event.setCancelled(true);
		}
	}
}
