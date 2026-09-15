package com.abyssredemption.abstool.client.elytra;

import com.abyssredemption.abstool.client.vault.config.ConfigManager;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

/** One bounded equipment transaction followed by normal jump input and a server-owned glide request. */
public final class ElytraLaunch {
    public enum State { IDLE, WAITING_FOR_SWAP, READY_TO_JUMP, WAITING_FOR_AIR, WAITING_FOR_GLIDE_CONFIRM }
    public enum Owner { NONE, ABS_TOOL, TWEAKEROO }
    public interface EquipmentBridge {
        boolean enabled();
        void equip(LocalPlayer player) throws ReflectiveOperationException;
    }
    public static EquipmentBridge external;
    private static State state = State.IDLE;
    private static Owner owner = Owner.NONE;
    private static LocalPlayer actor;
    private static Object level;
    private static boolean held;
    private static int ticks, since, requests, cooldown;
    private static ItemStack equipped = ItemStack.EMPTY;
    public static State state() { return state; }
    public static Owner owner() { return owner; }
    public static int requests() { return requests; }
    public static boolean usable(ItemStack stack) {
        return !stack.isEmpty() && !stack.nextDamageWillBreak()
                && LivingEntity.canGlideUsing(stack, EquipmentSlot.CHEST)
                && stack.get(net.minecraft.core.component.DataComponents.EQUIPPABLE) != null
                && stack.get(net.minecraft.core.component.DataComponents.EQUIPPABLE).slot() == EquipmentSlot.CHEST
                && stack.get(net.minecraft.core.component.DataComponents.EQUIPPABLE)
                    .canBeEquippedBy(net.minecraft.world.entity.EntityTypes.PLAYER.builtInRegistryHolder());
    }
    public static void reset() {
        state = State.IDLE; owner = Owner.NONE; actor = null; level = null; equipped = ItemStack.EMPTY;
    }
    private static boolean safe(Minecraft client) {
        var p = client.player;
        return p != null && client.level != null && client.gameMode != null && client.gui.screen() == null
                && client.isWindowActive() && p.isAlive() && !p.isSpectator() && !p.getAbilities().flying
                && !p.isPassenger() && !p.isInLiquid() && !p.onClimbable() && !p.hasEffect(MobEffects.LEVITATION)
                && p.containerMenu == p.inventoryMenu && p.inventoryMenu.getCarried().isEmpty();
    }
    public static void guard(Minecraft client) {
        if (!ConfigManager.get().elytraAssist.enabled || !safe(client)
                || actor != null && (actor != client.player || level != client.level)) reset();
    }
    private static void fail(String reason) {
        if (actor != null) actor.sendSystemMessage(Component.translatable("text.abstool.elytra.failure." + reason));
        reset(); cooldown = 10;
    }
    public static void begin(Minecraft client) {
        if (state != State.IDLE || cooldown > 0 || !ConfigManager.get().elytraAssist.enabled || !safe(client)) return;
        var p = client.player;
        if (!p.onGround() || p.isFallFlying()) return;
        actor = p; level = client.level; since = ticks; requests = 0;
        if (usable(p.getItemBySlot(EquipmentSlot.CHEST))) { ready(); return; }
        int source = -1;
        for (int slot = 9; slot < 45; slot++) {
            if (usable(p.inventoryMenu.getSlot(slot).getItem())) { source = slot; break; }
        }
        if (source < 0) { fail("missing"); return; }
        if (!p.inventoryMenu.getSlot(6).mayPickup(p)) { fail("equipment"); return; }
        state = State.WAITING_FOR_SWAP;
        if (external != null && external.enabled()) {
            owner = Owner.TWEAKEROO;
            try { external.equip(p); observeEquipment(); }
            catch (ReflectiveOperationException | LinkageError e) { fail("external"); }
            return;
        }
        owner = Owner.ABS_TOOL;
        if (!ConfigManager.get().elytraAssist.autoEquip) { fail("disabled"); return; }
        // Three vanilla SWAP clicks preserve every stack, including a full inventory and the temporary hotbar slot.
        int hotbar = source == 36 ? 1 : 0;
        client.gameMode.handleContainerInput(p.inventoryMenu.containerId, source, hotbar, ContainerInput.SWAP, p);
        client.gameMode.handleContainerInput(p.inventoryMenu.containerId, 6, hotbar, ContainerInput.SWAP, p);
        client.gameMode.handleContainerInput(p.inventoryMenu.containerId, source, hotbar, ContainerInput.SWAP, p);
        observeEquipment();
    }
    private static void observeEquipment() {
        if (usable(actor.getItemBySlot(EquipmentSlot.CHEST))) equipped = actor.getItemBySlot(EquipmentSlot.CHEST).copy();
    }
    private static void ready() {
        equipped = actor.getItemBySlot(EquipmentSlot.CHEST).copy();
        state = State.READY_TO_JUMP; since = ticks;
    }
    /** Called after physical input polling and before vanilla consumes the jump edge. */
    public static void input(LocalPlayer player) {
        var client = Minecraft.getInstance();
        ticks++; if (cooldown > 0) cooldown--;
        guard(client);
        var config = ConfigManager.get().elytraAssist;
        boolean down = client.isWindowActive() && (config.trigger == ElytraConfig.Trigger.SNEAK_JUMP
                ? player.input.keyPresses.shift() && player.input.keyPresses.jump()
                : config.keyCode >= 0 && InputConstants.isKeyDown(client.getWindow(), config.keyCode));
        boolean edge = down && !held; held = down;
        if (edge) begin(client);
        if (state == State.IDLE) return;
        if (state == State.WAITING_FOR_SWAP) {
            if (!equipped.isEmpty() && !ItemStack.isSameItemSameComponents(equipped, player.getItemBySlot(EquipmentSlot.CHEST))) {
                fail("equipment"); return;
            }
            if (usable(player.getItemBySlot(EquipmentSlot.CHEST)) && ticks > since) ready();
            else if (ticks - since >= config.swapTicks) { fail("swap_timeout"); return; }
        }
        if (state != State.WAITING_FOR_SWAP && !ItemStack.isSameItemSameComponents(equipped, player.getItemBySlot(EquipmentSlot.CHEST))) {
            fail("equipment"); return;
        }
        boolean jump = state == State.READY_TO_JUMP;
        if (jump && !player.onGround()) { fail("equipment"); return; }
        Input keys = player.input.keyPresses;
        player.input.keyPresses = new Input(keys.forward(), keys.backward(), keys.left(), keys.right(), jump, keys.shift(), keys.sprint());
        if (jump) { state = State.WAITING_FOR_AIR; since = ticks; }
        if (ticks - since > config.requestTicks + 40) fail("timeout");
    }
    /** Movement is sent by vanilla first so the server sees the actual off-ground position before the command. */
    public static void afterMovement(LocalPlayer p) {
        guard(Minecraft.getInstance());
        if (actor != p || state == State.IDLE || state == State.WAITING_FOR_SWAP || state == State.READY_TO_JUMP) return;
        if (p.isFallFlying()) { reset(); cooldown = 10; return; }
        if (!usable(p.getItemBySlot(EquipmentSlot.CHEST))) { fail("equipment"); return; }
        if (state == State.WAITING_FOR_AIR && !p.onGround()) {
            p.connection.send(new ServerboundPlayerCommandPacket(p, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
            requests++; state = State.WAITING_FOR_GLIDE_CONFIRM; since = ticks;
        } else if (state == State.WAITING_FOR_AIR && ticks - since > ConfigManager.get().elytraAssist.requestTicks) fail("timeout");
        else if (state == State.WAITING_FOR_GLIDE_CONFIRM && ticks - since > 40) fail("timeout");
    }
    private ElytraLaunch() {}
}
