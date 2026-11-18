package com.abyssredemption.abstool.client.serverstats.social;

import java.util.*;

/** Connection-scoped request tracking. Unsolicited, stale and cross-page replies never replace another page. */
public final class SocialLeaderboardState {
    public record Key(int board,int page) {}
    private record Pending(Key key,long at,long generation) {}
    private record Cached(SocialPayloads.Response value,long at) {}
    private final Map<Long,Pending> pending=new HashMap<>();
    private final Map<Key,Long> latest=new HashMap<>();
    private final Map<Key,Cached> pages=new LinkedHashMap<>();
    private final Map<Key,String> errors=new HashMap<>();
    private long generation;
    public void clear() { generation++; pending.clear(); latest.clear(); pages.clear(); errors.clear(); }
    public boolean expect(long id,int board,int page,long now) {
        if(pending.size()>=32 || board<0 || board>3 || page<1) return false;
        var key=new Key(board,page); pending.put(id,new Pending(key,now,generation)); latest.put(key,id); errors.remove(key); return true;
    }
    public boolean pending(int board,int page) { return pending.values().stream().anyMatch(p->p.key().equals(new Key(board,page))); }
    public SocialPayloads.Response page(int board,int page) { var cached=pages.get(new Key(board,page)); return cached==null?null:cached.value(); }
    public boolean fresh(int board,int page,long now) { var cached=pages.get(new Key(board,page)); return cached!=null && now-cached.at()<10000; }
    public String error(int board,int page) { return errors.get(new Key(board,page)); }
    public boolean accept(SocialPayloads.Response response,long now) {
        var request=pending.get(response.requestId());
        if(request==null || response.protocolVersion()!=2 || request.generation()!=generation
                || request.key().board()!=response.boardType() || request.key().page()!=response.page()) return false;
        pending.remove(response.requestId());
        if(!Objects.equals(latest.get(request.key()),response.requestId())) return false;
        if(pages.size()>=128 && !pages.containsKey(request.key())) pages.remove(pages.keySet().iterator().next());
        pages.put(request.key(),new Cached(response,now)); errors.remove(request.key()); return true;
    }
    public boolean error(long id,int version,String error) {
        if(version!=2) return false; var request=pending.remove(id); if(request==null) return false;
        if(request.generation()==generation && Objects.equals(latest.get(request.key()),id)) errors.put(request.key(),error);
        return true;
    }
    public void tick(long now) {
        var expired=pending.entrySet().stream().filter(e->now-e.getValue().at()>=6000).map(Map.Entry::getKey).toList();
        for(var id:expired) error(id,2,"timeout");
    }
}
