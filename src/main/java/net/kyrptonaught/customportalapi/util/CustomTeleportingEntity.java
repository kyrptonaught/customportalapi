package net.kyrptonaught.customportalapi.util;

import net.minecraft.world.TeleportTarget;

public interface CustomTeleportingEntity {

     void setCustomTeleportTarget(TeleportTarget teleportTarget);

     TeleportTarget getCustomTeleportTarget();
}
