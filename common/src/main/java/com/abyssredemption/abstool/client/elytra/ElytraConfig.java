package com.abyssredemption.abstool.client.elytra;

public final class ElytraConfig {
    public boolean enabled = false;
    public Trigger trigger = Trigger.SNEAK_JUMP;
    public int keyCode = 71;
    public boolean autoEquip = true;
    public int swapTicks = 10;
    public int requestTicks = 6;
    public enum Trigger { SNEAK_JUMP, KEY }
    public void validate() {
        if (trigger == null) trigger = Trigger.SNEAK_JUMP;
        if (keyCode < 32 || keyCode > 348) keyCode = -1;
        swapTicks = Math.max(2, Math.min(40, swapTicks));
        requestTicks = Math.max(1, Math.min(20, requestTicks));
    }
}
