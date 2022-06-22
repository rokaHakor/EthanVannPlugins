package com.example.pvpkeys;

import net.runelite.api.*;
import net.runelite.client.config.Keybind;
import net.runelite.client.plugins.timetracking.Tab;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;
import net.runelite.client.util.Text;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.runelite.client.RuneLite.RUNELITE_DIR;

public class PvPKeysPanel extends PluginPanel {
    private final JPanel display = new JPanel();
    private final Map<Tab, MaterialTab> uiTabs = new HashMap<>();
    private final MaterialTabGroup tabGroup = new MaterialTabGroup(display);
    Path path = Files.createDirectories(Paths.get(RUNELITE_DIR + "/PvPKeys/"));
    Client client;
    JComboBox<String> comboBox;
    JButton button = new JButton("Remove Set");
    JTextArea textArea = new JTextArea("", 10, 10);
    pvpkeys plugin;
    BufferedImage cross;
    BufferedImage check;
    ImageIcon crossImg = null;
    ImageIcon checkImg = null;
    HotkeyButton2 hkbutton2;
    JPanel panel = new JPanel();
    JPanel panel2 = new JPanel();

    PvPKeysPanel(Client client, pvpkeys plugin) throws IOException {
        JButton gearButton = new JButton("Copy Gear to clipboard");
        comboBox = new JComboBox<>();
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                String name = (String) value;
                if (cross == null || check == null || crossImg == null || checkImg == null) {
                    return this;
                }
                Setup setup =
                        plugin.setupList.stream().filter(x -> x.name.equals(name)).findFirst().orElse(null);
                if (setup != null) {
                    if (setup.enabled) {
                        //						setBackground(Color.GREEN);
                        this.setIcon(checkImg);
                        //						setForeground(Color.BLACK);
                    } else {
                        this.setIcon(crossImg);
                        //						setBackground(Color.RED);
                        //						setForeground(Color.BLACK);
                    }
                }
                if (isSelected) {
                    this.setForeground(Color.white);
                    this.setBackground(Color.darkGray);
                }
                return this;
            }
        });
        comboBox.setForeground(Color.white);
        comboBox.setFocusable(false);
        comboBox.setBackground(Color.darkGray);
        cross = ImageIO.read(plugin.getFileFromResourceAsStream("cross.png"));
        check = ImageIO.read(plugin.getFileFromResourceAsStream("check.png"));
        crossImg = new ImageIcon(cross.getScaledInstance(20,
                20,
                Image.SCALE_SMOOTH));
        checkImg = new ImageIcon(check.getScaledInstance(20, 20,
                Image.SCALE_SMOOTH));
        if (comboBox.getPreferredSize() != null && comboBox.getPreferredSize().height != 0) {
            crossImg = new ImageIcon(cross.getScaledInstance(comboBox.getPreferredSize().height,
                    comboBox.getPreferredSize().height,
                    Image.SCALE_SMOOTH));
            checkImg = new ImageIcon(check.getScaledInstance(comboBox.getPreferredSize().height, comboBox.getPreferredSize().height,
                    Image.SCALE_SMOOTH));
        }
        this.plugin = plugin;
        this.client = client;
        JButton enable = new JButton("Enable");
        enable.setVisible(true);
        setLayout(new BorderLayout(0, 0));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        display.setBorder(new EmptyBorder(10, 10, 8, 10));
        Files.walk(path).forEach(filePath ->
        {
            if (Files.isRegularFile(filePath)) {
                if (filePath.getFileName().toString().endsWith(".txt")) {
                    comboBox.addItem(filePath.getFileName().toString().split("\\.")[0]);
                    Setup setup = new Setup(filePath.getFileName().toString().split("\\.")[0]);
                    List<String> commands = new ArrayList<>();
                    try {
                        List<String> lines = Files.readAllLines(filePath);
                        for (String line : lines) {
                            if (line.contains("keybind")) {
                                String[] keybind = line.split(":")[1].split(",");
                                setup.keybind = new Keybind(Integer.parseInt(keybind[0]), Integer.parseInt(keybind[1]));
                            }
                            if (line.contains("enabled")) {
                                setup.enabled = Boolean.parseBoolean(line.split(":")[1].trim());
                            }
                            if (line.contains("command")) {
                                if (line.split(":").length == 2) {
                                    commands.add(line.split(":")[1]);
                                }
                            }
                        }
                        setup.commands = commands.toArray(new String[0]);
                        plugin.setupList.add(setup);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        if (comboBox.getItemCount() == 0) {
            comboBox.addItem("default set");
            Setup setup = new Setup("default set");
            plugin.setupList.add(setup);
            plugin.writeSetupToFile(setup);

        }
        if (comboBox.getItemCount() > 1) {
            Setup setup =
                    plugin.setupList.stream().filter(x -> x.name.equals(comboBox.getSelectedItem().toString())).findFirst().orElse(null);
            if (setup != null) {
                if (setup.enabled) {
                    enable.setText("Disable");
                } else {
                    enable.setText("Enable");
                }
            }
        }
        comboBox.addItem("new set");
        button.addActionListener(e ->
        {
            int input = JOptionPane.showConfirmDialog(null,
                    "Do you really want to delete this setup?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if(input==0)
            {
                if (comboBox != null && comboBox.getSelectedItem() != null)
                {
                    if (comboBox.getSelectedItem().equals("new set"))
                    {
                        return;
                    }
                    else
                    {
                        try
                        {
                            Files.delete(path.resolve(comboBox.getSelectedItem().toString() + ".txt"));
                        }
                        catch (IOException ex)
                        {
                            JOptionPane.showMessageDialog(null, "Error Removing Set");
                            ex.printStackTrace();
                        }
                        for (int i = plugin.setupList.size() - 1; i >= 0; i--)
                        {
                            if (plugin.setupList.get(i).name.equals(comboBox.getSelectedItem().toString()))
                            {
                                plugin.setupList.remove(i);
                            }
                        }
                        comboBox.removeItemAt(comboBox.getSelectedIndex());
                        plugin.updateHotkeys();
                        //				button.updateUI();
                        //				comboBox.updateUI();
                        //				display.updateUI();
                        //				hkbutton2.update(this.getGraphics());
                        update(getGraphics());
                    }
                }
            }
        });
        hkbutton2 = createKeybind();
        button.setVisible(true);
        panel.setLayout(new BorderLayout());
        panel2.setLayout(new BorderLayout());
        enable.addActionListener(e ->
        {
            if (plugin.setupList != null && plugin.setupList.size() >= 1) {
                if (enable.getText().equals("Enable")) {
                    Setup setup =
                            plugin.setupList.stream().filter(x -> x.name.equals(comboBox.getSelectedItem().toString())).findFirst().orElse(null);
                    setup.enabled = true;
                    try {
                        plugin.writeSetupToFile(setup);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    enable.setText("Disable");
                } else {
                    Setup setup =
                            plugin.setupList.stream().filter(x -> x.name.equals(comboBox.getSelectedItem().toString())).findFirst().orElse(null);
                    setup.enabled = false;
                    try {
                        plugin.writeSetupToFile(setup);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    enable.setText("Enable");
                }
                plugin.updateHotkeys();
                update(getGraphics());
            }
        });
        textArea.setVisible(true);
        textArea.setEditable(true);
        textArea.setBackground(Color.darkGray);
        textArea.setLineWrap(false);
        textArea.setForeground(Color.white);
        setForeground(Color.darkGray);
        textArea.setText("");
        enable.setForeground(Color.white);
        enable.setBackground(Color.darkGray);
        button.setForeground(Color.white);
        button.setBackground(Color.darkGray);
        hkbutton2.setForeground(Color.white);
        hkbutton2.setBackground(Color.darkGray);
        textArea.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                String setup = comboBox.getSelectedItem().toString();
                for (Setup setup1 : plugin.setupList) {
                    if (setup1.name.equals(setup)) {
                        //						client.getLogger().warn("removing: " + setup1.name);
                        setup1.commands = textArea.getText().split("\n");
                        try {
                            plugin.writeSetupToFile(setup1);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                String setup = comboBox.getSelectedItem().toString();
                for (Setup setup1 : plugin.setupList) {
                    if (setup1.name.equals(setup)) {
                        setup1.commands = textArea.getText().split("\n");
                        try {
                            plugin.writeSetupToFile(setup1);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });
        comboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (comboBox != null && enable != null && hkbutton2 != null) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        comboBox.setForeground(Color.white);
                        comboBox.setBackground(Color.darkGray);
                        if (e.getItem().equals("new set")) {
                            String x = JOptionPane.showInputDialog(null, "enter set name");
                            if (x == null) {
                                x = "default set";
                            }
                            if (x == "new set") {
                                x = "default set";
                            }
                            for (int i = 0; i < comboBox.getItemCount() - 1; i++) {
                                if (comboBox.getItemAt(i).equals(x)) {
                                    JOptionPane.showMessageDialog(null, "Set already exists");
                                    return;
                                }
                            }
                            try {
                                Files.createFile(Paths.get(path + "/" + x + ".txt"));
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            Setup setup = new Setup(x);
                            plugin.setupList.add(setup);
                            try {
                                plugin.writeSetupToFile(setup);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            comboBox.insertItemAt(x, Math.max(0, comboBox.getSelectedIndex()));
                            comboBox.setSelectedIndex(comboBox.getItemCount() - 2);
                        } else {
                            Setup setup =
                                    plugin.setupList.stream().filter(x -> x.name.equals(e.getItem().toString())).findFirst().orElse(null);
                            if (setup != null) {
                                //							client.getLogger().warn("found setup with name: " + setup.name + " loading");
                                String commands = "";
                                for (String command : setup.commands) {
                                    commands += command + "\n";
                                }
                                commands = commands.isBlank() ? "" : commands.trim();
                                //							client.getLogger().warn("loading commands: " + commands);
                                textArea.setText(commands);
                                //							client.getLogger().warn("loading hotkey: " + setup.keybind.toString());

                                hkbutton2.setValue(setup.keybind);
                                if (setup.enabled) {
                                    enable.setText("Disable");
                                } else {
                                    enable.setText("Enable");
                                }
                            }
                        }
                        update(getGraphics());
                    }
                }
            }
        });
        JButton docs = new JButton("Documentation/Help");
        docs.addActionListener(e ->
        {
            try {
                Desktop.getDesktop().browse(new URL("https://github.com/Ethan-Vann/EthanVannPlugins/wiki/PvPKeys-User" +
                        "-Guide").toURI());
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (URISyntaxException ex) {
                ex.printStackTrace();
            }
        });
        JCheckBox lastTarget = new JCheckBox("Last Target");
        if (plugin.lastTarget) {
            lastTarget.setSelected(true);
        }

        lastTarget.addActionListener(e ->
        {
            if (lastTarget.isSelected()) {
                plugin.lastTarget = true;
            } else {
                plugin.lastTarget = false;
            }
            try {
                plugin.writeConfig();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        panel2.add(comboBox, BorderLayout.NORTH);
        panel2.add(docs, BorderLayout.SOUTH);
        panel2.add(lastTarget, BorderLayout.EAST);
        textArea.setMinimumSize(new Dimension(0, 300));
        add(panel2, BorderLayout.NORTH);
        add(panel, BorderLayout.SOUTH);
        panel2.setVisible(true);
        JScrollPane pane = new JScrollPane(textArea);
        panel.add(pane, BorderLayout.SOUTH);
        panel.add(enable, BorderLayout.EAST);
        panel.add(button, BorderLayout.WEST);
        panel.add(hkbutton2, BorderLayout.NORTH);
        panel.setBackground(Color.darkGray);
        panel.setForeground(Color.darkGray);
        JCheckBox highlightTarget = new JCheckBox("Highlight Target");
        highlightTarget.addActionListener(e ->
        {
            if (highlightTarget.isSelected()) {
                plugin.highlightTarget = true;
            } else {
                plugin.highlightTarget = false;
            }
            try {
                plugin.writeConfig();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        if (plugin.highlightTarget) {
            highlightTarget.setSelected(true);
        }
        panel2.add(highlightTarget, BorderLayout.CENTER);
        add(gearButton, BorderLayout.CENTER);
        textArea.setBorder(BorderFactory.createLineBorder(Color.white));
        if (comboBox.getSelectedItem() != null) {
            Setup setup =
                    plugin.setupList.stream().filter(x -> x.name.equals(comboBox.getSelectedItem().toString())).findFirst().orElse(null);
            if (setup != null) {
                textArea.setText(String.join("\n", setup.commands));
                hkbutton2.setText(setup.keybind.toString());
            }
        }
        gearButton.addActionListener(e ->
        {
            plugin.clientThread.invoke(() ->
            {
                StringBuilder gear = new StringBuilder("equip ");
                ItemContainer container = client.getItemContainer(InventoryID.EQUIPMENT);
                if (container == null) {
                    return;
                }
                Item[] items = container.getItems();
                if (items == null) {
                    return;
                }
                for (int i = 0; i < items.length; i++) {
                    if (items[i] != null) {
                        String name = plugin.itemManager.getItemComposition(items[i].getId()).getName();
                        if (name != null && !name.equals("null")) {
                            if (!plugin.itemManager.getItemComposition(items[i].getId()).getName().equals("null")) {
                                gear.append(Text.removeTags(name)).append(",");
                            }
                        }
                    }
                }
                gear = new StringBuilder(gear.substring(0, gear.length() - 1));
                gear = new StringBuilder(gear.toString().trim());
                StringSelection stringSelection = new StringSelection(gear.toString());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
            });
        });
        update(getGraphics());
        plugin.updateHotkeys();
    }

    private HotkeyButton2 createKeybind() {
        Keybind startingValue = Keybind.NOT_SET;
        HotkeyButton2 button = new HotkeyButton2(startingValue, false, plugin, this);
        return button;
    }

    public void addchat(String text) {
        client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", text, null);
    }
}
