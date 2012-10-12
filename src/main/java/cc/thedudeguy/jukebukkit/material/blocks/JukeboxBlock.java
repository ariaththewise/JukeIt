/**
 * This file is part of JukeBukkit
 *
 * Copyright (C) 2011-2012  Chris Churchwell
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cc.thedudeguy.jukebukkit.material.blocks;

import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.block.SpoutBlock;
import org.getspout.spoutapi.block.design.GenericCubeBlockDesign;
import org.getspout.spoutapi.inventory.SpoutItemStack;
import org.getspout.spoutapi.material.block.GenericCustomBlock;
import org.getspout.spoutapi.player.SpoutPlayer;

import cc.thedudeguy.jukebukkit.JukeBukkit;
import cc.thedudeguy.jukebukkit.database.DiscData;
import cc.thedudeguy.jukebukkit.database.RecordPlayerData;
import cc.thedudeguy.jukebukkit.material.Items;
import cc.thedudeguy.jukebukkit.material.blocks.designs.RPNeedle;
import cc.thedudeguy.jukebukkit.material.items.BurnedDisc;
import cc.thedudeguy.jukebukkit.permission.CraftPermissible;
import cc.thedudeguy.jukebukkit.permission.UsePermissible;
import cc.thedudeguy.jukebukkit.sound.Sound;
import cc.thedudeguy.jukebukkit.sound.SoundEffect;
import cc.thedudeguy.jukebukkit.util.Debug;

public abstract class JukeboxBlock extends GenericCustomBlock implements CraftPermissible, UsePermissible {

	public JukeboxBlock(String name) {
		super(JukeBukkit.instance, name);
		setBlockDesign(getCustomBlockDesign());
		setRecipe();
	}
	
	public abstract void setRecipe();
	
	public abstract int getRange();
	
	public abstract boolean canRedstoneActivate();
	
	public abstract GenericCubeBlockDesign getCustomBlockDesign();
	
	@Override
	public String getCraftPermission() {
		return "jukebukkit.craft.jukebox";
	}
	
	@Override
	public String getUsePermission() {
		return "jukebukkit.use.jukebox";
	}
	
	public boolean canPlaceBlockAt(World arg0, int arg1, int arg2, int arg3) {
		//block is placeable.
		return true;
	}
	
	public boolean canPlaceBlockAt(World arg0, int arg1, int arg2, int arg3, BlockFace arg4) {
		//placeable anywhere
		return true;
	}
	
	public boolean isIndirectlyProdivingPowerTo(World arg0, int arg1, int arg2, int arg3, BlockFace arg4) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isProvidingPowerTo(World arg0, int arg1, int arg2, int arg3,
			BlockFace arg4) {
		// TODO Auto-generated method stub
		return false;
	}

	public void onBlockClicked(World world, int x, int y, int z, SpoutPlayer player) {
		
		if (player != null) {
		
			if (!player.hasPermission(getUsePermission())) {
				player.sendMessage("You do not have permission to perform this action.");
				player.sendMessage("("+getUsePermission()+")");
				return;
			}
			
		}
		
		Location location = new Location(world, (double)x, (double)y, (double)z);
		
		//get data from the db
		//TODO: create a join so dont have to make a second query for disc data
		RecordPlayerData rpdata = JukeBukkit.instance.getDatabase().find(RecordPlayerData.class)
				.where()
					.eq("x", (double)x)
					.eq("y", (double)y)
					.eq("z", (double)z)
					.ieq("worldName", world.getName())
				.findUnique();
		if (rpdata == null) {
			Bukkit.getLogger().log(Level.WARNING, "[JukeBukkit] Missing Record Player Data, this data should have been created when the block was placed.");
		} else {
			if (rpdata.hasDisc()) {
				DiscData discData = JukeBukkit.instance.getDatabase().find(DiscData.class)
						.where()
							.ieq("nameKey", rpdata.getDiscKey())
						.findUnique();
				if (discData == null) {
					Bukkit.getLogger().log(Level.WARNING, "Disc Key is missing from discs table");
				} else {
					//replay
					this.playMusic(discData.getUrl(), location);
					
					return;
				}
			}
		}
		
		//Sound sound = new Sound("disc_load.wav");
		new Sound(SoundEffect.JUKEBOX_STOP, world.getBlockAt(x,y,z), 8).play();
	}
	
	public void onBlockDestroyed(World world, int x, int y, int z) {
		
		Location location = new Location(world, (double)x, (double)y, (double)z);
		
		//get data from the db
		RecordPlayerData rpdata = JukeBukkit.instance.getDatabase().find(RecordPlayerData.class)
				.where()
					.eq("x", (double)x)
					.eq("y", (double)y)
					.eq("z", (double)z)
					.ieq("worldName", world.getName())
				.findUnique();
		if (rpdata == null) {
			Bukkit.getLogger().log(Level.WARNING, "[JukeBukkit] Missing Record Player Data, this data should have been created when the block was placed.");
		} else {
			
			if (rpdata.hasDisc()) {
				//get disc.
				DiscData discData = JukeBukkit.instance.getDatabase().find(DiscData.class)
						.where()
							.ieq("nameKey", rpdata.getDiscKey())
						.findUnique();
				if (discData == null) {
					Bukkit.getLogger().log(Level.WARNING, "Disc Key is missing from discs table");
				} else {
					//create disc to spawn
					BurnedDisc disc = new BurnedDisc(discData);
					ItemStack iss = new SpoutItemStack(disc, 1);
					Location spawnLoc = location;
					spawnLoc.setY(spawnLoc.getY()+1);
					spawnLoc.getWorld().dropItem(spawnLoc, iss);
				}
				
				//just in case there was a disc
				stopMusic(location);
			}
			
		}
		
		//delete ALL data associated to this location, just incase somehow multiples got into the database this will take care of that.
		List<RecordPlayerData> rpdall = JukeBukkit.instance.getDatabase().find(RecordPlayerData.class)
				.where()
					.eq("x", (double)x)
					.eq("y", (double)y)
					.eq("z", (double)z)
					.ieq("worldName", world.getName())
				.findList();
		if (!rpdall.isEmpty()) {
			JukeBukkit.instance.getDatabase().delete(rpdall);
		}
	}

	public boolean onBlockInteract(World world, int x, int y, int z, SpoutPlayer player) {
		
		if (!player.hasPermission(getUsePermission())) {
			player.sendMessage("You do not have permission to perform this action.");
			player.sendMessage("("+getUsePermission()+")");
			return true;
		}
		
		Location location = new Location(world, (double)x, (double)y, (double)z);
		
		//get data from the db
		RecordPlayerData rpdata = JukeBukkit.instance.getDatabase().find(RecordPlayerData.class)
				.where()
					.eq("x", (double)x)
					.eq("y", (double)y)
					.eq("z", (double)z)
					.ieq("worldName", world.getName())
				.findUnique();
		if (rpdata == null) {
			Bukkit.getLogger().log(Level.WARNING, "[JukeBukkit] Missing Record Player Data, this data should have been created when the block was placed.");
			return false;
		}
		
		if (rpdata.hasDisc()) {
			
			if (!player.hasPermission("jukebukkit.use.burneddisc")) {
				player.sendMessage("You do not have permission to perform this action.");
				player.sendMessage("(jukebukkit.use.burneddisc)");
				return false;
			}
			
			//get and eject disc.
			BurnedDisc b = Items.burnedDiscs.get(rpdata.getDiscKey());
			ItemStack iss = new SpoutItemStack(b, 1);
			Location spawnLoc = location;
			spawnLoc.setY(spawnLoc.getY()+1);
			spawnLoc.getWorld().dropItem(spawnLoc, iss);
			
			rpdata.setDiscKey(null);
			JukeBukkit.instance.getDatabase().save(rpdata);
			
			stopMusic(location);
			
			return true;
		}
		
		SpoutItemStack inHand = new SpoutItemStack(player.getItemInHand());
		
		if (inHand.getMaterial() instanceof BurnedDisc) {
			
			if (!player.hasPermission("jukebukkit.use.burneddisc")) {
				player.sendMessage("You do not have permission to perform this action.");
				player.sendMessage("(jukebukkit.use.burneddisc)");
				return false;
			}
			
			//we know its a custom item, go ahaed and remove 1 from the hand.
			if (inHand.getAmount()<2) {
				player.setItemInHand(new ItemStack(Material.AIR));
			} else {
				player.getInventory().getItemInHand().setAmount(player.getInventory().getItemInHand().getAmount()-1);
			}
			
			BurnedDisc discInHand = (BurnedDisc)inHand.getMaterial();
			
			rpdata.setDiscKey(discInHand.getKey());
			JukeBukkit.instance.getDatabase().save(rpdata);
			
			new Sound(SoundEffect.JUKEBOX_START, world.getBlockAt(x,y,z), 8).play();
			
			//start the music
			playMusic(discInHand.getUrl(), location);
			
			return true;
		}
		
		new Sound(SoundEffect.JUKEBOX_STOP, world.getBlockAt(x,y,z), 8).play();
		return false;
	}

	@Override
	public void onBlockPlace(World world, int x, int y, int z) {
		//when the block is placed we need to make sure to get data set up for it.
		RecordPlayerData rpd = JukeBukkit.instance.getDatabase().find(RecordPlayerData.class)
				.where()
					.eq("x", (double)x)
					.eq("y", (double)y)
					.eq("z", (double)z)
					.ieq("worldName", world.getName())
				.findUnique();
		if (rpd == null) {
			rpd = new RecordPlayerData();
			rpd.setNeedleType(RPNeedle.NONE);
			rpd.setDiscKey(null);
			rpd.setX((double)x);
			rpd.setY((double)y);
			rpd.setZ((double)z);
			rpd.setWorldName(world.getName());
			JukeBukkit.instance.getDatabase().save(rpd);
		}
		/* If its still set, well go ahead and leave it, because it could be an blockplace even from setting the custom block to a different subblock for this location */
	}

	@Override
	public void onBlockPlace(World arg0, int arg1, int arg2, int arg3, LivingEntity arg4) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onEntityMoveAt(World arg0, int arg1, int arg2, int arg3, Entity arg4) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public boolean isIndirectlyProvidingPowerTo(World arg0, int arg1, int arg2, int arg3, BlockFace arg4) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	/**
	 * Event fires when a neighboring block updates, like a Neighboring redstone becomes powered.
	 * We can use this to detemind if this block is now powered.
	 */
	public void onNeighborBlockChange(org.bukkit.World world, int x, int y, int z, int changedId) {
		Debug.debug("JukeboxBlock: Neighboring Block Change Event. changedId=", changedId);
		
		SpoutBlock block = (SpoutBlock)world.getBlockAt(x, y, z);
		if (
				(
						block.getData("jukeboxblock.powered") == null ||
						(Integer)block.getData("jukeboxblock.powered") == 0
				) &&
				block.isBlockPowered() == true
				) {
			block.setData("jukeboxblock.powered", 1);
			Debug.debug("JukeboxBlock: Redstone Activated");
			
			onBlockClicked(world, x, y, z, null);
			
		} else if (
				block.getData("jukeboxblock.powered") != null &&
				(Integer)block.getData("jukeboxblock.powered") == 1 &&
				block.isBlockPowered() == true
				) {
			Debug.debug("JukeboxBlock: New Redstone Power source, but block is already powered.");
		
		} else if (
				block.getData("jukeboxblock.powered") != null &&
				(Integer)block.getData("jukeboxblock.powered") == 1 &&
				block.isBlockPowered() == false
				) {
			block.setData("jukeboxblock.powered", 0);
			Debug.debug("JukeboxBlock: Lost Redstone Power.");
			
		} else {
			block.setData("jukeboxblock.powered", 0);
			Debug.debug("JukeboxBlock: Not Powered, and not powering");
		}
		
	}

	public void playMusic(String url, Location location) {
		
		url = JukeBukkit.finishIncompleteURL(url);
		
		//get players in radius of the jukebox and start it for only those players
		for(Player p:location.getWorld().getPlayers()) {
			double distance = location.toVector().distance(p.getLocation().toVector());
			if (distance<=(double)getRange()) {
				SpoutPlayer sp = SpoutManager.getPlayer(p);
				if (sp.isSpoutCraftEnabled()) {
					try {
						SpoutManager.getSoundManager().playCustomMusic(JukeBukkit.instance, sp, url, true, location, getRange());
					} catch (Exception e) {
						new Sound(SoundEffect.JUKEBOX_STOP, location, 8).play();
					}
				}
			}
		}
		
	}
	
	public void stopMusic(Location location) {
		//get players in radius of the jukebox and start it for only those players
		for(Player p:location.getWorld().getPlayers()) {
			double distance = location.toVector().distance(p.getLocation().toVector());
			if (distance<=(double)getRange()) {
				SpoutPlayer sp = SpoutManager.getPlayer(p);
				if (sp.isSpoutCraftEnabled()) {
					SpoutManager.getSoundManager().stopMusic(sp);
				}
			}
		}
	}
}
