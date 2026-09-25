# 60 — Hai source SUMO / Bun có tính năng gì đáng lấy (2026-09-25)

Soi phần **code** (không phải res) của `SRC-SUMOV2/SRC JAV/src` (400 file) và
`SrcBun/src` (428 file), đối chiếu với bên mình (591 file).

Hai source dùng engine "Lord", tên lớp khác bên mình gần hết, nên **không copy-paste được**:
lấy về là viết lại theo API của mình, chỉ mượn luật chơi và cách tính. Cái quyết định dễ hay
khó là **gói tin client có sẵn chưa**.

## Mình đã có rồi (khỏi ngó)

Đại hội võ thuật, Võ đài sinh tử, Doanh trại, Bản đồ kho báu, Con đường rắn độc, Ngọc rồng
sao đen, Ngọc rồng Namếc, Ma Bư, thách đấu + phục thù (`PVPService` có `openSelectRevenge`),
vệ tinh (`Satellite`), bẫy map (`TrapMap`), sổ sưu tập = hệ thẻ rađa, thành tựu, ký gửi,
mở giới hạn sức mạnh, vòng quay (mình có Thượng Đế).

## Đáng lấy — xếp theo dễ/lợi

| # | Tính năng | Nguồn | Cỡ | Gói tin client | Ghi chú |
|---|---|---|---|---|---|
| 1 | **Bầu Cua** | SUMO `npc/BauCua` | 418 dòng | **chỉ menu NPC** | an toàn nhất, không cần client hỗ trợ gì thêm |
| 2 | **Leo Tháp** | SUMO `map/dungeon/LeoThap` | 192 dòng | Boss + Zone + menu | boss máu/sát thương nhân theo tầng và level, map riêng 1 người |
| 3 | **Tài Xỉu** | SUMO `server/TaiXiu` | 309 dòng | `-126` | **mình đã có sẵn hàm gửi** `Service.showYourNumber` (đang không ai gọi) → client gần như chắc chắn vẽ được |
| 4 | **Hào quang / hiệu ứng đồ đeo lưng** | SUMO `player/EffectFlagBag` | 47 dòng | dùng hiệu ứng sẵn | vỏ ốc, cây kem, cá heo, con diều… khi đeo thì bật hiệu ứng riêng |
| 5 | **Tranh Ngọc Namếc** | SUMO `phuban/DragonNamecWar` | ~200 dòng | map + boss | đánh nhau tranh ngọc trên map 164 (map này mình chưa có) |
| 6 | **Hộ tống** | SUMO `boss/broly/HoTong` + `EscortedBoss` | 144 + 85 | boss thường | boss đi kèm, đánh boss chính phải dọn đám hộ tống |
| 7 | **Thuộc tính có hạn** (`attr`) | cả hai | ~150 dòng | không | buff cộng chỉ số theo template, đếm ngược theo phút |
| 8 | **Giới hạn mua/ngày** (`BuyLimit`) | cả hai | 20 dòng | không | chặn cày shop, dễ ghép vào `ShopService` |
| 9 | **Pet đi theo** (`PetFollow`, `MiniPet`) | cả hai | ~110 dòng | **chưa rõ** | 44 con có sẵn res; client mình là bản Unity riêng, phải thử 1 con trước |
| 10 | **Quay Tam Bảo** | SUMO `quayTamBao` | 1.118 dòng | `69` — **mình không dùng bao giờ** | giao diện riêng, rủi ro client không có; để cuối |

## Bộ boss có sẵn để bê nguyên luật

* SUMO: ~60 lớp boss (`BOSSSKH2..8`, `BOSSTHIENGIOI1..4`, `BOSSPHALE`, `BOSSNEZUCO`,
  `BOSSKEYVANG/BAC`, `Sumo1..3`, `TDTRUONG`…) đều `extends FutureBoss` — khung giống
  `BossData` bên mình, chép luật (máu, chiêu, rơi đồ) khá nhanh.
* Bun: cả một **tháp có tầng** — `Drabula_Tang1/5/6`, `BuiBui_Tang2/3`, `Yacon_Tang4`,
  `Calich_Tang5`, `Goku_Tang5`, `Mabu_Tang6` — ghép với Leo Tháp ở mục 2 là ra tính năng hoàn chỉnh.

## Không nên lấy

* **Netty / LoginService / ConnPool / jdbc**: khác hẳn tầng mạng của mình, đụng vào là gãy.
* **CollectionBook, MartialCongress, SnakeRoad, DoanhTrai…**: mình đã có bản riêng, chép về
  chỉ tạo hai hệ song song.
* **Event cũ** (Noel, SummerEvent, SantaCity, sukien2t9): nội dung theo mùa của server họ,
  lấy res thì được, lấy code thì không đáng.

## Chưa kiểm tra được

Chưa chạy thử cái nào. Rủi ro lớn nhất là **client**: ba mục 1, 2, 4 chỉ dùng thứ client đã
hiểu (menu NPC, boss, hiệu ứng) nên gần như chắc chạy; mục 3 nhiều khả năng chạy vì hàm gửi
đã nằm sẵn trong `Service`; mục 9, 10 phải thử mới biết.
