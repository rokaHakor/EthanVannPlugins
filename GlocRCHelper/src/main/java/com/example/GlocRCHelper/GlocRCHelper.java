package com.example.GlocRCHelper;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.LocatableQueryResults;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

import java.util.Arrays;
import java.util.stream.Collectors;

@PluginDescriptor(
        name = "Gloc RC Helper",
        description = "Makes ring of the elements use equipped dueling ring tele to cwars if you're at an altar"
)
@Slf4j
@Extension
public class GlocRCHelper extends Plugin {
    @Inject
    private Client client;
    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked) {
            if (menuOptionClicked.getItemId() == 26818) {
                GameObjectQuery x = new GameObjectQuery();
                LocatableQueryResults results = x.nameEquals("Altar").result(client);
                if (results.size() > 0) {
                    Widget widget = client.getWidget(WidgetInfo.EQUIPMENT_RING);
                    menuOptionClicked.setMenuEntry(createMenuEntry(Arrays.stream(widget.getActions()).collect(Collectors.toList()).indexOf("Castle Wars") + 1, MenuAction.CC_OP, widget.getIndex(), widget.getId(), true));
                }
            }
    }

    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }
}