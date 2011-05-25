package com.webkonsept.bukkit.konseptgate;

import java.io.File;
import java.io.IOException;

import org.bukkit.util.config.Configuration;

public class KGConfig {
	private KG plugin;
	private File configFile;
	private String configFileName;
	private Configuration config;
	
	// The actual settings
	public boolean verbose = false;
	
	KGConfig(KG instance){
		plugin = instance;
		/*
		
		if (!createIfMissing(configFile)){
			plugin.crap("Failed to create config file "+configFileName);
		}
		
		*/
	}
	public void load() {
		configFileName = plugin.getDataFolder().toString()+"/settings.yml";
		configFile = new File(configFileName);
		if (!createIfMissing()){
			plugin.crap("Failed to create config file!  Defaults FTW!");
		}
		else {
			config.load();
		}
		verbose = config.getBoolean("verbose",false);
		
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
