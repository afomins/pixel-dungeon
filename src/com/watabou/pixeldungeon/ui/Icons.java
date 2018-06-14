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
package com.watabou.pixeldungeon.ui;

import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.hero.HeroClass;

public enum Icons {

	SKULL( 0, 0, 8, 8 ),
	BUSY( 8, 0, 16, 8 ),
	COMPASS( 0, 8, 7, 13 ), 
	PREFS( 30, 0, 46, 16 ),
	WARNING( 46, 0, 58, 12 ),
	TARGET( 0, 13, 16, 29 ),
	WATA( 30, 16, 45, 26 ),
	WARRIOR( 0, 29, 16, 45 ),
	MAGE( 16, 29, 32, 45 ),
	ROGUE( 32, 29, 48, 45 ),
	HUNTRESS( 48, 29, 64, 45 ),
	CLOSE( 0, 45, 13, 58 ),
	DEPTH( 45, 12, 54, 20 ),
	SLEEP( 13, 45, 22, 53 ),
	ALERT( 22, 45, 30, 53 ),
	SUPPORT( 30, 45, 46, 61 ),
	SUPPORTED( 46, 45, 62, 61 ),
	BACKPACK( 58, 0, 68, 10 ),
	SEED_POUCH( 78, 0, 88, 10 ),
	SCROLL_HOLDER( 68, 0, 78, 10 ),
	WAND_HOLSTER( 88, 0, 98, 10 ),
	KEYRING( 64, 29, 74, 39 ),
	CHECKED( 54, 12, 66, 24 ),
	UNCHECKED( 66, 12, 78, 24 ),
	EXIT( 98, 0, 114, 16 ),
	CHALLENGE_OFF( 78, 16, 102, 40 ),
	CHALLENGE_ON( 102, 16, 126, 40 ),
	RESUME( 114, 0, 126, 11 );

    public int pd3d_x0, pd3d_y0, pd3d_x1, pd3d_y1;
    private Icons(int x0, int y0, int x1, int y1) {
        pd3d_x0 = x0; pd3d_y0 = y0;
        pd3d_x1 = x1; pd3d_y1 = y1;
    }

	public Image get() {
		return get( this );
	}
	
	public static Image get( Icons type ) {
		Image icon = new Image( Assets.ICONS );
		switch (type) {
		case SKULL:
			icon.frame( icon.texture.uvRect( 0, 0, 8, 8 ) );
			break;
		case BUSY:
			icon.frame( icon.texture.uvRect( 8, 0, 16, 8 ) );
			break;
		case COMPASS:
			icon.frame( icon.texture.uvRect( 0, 8, 7, 13 ) );
			break;
		case PREFS:
			icon.frame( icon.texture.uvRect( 30, 0, 46, 16 ) );
			break;
		case WARNING:
			icon.frame( icon.texture.uvRect( 46, 0, 58, 12 ) );
			break;
		case TARGET:
			icon.frame( icon.texture.uvRect( 0, 13, 16, 29 ) );
			break;
		case WATA:
			icon.frame( icon.texture.uvRect( 30, 16, 45, 26 ) );
			break;
		case WARRIOR:
			icon.frame( icon.texture.uvRect( 0, 29, 16, 45 ) );
			break;
		case MAGE:
			icon.frame( icon.texture.uvRect( 16, 29, 32, 45 ) );
			break;
		case ROGUE:
			icon.frame( icon.texture.uvRect( 32, 29, 48, 45 ) );
			break;
		case HUNTRESS:
			icon.frame( icon.texture.uvRect( 48, 29, 64, 45 ) );
			break;
		case CLOSE:
			icon.frame( icon.texture.uvRect( 0, 45, 13, 58 ) );
			break;
		case DEPTH:
			icon.frame( icon.texture.uvRect( 45, 12, 54, 20 ) );
			break;
		case SLEEP:
			icon.frame( icon.texture.uvRect( 13, 45, 22, 53 ) );
			break;
		case ALERT:
			icon.frame( icon.texture.uvRect( 22, 45, 30, 53 ) );
			break;
		case SUPPORT:
			icon.frame( icon.texture.uvRect( 30, 45, 46, 61 ) );
			break;
		case SUPPORTED:
			icon.frame( icon.texture.uvRect( 46, 45, 62, 61 ) );
			break;
		case BACKPACK:
			icon.frame( icon.texture.uvRect( 58, 0, 68, 10 ) );
			break;
		case SCROLL_HOLDER:
			icon.frame( icon.texture.uvRect( 68, 0, 78, 10 ) );
			break;
		case SEED_POUCH:
			icon.frame( icon.texture.uvRect( 78, 0, 88, 10 ) );
			break;
		case WAND_HOLSTER:
			icon.frame( icon.texture.uvRect( 88, 0, 98, 10 ) );
			break;
		case KEYRING:
			icon.frame( icon.texture.uvRect( 64, 29, 74, 39 ) );
			break;
		case CHECKED:
			icon.frame( icon.texture.uvRect( 54, 12, 66, 24 ) );
			break;
		case UNCHECKED:
			icon.frame( icon.texture.uvRect( 66, 12, 78, 24 ) );
			break;
		case EXIT:
			icon.frame( icon.texture.uvRect( 98, 0, 114, 16 ) );
			break;
		case CHALLENGE_OFF:
			icon.frame( icon.texture.uvRect( 78, 16, 102, 40 ) );
			break;
		case CHALLENGE_ON:
			icon.frame( icon.texture.uvRect( 102, 16, 126, 40 ) );
			break;
		case RESUME:
			icon.frame( icon.texture.uvRect( 114, 0, 126, 11 ) );
			break;
		}
		return icon;
	}
	
	public static Image get( HeroClass cl ) {
		switch (cl) {
		case WARRIOR:
			return get( WARRIOR );
		case MAGE:
			return get( MAGE );
		case ROGUE:
			return get( ROGUE );
		case HUNTRESS:
			return get( HUNTRESS );
		default:
			return null;
		}
	}
}
