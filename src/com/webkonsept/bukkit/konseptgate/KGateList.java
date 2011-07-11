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

public class KGateList {
	private KG plugin;
	private File source;
	
	protected HashMap<Location,KGate> gateLocation 	= new HashMap<Location,KGate>();
	protected HashMap<String,KGate> gateName		= new HashMap<String,KGate>();
	protected ArrayList<KGate> gates 				= new ArrayList<KGate>();
	
	KGateList (KG instance, File gateList){
		plugin = instance;
		source = gateList;
	}
	public void add(String name,Location location,String target){
		KGate newGate = new KGate(name,location,target);
		newGate.createBlock(plugin.underblock);
		gates.add(newGate);
		gateName.put(newGate.getName(),newGate);
		gateLocation.put(newGate.getLocation(),newGate);
		if (target.length() == 0){
			target = "[not set]";
		}
		plugin.babble("New gate created: "+name+".  Target is "+target+".");
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
		plugin.babble("Moved gate '"+name+"' to "+to.getX()+","+to.getY()+","+to.getZ());
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
			plugin.babble("Removed gate "+name);
		}
		else {
			plugin.babble("Attempt at removing NULL gate "+name+" averted");
		}
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
			plugin.babble("Loading gates from "+source.getAbsolutePath());
			gates.clear();
			gateLocation.clear();
			gateName.clear();
			try {
				BufferedReader in = new BufferedReader(new FileReader(source));
				String line = in.readLine();
				int gateCount = 0;
				while (line != null){
					KGate thisGate = new KGate(line);
					if (thisGate != null){
						thisGate.createBlock(plugin.underblock);
						gates.add(thisGate);
						gateLocation.put(thisGate.getLocation(),thisGate);
						gateName.put(thisGate.getName(),thisGate);
						loadedGates++;
						if (thisGate.getTargetName().length() > 0){
							plugin.babble("Loaded "+thisGate.getName()+", linked to "+thisGate.getTargetName());
						}
						else {
							plugin.babble("Loaded "+thisGate.getName()+", which is unlinked.");
						}
						gateCount++;
					}
					line = in.readLine();
				}
				in.close();
				plugin.babble(gateCount+" konsept gates!");
			}
			catch (FileNotFoundException e) {
				// Stupid Java... We'll NEVER GET HERE, unless the file stops existing in under a nanosecond!
				e.printStackTrace();
				plugin.crap("How the hell did THAT happen?!");
			}
			catch (IOException e) {
				e.printStackTrace();
				plugin.crap("IOException reading gate file "+source.getAbsolutePath());
				if (!source.canRead()){
					plugin.crap("CAN'T READ FROM "+source.getAbsolutePath());
				}
			} catch (ParseException e) {
				e.printStackTrace();
				plugin.crap("Parse Exception while reading gate file: "+e.getMessage());
			}
		}
		else {
			plugin.out("KonseptGateFile "+source.getAbsolutePath()+" does not exist:  Creating new!");
			try {
				source.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				plugin.crap("IOException creating gate file "+source.getAbsolutePath());
				if (!source.canWrite()){
					plugin.crap("CAN'T WRITE TO "+source.getAbsolutePath());
				}
			}
		}
		return loadedGates;
	}
	public void save(){
		plugin.babble("Saving gates");
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
			plugin.crap("IOException writing gate file "+source.getAbsolutePath());
			if (!source.canWrite()){
				plugin.crap("CAN'T WRITE TO "+source.getAbsolutePath());
			}
		}
		plugin.babble("Saved!");
	}
	public void save(File alternativeGateFile){
		source = alternativeGateFile;
		save();
	}
	
}
