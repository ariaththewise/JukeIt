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
package cc.thedudeguy.jukebukkit;

import java.io.File;
import java.lang.reflect.Method;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.block.SpoutBlock;

import cc.thedudeguy.jukebukkit.material.blocks.JukeboxBlock;
import cc.thedudeguy.jukebukkit.material.blocks.RecordPlayer;
import cc.thedudeguy.jukebukkit.util.ResourceManager;


/**
 * Command Executor for JukeBukkit.
 * @author Chris Churchwell
 *
 */
public class CommandHandler implements CommandExecutor {

	public JukeBukkit plugin;
	
	public CommandHandler() {
		this.plugin = JukeBukkit.instance;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args)
	{
		
		if (args.length < 1){
			return help(sender, args);
		}
		
		String subcommand = args[0].toLowerCase();
		String[] subargs = shiftArgs(args);
		
		try {
			Method m = this.getClass().getMethod(subcommand, CommandSender.class, String[].class);
			return (Boolean)m.invoke(this, sender, subargs);
		} catch (Exception e) {
			return help(sender, args);
		}
	}
	
	private String[] shiftArgs(String[] args) {
		
		if (args.length < 2) return new String[0];
		
		String[] shifted = new String[args.length-1];
		System.arraycopy(args, 1, shifted, 0, shifted.length);
		
		return shifted;
	}
	
	public Boolean help(CommandSender sender, String[] args)
	{
		sender.sendMessage(plugin.getDescription().getName());
		sender.sendMessage("------------------------------------");
		sender.sendMessage("Command Usage: /jukebukkit {command}");
		sender.sendMessage("------------------------------------");
		sender.sendMessage("Commands");
		sender.sendMessage("========");
		sender.sendMessage("version - Version Info");
		sender.sendMessage("help    - Show help");
		sender.sendMessage("resetcache - Can sometimes fix problems with textures.");
		
		if (!(sender instanceof Player)) {
			sender.sendMessage("playjuke [world] [x] [y] [z] - Play a JukeBox at this location");
		}
		return true;
	}
	
	public Boolean playjuke(CommandSender sender, String[] args) {
		
		if (sender instanceof Player) {
			return false;
		}
		
		if (args.length != 4) {
			sender.sendMessage("Invalid Usage");
			return true;
		}
		
		World world = JukeBukkit.instance.getServer().getWorld(args[0]);
		int x = Integer.valueOf(args[1]);
		int y = Integer.valueOf(args[2]);
		int z = Integer.valueOf(args[3]);
		
		if (world == null) {
			sender.sendMessage("World does not exist.");
			return true;
		}
		
		SpoutBlock block = (SpoutBlock)world.getBlockAt(x, y, z);
		
		if (block.getCustomBlock() instanceof JukeboxBlock) {
			((JukeboxBlock)block.getCustomBlock()).onBlockClicked(world, x, y, z, null);
		} else if (block.getCustomBlock() instanceof RecordPlayer) {
			((RecordPlayer)block.getCustomBlock()).onBlockClicked(world, x, y, z, null);
		}
		
		return true;
	}
	
	public Boolean version(CommandSender sender, String[] args)
	{
		sender.sendMessage(plugin.getDescription().getFullName());
		return true;
	}
	
	public Boolean resetcache(CommandSender sender, String[] args) {
		
		if (sender instanceof Player) {
			Player player = (Player)sender;
			
			if (!player.hasPermission("jukebukkit.command.resetcache")) {
				player.sendMessage("You do not have permission to use this command.");
				player.sendMessage("(jukebukkit.command.resetcache)");
				return true;
			}
		}
		
		ResourceManager.resetCache();
		sender.sendMessage("Cache has been reset.");
		return true;
	}
	
	public Boolean listmusic(CommandSender sender, String[] args) {
		sender.sendMessage("-- Server Music List --");
		
		if (!JukeBukkit.instance.HTTPserver.isRunning()) {
			sender.sendMessage("Server is not running");
			return true;
		}
		File musicFolder = new File(JukeBukkit.instance.getDataFolder(), "music");
		if (!musicFolder.exists()) {
			sender.sendMessage("No music files on server");
			return true;
		}
		File[] fileList = musicFolder.listFiles(); 
		if (fileList.length < 1) {
			sender.sendMessage("No music files on server");
			return true;
		}
		
		for (File file : fileList) {
			if (file.isFile()) {
				sender.sendMessage(file.getName());
			}
		}
		return true;
	}
}
