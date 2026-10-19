package com.abyssredemption.abstool.client.waypoint.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** Low-opacity category surface; text retains full contrast. */
public final class CategoryButton extends Button {
    public CategoryButton(Runnable action) { super(0,0,120,20,Component.empty(), b -> action.run(), DEFAULT_NARRATION); }
    @Override protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        boolean hover = active && (isMouseOver(mouseX,mouseY) || isFocused());
        graphics.fill(getX(),getY(),getX()+getWidth(),getY()+getHeight(),hover ? 0x48384050 : 0x24182028);
        graphics.outline(getX(),getY(),getWidth(),getHeight(),hover ? 0x809CA9B8 : 0x386E7A88);
        var font = Minecraft.getInstance().font;
        var label = Component.literal(font.plainSubstrByWidth(getMessage().getString(),Math.max(0,getWidth()-12)));
        graphics.textRenderer().accept(getX()+(getWidth()-font.width(label))/2,getY()+(getHeight()-8)/2,label);
    }
}
