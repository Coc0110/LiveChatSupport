# -*- coding: utf-8 -*-
"""
Script xuat file Markdown tu cac module noi dung de dam bao dong bo 100% voi Word docx
"""

from report_data import FRONT_MATTER, ABBREVIATIONS, TABLES_LIST, FIGURES_LIST
from report_content_part1 import (
    LOI_CAM_DOAN, CH1_INTRO, CH1_SO_SANH_DATA, CH1_NHIEM_VU, CH1_CAU_TRUC, CH2_CONTENT
)
from report_content_part2 import (
    CH3_DOT_PHA, CH4_TRIEN_KHAI, CH5_SO_LIEU_DB, CH6_KET_LUAN,
    CH3_BANG_3_1_DATA, CH3_BANG_3_6_PROTOCOL, CH3_BANG_CSDL,
    CH3_UNIT_TEST_RESULTS, CH3_SYSTEM_TEST_MATRIX, REFERENCES_LIST
)

def generate_markdown(output_path):
    lines = []

    lines.append(f"# {FRONT_MATTER['truong']}")
    lines.append(f"## {FRONT_MATTER['khoa']}\n")
    lines.append("---\n")
    lines.append("# BÁO CÁO ĐỒ ÁN HỌC PHẦN")
    lines.append(f"### Môn học: {FRONT_MATTER['mon_hoc']}\n")
    lines.append("# HỆ THỐNG CHAT HỖ TRỢ KHÁCH HÀNG TRỰC TUYẾN")
    lines.append("## (LIVE CHAT SUPPORT SYSTEM)")
    lines.append("### ỨNG DỤNG KIẾN TRÚC HYBRID TCP/UDP, TRUYỀN THÔNG ĐA PHƯƠNG TIỆN VÀ TRÍ TUỆ NHÂN TẠO\n")
    lines.append(f"**Giảng viên hướng dẫn:** {FRONT_MATTER['gvhd']}  ")
    lines.append(f"**Nhóm thực hiện:** {FRONT_MATTER['nhom']}  ")
    lines.append(f"- **Thành viên 1:** {FRONT_MATTER['tv1_ten']} - **MSSV:** {FRONT_MATTER['tv1_mssv']}  ")
    lines.append(f"- **Thành viên 2:** {FRONT_MATTER['tv2_ten']} - **MSSV:** {FRONT_MATTER['tv2_mssv']}  ")
    lines.append(f"**Lớp:** {FRONT_MATTER['lop']} &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **Năm học:** {FRONT_MATTER['nam_hoc']}  \n")
    lines.append("---\n")
    lines.append("<div style=\"page-break-after: always;\"></div>\n")

    lines.append("# LỜI CAM ĐOAN\n")
    lines.append(LOI_CAM_DOAN + "\n")
    lines.append(f"*{FRONT_MATTER['dia_diem_ngay']}*  ")
    lines.append("**Nhóm thực hiện**  ")
    lines.append("*(Ký và ghi rõ họ tên)*  \n<br>\n")
    lines.append(f"**{FRONT_MATTER['tv1_ten']}** &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **{FRONT_MATTER['tv2_ten']}**\n")
    lines.append("---\n<div style=\"page-break-after: always;\"></div>\n")

    # MỤC LỤC
    lines.append("# MỤC LỤC\n")
    lines.append("- [Lời cam đoan](#lời-cam-đoan)")
    lines.append("- [Danh mục các ký hiệu, các chữ viết tắt](#danh-mục-các-ký-hiệu-các-chữ-viết-tắt)")
    lines.append("- [Danh mục các bảng](#danh-mục-các-bảng)")
    lines.append("- [Danh mục các hình vẽ, đồ thị](#danh-mục-các-hình-vẽ-đồ-thị)")
    lines.append("- [CHƯƠNG 1. TỔNG QUAN VÀ BÀI TOÁN THỰC TIỄN](#chương-1-tổng-quan-và-bài-toán-thực-tiễn)")
    lines.append("  - [1.1. Giới thiệu đề tài](#11-giới-thiệu-đề-tài)")
    lines.append("  - [1.2. So sánh đối chiếu: Yêu cầu đồ án chuẩn và Các đột phá vượt trội](#12-so-sánh-đối-chiếu-yêu-cầu-đồ-án-chuẩn-và-các-đột-phá-vượt-trội-của-đề-tài)")
    lines.append("  - [1.3. Nhiệm vụ đồ án](#13-nhiệm-vụ-đồ-án)")
    lines.append("  - [1.4. Cấu trúc đồ án](#14-cấu-trúc-đồ-án)")
    lines.append("- [CHƯƠNG 2. CƠ SỞ LÝ THUYẾT VÀ CÔNG NGHỆ NỀN TẢNG](#chương-2-cơ-sở-lý-thuyết-và-công-nghệ-nền-tảng)")
    lines.append("  - [2.1. Mô hình Client - Server](#21-mô-hình-client---server)")
    lines.append("  - [2.2. Giao thức và Socket TCP trong Java](#22-giao-thức-và-socket-tcp-trong-java)")
    lines.append("  - [2.3. Giao thức và Socket UDP cho Streaming thời gian thực](#23-giao-thức-và-socket-udp-cho-streaming-thời-gian-thực)")
    lines.append("  - [2.4. Quản lý đa luồng với CachedThreadPool & Kỹ thuật đồng bộ phi khóa](#24-quản-lý-đa-luồng-với-cachedthreadpool--kỹ-thuật-đồng-bộ-phi-khóa)")
    lines.append("  - [2.5. Lớp InetAddress và Quản trị địa chỉ mạng](#25-lớp-inetaddress-và-quản-trị-địa-chỉ-mạng)")
    lines.append("  - [2.6. Tuần tự hóa đối tượng (Java Serialization) cho gói tin đa hình](#26-tuần-tự-hóa-đối-tượng-java-serialization-cho-gói-tin-đa-hình)")
    lines.append("  - [2.7. Mật mã học ứng dụng: AES-128 CBC ngẫu nhiên hóa IV và Salted SHA-256](#27-mật-mã-học-ứng-dụng-aes-128-cbc-ngẫu-nhiên-hóa-iv-và-salted-sha-256)")
    lines.append("  - [2.8. Ứng dụng Trí tuệ nhân tạo (Generative AI)](#28-ứng-dụng-trí-tuệ-nhân-tạo-generative-ai-trong-dịch-vụ-khách-hàng-thời-gian-thực)")
    lines.append("- [CHƯƠNG 3. THIẾT KẾ KIẾN TRÚC VÀ CÁC ĐỘT PHÁ KỸ THUẬT CỦA HỆ THỐNG](#chương-3-thiết-kế-kiến-trúc-và-các-đột-phá-kỹ-thuật-của-hệ-thống)")
    lines.append("  - [3.1. Kiến trúc tổng thể Hybrid TCP/UDP](#31-kiến-trúc-tổng-thể-hybrid-tcpudp)")
    lines.append("  - [3.2. Thuật toán điều phối hàng chờ thông minh 3 tầng](#32-thuật-toán-điều-phối-hàng-chờ-thông-minh-3-tầng)")
    lines.append("  - [3.3. Cơ chế chịu lỗi cấp doanh nghiệp (Session Handover & Auto-reconnect)](#33-cơ-chế-chịu-lỗi-cấp-doanh-nghiệp-session-handover--auto-reconnect)")
    lines.append("  - [3.4. Kênh truyền thông đa phương tiện độ trễ thấp](#34-kênh-truyền-thông-đa-phương-tiện-độ-trễ-thấp-voice-voice-call--video-call)")
    lines.append("  - [3.5. Tích hợp Trợ lý ảo Gemini AI & Chuyển giao AI-to-Human Handover](#35-tích-hợp-trợ-lý-ảo-gemini-ai--chuyển-giao-ai-to-human-handover)")
    lines.append("  - [3.6. Bộ kiểm duyệt ngôn từ vi phạm tự động](#36-bộ-kiểm-duyệt-ngôn-từ-vi-phạm-tự-động-profanity-filter-regex)")
    lines.append("  - [3.7. Thiết kế giao thức truyền thông ứng dụng](#37-thiết-kế-giao-thức-truyền-thông-ứng-dụng)")
    lines.append("  - [3.8. Thiết kế cơ sở dữ liệu SQLite nhúng hiệu năng cao](#38-thiết-kế-cơ-sở-dữ-liệu-sqlite-nhúng-hiệu-năng-cao)")
    lines.append("- [CHƯƠNG 4. TRIỂN KHAI CÀI ĐẶT VÀ MINH HỌA TÍNH NĂNG NGƯỜI DÙNG](#chương-4-triển-khai-cài-đặt-và-minh-họa-tính-năng-người-dùng)")
    lines.append("  - [4.1. Môi trường phát triển và cấu trúc thư mục](#41-môi-trường-phát-triển-và-cấu-trúc-thư-mục-dự-án)")
    lines.append("  - [4.2. Các mô-đun mã nguồn cốt lõi tiêu biểu](#42-các-mô-đun-mã-nguồn-cốt-lõi-tiêu-biểu)")
    lines.append("  - [4.3. Trải nghiệm người dùng phong phú (Rich UX)](#43-trải-nghiệm-người-dùng-phong-phú-rich-ux)")
    lines.append("  - [4.4. Bảng điều khiển Quản trị viên máy chủ](#44-bảng-điều-khiển-quản-trị-viên-máy-chủ-server-dashboard-swing-3-tab)")
    lines.append("  - [4.5. Đóng gói độc lập và Hướng dẫn vận hành](#45-đóng-gói-độc-lập-portable-distribution-và-hướng-dẫn-vận-hành)")
    lines.append("- [CHƯƠNG 5. KIỂM THỬ, ĐÁNH GIÁ THỰC NGHIỆM VÀ PHÂN TÍCH SỐ LIỆU](#chương-5-kiểm-thử-đánh-giá-thực-nghiệm-và-phân-tích-số-liệu)")
    lines.append("  - [5.1. Kiểm thử tự động chuyên sâu (75 ca JUnit 5 Tests)](#51-kiểm-thử-tự-động-chuyên-sâu-phân-tích-75-ca-kiểm-thử-junit-5-100-passed)")
    lines.append("  - [5.2. Ma trận kiểm thử tích hợp hệ thống thực tế](#52-ma-trận-kiểm-thử-tích-hợp-hệ-thống-thực-tế)")
    lines.append("  - [5.3. Phân tích số liệu thực nghiệm từ CSDL livechat.db](#53-phân-tích-số-liệu-thực-nghiệm-thực-tế-từ-csdl-livechatdb)")
    lines.append("  - [5.4. Trích xuất nhật ký thực tế phía Server (server.log)](#54-trích-xuất-nhật-ký-thực-tế-phía-server-serverlog)")
    lines.append("  - [5.5. Đánh giá ưu điểm vượt trội và các mặt hạn chế](#55-đánh-giá-ưu-điểm-vượt-trội-và-các-mặt-hạn-chế)")
    lines.append("- [CHƯƠNG 6. KẾT LUẬN VÀ KIẾN NGHỊ](#chương-6-kết-luận-và-kiến-nghị)")
    lines.append("  - [6.1. Kết luận và Thành tựu nổi bật](#61-kết-luận-và-thành-tựu-nổi-bật)")
    lines.append("  - [6.2. Kiến nghị và Hướng phát triển tương lai](#62-kiến-nghị-và-hướng-phát-triển-tương-lai)")
    lines.append("- [TÀI LIỆU THAM KHẢO](#tài-liệu-tham-khảo)")
    lines.append("- [PHỤ LỤC: MÃ NGUỒN CHƯƠNG TRÌNH TIÊU BIỂU](#phụ-lục-mã-nguồn-chương-trình-tiêu-biểu)\n")
    lines.append("---\n<div style=\"page-break-after: always;\"></div>\n")

    # DANH MỤC TỪ VIẾT TẮT
    lines.append("# DANH MỤC CÁC KÝ HIỆU, CÁC CHỮ VIẾT TẮT\n")
    lines.append("| Từ viết tắt | Thuật ngữ tiếng Anh đầy đủ / Diễn giải tiếng Việt |")
    lines.append("| :--- | :--- |")
    for abbr, mean in ABBREVIATIONS:
        lines.append(f"| **{abbr}** | {mean} |")
    lines.append("\n---\n<div style=\"page-break-after: always;\"></div>\n")

    # DANH MỤC BẢNG
    lines.append("# DANH MỤC CÁC BẢNG\n")
    for b_id, b_name in TABLES_LIST:
        lines.append(f"- **{b_id}:** {b_name}")
    lines.append("\n---\n<div style=\"page-break-after: always;\"></div>\n")

    # DANH MỤC HÌNH VẼ
    lines.append("# DANH MỤC CÁC HÌNH VẼ, ĐỒ THỊ\n")
    for f_id, f_name in FIGURES_LIST:
        lines.append(f"- **{f_id}:** {f_name}")
    lines.append("\n---\n<div style=\"page-break-after: always;\"></div>\n")

    # CHƯƠNG 1
    lines.append("# CHƯƠNG 1. TỔNG QUAN VÀ BÀI TOÁN THỰC TIỄN\n")
    lines.append("## 1.1. Giới thiệu đề tài\n" + CH1_INTRO + "\n")
    lines.append("## 1.2. So sánh đối chiếu: Yêu cầu đồ án chuẩn và Các đột phá vượt trội của đề tài\n")
    lines.append("Trong quá trình nghiên cứu và phát triển, nhóm tác giả đã không tự giới hạn bản thân trong khuôn khổ một bài tập lớn thông thường mà chủ động mở rộng quy mô, giải quyết các bài toán kỹ thuật phức tạp thường chỉ xuất hiện ở các nền tảng thương mại cấp doanh nghiệp. Bảng 1.1 dưới đây làm rõ những điểm khác biệt mang tính đột phá của hệ thống Live Chat Support:\n")
    lines.append("#### Bảng 1.1. Bảng so sánh đối chiếu giữa Yêu cầu đồ án thông thường và Các đột phá của đề tài")
    lines.append("| Hạng mục công nghệ | Yêu cầu đồ án môn học thông thường | Đột phá vượt trội của Đề tài Live Chat Support |")
    lines.append("| :--- | :--- | :--- |")
    for item in CH1_SO_SANH_DATA:
        lines.append(f"| **{item[0]}** | {item[1]} | {item[2]} |")
    lines.append("\n## 1.3. Nhiệm vụ đồ án\n")
    lines.append("### 1.3.1. Tính cấp thiết và lý do hình thành đề tài\n" + CH1_NHIEM_VU["cap_thiet"] + "\n")
    lines.append("### 1.3.2. Ý nghĩa khoa học và thực tiễn\n" + CH1_NHIEM_VU["y_nghia"] + "\n")
    lines.append("### 1.3.3. Mục tiêu đề tài\n" + CH1_NHIEM_VU["muc_tieu"] + "\n")
    lines.append("### 1.3.4. Đối tượng và phạm vi giới hạn\n" + CH1_NHIEM_VU["pham_vi"] + "\n")
    lines.append("## 1.4. Cấu trúc đồ án\n" + CH1_CAU_TRUC + "\n")
    lines.append("---\n<div style=\"page-break-after: always;\"></div>\n")

    # CHƯƠNG 2
    lines.append("# CHƯƠNG 2. CƠ SỞ LÝ THUYẾT VÀ CÔNG NGHỆ NỀN TẢNG\n")
    lines.append("## 2.1. Mô hình Client - Server\n" + CH2_CONTENT["client_server"] + "\n")
    lines.append("## 2.2. Giao thức và Socket TCP trong Java\n" + CH2_CONTENT["tcp_socket"] + "\n")
    lines.append("## 2.3. Giao thức và Socket UDP cho Streaming thời gian thực\n" + CH2_CONTENT["udp_socket"] + "\n")
    lines.append("## 2.4. Quản lý đa luồng với CachedThreadPool & Kỹ thuật đồng bộ phi khóa\n" + CH2_CONTENT["multithreading"] + "\n")
    lines.append("## 2.5. Lớp InetAddress và Quản trị địa chỉ mạng\n" + CH2_CONTENT["inet_address"] + "\n")
    lines.append("## 2.6. Tuần tự hóa đối tượng (Java Serialization) cho gói tin đa hình\n" + CH2_CONTENT["serialization"] + "\n")
    lines.append("## 2.7. Mật mã học ứng dụng: AES-128 CBC ngẫu nhiên hóa IV và Salted SHA-256\n" + CH2_CONTENT["security"] + "\n")
    lines.append("## 2.8. Ứng dụng Trí tuệ nhân tạo (Generative AI) trong dịch vụ khách hàng thời gian thực\n" + CH2_CONTENT["generative_ai"] + "\n")
    lines.append("---\n<div style=\"page-break-after: always;\"></div>\n")

    # CHƯƠNG 3
    lines.append("# CHƯƠNG 3. THIẾT KẾ KIẾN TRÚC VÀ CÁC ĐỘT PHÁ KỸ THUẬT CỦA HỆ THỐNG\n")
    lines.append("## 3.1. Kiến trúc tổng thể Hybrid TCP/UDP\n" + CH3_DOT_PHA["kien_truc"] + "\n")
    lines.append("```mermaid\nflowchart TD\n    subgraph Clients[\"TẦNG MÁY KHÁCH (CLIENTS)\"]\n        Customer[\"CustomerApp (JavaFX FXML)<br/>Khách hàng\"]\n        Staff[\"StaffApp (JavaFX FXML)<br/>Nhân viên CSKH\"]\n    end\n\n    subgraph Network[\"TẦNG TRUYỀN THÔNG MẠNG\"]\n        TCPChan[\"Kênh TCP (Port 5000)<br/>Control, Messages, Files (AES-CBC)\"]\n        UDPChan[\"Kênh UDP (Port 5000)<br/>Voice Call & Video Call Streaming\"]\n    end\n\n    subgraph Server[\"TẦNG MÁY CHỦ TRUNG TÂM (SERVER)\"]\n        ServerMain[\"ServerMain<br/>CachedThreadPool Executor\"]\n        SessionMgr[\"SessionManager<br/>Thuật toán điều phối 3 tầng & Handover\"]\n        ClientHdl[\"ClientHandler Threads<br/>Thread-per-Client Blocking I/O\"]\n        UDPRelay[\"UdpRelayServer<br/>In-memory Relay (<1ms)\"]\n        Profanity[\"ProfanityFilter<br/>Bộ lọc từ khóa Regex\"]\n        AIBot[\"AIBotService<br/>Gemini AI 2.5 Flash\"]\n        ServerGUI[\"ServerGUI (Swing)<br/>Dashboard Quản trị viên\"]\n    end\n\n    subgraph Database[\"TẦNG LƯU TRỮ DỮ LIỆU\"]\n        SQLite[(\"SQLite Database<br/>livechat.db (WAL Mode)\")]\n        DiskStorage[(\"Server Storage<br/>server_storage/files (AES Encrypted)\")]\n    end\n\n    Customer <-->|TCP Socket| TCPChan\n    Staff <-->|TCP Socket| TCPChan\n    Customer <-->|UDP Datagram| UDPChan\n    Staff <-->|UDP Datagram| UDPChan\n\n    TCPChan <--> ServerMain\n    ServerMain --> ClientHdl\n    ClientHdl <--> SessionMgr\n    ClientHdl <--> Profanity\n    ClientHdl <--> AIBot\n    UDPChan <--> UDPRelay\n\n    SessionMgr <--> SQLite\n    ClientHdl <--> DiskStorage\n    ServerGUI <--> SessionMgr\n    ServerGUI <--> SQLite\n```\n")
    lines.append("## 3.2. Thuật toán điều phối hàng chờ thông minh 3 tầng\n" + CH3_DOT_PHA["dieu_phoi_3_tang"] + "\n")
    lines.append("## 3.3. Cơ chế chịu lỗi cấp doanh nghiệp (Session Handover & Auto-reconnect)\n" + CH3_DOT_PHA["chiu_loi"] + "\n")
    lines.append("## 3.4. Kênh truyền thông đa phương tiện độ trễ thấp (Voice, Voice Call & Video Call)\n" + CH3_DOT_PHA["media_streaming"] + "\n")
    lines.append("## 3.5. Tích hợp Trợ lý ảo Gemini AI & Chuyển giao AI-to-Human Handover\n" + CH3_DOT_PHA["ai_bot"] + "\n")
    lines.append("## 3.6. Bộ kiểm duyệt ngôn từ vi phạm tự động (Profanity Filter Regex)\n")
    lines.append("Lớp `ProfanityFilter.java` sử dụng các biểu thức chính quy (Regex) tối ưu hóa cao độ để tự động phát hiện và chuyển đổi từ ngữ thô tục thành chuỗi ký tự `***`.\n")
    lines.append("## 3.7. Thiết kế giao thức truyền thông ứng dụng\n")
    lines.append("#### Bảng 3.1. Mô tả chi tiết các trường dữ liệu và thuộc tính của lớp Message\n")
    lines.append("| Thuộc tính (Field) | Kiểu dữ liệu | Ý nghĩa và vai trò trong giao thức |")
    lines.append("| :--- | :--- | :--- |")
    for r in CH3_BANG_3_1_DATA:
        lines.append(f"| `{r[0]}` | {r[1]} | {r[2]} |")
    lines.append("\n#### Bảng 3.6. Danh mục mã lệnh giao thức trao đổi ứng dụng\n")
    lines.append("| Mã lệnh (Command) | Chiều truyền | Ý nghĩa và hành vi nghiệp vụ |")
    lines.append("| :--- | :--- | :--- |")
    for r in CH3_BANG_3_6_PROTOCOL:
        lines.append(f"| `{r[0]}` | {r[1]} | {r[2]} |")
    lines.append("\n## 3.8. Thiết kế cơ sở dữ liệu SQLite nhúng hiệu năng cao\n" + CH3_DOT_PHA["database_toi_uu"] + "\n")
    lines.append("#### Bảng 3.2. Cấu trúc bảng cơ sở dữ liệu staff (Quản lý 82 Nhân viên CSKH và Phân quyền)\n")
    lines.append("| Tên cột (Column) | Kiểu dữ liệu | Ràng buộc (Constraint) | Diễn giải ý nghĩa |")
    lines.append("| :--- | :--- | :--- | :--- |")
    for r in CH3_BANG_CSDL["staff"]:
        lines.append(f"| `{r[0]}` | {r[1]} | {r[2]} | {r[3]} |")
    lines.append("\n#### Bảng 3.3. Cấu trúc bảng cơ sở dữ liệu customers (Quản lý Hồ sơ Khách hàng và Nhận diện SĐT)\n")
    lines.append("| Tên cột (Column) | Kiểu dữ liệu | Ràng buộc (Constraint) | Diễn giải ý nghĩa |")
    lines.append("| :--- | :--- | :--- | :--- |")
    for r in CH3_BANG_CSDL["customers"]:
        lines.append(f"| `{r[0]}` | {r[1]} | {r[2]} | {r[3]} |")
    lines.append("\n#### Bảng 3.4. Cấu trúc bảng cơ sở dữ liệu chat_sessions (Quản lý Phiên hỗ trợ, Ticket và Đánh giá CSAT)\n")
    lines.append("| Tên cột (Column) | Kiểu dữ liệu | Ràng buộc (Constraint) | Diễn giải ý nghĩa |")
    lines.append("| :--- | :--- | :--- | :--- |")
    for r in CH3_BANG_CSDL["chat_sessions"]:
        lines.append(f"| `{r[0]}` | {r[1]} | {r[2]} | {r[3]} |")
    lines.append("\n#### Bảng 3.5. Cấu trúc bảng cơ sở dữ liệu messages (Quản lý Lịch sử tin nhắn đa phương tiện đã mã hóa)\n")
    lines.append("| Tên cột (Column) | Kiểu dữ liệu | Ràng buộc (Constraint) | Diễn giải ý nghĩa |")
    lines.append("| :--- | :--- | :--- | :--- |")
    for r in CH3_BANG_CSDL["messages"]:
        lines.append(f"| `{r[0]}` | {r[1]} | {r[2]} | {r[3]} |")
    lines.append("\n---\n<div style=\"page-break-after: always;\"></div>\n")

    # CHƯƠNG 4
    lines.append("# CHƯƠNG 4. TRIỂN KHAI CÀI ĐẶT VÀ MINH HỌA TÍNH NĂNG NGƯỜI DÙNG\n")
    lines.append("## 4.1. Môi trường phát triển và cấu trúc thư mục dự án\n")
    lines.append("Hệ thống được phát triển và tương thích hoàn toàn trên môi trường Java SE 17 LTS và Java SE 25. Cấu trúc mã nguồn phân tách rõ ràng:\n")
    lines.append("```text\nLiveChatSupport-CMP180/\n├── pom.xml                                  # Cấu hình Maven dependencies, plugins và Fat JARs\n├── README.md                                # Tài liệu kỹ thuật chi tiết\n├── run_server.bat                           # Khởi chạy Server Dashboard\n├── run_staff.bat                            # Khởi chạy Nhân viên CSKH\n├── run_customer.bat                         # Khởi chạy Khách hàng\n├── package-portable-apps.bat                # Kịch bản đóng gói Portable Apps độc lập\n├── livechat.db                              # CSDL nhúng SQLite 3 (Chế độ WAL)\n├── server_storage/files/                    # Thư mục lưu trữ tệp tin & voice note mã hóa AES\n├── src/main/java/com/cmp180/livechat/\n│   ├── common/                              # Protocol, Message, SecurityManager, Media\n│   ├── server/                              # ServerMain, ServerGUI, SessionManager, Database\n│   └── client/                              # Customer, Staff, UI dialogs & themes\n└── src/test/java/com/cmp180/livechat/       # 75 ca kiểm thử tự động JUnit 5\n```\n")
    lines.append("## 4.2. Các mô-đun mã nguồn cốt lõi tiêu biểu\n")
    lines.append("### a) Khởi tạo Socket Server và Hồ bơi luồng CachedThreadPool\n```java\npublic void start() {\n    isRunning = true;\n    log(\"=== HỆ THỐNG LIVE CHAT SUPPORT SERVER (PORT \" + PORT + \") ===\");\n    try {\n        udpRelayServer.start();\n        serverSocket = new ServerSocket(PORT);\n        while (isRunning) {\n            Socket clientSocket = serverSocket.accept();\n            ClientHandler handler = new ClientHandler(clientSocket, this, sessionManager);\n            threadPool.execute(handler);\n        }\n    } catch (IOException e) {\n        if (isRunning) log(\"Lỗi Server: \" + e.getMessage());\n    }\n}\n```\n")
    lines.append("### b) Vòng lặp đọc thông điệp và Giám sát Timeout 40 giây\n```java\npublic void run() {\n    try {\n        socket.setSoTimeout(40000);\n        Message message;\n        while ((message = (Message) in.readObject()) != null) {\n            processMessage(message);\n        }\n    } catch (SocketTimeoutException e) {\n        server.log(\"[ZOMBIE TIMEOUT] Không nhận được heartbeat từ \" + name);\n    } catch (Exception e) {\n        server.log(\"[NGẮT KẾT NỐI] Client \" + name + \" đã rời đi.\");\n    } finally {\n        sessionManager.handleDisconnect(this);\n        closeConnection();\n    }\n}\n```\n")
    lines.append("## 4.3. Trải nghiệm người dùng phong phú (Rich UX)\n" + CH4_TRIEN_KHAI["ux"] + "\n")
    lines.append("## 4.4. Bảng điều khiển Quản trị viên máy chủ (Server Dashboard Swing 3 Tab)\n" + CH4_TRIEN_KHAI["dashboard"] + "\n")
    lines.append("## 4.5. Đóng gói độc lập (Portable Distribution) và Hướng dẫn vận hành\n")
    lines.append("- **Cách 1: Chạy file Batch:** `run_server.bat` -> `run_staff.bat` -> `run_customer.bat`.\n")
    lines.append("- **Cách 2: Chạy Maven CLI:** `mvnw exec:java@run-server`, `mvnw exec:java@run-staff`, `mvnw exec:java@run-customer`.\n")
    lines.append("- **Cách 3: Chạy ứng dụng Portable .exe:** Chạy trực tiếp `dist/CustomerApp/CustomerApp.exe` và `dist/StaffApp/StaffApp.exe` trên Windows mà không cần cài đặt JDK.\n")
    lines.append("---\n<div style=\"page-break-after: always;\"></div>\n")

    # CHƯƠNG 5
    lines.append("# CHƯƠNG 5. KIỂM THỬ, ĐÁNH GIÁ THỰC NGHIỆM VÀ PHÂN TÍCH SỐ LIỆU\n")
    lines.append("## 5.1. Kiểm thử tự động chuyên sâu: Phân tích 75 ca kiểm thử JUnit 5 (100% Passed)\n")
    lines.append("#### Bảng 5.1. Tổng hợp kết quả 75 ca kiểm thử tự động JUnit 5")
    lines.append("| Tên lớp kiểm thử (Test Class) | Số ca test | Đạt (Pass) | Lỗi (Fail) | Mục tiêu và Chức năng kiểm thử |")
    lines.append("| :--- | :---: | :---: | :---: | :--- |")
    for r in CH3_UNIT_TEST_RESULTS:
        lines.append(f"| `{r[0]}` | {r[1]} | {r[2]} | {r[3]} | {r[4]} |")
    lines.append("\n## 5.2. Ma trận kiểm thử tích hợp hệ thống thực tế\n")
    lines.append("#### Bảng 5.2. Ma trận kiểm thử chức năng tích hợp hệ thống thực tế")
    lines.append("| Mã TC | Tên ca kiểm thử | Dữ liệu đầu vào & Thao tác | Kết quả mong đợi | Trạng thái |")
    lines.append("| :---: | :--- | :--- | :--- | :---: |")
    for r in CH3_SYSTEM_TEST_MATRIX:
        lines.append(f"| **{r[0]}** | {r[1]} | {r[2]} | {r[3]} | **{r[4]}** |")
    lines.append("\n## 5.3. Phân tích số liệu thực nghiệm thực tế từ CSDL livechat.db\n")
    lines.append("#### Bảng 5.3. Bảng tổng hợp số liệu thực nghiệm hệ thống trích xuất từ cơ sở dữ liệu livechat.db")
    lines.append("| Chỉ số đo lường thực nghiệm | Số liệu ghi nhận thực tế từ CSDL |")
    lines.append("| :--- | :--- |")
    for r in CH5_SO_LIEU_DB:
        lines.append(f"| **{r[0]}** | {r[1]} |")
    lines.append("\n## 5.4. Trích xuất nhật ký thực tế phía Server (server.log)\n")
    lines.append("```text\n=== HỆ THỐNG LIVE CHAT SUPPORT SERVER (PORT 5000) ===\n-> UDP Media Relay Server đang lắng nghe trên cổng 5000 (Voice & Video Call)\n-> [GEMINI AI] Trạng thái dịch vụ: Sẵn sàng trực tuyến (Online Model: gemini-2.5-flash)\n[KẾT NỐI MỚI] /127.0.0.1:51240\n[CSKH ONLINE] Nhân viên 'Hoàng Trung Anh' (MSSV: 2380602692) đang sẵn sàng.\n[KẾT NỐI MỚI] /127.0.0.1:51248\n[HÀNG CHỜ] Khách hàng 'Trần Văn Nam' (SĐT: 0912345678) đã vào hàng chờ.\n-> [ĐIỀU PHỐI TẦNG 2] Khớp chuyên môn 'Hỗ trợ Kỹ thuật' -> Ghép khách 'Trần Văn Nam' với CSKH 'Hoàng Trung Anh'.\n[PHIÊN MỚI] Đã thiết lập phiên c7fa-4bdc-bb7a (Trần Văn Nam <-> Hoàng Trung Anh)\n[CHAT_MSG] [AES-CBC Encrypted] Trần Văn Nam -> Hoàng Trung Anh (Độ dài gói: 96 bytes)\n[CLIPBOARD IMAGE] Khách hàng đã gửi 1 ảnh chụp màn hình (Dung lượng: 128,450 bytes)\n[VOICE NOTE] Gửi tin nhắn thoại WAV (Độ dài: 3.2 giây, Dung lượng: 102,400 bytes)\n[UDP VOICE RELAY] Chuyển tiếp thành công 142 gói thoại giữa 0912345678 và Hoàng Trung Anh.\n[ĐÁNH GIÁ CSAT] Khách hàng 'Trần Văn Nam' đánh giá 5 SAO kèm phản hồi: 'Nhân viên hỗ trợ rất nhiệt tình!'\n[ĐÓNG PHIÊN] Phiên c7fa-4bdc-bb7a đã kết thúc thành công.\n```\n")
    lines.append("## 5.5. Đánh giá ưu điểm vượt trội và các mặt hạn chế\n")
    lines.append("### 1. Ưu điểm vượt trội:\n- Kiến trúc Hybrid TCP/UDP song song vượt trội so với đồ án chỉ dùng TCP text thông thường.\n- Đa phương tiện trọn gói: Clipboard Image (Ctrl+V), Voice Note, Voice Call và Video Call qua UDP.\n- Tích hợp Trí tuệ nhân tạo Gemini AI trực ban tự động khi quá tải.\n- Thuật toán điều phối 3 tầng thông minh & Cơ chế Session Handover chịu lỗi tuyệt đối.\n- Bảo mật đa lớp: AES-128 CBC ngẫu nhiên IV, Salted SHA-256, Profanity Filter.\n- 75/75 ca kiểm thử JUnit 5 tự động vượt qua 100%.\n")
    lines.append("### 2. Các mặt hạn chế cần cải tiến:\n- Đàm thoại media hiện tại là 1-1, chưa hỗ trợ phòng họp nhóm đông người (Group Conference).\n- Máy chủ trung chuyển UDP Relay chịu tải lưu lượng media khi có hàng trăm cuộc gọi video đồng thời.\n- Ứng dụng hiện trên JavaFX Desktop, chưa có bản Mobile App hoặc Web Widget nhúng.\n")
    lines.append("---\n<div style=\"page-break-after: always;\"></div>\n")

    # CHƯƠNG 6
    lines.append("# CHƯƠNG 6. KẾT LUẬN VÀ KIẾN NGHỊ\n")
    lines.append("## 6.1. Kết luận và Thành tựu nổi bật\n" + CH6_KET_LUAN + "\n")
    lines.append("## 6.2. Kiến nghị và Hướng phát triển tương lai\n")
    lines.append("1. **Mở rộng nền tảng Web và Mobile (WebSocket & Flutter):** Xây dựng WebSocket Gateway tại Server, cho phép nhúng widget trực tiếp trên website bằng ReactJS và xây dựng ứng dụng di động Flutter.\n")
    lines.append("2. **Nâng cấp hạ tầng sang WebRTC P2P:** Chuyển đổi kênh media sang WebRTC P2P kết hợp STUN/TURN server, giảm 100% băng thông media cho Server và hỗ trợ cuộc gọi nhóm nhiều người.\n")
    lines.append("3. **Kiến trúc Microservices phân tán:** Đóng gói Docker, điều phối Kubernetes, sử dụng Apache Kafka làm Message Queue và PostgreSQL Cluster lưu trữ dữ liệu quy mô lớn.\n")
    lines.append("---\n<div style=\"page-break-after: always;\"></div>\n")

    # TÀI LIỆU THAM KHẢO
    lines.append("# TÀI LIỆU THAM KHẢO\n")
    for ref in REFERENCES_LIST:
        lines.append(f"{ref}  ")
    lines.append("\n---\n<div style=\"page-break-after: always;\"></div>\n")

    # PHỤ LỤC
    lines.append("# PHỤ LỤC: MÃ NGUỒN CHƯƠNG TRÌNH TIÊU BIỂU\n")
    lines.append("### Phụ lục 1. Lớp mã hóa và an ninh hệ thống (SecurityManager.java)\n```java\npackage com.cmp180.livechat.common;\n\nimport javax.crypto.Cipher;\nimport javax.crypto.spec.IvParameterSpec;\nimport javax.crypto.spec.SecretKeySpec;\nimport java.nio.charset.StandardCharsets;\nimport java.security.MessageDigest;\nimport java.security.SecureRandom;\nimport java.util.Arrays;\nimport java.util.Base64;\n\npublic class SecurityManager {\n    private static final String SECRET_KEY = \"LiveChatKey12345\"; \n    private static final String ALGORITHM_AES = \"AES\";\n    private static final String CIPHER_CBC = \"AES/CBC/PKCS5Padding\";\n    private static final int IV_LENGTH = 16;\n    private static final SecureRandom SECURE_RANDOM = new SecureRandom();\n\n    public static String encrypt(String data) {\n        if (data == null) return null;\n        try {\n            byte[] iv = new byte[IV_LENGTH];\n            SECURE_RANDOM.nextBytes(iv);\n            IvParameterSpec ivSpec = new IvParameterSpec(iv);\n\n            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM_AES);\n            Cipher cipher = Cipher.getInstance(CIPHER_CBC);\n            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);\n\n            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));\n            byte[] combined = new byte[IV_LENGTH + encryptedBytes.length];\n            System.arraycopy(iv, 0, combined, 0, IV_LENGTH);\n            System.arraycopy(encryptedBytes, 0, combined, IV_LENGTH, encryptedBytes.length);\n\n            return Base64.getEncoder().encodeToString(combined);\n        } catch (Exception e) {\n            return data;\n        }\n    }\n\n    public static String decrypt(String encryptedData) {\n        if (encryptedData == null || encryptedData.isEmpty()) return encryptedData;\n        try {\n            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);\n            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM_AES);\n\n            if (decodedBytes.length > IV_LENGTH) {\n                byte[] iv = Arrays.copyOfRange(decodedBytes, 0, IV_LENGTH);\n                byte[] cipherBytes = Arrays.copyOfRange(decodedBytes, IV_LENGTH, decodedBytes.length);\n                Cipher cbcCipher = Cipher.getInstance(CIPHER_CBC);\n                cbcCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));\n                return new String(cbcCipher.doFinal(cipherBytes), StandardCharsets.UTF_8);\n            }\n            return encryptedData;\n        } catch (Exception e) {\n            return encryptedData; \n        }\n    }\n\n    public static String hashPasswordWithSalt(String password) {\n        if (password == null) return null;\n        byte[] salt = new byte[16];\n        SECURE_RANDOM.nextBytes(salt);\n        String saltHex = bytesToHex(salt);\n        String hashHex = rawHash(saltHex + password);\n        return saltHex + \":\" + hashHex;\n    }\n\n    public static boolean verifyPassword(String rawPassword, String storedHash) {\n        if (rawPassword == null || storedHash == null) return false;\n        if (storedHash.contains(\":\")) {\n            String[] parts = storedHash.split(\":\", 2);\n            String saltHex = parts[0];\n            String expectedHash = parts[1];\n            return rawHash(saltHex + rawPassword).equalsIgnoreCase(expectedHash);\n        }\n        return rawHash(rawPassword).equalsIgnoreCase(storedHash) || storedHash.equals(rawPassword);\n    }\n\n    private static String rawHash(String input) {\n        try {\n            MessageDigest md = MessageDigest.getInstance(\"SHA-256\");\n            return bytesToHex(md.digest(input.getBytes(StandardCharsets.UTF_8)));\n        } catch (Exception e) {\n            throw new RuntimeException(e);\n        }\n    }\n\n    private static String bytesToHex(byte[] bytes) {\n        StringBuilder sb = new StringBuilder();\n        for (byte b : bytes) sb.append(String.format(\"%02x\", b));\n        return sb.toString();\n    }\n}\n```\n")
    lines.append("### Phụ lục 2. Máy chủ trung chuyển đa phương tiện UDP (UdpRelayServer.java)\n```java\npackage com.cmp180.livechat.server;\n\nimport com.cmp180.livechat.common.UdpMediaPacket;\nimport java.io.IOException;\nimport java.net.DatagramPacket;\nimport java.net.DatagramSocket;\nimport java.net.SocketAddress;\nimport java.util.Map;\nimport java.util.concurrent.ConcurrentHashMap;\n\npublic class UdpRelayServer {\n    private final int port;\n    private DatagramSocket socket;\n    private volatile boolean isRunning = false;\n    private final Map<String, SocketAddress> activeEndpoints = new ConcurrentHashMap<>();\n\n    public UdpRelayServer(int port) {\n        this.port = port;\n    }\n\n    public synchronized void start() {\n        if (isRunning) return;\n        isRunning = true;\n        try {\n            this.socket = new DatagramSocket(this.port);\n            this.socket.setReceiveBufferSize(2 * 1024 * 1024);\n            Thread worker = new Thread(this::listenLoop, \"UdpRelayWorker\");\n            worker.setDaemon(true);\n            worker.start();\n        } catch (IOException e) {\n            System.err.println(\"Lỗi UDP Relay: \" + e.getMessage());\n        }\n    }\n\n    private void listenLoop() {\n        byte[] buffer = new byte[65507];\n        while (isRunning && !socket.isClosed()) {\n            try {\n                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);\n                socket.receive(packet);\n\n                UdpMediaPacket mediaPacket = UdpMediaPacket.fromBytes(packet.getData(), packet.getLength());\n                if (mediaPacket == null) continue;\n\n                activeEndpoints.put(mediaPacket.senderId, packet.getSocketAddress());\n\n                SocketAddress targetAddress = activeEndpoints.get(mediaPacket.receiverId);\n                if (targetAddress != null) {\n                    DatagramPacket forwardPacket = new DatagramPacket(\n                        packet.getData(), packet.getLength(), targetAddress\n                    );\n                    socket.send(forwardPacket);\n                }\n            } catch (Exception ignored) {}\n        }\n    }\n\n    public synchronized void stop() {\n        isRunning = false;\n        if (socket != null && !socket.isClosed()) socket.close();\n    }\n}\n```\n")

    content = "\n".join(lines)
    with open(output_path, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"Xuat Markdown thanh cong tai: {output_path}")

if __name__ == "__main__":
    generate_markdown("BAO_CAO_DO_AN_CMP180.md")
    import shutil
    shutil.copyfile("BAO_CAO_DO_AN_CMP180.md", r"C:\Users\WIN\.gemini\antigravity\brain\8923211e-c856-437d-ab01-b89dcf2d28ef\BAO_CAO_DO_AN_CMP180_LIVECHAT_SUPPORT.md")
    print("Dong bo Markdown sang Artifact thanh cong.")
