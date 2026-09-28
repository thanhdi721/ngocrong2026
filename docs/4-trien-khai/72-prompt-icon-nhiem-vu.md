# 72 — Prompt tạo 23 icon vật phẩm nhiệm vụ

> Dùng để đưa cho AI tạo ảnh. Mỗi mục dưới đây là **một prompt tự chứa, dán thẳng
> vào là chạy**, không phải ghép với mục nào khác.
>
> 23 vật phẩm nhiệm vụ id 2009–2031 hiện đang **mượn icon của món khác**, có 3 cặp
> trùng nhau nên nhìn y hệt trong hành trang. File này thay hết bằng icon riêng.
>
> 9 món còn lại của tuyến (2000–2008: Lõi Hư Không, Vỏ Lõi rỗng, 7 Mảnh Ký Ức) **đã có
> icon riêng 20000–20008**, không nằm trong danh sách này.

---

## 1. Cần giao gì

**Chỉ cần 23 file PNG ở kích thước x4.** Bản x1 / x2 / x3 tôi tự thu nhỏ khi nhận file.

| Mục | Giá trị |
|---|---|
| Số file | 23 |
| Kích thước | **88 × 88 pixel** (đây là bản x4; bản x1 sẽ là 22×22) |
| Định dạng | PNG, kênh alpha, **nền trong suốt hoàn toàn** |
| Tên file | đúng bằng số ở cột "Tên file" trong bảng mục 3, ví dụ `32705.png` |
| Nền | không nền trắng, không ô caro, không bóng đổ xuống đất |
| Nội dung | một vật thể duy nhất, nằm giữa, chiếm gần trọn khung, chừa 1–2 pixel mép |
| Cấm | chữ, số, khung viền ngoài, nhiều vật thể trong một ảnh |

**Nếu AI không xuất được đúng 88×88** thì cứ để nó xuất vuông lớn hơn (512×512 hoặc
1024×1024) nền trong suốt — tôi thu nhỏ về 88×88 rồi sinh tiếp x1/x2/x3. Miễn là **ảnh
vuông** và **nền thật sự trong suốt**.

Vì sao 88×88: 7 Mảnh Ký Ức đã vẽ trước đó là 22×22 ở x1, tức 88×88 ở x4. Giữ cùng cỡ
để cả bộ nhìn cùng một nhà.

---

## 2. Phong cách chung

Mọi prompt bên dưới đã nhúng sẵn khối này, chép nguyên là được. Ghi lại đây để nếu
cần sửa thì sửa đồng loạt:

```
STYLE: Dragon Ball anime game item icon, 16-bit pixel art, clean chunky pixels,
       1px dark outline around the object, light source from top-left,
       4-6 colors total, high saturation, still readable when shrunk to 22x22
BACKGROUND: fully transparent, no ground shadow
COMPOSITION: single object centered, fills the frame, 1-2px margin,
       no text, no numbers, no border frame
OUTPUT: square PNG with alpha, 88x88
NEGATIVE: photorealistic, 3D render, blurry, gradient mesh, text, watermark,
       drop shadow outside object, white background, multiple objects
```

Điểm quan trọng nhất: **người chơi nhìn icon này ở cỡ 22×22**. Chi tiết nhỏ sẽ biến
mất hết. Vẽ khối to, ít màu, tương phản mạnh.

---

## 3. Bảng tra

| # | Tên file | Vật phẩm | Tên | Đang mượn icon của |
|---|---|---|---|---|
| 1 | `32705.png` | 2009 | Mảnh Vỡ Hư Không | 1421 *Mảnh đá vụn* |
| 2 | `32706.png` | 2010 | Kỷ Vật Của Ông | 9067 *Nhẫn thời không sai lệch* |
| 3 | `32707.png` | 2011 | Máy Dò Ký Ức | 1089 *Rada cấp 1* |
| 4 | `32708.png` | 2012 | Hộp Ký Ức Bị Đánh Cắp | 7222 *Hộp Capsule* |
| 5 | `32709.png` | 2013 | Hạt Giống Hy Vọng | 241 *Đậu thần cấp 1* |
| 6 | `32710.png` | 2014 | Vỏ đạn khắc dấu | 1421 *Mảnh đá vụn* |
| 7 | `32711.png` | 2015 | Búa rèn cũ | 1417 *Đá Titan* |
| 8 | `32712.png` | 2016 | Thẻ tiền thưởng Granola | 5428 *Bản đồ kho báu* |
| 9 | `32713.png` | 2017 | Biên bản truy nã Ngân Hà | 5207 *Bí kiếp* |
| 10 | `32714.png` | 2018 | Máy đo ký ức | 6467 *Đá ngũ sắc* |
| 11 | `32715.png` | 2019 | Lõi năng lượng Android | 8620 *Đá xanh lam* |
| 12 | `32716.png` | 2020 | Mẫu kim loại có ký ức | 2288 *Hóa thạch Ngọc Rồng* |
| 13 | `32717.png` | 2021 | Mảnh giáp khắc tên | 10197 *Mảnh áo* |
| 14 | `32718.png` | 2022 | Thẻ từ phòng thí nghiệm | 9406 *Đá bảo vệ* |
| 15 | `32719.png` | 2023 | Bản thiết kế bản sao | 12846 *Rương ngọc rồng* |
| 16 | `32720.png` | 2024 | Lõi Ký Ức chưa hoàn chỉnh | 9650 *Ngọc rồng Siêu Cấp* |
| 17 | `32721.png` | 2025 | Mảnh Ký Ức Vỡ | 6467 *Đá ngũ sắc* |
| 18 | `32722.png` | 2026 | Mảnh Ký Ức Đóng Băng | 8620 *Đá xanh lam* |
| 19 | `32723.png` | 2027 | Mảnh Bùa Babiđây | 7743 *Hồng ngọc* |
| 20 | `32724.png` | 2028 | Lõi Phép Babiđây | 5829 *Bình chứa Commeson* |
| 21 | `32725.png` | 2029 | Ống nghiệm Myuu | 6849 *Siêu thần thủy* |
| 22 | `32726.png` | 2030 | **Danh hiệu** Người Trả Ký Ức | 11614 *Cao thủ siêu hạng* |
| 23 | `32727.png` | 2031 | **Danh hiệu** Kẻ Giữ Hư Không | 11617 *Trùm săn Boss* |

### Ba cặp bắt buộc phải nhìn khác nhau

Hiện ba cặp này dùng chung icon nên người chơi không phân biệt nổi. Vẽ mới phải tách bạch:

- `32705` Mảnh Vỡ Hư Không **tím đen** ↔ `32710` Vỏ đạn **đồng vàng**
- `32714` Máy đo ký ức **kính một mắt đeo tai** ↔ `32721` Mảnh Ký Ức Vỡ **pha lê hổ phách**
- `32715` Lõi năng lượng **ống trụ đỏ** ↔ `32722` Mảnh Ký Ức Đóng Băng **khối băng xanh**

Thêm một cặp nữa dễ lẫn vì cùng là thiết bị: `32707` Máy Dò Ký Ức là **máy cầm tay hình
hộp có màn hình**, còn `32714` Máy đo ký ức là **kính scouter một mắt đeo tai**.

---

## 4. Hai mươi ba prompt

### 1. `32705.png` — 2009 Mảnh Vỡ Hư Không

Mảnh pha lê hư không vỡ, tím đen, lõi có vệt sáng tím. Nhặt nhiều cái nên sẽ có số đè lên góc dưới phải — chừa chỗ.

```
SUBJECT: a jagged shard of broken void crystal, sharp angular fragment,
         dark translucent body with a thin violet light seam glowing inside,
         hairline cracks across the surface
COLORS: near-black purple, deep violet, magenta glow, pale lilac highlight
STYLE: Dragon Ball anime game item icon, 16-bit pixel art, clean chunky pixels,
       1px dark outline around the object, light source from top-left,
       4-6 colors total, high saturation, still readable when shrunk to 22x22
BACKGROUND: fully transparent, no ground shadow
COMPOSITION: single object centered, fills the frame, 1-2px margin,
       no text, no numbers, no border frame
OUTPUT: square PNG with alpha, 88x88
NEGATIVE: photorealistic, 3D render, blurry, gradient mesh, text, watermark,
       drop shadow outside object, white background, multiple objects
```

### 2. `32706.png` — 2010 Kỷ Vật Của Ông

Chiếc nhẫn cũ của ông, xỉn màu, mặt nhẫn khắc đã mờ.

```
SUBJECT: an old worn gold ring standing upright, tarnished dull band,
         flat oval signet face with a faded worn engraving, small scratches
COLORS: dull antique gold, brown tarnish, warm cream highlight
STYLE: Dragon Ball anime game item icon, 16-bit pixel art, clean chunky pixels,
       1px dark outline around the object, light source from top-left,
       4-6 colors total, high saturation, still readable when shrunk to 22x22
BACKGROUND: fully transparent, no ground shadow
COMPOSITION: single object centered, fills the frame, 1-2px margin,
       no text, no numbers, no border frame
OUTPUT: square PNG with alpha, 88x88
NEGATIVE: photorealistic, 3D render, blurry, gradient mesh, text, watermark,
       drop shadow outside object, white background, multiple objects, shiny new jewelry
```

### 3. `32707.png` — 2011 Máy Dò Ký Ức

Máy dò cầm tay hình hộp có màn hình. **Phải khác hẳn `32714`** (kính scouter đeo tai).

```
SUBJECT: a handheld scanner device, boxy casing held upright,
         a small rectangular screen glowing green, one short antenna on top,
         a row of tiny buttons below the screen
COLORS: olive green casing, bright green screen, dark gray trim, pale highlight
STYLE: Dragon Ball anime game item icon, 16-bit pixel art, clean chunky pixels,
       1px dark outline around the object, light source from top-left,
       4-6 colors total, high saturation, still readable when shrunk to 22x22
BACKGROUND: fully transparent, no ground shadow
COMPOSITION: single object centered, fills the frame, 1-2px margin,
       no text, no numbers, no border frame
OUTPUT: square PNG with alpha, 88x88
NEGATIVE: photorealistic, 3D render, blurry, gradient mesh, text, watermark,
       drop shadow outside object, white background, multiple objects,
       head-mounted visor, eyewear, scouter
```

### 4. `32708.png` — 2012 Hộp Ký Ức Bị Đánh Cắp

Hộp sắt có khoá, nắp hé, ánh sáng rò ra khe.

```
SUBJECT: a small metal strongbox with a latch, lid slightly ajar,
         pale blue light leaking out through the gap under the lid
COLORS: gunmetal gray, brass latch, cyan light leak, dark shadow
STYLE: Dragon Ball anime game item icon, 16-bit pixel art, clean chunky pixels,
       1px dark outline around the object, light source from top-left,
       4-6 colors total, high saturation, still readable when shrunk to 22x22
BACKGROUND: fully transparent, no ground shadow
COMPOSITION: single object centered, fills the frame, 1-2px margin,
       no text, no numbers, no border frame
OUTPUT: square PNG with alpha, 88x88
NEGATIVE: photorealistic, 3D render, blurry, gradient mesh, text, watermark,
       drop shadow outside object, white background, multiple objects,
       round capsule, pill shape
```

### 5. `32709.png` — 2013 Hạt Giống Hy Vọng

Hạt mầm đã nhú lá, có quầng sáng. **Không được giống hạt đậu thần trơn** — mầm và quầng sáng chính là điểm khác.

```
SUBJECT: a single round seed with a tiny green sprout growing out of the top,
         two small leaves, a soft warm glow halo around the seed
COLORS: jade green seed, light mint leaves, warm cream glow, brown shadow
STYLE: Dragon Ball anime game item icon, 16-bit pixel art, clean chunky pixels,
       1px dark outline around the object, light source from top-left,
       4-6 colors total, high saturation, still readable when shrunk to 22x22
BACKGROUND: fully transparent, no ground shadow
COMPOSITION: single object centered, fills the frame, 1-2px margin,
       no text, no numbers, no border frame
OUTPUT: square PNG with alpha, 88x88
NEGATIVE: photorealistic, 3D render, blurry, gradient mesh, text, watermark,
       drop shadow outside object, white background, multiple objects,
       plain bean without sprout, soil, flower pot
```

### 6. `32710.png` — 2014 Vỏ đạn khắc dấu

Vỏ đạn đồng rỗng, đáy khắc ký hiệu. Nhặt nhiều cái nên sẽ có số đè lên góc.

```
SUBJECT: an empty brass bullet casing standing upright, slightly dented body,
         a small engraved symbol stamped on the base rim, hollow open top
COLORS: brass gold, copper shadow, dark gray dent, pale metal highlight
STYLE: Dragon Ball anime game item icon, 16-bit pixel art, clean chunky pixels,
       1px dark outline around the object, light source from top-left,
       4-6 colors total, high saturation, still readable when shrunk to 22x22
BACKGROUND: fully transparent, no ground shadow
COMPOSITION: single object centered, fills the frame, 1-2px margin,
       no text, no numbers, no border frame
OUTPUT: square PNG with alpha, 88x88
NEGATIVE: photorealistic, 3D render, blurry, gradient mesh, text, watermark,
       drop shadow outside object, white background, multiple objects,
       whole bullet with tip, gun, stone shard
```

### 7. `32711.png` — 2015 Búa rèn cũ

Búa rèn cán gỗ, đầu sắt gỉ sứt mẻ. Dùng một lần rồi hỏng nên phải trông cũ nát.

```
SUBJECT: an old blacksmith hammer seen at a 3/4 diagonal angle,
         worn wooden handle with grip marks, rusty chipped iron head
COLORS: rust brown iron, warm wood brown, gray steel highlight, dark shadow
STYLE: Dragon Ball anime game item icon, 16-bit pixel art, clean chunky pixels,
       1px dark outline around the object, light source from top-left,
       4-6 colors total, high saturation, still readable when shrunk to 22x22
BACKGROUND: fully transparent, no ground shadow
COMPOSITION: single object centered, fills the frame, 1-2px margin,
       no text, no numbers, no border frame
OUTPUT: square PNG with alpha, 88x88
NEGATIVE: photorealistic, 3D render, blurry, gradient mesh, text, watermark,
       drop shadow outside object, white background, multiple objects,
       shiny new weapon, war hammer, anvil
```

### 8. `32712.png` — 2016 Thẻ tiền thưởng Granola

Thẻ kim loại của thợ săn tiền thưởng. Nhặt đủ 3 cái.

```
SUBJECT: a rectangular metal bounty tag, a blurred etched wanted portrait
         on the left half, a small currency stamp on the right half,
         one small hole punched in a corner
COLORS: steel silver, dark blue engraving, gold stamp, gray shadow
STYLE: Dragon Ball anime game item icon, 16-bit pixel art, clean chunky pixels,
       1px dark outline around the object, light source from top-left,
       4-6 colors total, high saturation, still readable when shrunk to 22x22
BACKGROUND: fully transparent, no ground shadow
COMPOSITION: single object centered, fills the frame, 1-2px margin,
       no text, no numbers, no border frame
OUTPUT: square PNG with alpha, 88x88
NEGATIVE: photorealistic, 3D render, blurry, gradient mesh, text, watermark,
       drop shadow outside object, white background, multiple objects,
       readable letters, treasure map, paper scroll
```

### 9. `32713.png` — 2017 Biên bản truy nã Ngân Hà

Giấy truy nã của cảnh sát vũ trụ, có dấu mộc đỏ. Nhặt đủ 3 cái.

```
SUBJECT: a rolled parchment warrant partly unrolled, a red wax seal pressed
         at the bottom edge, faint illegible printed lines on the paper,
         slightly curled corners
COLORS: aged cream paper, red wax seal, dark brown ink, tan shadow
STYLE: Dragon Ball anime game item icon, 16-bit pixel art, clean chunky pixels,
       1px dark outline around the object, light source from top-left,
       4-6 colors total, high saturation, still readable when shrunk to 22x22
BACKGROUND: fully transparent, no ground shadow
COMPOSITION: single object centered, fills the frame, 1-2px margin,
       no text, no numbers, no border frame
OUTPUT: square PNG with alpha, 88x88
NEGATIVE: photorealistic, 3D render, blurry, gradient mesh, text, watermark,
       drop shadow outside object, white background, multiple objects,
       readable letters, spell book, closed book
```

### 10. `32714.png` — 2018 Máy đo ký ức

Kính scouter một mắt đeo tai. **Phải khác hẳn `32707`** (máy cầm tay hình hộp).

```
SUBJECT: a one-eye scouter headset lying at a 3/4 angle, curved ear hook,
         a single translucent lens in front, a small blinking indicator light
COLORS: dark green translucent lens, off-white casing, silver frame, red dot
STYLE: Dragon Ball anime game item icon, 16-bit pixel art, clean chunky pixels,
       1px dark outline around the object, light source from top-left,
       4-6 colors total, high saturation, still readable when shrunk to 22x22
BACKGROUND: fully transparent, no ground shadow
COMPOSITION: single object centered, fills the frame, 1-2px margin,
       no text, no numbers, no border frame
OUTPUT: square PNG with alpha, 88x88
NEGATIVE: photorealistic, 3D render, blurry, gradient mesh, text, watermark,
       drop shadow outside object, white background, multiple objects,
       handheld box device, gemstone, head wearing it
```

### 11. `32715.png` — 2019 Lõi năng lượng Android

Trụ năng lượng moi từ người máy. Nhặt đủ 3 cái.

```
SUBJECT: a cylindrical power core standing upright, metal end caps top and bottom,
         a glowing red energy column visible through a glass window in the middle
COLORS: steel gray casing, crimson red glow, dark blue shadow, white highlight
STYLE: Dragon Ball anime game item icon, 16-bit pixel art, clean chunky pixels,
       1px dark outline around the object, light source from top-left,
       4-6 colors total, high saturation, still readable when shrunk to 22x22
BACKGROUND: fully transparent, no ground shadow
COMPOSITION: single object centered, fills the frame, 1-2px margin,
       no text, no numbers, no border frame
OUTPUT: square PNG with alpha, 88x88
NEGATIVE: photorealistic, 3D render, blurry, gradient mesh, text, watermark,
       drop shadow outside object, white background, multiple objects,
       raw gemstone, crystal, battery with plus minus signs
```

### 12. `32716.png` — 2020 Mẫu kim loại có ký ức

Mẩu kim loại méo, bề mặt gợn sóng âm thanh. Bấm "Dùng" để nghe lại ký ức.

```
SUBJECT: an irregular chunk of metal, bent and twisted, its surface covered
         in fine concentric ripples like frozen sound waves spreading from one point
COLORS: cold silver, blue-gray shadow, pale cyan ripple highlight
STYLE: Dragon Ball anime game item icon, 16-bit pixel art, clean chunky pixels,
       1px dark outline around the object, light source from top-left,
       4-6 colors total, high saturation, still readable when shrunk to 22x22
BACKGROUND: fully transparent, no ground shadow
COMPOSITION: single object centered, fills the frame, 1-2px margin,
       no text, no numbers, no border frame
OUTPUT: square PNG with alpha, 88x88
NEGATIVE: photorealistic, 3D render, blurry, gradient mesh, text, watermark,
       drop shadow outside object, white background, multiple objects,
       fossil, dragon ball, sphere
```

### 13. `32717.png` — 2021 Mảnh giáp khắc tên

Mảnh giáp cong, mép rách, chữ khắc đã mờ (chỉ là vệt khắc, không phải chữ thật).

```
SUBJECT: a curved broken piece of battle armor plating, torn jagged edge on one side,
         faint worn scratch marks engraved on the surface suggesting a name
COLORS: bronze plate, dark brown shadow, pale scratch highlight, rust spots
STYLE: Dragon Ball anime game item icon, 16-bit pixel art, clean chunky pixels,
       1px dark outline around the object, light source from top-left,
       4-6 colors total, high saturation, still readable when shrunk to 22x22
BACKGROUND: fully transparent, no ground shadow
COMPOSITION: single object centered, fills the frame, 1-2px margin,
       no text, no numbers, no border frame
OUTPUT: square PNG with alpha, 88x88
NEGATIVE: photorealistic, 3D render, blurry, gradient mesh, text, watermark,
       drop shadow outside object, white background, multiple objects,
       readable letters, cloth, fabric, shirt
```

### 14. `32718.png` — 2022 Thẻ từ phòng thí nghiệm

Thẻ từ giả để vào phòng thí nghiệm map 166.

```
SUBJECT: a plastic keycard tilted at a slight angle, rounded corners,
         a black magnetic stripe running across it, a tiny photo square
         in the top-left corner, a thin colored accent line
COLORS: white plastic, black stripe, cyan accent line, gray shadow
STYLE: Dragon Ball anime game item icon, 16-bit pixel art, clean chunky pixels,
       1px dark outline around the object, light source from top-left,
       4-6 colors total, high saturation, still readable when shrunk to 22x22
BACKGROUND: fully transparent, no ground shadow
COMPOSITION: single object centered, fills the frame, 1-2px margin,
       no text, no numbers, no border frame
OUTPUT: square PNG with alpha, 88x88
NEGATIVE: photorealistic, 3D render, blurry, gradient mesh, text, watermark,
       drop shadow outside object, white background, multiple objects,
       readable letters, gemstone, shield
```

### 15. `32719.png` — 2023 Bản thiết kế bản sao

Bản vẽ kỹ thuật đánh cắp. Nhặt đủ 5 bản trong 6 phút.

```
SUBJECT: a rolled blueprint partly unrolled showing white technical line drawings
         of a machine on deep blue paper, a faint grid, one end still rolled up
COLORS: deep blueprint blue, white lines, pale gray edge, dark navy shadow
STYLE: Dragon Ball anime game item icon, 16-bit pixel art, clean chunky pixels,
       1px dark outline around the object, light source from top-left,
       4-6 colors total, high saturation, still readable when shrunk to 22x22
BACKGROUND: fully transparent, no ground shadow
COMPOSITION: single object centered, fills the frame, 1-2px margin,
       no text, no numbers, no border frame
OUTPUT: square PNG with alpha, 88x88
NEGATIVE: photorealistic, 3D render, blurry, gradient mesh, text, watermark,
       drop shadow outside object, white background, multiple objects,
       readable letters, treasure chest, box
```

### 16. `32720.png` — 2024 Lõi Ký Ức chưa hoàn chỉnh

Quả cầu rỗng nứt, **7 khe trống** chờ lắp 7 Mảnh Ký Ức. Đây là món anh em với 7 mảnh
màu hổ phách đã vẽ trước đó — hình khe nên khớp dáng mảnh.

```
SUBJECT: a hollow cracked sphere shell, seven empty socket slots arranged evenly
         around its surface waiting for shards to be inserted, dark empty interior
         visible through the cracks
COLORS: dull pewter gray, dark hollow interior, faint gold rim on the sockets
STYLE: Dragon Ball anime game item icon, 16-bit pixel art, clean chunky pixels,
       1px dark outline around the object, light source from top-left,
       4-6 colors total, high saturation, still readable when shrunk to 22x22
BACKGROUND: fully transparent, no ground shadow
COMPOSITION: single object centered, fills the frame, 1-2px margin,
       no text, no numbers, no border frame
OUTPUT: square PNG with alpha, 88x88
NEGATIVE: photorealistic, 3D render, blurry, gradient mesh, text, watermark,
       drop shadow outside object, white background, multiple objects,
       orange dragon ball with stars, solid glowing orb
```

### 17. `32721.png` — 2025 Mảnh Ký Ức Vỡ

Mảnh ký ức màu hổ phách. **Phải đọc ra là chất liệu khác `32705`** (mảnh hư không tím đen).

```
SUBJECT: a small broken crystal shard, translucent amber-gold core with faint
         swirling light inside, hairline cracks on the surface, warm inner glow
COLORS: amber gold, warm orange, pale cream highlight, brown outline
STYLE: Dragon Ball anime game item icon, 16-bit pixel art, clean chunky pixels,
       1px dark outline around the object, light source from top-left,
       4-6 colors total, high saturation, still readable when shrunk to 22x22
BACKGROUND: fully transparent, no ground shadow
COMPOSITION: single object centered, fills the frame, 1-2px margin,
       no text, no numbers, no border frame
OUTPUT: square PNG with alpha, 88x88
NEGATIVE: photorealistic, 3D render, blurry, gradient mesh, text, watermark,
       drop shadow outside object, white background, multiple shards,
       purple, violet, dark colors
```

### 18. `32722.png` — 2026 Mảnh Ký Ức Đóng Băng

Mảnh ký ức bị đóng băng trong hang ở map 110.

```
SUBJECT: an amber memory shard sealed inside a chunk of blue ice,
         frost crystals growing on the ice surface, a wisp of cold vapor
COLORS: ice blue, white frost, amber glow trapped inside, pale cyan highlight
STYLE: Dragon Ball anime game item icon, 16-bit pixel art, clean chunky pixels,
       1px dark outline around the object, light source from top-left,
       4-6 colors total, high saturation, still readable when shrunk to 22x22
BACKGROUND: fully transparent, no ground shadow
COMPOSITION: single object centered, fills the frame, 1-2px margin,
       no text, no numbers, no border frame
OUTPUT: square PNG with alpha, 88x88
NEGATIVE: photorealistic, 3D render, blurry, gradient mesh, text, watermark,
       drop shadow outside object, white background, multiple objects,
       plain blue gemstone, water droplet
```

### 19. `32723.png` — 2027 Mảnh Bùa Babiđây

Mảnh giấy bùa phép rách của Babiđây.

```
SUBJECT: a torn strip of paper talisman, ragged uneven edge at the bottom,
         violet magic glyphs written vertically down the middle,
         one side slightly curled
COLORS: aged yellow paper, violet glyph, dark red border stripe, brown shadow
STYLE: Dragon Ball anime game item icon, 16-bit pixel art, clean chunky pixels,
       1px dark outline around the object, light source from top-left,
       4-6 colors total, high saturation, still readable when shrunk to 22x22
BACKGROUND: fully transparent, no ground shadow
COMPOSITION: single object centered, fills the frame, 1-2px margin,
       no text, no numbers, no border frame
OUTPUT: square PNG with alpha, 88x88
NEGATIVE: photorealistic, 3D render, blurry, gradient mesh, real text, watermark,
       drop shadow outside object, white background, multiple objects,
       red gemstone, ruby, readable letters
```

### 20. `32724.png` — 2028 Lõi Phép Babiđây

Cầu phép rơi ra khi Mabư gục xuống.

```
SUBJECT: a dark magic orb, smooth sphere with swirling violet arcane veins
         crawling across its surface, a faint dark aura bleeding off the edge
COLORS: near-black purple, violet swirl, magenta glow, deep shadow
STYLE: Dragon Ball anime game item icon, 16-bit pixel art, clean chunky pixels,
       1px dark outline around the object, inner glow instead of top-left highlight,
       4-6 colors total, high saturation, still readable when shrunk to 22x22
BACKGROUND: fully transparent, no ground shadow
COMPOSITION: single object centered, fills the frame, 1-2px margin,
       no text, no numbers, no border frame
OUTPUT: square PNG with alpha, 88x88
NEGATIVE: photorealistic, 3D render, blurry, gradient mesh, text, watermark,
       drop shadow outside object, white background, multiple objects,
       glass bottle, jar, container
```

### 21. `32725.png` — 2029 Ống nghiệm Myuu

Ống nghiệm của Dr. Myuu lấy từ Heart.

```
SUBJECT: a glass test tube standing upright with a rubber stopper on top,
         filled with bubbling violet liquid, small bubbles rising inside,
         a glass highlight streak down one side
COLORS: violet liquid, clear glass with white highlight, dark gray stopper
STYLE: Dragon Ball anime game item icon, 16-bit pixel art, clean chunky pixels,
       1px dark outline around the object, light source from top-left,
       4-6 colors total, high saturation, still readable when shrunk to 22x22
BACKGROUND: fully transparent, no ground shadow
COMPOSITION: single object centered, fills the frame, 1-2px margin,
       no text, no numbers, no border frame
OUTPUT: square PNG with alpha, 88x88
NEGATIVE: photorealistic, 3D render, blurry, gradient mesh, text, watermark,
       drop shadow outside object, white background, multiple objects,
       round potion flask, wide bottle
```

### 22. `32726.png` — 2030 Danh hiệu "Người Trả Ký Ức"

Huy hiệu nhánh A — trả ký ức về cho vũ trụ. **Cặp đôi với `32727`: cùng dáng huy hiệu
tròn, ngược tông màu và ngược cử chỉ bàn tay.**

```
SUBJECT: a round emblem badge, an open upward palm in the center releasing
         small motes of light that float upward, a thin laurel ring around the rim
COLORS: sky blue field, white light motes, silver ring, pale cyan highlight
STYLE: Dragon Ball anime game badge icon, 16-bit pixel art, clean chunky pixels,
       1px dark outline around the object, light source from top-left,
       4-6 colors total, high saturation, still readable when shrunk to 22x22
BACKGROUND: fully transparent, no ground shadow
COMPOSITION: single object centered, fills the frame, 1-2px margin,
       no text, no numbers, no border frame
OUTPUT: square PNG with alpha, 88x88
NEGATIVE: photorealistic, 3D render, blurry, gradient mesh, text, watermark,
       drop shadow outside object, white background, multiple objects,
       ribbon, trophy cup, star medal
```

### 23. `32727.png` — 2031 Danh hiệu "Kẻ Giữ Hư Không"

Huy hiệu nhánh B — giữ lại Lõi Hư Không. **Cặp đôi với `32726`.**

```
SUBJECT: a round emblem badge, a closed fist in the center gripping a dark orb,
         a thin laurel ring around the rim
COLORS: deep violet field, near-black orb, magenta glow, dark silver ring
STYLE: Dragon Ball anime game badge icon, 16-bit pixel art, clean chunky pixels,
       1px dark outline around the object, inner glow from the orb,
       4-6 colors total, high saturation, still readable when shrunk to 22x22
BACKGROUND: fully transparent, no ground shadow
COMPOSITION: single object centered, fills the frame, 1-2px margin,
       no text, no numbers, no border frame
OUTPUT: square PNG with alpha, 88x88
NEGATIVE: photorealistic, 3D render, blurry, gradient mesh, text, watermark,
       drop shadow outside object, white background, multiple objects,
       ribbon, trophy cup, star medal
```

---

## 5. Sau khi có ảnh

Giao 23 file `32705.png` … `32727.png` là xong phần thiết kế. Phần còn lại tôi làm:

1. Thu nhỏ ra đủ 4 mức, chép vào `SRC/data/icon/x1`, `x2`, `x3`, `x4`.
   Bản x2 / x3 / x4 phải **đúng bội số 2 / 3 / 4 của x1, không lệch một pixel**. Sai chỗ
   này từng làm bấm thông tin đệ Kid Jiren là văng client (icon 8094 để 306×264 ở x2
   thay vì 102×88). Máy chủ nay đã có `checkAvatarScale()` báo đỏ lúc khởi động nếu lệch.
2. Viết patch `93-icon-rieng-vat-pham-nhiem-vu.sql` trỏ `icon_id` cho 23 món.
3. Tăng `DataGame.vsItem` 34 → 35 để client tải lại bảng vật phẩm, nếu không thì máy cũ
   vẫn vẽ icon mượn.
4. Build lại jar.

**Phải đổi sang id icon mới (32705–32727) chứ không ghi đè lên icon đang mượn.** Client
nhớ ảnh theo id trong bộ nhớ đệm — ghi đè thì máy đã chơi rồi vẫn hiện ảnh cũ, mà lại
làm hỏng luôn icon của món gốc. Dải 32705–32727 hiện hoàn toàn trống. Trần cứng là
**32767** vì gói tin ghi icon bằng `short`.

---

## 6. Ngoài lề: 10 file hiệu ứng của hai danh hiệu

Không phải icon, nhưng nếu đang thuê vẽ thì làm luôn một thể.

Hai danh hiệu 2030 / 2031 dùng `idEffect` **257** và **258** để hiện hiệu ứng chạy trên
đầu nhân vật. Mười file đó **chưa có cái nào**:

```
SRC/data/effdata/DataEffect_257
SRC/data/effdata/DataEffect_258
SRC/data/effect/x1/ImgEffect_257.png   x2/   x3/   x4/
SRC/data/effect/x1/ImgEffect_258.png   x2/   x3/   x4/
```

Thiếu thì danh hiệu vẫn cộng chỉ số (+12% sức đánh, +12% HP, +12% KI) nhưng **không
hiện gì trên đầu**, và máy chủ im lặng chứ không báo lỗi.

Đây là **dải chữ một khung**, không phải icon vuông. Mẫu để đo theo:

| idEffect | Của | x1 | x4 | DataEffect |
|---|---|---|---|---|
| 220 | danh hiệu "Trùm săn Boss" | 47×20 | 188×80 | 26 byte |
| 222 | danh hiệu "Cao thủ siêu hạng" | 54×21 | 216×84 | 26 byte |

Cứ vẽ ở x4 khoảng **200×84**, tôi lo phần thu nhỏ và file `DataEffect_`.
