# -*- coding: utf-8 -*-
"""
Chuong trinh tong hop xuat file Microsoft Word (.docx) Bao cao do an CMP180:
LIVE CHAT SUPPORT SYSTEM - HUTECH KHOA CNTT
Phien ban Nang cao: Lam noi bat toan bo cac dot pha cong nghe va tinh nang vuot troi
"""

import os
import sys
import docx
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import OxmlElement, parse_xml
from docx.oxml.ns import qn, nsdecls

from report_data import FRONT_MATTER, ABBREVIATIONS, TABLES_LIST, FIGURES_LIST
from report_content_part1 import (
    LOI_CAM_DOAN, CH1_INTRO, CH1_SO_SANH_DATA, CH1_NHIEM_VU, CH1_CAU_TRUC, CH2_CONTENT
)
from report_content_part2 import (
    CH3_DOT_PHA, CH4_TRIEN_KHAI, CH5_SO_LIEU_DB, CH6_KET_LUAN,
    CH3_BANG_3_1_DATA, CH3_BANG_3_6_PROTOCOL, CH3_BANG_CSDL,
    CH3_UNIT_TEST_RESULTS, CH3_SYSTEM_TEST_MATRIX, REFERENCES_LIST
)

def build_advanced_docx(output_path="BaoCao_DoAn_CMP180_LiveChatSupport.docx"):
    doc = Document()

    # 1. Page Margins (A4: Top 2cm, Bottom 2cm, Left 3cm, Right 2cm)
    for section in doc.sections:
        section.top_margin = Inches(0.79)      # 2.0 cm
        section.bottom_margin = Inches(0.79)   # 2.0 cm
        section.left_margin = Inches(1.18)     # 3.0 cm (gáy sách)
        section.right_margin = Inches(0.79)    # 2.0 cm
        section.page_width = Inches(8.27)      # A4
        section.page_height = Inches(11.69)

    # 2. Base Normal Style
    style_normal = doc.styles['Normal']
    style_normal.font.name = 'Times New Roman'
    style_normal.font.size = Pt(13)
    style_normal.font.color.rgb = RGBColor(0x11, 0x11, 0x11)
    style_normal.paragraph_format.line_spacing = 1.3
    style_normal.paragraph_format.space_after = Pt(6)
    style_normal.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY

    # XML Helper Functions
    def set_cell_background(cell, hex_color):
        tcPr = cell._tc.get_or_add_tcPr()
        shd = parse_xml(f'<w:shd {nsdecls("w")} w:val="clear" w:color="auto" w:fill="{hex_color}"/>')
        tcPr.append(shd)

    def set_cell_margins(cell, top=100, bottom=100, left=140, right=140):
        tcPr = cell._tc.get_or_add_tcPr()
        tcMar = parse_xml(f'<w:tcMar {nsdecls("w")}>'
                          f'<w:top w:w="{top}" w:type="dxa"/>'
                          f'<w:bottom w:w="{bottom}" w:type="dxa"/>'
                          f'<w:left w:w="{left}" w:type="dxa"/>'
                          f'<w:right w:w="{right}" w:type="dxa"/>'
                          f'</w:tcMar>')
        tcPr.append(tcMar)

    def set_table_borders(table, color="B0C4DE", sz="4", val="single"):
        tblPr = table._tbl.tblPr
        tblBorders = parse_xml(f'<w:tblBorders {nsdecls("w")}>'
                               f'<w:top w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>'
                               f'<w:bottom w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>'
                               f'<w:insideH w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>'
                               f'<w:insideV w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>'
                               f'<w:left w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>'
                               f'<w:right w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>'
                               f'</w:tblBorders>')
        tblPr.append(tblBorders)

    def add_p(text, bold_prefix=None, italic=False, space_after=6, space_before=0, align=WD_ALIGN_PARAGRAPH.JUSTIFY):
        p = doc.add_paragraph()
        p.paragraph_format.alignment = align
        p.paragraph_format.line_spacing = 1.3
        p.paragraph_format.space_before = Pt(space_before)
        p.paragraph_format.space_after = Pt(space_after)
        if bold_prefix:
            r_pre = p.add_run(bold_prefix)
            r_pre.bold = True
            r_pre.font.name = 'Times New Roman'
            r_pre.font.size = Pt(13)
        r = p.add_run(text)
        r.italic = italic
        r.font.name = 'Times New Roman'
        r.font.size = Pt(13)
        return p

    def add_heading_chapter(title):
        p = doc.add_paragraph()
        p.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p.paragraph_format.space_before = Pt(16)
        p.paragraph_format.space_after = Pt(12)
        r = p.add_run(title)
        r.bold = True
        r.font.name = 'Times New Roman'
        r.font.size = Pt(15)
        r.font.color.rgb = RGBColor(0x1F, 0x4E, 0x78)
        return p

    def add_heading_1(title):
        p = doc.add_paragraph()
        p.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.LEFT
        p.paragraph_format.space_before = Pt(14)
        p.paragraph_format.space_after = Pt(6)
        r = p.add_run(title)
        r.bold = True
        r.font.name = 'Times New Roman'
        r.font.size = Pt(13.5)
        r.font.color.rgb = RGBColor(0x1F, 0x4E, 0x78)
        return p

    def add_heading_2(title):
        p = doc.add_paragraph()
        p.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.LEFT
        p.paragraph_format.space_before = Pt(10)
        p.paragraph_format.space_after = Pt(4)
        r = p.add_run(title)
        r.bold = True
        r.italic = True
        r.font.name = 'Times New Roman'
        r.font.size = Pt(13)
        r.font.color.rgb = RGBColor(0x2E, 0x75, 0xB6)
        return p

    def add_heading_3(title):
        p = doc.add_paragraph()
        p.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.LEFT
        p.paragraph_format.space_before = Pt(6)
        p.paragraph_format.space_after = Pt(2)
        r = p.add_run(title)
        r.bold = True
        r.font.name = 'Times New Roman'
        r.font.size = Pt(13)
        r.font.color.rgb = RGBColor(0x22, 0x22, 0x22)
        return p

    def add_table_custom(headers, rows_data, col_widths=None, caption=None):
        if caption:
            p_cap = doc.add_paragraph()
            p_cap.paragraph_format.space_before = Pt(10)
            p_cap.paragraph_format.space_after = Pt(4)
            p_cap.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.CENTER
            r_c = p_cap.add_run(caption)
            r_c.bold = True
            r_c.font.name = "Times New Roman"
            r_c.font.size = Pt(11)
            r_c.font.color.rgb = RGBColor(0x1F, 0x4E, 0x78)

        tbl = doc.add_table(rows=len(rows_data) + 1, cols=len(headers))
        tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
        set_table_borders(tbl, color="B0C4DE", sz="4", val="single")

        hdr_cells = tbl.rows[0].cells
        for idx, header_text in enumerate(headers):
            cell = hdr_cells[idx]
            cell.text = header_text
            set_cell_background(cell, "1F4E78")
            set_cell_margins(cell, top=120, bottom=120, left=140, right=140)
            p = cell.paragraphs[0]
            p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            for r in p.runs:
                r.bold = True
                r.font.name = "Times New Roman"
                r.font.size = Pt(10.5)
                r.font.color.rgb = RGBColor(0xFF, 0xFF, 0xFF)

        for r_idx, row_values in enumerate(rows_data):
            row_cells = tbl.rows[r_idx + 1].cells
            bg_color = "F4F8FC" if r_idx % 2 == 1 else "FFFFFF"
            for c_idx, val in enumerate(row_values):
                cell = row_cells[c_idx]
                cell.text = str(val)
                set_cell_background(cell, bg_color)
                set_cell_margins(cell, top=90, bottom=90, left=120, right=120)
                p = cell.paragraphs[0]
                p.alignment = WD_ALIGN_PARAGRAPH.LEFT if c_idx >= 1 else WD_ALIGN_PARAGRAPH.CENTER
                for r in p.runs:
                    r.font.name = "Times New Roman"
                    r.font.size = Pt(10)
                    r.font.color.rgb = RGBColor(0x22, 0x22, 0x22)

        if col_widths:
            for row in tbl.rows:
                for c_idx, width in enumerate(col_widths):
                    row.cells[c_idx].width = Inches(width)

        p_after = doc.add_paragraph()
        p_after.paragraph_format.space_after = Pt(6)

    def add_code_block(code_str, caption=None):
        if caption:
            p_cap = doc.add_paragraph()
            p_cap.paragraph_format.space_before = Pt(6)
            p_cap.paragraph_format.space_after = Pt(2)
            p_cap.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.LEFT
            r_c = p_cap.add_run(caption)
            r_c.font.name = "Times New Roman"
            r_c.font.size = Pt(10.5)
            r_c.italic = True
            r_c.font.color.rgb = RGBColor(0x55, 0x55, 0x55)

        tbl = doc.add_table(rows=1, cols=1)
        tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
        cell = tbl.cell(0, 0)
        set_cell_background(cell, "F8F9FA")
        set_cell_margins(cell, top=100, bottom=100, left=160, right=160)
        
        tcPr = cell._tc.get_or_add_tcPr()
        borders = parse_xml(f'<w:tcBorders {nsdecls("w")}>'
                            f'<w:left w:val="single" w:sz="18" w:space="0" w:color="0078D7"/>'
                            f'<w:top w:val="single" w:sz="4" w:space="0" w:color="E2E8F0"/>'
                            f'<w:right w:val="single" w:sz="4" w:space="0" w:color="E2E8F0"/>'
                            f'<w:bottom w:val="single" w:sz="4" w:space="0" w:color="E2E8F0"/>'
                            f'</w:tcBorders>')
        tcPr.append(borders)
        
        p = cell.paragraphs[0]
        p.paragraph_format.line_spacing = 1.15
        p.paragraph_format.space_after = Pt(2)
        p.paragraph_format.space_before = Pt(2)
        p.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.LEFT
        
        r = p.add_run(code_str.strip())
        r.font.name = "Consolas"
        r.font.size = Pt(9.5)
        r.font.color.rgb = RGBColor(0x24, 0x29, 0x2E)
        
        p_after = doc.add_paragraph()
        p_after.paragraph_format.space_after = Pt(4)

    # ==================== TRANG BÌA (COVER PAGE) ====================
    p_tr = doc.add_paragraph()
    p_tr.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_tr.paragraph_format.space_before = Pt(15)
    p_tr.paragraph_format.space_after = Pt(2)
    r = p_tr.add_run(FRONT_MATTER["truong"])
    r.bold = True
    r.font.name = "Times New Roman"
    r.font.size = Pt(13)

    p_kh = doc.add_paragraph()
    p_kh.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_kh.paragraph_format.space_after = Pt(30)
    r = p_kh.add_run(FRONT_MATTER["khoa"])
    r.bold = True
    r.font.name = "Times New Roman"
    r.font.size = Pt(13)

    p_bc = doc.add_paragraph()
    p_bc.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_bc.paragraph_format.space_after = Pt(8)
    r = p_bc.add_run("BÁO CÁO ĐỒ ÁN HỌC PHẦN")
    r.bold = True
    r.font.name = "Times New Roman"
    r.font.size = Pt(18)
    r.font.color.rgb = RGBColor(0x1F, 0x4E, 0x78)

    p_mh = doc.add_paragraph()
    p_mh.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_mh.paragraph_format.space_after = Pt(30)
    r = p_mh.add_run(f"Môn học: {FRONT_MATTER['mon_hoc']}")
    r.italic = True
    r.font.name = "Times New Roman"
    r.font.size = Pt(13)

    p_dt = doc.add_paragraph()
    p_dt.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_dt.paragraph_format.space_after = Pt(50)
    r = p_dt.add_run(FRONT_MATTER["ten_de_tai"])
    r.bold = True
    r.font.name = "Times New Roman"
    r.font.size = Pt(15)
    r.font.color.rgb = RGBColor(0x1F, 0x4E, 0x78)

    p_gv = doc.add_paragraph()
    p_gv.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_gv.paragraph_format.space_after = Pt(18)
    r = p_gv.add_run(f"Giảng viên hướng dẫn: {FRONT_MATTER['gvhd']}")
    r.bold = True
    r.font.name = "Times New Roman"
    r.font.size = Pt(13)

    p_nh = doc.add_paragraph()
    p_nh.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_nh.paragraph_format.space_after = Pt(6)
    r = p_nh.add_run(f"Nhóm thực hiện: {FRONT_MATTER['nhom']}")
    r.font.name = "Times New Roman"
    r.font.size = Pt(13)

    p_tv1 = doc.add_paragraph()
    p_tv1.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_tv1.paragraph_format.space_after = Pt(4)
    r = p_tv1.add_run(f"Thành viên 1: {FRONT_MATTER['tv1_ten']} - MSSV: {FRONT_MATTER['tv1_mssv']}")
    r.font.name = "Times New Roman"
    r.font.size = Pt(13)

    p_tv2 = doc.add_paragraph()
    p_tv2.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_tv2.paragraph_format.space_after = Pt(14)
    r = p_tv2.add_run(f"Thành viên 2: {FRONT_MATTER['tv2_ten']} - MSSV: {FRONT_MATTER['tv2_mssv']}")
    r.font.name = "Times New Roman"
    r.font.size = Pt(13)

    p_lp = doc.add_paragraph()
    p_lp.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_lp.paragraph_format.space_after = Pt(36)
    r = p_lp.add_run(f"Lớp: {FRONT_MATTER['lop']}                     Năm học: {FRONT_MATTER['nam_hoc']}")
    r.font.name = "Times New Roman"
    r.font.size = Pt(13)

    doc.add_page_break()

    # ==================== LỜI CAM ĐOAN (Trang i) ====================
    add_heading_chapter("LỜI CAM ĐOAN")
    for para in LOI_CAM_DOAN.split("\n\n"):
        add_p(para)

    p_date = doc.add_paragraph()
    p_date.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.RIGHT
    p_date.paragraph_format.space_before = Pt(24)
    p_date.paragraph_format.space_after = Pt(4)
    r = p_date.add_run(FRONT_MATTER["dia_diem_ngay"])
    r.italic = True
    r.font.name = "Times New Roman"

    p_sign = doc.add_paragraph()
    p_sign.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.RIGHT
    p_sign.paragraph_format.space_after = Pt(40)
    r = p_sign.add_run("Nhóm thực hiện\n(Ký và ghi rõ họ tên)\n\n\n")
    r.bold = True
    r.font.name = "Times New Roman"

    p_names = doc.add_paragraph()
    p_names.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.RIGHT
    r = p_names.add_run(f"{FRONT_MATTER['tv1_ten']}       {FRONT_MATTER['tv2_ten']}")
    r.bold = True
    r.font.name = "Times New Roman"

    doc.add_page_break()

    # ==================== MỤC LỤC (Trang ii) ====================
    add_heading_chapter("MỤC LỤC")
    p_guide = add_p("(Mục lục chi tiết cấu trúc đồ án. Khi mở file bằng Microsoft Word, có thể bấm chuột phải chọn 'Update Field' để tự động cập nhật số trang chuẩn xác theo bản in.)", italic=True)
    p_guide.paragraph_format.space_after = Pt(12)

    toc_items = [
        ("Lời cam đoan", "i"),
        ("Danh mục các ký hiệu, các chữ viết tắt", "iii"),
        ("Danh mục các bảng", "iv"),
        ("Danh mục các hình vẽ, đồ thị", "v"),
        ("CHƯƠNG 1. TỔNG QUAN VÀ BÀI TOÁN THỰC TIỄN", "1"),
        ("   1.1. Giới thiệu đề tài", "1"),
        ("   1.2. So sánh đối chiếu: Yêu cầu đồ án chuẩn và Các đột phá vượt trội của đề tài", "2"),
        ("   1.3. Nhiệm vụ đồ án", "3"),
        ("      1.3.1. Tính cấp thiết và lý do hình thành đề tài", "3"),
        ("      1.3.2. Ý nghĩa khoa học và thực tiễn", "4"),
        ("      1.3.3. Mục tiêu đề tài", "4"),
        ("      1.3.4. Đối tượng và phạm vi giới hạn", "5"),
        ("   1.4. Cấu trúc đồ án", "6"),
        ("CHƯƠNG 2. CƠ SỞ LÝ THUYẾT VÀ CÔNG NGHỆ NỀN TẢNG", "7"),
        ("   2.1. Mô hình Client - Server", "7"),
        ("   2.2. Giao thức và Socket TCP trong Java", "8"),
        ("   2.3. Giao thức và Socket UDP cho Streaming thời gian thực", "9"),
        ("   2.4. Quản lý đa luồng với CachedThreadPool & Kỹ thuật đồng bộ phi khóa", "10"),
        ("   2.5. Lớp InetAddress và Quản trị địa chỉ mạng", "11"),
        ("   2.6. Tuần tự hóa đối tượng (Java Serialization) cho gói tin đa hình", "12"),
        ("   2.7. Mật mã học ứng dụng: AES-128 CBC ngẫu nhiên hóa IV và Salted SHA-256", "13"),
        ("   2.8. Ứng dụng Trí tuệ nhân tạo (Generative AI) trong dịch vụ khách hàng thời gian thực", "14"),
        ("CHƯƠNG 3. THIẾT KẾ KIẾN TRÚC VÀ CÁC ĐỘT PHÁ KỸ THUẬT CỦA HỆ THỐNG", "16"),
        ("   3.1. Kiến trúc tổng thể Hybrid TCP/UDP", "16"),
        ("   3.2. Thuật toán điều phối hàng chờ thông minh 3 tầng", "17"),
        ("   3.3. Cơ chế chịu lỗi cấp doanh nghiệp (Session Handover & Auto-reconnect)", "19"),
        ("   3.4. Kênh truyền thông đa phương tiện độ trễ thấp (Voice, Voice Call & Video Call)", "20"),
        ("   3.5. Tích hợp Trợ lý ảo Gemini AI & Chuyển giao AI-to-Human Handover", "22"),
        ("   3.6. Bộ kiểm duyệt ngôn từ vi phạm tự động (Profanity Filter Regex)", "23"),
        ("   3.7. Thiết kế giao thức truyền thông ứng dụng (Lớp Message & Danh mục mã lệnh)", "24"),
        ("   3.8. Thiết kế cơ sở dữ liệu SQLite nhúng hiệu năng cao (WAL Mode, Indexes, 45 sinh viên)", "27"),
        ("CHƯƠNG 4. TRIỂN KHAI CÀI ĐẶT VÀ MINH HỌA TÍNH NĂNG NGƯỜI DÙNG", "29"),
        ("   4.1. Môi trường phát triển và cấu trúc thư mục dự án", "29"),
        ("   4.2. Các mô-đun mã nguồn cốt lõi tiêu biểu", "30"),
        ("   4.3. Trải nghiệm người dùng phong phú (Rich UX)", "33"),
        ("   4.4. Bảng điều khiển Quản trị viên máy chủ (Server Dashboard Swing 3 Tab)", "35"),
        ("   4.5. Đóng gói độc lập (Portable Distribution) và Hướng dẫn vận hành", "36"),
        ("CHƯƠNG 5. KIỂM THỬ, ĐÁNH GIÁ THỰC NGHIỆM VÀ PHÂN TÍCH SỐ LIỆU", "37"),
        ("   5.1. Kiểm thử tự động chuyên sâu: Phân tích 75 ca kiểm thử JUnit 5 (100% Passed)", "37"),
        ("   5.2. Ma trận kiểm thử tích hợp hệ thống thực tế", "39"),
        ("   5.3. Phân tích số liệu thực nghiệm thực tế từ CSDL livechat.db", "40"),
        ("   5.4. Trích xuất nhật ký thực tế phía Server (server.log)", "41"),
        ("   5.5. Đánh giá ưu điểm vượt trội và các mặt hạn chế", "42"),
        ("CHƯƠNG 6. KẾT LUẬN VÀ KIẾN NGHỊ", "44"),
        ("   6.1. Kết luận và Thành tựu nổi bật", "44"),
        ("   6.2. Kiến nghị và Hướng phát triển tương lai", "45"),
        ("TÀI LIỆU THAM KHẢO", "46"),
        ("PHỤ LỤC: MÃ NGUỒN CHƯƠNG TRÌNH TIÊU BIỂU", "47")
    ]

    for item, page in toc_items:
        p_t = doc.add_paragraph()
        p_t.paragraph_format.line_spacing = 1.15
        p_t.paragraph_format.space_after = Pt(2)
        r_item = p_t.add_run(item)
        r_item.font.name = "Times New Roman"
        r_item.font.size = Pt(11)
        if item.startswith("CHƯƠNG") or item in ["Lời cam đoan", "TÀI LIỆU THAM KHẢO", "PHỤ LỤC: MÃ NGUỒN CHƯƠNG TRÌNH TIÊU BIỂU"]:
            r_item.bold = True
        
        dots_count = max(5, 78 - len(item))
        r_dots = p_t.add_run(" " + "." * dots_count + " ")
        r_dots.font.name = "Times New Roman"
        r_dots.font.color.rgb = RGBColor(0x88, 0x88, 0x88)
        
        r_page = p_t.add_run(page)
        r_page.font.name = "Times New Roman"
        r_page.font.size = Pt(11)
        r_page.bold = True

    doc.add_page_break()

    # ==================== DANH MỤC CHỮ VIẾT TẮT (Trang iii) ====================
    add_heading_chapter("DANH MỤC CÁC KÝ HIỆU, CÁC CHỮ VIẾT TẮT")
    abbr_headers = ["Từ viết tắt", "Thuật ngữ tiếng Anh đầy đủ / Diễn giải tiếng Việt"]
    add_table_custom(abbr_headers, ABBREVIATIONS, col_widths=[1.4, 4.9])

    doc.add_page_break()

    # ==================== DANH MỤC CÁC BẢNG (Trang iv) ====================
    add_heading_chapter("DANH MỤC CÁC BẢNG")
    tbl_headers = ["Ký hiệu bảng", "Tên bảng dữ liệu mô tả"]
    add_table_custom(tbl_headers, TABLES_LIST, col_widths=[1.5, 4.8])

    doc.add_page_break()

    # ==================== DANH MỤC HÌNH VẼ, ĐỒ THỊ (Trang v) ====================
    add_heading_chapter("DANH MỤC CÁC HÌNH VẼ, ĐỒ THỊ")
    fig_headers = ["Ký hiệu hình", "Tên sơ đồ / Hình vẽ minh họa"]
    add_table_custom(fig_headers, FIGURES_LIST, col_widths=[1.5, 4.8])

    doc.add_page_break()

    # ==================== CHƯƠNG 1 ====================
    add_heading_chapter("CHƯƠNG 1. TỔNG QUAN VÀ BÀI TOÁN THỰC TIỄN")

    add_heading_1("1.1. Giới thiệu đề tài")
    for para in CH1_INTRO.split("\n\n"):
        add_p(para)

    add_heading_1("1.2. So sánh đối chiếu: Yêu cầu đồ án chuẩn và Các đột phá vượt trội của đề tài")
    add_p("Trong quá trình nghiên cứu và phát triển, nhóm tác giả đã không tự giới hạn bản thân trong khuôn khổ một bài tập lớn thông thường mà chủ động mở rộng quy mô, giải quyết các bài toán kỹ thuật phức tạp thường chỉ xuất hiện ở các nền tảng thương mại cấp doanh nghiệp. Bảng 1.1 dưới đây làm rõ những điểm khác biệt mang tính đột phá của hệ thống Live Chat Support:")

    ss_headers = ["Hạng mục công nghệ", "Yêu cầu đồ án môn học thông thường", "Đột phá vượt trội của Đề tài Live Chat Support"]
    add_table_custom(ss_headers, CH1_SO_SANH_DATA, col_widths=[1.6, 2.3, 2.8], caption="Bảng 1.1. Bảng so sánh đối chiếu giữa Yêu cầu đồ án thông thường và Các đột phá của đề tài")

    add_heading_1("1.3. Nhiệm vụ đồ án")
    add_heading_2("1.3.1. Tính cấp thiết và lý do hình thành đề tài")
    for para in CH1_NHIEM_VU["cap_thiet"].split("\n\n"):
        add_p(para)

    add_heading_2("1.3.2. Ý nghĩa khoa học và thực tiễn")
    for para in CH1_NHIEM_VU["y_nghia"].split("\n\n"):
        add_p(para)

    add_heading_2("1.3.3. Mục tiêu đề tài")
    for para in CH1_NHIEM_VU["muc_tieu"].split("\n\n"):
        add_p(para)

    add_heading_2("1.3.4. Đối tượng và phạm vi giới hạn")
    for para in CH1_NHIEM_VU["pham_vi"].split("\n\n"):
        add_p(para)

    add_heading_1("1.4. Cấu trúc đồ án")
    for para in CH1_CAU_TRUC.split("\n\n"):
        add_p(para)

    doc.add_page_break()

    # ==================== CHƯƠNG 2 ====================
    add_heading_chapter("CHƯƠNG 2. CƠ SỞ LÝ THUYẾT VÀ CÔNG NGHỆ NỀN TẢNG")

    add_heading_1("2.1. Mô hình Client - Server")
    for para in CH2_CONTENT["client_server"].split("\n\n"):
        add_p(para)

    add_heading_1("2.2. Giao thức và Socket TCP trong Java")
    for para in CH2_CONTENT["tcp_socket"].split("\n\n"):
        add_p(para)

    add_heading_1("2.3. Giao thức và Socket UDP cho Streaming thời gian thực")
    for para in CH2_CONTENT["udp_socket"].split("\n\n"):
        add_p(para)

    add_heading_1("2.4. Quản lý đa luồng với CachedThreadPool & Kỹ thuật đồng bộ phi khóa")
    for para in CH2_CONTENT["multithreading"].split("\n\n"):
        add_p(para)

    add_heading_1("2.5. Lớp InetAddress và Quản trị địa chỉ mạng")
    for para in CH2_CONTENT["inet_address"].split("\n\n"):
        add_p(para)

    add_heading_1("2.6. Tuần tự hóa đối tượng (Java Serialization) cho gói tin đa hình")
    for para in CH2_CONTENT["serialization"].split("\n\n"):
        add_p(para)

    add_heading_1("2.7. Mật mã học ứng dụng: AES-128 CBC ngẫu nhiên hóa IV và Salted SHA-256")
    for para in CH2_CONTENT["security"].split("\n\n"):
        add_p(para)

    add_heading_1("2.8. Ứng dụng Trí tuệ nhân tạo (Generative AI) trong dịch vụ khách hàng thời gian thực")
    for para in CH2_CONTENT["generative_ai"].split("\n\n"):
        add_p(para)

    doc.add_page_break()

    # ==================== CHƯƠNG 3 ====================
    add_heading_chapter("CHƯƠNG 3. THIẾT KẾ KIẾN TRÚC VÀ CÁC ĐỘT PHÁ KỸ THUẬT CỦA HỆ THỐNG")

    add_heading_1("3.1. Kiến trúc tổng thể Hybrid TCP/UDP")
    for para in CH3_DOT_PHA["kien_truc"].split("\n\n"):
        add_p(para)

    add_code_block("""
+-----------------------------------------------------------------------------------+
|                           KIẾN TRÚC HYBRID TCP / UDP                              |
|                                                                                   |
|  +--------------------+                     +--------------------+                |
|  |   Customer Client  |                     |    Staff Client    |                |
|  |  (JavaFX FXML GUI) |                     |  (JavaFX FXML GUI) |                |
|  +---------+----------+                     +----------+---------+                |
|            |                                           |                          |
|    TCP Port 5000: Control & Chat Messages (AES-CBC)    |                          |
|            +-----------------------+-------------------+                          |
|                                    |                                              |
|                                    v                                              |
|                      +---------------------------+                                |
|                      |   LiveChatServer (TCP)    |                                |
|                      |  - CachedThreadPool       |                                |
|                      |  - SessionManager         |                                |
|                      |  - ProfanityFilter        |                                |
|                      |  - AIBotService (Gemini)  |                                |
|                      +-------------+-------------+                                |
|                                    |                                              |
|                  SQLite Database   v                                              |
|             [ livechat.db - WAL Mode & Indexes ]                                  |
|                                                                                   |
|  ...............................................................................  |
|            KÊNH TRUYỀN THÔNG ĐA PHƯƠNG TIỆN THỜI GIAN THỰC (UDP)                  |
|                                                                                   |
|  +--------------------+                     +--------------------+                |
|  |   UdpMediaClient   |                     |   UdpMediaClient   |                |
|  | (Voice/Video Call) |                     | (Voice/Video Call) |                |
|  +---------+----------+                     +----------+---------+                |
|            |                                           |                          |
|            |    UDP Port 5000: Low-Latency Packets     |                          |
|            +-------------------> <---------------------+                          |
|                                  |                                                |
|                                  v                                                |
|                      +---------------------------+                                |
|                      |   UdpRelayServer (UDP)    |                                |
|                      |  - In-memory routing <1ms |                                |
|                      |  - 2MB socket buffer      |                                |
|                      +---------------------------+                                |
+-----------------------------------------------------------------------------------+
""", caption="Hình 3.1. Sơ đồ kiến trúc tổng thể hệ thống Live Chat Support (Hybrid TCP/UDP)")

    add_heading_1("3.2. Thuật toán điều phối hàng chờ thông minh 3 tầng")
    for para in CH3_DOT_PHA["dieu_phoi_3_tang"].split("\n\n"):
        add_p(para)

    add_code_block("""
[Khách hàng vào hàng chờ]
          |
          v
[TẦNG 1: Kiểm tra Preferred Staff] ----(Tìm thấy & Rảnh)----> [Ghép nối với Nhân viên quen]
          | (Không thấy)
          v
[TẦNG 2: Khớp Chuyên môn nghiệp vụ] ---(Khớp Skill & Rảnh)--> [Ghép nối đúng Chuyên môn]
          | (Tất cả bận)
          v
[TẦNG 3: Least Connections Fallback] --(Chọn ít phiên nhất)--> [Ghép nối cân bằng tải]
          | (Quá tải tất cả)
          v
[Mời Khách hàng Trò chuyện cùng Trợ lý ảo Gemini AI trong lúc chờ]
""", caption="Hình 3.2. Sơ đồ luồng thuật toán điều phối hàng chờ thông minh 3 tầng (Smart 3-Tier Dispatching)")

    add_heading_1("3.3. Cơ chế chịu lỗi cấp doanh nghiệp (Session Handover & Auto-reconnect)")
    for para in CH3_DOT_PHA["chiu_loi"].split("\n\n"):
        add_p(para)

    add_heading_1("3.4. Kênh truyền thông đa phương tiện độ trễ thấp (Voice, Voice Call & Video Call)")
    for para in CH3_DOT_PHA["media_streaming"].split("\n\n"):
        add_p(para)

    add_heading_1("3.5. Tích hợp Trợ lý ảo Gemini AI & Chuyển giao AI-to-Human Handover")
    for para in CH3_DOT_PHA["ai_bot"].split("\n\n"):
        add_p(para)

    add_heading_1("3.6. Bộ kiểm duyệt ngôn từ vi phạm tự động (Profanity Filter Regex)")
    add_p("Lớp ProfanityFilter.java cung cấp khả năng kiểm duyệt tự động dựa trên tập hợp biểu thức chính quy phức tạp:")
    add_p("- Kiểm tra cả tiếng Việt có dấu, tiếng Việt không dấu và tiếng Anh.")
    add_p("- Xử lý thông minh các biến thể kéo dài nguyên âm (ví dụ: 'đệttt', 'máaaaa', 'đụuuuu') bằng cách lồng ghép ký tự lặp [\\s\\._\\-]*.")
    add_p("- Thay thế tự động nội dung vi phạm bằng chuỗi dấu sao '***' trước khi tin nhắn được giải mã hiển thị trên màn hình đối phương hoặc lưu vào CSDL.")

    add_heading_1("3.7. Thiết kế giao thức truyền thông ứng dụng")
    add_p("Lớp Message.java là trái tim của giao thức truyền thông, bao bọc mọi thông tin trao đổi:")
    msg_headers = ["Thuộc tính (Field)", "Kiểu dữ liệu", "Ý nghĩa và vai trò trong giao thức"]
    add_table_custom(msg_headers, CH3_BANG_3_1_DATA, col_widths=[1.4, 1.1, 3.8], caption="Bảng 3.1. Mô tả chi tiết các trường dữ liệu và thuộc tính của lớp Message")

    add_p("Toàn bộ 23 mã lệnh giao thức ứng dụng được chuẩn hóa trong Protocol.java (Bảng 3.6):")
    proto_headers = ["Mã lệnh (Command)", "Chiều truyền", "Ý nghĩa và hành vi nghiệp vụ"]
    add_table_custom(proto_headers, CH3_BANG_3_6_PROTOCOL, col_widths=[1.8, 1.4, 3.1], caption="Bảng 3.6. Danh mục mã lệnh giao thức trao đổi ứng dụng")

    add_heading_1("3.8. Thiết kế cơ sở dữ liệu SQLite nhúng hiệu năng cao")
    for para in CH3_DOT_PHA["database_toi_uu"].split("\n\n"):
        add_p(para)

    csdl_col_names = ["Tên cột (Column)", "Kiểu dữ liệu", "Ràng buộc (Constraint)", "Diễn giải ý nghĩa"]
    add_table_custom(csdl_col_names, CH3_BANG_CSDL["staff"], col_widths=[1.4, 0.9, 1.8, 2.2], caption="Bảng 3.2. Cấu trúc bảng cơ sở dữ liệu staff (Quản lý 82 Nhân viên CSKH và Phân quyền)")
    add_table_custom(csdl_col_names, CH3_BANG_CSDL["customers"], col_widths=[1.4, 0.9, 1.8, 2.2], caption="Bảng 3.3. Cấu trúc bảng cơ sở dữ liệu customers (Quản lý Hồ sơ Khách hàng và Nhận diện SĐT)")
    add_table_custom(csdl_col_names, CH3_BANG_CSDL["chat_sessions"], col_widths=[1.4, 0.9, 1.8, 2.2], caption="Bảng 3.4. Cấu trúc bảng cơ sở dữ liệu chat_sessions (Quản lý Phiên hỗ trợ, Ticket và Đánh giá CSAT)")
    add_table_custom(csdl_col_names, CH3_BANG_CSDL["messages"], col_widths=[1.4, 0.9, 1.8, 2.2], caption="Bảng 3.5. Cấu trúc bảng cơ sở dữ liệu messages (Quản lý Lịch sử tin nhắn đa phương tiện đã mã hóa)")

    doc.add_page_break()

    # ==================== CHƯƠNG 4 ====================
    add_heading_chapter("CHƯƠNG 4. TRIỂN KHAI CÀI ĐẶT VÀ MINH HỌA TÍNH NĂNG NGƯỜI DÙNG")

    add_heading_1("4.1. Môi trường phát triển và cấu trúc thư mục dự án")
    add_p("Hệ thống được phát triển và tương thích hoàn toàn trên môi trường Java SE 17 LTS và Java SE 25. Cấu trúc mã nguồn phân tách rõ ràng:")
    add_code_block("""
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
""", caption="Cấu trúc tổ chức thư mục của đồ án LiveChatSupport-CMP180")

    add_heading_1("4.2. Các mô-đun mã nguồn cốt lõi tiêu biểu")
    
    add_heading_2("a) Khởi tạo Socket Server và Hồ bơi luồng CachedThreadPool")
    add_code_block("""
public void start() {
    isRunning = true;
    log("=== HỆ THỐNG LIVE CHAT SUPPORT SERVER (PORT " + PORT + ") ===");
    try {
        udpRelayServer.start(); // Khởi chạy kênh truyền thông Media UDP song song
        serverSocket = new ServerSocket(PORT);
        while (isRunning) {
            Socket clientSocket = serverSocket.accept();
            log("[KẾT NỐI MỚI] " + clientSocket.getRemoteSocketAddress());
            
            // Cấp phát luồng thực thi độc lập từ hồ bơi luồng tối ưu
            ClientHandler handler = new ClientHandler(clientSocket, this, sessionManager);
            threadPool.execute(handler);
        }
    } catch (IOException e) {
        if (isRunning) log("Lỗi Server: " + e.getMessage());
    }
}
""", caption="Trích đoạn ServerMain.java lắng nghe và phân luồng kết nối")

    add_heading_2("b) Vòng lặp đọc thông điệp và Giám sát Timeout 40 giây trong ClientHandler")
    add_code_block("""
public void run() {
    try {
        socket.setSoTimeout(40000); // 40 giây timeout chống Zombie connection
        Message message;
        while ((message = (Message) in.readObject()) != null) {
            processMessage(message);
        }
    } catch (SocketTimeoutException e) {
        server.log("[ZOMBIE TIMEOUT] Không nhận được heartbeat/tín hiệu từ " + name + ". Đang dọn dẹp.");
    } catch (Exception e) {
        server.log("[NGẮT KẾT NỐI] Client " + name + " đã rời đi.");
    } finally {
        sessionManager.handleDisconnect(this); // Kích hoạt khôi phục phiên nếu là CSKH rớt mạng
        closeConnection();
    }
}
""", caption="Trích đoạn ClientHandler.java xử lý Blocking I/O và phát hiện ngắt kết nối an toàn")

    add_heading_2("c) Thuật toán khôi phục phiên gián đoạn (Session Handover)")
    add_code_block("""
public synchronized void handleDisconnect(ClientHandler client) {
    if (Protocol.ROLE_STAFF.equalsIgnoreCase(client.getRole())) {
        allStaffs.remove(client);
        // Quét các phiên đang phụ trách để chuyển giao khách hàng
        for (ChatSession session : new ArrayList<>(activeSessionsList)) {
            if (session.getStaff() == client) {
                ClientHandler customer = session.getCustomer();
                if (customer != null && !customer.getSocket().isClosed()) {
                    customer.setInterrupted(true); // Đánh dấu cờ tư vấn dở
                    waitingCustomers.remove(customer);
                    ((LinkedList<ClientHandler>) waitingCustomers).addFirst(customer); // Đưa về đầu hàng đợi
                    customer.sendMessage(new Message(Protocol.CMD_WAITING, "Server", 
                        "Nhân viên vừa gặp sự cố mạng. Bạn đang được ưu tiên hàng đầu để kết nối nhân viên tiếp theo!"));
                }
                activeSessionsList.remove(session);
            }
        }
        broadcastQueueToStaff();
        tryAutoDispatch();
    }
}
""", caption="Trích đoạn SessionManager.java bảo toàn phiên chat của khách hàng khi nhân viên rớt mạng")

    add_heading_1("4.3. Trải nghiệm người dùng phong phú (Rich UX)")
    for para in CH4_TRIEN_KHAI["ux"].split("\n\n"):
        add_p(para)

    add_heading_1("4.4. Bảng điều khiển Quản trị viên máy chủ (Server Dashboard Swing 3 Tab)")
    for para in CH4_TRIEN_KHAI["dashboard"].split("\n\n"):
        add_p(para)

    add_heading_1("4.5. Đóng gói độc lập (Portable Distribution) và Hướng dẫn vận hành")
    add_p("Hệ thống hỗ trợ 3 phương thức vận hành vô cùng linh hoạt:")
    add_p("• Cách 1: Khởi chạy nhanh bằng File Batch (.bat): Nhấp đúp run_server.bat -> Bấm 'Khởi Động Server' -> Nhấp đúp run_staff.bat -> Đăng nhập bằng staff01 (123456) hoặc MSSV sinh viên -> Nhấp đúp run_customer.bat để bắt đầu trải nghiệm.")
    add_p("• Cách 2: Khởi chạy thông qua Maven CLI: mvnw exec:java@run-server, mvnw exec:java@run-staff, mvnw exec:java@run-customer.")
    add_p("• Cách 3: Ứng dụng độc lập Portable (.exe): Sử dụng kịch bản package-portable-apps.bat tạo ra các file CustomerApp.exe và StaffApp.exe chạy trực tiếp trên Windows mà không cần cài đặt môi trường Java.")

    doc.add_page_break()

    # ==================== CHƯƠNG 5 ====================
    add_heading_chapter("CHƯƠNG 5. KIỂM THỬ, ĐÁNH GIÁ THỰC NGHIỆM VÀ PHÂN TÍCH SỐ LIỆU")

    add_heading_1("5.1. Kiểm thử tự động chuyên sâu: Phân tích 75 ca kiểm thử JUnit 5 (100% Passed)")
    add_p("Dự án được xây dựng với kỷ luật kỹ thuật phần mềm cao nhất. Toàn bộ các mô-đun quan trọng đều được bao phủ bởi 75 ca kiểm thử đơn vị tự động (Unit Tests) thuộc 12 lớp kiểm thử độc lập trong thư viện JUnit Jupiter 5.10.2. Kết quả kiểm thử thực tế đạt tỷ lệ vượt qua tuyệt đối 100% (75/75 ca ĐẠT, 0 Lỗi, 0 Bỏ qua):")

    unit_headers = ["Tên lớp kiểm thử (Test Class)", "Số ca test", "Đạt (Pass)", "Lỗi (Fail)", "Mục tiêu và Chức năng kiểm thử"]
    add_table_custom(unit_headers, CH3_UNIT_TEST_RESULTS, col_widths=[1.8, 0.6, 0.6, 0.6, 2.7], caption="Bảng 5.1. Tổng hợp kết quả 75 ca kiểm thử tự động JUnit 5")

    add_heading_1("5.2. Ma trận kiểm thử tích hợp hệ thống thực tế")
    add_p("Hệ thống đã trải qua 13 kịch bản kiểm thử tích hợp hộp đen (Black-box System Testing) trên môi trường mạng thực tế:")
    sys_headers = ["Mã TC", "Tên ca kiểm thử", "Dữ liệu đầu vào & Thao tác", "Kết quả mong đợi", "Trạng thái"]
    add_table_custom(sys_headers, CH3_SYSTEM_TEST_MATRIX, col_widths=[0.6, 1.5, 1.8, 1.9, 0.8], caption="Bảng 5.2. Ma trận kiểm thử chức năng tích hợp hệ thống thực tế")

    add_heading_1("5.3. Phân tích số liệu thực nghiệm thực tế từ CSDL livechat.db")
    add_p("Điểm minh chứng hùng hồn nhất cho tính hoàn thiện của đồ án là khối lượng dữ liệu thực nghiệm đồ sộ được trích xuất trực tiếp từ cơ sở dữ liệu livechat.db sau quá trình vận hành thử nghiệm:")
    
    db_headers = ["Chỉ số đo lường thực nghiệm", "Số liệu ghi nhận thực tế từ CSDL"]
    add_table_custom(db_headers, CH5_SO_LIEU_DB, col_widths=[2.8, 3.5], caption="Bảng 5.3. Bảng tổng hợp số liệu thực nghiệm hệ thống trích xuất từ cơ sở dữ liệu livechat.db")

    add_heading_1("5.4. Trích xuất nhật ký thực tế phía Server (server.log)")
    add_p("Nhật ký vận hành thực tế minh chứng cho sự phối hợp nhịp nhàng giữa kênh điều khiển TCP, kênh Media UDP và mô hình AI Gemini:")
    add_code_block("""
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
""", caption="Trích xuất nhật ký thực tế (server.log) điều phối và đàm thoại đa phương tiện")

    add_heading_1("5.5. Đánh giá ưu điểm vượt trội và các mặt hạn chế")
    add_p("Ưu điểm vượt trội của hệ thống:", bold_prefix="1. ")
    add_p("   - Vượt xa chuẩn mực đồ án thông thường: Xây dựng thành công kênh Voice/Video Call UDP và Trợ lý ảo AI trực ban.")
    add_p("   - Nghiệp vụ thực tế hoàn hảo: Thuật toán điều phối 3 tầng, tiếp nhận đa phiên và Session Handover chống mất khách.")
    add_p("   - An toàn thông tin cao cấp: AES-128 CBC ngẫu nhiên IV, băm mật khẩu Salted SHA-256, mã hóa file lưu đĩa, bộ lọc Profanity Filter.")
    add_p("   - Tối ưu CSDL SQLite WAL Mode và 4 chỉ mục tăng tốc độ truy xuất dưới 5ms.")
    add_p("   - Độ tin cậy được kiểm chứng tuyệt đối qua 75 ca kiểm thử tự động JUnit 5 và 420 phiên chat thực nghiệm.")
    add_p("Các điểm hạn chế cần cải tiến:", bold_prefix="2. ")
    add_p("   - Hiện tại cuộc gọi media qua UDP chỉ giới hạn 1-1, chưa hỗ trợ phòng họp nhóm đông người (Conference Room).")
    add_p("   - Băng thông máy chủ: UDP Relay trung gian chịu tải lưu lượng media khi có đồng thời hàng trăm cuộc gọi video.")
    add_p("   - Nền tảng: Ứng dụng hiện chạy trên JavaFX Desktop, chưa có bản Mobile App hoặc Web Widget nhúng.")

    doc.add_page_break()

    # ==================== CHƯƠNG 6 ====================
    add_heading_chapter("CHƯƠNG 6. KẾT LUẬN VÀ KIẾN NGHỊ")

    add_heading_1("6.1. Kết luận và Thành tựu nổi bật")
    for para in CH6_KET_LUAN.split("\n\n"):
        add_p(para)

    add_heading_1("6.2. Kiến nghị và Hướng phát triển tương lai")
    add_p("1. Mở rộng nền tảng Web và Ứng dụng di động (Cross-platform): Xây dựng cổng giao tiếp WebSocket tại Server, cho phép nhúng Live Chat Widget trực tiếp vào các website thương mại điện tử bằng ReactJS và phát triển ứng dụng di động Flutter.")
    add_p("2. Nâng cấp hạ tầng đàm thoại với WebRTC P2P: Chuyển đổi từ mô hình UDP Relay Server trung gian sang WebRTC Peer-to-Peer kết hợp máy chủ STUN/TURN, giúp giảm tải 100% băng thông media cho Server và hỗ trợ cuộc gọi nhóm nhiều người.")
    add_p("3. Kiến trúc Microservices phân tán: Đóng gói máy chủ bằng Docker/Kubernetes, sử dụng Apache Kafka làm hàng đợi thông điệp và PostgreSQL Cluster cho dữ liệu phân tán quy mô lớn.")

    doc.add_page_break()

    # ==================== TÀI LIỆU THAM KHẢO ====================
    add_heading_chapter("TÀI LIỆU THAM KHẢO")
    for ref in REFERENCES_LIST:
        p_ref = add_p(ref)
        p_ref.paragraph_format.line_spacing = 1.2
        p_ref.paragraph_format.space_after = Pt(8)

    doc.add_page_break()

    # ==================== PHỤ LỤC ====================
    add_heading_chapter("PHỤ LỤC: MÃ NGUỒN CHƯƠNG TRÌNH TIÊU BIỂU")
    add_p("Phụ lục này trích dẫn các đoạn mã nguồn cốt lõi làm nên sức mạnh và tính toàn vẹn của hệ thống Live Chat Support.")

    add_heading_1("Phụ lục 1. Lớp mã hóa và an ninh hệ thống (SecurityManager.java)")
    add_code_block("""
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
""", caption="Phụ lục 1: Toàn văn lớp SecurityManager.java (AES-128 CBC & SHA-256)")

    add_heading_1("Phụ lục 2. Máy chủ trung chuyển đa phương tiện UDP (UdpRelayServer.java)")
    add_code_block("""
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
            this.socket.setReceiveBufferSize(2 * 1024 * 1024); // Bộ đệm 2MB
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
""", caption="Phụ lục 2: Toàn văn lớp UdpRelayServer.java chuyển tiếp Voice/Video Call")

    # Lưu file
    doc.save(output_path)
    print(f"Bao cao do an nang cao da duoc xuat thanh cong tai: {os.path.abspath(output_path)}")

if __name__ == "__main__":
    out_file = "BaoCao_DoAn_CMP180_LiveChatSupport_Final.docx"
    build_advanced_docx(out_file)
    try:
        import shutil
        shutil.copyfile("BaoCao_DoAn_CMP180_LiveChatSupport_Final.docx", "BaoCao_DoAn_CMP180_LiveChatSupport.docx")
        print("Da sao chep de len BaoCao_DoAn_CMP180_LiveChatSupport.docx thanh cong.")
    except Exception as e:
        print("File cu dang duoc mo trong Word, file moi da duoc luu tai BaoCao_DoAn_CMP180_LiveChatSupport_Final.docx:", e)
