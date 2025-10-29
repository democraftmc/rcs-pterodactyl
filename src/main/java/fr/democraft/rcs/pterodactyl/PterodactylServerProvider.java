package fr.democraft.rcs.pterodactyl;

import group.aelysium.rustyconnector.common.events.EventManager;
import group.aelysium.rustyconnector.common.modules.ExternalModuleBuilder;
import group.aelysium.rustyconnector.common.modules.Module;
import group.aelysium.rustyconnector.proxy.ProxyKernel;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class PterodactylServerProvider implements Module {
    static final String PANEL_API_URL = "https://your-panel.example.com"; // Replace with actual URL
    static final String API_KEY = "your-api-key"; // Replace with actual API key
    static final String ID = "pterodactyl";

    @Override
    public void close() throws Exception {
    }

    @Override
    public @Nullable Component details() {
        return Component.text("Pterodactyl Smart Connector");
    }

    public static class Builder extends ExternalModuleBuilder<PterodactylServerProvider> {
        public void bind(@NotNull ProxyKernel kernel, @NotNull PterodactylServerProvider instance) {
            System.out.println("Pterodactyl Smart Provider is registered!");
            kernel.<EventManager>fetchModule("EventManager").onStart(m -> {
                m.listen(ServerCreator.class);
            });
        }
        
        @NotNull
        @Override
        public PterodactylServerProvider onStart(@NotNull Context context) throws Exception {
            return new PterodactylServerProvider();
        }
    }
}