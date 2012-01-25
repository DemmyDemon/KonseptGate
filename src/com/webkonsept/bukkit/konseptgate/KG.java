package com.webkonsept.bukkit.konseptgate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class KG extends JavaPlugin {
	private Logger log = Logger.getLogger("Minecraft");
	private KGPlayerListener playerListener = new KGPlayerListener(this);
	private KGBlockListener blockListener = new KGBlockListener(this);
	private KGEntityListener entityListener = new KGEntityListener(this);
	private KGWorldListener worldListener = new KGWorldListener(this);
	protected KGateList	gates;
	protected int gatesPerPage = 5;
	protected boolean fireEffect = true;
	
	protected HashSet<Player> ignored = new HashSet<Player>();
	
	// Settings
	protected boolean verbose = true;
	protected Material underblock = Material.NETHER_BRICK;
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
		this.loadConfig(false);
	 	gates = new KGateList(this,new File(this.getDataFolder(),"gates.txt"));
		int gateNumber = gates.load();
		PluginManager pm = this.getServer().getPluginManager();
		/* OLD!
		pm.registerEvent(Event.Type.BLOCK_BREAK,blockListener,Priority.Normal,this);
		pm.registerEvent(Event.Type.BLOCK_PHYSICS,blockListener,Priority.Normal,this);
		pm.registerEvent(Event.Type.BLOCK_PISTON_EXTEND,blockListener,Priority.Normal,this);
		pm.registerEvent(Event.Type.BLOCK_PISTON_RETRACT,blockListener,Priority.Normal,this);
		*/
		pm.registerEvents(blockListener,this);
		
		/* OLD!
		pm.registerEvent(Event.Type.PLAYER_MOVE,playerListener,Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT,playerListener,Priority.High,this);
		*/
		pm.registerEvents(playerListener,this);
		
		/* OLD
		pm.registerEvent(Event.Type.ENTITY_EXPLODE,entityListener,Priority.High,this);
		pm.registerEvent(Event.Type.WORLD_LOAD,worldListener,Priority.Normal,this);
		*/
		pm.registerEvents(worldListener,this);
		pm.registerEvents(entityListener,this);
		this.out("Enabled ("+gateNumber+" gates)");
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
					String lookFor = null;
					int gateNum = gates.gates.size();
					if (args.length >= 2){
						lookFor = args[1];
						player.sendMessage(gateNum+" gate"+plural(gateNum)+", looking for gates matching "+lookFor);
					}
					else {
						player.sendMessage(gateNum+" gate"+plural(gateNum));
					}
					ArrayList<String> gateList = new ArrayList<String>();
					for (KGate gate : gates.gates){
						if (lookFor != null && gate.getName().contains(lookFor)){
							gateList.add(gate.getName().replace(lookFor,ChatColor.RED+lookFor+ChatColor.WHITE)+" -> "+gate.getTargetName());
						}
						else if (lookFor != null && gate.getTargetName().contains(lookFor)){
							gateList.add(gate.getName()+" -> "+gate.getTargetName().replace(lookFor,ChatColor.RED+lookFor+ChatColor.WHITE));
						}
						else if (lookFor == null){
							gateList.add(gate.getName()+" -> "+gate.getTargetName());
						}
					}
					Object[] sorted = gateList.toArray();
					Arrays.sort(sorted);
					
					for (Object gate : sorted ){
						if (gate instanceof String){
							player.sendMessage((String)gate);
						}
					}
				}
				else {
					player.sendMessage(ChatColor.RED+"Permission denied!");
				}
			}
			else if (args[0].equalsIgnoreCase("reload")){
				validCommand = true;
				if (permit(player,"konseptgate.command.reload")){
					loadConfig(true);
					int gatesLoaded = gates.load();
					player.sendMessage(ChatColor.GOLD+"KonseptGate reloaded!");
					player.sendMessage(ChatColor.GOLD+"   "+gatesLoaded+" gates found.");
					
					player.sendMessage(ChatColor.GOLD+"   Underblock is "+underblock.toString());
					if (defaultTarget.length() > 0){
						player.sendMessage(ChatColor.GOLD+"   Default target is '"+defaultTarget+"'");
					}
					else {
						player.sendMessage(ChatColor.GOLD+"   No default target set");
					}
					
					if (this.fireEffect){
						player.sendMessage(ChatColor.GOLD+"   Fire effect is ON");
					}
					else {
						player.sendMessage(ChatColor.GOLD+"   Fire effect is OFF");
					}
					if (this.verbose){
						player.sendMessage(ChatColor.GOLD+"   Console verbosity is ON");
					}
					else {
						player.sendMessage(ChatColor.GOLD+"   Console verbosity is OFF");
					}
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
						getServer().getScheduler().scheduleSyncDelayedTask(this, new KGPlayerTeleport(player,destination,playerListener.frozen,this.fireEffect),1);
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
		return player.hasPermission(permission);
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
	public void loadConfig(boolean forceReload) {
		File configFile = new File(getDataFolder(),"config.yml");
		File oldFile = new File(getDataFolder(),"settings.yml");
		FileConfiguration config = getConfig();
		
		if (forceReload){
			try {
				config.load(configFile);
			} catch (FileNotFoundException e) {
				crap("Forced reload of "+configFile.getAbsolutePath()+" FAILED:  File not found!");
			} catch (IOException e) {
				crap("Forced reload of "+configFile.getAbsolutePath()+" FAILED:  "+e.getMessage());
			} catch (InvalidConfigurationException e) {
				crap("Forced reload of "+configFile.getAbsolutePath()+" FAILED:  Invalid YAML!");
			}
		}
		
		if (oldFile.exists()){
			try {
				config.load(oldFile);
				
				if (oldFile.delete()){
					out("Old configuration file found, read and deleted.  New filename is "+configFile.getAbsolutePath());
				}
				else {
					crap("Old configuration file found and read BUT COULD NOT BE DELETED!  Suggest manual deletion of "+configFile.getAbsolutePath());
				}
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				crap("Okay, something REALLY odd happened.  A file I know to exist just suddenly went away: "+oldFile.getAbsolutePath());
			} catch (IOException e) {
				e.printStackTrace();
				crap("IOException while reading your old config file ("+oldFile.getAbsolutePath()+")");
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
				crap("Old config file ("+oldFile.getAbsolutePath()+") has broken YAML in it.");
			}
		}

		
		this.verbose = config.getBoolean("verbose", false);
		babble("Verbose mode ON!");
		
		this.defaultTarget = config.getString("defaultTarget","");
		if (defaultTarget.isEmpty()){
			babble("No default target gate (which is fine, really!)");
		}
		else {
			babble("Default target gate is "+defaultTarget);
		}
		
		this.fireEffect = config.getBoolean("fireEffect", true);
		if (fireEffect){
			babble("Fire effect is enabled");
		}
		else {
			babble("Fire effect is disabled");
		}
		
		int underblockID = config.getInt("underblockID",89);  // ID 89 is Glowstone
		Material useUnderblock = Material.getMaterial(underblockID);
		if (useUnderblock == null){
			crap("Invalid underblockID in config, using STONE");
			this.underblock = Material.STONE;
		}
		else {
			this.underblock = useUnderblock;
		}
		babble("Underblock is "+underblock.toString());
		
		
		if (!configFile.exists()){
			this.out("Configuration file does not exist.  Creating "+configFile.getAbsolutePath());
			try {
				config.save(configFile);
			} catch (IOException e) {
				e.printStackTrace();
				crap("Failed saving configuration file!  OSHIT!!!");
			}
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
