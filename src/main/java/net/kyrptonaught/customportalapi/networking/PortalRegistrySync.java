package net.kyrptonaught.customportalapi.networking;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.PerWorldPortals;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class PortalRegistrySync {
    private static boolean shouldSyncToPlayers = false;

    public static void enableSyncOnPlayerJoin() {
        shouldSyncToPlayers = true;
    }

    public static void registerSyncOnPlayerJoin() {
        ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender, minecraftServer) -> {
            if (shouldSyncToPlayers)
                if (NetworkManager.doesPlayerHaveMod(serverPlayNetworkHandler.player))
                    for (PortalLink link : CustomPortalApiRegistry.portals.values()) {
                        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                        buf.writeIdentifier(link.block);
                        //buf.writeInt(link.portalIgnitionSource.sourceType.ordinal());
                        //buf.writeIdentifier(link.portalIgnitionSource.ignitionSourceID);
                        //buf.writeIdentifier(Registry.BLOCK.getId(link.getPortalBlock()));
                        buf.writeIdentifier(link.dimID);
                        //buf.writeIdentifier(link.returnDimID);
                        buf.writeInt(link.colorID);
                        //buf.writeInt(link.forcedWidth);
                        //buf.writeInt(link.forcedHeight);
                        packetSender.sendPacket(NetworkManager.SYNC_PORTALS, buf);
                    }
        });
    }

    public static void registerReceivePortalData() {
        ClientPlayNetworking.registerGlobalReceiver(NetworkManager.SYNC_PORTALS, (client, handler, packet, sender) -> {
            Identifier frameBlock = packet.readIdentifier();
            //int ignitionSourceType = packet.readInt();
            //Identifier ignitionSourceID = packet.readIdentifier();
            //Identifier portalBlock = packet.readIdentifier();
            Identifier dimID = packet.readIdentifier();
            //Identifier returnDimID = packet.readIdentifier();
            int colorId = packet.readInt();
            //int forcedWidth = packet.readInt();
            //int forcedHeight = packet.readInt();
            PerWorldPortals.registerWorldPortal(new PortalLink(frameBlock, dimID, colorId));
        });
    }
}
