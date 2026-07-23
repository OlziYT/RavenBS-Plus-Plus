package xyz.ravenbs.utility;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateChecker {
    public static boolean isUpdateAvailable = false;
    public static String latestVersion = "";
    public static String downloadURL = "https://github.com/ByteFlipper-58/RavenBS-Fabric/releases";
    private static volatile boolean enabled = true;
    private static volatile boolean checked = false;
    private static String cachedEtag = null;
    private static long cachedLastModified = 0L;

    public static void checkForUpdates() {
        if (!enabled || checked) return;
        checked = true;
        new Thread(() -> {
            try {
                String currentVersion = FabricLoader.getInstance().getModContainer("ravenbs-fabric").get().getMetadata().getVersion().getFriendlyString();
                
                // GitHub API URL
                URL url = new URL("https://api.github.com/repos/ByteFlipper-58/RavenBS-Fabric/releases/latest");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "RavenBS-Fabric");
                connection.setRequestProperty("Accept", "application/vnd.github+json");
                if (cachedEtag != null) {
                    connection.setRequestProperty("If-None-Match", cachedEtag);
                }
                if (cachedLastModified > 0) {
                    connection.setIfModifiedSince(cachedLastModified);
                }
                connection.setConnectTimeout(4000);
                connection.setReadTimeout(4000);

                int status = connection.getResponseCode();
                if (status == HttpURLConnection.HTTP_NOT_MODIFIED) {
                    return; // Cached version still valid
                }
                if (status == HttpURLConnection.HTTP_OK) {
                    InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    latestVersion = json.get("tag_name").getAsString();
                    cachedEtag = connection.getHeaderField("ETag");
                    cachedLastModified = connection.getLastModified();

                    String cleanCurrent = currentVersion.replace("v", "");
                    String cleanLatest = latestVersion.replace("v", "");

                    if (isNewer(cleanCurrent, cleanLatest)) {
                        isUpdateAvailable = true;
                    }
                }
            } catch (Exception e) {
                xyz.ravenbs.RavenBSFabric.LOGGER.error("Update check failed", e);
                checked = false; // allow retry later if something went wrong
            }
        }).start();
    }

    private static boolean isNewer(String current, String latest) {
        try {
            String[] v1 = current.split("\\.");
            String[] v2 = latest.split("\\.");
            int length = Math.max(v1.length, v2.length);
            for (int i = 0; i < length; i++) {
                int num1 = i < v1.length ? Integer.parseInt(v1[i]) : 0;
                int num2 = i < v2.length ? Integer.parseInt(v2[i]) : 0;
                if (num1 < num2) return true;
                if (num1 > num2) return false;
            }
        } catch (Exception ignored) {}
        return false;
    }

    public static void onJoin() {
        if (!enabled || !isUpdateAvailable) {
            return;
        }
        if (isUpdateAvailable) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null) {
                String currentVersion = FabricLoader.getInstance().getModContainer("ravenbs-fabric").get().getMetadata().getVersion().getFriendlyString();
                
                // Message 1: GitHub Link ([RavenBS] New version available...)
                Text prefix = Text.translatable("raven.prefix");
                Text versionMsg = Text.translatable("raven.update.new_version", latestVersion, currentVersion);
                
                Text message = Text.empty().append(prefix).append(versionMsg);
                mc.player.sendMessage(message, false);

                // Message 2: Website Link (Also available at...)
                Text siteMsg = Text.translatable("raven.update.site_msg");
                Text siteLink = Text.literal("§bravenbs.xyz")
                        .styled(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://ravenbs.xyz"))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("raven.update.site_hover")))
                        );
                
                // No prefix for the second line
                Text siteMessage = Text.empty().append(siteMsg).append(siteLink);
                mc.player.sendMessage(siteMessage, false);
            }
        }
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        if (value) {
            checked = false; // allow re-check after re-enabling
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }
}
