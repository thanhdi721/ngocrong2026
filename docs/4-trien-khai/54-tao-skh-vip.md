# 54 — Tạo SKH VIP ở Bà Hạt Mít (2026-09-24)

Chỉ sửa code, không cần SQL. Nút mới nằm ở NPC **Bà Hạt Mít, đảo Kame (map 5)**:
"Chức năng pha lê · Chuyển hóa Trang bị · Võ đài Sinh tử · Phân rã Trang bị Kích hoạt ·
Tái tạo Capsule Kích hoạt · **Tạo SKH VIP**".

## Luật

* Đặt vào **3 món đồ Thần** (id 555–567). Món khác bị từ chối ngay khi đặt.
* **Món đầu tiên quyết định** loại và hệ của thứ nhận được:

| Món đầu tiên | Nhận được |
|---|---|
| Găng Thần Namếc | Găng kích hoạt hệ **Namếc** |
| Áo Thần Xayda | Áo kích hoạt hệ **Xayda** |
| Giầy Thần Linh | Giày kích hoạt hệ **Trái Đất** |
| Nhẫn Thần Linh (ô rađa) | Rađa kích hoạt, hệ lấy **theo người chơi** vì rađa không chia hệ |

* Món nhận được là **ngẫu nhiên trong cả dải**, từ món thường nhất tới **cấp Thần**.
  Ví dụ đặt Găng Thần Namếc + 2 Giầy Thần → ra một găng kích hoạt hệ Namếc, có thể là Găng vải đen
  mà cũng có thể là Găng Thần Namếc.
* Đồ nhận được là **đồ kích hoạt thật sự**: tạo bằng đúng hàm của Capsule kích hoạt
  (`ItemService.createItemSKH`), nên có option bộ kích hoạt theo hệ và khoá giao dịch như thường.
* Chi phí mỗi lần: **500 ngọc + 1 tỷ vàng**. Tỉ lệ thành công **100%**.
* Cần ít nhất 1 ô trống trong hành trang.

## Dải đồ nhận được

Dùng đúng dải của Capsule kích hoạt (`ItemService.OpenSKH`), thêm món Thần vào cuối mỗi dải:

| Hệ | Áo | Quần | Găng | Giày | Rađa |
|---|---|---|---|---|---|
| Trái Đất | 0, 3, 33, 34, 136–139, 230–233, **555** | 6, 9, 35, 36, 140–143, 242–245, **556** | 21, 24, 37, 38, 144–147, 254, 256, 257, **562** | 27, 30, 39, 40, 148–151, 266–269, **563** | 12, 57–59, 184–187, 278–281, **561** |
| Namếc | 1, 4, 41, 42, 152–155, 235–237, **557** | 7, 10, 43, 44, 156–159, 246–249, **558** | 22, 25, 45, 46, 160–163, 259–261, **564** | 28, 31, 47, 48, 164–167, 270–273, **565** | như trên |
| Xayda | 2, 5, 49, 50, 168–171, 238–241, **559** | 8, 11, 51, 52, 172–174, 250–253, **560** | 23, 26, 53, 54, 176–179, 262–265, **566** | 29, 32, 55, 56, 180–183, 274–277, **567** | như trên |

## Chỗ chỉnh

Đầu `SRC/src/nro/models/combine/TaoSKHVip.java`: `GOLD_TAO` (1 tỷ), `GEM_TAO` (500),
`SO_MON_CAN` (3), `RATIO_TAO` (100%), và các mảng `KHO_DO` nếu muốn đổi dải đồ ra.

## Chưa kiểm tra được

Chưa chạy thử trong game: cần xem nút hiện đúng chỗ, đặt đồ Thần vào có bị chặn nhầm không,
và món nhận được có đúng loại / hệ như bảng trên.
