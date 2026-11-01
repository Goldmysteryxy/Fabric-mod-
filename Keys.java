package dev.terminal.util;

import org.lwjgl.glfw.GLFW;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Keys {
    public static final int INVALID = -2;
    private static final Map<String, Integer> MAP = new LinkedHashMap<>();
    static {
        for (char c = 'a'; c <= 'z'; c++) MAP.put(String.valueOf(c), GLFW.GLFW_KEY_A + (c - 'a'));
        for (char c = '0'; c <= '9'; c++) MAP.put(String.valueOf(c), GLFW.GLFW_KEY_0 + (c - '0'));
        for (int i = 1; i <= 12; i++) MAP.put("f" + i, GLFW.GLFW_KEY_F1 + i - 1);
        MAP.put("space", GLFW.GLFW_KEY_SPACE);
        MAP.put("rshift", GLFW.GLFW_KEY_RIGHT_SHIFT); MAP.put("lshift", GLFW.GLFW_KEY_LEFT_SHIFT);
        MAP.put("rctrl", GLFW.GLFW_KEY_RIGHT_CONTROL); MAP.put("lctrl", GLFW.GLFW_KEY_LEFT_CONTROL);
        MAP.put("ralt", GLFW.GLFW_KEY_RIGHT_ALT); MAP.put("lalt", GLFW.GLFW_KEY_LEFT_ALT);
        MAP.put("tab", GLFW.GLFW_KEY_TAB); MAP.put("insert", GLFW.GLFW_KEY_INSERT);
        MAP.put("delete", GLFW.GLFW_KEY_DELETE); MAP.put("home", GLFW.GLFW_KEY_HOME);
        MAP.put("end", GLFW.GLFW_KEY_END); MAP.put("pageup", GLFW.GLFW_KEY_PAGE_UP);
        MAP.put("pagedown", GLFW.GLFW_KEY_PAGE_DOWN); MAP.put("up", GLFW.GLFW_KEY_UP);
        MAP.put("down", GLFW.GLFW_KEY_DOWN); MAP.put("left", GLFW.GLFW_KEY_LEFT);
        MAP.put("right", GLFW.GLFW_KEY_RIGHT); MAP.put("grave", GLFW.GLFW_KEY_GRAVE_ACCENT);
    }

    /** "none" -> -1, unknown -> INVALID */
    public static int parse(String s) {
        s = s.toLowerCase();
        if (s.equals("none")) return -1;
        return MAP.getOrDefault(s, INVALID);
    }

    public static String name(int code) {
        if (code < 0) return "none";
        for (var e : MAP.entrySet()) if (e.getValue() == code) return e.getKey().toUpperCase();
        return "key" + code;
    }
}
