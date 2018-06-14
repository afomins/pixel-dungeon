//------------------------------------------------------------------------------
package com.matalok.pd3d;

//------------------------------------------------------------------------------
import java.util.HashMap;
import java.util.LinkedList;

import com.matalok.pd3d.desc.DescAnim;
import com.matalok.pd3d.desc.DescRect;
import com.matalok.pd3d.desc.DescSprite;
import com.matalok.pd3d.map.MapEnum;
import com.matalok.pd3d.map.MapEnum.StatusPaneType;
import com.matalok.pd3d.shared.Logger;
import com.matalok.pd3d.shared.Utils;
import com.matalok.pd3d.shared.UtilsClass;
import com.matalok.pd3d.shared.UtilsClass.SmartList;
import com.matalok.pd3d.shared.UtilsClass.Vector2i;
import com.watabou.noosa.MovieClip.Animation;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.effects.BannerSprites;
import com.watabou.pixeldungeon.scenes.SurfaceScene;
import com.watabou.pixeldungeon.scenes.TitleScene;
import com.watabou.pixeldungeon.sprites.*;
import com.watabou.pixeldungeon.ui.Icons;
import com.watabou.pixeldungeon.ui.Toolbar;

//------------------------------------------------------------------------------
public class Pd3dSprite {
    //**************************************************************************
    // INTERFACE
    //**************************************************************************
    public interface ISprite {
        //----------------------------------------------------------------------
        public String Pd3dGetTextureName();
        public int Pd3dGetTileOffset();
        public String Pd3dGetObjectType();
        public UtilsClass.Vector2i Pd3dGetTileSize();
        public LinkedList<Animation> Pd3dGetAnimations(LinkedList<Animation> list);
    }

    //**************************************************************************
    // CLASS
    //**************************************************************************
    class DummyTilemapSprite
      implements ISprite {
        //------------------------------------------------------------------
        private String m_texture;
        private int m_frame_id;
        private String m_obj_type;
        private UtilsClass.Vector2i m_tile_size;

        //------------------------------------------------------------------
        public DummyTilemapSprite(String texture, int frame_id, 
          String obj_type, int tile_width, int tile_height) {
            m_texture = texture;
            m_frame_id = frame_id;
            m_obj_type = obj_type;
            m_tile_size = new UtilsClass.Vector2i(tile_width, tile_height);
        }

        //------------------------------------------------------------------
        @Override public String Pd3dGetTextureName() {
            return m_texture;
        }

        //------------------------------------------------------------------
        @Override public int Pd3dGetTileOffset() {
            return 0;
        }

        //------------------------------------------------------------------
        @Override public String Pd3dGetObjectType() {
            return m_obj_type;
        }

        //------------------------------------------------------------------
        @Override public Vector2i Pd3dGetTileSize() {
            return m_tile_size;
        }

        //------------------------------------------------------------------
        @Override public LinkedList<Animation> Pd3dGetAnimations(
          LinkedList<Animation> list) {
            // Create "idle" animation which consists of single frame
            Animation anim = new Animation("idle", 1, false);
            anim.pd3d_frames = new Integer[] { m_frame_id };
            list.add(anim);
            return list;
        }
    }

    //**************************************************************************
    // Pd3dSprite
    //**************************************************************************
    private HashMap<Class<?>, HashMap<String, Integer>> m_class_to_tx_id_map;
    private HashMap<String, HashMap<Integer, DescSprite>> m_obj_type_to_id_desc_map;
    private SmartList<DescSprite> m_update_list;
    private String m_terrain;

    //--------------------------------------------------------------------------
    public Pd3dSprite() {
        m_class_to_tx_id_map = 
          new HashMap<Class<?>, HashMap<String, Integer>>();
        m_obj_type_to_id_desc_map = 
          new HashMap<String, HashMap<Integer, DescSprite>>();
        m_update_list = new SmartList<DescSprite>();
    }

    //--------------------------------------------------------------------------
    public void SetTerrain(String terrain, String water) {
        if(m_terrain == null || !m_terrain.equals(terrain)) {
            RegisterTerrainSprites(terrain);
            RegisterWaterSprites(water);
            m_terrain = terrain;
        }
    }

    //--------------------------------------------------------------------------
    public SmartList<DescSprite> GetUpdateList() {
        return m_update_list;
    }

    //--------------------------------------------------------------------------
    public void FillUpdateList() {
        m_update_list.Clear();
        for(HashMap<Integer, DescSprite> class_map : 
          m_obj_type_to_id_desc_map.values()) {
            for(DescSprite desc : class_map.values()) {
                m_update_list.Add(desc);
            }
        }
    }

    //--------------------------------------------------------------------------
    public int GetObjectId(ISprite sprite) {
        HashMap<String, Integer> tx_id_map = 
          m_class_to_tx_id_map.get(sprite.getClass());
        Utils.Assert(tx_id_map != null, 
          "Failed to get sprite's obj-id, base ID is unknown :: #1");

        Integer obj_id = tx_id_map.get(sprite.Pd3dGetTextureName());
        Utils.Assert(obj_id != null, 
          "Failed to get sprite's obj-id, base ID is unknown :: #2");
        return obj_id + sprite.Pd3dGetTileOffset();
    }

    //--------------------------------------------------------------------------
    private DescSprite RegisterSprite(int obj_id, Class<?> sprite_class, 
      ISprite sprite) {
        //
        // Create sprite descriptor
        //
        DescSprite desc = new DescSprite();
        Vector2i tile_size = sprite.Pd3dGetTileSize();
        desc.obj_id = obj_id;
        desc.obj_type = sprite.Pd3dGetObjectType();
        desc.texture = sprite.Pd3dGetTextureName();
        desc.tile_offset = sprite.Pd3dGetTileOffset();
        desc.tile_width = tile_size.x;
        desc.tile_height = tile_size.y;

        // Write animation descriptor
        for(Animation anim : 
          sprite.Pd3dGetAnimations(new LinkedList<Animation>())) {
            if(anim == null) {
                continue;
            }

            // Create animation
            DescAnim anim_desc = new DescAnim();
            anim_desc.name = anim.pd3d_name;
            anim_desc.fps = anim.pd3d_fps;
            anim_desc.is_looped = anim.looped;

            // Fill animation frames
            anim_desc.frames = (anim.pd3d_frames.length > 0) ? new LinkedList<Integer>() : null;
            for(Object frame_idx : anim.pd3d_frames) {
                if(!(frame_idx instanceof Integer)) {
                    frame_idx = Integer.valueOf(0);
                    Logger.e("Failed to parse animation, index not an integer");
                }
                anim_desc.frames.add((Integer)frame_idx);
            }

            if(desc.anims == null) {
                desc.anims = new HashMap<String, DescAnim>();
            }
            desc.anims.put(anim.pd3d_name, anim_desc);
        }

        //
        // Register sprite descriptor
        //

        // Map [class -> <tx, id>]
        HashMap<String, Integer> tx_id_map = m_class_to_tx_id_map.get(sprite_class);
        if(tx_id_map == null) {
            tx_id_map = new HashMap<String, Integer>();
            m_class_to_tx_id_map.put(sprite_class, tx_id_map);
        }

        // Map [tx -> id]
        if(!tx_id_map.containsKey(desc.texture)) {
            tx_id_map.put(desc.texture, obj_id);
        }

        // Map [type -> <id, desc>]
        HashMap<Integer, DescSprite> id_desc_map = 
          m_obj_type_to_id_desc_map.get(desc.obj_type);
        if(id_desc_map == null) {
            id_desc_map = new HashMap<Integer, DescSprite>();
            m_obj_type_to_id_desc_map.put(desc.obj_type, id_desc_map);
        }
        id_desc_map.put(obj_id, desc);

        // Rect are needed only if tile size was not defined
        if(tile_size.x == 0) {
            desc.rects = new LinkedList<DescRect>();
        }
        return desc;
    }

    //--------------------------------------------------------------------------
    public void RegisterCharSprites() {
        Utils.Assert(!m_obj_type_to_id_desc_map.containsKey("char"), 
          "Failed to register char sprites twice");

        // Hero sprites
        int obj_id = 0;
        String hero_tx[] = new String[] { 
          Assets.WARRIOR, Assets.MAGE, Assets.ROGUE, Assets.HUNTRESS };
        for(String tx : hero_tx) {
            for(int i = 0; i < 7; i++) { // Each hero can have 7 armors
                RegisterSprite(obj_id++, HeroSprite.class, new HeroSprite(tx, i));
            }
        }

        // Mirror sprites
        for(String tx : hero_tx) {
            for(int i = 0; i < 7; i++) { // Each hero can have 7 armors
                RegisterSprite(obj_id++, MirrorSprite.class, new MirrorSprite(tx, i));
            }
        }

        // Mob sprites
        Class<?>[] mob_classes = new Class<?>[] {
            AcidicSprite.class,      AlbinoSprite.class,   BanditSprite.class,               BatSprite.class,      BeeSprite.class,        BlacksmithSprite.class,  BruteSprite.class, 
            BurningFistSprite.class, CrabSprite.class,     CursePersonificationSprite.class, DM300Sprite.class,    ElementalSprite.class,  EyeSprite.class,         FetidRatSprite.class,
            GhostSprite.class,       GnollSprite.class,    GolemSprite.class,                GooSprite.class,      ImpSprite.class,        KingSprite.class,        LarvaSprite.class,
            MimicSprite.class,       MonkSprite.class,     PiranhaSprite.class,              RatKingSprite.class,  RatSprite.class,        RottingFistSprite.class, ScorpioSprite.class,
            SeniorSprite.class,      ShamanSprite.class,   SheepSprite.class,                ShieldedSprite.class, ShopkeeperSprite.class, SkeletonSprite.class,    SpinnerSprite.class,
            StatueSprite.class,      SuccubusSprite.class, SwarmSprite.class,                TenguSprite.class,    ThiefSprite.class,      UndeadSprite.class,      WandmakerSprite.class,
            WarlockSprite.class,     WraithSprite.class,   YogSprite.class,
        };
        for(Class<?> obj_class : mob_classes) {
            try {
                RegisterSprite(obj_id++, obj_class, (CharSprite)obj_class.newInstance());
            } catch (Exception ex) {
                Utils.LogException(ex, "Failed to instantiate ");
                Utils.Assert(false, "fuck");
            }
        }
    }

    //--------------------------------------------------------------------------
    public void RegisterItemSprites() {
        Utils.Assert(!m_obj_type_to_id_desc_map.containsKey("item"), 
          "Failed to register item sprites twice");

        for(int i = 0; i < MapEnum.ItemType.GetSize(); i++) {
            RegisterSprite(i, DummyTilemapSprite.class, 
              new DummyTilemapSprite(
                Assets.ITEMS, i, "item", ItemSprite.SIZE, ItemSprite.SIZE));
        }
    }

    //--------------------------------------------------------------------------
    public void RegisterPlantSprites() {
        Utils.Assert(!m_obj_type_to_id_desc_map.containsKey("plant"), 
          "Failed to register plant sprites twice");

        for(int i = 0; i < MapEnum.PlantType.GetSize(); i++) {
            RegisterSprite(i, DummyTilemapSprite.class, 
              new DummyTilemapSprite(
                Assets.PLANTS, i, "plant", 16, 16));
        }
    }

    //--------------------------------------------------------------------------
    public void RegisterTerrainSprites(String texture) {
        // Register new sprites
        if(!m_obj_type_to_id_desc_map.containsKey("terrain")) {
            for(int i = 0; i < MapEnum.TerrainType.GetSize(); i++) {
                RegisterSprite(i, DummyTilemapSprite.class, 
                  new DummyTilemapSprite(
                    texture, i, "terrain", DungeonTilemap.SIZE, DungeonTilemap.SIZE));
            }

        // Update texture of existing sprites
        } else {
            for(DescSprite desc : m_obj_type_to_id_desc_map.get("terrain").values()) {
                desc.texture = texture;
                m_update_list.Add(desc);
            }
        }
    }

    //--------------------------------------------------------------------------
    public void RegisterWaterSprites(String texture) {
        // Register new sprite
        if(!m_obj_type_to_id_desc_map.containsKey("water")) {
            RegisterSprite(0, DummyTilemapSprite.class, 
              new DummyTilemapSprite(texture, 0, "water", 32, 32));

        // Update texture of existing sprite
        } else {
            DescSprite desc = 
              m_obj_type_to_id_desc_map.get("water").values().iterator().next();
            desc.texture = texture;
            m_update_list.Add(desc);
        }
    }

    //--------------------------------------------------------------------------
    public void RegisterIconSprites() {
        Utils.Assert(!m_obj_type_to_id_desc_map.containsKey("icon"), 
          "Failed to register icon sprites twice");

        for(Icons icon : Icons.values()) {
            DescSprite desc = RegisterSprite(icon.ordinal(), DummyTilemapSprite.class, 
              new DummyTilemapSprite(Assets.ICONS, 0, "icon", 0, 0));
            desc.rects.add(new DescRect().Set(
              icon.pd3d_x0, icon.pd3d_y0, 
              icon.pd3d_x1 - icon.pd3d_x0,
              icon.pd3d_y1 - icon.pd3d_y0));
        }
    }

    //--------------------------------------------------------------------------
    public void RegisterBannerSprites() {
        Utils.Assert(!m_obj_type_to_id_desc_map.containsKey("banner"), 
          "Failed to register banner sprites twice");

        for(BannerSprites.Type banner : BannerSprites.Type.values()) {
            DescSprite desc = RegisterSprite(banner.ordinal(), DummyTilemapSprite.class, 
              new DummyTilemapSprite(Assets.BANNERS, 0, "banner", 0, 0));
            desc.rects.add(new DescRect().Set(
              banner.pd3d_x0, banner.pd3d_y0, 
              banner.pd3d_x1 - banner.pd3d_x0,
              banner.pd3d_y1 - banner.pd3d_y0));
        }
    }

    //--------------------------------------------------------------------------
    public void RegisterDashboardItemSprites() {
        Utils.Assert(!m_obj_type_to_id_desc_map.containsKey("dashboard-item"), 
          "Failed to register dashboard item sprites twice");

        for(int i = 0; i < MapEnum.DashboardItemType.GetSize(); i++) {
            RegisterSprite(i, DummyTilemapSprite.class, 
              new DummyTilemapSprite(
                Assets.DASHBOARD, i, "dashboard-item", 
                TitleScene.DashboardItem.IMAGE_SIZE, 
                TitleScene.DashboardItem.IMAGE_SIZE));
        }
    }

    //--------------------------------------------------------------------------
    public void RegisterAvatarSprites() {
        Utils.Assert(!m_obj_type_to_id_desc_map.containsKey("avatar"), 
          "Failed to register avatar sprites twice");

        for(int i = 0; i < MapEnum.AvatarType.GetSize(); i++) {
            RegisterSprite(i, DummyTilemapSprite.class, 
              new DummyTilemapSprite(
                Assets.AVATARS, i, "avatar", 
                SurfaceScene.Avatar.WIDTH, SurfaceScene.Avatar.HEIGHT));
        }
    }

    //--------------------------------------------------------------------------
    public void RegisterToolbarSprites() {
        Utils.Assert(!m_obj_type_to_id_desc_map.containsKey("toolbar"), 
          "Failed to register toolbar sprites twice");

        for(Toolbar.Icons icon : Toolbar.Icons.values()) {
            DescSprite desc = RegisterSprite(icon.ordinal(), DummyTilemapSprite.class, 
              new DummyTilemapSprite(Assets.TOOLBAR, 0, "toolbar", 0, 0));
            desc.rects.add(new DescRect().Set(
              icon.pd3d_x0, icon.pd3d_y0, 
              icon.pd3d_x1 - icon.pd3d_x0,
              icon.pd3d_y1 - icon.pd3d_y0));
        }
    }

    //--------------------------------------------------------------------------
    public void RegisterBuffSprites() {
        Utils.Assert(!m_obj_type_to_id_desc_map.containsKey("buff"), 
          "Failed to register buff sprites twice");

        for(int i = 0; i < MapEnum.BuffType.GetSize(); i++) {
            RegisterSprite(i, DummyTilemapSprite.class, 
              new DummyTilemapSprite(
                Assets.BUFFS_LARGE, i, "buff", 16, 16));
        }
    }

    //--------------------------------------------------------------------------
    public void RegisterStatusPaneSprites() {
        Utils.Assert(!m_obj_type_to_id_desc_map.containsKey("status-pane"), 
          "Failed to register status-pane sprites twice");

        RegisterSprite(StatusPaneType.HERO.ordinal(), DummyTilemapSprite.class, 
          new DummyTilemapSprite(Assets.STATUS, 0, "status-pane", 0, 0))
          .rects.add(new DescRect().Set(0, 1, 34, 34));

        RegisterSprite(StatusPaneType.MENU.ordinal(), DummyTilemapSprite.class, 
          new DummyTilemapSprite(Assets.STATUS, 0, "status-pane", 0, 0))
          .rects.add(new DescRect().Set(107, 1, 21, 21));

        RegisterSprite(StatusPaneType.BORDER.ordinal(), DummyTilemapSprite.class, 
          new DummyTilemapSprite(Assets.STATUS, 0, "status-pane", 0, 0))
          .rects.add(new DescRect().Set(42, 0, 12, 3));

        RegisterSprite(StatusPaneType.DEPTH.ordinal(), DummyTilemapSprite.class, 
          new DummyTilemapSprite(Assets.STATUS, 0, "status-pane", 0, 0))
          .rects.add(new DescRect().Set(112, 16, 15, 15));
    }

    //--------------------------------------------------------------------------
    public void RegisterPfxImageSprites() {
        Utils.Assert(!m_obj_type_to_id_desc_map.containsKey("pfx-image"), 
          "Failed to register pfx-image sprites twice");

        for(int i = 0; i < MapEnum.PfxImage.GetSize(); i++) {
            RegisterSprite(i, DummyTilemapSprite.class, 
              new DummyTilemapSprite(
                "particles/particles.png", i, "pfx-image", 32, 32));
        }
    }

    //--------------------------------------------------------------------------
    public void RegisterSprites() {
        // Register sprites once
        RegisterCharSprites();
        RegisterItemSprites();
        SetTerrain(Assets.TILES_SEWERS, Assets.WATER_SEWERS);
//        RegisterTerrainSprites(Assets.TILES_SEWERS);
//        RegisterWaterSprites(Assets.WATER_SEWERS);
        RegisterIconSprites();
        RegisterBannerSprites();
        RegisterDashboardItemSprites();
        RegisterAvatarSprites();
        RegisterToolbarSprites();
        RegisterStatusPaneSprites();
        RegisterBuffSprites();
        RegisterPlantSprites();
        RegisterPfxImageSprites();
    }
}
