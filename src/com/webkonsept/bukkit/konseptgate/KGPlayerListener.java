package com.webkonsept.bukkit.konseptgate;

import org.bukkit.event.player.PlayerListener;

public class KGPlayerListener extends PlayerListener {
	KG plugin = null;
	
	KGPlayerListener (KG instance){
		plugin = instance;
	}

}
