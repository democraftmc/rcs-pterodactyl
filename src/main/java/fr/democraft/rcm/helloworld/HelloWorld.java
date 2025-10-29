package fr.democraft.rcm.helloworld;

import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.events.EventManager;
import group.aelysium.rustyconnector.common.modules.ExternalModuleBuilder;
import group.aelysium.rustyconnector.common.modules.Module;
import group.aelysium.rustyconnector.proxy.ProxyKernel;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class HelloWorld implements Module {
    @Override
    public void close() throws Exception {
        // Function to stop your module
        // Closes this resource, relinquishing any underlying resources.
    }

    @Override
    public @Nullable Component details() {
        // Returns a Component which describes the internal details of this module. If there's no details to show, can just return null.
        return Component.text("HelloWorld Module v1.0.0 ~ A simple example module for RustyConnector.");
    }

    public static class Builder extends ExternalModuleBuilder<HelloWorld> {
        public void bind(@NotNull ProxyKernel kernel, @NotNull HelloWorld instance) {
            // Runs after onStart(Context) successfully returns an instance and is registered into the RustyConnector kernel for the first time.
            // This method will only be run when your module is first registered to the kernel, or when the kernel is restarted.
            // It should be used to specifically link into kernel resources on a one-off basis.
            System.out.println("Hello World from RustyConnector Module!");
            // Example usages would be registering Lang nodes or adding events to the EventListener.
            // How to add events? Like this (you're welcome):
            kernel.<EventManager>fetchModule("EventManager").onStart(m -> {
                m.listen(OnServerRegister.class);
            });
        }
        
        @NotNull
        @Override
        public HelloWorld onStart(@NotNull Context context) throws Exception {
            // Runs when the RustyConnector kernel is ready to load your module.
            // This method should only be used to configure and start your module, you shouldn't interact with the RustyConnector kernel here.
            return new HelloWorld();
        }
    }
}