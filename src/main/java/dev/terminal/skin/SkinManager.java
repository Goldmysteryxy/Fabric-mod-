package dev.terminal.skin;

import com.google.gson.*;
import dev.terminal.core.Terminal;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Local skin library (config/terminalmod/skins). Applying a skin uploads it to YOUR account through
 * Mojang's official skin API, using the same session token the game is already logged in with.
 */
public final class SkinManager {
    public record Skin(String file, String variant) {}

    public static final int DEFAULT_MAX = 8;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, int[]> FACES = new HashMap<>();
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    public static Path folder() {
        Path p = FabricLoader.getInstance().getConfigDir().resolve("terminalmod").resolve("skins");
        try { Files.createDirectories(p); } catch (IOException ignored) {}
        return p;
    }
    private static Path metaFile() { return folder().resolve("skins.json"); }

    private static JsonObject readMeta() {
        try { if (Files.exists(metaFile())) return JsonParser.parseString(Files.readString(metaFile())).getAsJsonObject(); }
        catch (Exception ignored) {}
        return new JsonObject();
    }
    private static void writeMeta(JsonObject o) {
        try { Files.writeString(metaFile(), GSON.toJson(o)); } catch (IOException ignored) {}
    }
    private static JsonObject variants(JsonObject meta) {
        if (!meta.has("variants")) meta.add("variants", new JsonObject());
        return meta.getAsJsonObject("variants");
    }

    public static int max() { JsonObject m = readMeta(); return m.has("max") ? m.get("max").getAsInt() : DEFAULT_MAX; }
    public static void setMax(int n) { JsonObject m = readMeta(); m.addProperty("max", Math.max(1, Math.min(36, n))); writeMeta(m); }

    public static List<Skin> list() {
        JsonObject v = variants(readMeta());
        List<String> files = new ArrayList<>();
        try (Stream<Path> s = Files.list(folder())) {
            s.forEach(p -> { String n = p.getFileName().toString(); if (n.toLowerCase().endsWith(".png")) files.add(n); });
        } catch (IOException ignored) {}
        Collections.sort(files, String.CASE_INSENSITIVE_ORDER);
        List<Skin> out = new ArrayList<>();
        for (String f : files) out.add(new Skin(f, v.has(f) ? v.get(f).getAsString() : "classic"));
        return out;
    }

    /** Returns null on success, otherwise a message explaining what went wrong. */
    public static String add(Path src) {
        String name = src.getFileName().toString();
        if (!name.toLowerCase().endsWith(".png")) return name + " is not a .png file";
        if (list().size() >= max()) return "Skin limit reached (" + max() + "). Delete one, or raise it with: skin max <n>";
        BufferedImage img;
        try { img = ImageIO.read(src.toFile()); } catch (IOException e) { return "Could not read " + name; }
        if (img == null) return name + " is not a valid PNG";
        if (img.getWidth() != 64 || (img.getHeight() != 64 && img.getHeight() != 32))
            return name + " must be 64x64 (or 64x32), not " + img.getWidth() + "x" + img.getHeight();
        String clean = name.replaceAll("[^A-Za-z0-9_.-]", "_");
        Path dest = folder().resolve(clean);
        for (int i = 2; Files.exists(dest); i++) dest = folder().resolve(clean.substring(0, clean.length() - 4) + "_" + i + ".png");
        try { Files.copy(src, dest); } catch (IOException e) { return "Could not copy " + name; }
        // guess slim (Alex) skins: the extra arm column is transparent
        boolean slim = img.getHeight() == 64 && (img.getRGB(55, 20) >>> 24) == 0;
        setVariant(dest.getFileName().toString(), slim ? "slim" : "classic");
        return null;
    }

    public static void setVariant(String file, String variant) {
        JsonObject m = readMeta();
        variants(m).addProperty(file, variant);
        writeMeta(m);
    }

    public static void delete(String file) {
        try { Files.deleteIfExists(resolve(file)); } catch (Exception ignored) {}
        JsonObject m = readMeta();
        variants(m).remove(file);
        writeMeta(m);
        FACES.remove(file);
    }

    private static Path resolve(String file) {
        Path p = folder().resolve(file).normalize();
        if (!p.startsWith(folder().normalize())) throw new IllegalArgumentException("bad path");
        return p;
    }

    /** 8x8 ARGB pixels of the head (with hat layer), for a tiny preview. Cached. */
    public static int[] face(String file) {
        return FACES.computeIfAbsent(file, f -> {
            int[] px = new int[64];
            try {
                BufferedImage img = ImageIO.read(resolve(f).toFile());
                for (int y = 0; y < 8; y++) for (int x = 0; x < 8; x++) {
                    int base = img.getRGB(8 + x, 8 + y), hat = img.getRGB(40 + x, 8 + y);
                    px[y * 8 + x] = (hat >>> 24) > 0 ? (hat | 0xFF000000) : (base | 0xFF000000);
                }
            } catch (Exception ignored) {}
            return px;
        });
    }

    /** Completes with null on success, or an error message. */
    public static CompletableFuture<String> apply(Skin skin) {
        String token = Minecraft.getInstance().getUser().getAccessToken();
        if (token == null || token.isBlank() || token.equals("0") || token.equalsIgnoreCase("FabricMC"))
            return CompletableFuture.completedFuture("Not signed in with a Microsoft account (offline/dev session), so the skin can't be uploaded.");
        byte[] png;
        try { png = Files.readAllBytes(resolve(skin.file())); }
        catch (Exception e) { return CompletableFuture.completedFuture("Could not read " + skin.file()); }

        String b = "----terminalmod" + System.nanoTime();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write(("--" + b + "\r\nContent-Disposition: form-data; name=\"variant\"\r\n\r\n" + skin.variant() + "\r\n").getBytes(StandardCharsets.UTF_8));
            out.write(("--" + b + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"skin.png\"\r\nContent-Type: image/png\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            out.write(png);
            out.write(("\r\n--" + b + "--\r\n").getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) { return CompletableFuture.completedFuture("Could not build request"); }

        HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.minecraftservices.com/minecraft/profile/skins"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "multipart/form-data; boundary=" + b)
                .POST(HttpRequest.BodyPublishers.ofByteArray(out.toByteArray()))
                .build();
        return HTTP.sendAsync(req, HttpResponse.BodyHandlers.ofString()).thenApply(r -> {
            int c = r.statusCode();
            if (c == 200) return (String) null;
            if (c == 401 || c == 403) return "Mojang rejected the login (token expired?). Restart the launcher and try again.";
            if (c == 429) return "Rate limited by Mojang. Wait a minute and try again.";
            return "Mojang returned error " + c + ".";
        }).exceptionally(e -> "Network error: " + e.getClass().getSimpleName());
    }

    public static void init() {
        Terminal.reg("skin", "skin <list|max <n>> - skins live in config/terminalmod/skins", a -> {
            if (a.length >= 1 && a[0].equalsIgnoreCase("max") && a.length >= 2) {
                try { setMax(Integer.parseInt(a[1])); Terminal.print("Skin limit = " + max()); }
                catch (NumberFormatException e) { Terminal.print("skin max <number 1-36>"); }
            } else {
                Terminal.print(list().size() + "/" + max() + " skins");
                for (Skin s : list()) Terminal.print("  " + s.file() + " (" + s.variant() + ")");
            }
        });
    }
}
