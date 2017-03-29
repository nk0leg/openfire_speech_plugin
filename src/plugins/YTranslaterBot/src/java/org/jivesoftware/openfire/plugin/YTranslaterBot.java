package org.jivesoftware.openfire.plugin;

import java.io.File;

import org.jivesoftware.openfire.botz.BotzConnection;
import org.jivesoftware.openfire.botz.BotzPacketReceiver;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

/**
 * YTranslaterBot is a plugin demonstrating the use of
 * Yandex speech translate API for a chattranslating.
 * 
 * @author Oleg Krotenko
 * 
 */
public class YTranslaterBot implements Plugin {

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
		BotzPacketReceiver packetReceiver = new BotzPacketReceiver() {
			BotzConnection bot;

			public void initialize(BotzConnection bot) {
				this.bot = bot;
			}

			public void processIncoming(Packet packet) {
				if (packet instanceof Message) {
					// Echo <message/> back to sender
					packet.setTo(packet.getFrom());
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
			bot.login("YTranslaterBot");
			// Send the parrot's presence
			Presence presence = new Presence();
			presence.setStatus("Online");
			bot.sendPacket(presence);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}