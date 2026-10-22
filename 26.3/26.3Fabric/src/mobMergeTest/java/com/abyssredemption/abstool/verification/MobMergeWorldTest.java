package com.abyssredemption.abstool.verification;

import com.abyssredemption.abstool.client.mobmerge.MobMerge;
import com.abyssredemption.abstool.client.vault.config.ConfigManager;
import com.abyssredemption.abstool.client.vault.config.ClothConfigScreenFactory;
import com.abyssredemption.abstool.client.waypoint.SharedWaypoint;
import com.abyssredemption.abstool.client.waypoint.SharedWaypointClient;
import com.abyssredemption.abstool.client.waypoint.gui.WaypointVisibility;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

/** Creates a disposable real world, renders with the installed GPU, and saves actual frame captures. */
public final class MobMergeWorldTest implements FabricClientGameTest {
    public void runTest(ClientGameTestContext context) {
        String mode=FabricLoader.getInstance().isModLoaded("iris")?"iris":"vanilla";
        try(var server=context.worldBuilder().createServer();var connection=server.connect()) {
            server.runCommand("tick freeze");
            server.runCommand("gamemode spectator @a");
            server.runCommand("time set 3000");
            server.runCommand("weather clear");
            server.runCommand("fill -12 98 -8 12 98 16 minecraft:stone_bricks");
            server.runCommand("fill -11 99 -7 11 99 15 minecraft:grass_block");
            server.runCommand("fill -11 100 -7 11 108 15 minecraft:air");
            server.runCommand("tp @a 0.5 101.5 -2.5 0 10");
            for(int i=0;i<16;i++) server.runCommand("summon cow -3.5 100 5.5 {NoAI:1b,NoGravity:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"merge_fixture\"]}");
            for(int i=0;i<10;i++) server.runCommand("summon pig 0.5 100 5.5 {NoAI:1b,NoGravity:1b,Silent:1b,PersistenceRequired:1b,Tags:[\"merge_fixture\"]}");
            for(int i=0;i<8;i++) server.runCommand("summon sheep 4.5 100 5.5 {NoAI:1b,NoGravity:1b,Silent:1b,PersistenceRequired:1b,Color:0b,Tags:[\"merge_fixture\"]}");
            server.runCommand("summon pig 0.5 100 9.5 {NoAI:1b,NoGravity:1b,Silent:1b,PersistenceRequired:1b}");
            context.runOnClient(client -> {
                ConfigManager.get().mergeOverlappingMobs=false;
                ConfigManager.get().screenshotToClipboard=false;
                client.options.renderDistance().set(8);
                client.options.guiScale().set(2);
                client.resizeGui();
                client.gui.hud.getChat().clearMessages(true);
            });
            context.getInput().lookAt(new BlockPos(0,101,6));
            context.waitFor(client -> client.level!=null && client.level.getEntitiesOfClass(Mob.class,new net.minecraft.world.phys.AABB(-6,99,3,7,104,8)).size()==34,400);
            context.waitTicks(70);
            context.runOnClient(client -> client.gui.hud.getChat().clearMessages(true));
            if(mode.equals("iris")) context.runOnClient(client -> {
                try {
                    var api=Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                    Object instance=api.getMethod("getInstance").invoke(null);
                    if(!(boolean)api.getMethod("isShaderPackInUse").invoke(instance)) throw new AssertionError("Iris shaderpack is not active");
                } catch(ReflectiveOperationException error) { throw new AssertionError(error); }
            });
            context.takeScreenshot("mobmerge-"+mode+"-off");
            context.runOnClient(client -> ConfigManager.get().mergeOverlappingMobs=true);
            context.waitTicks(30);
            context.runOnClient(client -> {
                int hidden=0;
                for(var entity:client.level.entitiesForRendering()) if(entity instanceof Mob && entity.getZ()<8 && entity.getZ()>4
                        && List.of("minecraft:cow","minecraft:pig","minecraft:sheep").contains(type(entity))) {
                    int expected=type(entity).equals("minecraft:cow")?16:type(entity).equals("minecraft:pig")?10:8;
                    if(MobMerge.count(entity)!=expected) throw new AssertionError("Group mismatch: "+entity.getType()+" count="+MobMerge.count(entity));
                    if(MobMerge.hidden(entity)) hidden++;
                    else {
                        var state=client.getEntityRenderDispatcher().extractEntity(entity,1);
                        if(state.nameTag==null || !state.nameTag.getString().endsWith(" x "+expected)) throw new AssertionError("Count label absent");
                    }
                }
                if(hidden!=31) throw new AssertionError("Expected 31 culled duplicates, got "+hidden);
            });
            context.takeScreenshot("mobmerge-"+mode+"-on");
            server.runCommand("tp @e[type=cow,limit=1] -2.5 100 5.5");
            server.runCommand("tick step 4");
            context.waitTicks(15);
            context.runOnClient(client -> {
                var cows=client.level.getEntitiesOfClass(Mob.class,new net.minecraft.world.phys.AABB(-5,99,4,-1,104,7));
                long separate=cows.stream().filter(e -> MobMerge.count(e)==1 && !MobMerge.hidden(e)).count();
                System.out.println("MOB_MERGE_SEPARATED "+separate);
                if(separate!=1) throw new AssertionError("Separating mob did not restore its rendering: "+separate);
            });
            context.takeScreenshot("mobmerge-"+mode+"-separated");
            context.runOnClient(client -> {
                var state=SharedWaypointClient.state(); state.hello(true); state.permissions(false);
                UUID author=new UUID(0,1);
                state.snapshot(1,List.of(point("牛棚",-3,100,5,6,author),point("猪舍",0,100,5,6,author),point("羊圈",4,100,5,10,author)));
                state.categories(1,Map.of(6,"养殖区",10,"牧场"));
            });
            context.waitTicks(12);
            context.takeScreenshot("waypoints-"+mode+"-all");
            context.runOnClient(client -> WaypointVisibility.toggle(6));
            context.waitTicks(12);
            context.runOnClient(client -> {
                if(!WaypointVisibility.hidden(6) || WaypointVisibility.hidden(10) || SharedWaypointClient.state().waypointCount()!=3)
                    throw new AssertionError("Personal color hiding changed authoritative data");
                ConfigManager.load();
                if(!WaypointVisibility.hidden(6)) throw new AssertionError("Hidden color preference was not persisted");
                try {
                    var bridge=com.abyssredemption.abstool.client.compat.xaero.XaeroWaypointBridge.class;
                    var field=bridge.getDeclaredField("lastStorage"); field.setAccessible(true);
                    Object storage=field.get(null);
                    var displayed=(Map<?,?>)storage.getClass().getMethod("getWaypoints").invoke(storage);
                    if(displayed.size()!=1) throw new AssertionError("Xaero should retain only the other color, got "+displayed.size());
                } catch(ReflectiveOperationException error) { throw new AssertionError(error); }
            });
            context.takeScreenshot("waypoints-"+mode+"-color-hidden");
            context.runOnClient(client -> {
                var screen=(me.shedaniel.clothconfig2.gui.ClothConfigScreen)ClothConfigScreenFactory.create(null);
                int i=0;
                for(var key:screen.getCategorizedEntries().keySet()) {
                    if(key.equals(Component.translatable("text.abstool.waypoint.category"))) { screen.selectedCategoryIndex=i; break; } i++;
                }
                client.gui.setScreen(screen);
            });
            context.waitTicks(12);
            context.runOnClient(client -> {
                var screen=(me.shedaniel.clothconfig2.gui.ClothConfigScreen)client.gui.screen();
                for(var entries:screen.getCategorizedEntries().values()) for(var entry:entries)
                    if(entry instanceof com.abyssredemption.abstool.client.waypoint.gui.SharedWaypointsEntry browser)
                        for(var child:List.copyOf(browser.children()))
                            if(child instanceof net.minecraft.client.gui.components.Button button
                                    && button.getMessage().equals(Component.translatable("text.abstool.waypoint.collapse_all"))) button.onPress(null);
            });
            context.waitTicks(4);
            context.takeScreenshot("waypoint-categories-"+mode);
            context.runOnClient(client -> {
                var screen=(me.shedaniel.clothconfig2.gui.ClothConfigScreen)ClothConfigScreenFactory.create(null);
                screen.selectedCategoryIndex=2;
                client.gui.setScreen(screen);
            });
            context.waitTicks(10);
            context.takeScreenshot("optimization-"+mode);
            context.runOnClient(client -> {
                client.gui.setScreen(null);
                WaypointVisibility.toggle(6); ConfigManager.load();
                if(WaypointVisibility.hidden(6)) throw new AssertionError("Showing a color again did not survive reload");
                ConfigManager.get().mergeOverlappingMobs=false;
            });
            context.waitTicks(10);
            context.runOnClient(client -> {
                for(var entity:client.level.entitiesForRendering()) if(MobMerge.hidden(entity)) throw new AssertionError("OFF retained hidden entities");
                try {
                    var field=com.abyssredemption.abstool.client.compat.xaero.XaeroWaypointBridge.class.getDeclaredField("lastStorage"); field.setAccessible(true);
                    Object storage=field.get(null);
                    var points=(Map<?,?>)storage.getClass().getMethod("getWaypoints").invoke(storage);
                    if(points.size()!=3) throw new AssertionError("Showing a color again did not restore Xaero points");
                } catch(ReflectiveOperationException error) { throw new AssertionError(error); }
                System.out.println("ABSTOOL_MOB_MERGE_WORLD_PASS "+mode);
            });
            server.runCommand("tick unfreeze");
            context.waitTicks(40);
        }
    }
    private static SharedWaypoint point(String name,int x,int y,int z,int color,UUID author) {
        return new SharedWaypoint(UUID.randomUUID(),name,"minecraft:overworld",x,y,z,color,"P",true,author,1,1,1);
    }
    private static String type(net.minecraft.world.entity.Entity entity) {
        return net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
    }
}
