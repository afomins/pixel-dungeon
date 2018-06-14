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
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.SystemTime;

public class BlacksmithSprite extends MobSprite {
	
	private Emitter emitter;
	
    // PD3D
    private long pd3d_idle_animation_duration;
    private long pd3d_sample_timestamp;

	public BlacksmithSprite() {
		super();
		
		texture( Assets.TROLL );
		
		TextureFilm frames = new TextureFilm( texture, 13, 16 );
		
		idle = new Animation( "idle", 15, true );
		idle.frames( frames, 0, 0, 0, 0, 0, 0, 0, 1, 2, 2, 2, 3 );
		
		run = new Animation( "run", 20, true );
		run.frames( frames, 0 );
		
		die = new Animation( "die", 20, false );
		die.frames( frames, 0 );
		
		play( idle );

        // PD3D: duration of idle animation
        pd3d_idle_animation_duration = (long)(((float)idle.frames.length / idle.pd3d_fps) * 1000);
	}
	
	@Override
	public void link( Char ch ) {
		super.link( ch );
		
		emitter = new Emitter();
		emitter.autoKill = false;
		emitter.pos( x + 7, y + 12 );
		parent.add( emitter );
	}
	
	@Override
	public void update() {
		super.update();
		
		if (emitter != null) {
			emitter.visible = visible;
		}
	}
	
	@Override
	public void onComplete( Animation anim ) {
		super.onComplete( anim );

        // PD3DP: run this callback only when idle animation is over
        if(pd3d_sample_timestamp > SystemTime.now - pd3d_idle_animation_duration) {
            return;
        }
        pd3d_sample_timestamp = SystemTime.now;

		if (visible && emitter != null && anim == idle) {
			emitter.burst( Speck.factory( Speck.FORGE ), 3 );
			float volume = 0.2f / (Level.distance( ch.pos, Dungeon.hero.pos ));
			Sample.INSTANCE.play( Assets.SND_EVOKE, volume, volume, 0.8f  );
		}
	}

}
