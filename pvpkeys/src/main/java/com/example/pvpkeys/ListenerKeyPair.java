package com.example.pvpkeys;

import net.runelite.client.config.Keybind;
import net.runelite.client.util.HotkeyListener;

public class ListenerKeyPair {
    public HotkeyListener listener;
    public Keybind key;

    public ListenerKeyPair(HotkeyListener listener, Keybind key) {
        this.listener = listener;
        this.key = key;
    }

    public HotkeyListener getListener() {
        return listener;
    }

    public Keybind getKey() {
        return key;
    }
}
