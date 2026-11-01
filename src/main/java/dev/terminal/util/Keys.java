package dev.terminal.util;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.LinkedHashMap;
import java.util.Map;

/** Key names for terminal commands. 26.3 uses SDL scancodes, so every code comes from InputConstants. */
public final class Keys {
    public static final int INVALID = -2;
    private static final Map<String, Integer> MAP = new LinkedHashMap<>();
    static {
        MAP.put("a", InputConstants.KEY_A);
        MAP.put("b", InputConstants.KEY_B);
        MAP.put("c", InputConstants.KEY_C);
        MAP.put("d", InputConstants.KEY_D);
        MAP.put("e", InputConstants.KEY_E);
        MAP.put("f", InputConstants.KEY_F);
        MAP.put("g", InputConstants.KEY_G);
        MAP.put("h", InputConstants.KEY_H);
        MAP.put("i", InputConstants.KEY_I);
        MAP.put("j", InputConstants.KEY_J);
        MAP.put("k", InputConstants.KEY_K);
        MAP.put("l", InputConstants.KEY_L);
        MAP.put("m", InputConstants.KEY_M);
        MAP.put("n", InputConstants.KEY_N);
        MAP.put("o", InputConstants.KEY_O);
        MAP.put("p", InputConstants.KEY_P);
        MAP.put("q", InputConstants.KEY_Q);
        MAP.put("r", InputConstants.KEY_R);
        MAP.put("s", InputConstants.KEY_S);
        MAP.put("t", InputConstants.KEY_T);
        MAP.put("u", InputConstants.KEY_U);
        MAP.put("v", InputConstants.KEY_V);
        MAP.put("w", InputConstants.KEY_W);
        MAP.put("x", InputConstants.KEY_X);
        MAP.put("y", InputConstants.KEY_Y);
        MAP.put("z", InputConstants.KEY_Z);
        MAP.put("0", InputConstants.KEY_0);
        MAP.put("1", InputConstants.KEY_1);
        MAP.put("2", InputConstants.KEY_2);
        MAP.put("3", InputConstants.KEY_3);
        MAP.put("4", InputConstants.KEY_4);
        MAP.put("5", InputConstants.KEY_5);
        MAP.put("6", InputConstants.KEY_6);
        MAP.put("7", InputConstants.KEY_7);
        MAP.put("8", InputConstants.KEY_8);
        MAP.put("9", InputConstants.KEY_9);
        MAP.put("f1", InputConstants.KEY_F1);
        MAP.put("f2", InputConstants.KEY_F2);
        MAP.put("f3", InputConstants.KEY_F3);
        MAP.put("f4", InputConstants.KEY_F4);
        MAP.put("f5", InputConstants.KEY_F5);
        MAP.put("f6", InputConstants.KEY_F6);
        MAP.put("f7", InputConstants.KEY_F7);
        MAP.put("f8", InputConstants.KEY_F8);
        MAP.put("f9", InputConstants.KEY_F9);
        MAP.put("f10", InputConstants.KEY_F10);
        MAP.put("f11", InputConstants.KEY_F11);
        MAP.put("f12", InputConstants.KEY_F12);
        MAP.put("space", InputConstants.KEY_SPACE);
        MAP.put("rshift", InputConstants.KEY_RSHIFT);
        MAP.put("lshift", InputConstants.KEY_LSHIFT);
        MAP.put("rctrl", InputConstants.KEY_RCONTROL);
        MAP.put("lctrl", InputConstants.KEY_LCONTROL);
        MAP.put("ralt", InputConstants.KEY_RALT);
        MAP.put("lalt", InputConstants.KEY_LALT);
        MAP.put("tab", InputConstants.KEY_TAB);
        MAP.put("insert", InputConstants.KEY_INSERT);
        MAP.put("delete", InputConstants.KEY_DELETE);
        MAP.put("home", InputConstants.KEY_HOME);
        MAP.put("end", InputConstants.KEY_END);
        MAP.put("pageup", InputConstants.KEY_PAGEUP);
        MAP.put("pagedown", InputConstants.KEY_PAGEDOWN);
        MAP.put("up", InputConstants.KEY_UP);
        MAP.put("down", InputConstants.KEY_DOWN);
        MAP.put("left", InputConstants.KEY_LEFT);
        MAP.put("right", InputConstants.KEY_RIGHT);
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
