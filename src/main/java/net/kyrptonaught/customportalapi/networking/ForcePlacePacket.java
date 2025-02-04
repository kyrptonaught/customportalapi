package net.kyrptonaught.customportalapi.networking;

import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record ForcePlacePacket(BlockPos pos, int axis) implements CustomPayload {
    public static final CustomPayload.Id<ForcePlacePacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(CustomPortalsMod.MOD_ID, "forceplace"));
    public static final PacketCodec<RegistryByteBuf, ForcePlacePacket> codec = PacketCodec.of(ForcePlacePacket::write, ForcePlacePacket::read);

    public static ForcePlacePacket read(RegistryByteBuf buf) {
        return new ForcePlacePacket(buf.readBlockPos(), buf.readInt());
    }

    public void write(RegistryByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(axis);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}