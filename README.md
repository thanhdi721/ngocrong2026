# ngocrongteam

Dự án game Ngọc Rồng (Ngoc Rong Online / Teamobi).

## Cấu trúc thư mục

| Thư mục / File | Mô tả |
|---|---|
| `SRC/` | Mã nguồn game (client / server) |
| `assets-moi/` | Tài nguyên mới (icon, sprite, script tạo ảnh) |
| `docs/` | Tài liệu dự án (triển khai, hướng dẫn, prompt) |
| `LÂU CỒ MOD/` | Bản build game MOD (Unity) |
| `database team2026.sql` | Cơ sở dữ liệu MySQL |
| `item.xlsx` | Bảng item trong game |
| `Lệnh admin.docx` | Tài liệu lệnh admin |

## Icon (assets-moi)

Đã chuẩn hoá theo `docs/4-trien-khai/23-huong-dan-tao-anh-va-prompt.md`:

- **x1** = 24×24, **x2** = 48×48, **x3** = 72×72, **x4** = 96×96
- Nền trong suốt (alpha), scale nearest-neighbor
- 9 icon: `manh-ky-uc-1..7`, `loi-hu-khong`, `vo-loi-rong`

Chạy lại:

```bash
cd assets-moi/script
python3 from_ref_ai.py
```

## Tác giả

ThanhDi — thanhdi721@gmail.com
