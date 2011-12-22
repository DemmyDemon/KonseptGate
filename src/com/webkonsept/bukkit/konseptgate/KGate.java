package com.webkonsept.bukkit.konseptgate;

import java.text.ParseException;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class KGate {
	private KG plugin;
	private String name;
	private String deferredLocation;
	private String targetName;
	private Location location;
	private int yaw;
	
	private String split = ",";
	private int expectedNumberOfFields = 7;
	
	private static HashSet<Material> nonHinderingBlocks = new HashSet<Material>(){
		private static final long serialVersionUID = 12345L;  // Just to squash the damn warning :-P

		{
			// Air is the very definition of a non-hindering block
			add(Material.AIR);
			
			// Stuff hanging on the walls that we can walk through
			add(Material.WALL_SIGN);
			add(Material.SIGN_POST);
			add(Material.TORCH);
			add(Material.LADDER);
			
			// Plants that we phase through
			add(Material.DEAD_BUSH);
			add(Material.VINE);
			add(Material.YELLOW_FLOWER);
			add(Material.RED_ROSE);
			add(Material.SAPLING);
			add(Material.SUGAR_CANE_BLOCK);
			add(Material.LONG_GRASS);
			add(Material.BROWN_MUSHROOM);
			add(Material.RED_MUSHROOM);
			
			// Redstone/mechanism ignored as blocks
			add(Material.REDSTONE_WIRE);
			add(Material.REDSTONE_TORCH_OFF);
			add(Material.REDSTONE_TORCH_ON);
			add(Material.STONE_BUTTON);
			add(Material.STONE_PLATE);
			add(Material.WOOD_PLATE);
			add(Material.DIODE_BLOCK_OFF);
			add(Material.DIODE_BLOCK_ON);
			
			// Rails are ignored, too.
			add(Material.DETECTOR_RAIL);
			add(Material.RAILS);
		}
		
	};
	
	KGate (KG instance,String gateString) throws ParseException {
		this.plugin = instance;
		if (gateString == null){
			throw new ParseException("gateString is null.  Very bad.",0);
		}
		String[] gateSpec = gateString.split(split);
		
		if (gateSpec.length != expectedNumberOfFields && gateSpec.length != expectedNumberOfFields - 1){
			throw new ParseException("Invalid number of fields in gateString("+gateString+"):  Found "+gateSpec.length+", expected "+expectedNumberOfFields,0);
		}
		
		try {
			String name 		= gateSpec[0];
			String worldName	= gateSpec[1];
			int x 				= Integer.parseInt(gateSpec[2]);
			int y 				= Integer.parseInt(gateSpec[3]);
			int z 				= Integer.parseInt(gateSpec[4]);
			int yaw 			= Integer.parseInt(gateSpec[5]);
			String target 		= "";
			if (gateSpec.length == expectedNumberOfFields){
				target = gateSpec[6];
			}
			
			this.name = name;
			this.targetName = target;
			World world = Bukkit.getServer().getWorld(worldName);
			this.yaw = yaw;
			if (world != null){
				this.location = KGate.saneLocation(new Location(world,x,y,z));
			}
			else {
				plugin.babble("World '"+worldName+"' is not loaded yet:  Deferring location resolution for KonseptGate "+name);
				deferredLocation = worldName+split+x+split+y+split+z;
				plugin.gates.gateWorldNotLoaded++;
			}
			
		}
		catch (NumberFormatException e){
			throw new ParseException("Invalid integer found in gateString "+gateString,0);
		}
	}
	KGate (KG instance,String name, Location location, String targetName){
		this.plugin = instance;
		this.name = name.replace(split,"");
		this.targetName = targetName.replace(split,"");
		this.yaw = cardinalYaw(location.getYaw());
		this.location = saneLocation(location);
	}
	KGate (KG instance,String name, Location location){
		this.plugin = instance;
		this.name = name.replace(split,"");
		this.yaw = cardinalYaw(location.getYaw());
		this.location = saneLocation(location);
		this.targetName = "";
	}
	public void createBlock(Material underblock){
		Location blockLocation = getLocation();
		if (blockLocation == null) return;
		Block block = blockLocation.getBlock();
		block.setType(Material.STONE_PLATE);
		block.getRelative(BlockFace.DOWN).setType(underblock);
		Block[] clearBlocks = {
			block.getRelative(faceFromYaw(yaw)),
			block.getRelative(faceFromYaw(yaw)).getRelative(BlockFace.UP),
			block.getRelative(BlockFace.UP),
		};
		for (Block clearBlock : clearBlocks){
			if (!nonHinderingBlocks.contains(clearBlock.getType())){
				clearBlock.setType(Material.AIR);
			}
		}
		
	}
	public void eraseBlock(){
		Block block = location.getBlock();
		block.setType(Material.AIR);
		Block below = block.getRelative(BlockFace.DOWN);
		below.setType(below.getRelative(BlockFace.NORTH).getType());
	}
	public void setName(String name) {
		name.replaceAll(split,"");
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setLocation(Location to) {
		this.location = saneLocation(to);
	}
	public Location getLocation() {
		if (location == null && deferredLocation != null){
			String[] locationPeices = deferredLocation.split(split);
			String worldName	= locationPeices[0];
			int x 				= Integer.parseInt(locationPeices[1]);
			int y 				= Integer.parseInt(locationPeices[2]);
			int z 				= Integer.parseInt(locationPeices[3]);
			
			World world = Bukkit.getServer().getWorld(worldName);
			if (world != null){
				plugin.babble("World '"+worldName+"' wasn't here before, but it is now!  KonseptGate "+name+" should be fine.");
				this.location = KGate.saneLocation(new Location(world,x,y,z));
				this.plugin.gates.gateLocation.put(this.location,this);
				this.plugin.gates.gateName.put(this.name,this);
				createBlock(plugin.underblock);
				this.plugin.gates.gateWorldNotLoaded--;
				plugin.babble(this.plugin.gates.gateWorldNotLoaded+" gates left woth no world to call their own.");
			}
			else {
				plugin.babble("World '"+worldName+"' is not loaded yet, and KonseptGate '"+name+"' is in it!");
			}
		}
		
		return this.location;
	}
	public Location getLocationForTeleport() {
		Block teleportTo = getLocation().getBlock().getRelative(faceFromYaw(yaw));
		Location forTP = teleportTo.getLocation().clone();
		forTP.setX(forTP.getX()+0.5);
		forTP.setZ(forTP.getZ()+0.5);
		forTP.setYaw(this.yaw);
		return forTP;
	}
	public BlockFace faceFromYaw(int yaw){
		int cYaw = cardinalYaw(yaw);
		switch (cYaw){
			case 	0: 		return BlockFace.WEST; 
			case 	90: 	return BlockFace.NORTH; 
			case 	180: 	return BlockFace.EAST; 
			case 	270: 	return BlockFace.SOUTH; 
			default : 		return BlockFace.SOUTH; 
		}
	}
	public void setTargetName(String targetName) {
		targetName.replaceAll(split,"");
		this.targetName = targetName;
	}
	public String getTargetName() {
		return targetName;
	}
	public static Location saneLocation(Location insane){
		Location saneLocation = insane.clone();
		saneLocation.setX(saneLocation.getBlockX());
		saneLocation.setY(saneLocation.getBlockY());
		saneLocation.setZ(saneLocation.getBlockZ());
		saneLocation.setYaw(0f);
		saneLocation.setPitch(0f);
		return saneLocation;
	}
	private static int cardinalYaw (float yaw){
		int cardinalYaw = -1;
		yaw = (yaw % 360 + 360) % 360;
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
		return cardinalYaw;
	}
	public String toString() {
		location = getLocation();
		if (location != null){
			String worldName = location.getWorld().getName();
			int x = (int)location.getBlockX();
			int y = (int)location.getBlockY();
			int z = (int)location.getBlockZ();
			return name+split+worldName+split+x+split+y+split+z+split+yaw+split+targetName;
		}
		else {
			return name+split+deferredLocation+split+yaw+split+targetName;
		}
	}
	public void setYaw(float newYaw) {
		this.yaw = cardinalYaw(newYaw);
	}
	public int getYaw() {
		return yaw;
	}
}
