package fr.democraft.rcs.pterodactyl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.democraft.rcm.smart.events.DeletePhysicalServer;
import group.aelysium.rustyconnector.common.events.EventListener;
import fr.democraft.rcm.smart.events.CreatePhysicalServer;
import group.aelysium.rustyconnector.proxy.family.Family;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static fr.democraft.rcs.pterodactyl.PterodactylServerProvider.PANEL_URL;
import static fr.democraft.rcs.pterodactyl.PterodactylServerProvider.API_KEY;
import static fr.democraft.rcs.pterodactyl.PterodactylServerProvider.ID;

public class ServerCreator {
    private static final Gson gson = new Gson();

    @EventListener
    public static void createHandler(CreatePhysicalServer e) {
        // This line is due to my trash code; You only od things when you are explicitly asked.
        if (e.providerId != ID) return;
        
        System.out.println(e.family.displayName() + " needs a new server, Pterodactyl will create it.");
        int serverId = createServer(e.family);
    }

    @EventListener
    public static void deleteHandler(DeletePhysicalServer e) {
        // This line is due to my trash code; You only od things when you are explicitly asked.
        if (e.providerId != ID) return;

        System.out.println(e.family.displayName() + " needs a new server, Pterodactyl will create it.");
        //deleteServer()
    }

    public static class Limits {
        public int memory;
        public int swap;
        public int disk;
        public int io;
        public int cpu;
        public String threads;
        public boolean oom_disabled;

        public Limits(int memory, int swap, int disk, int io, int cpu) {
            this.memory = memory;
            this.swap = swap;
            this.disk = disk;
            this.io = io;
            this.cpu = cpu;
        }
    }

    public static class FeatureLimits {
        public int databases;
        public int allocations;
        public int backups;

        public FeatureLimits(int databases, int allocations, int backups) {
            this.databases = databases;
            this.allocations = allocations;
            this.backups = backups;
        }
    }

    public static class Allocation {
        public int defaultAllocation;
        public int[] additional;

        public Allocation(int defaultAllocation, int[] additional) {
            this.defaultAllocation = defaultAllocation;
            this.additional = additional;
        }
    }

    public static int createPterodactylServer(String name, int user, int egg, Limits limits, FeatureLimits featureLimits, Allocation allocation, String dockerImage, String startup, Map<String, String> environment, JsonObject deploy) throws IOException, InterruptedException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("name", name);
        requestBody.addProperty("user", user);
        requestBody.addProperty("egg", egg);

        if (dockerImage != null) requestBody.addProperty("docker_image", dockerImage);
        if (startup != null) requestBody.addProperty("startup", startup);
        if (environment != null) requestBody.add("environment", gson.toJsonTree(environment));
        if (deploy != null) requestBody.add("deploy", deploy);

        // Limits
        JsonObject limitsObj = new JsonObject();
        limitsObj.addProperty("memory", limits.memory);
        limitsObj.addProperty("swap", limits.swap);
        limitsObj.addProperty("disk", limits.disk);
        limitsObj.addProperty("io", limits.io);
        limitsObj.addProperty("cpu", limits.cpu);
        if (limits.threads != null) limitsObj.addProperty("threads", limits.threads);
        limitsObj.addProperty("oom_disabled", limits.oom_disabled);
        requestBody.add("limits", limitsObj);

        // Feature Limits
        JsonObject featureLimitsObj = new JsonObject();
        featureLimitsObj.addProperty("databases", featureLimits.databases);
        featureLimitsObj.addProperty("allocations", featureLimits.allocations);
        featureLimitsObj.addProperty("backups", featureLimits.backups);
        requestBody.add("feature_limits", featureLimitsObj);

        // Allocation
        JsonObject allocationObj = new JsonObject();
        allocationObj.addProperty("default", allocation.defaultAllocation);
        if (allocation.additional != null && allocation.additional.length > 0) {
            allocationObj.add("additional", gson.toJsonTree(allocation.additional));
        }
        requestBody.add("allocation", allocationObj);

        String jsonBody = gson.toJson(requestBody);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PANEL_URL + "/api/application/servers"))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) {
            JsonObject body = gson.fromJson(response.body(), JsonObject.class);
            JsonObject attributes = body.getAsJsonObject("attributes");
            int serverId = attributes.get("id").getAsInt();

            System.out.println("Server created successfully with ID: " + serverId);
            return serverId;
        } else {
            System.err.println("Failed to create server: " + response.statusCode() + " " + response.body());
            return 0;
        }
    }

    public static void deletePterodactylServer(int serverId) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PANEL_URL + "/api/application/servers/" + serverId))
                .header("Authorization", "Bearer " + API_KEY)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 204) {
            System.out.println("Server deleted successfully.");
        } else {
            System.err.println("Failed to delete server: " + response.statusCode() + " " + response.body());
        }
    }

    private static int createServer(Family family) {
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
            Limits limits = new Limits(memory, swap, disk, io, cpu);
            limits.threads = MainConfig.DEFAULT_THREADS;
            limits.oom_disabled = MainConfig.DEFAULT_OOM_DISABLED;

            // FeatureLimits (non-critical things default to 0)
            FeatureLimits featureLimits = new FeatureLimits(databases, allocations, backups);

            // Allocation: default + any additional (none by default)
            Allocation allocation = new Allocation(defaultAllocation, new int[0]);

            // Environment variables: you can fetch these from metadata (strings) or keep defaults here
            Optional<Map<String, String>> environment = family.fetchMetadata("smart.pterodactyl.env");
            Map<String, String> envMap = environment.orElse(new HashMap<>());

            // Docker image and startup: prefer config / metadata if available (assuming string metadata method)
            String dockerImage = MainConfig.DEFAULT_DOCKER_IMAGE;
            String startup = MainConfig.DEFAULT_STARTUP;
            Optional<String> metaDocker = family.fetchMetadata("smart.pterodactyl.docker_image");
            dockerImage = metaDocker.orElse(MainConfig.DEFAULT_DOCKER_IMAGE);
            Optional<String> metaStartup = family.fetchMetadata("smart.pterodactyl.startup");
            startup = metaStartup.orElse(MainConfig.DEFAULT_STARTUP);

            // Server name: use family displayName + random suffix or explicit metadata name
            String serverName = family.displayName() + "-" + UUID.randomUUID().toString().substring(0, 6);

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

    private static int unwrap(Optional<Integer> opt, int fallback) {
        return opt != null && opt.isPresent() ? opt.get() : fallback;
    }
}