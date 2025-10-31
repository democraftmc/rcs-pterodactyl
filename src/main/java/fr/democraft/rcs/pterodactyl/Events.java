package fr.democraft.rcs.pterodactyl;


import fr.democraft.rcm.smart.events.DeletePhysicalServer;
import fr.democraft.rcm.smart.events.CreatePhysicalServer;
import fr.democraft.rcs.pterodactyl.utils.PterodactylAPI;
import group.aelysium.rustyconnector.common.events.EventListener;
import group.aelysium.rustyconnector.proxy.events.ServerLeaveEvent;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import static fr.democraft.rcs.pterodactyl.PterodactylServerProvider.ID;
import static fr.democraft.rcs.pterodactyl.utils.MetadataHelper.getIDFromFamily;
import static fr.democraft.rcs.pterodactyl.utils.MetadataHelper.readAndComputeMetadata;

public class Events {
    private static final HashMap<String, Integer> serverCache = new HashMap<>();
    private static final HashMap<String, Integer> serverDeletitionWaitlist = new HashMap<>();

    @EventListener
    public static void createHandler(CreatePhysicalServer e) {
        // This line is due to my trash code; You only od things when you are explicitly asked.
        if (e.providerId != ID) return;
        
        System.out.println(e.family.displayName() + " needs a new server, Pterodactyl will create it.");
        String serverId = getIDFromFamily(e.family);
        int pterodactylId = readAndComputeMetadata(serverId, e.family);
        serverCache.put(serverId, pterodactylId);
    }

    @EventListener
    public static void deleteHandler(DeletePhysicalServer e) {
        // This line is due to my trash code; You only od things when you are explicitly asked.
        if (!Objects.equals(e.providerId, ID)) return;
        serverDeletitionWaitlist.compute(e.family.id(), (k, waitlist) -> (waitlist == null) ? 1 : waitlist + 1);
    }
    
    @EventListener
    public static void playerLeaveHandler(ServerLeaveEvent e) throws IOException, InterruptedException {
        if (serverDeletitionWaitlist.getOrDefault(e.server.family().get().id(), 0) > 0 && e.server.players() == 0) {
            String serverId = e.server.id();
            if (serverCache.containsKey(serverId)) {
                int pterodactylId = serverCache.get(serverId);
                System.out.println(e.server.id() + " server is no longer used, Pterodactyl will delete it.");
                PterodactylAPI.deletePterodactylServer(pterodactylId);
                serverCache.remove(serverId);
                serverDeletitionWaitlist.computeIfPresent(e.server.family().get().id(), (k, waitlist) -> waitlist - 1);
            }
        }
    }

}