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
package com.watabou.pixeldungeon.scenes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.SkinnedBlock;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.FogOfWar;
import com.watabou.pixeldungeon.PixelDungeon;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.npcs.NPC;
import com.watabou.pixeldungeon.actors.mobs.npcs.Shopkeeper;
import com.watabou.pixeldungeon.effects.BannerSprites;
import com.watabou.pixeldungeon.effects.BlobEmitter;
import com.watabou.pixeldungeon.effects.EmoIcon;
import com.watabou.pixeldungeon.effects.Flare;
import com.watabou.pixeldungeon.effects.FloatingText;
import com.watabou.pixeldungeon.effects.Ripple;
import com.watabou.pixeldungeon.effects.SpellSprite;
import com.watabou.pixeldungeon.items.Heap;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.TomeOfMastery;
import com.watabou.pixeldungeon.items.bags.Bag;
import com.watabou.pixeldungeon.items.potions.Potion;
import com.watabou.pixeldungeon.items.wands.Wand;
import com.watabou.pixeldungeon.items.wands.WandOfBlink;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.RegularLevel;
import com.watabou.pixeldungeon.levels.features.Chasm;
import com.watabou.pixeldungeon.plants.Plant;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.DiscardedItemSprite;
import com.watabou.pixeldungeon.sprites.HeroSprite;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.sprites.PlantSprite;
import com.watabou.pixeldungeon.ui.AttackIndicator;
import com.watabou.pixeldungeon.ui.Banner;
import com.watabou.pixeldungeon.ui.BusyIndicator;
import com.watabou.pixeldungeon.ui.GameLog;
import com.watabou.pixeldungeon.ui.HealthIndicator;
import com.watabou.pixeldungeon.ui.QuickSlot;
import com.watabou.pixeldungeon.ui.StatusPane;
import com.watabou.pixeldungeon.ui.Toast;
import com.watabou.pixeldungeon.ui.Toolbar;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.windows.WndBag.Mode;
import com.watabou.pixeldungeon.windows.WndGame;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.pixeldungeon.windows.WndStory;
import com.watabou.utils.Random;

import com.matalok.pd3d.Pd3d;
import com.matalok.pd3d.Pd3dGame;
import com.matalok.pd3d.Pd3dGame.EmitterDesc;
import com.matalok.pd3d.Pd3dGame.Step;
import com.matalok.pd3d.desc.*;
import com.matalok.pd3d.map.Map;
import com.matalok.pd3d.map.Map.ICell;
import com.matalok.pd3d.map.MapCell;
import com.matalok.pd3d.map.MapEnum;
import com.matalok.pd3d.map.MapEnum.TerrainType;
import com.matalok.pd3d.msg.*;
import com.matalok.pd3d.shared.ClientAPI;
import com.matalok.pd3d.shared.Logger;
import com.matalok.pd3d.shared.Utils;

public class GameScene extends PixelScene {
	
	private static final String TXT_WELCOME			= "Welcome to the level %d of Pixel Dungeon!";
	private static final String TXT_WELCOME_BACK	= "Welcome back to the level %d of Pixel Dungeon!";
	private static final String TXT_NIGHT_MODE		= "Be cautious, since the dungeon is even more dangerous at night!";
	
	private static final String TXT_CHASM	= "Your steps echo across the dungeon.";
	private static final String TXT_WATER	= "You hear the water splashing around you.";
	private static final String TXT_GRASS	= "The smell of vegetation is thick in the air.";
	private static final String TXT_SECRETS	= "The atmosphere hints that this floor hides many secrets.";
	
	static GameScene scene;
	
	private SkinnedBlock water;
	private DungeonTilemap tiles;
	private FogOfWar fog;
	private HeroSprite hero;
	
	private GameLog log;
	
	private BusyIndicator busy;
	
	private static CellSelector cellSelector;
	
	private Group terrain;
	private Group ripples;
	private Group plants;
	private Group heaps;
	private Group mobs;
	private Group emitters;
	private Group effects;
	private Group gases;
	private Group spells;
	private Group statuses;
	private Group emoicons;
	
	private Toolbar toolbar;
	private Toast prompt;

    // PD3D
    private StatusPane pd3d_sp;

	@Override
	public void create() {
		Music.INSTANCE.play( Assets.TUNE, true );
		Music.INSTANCE.volume( 1f );
		
		PixelDungeon.lastClass( Dungeon.hero.heroClass.ordinal() );
		
		super.create();
		Camera.main.zoom( defaultZoom + PixelDungeon.zoom() );
		
		scene = this;

		terrain = new Group();
		add( terrain );
		
		water = new SkinnedBlock( 
			Level.WIDTH * DungeonTilemap.SIZE, 
			Level.HEIGHT * DungeonTilemap.SIZE,
			Dungeon.level.waterTex() );
		terrain.add( water );
		
		ripples = new Group();
		terrain.add( ripples );
		
		tiles = new DungeonTilemap();
		terrain.add( tiles );
		
		Dungeon.level.addVisuals( this );
		
		plants = new Group();
		add( plants );
		
		int size = Dungeon.level.plants.size();
		for (int i=0; i < size; i++) {
			addPlantSprite( Dungeon.level.plants.valueAt( i ) );
		}
		
		heaps = new Group();
		add( heaps );
		
		size = Dungeon.level.heaps.size();
		for (int i=0; i < size; i++) {
			addHeapSprite( Dungeon.level.heaps.valueAt( i ) );
		}

		emitters = new Group();
		effects = new Group();
		emoicons = new Group();

		mobs = new Group();
		add( mobs );
		
		for (Mob mob : Dungeon.level.mobs) {
			addMobSprite( mob );
			if (Statistics.amuletObtained) {
				mob.beckon( Dungeon.hero.pos );
			}
		}
		
		add( emitters );
		add( effects );
		
		gases = new Group();
		add( gases );
		
		for (Blob blob : Dungeon.level.blobs.values()) {
			blob.emitter = null;
			addBlobSprite( blob );
		}
		
		fog = new FogOfWar( Level.WIDTH, Level.HEIGHT );
		fog.updateVisibility( Dungeon.visible, Dungeon.level.visited, Dungeon.level.mapped );
		add( fog );
		
		brightness( PixelDungeon.brightness() );
		
		spells = new Group();
		add( spells );
		
		statuses = new Group();
		add( statuses );
		
		add( emoicons );
		
		hero = new HeroSprite();
		hero.place( Dungeon.hero.pos );
		hero.updateArmor();
		mobs.add( hero );

		add( new HealthIndicator() );
		
		add( cellSelector = new CellSelector( tiles ) );
		
		StatusPane sb = pd3d_sp = new StatusPane();
		sb.camera = uiCamera;
		sb.setSize( uiCamera.width, 0 );
		add( sb );
		
		toolbar = new Toolbar();
		toolbar.camera = uiCamera;
		toolbar.setRect( 0,uiCamera.height - toolbar.height(), uiCamera.width, toolbar.height() );
		add( toolbar );
		
		AttackIndicator attack = new AttackIndicator();
		attack.camera = uiCamera;
		attack.setPos( 
			uiCamera.width - attack.width(), 
			toolbar.top() - attack.height() );
		add( attack );
		
		log = new GameLog();
		log.camera = uiCamera;
		log.setRect( 0, toolbar.top(), attack.left(),  0 );
		add( log );
		
		busy = new BusyIndicator();
		busy.camera = uiCamera;
		busy.x = 1;
		busy.y = sb.bottom() + 1;
		add( busy );
		
		switch (InterlevelScene.mode) {
		case RESURRECT:
			WandOfBlink.appear( Dungeon.hero, Dungeon.level.entrance );
			new Flare( 8, 32 ).color( 0xFFFF66, true ).show( hero, 2f ) ;
			break;
		case RETURN:
			WandOfBlink.appear(  Dungeon.hero, Dungeon.hero.pos );
			break;
		case FALL:
			Chasm.heroLand();
			break;
		case DESCEND:
			switch (Dungeon.depth) {
			case 1:
				WndStory.showChapter( WndStory.ID_SEWERS );
				break;
			case 6:
				WndStory.showChapter( WndStory.ID_PRISON );
				break;
			case 11:
				WndStory.showChapter( WndStory.ID_CAVES );
				break;
			case 16:
				WndStory.showChapter( WndStory.ID_METROPOLIS );
				break;
			case 22:
				WndStory.showChapter( WndStory.ID_HALLS );
				break;
			default:
			}
			if (Dungeon.hero.isAlive() && Dungeon.depth != 22) {
				Badges.validateNoKilling();
			}
			break;
		default:
		}
		
		ArrayList<Item> dropped = Dungeon.droppedItems.get( Dungeon.depth );
		if (dropped != null) {
			for (Item item : dropped) {
				int pos = Dungeon.level.randomRespawnCell();
				if (item instanceof Potion) {
					((Potion)item).shatter( pos );
				} else if (item instanceof Plant.Seed) {
					Dungeon.level.plant( (Plant.Seed)item, pos );
				} else {
					Dungeon.level.drop( item, pos );
				}
			}
			Dungeon.droppedItems.remove( Dungeon.depth );
		}
		
		Camera.main.target = hero;

		if (InterlevelScene.mode != InterlevelScene.Mode.NONE) {
			if (Dungeon.depth < Statistics.deepestFloor) {
				GLog.h( TXT_WELCOME_BACK, Dungeon.depth );
			} else {
				GLog.h( TXT_WELCOME, Dungeon.depth );
				Sample.INSTANCE.play( Assets.SND_DESCEND );
			}
			switch (Dungeon.level.feeling) {
				case CHASM:
					GLog.w( TXT_CHASM );
					break;
				case WATER:
					GLog.w( TXT_WATER );
					break;
				case GRASS:
					GLog.w( TXT_GRASS );
					break;
				default:
			}
			if (Dungeon.level instanceof RegularLevel &&
					((RegularLevel) Dungeon.level).secretDoors > Random.IntRange( 3, 4 )) {
				GLog.w( TXT_SECRETS );
			}
			if (Dungeon.nightMode && !Dungeon.bossLevel()) {
				GLog.w( TXT_NIGHT_MODE );
			}

			InterlevelScene.mode = InterlevelScene.Mode.NONE;

			fadeIn();
		}
	}
	
	public void destroy() {
		
		scene = null;
		Badges.saveGlobal();
		
		super.destroy();
	}
	
	@Override
	public synchronized void pause() {
		try {
			Dungeon.saveAll();
			Badges.saveGlobal();
		} catch (IOException e) {
			//
		}
	}
	
	@Override
	public synchronized void update() {
		if (Dungeon.hero == null) {
			return;
		}
			
		super.update();

        // PD3D: Use original time when rendering water
        water.offset( 0, -5 * Game.pd3d_elapsed_orig );

		Actor.process();
		
		if (Dungeon.hero.ready && !Dungeon.hero.paralysed) {
			log.newLine();
		}
		
		cellSelector.enabled = Dungeon.hero.ready;
	}
	
	@Override
	protected void onBackPressed() {
		if (!cancel()) {
			add( new WndGame() );
		}
	}
	
	@Override
	protected void onMenuPressed() {
		if (Dungeon.hero.ready) {
			selectItem( null, WndBag.Mode.ALL, null );
		}
	}
	
	public void brightness( boolean value ) {
		water.rm = water.gm = water.bm = 
		tiles.rm = tiles.gm = tiles.bm = 
			value ? 1.5f : 1.0f;
		if (value) {
			fog.am = +2f;
			fog.aa = -1f;
		} else {
			fog.am = +1f;
			fog.aa =  0f;
		}
	}
	
	private void addHeapSprite( Heap heap ) {
		ItemSprite sprite = heap.sprite = (ItemSprite)heaps.recycle( ItemSprite.class );
		sprite.revive();
		sprite.link( heap );
		heaps.add( sprite );
	}
	
	private void addDiscardedSprite( Heap heap ) {
		heap.sprite = (DiscardedItemSprite)heaps.recycle( DiscardedItemSprite.class );
		heap.sprite.revive();
		heap.sprite.link( heap );
		heaps.add( heap.sprite );
	}
	
	private void addPlantSprite( Plant plant ) {
		(plant.sprite = (PlantSprite)plants.recycle( PlantSprite.class )).reset( plant );
	}
	
	private void addBlobSprite( final Blob gas ) {
		if (gas.emitter == null) {
			gases.add( new BlobEmitter( gas ) );
		}
	}
	
	private void addMobSprite( Mob mob ) {
		CharSprite sprite = mob.sprite();
		sprite.visible = Dungeon.visible[mob.pos];
		mobs.add( sprite );
		sprite.link( mob );
	}
	
	private void prompt( String text ) {
		
		if (prompt != null) {
			prompt.killAndErase();
			prompt = null;
		}
		
		if (text != null) {
			prompt = new Toast( text ) {
				@Override
				protected void onClose() {
					cancel();
				}
			};
			prompt.camera = uiCamera;
			prompt.setPos( (uiCamera.width - prompt.width()) / 2, uiCamera.height - 60 );
			add( prompt );
		}
	}
	
	private void showBanner( Banner banner ) {
		banner.camera = uiCamera;
		banner.x = align( uiCamera, (uiCamera.width - banner.width) / 2 );
		banner.y = align( uiCamera, (uiCamera.height - banner.height) / 3 );
		add( banner );
	}
	
	// -------------------------------------------------------
	
	public static void add( Plant plant ) {
		if (scene != null) {
			scene.addPlantSprite( plant );
		}
	}
	
	public static void add( Blob gas ) {
		Actor.add( gas );
		if (scene != null) {
			scene.addBlobSprite( gas );
		}
	}
	
	public static void add( Heap heap ) {
		if (scene != null) {
			scene.addHeapSprite( heap );
		}
	}
	
	public static void discard( Heap heap ) {
		if (scene != null) {
			scene.addDiscardedSprite( heap );
		}
	}
	
	public static void add( Mob mob ) {
		Dungeon.level.mobs.add( mob );
		Actor.add( mob );
		Actor.occupyCell( mob );
		scene.addMobSprite( mob );
	}
	
	public static void add( Mob mob, float delay ) {
		Dungeon.level.mobs.add( mob );
		Actor.addDelayed( mob, delay );
		Actor.occupyCell( mob );
		scene.addMobSprite( mob );
	}
	
	public static void add( EmoIcon icon ) {
		scene.emoicons.add( icon );
	}
	
	public static void effect( Visual effect ) {
		scene.effects.add( effect );
	}
	
	public static Ripple ripple( int pos ) {
		Ripple ripple = (Ripple)scene.ripples.recycle( Ripple.class );
		ripple.reset( pos );
		return ripple;
	}
	
	public static SpellSprite spellSprite() {
		return (SpellSprite)scene.spells.recycle( SpellSprite.class );
	}
	
	public static Emitter emitter() {
		if (scene != null) {
			Emitter emitter = (Emitter)scene.emitters.recycle( Emitter.class );
			emitter.revive();
			return emitter;
		} else {
			return null;
		}
	}
	
	public static FloatingText status() {
		return scene != null ? (FloatingText)scene.statuses.recycle( FloatingText.class ) : null;
	}
	
	public static void pickUp( Item item ) {
		scene.toolbar.pickup( item );
	}
	
	public static void updateMap() {
		if (scene != null) {
			scene.tiles.updated.set( 0, 0, Level.WIDTH, Level.HEIGHT );
		}
	}
	
	public static void updateMap( int cell ) {
		if (scene != null) {
			scene.tiles.updated.union( cell % Level.WIDTH, cell / Level.WIDTH );
		}
	}
	
	public static void discoverTile( int pos, int oldValue ) {
		if (scene != null) {
			scene.tiles.discover( pos, oldValue );
		}
	}
	
	public static void show( Window wnd ) {
		cancelCellSelector();
		scene.add( wnd );
	}
	
	public static void afterObserve() {
		if (scene != null) {
			scene.fog.updateVisibility( Dungeon.visible, Dungeon.level.visited, Dungeon.level.mapped );
			
			for (Mob mob : Dungeon.level.mobs) {
				mob.sprite.visible = Dungeon.visible[mob.pos];
			}
		}
	}
	
	public static void flash( int color ) {
		scene.fadeIn( 0xFF000000 | color, true );
	}
	
	public static void gameOver() {
		Banner gameOver = new Banner( BannerSprites.get( BannerSprites.Type.GAME_OVER ) );
		gameOver.show( 0x000000, 1f );
		scene.showBanner( gameOver );
		
		Sample.INSTANCE.play( Assets.SND_DEATH );
	}
	
	public static void bossSlain() {
		if (Dungeon.hero.isAlive()) {
			Banner bossSlain = new Banner( BannerSprites.get( BannerSprites.Type.BOSS_SLAIN ) );
			bossSlain.show( 0xFFFFFF, 0.3f, 5f );
			scene.showBanner( bossSlain );
			
			Sample.INSTANCE.play( Assets.SND_BOSS );
		}
	}
	
	public static void handleCell( int cell ) {
		cellSelector.select( cell );
	}
	
	public static void selectCell( CellSelector.Listener listener ) {
		cellSelector.listener = listener;
		scene.prompt( listener.prompt() );
	}
	
	private static boolean cancelCellSelector() {
		if (cellSelector.listener != null && cellSelector.listener != defaultCellListener) {
			cellSelector.cancel();
			return true;
		} else {
			return false;
		}
	}
	
	public static WndBag selectItem( WndBag.Listener listener, WndBag.Mode mode, String title ) {
		cancelCellSelector();
		
		WndBag wnd = mode == Mode.SEED ?
			WndBag.seedPouch( listener, mode, title ) :
			WndBag.lastBag( listener, mode, title );

       // PD3D: Send inventory to client
       MsgGetInventory msg = MsgGetInventory.CreateRequest(title, 
         mode.name().toLowerCase(), 
         listener.getClass().getName().toLowerCase(), null);
       Pd3d.pd.AddToRecvQueue(msg);
       Pd3d.game.SetInventoryListener(listener);
//		scene.add( wnd );
		return wnd;
	}

	static boolean cancel() {
		if (Dungeon.hero.curAction != null || Dungeon.hero.restoreHealth) {
			
			Dungeon.hero.curAction = null;
			Dungeon.hero.restoreHealth = false;
			return true;
			
		} else {
			
			return cancelCellSelector();
			
		}
	}
	
	public static void ready() {
		selectCell( defaultCellListener );
		QuickSlot.cancel();
	}
	
	private static final CellSelector.Listener defaultCellListener = new CellSelector.Listener() {
		@Override
		public void onSelect( Integer cell ) {
			if (Dungeon.hero.handle( cell )) {
				Dungeon.hero.next();
			}
		}
		@Override
		public String prompt() {
			return null;
		}
	};

    // *************************************************************************
    // IHook
    // *************************************************************************
    @Override public void OnInit() {
        Pd3d.game.GetStep().SetState(Step.State.DISABLE);
        Pd3d.game.GetDeadMobs().Clear();
    }

    // *************************************************************************
    // IRequestHandler
    // *************************************************************************
    @Override public boolean OnRecvMsgUpdateScene(
      MsgUpdateScene req, MsgUpdateScene resp) {
        // Set map size
        DescSceneGame desc = resp.game_scene = new DescSceneGame();
        int w = desc.map_width = Level.WIDTH;
        int h = desc.map_height = Level.HEIGHT;

        // Step counter
        desc.step = Pd3d.game.GetStep().cnt;

        // Set chars
        DescChar hero_desc = null;
        LinkedList<Mob> dead_mobs = Pd3d.game.GetDeadMobs().PopList();
        LinkedList<Char> chars = new LinkedList<Char>();
        chars.add(Dungeon.hero);
        chars.addAll(Dungeon.level.mobs);
        if(dead_mobs != null) {
            chars.addAll(dead_mobs);
        }
        desc.chars = new LinkedList<DescChar>();
        for(Char c : chars) {
            DescChar desc_char = Pd3d.game.GetCharDesc(c);
            desc.chars.add(desc_char);
            if(c.id() == 1) {
                hero_desc = desc_char;
            }
        }

        // Set heaps
        int heap_num = Dungeon.level.heaps.size();
        desc.heaps = (heap_num > 0) ? new LinkedList<DescHeap>() : null;
        for(int i = 0; i < heap_num; i++) {
            Heap heap = Dungeon.level.heaps.valueAt(i);
            DescHeap heap_desc = new DescHeap();
            heap_desc.pos = heap.pos;
            heap_desc.sprite_id = heap.sprite.pd3d_obj_id;
            desc.heaps.add(heap_desc);
        }

        // Set plants
        int plant_num = Dungeon.level.plants.size();
        desc.plants = (plant_num > 0) ? new LinkedList<DescHeap>() : null;
        for(int i = 0; i < plant_num; i++) {
            Plant plant = Dungeon.level.plants.valueAt(i);
            DescHeap heap_desc = new DescHeap();
            heap_desc.pos = plant.pos;
            heap_desc.sprite_id = plant.sprite.pd3d_obj_id;
            desc.plants.add(heap_desc);
        }

        // Camera events
        if(Camera.main.pd3d_shake_event) {
            Camera.main.pd3d_shake_event = false;
            Pd3d.game.AddEvent(MapEnum.EventType.CAMERA_SHAKE);
        }

        // Emitter events
        LinkedList<EmitterDesc> emitters = Pd3d.game.GetEmitters().PopList();
        if(emitters != null) {
            for(EmitterDesc e : emitters) {
                // Target of emitter
                Integer target = (e.target instanceof Integer) ? 
                  (Integer)e.target : null;
                if(target == null) {
                    continue;
                }

                // Emitter event
                DescEvent event = (DescEvent)e.emitter.factory.Pd3dGetEvent();
                if(event == null) {
                    Logger.e("Ignoring emitter event :: factory=%s", 
                      e.emitter.factory.toString());
                    continue;
                }

                event.cell_id = target;
                Pd3d.game.AddEvent(event);
            }
            emitters.clear();
        }

        // Events
        desc.events = Pd3d.game.GetEvents().PopList();

        // Interrupt hero movement
        desc.interrupt = Dungeon.hero.pd3d_interrupt;
        Dungeon.hero.pd3d_interrupt = null;
        if(desc.interrupt == null && dead_mobs != null) {
            desc.interrupt = "target-reached";
        }

        // Loot item
        pd3d_sp.loot.update();
        if(pd3d_sp.loot.lastItem != null) {
            desc.loot_item_id = pd3d_sp.loot.lastItem.image();
        }

        // Quickslots
        desc.quickslot0 = Pd3d.game.GetItemDesc(
          QuickSlot.Pd3dGetItem(0), WndBag.Mode.ALL);
        desc.quickslot1 = Pd3d.game.GetItemDesc(
          QuickSlot.Pd3dGetItem(1), WndBag.Mode.ALL);

        // Hero stats
        desc.hero_stats = Pd3d.game.GetHeroStatsDesc(hero_desc);

        // Dungeon depth
        desc.dungeon_depth = Dungeon.depth;

        // Reset checksum of the map
        desc.map_csum = 0;

        // Update map
        ClientAPI client_api = Pd3d.pd.GetClientAPI();
        Map map = (client_api != null) ? 
          client_api.GetMap(w, h) : Pd3d.game.GetMap(w, h);
        Pd3dGame.LevelDesc level = new Pd3dGame.LevelDesc(Dungeon.level);
        ICell[] cells = map.GetCells();
        for(int i = 0; i < w * h; i++) {
            MapCell cell = (MapCell)cells[i];
            TerrainType new_terrain_type = level.GetType(i);
            cell.flags = level.GetFlags(i,
              (new_terrain_type != cell.type), cell.flags);
            cell.type = new_terrain_type;
            desc.map_csum += cell.GetCsum();
        }

        // Serialize map
        if(client_api == null) {
            desc.map_srlz = map.SerializeMap();
        }
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnRecvMsgHeroInteract(
      MsgHeroInteract req, MsgHeroInteract resp) {
        int hero_pos = Dungeon.hero.pos;
        Step.State step_state = null;

        // Move hero
        String type = req.interact_type;
        if(type.startsWith("move-")) {
            type = type.substring("move-".length());

            // Select destination cell
            int dest_idx = hero_pos;
            int next_step_idx = -1;
            switch(type) {
            // Find destination cell relative to hero position
            case "n":   dest_idx -= Level.WIDTH;       break;  // Up
            case "s":   dest_idx += Level.WIDTH;       break;  // Down
            case "e":   dest_idx += 1;                 break;  // Left
            case "w":   dest_idx -= 1;                 break;  // Right
            case "ne":  dest_idx -= (Level.WIDTH - 1); break;  // Up-Right
            case "nw":  dest_idx -= (Level.WIDTH + 1); break;  // Up-Left
            case "se":  dest_idx += Level.WIDTH + 1;   break;  // Down-Right
            case "sw":  dest_idx += Level.WIDTH - 1;   break;  // Down-Left

            // Find destination cell by index
            default: 
                try {
                    dest_idx = Integer.parseInt(type);
                }
                catch(Exception ex) {
                    Utils.LogException(ex, "Failed to parse cell index :: idx=%s", type);
                    dest_idx = -1;
                }
            }

            // Validate destination index
            if(dest_idx < 0 || dest_idx >= Level.LENGTH) {
                dest_idx = hero_pos;
            }

            // Reach destination in one step
            Dungeon.hero.pd3d_dest_in_one_step = Level.adjacent(hero_pos, dest_idx);
            if(Dungeon.hero.pd3d_dest_in_one_step) {
                next_step_idx = dest_idx;

            // Get closer to target cell if more than one step away
            } else {
                next_step_idx = Dungeon.findPath(Dungeon.hero, hero_pos, dest_idx, 
                 Level.passable, Level.fieldOfView);
            }

            // Do interrupt if target can not be reached
            if(next_step_idx == -1) {
                Dungeon.hero.pd3d_interrupt = "target-reached";

            // Target can be reached
            } else {
                Dungeon.hero.handle(next_step_idx);

                // Check if hero needs to be interrupted
                if(req.try_interrupt) {
                    // Hero reached destination cell
                    hero_pos = Dungeon.hero.pos;
                    if(dest_idx == hero_pos) {
                        Dungeon.hero.pd3d_interrupt = "target-reached";
    
                    // Hero stopped one step from destination cell
                    } else if(Dungeon.hero.pd3d_dest_in_one_step && 
                      Level.adjacent(hero_pos, dest_idx)) {
                        // Destination cell is not reachable or it is occupied by enemy char
                        if(!Level.passable[dest_idx] || Actor.findChar(dest_idx) != null) {
                            Dungeon.hero.pd3d_interrupt = "target-reached";
                        }
                    }
                }
            }
            step_state = Step.State.ENABLE_ONE;

        // Select cell
        } else if(type.equals("select-cell")) {
            Dungeon.hero.handle(req.cell_idx);
            step_state = Step.State.ENABLE_ONE;

        // Search nearby cells
        } else if(type.equals("search")) {
            if(pd3d_sp.loot.lastItem != null) {
                Dungeon.hero.handle(Dungeon.hero.pos);
//                Dungeon.hero.sprite.showStatus(CharSprite.DEFAULT, "^^^");
            } else {
                Dungeon.hero.search(true);
            }
            step_state = Step.State.ENABLE_ONE;

        // Rest
        } else if(type.equals("rest")) {
            Dungeon.hero.rest(false);
            step_state = Step.State.ENABLE_ONE;

        // Rest until hero restores full health
        } else if(type.equals("rest-till-healthy")) {
            Dungeon.hero.rest(true);
            step_state = Step.State.ENABLE_ONE;

        // Idle
        } else if(type.equals("idle")) {
            step_state = Step.State.ENABLE_ALL;
        }

        // Move game time forward
        if(step_state != null) {
            if(Actor.current != null) {
                Actor.current.next();
            }
            Pd3d.game.GetStep().SetState(step_state);
        }
        return false;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnRecvMsgGetInventory(
      MsgGetInventory req, MsgGetInventory resp) {
        // Get mode
        WndBag.Mode mode = null;
        try {
            mode = WndBag.Mode.valueOf(req.mode.toUpperCase());
        } catch(Exception ex) {
            Utils.LogException(ex, "Failed to get inventory, wrong mode");
            return true;
        }

        // Set mode & listener
        resp.mode = mode.toString().toLowerCase();
        resp.listener = req.listener;
        resp.listener_ex = req.listener_ex;

        // Gold
        resp.gold_num = Dungeon.gold;

        // Quickslot mode
        if(resp.listener.equals("quickslot")) {
            resp.title = QuickSlot.TXT_SELECT_ITEM + " #" + resp.listener_ex;

        // Other modes
        } else {
            resp.title = req.title;
        }

        // Create list of bags
        LinkedList<Bag> bags = new LinkedList<Bag>();
        bags.add(Dungeon.hero.belongings.backpack);
        for(Item item : Dungeon.hero.belongings.backpack) {
            if(item instanceof Bag) {
                bags.add((Bag)item);
            }
        }

        // Set bags
        resp.bags = new LinkedList<DescBag>();
        for(Bag bag : bags) {
            DescBag bag_desc = new DescBag();
            bag_desc.name = bag.name();
            bag_desc.items = (bag.items.size() > 0) ? new LinkedList<DescItem>() : null;
            for(Item item : bag.items) {
                bag_desc.items.add(Pd3d.game.GetItemDesc(item, mode));
            }
            resp.bags.add(bag_desc);
        }

        // Put all equipped items in virtual "equipped" bag
        Item equipped_items[] = new Item[] {
            Dungeon.hero.belongings.weapon,
            Dungeon.hero.belongings.armor,
            Dungeon.hero.belongings.ring1,
            Dungeon.hero.belongings.ring2,
        };
        DescBag equipped_bag = new DescBag();
        equipped_bag.name = "equipped";
        for(Item item : equipped_items) {
            if(item == null) {
                continue;
            }
            if(equipped_bag.items == null) {
                equipped_bag.items = new LinkedList<DescItem>();
            }
            equipped_bag.items.add(Pd3d.game.GetItemDesc(item, mode));
        }
        resp.bags.add(equipped_bag);
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnRecvMsgSelectQuickslotItem(
      MsgSelectQuickslotItem req, MsgSelectQuickslotItem resp) {
        // Find item by index
        Item item = Pd3d.game.GetInventoryItem(req.item_idx, null);
        if(item == null) {
            Logger.e("Failed to select quickslot item, item not found :: quickslot=%d item=%d", 
              req.quickslot_idx, req.item_idx);
            return true;
        }

        // Put item to quickslot
        QuickSlot.Pd3dPutItem(req.quickslot_idx, item);
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnRecvMsgSelectInventoryItem(
      MsgSelectInventoryItem req, MsgSelectInventoryItem resp) {
        // Find item by index
        Item item = Pd3d.game.GetInventoryItem(req.item_idx, null);
        if(item == null) {
            Logger.e("Failed to select inventory item, item not found :: item=%d", 
              req.item_idx);
            return true;
        }

        // Get inventory listener
        WndBag.Listener listener = Pd3d.game.GetInventoryListener();
        if(listener == null) {
            Logger.e("Failed to select inventory item, no inventory listener :: item=%d", 
              req.item_idx);
            return true;
        }

        // Run selection handler
        listener.onSelect(item);

        // Move game time forward by one step
        Pd3d.game.GetStep().SetState(Step.State.ENABLE_ONE);
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnRecvMsgRunItemAction(
      MsgRunItemAction req, MsgRunItemAction resp) {
        // Search for item in quickslots if item index is negative
        Item item = null;
        if(req.item_idx < 0) {
            req.item_idx = -req.item_idx - 1;
            item = QuickSlot.Pd3dGetItem(req.item_idx);

        // Search for item in inventory
        } else { 
            item = Pd3d.game.GetInventoryItem(req.item_idx, null);
        }

        // Item should be present
        if(item == null) {
            resp.SetStatus(false, "Failed to run item action, no item");
            return true;
        }

        // Set action
        resp.action = req.action;

        // Run item action on target cell
        if(req.dest_cell_idx != null) {
            if(req.action.equals("throw")) {
                Item.Pd3dThrow(Dungeon.hero, item, req.dest_cell_idx);
            } else if(req.action.equals("zap")) {
                Wand.Pd3dZap(Dungeon.hero, item, req.dest_cell_idx);
            } else {
                resp.SetStatus(false, "Failed to run remote item action");
                return true;
            }
            resp.src_cell_idx = Dungeon.hero.pos;
            resp.dest_cell_idx = Pd3d.game.GetTargetCell();
            resp.sprite_id = item.image();
            resp.do_circle_back = Pd3d.game.GetCircleBack();

        // Execute default item action
        } else {
            item.execute(Dungeon.hero, req.action.toUpperCase());
        }

        // Move game time forward by one step
        Pd3d.game.GetStep().SetState(Step.State.ENABLE_ONE);
        return true;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnRecvMsgQuestStart(MsgQuestStart req, MsgQuestStart resp) {
        // Copy quest data from request
        resp.quest = req.quest;

        // Process quests
        switch(req.quest.name) {
        //......................................................................
        case "read-tome-of-mastery": {
            Item item = Pd3d.game.GetQuestItem(
              req.quest.name, req.quest.target_item_id);
            if(item == null) {
                resp.SetStatus(false, 
                  "Failed to start quest, no item :: name=%s", req.quest.name);
                break;
            }

            // Start quest
            item.execute(Dungeon.hero, TomeOfMastery.AC_READ);
        } break;

        //......................................................................
        case "buy-item": {
            // Start quest
            Dungeon.hero.handle(req.quest.target_cell_id);
        } break;

        //......................................................................
        case "sell-item": 
        case "apply-weightstone": {
            // Find item in inventory
            Item item = Pd3d.game.GetInventoryItem(
              req.quest.target_item_id, null);
            if(item == null) {
                resp.SetStatus(false, 
                  "Failed to start quest, no item :: name=%s", req.quest.name);
                break;
            }

            // Get inventory listener
            WndBag.Listener listener = Pd3d.game.GetInventoryListener();
            if(listener == null) {
                resp.SetStatus(false, 
                  "Failed to start quest, no inventory listener :: item=%d", 
                  item.m_pd3d_id);
                break;
            }

            // Start quest
            Logger.d("Running inventory listener :: listener=%s", listener.toString());
            listener.onSelect(item);
        } break;

        //......................................................................
        case "jump-chasm": {
            Chasm.heroJump(Dungeon.hero);
        } break;

        //......................................................................
        // Default NPC quest
        default: {
            NPC npc = Pd3d.game.GetQuestNpc(
              req.quest.name, req.quest.target_char_id); 
            if(npc == null) {
                resp.SetStatus(false, 
                  "Failed to start quest, no char :: name=%s", req.quest.name);
                break;
            }

            // Start quest
            npc.interact();
        } break;
        }
        return req.quest.need_response;
    }

    //--------------------------------------------------------------------------
    @Override public boolean OnRecvMsgQuestAction(
      MsgQuestAction req, MsgQuestAction resp) {
        // Run quest action
        Pd3d.game.RunQuestAction(req.action);

        // Post-quest actions
        String qname = Pd3d.game.GetQuestName();
        switch(qname) {
        case "sell-item": {
            Logger.d("Restarting %s quest", qname);
            Shopkeeper.sell();
        }
        }
        return true;
    }
}
