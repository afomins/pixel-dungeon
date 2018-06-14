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
package com.watabou.pixeldungeon;

import java.io.IOException;

import javax.microedition.khronos.opengles.GL10;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.InterlevelScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.scenes.TitleScene;

import com.matalok.pd3d.Pd3d;
import com.matalok.pd3d.msg.Msg;
import com.matalok.pd3d.msg.MsgCommand;
import com.matalok.pd3d.msg.MsgGetInventory;
import com.matalok.pd3d.msg.MsgGetScene;
import com.matalok.pd3d.msg.MsgHeroInteract;
import com.matalok.pd3d.msg.MsgLocal;
import com.matalok.pd3d.msg.MsgQuestStart;
import com.matalok.pd3d.msg.MsgRunGame;
import com.matalok.pd3d.msg.MsgRunItemAction;
import com.matalok.pd3d.msg.MsgQuestAction;
import com.matalok.pd3d.msg.MsgSelectInventoryItem;
import com.matalok.pd3d.msg.MsgSelectQuickslotItem;
import com.matalok.pd3d.msg.MsgSwitchScene;
import com.matalok.pd3d.msg.MsgUpdateScene;
import com.matalok.pd3d.msg.MsgUpdateSprites;
import com.matalok.pd3d.shared.Logger;
import com.matalok.pd3d.shared.Utils;

public class PixelDungeon extends Game implements Pd3d.IHook {
	public PixelDungeon(boolean is_remote_server) {
		super( TitleScene.class );

        // PD3D - initialize
        Pd3d.Initialize(this, is_remote_server);

		com.watabou.utils.Bundle.addAlias( 
			com.watabou.pixeldungeon.items.scrolls.ScrollOfUpgrade.class, 
			"com.watabou.pixeldungeon.items.scrolls.ScrollOfEnhancement" );
		com.watabou.utils.Bundle.addAlias( 
			com.watabou.pixeldungeon.actors.blobs.WaterOfHealth.class, 
			"com.watabou.pixeldungeon.actors.blobs.Light" );
		com.watabou.utils.Bundle.addAlias( 
			com.watabou.pixeldungeon.items.rings.RingOfMending.class, 
			"com.watabou.pixeldungeon.items.rings.RingOfRejuvenation" );
		com.watabou.utils.Bundle.addAlias( 
			com.watabou.pixeldungeon.items.wands.WandOfReach.class, 
			"com.watabou.pixeldungeon.items.wands.WandOfTelekenesis" );
		com.watabou.utils.Bundle.addAlias( 
			com.watabou.pixeldungeon.actors.blobs.Foliage.class, 
			"com.watabou.pixeldungeon.actors.blobs.Blooming" );
		com.watabou.utils.Bundle.addAlias( 
			com.watabou.pixeldungeon.actors.buffs.Shadows.class, 
			"com.watabou.pixeldungeon.actors.buffs.Rejuvenation" );
		com.watabou.utils.Bundle.addAlias( 
			com.watabou.pixeldungeon.items.scrolls.ScrollOfPsionicBlast.class, 
			"com.watabou.pixeldungeon.items.scrolls.ScrollOfNuclearBlast" );
		com.watabou.utils.Bundle.addAlias( 
			com.watabou.pixeldungeon.actors.hero.Hero.class, 
			"com.watabou.pixeldungeon.actors.Hero" );
		com.watabou.utils.Bundle.addAlias( 
			com.watabou.pixeldungeon.actors.mobs.npcs.Shopkeeper.class,
			"com.watabou.pixeldungeon.actors.mobs.Shopkeeper" );
		// 1.6.1
		com.watabou.utils.Bundle.addAlias( 
			com.watabou.pixeldungeon.items.quest.DriedRose.class,
			"com.watabou.pixeldungeon.items.DriedRose" );
		com.watabou.utils.Bundle.addAlias( 
			com.watabou.pixeldungeon.actors.mobs.npcs.MirrorImage.class,
			"com.watabou.pixeldungeon.items.scrolls.ScrollOfMirrorImage$MirrorImage" );
		// 1.6.4
		com.watabou.utils.Bundle.addAlias( 
			com.watabou.pixeldungeon.items.rings.RingOfElements.class,
			"com.watabou.pixeldungeon.items.rings.RingOfCleansing" );
		com.watabou.utils.Bundle.addAlias( 
			com.watabou.pixeldungeon.items.rings.RingOfElements.class,
			"com.watabou.pixeldungeon.items.rings.RingOfResistance" );
		com.watabou.utils.Bundle.addAlias( 
			com.watabou.pixeldungeon.items.weapon.missiles.Boomerang.class,
			"com.watabou.pixeldungeon.items.weapon.missiles.RangersBoomerang" );
		com.watabou.utils.Bundle.addAlias( 
			com.watabou.pixeldungeon.items.rings.RingOfPower.class,
			"com.watabou.pixeldungeon.items.rings.RingOfEnergy" );
		// 1.7.2
		com.watabou.utils.Bundle.addAlias( 
			com.watabou.pixeldungeon.plants.Dreamweed.class,
			"com.watabou.pixeldungeon.plants.Blindweed" );
		com.watabou.utils.Bundle.addAlias( 
			com.watabou.pixeldungeon.plants.Dreamweed.Seed.class,
			"com.watabou.pixeldungeon.plants.Blindweed$Seed" );
		// 1.7.4
		com.watabou.utils.Bundle.addAlias( 
			com.watabou.pixeldungeon.items.weapon.enchantments.Shock.class,
			"com.watabou.pixeldungeon.items.weapon.enchantments.Piercing" );
		com.watabou.utils.Bundle.addAlias( 
			com.watabou.pixeldungeon.items.weapon.enchantments.Shock.class,
			"com.watabou.pixeldungeon.items.weapon.enchantments.Swing" );
		com.watabou.utils.Bundle.addAlias( 
			com.watabou.pixeldungeon.items.scrolls.ScrollOfEnchantment.class,
			"com.watabou.pixeldungeon.items.scrolls.ScrollOfWeaponUpgrade" );
		// 1.7.5
		com.watabou.utils.Bundle.addAlias( 
			com.watabou.pixeldungeon.items.scrolls.ScrollOfEnchantment.class,
			"com.watabou.pixeldungeon.items.Stylus" );
		// 1.8.0
		com.watabou.utils.Bundle.addAlias( 
			com.watabou.pixeldungeon.actors.mobs.FetidRat.class,
			"com.watabou.pixeldungeon.actors.mobs.npcs.Ghost$FetidRat" );
		com.watabou.utils.Bundle.addAlias( 
			com.watabou.pixeldungeon.plants.Rotberry.class,
			"com.watabou.pixeldungeon.actors.mobs.npcs.Wandmaker$Rotberry" );
		com.watabou.utils.Bundle.addAlias( 
			com.watabou.pixeldungeon.plants.Rotberry.Seed.class,
			"com.watabou.pixeldungeon.actors.mobs.npcs.Wandmaker$Rotberry$Seed" );
		// 1.9.0
		com.watabou.utils.Bundle.addAlias( 
			com.watabou.pixeldungeon.items.wands.WandOfReach.class,
			"com.watabou.pixeldungeon.items.wands.WandOfTelekinesis" );
	}
	
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		
        // PD3D
        Pd3d.sprite.RegisterSprites();
		
		updateImmersiveMode();
		
		DisplayMetrics metrics = new DisplayMetrics();
		instance.getWindowManager().getDefaultDisplay().getMetrics( metrics );
		boolean landscape = metrics.widthPixels > metrics.heightPixels;
		
		if (Preferences.INSTANCE.getBoolean( Preferences.KEY_LANDSCAPE, false ) != landscape) {
			landscape( !landscape );
		}
		
		Music.INSTANCE.enable( false /*music()*/ );
		Sample.INSTANCE.enable( false /*soundFx()*/ );
		
		Sample.INSTANCE.load( 
			Assets.SND_CLICK, 
			Assets.SND_BADGE, 
			Assets.SND_GOLD,
			
			Assets.SND_DESCEND,
			Assets.SND_STEP,
			Assets.SND_WATER,
			Assets.SND_OPEN,
			Assets.SND_UNLOCK,
			Assets.SND_ITEM,
			Assets.SND_DEWDROP, 
			Assets.SND_HIT, 
			Assets.SND_MISS,
			Assets.SND_EAT,
			Assets.SND_READ,
			Assets.SND_LULLABY,
			Assets.SND_DRINK,
			Assets.SND_SHATTER,
			Assets.SND_ZAP,
			Assets.SND_LIGHTNING,
			Assets.SND_LEVELUP,
			Assets.SND_DEATH,
			Assets.SND_CHALLENGE,
			Assets.SND_CURSED,
			Assets.SND_EVOKE,
			Assets.SND_TRAP,
			Assets.SND_TOMB,
			Assets.SND_ALERT,
			Assets.SND_MELD,
			Assets.SND_BOSS,
			Assets.SND_BLAST,
			Assets.SND_PLANT,
			Assets.SND_RAY,
			Assets.SND_BEACON,
			Assets.SND_TELEPORT,
			Assets.SND_CHARMS,
			Assets.SND_MASTERY,
			Assets.SND_PUFF,
			Assets.SND_ROCKS,
			Assets.SND_BURNING,
			Assets.SND_FALLING,
			Assets.SND_GHOST,
			Assets.SND_SECRET,
			Assets.SND_BONES,
			Assets.SND_BEE,
			Assets.SND_DEGRADE,
			Assets.SND_MIMIC );
	}
	
	@Override
	public void onWindowFocusChanged( boolean hasFocus ) {
		
		super.onWindowFocusChanged( hasFocus );
		
		if (hasFocus) {
			updateImmersiveMode();
		}
	}
	
	public static void switchNoFade( Class<? extends PixelScene> c ) {
		PixelScene.noFade = true;
		switchScene( c );
	}
	
	/*
	 * ---> Prefernces
	 */
	
	public static void landscape( boolean value ) {
		Game.instance.setRequestedOrientation( value ?
			ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE :
			ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
		Preferences.INSTANCE.put( Preferences.KEY_LANDSCAPE, value );
	}
	
	public static boolean landscape() {
		return width > height;
	}
	
	// *** IMMERSIVE MODE ****
	
	private static boolean immersiveModeChanged = false;
	
	@SuppressLint("NewApi")
	public static void immerse( boolean value ) {
		Preferences.INSTANCE.put( Preferences.KEY_IMMERSIVE, value );
		
		instance.runOnUiThread( new Runnable() {
			@Override
			public void run() {
				updateImmersiveMode();
				immersiveModeChanged = true;
			}
		} );
	}
	
	@Override
	public void onSurfaceChanged( GL10 gl, int width, int height ) {
		super.onSurfaceChanged( gl, width, height );
		
		if (immersiveModeChanged) {
			requestedReset = true;
			immersiveModeChanged = false;
		}
	}
	
	@SuppressLint("NewApi")
	public static void updateImmersiveMode() {
		if (android.os.Build.VERSION.SDK_INT >= 19) {
			try {
				// Sometime NullPointerException happens here
				instance.getWindow().getDecorView().setSystemUiVisibility( 
					immersed() ?
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE | 
					View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | 
					View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | 
					View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | 
					View.SYSTEM_UI_FLAG_FULLSCREEN | 
					View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY 
					:
					0 );
			} catch (Exception e) {
				reportException( e );
			}
		}
	}
	
	public static boolean immersed() {
		return Preferences.INSTANCE.getBoolean( Preferences.KEY_IMMERSIVE, false );
	}
	
	// *****************************
	
	public static void scaleUp( boolean value ) {
		Preferences.INSTANCE.put( Preferences.KEY_SCALE_UP, value );
		switchScene( TitleScene.class );
	}
	
	public static boolean scaleUp() {
		return Preferences.INSTANCE.getBoolean( Preferences.KEY_SCALE_UP, true );
	}

	public static void zoom( int value ) {
		Preferences.INSTANCE.put( Preferences.KEY_ZOOM, value );
	}
	
	public static int zoom() {
		return Preferences.INSTANCE.getInt( Preferences.KEY_ZOOM, 0 );
	}
	
	public static void music( boolean value ) {
		Music.INSTANCE.enable( value );
		Preferences.INSTANCE.put( Preferences.KEY_MUSIC, value );
	}
	
	public static boolean music() {
		return Preferences.INSTANCE.getBoolean( Preferences.KEY_MUSIC, true );
	}
	
	public static void soundFx( boolean value ) {
		Sample.INSTANCE.enable( value );
		Preferences.INSTANCE.put( Preferences.KEY_SOUND_FX, value );
	}
	
	public static boolean soundFx() {
		return Preferences.INSTANCE.getBoolean( Preferences.KEY_SOUND_FX, true );
	}
	
	public static void brightness( boolean value ) {
		Preferences.INSTANCE.put( Preferences.KEY_BRIGHTNESS, value );
		if (scene() instanceof GameScene) {
			((GameScene)scene()).brightness( value );
		}
	}
	
	public static boolean brightness() {
		return Preferences.INSTANCE.getBoolean( Preferences.KEY_BRIGHTNESS, false );
	}
	
	public static void donated( String value ) {
		Preferences.INSTANCE.put( Preferences.KEY_DONATED, value );
	}
	
	public static String donated() {
		return Preferences.INSTANCE.getString( Preferences.KEY_DONATED, "" );
	}
	
	public static void lastClass( int value ) {
		Preferences.INSTANCE.put( Preferences.KEY_LAST_CLASS, value );
	}
	
	public static int lastClass() {
		return Preferences.INSTANCE.getInt( Preferences.KEY_LAST_CLASS, 0 );
	}
	
	public static void challenges( int value ) {
		Preferences.INSTANCE.put( Preferences.KEY_CHALLENGES, value );
	}
	
	public static int challenges() {
		return Preferences.INSTANCE.getInt( Preferences.KEY_CHALLENGES, 0 );
	}
	
	public static void intro( boolean value ) {
		Preferences.INSTANCE.put( Preferences.KEY_INTRO, value );
	}
	
	public static boolean intro() {
		return Preferences.INSTANCE.getBoolean( Preferences.KEY_INTRO, true );
	}
	
	/*
	 * <--- Preferences
	 */
	
	public static void reportException( Throwable tr ) {
		Log.e( "PD", Log.getStackTraceString( tr ) ); 
	}

    // *************************************************************************
    // PixeDungeon
    // *************************************************************************
    private boolean pd3d_no_save;

    //--------------------------------------------------------------------------
    public PixelDungeon() {
        this(true); // Run pixel dungeon as remote server
    }

    //--------------------------------------------------------------------------
    public boolean OnRequest(Msg req, Msg resp) {
        if(!Pd3d.OnRecvRequest(this, req, resp)) {
            return false;
        }

        // Send bad response
        if(!resp.status_code) {
            return true;
        }

        // Forward request to current scene
        return Pd3d.OnRecvRequest((PixelScene)scene, req, resp);
    }

    // *************************************************************************
    // Game
    // *************************************************************************
    @Override protected void update() {
        Pd3d.pd.Process();
        super.update();
    }

    //--------------------------------------------------------------------------
    @Override protected void switchScene() {
        // Get names of old and new scenes
        String old_name = (scene == null) ? "none" : 
          Pd3d.names.GetSceneName((PixelScene)scene); 
        String new_name = Pd3d.names.GetSceneName(
          (PixelScene)requestedScene);

        // Check if old/new scenes are known
        if(old_name == null && new_name == null) {
            if(old_name == null) old_name = scene.getClass().getSimpleName();
            if(new_name == null) new_name = requestedScene.getClass().getSimpleName();
            Logger.e("Failed to switch scenes :: %s -> %s", old_name, new_name);

        // Update scene on client
        } else {
            Logger.d("Switching scene :: %s -> %s", old_name, new_name);
            if(scene != null) {
                Pd3d.pd.AddToRecvQueue(
                  MsgGetScene.CreateRequest());
            }

            // Save game
            if(old_name == "scene-game" && !pd3d_no_save) {
                try {
                    Dungeon.saveAll();
                } catch (IOException e) {
                    Utils.LogException(e, "Failed to save game while switching scene");
                }
            }
            pd3d_no_save = false;
        }

        // Switch scene
        super.switchScene();

        // Original game speed
        Game.pd3d_elapsed_orig = Game.elapsed;
        Game.pd3d_timescale_orig = Game.timeScale;

        // Increased game speed
        Game.elapsed = 0.0f;
        Game.timeScale = 4242.42f;
    }

    // *************************************************************************
    // IHook
    // *************************************************************************
    @Override public void OnInit() {
    }

    //--------------------------------------------------------------------------
    @Override public void OnClientConnect() {
    }

    //--------------------------------------------------------------------------
    @Override public void OnClientDisconnect() {
    }

    // *************************************************************************
    // IRequestHandler
    // *************************************************************************
    @Override public boolean OnRecvMsgLocal(
      MsgLocal req, MsgLocal resp) {
        // Set client API when connecting to local client
        if(req.state.equals("connect")) {
            Pd3d.pd.SetClientAPI(req.client_api);
        }
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnRecvMsgGetScene(
      MsgGetScene req, MsgGetScene resp) {
        // Set name of current scene
        resp.scene_name = Pd3d.names.GetSceneName(
          ((PixelScene)scene).getClass());
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnRecvMsgUpdateSprites(
      MsgUpdateSprites req, MsgUpdateSprites resp) {
        // Set updated sprites
        resp.sprites = Pd3d.sprite.GetUpdateList().PopList();
        return (resp.sprites != null);
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnRecvMsgUpdateScene(
      MsgUpdateScene req, MsgUpdateScene resp) {
        // Set name of current scene
        resp.scene_name = Pd3d.names.GetSceneName(
          ((PixelScene)scene).getClass());

        // Set log & info
        resp.log_lines = Pd3d.pd.GetLogList().PopList();
        resp.info_lines = Pd3d.pd.GetInfoList().PopList();
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnRecvMsgSwitchScene(
      MsgSwitchScene req, MsgSwitchScene resp) {
        // Get new scene
        Class<? extends PixelScene> new_scene = 
          Pd3d.names.GetSceneClass(req.scene_name);
        if(new_scene == null) {
            resp.SetStatus(false, 
              "Scene name is invalid, name=%s", req.scene_name);
            return true;
        }

        // Switch to new scene
        switchScene(new_scene);
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnRecvMsgHeroInteract(
      MsgHeroInteract req, MsgHeroInteract resp) {
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnRecvMsgGetInventory(
      MsgGetInventory req, MsgGetInventory resp) {
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnRecvMsgSelectInventoryItem(
      MsgSelectInventoryItem req, MsgSelectInventoryItem resp) {
        return true;  
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnRecvMsgSelectQuickslotItem(
      MsgSelectQuickslotItem req, MsgSelectQuickslotItem resp) {
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnRecvMsgRunItemAction(
      MsgRunItemAction req, MsgRunItemAction resp) {
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnRecvMsgRunGame(
      MsgRunGame req, MsgRunGame resp) {
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnRecvMsgCommand(
      MsgCommand req, MsgCommand resp) {
        // IDDQD
        if(req.iddqd != null) {
            resp.iddqd = 
              Pd3d.game.SetIddqd(req.iddqd);;
        }

        // Extended item info
        if(req.item_info_ext != null) {
            resp.item_info_ext = 
              Pd3d.game.SetItemInfoExt(req.item_info_ext);
        }

        // Music
        if(req.music != null) {
            Music.INSTANCE.enable(req.music);
        }

        // Sound
        if(req.sound != null) {
            Sample.INSTANCE.enable(req.sound);
        }

        // GAME-OP
        if(req.game_op != null) {
            resp.game_op = req.game_op;
            resp.game_args = req.game_args;

            switch(req.game_op) {
            // Save
            case "save": {
                try {
                    Dungeon.saveAll();
                } catch(Exception ex) {
                    Utils.LogException(ex, "Failed to save game");
                }
            } break;

            // Load
            case "load-new":
            case "load-continue": {
                pd3d_no_save = true;
                InterlevelScene.mode = (req.game_op.equals("load-new")) ? 
                  InterlevelScene.Mode.DESCEND : InterlevelScene.Mode.CONTINUE;
                Game.switchScene(InterlevelScene.class);
            }
            }
        }
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnRecvMsgQuestStart(
      MsgQuestStart req, MsgQuestStart resp) {
       return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnRecvMsgQuestAction(
      MsgQuestAction req, MsgQuestAction resp) {
        return true;
    }
}