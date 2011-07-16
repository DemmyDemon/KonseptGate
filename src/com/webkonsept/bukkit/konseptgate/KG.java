package com.webkonsept.bukkit.konseptgate;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class KG extends JavaPlugin {
	private Logger log = Logger.getLogger("Minecraft");
	private PermissionHandler Permissions;
	private KGPlayerListener playerListener = new KGPlayerListener(this); 
	private KGEntityListener entityListener = new KGEntityListener(this);
	private KGWorldListener worldListener = new KGWorldListener(this);
	protected KGateList	gates;
	protected int gatesPerPage = 5;
	
	protected HashSet<Player> ignored = new HashSet<Player>();
	
	// Settings
	protected boolean verbose = true;
	protected Material underblock = Material.GLOWSTONE;
	protected String defaultTarget = "";
	
	@Override
	public void onDisable() {
		if (playerListener != null){
			ignored.clear();
			playerListener.inTransit.clear();
			playerListener.frozen.clear();
		}
		if (gates != null){
			gates.save();
		}
		this.out("Disabled");
	}

	@Override
	public void onEnable() {
		this.loadConfig();
	 	gates = new KGateList(this,new File(this.getDataFolder(),"gates.txt"));
		gates.load();
		PluginManager pm = this.getServer().getPluginManager();
		if(!setupPermissions()){
			this.crap("PERMISSIONS plugin not loaded!  THIS WON'T WORK!");
			pm.disablePlugin(this);
			return;
		}
		pm.registerEvent(Event.Type.PLAYER_MOVE,playerListener,Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT,playerListener,Priority.High,this);
		pm.registerEvent(Event.Type.ENTITY_EXPLODE,entityListener,Priority.High,this);
		pm.registerEvent(Event.Type.WORLD_LOAD,worldListener,Priority.Normal,this);
		this.out("Enabled");
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
				if (args.length < 2){
					sender.sendMessage("You forgot the name of the gate!");
					return false;
				}
				String gateName = args[1];
				validCommand = true;
				if (permit(player,"konseptgate.command.create")){
					if (gates.gateName.containsKey(gateName)){
						sender.sendMessage("A gate named '"+gateName+"' already exists.  Did you mean /kg move "+gateName+"?");
						return true;
					}
					getServer().getScheduler().scheduleAsyncDelayedTask(this, new KGPlayerInTransit(player,playerListener.inTransit),10);
					Location newLocation = player.getLocation().clone();
					
					if (args.length == 2){
						gates.add(gateName, newLocation);
					}
					else if (args.length > 2){
						gates.add(gateName,newLocation,args[2]);
					}
					sender.sendMessage("Gate '"+gateName+"' created!");
				}
				else {
					player.sendMessage(ChatColor.RED+"Permission denied!");
				}
			}
			else if (args[0].equalsIgnoreCase("move")){
				if (args.length < 2){
					sender.sendMessage("You forgot the name of the gate!");
					return false;
				}
				String gateName = args[1];
				validCommand = true;
				if (permit(player,"konseptgate.command.move")){
					if (gates.gateName.containsKey(gateName)){
						getServer().getScheduler().scheduleAsyncDelayedTask(this, new KGPlayerInTransit(player,playerListener.inTransit),10);
						Location newLocation = player.getLocation().clone();
						gates.move(gateName, newLocation);
						sender.sendMessage("Gate '"+gateName+"' moved!");
					}
					else {
						player.sendMessage(gateName+"? Sorry, no such gate.");
					}
				}
				else {
					player.sendMessage(ChatColor.RED+"Permission denied!");
				}
			}
			else if (args[0].equalsIgnoreCase("delete")){
				if (args.length < 2){
					sender.sendMessage("You forgot the name of the gate!");
					return false;
				}
				String gateName = args[1];
				validCommand = true;
				if (permit(player,"konseptgate.command.delete")){
					gates.remove(gateName);
					sender.sendMessage("Gate '"+gateName+"' deleted!");
				}
				else {
					player.sendMessage(ChatColor.RED+"Permission denied!");
				}
			}
			else if (args[0].equalsIgnoreCase("list")){
				validCommand = true;
				if (permit(player,"konseptgate.command.list")){
					/*  TODO: Fix paging crap
					int listSize = gates.gates.size();
					int pages = (int) ((listSize / gatesPerPage) + 0.9);
					if (args.length > 1){
						int page = 1;
						try {
							page = Integer.parseInt(args[1]);
						}
						catch (NumberFormatException e){
							// swallow it, and go with the default of 1.
						}
					}
					if (pages > 1){
						
					}
					*/
					player.sendMessage(gates.gates.size()+" gates:");
					for (KGate gate : gates.gates){
						player.sendMessage(gate.getName()+" ("+gate.getYaw()+") -> "+gate.getTargetName());
					}
				}
				else {
					player.sendMessage(ChatColor.RED+"Permission denied!");
				}
			}
			else if (args[0].equalsIgnoreCase("reload")){
				validCommand = true;
				if (permit(player,"konseptgate.command.reload")){
					loadConfig();
					int gatesLoaded = gates.load();
					player.sendMessage(ChatColor.GOLD+"KonseptGate reloaded! "+gatesLoaded+" gates found.");
				}
			}
			else if (args[0].equalsIgnoreCase("jump")){
				
				if (args.length < 2){
					sender.sendMessage("You forgot the name of the gate!");
					return false;
				}
				String gateName = args[1];
				validCommand = true;
				if (permit(player,"konseptgate.command.jump")){
					if (gates.gateName.containsKey(gateName)){
						Location destination = gates.gateName.get(gateName).getLocationForTeleport();
						getServer().getScheduler().scheduleAsyncDelayedTask(this, new KGPlayerInTransit(player,playerListener.inTransit),20);
						getServer().getScheduler().scheduleSyncDelayedTask(this, new KGPlayerTeleport(player,destination,playerListener.frozen),1);
						player.teleport(destination);
					}
					else {
						player.sendMessage(gateName+"?  No such gate.");
					}
				}
				else {
					player.sendMessage(ChatColor.RED+"Permission denied!");
				}
			}
			else if (args[0].equalsIgnoreCase("ignore")){
				validCommand = true;
				if (permit(player,"konseptgate.command.ignore")){
					if (ignored.contains(player)){
						ignored.remove(player);
						playerListener.inTransit.put(player, System.currentTimeMillis()); // RIGHT NOW!!! :-P
						player.sendMessage(ChatColor.GREEN+"You can now use gates again.");
						
					}
					else {
						ignored.add(player);
						playerListener.inTransit.put(player, System.currentTimeMillis()+864000000); // Ignore for 10 days.
						player.sendMessage(ChatColor.GREEN+"You can now walk onto gates without being teleported!");
					}
				}
				else {
					player.sendMessage(ChatColor.RED+"Permission denied!");
				}
			}
			else if (args[0].equalsIgnoreCase("link")){
				if (args.length < 3){
					sender.sendMessage("You need both the origin and destination gate names!");
				}
				String gateFrom = args[1];
				String gateTo = args[2];
				validCommand = true;
				if (permit(player,"konseptgate.command.link")){
					if (gates.gateName.containsKey(gateFrom)){
						if (gates.gateName.containsKey(gateTo)){
							gates.gateName.get(gateFrom).setTargetName(gateTo);
							gates.save();
							sender.sendMessage("Stepping on gate '"+gateFrom+"' will now send you to '"+gateTo+"'");
						}
						else {
							sender.sendMessage("Can't link "+gateFrom+" to "+gateTo+" bacause destination gate does not exist!");
						}
					}
					else {
						sender.sendMessage("Can't link "+gateFrom+" to "+gateTo+" bacause origin gate does not exist!");
					}
				}
				else {
					player.sendMessage(ChatColor.RED+"Permission denied!");
				}
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
	public void loadConfig() {
		File configFile = new File(this.getDataFolder(),"settings.yml");
		File configDir = this.getDataFolder();
		Configuration config = new Configuration(configFile);
		
		config.load();
		this.verbose = config.getBoolean("verbose", false);
		this.defaultTarget = config.getString("defaultTarget","");
		int underblockID = config.getInt("underblockID",89);
		if (underblockID == 0){
			underblockID = 89;
		}
		this.underblock = Material.getMaterial(underblockID);
		if (!configFile.exists()){
			this.out("Configuration file does not exist.  Creating "+configFile.getAbsolutePath());
			if (!configDir.exists()){
				configDir.mkdir();
			}
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				this.crap("IOError while creating config file: "+e.getMessage());
			}
			config.save();
		}
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
