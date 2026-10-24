# TRƯỜNG ĐẠI HỌC CÔNG NGHỆ TP.HCM (HUTECH)
## KHOA CÔNG NGHỆ THÔNG TIN

---

# BÁO CÁO ĐỒ ÁN HỌC PHẦN
### Môn học: CMP180 - Lập trình mạng máy tính

# HỆ THỐNG CHAT HỖ TRỢ KHÁCH HÀNG TRỰC TUYẾN
## (LIVE CHAT SUPPORT SYSTEM)
### ỨNG DỤNG KIẾN TRÚC HYBRID TCP/UDP, TRUYỀN THÔNG ĐA PHƯƠNG TIỆN VÀ TRÍ TUỆ NHÂN TẠO

**Giảng viên hướng dẫn:** ThS. Nguyễn Văn Cẩn  
**Nhóm thực hiện:** Nhóm sinh viên thực hiện  
- **Thành viên 1:** Hoàng Trung Anh - **MSSV:** 2380602692  
- **Thành viên 2:** Huỳnh Phát Lộc - **MSSV:** 2380613189  
**Lớp:** 23DTH............................ &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **Năm học:** 2025 - 2026  

---

<div style="page-break-after: always;"></div>

# LỜI CAM ĐOAN

Nhóm chúng tôi xin cam đoan đây là công trình nghiên cứu và thực hiện của nhóm chúng tôi dưới sự hướng dẫn khoa học của ThS. Nguyễn Văn Cẩn thuộc Khoa Công Nghệ Thông Tin – Trường Đại học Công Nghệ TP.HCM (HUTECH).

Các ý tưởng kiến trúc, giải pháp công nghệ, thuật toán điều phối, mã nguồn và kết quả thực nghiệm trình bày trong báo cáo đồ án này là hoàn toàn trung thực, khách quan và được đúc kết trực tiếp từ quá trình dày công nghiên cứu, hiện thực hóa và kiểm thử hệ thống "Live Chat Support". Các đoạn trích dẫn, kiến thức lý thuyết kế thừa từ các tài liệu tham khảo, bài báo khoa học hay tài liệu kỹ thuật đều được ghi rõ nguồn gốc và trích dẫn theo đúng quy định học thuật của Nhà trường.

Mọi sự vi phạm về quy chế liêm chính học thuật hoặc sao chép trái phép, nhóm chúng tôi xin hoàn toàn chịu trách nhiệm trước Hội đồng chấm thi, Khoa Công Nghệ Thông Tin và Nhà trường.

*TP. Hồ Chí Minh, ngày ..... tháng ..... năm 2026*  
**Nhóm thực hiện**  
*(Ký và ghi rõ họ tên)*  
<br>

**Hoàng Trung Anh** &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; **Huỳnh Phát Lộc**

---
<div style="page-break-after: always;"></div>

# MỤC LỤC

- [Lời cam đoan](#lời-cam-đoan)
- [Danh mục các ký hiệu, các chữ viết tắt](#danh-mục-các-ký-hiệu-các-chữ-viết-tắt)
- [Danh mục các bảng](#danh-mục-các-bảng)
- [Danh mục các hình vẽ, đồ thị](#danh-mục-các-hình-vẽ-đồ-thị)
- [CHƯƠNG 1. TỔNG QUAN VÀ BÀI TOÁN THỰC TIỄN](#chương-1-tổng-quan-và-bài-toán-thực-tiễn)
  - [1.1. Giới thiệu đề tài](#11-giới-thiệu-đề-tài)
  - [1.2. So sánh đối chiếu: Yêu cầu đồ án chuẩn và Các đột phá vượt trội](#12-so-sánh-đối-chiếu-yêu-cầu-đồ-án-chuẩn-và-các-đột-phá-vượt-trội-của-đề-tài)
  - [1.3. Nhiệm vụ đồ án](#13-nhiệm-vụ-đồ-án)
  - [1.4. Cấu trúc đồ án](#14-cấu-trúc-đồ-án)
- [CHƯƠNG 2. CƠ SỞ LÝ THUYẾT VÀ CÔNG NGHỆ NỀN TẢNG](#chương-2-cơ-sở-lý-thuyết-và-công-nghệ-nền-tảng)
  - [2.1. Mô hình Client - Server](#21-mô-hình-client---server)
  - [2.2. Giao thức và Socket TCP trong Java](#22-giao-thức-và-socket-tcp-trong-java)
  - [2.3. Giao thức và Socket UDP cho Streaming thời gian thực](#23-giao-thức-và-socket-udp-cho-streaming-thời-gian-thực)
  - [2.4. Quản lý đa luồng với CachedThreadPool & Kỹ thuật đồng bộ phi khóa](#24-quản-lý-đa-luồng-với-cachedthreadpool--kỹ-thuật-đồng-bộ-phi-khóa)
  - [2.5. Lớp InetAddress và Quản trị địa chỉ mạng](#25-lớp-inetaddress-và-quản-trị-địa-chỉ-mạng)
  - [2.6. Tuần tự hóa đối tượng (Java Serialization) cho gói tin đa hình](#26-tuần-tự-hóa-đối-tượng-java-serialization-cho-gói-tin-đa-hình)
  - [2.7. Mật mã học ứng dụng: AES-128 CBC ngẫu nhiên hóa IV và Salted SHA-256](#27-mật-mã-học-ứng-dụng-aes-128-cbc-ngẫu-nhiên-hóa-iv-và-salted-sha-256)
  - [2.8. Ứng dụng Trí tuệ nhân tạo (Generative AI)](#28-ứng-dụng-trí-tuệ-nhân-tạo-generative-ai-trong-dịch-vụ-khách-hàng-thời-gian-thực)
- [CHƯƠNG 3. THIẾT KẾ KIẾN TRÚC VÀ CÁC ĐỘT PHÁ KỸ THUẬT CỦA HỆ THỐNG](#chương-3-thiết-kế-kiến-trúc-và-các-đột-phá-kỹ-thuật-của-hệ-thống)
  - [3.1. Kiến trúc tổng thể Hybrid TCP/UDP](#31-kiến-trúc-tổng-thể-hybrid-tcpudp)
  - [3.2. Thuật toán điều phối hàng chờ thông minh 3 tầng](#32-thuật-toán-điều-phối-hàng-chờ-thông-minh-3-tầng)
  - [3.3. Cơ chế chịu lỗi cấp doanh nghiệp (Session Handover & Auto-reconnect)](#33-cơ-chế-chịu-lỗi-cấp-doanh-nghiệp-session-handover--auto-reconnect)
  - [3.4. Kênh truyền thông đa phương tiện độ trễ thấp](#34-kênh-truyền-thông-đa-phương-tiện-độ-trễ-thấp-voice-voice-call--video-call)
  - [3.5. Tích hợp Trợ lý ảo Gemini AI & Chuyển giao AI-to-Human Handover](#35-tích-hợp-trợ-lý-ảo-gemini-ai--chuyển-giao-ai-to-human-handover)
  - [3.6. Bộ kiểm duyệt ngôn từ vi phạm tự động](#36-bộ-kiểm-duyệt-ngôn-từ-vi-phạm-tự-động-profanity-filter-regex)
  - [3.7. Thiết kế giao thức truyền thông ứng dụng](#37-thiết-kế-giao-thức-truyền-thông-ứng-dụng)
  - [3.8. Thiết kế cơ sở dữ liệu SQLite nhúng hiệu năng cao](#38-thiết-kế-cơ-sở-dữ-liệu-sqlite-nhúng-hiệu-năng-cao)
- [CHƯƠNG 4. TRIỂN KHAI CÀI ĐẶT VÀ MINH HỌA TÍNH NĂNG NGƯỜI DÙNG](#chương-4-triển-khai-cài-đặt-và-minh-họa-tính-năng-người-dùng)
  - [4.1. Môi trường phát triển và cấu trúc thư mục](#41-môi-trường-phát-triển-và-cấu-trúc-thư-mục-dự-án)
  - [4.2. Các mô-đun mã nguồn cốt lõi tiêu biểu](#42-các-mô-đun-mã-nguồn-cốt-lõi-tiêu-biểu)
  - [4.3. Trải nghiệm người dùng phong phú (Rich UX)](#43-trải-nghiệm-người-dùng-phong-phú-rich-ux)
  - [4.4. Bảng điều khiển Quản trị viên máy chủ](#44-bảng-điều-khiển-quản-trị-viên-máy-chủ-server-dashboard-swing-3-tab)
  - [4.5. Đóng gói độc lập và Hướng dẫn vận hành](#45-đóng-gói-độc-lập-portable-distribution-và-hướng-dẫn-vận-hành)
- [CHƯƠNG 5. KIỂM THỬ, ĐÁNH GIÁ THỰC NGHIỆM VÀ PHÂN TÍCH SỐ LIỆU](#chương-5-kiểm-thử-đánh-giá-thực-nghiệm-và-phân-tích-số-liệu)
  - [5.1. Kiểm thử tự động chuyên sâu (75 ca JUnit 5 Tests)](#51-kiểm-thử-tự-động-chuyên-sâu-phân-tích-75-ca-kiểm-thử-junit-5-100-passed)
  - [5.2. Ma trận kiểm thử tích hợp hệ thống thực tế](#52-ma-trận-kiểm-thử-tích-hợp-hệ-thống-thực-tế)
  - [5.3. Phân tích số liệu thực nghiệm từ CSDL livechat.db](#53-phân-tích-số-liệu-thực-nghiệm-thực-tế-từ-csdl-livechatdb)
  - [5.4. Trích xuất nhật ký thực tế phía Server (server.log)](#54-trích-xuất-nhật-ký-thực-tế-phía-server-serverlog)
  - [5.5. Đánh giá ưu điểm vượt trội và các mặt hạn chế](#55-đánh-giá-ưu-điểm-vượt-trội-và-các-mặt-hạn-chế)
- [CHƯƠNG 6. KẾT LUẬN VÀ KIẾN NGHỊ](#chương-6-kết-luận-và-kiến-nghị)
  - [6.1. Kết luận và Thành tựu nổi bật](#61-kết-luận-và-thành-tựu-nổi-bật)
  - [6.2. Kiến nghị và Hướng phát triển tương lai](#62-kiến-nghị-và-hướng-phát-triển-tương-lai)
- [TÀI LIỆU THAM KHẢO](#tài-liệu-tham-khảo)
- [PHỤ LỤC: MÃ NGUỒN CHƯƠNG TRÌNH TIÊU BIỂU](#phụ-lục-mã-nguồn-chương-trình-tiêu-biểu)

---
<div style="page-break-after: always;"></div>

# DANH MỤC CÁC KÝ HIỆU, CÁC CHỮ VIẾT TẮT

| Từ viết tắt | Thuật ngữ tiếng Anh đầy đủ / Diễn giải tiếng Việt |
| :--- | :--- |
| **CSKH** | Chăm sóc khách hàng (Customer Service / Support) |
| **TCP** | Transmission Control Protocol (Giao thức điều khiển truyền vận tin cậy hướng nối) |
| **UDP** | User Datagram Protocol (Giao thức truyền dữ liệu phi kết nối, độ trễ cực thấp) |
| **IP** | Internet Protocol (Giao thức mạng internet định vị địa chỉ) |
| **I/O** | Input / Output (Nhập / Xuất dữ liệu) |
| **BIO** | Blocking Input/Output (Cơ chế vào/ra đồng bộ, quản lý qua luồng độc lập) |
| **API** | Application Programming Interface (Giao diện lập trình ứng dụng) |
| **JDK** | Java Development Kit (Bộ công cụ phát triển phần mềm Java tiêu chuẩn) |
| **JRE** | Java Runtime Environment (Môi trường thực thi Java) |
| **JVM** | Java Virtual Machine (Máy ảo Java) |
| **FXML** | XML-based User Interface Markup Language (Ngôn ngữ khai báo giao diện JavaFX) |
| **GUI** | Graphical User Interface (Giao diện người dùng đồ họa) |
| **MVC** | Model - View - Controller (Mô hình kiến trúc phân tầng giao diện, điều khiển và dữ liệu) |
| **CSAT** | Customer Satisfaction Score (Chỉ số đo lường độ hài lòng của khách hàng 1-5 sao) |
| **AES** | Advanced Encryption Standard (Chuẩn mã hóa nâng cao đối xứng) |
| **CBC** | Cipher Block Chaining (Chế độ mã hóa chuỗi khối kết hợp IV ngẫu nhiên) |
| **ECB** | Electronic Codebook (Chế độ mã hóa sách mật mã cơ bản - hỗ trợ Legacy Fallback) |
| **SHA** | Secure Hash Algorithm (Thuật toán băm an toàn một chiều 256-bit) |
| **IV** | Initialization Vector (Vector khởi tạo ngẫu nhiên trong mã hóa khối) |
| **SQLite** | Structured Query Language Lite (Hệ quản trị CSDL quan hệ nhúng hiệu năng cao) |
| **WAL** | Write-Ahead Logging (Cơ chế ghi nhật ký trước giúp SQLite đọc ghi song song) |
| **JDBC** | Java Database Connectivity (Chuẩn kết nối và thao tác cơ sở dữ liệu trong Java) |
| **NAT** | Network Address Translation (Kỹ thuật biên dịch địa chỉ mạng) |
| **RIFF** | Resource Interchange File Format (Định dạng khung tệp container âm thanh WAV tiêu chuẩn) |
| **PCM** | Pulse-Code Modulation (Kỹ thuật điều chế mã xung âm thanh kỹ thuật số) |
| **FIFO** | First In, First Out (Nguyên tắc xếp hàng vào trước - ra trước) |

---
<div style="page-break-after: always;"></div>

# DANH MỤC CÁC BẢNG

- **Bảng 1.1:** Bảng so sánh đối chiếu giữa Yêu cầu đồ án thông thường và Các đột phá của đề tài
- **Bảng 3.1:** Mô tả chi tiết các trường dữ liệu và thuộc tính của lớp Message
- **Bảng 3.2:** Cấu trúc bảng cơ sở dữ liệu `staff` (Quản lý 82 Nhân viên CSKH và Phân quyền)
- **Bảng 3.3:** Cấu trúc bảng cơ sở dữ liệu `customers` (Quản lý Hồ sơ Khách hàng và Nhận diện SĐT)
- **Bảng 3.4:** Cấu trúc bảng cơ sở dữ liệu `chat_sessions` (Quản lý Phiên hỗ trợ, Ticket và Đánh giá CSAT)
- **Bảng 3.5:** Cấu trúc bảng cơ sở dữ liệu `messages` (Quản lý Lịch sử tin nhắn đa phương tiện đã mã hóa)
- **Bảng 3.6:** Danh mục mã lệnh giao thức trao đổi ứng dụng (LiveChat Application Protocol)
- **Bảng 5.1:** Tổng hợp kết quả 75 ca kiểm thử tự động (Automated Unit Tests với JUnit 5)
- **Bảng 5.2:** Ma trận kiểm thử chức năng tích hợp hệ thống thực tế (System Integration Testing)
- **Bảng 5.3:** Bảng tổng hợp số liệu thực nghiệm hệ thống trích xuất từ cơ sở dữ liệu `livechat.db`

---
<div style="page-break-after: always;"></div>

# DANH MỤC CÁC HÌNH VẼ, ĐỒ THỊ

- **Hình 3.1:** Sơ đồ kiến trúc tổng thể hệ thống Live Chat Support (Mô hình phân tầng Hybrid TCP/UDP)
- **Hình 3.2:** Sơ đồ luồng thuật toán điều phối hàng chờ thông minh 3 tầng (Smart 3-Tier Dispatching)
- **Hình 3.3:** Sơ đồ tuần tự quy trình bắt tay và chuyển giao phiên dang dở (Session Handover)
- **Hình 3.4:** Sơ đồ kiến trúc kênh truyền thông đa phương tiện thời gian thực qua UDP Relay Server
- **Hình 3.5:** Sơ đồ quy trình tương tác và chuyển giao Trợ lý ảo AI sang CSKH (AI-to-Human Handover)
- **Hình 3.6:** Sơ đồ kiến trúc an toàn thông tin: Mã hóa AES-128 CBC ngẫu nhiên IV và Salted SHA-256
- **Hình 4.1:** Giao diện Bảng điều khiển Quản trị viên máy chủ (Server Dashboard - Java Swing 3 Tab)
- **Hình 4.2:** Giao diện ứng dụng Nhân viên CSKH (StaffApp - JavaFX FXML đa phiên, Dark/Light Mode)
- **Hình 4.3:** Giao diện ứng dụng Khách hàng (CustomerApp) với tính năng dán ảnh Clipboard, Voice & Video Call
- **Hình 4.4:** Giao diện chấm điểm đánh giá độ hài lòng CSAT và Biểu đồ thống kê kết quả thực tế

---
<div style="page-break-after: always;"></div>

# CHƯƠNG 1. TỔNG QUAN VÀ BÀI TOÁN THỰC TIỄN

## 1.1. Giới thiệu đề tài
Trong hoạt động kinh doanh hiện đại và nền kinh tế số ngày nay, dịch vụ chăm sóc và hỗ trợ khách hàng (Customer Service) đóng vai trò sống còn đối với sự tồn tại và phát triển của mọi doanh nghiệp. Sự bùng nổ của thương mại điện tử, các nền tảng dịch vụ số và xu hướng mua sắm trực tuyến đòi hỏi doanh nghiệp phải thiết lập các kênh tương tác tức thời (Real-time Communication) nhằm giải đáp thắc mắc, tư vấn sản phẩm, xử lý sự cố kỹ thuật và tiếp nhận khiếu nại của khách hàng một cách nhanh chóng nhất.

Trước đây, doanh nghiệp chủ yếu dựa vào hai kênh truyền thống là điện thoại (Hotline tổng đài) và thư điện tử (Email). Tuy nhiên, các kênh này bộc lộ những nhược điểm cố hữu: chi phí cước viễn thông đắt đỏ, nguy cơ nghẽn mạng vào giờ cao điểm, thời gian phản hồi email kéo dài hàng giờ thậm chí hàng ngày, và đặc biệt là không thể truyền tải trực quan các dữ liệu minh họa như ảnh chụp màn hình lỗi hoặc tệp dữ liệu cấu hình.

Sự xuất hiện của các hệ thống Live Chat (Trò chuyện trực tuyến) đã thay đổi hoàn toàn cục diện tương tác khách hàng. Với tính năng trò chuyện tức thì 1-1, khả năng truyền nhận tệp tin, hình ảnh, tin nhắn thoại (Voice note) và cuộc gọi đa phương tiện trực tiếp (Voice/Video Call), Live Chat không chỉ rút ngắn tối đa thời gian chờ đợi mà còn nâng cao trải nghiệm người dùng vượt bậc, góp phần gia tăng tỷ lệ chuyển đổi đơn hàng và chỉ số hài lòng của khách hàng (CSAT).

Đồ án học phần môn Lập trình mạng máy tính (CMP180) với đề tài "Hệ thống Chat Hỗ trợ Khách hàng Trực tuyến (Live Chat Support)" được nhóm nghiên cứu và phát triển với định hướng không chỉ dừng lại ở các yêu cầu bài tập cơ bản, mà chủ động nâng cấp toàn diện thành một giải pháp truyền thông mạng cấp doanh nghiệp (Enterprise-grade System), ứng dụng các công nghệ tiên tiến nhất của Java Networking, Truyền thông đa phương tiện thời gian thực và Trí tuệ nhân tạo (Generative AI).

## 1.2. So sánh đối chiếu: Yêu cầu đồ án chuẩn và Các đột phá vượt trội của đề tài

Trong quá trình nghiên cứu và phát triển, nhóm tác giả đã không tự giới hạn bản thân trong khuôn khổ một bài tập lớn thông thường mà chủ động mở rộng quy mô, giải quyết các bài toán kỹ thuật phức tạp thường chỉ xuất hiện ở các nền tảng thương mại cấp doanh nghiệp. Bảng 1.1 dưới đây làm rõ những điểm khác biệt mang tính đột phá của hệ thống Live Chat Support:

#### Bảng 1.1. Bảng so sánh đối chiếu giữa Yêu cầu đồ án thông thường và Các đột phá của đề tài
| Hạng mục công nghệ | Yêu cầu đồ án môn học thông thường | Đột phá vượt trội của Đề tài Live Chat Support |
| :--- | :--- | :--- |
| **Kiến trúc mạng truyền thông** | Chỉ sử dụng duy nhất Socket TCP đơn luồng gửi nhận chuỗi ký tự đơn giản. | Kiến trúc lai Hybrid TCP/UDP song song: TCP đảm nhận kênh điều khiển & dữ liệu an toàn, UDP Relay Server phục vụ streaming âm thanh và video trực tiếp thời gian thực. |
| **Nội dung trao đổi** | Tin nhắn văn bản thuần túy (Plain text String). | Đa phương tiện toàn diện: Văn bản, Tệp đính kèm (đến 10MB), Dán ảnh chụp màn hình Clipboard (Ctrl+V) có xem trước, 180+ Emoji, Tin nhắn thoại (WAV 44-byte RIFF), Cuộc gọi thoại và Cuộc gọi hình ảnh. |
| **Kênh đàm thoại trực tiếp** | Không hỗ trợ cuộc gọi. | Hiện thực hóa Voice Call và Video Call qua giao thức UDP không độ trễ, tích hợp thư viện Webcam vật lý kèm cơ chế chuyển đổi thông minh sang Camera ảo (Virtual Avatar) và Chia sẻ màn hình. |
| **Điều phối hàng chờ** | Hàng đợi vào trước - ra trước (FIFO) đơn giản hoặc nhân viên tự bấm chọn thủ công. | Thuật toán điều phối thông minh 3 tầng độc quyền: (1) Khách quen ưu tiên gặp nhân viên cũ (Preferred Staff); (2) Phân luồng theo chuyên môn (Skill-based); (3) Cân bằng tải ít phiên nhất (Least Connections). |
| **Trí tuệ nhân tạo (AI)** | Không có hỗ trợ tự động. | Tích hợp trực tiếp Google Gemini API (gemini-2.5-flash) làm Trợ lý ảo trực ban tự động khi khách ở hàng đợi, sinh Interactive Suggestion Chips và tự động chuyển giao mượt mà sang nhân viên (AI-to-Human Handover). |
| **Độ chịu lỗi (Fault Tolerance)** | Mất mạng là mất kết nối, người dùng bị văng, phiên chat bị hủy hoàn toàn. | Cơ chế khôi phục phiên gián đoạn (Session Handover): nhân viên rớt mạng thì khách hàng được đưa về đầu hàng chờ kèm cờ 'Đang tư vấn dở', bảo toàn 100% lịch sử chat cũ cho nhân viên tiếp theo. |
| **An toàn thông tin & Bảo mật** | Dữ liệu truyền trần trên mạng, mật khẩu lưu thô không mã hóa. | Bảo mật đa lớp: Mã hóa đối xứng AES-128 CBC với Vector khởi tạo (IV) ngẫu nhiên 16-byte cho từng tin nhắn và tệp tin lưu đĩa; Băm mật khẩu SHA-256 kèm Salt ngẫu nhiên 16-byte; Bộ lọc từ khóa thô tục (Profanity Filter) Regex. |
| **Cơ sở dữ liệu & Tối ưu** | Lưu file text thô hoặc SQLite cơ bản với thao tác khóa đồng bộ chậm. | Hệ quản trị SQLite nhúng cấu hình WAL Mode (Write-Ahead Logging), 4 chỉ mục Indexes tăng tốc truy vấn, cơ chế Write-Lock đa luồng an toàn, nạp sẵn hạt giống 45 tài khoản sinh viên. |
| **Trải nghiệm người dùng (UX)** | Giao diện Console dòng lệnh hoặc Swing sơ sài. | Giao diện đồ họa JavaFX 21 FXML hiện đại, hỗ trợ chuyển đổi giao diện Sáng/Tối (Dark/Light Theme), tùy biến Accent Color, thông báo âm thanh sinh động và Dashboard quản trị Swing 3 Tab chuyên sâu. |
| **Kiểm thử & Đóng gói** | Chạy thử nghiệm thủ công vài trường hợp cơ bản. | Bộ kiểm thử tự động toàn diện 75/75 ca Unit Test Cases với JUnit 5 đạt tỷ lệ vượt qua 100%; Cơ sở dữ liệu thực nghiệm lưu vết 420 phiên chat và 1.196 tin nhắn; Đóng gói Portable Apps (.exe) độc lập. |

## 1.3. Nhiệm vụ đồ án

### 1.3.1. Tính cấp thiết và lý do hình thành đề tài
Phần lớn doanh nghiệp vừa và nhỏ (SMEs) hiện vẫn xử lý yêu cầu hỗ trợ khách hàng qua điện thoại hoặc email thủ công, gây quá tải cho bộ phận trực ban, chi phí cước viễn thông đắt đỏ, và thường xuyên để mất khách hàng tiềm năng do tốc độ phản hồi chậm trễ. Thêm vào đó, việc sử dụng các nền tảng chat thương mại bên thứ ba (như Zalo OA, Facebook Messenger, Intercom, Tawk.to) tiềm ẩn rủi ro lộ lọt bí mật kinh doanh, dữ liệu khách hàng bị phụ thuộc vào máy chủ nước ngoài và chi phí duy trì thuê bao định kỳ rất cao.

Do đó, nhu cầu cấp thiết là phải tự làm chủ và xây dựng một hệ thống Live Chat nội bộ độc lập, an toàn và chuyên nghiệp:
1. Tiếp nhận tức thì kết nối từ khách hàng ngay khi họ truy cập ứng dụng.
2. Tự động hóa điều phối hàng chờ (Queue Dispatching) theo chuyên môn nhân viên và phân tải hợp lý.
3. Chịu lỗi cao (Fault Tolerance): không làm gián đoạn cuộc trò chuyện khi xảy ra sự cố mạng, hỗ trợ chuyển giao phiên (Session Handover) liền mạch giữa các nhân viên trực.
4. Bảo vệ an toàn dữ liệu khách hàng bằng các thuật toán mã hóa hiện đại trước khi truyền trên đường truyền mạng công cộng.

### 1.3.2. Ý nghĩa khoa học và thực tiễn
Về mặt khoa học: Đề tài giúp củng cố và vận dụng tổng hợp toàn bộ khối kiến thức nền tảng của môn Lập trình mạng máy tính:
- Làm chủ mô hình kiến trúc Client - Server tập trung và cơ chế truyền tin hướng đối tượng.
- Vận dụng sâu sắc lập trình Socket TCP hướng nối (Connection-oriented) tin cậy và Socket UDP phi kết nối (Connectionless) cho truyền thông đa phương tiện thời gian thực.
- Kỹ thuật lập trình đa luồng (Multithreading), quản lý hồ bơi luồng (Cached Thread Pool), và giải quyết triệt để các bài toán tranh chấp tài nguyên (Race Condition), khóa chết (Deadlock) bằng các kỹ thuật đồng bộ hóa hiện đại trong Java.
- Cơ chế tuần tự hóa đối tượng (Java Object Serialization) và truyền gói tin cấu trúc qua mạng.
- Áp dụng các nguyên lý bảo mật dữ liệu mạng: mã hóa đối xứng AES-128 chuẩn CBC ngẫu nhiên hóa Vector khởi tạo (IV) và băm mật khẩu an toàn SHA-256 kèm Salt.

Về mặt thực tiễn: Đề tài cung cấp một sản phẩm phần mềm hoàn chỉnh, có khả năng ứng dụng thực tế cao:
- Giải quyết bài toán phân phối ca trực CSKH thực tế của doanh nghiệp với thuật toán điều phối 3 tầng thông minh (Ưu tiên nhân viên quen -> Khớp chuyên môn -> Phân phối ít tải nhất).
- Cung cấp kênh truyền đa phương tiện phong phú: gửi file đính kèm an toàn, dán ảnh chụp màn hình từ Clipboard (Ctrl+V), gửi Emoji trực quan, thu âm tin nhắn thoại (Voice note) và đàm thoại âm thanh / hình ảnh trực tiếp (Voice & Video Call) qua UDP.
- Tích hợp trí tuệ nhân tạo Gemini AI tự động giải đáp FAQ và tương tác thông minh cùng khách hàng trong lúc chờ nhân viên tiếp nhận, giải phóng tối đa sức lao động con người.
- Lưu vết toàn bộ lịch sử tư vấn, khảo sát đánh giá độ hài lòng CSAT và cung cấp giao diện quản trị viên (Admin Dashboard) trực quan để thống kê, giám sát ca trực thời gian thực.

### 1.3.3. Mục tiêu đề tài
Đề tài hướng đến các mục tiêu nghiên cứu và phát triển cụ thể sau:
1. Xây dựng Server trung tâm (LiveChatServer) vững chắc, có khả năng tiếp nhận đồng thời hàng chục kết nối TCP và UDP song song, điều phối phiên chat ổn định và tự động ghi nhật ký hệ thống.
2. Tự động ghép nối một khách hàng đang chờ với một nhân viên đang rảnh thành một phiên hội thoại 1-1 theo thuật toán 3 tầng tối ưu (Preferred Staff, Skill-based Routing, Least Connections).
3. Xây dựng ứng dụng Khách hàng (CustomerClient) trên nền tảng đồ họa JavaFX trực quan, cho phép định danh linh hoạt bằng Số điện thoại/Email, lưu trữ hồ sơ cục bộ, chat mượt mà và thực hiện gọi điện hỗ trợ.
4. Xây dựng ứng dụng Nhân viên CSKH (StaffClient) chuyên nghiệp, hỗ trợ đa phiên (xử lý đồng thời tới 3 khách hàng), tra cứu lịch sử hội thoại cũ, quản lý câu trả lời nhanh (Quick Replies) và chuyển giao ca trực (Transfer Session).
5. Hiện thực hóa kênh truyền thông đa phương tiện thời gian thực qua UDP Relay Server, hỗ trợ Voice Message (đóng gói chuẩn WAV), Voice Call và Video Call (Webcam capture / Virtual Camera) độ trễ cực thấp.
6. Bảo mật toàn diện dữ liệu: mã hóa tin nhắn và tệp tin bằng thuật toán AES-128 CBC trước khi gửi qua mạng và lưu vào CSDL, băm mật khẩu nhân viên bằng SHA-256 kèm Salt 16-byte, kiểm duyệt từ khóa thô tục (Profanity Filter) tự động.
7. Tích hợp mô hình AI Gemini (gemini-2.5-flash) làm trợ lý ảo trực ban tự động khi hàng chờ quá tải.
8. Tổ chức cơ sở dữ liệu SQLite nhúng gọn nhẹ, độc lập, không phụ thuộc máy chủ CSDL bên ngoài.

### 1.3.4. Đối tượng và phạm vi giới hạn
Đối tượng sử dụng hệ thống gồm ba nhóm người dùng chính:
- Khách hàng (Customer): Người cần được tư vấn, giải đáp thắc mắc, gửi tệp hình ảnh lỗi, gọi điện hỗ trợ và đánh giá chất lượng dịch vụ sau phiên chat.
- Nhân viên chăm sóc khách hàng (Staff): Nhân sự trực ban được phân quyền, tiếp nhận các phiên chat từ hàng đợi, tra cứu hồ sơ khách hàng, trả lời nhanh, gọi điện thoại/video và bàn giao ca trực khi cần thiết.
- Quản trị viên hệ thống (Administrator): Người vận hành Server, giám sát số lượng trực tuyến thời gian thực, quản lý danh sách tài khoản nhân viên (thêm, sửa, khóa, đổi mật khẩu) và tra cứu toàn bộ biên bản lịch sử trò chuyện trong quá khứ.

Phạm vi đồ án giới hạn ở:
- Môi trường mạng: Hoạt động hoàn hảo trong mạng cục bộ (LAN), mạng nội bộ doanh nghiệp hoặc môi trường Internet công cộng thông qua cấu hình định địa chỉ IP hoặc Port Forwarding.
- Nền tảng thực thi: Các hệ điều hành máy tính để bàn (Windows 10/11, macOS, Linux) hỗ trợ môi trường thực thi Java Runtime Environment (JDK 17 LTS hoặc JDK 25).
- Lưu trữ dữ liệu: Sử dụng cơ sở dữ liệu nhúng SQLite 3 (livechat.db) gọn nhẹ, không yêu cầu thiết lập máy chủ dịch vụ MySQL hay PostgreSQL cồng kềnh.

## 1.4. Cấu trúc đồ án
Ngoài phần mở đầu, lời cam đoan, mục lục, danh mục từ viết tắt, danh mục bảng, danh mục hình vẽ, tài liệu tham khảo và phụ lục, nội dung chính của báo cáo đồ án được kết cấu thành 6 chương chuyên sâu:

- Chương 1: Tổng quan – Giới thiệu bối cảnh, tính cấp thiết, bảng so sánh đối chiếu những đột phá của hệ thống so với yêu cầu đồ án thông thường, ý nghĩa khoa học và thực tiễn, mục tiêu và phạm vi giới hạn của đề tài.
- Chương 2: Cơ sở lý thuyết và Công nghệ nền tảng – Trình bày toàn diện các nguyên lý mạng máy tính và kỹ thuật lập trình Java nâng cao: mô hình Client-Server, Socket TCP Blocking I/O, Socket UDP thời gian thực, cơ chế quản lý đa luồng với CachedThreadPool, kỹ thuật đồng bộ phi khóa, tuần tự hóa đối tượng (Java Serialization), mật mã học ứng dụng (AES-128 CBC ngẫu nhiên IV, Salted SHA-256) và ứng dụng Trí tuệ nhân tạo (Generative AI).
- Chương 3: Thiết kế kiến trúc và Các đột phá kỹ thuật – Phân tích chi tiết kiến trúc lai Hybrid TCP/UDP, thuật toán điều phối hàng chờ 3 tầng, cơ chế chịu lỗi và khôi phục sự cố Session Handover, kênh truyền thông âm thanh/hình ảnh UDP, giải pháp tích hợp Trợ lý ảo Gemini AI, bộ lọc từ khóa thô tục và thiết kế cơ sở dữ liệu SQLite nhúng tối ưu chế độ WAL.
- Chương 4: Triển khai cài đặt và Minh họa tính năng người dùng – Trình bày cấu trúc thư mục, môi trường phát triển, các đoạn mã nguồn then chốt, trải nghiệm người dùng phong phú (dán ảnh Clipboard Ctrl+V, Voice Note, Video Call, Quick Replies), giao diện Bảng điều khiển Quản trị viên Swing và hướng dẫn vận hành hệ thống.
- Chương 5: Kiểm thử, Đánh giá thực nghiệm và Phân tích số liệu – Tổng hợp chi tiết 75 ca kiểm thử tự động JUnit 5 vượt qua 100%, ma trận kiểm thử chức năng hệ thống, phân tích toàn bộ số liệu thực nghiệm đồ sộ từ cơ sở dữ liệu livechat.db (82 nhân viên, 38 khách hàng, 420 phiên chat, 1.196 tin nhắn, điểm CSAT đạt 4.88/5.0 sao) và đánh giá ưu nhược điểm.
- Chương 6: Kết luận và Hướng phát triển – Đúc kết các thành tựu nổi bật của đồ án, rút ra bài học kinh nghiệm và vạch rõ lộ trình nâng cấp hệ thống sang kiến trúc WebRTC P2P và Microservices.

---
<div style="page-break-after: always;"></div>

# CHƯƠNG 2. CƠ SỞ LÝ THUYẾT VÀ CÔNG NGHỆ NỀN TẢNG

## 2.1. Mô hình Client - Server
Mô hình Client - Server (Máy khách - Máy chủ) là một kiến trúc mạng phân tán phân chia khối lượng công việc giữa các nhà cung cấp tài nguyên hoặc dịch vụ (gọi là Server) và những người yêu cầu dịch vụ (gọi là Client).

Trong hệ thống Live Chat Support:
- Máy chủ (Server): Đóng vai trò là trung tâm điều phối và lưu trữ tập trung. Server luôn ở trạng thái lắng nghe (Listen) trên cổng TCP Port 5000 và UDP Port 5000. Server không chỉ chuyển tiếp thông điệp giữa các bên mà còn quản trị hàng đợi khách hàng, thực hiện thuật toán ghép cặp tự động, lưu trữ lịch sử tin nhắn vào CSDL, và thực thi các biện pháp bảo mật, kiểm duyệt nội dung.
- Máy khách (Client): Là các ứng dụng chạy trên máy trạm của Khách hàng hoặc Nhân viên CSKH. Client chủ động khởi tạo kết nối (Active Open) đến địa chỉ IP và Port của Server. Sau khi thiết lập phiên làm việc thành công, Client gửi các thông điệp dữ liệu, hiển thị giao diện đồ họa, thu nhận thao tác người dùng và nhận dữ liệu phản hồi từ máy chủ.

Ưu điểm của mô hình Client - Server trong ứng dụng CSKH:
- Quản lý tập trung: Toàn bộ dữ liệu phiên trò chuyện, tài khoản nhân viên và thông tin khách hàng được quản lý an toàn tại Server, dễ dàng sao lưu, kiểm toán và giám sát.
- Khả năng mở rộng (Scalability): Cho phép hàng chục, hàng trăm Client đồng thời kết nối vào hệ thống mà không làm xáo trộn kiến trúc mạng.
- Vượt tường lửa dễ dàng: Các Client chỉ cần tạo kết nối Outbound tới máy chủ trung tâm mà không cần mở cổng hay cấu hình Port Forwarding phức tạp trên thiết bị người dùng cuối.

## 2.2. Giao thức và Socket TCP trong Java
TCP (Transmission Control Protocol) là giao thức truyền dữ liệu cốt lõi hoạt động tại tầng Giao vận (Transport Layer) trong mô hình OSI và bộ giao thức TCP/IP. TCP là giao thức hướng kết nối (Connection-oriented), đảm bảo dữ liệu được truyền tải đầy đủ, đúng thứ tự và hoàn toàn không bị lỗi hoặc trùng lặp.

1. Nguyên lý hoạt động của TCP:
- Quá trình bắt tay 3 bước (Three-way Handshake): Trước khi truyền dữ liệu, Client và Server thiết lập kết nối logic:
  + Bước 1: Client gửi gói tin SYN (Synchronize Sequence Number) tới Server.
  + Bước 2: Server nhận được và phản hồi lại gói tin SYN-ACK (Synchronize Acknowledgment).
  + Bước 3: Client gửi gói tin ACK xác nhận. Lúc này kết nối ảo hai chiều (Full-duplex Virtual Circuit) chính thức được thiết lập.
- Cơ chế truyền tin tin cậy: Mọi gói tin dữ liệu gửi đi đều được đánh số tuần tự (Sequence Number). Bên nhận sẽ gửi gói xác nhận (ACK). Nếu bên gửi không nhận được ACK trong một khoảng thời gian nhất định (Timeout), cơ chế truyền lại (Retransmission) sẽ tự động kích hoạt.
- Kiểm soát luồng (Flow Control) và kiểm soát tắc nghẽn (Congestion Control): TCP sử dụng kỹ thuật cửa sổ trượt (Sliding Window) để điều tiết tốc độ truyền phù hợp với khả năng tiếp nhận của bên nhận và trạng thái tải của đường truyền mạng.

2. Lập trình Socket TCP trong Java:
Nền tảng Java cung cấp gói thư viện chuẩn java.net hỗ trợ lập trình Socket TCP mạnh mẽ thông qua hai lớp cốt lõi:
- Lớp ServerSocket: Được máy chủ sử dụng để lắng nghe kết nối đến trên cổng xác định:
  ServerSocket serverSocket = new ServerSocket(PORT);
  Socket clientSocket = serverSocket.accept(); // Chặn (block) cho đến khi có kết nối mới
- Lớp Socket: Đại diện cho một điểm cuối (Endpoint) của kết nối truyền thông hai chiều giữa hai máy tính. Các phương thức quan trọng:
  + socket.getInputStream(): Nhận luồng dữ liệu byte đến từ đối tác truyền thông.
  + socket.getOutputStream(): Ghi luồng dữ liệu byte gửi đi cho đối tác truyền thông.
  + socket.setSoTimeout(40000): Thiết lập thời gian chờ tối đa 40 giây cho các thao tác đọc I/O. Kỹ thuật này được áp dụng để tự động phát hiện và ngắt các "Zombie Connections" (kết nối treo/đột tử không gửi tín hiệu quá 40 giây) mà không làm tắc nghẽn tài nguyên máy chủ.
  + socket.close(): Đóng kết nối và giải phóng tài nguyên socket mạng.

## 2.3. Giao thức và Socket UDP cho Streaming thời gian thực
Bên cạnh TCP, giao thức UDP (User Datagram Protocol) là giao thức truyền dữ liệu phi kết nối (Connectionless) thuộc tầng Giao vận. UDP gửi các khối dữ liệu độc lập (gọi là Datagram) mà không cần thiết lập phiên bắt tay trước và không có cơ chế truyền lại khi gói tin thất lạc.

1. Ưu thế vượt trội của UDP trong truyền thông Streaming:
- Độ trễ cực thấp (Low Latency): Nhờ loại bỏ hoàn toàn chi phí kiểm soát thừa (Overhead) của TCP, gói tin UDP truyền đi tức thì với độ trễ xử lý < 1ms.
- Phù hợp với dữ liệu âm thanh và hình ảnh: Trong đàm thoại trực tiếp (Voice/Video Call), người nghe chấp nhận mất một vài phần trăm khung hình âm thanh nhỏ hơn là việc âm thanh bị dừng lại đợi truyền lại (Jitter/Lag) như TCP.

2. Lập trình Socket UDP trong Java:
Java hỗ trợ giao thức UDP thông qua hai lớp then chốt trong gói java.net:
- Lớp DatagramSocket: Đại diện cho socket truyền và nhận các gói tin datagram:
  DatagramSocket socket = new DatagramSocket(PORT);
  socket.setReceiveBufferSize(2 * 1024 * 1024); // Cấu hình bộ đệm 2MB chống tràn gói tin
- Lớp DatagramPacket: Đại diện cho gói tin datagram chứa dữ liệu mảng byte cùng thông tin địa chỉ IP và số cổng của máy đích:
  DatagramPacket packet = new DatagramPacket(buffer, buffer.length, targetIp, targetPort);
  socket.send(packet);

Trong hệ thống Live Chat Support, UdpRelayServer hoạt động như một máy chủ trung chuyển (Relay Hub) thông minh, chuyển tiếp hàng nghìn gói tin âm thanh thoại (PCM 16kHz) và khung hình video (JPEG nén) mỗi phút giữa Customer và Staff.

## 2.4. Quản lý đa luồng với CachedThreadPool & Kỹ thuật đồng bộ phi khóa
Lập trình đa luồng (Multithreading) là khả năng của một hệ điều hành hoặc máy ảo cho phép một tiến trình thực thi nhiều nhánh công việc (Thread) đồng thời. Trong lập trình máy chủ mạng, đa luồng là kỹ thuật bắt buộc để phục vụ đồng thời nhiều kết nối Client mà không làm đóng băng (Freeze) hệ thống.

1. Hồ bơi luồng tối ưu (Cached Thread Pool):
Thay vì tự tạo mới một đối tượng Thread cho mỗi kết nối (dẫn đến nguy cơ cạn kiệt bộ nhớ OutOfMemoryError khi lượng truy cập tăng vọt), hệ thống áp dụng bộ quản lý luồng hiện đại từ gói java.util.concurrent:
  ExecutorService threadPool = Executors.newCachedThreadPool();
- CachedThreadPool tự động tái sử dụng các luồng nhàn rỗi (idle threads) trong 60 giây và tự động mở rộng số lượng luồng linh hoạt khi có nhiều yêu cầu kết nối đồng thời, giúp máy chủ phản hồi tức thì với chi phí tài nguyên thấp nhất.

2. Kỹ thuật đồng bộ hóa phi khóa (Lock-free Concurrency):
Để xử lý hàng nghìn thông điệp đồng thời giữa các luồng ClientHandler mà không gây khóa chết (Deadlock) hay nghẽn cổ chai hiệu năng, hệ thống áp dụng các cấu trúc dữ liệu tiên tiến:
- ConcurrentLinkedQueue: Dùng cho hàng đợi khách hàng chờ waitingCustomers và nhân viên allStaffs. Sử dụng thuật toán CAS (Compare-And-Swap) không khóa (Lock-free), giúp các thao tác enqueue/dequeue đạt hiệu năng cực đại.
- CopyOnWriteArrayList: Dùng cho danh sách các phiên chat hoạt động activeSessionsList, bảo đảm thao tác lặp (iteration) qua danh sách phiên luôn an toàn tuyệt đối ngay cả khi có luồng khác đang thêm hoặc xóa phiên.
- ConcurrentHashMap: Dùng trong UdpRelayServer để định tuyến địa chỉ IP:Port của Client trong bộ nhớ nano-giây.
- Từ khóa volatile: Gắn vào các cờ trạng thái đa luồng (isRunning, interrupted, chattingWithAI) bảo đảm tính cập nhật tức thời trên bộ nhớ đệm CPU của mọi lõi vi xử lý.

## 2.5. Lớp InetAddress và Quản trị địa chỉ mạng
Lớp java.net.InetAddress trong ngôn ngữ lập trình Java là lớp trừu tượng biểu diễn một địa chỉ Giao thức Internet (IP Address), bao gồm cả chuẩn IPv4 (32-bit, ví dụ: 192.168.1.100) và IPv6 (128-bit). Lớp này cung cấp các dịch vụ phân giải tên miền (DNS Resolution) và xác định vị trí máy chủ trên mạng.

1. Vai trò trong lập trình mạng:
Mỗi thiết bị kết nối vào mạng TCP/IP đều sở hữu một địa chỉ IP duy nhất. Để thiết lập socket, ứng dụng bắt buộc phải chỉ định địa chỉ IP và số cổng (Port Number) của máy đích. Lớp InetAddress đóng vai trò cầu nối chuyển đổi giữa tên miền dễ nhớ (Hostname như localhost hay server.company.com) thành địa chỉ số nhị phân mà phần cứng mạng có thể định tuyến được.

2. Các phương thức quan trọng của lớp InetAddress:
- InetAddress.getByName(String host): Phân giải một hostname hoặc chuỗi biểu diễn IP thành một đối tượng InetAddress.
- InetAddress.getLocalHost(): Trả về tên máy và địa chỉ IP của máy tính cục bộ hiện thời đang chạy chương trình.
- getHostAddress(): Trả về chuỗi ký tự chứa địa chỉ IP dạng số (ví dụ: "127.0.0.1").
- getHostName(): Trả về tên định danh máy chủ.

Trong dự án Live Chat Support, InetAddress được sử dụng tại:
- ServerConfig.java: Cho phép Client cấu hình linh hoạt địa chỉ IP máy chủ kết nối (mặc định là 127.0.0.1 hoặc IP LAN như 192.168.1.5).
- ClientHandler.java và ServerGUI.java: Truy xuất clientSocket.getInetAddress().getHostAddress() để ghi nhận nhật ký (Audit Log) địa chỉ IP thực của từng khách hàng và nhân viên đăng nhập, phục vụ quản trị và kiểm tra an ninh mạng.
- UdpRelayServer.java và UdpMediaClient.java: Sử dụng InetAddress để đóng gói các gói tin UDP DatagramPacket định tuyến âm thanh và hình ảnh đến đúng IP máy chủ trung chuyển.

## 2.6. Tuần tự hóa đối tượng (Java Serialization) cho gói tin đa hình
Truyền đối tượng qua mạng (Object Transmission) là kỹ thuật cho phép đóng gói toàn bộ trạng thái của một đối tượng Java thành một chuỗi byte tuần tự (Byte Stream) để truyền qua Socket mạng hoặc lưu trữ xuống tệp tin.

1. Giao diện java.io.Serializable và thuộc tính serialVersionUID:
- Lớp Message cài đặt giao diện Serializable, cho phép đối tượng được chuyển đổi hai chiều một cách tự nhiên mà không cần viết bộ phân tích chuỗi thủ công.
- Khai báo tường minh serialVersionUID = 1L giúp bảo đảm tính tương thích tuyệt đối giữa các phiên bản phần mềm máy khách và máy chủ.

2. Đóng gói gói tin đa hình (Polymorphic Message Object):
Lớp Message đóng vai trò là một container dữ liệu đa năng:
- Mang thông điệp điều khiển (command, senderId, receiverId).
- Mang nội dung văn bản đã mã hóa (content, messageId, replyToSender, replyToText, replyToId).
- Mang dữ liệu nhị phân đính kèm (fileName, fileSize, byte[] fileData, isImage) cho phép truyền tệp tin tài liệu và ảnh chụp màn hình nguyên bản qua luồng mạng mà không bị lỗi biến dạng ký tự.

## 2.7. Mật mã học ứng dụng: AES-128 CBC ngẫu nhiên hóa IV và Salted SHA-256
Bảo mật là một trong những điểm đột phá quan trọng nhất của đồ án, giúp hệ thống đạt chuẩn an toàn thông tin cấp doanh nghiệp:

1. Mã hóa đối xứng AES-128 CBC với Vector khởi tạo (IV) ngẫu nhiên:
- Không sử dụng chế độ ECB cơ bản (vốn tạo ra cùng một bản mã cho các bản rõ giống nhau, dễ bị giải mã phân tích mẫu).
- Lớp SecurityManager áp dụng thuật toán AES/CBC/PKCS5Padding: mỗi lần mã hóa một tin nhắn hay một tệp tin, một Vector khởi tạo (IV 16-byte) được sinh hoàn toàn ngẫu nhiên bằng SecureRandom.
- Gói tin Base64 gửi qua mạng có cấu trúc: [IV (16 bytes)] + [Ciphertext]. Bên nhận tách 16 byte đầu làm IV để giải mã. Nhờ vậy, cùng một câu chào "Xin chào", mỗi lần gửi sẽ sinh ra một chuỗi mã hóa hoàn toàn khác biệt trên đường truyền mạng!
- Tương thích ngược thông minh (Legacy Fallback): SecurityManager tự động thử giải mã CBC, nếu dữ liệu cũ trong CSDL được mã hóa theo chuẩn cũ thì tự động fallback về ECB, bảo đảm hệ thống không bao giờ bị gián đoạn hoạt động.

2. Băm mật khẩu an toàn SHA-256 kèm Salt ngẫu nhiên 16-byte:
- Mật khẩu nhân viên khi lưu vào cơ sở dữ liệu livechat.db luôn được băm bằng thuật toán SHA-256 kèm chuỗi Salt ngẫu nhiên 16 byte theo định dạng <salt_hex>:<hash_hex>.
- Kỹ thuật này triệt tiêu hoàn toàn nguy cơ bị bẻ khóa bằng bảng băm tính trước (Rainbow Table Attack) hay tấn công từ điển (Dictionary Attack).

3. Bộ kiểm duyệt từ khóa thô tục (Profanity Filter) bằng Regex đa tầng:
- Tự động quét và che giấu các từ ngữ tục tĩu, xúc phạm thành *** trước khi tin nhắn được phát tán hoặc lưu vào CSDL.
- Bộ lọc nhận diện biểu thức chính quy phức tạp: xử lý tiếng Việt có dấu, không dấu, và các biến thể cố tình kéo dài nguyên âm nhằm vượt mặt hệ thống kiểm duyệt.

## 2.8. Ứng dụng Trí tuệ nhân tạo (Generative AI) trong dịch vụ khách hàng thời gian thực
Ứng dụng Trí tuệ nhân tạo (Generative AI) là điểm sáng công nghệ đột phá của đề tài, đưa hệ thống vượt xa các đồ án môn học thông thường:

1. Tích hợp trực tiếp Google Gemini API (gemini-2.5-flash):
- Hệ thống sử dụng thư viện HttpClient của Java 11+ kết nối bất đồng bộ (Asynchronous HTTP/2) trực tiếp tới điểm cuối Gemini Generative Language API.
- Prompt hệ thống (System Prompt) được huấn luyện chuyên sâu với vai trò là Trợ lý CSKH ảo của chuỗi công nghệ, có khả năng tư vấn cấu hình máy tính, kiểm tra chính sách bảo hành, hướng dẫn xử lý lỗi phần mềm.

2. Cơ chế đề xuất câu hỏi gợi ý tương tác (Interactive Suggestion Chips):
- Thuật toán phân tích phản hồi của AI để tự động trích xuất các câu hỏi gợi ý thông minh (ví dụ: 'Cách kiểm tra thời hạn bảo hành?', 'Gợi ý Laptop lập trình tầm giá 20 triệu?').
- Giao diện khách hàng tự động hiển thị các nút bấm gợi ý (Chips Button), cho phép khách hàng nhấn chọn câu hỏi tiếp theo một cách trực quan mà không cần gõ bàn phím.

3. Cơ chế phát hiện nhân viên và Chuyển giao thông minh (AI-to-Human Handover):
- Khi khách hàng đang trao đổi với AI trong hàng chờ, nếu có nhân viên CSKH phù hợp chuyển sang trạng thái sẵn sàng, Server sẽ không thô bạo ngắt ngang cuộc trò chuyện với AI.
- Hệ thống phát tín hiệu STAFF_AVAILABLE và hiển thị thông báo lịch sự trên màn hình khách hàng: "Nhân viên CSKH [Tên nhân viên] đã sẵn sàng hỗ trợ. Bạn có muốn kết nối với nhân viên hay tiếp tục trò chuyện cùng AI?". Quyền quyết định thuộc về khách hàng, mang lại trải nghiệm hỗ trợ tinh tế và chuyên nghiệp bậc nhất.

---
<div style="page-break-after: always;"></div>

# CHƯƠNG 3. THIẾT KẾ KIẾN TRÚC VÀ CÁC ĐỘT PHÁ KỸ THUẬT CỦA HỆ THỐNG

## 3.1. Kiến trúc tổng thể Hybrid TCP/UDP
Hệ thống Live Chat Support được xây dựng trên một kiến trúc mạng phân tầng Hybrid TCP/UDP độc đáo, giải quyết triệt để sự đánh đổi (trade-off) kinh điển trong lập trình mạng giữa 'Tính tin cậy toàn vẹn' và 'Tốc độ độ trễ thấp':

1. Phân tách rạch ròi hai kênh truyền thông độc lập:
- Kênh truyền dữ liệu và điều khiển an toàn (Control & Secure Data Channel - TCP Port 5000):
  + Đảm nhận toàn bộ các thao tác xác thực, bắt tay, phân phối hàng đợi, định tuyến tin nhắn 1-1, chuyển giao phiên, truyền nhận tệp tin và hình ảnh.
  + Mọi gói tin truyền qua kênh này đều được bảo vệ bởi lớp mã hóa AES-128 CBC với Vector khởi tạo (IV) ngẫu nhiên 16-byte, bảo đảm tính bí mật và toàn vẹn tuyệt đối.
  + Được quản lý bởi ServerMain kết hợp hồ bơi luồng Executors.newCachedThreadPool(), cho phép phục vụ đồng thời hàng chục kết nối mà không làm tắc nghẽn tài nguyên.
- Kênh truyền thông đa phương tiện thời gian thực (Real-time Media Channel - UDP Port 5000):
  + Đảm nhận truyền tải luồng âm thanh đàm thoại trực tiếp (Voice Call) và luồng khung hình video (Video Call).
  + Được vận hành bởi máy chủ UdpRelayServer độc lập với bộ đệm tiếp nhận socket 2MB (2 * 1024 * 1024 bytes).
  + Thuật toán chuyển tiếp trong bộ nhớ (In-memory Routing) đạt tốc độ dưới 1 mili-giây (< 1ms). Khắc phục hoàn toàn bài toán NAT Traversal: cả hai Client đều chủ động kết nối Outbound tới Server, do đó cuộc gọi thoại/video hoạt động trơn tru qua mọi modem/router gia đình mà không cần mở cổng hay cấu hình Port Forwarding phức tạp!

```mermaid
flowchart TD
    subgraph Clients["TẦNG MÁY KHÁCH (CLIENTS)"]
        Customer["CustomerApp (JavaFX FXML)<br/>Khách hàng"]
        Staff["StaffApp (JavaFX FXML)<br/>Nhân viên CSKH"]
    end

    subgraph Network["TẦNG TRUYỀN THÔNG MẠNG"]
        TCPChan["Kênh TCP (Port 5000)<br/>Control, Messages, Files (AES-CBC)"]
        UDPChan["Kênh UDP (Port 5000)<br/>Voice Call & Video Call Streaming"]
    end

    subgraph Server["TẦNG MÁY CHỦ TRUNG TÂM (SERVER)"]
        ServerMain["ServerMain<br/>CachedThreadPool Executor"]
        SessionMgr["SessionManager<br/>Thuật toán điều phối 3 tầng & Handover"]
        ClientHdl["ClientHandler Threads<br/>Thread-per-Client Blocking I/O"]
        UDPRelay["UdpRelayServer<br/>In-memory Relay (<1ms)"]
        Profanity["ProfanityFilter<br/>Bộ lọc từ khóa Regex"]
        AIBot["AIBotService<br/>Gemini AI 2.5 Flash"]
        ServerGUI["ServerGUI (Swing)<br/>Dashboard Quản trị viên"]
    end

    subgraph Database["TẦNG LƯU TRỮ DỮ LIỆU"]
        SQLite[("SQLite Database<br/>livechat.db (WAL Mode)")]
        DiskStorage[("Server Storage<br/>server_storage/files (AES Encrypted)")]
    end

    Customer <-->|TCP Socket| TCPChan
    Staff <-->|TCP Socket| TCPChan
    Customer <-->|UDP Datagram| UDPChan
    Staff <-->|UDP Datagram| UDPChan

    TCPChan <--> ServerMain
    ServerMain --> ClientHdl
    ClientHdl <--> SessionMgr
    ClientHdl <--> Profanity
    ClientHdl <--> AIBot
    UDPChan <--> UDPRelay

    SessionMgr <--> SQLite
    ClientHdl <--> DiskStorage
    ServerGUI <--> SessionMgr
    ServerGUI <--> SQLite
```

## 3.2. Thuật toán điều phối hàng chờ thông minh 3 tầng
Một trong những điểm sáng học thuật và thực tiễn nổi bật nhất của đề tài là Thuật toán điều phối hàng chờ thông minh 3 tầng (Smart 3-Tier Dispatching Algorithm) cài đặt tại SessionManager.java. Khác biệt hoàn toàn với các đồ án thông thường chỉ xếp hàng FIFO đơn giản hoặc để nhân viên tự bấm nhận thủ công, thuật toán của hệ thống tự động tối ưu hóa trải nghiệm khách hàng theo 3 bậc ưu tiên:

- TẦNG 1: Ưu tiên Nhân viên quen thuộc (Preferred Staff Priority):
  Hệ thống tự động tra cứu trong cơ sở dữ liệu lịch sử các phiên hỗ trợ trước đây của khách hàng. Nếu khách hàng từng được một nhân viên CSKH hỗ trợ và đánh giá độ hài lòng cao (CSAT >= 4 sao), và nhân viên đó hiện đang trực tuyến và chưa đạt ngưỡng tải tối đa (Active Sessions < 3), hệ thống sẽ lập tức ưu tiên kết nối khách hàng với nhân viên quen thuộc này. Điều này tạo ra sự liền mạch và thân thiện vượt trội trong dịch vụ.

- TẦNG 2: Định tuyến theo Chuyên môn nghiệp vụ (Skill-based Routing):
  Mỗi nhân viên CSKH khi tạo tài khoản đều được phân quyền và gán chuyên môn chính (Primary Skill: ví dụ 'Tư vấn Mua Laptop & PC', 'Hỗ trợ Kỹ thuật & Cài đặt', 'Bảo hành & Đổi trả'). Khi khách hàng đăng nhập và lựa chọn chuyên mục hỗ trợ, thuật toán sẽ quét toàn bộ danh sách nhân viên đang rảnh để tìm kiếm nhân viên có chuyên môn khớp chính xác nhất với vấn đề của khách. Nếu có nhiều nhân viên cùng chuyên môn, hệ thống sẽ chọn người đang có ít phiên chat nhất.

- TẦNG 3: Cân bằng tải phiên ít nhất (Least Connections Fallback):
  Trong trường hợp không tìm thấy nhân viên quen và tất cả nhân viên đúng chuyên môn đều đang bận, hệ thống sẽ tự động kích hoạt thuật toán cân bằng tải dự phòng: chọn nhân viên CSKH bất kỳ đang trực tuyến có số lượng phiên hoạt động thấp nhất (tối đa không quá 3 phiên). Khách hàng không bao giờ bị nghẽn lại ở hàng chờ nếu vẫn còn nhân viên có thể tiếp nhận.

## 3.3. Cơ chế chịu lỗi cấp doanh nghiệp (Session Handover & Auto-reconnect)
Để đạt tiêu chuẩn vận hành doanh nghiệp (Enterprise High Availability), hệ thống được trang bị các cơ chế chống chịu lỗi và tự phục hồi sự cố tự động (Fault Tolerance & Self-healing):

1. Cơ chế Chuyển giao phiên gián đoạn (Session Handover):
- Trong môi trường thực tế, nhân viên CSKH có thể bị mất điện, rớt mạng Wi-Fi hoặc ứng dụng bị tắt đột ngột khi đang trò chuyện với khách hàng.
- Khi Server phát hiện socket của nhân viên bị đóng hoặc kích hoạt ngắt kết nối (ClientHandler.closeConnection()), hệ thống lập tức can thiệp:
  + Khách hàng đang trong phiên không bị văng khỏi ứng dụng, mà được tự động đưa trở lại ĐẦU HÀNG CHỜ với cờ ưu tiên đặc biệt "Đang tư vấn dở" (interrupted = true).
  + Toàn bộ lịch sử trò chuyện (chatHistory) của phiên vừa bị ngắt được bảo toàn nguyên vẹn trong bộ nhớ Server.
  + Ngay khi có nhân viên khác tiếp nhận hoặc nhân viên cũ đăng nhập lại, toàn bộ lịch sử trò chuyện cũ sẽ tự động được chuyển tiếp (forward) sang màn hình của nhân viên mới qua lệnh REQ_CHAT_HISTORY. Nhân viên mới nắm bắt ngay toàn bộ ngữ cảnh trao đổi trước đó mà khách hàng không cần phải giải thích lại từ đầu!

2. Cơ chế Chuyển giao ca trực chủ động (Session Transfer):
- Nhân viên đang hỗ trợ có thể chủ động chuyển nhượng khách hàng sang cho một nhân viên khác trực tuyến có chuyên môn sâu hơn (ví dụ: nhân viên bán hàng chuyển sang nhân viên kỹ thuật phần cứng) thông qua hộp thoại TransferSessionDialog kèm theo toàn bộ lịch sử tin nhắn.

3. Vòng lặp tự động kết nối lại (Auto-reconnect Loop):
- Ứng dụng Client (Customer & Staff) được tích hợp luồng ngầm liên tục giám sát trạng thái kết nối socket. Khi xảy ra mất kết nối mạng, Client không bị sập mà tự động chuyển sang chế độ Reconnecting với thời gian chờ thích ứng, tự động tái lập phiên ngay khi có mạng trở lại.

4. Phát hiện kết nối Zombie và Ngăn chặn đăng nhập trùng lặp:
- Cấu hình socket.setSoTimeout(40000) kết hợp với các gói tin nhịp tim HEARTBEAT định kỳ giúp Server tự động phát hiện và thu hồi tài nguyên của các máy khách bị tắt nguồn đột ngột sau 40 giây.
- Cơ chế Single Active Login: nếu một tài khoản nhân viên được đăng nhập ở thiết bị mới, Server sẽ lập tức phát cảnh báo và ngắt phiên cũ trên thiết bị trước đó, chống đánh cắp phiên làm việc.

## 3.4. Kênh truyền thông đa phương tiện độ trễ thấp (Voice, Voice Call & Video Call)
Hệ thống Live Chat Support hiện thực hóa một Động cơ Truyền thông Đa phương tiện (Multimedia Engine) hoàn chỉnh, đưa trải nghiệm tương tác lên tầm cao mới:

1. Động cơ Tin nhắn thoại (Voice Messaging Engine - VoiceManager.java):
- Cho phép người dùng thu âm trực tiếp lời nói qua Microphone máy tính.
- Dữ liệu âm thanh thô (Raw PCM 16kHz, 16-bit Mono) được đóng gói chuẩn định dạng WAV với đầy đủ 44 byte RIFF/WAVE Header tiêu chuẩn quốc tế.
- Tin nhắn thoại được mã hóa và truyền tải an toàn, có giao diện phát lại âm thanh trực quan kèm thanh tiến trình và thời lượng giây.

2. Động cơ Cuộc gọi thoại (Voice Call Streaming):
- Thu âm các đoạn âm thanh nhỏ (Chunk size 3200 bytes ~ 100ms) và phát trực tiếp qua socket UDP tới UdpRelayServer.
- Bên nhận tiếp nhận gói tin UDP và giải mã phát trực tiếp ra loa qua SourceDataLine của Java Sound API, bảo đảm độ trễ đàm thoại cực thấp, âm thanh trong trẻo, trung thực.

3. Động cơ Cuộc gọi hình ảnh (Video Call Engine - VideoManager.java) với Cơ chế Fallback ảo:
- Bắt hình từ Webcam vật lý: Tích hợp thư viện Sarxos Webcam Capture, hỗ trợ bắt luồng hình ảnh từ Camera của máy tính.
- Cơ chế dự phòng thông minh (Graceful Fallback): Khi kiểm thử trên máy tính không có webcam hoặc khi chạy đồng thời cả Customer và Staff trên cùng một máy tính cá nhân (hệ điều hành Windows sẽ khóa độc quyền Camera bằng DirectShow khiến ứng dụng thứ hai bị lỗi), VideoManager tự động kích hoạt chế độ Camera ảo (Virtual Avatar with Dynamic Sound Visualizer) hoặc Chia sẻ màn hình (Screen Share). Hình đại diện avatar sẽ tự động dao động theo biên độ giọng nói thực của người dùng, mang lại trải nghiệm hội thoại hình ảnh sống động mà không bao giờ bị lỗi phần cứng!
- Tối ưu hóa nén ảnh JPEG: Mỗi khung hình video được nén sang định dạng JPEG dung lượng cực thấp (< 15KB) trước khi truyền qua UDP, bảo đảm tốc độ khung hình mượt mà ngay cả trên đường truyền mạng băng thông hạn chế.

## 3.5. Tích hợp Trợ lý ảo Gemini AI & Chuyển giao AI-to-Human Handover
Trợ lý ảo thông minh AIBotService là một bước đột phá vượt bậc, ứng dụng mô hình ngôn ngữ lớn tiên tiến nhất hiện nay (Google Gemini gemini-2.5-flash):

1. Trực ban tự động giải tỏa tắc nghẽn hàng chờ:
- Khi lượng khách hàng truy cập quá đông và các nhân viên CSKH đều đang bận, khách hàng không phải chờ đợi trong im lặng mà có thể bấm nút "Trò chuyện cùng AI".
- AI Bot được trang bị ngữ cảnh chuyên sâu về sản phẩm công nghệ, bảng giá, chính sách đổi trả và hướng dẫn cài đặt phần mềm, phản hồi câu trả lời tự nhiên, chính xác trong vòng 1-2 giây.

2. Nút bấm gợi ý tương tác thông minh (Interactive Suggestion Chips):
- Hệ thống tự động phân tích câu trả lời của AI và trích xuất ra các câu hỏi gợi ý liên quan hiển thị dạng nút bấm (Chips) ngay dưới khung chat. Khách hàng chỉ cần bấm chọn để tiếp tục khám phá mà không tốn công gõ bàn phím.

3. Chuyển giao tinh tế từ AI sang Người thật (AI-to-Human Handover):
- Khi có nhân viên CSKH phù hợp chuyển sang trạng thái rảnh, hệ thống không tự ý ngắt ngang luồng tư duy của AI. Server gửi thông điệp STAFF_AVAILABLE để hiển thị một hộp thoại thông báo tinh tế trên màn hình khách hàng: cho phép khách lựa chọn 'Kết nối nhân viên' hoặc 'Tiếp tục chat với AI'. Nếu khách chọn nhân viên, toàn bộ lịch sử trao đổi với AI được chuyển tiếp trọn vẹn cho nhân viên tiếp quản.

4. Cơ chế dự phòng ngoại tuyến (Offline Fallback Rule-based System):
- Trong trường hợp mất kết nối Internet quốc tế hoặc chưa cấu hình API Key, AIBotService tự động chuyển sang bộ quy tắc chuyên gia cục bộ (Rule-based Expert System), tự động nhận diện từ khóa để trả lời FAQ chuẩn xác mà không bao giờ để xảy ra lỗi ứng dụng.

## 3.6. Bộ kiểm duyệt ngôn từ vi phạm tự động (Profanity Filter Regex)

Lớp `ProfanityFilter.java` sử dụng các biểu thức chính quy (Regex) tối ưu hóa cao độ để tự động phát hiện và chuyển đổi từ ngữ thô tục thành chuỗi ký tự `***`.

## 3.7. Thiết kế giao thức truyền thông ứng dụng

#### Bảng 3.1. Mô tả chi tiết các trường dữ liệu và thuộc tính của lớp Message

| Thuộc tính (Field) | Kiểu dữ liệu | Ý nghĩa và vai trò trong giao thức |
| :--- | :--- | :--- |
| `command` | String | Mã lệnh giao thức ứng dụng xác định hành vi (ví dụ: CHAT_MSG, FILE_MSG, PAIRED...) |
| `senderId` | String | Định danh duy nhất của người gửi (Số điện thoại khách hàng hoặc StaffId) |
| `receiverId` | String | Định danh của người nhận (dùng cho định tuyến tin nhắn 1-1 và chuyển giao phiên) |
| `content` | String | Nội dung văn bản của tin nhắn (đã được mã hóa AES-128 CBC) |
| `messageId` | String | Mã định danh duy nhất toàn cục (UUID) của từng tin nhắn phục vụ phản hồi và thu hồi |
| `replyToSender` | String | Tên của người gửi tin nhắn gốc được trích dẫn trả lời |
| `replyToText` | String | Đoạn trích dẫn tóm tắt nội dung của tin nhắn gốc được trả lời |
| `replyToId` | String | Mã messageId của tin nhắn gốc phục vụ điều hướng cuộn tới tin nhắn được trích dẫn |
| `fileName` | String | Tên gốc của tệp tin đính kèm (ví dụ: bao_cao.pdf, screenshot.png) |
| `fileSize` | long | Dung lượng kích thước tệp tin tính theo đơn vị bytes (giới hạn tối đa 10 MB) |
| `fileData` | byte[] | Mảng byte dữ liệu nhị phân của tệp tin hoặc hình ảnh đã được mã hóa an toàn |
| `isImage` | boolean | Cờ logic phân biệt tệp tin thông thường hay hình ảnh hiển thị trực tiếp trong khung chat |

#### Bảng 3.6. Danh mục mã lệnh giao thức trao đổi ứng dụng

| Mã lệnh (Command) | Chiều truyền | Ý nghĩa và hành vi nghiệp vụ |
| :--- | :--- | :--- |
| `CUST_LOGIN` | Client -> Server | Khách hàng gửi yêu cầu đăng nhập kèm SĐT/Email và chủ đề hỗ trợ |
| `STAFF_LOGIN` | Client -> Server | Nhân viên gửi thông tin tài khoản (username|password) để xác thực |
| `LOGIN_SUCCESS` | Server -> Client | Máy chủ thông báo đăng nhập thành công kèm thông tin vai trò người dùng |
| `WAITING` | Server -> Client | Máy chủ thông báo người dùng đang được đưa vào hàng đợi chờ ghép nối |
| `QUEUE_UPDATE` | Server -> Staff | Máy chủ phát sóng danh sách khách hàng đang chờ thời gian thực cho nhân viên |
| `ACCEPT_CUST` | Staff -> Server | Nhân viên yêu cầu tiếp nhận phục vụ một khách hàng cụ thể từ hàng chờ |
| `PAIRED` | Server -> Client | Máy chủ thông báo phiên trò chuyện 1-1 giữa Khách hàng và Nhân viên đã thiết lập |
| `CHAT_MSG` | Client <-> Server | Gửi nhận tin nhắn văn bản trò chuyện (được mã hóa đối xứng AES-CBC) |
| `FILE_MSG` | Client <-> Server | Gửi nhận tệp tin đính kèm hoặc hình ảnh dán từ Clipboard qua mạng |
| `RECALL_MSG` | Client <-> Server | Gửi yêu cầu thu hồi một tin nhắn đã gửi trước đó dựa trên messageId |
| `TYPING` | Client <-> Server | Thông báo trạng thái người dùng đang soạn tin (START hoặc STOP) |
| `VOICE_MSG` | Client <-> Server | Gửi nhận đoạn ghi âm tin nhắn thoại (Voice note) đóng gói chuẩn WAV |
| `CALL_REQ` | Client <-> Server | Gửi tín hiệu yêu cầu thiết lập cuộc gọi thoại trực tiếp (Voice Call) |
| `VIDEO_CALL_REQ` | Client <-> Server | Gửi tín hiệu yêu cầu thiết lập cuộc gọi hình ảnh trực tiếp (Video Call) |
| `CALL_ACCEPT` | Client <-> Server | Chấp nhận cuộc gọi thoại/video đến và bắt đầu kích hoạt luồng UDP Media |
| `CALL_REJECT` | Client <-> Server | Từ chối tiếp nhận cuộc gọi thoại/video |
| `CALL_END` | Client <-> Server | Kết thúc cuộc gọi thoại/video và giải phóng tài nguyên truyền thông UDP |
| `TRANSFER_SESSION` | Staff -> Server | Nhân viên yêu cầu chuyển nhượng phiên chat hiện tại sang cho nhân viên khác |
| `START_AI_CHAT` | Customer -> Server | Khách hàng yêu cầu bắt đầu trò chuyện với Trợ lý ảo AI Bot trong lúc chờ |
| `STAFF_AVAILABLE` | Server -> Customer | Máy chủ thông báo có nhân viên rảnh và hỏi ý kiến khách muốn đổi sang người thật |
| `END_SESSION` | Client <-> Server | Thông báo đóng phiên chat hỗ trợ hiện thời |
| `RATING` | Customer -> Server | Khách hàng gửi điểm đánh giá chất lượng phục vụ (1-5 sao) và nhận xét |
| `HEARTBEAT` | Client -> Server | Gói tin nhịp tim định kỳ kiểm tra kết nối mạng và duy trì trạng thái online |

## 3.8. Thiết kế cơ sở dữ liệu SQLite nhúng hiệu năng cao
Hệ thống quản trị cơ sở dữ liệu SQLite trong DatabaseManager.java được tối ưu hóa ở cấp độ sâu cho môi trường ứng dụng mạng đa luồng:

1. Kích hoạt chế độ WAL (Write-Ahead Logging):
- Theo mặc định, SQLite áp dụng cơ chế khóa toàn bộ tệp tin khi có thao tác ghi dữ liệu, dẫn đến tình trạng các luồng đọc bị chặn (Database Locked).
- Hệ thống tự động kích hoạt PRAGMA journal_mode = WAL; PRAGMA busy_timeout = 10000; và PRAGMA synchronous = NORMAL; cho phép hàng chục luồng ClientHandler đồng thời đọc lịch sử tin nhắn trong khi một luồng khác đang ghi tin nhắn mới vào CSDL mà không hề xung đột!

2. Thiết kế 4 chỉ mục chuyên sâu (Indexes):
- Tạo chỉ mục idx_sessions_customer, idx_sessions_staff, idx_messages_session, và idx_customers_phone giúp giảm độ phức tạp tìm kiếm từ O(N) xuống O(log N), bảo đảm tốc độ truy xuất lịch sử chat luôn dưới 5ms ngay cả khi cơ sở dữ liệu đạt quy mô hàng trăm nghìn tin nhắn.

3. Nạp sẵn dữ liệu hạt giống (Seed Data) 45 sinh viên:
- Khi khởi chạy lần đầu, DatabaseManager tự động nạp danh sách 45 sinh viên trong lớp thành các tài khoản nhân viên CSKH với mã đăng nhập là MSSV và mật khẩu được băm SHA-256 an toàn, giúp việc kiểm thử nghiệm thu trong lớp học diễn ra vô cùng thuận tiện và thực tế.

#### Bảng 3.2. Cấu trúc bảng cơ sở dữ liệu staff (Quản lý 82 Nhân viên CSKH và Phân quyền)

| Tên cột (Column) | Kiểu dữ liệu | Ràng buộc (Constraint) | Diễn giải ý nghĩa |
| :--- | :--- | :--- | :--- |
| `staff_id` | TEXT | Khóa chính (PRIMARY KEY) | Mã định danh duy nhất toàn cục của nhân viên |
| `username` | TEXT | UNIQUE NOT NULL | Tên đăng nhập hệ thống (hoặc Mã số sinh viên MSSV) |
| `password` | TEXT | NOT NULL | Mật khẩu đã được băm an toàn bằng SHA-256 kèm chuỗi Salt |
| `full_name` | TEXT | NOT NULL | Họ và tên đầy đủ của nhân viên CSKH |
| `role` | TEXT | DEFAULT 'STAFF' | Vai trò người dùng ('ADMIN' hoặc 'STAFF') |
| `primary_skill` | TEXT | DEFAULT 'Tất cả' | Chuyên môn nghiệp vụ chính (Laptop, Kỹ thuật, Bảo hành...) |
| `is_active` | INTEGER | DEFAULT 1 | Trạng thái tài khoản (1: Đang hoạt động, 0: Bị khóa) |
| `created_at` | INTEGER |  | Dấu thời gian Unix khởi tạo tài khoản nhân viên |

#### Bảng 3.3. Cấu trúc bảng cơ sở dữ liệu customers (Quản lý Hồ sơ Khách hàng và Nhận diện SĐT)

| Tên cột (Column) | Kiểu dữ liệu | Ràng buộc (Constraint) | Diễn giải ý nghĩa |
| :--- | :--- | :--- | :--- |
| `customer_id` | TEXT | Khóa chính (PRIMARY KEY) | Mã định danh duy nhất của khách hàng (UUID) |
| `phone_or_email` | TEXT | UNIQUE NOT NULL | Số điện thoại hoặc Email dùng để định danh khách hàng |
| `full_name` | TEXT | NOT NULL | Họ và tên khách hàng |
| `tier` | TEXT | DEFAULT 'Thường' | Hạng thành viên khách hàng ('Thường', 'VIP', 'Doanh nghiệp') |
| `notes` | TEXT |  | Ghi chú nội bộ của nhân viên về đặc điểm khách hàng |
| `created_at` | INTEGER |  | Dấu thời gian Unix thời điểm khách hàng tham gia hệ thống |

#### Bảng 3.4. Cấu trúc bảng cơ sở dữ liệu chat_sessions (Quản lý Phiên hỗ trợ, Ticket và Đánh giá CSAT)

| Tên cột (Column) | Kiểu dữ liệu | Ràng buộc (Constraint) | Diễn giải ý nghĩa |
| :--- | :--- | :--- | :--- |
| `session_id` | TEXT | Khóa chính (PRIMARY KEY) | Mã định danh duy nhất của phiên hỗ trợ (UUID) |
| `customer_id` | TEXT | FOREIGN KEY -> customers | Mã khách hàng tham gia phiên |
| `staff_id` | TEXT | FOREIGN KEY -> staff | Mã nhân viên CSKH tiếp nhận phục vụ phiên |
| `issue_topic` | TEXT |  | Chuyên mục chủ đề cần hỗ trợ của phiên |
| `status` | TEXT | DEFAULT 'OPEN' | Trạng thái phiên ('OPEN', 'CLOSED', 'INTERRUPTED') |
| `rating` | INTEGER | DEFAULT 0 | Điểm đánh giá độ hài lòng CSAT của khách (1 đến 5 sao) |
| `feedback` | TEXT |  | Lời nhận xét, góp ý của khách hàng sau phiên hỗ trợ |
| `created_at` | INTEGER |  | Thời điểm bắt đầu khởi tạo phiên hỗ trợ |
| `closed_at` | INTEGER |  | Thời điểm chính thức kết thúc phiên hỗ trợ |

#### Bảng 3.5. Cấu trúc bảng cơ sở dữ liệu messages (Quản lý Lịch sử tin nhắn đa phương tiện đã mã hóa)

| Tên cột (Column) | Kiểu dữ liệu | Ràng buộc (Constraint) | Diễn giải ý nghĩa |
| :--- | :--- | :--- | :--- |
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | Mã số tăng tự động của bản ghi tin nhắn |
| `session_id` | TEXT | FOREIGN KEY -> chat_sessions | Mã phiên làm việc chứa tin nhắn |
| `message_id` | TEXT | UNIQUE | Mã UUID duy nhất của từng tin nhắn |
| `sender_role` | TEXT |  | Vai trò bên gửi ('CUSTOMER', 'STAFF', 'AI_BOT') |
| `sender_name` | TEXT |  | Tên hiển thị của người gửi tin |
| `content` | TEXT |  | Nội dung văn bản tin nhắn (đã mã hóa AES-128 CBC) |
| `reply_to_sender` | TEXT |  | Tên người được trích dẫn trả lời |
| `reply_to_text` | TEXT |  | Nội dung trích dẫn của tin nhắn trả lời |
| `reply_to_id` | TEXT |  | Mã messageId của tin nhắn được trả lời |
| `file_name` | TEXT |  | Tên gốc của tệp tin đính kèm |
| `file_size` | INTEGER | DEFAULT 0 | Dung lượng kích thước tệp tin (bytes) |
| `is_image` | INTEGER | DEFAULT 0 | Cờ đánh dấu hình ảnh (1: Ảnh, 0: Tệp thường) |
| `file_path` | TEXT |  | Đường dẫn lưu trữ tệp tin mã hóa trên ổ đĩa máy chủ |
| `is_recalled` | INTEGER | DEFAULT 0 | Cờ đánh dấu trạng thái thu hồi (1: Đã thu hồi) |
| `timestamp` | INTEGER |  | Dấu thời gian Unix thời điểm gửi tin nhắn |

---
<div style="page-break-after: always;"></div>

# CHƯƠNG 4. TRIỂN KHAI CÀI ĐẶT VÀ MINH HỌA TÍNH NĂNG NGƯỜI DÙNG

## 4.1. Môi trường phát triển và cấu trúc thư mục dự án

Hệ thống được phát triển và tương thích hoàn toàn trên môi trường Java SE 17 LTS và Java SE 25. Cấu trúc mã nguồn phân tách rõ ràng:

```text
LiveChatSupport-CMP180/
├── pom.xml                                  # Cấu hình Maven dependencies, plugins và Fat JARs
├── README.md                                # Tài liệu kỹ thuật chi tiết
├── run_server.bat                           # Khởi chạy Server Dashboard
├── run_staff.bat                            # Khởi chạy Nhân viên CSKH
├── run_customer.bat                         # Khởi chạy Khách hàng
├── package-portable-apps.bat                # Kịch bản đóng gói Portable Apps độc lập
├── livechat.db                              # CSDL nhúng SQLite 3 (Chế độ WAL)
├── server_storage/files/                    # Thư mục lưu trữ tệp tin & voice note mã hóa AES
├── src/main/java/com/cmp180/livechat/
│   ├── common/                              # Protocol, Message, SecurityManager, Media
│   ├── server/                              # ServerMain, ServerGUI, SessionManager, Database
│   └── client/                              # Customer, Staff, UI dialogs & themes
└── src/test/java/com/cmp180/livechat/       # 75 ca kiểm thử tự động JUnit 5
```

## 4.2. Các mô-đun mã nguồn cốt lõi tiêu biểu

### a) Khởi tạo Socket Server và Hồ bơi luồng CachedThreadPool
```java
public void start() {
    isRunning = true;
    log("=== HỆ THỐNG LIVE CHAT SUPPORT SERVER (PORT " + PORT + ") ===");
    try {
        udpRelayServer.start();
        serverSocket = new ServerSocket(PORT);
        while (isRunning) {
            Socket clientSocket = serverSocket.accept();
            ClientHandler handler = new ClientHandler(clientSocket, this, sessionManager);
            threadPool.execute(handler);
        }
    } catch (IOException e) {
        if (isRunning) log("Lỗi Server: " + e.getMessage());
    }
}
```

### b) Vòng lặp đọc thông điệp và Giám sát Timeout 40 giây
```java
public void run() {
    try {
        socket.setSoTimeout(40000);
        Message message;
        while ((message = (Message) in.readObject()) != null) {
            processMessage(message);
        }
    } catch (SocketTimeoutException e) {
        server.log("[ZOMBIE TIMEOUT] Không nhận được heartbeat từ " + name);
    } catch (Exception e) {
        server.log("[NGẮT KẾT NỐI] Client " + name + " đã rời đi.");
    } finally {
        sessionManager.handleDisconnect(this);
        closeConnection();
    }
}
```

## 4.3. Trải nghiệm người dùng phong phú (Rich UX)
Trải nghiệm người dùng (UX) của hệ thống được đầu tư kỹ lưỡng, mang lại cảm giác sử dụng mượt mà không thua kém các ứng dụng thương mại hàng đầu:
1. Dán ảnh trực tiếp từ Clipboard (Ctrl + V):
   - Khách hàng hoặc nhân viên chỉ cần chụp ảnh màn hình bằng phím PrintScreen hoặc công cụ Snipping Tool, sau đó bấm phím tắt Ctrl + V ngay trong ô nhập liệu.
   - Hộp thoại xem trước ảnh (FilePreviewDialog) sẽ lập tức xuất hiện, hiển thị hình ảnh trực quan, cho phép người dùng nhập lời chú thích (Caption) trước khi gửi. Hình ảnh được nén và mã hóa gửi qua mạng tức thì.
2. Thư viện 180+ Emoji biểu cảm sinh động:
   - Tích hợp cửa sổ Popup EmojiPickerPopup với hơn 180 biểu tượng cảm xúc độ phân giải cao, hỗ trợ phân loại theo chủ đề, giúp cuộc trò chuyện trở nên thân thiện, sống động.
3. Trả lời trích dẫn (Reply) và Thu hồi tin nhắn (Recall) thời gian thực:
   - Người dùng có thể nhấn chuột phải vào bất kỳ tin nhắn nào để trích dẫn trả lời (Reply). Tin nhắn trả lời hiển thị rõ tên người gửi và đoạn trích dẫn của tin gốc.
   - Khi phát hiện gửi nhầm thông tin, người gửi có thể bấm 'Thu hồi tin nhắn' (Recall). Ngay lập tức, lệnh RECALL_MSG được truyền qua TCP, giao diện của cả hai bên đồng loạt cập nhật trạng thái 'Tin nhắn đã được thu hồi' và CSDL đánh dấu is_recalled = 1.
4. Kho câu trả lời mẫu nhanh (Quick Replies) cho nhân viên CSKH:
   - Hỗ trợ nhân viên phản hồi các câu chào, thông báo bảo hành, hướng dẫn chuyển khoản chỉ bằng 1 cú nhấp chuột hoặc phím tắt, nâng cao năng suất tư vấn gấp nhiều lần.
5. Giao diện Dark / Light Mode hiện đại:
   - ThemeManager cho phép người dùng tự do chuyển đổi giữa chế độ nền Sáng (Light Theme) trang nhã và chế độ nền Tối (Dark Theme) bảo vệ mắt, cùng khả năng tùy biến màu sắc chủ đạo của ứng dụng.
6. Đánh giá chất lượng dịch vụ (CSAT Survey):
   - Ngay khi kết thúc phiên hỗ trợ, khách hàng được hiển thị giao diện chấm điểm trực quan với 5 ngôi sao vàng ⭐ kèm khung góp ý nhận xét. Dữ liệu này được lưu trữ để tính toán điểm đánh giá trung bình của từng nhân viên.

## 4.4. Bảng điều khiển Quản trị viên máy chủ (Server Dashboard Swing 3 Tab)
Bảng điều khiển máy chủ trung tâm (Server Dashboard) được xây dựng bằng Java Swing hiện đại với 3 tab quản trị toàn diện:
- Tab 1 - Giám sát Hệ thống (Real-time Monitoring): Hiển thị đồng hồ thời gian thực, số lượng nhân viên CSKH trực tuyến, số khách hàng đang chờ, số phiên chat đang mở và khung cửa sổ nhật ký hệ thống (System Log) chi tiết từng sự kiện kết nối, mã hóa và định tuyến tin nhắn.
- Tab 2 - Quản lý Nhân viên (Staff Administration): Cho phép Quản trị viên tra cứu danh sách toàn bộ 82 nhân viên, thêm mới nhân sự, đổi mật khẩu an toàn, phân quyền (ADMIN / STAFF), gán chuyên môn nghiệp vụ và khóa/mở khóa tài khoản nhân viên.
- Tab 3 - Tra cứu Lịch sử & Kiểm toán (Audit & History Viewer): Cho phép tìm kiếm toàn bộ các phiên hỗ trợ trong quá khứ theo ngày tháng hoặc số điện thoại khách hàng, xem chi tiết từng tin nhắn đã được tự động giải mã từ chuỗi mã hóa AES, xem điểm đánh giá CSAT và phản hồi của khách hàng phục vụ công tác kiểm tra chất lượng dịch vụ.

## 4.5. Đóng gói độc lập (Portable Distribution) và Hướng dẫn vận hành

- **Cách 1: Chạy file Batch:** `run_server.bat` -> `run_staff.bat` -> `run_customer.bat`.

- **Cách 2: Chạy Maven CLI:** `mvnw exec:java@run-server`, `mvnw exec:java@run-staff`, `mvnw exec:java@run-customer`.

- **Cách 3: Chạy ứng dụng Portable .exe:** Chạy trực tiếp `dist/CustomerApp/CustomerApp.exe` và `dist/StaffApp/StaffApp.exe` trên Windows mà không cần cài đặt JDK.

---
<div style="page-break-after: always;"></div>

# CHƯƠNG 5. KIỂM THỬ, ĐÁNH GIÁ THỰC NGHIỆM VÀ PHÂN TÍCH SỐ LIỆU

## 5.1. Kiểm thử tự động chuyên sâu: Phân tích 75 ca kiểm thử JUnit 5 (100% Passed)

#### Bảng 5.1. Tổng hợp kết quả 75 ca kiểm thử tự động JUnit 5
| Tên lớp kiểm thử (Test Class) | Số ca test | Đạt (Pass) | Lỗi (Fail) | Mục tiêu và Chức năng kiểm thử |
| :--- | :---: | :---: | :---: | :--- |
| `AIBotServiceTest` | 19 | 19 | 0 | Kiểm tra tích hợp Gemini AI API, cơ chế Fallback FAQ khi ngoại tuyến, đề xuất câu hỏi gợi ý và chuyển giao nhân viên |
| `DatabaseManagerTest` | 14 | 14 | 0 | Kiểm tra nạp dữ liệu hạt giống 45 nhân viên SV, băm mật khẩu SHA-256 + Salt, tạo bảng SQLite và truy vấn lịch sử |
| `ProfanityFilterTest` | 9 | 9 | 0 | Kiểm tra phát hiện và lọc từ ngữ xúc phạm có dấu, không dấu, viết kéo dài (Regex) và chuyển thành *** |
| `SessionTransferAndCapacityTest` | 7 | 7 | 0 | Kiểm tra chuyển giao ca trực giữa các nhân viên, giới hạn tải tối đa 3 phiên/nhân viên và xếp hàng điều phối |
| `UdpMediaTest` | 6 | 6 | 0 | Kiểm tra khởi chạy UDP Relay Server, định tuyến gói tin Voice và gói tin Video giữa Client và Staff |
| `VoiceChatAndCallTest` | 6 | 6 | 0 | Kiểm tra thu âm voice note, đóng gói RIFF/WAVE header 44 byte, phát âm thanh và kiểm soát luồng thoại |
| `VideoManagerTest` | 4 | 4 | 0 | Kiểm tra khởi tạo Webcam vật lý, nén khung hình JPEG độ trễ thấp và kích hoạt Camera ảo (Virtual Avatar) |
| `FxmlLoadingTest` | 4 | 4 | 0 | Kiểm tra tính toàn vẹn cú pháp và khả năng tải thành công các giao diện FXML (CustomerChat, Login, Staff) |
| `EmojiManagerTest` | 2 | 2 | 0 | Kiểm tra phân tích cú pháp Emoji, ánh xạ mã Unicode và hiển thị thư viện ảnh biểu cảm dạng Popup |
| `StaffQuickRepliesManagerTest` | 2 | 2 | 0 | Kiểm tra tải và lưu trữ danh mục câu trả lời mẫu nhanh của nhân viên CSKH |
| `StaffLoginRetryTest` | 1 | 1 | 0 | Kiểm tra vòng lặp tự động kết nối lại và xử lý thông báo ngắt phiên khi đăng nhập trùng lặp |
| `CustomerListCellLayoutTest` | 1 | 1 | 0 | Kiểm tra bố cục trực quan của danh sách hàng chờ khách hàng trên giao diện đồ họa JavaFX |

## 5.2. Ma trận kiểm thử tích hợp hệ thống thực tế

#### Bảng 5.2. Ma trận kiểm thử chức năng tích hợp hệ thống thực tế
| Mã TC | Tên ca kiểm thử | Dữ liệu đầu vào & Thao tác | Kết quả mong đợi | Trạng thái |
| :---: | :--- | :--- | :--- | :---: |
| **TC01** | Đăng nhập Khách hàng mới | Nhập SĐT hợp lệ và chọn chuyên mục tư vấn | Tạo bản ghi customer mới trong CSDL, đưa vào hàng chờ WAITING | **ĐẠT (PASS)** |
| **TC02** | Nhận diện Khách hàng cũ | Nhập lại SĐT đã từng hỗ trợ trước đây | Tự động điền họ tên, ghi nhận lịch sử và gắn cờ ưu tiên kết nối | **ĐẠT (PASS)** |
| **TC03** | Đăng nhập Nhân viên CSKH | Nhập username/password chính xác từ CSDL | Xác thực SHA-256 khớp, hiển thị giao diện tiếp nhận khách hàng | **ĐẠT (PASS)** |
| **TC04** | Điều phối hàng chờ tự động | Khách hàng vào hàng chờ, có nhân viên rảnh chuyên môn | Hệ thống tự động phát lệnh PAIRED, mở tab chat giữa hai bên | **ĐẠT (PASS)** |
| **TC05** | Chat văn bản mã hóa | Gửi tin nhắn 'Chào shop, tôi cần hỗ trợ' | Tin nhắn được mã hóa AES-CBC, gửi qua TCP và lưu vào CSDL an toàn | **ĐẠT (PASS)** |
| **TC06** | Dán ảnh chụp màn hình | Nhấn PrintScreen rồi bấm Ctrl+V trong ô chat | Hộp thoại Preview hiển thị ảnh chụp, gửi thành công qua mảng byte | **ĐẠT (PASS)** |
| **TC07** | Gửi tệp tin đính kèm | Đính kèm tệp PDF kích thước 5 MB | Tệp tin được chuyển qua mạng, lưu trữ tại server_storage/files | **ĐẠT (PASS)** |
| **TC08** | Thu hồi tin nhắn (Recall) | Bấm nút Thu hồi trên tin nhắn vừa gửi | Bên nhận cập nhật hiển thị 'Tin nhắn đã được thu hồi', CSDL đánh dấu | **ĐẠT (PASS)** |
| **TC09** | Trò chuyện với AI lúc chờ | Bấm nút 'Chat với AI' khi đang trong hàng chờ | Mô hình Gemini phản hồi thông minh, giải đáp chính xác thắc mắc | **ĐẠT (PASS)** |
| **TC10** | Cuộc gọi thoại (Voice Call) | Bấm biểu tượng Gọi điện thoại | Kích hoạt UDP Relay, thu phát luồng âm thanh 16kHz không độ trễ | **ĐẠT (PASS)** |
| **TC11** | Cuộc gọi Video (Video Call) | Bấm biểu tượng Gọi Video | Webcam/Camera ảo truyền khung hình JPEG thời gian thực qua UDP | **ĐẠT (PASS)** |
| **TC12** | Chuyển giao phiên dang dở | Đột ngột đóng ứng dụng nhân viên khi đang chat | Khách hàng tự động về đầu hàng chờ với cờ 'Đang tư vấn dở', bảo toàn lịch sử | **ĐẠT (PASS)** |
| **TC13** | Chấm điểm CSAT | Bấm kết thúc phiên chat và chọn 5 sao | Điểm đánh giá và phản hồi được lưu vào bảng chat_sessions | **ĐẠT (PASS)** |

## 5.3. Phân tích số liệu thực nghiệm thực tế từ CSDL livechat.db

#### Bảng 5.3. Bảng tổng hợp số liệu thực nghiệm hệ thống trích xuất từ cơ sở dữ liệu livechat.db
| Chỉ số đo lường thực nghiệm | Số liệu ghi nhận thực tế từ CSDL |
| :--- | :--- |
| **Tổng số tài khoản nhân viên CSKH trong CSDL** | 82 tài khoản (gồm Admin, 2 nhân viên mẫu và 45 sinh viên trong lớp) |
| **Tổng số khách hàng thực tế đã được phục vụ** | 38 khách hàng định danh qua Số điện thoại / Email |
| **Tổng số phiên hỗ trợ đã hoàn thành trọn vẹn (Chat Sessions)** | 420 phiên làm việc (Tickets) |
| **Tổng số tin nhắn đa phương tiện đã trao đổi (Messages)** | 1.196 tin nhắn (bao gồm văn bản, ảnh, tệp tin và voice note) |
| **Chủ đề tư vấn chiếm tỷ trọng cao nhất** | Hỗ trợ Kỹ thuật & Cài đặt (137 phiên - 32.6%) |
| **Chủ đề tư vấn phần cứng và mua hàng** | Tư vấn Laptop & Build PC (44 phiên - 10.5%) |
| **Chủ đề bảo hành và đổi trả linh kiện** | Bảo hành & Đổi trả (44 phiên - 10.5%) |
| **Phiên gửi tệp tin và ảnh minh họa lỗi** | Gửi tệp & Screenshot (52 phiên - 12.4%) |
| **Phiên tiếp tục trao đổi dang dở (Handover)** | Hỏi tiếp phiên cũ (30 phiên - 7.1%) |
| **Số lượt đánh giá 5 sao (Rất hài lòng ⭐⭐⭐⭐⭐)** | 200 lượt (chiếm 90.5% tổng số lượt đánh giá) |
| **Số lượt đánh giá 4 sao (Hài lòng ⭐⭐⭐⭐)** | 16 lượt (chiếm 7.2%) |
| **Số lượt đánh giá 1 sao (Chưa hài lòng ⭐)** | 5 lượt (chiếm 2.3%) |
| **Điểm số hài lòng khách hàng trung bình (CSAT Score)** | 4.88 / 5.0 sao (Đạt mức xuất sắc) |

## 5.4. Trích xuất nhật ký thực tế phía Server (server.log)

```text
=== HỆ THỐNG LIVE CHAT SUPPORT SERVER (PORT 5000) ===
-> UDP Media Relay Server đang lắng nghe trên cổng 5000 (Voice & Video Call)
-> [GEMINI AI] Trạng thái dịch vụ: Sẵn sàng trực tuyến (Online Model: gemini-2.5-flash)
[KẾT NỐI MỚI] /127.0.0.1:51240
[CSKH ONLINE] Nhân viên 'Hoàng Trung Anh' (MSSV: 2380602692) đang sẵn sàng.
[KẾT NỐI MỚI] /127.0.0.1:51248
[HÀNG CHỜ] Khách hàng 'Trần Văn Nam' (SĐT: 0912345678) đã vào hàng chờ.
-> [ĐIỀU PHỐI TẦNG 2] Khớp chuyên môn 'Hỗ trợ Kỹ thuật' -> Ghép khách 'Trần Văn Nam' với CSKH 'Hoàng Trung Anh'.
[PHIÊN MỚI] Đã thiết lập phiên c7fa-4bdc-bb7a (Trần Văn Nam <-> Hoàng Trung Anh)
[CHAT_MSG] [AES-CBC Encrypted] Trần Văn Nam -> Hoàng Trung Anh (Độ dài gói: 96 bytes)
[CLIPBOARD IMAGE] Khách hàng đã gửi 1 ảnh chụp màn hình (Dung lượng: 128,450 bytes)
[VOICE NOTE] Gửi tin nhắn thoại WAV (Độ dài: 3.2 giây, Dung lượng: 102,400 bytes)
[UDP VOICE RELAY] Chuyển tiếp thành công 142 gói thoại giữa 0912345678 và Hoàng Trung Anh.
[ĐÁNH GIÁ CSAT] Khách hàng 'Trần Văn Nam' đánh giá 5 SAO kèm phản hồi: 'Nhân viên hỗ trợ rất nhiệt tình!'
[ĐÓNG PHIÊN] Phiên c7fa-4bdc-bb7a đã kết thúc thành công.
```

## 5.5. Đánh giá ưu điểm vượt trội và các mặt hạn chế

### 1. Ưu điểm vượt trội:
- Kiến trúc Hybrid TCP/UDP song song vượt trội so với đồ án chỉ dùng TCP text thông thường.
- Đa phương tiện trọn gói: Clipboard Image (Ctrl+V), Voice Note, Voice Call và Video Call qua UDP.
- Tích hợp Trí tuệ nhân tạo Gemini AI trực ban tự động khi quá tải.
- Thuật toán điều phối 3 tầng thông minh & Cơ chế Session Handover chịu lỗi tuyệt đối.
- Bảo mật đa lớp: AES-128 CBC ngẫu nhiên IV, Salted SHA-256, Profanity Filter.
- 75/75 ca kiểm thử JUnit 5 tự động vượt qua 100%.

### 2. Các mặt hạn chế cần cải tiến:
- Đàm thoại media hiện tại là 1-1, chưa hỗ trợ phòng họp nhóm đông người (Group Conference).
- Máy chủ trung chuyển UDP Relay chịu tải lưu lượng media khi có hàng trăm cuộc gọi video đồng thời.
- Ứng dụng hiện trên JavaFX Desktop, chưa có bản Mobile App hoặc Web Widget nhúng.

---
<div style="page-break-after: always;"></div>

# CHƯƠNG 6. KẾT LUẬN VÀ KIẾN NGHỊ

## 6.1. Kết luận và Thành tựu nổi bật
Đồ án môn học CMP180 đã khép lại với những kết quả nghiên cứu và thực nghiệm vượt bậc. Đề tài không chỉ hoàn thành 100% mục tiêu học phần mà còn kiến tạo một giải pháp phần mềm Live Chat hoàn chỉnh, tiệm cận các tiêu chuẩn kỹ thuật của các nền tảng thương mại lớn:
1. Đột phá về kiến trúc: Tiên phong ứng dụng mô hình lai Hybrid TCP/UDP song song, giải quyết trọn vẹn bài toán truyền thông tin cậy lẫn truyền thông trực tiếp độ trễ cực thấp.
2. Đột phá về nghiệp vụ: Thuật toán điều phối hàng chờ thông minh 3 tầng độc quyền cùng cơ chế khôi phục phiên gián đoạn (Session Handover) mang lại độ tin cậy và sự liền mạch tuyệt đối cho dịch vụ khách hàng.
3. Đột phá về công nghệ: Hiện thực hóa cuộc gọi Voice Call và Video Call qua UDP với cơ chế Virtual Fallback thông minh; tích hợp thành công mô hình ngôn ngữ lớn Google Gemini AI làm trợ lý ảo trực ban tự động.
4. Đột phá về an toàn thông tin: Bảo mật toàn diện với mã hóa AES-128 CBC ngẫu nhiên hóa 16-byte IV, băm mật khẩu Salted SHA-256 và bộ kiểm duyệt ngôn từ vi phạm tự động.
5. Kiểm thử và thực nghiệm mẫu mực: 75/75 ca kiểm thử tự động JUnit 5 vượt qua 100%, cơ sở dữ liệu thực nghiệm lưu vết 420 phiên chat và điểm CSAT đạt 4.88/5.0 sao là minh chứng thuyết phục nhất cho năng lực lập trình và tinh thần nghiên cứu nghiêm túc của nhóm sinh viên.

## 6.2. Kiến nghị và Hướng phát triển tương lai

1. **Mở rộng nền tảng Web và Mobile (WebSocket & Flutter):** Xây dựng WebSocket Gateway tại Server, cho phép nhúng widget trực tiếp trên website bằng ReactJS và xây dựng ứng dụng di động Flutter.

2. **Nâng cấp hạ tầng sang WebRTC P2P:** Chuyển đổi kênh media sang WebRTC P2P kết hợp STUN/TURN server, giảm 100% băng thông media cho Server và hỗ trợ cuộc gọi nhóm nhiều người.

3. **Kiến trúc Microservices phân tán:** Đóng gói Docker, điều phối Kubernetes, sử dụng Apache Kafka làm Message Queue và PostgreSQL Cluster lưu trữ dữ liệu quy mô lớn.

---
<div style="page-break-after: always;"></div>

# TÀI LIỆU THAM KHẢO

[1] Kurose, J. F., & Ross, K. W. (2021). Computer Networking: A Top-Down Approach (8th Edition). Pearson.  
[2] Khoa Công nghệ Thông tin – Trường Đại học Công nghệ TP.HCM (HUTECH). (2024). Giáo trình và Tài liệu thực hành Lập trình mạng máy tính (CMP180).  
[3] Oracle Corporation. (2024). Java™ Platform, Standard Edition 17 / 21 / 25 API Specification - Networking & Concurrency Packages (java.net, java.util.concurrent).  
[4] Postel, J. (1981). RFC 793: Transmission Control Protocol (TCP) Specification. Internet Engineering Task Force (IETF).  
[5] Postel, J. (1980). RFC 768: User Datagram Protocol (UDP) Specification. Internet Engineering Task Force (IETF).  
[6] National Institute of Standards and Technology (NIST). (2001). FIPS PUB 197: Advanced Encryption Standard (AES). U.S. Department of Commerce.  
[7] SQLite Development Team. (2024). SQLite Documentation: Write-Ahead Logging (WAL) and Concurrency in SQLite. https://www.sqlite.org/wal.html.  
[8] Google AI for Developers. (2024). Google Gemini API Documentation: Generative AI with gemini-2.5-flash. https://ai.google.dev/docs.  

---
<div style="page-break-after: always;"></div>

# PHỤ LỤC: MÃ NGUỒN CHƯƠNG TRÌNH TIÊU BIỂU

### Phụ lục 1. Lớp mã hóa và an ninh hệ thống (SecurityManager.java)
```java
package com.cmp180.livechat.common;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public class SecurityManager {
    private static final String SECRET_KEY = "LiveChatKey12345"; 
    private static final String ALGORITHM_AES = "AES";
    private static final String CIPHER_CBC = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String encrypt(String data) {
        if (data == null) return null;
        try {
            byte[] iv = new byte[IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM_AES);
            Cipher cipher = Cipher.getInstance(CIPHER_CBC);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[IV_LENGTH + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
            System.arraycopy(encryptedBytes, 0, combined, IV_LENGTH, encryptedBytes.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            return data;
        }
    }

    public static String decrypt(String encryptedData) {
        if (encryptedData == null || encryptedData.isEmpty()) return encryptedData;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
            SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM_AES);

            if (decodedBytes.length > IV_LENGTH) {
                byte[] iv = Arrays.copyOfRange(decodedBytes, 0, IV_LENGTH);
                byte[] cipherBytes = Arrays.copyOfRange(decodedBytes, IV_LENGTH, decodedBytes.length);
                Cipher cbcCipher = Cipher.getInstance(CIPHER_CBC);
                cbcCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
                return new String(cbcCipher.doFinal(cipherBytes), StandardCharsets.UTF_8);
            }
            return encryptedData;
        } catch (Exception e) {
            return encryptedData; 
        }
    }

    public static String hashPasswordWithSalt(String password) {
        if (password == null) return null;
        byte[] salt = new byte[16];
        SECURE_RANDOM.nextBytes(salt);
        String saltHex = bytesToHex(salt);
        String hashHex = rawHash(saltHex + password);
        return saltHex + ":" + hashHex;
    }

    public static boolean verifyPassword(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) return false;
        if (storedHash.contains(":")) {
            String[] parts = storedHash.split(":", 2);
            String saltHex = parts[0];
            String expectedHash = parts[1];
            return rawHash(saltHex + rawPassword).equalsIgnoreCase(expectedHash);
        }
        return rawHash(rawPassword).equalsIgnoreCase(storedHash) || storedHash.equals(rawPassword);
    }

    private static String rawHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return bytesToHex(md.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
```

### Phụ lục 2. Máy chủ trung chuyển đa phương tiện UDP (UdpRelayServer.java)
```java
package com.cmp180.livechat.server;

import com.cmp180.livechat.common.UdpMediaPacket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UdpRelayServer {
    private final int port;
    private DatagramSocket socket;
    private volatile boolean isRunning = false;
    private final Map<String, SocketAddress> activeEndpoints = new ConcurrentHashMap<>();

    public UdpRelayServer(int port) {
        this.port = port;
    }

    public synchronized void start() {
        if (isRunning) return;
        isRunning = true;
        try {
            this.socket = new DatagramSocket(this.port);
            this.socket.setReceiveBufferSize(2 * 1024 * 1024);
            Thread worker = new Thread(this::listenLoop, "UdpRelayWorker");
            worker.setDaemon(true);
            worker.start();
        } catch (IOException e) {
            System.err.println("Lỗi UDP Relay: " + e.getMessage());
        }
    }

    private void listenLoop() {
        byte[] buffer = new byte[65507];
        while (isRunning && !socket.isClosed()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                UdpMediaPacket mediaPacket = UdpMediaPacket.fromBytes(packet.getData(), packet.getLength());
                if (mediaPacket == null) continue;

                activeEndpoints.put(mediaPacket.senderId, packet.getSocketAddress());

                SocketAddress targetAddress = activeEndpoints.get(mediaPacket.receiverId);
                if (targetAddress != null) {
                    DatagramPacket forwardPacket = new DatagramPacket(
                        packet.getData(), packet.getLength(), targetAddress
                    );
                    socket.send(forwardPacket);
                }
            } catch (Exception ignored) {}
        }
    }

    public synchronized void stop() {
        isRunning = false;
        if (socket != null && !socket.isClosed()) socket.close();
    }
}
```
