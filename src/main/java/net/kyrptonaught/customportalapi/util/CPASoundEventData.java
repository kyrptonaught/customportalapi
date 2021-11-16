package net.kyrptonaught.customportalapi.util;

import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;

public record CPASoundEventData(SoundEvent sound, float pitch, float volume) {

    public PositionedSoundInstance getInstance() {
        return PositionedSoundInstance.ambient(sound, pitch, volume);
    }
}
