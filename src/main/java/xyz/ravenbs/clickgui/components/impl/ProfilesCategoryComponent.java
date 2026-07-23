package xyz.ravenbs.clickgui.components.impl;

import xyz.ravenbs.config.ConfigManager;
import xyz.ravenbs.module.Module;
import xyz.ravenbs.module.ModuleCategory;
import xyz.ravenbs.module.setting.impl.ButtonSetting;
import xyz.ravenbs.module.setting.impl.DescriptionSetting;
import xyz.ravenbs.module.setting.impl.StringSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Comparator;
import java.util.List;

public class ProfilesCategoryComponent extends CategoryComponent {

    private boolean sortByName = false;

    public ProfilesCategoryComponent(int x, int y) {
        super(ModuleCategory.profiles, x, y);
        ConfigManager.addListener(this::reloadModules);
    }

    @Override
    public void reloadModules() {
        this.getModules().clear();

        // 1. Settings / Tools
        this.getModules().add(new ModuleComponent(new SortModule(), this, 0));
        this.getModules().add(new ModuleComponent(new CreateProfileModule(), this, 0));

        List<File> files = new ArrayList<>(ConfigManager.listProfileFiles());
        if (sortByName) {
            files.sort(Comparator.comparing(File::getName, String::compareToIgnoreCase));
        } else {
            files.sort((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        }

        for (File file : files) {
            String name = file.getName().replace(".json", "");
            this.getModules().add(new ModuleComponent(new ProfileModule(name, file), this, 0));
        }
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.WRITABLE_BOOK);
    }

    // --- Fake Modules ---

    private class SortModule extends Module {
        public SortModule() {
            super("Sorting: " + (sortByName ? "Name" : "Date"), ModuleCategory.profiles, 0);
        }

        @Override
        public void onEnable() {
            sortByName = !sortByName;
            this.name = "Sorting: " + (sortByName ? "Name" : "Date"); // Update name
            ProfilesCategoryComponent.this.reloadModules();
            // Don't disable, let it reload which resets state
        }
    }

    private class CreateProfileModule extends Module {
        private final StringSetting nameSetting;

        public CreateProfileModule() {
            super("Create Profile", ModuleCategory.profiles, 0);
            String defaultName = "profile_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            this.registerSetting(nameSetting = new StringSetting("Name", defaultName));
            
            this.registerSetting(new ButtonSetting("Create", false) {
                 @Override
                 public void toggle() {
                      String name = nameSetting.getString();
                      if (name.isEmpty()) name = defaultName;
                      ConfigManager.saveProfile(name);
                 }
            });
        }
        
        @Override
        public void onEnable() {
             this.disable(); // It's just a folder for settings
        }
    }

    private class ProfileModule extends Module {
        private final String profileName;
        private final File file;
        private final StringSetting renameSetting;

        public ProfileModule(String name, File file) {
            super(name, ModuleCategory.profiles, 0);
            this.profileName = name;
            this.file = file;

            // Highlight active
            if (profileName.equals(ConfigManager.getCurrentProfileName())) {
                this.enabled = true;
            }

            // Settings
            this.registerSetting(new DescriptionSetting("Modified: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(file.lastModified()))));
            
            this.registerSetting(new ButtonSetting("Overwrite with Current", false) {
                 @Override
                 public void toggle() {
                     ConfigManager.saveProfile(profileName);
                 }
            });
            
            this.registerSetting(renameSetting = new StringSetting("Rename to", profileName));
            this.registerSetting(new ButtonSetting("Apply Rename", false) {
                 @Override
                 public void toggle() {
                     String newName = renameSetting.getString();
                     if (!newName.equals(profileName) && !newName.isEmpty()) {
                         ConfigManager.renameProfile(profileName, newName);
                     }
                 }
            });

            this.registerSetting(new ButtonSetting("Delete Profile", false) {
                @Override
                public void toggle() {
                     ConfigManager.deleteProfile(profileName);
                }
            });
        }

        @Override
        public void onEnable() {
            ConfigManager.loadProfile(profileName);
        }
    }
}
