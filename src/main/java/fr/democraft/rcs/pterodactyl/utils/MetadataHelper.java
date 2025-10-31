package fr.democraft.rcs.pterodactyl.utils;

import fr.democraft.rcs.pterodactyl.PterodactylServerProvider;
import fr.democraft.rcs.pterodactyl.configs.MainConfig;
import group.aelysium.rustyconnector.proxy.family.Family;

import java.io.IOException;
import java.util.*;

import static fr.democraft.rcs.pterodactyl.utils.PterodactylAPI.createPterodactylServer;

public class MetadataHelper {
    public static int readAndComputeMetadata(String serverName, Family family) {
        try {
            // --- read numeric metadata (these calls depend on your family's fetchMetadata returning Optional<Integer>) ---
            Optional<Integer> metaMemory = family.fetchMetadata("smart.memory");
            Optional<Integer> metaDisk = family.fetchMetadata("smart.pterodactyl.disk");
            Optional<Integer> metaCpu = family.fetchMetadata("smart.pterodactyl.cpu");
            Optional<Integer> metaSwap = family.fetchMetadata("smart.pterodactyl.swap");
            Optional<Integer> metaIo = family.fetchMetadata("smart.pterodactyl.io");
            Optional<Integer> metaDatabases = family.fetchMetadata("smart.pterodactyl.databases");
            Optional<Integer> metaAllocations = family.fetchMetadata("smart.pterodactyl.allocations");
            Optional<Integer> metaBackups = family.fetchMetadata("smart.pterodactyl.backups");
            Optional<Integer> metaEgg = family.fetchMetadata("smart.pterodactyl.egg");
            Optional<Integer> metaOwner = family.fetchMetadata("smart.pterodactyl.user");
            Optional<Integer> metaDefaultAlloc = family.fetchMetadata("smart.pterodactyl.allocation_default");
            // If your family metadata stores arrays or strings for additional allocations, adapt here.

            // Helper to unwrap optionals with fallback
            int memory = unwrap(metaMemory, MainConfig.DEFAULT_MEMORY);
            int disk = unwrap(metaDisk, MainConfig.DEFAULT_DISK);
            int cpu = unwrap(metaCpu, MainConfig.DEFAULT_CPU);
            int swap = unwrap(metaSwap, MainConfig.DEFAULT_SWAP);
            int io = unwrap(metaIo, MainConfig.DEFAULT_IO);

            int databases = unwrap(metaDatabases, MainConfig.DEFAULT_DATABASES);
            int allocations = unwrap(metaAllocations, MainConfig.DEFAULT_ALLOCATIONS);
            int backups = unwrap(metaBackups, MainConfig.DEFAULT_BACKUPS);

            int egg = unwrap(metaEgg, MainConfig.DEFAULT_EGG);
            int owner = unwrap(metaOwner, MainConfig.DEFAULT_OWNER);
            int defaultAllocation = unwrap(metaDefaultAlloc, MainConfig.DEFAULT_ALLOCATION);

            // Build the Limits object
            PterodactylAPI.Limits limits = new PterodactylAPI.Limits(memory, swap, disk, io, cpu);
            limits.threads = MainConfig.DEFAULT_THREADS;
            limits.oom_disabled = MainConfig.DEFAULT_OOM_DISABLED;

            // FeatureLimits (non-critical things default to 0)
            PterodactylAPI.FeatureLimits featureLimits = new PterodactylAPI.FeatureLimits(databases, allocations, backups);

            // Allocation: default + any additional (none by default)
            PterodactylAPI.Allocation allocation = new PterodactylAPI.Allocation(defaultAllocation, new int[0]);

            // Environment variables: you can fetch these from metadata (strings) or keep defaults here
            Optional<Map<String, String>> environment = family.fetchMetadata("smart.pterodactyl.env");
            Map<String, String> envMap = environment.orElse(new HashMap<>());
            envMap.put("RUSTYCONNECTOR_SERVERID", "");

            // Docker image and startup: prefer config / metadata if available (assuming string metadata method)
            String dockerImage = MainConfig.DEFAULT_DOCKER_IMAGE;
            String startup = MainConfig.DEFAULT_STARTUP;
            Optional<String> metaDocker = family.fetchMetadata("smart.pterodactyl.docker_image");
            dockerImage = metaDocker.orElse(MainConfig.DEFAULT_DOCKER_IMAGE);
            Optional<String> metaStartup = family.fetchMetadata("smart.pterodactyl.startup");
            startup = metaStartup.orElse(MainConfig.DEFAULT_STARTUP);

            // Finally call the createServer method (this may throw)
            return createPterodactylServer(serverName, owner, egg, limits, featureLimits, allocation, dockerImage, startup, envMap, null);

        } catch (IOException | InterruptedException ex) {
            System.err.println("Error creating pterodactyl server: " + ex.getMessage());
            ex.printStackTrace();
            return 0;
        } catch (Exception ex) {
            System.err.println("Unexpected error while preparing server creation: " + ex.getMessage());
            ex.printStackTrace();
            return 0;
        }
    }

    public static String getIDFromFamily(Family family) {
        if (Objects.equals(PterodactylServerProvider.config.ID_PREFIX, "NANOID")) {
            return family.id() + (family.size() + 1);
        } else if (Objects.equals(PterodactylServerProvider.config.ID_PREFIX, "UUID")) {
            return UUID.randomUUID().toString();
        } else {
            return family.id() + UUID.randomUUID().toString();
        }
    }

    private static int unwrap(Optional<Integer> opt, int fallback) {
        return opt != null && opt.isPresent() ? opt.get() : fallback;
    }
}
