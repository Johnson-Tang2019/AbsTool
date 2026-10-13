package com.abyssredemption.abstool.verification;

import com.abyssredemption.abstool.client.waypoint.SharedWaypoint;
import com.abyssredemption.abstool.client.waypoint.SharedWaypointClient;
import com.abyssredemption.abstool.client.compat.xaero.XaeroWaypointBridge;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.LoggerFactory;

/** Actual Xaero classes and transformed menu method; isolated from production JARs. */
public final class WaypointMenuSmoke implements ClientModInitializer {
    private int ticks;
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!client.isGameLoadFinished() || client.gui.overlay()!=null || ++ticks!=40) return;
            try {
                verify();
                LoggerFactory.getLogger("abstool").info("ABSTOOL_WAYPOINT_MENU_SMOKE_PASSED");
                client.stop();
            } catch (ReflectiveOperationException failure) { throw new AssertionError(failure); }
        });
    }
    @SuppressWarnings("unchecked")
    private void verify() throws ReflectiveOperationException {
        check(XaeroWaypointBridge.compatible());
        Class<?> originalClass=Class.forName("xaero.common.minimap.waypoints.Waypoint");
        Object original=originalClass.getConstructor(int.class,int.class,int.class,String.class,String.class,int.class)
                .newInstance(10,64,20,"Menu test","M",15);
        Class<?> mapWaypoint=Class.forName("xaero.map.mods.gui.Waypoint");
        Object element=mapWaypoint.getConstructor(Object.class,boolean.class,String.class,double.class)
                .newInstance(original,true,"waypoints",1.0);
        Class<?> readerClass=Class.forName("xaero.map.mods.gui.WaypointReader");
        Object reader=readerClass.getConstructor().newInstance();
        var method=readerClass.getMethod("getRightClickOptions",mapWaypoint,Class.forName("xaero.map.gui.IRightClickableElement"));
        var state=SharedWaypointClient.state();
        state.hello(true); state.snapshot(0,List.of()); state.permissions(false);
        option((List<?>)method.invoke(reader,element,null),"publish",false);
        state.permissions(true);
        option((List<?>)method.invoke(reader,element,null),"publish",true);
        state.clear();
        option((List<?>)method.invoke(reader,element,null),"publish",false);
        UUID id=UUID.randomUUID();
        var shared=new SharedWaypoint(id,"Menu test","minecraft:overworld",10,64,20,15,"M",true,UUID.randomUUID(),1,1,1);
        state.hello(true); state.snapshot(1,List.of(shared)); state.permissions(true);
        var storage=XaeroWaypointBridge.class.getDeclaredField("lastStorage"); storage.setAccessible(true);
        var slots=XaeroWaypointBridge.class.getDeclaredField("slots"); slots.setAccessible(true);
        storage.set(null,new Storage(original,id));
        ((Map<UUID,String>)slots.get(null)).put(id,id.toString());
        var options=(List<?>)method.invoke(reader,element,null);
        option(options,"update",true); option(options,"remove",true);
        state.permissions(false);
        options=(List<?>)method.invoke(reader,element,null);
        option(options,"update",false); option(options,"remove",false);
        storage.set(null,null); ((Map<?,?>)slots.get(null)).clear(); state.clear();
    }
    private static void option(List<?> options,String suffix,boolean active) throws ReflectiveOperationException {
        Class<?> base=Class.forName("xaero.map.gui.dropdown.rightclick.RightClickOption");
        var name=base.getDeclaredField("name"); name.setAccessible(true);
        int count=0;
        for(Object option:options) if (name.get(option).equals("text.abstool.waypoint."+suffix)) {
            count++; check((boolean)base.getMethod("isActive").invoke(option)==active);
        }
        check(count==1);
    }
    public static final class Storage {
        private final Map<String,Object> waypoints;
        public Storage(Object original,UUID id) { waypoints=Map.of(id.toString(),original); }
        public Map<String,Object> getWaypoints() { return waypoints; }
    }
    private static void check(boolean value) { if (!value) throw new AssertionError("Xaero right-click menu mismatch"); }
}
