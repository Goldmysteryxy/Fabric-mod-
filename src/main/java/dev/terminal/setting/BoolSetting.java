package dev.terminal.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class BoolSetting extends Setting<Boolean> {
    public BoolSetting(String name, String desc, boolean def) { super(name, desc, def); }
    @Override public JsonElement toJson() { return new JsonPrimitive(value); }
    @Override public void fromJson(JsonElement e) { value = e.getAsBoolean(); }
    @Override public boolean parse(String s) {
        switch (s.toLowerCase()) {
            case "true", "on", "yes", "1" -> value = true;
            case "false", "off", "no", "0" -> value = false;
            default -> { return false; }
        }
        return true;
    }
    @Override public String display() { return value ? "ON" : "off"; }
}
