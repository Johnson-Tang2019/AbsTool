package com.abyssredemption.abstool.client.mobmerge;

import com.abyssredemption.abstool.client.vault.config.ConfigManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/** Client-only presentation. No entity removal, NBT writes, AI or collision changes. */
public final class MobMerge {
    private record Equipment(Object item, Object components) {}
    private record Appearance(Object type, Object pose, Object team, List<?> synced, List<Equipment> equipment, int hurt) {}
    private static Map<Integer,OverlapGroups.Member> groups=Map.of();
    private static Object level;
    private MobMerge() {}
    public static void clear() { groups=Map.of(); level=null; }
    public static void prepare(net.minecraft.client.DeltaTracker delta) {
        var client=Minecraft.getInstance();
        if(!ConfigManager.get().mergeOverlappingMobs || client.level==null || client.player==null) { clear(); return; }
        var candidates=new ArrayList<OverlapGroups.Candidate>();
        for(var entity:client.level.entitiesForRendering()) {
            if(!(entity instanceof Mob mob) || !mob.isAlive() || mob.isRemoved() || mob.isInvisible()
                    || mob.isPassenger() || mob.isVehicle() || mob.isLeashed() || mob.isCurrentlyGlowing()
                    || mob.deathTime>0 || mob.distanceToSqr(client.player)>128*128) continue;
            var equipment=new ArrayList<Equipment>();
            for(var slot:EquipmentSlot.values()) {
                var stack=mob.getItemBySlot(slot);
                equipment.add(new Equipment(stack.getItem(),stack.getComponentsPatch()));
            }
            var synced=mob.getEntityData().getNonDefaultValues();
            var appearance=new Appearance(mob.getType(),mob.getPose(),mob.getTeam(),synced==null?List.of():List.copyOf(synced),List.copyOf(equipment),mob.hurtTime);
            float partialTick=delta.getGameTimeDeltaPartialTick(!client.level.tickRateManager().isEntityFrozen(mob));
            var box=mob.getBoundingBox().move(mob.getPosition(partialTick).subtract(mob.position()));
            candidates.add(new OverlapGroups.Candidate(mob.getId(),appearance,box));
        }
        groups=OverlapGroups.build(candidates); level=client.level;
    }
    public static int count(Entity entity) {
        if(!ConfigManager.get().mergeOverlappingMobs || entity.level()!=level) return 1;
        var member=groups.get(entity.getId()); return member==null?1:member.count();
    }
    public static boolean hidden(Entity entity) {
        if(!ConfigManager.get().mergeOverlappingMobs || entity.level()!=level) return false;
        var member=groups.get(entity.getId()); return member!=null && member.representative()!=entity.getId();
    }
    public static void decorate(Entity entity,EntityRenderState state,float partialTick) {
        ((MobMergeState)state).abstool$mergedAway(hidden(entity));
        ((MobMergeState)state).abstool$groupCount(0);
        int count=count(entity);
        if(count<2 || hidden(entity) || state.distanceToCameraSq>64*64 || Minecraft.getInstance().gui.hud.isHidden()) return;
        ((MobMergeState)state).abstool$groupCount(count);
        state.nameTag=entity.getName().copy().append(" x "+count).withStyle(net.minecraft.ChatFormatting.WHITE);
        state.nameTagAttachment=entity.getAttachments().getNullable(EntityAttachment.NAME_TAG,0,entity.getYRot(partialTick));
        if(state.nameTagAttachment==null) state.nameTagAttachment=new Vec3(0,entity.getBbHeight()+0.5,0);
    }
}
