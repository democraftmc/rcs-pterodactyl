package fr.democraft.rcs.pterodactyl;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

@Plugin(id = "rcs-pterodactyl", name = "Smart Pterodactyl Provider", version = "0.1.0-SNAPSHOT",
        url = "https://smart.democraft.fr", description = "NOT A VELOCITY PLUGIN", authors = {"Funasitien"})
public class NotAVelocityPlugin {

    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public NotAVelocityPlugin(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;

        logger.error("================================");
        logger.error(" SmartRCM cannot be run as a Bukkit/Spigot/Paper plugin!");
        logger.error(" Please use it as a RustyConnector module.");
        logger.error("================================");
    }
}