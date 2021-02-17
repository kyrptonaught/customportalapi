package net.kyrptonaught.customportalapi;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.util.Identifier;

public class ServerSideOnly {
    private static final Identifier MOD_INSTALLED = new Identifier(CustomPortalsMod.MOD_ID, "clienthasmod");

    @Environment(EnvType.CLIENT)
    public static void clientSendHasMod() {
        ClientPlayNetworking.registerGlobalReceiver(MOD_INSTALLED, (client, handler, packet, sender) -> {
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
