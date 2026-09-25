# 58 — Khảo sát hai source SUMO và Bun: lấy về được gì (2026-09-25)

Hai nguồn:

* `/Users/phanthanhdi/Downloads/SRC-SUMOV2/SRC JAV` — có `huyensumo.sql` (17 MB) + `resources`
* `/Users/phanthanhdi/Downloads/SrcBun` — có `bunthoi (4).sql` (36 MB) + `resources`

Cả hai cùng engine "Lord-1.0", **định dạng part giống hệt bên mình** (`[[icon,dx,dy],…]`),
bảng `flag_bag` giống hệt, `cai_trang`/`mini_pet` chỉ là cách khác để ghi head/body/leg.
Nghĩa là đường port giống y hệt lần mang cải trang HUNR về (patch 48), không phải đổi định dạng.

**Bun gần như là tập con của SUMO** — chỉ hơn được 4 món (xem mục cuối). Lấy SUMO làm nguồn chính.

## Lấy được gì (so với những thứ mình đang có)

| Loại | SUMO | Bun | Ghi chú |
|---|---|---|---|
| Cải trang | **79 bộ** (đủ part) | 57 (trùng SUMO) | Vegito, Goku Mui, Zeno, Gogeta Blue, Naruto, Kaido, Wukong, Nữ Thần, Trunk… |
| Linh thú (`mini_pet`) | **43 dòng** | 18 | chỉ ~23 cái có tên riêng, còn lại tên "Pét SS"/"0" phải tự đặt |
| Đeo lưng (`flag_bag`) | **50 mục** | 30 | Kiếm Z, Đao Quan Vũ, Quạt Gunbai, Fashion Wing 1–8, Trượng Thiên Sứ… |
| Thú cưỡi | **2** (`mount_53`, `mount_54`) | 2 (trùng) | "Thú Cưỡi cực vip", ảnh 512×3520, 8 khung |
| Hào quang | **1** (`aura_99`, 6 khung) | 1 (`aura_79`) | hai cái khác nhau, lấy được cả hai |
| Map | **13** | 7 | 201–211 có sẵn file địa hình; tile/bg dùng bộ mình đã có |
| Pet đi theo (`pet_follow`) | 44 | 24 | **cơ chế mình chưa có**, xem phần rủi ro |
| Ảnh kỹ năng `Skills_24/25/26` | 25 file | 25 file | kỹ năng riêng của họ, không port hệ kỹ năng thì vô dụng |

Thú cưỡi khác, đồ chỉ số, ngọc rồng… đều trùng với mình, không có gì mới.

## Chỗ khó: đánh số icon

Ba nhóm cải trang + linh thú + đeo lưng cần **2.613 icon**. Icon của mình đang dùng 18.384 id,
trần cứng là 32.767 (client đọc `Short`), còn trống **14.384 chỗ** — đủ, nhưng phải nhét vào
các khoảng trống chứ không nối tiếp đuôi được.

Ảnh bên SUMO: x2/x3/x4 gần đủ (thiếu ~150 file), x1 thiếu 1.823 file. Xử lý:

* thiếu ở x1 → thu nhỏ từ x2 (đúng tỉ lệ zoom, mình vừa làm y vậy cho hào quang);
* trong 148 icon thiếu ở x2, **144 cái trùng id với icon gốc mình đang có** (ảnh gốc game cũ),
  dùng lại id của mình; còn 4 cái không có ở đâu → thay bằng icon trong suốt 2955.

## Việc kéo theo

* `vsItem` và `vsData` phải tăng (thêm item + part).
* Cải trang cần thêm `head_avatar` cho mảnh đầu, nếu không avatar khung chat sẽ trống — lần
  HUNR đã dính lỗi này (patch 55).
* Linh thú phải là **type 27 + đủ part**, đeo ở ô 7, như patch 51/53 đã làm, chứ đừng để type 5.
* Đeo lưng: `flag_bag` của mình đang 156 dòng, id mới nối từ 157 trở đi; item type 11 trỏ
  `part` = id flag_bag.

## Rủi ro / thứ KHÔNG nên lấy

* **`pet_follow` (44 con pet bay theo)**: bảng và code bên mình không có. Server mình chỉ có
  `Service.sendPetFollow` dùng cho chibi. Muốn có phải thêm bảng + code + **và chưa chắc client
  của mình vẽ được** (client mình là bản Unity riêng). Nên để sau, thử 1 con trước.
* **Map "Tháp Tiên Môn", "Tháp Boss vip", "Thái Cực Điện", "Nam Thiên Môn", "Võ Đài Thiên giới",
  "Tây Phương Cực Lạc"**: chỉ là cái vỏ, mob và npc trong đó trỏ tới NPC id 89/90 và tính năng
  riêng của họ. Kéo map về thì chỉ có cảnh, muốn chơi được phải viết tính năng.
  Ba map "Núi Hủy Diệt" thì dùng ngay được (có cửa về Vách núi Aru).
* **Tên trùng nhưng đồ khác**: 50 mục `flag_bag` "mới" là so theo tên; vài cái như "Cờ xanh dương"
  nhiều khả năng mình đã có dưới tên khác. Trước khi nhập phải soi ảnh từng cái.
* Vài cải trang bên họ tên trùng nhau hoặc đặt bừa ("Pét SS", "0", "vegetaa") — phải đặt lại tên.

## Đề nghị thứ tự làm

1. **79 cải trang** (giá trị cao nhất, đường đã quen).
2. **50 đeo lưng** (nhẹ nhất: 49 icon).
3. **2 thú cưỡi + 2 hào quang** (chỉ chép ảnh + 2 dòng `img_by_name` + 2 item).
4. **43 linh thú** (phải đặt tên lại ~20 con).
5. **3 map Núi Hủy Diệt** nếu muốn thêm chỗ cày.
6. Pet đi theo: thử 1 con, được mới làm tiếp.

## Chưa kiểm tra được

Chưa mở từng ảnh xem đẹp/xấu hay trùng đồ mình đang có, và chưa thử trong game.
