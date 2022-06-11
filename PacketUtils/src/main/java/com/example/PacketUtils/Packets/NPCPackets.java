package com.example.PacketUtils.Packets;

import com.example.PacketUtils.PacketDef;
import com.example.PacketUtils.PacketReflection;
import com.google.inject.Inject;
import lombok.SneakyThrows;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.widgets.Widget;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NPCPackets
{
	@Inject
	Client client;
	@Inject
	PacketReflection packetReflection;
	@SneakyThrows
	public void queueNPCAction(int actionFieldNo, int npcIndex,boolean ctrlDown){
		int ctrl = ctrlDown ? 1 : 0;
		switch(actionFieldNo){
			case 1:
				packetReflection.sendPacket(PacketDef.OPNPC1,npcIndex,ctrl);
				break;
			case 2:
				packetReflection.sendPacket(PacketDef.OPNPC2,npcIndex,ctrl);
				break;
			case 3:
				packetReflection.sendPacket(PacketDef.OPNPC3,npcIndex,ctrl);
				break;
			case 4:
				packetReflection.sendPacket(PacketDef.OPNPC4,npcIndex,ctrl);
				break;
			case 5:
				packetReflection.sendPacket(PacketDef.OPNPC5,npcIndex,ctrl);
				break;
		}
	}
	@SneakyThrows
	public void queueNPCAction(NPC npc, String... actionlist){
		List<String> actions = Arrays.stream(npc.getComposition().getActions()).collect(Collectors.toList());
		int num = -1;
		for (String action : actions) {
			for (String action2 : actionlist) {
				if (action!=null&&action.equals(action2)) {
					num = actions.indexOf(action)+1;
				}
			}
		}

		if(num < 1 || num > 10)
		{
			client.getLogger().warn("Unknown action");
			return;
		}
		queueNPCAction(num,npc.getIndex(),false);
	}
	public void queueWidgetOnNPC(int npcIndex, int sourceItemId, int sourceSlot, int sourceWidgetId,
									boolean ctrlDown){
		int ctrl = ctrlDown ? 1 : 0;
		packetReflection.sendPacket(PacketDef.OPNPCT,npcIndex,sourceItemId,sourceSlot,sourceWidgetId,ctrl);
	}
	public void queueWidgetOnNPC(NPC npc, Widget widget){
		queueWidgetOnNPC(npc.getIndex(),widget.getItemId(),widget.getIndex(),widget.getId(),false);
	}
}
