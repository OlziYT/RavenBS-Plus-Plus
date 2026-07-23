package xyz.ravenbs.utility;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import xyz.ravenbs.config.ConfigManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Stores the global friend list independently from module profiles.
 * UUIDs are preferred when available so a renamed player remains a friend.
 */
public final class FriendManager {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("RavenBS/FriendManager");
    private static final int FORMAT_VERSION = 2;

    private static final List<Friend> friends = new ArrayList<>();
    private static final List<Runnable> listeners = new ArrayList<>();
    private static boolean initialized;

    private FriendManager() {
    }

    public enum ChangeResult {
        ADDED,
        UPDATED,
        ALREADY_EXISTS,
        REMOVED,
        NOT_FOUND,
        INVALID_NAME,
        INVALID_ALIAS
    }

    public static final class Friend {
        private final String name;
        private final UUID uuid;
        private final String alias;
        private final long addedAt;

        private Friend(String name, UUID uuid, String alias, long addedAt) {
            this.name = name;
            this.uuid = uuid;
            this.alias = alias;
            this.addedAt = addedAt;
        }

        public String getName() {
            return name;
        }

        public UUID getUuid() {
            return uuid;
        }

        public String getAlias() {
            return alias;
        }

        public long getAddedAt() {
            return addedAt;
        }

        public String getDisplayName() {
            return alias.isEmpty() ? name : alias + " (" + name + ")";
        }
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;
        load();
    }

    public static synchronized void addListener(Runnable runnable) {
        if (runnable != null) {
            listeners.add(runnable);
        }
    }

    public static synchronized List<Friend> getFriends() {
        List<Friend> snapshot = new ArrayList<>(friends);
        snapshot.sort(Comparator.comparing(Friend::getDisplayName, String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(snapshot);
    }

    public static synchronized int getOnlineCount() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return 0;
        }

        int online = 0;
        for (Friend friend : friends) {
            if (findOnlinePlayer(friend) != null) {
                online++;
            }
        }
        return online;
    }

    public static synchronized List<Friend> getOnlineFriends() {
        List<Friend> online = new ArrayList<>();
        for (Friend friend : friends) {
            if (findOnlinePlayer(friend) != null) {
                online.add(friend);
            }
        }
        online.sort(Comparator.comparing(Friend::getDisplayName, String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(online);
    }

    public static synchronized ChangeResult addFriend(String rawName, String rawAlias) {
        String name = normalizeName(rawName);
        if (name.isEmpty()) {
            return ChangeResult.INVALID_NAME;
        }

        String alias = normalizeAlias(rawAlias);
        if (rawAlias != null && !rawAlias.isBlank() && alias.isEmpty()) {
            return ChangeResult.INVALID_ALIAS;
        }

        PlayerEntity onlinePlayer = findOnlinePlayer(name);
        UUID uuid = onlinePlayer == null ? null : onlinePlayer.getUuid();
        Friend existing = findFriend(name, uuid);
        if (existing != null) {
            UUID updatedUuid = uuid != null ? uuid : existing.uuid;
            boolean identityChanged = !existing.name.equals(name)
                    || (updatedUuid != null && !updatedUuid.equals(existing.uuid));
            if (alias.equals(existing.alias) && !identityChanged) {
                return ChangeResult.ALREADY_EXISTS;
            }

            replaceFriend(existing, new Friend(name, updatedUuid, alias, existing.addedAt));
            save();
            notifyListeners();
            return ChangeResult.UPDATED;
        }

        friends.add(new Friend(name, uuid, alias, System.currentTimeMillis()));
        save();
        notifyListeners();
        return ChangeResult.ADDED;
    }

    public static synchronized ChangeResult toggleFriend(PlayerEntity player) {
        if (player == null) {
            return ChangeResult.INVALID_NAME;
        }

        Friend existing = findFriend(player.getName().getString(), player.getUuid());
        if (existing != null) {
            friends.remove(existing);
            save();
            notifyListeners();
            return ChangeResult.REMOVED;
        }

        friends.add(new Friend(player.getName().getString(), player.getUuid(), "", System.currentTimeMillis()));
        save();
        notifyListeners();
        return ChangeResult.ADDED;
    }

    public static synchronized ChangeResult removeFriend(String query) {
        Friend friend = findFriend(query, null);
        if (friend == null) {
            return ChangeResult.NOT_FOUND;
        }

        friends.remove(friend);
        save();
        notifyListeners();
        return ChangeResult.REMOVED;
    }

    public static synchronized ChangeResult setAlias(String query, String rawAlias) {
        Friend friend = findFriend(query, null);
        if (friend == null) {
            return ChangeResult.NOT_FOUND;
        }

        String alias = normalizeAlias(rawAlias);
        if (rawAlias != null && !rawAlias.isBlank() && alias.isEmpty()) {
            return ChangeResult.INVALID_ALIAS;
        }
        if (friend.alias.equals(alias)) {
            return ChangeResult.ALREADY_EXISTS;
        }

        replaceFriend(friend, new Friend(friend.name, friend.uuid, alias, friend.addedAt));
        save();
        notifyListeners();
        return ChangeResult.UPDATED;
    }

    public static synchronized int clear() {
        int count = friends.size();
        if (count == 0) {
            return 0;
        }

        friends.clear();
        save();
        notifyListeners();
        return count;
    }

    public static synchronized boolean isFriended(String name) {
        return findFriend(name, null) != null;
    }

    public static synchronized boolean isFriend(PlayerEntity player) {
        return player != null && findFriend(player.getName().getString(), player.getUuid()) != null;
    }

    /** Updates legacy name-only entries and renamed accounts after joining a world. */
    public static synchronized void refreshOnlineIdentities() {
        boolean changed = false;
        for (Friend friend : List.copyOf(friends)) {
            PlayerEntity player = findOnlinePlayer(friend);
            if (player == null) {
                continue;
            }

            String updatedName = player.getName().getString();
            UUID updatedUuid = player.getUuid();
            if (!friend.name.equals(updatedName) || !updatedUuid.equals(friend.uuid)) {
                replaceFriend(friend, new Friend(updatedName, updatedUuid, friend.alias, friend.addedAt));
                changed = true;
            }
        }

        if (changed) {
            save();
            notifyListeners();
        }
    }

    public static synchronized void save() {
        File file = getFile();
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            LOGGER.error("Failed to create friend directory {}", parent);
            return;
        }

        JsonObject root = new JsonObject();
        root.addProperty("version", FORMAT_VERSION);
        JsonArray entries = new JsonArray();
        for (Friend friend : friends) {
            entries.add(toJson(friend));
        }
        root.add("friends", entries);

        try {
            File temporary = File.createTempFile("friends-", ".tmp", parent);
            try (BufferedWriter writer = Files.newBufferedWriter(temporary.toPath(), StandardCharsets.UTF_8)) {
                new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(root, writer);
            }

            try {
                Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save friends to {}", file, e);
        }
    }

    public static synchronized void load() {
        File file = getFile();
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!isCurrentFormat(root)) {
                friends.clear();
                LOGGER.info("Discarded legacy friends file at {}; FriendManager now starts with an empty version {} list", file, FORMAT_VERSION);
                save();
                notifyListeners();
                return;
            }

            List<Friend> loaded = parseFriends(root.getAsJsonObject().getAsJsonArray("friends"));
            friends.clear();
            for (Friend friend : loaded) {
                if (findFriend(friend.name, friend.uuid) == null) {
                    friends.add(friend);
                }
            }
            notifyListeners();
        } catch (Exception e) {
            LOGGER.error("Failed to load friends from {}", file, e);
        }
    }

    private static File getFile() {
        return new File(ConfigManager.getConfigDirectory(), "friends.json");
    }

    private static boolean isCurrentFormat(JsonElement root) {
        if (root == null || !root.isJsonObject()) {
            return false;
        }

        JsonObject object = root.getAsJsonObject();
        try {
            return object.has("version")
                    && object.get("version").isJsonPrimitive()
                    && object.get("version").getAsInt() == FORMAT_VERSION
                    && object.has("friends")
                    && object.get("friends").isJsonArray();
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static List<Friend> parseFriends(JsonArray entries) {
        List<Friend> loaded = new ArrayList<>();
        for (JsonElement entry : entries) {
            if (!entry.isJsonObject()) {
                continue;
            }

            JsonObject friendJson = entry.getAsJsonObject();
            if (!friendJson.has("name")) {
                continue;
            }

            String name = normalizeName(friendJson.get("name").getAsString());
            if (name.isEmpty()) {
                continue;
            }

            UUID uuid = parseUuid(friendJson);
            String alias = friendJson.has("alias") ? normalizeAlias(friendJson.get("alias").getAsString()) : "";
            long addedAt = friendJson.has("addedAt") ? safeLong(friendJson.get("addedAt")) : 0L;
            loaded.add(new Friend(name, uuid, alias, addedAt));
        }
        return loaded;
    }

    private static JsonObject toJson(Friend friend) {
        JsonObject entry = new JsonObject();
        entry.addProperty("name", friend.name);
        if (friend.uuid != null) {
            entry.addProperty("uuid", friend.uuid.toString());
        }
        if (!friend.alias.isEmpty()) {
            entry.addProperty("alias", friend.alias);
        }
        entry.addProperty("addedAt", friend.addedAt);
        return entry;
    }

    private static Friend findFriend(String query, UUID uuid) {
        if (uuid != null) {
            for (Friend friend : friends) {
                if (uuid.equals(friend.uuid)) {
                    return friend;
                }
            }
        }

        String normalizedQuery = normalizeName(query);
        String normalizedAlias = normalizeAlias(query);
        for (Friend friend : friends) {
            if (friend.name.equalsIgnoreCase(normalizedQuery)
                    || (!friend.alias.isEmpty() && friend.alias.equalsIgnoreCase(normalizedAlias))) {
                return friend;
            }
        }
        return null;
    }

    private static PlayerEntity findOnlinePlayer(String name) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return null;
        }

        String normalizedName = normalizeName(name);
        for (PlayerEntity player : client.world.getPlayers()) {
            if (player.getName().getString().equalsIgnoreCase(normalizedName)) {
                return player;
            }
        }
        return null;
    }

    private static PlayerEntity findOnlinePlayer(Friend friend) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return null;
        }

        for (PlayerEntity player : client.world.getPlayers()) {
            if ((friend.uuid != null && friend.uuid.equals(player.getUuid()))
                    || player.getName().getString().equalsIgnoreCase(friend.name)) {
                return player;
            }
        }
        return null;
    }

    private static void replaceFriend(Friend oldFriend, Friend newFriend) {
        int index = friends.indexOf(oldFriend);
        if (index >= 0) {
            friends.set(index, newFriend);
        }
    }

    private static UUID parseUuid(JsonObject friendJson) {
        if (!friendJson.has("uuid")) {
            return null;
        }
        try {
            return UUID.fromString(friendJson.get("uuid").getAsString());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static long safeLong(JsonElement value) {
        try {
            return value.getAsLong();
        } catch (Exception ignored) {
            return 0L;
        }
    }

    private static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim();
        return normalized.length() > 16 || !normalized.matches("[A-Za-z0-9_]{3,16}") ? "" : normalized;
    }

    private static String normalizeAlias(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replace('§', ' ').replaceAll("[\\p{Cntrl}]", "").trim();
        return normalized.length() > 24 ? "" : normalized;
    }

    private static void notifyListeners() {
        for (Runnable listener : List.copyOf(listeners)) {
            try {
                listener.run();
            } catch (Throwable t) {
                LOGGER.warn("Friend listener failed", t);
            }
        }
    }
}
