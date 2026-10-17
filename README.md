# Live Chat Support System (Hệ thống CSKH Trực Tuyến 1-1)

> **Môn học:** Lập trình mạng (CMP180)  
> **Ngăn xếp công nghệ:** Java 17+ (Tương thích JDK 25), TCP Socket Blocking I/O, Đa luồng (Multithreading), Java Object Serialization, JavaFX 21 + FXML, Java Swing (Server Dashboard), SQLite Database, AES-128 Encryption, Maven.

---

## 📌 1. Giới thiệu Đề tài
Hệ thống **Live Chat Support** kết nối Khách hàng và Nhân viên Chăm sóc khách hàng (CSKH) thông qua Server trung gian:
- **Khách hàng (Customer):** Định danh bằng **Số điện thoại/Email**, chọn chủ đề cần hỗ trợ (Mua hàng, Kỹ thuật, Bảo hành...). Hệ thống tự động ghi nhớ hồ sơ, đưa vào hàng chờ (Waiting Queue) hoặc ưu tiên kết nối với nhân viên quen thuộc. Sau khi kết thúc phiên, khách hàng đánh giá chất lượng phục vụ (CSAT 1 - 5 sao ⭐ kèm nhận xét).
- **Nhân viên (Staff):** Đăng nhập an toàn với tài khoản & mật khẩu lưu trong CSDL. Theo dõi danh sách khách hàng đang chờ thời gian thực và chủ động tiếp nhận (1-1 hoặc đa phiên). Hỗ trợ xem lại lịch sử trò chuyện cũ của khách hàng nếu khách từng bị gián đoạn ca trực.
- **Server trung tâm (Admin Dashboard):**
  - **Giám sát thời gian thực:** Quản lý Thread-per-Client, điều phối hàng chờ, định tuyến tin nhắn, tự động khôi phục phiên chat (Session Handover) khi nhân viên mất mạng đột ngột.
  - **Cơ sở dữ liệu SQLite nhúng:** Tự động tạo và lưu trữ thông tin nhân viên, khách hàng, các phiên hỗ trợ và lịch sử tin nhắn.
  - **Giao diện Quản trị viên (Swing):** Thống kê số lượng trực tiếp, quản lý nhân viên (thêm mới, đổi mật khẩu, xóa/khóa) và tra cứu toàn bộ lịch sử các phiên chat trong quá khứ kèm chi tiết từng tin nhắn.

---

## 🛠️ 2. Cấu trúc Dự án
```text
LiveChatSupport-CMP180/
├── pom.xml                                  # Cấu hình Maven dependencies & plugins
├── README.md                                # Tài liệu hướng dẫn dự án
├── run_server.bat                           # Script khởi động nhanh Server Dashboard
├── run_customer.bat                         # Script khởi động nhanh Customer Client
├── run_staff.bat                            # Script khởi động nhanh Staff Client
├── src/
│   ├── main/
│   │   ├── java/com/cmp180/livechat/
│   │   │   ├── common/                      # Dùng chung Client & Server
│   │   │   │   ├── Protocol.java            # Các mã lệnh giao thức TCP
│   │   │   │   ├── Message.java             # Đối tượng tin nhắn tuần tự hóa (Serializable)
│   │   │   │   └── SecurityManager.java     # Mã hóa & giải mã AES-128
│   │   │   ├── server/                      # Server & Quản trị CSDL
│   │   │   │   ├── ServerMain.java          # Khởi tạo ServerSocket & CachedThreadPool
│   │   │   │   ├── ServerGUI.java           # Dashboard Swing 3 Tab quản trị
│   │   │   │   ├── SessionManager.java      # Điều phối hàng chờ & phiên chat
│   │   │   │   ├── ClientHandler.java       # Luồng xử lý từng Client (Runnable)
│   │   │   │   ├── ChatSession.java         # Quản lý phiên trò chuyện
│   │   │   │   └── DatabaseManager.java     # Tầng kết nối & thao tác SQLite Database
│   │   │   └── client/                      # Giao diện & Mạng Client
│   │   │       ├── customer/                # Ứng dụng dành cho Khách hàng
│   │   │       ├── staff/                   # Ứng dụng dành cho Nhân viên CSKH
│   │   │       └── ui/                      # Các tiện ích UI (Theme, Emoji, ServerConfig, Dialogs)
│   │   └── resources/                       # Tài nguyên hình ảnh, FXML, CSS, Emoji
│   └── test/                                # Kiểm thử tự động (Unit Tests)
│       └── java/com/cmp180/livechat/
│           └── DatabaseManagerTest.java     # Test JUnit 5 cho CSDL SQLite & Bảo mật AES
```

---

## 🔐 3. Tài khoản Nhân viên mặc định (Seed Data)

Khi khởi động Server lần đầu tiên, hệ thống sẽ tự động tạo cơ sở dữ liệu `livechat.db` cùng các tài khoản mặc định:

| Tên đăng nhập | Mật khẩu | Họ và Tên | Vai trò |
| :--- | :--- | :--- | :--- |
| **`staff01`** | `123456` | Nguyễn Văn An | Nhân viên CSKH |
| **`staff02`** | `123456` | Trần Thị Bích | Nhân viên CSKH |
| **`admin`** | `admin123` | Quản Trị Viên | Quản trị hệ thống |

*(Bạn cũng có thể thêm/sửa/xóa nhân viên bất kỳ lúc nào trực tiếp trên Tab **"👥 Quản lý Nhân viên"** của Server Dashboard).*

---

## 🚀 4. Hướng dẫn Chạy ứng dụng

### Cách 1: Chạy nhanh bằng file Batch (.bat)
* **Khởi chạy Server Dashboard:** Nhấp đúp vào `run_server.bat` -> Bấm nút **"Khởi Động Server"**.
* **Khởi chạy Nhân viên CSKH:** Nhấp đúp vào `run_staff.bat` -> Đăng nhập bằng `staff01` / `123456`.
* **Khởi chạy Khách hàng:** Nhấp đúp vào `run_customer.bat` -> Nhập SĐT, Họ tên và chọn vấn đề để bắt đầu chat.

### Cách 2: Chạy thông qua Maven CLI
```bash
# 1. Khởi chạy Server Dashboard:
call mvnw.cmd exec:java@run-server

# 2. Khởi chạy Nhân viên CSKH:
call mvnw.cmd exec:java@run-staff

# 3. Khởi chạy Khách hàng:
call mvnw.cmd exec:java@run-customer
```

### Cách 3: Chạy Kiểm thử tự động (Unit Tests)
```bash
call mvnw.cmd test
```

---

## 🌟 5. Các Tính năng Nổi bật
1. **Trải nghiệm Khách hàng (Customer Experience):**
   - Đăng nhập bằng SĐT, tự động ghi nhớ hồ sơ trên thiết bị.
   - Hỗ trợ đánh giá độ hài lòng (CSAT 1 - 5 sao ⭐) và đóng góp ý kiến sau phiên hỗ trợ.
2. **Giao tiếp Đa phương tiện:**
   - Hỗ trợ gửi tập tin đính kèm & hình ảnh (giới hạn dung lượng an toàn).
   - Dán ảnh chụp màn hình trực tiếp từ Clipboard (**Ctrl + V**), có hộp thoại xem trước (Preview) và chú thích ảnh.
   - Thư viện Emoji Picker dạng popup phong phú.
   - Trả lời tin nhắn (Reply) và Thu hồi tin nhắn (Recall) theo thời gian thực.
3. **Độ tin cậy & Chống chịu lỗi (Fault Tolerance):**
   - Vòng lặp tự động kết nối lại (Auto-reconnect) khi rớt mạng.
   - Chuyển giao phiên dang dở (Session Handover): Nếu nhân viên rớt mạng đột ngột, khách hàng được đưa lại đầu hàng chờ kèm cờ "Đang tư vấn dở" và toàn bộ lịch sử chat cũ được chuyển tiếp nguyên vẹn cho nhân viên tiếp theo.
4. **Cơ sở dữ liệu nhúng SQLite:**
   - Dữ liệu lưu trong 1 file duy nhất `livechat.db`, hoàn toàn độc lập, không cần cài đặt MySQL/XAMPP cồng kềnh.
   - Server Dashboard hiển thị thống kê, quản lý nhân viên và tra cứu lịch sử chi tiết.
5. **Giao diện hiện đại:** Hỗ trợ chế độ Sáng / Tối (Dark / Light Mode) và tùy biến màu sắc chủ đạo.
