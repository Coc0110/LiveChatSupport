package com.cmp180.livechat.server;

import java.io.File;
import java.nio.file.Files;
import java.sql.*;
import java.util.ArrayList;
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

    private void seedDefaultStaff(Connection conn) {
        String countSql = "SELECT COUNT(*) FROM staff";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(countSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                // Thêm 3 tài khoản mẫu với mật khẩu băm SHA-256 kèm chuyên ngành
                insertStaffDirect(conn, "staff01", com.cmp180.livechat.common.SecurityManager.hashPassword("123456"), "Nguyễn Văn An", "STAFF", "Tư vấn Mua Laptop & Build PC");
                insertStaffDirect(conn, "staff02", com.cmp180.livechat.common.SecurityManager.hashPassword("123456"), "Trần Thị Bích", "STAFF", "Hỗ trợ Kỹ thuật & Cài đặt");
                insertStaffDirect(conn, "admin", com.cmp180.livechat.common.SecurityManager.hashPassword("admin123"), "Quản Trị Viên", "ADMIN", "Tất cả");
                System.out.println("-> [DATABASE] Đã nạp 3 tài khoản CSKH mặc định (staff01, staff02, admin).");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi seed dữ liệu staff: " + e.getMessage());
        }
    }

    private void insertStaffDirect(Connection conn, String username, String password, String fullName, String role, String primarySkill) throws SQLException {
        String sql = "INSERT INTO staff (staff_id, username, password, full_name, role, primary_skill, is_active, created_at) VALUES (?, ?, ?, ?, ?, ?, 1, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, UUID.randomUUID().toString());
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
        String sql = "SELECT staff_id, username, password, full_name, role, COALESCE(primary_skill, 'Tất cả') AS primary_skill FROM staff WHERE username = ? AND is_active = 1";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
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
