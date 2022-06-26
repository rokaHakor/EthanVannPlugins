package com.example.pvpkeys;

import com.example.PacketUtils.PacketUtilsPlugin;
import com.example.PacketUtils.Packets.MousePackets;
import com.example.PacketUtils.Packets.MovementPackets;
import com.example.PacketUtils.Packets.NPCPackets;
import com.example.PacketUtils.Packets.PlayerPackets;
import com.example.PacketUtils.Packets.WidgetPackets;
import com.google.common.collect.Range;
import com.google.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.HeadIcon;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;
import net.runelite.api.SpriteID;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.util.Text;
import net.runelite.client.util.WildcardMatcher;
import org.pf4j.Extension;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import static net.runelite.api.ScriptID.XPDROPS_SETDROPSIZE;
import static net.runelite.client.RuneLite.RUNELITE_DIR;
import static net.runelite.client.externalplugins.ExternalPluginManager.pluginManager;

@PluginDescriptor(name = "PvP Keys", description = "", enabledByDefault = false,
		tags = {"ethan"})
@PluginDependency(PacketUtilsPlugin.class)
@Extension
public class pvpkeys extends Plugin
{
	public boolean highlightTarget = false;
	@Inject
	private ClientToolbar clientToolbar;
	@Inject
	public ClientThread clientThread;
	private ConcurrentHashMap<Integer, String[]> queuedCommands = new ConcurrentHashMap<>();
	@Inject
	WidgetPackets widgetPackets;
	@Inject
	EventBus eventBus;
	@Inject
	NPCPackets npcPackets;
	@Inject
	PlayerPackets playerPackets;
	@Inject
	ItemManager itemManager;
	@Inject
	MousePackets mousePackets;
	@Inject
	MovementPackets movementPackets;
	@Inject
	OverlayManager overlayManager;
	PvpKeysOverlay overlay;
	boolean lastTarget = false;
	String[] xpDropCommands = null;
	int hitTrigger = 0;
	int xp = -1;
	ScheduledExecutorService executor;
	Actor target = null;
	private PvPKeysPanel panel;
	Path path = Files.createDirectories(Paths.get(RUNELITE_DIR + "/PvPKeys/"));
	private NavigationButton navButton;
	private HashMap<Integer, List<String>> ticks = new HashMap<Integer, List<String>>();
	@Inject
	private KeyManager keyManager;
	ArrayList<Setup> setupList = new ArrayList<Setup>();
	ConcurrentHashMap<String, ListenerKeyPair> hotkeyListenerList = new ConcurrentHashMap<>();
	HashMap<Integer, Integer> toStandardMap = new HashMap<>()
	{{
		put(1, 1);
		put(2, 1);
		put(3, 1);
	}};
	HashMap<Integer, Integer> toArceusMap = new HashMap<>()
	{{
		put(0, 3);
		put(1, 3);
		put(2, 3);
	}};
	HashMap<Integer, Integer> toLunarMap = new HashMap<>()
	{{
		put(0, 2);
		put(1, 2);
		put(3, 3);
	}};
	HashMap<Integer, Integer> toAncientMap = new HashMap<>()
	{{
		put(0, 1);
		put(2, 2);
		put(3, 2);
	}};
	@Inject
	Client client;
	private HeadIcon hitTriggerType;

	public pvpkeys() throws IOException
	{
	}

	@Override
	protected void startUp() throws Exception
	{
		overlay = new PvpKeysOverlay(this, client);
		overlayManager.add(overlay);
		readConfig();
		writeConfig();
		executor = Executors.newScheduledThreadPool(1);
		final BufferedImage icon = ImageIO.read(getFileFromResourceAsStream("73.png"));
		panel = new PvPKeysPanel(client, this);
		navButton = NavigationButton.builder().tooltip("Pvp Keys").icon(icon).panel(panel).priority(4).build();
		clientToolbar.addNavigation(navButton);
		if (client.getRevision() != PacketUtilsPlugin.CLIENT_REV)
		{
			SwingUtilities.invokeLater(() ->
			{
				JOptionPane.showMessageDialog(ClientUI.getFrame(), "pvpkeys not updated for this rev please wait for plugin update");
				try
				{
					pluginManager.setPluginEnabled(this, false);
					pluginManager.stopPlugin(this);
				}
				catch (PluginInstantiationException ignored)
				{
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
		for (Iterator<String> keys = hotkeyListenerList.keySet().iterator(); keys.hasNext(); )
		{
			String key = keys.next();
			ListenerKeyPair val = hotkeyListenerList.get(key);
			if (!setupList.stream().anyMatch(x -> x.name.equals(key)))
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
								widgetPackets.queueWidgetAction(client.getWidget(prayer.getWidgetInfo()), "Activate", "Deactivate");
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
					case "quickprayer":
						if (client.getWidget(WidgetInfo.QUICK_PRAYER_PRAYERS) == null)
						{
							mousePackets.queueClickPacket();
							widgetPackets.queueWidgetActionPacket(2, 10485775, -1, -1);
							MenuEntry x = client.createMenuEntry(-1).setParam1(WidgetInfo.QUICK_PRAYER_PRAYERS.getId()).setType(MenuAction.RUNELITE).setOption("Quick Prayer Update");
							eventBus.post(new MenuOptionClicked(x));
						}
						if (args[0].equals("on"))
						{
							args[1] = args[1].replaceAll("\\s", "_").toUpperCase();
							QuickPrayer prayer = QuickPrayer.valueOf(args[1]);
							if (!isQuickPrayerActive(prayer))
							{
								mousePackets.queueClickPacket();
								widgetPackets.queueWidgetActionPacket(1, WidgetInfo.QUICK_PRAYER_PRAYERS.getId(), -1, prayer.getIndex());
							}
						}
						else if (args[0].equals("off"))
						{
							args[1] = args[1].replaceAll(" ", "_").toUpperCase();
							QuickPrayer prayer = QuickPrayer.valueOf(args[1]);
							if (isQuickPrayerActive(prayer))
							{
								mousePackets.queueClickPacket();
								widgetPackets.queueWidgetActionPacket(1, WidgetInfo.QUICK_PRAYER_PRAYERS.getId(), -1, prayer.getIndex());
							}
						}
						else if (args[0].equals("toggle"))
						{
							args[1] = args[1].replaceAll(" ", "_").toUpperCase();
							QuickPrayer prayer = QuickPrayer.valueOf(args[1]);
							mousePackets.queueClickPacket();
							widgetPackets.queueWidgetActionPacket(1, WidgetInfo.QUICK_PRAYER_PRAYERS.getId(), -1, prayer.getIndex());
						}
						if (client.getWidget(WidgetInfo.QUICK_PRAYER_PRAYERS) == null)
						{
							mousePackets.queueClickPacket();
							widgetPackets.queueWidgetActionPacket(1, 5046277, -1, -1);
						}
					case "attack":
						if (target == null)
						{
							continue;
						}
						if (target instanceof NPC)
						{
							mousePackets.queueClickPacket();
							npcPackets.queueNPCAction(2, ((NPC) target).getIndex(), false);
						}
						else if (target instanceof Player)
						{
							mousePackets.queueClickPacket();
							playerPackets.queuePlayerAction(2, ((Player) target).getId(), false);
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
								widgetPackets.queueWidgetAction(widget, "Equip", "Wear", "Wield");
							}
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
							queuedCommands.put(client.getTickCount() + delay, Arrays.copyOfRange(commands, i + 1, commands.length));
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
						Widget spell = client.getWidget(WidgetInfo.valueOf("SPELL_" + args[0].replaceAll(" ", "_").toUpperCase()));
						if (spell != null)
						{
							mousePackets.queueClickPacket();
							client.setSelectedSpellName(spell.getName());
							client.setSpellSelected(true);
							client.setSelectedSpellWidget(spell.getId());
							client.setSelectedSpellChildIndex(-1);

						}
						break;
					case "equipment":
						if (args.length < 2)
						{
							//							client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "not enough args for equipment command",
							//null);
							continue;
						}
						Widget widget = isInteger(args[0]) ? getEquipment(Integer.parseInt(args[0])) : getEquipment(args[0]);
						if (widget != null)
						{
							mousePackets.queueClickPacket();
							widgetPackets.queueWidgetAction(widget, args[1]);
						}
						break;
					case "nearest":
						if (args[0].equals("player"))
						{
							target =
									client.getPlayers().stream().filter(y -> y != client.getLocalPlayer()).sorted(Comparator.comparingInt(x -> client.getLocalPlayer().getWorldLocation().distanceTo(x.getWorldLocation()))).findFirst().orElse(null);
						}
						else if (args[0].equals("npc"))
						{
							target = client.getNpcs().stream().sorted(Comparator.comparingInt(x -> client.getLocalPlayer().getWorldLocation().distanceTo(x.getWorldLocation()))).findFirst().orElse(null);
						}
						else
						{
							List<Actor> actors = new ArrayList<>();
							actors.addAll(client.getNpcs());
							actors.addAll(client.getPlayers().stream().filter(y -> y != client.getLocalPlayer()).collect(Collectors.toList()));
							target = actors.stream().sorted(Comparator.comparingInt(x -> client.getLocalPlayer().getWorldLocation().distanceTo(x.getWorldLocation()))).findFirst().orElse(null);
						}
						break;
					case "unequip":
						for (String arg : args)
						{
							if (arg == null)
							{
								continue;
							}
							widget = isInteger(arg) ? getEquipment(Integer.parseInt(arg)) : getEquipment(arg);
							if (widget != null)
							{
								mousePackets.queueClickPacket();
								widgetPackets.queueWidgetAction(widget, "Remove");
							}
						}
						break;
					case "walkunder":
						if (target != null)
						{
							if (target.getWorldLocation() != null)
							{
								mousePackets.queueClickPacket();
								movementPackets.queueMovement(target.getWorldLocation().getX(), target.getWorldLocation().getY(), false);
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
						args[0] = args[0].trim().replaceAll("\\s", "_").toUpperCase();
						if (args[0].equals("ICE") || (args[0].equals("BLOOD")))
						{
							if (args[0].equals("ICE"))
							{
								if (client.getBoostedSkillLevel(Skill.MAGIC) >= 94)
								{
									widgetSpell = WidgetInfo.SPELL_ICE_BARRAGE;
								}
								else if (client.getBoostedSkillLevel(Skill.MAGIC) >= 82)
								{
									widgetSpell = WidgetInfo.SPELL_ICE_BLITZ;
								}
								else if (client.getBoostedSkillLevel(Skill.MAGIC) >= 70)
								{
									widgetSpell = WidgetInfo.SPELL_ICE_BURST;
								}
								else if (client.getBoostedSkillLevel(Skill.MAGIC) >= 58)
								{
									widgetSpell = WidgetInfo.SPELL_ICE_RUSH;
								}
								else
								{
									widgetSpell = null;
								}
							}
							else
							{
								if (client.getBoostedSkillLevel(Skill.MAGIC) >= 92)
								{
									widgetSpell = WidgetInfo.SPELL_BLOOD_BARRAGE;
								}
								else if (client.getBoostedSkillLevel(Skill.MAGIC) >= 80)
								{
									widgetSpell = WidgetInfo.SPELL_BLOOD_BLITZ;
								}
								else if (client.getBoostedSkillLevel(Skill.MAGIC) >= 68)
								{
									widgetSpell = WidgetInfo.SPELL_BLOOD_BURST;
								}
								else if (client.getBoostedSkillLevel(Skill.MAGIC) >= 56)
								{
									widgetSpell = WidgetInfo.SPELL_BLOOD_RUSH;
								}
								else
								{
									widgetSpell = null;
								}
							}
						}
						else
						{
							args[0] = "SPELL_" + args[0];
							widgetSpell = WidgetInfo.valueOf(args[0]);
						}
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
					case "spellbook":
						int spellbook = client.getVarbitValue(4070);
						Widget magecape = getItem("Magic cape*");
						if (magecape == null)
						{
							magecape = getEquipment("Magic cape*");
						}
						if (magecape == null)
						{
							continue;
						}
						int childId = -1;
						switch (args[0])
						{
							case "standard":
								childId = toStandardMap.get(spellbook);
								break;
							case "lunars":
								childId = toLunarMap.get(spellbook);
								break;
							case "ancient":
								childId = toAncientMap.get(spellbook);
								break;
							case "arceuus":
								childId = toArceusMap.get(spellbook);
								break;
						}
						if (childId == -1)
						{
							continue;
						}
						mousePackets.queueClickPacket();
						widgetPackets.queueWidgetAction(magecape, "Spellbook");
						mousePackets.queueClickPacket();
						widgetPackets.queueResumePause(14352385, childId);
						break;
					case "waitforhit":
						if (args.length > 1)
						{
							switch (args[1])
							{
								case "mage":
									hitTriggerType = HeadIcon.MAGIC;
									break;
								case "ranged":
									hitTriggerType = HeadIcon.RANGED;
									break;
								case "melee":
									hitTriggerType = HeadIcon.MELEE;
									break;
							}
						}else{
							hitTriggerType = null;
						}
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
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (lastTarget)
		{
			int id = event.getMenuAction().getId();
			Range<Integer> npcAction = Range.closed(MenuAction.NPC_FIRST_OPTION.getId(), MenuAction.NPC_FIFTH_OPTION.getId());
			Range<Integer> playerAction = Range.closed(MenuAction.PLAYER_FIRST_OPTION.getId(), MenuAction.PLAYER_EIGTH_OPTION.getId());
			if (npcAction.contains(event.getMenuAction().getId()))
			{
				target = client.getNpcs().stream().filter(p -> p.getIndex() == event.getMenuEntry().getIdentifier()).findFirst().orElse(null);
			}
			if (playerAction.contains(event.getMenuAction().getId()))
			{
				target = client.getPlayers().stream().filter(p -> p.getId() == event.getMenuEntry().getIdentifier()).findFirst().orElse(null);
			}
			if (event.getMenuAction() == MenuAction.WIDGET_TARGET_ON_PLAYER)
			{
				target = client.getPlayers().stream().filter(p -> p.getId() == event.getMenuEntry().getIdentifier()).findFirst().orElse(null);
			}
			if (event.getMenuAction() == MenuAction.WIDGET_TARGET_ON_NPC)
			{
				target = client.getNpcs().stream().filter(p -> p.getIndex() == event.getMenuEntry().getIdentifier()).findFirst().orElse(null);
			}
			//client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "set target to " + target.getName(), null);
		}
		if(event.getMenuAction().equals(MenuAction.CC_OP)||event.getMenuAction().equals(MenuAction.CC_OP_LOW_PRIORITY))
		{
			if(event.getItemId()!=-1){
				for (Setup setup : setupList)
				{
					if(setup.enabled){
						String items ="";
						for (String itemTrigger : setup.itemTriggers)
						{
							for (String s : itemTrigger.split(","))
							{
								items += s+",";
							}
						}
						if(items.length()>1)
						{
							items = items.substring(0, items.length() - 1);
						}
						for (String s : items.split(","))
						{
							if(isInteger(s) ?
									Integer.parseInt(s) == event.getItemId() : WildcardMatcher.matches(s.toLowerCase(),
									Text.removeTags(itemManager.getItemComposition(event.getItemId()).getName()).toLowerCase())){
								parseCommands(setup.commands);
								event.consume();
								return;
							}
						}
					}
				}
			}
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (event.getType() == MenuAction.PLAYER_THIRD_OPTION.getId())
		{
			//			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "doing stuff", null);
			client.createMenuEntry(client.getMenuOptionCount() - 2).setOption("(Player)Set Target").setTarget(event.getTarget()).setIdentifier(event.getIdentifier()).setType(MenuAction.RUNELITE).onClick(this::tag);
		}
		else if (event.getType() == MenuAction.EXAMINE_NPC.getId())
		{
			client.createMenuEntry(client.getMenuOptionCount() - 2).setOption("(NPC)Set Target").setTarget(event.getTarget()).setIdentifier(event.getIdentifier()).setType(MenuAction.RUNELITE).onClick(this::tag);
		}
	}

	public Widget getEquipment(String str)
	{
		Widget[] items = client.getWidget(WidgetInfo.EQUIPMENT).getStaticChildren();
		for (int i = 0; i < items.length; i++)
		{
			if (WildcardMatcher.matches(str.toLowerCase(), Text.removeTags(items[i].getName()).toLowerCase()))
			{
				return items[i];
			}
		}
		return null;
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
	@Subscribe(priority = 1000)
	public void onScriptPreFired(ScriptPreFired scriptPreFired)
	{
		if (scriptPreFired.getScriptId() == XPDROPS_SETDROPSIZE)
		{
			final int[] intStack = client.getIntStack();
			final int intStackSize = client.getIntStackSize();
			final int widgetId = intStack[intStackSize - 4];
			final Widget[] children = client.getWidget(widgetId).getChildren();
			final Widget text = children[0];
			final int[] spriteIDs = Arrays.stream(children).skip(1) // skip text
					.filter(Objects::nonNull).mapToInt(Widget::getSpriteId).toArray();
			for (int spriteID : spriteIDs)
			{
				if (spriteID == SpriteID.SKILL_HITPOINTS)
				{
					String hitsplat = Text.removeTags(text.getText()).trim();
					int xp = Integer.parseInt(hitsplat);
					int hit = (int) Math.round(xp / 1.33);
					if (hit >= hitTrigger && xpDropCommands != null)
					{
						if (hitTriggerType != null)
						{
							if (target != null)
							{
								if (target instanceof Player)
								{
									Player pTarget = (Player) target;
									if (pTarget.getOverheadIcon().equals(hitTriggerType))
									{
										return;
									}
								}
								else if (target instanceof NPC)
								{
									NPC nTarget = (NPC) target;
									if (nTarget.getComposition().getOverheadIcon().equals(hitTriggerType))
									{
										return;
									}
								}
							}
							else
							{
								return;
							}
						}
						queuedCommands.put(client.getTickCount(), xpDropCommands);
						xpDropCommands = null;
						hitTrigger = 0;
						hitTriggerType = null;
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
		setupList.clear();
		executor.shutdown();
		overlayManager.remove(overlay);
		updateHotkeys();
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
			target = client.getNpcs().stream().filter(p -> p.getIndex() == entry.getIdentifier()).findFirst().orElse(null);
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "set target to " + target.getName(), null);
		}
	}

	public boolean isQuickPrayerActive(QuickPrayer prayer)
	{
		if ((client.getVarbitValue(4102) & (int) Math.pow(2, prayer.getIndex())) == Math.pow(2, prayer.getIndex()))
		{
			return true;
		}
		return false;
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
		for (String itemTrigger : setup.itemTriggers)
		{
			output += "itemtrigger:" + itemTrigger + "\n";
		}
		Files.write(path.resolve(setup.name + ".txt"), output.getBytes(StandardCharsets.UTF_8));
	}

	public Widget getEquipment(int id)
	{
		Widget[] equipmentWidget = client.getWidget(WidgetInfo.EQUIPMENT).getStaticChildren();
		for (int i = 0; i < equipmentWidget.length; i++)
		{
			if (equipmentWidget[i].getDynamicChildren() != null && equipmentWidget[i].getDynamicChildren().length > 1)
			{
				if (equipmentWidget[i].getDynamicChildren()[1].getItemId() == id)
				{
					return equipmentWidget[i];
				}
			}
		}
		return null;
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

	public void writeConfig() throws IOException
	{
		String output = "";
		output += lastTarget ? "true" : "false";
		output += "\n";
		output += highlightTarget ? "true" : "false";
		Files.write(path.resolve("pvpkeys.config"), output.getBytes(StandardCharsets.UTF_8));
	}

	public void readConfig() throws IOException
	{
		Files.walk(path).forEach(filePath ->
		{
			if (Files.isRegularFile(filePath))
			{
				if (filePath.getFileName().toString().equals("pvpkeys.config"))
				{
					List<String> lines = Collections.emptyList();
					try
					{
						lines = Files.readAllLines(filePath);
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					if (lines.size() > 0 && lines.get(0).equals("true"))
					{
						lastTarget = true;
					}
					else
					{
						lastTarget = false;
					}
					if (lines.size() > 1 && lines.get(1).equals("true"))
					{
						highlightTarget = true;
					}
					else
					{
						highlightTarget = false;
					}
				}
			}
		});
	}

	public void addchat(String text)
	{
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", text, null);
	}
}
