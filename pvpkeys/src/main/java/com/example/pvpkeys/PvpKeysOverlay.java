package com.example.pvpkeys;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

import java.awt.*;

public class PvpKeysOverlay extends Overlay
{
	pvpkeys plugin;
	Client client;

	public PvpKeysOverlay(pvpkeys plugin, Client client)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.LOW);
		setLayer(OverlayLayer.ABOVE_SCENE);
		this.plugin = plugin;
		this.client = client;
	}
	{
		this.plugin = plugin;
	}
	@Override
	public Dimension render(Graphics2D graphics)
	{
		if(plugin.highlightTarget)
		{
			if (plugin.target instanceof NPC)
			{
				if (!client.getNpcs().contains(plugin.target))
				{
					return null;
				}
			}
			else if (plugin.target instanceof Player)
			{
				if (!client.getPlayers().contains(plugin.target))
				{
					return null;
				}
			}
			if (plugin.target != null)
			{
				final WorldPoint playerPos = plugin.target.getWorldLocation();
				if (playerPos == null)
				{
					return null;
				}

				final LocalPoint playerPosLocal = LocalPoint.fromWorld(client, playerPos);
				if (playerPosLocal == null)
				{
					return null;
				}
				renderTile(graphics, playerPosLocal, Color.RED, 1, new Color(0, 0, 0, 0));
			}
		}
		return null;
	}
	private void renderTile(final Graphics2D graphics, final LocalPoint dest, final Color color, final double borderWidth, final Color fillColor)
	{
		if (dest == null)
		{
			return;
		}

		final Polygon poly = Perspective.getCanvasTilePoly(client, dest);

		if (poly == null)
		{
			return;
		}

		OverlayUtil.renderPolygon(graphics, poly, color, fillColor, new BasicStroke((float) borderWidth));
	}
}
