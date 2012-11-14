/**
 * This file is part of JukeIt
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
package com.chrischurchwell.jukeit.material.blocks;

import java.util.List;

import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.block.design.GenericCubeBlockDesign;
import org.getspout.spoutapi.inventory.SpoutItemStack;
import org.getspout.spoutapi.inventory.SpoutShapedRecipe;
import org.getspout.spoutapi.material.MaterialData;

import com.chrischurchwell.jukeit.JukeIt;
import com.chrischurchwell.jukeit.material.Blocks;
import com.chrischurchwell.jukeit.texture.TextureFile;
import com.chrischurchwell.jukeit.util.Recipies;


public class JukeboxMidRange extends JukeboxBlock {
	
	public JukeboxMidRange()
	{
		super("Mid Range Jukebox");
	}
	
	@Override
	public GenericCubeBlockDesign getCustomBlockDesign() {
		
		return new GenericCubeBlockDesign(
				JukeIt.getInstance(), 
				TextureFile.BLOCK_JUKEBOX_MID.getTexture(), 
				new int[] { 0, 2, 2, 2, 2, 1 }
			);
	}
	
	@Override
	public boolean canRedstoneActivate() {
		return true;
	}
	
	@Override
	public int getRange()
	{
		// Ariath's Patch - (JukeBoxMidRange's range customizable).
		return JukeIt.getInstance().getConfig().getInt("midRangeJukeboxRange", 30);
		// Ariath's Patch - END
	}

	@Override
	public void setRecipe() {
			
			List<org.getspout.spoutapi.material.Material> woods = Recipies.getWoods();
			
			for (org.getspout.spoutapi.material.Material mat1 : woods) {
				for (org.getspout.spoutapi.material.Material mat2 : woods) {
					for (org.getspout.spoutapi.material.Material mat3 : woods) {
						for (org.getspout.spoutapi.material.Material mat4 : woods) {
							for (org.getspout.spoutapi.material.Material mat5 : woods) {
								for (org.getspout.spoutapi.material.Material mat6 : woods) {
									
									SpoutShapedRecipe r = new SpoutShapedRecipe( new SpoutItemStack(this, 1) );
									r.shape(Recipies.buildWoodRefString(mat1, mat2, mat3), "njn", Recipies.buildWoodRefString(mat4, mat5, mat6));
									r.setIngredient('j', Blocks.jukeboxLowRange);
									r.setIngredient('n', MaterialData.noteblock);
									
									for (org.getspout.spoutapi.material.Material umat : Recipies.getWoodUniqueMats(mat1, mat2, mat3, mat4, mat5, mat6) ) {
										r.setIngredient(Recipies.getWoodMatRefLetter(umat), umat);
									}
									
									SpoutManager.getMaterialManager().registerSpoutRecipe(r);
									
								}
							}
						}
					}
				}
			}
	}
}
