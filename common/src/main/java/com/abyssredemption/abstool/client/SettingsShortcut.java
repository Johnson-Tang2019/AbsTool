package com.abyssredemption.abstool.client;

/** Edge-triggered chord, independent of the feature's enabled state. */
public final class SettingsShortcut {
    private boolean wasDown;

    public boolean update(boolean rDown, boolean bDown, boolean canOpen) {
        boolean down = rDown && bDown;
        boolean open = down && !wasDown && canOpen;
        wasDown = down;
        return open;
    }
}
