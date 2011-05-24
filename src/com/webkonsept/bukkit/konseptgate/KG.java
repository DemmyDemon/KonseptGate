package com.webkonsept.bukkit.konseptgate;

import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class KG extends JavaPlugin {
	private Logger log = Logger.getLogger("Minecraft");
	private PermissionHandler Permissions;
	private KGPlayerListener playerListener = new KGPlayerListener(this); 
	public boolean verbose = true;
	public KGConfig config	= new KGConfig(this);
	//public KGGates	gates 	= new KGGates(this);
	
	@Override
	public void onDisable() {
		// gates.save();
	}

	@Override
	public void onEnable() {
		// config.load();
		// gates.load();
		PluginManager pm = this.getServer().getPluginManager();
		if(!setupPermissions()){
			this.crap("PERMISSIONS plugin not loaded!  THIS WON'T WORK!");
			pm.disablePlugin(this);
			return;
		}
		pm.registerEvent(Event.Type.PLAYER_MOVE,playerListener,Priority.Normal,this);
	}
	public boolean permit(Player player,String permission){
		if (player == null) {
			this.crap("NULL player passed to permission check!");
			return false;
		}
		if (permission == null) {
			this.crap("NULL permission passed to permission check!");
			return false;
		}
		boolean allow = Permissions.has(player,permission);
		this.babble(player.getName()+" asked permission to "+permission+": "+allow);
		return allow;
	}
	private boolean setupPermissions() {
		Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
		if (this.Permissions == null){
			if (test != null){
				this.Permissions = ((Permissions)test).getHandler();
				return true;
			}
			else {
				return false;
			}
		}
		else {
			this.crap("Urr, this is odd...  Permissions are already set up!");
			return true;
		}
	}
	public void out(String message) {
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info("[" + pdfFile.getName()+ " " + pdfFile.getVersion() + "] " + message);
	}
	public void crap(String message){
		PluginDescriptionFile pdfFile = this.getDescription();
		log.severe("[" + pdfFile.getName()+ " " + pdfFile.getVersion() + "] " + message);
	}
	public void babble(String message){
		if (!this.verbose){ return; }
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info("[" + pdfFile.getName()+ " " + pdfFile.getVersion() + " VERBOSE] " + message);
	}
	public String plural(int number) {
		if (number == 1){
			return "";
		}
		else {
			return "s";
		}
	}
}
