package net.kyrptonaught.customportalapi.networking;

import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record LinkSyncPacket(Identifier blockID, Identifier dimID, int color) implements CustomPayload {
    public static final CustomPayload.Id<LinkSyncPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of(CustomPortalsMod.MOD_ID, "syncportals"));
    public static final PacketCodec<RegistryByteBuf, LinkSyncPacket> codec = PacketCodec.of(LinkSyncPacket::write, LinkSyncPacket::read);

    public static LinkSyncPacket read(RegistryByteBuf buf) {
        return new LinkSyncPacket(buf.readIdentifier(), buf.readIdentifier(), buf.readInt());
    }

    public void write(RegistryByteBuf buf) {
        buf.writeIdentifier(blockID);
        buf.writeIdentifier(dimID);
        buf.writeInt(color);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}