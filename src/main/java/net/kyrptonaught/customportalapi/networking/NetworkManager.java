package net.kyrptonaught.customportalapi.networking;


import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.PerWorldPortals;
import net.kyrptonaught.customportalapi.util.CustomPortalHelper;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class NetworkManager implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender, minecraftServer) -> {
            for (PortalLink link : CustomPortalApiRegistry.getAllPortalLinks()) {
                packetSender.sendPacket(createPacket(link));
            }
        });
    }

    public static void syncLinkToAllPlayers(PortalLink link, MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            syncLinkToPlayer(link, player);
        }
    }

    public static void syncLinkToPlayer(PortalLink link, ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, createPacket(link));
    }

    private static LinkSyncPacket createPacket(PortalLink link) {
        return new LinkSyncPacket(link.block, link.dimID, link.colorID);
    }

    public static void sendForcePacket(ServerPlayerEntity player, BlockPos pos, Direction.Axis axis) {
        ServerPlayNetworking.send(player, new ForcePlacePacket(pos, axis.ordinal()));
    }

    @Environment(EnvType.CLIENT)
    public static void registerPackets() {
        ClientPlayNetworking.registerGlobalReceiver(LinkSyncPacket.PACKET_ID, (payload, context) -> {
            PerWorldPortals.registerWorldPortal(new PortalLink(payload.blockID(), payload.dimID(), payload.color()));
        });

        ClientPlayNetworking.registerGlobalReceiver(ForcePlacePacket.PACKET_ID, (payload, context) -> {
            context.client().execute(() -> {
                if (context.client().world == null) return;

                Direction.Axis axis;
                if (payload.axis() > -1) {
                    axis = Direction.Axis.values()[payload.axis()];
                } else {
                    BlockState old = context.client().world.getBlockState(payload.pos());
                    axis = CustomPortalHelper.getAxisFrom(old);
                }

                context.client().world.setBlockState(payload.pos(), CustomPortalHelper.blockWithAxis(CustomPortalsMod.getDefaultPortalBlock().getDefaultState(), axis));

            });
        });
    }
}