# Live Chat Support System (CSKH Trực Tuyến 1-1)
> **Môn học:** Lập trình mạng (CMP180)  
> **Công nghệ:** Java TCP Sockets + Multithreading (Bài 3, 5, 6)

---

## 📌 1. Giới thiệu Đề tài
Hệ thống **Live Chat Support** kết nối Khách hàng và Nhân viên CSKH thông qua Server trung gian.
- **Khách hàng (Customer):** Gửi yêu cầu hỗ trợ, được xếp vào hàng chờ (Waiting Queue) nếu chưa có CSKH rảnh.
- **Nhân viên (Staff):** Đăng nhập vào hệ thống ở trạng thái Sẵn sàng (Available). Khi có khách hàng chờ, Server sẽ tự động ghép phiên (Pairing) 1-1.
- **Server:** Quản lý kết nối Đa luồng (Multithreading), chuyển tiếp tin nhắn 1-1 giữa Khách hàng và CSKH, điều phối hàng chờ và ghép phiên.

---

## 🛠️ 2. Cấu trúc Dự án
```text
LiveChatSupport-CMP180/
├── README.md
├── .gitignore
└── src/
    └── com/
        └── cmp180/
            └── livechat/
                ├── common/
                │   ├── Protocol.java      # Định nghĩa định dạng gói tin ứng dụng (Bài 6)
                │   └── ChatSession.java   # Quản lý phiên trò chuyện 1-1
                ├── server/
                │   ├── ServerMain.java    # Khởi tạo ServerSocket & điều phối kết nối (Bài 3 & 5)
                │   └── ClientHandler.java # Luồng xử lý từng Client (Bài 5)
                └── client/
                    ├── CustomerClient.java # Client dành cho Khách hàng
                    └── StaffClient.java    # Client dành cho Nhân viên CSKH
```

---

## 📡 3. Giao thức ứng dụng (Protocol Specification - Bài 6)

Các gói tin được truyền qua TCP Socket dạng chuỗi phân cách bởi dấu `|`:

| Lệnh | Định dạng gửi | Ý nghĩa |
| :--- | :--- | :--- |
| **Đăng ký** | `CONNECT\|ROLE\|NAME` | Khai báo vai trò (`CUSTOMER` hoặc `STAFF`) và tên hiển thị |
| **Hàng chờ** | `WAITING\|MESSAGE` | Server thông báo trạng thái chờ ghép phiên |
| **Ghép phiên** | `PAIRED\|PARTNER_NAME` | Server thông báo đã kết nối thành công 1-1 |
| **Tin nhắn** | `MSG\|CONTENT` | Gửi nội dung tin nhắn 1-1 |
| **Kết thúc** | `END\|REASON` | Yêu cầu hoặc thông báo kết thúc phiên chat |

---

## 🚀 4. Hướng dẫn Chạy ứng dụng

### Cách 1: Biên dịch và chạy bằng Command Line (Terminal)

1. **Biên dịch toàn bộ mã nguồn:**
   ```bash
   javac -d bin -encoding UTF-8 src/com/cmp180/livechat/common/*.java src/com/cmp180/livechat/server/*.java src/com/cmp180/livechat/client/*.java
   ```

2. **Khởi chạy Server:**
   ```bash
   java -cp bin com.cmp180.livechat.server.ServerMain
   ```

3. **Khởi chạy Khách hàng:**
   ```bash
   java -cp bin com.cmp180.livechat.client.CustomerClient
   ```

4. **Khởi chạy Nhân viên CSKH:**
   ```bash
   java -cp bin com.cmp180.livechat.client.StaffClient
   ```

---

## 🤝 5. Hướng dẫn Làm việc nhóm trên GitHub

1. **Clone dự án về máy:**
   ```bash
   git clone <URL_REPO_GITHUB>
   ```
2. **Tạo nhánh (Branch) mới để làm tính năng:**
   ```bash
   git checkout -b feature/ten-tinh-nang
   ```
3. **Commit & Push thay đổi:**
   ```bash
   git add .
   git commit -m "Thêm tính năng X"
   git push origin feature/ten-tinh-nang
   ```
4. **Tạo Pull Request (PR)** trên GitHub để Trưởng nhóm review và merge vào nhánh `main`.
