//------------------------------------------------------------------------------
package com.matalok.pd3d;

//------------------------------------------------------------------------------
import java.util.HashMap;
import com.matalok.pd3d.shared.Utils;
import com.matalok.pd3d.shared.UtilsClass;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.plants.*;
import com.watabou.pixeldungeon.scenes.*;
import com.watabou.pixeldungeon.items.*;
import com.watabou.pixeldungeon.items.armor.*;
import com.watabou.pixeldungeon.items.armor.glyphs.*;
import com.watabou.pixeldungeon.items.bags.*;
import com.watabou.pixeldungeon.items.food.*;
import com.watabou.pixeldungeon.items.keys.*;
import com.watabou.pixeldungeon.items.potions.*;
import com.watabou.pixeldungeon.items.quest.*;
import com.watabou.pixeldungeon.items.rings.*;
import com.watabou.pixeldungeon.items.scrolls.*;
import com.watabou.pixeldungeon.items.wands.*;
import com.watabou.pixeldungeon.items.weapon.enchantments.*;
import com.watabou.pixeldungeon.items.weapon.melee.*;
import com.watabou.pixeldungeon.items.weapon.missiles.*;

//------------------------------------------------------------------------------
public class Pd3dNames {
    // Map that glues scene-class and scene-name
    private UtilsClass.DoubleMap<Class<? extends PixelScene>, String> 
        m_scene_name_map;

    // Map that glues hero-class and hero-name
    private UtilsClass.DoubleMap<HeroClass, String> 
        m_hero_name_map;

    // Map that glues heap-class and heap-name
    private UtilsClass.DoubleMap<Heap.Type, String> 
        m_heap_name_map;

    // Map that glues item-class and item-name
    private HashMap<Class<?>, String> m_item_type;
    private HashMap<Class<?>, String> m_item_name;

    //--------------------------------------------------------------------------
    public Pd3dNames() {
        // Scenes
        m_scene_name_map = 
          new UtilsClass.DoubleMap<Class<? extends PixelScene>, String>();
        m_scene_name_map.Put(AboutScene.class,      "scene-about");
        m_scene_name_map.Put(AmuletScene.class,     "scene-amulet");
        m_scene_name_map.Put(BadgesScene.class,     "scene-badges");
        m_scene_name_map.Put(GameScene.class,       "scene-game");
        m_scene_name_map.Put(InterlevelScene.class, "scene-inter-level");
        m_scene_name_map.Put(IntroScene.class,      "scene-intro");
        m_scene_name_map.Put(RankingsScene.class,   "scene-rankings");
        m_scene_name_map.Put(StartScene.class,      "scene-start");
        m_scene_name_map.Put(SurfaceScene.class,    "scene-surface");
        m_scene_name_map.Put(TitleScene.class,      "scene-title");

        // Heroes
        m_hero_name_map = new UtilsClass.DoubleMap<HeroClass, String>();
        m_hero_name_map.Put(HeroClass.WARRIOR,  HeroClass.WARRIOR.title());
        m_hero_name_map.Put(HeroClass.MAGE,     HeroClass.MAGE.title());
        m_hero_name_map.Put(HeroClass.ROGUE,    HeroClass.ROGUE.title());
        m_hero_name_map.Put(HeroClass.HUNTRESS, HeroClass.HUNTRESS.title());

        // Heaps 
        m_heap_name_map = new UtilsClass.DoubleMap<Heap.Type, String>();
        m_heap_name_map.Put(Heap.Type.CHEST,         "chest");
        m_heap_name_map.Put(Heap.Type.CRYSTAL_CHEST, "chest-crystal");
        m_heap_name_map.Put(Heap.Type.FOR_SALE,      "for-sale");
        m_heap_name_map.Put(Heap.Type.HEAP,          "heap");
        m_heap_name_map.Put(Heap.Type.HIDDEN,        "hidden");
        m_heap_name_map.Put(Heap.Type.LOCKED_CHEST,  "chest-locked");
        m_heap_name_map.Put(Heap.Type.MIMIC,         "chest-mimic");
        m_heap_name_map.Put(Heap.Type.SKELETON,      "skeleton");
        m_heap_name_map.Put(Heap.Type.TOMB,          "tomp");

        // Items
        m_item_type = new HashMap<Class<?>, String>();
        m_item_name = new HashMap<Class<?>, String>();

        // Misc
        InitializeItems("misc", new Class<?>[] {
            Amulet.class, Ankh.class, ArmorKit.class, Bomb.class, DewVial.class, 
            Dewdrop.class, Gold.class, Heap.class, Honeypot.class, LloydsBeacon.class, 
            TomeOfMastery.class, Torch.class, Weightstone.class,
        });

        // Armor
        InitializeItems("armor", new Class<?>[] {
            ClothArmor.class, HuntressArmor.class, LeatherArmor.class, MageArmor.class, 
            MailArmor.class, PlateArmor.class, RogueArmor.class, 
            ScaleArmor.class, WarriorArmor.class, 
        });

        // Glyphs
        InitializeItems("glyph", new Class<?>[] {
            Affection.class, AntiEntropy.class, AutoRepair.class, Bounce.class, 
            Displacement.class, Entanglement.class, Metabolism.class, Multiplicity.class, 
            Potential.class, Stench.class, Viscosity.class, 
        });

        // Bags
        InitializeItems("bag", new Class<?>[] {
            Keyring.class, ScrollHolder.class, SeedPouch.class, WandHolster.class, 
        });

        // Food
        InitializeItems("food", new Class<?>[] {
            ChargrilledMeat.class, Food.class, FrozenCarpaccio.class, MysteryMeat.class, 
            OverpricedRation.class, Pasty.class, 
        });

        // Keys
        InitializeItems("key", new Class<?>[] {
            GoldenKey.class, IronKey.class, SkeletonKey.class, 
        });

        // Potions
        InitializeItems("potion", new Class<?>[] {
            PotionOfExperience.class, PotionOfFrost.class, PotionOfHealing.class, 
            PotionOfInvisibility.class, PotionOfLevitation.class, PotionOfLiquidFlame.class, 
            PotionOfMight.class, PotionOfMindVision.class, PotionOfParalyticGas.class, 
            PotionOfPurity.class, PotionOfStrength.class, PotionOfToxicGas.class, 
        });

        // Quests
        InitializeItems("quest", new Class<?>[] {
            CorpseDust.class, DarkGold.class, DriedRose.class, DwarfToken.class, 
            PhantomFish.class, Pickaxe.class, RatSkull.class, 
        });

        // Rings
        InitializeItems("ring", new Class<?>[] {
            RingOfAccuracy.class, RingOfDetection.class, RingOfElements.class, 
            RingOfEvasion.class, RingOfHaggler.class, RingOfHaste.class, 
            RingOfHerbalism.class, RingOfMending.class, RingOfPower.class, 
            RingOfSatiety.class, RingOfShadows.class, RingOfThorns.class, 
        });

        // Scrolls
        InitializeItems("scroll", new Class<?>[] {
            ScrollOfChallenge.class, ScrollOfEnchantment.class, ScrollOfIdentify.class, 
            ScrollOfLullaby.class, ScrollOfMagicMapping.class, ScrollOfMirrorImage.class, 
            ScrollOfPsionicBlast.class, ScrollOfRecharging.class, ScrollOfRemoveCurse.class, 
            ScrollOfTeleportation.class, ScrollOfTerror.class, ScrollOfUpgrade.class, 
            ScrollOfWipeOut.class,
        });

        // Wands
        InitializeItems("wand", new Class<?>[] {
            WandOfAmok.class, WandOfAvalanche.class, WandOfBlink.class, 
            WandOfDisintegration.class, WandOfFirebolt.class, WandOfFlock.class, 
            WandOfLightning.class, WandOfMagicMissile.class, WandOfPoison.class, 
            WandOfReach.class, WandOfRegrowth.class, WandOfSlowness.class, 
            WandOfTeleportation.class, 
        });

        // Enchantments
        InitializeItems("enchantment", new Class<?>[] {
            Death.class, Fire.class, Horror.class, Instability.class, Leech.class, 
            Luck.class, Paralysis.class, Poison.class, Shock.class, Slow.class, 
            Tempering.class, 
        });

        // Weapons
        InitializeItems("weapon", new Class<?>[] {
            BattleAxe.class, Dagger.class, Glaive.class, Knuckles.class, Longsword.class, 
            Mace.class, Quarterstaff.class, ShortSword.class, Spear.class, Sword.class, 
            WarHammer.class, 
        });

        // Missiles
        InitializeItems("missile", new Class<?>[] {
            Boomerang.class, CurareDart.class, Dart.class, IncendiaryDart.class, 
            Javelin.class, Shuriken.class, Tamahawk.class, 
        });

        // Seeds
        InitializeItems("seed", new Class<?>[] {
            Firebloom.Seed.class, Icecap.Seed.class, Sorrowmoss.Seed.class, 
            Dreamweed.Seed.class, Sungrass.Seed.class, Earthroot.Seed.class,
            Fadeleaf.Seed.class, Rotberry.Seed.class,
        });
    }

    //--------------------------------------------------------------------------
    private void InitializeItems(String type, Class<?> item_class_array[]) {
        for(Class<?> item_class : item_class_array) {
            Utils.Assert(!m_item_type.containsKey(item_class), 
              "Faild to initialize item type, duplicate class :: item=%s type=%s",
              item_class.getSimpleName(), type);
            m_item_type.put(item_class, type);
            m_item_name.put(item_class, Utils.GetFriendlyName(item_class));
        }
    }

    //--------------------------------------------------------------------------
    public String GetSceneName(PixelScene scene) {
        return m_scene_name_map.GetT2(scene.getClass());
    }

    //--------------------------------------------------------------------------
    public String GetSceneName(Class<? extends PixelScene> scene_class) {
        return m_scene_name_map.GetT2(scene_class);
    }

    //--------------------------------------------------------------------------
    public Class<? extends PixelScene> GetSceneClass(String scene_name) {
        return m_scene_name_map.GetT1(scene_name);
    }

    //--------------------------------------------------------------------------
    public HeroClass GetHeroClass(String name) {
        return m_hero_name_map.GetT1(name);
    }

    //--------------------------------------------------------------------------
    public String GetHeapName(Heap.Type type) {
        return m_heap_name_map.GetT2(type);
    }

    //--------------------------------------------------------------------------
    public String GetItemName(Item item) {
        Class<?> item_class = item.getClass();
        return m_item_name.containsKey(item_class) ? 
          m_item_name.get(item_class) : "unknown";
    }

    //--------------------------------------------------------------------------
    public String GetItemType(Item item) {
        Class<?> item_class = item.getClass();
        return m_item_type.containsKey(item_class) ? 
          m_item_type.get(item_class) : "unknown";
    }
}
