package com.abyssredemption.abstool.client.vault.config;

import java.util.List;
import java.util.function.Function;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;

/** Loader-owned optional settings; each screen gets its own pending edits and save action. */
public final class QuickSettings {
    public record Session(List<AbstractConfigListEntry<?>> entries, Runnable save) {}
    public static final Session EMPTY = new Session(List.of(), () -> {});
    public static Function<ConfigEntryBuilder, Session> provider = entries -> EMPTY;
    private QuickSettings() {}
}
