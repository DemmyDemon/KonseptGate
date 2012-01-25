package com.webkonsept.bukkit.konseptgate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.block.Block;

public class KGateList {
	private KG plugin;
	private File source;
	
	protected HashMap<Location,KGate> gateLocation 	= new HashMap<Location,KGate>();
	protected HashMap<String,KGate> gateName		= new HashMap<String,KGate>();
	protected ArrayList<KGate> gates 				= new ArrayList<KGate>();
	protected int gateWorldNotLoaded				= 0;
	
	KGateList (KG instance, File gateList){
		plugin = instance;
		source = gateList;
	}
	public void add(String name,Location location,String target){
		KGate newGate = new KGate(plugin,name,location,target);
		gates.add(newGate);
		gateName.put(newGate.getName(),newGate);
		gateLocation.put(newGate.getLocation(),newGate);
		if (target.length() == 0){
			target = "[not set]";
		}
		newGate.createBlock(plugin.underblock);
		plugin.verbose("New gate created: "+name+".  Target is "+target+".");
		save();
	}
	public void add(String name,Location location){
		this.add(name,location,plugin.defaultTarget);
	}
	public void move(String name,Location to){
		KGate gate = gateName.get(name);
		gate.eraseBlock();
		gateLocation.remove(gate.getLocation());
		gate.setLocation(to);
		gate.setYaw(to.getYaw());
		gateLocation.put(gate.getLocation().clone(),gate);
		gate.createBlock(plugin.underblock);
		save();
		plugin.verbose("Moved gate '"+name+"' to "+to.getX()+","+to.getY()+","+to.getZ());
	}
	public void remove(String name){
		if (name == null) return;
		KGate doomed = gateName.get(name);
		if (doomed != null){
			doomed.eraseBlock();
			gateName.remove(name);
			gateLocation.remove(doomed.getLocation());
			gates.remove(doomed);
			save();
			plugin.verbose("Removed gate "+name);
		}
		else {
			plugin.verbose("Attempt at removing NULL gate "+name+" averted");
		}
	}
	public boolean isGate(Location location){
		return gateLocation.containsKey(location);
	}
	public boolean isGate(Block block){
		return isGate(block.getLocation());
	}
	public void remove(KGate gate){
		if (gate == null) return;
		remove(gate.getName());
	}
	public void remove(Location location){
		if (location == null) return;
		remove(gateLocation.get(location).getName());
	}
	public int load(){
		int loadedGates = 0;
		if (source.exists()){
			plugin.verbose("Loading gates from "+source.getAbsolutePath());
			gates.clear();
			gateLocation.clear();
			gateName.clear();
			BufferedReader in = null;
			String line = "";
			int gateCount = 0;
			try {
				in = new BufferedReader(new FileReader(source));
				line = in.readLine();
			}
			catch (FileNotFoundException e) {
				// Stupid Java... We'll NEVER GET HERE, unless the file stops existing in under a nanosecond!
				e.printStackTrace();
				plugin.problem("Gates file went away!  This is a MAJOR problem.");
				return gateCount;
			}
			catch (IOException e){
				e.printStackTrace();
				plugin.problem("There was an IOException while reading the very first line of your gates file.  VERY VERY BAD!");
				return gateCount;
			}
				
				
				
			while (line != null){
				try {
					KGate thisGate = new KGate(plugin,line);
					if (thisGate != null){
						gates.add(thisGate);
						Location thisGateLocation = thisGate.getLocation();
						if (thisGateLocation != null){
							gateLocation.put(thisGateLocation,thisGate);
							thisGate.createBlock(plugin.underblock);
						}
						gateName.put(thisGate.getName(),thisGate);
						loadedGates++;
						if (thisGate.getTargetName().length() > 0){
							plugin.verbose("Loaded "+thisGate.getName()+", linked to "+thisGate.getTargetName());
						}
						else {
							plugin.verbose("Loaded "+thisGate.getName()+", which is unlinked.");
						}
						gateCount++;
					}
					line = in.readLine();
				}
				catch (IOException e) {
					e.printStackTrace();
					plugin.problem("IOException reading gate file "+source.getAbsolutePath());
					if (!source.canRead()){
						plugin.problem("CAN'T READ FROM "+source.getAbsolutePath());
					}
				} catch (ParseException e) {
					e.printStackTrace();
					plugin.problem("Parse Exception while reading gate file: "+e.getMessage());
				}
			}
			
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
				plugin.problem("Oh no!  There was an IOException closing your gates.txt file.  The file is probably lost :-(");
			}
			
			plugin.verbose(gateCount+" konsept gates!");
		}
		else {
			plugin.out("KonseptGateFile "+source.getAbsolutePath()+" does not exist:  Creating new!");
			try {
				source.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				plugin.problem("IOException creating gate file "+source.getAbsolutePath());
				if (!source.canWrite()){
					plugin.problem("CAN'T WRITE TO "+source.getAbsolutePath());
				}
			}
		}
		return loadedGates;
	}
	public void save(){
		plugin.verbose("Saving gates");
		try {
			if (!source.exists()){
				source.createNewFile();
			}
			BufferedWriter out = new BufferedWriter(new FileWriter(source));
			for (KGate gate : gates){
				out.write(gate.toString());
				out.newLine();
			}
			out.close();
		}
		catch (IOException e){
			e.printStackTrace();
			plugin.problem("IOException writing gate file "+source.getAbsolutePath());
			if (!source.canWrite()){
				plugin.problem("CAN'T WRITE TO "+source.getAbsolutePath());
			}
		}
		plugin.verbose("Saved!");
	}
	public void save(File alternativeGateFile){
		source = alternativeGateFile;
		save();
	}
	
}
