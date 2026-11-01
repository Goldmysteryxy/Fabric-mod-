package dev.terminal.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class EnumSetting extends Setting<String> {
    public final String[] options;
    public EnumSetting(String name, String desc, String def, String... options) {
        super(name, desc, def); this.options = options;
    }
    public int index() {
        for (int i = 0; i < options.length; i++) if (options[i].equals(value)) return i;
        return 0;
    }
    public void cycle(int dir) { value = options[Math.floorMod(index() + dir, options.length)]; }
    @Override public JsonElement toJson() { return new JsonPrimitive(value); }
    @Override public void fromJson(JsonElement e) { parse(e.getAsString()); }
    @Override public boolean parse(String s) {
        for (String o : options) if (o.equalsIgnoreCase(s)) { value = o; return true; }
        return false;
    }
    @Override public String display() { return value; }
}
