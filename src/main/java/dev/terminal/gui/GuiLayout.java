package dev.terminal.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.terminal.core.Category;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** Remembers where you dragged each GUI panel and which ones are collapsed. */
public final class GuiLayout {
    private static final Map<Category, int[]> POS = new EnumMap<>(Category.class);
    private static final Set<Category> COLLAPSED = EnumSet.noneOf(Category.class);

    public static int[] get(Category c) { return POS.get(c); }
    public static void set(Category c, int x, int y) { POS.put(c, new int[]{x, y}); }
    public static boolean collapsed(Category c) { return COLLAPSED.contains(c); }
    public static void toggleCollapsed(Category c) { if (!COLLAPSED.remove(c)) COLLAPSED.add(c); }
    public static void reset() { POS.clear(); COLLAPSED.clear(); }

    public static JsonObject toJson() {
        JsonObject root = new JsonObject(), pos = new JsonObject();
        POS.forEach((c, p) -> { JsonArray a = new JsonArray(); a.add(p[0]); a.add(p[1]); pos.add(c.name(), a); });
        JsonArray col = new JsonArray();
        COLLAPSED.forEach(c -> col.add(c.name()));
        root.add("pos", pos);
        root.add("collapsed", col);
        return root;
    }

    public static void fromJson(JsonObject root) {
        reset();
        try {
            if (root.has("pos")) for (String k : root.getAsJsonObject("pos").keySet()) {
                JsonArray a = root.getAsJsonObject("pos").getAsJsonArray(k);
                POS.put(Category.valueOf(k), new int[]{a.get(0).getAsInt(), a.get(1).getAsInt()});
            }
            if (root.has("collapsed")) root.getAsJsonArray("collapsed").forEach(e -> COLLAPSED.add(Category.valueOf(e.getAsString())));
        } catch (Exception ignored) {}
    }
}
