package fr.democraft.rcs.pterodactyl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import group.aelysium.rustyconnector.common.events.EventListener;
import fr.democraft.rcm.smart.events.CreatePhysicalServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static fr.democraft.rcs.pterodactyl.PterodactylServerProvider.PANEL_API_URL;
import static fr.democraft.rcs.pterodactyl.PterodactylServerProvider.API_KEY;
import static fr.democraft.rcs.pterodactyl.PterodactylServerProvider.ID;

public class ServerCreator {
    private static final Gson gson = new Gson();

    @EventListener
    public static void handler(CreatePhysicalServer event) {
        // This line is due to my trash code; You only od things when you are explicitly asked.
        if (event.providerId != ID) return;

        System.out.println(event.family.displayName() + " needs a new server, Pterodactyl will create it.");
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

    public static void createServer(String name, int user, int egg, Limits limits, FeatureLimits featureLimits, Allocation allocation, String dockerImage, String startup, Map<String, String> environment, JsonObject deploy) throws IOException, InterruptedException {
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
                .uri(URI.create(PANEL_API_URL + "/api/application/servers"))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) {
            System.out.println("Server created successfully: " + response.body());
        } else {
            System.err.println("Failed to create server: " + response.statusCode() + " " + response.body());
        }
    }

    public static void deleteServer(int serverId) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(PANEL_API_URL + "/api/application/servers/" + serverId))
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
}