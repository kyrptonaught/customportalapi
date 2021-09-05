package net.kyrptonaught.customportalapi.networking;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.util.CustomPortalHelper;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class ForcePlacePortalPacket {
    public static void sendForcePacket(ServerPlayerEntity player, BlockPos pos) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        ServerPlayNetworking.send(player, NetworkManager.PLACE_PORTAL, buf);
    }


    @Environment(EnvType.CLIENT)
    public static void registerReceive() {
        ClientPlayNetworking.registerGlobalReceiver(NetworkManager.PLACE_PORTAL, (client, handler, packet, sender) -> {
            BlockPos blockPos = packet.readBlockPos();
            client.execute(() -> {
                BlockState oldState = client.world.getBlockState(blockPos);
                client.world.setBlockState(blockPos, CustomPortalHelper.blockWithAxis(CustomPortalsMod.getDefaultPortalBlock().getDefaultState(), CustomPortalHelper.getAxisFrom(oldState)));
            });
        });
    }
}
