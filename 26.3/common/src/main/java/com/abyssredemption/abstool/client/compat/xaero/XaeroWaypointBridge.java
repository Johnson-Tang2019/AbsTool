package com.abyssredemption.abstool.client.compat.xaero;

import com.abyssredemption.abstool.client.vault.config.ConfigManager;
import com.abyssredemption.abstool.client.waypoint.SharedWaypoint;
import com.abyssredemption.abstool.client.waypoint.SharedWaypointClient;
import com.abyssredemption.abstool.client.waypoint.SharedWaypointClientState;
import com.abyssredemption.abstool.client.waypoint.WaypointDraft;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** The only code that inspects Xaero internals; integration requires a matching runtime contract. */
public final class XaeroWaypointBridge {
    private static final Identifier ORIGIN = Identifier.fromNamespaceAndPath("abstool", "server_waypoints");
    private static final String SCREEN = "xaero.common.gui.GuiWaypoints";
    private static final String WAYPOINT = "xaero.common.minimap.waypoints.Waypoint";
    private static Object lastStorage;
    private static Object lastSession;
    private static String lastDimension;
    private static long lastRevision = -1;
    private static int lastHiddenColors = -1;
    private static long ticks;
    private static final Map<UUID,String> slots = new HashMap<>();
    private static Boolean contractSupported;
    private static boolean minimapInstalled;
    private XaeroWaypointBridge() {}
    public static boolean installed() { return minimapInstalled; }
    public static synchronized boolean compatible() {
        if (!installed()) return false;
        if (contractSupported == null) contractSupported = inspectContract();
        return contractSupported;
    }
    private static boolean inspectContract() {
        return verifyContract(XaeroWaypointBridge.class.getClassLoader());
    }
    public static boolean verifyContract(ClassLoader loader) {
        try {
            Class<?> hudMod = inspectClass("xaero.common.HudMod", loader);
            Field instance = hudMod.getField("INSTANCE");
            if (!Modifier.isStatic(instance.getModifiers()) || instance.getType() != hudMod) return false;
            Class<?> hud = hudMod.getMethod("getHud").getReturnType();
            Class<?> moduleManager = hud.getMethod("getModuleManager").getReturnType();
            if (!Iterable.class.isAssignableFrom(moduleManager.getMethod("getModules").getReturnType())) return false;
            inspectClass("xaero.hud.module.HudModule", loader).getMethod("getCurrentSession");

            Class<?> session = inspectClass("xaero.hud.minimap.module.MinimapSession", loader);
            Class<?> worldManager = session.getMethod("getWorldManager").getReturnType();
            Class<?> world = worldManager.getMethod("getCurrentWorld").getReturnType();
            if (world != inspectClass("xaero.hud.minimap.world.MinimapWorld", loader)) return false;
            world.getMethod("getDimId");
            Class<?> container = world.getMethod("getContainer").getReturnType();
            Class<?> thirdParty = container.getMethod("getThirdPartyWaypointManager").getReturnType();
            Class<?> storage = thirdParty.getMethod("get", Identifier.class).getReturnType();
            if (!Map.class.isAssignableFrom(storage.getMethod("getWaypoints").getReturnType())) return false;
            storage.getMethod("add", String.class, inspectClass(WAYPOINT, loader));
            storage.getMethod("clear");
            if (!Map.class.isAssignableFrom(storage.getDeclaredField("renderInfoOverrides").getType())) return false;

            Class<?> waypoint = inspectClass(WAYPOINT, loader);
            waypoint.getConstructor(int.class, int.class, int.class, String.class, String.class, int.class);
            for (String name : List.of("getName", "getX", "getY", "getZ", "getColor", "getSymbol", "isDisabled", "isThirdParty")) {
                waypoint.getMethod(name);
            }
            Class<?> screen = inspectClass(SCREEN, loader);
            if (!List.class.isAssignableFrom(screen.getDeclaredMethod("getSelectedWaypointsList").getReturnType())) return false;
            if (screen.getDeclaredField("displayedWorld").getType() != world) return false;
            return true;
        } catch (ReflectiveOperationException | LinkageError | SecurityException failure) {
            com.abyssredemption.abstool.AbsTool.LOGGER.warn("Xaero waypoint contract is unavailable", failure);
            return false;
        }
    }
    private static Class<?> inspectClass(String name) throws ClassNotFoundException {
        return inspectClass(name, XaeroWaypointBridge.class.getClassLoader());
    }
    private static Class<?> inspectClass(String name, ClassLoader loader) throws ClassNotFoundException {
        return Class.forName(name, false, loader);
    }
    private static void disableBridge(Exception failure) {
        com.abyssredemption.abstool.AbsTool.LOGGER.warn("Xaero waypoint integration disabled after a contract failure", failure);
        contractSupported = false;
        clear();
        SharedWaypointClient.xaeroStatus("unsupported");
    }
    public static String status() { return !installed()?"missing":compatible()?"installed":"unsupported"; }
    public static void init(boolean installed) {
        minimapInstalled = installed;
        contractSupported = null;
        SharedWaypointClient.xaeroStatus(status());
        if (compatible()) SharedWaypointClient.display(XaeroWaypointBridge::reconcile);
    }
    public static boolean isWaypointScreen(Object screen) {
        if (!compatible() || screen == null) return false;
        try { return inspectClass(SCREEN).isInstance(screen); }
        catch (ClassNotFoundException e) { return false; }
    }
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
                    || state.datasetRevision()!=lastRevision
                    || lastHiddenColors!=com.abyssredemption.abstool.client.waypoint.gui.WaypointVisibility.mask();
            if(!changed && ++ticks%100!=0) return;
            if(lastStorage!=null && lastStorage!=storage) resetStorage(lastStorage);
            resetStorage(storage); slots.clear();
            for(SharedWaypoint w:state.waypoints().values()) {
                if(!w.enabled() || !dimension.equals(w.dimensionId())
                        || com.abyssredemption.abstool.client.waypoint.gui.WaypointVisibility.hidden(w.color())) continue;
                try {
                    Object waypoint=Class.forName(WAYPOINT).getConstructor(int.class,int.class,int.class,String.class,String.class,int.class)
                            .newInstance(w.x(),w.y(),w.z(),com.abyssredemption.abstool.client.waypoint.gui.ServerWaypointIcon.displayName(w.name()),w.symbol(),w.color());
                    String slot=w.id().toString();
                    call(storage,"add",new Class<?>[]{String.class,Class.forName(WAYPOINT)},slot,waypoint);
                    slots.put(w.id(),slot);
                } catch(ReflectiveOperationException | IllegalArgumentException ignored) {
                    // A malformed entry must not prevent the other waypoints from appearing.
                }
            }
            lastStorage=storage; lastSession=session; lastDimension=dimension; lastRevision=state.datasetRevision();
            lastHiddenColors=com.abyssredemption.abstool.client.waypoint.gui.WaypointVisibility.mask();
        } catch(ReflectiveOperationException | IllegalArgumentException e) {
            disableBridge(e);
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
            if(candidate!=null && inspectClass("xaero.hud.minimap.module.MinimapSession").isInstance(candidate)) return candidate;
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
            return draftInWorld(world, waypoint);
        } catch(ReflectiveOperationException e) { return null; }
    }
    public static WaypointDraft draftInWorld(Object world, Object waypoint) {
        if (!compatible() || waypoint == null) return null;
        try {
            if(world==null) return null;
            Object dim=call(world,"getDimId");
            if(dim==null) return null;
            String dimension=call(dim,"identifier").toString();
            var shared = serverWaypoint(waypoint);
            return new WaypointDraft(shared == null ? (String)call(waypoint,"getName") : shared.name(),dimension,
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
