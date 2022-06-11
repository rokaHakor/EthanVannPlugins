package com.example.PacketUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class PacketDef
{
	final String name;
	final LinkedHashMap<String,String> fields;
	final PacketType type;

	public static final PacketDef OPOBJ1;
	public static final PacketDef IF_BUTTON9;
	public static final PacketDef IF_BUTTON8;
	public static final PacketDef OPOBJ5;
	public static final PacketDef IF_BUTTON5;
	public static final PacketDef OPOBJ4;
	public static final PacketDef IF_BUTTON4;
	public static final PacketDef OPOBJ3;
	public static final PacketDef IF_BUTTON7;
	public static final PacketDef OPOBJ2;
	public static final PacketDef IF_BUTTON6;
	public static final PacketDef OPLOCT;
	public static final PacketDef OPNPCT;
	public static final PacketDef OPPLAYERT;
	public static final PacketDef OPOBJT;
	public static final PacketDef IF_BUTTONT;
	public static final PacketDef OPNPC2;
	public static final PacketDef OPPLAYER6;
	public static final PacketDef OPNPC3;
	public static final PacketDef OPPLAYER7;
	public static final PacketDef OPLOC2;
	public static final PacketDef OPPLAYER8;
	public static final PacketDef OPLOC1;
	public static final PacketDef OPNPC1;
	public static final PacketDef OPLOC4;
	public static final PacketDef OPPLAYER2;
	public static final PacketDef OPLOC3;
	public static final PacketDef OPPLAYER3;
	public static final PacketDef OPNPC4;
	public static final PacketDef OPPLAYER4;
	public static final PacketDef OPNPC5;
	public static final PacketDef OPPLAYER5;
	public static final PacketDef OPLOC5;
	public static final PacketDef OPPLAYER1;
	public static final PacketDef MOVE_GAMECLICK;
	public static final PacketDef IF_BUTTON1;
	public static final PacketDef IF_BUTTON3;
	public static final PacketDef IF_BUTTON2;
	public static final PacketDef EVENT_MOUSE_CLICK;
	public static final PacketDef IF_BUTTON10;

	PacketDef(String var1, LinkedHashMap fields, PacketType type) {
		this.name = var1;
		this.fields = fields;
		this.type = type;
	}
	static
	{
		OPOBJ1 = new PacketDef(ObfuscatedNames.OPOBJ1, new LinkedHashMap<String,String>(){
			{
				put("worldPointX","cp");
				put("groundItemId","dc");
				put("worldPointY","dp");
				put("ctrlDown","av");
			}
		}, PacketType.OPOBJ);
		IF_BUTTON9 = new PacketDef(ObfuscatedNames.IF_BUTTON9, new LinkedHashMap<String,String>(){
			{
				put("widgetId","aa");
				put("childId","ai");
				put("itemId","ai");
			}
		}, PacketType.IF_BUTTON);
		IF_BUTTON8 = new PacketDef(ObfuscatedNames.IF_BUTTON8, new LinkedHashMap<String,String>(){
			{
				put("widgetId","aa");
				put("childId","ai");
				put("itemId","ai");
			}
		}, PacketType.IF_BUTTON);
		OPOBJ5 = new PacketDef(ObfuscatedNames.OPOBJ5, new LinkedHashMap<String,String>(){
			{
				put("ctrlDown","cq");
				put("groundItemId","cp");
				put("worldPointY","dc");
				put("worldPointX","dp");
			}
		}, PacketType.OPOBJ);
		IF_BUTTON5 = new PacketDef(ObfuscatedNames.IF_BUTTON5, new LinkedHashMap<String,String>(){
			{
				put("widgetId","aa");
				put("childId","ai");
				put("itemId","ai");
			}
		}, PacketType.IF_BUTTON);
		OPOBJ4 = new PacketDef(ObfuscatedNames.OPOBJ4, new LinkedHashMap<String,String>(){
			{
				put("ctrlDown","cl");
				put("worldPointX","cp");
				put("groundItemId","ai");
				put("worldPointY","ai");
			}
		}, PacketType.OPOBJ);
		IF_BUTTON4 = new PacketDef(ObfuscatedNames.IF_BUTTON4, new LinkedHashMap<String,String>(){
			{
				put("widgetId","aa");
				put("childId","ai");
				put("itemId","ai");
			}
		}, PacketType.IF_BUTTON);
		OPOBJ3 = new PacketDef(ObfuscatedNames.OPOBJ3, new LinkedHashMap<String,String>(){
			{
				put("worldPointY","ai");
				put("ctrlDown","cl");
				put("worldPointX","dc");
				put("groundItemId","dp");
			}
		}, PacketType.OPOBJ);
		IF_BUTTON7 = new PacketDef(ObfuscatedNames.IF_BUTTON7, new LinkedHashMap<String,String>(){
			{
				put("widgetId","aa");
				put("childId","ai");
				put("itemId","ai");
			}
		}, PacketType.IF_BUTTON);
		OPOBJ2 = new PacketDef(ObfuscatedNames.OPOBJ2, new LinkedHashMap<String,String>(){
			{
				put("ctrlDown","cg");
				put("worldPointX","cp");
				put("worldPointY","dp");
				put("groundItemId","dc");
			}
		}, PacketType.OPOBJ);
		IF_BUTTON6 = new PacketDef(ObfuscatedNames.IF_BUTTON6, new LinkedHashMap<String,String>(){
			{
				put("widgetId","aa");
				put("childId","ai");
				put("itemId","ai");
			}
		}, PacketType.IF_BUTTON);
		OPLOCT = new PacketDef(ObfuscatedNames.OPLOCT, new LinkedHashMap<String,String>(){
			{
				put("worldPointX","dc");
				put("worldPointY","ai");
				put("sourceItemId","dc");
				put("sourceSlot","dc");
				put("objectId","dp");
				put("ctrlDown","cq");
				put("sourceWidgetId","dz");
			}
		}, PacketType.OPLOCT);
		OPNPCT = new PacketDef(ObfuscatedNames.OPNPCT, new LinkedHashMap<String,String>(){
			{
				put("npcIndex","dp");
				put("sourceItemId","ai");
				put("ctrlDown","cg");
				put("sourceSlot","dc");
				put("sourceWidgetId","dn");
			}
		}, PacketType.OPNPCT);
		OPPLAYERT = new PacketDef(ObfuscatedNames.OPPLAYERT, new LinkedHashMap<String,String>(){
			{
				put("sourceWidgetId","aa");
				put("ctrlDown","cl");
				put("sourceItemId","cp");
				put("sourceSlot","ai");
				put("playerIndex","dp");
			}
		}, PacketType.OPPLAYERT);
		OPOBJT = new PacketDef(ObfuscatedNames.OPOBJT, new LinkedHashMap<String,String>(){
			{
				put("ctrlDown","av");
				put("sourceItemId","dc");
				put("sourceSlot","dp");
				put("groundItemId","ai");
				put("worldPointY","dp");
				put("sourceWidgetId","aa");
				put("worldPointX","ai");
			}
		}, PacketType.OPOBJT);
		IF_BUTTONT = new PacketDef(ObfuscatedNames.IF_BUTTONT, new LinkedHashMap<String,String>(){
			{
				put("sourceWidgetId","dl");
				put("widgetId","dn");
				put("itemId","ai");
				put("sourceSlot","dc");
				put("destinationSlot","cp");
				put("sourceItemId","dp");
			}
		}, PacketType.IF_BUTTONT);
		OPNPC2 = new PacketDef(ObfuscatedNames.OPNPC2, new LinkedHashMap<String,String>(){
			{
				put("npcIndex","cp");
				put("ctrlDown","cg");
			}
		}, PacketType.OPNPC);
		OPPLAYER6 = new PacketDef(ObfuscatedNames.OPPLAYER6, new LinkedHashMap<String,String>(){
			{
				put("playerIndex","dp");
				put("ctrlDown","cq");
			}
		}, PacketType.OPPLAYER);
		OPNPC3 = new PacketDef(ObfuscatedNames.OPNPC3, new LinkedHashMap<String,String>(){
			{
				put("ctrlDown","cg");
				put("npcIndex","dp");
			}
		}, PacketType.OPNPC);
		OPPLAYER7 = new PacketDef(ObfuscatedNames.OPPLAYER7, new LinkedHashMap<String,String>(){
			{
				put("ctrlDown","cl");
				put("playerIndex","dp");
			}
		}, PacketType.OPPLAYER);
		OPLOC2 = new PacketDef(ObfuscatedNames.OPLOC2, new LinkedHashMap<String,String>(){
			{
				put("objectId","dp");
				put("ctrlDown","cl");
				put("worldPointY","cp");
				put("worldPointX","cp");
			}
		}, PacketType.OPLOC);
		OPPLAYER8 = new PacketDef(ObfuscatedNames.OPPLAYER8, new LinkedHashMap<String,String>(){
			{
				put("ctrlDown","cg");
				put("playerIndex","dp");
			}
		}, PacketType.OPPLAYER);
		OPLOC1 = new PacketDef(ObfuscatedNames.OPLOC1, new LinkedHashMap<String,String>(){
			{
				put("ctrlDown","cl");
				put("objectId","cp");
				put("worldPointX","dp");
				put("worldPointY","dc");
			}
		}, PacketType.OPLOC);
		OPNPC1 = new PacketDef(ObfuscatedNames.OPNPC1, new LinkedHashMap<String,String>(){
			{
				put("npcIndex","dc");
				put("ctrlDown","av");
			}
		}, PacketType.OPNPC);
		OPLOC4 = new PacketDef(ObfuscatedNames.OPLOC4, new LinkedHashMap<String,String>(){
			{
				put("objectId","ai");
				put("worldPointY","ai");
				put("worldPointX","ai");
				put("ctrlDown","cg");
			}
		}, PacketType.OPLOC);
		OPPLAYER2 = new PacketDef(ObfuscatedNames.OPPLAYER2, new LinkedHashMap<String,String>(){
			{
				put("playerIndex","cp");
				put("ctrlDown","cg");
			}
		}, PacketType.OPPLAYER);
		OPLOC3 = new PacketDef(ObfuscatedNames.OPLOC3, new LinkedHashMap<String,String>(){
			{
				put("worldPointY","dp");
				put("ctrlDown","cq");
				put("worldPointX","ai");
				put("objectId","cp");
			}
		}, PacketType.OPLOC);
		OPPLAYER3 = new PacketDef(ObfuscatedNames.OPPLAYER3, new LinkedHashMap<String,String>(){
			{
				put("ctrlDown","cl");
				put("playerIndex","ai");
			}
		}, PacketType.OPPLAYER);
		OPNPC4 = new PacketDef(ObfuscatedNames.OPNPC4, new LinkedHashMap<String,String>(){
			{
				put("ctrlDown","cl");
				put("npcIndex","dp");
			}
		}, PacketType.OPNPC);
		OPPLAYER4 = new PacketDef(ObfuscatedNames.OPPLAYER4, new LinkedHashMap<String,String>(){
			{
				put("ctrlDown","av");
				put("playerIndex","dp");
			}
		}, PacketType.OPPLAYER);
		OPNPC5 = new PacketDef(ObfuscatedNames.OPNPC5, new LinkedHashMap<String,String>(){
			{
				put("npcIndex","cp");
				put("ctrlDown","cg");
			}
		}, PacketType.OPNPC);
		OPPLAYER5 = new PacketDef(ObfuscatedNames.OPPLAYER5, new LinkedHashMap<String,String>(){
			{
				put("playerIndex","ai");
				put("ctrlDown","cl");
			}
		}, PacketType.OPPLAYER);
		OPLOC5 = new PacketDef(ObfuscatedNames.OPLOC5, new LinkedHashMap<String,String>(){
			{
				put("worldPointY","ai");
				put("objectId","ai");
				put("worldPointX","dp");
				put("ctrlDown","av");
			}
		}, PacketType.OPLOC);
		OPPLAYER1 = new PacketDef(ObfuscatedNames.OPPLAYER1, new LinkedHashMap<String,String>(){
			{
				put("playerIndex","ai");
				put("ctrlDown","cq");
			}
		}, PacketType.OPPLAYER);
		MOVE_GAMECLICK = new PacketDef(ObfuscatedNames.MOVE_GAMECLICK, new LinkedHashMap<String,String>(){
			{
				put("5","av");
				put("ctrlDown","cl");
				put("worldPointY","dp");
				put("worldPointX","ai");
			}
		}, PacketType.MOVE_GAMECLICK);
		IF_BUTTON1 = new PacketDef(ObfuscatedNames.IF_BUTTON1, new LinkedHashMap<String,String>(){
			{
				put("widgetId","aa");
				put("childId","ai");
				put("itemId","ai");
			}
		}, PacketType.IF_BUTTON);
		IF_BUTTON3 = new PacketDef(ObfuscatedNames.IF_BUTTON3, new LinkedHashMap<String,String>(){
			{
				put("widgetId","aa");
				put("childId","ai");
				put("itemId","ai");
			}
		}, PacketType.IF_BUTTON);
		IF_BUTTON2 = new PacketDef(ObfuscatedNames.IF_BUTTON2, new LinkedHashMap<String,String>(){
			{
				put("widgetId","aa");
				put("childId","ai");
				put("itemId","ai");
			}
		}, PacketType.IF_BUTTON);
		EVENT_MOUSE_CLICK = new PacketDef(ObfuscatedNames.EVENT_MOUSE_CLICK, new LinkedHashMap<String,String>(){
			{
				put("mouseInfo","ai");
				put("x","ai");
				put("y","ai");
			}
		}, PacketType.EVENT_MOUSE_CLICK);
		IF_BUTTON10 = new PacketDef(ObfuscatedNames.IF_BUTTON10, new LinkedHashMap<String,String>(){
			{
				put("widgetId","aa");
				put("childId","ai");
				put("itemId","ai");
			}
		}, PacketType.IF_BUTTON);
	}
}
