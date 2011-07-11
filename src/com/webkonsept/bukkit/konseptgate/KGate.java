package com.webkonsept.bukkit.konseptgate;

import java.text.ParseException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class KGate {
	private String name;
	private String targetName;
	private Location location;
	private int yaw;
	
	private String split = ",";
	private int expectedNumberOfFields = 7;
	
	KGate (String gateString) throws ParseException {
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
			this.location = new Location(world,x,y,z);
			this.yaw = yaw;
			this.location = KGate.saneLocation(this.location);
		}
		catch (NumberFormatException e){
			throw new ParseException("Invalid integer found in gateString "+gateString,0);
		}
	}
	KGate (String name, Location location, String targetName){
		this.name = name.replace(split,"");
		this.targetName = targetName.replace(split,"");
		this.yaw = cardinalYaw(location.getYaw());
		this.location = saneLocation(location);
	}
	KGate (String name, Location location){
		this.name = name.replace(split,"");
		this.yaw = cardinalYaw(location.getYaw());
		this.location = saneLocation(location);
		this.targetName = "";
	}
	public void createBlock(Material underblock){
		Block block = location.getBlock();
		block.setType(Material.STONE_PLATE);
		block.getFace(BlockFace.DOWN).setType(underblock);
		block.getFace(BlockFace.UP).setType(Material.AIR);
		Block target = location.getBlock().getFace(faceFromYaw(yaw));
		target.setType(Material.AIR);
		target.getFace(BlockFace.UP).setType(Material.AIR);
	}
	public void eraseBlock(){
		Block block = location.getBlock();
		block.setType(Material.AIR);
		Block below = block.getFace(BlockFace.DOWN);
		below.setType(below.getFace(BlockFace.NORTH).getType());
		block.getFace(BlockFace.UP).setType(Material.AIR);
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
		return location;
	}
	public Location getLocationForTeleport() {
		Block teleportTo = location.getBlock().getFace(faceFromYaw(yaw));
		//teleportTo.setType(Material.AIR);
		//teleportTo.getFace(BlockFace.UP).setType(Material.AIR);
		Location forTP = teleportTo.getLocation().clone();
		forTP.setX(forTP.getX()+0.5);
		forTP.setZ(forTP.getZ()+0.5);
		forTP.setYaw(this.yaw);
		return forTP;
	}
	public BlockFace faceFromYaw(int yaw){
		switch (cardinalYaw(yaw)){
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
		yaw = Math.abs(yaw % 360);
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
		String worldName = location.getWorld().getName();
		int x = (int)location.getBlockX();
		int y = (int)location.getBlockY();
		int z = (int)location.getBlockZ();
		return name+split+worldName+split+x+split+y+split+z+split+yaw+split+targetName;
	}
	public void setYaw(float newYaw) {
		this.yaw = cardinalYaw(newYaw);
	}
	public int getYaw() {
		return yaw;
	}
}
