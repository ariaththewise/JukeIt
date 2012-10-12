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
package cc.thedudeguy.jukebukkit.material.items.colorblankdisc;

import cc.thedudeguy.jukebukkit.material.items.BlankDisc;
import cc.thedudeguy.jukebukkit.material.items.DiscColor;


public class BlankDiscPurple extends BlankDisc {

	public BlankDiscPurple() {
		super("Blank Purple Obsidyisc");
		setColor(DiscColor.PURPLE);
	}

	@Override
	public String getTextureFileName() {
		return "blank_disc_purple.png";
	}

}