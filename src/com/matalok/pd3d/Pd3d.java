//------------------------------------------------------------------------------
package com.matalok.pd3d;

//------------------------------------------------------------------------------
import com.matalok.pd3d.msg.*;
import com.matalok.pd3d.proxy.Interfaces.IProxyListener;
import com.matalok.pd3d.shared.ClientAPI;
import com.matalok.pd3d.shared.Logger;
import com.matalok.pd3d.shared.UtilsClass.Callback;
import com.matalok.pd3d.shared.UtilsClass.SmartList;
import com.watabou.pixeldungeon.PixelDungeon;

//------------------------------------------------------------------------------
public class Pd3d 
  implements IProxyListener {
    //**************************************************************************
    // INTERFACES
    //**************************************************************************
    public interface IRequestHandler {
        //----------------------------------------------------------------------
        boolean OnRecvMsgLocal(MsgLocal req, MsgLocal resp);
        boolean OnRecvMsgGetScene(MsgGetScene req, MsgGetScene resp);
        boolean OnRecvMsgUpdateSprites(MsgUpdateSprites req, MsgUpdateSprites resp);
        boolean OnRecvMsgUpdateScene(MsgUpdateScene req, MsgUpdateScene resp);
        boolean OnRecvMsgSwitchScene(MsgSwitchScene req, MsgSwitchScene resp);
        boolean OnRecvMsgHeroInteract(MsgHeroInteract req, MsgHeroInteract resp);
        boolean OnRecvMsgGetInventory(MsgGetInventory req, MsgGetInventory resp);
        boolean OnRecvMsgSelectInventoryItem(MsgSelectInventoryItem req, MsgSelectInventoryItem resp);
        boolean OnRecvMsgSelectQuickslotItem(MsgSelectQuickslotItem req, MsgSelectQuickslotItem resp);
        boolean OnRecvMsgRunItemAction(MsgRunItemAction req, MsgRunItemAction resp);
        boolean OnRecvMsgRunGame(MsgRunGame req, MsgRunGame resp);
        boolean OnRecvMsgCommand(MsgCommand req, MsgCommand resp);
        boolean OnRecvMsgQuestStart(MsgQuestStart req, MsgQuestStart resp);
        boolean OnRecvMsgQuestAction(MsgQuestAction req, MsgQuestAction resp);
    }

    //--------------------------------------------------------------------------
    public interface IHook 
      extends IRequestHandler {
        //----------------------------------------------------------------------
        void OnInit();
        void OnClientConnect();
        void OnClientDisconnect();
    }

    //**************************************************************************
    // STATIC
    //**************************************************************************
    public static Msg cur_req, cur_resp;

    //--------------------------------------------------------------------------
    public static Msg GetRespMsg(Class<?> msg_class) {
        return (cur_resp != null && cur_resp.getClass() == msg_class) ? 
          cur_resp : null;
    }

    //--------------------------------------------------------------------------
    public static Msg GetReqMsg(Class<?> msg_class) {
        return (cur_req != null && cur_req.getClass() == msg_class) ? 
          cur_req : null;
    }

    //--------------------------------------------------------------------------
    public static boolean OnRecvRequest(IRequestHandler handler, 
      Msg req, Msg resp) {
        // Set current messages
        cur_req = req; cur_resp = resp;

        //......................................................................
        // LOCAL
        Class<? extends Msg> req_class = req.getClass();
        if(req_class == MsgLocal.class) {
            return handler.OnRecvMsgLocal(
              (MsgLocal)req, (MsgLocal)resp);

        //......................................................................
        // GET-SCENE
        } else if(req_class == MsgGetScene.class) {
            return handler.OnRecvMsgGetScene(
              (MsgGetScene)req, (MsgGetScene)resp);

        //......................................................................
        // UPDATE-SPRITES
        } else if(req_class == MsgUpdateSprites.class) {
            return handler.OnRecvMsgUpdateSprites(
              (MsgUpdateSprites)req, (MsgUpdateSprites)resp);

        //......................................................................
        // UPDATE-SCENE
        } else if(req_class == MsgUpdateScene.class) {
            return handler.OnRecvMsgUpdateScene(
              (MsgUpdateScene)req, (MsgUpdateScene)resp);

        //......................................................................
        // SWITCH-SCENE
        } else if(req_class == MsgSwitchScene.class) {
            return handler.OnRecvMsgSwitchScene(
              (MsgSwitchScene)req, (MsgSwitchScene)resp);

        //......................................................................
        // HERO-INTERACT
        } else if(req_class == MsgHeroInteract.class) {
            return handler.OnRecvMsgHeroInteract(
              (MsgHeroInteract)req, (MsgHeroInteract)resp);

        //......................................................................
        // GET-INVENTORY
        } else if(req_class == MsgGetInventory.class) {
            return handler.OnRecvMsgGetInventory(
              (MsgGetInventory)req, (MsgGetInventory)resp);

        //......................................................................
        // SELECT-INVENTORY-ITEM
        } else if(req_class == MsgSelectInventoryItem.class) {
            return handler.OnRecvMsgSelectInventoryItem(
              (MsgSelectInventoryItem)req, (MsgSelectInventoryItem)resp);

        //......................................................................
        // SELECT-QUICKSLOT-ITEM
        } else if(req_class == MsgSelectQuickslotItem.class) {
            return handler.OnRecvMsgSelectQuickslotItem(
              (MsgSelectQuickslotItem)req, (MsgSelectQuickslotItem)resp);

        //......................................................................
        // RUN-ITEM-ACTION
        } else if(req_class == MsgRunItemAction.class) {
            return handler.OnRecvMsgRunItemAction(
              (MsgRunItemAction)req, (MsgRunItemAction)resp);

        //......................................................................
        // RUN-GAME
        } else if(req_class == MsgRunGame.class) {
            return handler.OnRecvMsgRunGame(
              (MsgRunGame)req, (MsgRunGame)resp);

        //......................................................................
        // COMMAND
        } else if(req_class == MsgCommand.class) {
            return handler.OnRecvMsgCommand(
              (MsgCommand)req, (MsgCommand)resp);

        //......................................................................
        // QUEST-START
        } else if(req_class == MsgQuestStart.class) {
            return handler.OnRecvMsgQuestStart(
              (MsgQuestStart)req, (MsgQuestStart)resp);

        //......................................................................
        // QUEST-FINISH
        } else if(req_class == MsgQuestAction.class) {
            return handler.OnRecvMsgQuestAction(
              (MsgQuestAction)req, (MsgQuestAction)resp);
        }

        // Reset current messages
        cur_req = cur_resp = null;
        return true;
    }

    //--------------------------------------------------------------------------
    public static Pd3d pd;
    public static Pd3dNames names;
    public static Pd3dGame game;
    public static Pd3dSprite sprite;

    //--------------------------------------------------------------------------
    public static void Initialize(PixelDungeon pd_game_inst, 
      boolean is_remote_server) {
        pd = new Pd3d(pd_game_inst, is_remote_server);
        names = pd.m_names;
        game = pd.m_game;
        sprite = pd.m_new_sprite;
    }

    //**************************************************************************
    // Pd3d
    //**************************************************************************

    // Original PixelDungeon game instance
    private PixelDungeon m_pd;

    // Proxy server
    private ProxyServer m_proxy_server;

    // New info message
    private SmartList<String> m_info_buff;

    // New log messages
    private SmartList<String> m_log_buff;

    // Pd3d children
    private Pd3dNames m_names;
    private Pd3dGame m_game;
    private Pd3dSprite m_new_sprite;

    // Client API
    private ClientAPI m_client_api;

    //--------------------------------------------------------------------------
    private Pd3d(PixelDungeon pd_game, boolean is_remote_server) {
        m_pd = pd_game;

        // Pd3d children
        m_names = new Pd3dNames();
        m_game = new Pd3dGame();
        m_new_sprite = new Pd3dSprite();

        // Initialize logger
        if(is_remote_server) {
            Logger.Register(
              new Logger() {
                  @Override public synchronized void WriteRaw(String str) {
                      android.util.Log.i("PD3D", str);
                  }

                  @Override public Logger Init() {
                      return this;
                  }}, 

              new Callback() {
                  @Override public Object Run(Object... args) {
                      if(args == null || args.length != 1 || 
                        !(args[0] instanceof String)) {
                          return null;
                      }
                      Pd3d.pd.GetLogList().Add("PD3D :: " + (String)args[0]);
                      return null;
                  }}
            );
            Logger.SetPrefix("S");
        }

        // Info buffer
        m_info_buff = new SmartList<String>();

        // Log buffer
        m_log_buff = new SmartList<String>();

        // Start proxy server
        m_proxy_server = new ProxyServer();
        if(is_remote_server) {
            m_proxy_server.Start(2, "10.0.2.15", 12345);
        } else {
            m_proxy_server.Start(2, null, 0);
        }
        m_proxy_server.SetListener(this);
    }

    //--------------------------------------------------------------------------
    public void AddToRecvQueue(Msg msg) {
        m_proxy_server.AddToRecvQueue(msg);
    }

    //--------------------------------------------------------------------------
    public Msg PeekLastRecv() {
        return m_proxy_server.PeekLastRecv();
    }

    //--------------------------------------------------------------------------
    public boolean IsSceneSwitchRequested() {
        return m_pd.requestedReset;
    }

    //--------------------------------------------------------------------------
    public SmartList<String> GetLogList() {
        return m_log_buff;
    }

    //--------------------------------------------------------------------------
    public SmartList<String> GetInfoList() {
        return m_info_buff;
    }

    //--------------------------------------------------------------------------
    public void Process() {
        // Update new sprites
        if(m_proxy_server.IsConnected()) {
            if(sprite.GetUpdateList().PeekList().size() > 0) {
                Pd3d.pd.AddToRecvQueue(
                  MsgUpdateSprites.CreateRequest());
            }
        }

        // Process server
        m_proxy_server.Process();
    }

    //--------------------------------------------------------------------------
    public void SetClientAPI(ClientAPI api) {
        m_client_api = api;
    }

    //--------------------------------------------------------------------------
    public ClientAPI GetClientAPI() {
        return m_client_api;
    }

    //**************************************************************************
    // IProxyListener
    //**************************************************************************
    @Override public void OnConnected() {
        Logger.d("Client connected");

        // Put all sprites to update list
        sprite.FillUpdateList();
    }

    //--------------------------------------------------------------------------
    @Override public void OnDisconnected() {
        Logger.d("Client disconnected");
    }

    //--------------------------------------------------------------------------
    @Override public void OnRecv(Msg msg) {
        // Ignore invalid messages
        if(!msg.Validate()) {
            if(msg.IsRequest()) {
                Logger.e("Received invalid request");

            } else {
                Logger.e("Received invalid response :: code=%s text=%s", 
                  msg.status_code.toString(), msg.status_text);
            }
            return;
        }

        // Process incoming request message
        if(msg.IsRequest()) {
            Msg resp = Msg.CreateResponse(msg);
            if(m_pd.OnRequest(msg, resp)) {
                m_proxy_server.Send(resp);
            }

        // Process incoming response message
        } else {
            // Ignore for now
        }
    }

    //--------------------------------------------------------------------------
    @Override public void OnError() {
    }
}
