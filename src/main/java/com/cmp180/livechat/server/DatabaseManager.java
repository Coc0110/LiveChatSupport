package com.cmp180.livechat.server;

import java.io.File;
import java.nio.file.Files;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DatabaseManager {
    private static final String DB_FILE = "livechat.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    private static DatabaseManager instance;
    private final Object writeLock = new Object();

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private DatabaseManager() {
        initDatabase();
    }

    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        try (Statement s = conn.createStatement()) {
            s.execute("PRAGMA busy_timeout = 10000;");
            s.execute("PRAGMA journal_mode = WAL;");
            s.execute("PRAGMA synchronous = NORMAL;");
        } catch (SQLException ignored) {}
        return conn;
    }

    private void initDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Tối ưu hóa SQLite cho môi trường ứng dụng chat đa luồng
            stmt.execute("PRAGMA journal_mode = WAL;");
            stmt.execute("PRAGMA busy_timeout = 10000;");
            stmt.execute("PRAGMA synchronous = NORMAL;");

            // 1. Bảng Nhân viên (Staff)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS staff (
                    staff_id TEXT PRIMARY KEY,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    full_name TEXT NOT NULL,
                    role TEXT DEFAULT 'STAFF',
                    primary_skill TEXT DEFAULT 'Tất cả',
                    is_active INTEGER DEFAULT 1,
                    created_at INTEGER
                );
            """);

            try {
                stmt.execute("ALTER TABLE staff ADD COLUMN primary_skill TEXT DEFAULT 'Tất cả';");
            } catch (SQLException ignored) {}

            // 2. Bảng Khách hàng (Customers)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS customers (
                    customer_id TEXT PRIMARY KEY,
                    phone_or_email TEXT UNIQUE NOT NULL,
                    full_name TEXT NOT NULL,
                    created_at INTEGER
                );
            """);

            try {
                stmt.execute("ALTER TABLE customers ADD COLUMN email TEXT;");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE customers ADD COLUMN tier TEXT DEFAULT 'Thường';");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE customers ADD COLUMN notes TEXT;");
            } catch (SQLException ignored) {}

            // 3. Bảng Phiên hỗ trợ (Chat Sessions / Tickets)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS chat_sessions (
                    session_id TEXT PRIMARY KEY,
                    customer_id TEXT NOT NULL,
                    staff_id TEXT,
                    issue_topic TEXT,
                    status TEXT DEFAULT 'OPEN',
                    rating INTEGER DEFAULT 0,
                    feedback TEXT,
                    created_at INTEGER,
                    closed_at INTEGER,
                    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
                    FOREIGN KEY (staff_id) REFERENCES staff(staff_id)
                );
            """);

            // 4. Bảng Tin nhắn (Messages)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS messages (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    session_id TEXT,
                    message_id TEXT UNIQUE,
                    sender_role TEXT,
                    sender_name TEXT,
                    content TEXT,
                    reply_to_sender TEXT,
                    reply_to_text TEXT,
                    file_name TEXT,
                    file_size INTEGER DEFAULT 0,
                    is_image INTEGER DEFAULT 0,
                    file_path TEXT,
                    is_recalled INTEGER DEFAULT 0,
                    timestamp INTEGER,
                    FOREIGN KEY (session_id) REFERENCES chat_sessions(session_id)
                );
            """);

            try {
                stmt.execute("ALTER TABLE messages ADD COLUMN file_path TEXT;");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE messages ADD COLUMN reply_to_id TEXT;");
            } catch (SQLException ignored) {}

            // 5. Đánh chỉ mục (Indexes) tăng tốc độ truy vấn
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_sessions_customer ON chat_sessions(customer_id);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_sessions_staff ON chat_sessions(staff_id);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_messages_session ON messages(session_id);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_customers_phone ON customers(phone_or_email);");

            // Nạp dữ liệu tài khoản nhân viên mặc định nếu bảng trống
            seedDefaultStaff(conn);

        } catch (SQLException e) {
            System.err.println("Lỗi khởi tạo SQLite Database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Danh sách 45 nhân viên CSKH gán cứng từ danh sách sinh viên (MSSV, Họ và tên)
    public static final String[][] DEFAULT_STUDENT_STAFF = {
        {"2380600001", "Đoàn Văn Ái"},
        {"2380600025", "Trương Thanh Hoài An"},
        {"2380600088", "Võ Đình Nguyễn Minh Anh"},
        {"2380602692", "Hoàng Trung Anh"},
        {"2380600187", "Lê Quang Bắc"},
        {"2380600165", "Nguyễn Trần Lê Bảo"},
        {"2380600183", "Trương Gia Bảo"},
        {"2380600479", "Dương Khánh Đăng"},
        {"2380612598", "Nguyễn Hải Đăng"},
        {"2280600626", "Nguyễn Văn Đạt"},
        {"2380600533", "Lê Việt Đức"},
        {"2380600550", "Trần Quang Đức"},
        {"2188503419", "Trịnh Minh Hoàng"},
        {"2380614932", "Nguyễn Quang Huy"},
        {"2380600951", "Võ Duy Kha"},
        {"2380600981", "Lê Tuấn Khang"},
        {"2380600990", "Nguyễn Hoàng Khang"},
        {"2380601010", "Trần Thanh Khang"},
        {"2380601076", "Phạm Đăng Khoa"},
        {"2380601205", "Đỗ Khắc Levis"},
        {"2380613189", "Huỳnh Phát Lộc"},
        {"2380601285", "Đinh Thành Lợi"},
        {"2380601295", "Phạm Trần Khánh Luân"},
        {"2380601313", "Trần Ngọc Mai"},
        {"2380601358", "Nguyễn Lê Quang Minh"},
        {"2380601439", "Đoàn Trọng Nghĩa"},
        {"2380601470", "Nguyễn Trần Như Ngọc"},
        {"2380601531", "Nguyễn Trọng Nhân"},
        {"2380601666", "Nguyễn Thanh Phi"},
        {"2280602394", "Châu Hoàng Phú"},
        {"2380601745", "Nguyễn Vũ Bảo Phúc"},
        {"2380601783", "Lê Hồng Phương"},
        {"2380601858", "Phan Anh Quốc"},
        {"2380601970", "Vũ Thái Tài"},
        {"2380601994", "Nguyễn Hữu Tân"},
        {"2380602068", "Đỗ Võ Ngọc Thắm"},
        {"2380602160", "Đoàn Minh Thuận"},
        {"2380602216", "Bùi Văn Tiến"},
        {"2380602227", "Mạc Thanh Tiến"},
        {"2380602253", "Trần Đức Tín"},
        {"2380602264", "Đặng Thái Toàn"},
        {"2380608412", "Ngô Đình Hoài Trung"},
        {"2380602510", "Nguyễn Minh Văn"},
        {"2380614342", "Phạm Ngọc Vũ"},
        {"2380602621", "Lê Hoàng Phong Vỹ"}
    };

    private void seedDefaultStaff(Connection conn) {
        // 1. Luôn bảo đảm các tài khoản hệ thống & demo cơ bản tồn tại
        ensureStaff(conn, "admin", "admin123", "Quản Trị Viên", "ADMIN", "Tất cả");
        ensureStaff(conn, "staff01", "123456", "Nguyễn Văn An", "STAFF", "Tư vấn Mua Laptop & Build PC");
        ensureStaff(conn, "staff02", "123456", "Trần Thị Bích", "STAFF", "Hỗ trợ Kỹ thuật & Cài đặt");

        // 2. Gán cứng 45 tài khoản nhân viên CSKH từ danh sách sinh viên
        // Tên đăng nhập là MSSV (hoặc có thể đăng nhập bằng Họ và tên), mật khẩu là MSSV
        int seededCount = 0;
        for (String[] item : DEFAULT_STUDENT_STAFF) {
            String mssv = item[0];
            String fullName = item[1];
            if (ensureStaff(conn, mssv, mssv, fullName, "STAFF", "Tất cả")) {
                seededCount++;
            }
        }
        if (seededCount > 0) {
            System.out.println("-> [DATABASE] Đã nạp thành công " + seededCount + " tài khoản CSKH sinh viên mới vào cơ sở dữ liệu.");
        }
    }

    private boolean ensureStaff(Connection conn, String username, String rawPassword, String fullName, String role, String primarySkill) {
        try {
            // Kiểm tra xem username (MSSV) đã tồn tại chưa
            String sqlUser = "SELECT staff_id FROM staff WHERE username = ?";
            try (PreparedStatement psUser = conn.prepareStatement(sqlUser)) {
                psUser.setString(1, username);
                try (ResultSet rsUser = psUser.executeQuery()) {
                    if (rsUser.next()) {
                        String sId = rsUser.getString("staff_id");
                        // Cập nhật lại mật khẩu và họ tên để đảm bảo mật khẩu luôn đúng với MSSV
                        String updateSql = "UPDATE staff SET password = ?, full_name = ?, role = ?, is_active = 1 WHERE staff_id = ?";
                        try (PreparedStatement ups = conn.prepareStatement(updateSql)) {
                            ups.setString(1, com.cmp180.livechat.common.SecurityManager.hashPassword(rawPassword));
                            ups.setString(2, fullName);
                            ups.setString(3, role);
                            ups.setString(4, sId);
                            ups.executeUpdate();
                        }
                        return false;
                    }
                }
            }

            // Nếu username chưa có, kiểm tra xem có tài khoản cũ trùng full_name không (vd: đặt nhầm username là tên)
            String sqlName = "SELECT staff_id FROM staff WHERE full_name = ?";
            try (PreparedStatement psName = conn.prepareStatement(sqlName)) {
                psName.setString(1, fullName);
                try (ResultSet rsName = psName.executeQuery()) {
                    if (rsName.next()) {
                        String sId = rsName.getString("staff_id");
                        String updateSql = "UPDATE staff SET username = ?, password = ?, role = ?, is_active = 1 WHERE staff_id = ?";
                        try (PreparedStatement ups = conn.prepareStatement(updateSql)) {
                            ups.setString(1, username);
                            ups.setString(2, com.cmp180.livechat.common.SecurityManager.hashPassword(rawPassword));
                            ups.setString(3, role);
                            ups.setString(4, sId);
                            ups.executeUpdate();
                        }
                        return false;
                    }
                }
            }

            // Chưa tồn tại -> Thêm mới hoàn toàn
            insertStaffDirect(conn, username, com.cmp180.livechat.common.SecurityManager.hashPassword(rawPassword), fullName, role, primarySkill);
            return true;
        } catch (SQLException e) {
            System.err.println("Lỗi kiểm tra/nạp staff [" + username + " - " + fullName + "]: " + e.getMessage());
            return false;
        }
    }

    private void insertStaffDirect(Connection conn, String username, String password, String fullName, String role, String primarySkill) throws SQLException {
        String sql = "INSERT INTO staff (staff_id, username, password, full_name, role, primary_skill, is_active, created_at) VALUES (?, ?, ?, ?, ?, ?, 1, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            // Tạo UUID xác định từ username để giữ nguyên staff_id nhất quán khi khởi tạo CSDL
            String staffId = java.util.UUID.nameUUIDFromBytes(("staff:" + username).getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString();
            ps.setString(1, staffId);
            ps.setString(2, username);
            ps.setString(3, password);
            ps.setString(4, fullName);
            ps.setString(5, role);
            ps.setString(6, primarySkill != null ? primarySkill : "Tất cả");
            ps.setLong(7, System.currentTimeMillis());
            ps.executeUpdate();
        }
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ NHÂN VIÊN (STAFF)
    // ==========================================

    public static class StaffRecord {
        public String staffId;
        public String username;
        public String fullName;
        public String role;
        public String primarySkill;
        public int sessionCount;
        public double avgRating;

        public StaffRecord(String staffId, String username, String fullName, String role, String primarySkill, int sessionCount, double avgRating) {
            this.staffId = staffId;
            this.username = username;
            this.fullName = fullName;
            this.role = role;
            this.primarySkill = (primarySkill != null && !primarySkill.isEmpty()) ? primarySkill : "Tất cả";
            this.sessionCount = sessionCount;
            this.avgRating = avgRating;
        }

        public StaffRecord(String staffId, String username, String fullName, String role, int sessionCount, double avgRating) {
            this(staffId, username, fullName, role, "Tất cả", sessionCount, avgRating);
        }
    }

    public StaffRecord authenticateStaff(String username, String password) {
        if (username == null || password == null) return null;
        String cleanUser = username.trim();
        // Cho phép nhân viên đăng nhập bằng MSSV (username) hoặc Họ và tên đầy đủ
        String sql = "SELECT staff_id, username, password, full_name, role, COALESCE(primary_skill, 'Tất cả') AS primary_skill " +
                     "FROM staff WHERE (username = ? OR LOWER(username) = LOWER(?) OR LOWER(full_name) = LOWER(?)) AND is_active = 1";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cleanUser);
            ps.setString(2, cleanUser);
            ps.setString(3, cleanUser);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String storedHash = rs.getString("password");
                    if (com.cmp180.livechat.common.SecurityManager.verifyPassword(password, storedHash)) {
                        // Tự động nâng cấp tài khoản cũ (chưa dùng Salt) lên Salted SHA-256
                        if (storedHash == null || !storedHash.contains(":")) {
                            String newSaltedHash = com.cmp180.livechat.common.SecurityManager.hashPasswordWithSalt(password);
                            updateStaffPassword(rs.getString("staff_id"), newSaltedHash);
                        }
                        return new StaffRecord(
                                rs.getString("staff_id"),
                                rs.getString("username"),
                                rs.getString("full_name"),
                                rs.getString("role"),
                                rs.getString("primary_skill"),
                                0, 0.0
                        );
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi xác thực Staff: " + e.getMessage());
        }
        return null;
    }

    public List<StaffRecord> getAllStaffWithStats() {
        List<StaffRecord> list = new ArrayList<>();
        String sql = """
            SELECT s.staff_id, s.username, s.full_name, s.role, COALESCE(s.primary_skill, 'Tất cả') AS primary_skill,
                   COUNT(cs.session_id) AS total_sessions,
                   COALESCE(AVG(CASE WHEN cs.rating > 0 THEN cs.rating ELSE NULL END), 0.0) AS avg_rating
            FROM staff s
            LEFT JOIN chat_sessions cs ON s.staff_id = cs.staff_id
            WHERE s.is_active = 1
            GROUP BY s.staff_id, s.username, s.full_name, s.role, s.primary_skill
            ORDER BY s.full_name ASC
        """;
        try (Connection conn = getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new StaffRecord(
                        rs.getString("staff_id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getString("role"),
                        rs.getString("primary_skill"),
                        rs.getInt("total_sessions"),
                        rs.getDouble("avg_rating")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách Staff: " + e.getMessage());
        }
        return list;
    }

    public boolean addStaff(String username, String password, String fullName, String role, String primarySkill) {
        synchronized (writeLock) {
            String sql = "INSERT INTO staff (staff_id, username, password, full_name, role, primary_skill, is_active, created_at) VALUES (?, ?, ?, ?, ?, ?, 1, ?)";
            try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, username);
                ps.setString(3, com.cmp180.livechat.common.SecurityManager.hashPassword(password));
                ps.setString(4, fullName);
                ps.setString(5, (role != null && !role.isEmpty()) ? role : "STAFF");
                ps.setString(6, (primarySkill != null && !primarySkill.isEmpty()) ? primarySkill : "Tất cả");
                ps.setLong(7, System.currentTimeMillis());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("Lỗi thêm Staff: " + e.getMessage());
                return false;
            }
        }
    }

    public boolean addStaff(String username, String password, String fullName, String role) {
        return addStaff(username, password, fullName, role, "Tất cả");
    }

    public boolean updateStaffPassword(String staffId, String newPassword) {
        synchronized (writeLock) {
            String sql = "UPDATE staff SET password = ? WHERE staff_id = ?";
            try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                String finalPass = (newPassword != null && (newPassword.contains(":") || (newPassword.length() == 64 && newPassword.matches("^[a-fA-F0-9]{64}$"))))
                        ? newPassword
                        : com.cmp180.livechat.common.SecurityManager.hashPasswordWithSalt(newPassword);
                ps.setString(1, finalPass);
                ps.setString(2, staffId);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("Lỗi đổi mật khẩu: " + e.getMessage());
                return false;
            }
        }
    }

    public boolean verifyStaffPassword(String staffId, String plainPassword) {
        if (staffId == null || plainPassword == null) return false;
        String sql = "SELECT password FROM staff WHERE (staff_id = ? OR username = ?) AND is_active = 1";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, staffId);
            ps.setString(2, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    return com.cmp180.livechat.common.SecurityManager.verifyPassword(plainPassword, storedHash);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi xác thực mật khẩu staff: " + e.getMessage());
        }
        return false;
    }

    public StaffRecord getStaffById(String staffId) {
        if (staffId == null || staffId.trim().isEmpty()) return null;
        List<StaffRecord> list = getAllStaffWithStats();
        for (StaffRecord s : list) {
            if (staffId.equalsIgnoreCase(s.staffId) || staffId.equalsIgnoreCase(s.username)) {
                return s;
            }
        }
        return null;
    }

    public boolean updateStaffCredentials(String staffId, String newUsername, String newPassword) {
        if (staffId == null || staffId.trim().isEmpty()) return false;
        if (newUsername == null || newUsername.trim().isEmpty()) return false;
        String cleanUsername = newUsername.trim();

        synchronized (writeLock) {
            try (Connection conn = getConnection()) {
                // Kiểm tra tên đăng nhập đã được dùng bởi staff khác chưa
                String checkSql = "SELECT COUNT(*) FROM staff WHERE username = ? AND staff_id != ? AND is_active = 1";
                try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                    checkPs.setString(1, cleanUsername);
                    checkPs.setString(2, staffId.trim());
                    try (ResultSet rs = checkPs.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            System.err.println("Tên đăng nhập đã tồn tại: " + cleanUsername);
                            return false;
                        }
                    }
                }

                if (newPassword != null && !newPassword.trim().isEmpty()) {
                    String finalPass = (newPassword.contains(":") || (newPassword.length() == 64 && newPassword.matches("^[a-fA-F0-9]{64}$")))
                            ? newPassword.trim()
                            : com.cmp180.livechat.common.SecurityManager.hashPasswordWithSalt(newPassword.trim());
                    String updateSql = "UPDATE staff SET username = ?, password = ? WHERE staff_id = ?";
                    try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                        updatePs.setString(1, cleanUsername);
                        updatePs.setString(2, finalPass);
                        updatePs.setString(3, staffId.trim());
                        return updatePs.executeUpdate() > 0;
                    }
                } else {
                    String updateSql = "UPDATE staff SET username = ? WHERE staff_id = ?";
                    try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                        updatePs.setString(1, cleanUsername);
                        updatePs.setString(2, staffId.trim());
                        return updatePs.executeUpdate() > 0;
                    }
                }
            } catch (SQLException e) {
                System.err.println("Lỗi cập nhật thông tin tài khoản nhân viên: " + e.getMessage());
                return false;
            }
        }
    }

    public boolean deleteStaff(String staffId) {
        synchronized (writeLock) {
            String sql = "UPDATE staff SET is_active = 0 WHERE staff_id = ?";
            try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, staffId);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("Lỗi xoá staff: " + e.getMessage());
                return false;
            }
        }
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ KHÁCH HÀNG (CUSTOMERS)
    // ==========================================

    public static class CustomerRecord {
        public String customerId;
        public String phoneOrEmail;
        public String fullName;
        public String email;
        public String tier;
        public String notes;
        public long createdAt;

        public CustomerRecord(String customerId, String phoneOrEmail, String fullName, long createdAt) {
            this(customerId, phoneOrEmail, fullName, null, "Thường", null, createdAt);
        }

        public CustomerRecord(String customerId, String phoneOrEmail, String fullName, String email, String tier, String notes, long createdAt) {
            this.customerId = customerId;
            this.phoneOrEmail = phoneOrEmail;
            this.fullName = fullName;
            this.email = email != null ? email : "";
            this.tier = (tier != null && !tier.trim().isEmpty()) ? tier.trim() : "Thường";
            this.notes = notes != null ? notes : "";
            this.createdAt = createdAt;
        }
    }

    public CustomerRecord findCustomerByPhone(String phoneOrEmail) {
        if (phoneOrEmail == null || phoneOrEmail.trim().isEmpty()) return null;
        String sql = "SELECT customer_id, phone_or_email, full_name, COALESCE(email, '') AS email, COALESCE(tier, 'Thường') AS tier, COALESCE(notes, '') AS notes, created_at FROM customers WHERE phone_or_email = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phoneOrEmail.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new CustomerRecord(
                            rs.getString("customer_id"),
                            rs.getString("phone_or_email"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("tier"),
                            rs.getString("notes"),
                            rs.getLong("created_at")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm khách hàng: " + e.getMessage());
        }
        return null;
    }

    public CustomerRecord getCustomerById(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) return null;
        String sql = "SELECT customer_id, phone_or_email, full_name, COALESCE(email, '') AS email, COALESCE(tier, 'Thường') AS tier, COALESCE(notes, '') AS notes, created_at FROM customers WHERE customer_id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new CustomerRecord(
                            rs.getString("customer_id"),
                            rs.getString("phone_or_email"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("tier"),
                            rs.getString("notes"),
                            rs.getLong("created_at")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm khách hàng theo ID: " + e.getMessage());
        }
        return null;
    }

    public CustomerRecord findCustomerByName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return null;
        String cleanName = fullName.replaceAll("\\s*\\([^)]*\\)$", "").trim();
        String sql = "SELECT customer_id, phone_or_email, full_name, COALESCE(email, '') AS email, COALESCE(tier, 'Thường') AS tier, COALESCE(notes, '') AS notes, created_at FROM customers WHERE LOWER(full_name) = LOWER(?) LIMIT 1";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cleanName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new CustomerRecord(
                            rs.getString("customer_id"),
                            rs.getString("phone_or_email"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("tier"),
                            rs.getString("notes"),
                            rs.getLong("created_at")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm khách hàng theo tên: " + e.getMessage());
        }
        return null;
    }

    public CustomerRecord findCustomerByPhoneOrName(String query) {
        if (query == null || query.trim().isEmpty()) return null;
        String clean = query.trim();
        CustomerRecord c = findCustomerByPhone(clean);
        if (c != null) return c;
        c = getCustomerById(clean);
        if (c != null) return c;
        return findCustomerByName(clean);
    }

    public boolean updateCustomerProfile(String customerId, String fullName, String phoneOrEmail, String email, String tier, String notes) {
        if (customerId == null || customerId.trim().isEmpty()) return false;
        synchronized (writeLock) {
            String sql = "UPDATE customers SET full_name = ?, phone_or_email = ?, email = ?, tier = ?, notes = ? WHERE customer_id = ?";
            try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, fullName != null ? fullName.trim() : "");
                ps.setString(2, phoneOrEmail != null ? phoneOrEmail.trim() : "");
                ps.setString(3, email != null ? email.trim() : "");
                ps.setString(4, (tier != null && !tier.trim().isEmpty()) ? tier.trim() : "Thường");
                ps.setString(5, notes != null ? notes.trim() : "");
                ps.setString(6, customerId.trim());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("Lỗi cập nhật hồ sơ khách hàng: " + e.getMessage());
                return false;
            }
        }
    }

    public CustomerRecord registerOrUpdateCustomer(String phoneOrEmail, String fullName) {
        if (phoneOrEmail == null || phoneOrEmail.trim().isEmpty()) return null;
        synchronized (writeLock) {
            String cleanPhone = phoneOrEmail.trim();
            CustomerRecord existing = findCustomerByPhone(cleanPhone);
            if (existing != null) {
                // Cập nhật tên nếu có truyền tên mới
                if (fullName != null && !fullName.trim().isEmpty() && !fullName.trim().equals(existing.fullName)) {
                    String updateSql = "UPDATE customers SET full_name = ? WHERE customer_id = ?";
                    try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(updateSql)) {
                        ps.setString(1, fullName.trim());
                        ps.setString(2, existing.customerId);
                        ps.executeUpdate();
                        existing.fullName = fullName.trim();
                    } catch (SQLException ignored) {}
                }
                return existing;
            }

            // Tạo mới
            String newId = UUID.randomUUID().toString();
            long now = System.currentTimeMillis();
            String insertSql = "INSERT INTO customers (customer_id, phone_or_email, full_name, created_at) VALUES (?, ?, ?, ?)";
            try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, newId);
                ps.setString(2, cleanPhone);
                ps.setString(3, (fullName != null && !fullName.trim().isEmpty()) ? fullName.trim() : "Khách hàng");
                ps.setLong(4, now);
                ps.executeUpdate();
                return new CustomerRecord(newId, cleanPhone, (fullName != null) ? fullName.trim() : "Khách hàng", now);
            } catch (SQLException e) {
                System.err.println("Lỗi đăng ký khách hàng mới: " + e.getMessage());
                return null;
            }
        }
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ PHIÊN HỖ TRỢ (CHAT SESSIONS)
    // ==========================================

    public static class SessionRecord {
        public String sessionId;
        public String customerId;
        public String customerName;
        public String customerPhone;
        public String staffId;
        public String staffName;
        public String issueTopic;
        public String status;
        public int rating;
        public String feedback;
        public long createdAt;
        public long closedAt;

        public SessionRecord(String sessionId, String customerId, String customerName, String customerPhone,
                             String staffId, String staffName, String issueTopic, String status,
                             int rating, String feedback, long createdAt, long closedAt) {
            this.sessionId = sessionId;
            this.customerId = customerId;
            this.customerName = customerName;
            this.customerPhone = customerPhone;
            this.staffId = staffId;
            this.staffName = staffName;
            this.issueTopic = issueTopic;
            this.status = status;
            this.rating = rating;
            this.feedback = feedback;
            this.createdAt = createdAt;
            this.closedAt = closedAt;
        }
    }

    public void createSession(String sessionId, String customerId, String staffId, String issueTopic) {
        synchronized (writeLock) {
            String sql = "INSERT INTO chat_sessions (session_id, customer_id, staff_id, issue_topic, status, rating, created_at) VALUES (?, ?, ?, ?, 'OPEN', 0, ?)";
            try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, sessionId);
                ps.setString(2, customerId);
                ps.setString(3, staffId);
                ps.setString(4, (issueTopic != null) ? issueTopic : "Tư vấn chung");
                ps.setLong(5, System.currentTimeMillis());
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Lỗi tạo session trong DB: " + e.getMessage());
            }
        }
    }

    public void closeSession(String sessionId, int rating, String feedback) {
        synchronized (writeLock) {
            String sql = "UPDATE chat_sessions SET status = 'CLOSED', rating = ?, feedback = ?, closed_at = ? WHERE session_id = ?";
            try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, rating);
                ps.setString(2, feedback);
                ps.setLong(3, System.currentTimeMillis());
                ps.setString(4, sessionId);
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Lỗi đóng session trong DB: " + e.getMessage());
            }
        }
    }

    public List<SessionRecord> getAllSessions() {
        List<SessionRecord> list = new ArrayList<>();
        String sql = """
            SELECT cs.session_id, cs.customer_id, c.full_name AS cust_name, c.phone_or_email AS cust_phone,
                   cs.staff_id, COALESCE(s.full_name, 'Chưa gán') AS staff_name,
                   cs.issue_topic, cs.status, cs.rating, cs.feedback, cs.created_at, cs.closed_at
            FROM chat_sessions cs
            LEFT JOIN customers c ON cs.customer_id = c.customer_id
            LEFT JOIN staff s ON cs.staff_id = s.staff_id
            ORDER BY cs.created_at DESC
        """;
        try (Connection conn = getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new SessionRecord(
                        rs.getString("session_id"),
                        rs.getString("customer_id"),
                        rs.getString("cust_name"),
                        rs.getString("cust_phone"),
                        rs.getString("staff_id"),
                        rs.getString("staff_name"),
                        rs.getString("issue_topic"),
                        rs.getString("status"),
                        rs.getInt("rating"),
                        rs.getString("feedback"),
                        rs.getLong("created_at"),
                        rs.getLong("closed_at")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách Sessions: " + e.getMessage());
        }
        return list;
    }

    public List<SessionRecord> getSessionsByCustomer(String customerId) {
        List<SessionRecord> list = new ArrayList<>();
        String sql = """
            SELECT cs.session_id, cs.customer_id, c.full_name AS cust_name, c.phone_or_email AS cust_phone,
                   cs.staff_id, COALESCE(s.full_name, 'Chưa gán') AS staff_name,
                   cs.issue_topic, cs.status, cs.rating, cs.feedback, cs.created_at, cs.closed_at
            FROM chat_sessions cs
            LEFT JOIN customers c ON cs.customer_id = c.customer_id
            LEFT JOIN staff s ON cs.staff_id = s.staff_id
            WHERE cs.customer_id = ?
            ORDER BY cs.created_at DESC
        """;
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new SessionRecord(
                            rs.getString("session_id"),
                            rs.getString("customer_id"),
                            rs.getString("cust_name"),
                            rs.getString("cust_phone"),
                            rs.getString("staff_id"),
                            rs.getString("staff_name"),
                            rs.getString("issue_topic"),
                            rs.getString("status"),
                            rs.getInt("rating"),
                            rs.getString("feedback"),
                            rs.getLong("created_at"),
                            rs.getLong("closed_at")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy Sessions theo Customer: " + e.getMessage());
        }
        return list;
    }

    /**
     * Tìm nhân viên CSKH từng hỗ trợ khách hàng gần đây nhất và được đánh giá tốt
     */
    public String getPreferredStaffIdForCustomer(String customerId) {
        String sql = """
            SELECT staff_id FROM chat_sessions 
            WHERE customer_id = ? AND staff_id IS NOT NULL AND status = 'CLOSED' 
            ORDER BY rating DESC, closed_at DESC LIMIT 1
        """;
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("staff_id");
                }
            }
        } catch (SQLException ignored) {}
        return null;
    }

    public SessionRecord getSessionById(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) return null;
        String sql = """
            SELECT cs.session_id, cs.customer_id, c.full_name AS cust_name, c.phone_or_email AS cust_phone,
                   cs.staff_id, COALESCE(s.full_name, 'Chưa gán') AS staff_name,
                   cs.issue_topic, cs.status, cs.rating, cs.feedback, cs.created_at, cs.closed_at
            FROM chat_sessions cs
            LEFT JOIN customers c ON cs.customer_id = c.customer_id
            LEFT JOIN staff s ON cs.staff_id = s.staff_id
            WHERE cs.session_id = ?
        """;
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sessionId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new SessionRecord(
                            rs.getString("session_id"),
                            rs.getString("customer_id"),
                            rs.getString("cust_name"),
                            rs.getString("cust_phone"),
                            rs.getString("staff_id"),
                            rs.getString("staff_name"),
                            rs.getString("issue_topic"),
                            rs.getString("status"),
                            rs.getInt("rating"),
                            rs.getString("feedback"),
                            rs.getLong("created_at"),
                            rs.getLong("closed_at")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm session theo ID: " + e.getMessage());
        }
        return null;
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ TIN NHẮN (MESSAGES)
    // ==========================================

    public static class MessageRecord {
        public String messageId;
        public String sessionId;
        public String senderRole;
        public String senderName;
        public String content;
        public String replyToSender;
        public String replyToText;
        public String replyToId;
        public String fileName;
        public long fileSize;
        public boolean isImage;
        public boolean isRecalled;
        public long timestamp;
        public String filePath;
        public byte[] fileData;

        public MessageRecord(String messageId, String sessionId, String senderRole, String senderName,
                             String content, String replyToSender, String replyToText,
                             String fileName, long fileSize, boolean isImage, boolean isRecalled, long timestamp) {
            this(messageId, sessionId, senderRole, senderName, content, replyToSender, replyToText, null,
                 fileName, fileSize, isImage, isRecalled, timestamp, null, null);
        }

        public MessageRecord(String messageId, String sessionId, String senderRole, String senderName,
                             String content, String replyToSender, String replyToText, String replyToId,
                             String fileName, long fileSize, boolean isImage, boolean isRecalled, long timestamp,
                             String filePath, byte[] fileData) {
            this.messageId = messageId;
            this.sessionId = sessionId;
            this.senderRole = senderRole;
            this.senderName = senderName;
            this.content = content;
            this.replyToSender = replyToSender;
            this.replyToText = replyToText;
            this.replyToId = replyToId;
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.isImage = isImage;
            this.isRecalled = isRecalled;
            this.timestamp = timestamp;
            this.filePath = filePath;
            this.fileData = fileData;
        }
    }

    public void saveMessage(String sessionId, String messageId, String senderRole, String senderName,
                            String content, String replyToSender, String replyToText,
                            String fileName, long fileSize, boolean isImage, long timestamp) {
        saveMessage(sessionId, messageId, senderRole, senderName, content, replyToSender, replyToText, null,
                    fileName, fileSize, isImage, timestamp, null);
    }

    public void saveMessage(String sessionId, String messageId, String senderRole, String senderName,
                            String content, String replyToSender, String replyToText,
                            String fileName, long fileSize, boolean isImage, long timestamp, byte[] fileData) {
        saveMessage(sessionId, messageId, senderRole, senderName, content, replyToSender, replyToText, null,
                    fileName, fileSize, isImage, timestamp, fileData);
    }

    public void saveMessage(String sessionId, String messageId, String senderRole, String senderName,
                            String content, String replyToSender, String replyToText, String replyToId,
                            String fileName, long fileSize, boolean isImage, long timestamp, byte[] fileData) {
        synchronized (writeLock) {
            String savedFilePath = null;
            if (fileData != null && fileData.length > 0 && fileName != null && !fileName.trim().isEmpty()) {
                try {
                    File dir = new File("server_storage/files");
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    String safeName = fileName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
                    String targetFileName = (messageId != null ? messageId : UUID.randomUUID().toString()) + "_" + safeName;
                    File targetFile = new File(dir, targetFileName);
                    Files.write(targetFile.toPath(), fileData);
                    savedFilePath = targetFile.getPath();
                } catch (Exception e) {
                    System.err.println("Lỗi lưu file đính kèm vào ổ đĩa: " + e.getMessage());
                }
            }

            String sql = """
                INSERT INTO messages (session_id, message_id, sender_role, sender_name, content, reply_to_sender, reply_to_text, reply_to_id, file_name, file_size, is_image, file_path, is_recalled, timestamp)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?)
                ON CONFLICT(message_id) DO UPDATE SET
                    session_id = excluded.session_id,
                    sender_role = excluded.sender_role,
                    sender_name = excluded.sender_name,
                    content = excluded.content,
                    reply_to_sender = excluded.reply_to_sender,
                    reply_to_text = excluded.reply_to_text,
                    reply_to_id = excluded.reply_to_id,
                    file_name = excluded.file_name,
                    file_size = excluded.file_size,
                    is_image = excluded.is_image,
                    file_path = excluded.file_path,
                    timestamp = excluded.timestamp
            """;
            try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, sessionId);
                ps.setString(2, messageId);
                ps.setString(3, senderRole);
                ps.setString(4, senderName);
                ps.setString(5, content);
                ps.setString(6, replyToSender);
                ps.setString(7, replyToText);
                ps.setString(8, replyToId);
                ps.setString(9, fileName);
                ps.setLong(10, fileSize);
                ps.setInt(11, isImage ? 1 : 0);
                ps.setString(12, savedFilePath);
                ps.setLong(13, timestamp > 0 ? timestamp : System.currentTimeMillis());
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Lỗi lưu Message vào DB: " + e.getMessage());
            }
        }
    }

    public void recallMessage(String messageId) {
        synchronized (writeLock) {
            try (Connection conn = getConnection()) {
                String sql1 = "UPDATE messages SET is_recalled = 1 WHERE message_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql1)) {
                    ps.setString(1, messageId);
                    ps.executeUpdate();
                }
                String sql2 = "UPDATE messages SET reply_to_text = 'Tin nhắn đã được thu hồi' WHERE reply_to_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql2)) {
                    ps.setString(1, messageId);
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                System.err.println("Lỗi recall message trong DB: " + e.getMessage());
            }
        }
    }

    public List<MessageRecord> getMessagesBySession(String sessionId) {
        List<MessageRecord> list = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE session_id = ? ORDER BY timestamp ASC";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String filePath = null;
                    try {
                        filePath = rs.getString("file_path");
                    } catch (SQLException ignored) {}

                    byte[] fileData = null;
                    if (filePath != null && !filePath.trim().isEmpty()) {
                        File f = new File(filePath);
                        if (f.exists() && f.isFile()) {
                            try {
                                fileData = Files.readAllBytes(f.toPath());
                            } catch (Exception ex) {
                                System.err.println("Không thể đọc tệp lưu trữ: " + filePath + " (" + ex.getMessage() + ")");
                            }
                        }
                    }

                    String replyToId = null;
                    try {
                        replyToId = rs.getString("reply_to_id");
                    } catch (SQLException ignored) {}

                    list.add(new MessageRecord(
                            rs.getString("message_id"),
                            rs.getString("session_id"),
                            rs.getString("sender_role"),
                            rs.getString("sender_name"),
                            rs.getString("content"),
                            rs.getString("reply_to_sender"),
                            rs.getString("reply_to_text"),
                            replyToId,
                            rs.getString("file_name"),
                            rs.getLong("file_size"),
                            rs.getInt("is_image") == 1,
                            rs.getInt("is_recalled") == 1,
                            rs.getLong("timestamp"),
                            filePath,
                            fileData
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy messages theo session: " + e.getMessage());
        }
        return list;
    }

    public List<MessageRecord> getAllMessagesByCustomer(String customerId) {
        List<MessageRecord> list = new ArrayList<>();
        if (customerId == null || customerId.trim().isEmpty()) return list;
        String sql = """
            SELECT m.* FROM messages m
            JOIN chat_sessions cs ON m.session_id = cs.session_id
            WHERE cs.customer_id = ?
            ORDER BY m.timestamp ASC
        """;
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String filePath = null;
                    try {
                        filePath = rs.getString("file_path");
                    } catch (SQLException ignored) {}

                    byte[] fileData = null;
                    if (filePath != null && !filePath.trim().isEmpty()) {
                        File f = new File(filePath);
                        if (f.exists() && f.isFile()) {
                            try {
                                fileData = Files.readAllBytes(f.toPath());
                            } catch (Exception ex) {
                                System.err.println("Không thể đọc tệp lưu trữ: " + filePath + " (" + ex.getMessage() + ")");
                            }
                        }
                    }

                    String replyToId = null;
                    try {
                        replyToId = rs.getString("reply_to_id");
                    } catch (SQLException ignored) {}

                    list.add(new MessageRecord(
                            rs.getString("message_id"),
                            rs.getString("session_id"),
                            rs.getString("sender_role"),
                            rs.getString("sender_name"),
                            rs.getString("content"),
                            rs.getString("reply_to_sender"),
                            rs.getString("reply_to_text"),
                            replyToId,
                            rs.getString("file_name"),
                            rs.getLong("file_size"),
                            rs.getInt("is_image") == 1,
                            rs.getInt("is_recalled") == 1,
                            rs.getLong("timestamp"),
                            filePath,
                            fileData
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy toàn bộ lịch sử tin nhắn của khách hàng: " + e.getMessage());
        }
        return list;
    }

    public static boolean isChainedTopic(String topic) {
        if (topic == null) return false;
        String lower = topic.trim().toLowerCase();
        return lower.startsWith("hỏi tiếp:") || lower.startsWith("re:");
    }

    public static String normalizeTopic(String topic) {
        if (topic == null) return "";
        String normalized = topic.trim();
        while (true) {
            String lower = normalized.toLowerCase();
            if (lower.startsWith("hỏi tiếp:")) {
                normalized = normalized.substring("hỏi tiếp:".length()).trim();
            } else if (lower.startsWith("re:")) {
                normalized = normalized.substring("re:".length()).trim();
            } else {
                break;
            }
        }
        return normalized;
    }

    public List<MessageRecord> getMessagesBySessionIds(List<String> sessionIds) {
        List<MessageRecord> list = new ArrayList<>();
        if (sessionIds == null || sessionIds.isEmpty()) return list;

        String placeholders = String.join(",", Collections.nCopies(sessionIds.size(), "?"));
        String sql = "SELECT * FROM messages WHERE session_id IN (" + placeholders + ") ORDER BY timestamp ASC";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < sessionIds.size(); i++) {
                ps.setString(i + 1, sessionIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String filePath = null;
                    try {
                        filePath = rs.getString("file_path");
                    } catch (SQLException ignored) {}

                    byte[] fileData = null;
                    if (filePath != null && !filePath.trim().isEmpty()) {
                        File f = new File(filePath);
                        if (f.exists() && f.isFile()) {
                            try {
                                fileData = Files.readAllBytes(f.toPath());
                            } catch (Exception ex) {
                                System.err.println("Không thể đọc tệp lưu trữ: " + filePath + " (" + ex.getMessage() + ")");
                            }
                        }
                    }

                    String replyToId = null;
                    try {
                        replyToId = rs.getString("reply_to_id");
                    } catch (SQLException ignored) {}

                    list.add(new MessageRecord(
                            rs.getString("message_id"),
                            rs.getString("session_id"),
                            rs.getString("sender_role"),
                            rs.getString("sender_name"),
                            rs.getString("content"),
                            rs.getString("reply_to_sender"),
                            rs.getString("reply_to_text"),
                            replyToId,
                            rs.getString("file_name"),
                            rs.getLong("file_size"),
                            rs.getInt("is_image") == 1,
                            rs.getInt("is_recalled") == 1,
                            rs.getLong("timestamp"),
                            filePath,
                            fileData
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy messages theo danh sách session: " + e.getMessage());
        }
        return list;
    }

    /**
     * Lấy danh sách tin nhắn cho phiên hỗ trợ được mở lại.
     * Chỉ nạp đúng phiên đó (và các phiên liền trước cùng chuỗi đề tài nếu phiên được mở lại là phiên ghép "Hỏi tiếp...").
     * Tuyệt đối không nạp tin nhắn từ các chủ đề khác hoặc các phiên diễn ra sau phiên mục tiêu.
     */
    public List<MessageRecord> getMessagesForReopenSession(String targetSessionId) {
        List<MessageRecord> list = new ArrayList<>();
        if (targetSessionId == null || targetSessionId.trim().isEmpty()) return list;

        SessionRecord target = getSessionById(targetSessionId);
        if (target == null) {
            return getMessagesBySession(targetSessionId);
        }

        List<String> chainSessionIds = new ArrayList<>();
        chainSessionIds.add(target.sessionId);

        // Nếu target là một phiên ghép (bắt đầu bằng "Hỏi tiếp:" hoặc "Re:"),
        // truy ngược các phiên trước đó cùng chủ đề của khách hàng này
        if (target.customerId != null && isChainedTopic(target.issueTopic)) {
            String normTargetTopic = normalizeTopic(target.issueTopic);
            SessionRecord curr = target;

            while (curr != null && isChainedTopic(curr.issueTopic)) {
                String sql = """
                    SELECT session_id, customer_id, issue_topic, created_at
                    FROM chat_sessions
                    WHERE customer_id = ? AND created_at < ?
                    ORDER BY created_at DESC
                """;
                SessionRecord prevMatch = null;
                try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, curr.customerId);
                    ps.setLong(2, curr.createdAt);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String prevTopic = rs.getString("issue_topic");
                            if (normalizeTopic(prevTopic).equalsIgnoreCase(normTargetTopic)) {
                                prevMatch = new SessionRecord(
                                        rs.getString("session_id"),
                                        rs.getString("customer_id"),
                                        null, null, null, null,
                                        prevTopic, null, 0, null,
                                        rs.getLong("created_at"), 0
                                );
                                break;
                            }
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Lỗi truy ngược phiên trong chuỗi: " + e.getMessage());
                    break;
                }

                if (prevMatch != null && !chainSessionIds.contains(prevMatch.sessionId)) {
                    chainSessionIds.add(0, prevMatch.sessionId);
                    curr = prevMatch;
                } else {
                    break;
                }
            }
        }

        return getMessagesBySessionIds(chainSessionIds);
    }

    /**
     * Lấy danh sách tin nhắn cho chuỗi chủ đề của khách hàng (khi khách kết nối lại hoặc mở tiếp chủ đề "Hỏi tiếp...").
     * Nếu không phải chủ đề chuỗi ("Hỏi tiếp:"), trả về danh sách rỗng để phiên mới bắt đầu trong sạch.
     */
    public List<MessageRecord> getMessagesForCustomerTopicChain(String customerId, String topic) {
        List<MessageRecord> list = new ArrayList<>();
        if (customerId == null || customerId.trim().isEmpty() || topic == null || topic.trim().isEmpty()) {
            return list;
        }
        if (!isChainedTopic(topic)) {
            return list;
        }
        String normTopic = normalizeTopic(topic);
        String sql = """
            SELECT session_id, issue_topic, created_at
            FROM chat_sessions
            WHERE customer_id = ?
            ORDER BY created_at DESC
        """;
        String latestMatchSessionId = null;
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String pastTopic = rs.getString("issue_topic");
                    if (normalizeTopic(pastTopic).equalsIgnoreCase(normTopic)) {
                        latestMatchSessionId = rs.getString("session_id");
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm phiên gần nhất cho chuỗi đề tài: " + e.getMessage());
        }

        if (latestMatchSessionId != null) {
            return getMessagesForReopenSession(latestMatchSessionId);
        }
        return list;
    }

    // ==========================================
    // CÁC HÀM TIỆN ÍCH DỮ LIỆU MẪU (DEMO)
    // ==========================================

    public void resetAndSeedSampleData() {
        synchronized (writeLock) {
            try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
                st.execute("DELETE FROM messages;");
                st.execute("DELETE FROM chat_sessions;");
                st.execute("DELETE FROM customers;");
                st.execute("DELETE FROM staff;");

                // Nạp lại staff
                seedDefaultStaff(conn);

                // Nạp 2 khách hàng mẫu
                CustomerRecord c1 = registerOrUpdateCustomer("0987654321", "Nguyễn Thu Hà");
                CustomerRecord c2 = registerOrUpdateCustomer("0912345678", "Lê Hoàng Long");

                // Lấy ID của staff01
                StaffRecord s1 = authenticateStaff("staff01", "123456");
                if (s1 != null && c1 != null && c2 != null) {
                    // Tạo 1 phiên cũ đã giải quyết có rating 5 sao
                    String sessId1 = UUID.randomUUID().toString();
                    createSession(sessId1, c1.customerId, s1.staffId, "Bảo hành & Đổi trả");
                    saveMessage(sessId1, UUID.randomUUID().toString(), "CUSTOMER", c1.fullName, "Chào shop, mình muốn hỏi về chính sách bảo hành tai nghe.", null, null, null, 0, false, System.currentTimeMillis() - 3600000);
                    saveMessage(sessId1, UUID.randomUUID().toString(), "STAFF", s1.fullName, "Dạ em chào chị Hà, bên em hỗ trợ bảo hành 12 tháng 1 đổi 1 trong 30 ngày ạ!", null, null, null, 0, false, System.currentTimeMillis() - 3500000);
                    closeSession(sessId1, 5, "Nhân viên hỗ trợ rất nhiệt tình!");

                    // Tạo 1 phiên cũ có rating 4 sao
                    String sessId2 = UUID.randomUUID().toString();
                    createSession(sessId2, c2.customerId, s1.staffId, "Tư vấn Mua hàng");
                    saveMessage(sessId2, UUID.randomUUID().toString(), "CUSTOMER", c2.fullName, "Sản phẩm bàn phím cơ còn hàng màu đen không shop?", null, null, null, 0, false, System.currentTimeMillis() - 1800000);
                    saveMessage(sessId2, UUID.randomUUID().toString(), "STAFF", s1.fullName, "Dạ màu đen hiện còn 3 chiếc tại kho Hà Nội anh nhé.", null, null, null, 0, false, System.currentTimeMillis() - 1700000);
                    closeSession(sessId2, 4, "Tư vấn nhanh chóng.");
                }
                System.out.println("-> [DATABASE] Đã thiết lập lại dữ liệu mẫu thành công.");
            } catch (SQLException e) {
                System.err.println("Lỗi reset dữ liệu mẫu: " + e.getMessage());
            }
        }
    }
}
