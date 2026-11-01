package dev.terminal.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class StringSetting extends Setting<String> {
    public StringSetting(String name, String desc, String def) { super(name, desc, def); }
    @Override public JsonElement toJson() { return new JsonPrimitive(value); }
    @Override public void fromJson(JsonElement e) { value = e.getAsString(); }
    @Override public boolean parse(String s) { value = s.equalsIgnoreCase("none") ? "" : s; return true; }
    @Override public String display() { return value.isEmpty() ? "(empty)" : value; }
}
