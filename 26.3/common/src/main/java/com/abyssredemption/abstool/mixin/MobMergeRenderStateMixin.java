package com.abyssredemption.abstool.mixin;

import com.abyssredemption.abstool.client.mobmerge.MobMergeState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public abstract class MobMergeRenderStateMixin implements MobMergeState {
    @Unique private boolean abstool$mergedAway;
    @Unique private int abstool$groupCount;
    public boolean abstool$mergedAway() { return abstool$mergedAway; }
    public void abstool$mergedAway(boolean value) { abstool$mergedAway=value; }
    public int abstool$groupCount() { return abstool$groupCount; }
    public void abstool$groupCount(int value) { abstool$groupCount=value; }
}
