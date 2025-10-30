package fr.democraft.rcs.pterodactyl;

import group.aelysium.rustyconnector.shaded.group.aelysium.declarative_yaml.DeclarativeYAML;
import group.aelysium.rustyconnector.shaded.group.aelysium.declarative_yaml.annotations.Config;
import group.aelysium.rustyconnector.shaded.group.aelysium.declarative_yaml.annotations.Namespace;
import group.aelysium.rustyconnector.shaded.group.aelysium.declarative_yaml.annotations.Node;

@Namespace("rustyconnector-modules")
@Config("/smart/pterodactyl.yml")
public class MainConfig {
    @Node(value = 1, key = "PANEL_URL")
    public String PANEL_URL = "https://panel.examle.com";

    @Node(value = 2, key = "API_KEY")
    public String API_KEY = "your_api_key";

    @Node(value = 3, key = "DEFAULT_MEMORY")
    public static final int DEFAULT_MEMORY = 1024;
    @Node(value = 4, key = "DEFAULT_SWAP")// MB
    public static final int DEFAULT_SWAP = 0;
    @Node(value = 5, key = "DEFAULT_DISK")
    public static final int DEFAULT_DISK = 10240;
    @Node(value = 6, key = "DEFAULT_IO")
    public static final int DEFAULT_IO = 500;
    @Node(value = 7, key = "DEFAULT_CPU")
    public static final int DEFAULT_CPU = 0;
    @Node(value = 8, key = "DEFAULT_THREADS")
    public static final String DEFAULT_THREADS = null;
    @Node(value = 9, key = "DEFAULT_OOM_DISABLED")
    public static final boolean DEFAULT_OOM_DISABLED = false;
    @Node(value = 10, key = "DEFAULT_DATABASES")
    public static final int DEFAULT_DATABASES = 0;
    @Node(value = 11, key = "DEFAULT_ALLOCATIONS")
    public static final int DEFAULT_ALLOCATIONS = 1;
    @Node(value = 12, key = "DEFAULT_BACKUPS")
    public static final int DEFAULT_BACKUPS = 0;
    @Node(value = 13, key = "DEFAULT_EGG")
    public static final int DEFAULT_EGG = 1;
    @Node(value = 14, key = "DEFAULT_OWNER")
    public static final int DEFAULT_OWNER = 1;
    @Node(value = 15, key = "DEFAULT_ALLOCATION")
    public static final int DEFAULT_ALLOCATION = 1;
    @Node(value = 16, key = "DEFAULT_DOCKER_IMAGE")
    public static final String DEFAULT_DOCKER_IMAGE = "quay.io/pterodactyl/core:java-17";
    @Node(value = 17, key = "DEFAULT_STARTUP")
    public static final String DEFAULT_STARTUP = "java -Xms128M -Xmx${SERVER_MEMORY}M -jar server.jar";


    public static MainConfig New() {
        return DeclarativeYAML.From(MainConfig.class);
    }
}
