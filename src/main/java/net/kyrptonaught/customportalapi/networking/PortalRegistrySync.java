package net.kyrptonaught.customportalapi.networking;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.PerWorldPortals;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class PortalRegistrySync {
    public static void registerSyncOnPlayerJoin() {
        ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender, minecraftServer) -> {
            if (NetworkManager.doesPlayerHaveMod(serverPlayNetworkHandler.player)) {
                for (PortalLink link : CustomPortalApiRegistry.getAllPortalLinks()) {
                    PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                    buf.writeIdentifier(link.block);
                    buf.writeIdentifier(link.dimID);
                    buf.writeInt(link.colorID);
                    packetSender.sendPacket(NetworkManager.SYNC_PORTALS, buf);
                }
            }
        });
    }


    @Environment(EnvType.CLIENT)
    public static void registerReceivePortalData() {
        ClientPlayNetworking.registerGlobalReceiver(NetworkManager.SYNC_PORTALS, (client, handler, packet, sender) -> {
            Identifier frameBlock = packet.readIdentifier();
            Identifier dimID = packet.readIdentifier();
            int colorId = packet.readInt();
            PerWorldPortals.registerWorldPortal(new PortalLink(frameBlock, dimID, colorId));
        });
    }
}
