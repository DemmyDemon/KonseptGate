package com.webkonsept.bukkit.konseptgate;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class KGWorldListener implements Listener {
	KG plugin;
	KGWorldListener(KG instance){
		plugin = instance;
	}
	@EventHandler
	public void onWorldLoad(final WorldLoadEvent event){
		if (plugin.gates.gateWorldNotLoaded > 0){
			for (KGate gate : plugin.gates.gates){
				gate.getLocation();
			}
		}
	}
}
