package com.abyssredemption.abstool.client.waypoint;

import com.abyssredemption.abstool.client.serverstats.network.ProtocolConstants;
import com.abyssredemption.abstool.client.vault.config.ConfigManager;
import com.abyssredemption.abstool.client.waypoint.network.WaypointPayloads;
import com.abyssredemption.abstool.client.waypoint.network.WaypointCategories;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Lifecycle and transport for the optional server feature. */
public final class SharedWaypointClient {
    private static final SharedWaypointClientState STATE = new SharedWaypointClientState();
    private static Consumer<CustomPacketPayload> sender;
    private static Predicate<Identifier> canSend;
    private static Consumer<SharedWaypointClientState> display = ignored -> {};
    private static String xaeroStatus = "unsupported";
    private static long nextRequestId;
    private static long pendingSync;
    private static long lastSyncTick;
    private static long ticks;
    private SharedWaypointClient() {}
    public static SharedWaypointClientState state() { return STATE; }
    public static String xaeroStatus() { return xaeroStatus; }
    public static void xaeroStatus(String value) { xaeroStatus=value; }
    public static void install(Consumer<CustomPacketPayload> send, Predicate<Identifier> available) { sender=send; canSend=available; }
    public static void display(Consumer<SharedWaypointClientState> adapter) { display=adapter; }
    public static void clear() { STATE.clear(); pendingSync=0; display.accept(STATE); }
    public static void tick() {
        ticks++;
        if (STATE.supported() && !STATE.synced() && ticks-lastSyncTick >= 200) sync();
        display.accept(STATE);
    }
    public static void hello(int capabilities) {
        STATE.hello((capabilities & ProtocolConstants.CAP_SHARED_WAYPOINTS)!=0);
        display.accept(STATE);
        if (STATE.supported()) sync();
    }
    public static void sync() {
        if (!STATE.supported() || sender==null || canSend==null || !canSend.test(WaypointPayloads.SyncRequest.TYPE.id())) return;
        pendingSync=++nextRequestId; lastSyncTick=ticks;
        sender.accept(new WaypointPayloads.SyncRequest(ProtocolConstants.VERSION,pendingSync));
        if (canSend.test(WaypointCategories.Request.TYPE.id())) sender.accept(new WaypointCategories.Request(ProtocolConstants.VERSION,++nextRequestId,false,0,0,""));
    }
    public static void snapshot(WaypointPayloads.Snapshot p) {
        if (p.version()!=ProtocolConstants.VERSION || !STATE.supported() || p.requestId()!=pendingSync) return;
        var result=STATE.snapshot(p.datasetRevision(),p.waypoints());
        if (result==SharedWaypointClientState.ApplyResult.RESYNC) sync();
        else if (result==SharedWaypointClientState.ApplyResult.APPLIED) display.accept(STATE);
    }
    public static void delta(WaypointPayloads.Delta p) {
        if (p.version()!=ProtocolConstants.VERSION || !STATE.supported()) return;
        var result=STATE.delta(p.datasetRevision(),p.operation(),p.id(),p.revision(),p.waypoint());
        if (result==SharedWaypointClientState.ApplyResult.RESYNC) sync();
        else if (result==SharedWaypointClientState.ApplyResult.APPLIED) display.accept(STATE);
    }
    public static void permissions(WaypointPayloads.Permissions p) {
        if (p.version()==ProtocolConstants.VERSION) STATE.permissions(p.canRead() && p.canManage());
    }
    public static void mutationResult(WaypointPayloads.MutationResult p) {
        if (p.version()!=ProtocolConstants.VERSION) return;
        if (p.status()==3) sync();
        String key=p.status()==0?"text.abstool.waypoint.saved":"text.abstool.waypoint.error."+p.status();
        Minecraft client=Minecraft.getInstance();
        if (client.player!=null) client.player.sendSystemMessage(Component.translatable(key));
    }
    public static void error(WaypointPayloads.Error p) {
        if (p.version()!=ProtocolConstants.VERSION || !STATE.supported()) return;
        Minecraft client=Minecraft.getInstance();
        if (client.player!=null) client.player.sendSystemMessage(Component.translatable("text.abstool.waypoint.server_error",p.code()));
    }
    public static void categories(WaypointCategories.Snapshot p) {
        if (p.version()!=ProtocolConstants.VERSION || !STATE.supported()) return;
        if (p.status()==0 || p.status()==3) STATE.categories(p.revision(),p.names());
        if (p.status()!=0 && Minecraft.getInstance().player!=null)
            Minecraft.getInstance().player.sendSystemMessage(Component.translatable("text.abstool.waypoint.error."+p.status()));
    }
    public static boolean canRenameCategories() { return canManage() && STATE.categoriesSynced() && canSend.test(WaypointCategories.Request.TYPE.id()); }
    public static boolean renameCategory(int color, long revision, String name) {
        if (!canRenameCategories() || revision!=STATE.categoryRevision() || color<0 || color>15 || !WaypointCategories.validName(name)) return false;
        sender.accept(new WaypointCategories.Request(ProtocolConstants.VERSION,++nextRequestId,true,color,revision,name.strip())); return true;
    }
    public static boolean create(WaypointDraft draft) {
        if (!canManage() || !draft.valid() || !canSend.test(WaypointPayloads.MutationRequest.TYPE.id())) return false;
        sender.accept(new WaypointPayloads.MutationRequest(ProtocolConstants.VERSION,++nextRequestId,1,null,0,draft)); return true;
    }
    public static boolean update(SharedWaypoint old, WaypointDraft draft) {
        if (!canManage() || old==null || !draft.valid() || !canSend.test(WaypointPayloads.MutationRequest.TYPE.id())) return false;
        sender.accept(new WaypointPayloads.MutationRequest(ProtocolConstants.VERSION,++nextRequestId,2,old.id(),old.revision(),draft)); return true;
    }
    public static boolean delete(SharedWaypoint old) {
        if (!canManage() || old==null || !canSend.test(WaypointPayloads.MutationRequest.TYPE.id())) return false;
        sender.accept(new WaypointPayloads.MutationRequest(ProtocolConstants.VERSION,++nextRequestId,3,old.id(),old.revision(),
                new WaypointDraft("","",0,0,0,0,"",false))); return true;
    }
    public static boolean canManage() { return sender!=null && canSend!=null && STATE.supported() && STATE.synced()
            && STATE.canManage() && ConfigManager.get().serverWaypoints.enabled; }
}
