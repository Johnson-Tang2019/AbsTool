package com.abyssredemption.abstool.verification;

import com.abyssredemption.abstool.client.mobmerge.OverlapGroups;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.phys.AABB;

public final class OverlapVerification {
    public static void run() throws Exception {
        var box=new AABB(-0.3,0,-0.3,0.3,1,0.3);
        check(OverlapGroups.overlaps(box,box.move(0.005,0,0)),"Near-total overlap accepted");
        check(!OverlapGroups.overlaps(box,box.move(0.1,0,0)),"Ordinary crowding is not merging");
        check(!OverlapGroups.overlaps(box,new AABB(-0.15,0,-0.15,0.15,0.5,0.15)),"Contained baby and adult must not merge");
        var entries=new ArrayList<OverlapGroups.Candidate>();
        for(int i=1;i<=1000;i++) entries.add(new OverlapGroups.Candidate(i,"cow",box));
        Collections.reverse(entries);
        var result=OverlapGroups.build(entries);
        check(result.size()==1000 && result.values().stream().allMatch(m -> m.count()==1000 && m.representative()==1),"Dense stack has one stable representative");
        entries.add(new OverlapGroups.Candidate(1001,"pig",box));
        check(!OverlapGroups.build(entries).containsKey(1001),"Different type/appearance stays separate");
        var chain=List.of(new OverlapGroups.Candidate(1,"cow",box),new OverlapGroups.Candidate(2,"cow",box.move(0.01,0,0)),new OverlapGroups.Candidate(3,"cow",box.move(0.02,0,0)));
        check(!OverlapGroups.build(chain).containsKey(3),"Near neighbors cannot form a transitive merge chain");
        var boundary=List.of(new OverlapGroups.Candidate(1,"cow",box.move(0.999,0,0)),new OverlapGroups.Candidate(2,"cow",box.move(1.001,0,0)));
        check(OverlapGroups.build(boundary).size()==2,"Spatial bucket boundaries must not split identical stacks");
        var directory=java.nio.file.Files.createTempDirectory("abstool-visibility-");
        try {
            com.abyssredemption.abstool.client.vault.config.ConfigManager.init(directory);
            var config=com.abyssredemption.abstool.client.vault.config.ConfigManager.get();
            config.serverWaypoints.hiddenColors.put("server-a",64); config.serverWaypoints.hiddenColors.put("server-b",1024);
            com.abyssredemption.abstool.client.vault.config.ConfigManager.save();
            com.abyssredemption.abstool.client.vault.config.ConfigManager.load();
            config=com.abyssredemption.abstool.client.vault.config.ConfigManager.get();
            check(config.serverWaypoints.hiddenColors.get("server-a")==64,"Hidden colors survive reload");
            config.serverWaypoints.hiddenColors.remove("server-a");
            com.abyssredemption.abstool.client.vault.config.ConfigManager.save();
            com.abyssredemption.abstool.client.vault.config.ConfigManager.load();
            config=com.abyssredemption.abstool.client.vault.config.ConfigManager.get();
            check(!config.serverWaypoints.hiddenColors.containsKey("server-a") && config.serverWaypoints.hiddenColors.get("server-b")==1024,
                    "Showing colors again must survive reload without changing another server");
        } finally {
            try(var files=java.nio.file.Files.list(directory)) { for(var file:files.toList()) java.nio.file.Files.delete(file); }
            java.nio.file.Files.delete(directory);
        }
        System.out.println("Overlap grouping passed: dense stacks, stable representatives, threshold, appearances, sizes, non-transitive groups and bucket boundaries.");
    }
    private static void check(boolean condition,String message) { if(!condition) throw new AssertionError(message); }
}
