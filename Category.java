package dev.terminal.core;

public enum Category {
    PLAYER("Player"), RENDER("Render"), WORLD("World"), HUD("HUD"), MISC("Misc"), MACROS("Macros");
    public final String label;
    Category(String label) { this.label = label; }
}
