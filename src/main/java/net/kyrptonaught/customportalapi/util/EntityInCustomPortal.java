package net.kyrptonaught.customportalapi.util;

public interface EntityInCustomPortal {

    void setInPortal(boolean inPortal);

    int getTimeInPortal();

    void teleported();

    boolean didTeleport();

    void setDidTP(boolean didTP);

    void increaseCooldown();
}
