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
package com.watabou.pixeldungeon.sprites;

import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.Assets;

public class AcidicSprite extends ScorpioSprite {
	
	public AcidicSprite() {
		super();
		
		texture( Assets.SCORPIO );
		
		TextureFilm frames = new TextureFilm( texture, 18, 17 );
		
		idle = new Animation( "idle", 12, true );
		idle.frames( frames, 14, 14, 14, 14, 14, 14, 14, 14, 15, 16, 15, 16, 15, 16 );
		
		run = new Animation( "run", 4, true );
		run.frames( frames, 19, 20 );
		
		attack = new Animation( "atack", 15, false );
		attack.frames( frames, 14, 17, 18 );
		
		zap = attack.clone("zap");
		
		die = new Animation( "die", 12, false );
		die.frames( frames, 14, 21, 22, 23, 24 );
		
		play( idle );
	}
	
	@Override
	public int blood() {
		return 0xFF66FF22;
	}
}
