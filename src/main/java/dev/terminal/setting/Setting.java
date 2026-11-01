package dev.terminal.setting;

import com.google.gson.JsonElement;

public abstract class Setting<T> {
    public final String name, description;
    protected T value;
    private final T def;

    protected Setting(String name, String description, T def) {
        this.name = name; this.description = description; this.def = def; this.value = def;
    }
    public T get() { return value; }
    public void set(T v) { value = v; }
    public void reset() { value = def; }
    /** Name used in terminal commands: lowercase, no spaces. */
    public String key() { return name.toLowerCase().replace(" ", ""); }

    public abstract JsonElement toJson();
    public abstract void fromJson(JsonElement e);
    /** Parse from terminal text. Returns false if invalid. */
    public abstract boolean parse(String s);
    public abstract String display();
}
