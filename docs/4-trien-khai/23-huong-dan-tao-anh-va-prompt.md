# 23 — Hướng dẫn tạo ảnh cho game + prompt chuẩn

> Trả lời câu: "cần tạo ảnh boss / vật phẩm / NPC như thế nào".
> Mọi quy cách dưới đây **đo trực tiếp từ file ảnh thật** trong `SRC/data/`, không phải phỏng đoán.

## Mục lục

1. [Game có mấy loại ảnh](#1-game-có-mấy-loại-ảnh)
2. [Cái nào phải vẽ mới, cái nào không](#2-cái-nào-phải-vẽ-mới-cái-nào-không)
3. [Quy cách kỹ thuật bắt buộc](#3-quy-cách-kỹ-thuật-bắt-buộc)
4. [Phong cách mỹ thuật của game](#4-phong-cách-mỹ-thuật-của-game)
5. [Prompt chuẩn — copy ra dùng](#5-prompt-chuẩn--copy-ra-dùng)
6. [Quy trình đưa ảnh vào game](#6-quy-trình-đưa-ảnh-vào-game)
7. [Danh sách ảnh cần tạo cho dự án này](#7-danh-sách-ảnh-cần-tạo-cho-dự-án-này)
8. [Cạm bẫy hay gặp](#8-cạm-bẫy-hay-gặp)

---

## 1. Game có mấy loại ảnh

| Loại | Nằm ở | Định dạng | Ghi chú |
|---|---|---|---|
| **Icon vật phẩm** | `SRC/data/icon/x1..x4/<icon_id>.png` | PNG, nền trong suốt | 15.121 file ở x1, 16.478 ở x4. Đây là loại **dễ thêm nhất** |
| **Bộ phận nhân vật** (đầu, thân, chân) | cũng trong `icon/x1..x4/` | PNG | Dùng cho NPC và boss. Id ghi ở `npc_template.head/body/leg` và `BossData.outfit`. Cách ghép khung hình nằm ở bảng `part` trong DB |
| **Sprite quái** | `SRC/data/mob/x1..x4/<mobId>` | **File nhị phân, KHÔNG phải PNG** | Định dạng riêng của game. **Không thể tạo bằng ảnh thường** → quái mới bắt buộc dùng lại hình quái cũ |
| **Ảnh theo tên** | `SRC/data/img_by_name/x1..x4/` | PNG | Hiệu ứng kỹ năng, ảnh giao diện. Ví dụ `Skills_24_1_0.png` |
| **Hiệu ứng** | `SRC/data/effect/`, `effdata/` | Có bảng dữ liệu riêng | Dùng cho danh hiệu, hào quang. Phức tạp nhất |
| **Nền khung vật phẩm** | `SRC/data/item_bg_temp/x1..x4/` | PNG | Khung viền theo độ hiếm |

**Điều quan trọng nhất cần nhớ:** mỗi ảnh phải có **đủ 4 phiên bản** x1, x2, x3, x4 — tương ứng 1×, 2×, 3×, 4× kích thước gốc. Client chọn bộ nào tùy độ phân giải màn hình.

Ví dụ thật đã đo:

| icon_id | Vật phẩm | x1 | x4 |
|---|---|---|---|
| 419 | Ngọc Rồng 1 sao | 16×16 | 64×64 |
| 241 | Đậu thần | 12×12 | 48×48 |
| 12846 | Rương ngọc rồng | 21×21 | 84×84 |
| 5428 | Bản đồ kho báu | 20×18 | 80×72 |

## 2. Cái nào phải vẽ mới, cái nào không

| Thứ cần | Có phải vẽ mới không | Lý do |
|---|---|---|
| **Boss Heart** (phản diện chính) | **Không** | Dùng lại bộ phận của NPC 108 đã có sẵn trong game |
| **6 boss bản nhiệm vụ** (Cooler, Black Goku, Baby, Cumber, Xên, Mabư bản dễ) | **Không** | Dùng lại y hệt tạo hình boss gốc, chỉ khác chỉ số |
| **Boss Kẻ Thu Gom, Jaco mất ký ức** | **Không** | Mượn tạo hình NPC 26 và NPC 63 |
| **Quái mới** | **Không làm được** | Sprite quái là file nhị phân định dạng riêng |
| **NPC mới** | **Không cần** | Mọi nhân vật trong cốt truyện đều là NPC đã có |
| **Vật phẩm nhiệm vụ** | **Tùy bạn** | Mượn icon có sẵn thì 0 ảnh. Muốn nhìn riêng thì vẽ mới |
| **Danh hiệu kết thúc** | Nên mượn | Cần thêm file hiệu ứng, phức tạp và dễ lỗi |

**Khuyến nghị:** chỉ vẽ mới **9 icon** cho những thứ là biểu tượng của cốt truyện (7 Mảnh Ký Ức, Lõi Hư Không, Vỏ Lõi rỗng). Còn lại mượn icon sẵn có. Cách này vừa đẹp vừa ít rủi ro.

## 3. Quy cách kỹ thuật bắt buộc

| Yêu cầu | Giá trị |
|---|---|
| Định dạng | PNG, nền **trong suốt hoàn toàn** (không nền trắng, không ô caro) |
| Kích thước gốc (x1) | Vuông hoặc gần vuông, **16×16 tới 24×24** cho icon vật phẩm |
| 4 phiên bản | x1 = gốc · x2 = 2× · x3 = 3× · x4 = 4×, **đúng bội số, không lẻ** |
| Cách phóng to | **Nearest-neighbor** (giữ pixel sắc nét), không dùng làm mờ |
| Tên file | `<icon_id>.png` — đúng số icon_id, không có tiền tố |
| Nơi đặt | 4 thư mục `SRC/data/icon/x1`, `x2`, `x3`, `x4` |
| Nội dung | Vật thể chiếm gần trọn khung, chừa 1 pixel mép; không chữ, không số, không khung viền ngoài |
| Bóng đổ | Không đổ bóng ra ngoài vật thể |

## 4. Phong cách mỹ thuật của game

Quan sát từ icon thật:

- **Pixel art màu tươi**, độ bão hòa cao, kiểu game 16-bit.
- **Viền tối 1 pixel** quanh vật thể để nổi trên nền bản đồ.
- **Ánh sáng từ trên bên trái**: điểm sáng ở góc trên trái, bóng đậm dần xuống dưới phải.
- **Ít chi tiết, khối rõ ràng** — vì thật ra người chơi nhìn ở cỡ 16×16.
- **Không phối màu phức tạp**, mỗi vật thể khoảng 3–5 màu cộng với sắc độ.
- Phong cách chung là **anime Dragon Ball**: tròn trịa, sáng sủa, dễ đọc.

## 5. Prompt chuẩn — copy ra dùng

### 5.1 Mẫu chung (điền vào chỗ trống)

```
Pixel art game icon, 16-bit RPG style, single object centered.
SUBJECT: <mô tả vật thể>
COLORS: <2-3 màu chủ đạo>
STYLE: Dragon Ball anime game item icon, clean chunky pixels, 1px dark outline,
       light source from top-left, soft shadow bottom-right, 4-6 colors total,
       high saturation, readable at very small size.
BACKGROUND: fully transparent, no background, no shadow on ground.
COMPOSITION: object fills the frame, 1px margin, no text, no numbers, no border frame,
       front view, slight 3/4 angle.
OUTPUT: square PNG with alpha, 64x64.
NEGATIVE: photorealistic, 3D render, blurry, gradient mesh, text, watermark,
       drop shadow outside object, white background, multiple objects.
```

> Tạo ở **64×64** rồi thu nhỏ về 16×16 làm bản x1, sau đó phóng lại bằng nearest-neighbor để có x2, x3, x4. Cách này cho hình sắc nét hơn là vẽ thẳng ở 16×16.

### 5.2 Mảnh Ký Ức (7 viên — vật phẩm cốt truyện)

```
Pixel art game icon, 16-bit RPG style, single object centered.
SUBJECT: a glowing crystal shard of memory, jagged broken fragment,
         translucent amber-gold core with faint swirling light inside,
         hairline cracks on the surface, <N> small star marks etched on it
COLORS: amber gold, warm orange, pale cream highlight
STYLE: Dragon Ball anime game item icon, clean chunky pixels, 1px dark brown outline,
       light source from top-left, 5 colors total, high saturation, glowing feel
BACKGROUND: fully transparent
COMPOSITION: shard fills the frame, 1px margin, no text, no border frame
OUTPUT: square PNG with alpha, 64x64
NEGATIVE: photorealistic, 3D, blurry, text, white background, multiple shards
```

Thay `<N>` bằng 1 tới 7 cho 7 viên, và đổi màu dần cho dễ phân biệt: viên 1 vàng nhạt → viên 7 cam đỏ rực.

### 5.3 Lõi Hư Không (vật phẩm kết truyện)

```
Pixel art game icon, 16-bit RPG style, single object centered.
SUBJECT: a dark orb of void energy, black sphere with a deep purple event-horizon ring,
         thin violet cracks leaking light, tiny white sparks orbiting it
COLORS: near-black, deep violet, magenta glow, white spark
STYLE: Dragon Ball anime game item icon, clean chunky pixels, 1px outline,
       inner glow instead of top-left highlight, ominous, 5 colors total
BACKGROUND: fully transparent
COMPOSITION: orb fills the frame, 1px margin, no text, no border frame
OUTPUT: square PNG with alpha, 64x64
NEGATIVE: photorealistic, 3D render, blurry, text, white background, lens flare
```

### 5.4 Nguyên liệu / mảnh vỡ (đá, mảnh giáp, lõi máy)

```
Pixel art game icon, 16-bit RPG style, single object centered.
SUBJECT: <ví dụ: a broken piece of robot armor plating with exposed wires>
COLORS: <ví dụ: steel gray, cyan energy, dark blue shadow>
STYLE: Dragon Ball anime game material icon, clean chunky pixels, 1px dark outline,
       light from top-left, matte metal shading, 4-5 colors
BACKGROUND: fully transparent
COMPOSITION: fills the frame, 1px margin, no text, no border frame
OUTPUT: square PNG with alpha, 64x64
NEGATIVE: photorealistic, 3D, blurry, text, white background, multiple objects
```

### 5.5 Chân dung NPC / avatar hội thoại

Dùng khi muốn ảnh đại diện lúc NPC nói chuyện (cột `avatar` trong `npc_template`).

```
Pixel art character portrait, 16-bit JRPG dialogue avatar, head and shoulders only.
SUBJECT: <mô tả nhân vật: giới tính, kiểu tóc, trang phục, biểu cảm>
STYLE: Dragon Ball anime art style, bold clean lineart, cel shading with 2 tones,
       bright saturated palette, friendly readable face, facing slightly to the right
BACKGROUND: fully transparent
COMPOSITION: head fills most of the frame, centered, no text, no frame, no body below chest
OUTPUT: square PNG with alpha, 128x128
NEGATIVE: photorealistic, 3D render, realistic proportions, text, watermark,
       white background, full body, multiple characters
```

### 5.6 Bộ phận nhân vật cho boss / NPC mới (đầu, thân, chân)

> Khó nhất, chỉ làm khi thật sự cần một boss có tạo hình riêng. Phải vẽ **3 mảnh rời**, mỗi mảnh một file icon riêng, rồi khai báo cách ghép trong bảng `part`.

```
Pixel art sprite part for a 2D side-view game character, single body part only.
SUBJECT: <ví dụ: the HEAD of a pale humanoid villain, white hair, cold blue eyes,
         black high collar>
VIEW: side-scrolling game sprite, character faces RIGHT
STYLE: Dragon Ball 16-bit game sprite, 1px dark outline, cel shading, 4-6 colors,
       light from top-left, crisp pixels
BACKGROUND: fully transparent
COMPOSITION: only the <head / torso / legs>, nothing else in frame, no text
OUTPUT: PNG with alpha, about <32x32 cho đầu / 32x24 cho thân / 32x24 cho chân>
NEGATIVE: full body, multiple parts in one image, photorealistic, 3D, blurry, text,
       white background, front view
```

Ba mảnh phải **cùng tỉ lệ và cùng nguồn sáng**, nếu không ghép lại sẽ lệch.

## 6. Quy trình đưa ảnh vào game

1. **Chọn icon_id** chưa dùng. Kiểm tra bằng cách xem file `SRC/data/icon/x1/<id>.png` đã tồn tại chưa. **Lưu ý: icon_id khác id vật phẩm.**
2. Tạo ảnh ở 64×64 theo prompt ở mục 5.
3. Xóa nền, cắt sát vật thể, thu về kích thước x1 (16×16 hoặc 24×24).
4. Phóng nearest-neighbor ra x2, x3, x4 — đúng bội số.
5. Chép 4 file vào 4 thư mục `SRC/data/icon/x1..x4`, tên `<icon_id>.png`.
6. Thêm dòng vật phẩm vào `item_template` với `icon_id` vừa chọn (xem [21b](../3-kiem-tra-can-bang/21b-item-moi-dac-ta.md)).
7. **Tăng số phiên bản bảng item** để client tải lại dữ liệu, nếu không client cũ sẽ hiện tên rỗng hoặc icon trắng.
8. Test: vào game, tự phát vật phẩm cho mình, xem icon trong hành trang, trong rương, khi rơi trên đất và khi ký gửi.

**Checklist nghiệm thu một ảnh:**

- [ ] Có đủ 4 file ở 4 thư mục
- [ ] x2/x3/x4 đúng bằng 2/3/4 lần x1
- [ ] Nền trong suốt, không viền trắng quanh mép
- [ ] Nhìn ở cỡ 16×16 vẫn phân biệt được với vật phẩm khác
- [ ] Không trùng icon_id với vật phẩm đang dùng

## 7. Danh sách ảnh cần tạo cho dự án này

Theo quyết định của bạn ("dùng đồ có sẵn, chỉ tạo mới khi cần"):

| Ưu tiên | Ảnh | Số lượng | Ghi chú |
|---|---|---|---|
| **Nên vẽ mới** | Mảnh Ký Ức #1 → #7 | 7 | Biểu tượng xuyên suốt cốt truyện, người chơi nhìn suốt 48 nhiệm vụ |
| **Nên vẽ mới** | Lõi Hư Không | 1 | Vật phẩm quyết định đoạn kết |
| **Nên vẽ mới** | Vỏ Lõi rỗng | 1 | Kỷ vật nhánh kết A |
| Mượn icon sẵn có | 22 vật phẩm nhiệm vụ còn lại | 0 | Danh sách icon mượn đã ghi sẵn trong [21b](../3-kiem-tra-can-bang/21b-item-moi-dac-ta.md) |
| Không cần | Boss, NPC, quái | 0 | Dùng lại tạo hình có sẵn |

**Tổng: 9 icon, tức 36 file ảnh** (9 × 4 kích thước).

## 8. Cạm bẫy hay gặp

| Cạm bẫy | Hậu quả |
|---|---|
| Thiếu một trong 4 kích thước | Người chơi ở độ phân giải đó thấy ô trống |
| Phóng to bằng thuật toán làm mờ | Icon nhòe, lạc lõng giữa đám icon pixel sắc nét |
| Để nền trắng thay vì trong suốt | Ô vuông trắng xấu xí trong hành trang |
| Ảnh quá nhiều chi tiết | Ở cỡ 16×16 thành một đốm màu, không nhận ra là gì |
| Quên tăng phiên bản dữ liệu | Client cũ không thấy vật phẩm mới, tên rỗng hoặc văng |
| Nhầm icon_id với id vật phẩm | Vật phẩm hiện nhầm hình của món khác |
| Đè lên icon_id đang dùng | Một vật phẩm cũ bỗng đổi hình |
| Định vẽ quái mới | Không dùng được — sprite quái là định dạng nhị phân riêng |
