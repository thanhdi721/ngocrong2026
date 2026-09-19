package nro.models.event;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Đọc / ghi từng khóa trong Config.properties (thư mục chạy server).
 *
 * Ghi theo từng dòng để GIỮ NGUYÊN chú thích và thứ tự các khóa khác
 * (Properties.store sẽ xóa hết chú thích). Chỉ ghi giá trị ASCII.
 */
public final class EventConfig {

    public static final String FILE = "Config.properties";

    private EventConfig() {
    }

    /** Đọc toàn bộ file; lỗi (thiếu file...) -> Properties rỗng. */
    public static synchronized Properties load() {
        Properties p = new Properties();
        try (InputStream in = new FileInputStream(FILE)) {
            p.load(in);
        } catch (Exception ignored) {
        }
        return p;
    }

    public static boolean getBool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        if (v == null) {
            return def;
        }
        v = v.trim();
        if (v.equalsIgnoreCase("true") || v.equals("1") || v.equalsIgnoreCase("on") || v.equalsIgnoreCase("yes")) {
            return true;
        }
        if (v.equalsIgnoreCase("false") || v.equals("0") || v.equalsIgnoreCase("off") || v.equalsIgnoreCase("no")) {
            return false;
        }
        return def;
    }

    /**
     * Đặt {@code key=value}: thay dòng cũ nếu có, không thì thêm cuối file (kèm chú thích).
     * Ghi ra file tạm rồi thay thế để không làm hỏng file nếu lỗi giữa chừng.
     */
    public static synchronized void set(String key, String value, String comment) throws IOException {
        if (!key.matches("[A-Za-z0-9_.]+") || !value.matches("[\\x20-\\x7E]*")) {
            throw new IllegalArgumentException("Khóa/giá trị cấu hình không hợp lệ: " + key);
        }
        Path path = Paths.get(FILE);
        List<String> lines = Files.exists(path)
                ? new ArrayList<>(Files.readAllLines(path, StandardCharsets.ISO_8859_1))
                : new ArrayList<>();
        boolean replaced = false;
        for (int i = 0; i < lines.size(); i++) {
            String s = lines.get(i).trim();
            if (s.startsWith("#") || s.startsWith("!")) {
                continue;
            }
            if (s.matches(java.util.regex.Pattern.quote(key) + "\\s*[=:].*") || s.equals(key)) {
                lines.set(i, key + "=" + value);
                replaced = true;
            }
        }
        if (!replaced) {
            if (comment != null && !comment.isEmpty()) {
                lines.add("# " + comment);
            }
            lines.add(key + "=" + value);
        }
        Path tmp = Paths.get(FILE + ".tmp");
        Files.write(tmp, lines, StandardCharsets.ISO_8859_1);
        try {
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
