package com.example.pvpkeys;

import net.runelite.client.config.Keybind;

public class Setup {
    String name;
    Keybind keybind;
    String[] commands;
    boolean enabled;
    String[] itemTriggers;

    public Setup(String name, Keybind keybind, String[] commands, boolean enabled) {
        this.name = name;
        this.keybind = keybind;
        this.commands = commands;
        this.enabled = enabled;
    }

    public Setup(String name) {
        this.name = name;
        this.keybind = Keybind.NOT_SET;
        this.commands = new String[]{""};
        this.enabled = true;
    }
}
