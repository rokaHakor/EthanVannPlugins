package com.example.PrayerFlicker;

import com.google.inject.Provides;
import lombok.SneakyThrows;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.util.HotkeyListener;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.WidgetInfo;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import static net.runelite.client.externalplugins.ExternalPluginManager.pluginManager;

@PluginDescriptor(
		name = "PrayerFlickerPlugin",
		description = "prayer flicker for quick prayers",
		enabledByDefault = false
)
@Extension
public class PrayerFlickerPlugin2 extends Plugin
{
	public int timeout = 0;
	@Inject
	Client client;
	@Inject
	private ClientThread clientThread;
	private int rev = 203;
	private boolean loaded = false;
	@Inject
	private KeyManager keyManager;
	@Inject
	private PrayerFlickerConfig config;
	public Class class135 = null;
	public Method getPacketBufferNode = null;
	public Class ClientPacket = null;
	public Field Widget1Packet = null;
	public Field ClickPacket = null;
	public Class isaacClass = null;
	public Class PacketBufferNode = null;
	public Field packetWriter = null;
	public Object isaac = null;
	private int quickPrayerWidgetID = WidgetInfo.MINIMAP_QUICK_PRAYER_ORB.getPackedId();

	@SneakyThrows
	private boolean loadShit(){
		try {
			class135 = client.getClass().getClassLoader().loadClass("ev");
			ClientPacket = client.getClass().getClassLoader().loadClass("ie");
			ClickPacket = ClientPacket.getDeclaredField("ab");
			ClickPacket.setAccessible(true);
			Widget1Packet = ClientPacket.getDeclaredField("b");
			Widget1Packet.setAccessible(true);
			packetWriter = client.getClass().getDeclaredField("ga");
			PacketBufferNode = client.getClass().getClassLoader().loadClass("ii");
			packetWriter.setAccessible(true);
			Field isaac2 = packetWriter.get(null).getClass().getDeclaredField("r");
			isaac2.setAccessible(true);
			isaac = isaac2.get(packetWriter.get(null));
			isaacClass = client.getClass().getClassLoader().loadClass("pv");
			getPacketBufferNode = Arrays.stream(class135.getDeclaredMethods()).filter(x -> x.getParameterCount() == 3).findFirst().orElse(null);
			getPacketBufferNode.setAccessible(true);
		}catch(Exception e){
			client.getLogger().warn("Failed to load prayer flicker plugin");
			return false;
		}
		client.getLogger().warn("prayer flicker plugin loaded");
		return true;
	}
	@SneakyThrows
	private void queueClickPacket(){
		Object packetBufferNode = getPacketBufferNode.invoke(null, ClickPacket.get(ClientPacket),isaac, (byte) -119);
		Buffer buffer = (net.runelite.api.Buffer) packetBufferNode.getClass().getDeclaredField("s").get(packetBufferNode);
		client.setMouseLastPressedMillis(System.currentTimeMillis());
		int mousePressedTime = ((int) (client.getMouseLastPressedMillis() - client.getClientMouseLastPressedMillis()));
		if (mousePressedTime < 0)
		{
			mousePressedTime = 0;
		}
		if (mousePressedTime > 32767)
		{
			mousePressedTime = 32767;
		}
		client.setClientMouseLastPressedMillis(client.getMouseLastPressedMillis());
		int mouseInfo = (mousePressedTime << 1) + 1;

		buffer.writeShort(mouseInfo);
		buffer.writeShort(0);
		buffer.writeShort(0);
		Method addNode = packetWriter.get(null).getClass().getMethod("s",PacketBufferNode,int.class);
		addNode.setAccessible(true);
		addNode.invoke(packetWriter.get(null),packetBufferNode,1102926475);
	}
	@Subscribe
	public void onGameStateChanged(GameStateChanged event){
		if(event.getGameState()==GameState.LOGGED_IN&&!loaded){
			loaded = loadShit();
		}
		if(event.getGameState()==GameState.LOGIN_SCREEN){
			loaded = false;
		}
	}
	@SneakyThrows
	private void queueWidgetPacket(int one,int two,int three){
		Object packetBufferNode = getPacketBufferNode.invoke(null, Widget1Packet.get(ClientPacket),isaac, (byte) -119);
		Buffer buffer = (net.runelite.api.Buffer) packetBufferNode.getClass().getDeclaredField("s").get(packetBufferNode);
		buffer.writeInt(one);
		buffer.writeShort(three);
		buffer.writeShort(two);
		Method addNode = packetWriter.get(null).getClass().getMethod("s",PacketBufferNode,int.class);
		addNode.setAccessible(true);
		addNode.invoke(packetWriter.get(null),packetBufferNode,1102926475);
	}
	@Provides
	public PrayerFlickerConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PrayerFlickerConfig.class);
	}

	private void togglePrayer()
	{
		if(!loaded){
			loaded = loadShit();
		}
		queueClickPacket();
		queueWidgetPacket(quickPrayerWidgetID,-1,-1);
	}

	@Override
	@SneakyThrows
	public void startUp(){
		if(client.getRevision() != rev){
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "plugin not updated for this rev please wait for plugin update", null);
			this.shutDown();
			EventQueue.invokeAndWait(()->{
				try {
					pluginManager.stopPlugin(this);
				} catch (PluginInstantiationException ex) {
					ex.printStackTrace();
				}
			});
			return;
		}
		keyManager.registerKeyListener(prayerToggle);
	}

	@Override
	public void shutDown()
	{
		loaded = false;
		keyManager.unregisterKeyListener(prayerToggle);
		toggle = false;
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
		clientThread.invoke(() ->
		{
			if (client.getVar(Varbits.QUICK_PRAYER)==1)
			{
				togglePrayer();
			}
		});
	}

	boolean toggle;

	public void switchAndUpdatePrayers(int i){
		queueClickPacket();
		queueWidgetPacket(5046276, -1, i);
		togglePrayer();
		togglePrayer();
	}
	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event){
		if(toggle) {
			if (event.getWidgetId() == 5046276) {
				event.consume();
				switchAndUpdatePrayers(event.getActionParam());
			}
		}
	}
	@Subscribe
	public void onGameTick(GameTick event) throws NoSuchFieldException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		if (toggle)
		{
			if (client.getVar(Varbits.QUICK_PRAYER) == 1)
			{
				togglePrayer();
			}
			togglePrayer();
		}
	}

	private final HotkeyListener prayerToggle = new HotkeyListener(() -> config.toggle())
	{
		@Override
		public void hotkeyPressed()
		{
			toggleFlicker();
		}
	};

	public void toggleFlicker()
	{
		toggle = !toggle;
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
		if (!toggle)
		{
			clientThread.invoke(() ->
			{
				if (client.getVar(Varbits.QUICK_PRAYER)==1)
				{
					togglePrayer();
				}
			});
		}
	}

	public void toggleFlicker(boolean on)
	{
		toggle = on;
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
		if (!toggle)
		{
			clientThread.invoke(() ->
			{
				if (client.getVar(Varbits.QUICK_PRAYER)==1)
				{
					togglePrayer();
				}
			});
		}
	}
}
