package net.kyrptonaught.customportalapi.interfaces;

import net.minecraft.world.TeleportTarget;

public interface CustomTeleportingEntity {

    void setCustomTeleportTarget(TeleportTarget teleportTarget);

    TeleportTarget getCustomTeleportTarget();
}
