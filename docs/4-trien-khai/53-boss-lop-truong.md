# 53 — Bộ boss "Lốp Trưởng" (2026-09-24)

Bản dựng đầu tiên để chạy thử. Code trong `SRC/src/nro/models/boss/lop_truong/`, không cần SQL.

## Luật chơi

* **8 con**: Lốp Trưởng 1 → 8, hình lấy từ 8 cải trang Goku Super Saiyan (2105–2112, part 2313–2336).
* **Mỗi lượt chỉ ra 2 con**, bốc ngẫu nhiên, cùng một khu; **Nữ Thần Băng Tinh** đứng giữa map,
  dùng **bản nhỏ 55%** của cải trang 2079 (part 2367/2368/2369, patch 60).
* Ba nhân vật **ra map là đứng sẵn cạnh nhau** ở giữa map: nữ thần ở giữa, hai boss hai bên, cách
  70 pixel (`KHOANG_CACH`), cùng một mặt nền phía trên, để cả ba cùng lọt vào một màn hình.
  Vị trí được đặt ngay lúc vào map (`joinMapByZone`), không phải kéo về sau — `Boss.moveTo` chỉ đi
  thêm 40–60 pixel mỗi lần nên không bao giờ tới đúng chỗ.
* Có người vào khu → hai con **cãi nhau tranh gái 19 câu** (2,5 giây một câu, khoảng 48 giây), nữ
  thần chen vào giữa, rồi lao vào đánh nhau.
* Trong lúc đánh: hai boss **vừa đánh vừa chửi** (5 giây một câu, luân phiên, 10 câu), nữ thần **cổ
  vũ liên tục** (6 giây một câu, 10 câu).
* **Người chơi mới bước vào khu được réo tên**: hoặc một boss đuổi khéo, hoặc nữ thần than thở. Mỗi
  người chỉ được chào một lần mỗi lượt.
* Hai boss **chỉ đánh nhau, không đánh người chơi**, và **không trừ máu nhau** — đòn qua lại chỉ là
  diễn. Người chơi mới là bên hạ được boss, nên đồ rơi luôn có chủ rõ ràng.
* Khi đánh nhau: hai con rượt và đổi chỗ quanh đối thủ như boss thường, ra đòn theo hồi chiêu.
* Một con chết → **màn kết thúc kéo dài khoảng 10 giây**: con còn sống sỉ nhục 2 câu, nữ thần nói lời
  chia tay, rồi cả ba mới biến mất. Trước đây chat xong biến ngay nên không ai kịp đọc.
* **15 phút** sau khi kết thúc thì ra lượt mới. **30 phút** không ai vào khu thì tự đi.
* Map: khu boss Black Goku — 92, 93, 94, 96, 97, 98, 99, 100, 102.

## Hai đợt luân phiên

| | Đợt A | Đợt B |
|---|---|---|
| Máu mỗi con | 2.000.000.000 | 20.000 |
| Chặn sát thương | 50% | 0% |
| Sát thương boss | 2.000 | 10 |
| Trần sát thương người chơi | không | **100 mỗi đòn** |
| Rơi đồ | như Super Black Goku | bảng riêng bên dưới |

Lượt đầu là đợt A, sau đó đổi qua lại. Đợt B dành cho người cày chay: 20.000 máu với trần 100
là khoảng 200 đòn.

## Bảng rơi đợt B (tổng 100%)

| Tỉ lệ | Phần thưởng |
|---|---|
| 50% | 1–3 bình ngẫu nhiên: Cuồng nộ 2 (1150), Bổ huyết 2 (1152), Bổ khí 2 (1151) |
| 20% | Bùa x2 tn,sm đệ tử (1628) ×1 |
| 10% | Đá bảo vệ (987) ×1 |
| 10% | Sách nâng kỹ năng đệ tử 2/3/4/5 (403, 404, 759, 2123) ×1 |
| 10% | Ngọc Rồng 3 sao (16) ×1 |

Đợt A: vàng 20.000–30.000, cơ hội rơi đồ Thần Linh theo `BossDropRate`, và 5% rơi một món trang bị,
giống hệt Super Black Goku.

## Kỹ năng boss

Đấm Liên Hoàn, Cadic Liên Hoàn Chưởng, Super Kamejoko (cấp 7) và Tái Tạo Năng Lượng cấp 2.
Đã bỏ Ma Phong Ba.

Cách đánh dùng **y hệt lớp Boss gốc**: nhịp 100ms, bước đi 40–60 pixel bằng `moveTo`, hồi chiêu do
từng chiêu quyết định. Nhờ vậy hiệu ứng, animation và độ "nhúng nhúng" giống hệt các boss khác.
Chỉ khác một chỗ: đổi chỗ thưa hơn (tối đa 1,5 giây một lần) để người chơi đỡ đánh hụt.

## Chỗ chỉnh nhanh

Mọi con số và lời thoại nằm đầu `LopTruong.java`:

* `HINH` (8 bộ cải trang), `MAP_JOIN`, `SKILL`, máu / sát thương trong `taoData`.
* `CHO_RA_LAI` 15 phút, `TU_DI` 30 phút, `DAME_TOI_DA_NGUOI_CHOI` 100.
* Nhịp nói: `NHIP_THOAI` 2,5 giây, `NHIP_CHUI` 5 giây, `NHIP_CO_VU` 6 giây.
* Lời thoại: `KICH_BAN` (19 câu cãi nhau), `CHUI_LUC_DANH` (10), `CO_VU` (10), `CHAO_NGUOI_CHOI` (4),
  `NU_THAN_CHAO` (3), `SI_NHUC` (5). Thêm câu chỉ cần thêm dòng vào mảng.

## Vì sao nữ thần phải dùng bản nhỏ

Client vẽ bong bóng thoại ở **độ cao cố định phía trên mốc chân**, không theo chiều cao thật của
nhân vật. Hình cải trang 2079 cao gần gấp đôi người thường nên bóng thoại rơi xuống ngang ngực,
nhìn như nằm dưới chân. Patch 60 thêm 3 part bản nhỏ 55% (31 icon mới, dx/dy thu theo cùng tỉ lệ)
chỉ dùng cho nhân vật boss này; **cải trang 2079 người chơi mặc giữ nguyên**.

Muốn to/nhỏ khác thì sửa `TY_LE` trong đoạn sinh patch rồi tạo lại, hoặc nói tôi làm.

## Chưa kiểm tra được

* Chưa chạy thử trong game: cần xem hình 8 con, vị trí đứng giữa của nữ thần, nhịp thoại 3 giây một
  câu có hợp lý không.
* Boss dùng cpanel tab Boss để ép hồi sinh lẻ một con thì phần điều phối không biết, cặp sẽ lệch.
  Muốn thử thì đợi lượt tự ra, hoặc nói tôi thêm nút riêng trong cpanel.
* Đợt A cần 4 tỷ sát thương thật (2 tỷ máu + chặn 50%) mới hạ được một con. Nếu thấy lâu quá thì
  hạ máu hoặc bỏ phần chặn.

## Quản lý trong cpanel

Tab mới **"Rơi đồ boss"** (`BossDropTab`), lưu vào `data/bossdrop.properties` qua `BossDropConfig`.
Sửa xong bấm "Áp dụng ngay" là ăn liền, không cần khởi động lại.

Sửa được:

| Nhóm | Mục |
|---|---|
| Đồ Thần Linh (mọi boss) | sàn % và trần % — tỉ lệ thật vẫn tính theo máu hiệu dụng từng con |
| Đồ kích hoạt (quái) | `rate` / `per`, mặc định 6 / 99990 |
| Lốp Trưởng đợt 2 tỷ | vàng ít nhất, vàng nhiều nhất, % rơi trang bị, id vàng |
| Lốp Trưởng đợt 20k | % bình, số lượng bình ít/nhiều nhất, % bùa, % đá bảo vệ, % sách đệ tử, % ngọc rồng |
| Vật phẩm rơi | id bình, id bùa, id đá, id sách, id ngọc — nhập nhiều id cách nhau bằng dấu phẩy |
| Nhịp ra boss | phút chờ ra lại, phút vắng người thì đi, trần sát thương người chơi |

Hai nút điều khiển nhanh: **"Lốp Trưởng: cho ra ngay"** (bỏ qua thời gian chờ) và
**"Lốp Trưởng: kết thúc lượt"**. Dòng trạng thái tự làm mới 3 giây một lần, cho biết đang nghỉ còn
bao nhiêu giây, hay đang ở map nào, đợt nào, đang cãi nhau hay đang đánh nhau.

Tab "Boss" sẵn có vẫn dùng được để xem và chỉnh máu / sát thương / thời gian nghỉ của từng con,
kể cả 8 Lốp Trưởng. Chỉ lưu ý: ép hồi sinh lẻ một con từ tab đó thì phần điều phối không biết,
nên muốn thử hãy dùng nút "cho ra ngay" ở tab "Rơi đồ boss".
