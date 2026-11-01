package dev.terminal.module;

import dev.terminal.core.Category;
import dev.terminal.core.Module;
import dev.terminal.setting.BoolSetting;
import dev.terminal.setting.StringSetting;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/** Timestamps + keyword filter for game/system messages. Edit text settings in the terminal:  set chattweaks filter word1,word2 */
public class ChatTweaks extends Module {
    private final BoolSetting timestamps = add(new BoolSetting("Timestamps", "Prefix messages with the time", true));
    private final StringSetting format = add(new StringSetting("Format", "Time format, e.g. HH:mm:ss", "HH:mm"));
    private final StringSetting filter = add(new StringSetting("Filter", "Hide messages containing any of these (comma separated)", ""));

    public ChatTweaks() { super("ChatTweaks", "Chat timestamps and filter", Category.MISC); }

    public boolean allow(Component msg) {
        if (!isEnabled() || filter.get().isBlank()) return true;
        String text = msg.getString().toLowerCase();
        for (String w : filter.get().toLowerCase().split(",")) {
            w = w.trim();
            if (!w.isEmpty() && text.contains(w)) return false;
        }
        return true;
    }

    public Component modify(Component msg, boolean overlay) {
        if (!isEnabled() || overlay || !timestamps.get()) return msg;
        String t;
        try { t = LocalTime.now().format(DateTimeFormatter.ofPattern(format.get())); }
        catch (IllegalArgumentException e) { t = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")); }
        return Component.literal("[" + t + "] ").withStyle(ChatFormatting.GRAY).append(msg);
    }
}
