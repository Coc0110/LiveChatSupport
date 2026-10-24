# -*- coding: utf-8 -*-
import os
import docx
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml import parse_xml
from docx.oxml.ns import nsdecls

def create_appendix_doc(output_path="PHU_LUC_MA_NGUON_LIVECHAT.docx"):
    doc = Document()

    for s in doc.sections:
        s.top_margin = Inches(0.79)
        s.bottom_margin = Inches(0.79)
        s.left_margin = Inches(1.18)
        s.right_margin = Inches(0.79)
        s.page_width = Inches(8.27)
        s.page_height = Inches(11.69)

    def set_cell_background(cell, hex_color):
        tcPr = cell._tc.get_or_add_tcPr()
        shd = parse_xml(f'<w:shd {nsdecls("w")} w:val="clear" w:color="auto" w:fill="{hex_color}"/>')
        tcPr.append(shd)

    def set_cell_margins(cell, top=80, bottom=80, left=140, right=140):
        tcPr = cell._tc.get_or_add_tcPr()
        tcMar = parse_xml(f'<w:tcMar {nsdecls("w")}>'
                          f'<w:top w:w="{top}" w:type="dxa"/>'
                          f'<w:bottom w:w="{bottom}" w:type="dxa"/>'
                          f'<w:left w:w="{left}" w:type="dxa"/>'
                          f'<w:right w:w="{right}" w:type="dxa"/>'
                          f'</w:tcMar>')
        tcPr.append(tcMar)

    def add_title(text):
        p = doc.add_paragraph()
        p.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p.paragraph_format.space_before = Pt(14)
        p.paragraph_format.space_after = Pt(8)
        r = p.add_run(text)
        r.bold = True
        r.font.name = "Times New Roman"
        r.font.size = Pt(16)
        r.font.color.rgb = RGBColor(0x1F, 0x4E, 0x78)

    def add_desc(text):
        p = doc.add_paragraph()
        p.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
        p.paragraph_format.space_after = Pt(8)
        r = p.add_run(text)
        r.italic = True
        r.font.name = "Times New Roman"
        r.font.size = Pt(12)

    def add_appendix_section(title, file_path, desc):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(12)
        p.paragraph_format.space_after = Pt(4)
        r = p.add_run(title)
        r.bold = True
        r.font.name = "Times New Roman"
        r.font.size = Pt(13.5)
        r.font.color.rgb = RGBColor(0x1F, 0x4E, 0x78)

        if desc:
            p_d = doc.add_paragraph()
            p_d.paragraph_format.space_after = Pt(4)
            r_d = p_d.add_run(desc)
            r_d.italic = True
            r_d.font.name = "Times New Roman"
            r_d.font.size = Pt(11)

        code_text = ""
        if os.path.exists(file_path):
            with open(file_path, "r", encoding="utf-8", errors="ignore") as f:
                code_text = f.read().strip()

        tbl = doc.add_table(rows=1, cols=1)
        tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
        cell = tbl.cell(0, 0)
        set_cell_background(cell, "F8F9FA")
        set_cell_margins(cell, top=80, bottom=80, left=120, right=120)

        tcPr = cell._tc.get_or_add_tcPr()
        borders = parse_xml(f'<w:tcBorders {nsdecls("w")}>'
                            f'<w:left w:val="single" w:sz="18" w:space="0" w:color="1F4E78"/>'
                            f'<w:top w:val="single" w:sz="4" w:space="0" w:color="E2E8F0"/>'
                            f'<w:right w:val="single" w:sz="4" w:space="0" w:color="E2E8F0"/>'
                            f'<w:bottom w:val="single" w:sz="4" w:space="0" w:color="E2E8F0"/>'
                            f'</w:tcBorders>')
        tcPr.append(borders)

        p_c = cell.paragraphs[0]
        p_c.paragraph_format.line_spacing = 1.05
        p_c.paragraph_format.space_before = Pt(2)
        p_c.paragraph_format.space_after = Pt(2)
        r_c = p_c.add_run(code_text)
        r_c.font.name = "Consolas"
        r_c.font.size = Pt(8.5)
        r_c.font.color.rgb = RGBColor(0x22, 0x22, 0x22)

        p_sp = doc.add_paragraph()
        p_sp.paragraph_format.space_after = Pt(6)

    add_title("PHỤ LỤC: MÃ NGUỒN CHƯƠNG TRÌNH")
    add_desc("Phụ lục này liệt kê đầy đủ toàn bộ mã nguồn của các lớp Java nòng cốt cấu thành nên Hệ thống Chat Hỗ trợ Khách hàng Trực tuyến (Live Chat Support), bao gồm: Giao thức ứng dụng, Lớp đối tượng tin nhắn đa phương tiện tuần tự hóa, Cơ chế mã hóa AES-128 CBC ngẫu nhiên IV & Băm Salted SHA-256, Bộ kiểm duyệt ngôn từ vi phạm tự động, Máy chủ TCP Blocking I/O kết hợp CachedThreadPool, Máy chủ chuyển tiếp truyền thông đa phương tiện thời gian thực UDP Relay Server, và Trợ lý ảo Google Gemini AI.")

    sections = [
        ("Phụ lục 1. Giao thức truyền thông ứng dụng (Protocol.java)",
         "src/main/java/com/cmp180/livechat/common/Protocol.java",
         "Quy định toàn bộ 23 mã lệnh giao thức trao đổi dữ liệu tầng ứng dụng giữa Customer, Staff và LiveChatServer."),

        ("Phụ lục 2. Lớp đối tượng tin nhắn tuần tự hóa (Message.java)",
         "src/main/java/com/cmp180/livechat/common/Message.java",
         "Lớp thực thể đóng gói đa phương tiện (văn bản, tệp tin, dữ liệu ảnh, trích dẫn trả lời, thu hồi) cài đặt Serializable."),

        ("Phụ lục 3. Lớp An toàn thông tin & Mật mã học (SecurityManager.java)",
         "src/main/java/com/cmp180/livechat/common/SecurityManager.java",
         "Cài đặt thuật toán mã hóa đối xứng AES-128 CBC với IV 16-byte ngẫu nhiên và thuật toán băm mật khẩu an toàn SHA-256 kèm Salt 16-byte."),

        ("Phụ lục 4. Bộ kiểm duyệt từ khóa vi phạm tự động (ProfanityFilter.java)",
         "src/main/java/com/cmp180/livechat/common/ProfanityFilter.java",
         "Động cơ phân tích biểu thức chính quy (Regex) đa tầng lọc từ ngữ thô tục tiếng Việt có dấu, không dấu và kéo dài nguyên âm thành '***'."),

        ("Phụ lục 5. Cấu trúc gói tin đa phương tiện UDP (UdpMediaPacket.java)",
         "src/main/java/com/cmp180/livechat/common/UdpMediaPacket.java",
         "Định dạng đóng gói nhị phân cho các gói tin âm thanh thoại (TYPE_VOICE) và khung hình video JPEG (TYPE_VIDEO) truyền qua UDP."),

        ("Phụ lục 6. Điểm khởi nhập và Máy chủ Socket TCP (ServerMain.java)",
         "src/main/java/com/cmp180/livechat/server/ServerMain.java",
         "Khởi tạo ServerSocket trên cổng 5000, quản lý hồ bơi luồng CachedThreadPool và điều phối các dịch vụ nền tảng."),

        ("Phụ lục 7. Máy chủ trung chuyển đa phương tiện thời gian thực (UdpRelayServer.java)",
         "src/main/java/com/cmp180/livechat/server/UdpRelayServer.java",
         "Máy chủ UDP Datagram độc lập với bộ đệm 2MB, chuyển tiếp âm thanh và hình ảnh trong bộ nhớ với độ trễ < 1ms, vượt rào cản NAT.")
    ]

    for title, fpath, desc in sections:
        add_appendix_section(title, fpath, desc)

    doc.save(output_path)
    print("Xuat thanh cong file:", output_path)

if __name__ == "__main__":
    create_appendix_doc()
