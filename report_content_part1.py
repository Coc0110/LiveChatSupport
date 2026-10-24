# -*- coding: utf-8 -*-
"""
Noi dung chi tiet cho Chuong 1 va Chuong 2 (Ban nang cao, lam noi bat dot pha)
Bao cao Do an CMP180 - Live Chat Support System
"""

LOI_CAM_DOAN = """Nhóm chúng tôi xin cam đoan đây là công trình nghiên cứu và thực hiện của nhóm chúng tôi dưới sự hướng dẫn khoa học của ThS. Nguyễn Văn Cẩn thuộc Khoa Công Nghệ Thông Tin – Trường Đại học Công Nghệ TP.HCM (HUTECH).

Các ý tưởng kiến trúc, giải pháp công nghệ, thuật toán điều phối, mã nguồn và kết quả thực nghiệm trình bày trong báo cáo đồ án này là hoàn toàn trung thực, khách quan và được đúc kết trực tiếp từ quá trình dày công nghiên cứu, hiện thực hóa và kiểm thử hệ thống "Live Chat Support". Các đoạn trích dẫn, kiến thức lý thuyết kế thừa từ các tài liệu tham khảo, bài báo khoa học hay tài liệu kỹ thuật đều được ghi rõ nguồn gốc và trích dẫn theo đúng quy định học thuật của Nhà trường.

Mọi sự vi phạm về quy chế liêm chính học thuật hoặc sao chép trái phép, nhóm chúng tôi xin hoàn toàn chịu trách nhiệm trước Hội đồng chấm thi, Khoa Công Nghệ Thông Tin và Nhà trường."""

CH1_INTRO = """Trong hoạt động kinh doanh hiện đại và nền kinh tế số ngày nay, dịch vụ chăm sóc và hỗ trợ khách hàng (Customer Service) đóng vai trò sống còn đối với sự tồn tại và phát triển của mọi doanh nghiệp. Sự bùng nổ của thương mại điện tử, các nền tảng dịch vụ số và xu hướng mua sắm trực tuyến đòi hỏi doanh nghiệp phải thiết lập các kênh tương tác tức thời (Real-time Communication) nhằm giải đáp thắc mắc, tư vấn sản phẩm, xử lý sự cố kỹ thuật và tiếp nhận khiếu nại của khách hàng một cách nhanh chóng nhất.

Trước đây, doanh nghiệp chủ yếu dựa vào hai kênh truyền thống là điện thoại (Hotline tổng đài) và thư điện tử (Email). Tuy nhiên, các kênh này bộc lộ những nhược điểm cố hữu: chi phí cước viễn thông đắt đỏ, nguy cơ nghẽn mạng vào giờ cao điểm, thời gian phản hồi email kéo dài hàng giờ thậm chí hàng ngày, và đặc biệt là không thể truyền tải trực quan các dữ liệu minh họa như ảnh chụp màn hình lỗi hoặc tệp dữ liệu cấu hình.

Sự xuất hiện của các hệ thống Live Chat (Trò chuyện trực tuyến) đã thay đổi hoàn toàn cục diện tương tác khách hàng. Với tính năng trò chuyện tức thì 1-1, khả năng truyền nhận tệp tin, hình ảnh, tin nhắn thoại (Voice note) và cuộc gọi đa phương tiện trực tiếp (Voice/Video Call), Live Chat không chỉ rút ngắn tối đa thời gian chờ đợi mà còn nâng cao trải nghiệm người dùng vượt bậc, góp phần gia tăng tỷ lệ chuyển đổi đơn hàng và chỉ số hài lòng của khách hàng (CSAT).

Đồ án học phần môn Lập trình mạng máy tính (CMP180) với đề tài "Hệ thống Chat Hỗ trợ Khách hàng Trực tuyến (Live Chat Support)" được nhóm nghiên cứu và phát triển với định hướng không chỉ dừng lại ở các yêu cầu bài tập cơ bản, mà chủ động nâng cấp toàn diện thành một giải pháp truyền thông mạng cấp doanh nghiệp (Enterprise-grade System), ứng dụng các công nghệ tiên tiến nhất của Java Networking, Truyền thông đa phương tiện thời gian thực và Trí tuệ nhân tạo (Generative AI)."""

CH1_SO_SANH_DATA = [
    ("Kiến trúc mạng truyền thông", "Chỉ sử dụng duy nhất Socket TCP đơn luồng gửi nhận chuỗi ký tự đơn giản.", "Kiến trúc lai Hybrid TCP/UDP song song: TCP đảm nhận kênh điều khiển & dữ liệu an toàn, UDP Relay Server phục vụ streaming âm thanh và video trực tiếp thời gian thực."),
    ("Nội dung trao đổi", "Tin nhắn văn bản thuần túy (Plain text String).", "Đa phương tiện toàn diện: Văn bản, Tệp đính kèm (đến 10MB), Dán ảnh chụp màn hình Clipboard (Ctrl+V) có xem trước, 180+ Emoji, Tin nhắn thoại (WAV 44-byte RIFF), Cuộc gọi thoại và Cuộc gọi hình ảnh."),
    ("Kênh đàm thoại trực tiếp", "Không hỗ trợ cuộc gọi.", "Hiện thực hóa Voice Call và Video Call qua giao thức UDP không độ trễ, tích hợp thư viện Webcam vật lý kèm cơ chế chuyển đổi thông minh sang Camera ảo (Virtual Avatar) và Chia sẻ màn hình."),
    ("Điều phối hàng chờ", "Hàng đợi vào trước - ra trước (FIFO) đơn giản hoặc nhân viên tự bấm chọn thủ công.", "Thuật toán điều phối thông minh 3 tầng độc quyền: (1) Khách quen ưu tiên gặp nhân viên cũ (Preferred Staff); (2) Phân luồng theo chuyên môn (Skill-based); (3) Cân bằng tải ít phiên nhất (Least Connections)."),
    ("Trí tuệ nhân tạo (AI)", "Không có hỗ trợ tự động.", "Tích hợp trực tiếp Google Gemini API (gemini-2.5-flash) làm Trợ lý ảo trực ban tự động khi khách ở hàng đợi, sinh Interactive Suggestion Chips và tự động chuyển giao mượt mà sang nhân viên (AI-to-Human Handover)."),
    ("Độ chịu lỗi (Fault Tolerance)", "Mất mạng là mất kết nối, người dùng bị văng, phiên chat bị hủy hoàn toàn.", "Cơ chế khôi phục phiên gián đoạn (Session Handover): nhân viên rớt mạng thì khách hàng được đưa về đầu hàng chờ kèm cờ 'Đang tư vấn dở', bảo toàn 100% lịch sử chat cũ cho nhân viên tiếp theo."),
    ("An toàn thông tin & Bảo mật", "Dữ liệu truyền trần trên mạng, mật khẩu lưu thô không mã hóa.", "Bảo mật đa lớp: Mã hóa đối xứng AES-128 CBC với Vector khởi tạo (IV) ngẫu nhiên 16-byte cho từng tin nhắn và tệp tin lưu đĩa; Băm mật khẩu SHA-256 kèm Salt ngẫu nhiên 16-byte; Bộ lọc từ khóa thô tục (Profanity Filter) Regex."),
    ("Cơ sở dữ liệu & Tối ưu", "Lưu file text thô hoặc SQLite cơ bản với thao tác khóa đồng bộ chậm.", "Hệ quản trị SQLite nhúng cấu hình WAL Mode (Write-Ahead Logging), 4 chỉ mục Indexes tăng tốc truy vấn, cơ chế Write-Lock đa luồng an toàn, nạp sẵn hạt giống 45 tài khoản sinh viên."),
    ("Trải nghiệm người dùng (UX)", "Giao diện Console dòng lệnh hoặc Swing sơ sài.", "Giao diện đồ họa JavaFX 21 FXML hiện đại, hỗ trợ chuyển đổi giao diện Sáng/Tối (Dark/Light Theme), tùy biến Accent Color, thông báo âm thanh sinh động và Dashboard quản trị Swing 3 Tab chuyên sâu."),
    ("Kiểm thử & Đóng gói", "Chạy thử nghiệm thủ công vài trường hợp cơ bản.", "Bộ kiểm thử tự động toàn diện 75/75 ca Unit Test Cases với JUnit 5 đạt tỷ lệ vượt qua 100%; Cơ sở dữ liệu thực nghiệm lưu vết 420 phiên chat và 1.196 tin nhắn; Đóng gói Portable Apps (.exe) độc lập.")
]

CH1_NHIEM_VU = {
    "cap_thiet": """Phần lớn doanh nghiệp vừa và nhỏ (SMEs) hiện vẫn xử lý yêu cầu hỗ trợ khách hàng qua điện thoại hoặc email thủ công, gây quá tải cho bộ phận trực ban, chi phí cước viễn thông đắt đỏ, và thường xuyên để mất khách hàng tiềm năng do tốc độ phản hồi chậm trễ. Thêm vào đó, việc sử dụng các nền tảng chat thương mại bên thứ ba (như Zalo OA, Facebook Messenger, Intercom, Tawk.to) tiềm ẩn rủi ro lộ lọt bí mật kinh doanh, dữ liệu khách hàng bị phụ thuộc vào máy chủ nước ngoài và chi phí duy trì thuê bao định kỳ rất cao.

Do đó, nhu cầu cấp thiết là phải tự làm chủ và xây dựng một hệ thống Live Chat nội bộ độc lập, an toàn và chuyên nghiệp:
1. Tiếp nhận tức thì kết nối từ khách hàng ngay khi họ truy cập ứng dụng.
2. Tự động hóa điều phối hàng chờ (Queue Dispatching) theo chuyên môn nhân viên và phân tải hợp lý.
3. Chịu lỗi cao (Fault Tolerance): không làm gián đoạn cuộc trò chuyện khi xảy ra sự cố mạng, hỗ trợ chuyển giao phiên (Session Handover) liền mạch giữa các nhân viên trực.
4. Bảo vệ an toàn dữ liệu khách hàng bằng các thuật toán mã hóa hiện đại trước khi truyền trên đường truyền mạng công cộng.""",

    "y_nghia": """Về mặt khoa học: Đề tài giúp củng cố và vận dụng tổng hợp toàn bộ khối kiến thức nền tảng của môn Lập trình mạng máy tính:
- Làm chủ mô hình kiến trúc Client - Server tập trung và cơ chế truyền tin hướng đối tượng.
- Vận dụng sâu sắc lập trình Socket TCP hướng nối (Connection-oriented) tin cậy và Socket UDP phi kết nối (Connectionless) cho truyền thông đa phương tiện thời gian thực.
- Kỹ thuật lập trình đa luồng (Multithreading), quản lý hồ bơi luồng (Cached Thread Pool), và giải quyết triệt để các bài toán tranh chấp tài nguyên (Race Condition), khóa chết (Deadlock) bằng các kỹ thuật đồng bộ hóa hiện đại trong Java.
- Cơ chế tuần tự hóa đối tượng (Java Object Serialization) và truyền gói tin cấu trúc qua mạng.
- Áp dụng các nguyên lý bảo mật dữ liệu mạng: mã hóa đối xứng AES-128 chuẩn CBC ngẫu nhiên hóa Vector khởi tạo (IV) và băm mật khẩu an toàn SHA-256 kèm Salt.

Về mặt thực tiễn: Đề tài cung cấp một sản phẩm phần mềm hoàn chỉnh, có khả năng ứng dụng thực tế cao:
- Giải quyết bài toán phân phối ca trực CSKH thực tế của doanh nghiệp với thuật toán điều phối 3 tầng thông minh (Ưu tiên nhân viên quen -> Khớp chuyên môn -> Phân phối ít tải nhất).
- Cung cấp kênh truyền đa phương tiện phong phú: gửi file đính kèm an toàn, dán ảnh chụp màn hình từ Clipboard (Ctrl+V), gửi Emoji trực quan, thu âm tin nhắn thoại (Voice note) và đàm thoại âm thanh / hình ảnh trực tiếp (Voice & Video Call) qua UDP.
- Tích hợp trí tuệ nhân tạo Gemini AI tự động giải đáp FAQ và tương tác thông minh cùng khách hàng trong lúc chờ nhân viên tiếp nhận, giải phóng tối đa sức lao động con người.
- Lưu vết toàn bộ lịch sử tư vấn, khảo sát đánh giá độ hài lòng CSAT và cung cấp giao diện quản trị viên (Admin Dashboard) trực quan để thống kê, giám sát ca trực thời gian thực.""",

    "muc_tieu": """Đề tài hướng đến các mục tiêu nghiên cứu và phát triển cụ thể sau:
1. Xây dựng Server trung tâm (LiveChatServer) vững chắc, có khả năng tiếp nhận đồng thời hàng chục kết nối TCP và UDP song song, điều phối phiên chat ổn định và tự động ghi nhật ký hệ thống.
2. Tự động ghép nối một khách hàng đang chờ với một nhân viên đang rảnh thành một phiên hội thoại 1-1 theo thuật toán 3 tầng tối ưu (Preferred Staff, Skill-based Routing, Least Connections).
3. Xây dựng ứng dụng Khách hàng (CustomerClient) trên nền tảng đồ họa JavaFX trực quan, cho phép định danh linh hoạt bằng Số điện thoại/Email, lưu trữ hồ sơ cục bộ, chat mượt mà và thực hiện gọi điện hỗ trợ.
4. Xây dựng ứng dụng Nhân viên CSKH (StaffClient) chuyên nghiệp, hỗ trợ đa phiên (xử lý đồng thời tới 3 khách hàng), tra cứu lịch sử hội thoại cũ, quản lý câu trả lời nhanh (Quick Replies) và chuyển giao ca trực (Transfer Session).
5. Hiện thực hóa kênh truyền thông đa phương tiện thời gian thực qua UDP Relay Server, hỗ trợ Voice Message (đóng gói chuẩn WAV), Voice Call và Video Call (Webcam capture / Virtual Camera) độ trễ cực thấp.
6. Bảo mật toàn diện dữ liệu: mã hóa tin nhắn và tệp tin bằng thuật toán AES-128 CBC trước khi gửi qua mạng và lưu vào CSDL, băm mật khẩu nhân viên bằng SHA-256 kèm Salt 16-byte, kiểm duyệt từ khóa thô tục (Profanity Filter) tự động.
7. Tích hợp mô hình AI Gemini (gemini-2.5-flash) làm trợ lý ảo trực ban tự động khi hàng chờ quá tải.
8. Tổ chức cơ sở dữ liệu SQLite nhúng gọn nhẹ, độc lập, không phụ thuộc máy chủ CSDL bên ngoài.""",

    "pham_vi": """Đối tượng sử dụng hệ thống gồm ba nhóm người dùng chính:
- Khách hàng (Customer): Người cần được tư vấn, giải đáp thắc mắc, gửi tệp hình ảnh lỗi, gọi điện hỗ trợ và đánh giá chất lượng dịch vụ sau phiên chat.
- Nhân viên chăm sóc khách hàng (Staff): Nhân sự trực ban được phân quyền, tiếp nhận các phiên chat từ hàng đợi, tra cứu hồ sơ khách hàng, trả lời nhanh, gọi điện thoại/video và bàn giao ca trực khi cần thiết.
- Quản trị viên hệ thống (Administrator): Người vận hành Server, giám sát số lượng trực tuyến thời gian thực, quản lý danh sách tài khoản nhân viên (thêm, sửa, khóa, đổi mật khẩu) và tra cứu toàn bộ biên bản lịch sử trò chuyện trong quá khứ.

Phạm vi đồ án giới hạn ở:
- Môi trường mạng: Hoạt động hoàn hảo trong mạng cục bộ (LAN), mạng nội bộ doanh nghiệp hoặc môi trường Internet công cộng thông qua cấu hình định địa chỉ IP hoặc Port Forwarding.
- Nền tảng thực thi: Các hệ điều hành máy tính để bàn (Windows 10/11, macOS, Linux) hỗ trợ môi trường thực thi Java Runtime Environment (JDK 17 LTS hoặc JDK 25).
- Lưu trữ dữ liệu: Sử dụng cơ sở dữ liệu nhúng SQLite 3 (livechat.db) gọn nhẹ, không yêu cầu thiết lập máy chủ dịch vụ MySQL hay PostgreSQL cồng kềnh."""
}

CH1_CAU_TRUC = """Ngoài phần mở đầu, lời cam đoan, mục lục, danh mục từ viết tắt, danh mục bảng, danh mục hình vẽ, tài liệu tham khảo và phụ lục, nội dung chính của báo cáo đồ án được kết cấu thành 6 chương chuyên sâu:

- Chương 1: Tổng quan – Giới thiệu bối cảnh, tính cấp thiết, bảng so sánh đối chiếu những đột phá của hệ thống so với yêu cầu đồ án thông thường, ý nghĩa khoa học và thực tiễn, mục tiêu và phạm vi giới hạn của đề tài.
- Chương 2: Cơ sở lý thuyết và Công nghệ nền tảng – Trình bày toàn diện các nguyên lý mạng máy tính và kỹ thuật lập trình Java nâng cao: mô hình Client-Server, Socket TCP Blocking I/O, Socket UDP thời gian thực, cơ chế quản lý đa luồng với CachedThreadPool, kỹ thuật đồng bộ phi khóa, tuần tự hóa đối tượng (Java Serialization), mật mã học ứng dụng (AES-128 CBC ngẫu nhiên IV, Salted SHA-256) và ứng dụng Trí tuệ nhân tạo (Generative AI).
- Chương 3: Thiết kế kiến trúc và Các đột phá kỹ thuật – Phân tích chi tiết kiến trúc lai Hybrid TCP/UDP, thuật toán điều phối hàng chờ 3 tầng, cơ chế chịu lỗi và khôi phục sự cố Session Handover, kênh truyền thông âm thanh/hình ảnh UDP, giải pháp tích hợp Trợ lý ảo Gemini AI, bộ lọc từ khóa thô tục và thiết kế cơ sở dữ liệu SQLite nhúng tối ưu chế độ WAL.
- Chương 4: Triển khai cài đặt và Minh họa tính năng người dùng – Trình bày cấu trúc thư mục, môi trường phát triển, các đoạn mã nguồn then chốt, trải nghiệm người dùng phong phú (dán ảnh Clipboard Ctrl+V, Voice Note, Video Call, Quick Replies), giao diện Bảng điều khiển Quản trị viên Swing và hướng dẫn vận hành hệ thống.
- Chương 5: Kiểm thử, Đánh giá thực nghiệm và Phân tích số liệu – Tổng hợp chi tiết 75 ca kiểm thử tự động JUnit 5 vượt qua 100%, ma trận kiểm thử chức năng hệ thống, phân tích toàn bộ số liệu thực nghiệm đồ sộ từ cơ sở dữ liệu livechat.db (82 nhân viên, 38 khách hàng, 420 phiên chat, 1.196 tin nhắn, điểm CSAT đạt 4.88/5.0 sao) và đánh giá ưu nhược điểm.
- Chương 6: Kết luận và Hướng phát triển – Đúc kết các thành tựu nổi bật của đồ án, rút ra bài học kinh nghiệm và vạch rõ lộ trình nâng cấp hệ thống sang kiến trúc WebRTC P2P và Microservices."""

CH2_CONTENT = {
    "client_server": """Mô hình Client - Server (Máy khách - Máy chủ) là một kiến trúc mạng phân tán phân chia khối lượng công việc giữa các nhà cung cấp tài nguyên hoặc dịch vụ (gọi là Server) và những người yêu cầu dịch vụ (gọi là Client).

Trong hệ thống Live Chat Support:
- Máy chủ (Server): Đóng vai trò là trung tâm điều phối và lưu trữ tập trung. Server luôn ở trạng thái lắng nghe (Listen) trên cổng TCP Port 5000 và UDP Port 5000. Server không chỉ chuyển tiếp thông điệp giữa các bên mà còn quản trị hàng đợi khách hàng, thực hiện thuật toán ghép cặp tự động, lưu trữ lịch sử tin nhắn vào CSDL, và thực thi các biện pháp bảo mật, kiểm duyệt nội dung.
- Máy khách (Client): Là các ứng dụng chạy trên máy trạm của Khách hàng hoặc Nhân viên CSKH. Client chủ động khởi tạo kết nối (Active Open) đến địa chỉ IP và Port của Server. Sau khi thiết lập phiên làm việc thành công, Client gửi các thông điệp dữ liệu, hiển thị giao diện đồ họa, thu nhận thao tác người dùng và nhận dữ liệu phản hồi từ máy chủ.

Ưu điểm của mô hình Client - Server trong ứng dụng CSKH:
- Quản lý tập trung: Toàn bộ dữ liệu phiên trò chuyện, tài khoản nhân viên và thông tin khách hàng được quản lý an toàn tại Server, dễ dàng sao lưu, kiểm toán và giám sát.
- Khả năng mở rộng (Scalability): Cho phép hàng chục, hàng trăm Client đồng thời kết nối vào hệ thống mà không làm xáo trộn kiến trúc mạng.
- Vượt tường lửa dễ dàng: Các Client chỉ cần tạo kết nối Outbound tới máy chủ trung tâm mà không cần mở cổng hay cấu hình Port Forwarding phức tạp trên thiết bị người dùng cuối.""",

    "tcp_socket": """TCP (Transmission Control Protocol) là giao thức truyền dữ liệu cốt lõi hoạt động tại tầng Giao vận (Transport Layer) trong mô hình OSI và bộ giao thức TCP/IP. TCP là giao thức hướng kết nối (Connection-oriented), đảm bảo dữ liệu được truyền tải đầy đủ, đúng thứ tự và hoàn toàn không bị lỗi hoặc trùng lặp.

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
  + socket.close(): Đóng kết nối và giải phóng tài nguyên socket mạng.""",

    "udp_socket": """Bên cạnh TCP, giao thức UDP (User Datagram Protocol) là giao thức truyền dữ liệu phi kết nối (Connectionless) thuộc tầng Giao vận. UDP gửi các khối dữ liệu độc lập (gọi là Datagram) mà không cần thiết lập phiên bắt tay trước và không có cơ chế truyền lại khi gói tin thất lạc.

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

Trong hệ thống Live Chat Support, UdpRelayServer hoạt động như một máy chủ trung chuyển (Relay Hub) thông minh, chuyển tiếp hàng nghìn gói tin âm thanh thoại (PCM 16kHz) và khung hình video (JPEG nén) mỗi phút giữa Customer và Staff.""",

    "inet_address": """Lớp java.net.InetAddress trong ngôn ngữ lập trình Java là lớp trừu tượng biểu diễn một địa chỉ Giao thức Internet (IP Address), bao gồm cả chuẩn IPv4 (32-bit, ví dụ: 192.168.1.100) và IPv6 (128-bit). Lớp này cung cấp các dịch vụ phân giải tên miền (DNS Resolution) và xác định vị trí máy chủ trên mạng.

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
- UdpRelayServer.java và UdpMediaClient.java: Sử dụng InetAddress để đóng gói các gói tin UDP DatagramPacket định tuyến âm thanh và hình ảnh đến đúng IP máy chủ trung chuyển.""",

    "multithreading": """Lập trình đa luồng (Multithreading) là khả năng của một hệ điều hành hoặc máy ảo cho phép một tiến trình thực thi nhiều nhánh công việc (Thread) đồng thời. Trong lập trình máy chủ mạng, đa luồng là kỹ thuật bắt buộc để phục vụ đồng thời nhiều kết nối Client mà không làm đóng băng (Freeze) hệ thống.

1. Hồ bơi luồng tối ưu (Cached Thread Pool):
Thay vì tự tạo mới một đối tượng Thread cho mỗi kết nối (dẫn đến nguy cơ cạn kiệt bộ nhớ OutOfMemoryError khi lượng truy cập tăng vọt), hệ thống áp dụng bộ quản lý luồng hiện đại từ gói java.util.concurrent:
  ExecutorService threadPool = Executors.newCachedThreadPool();
- CachedThreadPool tự động tái sử dụng các luồng nhàn rỗi (idle threads) trong 60 giây và tự động mở rộng số lượng luồng linh hoạt khi có nhiều yêu cầu kết nối đồng thời, giúp máy chủ phản hồi tức thì với chi phí tài nguyên thấp nhất.

2. Kỹ thuật đồng bộ hóa phi khóa (Lock-free Concurrency):
Để xử lý hàng nghìn thông điệp đồng thời giữa các luồng ClientHandler mà không gây khóa chết (Deadlock) hay nghẽn cổ chai hiệu năng, hệ thống áp dụng các cấu trúc dữ liệu tiên tiến:
- ConcurrentLinkedQueue: Dùng cho hàng đợi khách hàng chờ waitingCustomers và nhân viên allStaffs. Sử dụng thuật toán CAS (Compare-And-Swap) không khóa (Lock-free), giúp các thao tác enqueue/dequeue đạt hiệu năng cực đại.
- CopyOnWriteArrayList: Dùng cho danh sách các phiên chat hoạt động activeSessionsList, bảo đảm thao tác lặp (iteration) qua danh sách phiên luôn an toàn tuyệt đối ngay cả khi có luồng khác đang thêm hoặc xóa phiên.
- ConcurrentHashMap: Dùng trong UdpRelayServer để định tuyến địa chỉ IP:Port của Client trong bộ nhớ nano-giây.
- Từ khóa volatile: Gắn vào các cờ trạng thái đa luồng (isRunning, interrupted, chattingWithAI) bảo đảm tính cập nhật tức thời trên bộ nhớ đệm CPU của mọi lõi vi xử lý.""",

    "serialization": """Truyền đối tượng qua mạng (Object Transmission) là kỹ thuật cho phép đóng gói toàn bộ trạng thái của một đối tượng Java thành một chuỗi byte tuần tự (Byte Stream) để truyền qua Socket mạng hoặc lưu trữ xuống tệp tin.

1. Giao diện java.io.Serializable và thuộc tính serialVersionUID:
- Lớp Message cài đặt giao diện Serializable, cho phép đối tượng được chuyển đổi hai chiều một cách tự nhiên mà không cần viết bộ phân tích chuỗi thủ công.
- Khai báo tường minh serialVersionUID = 1L giúp bảo đảm tính tương thích tuyệt đối giữa các phiên bản phần mềm máy khách và máy chủ.

2. Đóng gói gói tin đa hình (Polymorphic Message Object):
Lớp Message đóng vai trò là một container dữ liệu đa năng:
- Mang thông điệp điều khiển (command, senderId, receiverId).
- Mang nội dung văn bản đã mã hóa (content, messageId, replyToSender, replyToText, replyToId).
- Mang dữ liệu nhị phân đính kèm (fileName, fileSize, byte[] fileData, isImage) cho phép truyền tệp tin tài liệu và ảnh chụp màn hình nguyên bản qua luồng mạng mà không bị lỗi biến dạng ký tự.""",

    "security": """Bảo mật là một trong những điểm đột phá quan trọng nhất của đồ án, giúp hệ thống đạt chuẩn an toàn thông tin cấp doanh nghiệp:

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
- Bộ lọc nhận diện biểu thức chính quy phức tạp: xử lý tiếng Việt có dấu, không dấu, và các biến thể cố tình kéo dài nguyên âm nhằm vượt mặt hệ thống kiểm duyệt.""",

    "generative_ai": """Ứng dụng Trí tuệ nhân tạo (Generative AI) là điểm sáng công nghệ đột phá của đề tài, đưa hệ thống vượt xa các đồ án môn học thông thường:

1. Tích hợp trực tiếp Google Gemini API (gemini-2.5-flash):
- Hệ thống sử dụng thư viện HttpClient của Java 11+ kết nối bất đồng bộ (Asynchronous HTTP/2) trực tiếp tới điểm cuối Gemini Generative Language API.
- Prompt hệ thống (System Prompt) được huấn luyện chuyên sâu với vai trò là Trợ lý CSKH ảo của chuỗi công nghệ, có khả năng tư vấn cấu hình máy tính, kiểm tra chính sách bảo hành, hướng dẫn xử lý lỗi phần mềm.

2. Cơ chế đề xuất câu hỏi gợi ý tương tác (Interactive Suggestion Chips):
- Thuật toán phân tích phản hồi của AI để tự động trích xuất các câu hỏi gợi ý thông minh (ví dụ: 'Cách kiểm tra thời hạn bảo hành?', 'Gợi ý Laptop lập trình tầm giá 20 triệu?').
- Giao diện khách hàng tự động hiển thị các nút bấm gợi ý (Chips Button), cho phép khách hàng nhấn chọn câu hỏi tiếp theo một cách trực quan mà không cần gõ bàn phím.

3. Cơ chế phát hiện nhân viên và Chuyển giao thông minh (AI-to-Human Handover):
- Khi khách hàng đang trao đổi với AI trong hàng chờ, nếu có nhân viên CSKH phù hợp chuyển sang trạng thái sẵn sàng, Server sẽ không thô bạo ngắt ngang cuộc trò chuyện với AI.
- Hệ thống phát tín hiệu STAFF_AVAILABLE và hiển thị thông báo lịch sự trên màn hình khách hàng: "Nhân viên CSKH [Tên nhân viên] đã sẵn sàng hỗ trợ. Bạn có muốn kết nối với nhân viên hay tiếp tục trò chuyện cùng AI?". Quyền quyết định thuộc về khách hàng, mang lại trải nghiệm hỗ trợ tinh tế và chuyên nghiệp bậc nhất."""
}

print("report_content_part1.py updated with advanced content.")
