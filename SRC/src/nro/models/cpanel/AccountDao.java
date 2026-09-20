package nro.models.cpanel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import nro.models.cpanel.CPanel.CPanelException;
import nro.models.data.LocalManager;
import nro.models.network.MySession;
import nro.models.player.Player;
import nro.models.server.Client;

/**
 * Thao tác bảng {@code account}. Chỉ được gọi trên luồng nền của cpanel.
 * Mọi câu SQL đều dùng PreparedStatement có tham số.
 *
 * Ghi chú: các cột account KHÔNG bị luồng lưu nhân vật (PlayerDAO.updatePlayer) ghi đè,
 * nên luôn sửa thẳng DB; nếu tài khoản đang online thì đồng bộ thêm trường tương ứng
 * trong MySession để có hiệu lực ngay.
 */
final class AccountDao {

    static final int MAX_ROWS = 500;

    private AccountDao() {
    }

    static final class AccountRow {

        int id;
        String username;
        boolean admin;
        boolean ban;
        int vnd;
        int tongnap;
        boolean active;
        Timestamp createTime;
        String online; // tên nhân vật nếu đang online
    }

    static String likeEscape(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    static List<AccountRow> search(String keyword) throws SQLException {
        List<AccountRow> list = new ArrayList<>();
        String sql = "SELECT id, username, is_admin, ban, vnd, tongnap, active, create_time FROM account"
                + " WHERE username LIKE ? ORDER BY id DESC LIMIT " + MAX_ROWS;
        try (Connection con = LocalManager.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + likeEscape(keyword == null ? "" : keyword.trim()) + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AccountRow r = new AccountRow();
                    r.id = rs.getInt("id");
                    r.username = rs.getString("username");
                    r.admin = rs.getBoolean("is_admin");
                    r.ban = rs.getBoolean("ban");
                    r.vnd = rs.getInt("vnd");
                    r.tongnap = rs.getInt("tongnap");
                    r.active = rs.getInt("active") != 0;
                    try {
                        r.createTime = rs.getTimestamp("create_time");
                    } catch (SQLException e) {
                        r.createTime = null;
                    }
                    Player p = Client.gI().getPlayerByUser(r.id);
                    r.online = p != null ? p.name : null;
                    list.add(r);
                }
            }
        }
        return list;
    }

    /** Tạo tài khoản mới, trả về id. Mật khẩu lưu dạng chữ thường như code đăng nhập hiện có. */
    static int create(String username, String password, boolean active) throws Exception {
        validateUsername(username);
        validatePassword(password);
        try (Connection con = LocalManager.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT id FROM account WHERE username = ? LIMIT 1")) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new CPanelException("Tên đăng nhập \"" + username + "\" đã tồn tại.");
                    }
                }
            }
            // email/token/xsrf_token/newpass là NOT NULL không có DEFAULT -> phải truyền chuỗi rỗng
            String sql = "INSERT INTO account (username, password, email, token, xsrf_token, newpass, active)"
                    + " VALUES (?, ?, '', '', '', '', ?)";
            try (PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username);
                ps.setString(2, password);
                ps.setInt(3, active ? 1 : 0);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    return rs.next() ? rs.getInt(1) : -1;
                }
            } catch (SQLException e) {
                if (e.getErrorCode() == 1062) { // duplicate key (UNIQUE username)
                    throw new CPanelException("Tên đăng nhập \"" + username + "\" đã tồn tại.");
                }
                throw e;
            }
        }
    }

    static void validateUsername(String u) throws CPanelException {
        if (u == null || u.isEmpty()) {
            throw new CPanelException("Tên đăng nhập không được để trống.");
        }
        if (u.length() > 20) {
            throw new CPanelException("Tên đăng nhập tối đa 20 ký tự (cột varchar(20)).");
        }
        if (!u.matches("[A-Za-z0-9_.@-]+")) {
            throw new CPanelException("Tên đăng nhập chỉ gồm chữ không dấu, số và các ký tự _ . @ -");
        }
    }

    static void validatePassword(String p) throws CPanelException {
        if (p == null || p.isEmpty()) {
            throw new CPanelException("Mật khẩu không được để trống.");
        }
        if (p.length() > 100) {
            throw new CPanelException("Mật khẩu tối đa 100 ký tự.");
        }
    }

    static void changePassword(int id, String password) throws Exception {
        validatePassword(password);
        update("UPDATE account SET password = ? WHERE id = ?", password, id);
    }

    static void setAdmin(int id, boolean admin) throws Exception {
        update("UPDATE account SET is_admin = ? WHERE id = ?", admin ? 1 : 0, id);
        MySession s = onlineSession(id);
        if (s != null) {
            s.isAdmin = admin; // có hiệu lực ngay, không cần đăng nhập lại
        }
    }

    static void setBan(int id, boolean ban) throws Exception {
        update("UPDATE account SET ban = ? WHERE id = ?", ban ? 1 : 0, id);
    }

    static void setActive(int id, boolean active) throws Exception {
        update("UPDATE account SET active = ? WHERE id = ?", active ? 1 : 0, id);
        MySession s = onlineSession(id);
        if (s != null) {
            s.actived = active;
        }
    }

    /**
     * Cộng (delta > 0) / trừ (delta < 0) VND. Cập nhật tương đối trên DB (giống PlayerDAO.subvnd)
     * nên không đè lên giao dịch đang diễn ra; không cho số dư âm.
     * @return số dư mới
     */
    static int addVnd(int id, int delta) throws Exception {
        try (Connection con = LocalManager.getConnection()) {
            int rows;
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE account SET vnd = vnd + ? WHERE id = ? AND vnd + ? >= 0")) {
                ps.setInt(1, delta);
                ps.setInt(2, id);
                ps.setInt(3, delta);
                rows = ps.executeUpdate();
            }
            if (rows == 0) {
                throw new CPanelException("Không cập nhật được: tài khoản không tồn tại hoặc số dư không đủ để trừ.");
            }
            int newVnd = 0;
            try (PreparedStatement ps = con.prepareStatement("SELECT vnd FROM account WHERE id = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        newVnd = rs.getInt(1);
                    }
                }
            }
            MySession s = onlineSession(id);
            if (s != null) {
                s.vnd = newVnd;
            }
            return newVnd;
        }
    }

    static MySession onlineSession(int accountId) {
        Player p = Client.gI().getPlayerByUser(accountId);
        return p != null ? p.getSession() : null;
    }

    private static void update(String sql, Object... args) throws Exception {
        try (Connection con = LocalManager.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            if (ps.executeUpdate() == 0) {
                throw new CPanelException("Không tìm thấy tài khoản.");
            }
        }
    }
}
