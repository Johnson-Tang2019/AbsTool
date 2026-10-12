package com.abyssredemption.abstool.fabric.compat.schematic;

import java.io.IOException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

/** Checks the Iris members used by our mixins before Mixin attempts to apply them. */
public final class IrisBytecodeContract {
    @FunctionalInterface
    public interface ResourceReader {
        byte[] read(String resource) throws IOException;
    }

    private IrisBytecodeContract() {}

    public static String rejection(ResourceReader resources) throws IOException {
        ClassNode pipeline = read(resources, "net/irisshaders/iris/pipeline/IrisRenderingPipeline.class");
        if (!method(pipeline, "beginLevelRendering", "()V", false)
                || !method(pipeline, "finalizeLevelRendering", "()V", false)) {
            return "Unsupported Iris level rendering hooks";
        }
        ClassNode state = read(resources, "net/irisshaders/iris/vertices/ImmediateState.class");
        if (!field(state, "isRenderingLevel", "Z") || !field(state, "bypass", "Z")
                || !field(state, "skipExtension", "Ljava/lang/ThreadLocal;")) {
            return "Unsupported Iris immediate rendering state";
        }
        ClassNode api = read(resources, "net/irisshaders/iris/api/v0/IrisApi.class");
        if (!method(api, "getInstance", "()Lnet/irisshaders/iris/api/v0/IrisApi;", true)
                || !method(api, "isShaderPackInUse", "()Z", false)
                || !method(api, "isRenderingShadowPass", "()Z", false)) {
            return "Unsupported Iris public API";
        }
        return "";
    }

    private static ClassNode read(ResourceReader resources, String path) throws IOException {
        ClassNode node = new ClassNode();
        new ClassReader(resources.read(path)).accept(node, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return node;
    }

    private static boolean method(ClassNode node, String name, String descriptor, boolean staticMethod) {
        return node.methods.stream().anyMatch(method -> method.name.equals(name) && method.desc.equals(descriptor)
                && (method.access & Opcodes.ACC_PUBLIC) != 0
                && ((method.access & Opcodes.ACC_STATIC) != 0) == staticMethod);
    }

    private static boolean field(ClassNode node, String name, String descriptor) {
        return node.fields.stream().anyMatch(field -> field.name.equals(name) && field.desc.equals(descriptor)
                && (field.access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)) == (Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC));
    }
}
