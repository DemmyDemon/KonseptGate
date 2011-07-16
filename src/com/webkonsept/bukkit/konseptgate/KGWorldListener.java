package com.webkonsept.bukkit.konseptgate;

import org.bukkit.event.world.WorldListener;
import org.bukkit.event.world.WorldLoadEvent;

public class KGWorldListener extends WorldListener {
	KG plugin;
	KGWorldListener(KG instance){
		plugin = instance;
	}
	public void onWorldLoad(WorldLoadEvent event){
		if (plugin.gates.gateWorldNotLoaded > 0){
			for (KGate gate : plugin.gates.gates){
				gate.getLocation();
			}
		}
	}
}
