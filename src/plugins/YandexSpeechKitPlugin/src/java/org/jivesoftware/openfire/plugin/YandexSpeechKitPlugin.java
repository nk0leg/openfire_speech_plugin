package org.jivesoftware.openfire.plugin;

import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.auth.AuthToken;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.multiplex.ClientSessionConnection;
import org.jivesoftware.openfire.session.LocalClientSession;
import org.jivesoftware.openfire.interceptor.*;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.*;

import java.io.File;


/**
 * Created by Okrotenko on 21.02.2017.
 */
public class YandexSpeechKitPlugin implements Plugin{
    private JID speechJID;
    private LocalClientSession yaSpeechSession;
    private YandexSpeechPacketInterceptor myInterceptor = new YandexSpeechPacketInterceptor();


    @Override
    public void initializePlugin(PluginManager manager, File pluginDirectory) {
    System.out.println("Starting simple speech bot plugin");

    speechJID = new JID("test", XMPPServer.getInstance().getServerInfo().getXMPPDomain(),
            "simple speech bot");

        ClientSessionConnection connection =
                new ClientSessionConnection(XMPPServer.getInstance().getConnectionManager().getClass().getName(),
                        "localhost","127.0.0.1");

        yaSpeechSession = SessionManager.getInstance().createClientSession(connection);
        yaSpeechSession.setAuthToken(new AuthToken(speechJID.getNode(), speechJID.getResource() ));
    }

    @Override
    public void destroyPlugin() {

    }

    private class YandexSpeechPacketInterceptor implements PacketInterceptor {
        @Override
        public void interceptPacket(Packet packet,
                                    Session session,
                                    boolean incoming,
                                    boolean processed)
                throws PacketRejectedException {

        }
    }
}
