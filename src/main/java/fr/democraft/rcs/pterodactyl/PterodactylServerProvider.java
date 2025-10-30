package fr.democraft.rcs.pterodactyl;

import group.aelysium.rustyconnector.common.events.EventManager;
import group.aelysium.rustyconnector.common.modules.ExternalModuleBuilder;
import group.aelysium.rustyconnector.common.modules.Module;
import group.aelysium.rustyconnector.proxy.ProxyKernel;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class PterodactylServerProvider implements Module {
    static String PANEL_URL;
    static String API_KEY;
    public static final String ID = "pterodactyl";

    @Override
    public void close() throws Exception {
    }

    @Override
    public @Nullable Component details() {
        return Component.text("Pterodactyl Smart Connector");
    }

    public static class Builder extends ExternalModuleBuilder<PterodactylServerProvider> {
        public void bind(@NotNull ProxyKernel kernel, @NotNull PterodactylServerProvider instance) {
            MainConfig config = MainConfig.New();
            PANEL_URL = System.getenv("PTERODACTYL_PANEL_URL");
            if (PANEL_URL == null || PANEL_URL.isEmpty()) {
                PANEL_URL = config.PANEL_URL;
            }
            API_KEY = System.getenv("PTERODACTYL_API_KEY");
            if (API_KEY == null || API_KEY.isEmpty()) {
                API_KEY = config.API_KEY;
            }
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