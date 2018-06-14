//------------------------------------------------------------------------------
package com.matalok.pd3d;

//------------------------------------------------------------------------------
import java.net.InetSocketAddress;

import com.matalok.pd3d.msg.Msg;
import com.matalok.pd3d.proxy.Interfaces.IProxy;
import com.matalok.pd3d.proxy.Interfaces.IProxyBase;
import com.matalok.pd3d.proxy.Interfaces.IProxyListener;
import com.matalok.pd3d.shared.Logger;
import com.matalok.pd3d.proxy.LocalServer;
import com.matalok.pd3d.proxy.ProxyBase;
import com.matalok.pd3d.proxy.WsServer;
import com.matalok.pd3d.proxy.LocalQueue;

//------------------------------------------------------------------------------
public class ProxyServer 
  implements IProxyBase {
    //**************************************************************************
    // ProxyServer
    //**************************************************************************
    private ProxyBase m_base;

    //--------------------------------------------------------------------------
    public ProxyServer() {
        // Create proxy server base
        m_base = new ProxyBase() {
            //..................................................................
            @Override public IProxy CreateBackend(int log_level, String address, 
              int port) {
                // Local server via SharedQueue
                if(address == null) {
                    return new LocalServer(log_level, 
                      LocalQueue.server, LocalQueue.client);

                // Remote server via WebSocket
                } else {
                    log_level = 1;
                    return new WsServer(log_level,
                      new InetSocketAddress(address, port));
                }
            }
        };
    }

    //--------------------------------------------------------------------------
    public void AddToRecvQueue(Msg msg) {
        Logger.d(". >>> S :: msg=%s", msg.ToShortString());
        m_base.OnRecv(msg);
    }

    //--------------------------------------------------------------------------
    public Msg PeekLastRecv() {
        return (Msg)m_base.PeekLast(true);
    }

    // *************************************************************************
    // IProxyBase
    // *************************************************************************
    @Override public void SetListener(IProxyListener listener) {
        m_base.SetListener(listener);
    }

    //------------------------------------------------------------------------------
    @Override public boolean IsConnected() {
        return m_base.IsConnected();
    }

    //------------------------------------------------------------------------------
    @Override public void Start(int log_level, String address, int port) {
        m_base.Start(log_level, address, port);
    }

    //------------------------------------------------------------------------------
    @Override public void Stop() {
        m_base.Stop();
    }

    //------------------------------------------------------------------------------
    @Override public void Send(Msg msg) {
        m_base.Send(msg);
    }

    //------------------------------------------------------------------------------
    @Override public void Process() {
        m_base.Process();
    }
}
