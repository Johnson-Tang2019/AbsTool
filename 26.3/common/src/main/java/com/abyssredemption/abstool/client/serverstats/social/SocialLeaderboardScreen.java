package com.abyssredemption.abstool.client.serverstats.social;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Optional entertainment boards in a separate responsive screen; existing statistics tabs remain intact. */
public final class SocialLeaderboardScreen extends Screen {
    private final Screen parent;
    private final List<Button> rows=new ArrayList<>(), filters=new ArrayList<>();
    private Button previous,next,up,down,refresh;
    private int board,page=1,offset;
    private SocialPayloads.Entry selected;
    public SocialLeaderboardScreen(Screen parent) { super(label("title")); this.parent=parent; }
    private static Component label(String key,Object... args) { return Component.translatable("abstool.social."+key,args); }
    private int area() { return Math.max(1,Math.min(560,width-16)); }
    private int left() { return Math.max(8,(width-area())/2); }
    private int rowCount() { return Math.max(0,(height-164)/24); }
    private void choose(int type) { board=type; page=1; offset=0; selected=null; SocialLeaderboardClient.request(board,page,false); }
    @Override protected void init() {
        rows.clear(); filters.clear(); int left=left(),area=area();
        int column=(area-4)/2;
        addRenderableWidget(Button.builder(label("pigeon"),b->choose(0)).bounds(left,28,column,20).build());
        addRenderableWidget(Button.builder(label("street"),b->choose(3)).bounds(left+column+4,28,column,20).build());
        for(int i=0;i<3;i++) {
            int type=i; int size=(area-8)/3;
            filters.add(addRenderableWidget(Button.builder(label("filter."+i),b->choose(type)).bounds(left+i*(size+4),52,size,18).build()));
        }
        int size=Math.max(1,(area-20)/6); int bottom=height-24;
        previous=addRenderableWidget(Button.builder(label("previous"),b->{if(page>1){page--;offset=0;SocialLeaderboardClient.request(board,page,false);}}).bounds(left,bottom,size,20).build());
        next=addRenderableWidget(Button.builder(label("next"),b->{var data=data();if(data!=null && page<data.totalPages()){page++;offset=0;SocialLeaderboardClient.request(board,page,false);}}).bounds(left+size+4,bottom,size,20).build());
        up=addRenderableWidget(Button.builder(label("up"),b->offset=Math.max(0,offset-1)).bounds(left+2*(size+4),bottom,size,20).build());
        down=addRenderableWidget(Button.builder(label("down"),b->offset++).bounds(left+3*(size+4),bottom,size,20).build());
        refresh=addRenderableWidget(Button.builder(label("refresh"),b->SocialLeaderboardClient.request(board,page,true)).bounds(left+4*(size+4),bottom,size,20).build());
        addRenderableWidget(Button.builder(label("back"),b->{if(selected!=null)selected=null;else onClose();}).bounds(left+5*(size+4),bottom,size,20).build());
        for(int i=0;i<rowCount();i++) {
            int index=i;
            rows.add(addRenderableWidget(Button.builder(Component.empty(),b->{var value=data();if(value!=null && offset+index<value.entries().size())selected=value.entries().get(offset+index);})
                    .bounds(left,94+i*24,area,22).build()));
        }
        SocialLeaderboardClient.request(board,page,false);
    }
    private SocialPayloads.Response data() { return SocialLeaderboardClient.state().page(board,page); }
    @Override public void onClose() { Minecraft.getInstance().gui.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float delta) {
        SocialLeaderboardClient.request(board,page,false);
        boolean supported=SocialLeaderboardClient.supported(); var data=data();
        for(int i=0;i<filters.size();i++) { filters.get(i).visible=board!=3 && selected==null; filters.get(i).active=supported && board!=i; }
        for(int i=0;i<rows.size();i++) {
            var button=rows.get(i); int index=offset+i;
            button.visible=selected==null && supported && data!=null && index<data.entries().size();
            if(button.visible) { var entry=data.entries().get(index); button.setMessage(row(entry)); button.setTooltip(Tooltip.create(label("details_hint"))); }
        }
        previous.active=selected==null && supported && page>1;
        next.active=selected==null && supported && data!=null && page<data.totalPages();
        up.active=selected==null && offset>0;
        down.active=selected==null && data!=null && offset+rows.size()<data.entries().size();
        refresh.active=supported && !SocialLeaderboardClient.state().pending(board,page);
        super.extractRenderState(graphics,mouseX,mouseY,delta);
        int left=left(); graphics.textRenderer().accept(left,10,title);
        if(!supported) text(graphics,label("unsupported"),left,76,height-72);
        else if(selected!=null) {
            var entry=selected; int y=54;
            for(var line:List.of(label("detail.player",entry.playerName()),label("detail.type",label("subtype."+entry.subtype()),entry.score()),
                    label("detail.play",format(entry.windowPlayTimeTicks()/1200.0)),label("detail.foot",format(entry.onFootDistanceCm()/100000.0)),
                    label("detail.actions",entry.observableActions(),format(rate(entry))),label("detail.days",entry.activeDays(),entry.shortVisits()),label("detail.last",date(entry.lastSeenAtEpochMillis())),label("metric_note")))
                y=text(graphics,line,left,y,height-72)+3;
        } else {
            String error=SocialLeaderboardClient.state().error(board,page);
            if(error!=null) text(graphics,label("error."+error),left,76,92);
            else if(data==null) text(graphics,label("loading"),left,76,92);
            else {
                text(graphics,label("page",page,data.totalPages(),data.totalEntries()),left,76,92);
                if(data.entries().isEmpty()) text(graphics,label("observing"),left,98,height-72);
                text(graphics,label("window",date(data.windowStartEpochMillis()),date(data.windowEndEpochMillis())),left,height-66,height-50);
                if(!data.complete()) refresh.setTooltip(Tooltip.create(label("observing"))); else refresh.setTooltip(null);
            }
        }
        text(graphics,label("disclaimer"),left,height-48,height-26);
    }
    private int text(GuiGraphicsExtractor graphics,Component line,int x,int y,int bottom) {
        for(var wrapped:font.split(line,area())) { if(y+9>bottom) break; graphics.textRenderer().accept(x,y,wrapped); y+=11; } return y;
    }
    private static Component row(SocialPayloads.Entry entry) {
        if(entry.subtype()==3) return label("row.street",entry.rank(),entry.playerName(),entry.score(),format(entry.onFootDistanceCm()/100000.0),format(rate(entry)));
        return label("row.pigeon",entry.rank(),entry.playerName(),label("subtype."+entry.subtype()),entry.score(),
                entry.subtype()==1?label("evidence.absent",entry.daysSinceLastSeen()):label("evidence.flash",entry.shortVisits()));
    }
    private static double rate(SocialPayloads.Entry entry) { return entry.observableActions()/Math.max(0.01,entry.windowPlayTimeTicks()/72000.0); }
    private static String format(double value) { return String.format(Locale.ROOT,"%.1f",value); }
    private static String date(long millis) { return java.time.format.DateTimeFormatter.ISO_LOCAL_DATE.withZone(java.time.ZoneId.systemDefault()).format(java.time.Instant.ofEpochMilli(millis)); }
}
