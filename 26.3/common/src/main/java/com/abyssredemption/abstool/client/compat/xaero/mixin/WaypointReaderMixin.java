package com.abyssredemption.abstool.client.compat.xaero.mixin;

import com.abyssredemption.abstool.client.compat.xaero.XaeroWorldMapActions;
import com.abyssredemption.abstool.client.compat.xaero.XaeroWorldMapContract;
import java.util.ArrayList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.gui.IRightClickableElement;
import xaero.map.gui.dropdown.rightclick.RightClickOption;
import xaero.map.mods.gui.Waypoint;

@Mixin(targets = "xaero.map.mods.gui.WaypointReader", remap = false)
public abstract class WaypointReaderMixin {
    @Inject(method = XaeroWorldMapContract.MENU_METHOD, at = @At("RETURN"), remap = false)
    private void abstool$sharedActions(Waypoint waypoint, IRightClickableElement target,
            CallbackInfoReturnable<ArrayList<RightClickOption>> callback) {
        XaeroWorldMapActions.append(waypoint, target, callback.getReturnValue());
    }
}
