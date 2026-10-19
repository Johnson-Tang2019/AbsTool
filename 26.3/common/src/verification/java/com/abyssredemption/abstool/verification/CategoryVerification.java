package com.abyssredemption.abstool.verification;

import com.abyssredemption.abstool.client.vault.config.ConfigManager;
import com.abyssredemption.abstool.client.waypoint.SharedWaypointClient;
import com.abyssredemption.abstool.client.waypoint.network.WaypointCategories;
import com.abyssredemption.abstool.client.waypoint.gui.ServerWaypointIcon;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public final class CategoryVerification {
    public static void run() throws Exception {
        var state=SharedWaypointClient.state(); state.hello(true); state.snapshot(1,List.of());
        var sent=new java.util.ArrayList<net.minecraft.network.protocol.common.custom.CustomPacketPayload>();
        SharedWaypointClient.install(sent::add,ignored -> true);
        ConfigManager.get().serverWaypoints.enabled=true;
        check(!SharedWaypointClient.canRenameCategories(),"Older servers must not enable category mutations");
        check(state.categories(2,Map.of(6,"Buildings")),"Categories should synchronize");
        check(!SharedWaypointClient.renameCategory(6,2,"Other") && sent.isEmpty(),"Viewer cannot rename categories");
        state.permissions(true);
        check(!SharedWaypointClient.renameCategory(6,1,"Other"),"Stale editor must not overwrite newer names");
        check(SharedWaypointClient.renameCategory(6,2,"Homes") && sent.size()==1,"Administrator can request rename");
        check(state.categoryName(6).equals("Buildings"),"Rename waits for authoritative server response");
        check(!state.categories(1,Map.of()),"Older snapshots must not roll back categories");
        check(!state.categories(3,Map.of(16,"Invalid")),"Invalid colors rejected");
        check(!SharedWaypointClient.renameCategory(6,2,"Bad\nName") && !SharedWaypointClient.renameCategory(6,2,"x".repeat(33)),"Invalid names rejected");
        state.permissions(false);
        check(!SharedWaypointClient.renameCategory(6,2,"Homes"),"Permission loss blocks category writes");
        state.clear(); check(state.categoryName(6).isEmpty() && !state.categoriesSynced(),"Disconnect clears server names");
        SharedWaypointClient.install(null,null);
        wireFixtures(); assets();
        System.out.println("Categories passed: old-server fallback, admin/viewer/revoked permissions, conflicts, wire fixtures, disconnect isolation and icon/background assets.");
    }
    private static void wireFixtures() {
        var b=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
        try {
            b.writeVarInt(2); b.writeVarLong(71); b.writeBoolean(true); b.writeVarInt(6); b.writeVarLong(3); b.writeUtf("Homes",32);
            var request=WaypointCategories.Request.CODEC.decode(b);
            check(request.equals(new WaypointCategories.Request(2,71,true,6,3,"Homes")) && !b.isReadable(),"Request wire order");
            WaypointCategories.Request.CODEC.encode(b,request);
            check(b.readVarInt()==2 && b.readVarLong()==71 && b.readBoolean() && b.readVarInt()==6 && b.readVarLong()==3 && b.readUtf(32).equals("Homes") && !b.isReadable(),"Encoded request matches server fixture");
            b.writeVarInt(2); b.writeVarLong(71); b.writeVarInt(0); b.writeVarLong(4); b.writeVarInt(1); b.writeVarInt(6); b.writeUtf("Homes",32);
            var snapshot=WaypointCategories.Snapshot.CODEC.decode(b);
            check(snapshot.revision()==4 && snapshot.names().equals(Map.of(6,"Homes")) && !b.isReadable(),"Server snapshot decoded");
            b.writeVarInt(2); b.writeVarLong(0); b.writeVarInt(0); b.writeVarLong(4); b.writeVarInt(2);
            b.writeVarInt(6); b.writeUtf("One",32); b.writeVarInt(6); b.writeUtf("Two",32);
            boolean rejected=false;
            try { WaypointCategories.Snapshot.CODEC.decode(b); } catch(IllegalArgumentException expected) { rejected=true; }
            check(rejected,"Duplicate colors must be rejected");
        } finally { b.release(); }
    }
    private static ClassNode resource(String name) throws Exception {
        var node=new ClassNode();
        try(var in=CategoryVerification.class.getClassLoader().getResourceAsStream(name)) { new ClassReader(in).accept(node,0); }
        return node;
    }
    private static void assets() throws Exception {
        var screen=resource("net/minecraft/client/gui/screens/Screen.class");
        check(screen.methods.stream().anyMatch(m -> m.name.equals("extractMenuBackground") && m.desc.equals("(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V")),"Background hook exists");
        check(screen.methods.stream().anyMatch(m -> m.name.equals("extractBlurredBackground")),"Blur hook exists");
        var font=resource("net/minecraft/client/gui/font/FontManager.class");
        check(font.methods.stream().anyMatch(m -> m.name.equals("loadResourceStack")),"Font resource-stack loader exists");
        try(var input=CategoryVerification.class.getResourceAsStream("/assets/abstool/textures/font/server.png")) {
            var image=ImageIO.read(input); check(image!=null && image.getWidth()==9 && image.getHeight()==8,"Server glyph decodes");
            check((image.getRGB(8,0)>>>24)==0 && (image.getRGB(0,0)>>>24)==255,"Glyph background transparent");
        }
        try(var input=CategoryVerification.class.getResourceAsStream("/assets/minecraft/font/default.json")) {
            var definition=com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(input,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
            var glyph=definition.getAsJsonArray("providers").get(0).getAsJsonObject();
            check(glyph.getAsJsonArray("chars").get(0).getAsString().equals(ServerWaypointIcon.PREFIX.strip()),"Font maps the server prefix codepoint");
        }
        check(ServerWaypointIcon.displayName("Home").equals(ServerWaypointIcon.PREFIX+"Home"),"Display prefix preserves name");
    }
    private static void check(boolean condition,String message) { if(!condition) throw new AssertionError(message); }
}
