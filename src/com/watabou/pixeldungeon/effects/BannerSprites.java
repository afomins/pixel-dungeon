/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon.effects;

import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.Assets;

public class BannerSprites {

	public enum  Type {
		PIXEL_DUNGEON( 0, 0, 128, 70 ),
		BOSS_SLAIN( 0, 70, 128, 105 ),
		GAME_OVER( 0, 105, 128, 140 ),
		SELECT_YOUR_HERO( 0, 140, 128, 161 ),
		PIXEL_DUNGEON_SIGNS( 0, 161, 128, 218 );

       public int pd3d_x0, pd3d_y0, pd3d_x1, pd3d_y1;
        private Type(int x0, int y0, int x1, int y1) {
            pd3d_x0 = x0; pd3d_y0 = y0;
            pd3d_x1 = x1; pd3d_y1 = y1;
        }
	};
	
	public static Image get( Type type ) {
		Image icon = new Image( Assets.BANNERS );
		switch (type) {
		case PIXEL_DUNGEON:
			icon.frame( icon.texture.uvRect( 0, 0, 128, 70 ) );
			break;
		case BOSS_SLAIN:
			icon.frame( icon.texture.uvRect( 0, 70, 128, 105 ) );
			break;
		case GAME_OVER:
			icon.frame( icon.texture.uvRect( 0, 105, 128, 140 ) );
			break;
		case SELECT_YOUR_HERO:
			icon.frame( icon.texture.uvRect( 0, 140, 128, 161 ) );
			break;
		case PIXEL_DUNGEON_SIGNS:
			icon.frame( icon.texture.uvRect( 0, 161, 128, 218 ) );
			break;
		}
		return icon;
	}
}
