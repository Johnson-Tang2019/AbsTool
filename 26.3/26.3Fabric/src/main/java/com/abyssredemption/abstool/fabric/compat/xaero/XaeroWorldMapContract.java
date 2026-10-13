package com.abyssredemption.abstool.fabric.compat.xaero;

import java.io.IOException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

/** Inspect optional map interfaces without loading their classes before Mixin. */
public final class XaeroWorldMapContract {
    public static final String MENU_DESCRIPTOR = "(Lxaero/map/mods/gui/Waypoint;Lxaero/map/gui/IRightClickableElement;)Ljava/util/ArrayList;";
    public static final String MENU_METHOD = "getRightClickOptions" + MENU_DESCRIPTOR;
    @FunctionalInterface
    public interface ResourceReader { byte[] read(String resource) throws IOException; }
    private XaeroWorldMapContract() {}

    public static String rejection(ResourceReader resources) throws IOException {
        var reader = read(resources, "xaero/map/mods/gui/WaypointReader.class");
        if (!method(reader, "getRightClickOptions", MENU_DESCRIPTOR)) return "Missing waypoint context menu hook";
        var waypoint = read(resources, "xaero/map/mods/gui/Waypoint.class");
        if (!method(waypoint, "getOriginal", "()Ljava/lang/Object;")) return "Missing original waypoint accessor";
        var target = read(resources, "xaero/map/gui/IRightClickableElement.class");
        if ((target.access & Opcodes.ACC_INTERFACE) == 0) return "Changed context menu target type";
        var option = read(resources, "xaero/map/gui/dropdown/rightclick/RightClickOption.class");
        if ((option.access & Opcodes.ACC_FINAL) != 0
                || !method(option, "<init>", "(Ljava/lang/String;ILxaero/map/gui/IRightClickableElement;)V")
                || !method(option, "setActive", "(Z)Lxaero/map/gui/dropdown/rightclick/RightClickOption;")
                || !method(option, "onAction", "(Lnet/minecraft/client/gui/screens/Screen;)V", true)
                || option.methods.stream().anyMatch(m -> m.name.equals("onAction") && (m.access & Opcodes.ACC_FINAL) != 0)) {
            return "Changed context menu action interface";
        }
        var support = read(resources, "xaero/map/mods/SupportMods.class");
        if (support.fields.stream().noneMatch(f -> f.name.equals("xaeroMinimap")
                && f.desc.equals("Lxaero/map/mods/SupportXaeroMinimap;")
                && (f.access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)) == (Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC))) {
            return "Changed minimap bridge field";
        }
        var minimap = read(resources, "xaero/map/mods/SupportXaeroMinimap.class");
        if (minimap.fields.stream().noneMatch(f -> f.name.equals("rightClickWaypointWorld")
                && f.desc.equals("Lxaero/hud/minimap/world/MinimapWorld;") && (f.access & Opcodes.ACC_STATIC) == 0)) {
            return "Changed context menu waypoint world";
        }
        return "";
    }

    private static ClassNode read(ResourceReader resources, String path) throws IOException {
        var node = new ClassNode();
        new ClassReader(resources.read(path)).accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        if ((node.access & Opcodes.ACC_PUBLIC) == 0) throw new IOException("Inaccessible " + path);
        return node;
    }
    private static boolean method(ClassNode node, String name, String descriptor) {
        return method(node, name, descriptor, false);
    }
    private static boolean method(ClassNode node, String name, String descriptor, boolean allowAbstract) {
        return node.methods.stream().anyMatch(m -> m.name.equals(name) && m.desc.equals(descriptor)
                && (m.access & Opcodes.ACC_PUBLIC) != 0
                && (m.access & (Opcodes.ACC_STATIC | Opcodes.ACC_NATIVE | (allowAbstract ? 0 : Opcodes.ACC_ABSTRACT))) == 0);
    }
}
