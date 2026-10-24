# -*- coding: utf-8 -*-
"""
Du lieu chi tiet cho Bao cao Do an Mon hoc: CMP180 - Lap trinh mang may tinh
De tai: HE THONG CHAT HO TRO KHACH HANG TRUC TUYEN (LIVE CHAT SUPPORT)
Phien ban: Nang cao - Lam noi bat cac tinh nang vuot troi va kien truc dot pha
"""

FRONT_MATTER = {
    "truong": "TRƯỜNG ĐẠI HỌC CÔNG NGHỆ TP.HCM (HUTECH)",
    "khoa": "KHOA CÔNG NGHỆ THÔNG TIN",
    "mon_hoc": "CMP180 - Lập trình mạng máy tính",
    "ten_de_tai": "HỆ THỐNG CHAT HỖ TRỢ KHÁCH HÀNG TRỰC TUYẾN\n(LIVE CHAT SUPPORT SYSTEM)\nỨNG DỤNG KIẾN TRÚC HYBRID TCP/UDP, TRUYỀN THÔNG ĐA PHƯƠNG TIỆN VÀ TRÍ TUỆ NHÂN TẠO",
    "gvhd": "ThS. Nguyễn Văn Cẩn",
    "nhom": "Nhóm sinh viên thực hiện",
    "tv1_ten": "Hoàng Trung Anh",
    "tv1_mssv": "2380602692",
    "tv2_ten": "Huỳnh Phát Lộc",
    "tv2_mssv": "2380613189",
    "lop": "23DTH............................",
    "nam_hoc": "2025 - 2026",
    "dia_diem_ngay": "TP. Hồ Chí Minh, ngày ..... tháng ..... năm 2026"
}

ABBREVIATIONS = [
    ("CSKH", "Chăm sóc khách hàng (Customer Service / Support)"),
    ("TCP", "Transmission Control Protocol (Giao thức điều khiển truyền vận tin cậy hướng nối)"),
    ("UDP", "User Datagram Protocol (Giao thức truyền dữ liệu phi kết nối, độ trễ cực thấp)"),
    ("IP", "Internet Protocol (Giao thức mạng internet định vị địa chỉ)"),
    ("I/O", "Input / Output (Nhập / Xuất dữ liệu)"),
    ("BIO", "Blocking Input/Output (Cơ chế vào/ra đồng bộ, quản lý qua luồng độc lập)"),
    ("API", "Application Programming Interface (Giao diện lập trình ứng dụng)"),
    ("JDK", "Java Development Kit (Bộ công cụ phát triển phần mềm Java tiêu chuẩn)"),
    ("JRE", "Java Runtime Environment (Môi trường thực thi Java)"),
    ("JVM", "Java Virtual Machine (Máy ảo Java)"),
    ("FXML", "XML-based User Interface Markup Language (Ngôn ngữ khai báo giao diện JavaFX)"),
    ("GUI", "Graphical User Interface (Giao diện người dùng đồ họa)"),
    ("MVC", "Model - View - Controller (Mô hình kiến trúc phân tầng giao diện, điều khiển và dữ liệu)"),
    ("CSAT", "Customer Satisfaction Score (Chỉ số đo lường độ hài lòng của khách hàng 1-5 sao)"),
    ("AES", "Advanced Encryption Standard (Chuẩn mã hóa nâng cao đối xứng)"),
    ("CBC", "Cipher Block Chaining (Chế độ mã hóa chuỗi khối kết hợp IV ngẫu nhiên)"),
    ("ECB", "Electronic Codebook (Chế độ mã hóa sách mật mã cơ bản - hỗ trợ Legacy Fallback)"),
    ("SHA", "Secure Hash Algorithm (Thuật toán băm an toàn một chiều 256-bit)"),
    ("IV", "Initialization Vector (Vector khởi tạo ngẫu nhiên trong mã hóa khối)"),
    ("SQLite", "Structured Query Language Lite (Hệ quản trị CSDL quan hệ nhúng hiệu năng cao)"),
    ("WAL", "Write-Ahead Logging (Cơ chế ghi nhật ký trước giúp SQLite đọc ghi song song)"),
    ("JDBC", "Java Database Connectivity (Chuẩn kết nối và thao tác cơ sở dữ liệu trong Java)"),
    ("NAT", "Network Address Translation (Kỹ thuật biên dịch địa chỉ mạng)"),
    ("RIFF", "Resource Interchange File Format (Định dạng khung tệp container âm thanh WAV tiêu chuẩn)"),
    ("PCM", "Pulse-Code Modulation (Kỹ thuật điều chế mã xung âm thanh kỹ thuật số)"),
    ("FIFO", "First In, First Out (Nguyên tắc xếp hàng vào trước - ra trước)")
]

TABLES_LIST = [
    ("Bảng 1.1", "Bảng so sánh đối chiếu giữa Yêu cầu đồ án thông thường và Các đột phá của đề tài"),
    ("Bảng 3.1", "Mô tả chi tiết các trường dữ liệu và thuộc tính của lớp Message"),
    ("Bảng 3.2", "Cấu trúc bảng cơ sở dữ liệu `staff` (Quản lý 82 Nhân viên CSKH và Phân quyền)"),
    ("Bảng 3.3", "Cấu trúc bảng cơ sở dữ liệu `customers` (Quản lý Hồ sơ Khách hàng và Nhận diện SĐT)"),
    ("Bảng 3.4", "Cấu trúc bảng cơ sở dữ liệu `chat_sessions` (Quản lý Phiên hỗ trợ, Ticket và Đánh giá CSAT)"),
    ("Bảng 3.5", "Cấu trúc bảng cơ sở dữ liệu `messages` (Quản lý Lịch sử tin nhắn đa phương tiện đã mã hóa)"),
    ("Bảng 3.6", "Danh mục mã lệnh giao thức trao đổi ứng dụng (LiveChat Application Protocol)"),
    ("Bảng 5.1", "Tổng hợp kết quả 75 ca kiểm thử tự động (Automated Unit Tests với JUnit 5)"),
    ("Bảng 5.2", "Ma trận kiểm thử chức năng tích hợp hệ thống thực tế (System Integration Testing)"),
    ("Bảng 5.3", "Bảng tổng hợp số liệu thực nghiệm hệ thống trích xuất từ cơ sở dữ liệu `livechat.db`")
]

FIGURES_LIST = [
    ("Hình 3.1", "Sơ đồ kiến trúc tổng thể hệ thống Live Chat Support (Mô hình phân tầng Hybrid TCP/UDP)"),
    ("Hình 3.2", "Sơ đồ luồng thuật toán điều phối hàng chờ thông minh 3 tầng (Smart 3-Tier Dispatching)"),
    ("Hình 3.3", "Sơ đồ tuần tự quy trình bắt tay và chuyển giao phiên dang dở (Session Handover)"),
    ("Hình 3.4", "Sơ đồ kiến trúc kênh truyền thông đa phương tiện thời gian thực qua UDP Relay Server"),
    ("Hình 3.5", "Sơ đồ quy trình tương tác và chuyển giao Trợ lý ảo AI sang CSKH (AI-to-Human Handover)"),
    ("Hình 3.6", "Sơ đồ kiến trúc an toàn thông tin: Mã hóa AES-128 CBC ngẫu nhiên IV và Salted SHA-256"),
    ("Hình 4.1", "Giao diện Bảng điều khiển Quản trị viên máy chủ (Server Dashboard - Java Swing 3 Tab)"),
    ("Hình 4.2", "Giao diện ứng dụng Nhân viên CSKH (StaffApp - JavaFX FXML đa phiên, Dark/Light Mode)"),
    ("Hình 4.3", "Giao diện ứng dụng Khách hàng (CustomerApp) với tính năng dán ảnh Clipboard, Voice & Video Call"),
    ("Hình 4.4", "Giao diện chấm điểm đánh giá độ hài lòng CSAT và Biểu đồ thống kê kết quả thực tế")
]

print("report_data.py updated.")
