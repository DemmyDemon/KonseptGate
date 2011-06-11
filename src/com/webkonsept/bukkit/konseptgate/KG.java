package com.webkonsept.bukkit.konseptgate;

import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		boolean validCommand = false;
		Player player;
		if (!(sender instanceof Player)){
			sender.sendMessage("Sorry, mr. Console, but you have to be in-game to do this because of all the coordinates and stuff derived from player location.");
			return true;
		}
		else {
			player = (Player) sender;
			if (args.length == 0){
				return false;
			}
			else if (args[0].equalsIgnoreCase("create")){
				validCommand = true;
				float yaw = Math.abs(player.getLocation().getYaw() % 360);
				player.sendMessage("Exact yaw: "+yaw);
				int cardinalYaw = -1;
				if (yaw >= 315 || yaw <= 45){
					cardinalYaw = 0;
				}
				else if (yaw > 45 && yaw < 135){
					cardinalYaw = 90;
				}
				else if (yaw >= 135 && yaw <= 225){
					cardinalYaw = 180;
				}
				else if (yaw > 225 && yaw < 315){
					cardinalYaw = 270;
				}
				player.sendMessage("Cadrinal yaw: "+cardinalYaw);
			}
		}
		return validCommand;
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
