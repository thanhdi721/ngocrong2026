# 66 — Nhẫn Chí Tôn, hai boss Siêu Thần God và NPC Bà Mối

Ngày: 2026-09-26 · Patch SQL: `SRC/sql/patch/78-nhan-chi-ton-ba-moi.sql`

---

## 1. Tóm tắt

Ba thứ đi chung một gói:

| Thứ | Nội dung |
|---|---|
| **Vật phẩm 2264 "Nhẫn Chí Tôn"** | icon 30583 lấy từ SrcBun. Công dụng duy nhất: "thông đít". Mỗi lần dùng mất 1 cái. |
| **Hai boss Siêu Thần God** | Vegeta Siêu Thần God + Goku Siêu Thần God, 500 triệu HP, 50.000 sát thương, 10 phút ra một lượt, mỗi lượt ra CẢ HAI ở hai hành tinh khác nhau. |
| **NPC 87 "Bà Mối"** | đứng cạnh GoKu Nỗi Loạn ở đảo Kamê (map 5), xem sổ đã thông / bị thông bao nhiêu lần. |

---

## 2. Hai boss

`SRC/src/nro/models/boss/sieu_than_god/BossSieuThanGod.java`

* **Ngoại hình** dùng lại part của hai cải trang đã có từ patch 35:
  * Vegeta Siêu Thần God — part `2126 / 2127 / 2128` (item 2041 "Vegeta god"), hào quang 100 Khí Vàng.
  * Goku Siêu Thần God — part `2147 / 2148 / 2149` (item 2048 "Goku SSJ God"), hào quang 99 Khí Xanh Dương.
  * Đã kiểm: cả 6 part đủ số mảnh 3 / 17 / 14 và **không thiếu file icon nào** ở cả 4 mức phóng to.
* **Map**: `{4, 12, 20}` — Rừng xương (Trái Đất) / Vực Maima (Namếc) / Vách núi đen (Xayda).
* **Chiêu**: y hệt cặp Fu (Thái Dương Hạ San, Tái Tạo Năng Lượng, Khiên Năng Lượng, Kamejoko, Galick), `attack()` cũng chép từ Fu.
* **Nhịp ra**: `PHUT_CHO = 10`.
  * Cờ `daRaLuotNay` của từng con giữ nhịp. Con nào chết trước thì **nằm chờ** con kia, không hồi sinh ngay.
  * Khi cả hai đã về `REST` mới bấm lại đồng hồ 10 phút cho lượt sau.
  * (Nếu chỉ so mốc giờ chung thì con chết trước thấy mốc cũ đã quá 10 phút và sẽ ra lại liên tục — đây là lỗi đã tránh.)
* **Khác vị trí**: `getMapJoin()` được ghi đè, bốc lại tối đa 12 lần để không trùng map với con kia. Vegeta được dựng trước nên luôn vào map trước trong cùng một nhịp, con sau nhìn thấy map của con trước.
* **Rơi đồ** — đúng MỘT lượt quay 100 %, rơi ra đất và gán cho người hạ boss:

  | Tỉ lệ | Rơi |
  |---|---|
  | 40 % | Ngọc Rồng 3 / 4 / 5 sao (item 16–18) |
  | 40 % | Ngọc Rồng bí ngô 1–7 sao (item 702–708) |
  | 10 % | một trong: Cuồng nộ 2 (1150), Bổ khí 2 (1151), Bổ huyết 2 (1152), Giáp Xên bọ hung 2 (1153) |
  | 5 % | 1 Nhẫn Chí Tôn (2264) |
  | 5 % | 5 Đá Pháp Sư (2262) |

  Có chốt chặn: nếu chưa chạy patch (id vượt cỡ `ITEM_TEMPLATES`) thì bỏ rơi và ghi log, thay vì văng `IndexOutOfBounds` giữa luồng boss.
* **Thông báo**: máu 500 triệu ≥ ngưỡng `THONG_BAO_MAU_MIN` (1 triệu) nên server tự loa khi boss ra map.

---

## 3. "Thông đít"

`SRC/src/nro/models/services/ThongDitService.java`

### Cách chơi
1. Hai người đứng **sát nhau** (≤ 80 px, cùng khu).
2. Bấm vào người kia → menu **Thách đấu** → mục cuối cùng là **"Thông đít"**.
3. Người kia nhận menu hỏi, phải bấm **Đồng ý** thì mới tính (lời mời sống 30 giây).
4. Người đi thông mất **1 Nhẫn Chí Tôn**.

### Chỉ số
| Vai | Số lần tối đa | Mỗi lần | Kịch trần |
|---|---|---|---|
| Người đi thông | 10 | +1 % | +10 % |
| Người bị thông | 20 | 2 lần mới +1 % | +10 % |

Cộng vào **HP tối đa, KI tối đa và sức đánh**. Ai vừa thông 10 lần vừa bị thông 20 lần thì được +20 %.
Công thức nằm ở `ThongDitService.phanTram()`, được `NPoint.setHpMax / setMpMax / setDame` nhân vào ở bước cuối cùng (chỉ áp cho người chơi thật, pet và boss dùng chung `NPoint` nên bị loại bằng `isPl()`).

### Chỗ móc vào menu
`PVPService.openSelectGold` nay mở menu bằng `optionsWithThongDit` — ba mức cược cũ **cộng thêm** mục "Thông đít" ở cuối. `NpcFactory` (NPC con mèo) gặp `select == PVPService.CHON_THONG_DIT` thì gọi `ThongDitService.moiThongDit`, còn lại vẫn là thách đấu như cũ. `sendInvitePVP` đã được chặn chỉ số ngoài bảng để client chế không gửi số bậy được.

### Chống lạm dụng
* Bắt buộc **người kia đồng ý**.
* Giãn cách 8 giây giữa hai lần ngỏ lời (chặn spam menu vào mặt người khác).
* Lúc bấm Đồng ý **kiểm tra lại toàn bộ**: còn cùng khu, còn đứng gần, còn nhẫn, chưa kịch trần. Lời mời chỉ dùng được một lần.
* Trừ nhẫn **trước** khi cộng chỉ số — hết nhẫn giữa chừng thì không ai được gì.

### Lưu dữ liệu
Cột `player.thong_dit` (TEXT) chứa `"soLanThong|soLanBiThong"`.

> **Bảng `player` đã kịch trần dòng của InnoDB.** Riêng các cột VARCHAR cộng lại đã 65.040 / 65.535 byte, nên thêm **bất kỳ** cột nào cũng văng lỗi 1118 "Row size too large" — kể cả cột TEXT (TEXT vẫn tốn ~20 byte con trỏ). Patch 78 vì vậy đổi `data_card` từ `VARCHAR(10000)` (= 40.000 byte trong ngân sách dòng, trong khi nó chỉ chứa một chuỗi JSON ngắn) sang `TEXT`, trả lại ~40 KB. Cột này không nằm trong index nào, được ghi bằng `JSONValue.toJSONString` và đọc bằng `rs.getString`, nên đổi kiểu không ảnh hưởng gì. `Manager.ensureSchema` cũng tự làm việc này lúc khởi động.

---

## 4. NPC Bà Mối

`SRC/src/nro/models/npc_list/BaMoi.java` · `npc_template` 87 · map 5 toạ độ `(380, 288)`

* Ngoại hình: cải trang "Cải trang Bunma rực rỡ" (item 1476, part 1380 / 1381 / 1382), avatar 12387.
* Vị trí: Chi Chi ở x = 240, GoKu Nỗi Loạn ở x = 310, Bà Mối ở x = 380 — cách nhau 70 px, hơn bán kính 60 px của `Map.getNpc` nên bấm không lộn NPC. Đã tính lại tile map 5: x = 380 vẫn là nền đặc ở y = 288.
* Hai mục: **Xem sổ của con** (số lần thông / bị thông, danh hiệu theo mốc, tổng % đang có) và **Luật lệ**.

---

## 5. Kiểm tra đã làm

* Biên dịch sạch bằng **JDK 21**, 685 file class.
* Dựng CSDL nháp từ `database team2026.sql` + toàn bộ patch (bỏ qua `36-go-vat-pham-ngol.sql` vì đó là **file lùi lại**, không phải bản vá — chạy nhầm là mất sạch 43 vật phẩm NGOL, trong đó có đúng hai cải trang làm hình cho hai boss này).
* Patch 78 chạy sạch, sáu mục kiểm tra sau đều `dat = 1`.
* Part của hai boss và của Bà Mối: đủ mảnh, đủ file icon cả 4 mức phóng to.
* Gói dữ liệu vật phẩm sau khi thêm item 2264: **63.020 + 63.090 byte** (trần mỗi gói 65.000). **Chỉ còn chỗ cho khoảng 42 vật phẩm nữa** — lần tới thêm đồ phải rút gọn mô tả hoặc đổi cách chia gói.

## 6. Việc người vận hành phải làm

1. Tắt server.
2. `mysqldump` bốn bảng `npc_template map_template item_template player`.
3. Chạy `SRC/sql/patch/78-nhan-chi-ton-ba-moi.sql`, xem khối (3) phải toàn `1`.
4. Bật server bản jar mới (`vsItem = 27`, `vsMap = 9`) để client tải lại bảng vật phẩm và danh sách NPC.
