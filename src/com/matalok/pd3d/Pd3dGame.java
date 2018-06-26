/*
 * Pixel Dungeon 3D
 * Copyright (C) 2016-2018 Alex Fomins
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

//------------------------------------------------------------------------------
package com.matalok.pd3d;

//------------------------------------------------------------------------------
import java.util.HashMap;
import java.util.LinkedList;

import com.matalok.pd3d.desc.DescChar;
import com.matalok.pd3d.desc.DescEvent;
import com.matalok.pd3d.desc.DescStringInst;
import com.matalok.pd3d.desc.DescHeroStats;
import com.matalok.pd3d.desc.DescItem;
import com.matalok.pd3d.desc.DescQuest;
import com.matalok.pd3d.desc.DescQuestAction;
import com.matalok.pd3d.map.Map;
import com.matalok.pd3d.map.MapCell;
import com.matalok.pd3d.map.MapEnum;
import com.matalok.pd3d.msg.MsgUpdateScene;
import com.matalok.pd3d.shared.Logger;
import com.matalok.pd3d.shared.Utils;
import com.matalok.pd3d.shared.UtilsClass;
import com.matalok.pd3d.shared.UtilsClass.SmartList;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.particles.Emitter;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.Statistics;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.blobs.Alchemy;
import com.watabou.pixeldungeon.actors.blobs.Blob;
import com.watabou.pixeldungeon.actors.blobs.ConfusionGas;
import com.watabou.pixeldungeon.actors.blobs.Foliage;
import com.watabou.pixeldungeon.actors.blobs.ParalyticGas;
import com.watabou.pixeldungeon.actors.blobs.Regrowth;
import com.watabou.pixeldungeon.actors.blobs.SacrificialFire;
import com.watabou.pixeldungeon.actors.blobs.ToxicGas;
import com.watabou.pixeldungeon.actors.blobs.WaterOfAwareness;
import com.watabou.pixeldungeon.actors.blobs.WaterOfHealth;
import com.watabou.pixeldungeon.actors.blobs.WaterOfTransmutation;
import com.watabou.pixeldungeon.actors.blobs.Web;
import com.watabou.pixeldungeon.actors.blobs.WellWater;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.npcs.Blacksmith;
import com.watabou.pixeldungeon.actors.mobs.npcs.Ghost;
import com.watabou.pixeldungeon.actors.mobs.npcs.NPC;
import com.watabou.pixeldungeon.actors.mobs.npcs.Wandmaker;
import com.watabou.pixeldungeon.actors.mobs.npcs.Imp;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.TomeOfMastery;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.ItemSlot;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.windows.WndBag;
import com.watabou.pixeldungeon.windows.WndQuest;

//------------------------------------------------------------------------------
public class Pd3dGame {
    //**************************************************************************
    // CLASS
    //**************************************************************************
    private class DummyItemSlot
      extends ItemSlot {
        @Override protected void createChildren() { 
            icon = new ItemSprite();
            topLeft = new BitmapText( PixelScene.font1x );
            topRight = new BitmapText( PixelScene.font1x );
            bottomRight = new BitmapText( PixelScene.font1x );
        };
        @Override public void update() { };
        @Override protected void layout() { };

        public BitmapText GetTopLeft() { return topLeft; };
        public BitmapText GetTopRight() { return topRight; };
        public BitmapText GetBottomRight() { return bottomRight; };
    };

    //--------------------------------------------------------------------------
    public static class Step {
        //----------------------------------------------------------------------
        public enum State {
            DISABLE, ENABLE_ONE, ENABLE_ALL,
        }

        //----------------------------------------------------------------------
        public State state;
        public long start_time;
        public int cnt;
        public float duration;

        //----------------------------------------------------------------------
        public Step() {
            state = State.DISABLE;
        }

        //----------------------------------------------------------------------
        public void SetState(State new_state) {
            if(state != new_state) {
                Logger.d("Switching step state :: %s->%s", state, new_state);
            }
            state = new_state;
            start_time = Utils.GetMsec();
        }

        //----------------------------------------------------------------------
        public long GetDuration() {
            return Utils.GetMsec() - start_time;
        }

        //----------------------------------------------------------------------
        public State TryNext() {
            // Allow next step and become disabled
            if(state == State.ENABLE_ONE) {
                return State.DISABLE;

            // Allow next step only when timeout is over
            } else if(state == State.ENABLE_ALL && GetDuration() > 200) {
                return State.ENABLE_ALL;

            // Don't allow next step
            } else {
                return null;
            }
        }

        //----------------------------------------------------------------------
        public void Finalize(float duration) {
            // Switch scene has been requested - update is not needed
            if(Pd3d.pd.IsSceneSwitchRequested()) {
                Logger.d("Ignoring next game step because of scene switch");
                return;
            }

            // Increment step counter
            this.cnt++;

            // Duration of last step (in game time)
            this.duration = duration;

            Logger.d("Finalizing game step :: cnt=%d duration=%f", 
              cnt, duration);

            // Put update message request to incoming queue...
            Pd3d.pd.AddToRecvQueue(
              MsgUpdateScene.CreateRequest());

            // ... and process it instantly so that client 
            // receives response ASAP
            Pd3d.pd.Process();
        }
    }

    //--------------------------------------------------------------------------
    public static class LevelDesc {
        //----------------------------------------------------------------------
        private static final int[] blob_empty = new int[Blob.LENGTH];
        private static int[] GetBlobArray(Level level, Class<? extends Blob> type) {
            return level.blobs.containsKey(type) ? level.blobs.get(type).cur : blob_empty;
        }

        //----------------------------------------------------------------------
        private int[] map;
        private boolean[] visited, mapped, visible, fov, passable, los_blocking, 
          flamable, secret, solid, water, pit, avoid, discoverable;
        private int[] blob_alchemy, blob_confusion_gas, blob_fire, blob_foliage,
          blob_paralytic_gas, blob_regrowth, blob_sacrificial_fire, blob_toxic_gas,
          blob_water_of_awareness, blob_water_of_health, blob_water_of_transmutation,
          blob_web, blob_well_water;

        //----------------------------------------------------------------------
        public LevelDesc(Level level) {
            // Terrain type
            map = level.map;

            // Generic terrain flags
            visited = level.visited; 
            mapped = level.mapped; 
            visible = Dungeon.visible;
            fov = Level.fieldOfView;
            passable = Level.passable;
            los_blocking = Level.losBlocking; 
            flamable = Level.flamable;
            secret = Level.secret;
            solid = Level.solid;
            water = Level.water;
            pit = Level.pit;
            avoid = Level.avoid;
            discoverable = Level.discoverable;

            // Blob flags
            blob_alchemy = GetBlobArray(level, Alchemy.class);
            blob_confusion_gas = GetBlobArray(level, ConfusionGas.class);
            blob_fire = GetBlobArray(level, com.watabou.pixeldungeon.actors.blobs.Fire.class);;
            blob_foliage = GetBlobArray(level, Foliage.class);
            blob_paralytic_gas = GetBlobArray(level, ParalyticGas.class);
            blob_regrowth = GetBlobArray(level, Regrowth.class);
            blob_sacrificial_fire = GetBlobArray(level, SacrificialFire.class);
            blob_toxic_gas = GetBlobArray(level, ToxicGas.class);
            blob_water_of_awareness = GetBlobArray(level, WaterOfAwareness.class);
            blob_water_of_health = GetBlobArray(level, WaterOfHealth.class);
            blob_water_of_transmutation = GetBlobArray(level, WaterOfTransmutation.class);
            blob_web = GetBlobArray(level, Web.class);
            blob_well_water = GetBlobArray(level, WellWater.class);
        }

        //--------------------------------------------------------------------------
        public MapEnum.TerrainType GetType(int idx) {
            return MapEnum.TerrainType.Get(map[idx]);
        }

        //--------------------------------------------------------------------------
        public int GetFlags(int idx, boolean force_dirty, int old_flags) {
            // Calculate current flags
            int new_flags = 
              // Generic terrain flags
              (visited[idx]         ? MapEnum.TerrainFlags.VISITED.flag : 0) | 
              (mapped[idx]          ? MapEnum.TerrainFlags.MAPPED.flag : 0) | 
              (visible[idx]         ? MapEnum.TerrainFlags.VISIBLE.flag : 0) |
              (fov[idx]             ? MapEnum.TerrainFlags.FOV.flag : 0) |
              (passable[idx]        ? MapEnum.TerrainFlags.PASSABLE.flag : 0) |
              (los_blocking[idx]    ? MapEnum.TerrainFlags.LOS_BLOCKING.flag : 0) | 
              (flamable[idx]        ? MapEnum.TerrainFlags.FLAMABLE.flag : 0) |
              (secret[idx]          ? MapEnum.TerrainFlags.SECRET.flag : 0) |
              (solid[idx]           ? MapEnum.TerrainFlags.SOLID.flag : 0) |
              (water[idx]           ? MapEnum.TerrainFlags.WATER.flag : 0) |
              (pit[idx]             ? MapEnum.TerrainFlags.PIT.flag : 0) |
              (avoid[idx]           ? MapEnum.TerrainFlags.AVOID.flag : 0) |
              (discoverable[idx]    ? MapEnum.TerrainFlags.DISCOVERABLE.flag : 0) |

              // Blobs flags
              (blob_alchemy[idx] > 0                ? MapEnum.TerrainFlags.BLOB_ALCHEMY.flag : 0) |  
              (blob_confusion_gas[idx] > 0          ? MapEnum.TerrainFlags.BLOB_CONFUSION_GAS.flag : 0) |
              (blob_fire[idx] > 0                   ? MapEnum.TerrainFlags.BLOB_FIRE.flag : 0) |
              (blob_foliage[idx] > 0                ? MapEnum.TerrainFlags.BLOB_FOLIAGE.flag : 0) |
              (blob_paralytic_gas[idx] > 0          ? MapEnum.TerrainFlags.BLOB_PARALYTIC_GAS.flag : 0) |
              (blob_regrowth[idx] > 0               ? MapEnum.TerrainFlags.BLOB_REGROWTH.flag : 0) |
              (blob_sacrificial_fire[idx] > 0       ? MapEnum.TerrainFlags.BLOB_SACRIFICIAL_FIRE.flag : 0) |
              (blob_toxic_gas[idx] > 0              ? MapEnum.TerrainFlags.BLOB_TOXIC_GAS.flag : 0) |
              (blob_water_of_awareness[idx] > 0     ? MapEnum.TerrainFlags.BLOB_WATER_OF_AWARENESS.flag : 0) |
              (blob_water_of_health[idx] > 0        ? MapEnum.TerrainFlags.BLOB_WATER_OF_HEALTH.flag : 0) |
              (blob_water_of_transmutation[idx] > 0 ? MapEnum.TerrainFlags.BLOB_WATER_OF_TRANSMUTATION.flag : 0) |
              (blob_web[idx] > 0                    ? MapEnum.TerrainFlags.BLOB_WEB.flag : 0) |
              (blob_well_water[idx] > 0             ? MapEnum.TerrainFlags.BLOB_WELL_WATER.flag : 0);

            // Exclude DIRTY flag before comparing old & new
            old_flags &= ~MapEnum.TerrainFlags.PD3D_DIRTY.flag;

            // Make dirty
            if(force_dirty || old_flags != new_flags) {
                new_flags |= MapEnum.TerrainFlags.PD3D_DIRTY.flag;
            }
            return new_flags;
        }
    }

    //--------------------------------------------------------------------------
    public static class EmitterDesc {
        //----------------------------------------------------------------------
        public Emitter emitter;
        public Object target;

        //----------------------------------------------------------------------
        public EmitterDesc(Emitter emitter, Object target) {
            this.emitter = emitter;
            this.target = target;
        }
    }

    //**************************************************************************
    // Pd3dGame
    //**************************************************************************

    // Level map
    private Map m_map;
    private MapCell m_map_cells[];

    // Killed mobs 
    private SmartList<Mob> m_dead_mobs;

    // Events
    private SmartList<DescEvent> m_events;

    // Target cell when throwing or zapping
    private int m_target_cell_idx;

    // Circle back to hero if true (e.g. boomerang)
    private boolean m_item_circle_back;

    // Inventory listener that should be called for selected item 
    private WndBag.Listener m_inventory_listener;

    // IQQDQ 
    private boolean m_iddqd;

    // IQQDQ 
    private boolean m_item_info_ext;

    // Step 
    private Step m_step;

    // Quest callbacks
    private String m_quest_name;
    private HashMap<String, UtilsClass.Callback> m_quest_cb;

    // List of new emitters
    private SmartList<EmitterDesc> m_emitters;

    //--------------------------------------------------------------------------
    public Pd3dGame() {
        m_dead_mobs = new SmartList<Mob>();
        m_events = new SmartList<DescEvent>();
        m_step = new Step();
        m_quest_cb = new HashMap<String, UtilsClass.Callback>();
        m_emitters = new SmartList<EmitterDesc>();
    }

    //--------------------------------------------------------------------------
    public String GetQuestName() {
        return m_quest_name;
    }

    //--------------------------------------------------------------------------
    public Pd3dGame ResetQuest(DescQuest quest) {
        quest.actions = new LinkedList<DescQuestAction>();

        m_quest_name = quest.name;
        m_quest_cb.clear();
        Logger.d("Resetting quest :: quest=%s", m_quest_name);
        return this;
    }

    //--------------------------------------------------------------------------
    public Pd3dGame RegisterQuestAction(DescQuest quest, String action, 
      String desc, boolean is_active, UtilsClass.Callback cb) {
        quest.actions.add(new DescQuestAction(action, desc, is_active));

        m_quest_cb.put(action, cb);
        Logger.d("Registering quest action :: quest=%s action=%s active=%s", 
          m_quest_name, action, Boolean.toString(is_active));
        return this;
    }

    //--------------------------------------------------------------------------
    public Pd3dGame RegisterQuestAction(DescQuest quest, String action, 
      final RedButton button) {
        return RegisterQuestAction(
          quest, action, button.Pd3dGetText(), button.isActive(), 
          new UtilsClass.Callback() {
              @Override public Object Run(Object... args) {
                  button.Pd3dClick();
                  return null;
              }
          });
    }

    //--------------------------------------------------------------------------
    public Pd3dGame RegisterQuestAction(DescQuest quest, String action, 
      boolean is_active, final WndQuest wnd_quest) {
        return RegisterQuestAction(quest, action, "Ok", is_active,
          new UtilsClass.Callback() {
              @Override public Object Run(Object... args) {
                  wnd_quest.onBackPressed();
                  return null;
              }
          });
    }

    //--------------------------------------------------------------------------
    public void RunQuestAction(String action) {
        Logger.d("Running quest action :: quest=%s action=%s", 
          m_quest_name, action);
        UtilsClass.Callback cb = m_quest_cb.get(action);
        if(action == null) {
            Logger.e("Failed to run quest action");
            return;
        }
        cb.Run();
    }

    //--------------------------------------------------------------------------
    public Item GetQuestItem(String quest_name, int item_id) {
        Class<? extends Item> item_class = 
          quest_name.equals("read-tome-of-mastery") ? TomeOfMastery.class : null;

        Item item = Pd3d.game.GetInventoryItem(item_id, item_class);
        if(item == null) {
            Logger.e(
              "Failed to get quest item :: quest-name=%s item-id=%d", 
              quest_name, item_id);
        }
        return item;
    }

    //--------------------------------------------------------------------------
    public NPC GetQuestNpc(String quest_name, int char_id) {
        Class<? extends NPC> npc_class = 
          quest_name.equals("sadghost") ? Ghost.class : 
          quest_name.equals("wandmaker") ? Wandmaker.class :
          quest_name.equals("blacksmith") ? Blacksmith.class : 
          quest_name.equals("blacksmith-reforge") ? Blacksmith.class : 
          quest_name.equals("blacksmith-reforge-update") ? Blacksmith.class :
          quest_name.equals("imp") ? Imp.class : null;

        NPC npc = (NPC)Pd3d.game.GetCharById(char_id, npc_class);
        if(npc == null) {
            Logger.e(
              "Failed to get quest NPC :: quest-name=%s char-id=%d", 
              quest_name, char_id);
        }
        return npc;
    }

    //--------------------------------------------------------------------------
    public Step GetStep() {
        return m_step;
    }

    //--------------------------------------------------------------------------
    public Map GetMap(int width, int height) {
        if(m_map != null && m_map.GetWidth() == width && 
          m_map.GetHeight() == height) {
            return m_map;
        }

        m_map_cells = new MapCell[width * height];
        for(int i = 0; i < m_map_cells.length; i++) {
            m_map_cells[i] = new MapCell();
        }
        m_map = new Map(m_map_cells, width, height);
        return m_map;
    }

    //--------------------------------------------------------------------------
    public boolean SetIddqd(boolean value) {
        m_iddqd = value;
        return value;
    }

    //--------------------------------------------------------------------------
    public boolean IsIddqd() {
        return m_iddqd;
    }

    //--------------------------------------------------------------------------
    public boolean SetItemInfoExt(boolean value) {
        m_item_info_ext = value;
        return value;
    }

    //--------------------------------------------------------------------------
    public boolean IsItemInfoExt() {
        return m_item_info_ext;
    }

    //--------------------------------------------------------------------------
    public void SetTargetCell(int cell_idx) {
        m_target_cell_idx = cell_idx;
    }

    //--------------------------------------------------------------------------
    public int GetTargetCell() {
        return m_target_cell_idx;
    }

    //--------------------------------------------------------------------------
    public void SetCircleBack(boolean value) {
        m_item_circle_back = value;
    }

    //--------------------------------------------------------------------------
    public boolean GetCircleBack() {
        boolean tmp = m_item_circle_back;
        m_item_circle_back = false;
        return tmp;
    }

    //--------------------------------------------------------------------------
    public SmartList<Mob> GetDeadMobs() {
        return m_dead_mobs;
    }

    //--------------------------------------------------------------------------
    public SmartList<DescEvent> GetEvents() {
        return m_events;
    }

    //--------------------------------------------------------------------------
    public SmartList<EmitterDesc> GetEmitters() {
        return m_emitters;
    }

    //--------------------------------------------------------------------------
    public EmitterDesc AddEmitter(Emitter emitter, Object target) {
        EmitterDesc e = new EmitterDesc(emitter, target);
        m_emitters.Add(e);
        return e;
    }

    //--------------------------------------------------------------------------
    public DescEvent CreateEvent(MapEnum.EventType event) {
        return new DescEvent().SetEventId(event.ordinal());
    }

    //--------------------------------------------------------------------------
    public DescEvent AddEvent(MapEnum.EventType event) {
        return AddEvent(CreateEvent(event));
    }

    //--------------------------------------------------------------------------
    public DescEvent AddEvent(DescEvent event) {
        m_events.Add(event);
        return event;
    }

    //--------------------------------------------------------------------------
    private DescStringInst GetColorStringDesc(BitmapText bm_text) {
        if(bm_text.text() == null) {
            return null;
        }
        DescStringInst str = new DescStringInst();
        str.text = bm_text.text();
        str.color = Utils.GetColor(
          bm_text.ra + bm_text.rm, 
          bm_text.ga + bm_text.gm, 
          bm_text.ba + bm_text.bm, 
          bm_text.am + bm_text.aa);
        return str;
    }

    //--------------------------------------------------------------------------
    public Char GetCharById(int id, Class<?> char_class) {
        for(Char c : Dungeon.level.mobs) {
            if(c.id() == id) {
                if(char_class != null && c.getClass() != char_class) {
                    break;
                }
                return c;
            }
        }
        return null;
    }

    //--------------------------------------------------------------------------
    public DescChar GetCharDesc(Char c) {
        // Create list of buffs
        LinkedList<Integer> buffs = new LinkedList<Integer>();
        for(Buff b : c.buffs()) {
            int buff_idx = b.icon();
            if(buff_idx >= 0) {
                buffs.add(buff_idx);
            }
        }

        // Create descriptor
        DescChar char_desc = new DescChar();
        char_desc.id = c.id();
        char_desc.pos = c.pos;
        char_desc.hp = c.HP;
        char_desc.ht = c.HT;
        char_desc.sprite_id = Pd3d.sprite.GetObjectId(c.sprite);
        char_desc.emotion = c.sprite.Pd3dGetEmotionName();
        char_desc.anims = c.sprite.Pd3dGetAnimationCache().PopList();
        char_desc.status_string = c.sprite.Pd3dGetStringCache().PopList();
        char_desc.status_sprite = c.sprite.Pd3dGetSpriteCache().PopList();
        char_desc.buffs = (buffs.size() > 0) ? buffs : null;
        char_desc.time = c.time;
        return char_desc;
    }

    //--------------------------------------------------------------------------
    private static DummyItemSlot dummy_slot = null;
    private int red = Utils.GetColor(1.0f, 0.0f, 0.0f, 1.0f);
    private int yellow = Utils.GetColor(1.0f, 1.0f, 0.0f, 1.0f);
    private int green = Utils.GetColor(0.0f, 1.0f, 0.0f, 1.0f);
    public DescItem GetItemDesc(Item item, WndBag.Mode inventory_mode) {
        if(item == null || item.quantity() == 0) {
            return null;
        }

        // Create descriptor
        DescItem item_desc = new DescItem();
        item_desc.item_id = item.m_pd3d_id;
        item_desc.sprite_id = item.image();
        item_desc.level = item.level();
        item_desc.level_effective = item.effectiveLevel();
        item_desc.count = item.quantity();
        item_desc.durability = (item.isUpgradable() && item.levelKnown) ? 
          (float)item.durability() / item.maxDurability() : null;
        item_desc.name = item.toString();//.name();
        item_desc.name_real = item.trueName();
        item_desc.type = Pd3d.names.GetItemType(item);
        item_desc.info = item.info();
        item_desc.default_action = (item.defaultAction == null) ? null : 
          item.defaultAction.toLowerCase();
        item_desc.is_broken = item.isBroken();
        item_desc.is_broken_visibly = item.visiblyBroken();
        item_desc.is_equipped = item.isEquipped(Dungeon.hero);
        item_desc.is_cursed = item.cursed;
        item_desc.is_cursed_known = item.cursedKnown;
        item_desc.is_cursed_visibly = item.visiblyCursed();
        item_desc.is_unique = item.unique;
        item_desc.is_identified = item.isIdentified();
        item_desc.is_level_known = item.levelKnown;
        item_desc.is_selectable = 
          WndBag.Pd3dIsItemSelectable(item, inventory_mode);

        // Extended info
        if(IsItemInfoExt()) {
            item_desc.info += "\n\n>>>" + item_desc.name_real + "<<<";
        }

        // Create dummy slot
        if(dummy_slot == null) {
            dummy_slot = new DummyItemSlot();
        }

        // Set corner text
        dummy_slot.item(item);
        item_desc.txt_top_left = GetColorStringDesc(dummy_slot.GetTopLeft());
        item_desc.txt_top_right = GetColorStringDesc(dummy_slot.GetTopRight());
        item_desc.txt_bottom_right = GetColorStringDesc(dummy_slot.GetBottomRight());

        // Durability
        if(item_desc.durability != null) {
            item_desc.txt_bottom_left = new DescStringInst();
            item_desc.txt_bottom_left.text = String.format("%d%%", (int)(item_desc.durability * 100.0f));
            item_desc.txt_bottom_left.color = 
                item_desc.durability < 0.3f ? red : 
                item_desc.durability < 0.7f ? yellow : green;
        }

        // Set actions
        item_desc.actions = (item.actions(Dungeon.hero).size() > 0) ? 
          new LinkedList<String>() : null;
        for(String action : item.actions(Dungeon.hero)) {
            item_desc.actions.add(action.toLowerCase());
        }
        return item_desc;
    }

    //--------------------------------------------------------------------------
    public DescHeroStats GetHeroStatsDesc(DescChar hero_desc) {
        Hero h = Dungeon.hero;
        DescHeroStats hero_stats = new DescHeroStats();
        hero_stats.hp = h.HP;
        hero_stats.ht = h.HT;
        hero_stats.exp = h.exp;
        hero_stats.exp_max = h.maxExp();
        hero_stats.strength = h.STR;
        hero_stats.level = h.lvl;
        hero_stats.time_hero = h.time;
        hero_stats.time_global = Statistics.duration + Actor.now;
        hero_stats.status = 
          h.HP <= 0 ? "dead" : 
          hero_desc.emotion != null && hero_desc.emotion.equals("sleep") ? "sleeping" : 
          h.paralysed ? "paralyzed" : 
          h.rooted ? "rooted" : 
          h.flying ? "flying" : "ready";
        return hero_stats;
    }

    //--------------------------------------------------------------------------
    public Item GetInventoryItem(int item_idx, Class<?> item_class) {
        for(Item i : Dungeon.hero.belongings) {
            if(item_idx == i.m_pd3d_id) {
                if(item_class != null && i.getClass() != item_class) {
                    break;
                }
                return i;
            }
        }
        return null;
    }

    //--------------------------------------------------------------------------
    public void SetInventoryListener(WndBag.Listener inventory_listener) {
        m_inventory_listener = inventory_listener;
    }

    //--------------------------------------------------------------------------
    public WndBag.Listener GetInventoryListener() {
        return m_inventory_listener;
    }
}
