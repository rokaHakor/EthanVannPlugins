package com.example.PacketUtils.Packets;

import com.example.PacketUtils.PacketDef;
import com.example.PacketUtils.PacketReflection;
import lombok.SneakyThrows;
import net.runelite.api.Client;

import javax.inject.Inject;

public class MousePackets {
    @Inject
    Client client;
    @Inject
    PacketReflection packetReflection;

    @SneakyThrows
    public void queueClickPacket(int x, int y) {
        client.setMouseLastPressedMillis(System.currentTimeMillis());
        int mousePressedTime = ((int) (client.getMouseLastPressedMillis() - client.getClientMouseLastPressedMillis()));
        if (mousePressedTime < 0) {
            mousePressedTime = 0;
        }
        if (mousePressedTime > 32767) {
            mousePressedTime = 32767;
        }
        client.setClientMouseLastPressedMillis(client.getMouseLastPressedMillis());
        int mouseInfo = (mousePressedTime << 1) + 1;
        packetReflection.sendPacket(PacketDef.EVENT_MOUSE_CLICK, mouseInfo, x, y);
    }

    public void queueClickPacket() {
        queueClickPacket(0, 0);
    }
}