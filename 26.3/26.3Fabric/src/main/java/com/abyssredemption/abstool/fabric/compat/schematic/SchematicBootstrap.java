package com.abyssredemption.abstool.fabric.compat.schematic;

import com.abyssredemption.abstool.client.schematic.SchematicShaderStatus;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.network.chat.Component;

public final class SchematicBootstrap {
    public static void init() {
        if (SchematicMixinPlugin.supported) {
            SchematicShaderStatus.register(SchematicAdapter::status);
            ClientTickEvents.END_CLIENT_TICK.register(SchematicAdapter::tick);
        } else {
            SchematicShaderStatus.register(() -> new SchematicShaderStatus.Snapshot(
                    SchematicMixinPlugin.reason.startsWith("Missing") ? SchematicShaderStatus.State.MISSING_DEPENDENCY
                            : SchematicShaderStatus.State.UNSUPPORTED_VERSION, SchematicMixinPlugin.reason, 0));
        }
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> dispatcher.register(
                ClientCommands.literal("abstool").then(ClientCommands.literal("schematicshader")
                        .then(ClientCommands.literal("status").executes(context -> {
                            context.getSource().sendFeedback(Component.literal(SchematicShaderStatus.get().toString()));
                            return 1;
                        })))));
    }
}
