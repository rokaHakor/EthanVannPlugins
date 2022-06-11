package com.example.PacketUtils.Packets;

import com.example.PacketUtils.PacketDef;
import com.example.PacketUtils.PacketReflection;
import com.google.inject.Inject;
import lombok.SneakyThrows;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.widgets.Widget;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerPackets
{
	@Inject
	Client client;
	@Inject
	PacketReflection packetReflection;
	@SneakyThrows
	public void queuePlayerAction(int actionFieldNo, int playerIndex,boolean ctrlDown){
		int ctrl = ctrlDown ? 1 : 0;
		switch(actionFieldNo){
			case 1:
				packetReflection.sendPacket(PacketDef.OPPLAYER1,playerIndex,ctrl);
				break;
			case 2:
				packetReflection.sendPacket(PacketDef.OPPLAYER2,playerIndex,ctrl);
				break;
			case 3:
				packetReflection.sendPacket(PacketDef.OPPLAYER3,playerIndex,ctrl);
				break;
			case 4:
				packetReflection.sendPacket(PacketDef.OPPLAYER4,playerIndex,ctrl);
				break;
			case 5:
				packetReflection.sendPacket(PacketDef.OPPLAYER5,playerIndex,ctrl);
				break;
			case 6:
				packetReflection.sendPacket(PacketDef.OPPLAYER6,playerIndex,ctrl);
				break;
			case 7:
				packetReflection.sendPacket(PacketDef.OPPLAYER7,playerIndex,ctrl);
				break;
			case 8:
				packetReflection.sendPacket(PacketDef.OPPLAYER8,playerIndex,ctrl);
				break;
		}
	}
	@SneakyThrows
	public void queuePlayerAction(Player player, String... actionlist){
		List<String> actions = Arrays.stream(player.getActions()).collect(Collectors.toList());
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
		queuePlayerAction(num,player.getPlayerId(),false);
	}
	public void queueWidgetOnPlayer(int playerIndex, int sourceItemId, int sourceSlot, int sourceWidgetId,
									boolean ctrlDown){
		int ctrl = ctrlDown ? 1 : 0;
		packetReflection.sendPacket(PacketDef.OPPLAYERT,playerIndex,sourceItemId,sourceSlot,sourceWidgetId,ctrl);
	}
	public void queueWidgetOnPlayer(Player player,Widget widget){
		queueWidgetOnPlayer(player.getPlayerId(),widget.getItemId(),widget.getIndex(),widget.getId(),false);
	}
}
