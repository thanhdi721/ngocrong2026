# 43 — Bỏ đệ tử khỏi tuyến nhiệm vụ chính

> SQL: [`02-nhiem-vu-moi.sql`](../../SRC/sql/patch/02-nhiem-vu-moi.sql) (cài mới) ·
> [`08-bo-de-tu-khoi-nhiem-vu.sql`](../../SRC/sql/patch/08-bo-de-tu-khoi-nhiem-vu.sql) (server đang chạy)
>
> Java: `services/TaskService.java`, `services/PetService.java`, `player/Player.java`, `npc_list/Jaco.java`

## 1. Vấn đề và quyết định

Người chơi kẹt ở **NV 12 "Bạn đồng hành"**, bước đầu **"Nở trứng nhận đệ tử (0/1)"**. Bước này chỉ
xong khi `player.pet != null`, mà server không có nguồn đệ tử miễn phí nào: đệ tử chỉ đến từ
**Super Broly**, gói VIP của ToriBot và lệnh admin. Menu "Nở trứng" mà tài liệu 20a/27 đề xuất cho
`npc_list/QuaTrung.java` **chưa bao giờ được viết** (QuaTrung vẫn chỉ phục vụ trứng Mabư).

Chủ dự án chốt: **đệ tử vẫn giữ cách nhận cũ (săn Super Broly)**. Vì vậy mọi thứ liên quan tới
đệ tử được gỡ khỏi tuyến chính — cả bước nhiệm vụ lẫn phần thưởng.

## 2. Những gì đã gỡ

### 2.1 Bước nhiệm vụ

Đã quét toàn bộ 51 nhiệm vụ / 237 bước trong `02` (và chữ của `06`, `07`). Chỉ **NV 12** có bước
dính đệ tử:

| Bước | Cũ | Trigger cũ |
|---|---|---|
| 12.0 | Nở trứng nhận đệ tử | B8 `checkDoneTaskHavePet` (`player.pet != null`) |
| 12.1 | Cùng đệ tử hạ 25 quái mẹ | A1 (thực ra không cần đệ tử, chỉ chữ) |
| 12.2 | Dẫn đệ tử gặp %2 | A3 nói chuyện ông |

Không có bước "đệ tử đạt sức mạnh" hay "hợp thể" nào khác. NV 45 "Hợp nhất 7 mảnh" là ghép
Mảnh Ký Ức, **không** phải hợp thể với đệ. NV 10 "xin làm đệ tử" là người chơi làm đệ tử của sư
phụ — giữ nguyên.

### 2.2 Code

| File | Đã gỡ |
|---|---|
| `services/TaskService.java` | Hàm `checkDoneTaskHavePet` (B8); nhánh `TASK_12_0` trong `recheckPassiveSubTask` |
| `services/PetService.java` | Lời gọi `checkDoneTaskHavePet` cuối `createNewPet` (+ import thừa do nhóm trước thêm) |
| `player/Player.java` | Lời gọi định kỳ `checkDoneTaskHavePet(this)` trong `update()` |

**Không đụng** cơ chế nhận đệ cũ: `SuperBroly.reward` vẫn `createNormalPet`, `PetService`,
`QuaTrung` (trứng Mabư), ToriBot, lệnh admin giữ nguyên. Không file nào trong `cpanel`, `event/**`,
hai file `NpcService`, `mob/Mob.java` bị sửa; `checkDoneTaskTalkNpc` giữ nguyên, chỉ sửa
`checkDoneTaskTalkNpcInner`.

### 2.3 Phần thưởng chỉ dùng được cho đệ tử

Tra `docs/1-he-thong-hien-tai/05-de-tu-pet.md` và `06-vat-pham.md`:

| Id | Vật phẩm | Vì sao vô dụng khi không có đệ | Có trong thưởng? |
|---|---|---|---|
| 401 | Đổi đệ tử | `changePet` cần đệ | **NV 12** |
| 402 | Nâng kỹ năng 1 đệ tử | `upSkillPet` | **NV 12** |
| 454 | Bông tai Porata | hợp thể với đệ | **NV 34** |
| 921 | Bông tai Porata (cấp 2) | hợp thể với đệ | **NV 37** |
| 1819 | Bông tai Porata (cấp 3) | hợp thể với đệ | **NV 39** |
| 1795 | Bình hút năng lượng | chỉ để đổi đệ Mabư ≥ 40 tỷ SM sang đệ VIP | **NV 36 bước 0** |
| 400, 403, 404, 759, 1628, 1758–1760 | thẻ tên đệ, nâng kỹ năng 2–4, bùa x2 đệ, phở đổi chiêu đệ | chỉ cho đệ | không có |

Mọi vật phẩm khác trong bảng thưởng (đậu, rada, capsule, đá nâng cấp, đá bảo vệ, đá ngũ sắc,
sao pha lê, mảnh Thần Linh, item nhiệm vụ 2000–2031…) đều dùng được khi không có đệ.

## 3. NV 12 mới — "Bạn đồng hành" (bạn là Jaco)

Hợp mạch chương 2 "Kẻ trộm ký ức": Jaco là người dẫn chuyện chương 1–2 và là "người đồng hành
chương 2" bị Heart xóa ký ức ở **NV 15 "Người bạn đã quên"**. Cho Jaco chính thức đi cùng người
chơi ở NV 12 làm cú phản bội NV 15 nặng hơn.

**Mô tả** (194 ký tự với placeholder dài nhất):

```
Jaco xin đi cùng ngươi: hai cái đầu thì quên chậm hơn một.
Gặp Jaco ở trạm tàu, cùng hạ quái mẹ ở %15 rồi về nhà.
Thưởng: 200.000 SM, 200.000 TN, 1 Gói 30 đậu cấp 3, 5 Đá nâng cấp 1
```

Giữ **3 bước**, cùng `ducvupro` 39/40/41, nên dòng thưởng theo bước (`sub_index` 0–2, 20.000 SM & TN
mỗi bước) không đổi.

| # | Tên bước (dài) | Kiểu / trigger | SL | NPC | Mũi tên (`map`) | Câu nhắc |
|---|---|---|---|---|---|---|
| 0 | Gặp Jaco ở Trạm tàu vũ trụ (26) | A3 — `checkDoneTaskTalkNpcInner`, nhánh `JACO` + `isMapTTVT` | 1 | 63 Jaco | -6 → 24 / 25 / 26 | Jaco đợi ở Trạm tàu vũ trụ hành tinh ngươi, hắn muốn đi cùng ngươi |
| 1 | Cùng Jaco hạ 25 quái mẹ (23) | A1 — `checkDoneTaskKillMob` mob 10/11/12, mọi map (đã có sẵn) | 25 | — | -11 → 4 / 12 / 18 | Jaco đi tuần cùng ngươi: %14 ở %15; quái mẹ loại nào, map nào cũng tính |
| 2 | Đưa Jaco về gặp %2 (27) | A3 — nhánh ông nội (đã có sẵn) | 1 | -2 ông | -2 → 21 / 22 / 23 | Về nhà giới thiệu Jaco với %2 |

- **Quái / map**: Thằn lằn mẹ / Phi long mẹ / Quỷ bay mẹ (HP 1.000) ở Rừng xương 4 / Vực maima 12 /
  Rừng thông Xayda 18 (và 27/28, 31/32, 35/36). Đúng tầm NV 12 (~450.000 → ~800.000 SM), cùng tầm
  với NV 13 (quái mẹ) và NV 14 (heo HP 1.500). Map 4/12/18 và Trạm tàu vũ trụ đều đã mở từ NV 7;
  nhóm 27/31/35 khóa tới `TASK_14_2` nên không dùng làm mũi tên.
- **Jaco ở Trạm tàu vũ trụ Namếc / Xayda** cần `04-npc-tren-map.sql` (đã bắt buộc từ trước cho
  NV 7.2 và NV 15.2).
- **Lời thoại** (thêm trong `doneTask`):
  - 12.0 Jaco: "Ngươi tới rồi. Ta ghi chép suốt mà trí nhớ cứ rơi như cát. / Hai cái đầu thì quên
    chậm hơn một. Từ nay ta đi cùng ngươi. / Lũ quái mẹ ở %15 lớn nhanh bất thường. Dọn 25 con với ta."
  - 12.2 ông: "Bạn con đấy à? Cảnh sát vũ trụ cơ đấy! / Ông không nhớ đã gặp cậu ta chưa, mà sao
    thấy quen quen... / Có người đi cùng thì ông yên tâm. Hai đứa nhớ giữ lấy nhau nhé."
  - Vì có lời thoại, `checkDoneTaskTalkNpc` không phải gửi câu "Tốt lắm! Việc tiếp theo…" thay.

## 4. Bảng thưởng cũ → mới

Chỉ sức mạnh, tiềm năng và vật phẩm; không vàng, ngọc, Ngọc Rồng. Tất cả vật phẩm thay thế là
TYPE 27 / đậu (không có chỉ số) nên cột option để `[]`.

| NV | Dòng | Cũ | Mới | Ghi chú |
|---|---|---|---|---|
| 12 | -1 | 200.000 SM/TN · 1 Đổi đệ tử (401) · 1 Nâng kỹ năng 1 đệ tử (402) | 200.000 SM/TN · **1 Gói 30 đậu thần cấp 3 (295) · 5 Đá nâng cấp cấp 1 (1074)** | nằm giữa NV 11 (1 gói đậu) và NV 14 (10 đá nâng cấp 1) |
| 34 | -1 | 300 triệu SM/TN · 1 Bông tai Porata (454) | 300 triệu SM/TN · **5 Đá bảo vệ (987) · 3 Đá ngũ sắc (674)** | ngang NV 33 (5 đá bảo vệ) + NV 26 (3 đá ngũ sắc) |
| 36 | 0 | 40 triệu SM/TN · 1 Bình hút năng lượng (1795) | 40 triệu SM/TN · **10 Đậu thần cấp 8 (352)** | |
| 37 | -1 | 450 triệu SM/TN · 1 Bông tai Porata cấp 2 (921) | 450 triệu SM/TN · **5 Đá bảo vệ (987) · 5 Đá ngũ sắc (674)** | Porata 2 quý hơn Porata 1 → nhiều đá ngũ sắc hơn |
| 39 | -1 | 1 tỷ SM/TN · 1 Bông tai Porata cấp 3 (1819) · 50 Đậu thần cấp 8 | 1 tỷ SM/TN · **10 Đá bảo vệ (987)** · 50 Đậu thần cấp 8 | |

Mô tả NV 34 / 37 / 39 sửa dòng "Thưởng:" cho khớp. Câu kiểm tra 5.8 của `02` bỏ các id
401/402/454/921/1795/1819 khỏi danh sách.

Người chơi đã nhận 401/402/Porata trước đây **giữ nguyên** vật phẩm (không thu hồi).

## 5. Chạy file 08 (server đang chạy — đã có 02, 05, 06, 07)

1. **Tắt server.** Nếu không, server ghi đè `player.data_task` từ bộ nhớ và lệnh đưa về đầu NV 12 mất tác dụng.
2. Sao lưu:
   `mysqldump -u root -p team2026 task_main_template task_sub_template task_main_reward player > backup_truoc_08_$(date +%F).sql`
3. Chạy: `mysql -u root -p team2026 < SRC/sql/patch/08-bo-de-tu-khoi-nhiem-vu.sql` (hoặc dán vào phpMyAdmin).
4. Xem mục KIỂM TRA cuối file:
   - K1: 3 bước NV 12 như bảng mục 3 · K2: 51 nhiệm vụ / 237 bước
   - K3, K4, K6, K7: 0 dòng · K5: đúng 1 dòng (NV 10 "xin làm đệ tử")
   - K8: số người chơi NV 12 đã sao lưu · K9: tên bước ≤ 28, mô tả ≤ 200
5. Build lại jar (Java mục 2.2 + bước Jaco) và khởi động server.

File 08 làm gì:
- Cập nhật mô tả NV 12/34/37/39, 3 bước NV 12 (tên, `max_count`, câu nhắc, NPC, mũi tên) và 5 dòng thưởng.
- Chép `data_task` gốc của mọi người chơi đang ở NV 12 vào `player_task_backup_08` (`INSERT IGNORE` — lần chạy sau không ghi đè bản gốc).
- Đặt `data_task = '[12,0,0,0]'` cho họ. Chỉ dùng `JSON_VALID` / `JSON_EXTRACT` (có trong MariaDB 10.4), không `JSON_TABLE`.
- Chạy lại nhiều lần cho cùng kết quả.

Tác dụng phụ: người chơi đang ở bước 1–2 NV 12 làm lại từ bước 0, và nhận lại thưởng bước (20.000 SM/TN mỗi bước, không đáng kể).

**Cài mới**: `02` đã chứa nội dung mới. Nếu vẫn chạy `06` và `07` sau `02` thì hai file đó ghi lại chữ NV 12 cũ, nên **chạy `08` cuối cùng**. Sau `08`, câu K2 (dấu vân tay CRC32) của `06` và `07` không còn bằng 1 — đúng như dự kiến.

## 6. Kết quả kiểm tra

- `javac` toàn bộ `src`: sạch.
- Import thử (MySQL 8.4 cục bộ, không có MariaDB; file 08 chỉ dùng cú pháp có trong MariaDB 10.4):
  - DB tạm A: `database team2026.sql` → 01 → 02 (mới) → 05 → 06 → 07 → 08: không lỗi. 4 người chơi
    thử `[12,1,7,…]`, `[12,0,0,0]`, `[12,2,0,5]`, `[13,1,5,0]` → ba người NV 12 về `[12,0,0,0]`, người NV 13 giữ nguyên.
    Chạy 08 lần hai: kết quả giống hệt.
  - DB tạm B (giả lập server đang chạy): … → 02 **bản cũ trong git HEAD** → 05 → 06 → 07 → 08:
    ba bảng nhiệm vụ **giống hệt** DB A.
  - DB tạm C: … → 02 (mới) → 05: ba bảng nhiệm vụ **giống hệt** DB A.
  - Đã xóa cả ba DB tạm.
