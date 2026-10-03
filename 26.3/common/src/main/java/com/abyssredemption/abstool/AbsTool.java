package com.abyssredemption.abstool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Shared initialization used by both loaders. */
public final class AbsTool {
    public static final String MOD_ID = "abstool";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private AbsTool() {
    }

    public static void init() {
        LOGGER.info("Initializing Ab's Tool");
    }
}
