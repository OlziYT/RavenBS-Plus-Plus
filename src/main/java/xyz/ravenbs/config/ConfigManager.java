package xyz.ravenbs.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleManager;
import xyz.ravenbs.module.setting.Setting;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("RavenBS/ConfigManager");
    private static final String DEFAULT_CONFIG_FILE = "config.json";
    private static final String STATE_FILE = "state.json";

    private static final List<WeakReference<Runnable>> listeners = new ArrayList<>();
    private static final Map<String, Boolean> savedModuleStates = new HashMap<>();

    private static boolean loaded = false;
    private static String currentProfileName = "";

    public static boolean isLoaded() {
        return loaded;
    }

    public static String getCurrentProfileName() {
        return currentProfileName;
    }

    public static void addListener(Runnable listener) {
        if (listener == null) {
            return;
        }
        synchronized (listeners) {
            listeners.add(new WeakReference<>(listener));
        }
    }

    public static File getConfigDirectory() {
        File dir = new File(MinecraftClient.getInstance().runDirectory, "config/ravenbs");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File getProfilesDirectory() {
        File dir = new File(getConfigDirectory(), "profiles");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static synchronized void bootstrap() {
        if (loaded) {
            return;
        }

        loaded = true;
        currentProfileName = readPersistedProfileName();

        if (!currentProfileName.isEmpty() && loadProfileInternal(currentProfileName, false)) {
            return;
        }

        currentProfileName = "";
        persistState();
        loadDefaultConfigInternal();
    }

    public static synchronized boolean saveProfile(String rawName) {
        String name = normalizeProfileName(rawName);
        if (name.isEmpty()) {
            return false;
        }

        boolean result = saveConfigInternal(getProfileFile(name));
        if (result) {
            notifyListeners();
        }
        return result;
    }

    public static synchronized boolean loadProfile(String rawName) {
        String name = normalizeProfileName(rawName);
        if (name.isEmpty()) {
            return false;
        }

        boolean result = loadProfileInternal(name, true);
        if (result) {
            notifyListeners();
        }
        return result;
    }

    public static synchronized boolean renameProfile(String oldRawName, String newRawName) {
        String oldName = normalizeProfileName(oldRawName);
        String newName = normalizeProfileName(newRawName);
        if (oldName.isEmpty() || newName.isEmpty() || oldName.equalsIgnoreCase(newName)) {
            return false;
        }

        File source = getProfileFile(oldName);
        File target = getProfileFile(newName);
        if (!source.exists() || target.exists()) {
            return false;
        }

        try {
            Files.move(source.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException atomicMoveFailed) {
            try {
                Files.move(source.toPath(), target.toPath());
            } catch (IOException moveFailed) {
                LOGGER.error("Failed to rename profile {} to {}", oldName, newName, moveFailed);
                return false;
            }
        }

        if (oldName.equalsIgnoreCase(currentProfileName)) {
            currentProfileName = newName;
            persistState();
        }

        notifyListeners();
        return true;
    }

    public static synchronized boolean deleteProfile(String rawName) {
        String name = normalizeProfileName(rawName);
        if (name.isEmpty()) {
            return false;
        }

        File file = getProfileFile(name);
        if (!file.exists()) {
            return false;
        }

        try {
            Files.delete(file.toPath());
        } catch (IOException e) {
            LOGGER.error("Failed to delete profile {}", name, e);
            return false;
        }

        if (name.equalsIgnoreCase(currentProfileName)) {
            currentProfileName = "";
            persistState();
        }

        notifyListeners();
        return true;
    }

    public static synchronized List<String> listProfiles() {
        List<String> names = new ArrayList<>();
        for (File file : listProfileFiles()) {
            String fileName = file.getName();
            names.add(fileName.substring(0, fileName.length() - 5));
        }
        names.sort(String::compareToIgnoreCase);
        return names;
    }

    public static synchronized List<File> listProfileFiles() {
        File[] files = getProfilesDirectory().listFiles((dir, name) -> name.endsWith(".json"));
        List<File> result = new ArrayList<>();
        if (files == null) {
            return result;
        }
        for (File file : files) {
            result.add(file);
        }
        return result;
    }

    public static synchronized void saveActiveConfig() {
        File target = currentProfileName.isEmpty() ? getDefaultConfigFile() : getProfileFile(currentProfileName);
        saveConfigInternal(target);
        persistState();
    }

    public static synchronized void loadDefaultConfig() {
        loaded = true;
        currentProfileName = "";
        persistState();
        loadDefaultConfigInternal();
    }

    public static synchronized boolean isModuleEnabledInConfig(String moduleName) {
        return savedModuleStates.getOrDefault(moduleName, false);
    }

    public static boolean saveConfig(String name) {
        return saveProfile(name);
    }

    public static boolean deleteConfig(String name) {
        return deleteProfile(name);
    }

    public static void saveConfig() {
        saveActiveConfig();
    }

    public static boolean loadConfig(String name) {
        return loadProfile(name);
    }

    public static void loadConfig() {
        loadDefaultConfig();
    }

    private static boolean loadProfileInternal(String name, boolean persistProfileState) {
        File file = getProfileFile(name);
        if (!file.exists()) {
            return false;
        }

        if (!loadConfigInternal(file)) {
            return false;
        }

        currentProfileName = name;
        loaded = true;
        if (persistProfileState) {
            persistState();
        }
        return true;
    }

    private static boolean loadDefaultConfigInternal() {
        File file = getDefaultConfigFile();
        if (!file.exists()) {
            savedModuleStates.clear();
            for (Module module : ModuleManager.modules) {
                savedModuleStates.put(module.getName(), module.isEnabled());
            }
            return false;
        }

        boolean result = loadConfigInternal(file);
        if (result) {
            loaded = true;
        }
        return result;
    }

    private static boolean saveConfigInternal(File file) {
        if (MinecraftClient.getInstance().runDirectory == null) {
            return false;
        }

        JsonObject root = new JsonObject();
        for (Module module : ModuleManager.modules) {
            savedModuleStates.put(module.getName(), module.isEnabled());

            JsonObject moduleJson = new JsonObject();
            moduleJson.addProperty("enabled", module.isEnabled());
            moduleJson.addProperty("keycode", module.getKeycode());

            JsonObject settingsJson = new JsonObject();
            for (Setting setting : module.getSettings()) {
                JsonObject settingJson = setting.toJson();
                if (settingJson == null) {
                    continue;
                }
                for (String key : settingJson.keySet()) {
                    settingsJson.add(key, settingJson.get(key));
                }
            }

            moduleJson.add("settings", settingsJson);
            root.add(module.getName(), moduleJson);
        }

        try (Writer writer = new FileWriter(file)) {
            GSON.toJson(root, writer);
            return true;
        } catch (IOException e) {
            LOGGER.error("Failed to save config to {}", file, e);
            return false;
        }
    }

    private static boolean loadConfigInternal(File file) {
        try (Reader reader = new FileReader(file)) {
            JsonElement jsonElement = JsonParser.parseReader(reader);
            if (!jsonElement.isJsonObject()) {
                return false;
            }

            JsonObject root = jsonElement.getAsJsonObject();
            savedModuleStates.clear();

            for (Module module : ModuleManager.modules) {
                if (!root.has(module.getName()) || !root.get(module.getName()).isJsonObject()) {
                    savedModuleStates.put(module.getName(), false);
                    module.setEnabled(false);
                    continue;
                }

                JsonObject moduleJson = root.getAsJsonObject(module.getName());
                boolean enabled = moduleJson.has("enabled") && moduleJson.get("enabled").getAsBoolean();
                savedModuleStates.put(module.getName(), enabled);
                module.setEnabled(enabled);

                if (moduleJson.has("keycode")) {
                    module.setBind(moduleJson.get("keycode").getAsInt());
                }

                if (moduleJson.has("settings") && moduleJson.get("settings").isJsonObject()) {
                    JsonObject settingsJson = moduleJson.getAsJsonObject("settings");
                    for (Setting setting : module.getSettings()) {
                        setting.loadProfile(settingsJson);
                    }
                }
            }

            return true;
        } catch (IOException e) {
            LOGGER.error("Failed to load config from {}", file, e);
            return false;
        }
    }

    private static void persistState() {
        JsonObject stateJson = new JsonObject();
        stateJson.addProperty("currentProfile", currentProfileName);

        try (Writer writer = new FileWriter(getStateFile())) {
            GSON.toJson(stateJson, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save config state", e);
        }
    }

    private static String readPersistedProfileName() {
        File stateFile = getStateFile();
        if (!stateFile.exists()) {
            return "";
        }

        try (Reader reader = new FileReader(stateFile)) {
            JsonElement jsonElement = JsonParser.parseReader(reader);
            if (!jsonElement.isJsonObject()) {
                return "";
            }

            JsonObject stateJson = jsonElement.getAsJsonObject();
            if (!stateJson.has("currentProfile")) {
                return "";
            }

            return normalizeProfileName(stateJson.get("currentProfile").getAsString());
        } catch (IOException e) {
            LOGGER.error("Failed to read config state", e);
            return "";
        }
    }

    private static File getDefaultConfigFile() {
        return new File(getConfigDirectory(), DEFAULT_CONFIG_FILE);
    }

    private static File getStateFile() {
        return new File(getConfigDirectory(), STATE_FILE);
    }

    private static File getProfileFile(String profileName) {
        return new File(getProfilesDirectory(), profileName + ".json");
    }

    private static String normalizeProfileName(String rawName) {
        if (rawName == null) {
            return "";
        }

        String name = rawName.trim();
        if (name.endsWith(".json")) {
            name = name.substring(0, name.length() - 5);
        }

        name = name.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").trim();
        while (name.endsWith(".")) {
            name = name.substring(0, name.length() - 1);
        }
        return name;
    }

    private static void notifyListeners() {
        List<Runnable> snapshot = new ArrayList<>();
        synchronized (listeners) {
            listeners.removeIf(ref -> ref.get() == null);
            for (WeakReference<Runnable> ref : listeners) {
                Runnable listener = ref.get();
                if (listener != null) {
                    snapshot.add(listener);
                }
            }
        }

        for (Runnable listener : snapshot) {
            try {
                listener.run();
            } catch (Throwable t) {
                LOGGER.warn("Config listener failed", t);
            }
        }
    }
}
