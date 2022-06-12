package com.example.pvpkeys;

import com.example.PacketUtils.Packets.MousePackets;
import com.example.PacketUtils.Packets.MovementPackets;
import com.example.PacketUtils.Packets.NPCPackets;
import com.example.PacketUtils.Packets.PlayerPackets;
import com.example.PacketUtils.Packets.WidgetPackets;
import com.google.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.SpriteID;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.util.Text;
import net.runelite.client.util.WildcardMatcher;
import org.pf4j.Extension;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static net.runelite.api.ScriptID.XPDROPS_SETDROPSIZE;
import static net.runelite.client.RuneLite.RUNELITE_DIR;
import static net.runelite.client.externalplugins.ExternalPluginManager.pluginManager;

@PluginDescriptor(
		name = "PvP Keys",
		description = "",
		enabledByDefault = false
)
@Extension
public class pvpkeys extends Plugin
{
	@Inject
	private ClientToolbar clientToolbar;
	@Inject
	private ClientThread clientThread;
	private ConcurrentHashMap<Integer, String[]> queuedCommands = new ConcurrentHashMap<>();
	@Inject
	WidgetPackets widgetPackets;
	@Inject
	NPCPackets npcPackets;
	@Inject
	PlayerPackets playerPackets;
	@Inject
	MousePackets mousePackets;
	@Inject
	MovementPackets movementPackets;
	String[] xpDropCommands = null;
	int hitTrigger = 0;
	int xp = -1;
	ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	Actor target = null;
	private PvPKeysPanel panel;
	Path path = Files.createDirectories(Paths.get(RUNELITE_DIR + "/PvPKeys/"));
	private NavigationButton navButton;
	private HashMap<Integer, List<String>> ticks = new HashMap<Integer, List<String>>();
	@Inject
	private KeyManager keyManager;
	ArrayList<Setup> setupList = new ArrayList<Setup>();
	ConcurrentHashMap<String, ListenerKeyPair> hotkeyListenerList = new ConcurrentHashMap<>();
	@Inject
	Client client;
	int rev = 205;

	public pvpkeys() throws IOException
	{
	}

	@Override
	protected void startUp() throws Exception
	{
		final BufferedImage icon = ImageIO.read(getFileFromResourceAsStream("73.png"));
		panel = new PvPKeysPanel(client,this);
		navButton = NavigationButton.builder()
				.tooltip("Pvp Keys")
				.icon(icon)
				.panel(panel)
				.priority(4)
				.build();

		clientToolbar.addNavigation(navButton);
		if (client.getRevision() != rev)
		{
			JOptionPane.showMessageDialog(null, "pvpkeys not updated for this rev please wait for plugin update");
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "plugin not updated for this rev please wait for plugin update", null);
			this.shutDown();
			EventQueue.invokeLater(() ->
			{
				try
				{
					pluginManager.stopPlugin(this);
				}
				catch (PluginInstantiationException ex)
				{
					ex.printStackTrace();
				}
			});
			return;
		}
	}

	InputStream getFileFromResourceAsStream(String fileName)
	{

		// The class loader that loaded the class
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream(fileName);

		// the stream holding the file content
		if (inputStream == null)
		{
			throw new IllegalArgumentException("file not found! " + fileName);
		}
		else
		{
			return inputStream;
		}

	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (queuedCommands.containsKey(client.getTickCount()))
		{
			parseCommands(queuedCommands.get(client.getTickCount()));
			queuedCommands.remove(client.getTickCount());
		}
	}

	void updateHotkeys()
	{
		for (int i = setupList.size() - 1; i >= 0; i--)
		{
			if (setupList.get(i).enabled)
			{
				if (!hotkeyListenerList.containsKey(setupList.get(i).name))
				{
					int finalI = i;
					String name = setupList.get(i).name;
					HotkeyListener listener = new HotkeyListener(() -> setupList.get(finalI).keybind)
					{
						@Override
						public void hotkeyPressed()
						{
							//							client.getLogger().warn("pressed hotkey");
							translateNameToCommands(name);
						}
					};
					hotkeyListenerList.put(setupList.get(i).name, new ListenerKeyPair(listener, setupList.get(i).keybind));
					keyManager.registerKeyListener(hotkeyListenerList.get(setupList.get(i).name).getListener());
					//client.getLogger().warn("adding hotkey listener for "+setupList.get(i).name+" "+setupList.get
					// (i).keybind);
				}
				else
				{
					if (hotkeyListenerList.get(setupList.get(i).name).getKey() != setupList.get(i).keybind)
					{
						keyManager.unregisterKeyListener(hotkeyListenerList.get(setupList.get(i).name).getListener());
						hotkeyListenerList.remove(setupList.get(i).name);
						String name = setupList.get(i).name;
						int finalI1 = i;
						HotkeyListener listener = new HotkeyListener(() -> setupList.get(finalI1).keybind)
						{
							@Override
							public void hotkeyPressed()
							{
								//client.getLogger().warn("pressed hotkey");
								translateNameToCommands(name);
							}
						};
						//client.getLogger().warn("updating hotkey listener for "+setupList.get(i).name+" to
						// "+setupList.get(i).keybind);
						hotkeyListenerList.put(setupList.get(i).name, new ListenerKeyPair(listener, setupList.get(i).keybind));
						keyManager.registerKeyListener(hotkeyListenerList.get(setupList.get(i).name).getListener());
					}
				}
			}
			else
			{
				if (hotkeyListenerList.containsKey(setupList.get(i).name))
				{
					//client.getLogger().warn("removing hotkey listener for "+setupList.get(i).name);
					keyManager.unregisterKeyListener(hotkeyListenerList.get(setupList.get(i).name).getListener());
					hotkeyListenerList.remove(setupList.get(i).name);
				}
			}
		}
		for (Iterator<String> keys = hotkeyListenerList.keySet().iterator(); keys.hasNext();) {
			String key = keys.next();
			ListenerKeyPair val = hotkeyListenerList.get(key);
			if(!setupList.stream().anyMatch(x -> x.name.equals(key)))
			{
				keyManager.unregisterKeyListener(val.getListener());
				hotkeyListenerList.remove(key);
			}
		}
//		hotkeyListenerList.forEach((k, v) ->
//		{
//			if (setupList.stream().noneMatch(s -> s.name.equals(k)))
//			{
//				//				client.getLogger().warn("removing hotkey listener for " + k);
//				keyManager.unregisterKeyListener(v.getListener());
//				hotkeyListenerList.remove(k);
//			}
//		});
	}

	void translateNameToCommands(String name)
	{
		String[] commands = setupList.stream().filter(s -> s.name.equals(name)).findFirst().get().commands;
		parseCommands(commands);
	}

	void parseCommands(String[] commands)
	{
		clientThread.invoke(() ->
		{
			for (int i = 0; i < commands.length; i++)
			{
				if (commands[i] != null && commands[i].startsWith("//"))
				{
					continue;
				}
				String action = commands[i].split(" ")[0].trim().toLowerCase();
				String[] args = null;
				if (commands[i].split(" ").length > 1)
				{
					args = commands[i].split(" ", 2)[1].split(",");
				}
				if (args != null)
				{
					for (int j = 0; j < args.length; j++)
					{
						args[j] = args[j].trim();
					}
				}
				if (args == null || args.length == 0)
				{
					args = new String[]{"dummy"};
				}
				switch (action)
				{
					case "prayer":
						if (args[0].equals("on"))
						{
							args[1] = args[1].replaceAll("\\s", "_").toUpperCase();
							Prayer prayer = Prayer.valueOf(args[1]);
							if (!client.isPrayerActive(prayer))
							{
								mousePackets.queueClickPacket();
								widgetPackets.queueWidgetAction(client.getWidget(prayer.getWidgetInfo()), "Activate",
										"Deactivate");
							}
						}
						else if (args[0].equals("off"))
						{
							args[1] = args[1].replaceAll(" ", "_").toUpperCase();
							Prayer prayer = Prayer.valueOf(args[1]);
							if (client.isPrayerActive(prayer))
							{
								mousePackets.queueClickPacket();
								widgetPackets.queueWidgetAction(client.getWidget(prayer.getWidgetInfo()), "Activate", "Deactivate");
							}
						}
						else if (args[0].equals("toggle"))
						{
							args[1] = args[1].replaceAll(" ", "_").toUpperCase();
							Prayer prayer = Prayer.valueOf(args[1]);
							mousePackets.queueClickPacket();
							widgetPackets.queueWidgetAction(client.getWidget(prayer.getWidgetInfo()), "Activate", "Deactivate");

						}
						break;
					case "attack":
						if (target == null)
						{
							continue;
						}
						if (target instanceof NPC)
						{
							mousePackets.queueClickPacket();
							npcPackets.queueNPCAction((NPC) target, "Attack");
						}
						else if (target instanceof Player)
						{
							mousePackets.queueClickPacket();
							playerPackets.queuePlayerAction((Player) target, "Attack");
						}
						break;
					case "equip":
						for (String arg : args)
						{
							if (arg == null)
							{
								//								client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "null arg", null);
								continue;
							}
							Widget widget = isInteger(arg) ? getItem(Integer.parseInt(arg)) : getItem(arg);
							if (widget != null)
							{
								mousePackets.queueClickPacket();
								widgetPackets.queueWidgetAction(widget,
										"Equip",
										"Wear",
										"Wield");
							}//else{
							//								client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "null widget from arg: "+arg,
							//										null);
							//							}
						}
						break;
					case "spec":
						mousePackets.queueClickPacket();
						widgetPackets.queueWidgetActionPacket(1, 38862884, -1, -1);
						break;
					case "tick":
						int delay;
						if (args[0].equals("dummy"))
						{
							delay = 1;
						}
						else
						{
							delay = Integer.parseInt(args[0]);
							if (delay < 0)
							{
								delay = 1;
							}
						}
						if (commands.length > i + 1)
						{
							queuedCommands.put(client.getTickCount() + delay, Arrays.copyOfRange(commands, i + 1,
									commands.length));
						}
						return;
					case "item":
						if (args.length < 2)
						{
//							client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "not enough args for item command",
									//null);
							continue;
						}
						Widget item = isInteger(args[0]) ? getItem(Integer.parseInt(args[0])) : getItem(args[0]);
						if (item != null)
						{
//							client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "doing item command", null);

							mousePackets.queueClickPacket();
							widgetPackets.queueWidgetAction(item, args[1]);
						}
						else
						{
//							client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "item was null", null);
						}
						break;
					case "selectspell":
						Widget spell =
								client.getWidget(WidgetInfo.valueOf("SPELL_" + args[0].replaceAll(" ", "_").toUpperCase()));
						if (spell != null)
						{
							mousePackets.queueClickPacket();
							client.setSelectedSpellName(spell.getName());
							client.setSpellSelected(true);
							client.setSelectedSpellWidget(spell.getId());
							client.setSelectedSpellChildIndex(-1);

						}
						break;
					case "walkunder":
						if (target != null)
						{
							if (target.getWorldLocation() != null)
							{
								mousePackets.queueClickPacket();
								movementPackets.queueMovement(target.getWorldLocation().getX(),
										target.getWorldLocation().getY(), false);
							}
						}
						break;
					case "spell":
						args[0] = "SPELL_" + args[0].replaceAll("\\s", "_").toUpperCase();
						WidgetInfo widgetSpell = WidgetInfo.valueOf(args[0]);
						if (widgetSpell != null)
						{
							Widget spellWidget = client.getWidget(widgetSpell);
							if (spellWidget != null)
							{
								mousePackets.queueClickPacket();
								widgetPackets.queueWidgetAction(spellWidget, "Cast");
							}
							break;
						}
					case "spellattack":
						args[0] = "SPELL_" + args[0].replaceAll("\\s", "_").toUpperCase();
						widgetSpell = WidgetInfo.valueOf(args[0]);
						if (widgetSpell != null)
						{
							Widget spellWidget = client.getWidget(widgetSpell);
							if (spellWidget != null)
							{
								if (target != null)
								{
									if (target instanceof NPC)
									{
										mousePackets.queueClickPacket();
										npcPackets.queueWidgetOnNPC((NPC) target, spellWidget);
									}
									else if (target instanceof Player)
									{
										mousePackets.queueClickPacket();
										playerPackets.queueWidgetOnPlayer((Player) target, spellWidget);
									}
								}
							}
						}
						break;
					case "move":
						if (args.length <= 1)
						{
							//							client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
							//									"not enough args for movement: "+args.length,	null);
							continue;
						}
						int deltax = Integer.parseInt(args[0]);
						int deltay = Integer.parseInt(args[1]);
						mousePackets.queueClickPacket();
						movementPackets.queueMovement(client.getLocalPlayer().getWorldLocation().dx(deltax).dy(deltay));
						break;
					case "waitforhit":
						if (!args[0].equals("dummy"))
						{
							hitTrigger = Integer.parseInt(args[0]);
						}
						else
						{
							hitTrigger = 1;
						}
						xpDropCommands = Arrays.copyOfRange(commands, i + 1, commands.length);
						return;
				}
			}
		});
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (event.getType() == MenuAction.PLAYER_THIRD_OPTION.getId())
		{
			//			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "doing stuff", null);
			client.createMenuEntry(-1)
					.setOption("(Player)Set Target")
					.setTarget(event.getTarget())
					.setIdentifier(event.getIdentifier())
					.setType(MenuAction.RUNELITE)
					.onClick(this::tag);
		}
		else if (event.getType() == MenuAction.EXAMINE_NPC.getId())
		{
			client.createMenuEntry(-1)
					.setOption("(NPC)Set Target")
					.setTarget(event.getTarget())
					.setIdentifier(event.getIdentifier())
					.setType(MenuAction.RUNELITE)
					.onClick(this::tag);
		}
	}

	public Widget getItem(String str)
	{
		Widget[] items = client.getWidget(WidgetInfo.INVENTORY).getDynamicChildren();
		for (int i = 0; i < items.length; i++)
		{
			if (WildcardMatcher.matches(str.toLowerCase(), Text.removeTags(items[i].getName()).toLowerCase()))
			{
				return items[i];
			}
		}
		return null;
	}
	//credit to https://github.com/geeckon/instant-damage-calculator/blob/master/src/main/java/com/geeckon/instantdamagecalculator/InstantDamageCalculatorPlugin.java
	@Subscribe
	public void onScriptPreFired(ScriptPreFired scriptPreFired)
	{
		if (scriptPreFired.getScriptId() == XPDROPS_SETDROPSIZE)
		{
			final int[] intStack = client.getIntStack();
			final int intStackSize = client.getIntStackSize();
			final int widgetId = intStack[intStackSize - 4];
			final Widget[] children = client.getWidget(widgetId).getChildren();
			final Widget text = children[0];
			final int[] spriteIDs =
					Arrays.stream(children)
							.skip(1) // skip text
							.filter(Objects::nonNull)
							.mapToInt(Widget::getSpriteId)
							.toArray();
			for (int spriteID : spriteIDs)
			{
				if (spriteID == SpriteID.SKILL_HITPOINTS)
				{
					String hitsplat = Text.removeTags(text.getText()).trim();
					int xp = Integer.parseInt(hitsplat);
					int hit = (int) Math.round(xp / 1.33);
					if (hit >= hitTrigger && xpDropCommands != null)
					{
						queuedCommands.put(client.getTickCount(), xpDropCommands);
						xpDropCommands = null;
						hitTrigger = 0;
					}
				}
			}
		}
	}

	@Subscribe
	public void onDeath(ActorDeath event)
	{
		if (event.getActor() instanceof Player)
		{
			Player player = (Player) event.getActor();
			if (player.equals(client.getLocalPlayer()))
			{
				hitTrigger = 1;
				xpDropCommands = null;
			}
		}
	}
	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
	}

	public Widget getItem(int id)
	{
		Item[] items = client.getItemContainer(InventoryID.INVENTORY).getItems();
		for (int i = 0; i < items.length; i++)
		{
			if (items[i].getId() == id)
			{
				return client.getWidget(WidgetInfo.INVENTORY).getDynamicChildren()[i];
			}
		}
		return null;
	}

	public void tag(MenuEntry entry)
	{
		if (entry.getOption().contains("(Player)"))
		{
			target = client.getPlayers().stream().filter(p -> p.getId() == entry.getIdentifier()).findFirst().orElse(null);
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "set target to " + target.getName(), null);
		}
		else
		{
			target =
					client.getNpcs().stream().filter(p -> p.getIndex() == entry.getIdentifier()).findFirst().orElse(null);
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "set target to " + target.getName(), null);
		}
	}

	public void writeSetupToFile(Setup setup) throws IOException
	{
		String output = "";
		output += "keybind:" + setup.keybind.getKeyCode() + "," + setup.keybind.getModifiers() + "\n";
		output += "enabled:" + (setup.enabled ? "true" : "false") + "\n";
		for (String command : setup.commands)
		{
			output += "command:" + command + "\n";
		}
		Files.write(path.resolve(setup.name + ".txt"),
				output.getBytes(StandardCharsets.UTF_8));
	}


	//credit https://stackoverflow.com/a/237204
	public static boolean isInteger(String str)
	{
		if (str == null)
		{
			return false;
		}
		int length = str.length();
		if (length == 0)
		{
			return false;
		}
		int i = 0;
		if (str.charAt(0) == '-')
		{
			if (length == 1)
			{
				return false;
			}
			i = 1;
		}
		for (; i < length; i++)
		{
			char c = str.charAt(i);
			if (c < '0' || c > '9')
			{
				return false;
			}
		}
		return true;
	}

	public void addchat(String text)
	{
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", text, null);
	}
}
