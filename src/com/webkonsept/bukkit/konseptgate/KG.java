package com.webkonsept.bukkit.konseptgate;

import com.webkonsept.minecraft.boilerplate.KonseptUpdate;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Logger;

public class KG extends JavaPlugin {
    private static final String pluginName = "KonseptGate";
    private static String pluginVersion = "???";
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
    protected boolean checkUpdates = true;
    protected Material underblock = Material.NETHER_BRICK;
    protected String defaultTarget = "";
    protected String defaultCommand = "";
    protected boolean missingDestinationMessage = true;

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
        pluginVersion = this.getDescription().getVersion();
        this.loadConfig();
        if (checkUpdates){
            KonseptUpdate.check(pluginName, pluginVersion);
        }
        gates = new KGateList(this,new File(this.getDataFolder(),"gates.txt"));
        int gateNumber = gates.load();

        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(blockListener,this);
        pm.registerEvents(playerListener,this);
        pm.registerEvents(worldListener,this);
        pm.registerEvents(entityListener,this);

        this.out("Enabled ("+gateNumber+" gates)");
    }
    
    public void registerPermission(String gateName) {
    	this.getServer().getPluginManager().addPermission(new Permission("konseptgate.gate."+gateName));
    }
    public void unRegisterPermission(String gateName) {
    	this.getServer().getPluginManager().removePermission(new Permission("konseptgate.gate."+gateName));
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
                    getServer().getScheduler().runTaskLaterAsynchronously(this, new KGPlayerInTransit(player,playerListener.inTransit),10);
                    Location newLocation = player.getLocation().clone();

                    if (args.length == 2){
                        gates.add(gateName, newLocation);
                    }
                    else if (args.length == 3){
                        gates.add(gateName,newLocation,args[2],"");
                    }
                    else if (args.length == 4){
                        gates.add(gateName,newLocation,args[2],args[3]);
                    }
                    sender.sendMessage("Gate '"+gateName+"' created!");
                }
                else {
                    player.sendMessage(ChatColor.RED+"Permission denied!");
                }
            }
            else if (args[0].equalsIgnoreCase("command")){
                validCommand = true;

                if (args.length < 2){
                    sender.sendMessage("You forgot the name of the gate!");
                    return false;
                }
                
                String gateName = args[1];
                if (permit(player,"konseptgate.command.command")){

                    String newGateCommand = "";
                    if (args.length >= 3){
                        StringBuilder sb = new StringBuilder();
                        for (int i = 2; i < args.length; i++){
                            if (i > 2){
                                sb.append(" ");
                            }
                            sb.append(args[i]);
                        }
                        newGateCommand = sb.toString();
                    }
                    // If it's exactly 2, that means we remove the command
                    else if (args.length < 2){
                        sender.sendMessage("Setting the command takes at least three arguments:  /kg command [gate] [command]");
                        sender.sendMessage("Omitting the command removes any set command.");
                        return false;
                    }

                    if (gates.gateName.containsKey(gateName)){
                        gates.gateName.get(gateName).setCommand(newGateCommand);
                        gates.save();
                        if (newGateCommand.isEmpty()){
                            player.sendMessage(gateName+" will not issue a command when used.");
                        }
                        else {
                            player.sendMessage("Users of "+gateName+" will now do /"+newGateCommand);
                        }
                    }
                    else {
                        player.sendMessage(gateName+"? Sorry, no such gate.");
                    }
                }
                else {
                    player.sendMessage("Sorry, access denied.");
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
                        getServer().getScheduler().runTaskLaterAsynchronously(this, new KGPlayerInTransit(player,playerListener.inTransit),10);
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
                    int page = 0;

                    if (args.length == 2){

                        // This try/catch pair gets on my nerves, but I must abandon my Perl ways and be one with the Java.
                        // ...or go completely insane, which is pretty much the same thing.
                        // TODO:  Murder whomever came up with this as being "better" than string matching.
                        try {
                            page = Integer.parseInt(args[1]);
                        }
                        catch (NumberFormatException e){
                            lookFor = args[1];
                        }
                        if (page < 0) page = 0;

                        if (page == 0){
                            player.sendMessage(ChatColor.GOLD+""+gateNum+" gate"+plural(gateNum)+", looking for gates matching "+lookFor);
                        }
                        else {
                            int gatePages = gateNum + ( gateNum % gatesPerPage);
                            int pages = (gatePages/gatesPerPage);
                            player.sendMessage(ChatColor.GOLD+""+gateNum+" gate"+plural(gateNum)+", page "+page+" of "+pages);
                        }
                    }
                    else if (args.length > 2){
                        player.sendMessage(ChatColor.RED+"You can't both search and paginate, sorry.");
                        return true;
                    }
                    else {
                        player.sendMessage(ChatColor.GOLD+""+gateNum+" gate"+plural(gateNum));
                    }
                    ArrayList<String> gateList = new ArrayList<String>();

                    for (KGate gate : gates.gates){
                        String gateCommand = gate.getCommand();
                        if (gateCommand.isEmpty()){
                            gateCommand = "[no command]";
                        }
                        if (lookFor != null && gate.getName().contains(lookFor)){
                            gateList.add(String.format("%s -> %s (%s)",gate.getName().replace(lookFor,ChatColor.RED+lookFor+ChatColor.RESET),gate.getTargetName(),gateCommand));
                        }
                        else if (lookFor != null && gate.getTargetName().contains(lookFor)){
                            gateList.add(String.format("%s -> %s (%s)",gate.getName(),gate.getTargetName().replace(lookFor,ChatColor.RED+lookFor+ChatColor.RESET),gateCommand));
                        }
                        else if (lookFor == null){
                            gateList.add(String.format("%s -> %s (%s)", gate.getName(),gate.getTargetName(),command));
                        }
                    }
                    Object[] sorted = gateList.toArray();
                    Arrays.sort(sorted);

                    if (page > 0){
                        int begin = (gatesPerPage*page)-gatesPerPage;
                        int end = begin+gatesPerPage;
                        if (sorted.length < begin+1){
                            player.sendMessage(ChatColor.RED+"There is no page "+page+"!");
                            return true;
                        }
                        if (end+1 > sorted.length){
                            end = sorted.length-1;
                        }
                        sorted = Arrays.copyOfRange(sorted,begin,end);
                    }

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
                    loadConfig();
                    int gatesLoaded = gates.load();
                    int pages = (gatesLoaded + ( gatesLoaded % gatesPerPage) ) / gatesPerPage;
                    player.sendMessage(ChatColor.GOLD+"KonseptGate reloaded!");
                    player.sendMessage(ChatColor.GOLD+"   "+gatesLoaded+" gates found.");
                    player.sendMessage(ChatColor.GOLD+"   Listing "+gatesPerPage+" gates per page ("+pages+" pages)");

                    player.sendMessage(ChatColor.GOLD+"   Underblock is "+underblock.toString());
                    if (defaultTarget.length() > 0){
                        player.sendMessage(ChatColor.GOLD+"   Default target is '"+defaultTarget+"'");
                    }
                    else {
                        player.sendMessage(ChatColor.GOLD+"   No default target set");
                    }
                    if (defaultCommand.length() > 0){
                        player.sendMessage(ChatColor.GOLD+"   Default command is '"+defaultCommand+"'");
                    }
                    else {
                        player.sendMessage(ChatColor.GOLD+"   No default command set");
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
                        getServer().getScheduler().runTaskLaterAsynchronously(this, new KGPlayerInTransit(player,playerListener.inTransit),20);
                        getServer().getScheduler().scheduleSyncDelayedTask(this, new KGPlayerTeleport(player,null,destination,playerListener.frozen,this.fireEffect),1);
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
                    return false;
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
            else if(args[0].equalsIgnoreCase("togglepermission") || args[0].equalsIgnoreCase("toggleperm")) {
            	if(args.length < 2) {
                    sender.sendMessage("You forgot the name of the gate!");
                    return false;
            	}
            	String gateName = args[1];
            	if(permit(player,"konseptgate.command.toggleperm")) {
            		if(gates.gateName.containsKey(gateName)) {
            			KGate gate = gates.gateName.get(gateName);
            			gate.togglePerm();
            			if(gate.getPermission()) 
            				player.sendMessage(gate.getName()+" now requires permission to use!");
            			else
            				player.sendMessage(gate.getName()+" now does not require permission to use!");
            				
            		}
                    else {
                        player.sendMessage(gateName+"?  No such gate.");
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
        log.info("[" +pluginName+ " " + pluginVersion + "] " + message);
    }
    public void problem(String message){
        log.severe("[" +pluginName+ " " + pluginVersion + "] " + message);
    }
    public void verbose(String message){
        if (!this.verbose) return;
        log.info("[" +pluginName+ " " + pluginVersion + " VERBOSE] " + message);
    }
    private void loadConfig(){
        reloadConfig();
        getConfig().options().copyDefaults(true);
        this.verbose = getConfig().getBoolean("verbose",false);
        verbose("Verbose mode!  Spammy as hell!");

        this.checkUpdates = getConfig().getBoolean("checkUpdates",true);
        if (checkUpdates){
            verbose("Will call home to check for updates");
        }

        this.fireEffect = getConfig().getBoolean("fireEffect", true);
        if (fireEffect){
            verbose("fireEffect is ON!");
        }
        else {
            verbose("fireEffect is OFF!");
        }
        int underblockID = getConfig().getInt("underblockID",42);

        Material useUnderblock = Material.getMaterial(underblockID);
        if (useUnderblock == null){
            problem("Invalid underblockID in config, using STONE");
            this.underblock = Material.STONE;
        }
        else {
            this.underblock = useUnderblock;
            verbose("underblockID("+underblockID+") is "+underblock.toString());
        }

        this.gatesPerPage = getConfig().getInt("gatesPerPage",9);
        verbose("Listing "+gatesPerPage+" gates per page");

        this.defaultTarget = getConfig().getString("defaultTarget","");
        if (defaultTarget.isEmpty()){
            verbose("No default target gate (which is fine, really!)");
        }
        else {
            verbose("Default target gate is "+defaultTarget);
        }
        this.defaultCommand = getConfig().getString("defaultCommand","");
        if (defaultCommand.isEmpty()){
            verbose("No default command (which is fine, really!)");
        }
        else{
            verbose("Default command is '"+defaultCommand+"'");
        }
        this.missingDestinationMessage = getConfig().getBoolean("missingDestinationMessage",true);
        if (missingDestinationMessage){
            verbose("Missing destination message is ON");
        }
        else {
            verbose("Missing destination message is OFF");
        }
        saveConfig();
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
