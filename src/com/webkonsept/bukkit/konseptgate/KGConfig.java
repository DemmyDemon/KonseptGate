package com.webkonsept.bukkit.konseptgate;

import java.io.File;
import java.io.IOException;

public class KGConfig {
	private KG plugin;
	private File configFile;
	public String configFileName;
	
	KGConfig(KG instance){
		plugin = instance;
		configFileName = plugin.getDataFolder().toString()+"/settings.yml";
		configFile = new File(configFileName);
		/*
		
		if (!createIfMissing(configFile)){
			plugin.crap("Failed to create config file "+configFileName);
		}
		
		*/
	}
	public boolean createIfMissing () {
		boolean success = false;
		if (configFile == null) return false;
		
		if (configFile.exists()){
			success = true;
		}
		else {
			if (configFile.mkdirs()){
				try {
					configFile.createNewFile();
					success = true;
				} catch (IOException e) {
					e.printStackTrace();
					success = false;
				}
			}
			else {
				success = false;
			}
		}
		return success;
	}
}
