package org.jivesoftware.openfire.plugin;

import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.multiplex.ClientSessionConnection;
import org.jivesoftware.openfire.SessionPacketRouter;
import org.jivesoftware.openfire.session.LocalClientSession;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.openfire.auth.AuthToken;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.jivesoftware.openfire.interceptor.*;
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
        yaSpeechSession.setAuthToken(new AuthToken(speechJID.getNode()/*, speechJID.getResource() */));
    }

    @Override
    public void destroyPlugin() {
        InterceptorManager.getInstance().removeInterceptor(myInterceptor);
        yaSpeechSession.close();

        speechJID = null;
        yaSpeechSession = null;
        myInterceptor = null;
    }

    private class YandexSpeechPacketInterceptor implements PacketInterceptor {
        @Override
        public void interceptPacket(Packet packet,
                                    Session session,
                                    boolean incoming,
                                    boolean processed)
                throws PacketRejectedException {
            if (packet instanceof Message &&
                    packet.getTo().getNode().equals(speechJID.getNode()) &&
                    incoming == true &&
                    processed == true)
            {
                // Create the response ...
                Message message = new Message();
                message.setTo(packet.getFrom());
                message.setFrom(packet.getTo());
                message.setSubject("");
                message.setBody("Hello from Simple OpenFire Bot");

                // ... and send it
                SessionPacketRouter router = new SessionPacketRouter(yaSpeechSession);
                router.route(message);
            }
        }
    }
}
