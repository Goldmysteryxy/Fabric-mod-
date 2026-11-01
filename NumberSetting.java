package dev.terminal.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.Locale;

public class NumberSetting extends Setting<Double> {
    public final double min, max, step;
    public NumberSetting(String name, String desc, double def, double min, double max, double step) {
        super(name, desc, def); this.min = min; this.max = max; this.step = step;
    }
    @Override public void set(Double v) {
        double snapped = Math.round((v - min) / step) * step + min;
        value = Math.max(min, Math.min(max, snapped));
    }
    public int intValue() { return (int) Math.round(value); }
    public double frac() { return (value - min) / (max - min); }
    public void setFromFrac(double f) { set(min + Math.max(0, Math.min(1, f)) * (max - min)); }
    @Override public JsonElement toJson() { return new JsonPrimitive(value); }
    @Override public void fromJson(JsonElement e) { set(e.getAsDouble()); }
    @Override public boolean parse(String s) {
        try { set(Double.parseDouble(s)); return true; } catch (NumberFormatException ex) { return false; }
    }
    @Override public String display() {
        return step >= 1 ? String.valueOf((int) Math.round(value)) : String.format(Locale.ROOT, "%.2f", value);
    }
}
