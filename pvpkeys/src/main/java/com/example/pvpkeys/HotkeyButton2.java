package com.example.pvpkeys;

import lombok.Getter;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.ModifierlessKeybind;
import net.runelite.client.ui.FontManager;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class HotkeyButton2 extends JButton
{
	@Getter
	public Keybind value;
	public HotkeyButton2(Keybind value, boolean modifierless,pvpkeys plugin,PvPKeysPanel panel)
	{
		setFont(FontManager.getDefaultFont().deriveFont(12.f));
		setValue(value);
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(MouseEvent e)
			{
				setValue(Keybind.NOT_SET);
			}
		});

		addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				setValue(new Keybind(e));
				String name = panel.comboBox.getSelectedItem().toString();
				for (Setup setup : plugin.setupList)
				{
					if(setup.name.equals(name)){
						setup.keybind = new Keybind(e);
						try
						{
							plugin.writeSetupToFile(setup);
						}
						catch (IOException ex)
						{
							ex.printStackTrace();
						}
					}
				}
				plugin.updateHotkeys();
			}
		});
	}

	public void setValue(Keybind value)
	{
		if (value == null)
		{
			value = Keybind.NOT_SET;
		}

		this.value = value;
		setText(value.toString());
	}
}
