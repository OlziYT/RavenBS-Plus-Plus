package xyz.ravenbs.utility;

import xyz.ravenbs.module.ModuleManager;
import xyz.ravenbs.module.Module;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.resource.language.I18n;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandManager {
    
    public static boolean onChat(String message) {
        if (!message.startsWith(".")) return false;
        
        String[] args = message.substring(1).trim().split("\\s+");
        if (args.length == 0) return false;
        
        String cmd = args[0].toLowerCase();
        
        switch (cmd) {
            case "help":
                Utils.sendMessage("§bRavenBS Fabric Commands:");
                Utils.sendMessage("§7.bind <module> <key> - Bind module");
                Utils.sendMessage(tr("raven.command.help.friend"));
                Utils.sendMessage(tr("raven.command.help.config"));
                Utils.sendMessage(tr("raven.command.help.update"));
                return true;
                
            case "bind":
                if (args.length < 3) {
                    Utils.sendMessage("§cUsage: .bind <module> <key>");
                    return true;
                }
                Module m = ModuleManager.getModule(args[1]);
                if (m == null) {
                    Utils.sendMessage("§cModule not found: " + args[1]);
                    return true;
                }
                int keycode = resolveKeycode(args[2]);
                if (keycode == Integer.MIN_VALUE) {
                    Utils.sendMessage("§cInvalid key: " + args[2]);
                    return true;
                }
                m.setBind(keycode);
                Utils.sendMessage("§aBound " + m.getName() + " to " + args[2].toUpperCase());
                return true;
                
            case "friend":
            case "f":
                if (args.length < 2) {
                    Utils.sendMessage(tr("raven.command.friend.usage.full"));
                    return true;
                }
                handleFriendCommand(args);
                return true;
            
            case "config":
            case "c":
                if (args.length < 2) {
                    Utils.sendMessage(tr("raven.command.config.usage.full"));
                    return true;
                }
                if (args[1].equalsIgnoreCase("list")) {
                    java.util.List<String> profiles = xyz.ravenbs.config.ConfigManager.listProfiles();
                    if (profiles.isEmpty()) {
                        Utils.sendMessage(tr("raven.command.config.list.empty"));
                        return true;
                    }
                    String currentProfile = xyz.ravenbs.config.ConfigManager.getCurrentProfileName();
                    if (currentProfile != null && !currentProfile.isEmpty()) {
                        Utils.sendMessage(tr("raven.command.config.current", currentProfile));
                    }
                    String names = profiles.stream().collect(Collectors.joining(", "));
                    Utils.sendMessage(tr("raven.command.config.list", names));
                    return true;
                }
                if (args.length < 3) {
                    Utils.sendMessage(tr("raven.command.config.usage.short"));
                    return true;
                }
                String profileName = args[2];
                if (args[1].equalsIgnoreCase("save")) {
                    if (xyz.ravenbs.config.ConfigManager.saveProfile(profileName)) {
                        Utils.sendMessage("§aSaved config: " + profileName);
                    } else {
                        Utils.sendMessage("§cFailed to save.");
                    }
                } else if (args[1].equalsIgnoreCase("load")) {
                    if (xyz.ravenbs.config.ConfigManager.loadProfile(profileName)) {
                        Utils.sendMessage("§aLoaded config: " + profileName);
                    } else {
                        Utils.sendMessage("§cConfig not found.");
                    }
                }
                return true;

            case "updatecheck":
            case "update":
                if (args.length < 2) {
                    Utils.sendMessage(tr("raven.command.update.usage"));
                    return true;
                }
                String toggle = args[1].toLowerCase();
                if (toggle.equals("on") || toggle.equals("enable")) {
                    UpdateChecker.setEnabled(true);
                    UpdateChecker.checkForUpdates();
                    Utils.sendMessage(tr("raven.command.update.enabled"));
                } else if (toggle.equals("off") || toggle.equals("disable")) {
                    UpdateChecker.setEnabled(false);
                    Utils.sendMessage(tr("raven.command.update.disabled"));
                } else if (toggle.equals("status")) {
                    Utils.sendMessage(UpdateChecker.isEnabled() ? tr("raven.command.update.status_on") : tr("raven.command.update.status_off"));
                } else {
                    Utils.sendMessage(tr("raven.command.update.usage"));
                }
                return true;
                
            case "toggle":
            case "t":
                if (args.length < 2) {
                    Utils.sendMessage("§cUsage: .toggle <module>");
                    return true;
                }
                Module mToggle = ModuleManager.getModule(args[1]);
                if (mToggle == null) {
                    Utils.sendMessage("§cModule not found: " + args[1]);
                    return true;
                }
                mToggle.toggle();
                Utils.sendMessage(mToggle.isEnabled() ? "§aEnabled " + mToggle.getName() : "§cDisabled " + mToggle.getName());
                return true;
                
            default:
                Utils.sendMessage("§cUnknown command. Type .help");
                return true;
        }
    }
    public static class Suggestion {
        public final String text;
        public final String tooltip;
        public Suggestion(String text, String tooltip) {
            this.text = text;
            this.tooltip = tooltip;
        }
    }

    public static class SuggestionContext {
        public final int offset;
        public final java.util.List<Suggestion> suggestions;
        public SuggestionContext(int offset, java.util.List<Suggestion> suggestions) {
            this.offset = offset;
            this.suggestions = suggestions;
        }
    }

    public static SuggestionContext getSuggestions(String input) {
        if (input.endsWith(" ")) {
            // Treat "cmd " as "cmd " (ready for next arg)
            // But split ignores trailing empty strings usually unless -1 used.
        }
        
        String[] args = input.split(" ", -1);
        
        // Case 1: Typing command
        // Input: "." or ".t" -> args[0] is "." or ".t"
        if (args.length == 1) {
            java.util.List<Suggestion> list = new java.util.ArrayList<>();
            String current = args[0];
            
            addIfMatch(list, current, ".help", "Show help");
            addIfMatch(list, current, ".bind", "Bind a module to a key");
            addIfMatch(list, current, ".friend", "Manage friends");
            addIfMatch(list, current, ".config", "Manage config profiles");
            addIfMatch(list, current, ".toggle", "Toggle a module");
            addIfMatch(list, current, ".updatecheck", tr("raven.suggest.update.toggle"));
            addIfMatch(list, current, ".update", tr("raven.suggest.update.alias"));
            // Aliases
            addIfMatch(list, current, ".t", "Alias for .toggle");
            addIfMatch(list, current, ".f", "Alias for .friend");
            addIfMatch(list, current, ".c", "Alias for .config");
            
            return new SuggestionContext(0, list);
        }
        
        // Case 2: Typing first argument
        if (args.length == 2) {
            String cmd = args[0].toLowerCase();
            String arg = args[1].toLowerCase();
            int offset = input.lastIndexOf(" ") + 1;
            java.util.List<Suggestion> list = new java.util.ArrayList<>();
            
            if (cmd.equals(".toggle") || cmd.equals(".t") || cmd.equals(".bind")) {
                for (Module m : ModuleManager.modules) {
                    if (m.getName().toLowerCase().startsWith(arg)) {
                        list.add(new Suggestion(m.getName(), m.getDescription())); // Assuming getDescription exists or use name
                    }
                }
            } else if (cmd.equals(".friend") || cmd.equals(".f")) {
                addIfMatch(list, arg, "add", "Add a friend");
                addIfMatch(list, arg, "remove", "Remove a friend");
                addIfMatch(list, arg, "toggle", "Add or remove a friend");
                addIfMatch(list, arg, "alias", "Set a friend alias");
                addIfMatch(list, arg, "clear", "Clear all friends");
                addIfMatch(list, arg, "list", tr("raven.suggest.friend.list"));
                addIfMatch(list, arg, "online", tr("raven.suggest.friend.online"));
            } else if (cmd.equals(".config") || cmd.equals(".c")) {
                addIfMatch(list, arg, "save", "Save current profile");
                addIfMatch(list, arg, "load", "Load a profile");
                addIfMatch(list, arg, "list", tr("raven.suggest.config.list"));
            } else if (cmd.equals(".updatecheck") || cmd.equals(".update")) {
                addIfMatch(list, arg, "on", tr("raven.suggest.update.on"));
                addIfMatch(list, arg, "off", tr("raven.suggest.update.off"));
                addIfMatch(list, arg, "status", tr("raven.suggest.update.status"));
            }
            
            // Sort list?
            list.sort((s1, s2) -> s1.text.compareToIgnoreCase(s2.text));
            
            return new SuggestionContext(offset, list);
        }
        
        if (args.length == 3) {
            String cmd = args[0].toLowerCase();
            String action = args[1].toLowerCase();
            if ((cmd.equals(".friend") || cmd.equals(".f"))
                    && (action.equals("remove") || action.equals("toggle") || action.equals("alias"))) {
                java.util.List<Suggestion> list = new java.util.ArrayList<>();
                String query = args[2].toLowerCase();
                for (FriendManager.Friend friend : FriendManager.getFriends()) {
                    if (friend.getName().toLowerCase().startsWith(query)) {
                        list.add(new Suggestion(friend.getName(), friend.getDisplayName()));
                    }
                }
                list.sort((first, second) -> first.text.compareToIgnoreCase(second.text));
                return new SuggestionContext(input.lastIndexOf(" ") + 1, list);
            }
        }
        
        return new SuggestionContext(0, new java.util.ArrayList<>());
    }
    
    private static void addIfMatch(java.util.List<Suggestion> list, String input, String target, String tooltip) {
        if (target.toLowerCase().startsWith(input.toLowerCase())) {
            list.add(new Suggestion(target, tooltip));
        }
    }

    private static void handleFriendCommand(String[] args) {
        String action = args[1].toLowerCase();
        if (action.equals("list")) {
            java.util.List<FriendManager.Friend> friends = FriendManager.getFriends();
            if (friends.isEmpty()) {
                Utils.sendMessage(tr("raven.command.friend.list.empty"));
                return;
            }
            String names = friends.stream().map(FriendManager.Friend::getDisplayName).collect(Collectors.joining(", "));
            Utils.sendMessage(tr("raven.command.friend.list", names));
            Utils.sendMessage(tr("raven.command.friend.summary", FriendManager.getOnlineCount(), friends.size()));
            return;
        }
        if (action.equals("online")) {
            java.util.List<FriendManager.Friend> friends = FriendManager.getOnlineFriends();
            if (friends.isEmpty()) {
                Utils.sendMessage(tr("raven.command.friend.online.empty"));
                return;
            }
            String names = friends.stream().map(FriendManager.Friend::getDisplayName).collect(Collectors.joining(", "));
            Utils.sendMessage(tr("raven.command.friend.online", names));
            return;
        }
        if (action.equals("clear")) {
            int cleared = FriendManager.clear();
            Utils.sendMessage(cleared == 0
                    ? tr("raven.command.friend.list.empty")
                    : tr("raven.command.friend.cleared", cleared));
            return;
        }
        if (args.length < 3) {
            Utils.sendMessage(tr("raven.command.friend.usage.short"));
            return;
        }

        String name = args[2];
        FriendManager.ChangeResult result;
        switch (action) {
            case "add":
                result = FriendManager.addFriend(name, joinArguments(args, 3));
                break;
            case "remove":
                result = FriendManager.removeFriend(name);
                break;
            case "toggle":
                result = FriendManager.isFriended(name)
                        ? FriendManager.removeFriend(name)
                        : FriendManager.addFriend(name, "");
                break;
            case "alias":
                if (args.length < 4) {
                    Utils.sendMessage(tr("raven.command.friend.alias.usage"));
                    return;
                }
                String alias = joinArguments(args, 3);
                result = FriendManager.setAlias(name, alias.equalsIgnoreCase("clear") ? "" : alias);
                break;
            default:
                Utils.sendMessage(tr("raven.command.friend.usage.full"));
                return;
        }
        sendFriendResult(result, name);
    }

    private static String joinArguments(String[] args, int startIndex) {
        if (args.length <= startIndex) {
            return "";
        }
        return String.join(" ", java.util.Arrays.copyOfRange(args, startIndex, args.length));
    }

    private static void sendFriendResult(FriendManager.ChangeResult result, String name) {
        switch (result) {
            case ADDED:
                Utils.sendMessage(tr("raven.command.friend.added", name));
                break;
            case UPDATED:
                Utils.sendMessage(tr("raven.command.friend.updated", name));
                break;
            case REMOVED:
                Utils.sendMessage(tr("raven.command.friend.removed", name));
                break;
            case ALREADY_EXISTS:
                Utils.sendMessage(tr("raven.command.friend.exists", name));
                break;
            case NOT_FOUND:
                Utils.sendMessage(tr("raven.command.friend.not_found", name));
                break;
            case INVALID_NAME:
                Utils.sendMessage(tr("raven.command.friend.invalid_name"));
                break;
            case INVALID_ALIAS:
                Utils.sendMessage(tr("raven.command.friend.invalid_alias"));
                break;
        }
    }

    private static String tr(String key, Object... args) {
        return I18n.translate(key, args);
    }

    private static final Map<String, String> KEY_ALIASES = new HashMap<>();
    static {
        KEY_ALIASES.put("shift", "key.keyboard.left.shift");
        KEY_ALIASES.put("lshift", "key.keyboard.left.shift");
        KEY_ALIASES.put("rshift", "key.keyboard.right.shift");
        KEY_ALIASES.put("ctrl", "key.keyboard.left.control");
        KEY_ALIASES.put("lctrl", "key.keyboard.left.control");
        KEY_ALIASES.put("rctrl", "key.keyboard.right.control");
        KEY_ALIASES.put("alt", "key.keyboard.left.alt");
        KEY_ALIASES.put("lalt", "key.keyboard.left.alt");
        KEY_ALIASES.put("ralt", "key.keyboard.right.alt");
        KEY_ALIASES.put("space", "key.keyboard.space");
        KEY_ALIASES.put("enter", "key.keyboard.enter");
        KEY_ALIASES.put("return", "key.keyboard.enter");
        KEY_ALIASES.put("esc", "key.keyboard.escape");
        KEY_ALIASES.put("escape", "key.keyboard.escape");
        KEY_ALIASES.put("tab", "key.keyboard.tab");
        KEY_ALIASES.put("backspace", "key.keyboard.backspace");
        KEY_ALIASES.put("delete", "key.keyboard.delete");
        KEY_ALIASES.put("home", "key.keyboard.home");
        KEY_ALIASES.put("end", "key.keyboard.end");
        KEY_ALIASES.put("pageup", "key.keyboard.page.up");
        KEY_ALIASES.put("pagedown", "key.keyboard.page.down");
        KEY_ALIASES.put("up", "key.keyboard.up");
        KEY_ALIASES.put("down", "key.keyboard.down");
        KEY_ALIASES.put("left", "key.keyboard.left");
        KEY_ALIASES.put("right", "key.keyboard.right");
    }

    private static int resolveKeycode(String raw) {
        if (raw == null || raw.isEmpty()) return Integer.MIN_VALUE;
        String normalized = raw.trim().toLowerCase();

        Integer mouseCode = parseMouseKey(normalized);
        if (mouseCode != null) {
            return mouseCode;
        }

        String translationKey = KEY_ALIASES.getOrDefault(normalized, normalized);
        String candidate = translationKey.startsWith("key.") ? translationKey : "key.keyboard." + translationKey;

        InputUtil.Key key = InputUtil.fromTranslationKey(candidate);
        if (key == InputUtil.UNKNOWN_KEY && !translationKey.startsWith("key.")) {
            key = InputUtil.fromTranslationKey("key." + translationKey);
        }

        if (key == InputUtil.UNKNOWN_KEY) {
            return Integer.MIN_VALUE;
        }
        return key.getCode();
    }

    private static Integer parseMouseKey(String normalized) {
        if (!normalized.startsWith("mouse")) {
            return null;
        }
        String digits = normalized.replace("mouse", "").replace("button", "");
        if (digits.isEmpty()) {
            return null;
        }
        try {
            int mouseIndex = Integer.parseInt(digits);
            if (mouseIndex <= 0) {
                return null;
            }
            // GLFW mouse buttons start at 0, user-friendly numbering starts at 1
            int button = mouseIndex - 1;
            return -100 - button;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
