package org.jivesoftware.openfire.plugin;

import com.rmtheis.yandtran.language.Language;
import com.rmtheis.yandtran.translate.Translate;
import org.jivesoftware.openfire.MessageRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.botz.BotzConnection;
import org.jivesoftware.openfire.botz.BotzPacketReceiver;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.util.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import java.io.File;

/**
 * YTranslaterBot is a plugin demonstrating the use of
 * Yandex speech translate API for a chattranslating.
 * 
 * @author Oleg Krotenko
 * 
 */
public class YTranslaterBot implements Plugin, PacketInterceptor {
	private static final Logger Log = LoggerFactory.getLogger(YTranslaterBot.class);
    /**
     * the hook into the inteceptor chain
     */
    private InterceptorManager interceptorManager;

    /**
     * used to send violation notifications
     */
    private MessageRouter messageRouter;

    /**
     * violation notification messages will be from this JID
     */
    private JID violationNotificationFrom;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jivesoftware.openfire.container.Plugin#destroyPlugin()
	 */
	@Override
	public void destroyPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jivesoftware.openfire.container.Plugin#initializePlugin(org.jivesoftware.openfire.container.PluginManager,
	 *      java.io.File)
	 */
	@Override
	public void initializePlugin(PluginManager manager, File pluginDirectory) {
        interceptorManager = InterceptorManager.getInstance();
        messageRouter = XMPPServer.getInstance().getMessageRouter();

		BotzPacketReceiver packetReceiver = new BotzPacketReceiver() {
			BotzConnection bot;

			public void initialize(BotzConnection bot) {
				this.bot = bot;
			}

			public void processIncoming(Packet packet) {
				if (packet instanceof Message) {
					// Echo <message/> back to sender
					packet.setTo(packet.getFrom());
					String incoming_text = packet.getElement().elementText("body");
//					try {
//						translatedText = translateProvider.translate("en", incoming_text);
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
					Translate.setKey("trnsl.1.1.20170329T073947Z.e4bb4a2e6ca50a12.af7ce267adfc13c1706ec997474bc3a33629d20c");
					String translatedText = null;
					try {
						translatedText = Translate.execute(incoming_text, Language.ENGLISH, Language.RUSSIAN);
						Log.info("Message = " + translatedText);
					} catch (Exception e) {
						e.printStackTrace();
					}
                    ((Message) packet).setBody(translatedText);
					bot.sendPacket(packet);
				}
			}

			public void processIncomingRaw(String rawText) {
			};

			public void terminate() {
			};
		};

		BotzConnection bot = new BotzConnection(packetReceiver);
		try {
			// Create user "YTranslaterBot" and login virtually
			bot.login("driver1");
			// Send the parrot's presence
			Presence presence = new Presence();
			presence.setStatus("Online");
			bot.sendPacket(presence);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    @Override
    public void interceptPacket(Packet packet, Session session, boolean read, boolean processed) throws PacketRejectedException {
	    Log.info("Hello InterceptPacket");
        if (isValidTargetPacket(packet, read, processed)) {

            Packet original = packet;

            Log.debug("Content filter: intercepted packet:"
                        + original.toString());

                original = packet.createCopy();

            String incoming_text = original.getElement().elementText("body");
            Translate.setKey("trnsl.1.1.20170329T073947Z.e4bb4a2e6ca50a12.af7ce267adfc13c1706ec997474bc3a33629d20c");
            String translatedText = null;
            try {
                translatedText = Translate.execute(incoming_text, Language.ENGLISH, Language.RUSSIAN);
                Log.info("Message = " + translatedText);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ((Message) original).setBody(translatedText);
            violationNotificationFrom  = packet.getFrom();
            messageRouter.route((Message)original);

        }
    }
    private void sendViolationNotificationEmail(String subject, String body) {
        try {
            User user = UserManager.getInstance().getUser("admin");

            //this is automatically put on a another thread for execution.
            EmailService.getInstance().sendMessage(user.getName(), user.getEmail(), "Openfire",
                    "no_reply@" + violationNotificationFrom.getDomain(), subject, body, null);

        }
        catch (Throwable e) {
            // catch throwable in case email setup is invalid
            Log.error("Content Filter: Failed to send email, please review Openfire setup", e);
        }
    }

    private boolean isValidTargetPacket(Packet packet, boolean read,
                                        boolean processed) {
        return  !processed
                && read
                && (packet instanceof Message || ( packet instanceof Presence));
    }

}