package com.abyssredemption.abstool.verification;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

public final class SchematicTestEntry implements FabricClientGameTest {
    public void runTest(ClientGameTestContext context) {
        if (!Boolean.getBoolean("abstool.schematicTests")) return;
        try {
            ((FabricClientGameTest) Class.forName("com.abyssredemption.abstool.verification.SchematicWorldTest")
                    .getConstructor().newInstance()).runTest(context);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Schematic test fixture unavailable", e);
        }
    }
}
