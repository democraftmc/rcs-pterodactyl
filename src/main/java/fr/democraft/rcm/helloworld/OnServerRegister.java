package fr.democraft.rcm.helloworld;

import group.aelysium.rustyconnector.common.events.EventListener;
import group.aelysium.rustyconnector.proxy.events.ServerRegisterEvent;

public class OnServerRegister {
    @EventListener
    public static void handler(ServerRegisterEvent event) {
        System.out.println(event.server.address() + " has registered to the family "+ event.family.displayName());
    }
}
