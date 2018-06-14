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
package com.watabou.pixeldungeon.windows;

import com.matalok.pd3d.Pd3d;
import com.matalok.pd3d.desc.DescQuest;
import com.matalok.pd3d.msg.MsgQuestStart;
import com.matalok.pd3d.shared.UtilsClass;
import com.watabou.noosa.BitmapTextMultiline;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Chrome;
import com.watabou.pixeldungeon.actors.hero.Hero;
import com.watabou.pixeldungeon.actors.mobs.npcs.Blacksmith;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.pixeldungeon.scenes.PixelScene;
import com.watabou.pixeldungeon.ui.ItemSlot;
import com.watabou.pixeldungeon.ui.RedButton;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;

public class WndBlacksmith extends Window {

	private static final int BTN_SIZE	= 36;
	private static final float GAP		= 2;
	private static final float BTN_GAP	= 10;
	private static final int WIDTH		= 116;
	
	private ItemButton btnPressed;
	
	private ItemButton btnItem1;
	private ItemButton btnItem2;
	private RedButton btnReforge;
	
	private static final String TXT_PROMPT =
		"Ok, a deal is a deal, dat's what I can do for you: I can reforge " +
		"2 items and turn them into one of a better quality.";
	private static final String TXT_SELECT =
		"Select an item to reforge";
	private static final String TXT_REFORGE =
		"Reforge them";
	
    // PD3D: Initial quest descriptor
    private DescQuest pd3d_quest;

	public WndBlacksmith( Blacksmith troll, Hero hero ) {
		
		super();
		
		IconTitle titlebar = new IconTitle();
		titlebar.icon( troll.sprite() );
		titlebar.label( Utils.capitalize( troll.name ) );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );
		
		BitmapTextMultiline message = PixelScene.createMultiline( TXT_PROMPT, 6 );
		message.maxWidth = WIDTH;
		message.measure();
		message.y = titlebar.bottom() + GAP;
		add( message );
		
		btnItem1 = new ItemButton() {
			@Override
			/*protected*/public void onClick() {
				btnPressed = btnItem1;
				GameScene.selectItem( itemSelector, WndBag.Mode.UPGRADEABLE, TXT_SELECT );
			}
		};
		btnItem1.setRect( (WIDTH - BTN_GAP) / 2 - BTN_SIZE, message.y + message.height() + BTN_GAP, BTN_SIZE, BTN_SIZE );
		add( btnItem1 );
		
		btnItem2 = new ItemButton() {
			@Override
			/*protected*/public void onClick() {
				btnPressed = btnItem2;
				GameScene.selectItem( itemSelector, WndBag.Mode.UPGRADEABLE, TXT_SELECT );
			}
		};
		btnItem2.setRect( btnItem1.right() + BTN_GAP, btnItem1.top(), BTN_SIZE, BTN_SIZE );
		add( btnItem2 );
		
		btnReforge = new RedButton( TXT_REFORGE ) {
			@Override
			protected void onClick() {
				Blacksmith.upgrade( btnItem1.item, btnItem2.item );
				hide();
			}
		};
		btnReforge.enable( false );
		btnReforge.setRect( 0, btnItem1.bottom() + BTN_GAP, WIDTH, 20 );
		add( btnReforge );
		
		
		resize( WIDTH, (int)btnReforge.bottom() );

        // PD3D: Continue quest
        MsgQuestStart msg = (MsgQuestStart)Pd3d.GetRespMsg(MsgQuestStart.class);
        if(msg != null) {
            Pd3dCreateQuestDesc(msg, message.text());
        }
	}
	
	protected WndBag.Listener itemSelector = new WndBag.Listener() {
		@Override
		public void onSelect( Item item ) {
			if (item != null) {
				btnPressed.item( item );
				String result = null;
				if (btnItem1.item != null && btnItem2.item != null) {
					result = Blacksmith.verify( btnItem1.item, btnItem2.item );
					if (result != null) {
//						GameScene.show( new WndMessage( result ) );
						btnReforge.enable( false );
					} else {
						btnReforge.enable( true );
					}
				}

                // PD3D: Update quest
                if(pd3d_quest != null) {
                    MsgQuestStart msg = MsgQuestStart.CreateRequest();
                    msg.quest = pd3d_quest;
                    msg.quest.name = "blacksmith-reforge-update";
                    Pd3dCreateQuestDesc(msg, (result != null) ? result : TXT_PROMPT);
                    Pd3d.pd.AddToRecvQueue(msg);
                }
			}
		}
	};

    private MsgQuestStart Pd3dCreateQuestDesc(MsgQuestStart msg, String text) {
        DescQuest quest = pd3d_quest = msg.quest;
        quest.text = text;
        quest.target_item_id = (btnItem1.item != null) ? btnItem1.item.m_pd3d_id : -1;
        quest.target_item_id2 = (btnItem2.item != null) ? btnItem2.item.m_pd3d_id : -1;
        Pd3d.game.ResetQuest(quest);

        // Select item
        int idx = 0;
        for(final ItemButton btn : new ItemButton[] {btnItem1, btnItem2}) {
            String name =  "#" + idx++ + " " + ((btn.item == null) ? "Select item" : 
              Pd3d.names.GetItemName(btn.item) + "-" + btn.item.m_pd3d_id);

            Pd3d.game.RegisterQuestAction(
              quest, name, name, true, new UtilsClass.Callback() {
              @Override public Object Run(Object... args) {
                  btn.onClick();
                  return null;
            }});
        }

        // Reforge
        Pd3d.game.RegisterQuestAction(
          quest, "reforge", btnReforge);
        return msg;
    }

	public static class ItemButton extends Component {
		
		protected NinePatch bg;
		protected ItemSlot slot;
		
		public Item item = null;
		
		@Override
		protected void createChildren() {
			super.createChildren();
			
			bg = Chrome.get( Chrome.Type.BUTTON );
			add( bg );
			
			slot = new ItemSlot() {
				@Override
				protected void onTouchDown() {
					bg.brightness( 1.2f );
					Sample.INSTANCE.play( Assets.SND_CLICK );
				};
				@Override
				protected void onTouchUp() {
					bg.resetColor();
				}
				@Override
				protected void onClick() {
					ItemButton.this.onClick();
				}
			};
			add( slot );
		}
		
		/*protected*/public void onClick() {};
		
		@Override
		protected void layout() {	
			super.layout();
			
			bg.x = x;
			bg.y = y;
			bg.size( width, height );
			
			slot.setRect( x + 2, y + 2, width - 4, height - 4 );
		};
		
		public void item( Item item ) {
			slot.item( this.item = item );
		}
	}
}
