package com.abyssredemption.abstool.fabric.compat.xaero;

import com.abyssredemption.abstool.client.vault.config.ConfigManager;
import com.abyssredemption.abstool.client.waypoint.SharedWaypoint;
import com.abyssredemption.abstool.client.waypoint.SharedWaypointClient;
import com.abyssredemption.abstool.client.waypoint.SharedWaypointClientState;
import com.abyssredemption.abstool.client.waypoint.WaypointDraft;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** The only code that inspects Xaero internals. Enabled solely for verified 26.5.0. */
public final class XaeroWaypointBridge {
    private static final Identifier ORIGIN = Identifier.fromNamespaceAndPath("abstool", "server_waypoints");
    private static final String SCREEN = "xaero.common.gui.GuiWaypoints";
    private static final String WAYPOINT = "xaero.common.minimap.waypoints.Waypoint";
    private static Object lastStorage;
    private static Object lastSession;
    private static String lastDimension;
    private static long lastRevision = -1;
    private static long ticks;
    private static final Map<UUID,String> slots = new HashMap<>();
    private XaeroWaypointBridge() {}
    public static boolean installed() { return FabricLoader.getInstance().isModLoaded("xaerominimap"); }
    public static boolean compatible() {
        return FabricLoader.getInstance().getModContainer("xaerominimap")
                .map(m -> "26.5.0".equals(m.getMetadata().getVersion().getFriendlyString())).orElse(false);
    }
    public static String status() { return !installed()?"missing":compatible()?"installed":"unsupported"; }
    public static void init() { SharedWaypointClient.xaeroStatus(status()); if (compatible()) SharedWaypointClient.display(XaeroWaypointBridge::reconcile); }
    public static boolean isWaypointScreen(Object screen) { return compatible() && screen.getClass().getName().equals(SCREEN); }
    public static void clear() {
        if(lastStorage!=null) try { resetStorage(lastStorage); } catch(ReflectiveOperationException ignored) { }
        lastStorage=null; lastSession=null; lastDimension=null; lastRevision=-1; slots.clear();
    }
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void reconcile(SharedWaypointClientState state) {
        if (!compatible() || !state.supported() || !ConfigManager.get().serverWaypoints.enabled
                || !ConfigManager.get().serverWaypoints.showInXaero) { clear(); return; }
        Minecraft mc=Minecraft.getInstance();
        if(mc.level==null) { clear(); return; }
        String dimension=mc.level.dimension().identifier().toString();
        try {
            Object session=currentSession();
            if(session==null) return;
            Object manager=call(session,"getWorldManager");
            Object world=call(manager,"getCurrentWorld");
            if(world==null) return;
            Object container=call(world,"getContainer");
            Object thirdParty=call(container,"getThirdPartyWaypointManager");
            Object storage=call(thirdParty,"get",new Class<?>[]{Identifier.class},ORIGIN);
            if(!(call(storage,"getWaypoints") instanceof Map display)) return;
            boolean changed=storage!=lastStorage || session!=lastSession || !dimension.equals(lastDimension)
                    || state.datasetRevision()!=lastRevision;
            if(!changed && ++ticks%100!=0) return;
            if(lastStorage!=null && lastStorage!=storage) resetStorage(lastStorage);
            resetStorage(storage); slots.clear();
            for(SharedWaypoint w:state.waypoints().values()) {
                if(!w.enabled() || !dimension.equals(w.dimensionId())) continue;
                try {
                    Object waypoint=Class.forName(WAYPOINT).getConstructor(int.class,int.class,int.class,String.class,String.class,int.class)
                            .newInstance(w.x(),w.y(),w.z(),w.name(),w.symbol(),w.color());
                    String slot=w.id().toString();
                    call(storage,"add",new Class<?>[]{String.class,Class.forName(WAYPOINT)},slot,waypoint);
                    slots.put(w.id(),slot);
                } catch(ReflectiveOperationException | IllegalArgumentException ignored) {
                    // A malformed entry must not prevent the other waypoints from appearing.
                }
            }
            lastStorage=storage; lastSession=session; lastDimension=dimension; lastRevision=state.datasetRevision();
        } catch(ReflectiveOperationException | IllegalArgumentException e) {
            clear();
        }
    }
    private static Object currentSession() throws ReflectiveOperationException {
        Class<?> hud=Class.forName("xaero.common.HudMod");
        Object instance=hud.getField("INSTANCE").get(null);
        if(instance==null) return null;
        Object mod=call(call(instance,"getHud"),"getModuleManager");
        Object modules=call(mod,"getModules");
        if(!(modules instanceof Iterable<?> iterable)) return null;
        for(Object module:iterable) {
            Object candidate=call(module,"getCurrentSession");
            if(candidate!=null && candidate.getClass().getName().equals("xaero.hud.minimap.module.MinimapSession")) return candidate;
        }
        return null;
    }
    private static Object call(Object object,String name) throws ReflectiveOperationException { return call(object,name,new Class<?>[0]); }
    private static Object call(Object object,String name,Class<?>[] types,Object... args) throws ReflectiveOperationException {
        try { return object.getClass().getMethod(name,types).invoke(object,args); }
        catch(InvocationTargetException e) { throw new ReflectiveOperationException(e.getCause()); }
    }
    private static Object field(Object object,String name) throws ReflectiveOperationException {
        Field f=object.getClass().getDeclaredField(name); f.setAccessible(true); return f.get(object);
    }
    private static void resetStorage(Object storage) throws ReflectiveOperationException {
        call(storage,"clear");
        if(field(storage,"renderInfoOverrides") instanceof Map<?,?> overrides) overrides.clear();
    }
    @SuppressWarnings("unchecked")
    public static List<Object> selection(Object screen) {
        if(!isWaypointScreen(screen)) return List.of();
        try { Method m=screen.getClass().getDeclaredMethod("getSelectedWaypointsList"); m.setAccessible(true); return (List<Object>)m.invoke(screen); }
        catch(ReflectiveOperationException | ClassCastException e) { return List.of(); }
    }
    public static SharedWaypoint serverWaypoint(Object selected) {
        if(lastStorage==null) return null;
        try {
            if(!(call(lastStorage,"getWaypoints") instanceof Map<?,?> map)) return null;
            for(var entry:slots.entrySet()) if(map.get(entry.getValue())==selected)
                return SharedWaypointClient.state().waypoints().get(entry.getKey());
        } catch(ReflectiveOperationException ignored) { }
        return null;
    }
    public static boolean isLocalWaypoint(Object selected) {
        try { return selected!=null && !(boolean)call(selected,"isThirdParty"); }
        catch(ReflectiveOperationException | ClassCastException e) { return false; }
    }
    public static WaypointDraft draft(Object screen,Object waypoint) {
        if(!isWaypointScreen(screen) || waypoint==null) return null;
        try {
            Object world=field(screen,"displayedWorld");
            if(world==null) return null;
            Object dim=call(world,"getDimId");
            if(dim==null) return null;
            String dimension=call(dim,"identifier").toString();
            return new WaypointDraft((String)call(waypoint,"getName"),dimension,
                    (int)call(waypoint,"getX"),(int)call(waypoint,"getY"),(int)call(waypoint,"getZ"),
                    (int)call(waypoint,"getColor"),(String)call(waypoint,"getSymbol"),
                    !(boolean)call(waypoint,"isDisabled"));
        } catch(ReflectiveOperationException | ClassCastException e) { return null; }
    }
    public static void publish(Object screen) {
        List<Object> selected=selection(screen);
        if(selected.size()!=1 || !isLocalWaypoint(selected.getFirst())) return;
        WaypointDraft draft=draft(screen,selected.getFirst());
        if(draft==null || !SharedWaypointClient.create(draft)) notifyUser("text.abstool.waypoint.publish_failed");
    }
    public static void update(Object screen) {
        List<Object> selected=selection(screen);
        if(selected.size()!=1) return;
        SharedWaypoint old=serverWaypoint(selected.getFirst()); WaypointDraft draft=draft(screen,selected.getFirst());
        if(old==null || draft==null || !SharedWaypointClient.update(old,draft)) notifyUser("text.abstool.waypoint.publish_failed");
    }
    public static void remove(Object screen) {
        List<Object> selected=selection(screen);
        if(selected.size()!=1) return;
        SharedWaypoint old=serverWaypoint(selected.getFirst());
        if(old==null || !SharedWaypointClient.delete(old)) notifyUser("text.abstool.waypoint.publish_failed");
    }
    private static void notifyUser(String key) {
        if(Minecraft.getInstance().player!=null) Minecraft.getInstance().player.sendSystemMessage(Component.translatable(key));
    }
}
