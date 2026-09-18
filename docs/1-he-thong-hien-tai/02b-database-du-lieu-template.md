# 02b. Dữ liệu template trong Database (Ngọc Rồng Online – Teamobi2026)

> Tài liệu tóm tắt **dữ liệu template** (dữ liệu tĩnh, nạp một lần khi khởi động server) trong dump `database team2026.sql`.
> Toàn bộ bảng/số liệu dưới đây được **sinh tự động bằng script Python** parse các câu `INSERT` trong dump (không chỉnh tay), ý nghĩa cột lấy từ code nạp dữ liệu trong `SRC/src/nro/models/server/Manager.java` (hàm `loadDatabase()`, dòng ~288–960) và `SRC/src/nro/models/database/ShopDAO.java`.
> Cấu trúc bảng (kiểu cột, index, code đọc/ghi) xem tại [02-database.md](02-database.md).

## Mục lục

1. [Quy ước chung](#1-quy-ước-chung)
2. [item_option_template – danh sách chỉ số (option) vật phẩm](#2-item_option_template--danh-sách-chỉ-số-option-vật-phẩm)
3. [item_template – vật phẩm](#3-item_template--vật-phẩm)
   - 3.1 Thống kê theo TYPE và ý nghĩa TYPE
   - 3.2 Nhóm vật phẩm quan trọng
   - 3.3 Danh sách đầy đủ 2000 vật phẩm
4. [skill_template – kỹ năng](#4-skill_template--kỹ-năng)
5. [map_template – bản đồ](#5-map_template--bản-đồ)
6. [mob_template – quái](#6-mob_template--quái)
7. [npc_template – NPC](#7-npc_template--npc)
8. [intrinsic – nội tại](#8-intrinsic--nội-tại)
9. [radar – thẻ sưu tầm (Rada)](#9-radar--thẻ-sưu-tầm-rada)
10. [shop / tab_shop / item_shop – cửa hàng](#10-shop--tab_shop--item_shop--cửa-hàng)
11. [Nhiệm vụ: task_main_template / task_sub_template / side_task_template / clan_task_template](#11-nhiệm-vụ)
12. [achievement_template, data_badges, task_badges_template](#12-thành-tựu-và-danh-hiệu)
13. [Ghi chú / điểm cần lưu ý](#13-ghi-chú--điểm-cần-lưu-ý)

---

## 1. Quy ước chung

| Khái niệm | Giá trị | Nguồn |
|---|---|---|
| gender / nclass | `0` = Trái Đất, `1` = Namếc, `2` = Xayda, `3` = dùng chung mọi hành tinh | `nro/models/consts/ConstPlayer.java` (TRAI_DAT=0, NAMEC=1, XAYDA=2); `Manager.loadDatabase()` phần intrinsic: gender khác 0/1/2 thì thêm vào cả 3 list |
| planet_id (map) | `0` = Trái Đất, `1` = Namếc, `2` = Xayda (suy ra từ tên map: Làng Aru/Làng Mori/Làng Kakarot) | dữ liệu `map_template` |
| type (map) | 0 MAP_NORMAL, 1 MAP_OFFLINE, 2 MAP_DOANH_TRAI, 3 MAP_BLACK_BALL_WAR, 4 MAP_BAN_DO_KHO_BAU, 5 MAP_MA_BU, 6 MAP_CON_DUONG_RAN_DOC, 7 MAP_KHI_GAS_HUY_DIET, 8 MAP_TAY_KARIN, 9 MAP_MABU_14H | `nro/models/consts/ConstMap.java` |
| type_shop | 0 = NORMAL_SHOP, 1 = KINANG_SHOP (shop sách kỹ năng), 3 = SPEC_SHOP | `nro/models/shop/ShopService.java` dòng 45–47, `openShop` dòng ~83 |
| type_sell (item_shop) | 0 = vàng (COST_GOLD), 1 = ngọc xanh (COST_GEM), 3 = hồng ngọc (COST_RUBY), 4 = điểm/coupon (COST_COUPON) | `ShopService.java` dòng 40–43 |
| Ký tự `#` trong tên option | được thay bằng `param` của option khi hiển thị | `item_option_template.NAME` |
| `<>` trong tên tab shop | xuống dòng | `ShopDAO.loadShopTab()` (`replaceAll("<>", "\n")`) |

## 2. item_option_template – danh sách chỉ số (option) vật phẩm

Tổng số: **251** option (id 0 → 250). Nạp tại `Manager.loadDatabase()` (`select id, name from item_option_template`, dòng 677) vào `Manager.ITEM_OPTION_TEMPLATES`. Option của vật phẩm lưu dạng cặp `[option_id, param]` (xem 02-database.md, cột `items_bag`).

Một số option có ý nghĩa đặc biệt trong code: `93` = hạn sử dụng (ngày) – `ItemService.isOutOfDateTime()` trừ dần mỗi ngày và xóa vật phẩm khi về 0; `127`–`135` = option set kích hoạt (`Item.isSKH()`); `86`/`87` = ký gửi vàng/ngọc (`Item.isDoKyGui()`); `30` = không thể giao dịch.

| id | Tên option (NAME) |
|---|---|
| 0 | Tấn công+# |
| 1 | Thời gian sử dụng # phút |
| 2 | HP, KI+#000 |
| 3 | Vô hiệu và biến #% sát thương chưởng thành KI |
| 4 | Hồi phục #% KI khi bị đánh |
| 5 | +#% sức đánh chí mạng |
| 6 | HP+# |
| 7 | KI+# |
| 8 | Hút #% HP, KI xung quanh mỗi 5 giây |
| 9 | Hiệu lực trong # phút |
| 10 | Sát thương chuẩn #% |
| 11 | Công đức +# |
| 12 | Số lần sử dụng còn lại +# |
| 13 | Số yêu quái đã hạ +# |
| 14 | Chí mạng+#% |
| 15 | Phản đòn cận chiến+# |
| 16 | Tốc độ di chuyển+#% |
| 17 | Né đòn: +# |
| 18 | Chính xác: +#% |
| 19 | Tấn công+#% khi đánh quái |
| 20 | PIN # |
| 21 | Yêu cầu sức mạnh # tỉ |
| 22 | HP+#K |
| 23 | KI+#K |
| 24 | Làm tăng trọng lực, gây chậm mọi người xung quanh |
| 25 | Tàng hình mỗi 5 giây |
| 26 | Hóa đá mọi người xung quanh mỗi 30 giây |
| 27 | +# HP/30s |
| 28 | +# KI/30s |
| 29 | Biến Sôcôla mọi người xung quanh mỗi 30 giây |
| 30 | Không thể giao dịch |
| 31 | Số lượng # |
| 32 | Không bị hóa Xương |
| 33 | Dịch chuyển tức thời |
| 34 | Tinh ấn |
| 35 | Nguyệt ấn |
| 36 | Nhật ấn |
| 37 | Số lần đã ký gửi # |
| 38 | Chỉ có tác dụng khi hợp thể |
| 39 | <Cập nhật bản mới để xem> |
| 40 | Siêu cải trang # đá ngũ sắc |
| 41 | Chỉ số thưởng +#: |
| 42 | Tấn công+#% lên quái bay |
| 43 | Tấn công+#% lên quái khỉ |
| 44 | Tấn công+#% lên quái mặt đất |
| 45 | Tấn công+#% lên tộc Namếc |
| 46 | Tấn công+#% lên tộc Trái đất |
| 47 | Giáp+# |
| 48 | HP, KI+# |
| 49 | Tấn công+#% |
| 50 | Sức đánh+#% |
| 51 | Sôn Gô Ku ss# |
| 52 | Ca Đic siêu ss# |
| 53 | Broly |
| 54 | Mr. Santa |
| 55 | Broly ss# |
| 56 | Ca Lích |
| 57 | Thên Xin Hăng |
| 58 | Pic |
| 59 | Siêu na mếc # |
| 60 | Sôn Gô Tên |
| 61 | Sôn Gô Ku |
| 62 | Phục hồi thể lực #% |
| 63 | Còn lại # ngày |
| 64 | Còn lại # giờ |
| 65 | Còn lại # phút |
| 66 | Chưa có |
| 67 | Dùng nâng cấp găng tay |
| 68 | Dùng để nâng cấp áo |
| 69 | Dùng để nâng cấp quần |
| 70 | Dùng để nâng cấp giày |
| 71 | Dùng để nâng cấp rađa |
| 72 | Cấp # |
| 73 | *(rỗng)* |
| 74 | Dùng để ép thành đá |
| 75 | Dùng để làm phép |
| 76 | Vip |
| 77 | HP+#% |
| 78 | Sức hủy diệt+#% |
| 79 | Đệ tử #% sức đánh |
| 80 | HP+#%/30s |
| 81 | KI+#%/30s |
| 82 | Không bị quái chủ động đánh và giảm 20% sát thương khi bị đánh |
| 83 | Tăng 20% sức mạnh và tiềm năng nhận được khi đánh quái |
| 84 | Dùng để bay không tốn KI |
| 85 | Dùng để bay và phục hồi KI |
| 86 | Ký gửi vàng |
| 87 | Ký gửi ngọc |
| 88 | Cộng #% Tiềm năng và sức mạnh nhận được khi đánh quái |
| 89 | Dùng để bay và phục hồi HP, KI |
| 90 | Tặng cho người khác (bỏ ra đất) sẽ được nhận may mắn |
| 91 | Mở ra để nhận may mắn |
| 92 | Chúc tết bang hội và mọi người kèm theo pháo hoa |
| 93 | Hạn sử dụng # ngày |
| 94 | Giảm #% sát thương |
| 95 | Biến #% tấn công thành HP |
| 96 | Biến #% tấn công thành KI |
| 97 | Phản #% sát thương |
| 98 | Xuyên giáp #% chưởng |
| 99 | Xuyên giáp #% cận chiến |
| 100 | +#% vàng từ quái |
| 101 | +#% tiềm năng, sức mạnh |
| 102 | # Sao Pha Lê |
| 103 | KI +#% |
| 104 | Biến #% tấn công quái thành HP |
| 105 | Vô hình khi không đánh quái và boss |
| 106 | Không ảnh hưởng bởi cái lạnh |
| 107 | # Sao Pha Lê |
| 108 | #% Né đòn |
| 109 | Hôi, giảm #% HP |
| 110 | Dò pha lê |
| 111 | Phân tâm |
| 112 | Giảm #% khi mua Avatar hoặc Cải trang |
| 113 | Ném cho Sói Hẹc Quyn |
| 114 | +#% TĐ chạy |
| 115 | Biến cà rốt |
| 116 | Kháng TDHS |
| 117 | Đẹp +#% SĐ cho mình và người xung quanh |
| 118 | Tới ngay mục tiêu và gây choáng trong # mili giây |
| 119 | Gây mù xung quanh trong # giây |
| 120 | Ra đòn sau # giây |
| 121 | Ru ngủ trong # giây |
| 122 | Bảo vệ trong # giây |
| 123 | Trói gô mục tiêu trong # giây |
| 124 | Tỉnh giấc bị yếu đi -#% sức đánh trong 10 giây |
| 125 | Tăng và hồi phục #% HP tạm thời cho mình và xung quanh trong 30 giây |
| 126 | Biến sôcôla làm yếu đi -#% sức đánh trong 30 giây |
| 127 | Set Thên Xin Hăng |
| 128 | Set Kirin |
| 129 | Set Sôngôku |
| 130 | Set Picolo |
| 131 | Set Ốc tiêu |
| 132 | Set Pikkoro Daimao |
| 133 | Set Kakarot |
| 134 | Set Ca Đíc |
| 135 | Set Nappa |
| 136 | $(5 món +100% sát thương đấm Galick) |
| 137 | $(5 món x5 thời gian hóa khỉ) |
| 138 | $(5 món +80% HP) |
| 139 | $(5 món x2 thời gian chói mắt) |
| 140 | $(5 món +100% sát thương Quả Cầu Kênh Khi) |
| 141 | $(5 món +100% sát thương Kamejoko) |
| 142 | $(5 món +100% sát thương Masenkosappo) |
| 143 | $(5 món +100% sát thương Liên Hoàn) |
| 144 | $(5 món +100% sát thương và bất tử đệ từ Đẻ Trứng) |
| 145 | $(Ở gần 1 CT Dr Slum khác loại +20% sức đánh +66% tốc độ chạy) |
| 146 | $(Ở gần 2 CT Dr Slum khác loại +30% sức đánh +100% tốc độ chạy) |
| 147 | +#% sức đánh |
| 148 | +#% tốc độ chạy |
| 149 | (Chỉ số tăng khi ở gần CT Hải Tặc khác loại) |
| 150 | (Chỉ số tăng khi ở gần Android Sát Thủ khác loại) |
| 151 | $(Ở gần 1 CT nhóm Piláp khác loại +20% sức đánh +66% tốc độ chạy) |
| 152 | $(Ở gần 2 CT nhóm Piláp khác loại +30% sức đánh +100% tốc độ chạy) |
| 153 | #% tỉ lệ phát nổ sau khi chết |
| 154 | Không thể bán lại |
| 155 | Giảm 50% sức đánh, HP, KI và +#% SM, TN, vàng từ quái |
| 156 | Cộng dồn 1% sức đánh khi chỉ dùng chiêu đấm, tối đa #% |
| 157 | Giảm #% mọi sát thương khi KI dưới 20% |
| 158 | Khi ở trần và không đeo cải trang sẽ có cơ hội tìm thấy vật phẩm sự kiện |
| 159 | x# sức đánh đòn chưởng cơ bản mỗi phút |
| 160 | +#% TN, SM cho đệ tử khi sư phụ mặc |
| 161 | Thêm # ngọc mỗi ngày khi đánh quái. |
| 162 | Cute hồi #% KI/s bản thân và xung quanh |
| 163 | Biến người xung quanh thành Bí Ngô |
| 164 | Đổi bằng # điểm sự kiện |
| 165 | Cơ hội ra đòn +#% sát thương lửa khi cận chiến quái |
| 166 | Ngầu +#% sức đánh lên quái khi bay với Cải Trang Tàu Pảy Pảy |
| 167 | Tạo không khí lạnh |
| 168 | Hấp thụ sức mạnh rồi bộc phá |
| 169 | Cơ hội hạ độc đối thủ |
| 170 | Ngầu +#% sức đánh lên quái khi bay với Cải Trang nhà Fide |
| 171 | Số lượng #K |
| 172 | # |
| 173 | Phục hồi #% HP và KI cho đồng đội |
| 174 | Sự kiện năm # |
| 175 | Giảm #% thời gian bị mù |
| 176 | $(Ở gần đủ 5 loại +20% sức đánh +50% tốc độ chạy) |
| 177 | $(Tối đa +2% tất cả khi ở gần Mabư mập) |
| 178 | KI+#%/10s |
| 179 | +2% sức đánh, tối đa 10% khi ở gần Cải Trang tộc Demons Frost |
| 180 | +#% sức đánh khi ở gần Cải Trang Black Gohan Rose |
| 181 | Dịch chuyển tức thời +#% sát thương |
| 182 | +#% sát thương đệ từ trứng |
| 183 | Giảm #% thời gian hồi Khiên |
| 184 | +#% sức đánh, tối đa 10% khi ở gần Gohan xanh, Poc đỏ, Arale búp bê |
| 185 | +# giờ sử dụng |
| 186 | Biến kẹo 30 giây |
| 187 | Giảm # giây thời gian bị mù |
| 188 | Đệ tử chưởng kamejoko +#% sát thương |
| 189 | Đệ tử chưởng antomic +#% sát thương |
| 190 | Đệ tử chưởng masenko +#% sát thương |
| 191 | Né chí mạng+#% |
| 192 | +#% Chí mạng |
| 193 | +#% sức đánh, tối đa 11% khi ở gần Cải trang cầu thủ khác |
| 194 | +#% sức đánh, tối đa 10% khi ở gần Cải trang hè khác |
| 195 | +#% sức đánh, tối đa 10% khi ở gần Cải trang siêu nhân khác |
| 196 | Xinh +#% sức đánh, tối đa 18% khi ở gần Cải trang Thỏ khác |
| 197 | Tấn công+#% lên tộc Xayda |
| 198 | Giảm #% sát thương từ tộc Trái Đất |
| 199 | Giảm #% sát thương từ tộc Namếc |
| 200 | Giảm #% sát thương từ tộc Xayda |
| 201 | Tấn công+#% gần 2 thành viên bang |
| 202 | HP+#% gần 2 thành viên bang |
| 203 | KI+#% gần 2 thành viên bang |
| 204 | Tấn công+#% lên Boss |
| 205 | Đi cùng CT Diệt Quỷ +#% SĐ, tối đa 18% |
| 206 | Vật phẩm hiếm rơi từ quái (+#%) |
| 207 | Vật phẩm hiếm rơi từ boss (+#%) |
| 208 | Trang bị đã chuyển hóa |
| 209 | Bị rớt cấp # lần |
| 210 | # Dòng chỉ số ẩn |
| 211 | Giám định #/5 |
| 212 | Độ bền #/1000 |
| 213 | Giá bán: # triệu vàng |
| 214 | Phạm vi tuyệt kỹ +#% |
| 215 | Số lượng mục tiêu +# |
| 216 | Tuyệt kỹ +#% sát thương |
| 217 | - Chưa giám định |
| 218 | --------- |
| 219 | Số lần tẩy # |
| 220 | Hoàn thành #% |
| 221 | Phục hồi #% HP và  KI mỗi 30 giây cho bản thân và đồng bang |
| 222 | 120 giây kích nộ sức đánh #% trong 30 giây |
| 223 | 120 giây kích hồi #% HP trong vòng 30 giây |
| 224 | Giảm thời gian cooldown của tất cả kỹ năng về 0,1 giây |
| 225 | Giảm # giây bị phong ấn của Ma Phong Ba |
| 226 | Cute +#% SĐ cho mình và người xung quanh |
| 227 | Giảm #% tác dụng của các đòn khống chế khi dùng khiên năng lượng |
| 228 | Cường hóa tới ô sao pha lê # |
| 229 | Lạnh gây giảm #% HP , KI mỗi 30s cho những người xung quanh |
| 230 | +# triệu tiềm năng, sức mạnh |
| 231 | Hạn sử dụng hoặc vĩnh viễn |
| 232 | Số lần giao dịch còn lại: # |
| 233 | Set Gohan |
| 234 | $(5 món +150% may mắn +30% vàng rơi từ quái) |
| 235 | Số lần ký gửi còn lại: # |
| 236 | +#% May mắn |
| 237 | Set Nail chiến binh Namếc |
| 238 | $[2] Tăng nhẹ sát thương chí mạng |
| 239 | $[4] Giảm nhẹ hồi chiêu Masenko |
| 240 | $[5] Tăng sát thương Masenko, giảm mạnh hồi chiêu Masenko |
| 241 | Set Cađic M |
| 242 | $[2] Tăng nhẹ HP và phạm vi ảnh hưởng chiêu phát nổ |
| 243 | $[4] Tăng nhẹ sát thương chiêu phát nổ |
| 244 | $[5] Tăng mạnh sát thương chiêu phát nổ |
| 245 | Set Thần Vũ Trụ Kaio |
| 246 | $[2] Tăng mạnh chí mạng |
| 247 | $[4] Giảm nhẹ hao HP,KI chiêu Kaioken |
| 248 | $[5] Tăng mạnh sát thương và giảm mạnh hao HP,KI chiêu Kaioken |
| 249 | Hắc hóa: +#% HP cho người chơi xung quanh |
| 250 | # Kilis |

## 3. item_template – vật phẩm

Tổng số: **2000** vật phẩm (id 0 → 1999). Nạp tại `Manager.loadDatabase()` theo lô 750 dòng (`SELECT * FROM item_template LIMIT ? OFFSET ?`, dòng 629) vào `Manager.ITEM_TEMPLATES`. Cột `level`, `type`, `gender` đọc bằng `getByte`, `id` đọc bằng `getShort`.

### 3.1 Thống kê theo TYPE và ý nghĩa TYPE

Ý nghĩa TYPE lấy từ code (`InventoryService.putItemBody()` dòng 289–360, `UseItem.java` switch `item.template.type` dòng ~268–290, `Item.java`); với TYPE không có xử lý riêng trong code thì ghi "suy ra từ tên".

| TYPE | Số lượng | Ý nghĩa | Căn cứ | Ví dụ (id:tên) |
|---|---|---|---|---|
| 0 | 45 | Áo | Trang bị ô 0 (`InventoryService.putItemBody()`: type 0–5 → ô = type) | 0:Áo vải 3 lỗ, 1:Áo sợi len, 2:Áo vải thô, 3:Áo vải dày |
| 1 | 48 | Quần | Trang bị ô 1 | 6:Quần vải đen, 7:Quần sợi len, 8:Quần vải thô, 9:Quần vải dày |
| 2 | 45 | Găng tay | Trang bị ô 2 | 21:Găng vải đen, 22:Găng sợi len, 23:Găng vải thô, 24:Găng thun đen |
| 3 | 45 | Giày | Trang bị ô 3 | 27:Giầy nhựa, 28:Giầy sợi len, 29:Giầy vải thô, 30:Giầy cao su |
| 4 | 17 | Rada / Nhẫn | Trang bị ô 4 (Rada cấp 1..; Nhẫn Thần Linh/Hủy Diệt/Thiên Sứ cũng type 4) | 12:Rada cấp 1, 57:Rada cấp 2, 58:Rada cấp 3, 59:Rada cấp 4 |
| 5 | 457 | Cải trang / Avatar | Trang bị ô 5 | 196:Avatar, 197:Avatar, 198:Avatar, 199:Avatar |
| 6 | 11 | Đậu thần | `UseItem` case 6 "đậu thần"; `Item.isDoKyGui()` coi type 6 là đồ ký gửi được | 13:Đậu thần cấp 1, 60:Đậu thần cấp 2, 61:Đậu thần cấp 3, 62:Đậu thần cấp 4 |
| 7 | 154 | Sách kỹ năng | `UseItem` case 7 "sách học, nâng skill" | 66:Sách đấm Dragon lv1, 67:Sách đấm Dragon lv2, 68:Sách đấm Dragon lv3, 69:Sách đấm Dragon lv4 |
| 8 | 3 | Vật phẩm nhiệm vụ | Đùi gà, Truyện tranh... (suy ra từ tên) | 73:Đùi gà, 75:Đùi heo Xayda, 85:Truyện tranh |
| 9 | 4 | Vàng (item nhặt trên map) | suy ra từ tên | 76:Vàng, 188:Vàng, 189:Vàng, 190:Vàng |
| 10 | 1 | Ngọc (item nhặt trên map) | suy ra từ tên | 77:Ngọc |
| 11 | 190 | Đeo lưng / item bag | Trang bị ô 8; `UseItem` case 11 "item bag"; đệ tử chỉ đeo được nếu là đệ VIP (type pet 2/3/4) | 78:Đứa bé, 353:Ngọc Rồng Namek 1 Sao, 354:Ngọc Rồng Namek 2 Sao, 355:Ngọc Rồng Namek 3 Sao |
| 12 | 28 | Ngọc rồng các loại | `UseItem` case 12 "ngọc rồng các loại" | 14:Ngọc Rồng 1 sao, 15:Ngọc Rồng 2 sao, 16:Ngọc Rồng 3 sao, 17:Ngọc Rồng 4 sao |
| 13 | 14 | Bùa | suy ra từ tên; các bùa khớp danh sách trong cột `data_charm` của player | 213:Bùa Trí Tuệ, 214:Bùa Mạnh Mẽ, 215:Bùa Da Trâu, 216:Bùa Oai Hùng |
| 14 | 5 | Đá quý (Lục bảo, Saphia, Ruby, Titan, Thạch anh tím) | `Item.isDoKyGui()` cho phép ký gửi | 220:Đá lục bảo, 221:Đá Saphia, 222:Đá Ruby, 223:Đá Titan |
| 15 | 1 | Mảnh đá vụn | `Item.isDoKyGui()`; `ConsignShopService` dòng 339 | 225:Mảnh đá vụn |
| 16 | 1 | Bình nước phép | suy ra từ tên | 226:Bình nước phép |
| 17 | 3 | Đai lưng | suy ra từ tên | 1752:Đai lưng, 1753:Đai lưng, 1754:Đai lưng |
| 22 | 4 | Vệ tinh | Vật phẩm thả xuống map (`UseItem` dòng 187, `ItemMap` dòng 108) | 342:Vệ tinh trí lực, 343:Vệ tinh trí tuệ, 344:Vệ tinh phòng thủ, 345:Vệ tinh sinh lực |
| 23 | 50 | Thú cưỡi (mới) | Trang bị ô 9; `UseItem` case 23; mount được map sang `MAP_MOUNT_NUM` nếu có ảnh `mount_<part>_0` (`Manager` dòng ~709) | 346:Cân đẩu vân, 347:Phi Long, 348:Ván bay, 733:Cân đẩu vân ngũ sắc |
| 24 | 5 | Thú cưỡi (cũ / VIP) | Trang bị ô 9; `UseItem` case 24 | 349:Cân đẩu vân VIP, 350:Phi Long VIP, 351:Ván bay VIP, 396:Thú cưỡi cực VIP |
| 25 | 7 | Vật phẩm đặc biệt (Sách tuyệt kỹ, Gói rada...) | Trang bị ô 10 (đệ tử: ô 8); `UseItem` case 25 | 361:Gói 10 Rađa dò ngọc, 1044:Sách tuyệt kỹ 1, 1211:Sách tuyệt kỹ 1, 1212:Sách tuyệt kỹ 1 |
| 27 | 503 | Vật phẩm hỗ trợ / sử dụng / sự kiện | Trang bị ô 7 khi mặc được; phần lớn là item dùng (capsule, thỏi vàng, đá nâng cấp, bông tai...) | 74:Đùi gà nướng, 191:Cà chua, 192:Cà rốt, 193:Gói 10 viên Capsule |
| 28 | 14 | Cờ (cờ PK) | suy ra từ tên (Tháo cờ, Cờ xanh...) | 363:Tháo cờ, 364:Cờ xanh, 365:Cờ đỏ, 366:Cờ tím |
| 29 | 77 | Item thời gian (buff) | Cuồng nộ, Bổ huyết, Bổ khí, Giáp Xên, Ẩn danh, thức ăn... (tương ứng `data_item_time`); `ConsignShopService` dòng 371 | 379:Máy dò Capsule kì bí, 381:Cuồng nộ, 382:Bổ huyết, 383:Bổ khí |
| 30 | 25 | Sao pha lê | dùng ép sao vào trang bị (suy ra từ tên) | 441:Sao pha lê đỏ, 442:Sao pha lê lam, 443:Sao pha lê hồng, 444:Sao pha lê tím |
| 31 | 9 | Bánh sự kiện | suy ra từ tên (Bánh Trung Thu, bánh chưng...) | 465:Bánh Trung Thu 1 trứng, 466:Bánh Trung Thu 2 trứng, 472:Bánh Trung Thu Đặc Biệt, 473:Hộp bánh Trung Thu |
| 32 | 8 | Giáp tập luyện | Trang bị ô 6 (`putItemBody`: case 32 → index 6) | 529:Giáp tập luyện cấp 1, 530:Giáp tập luyện cấp 2, 531:Giáp tập luyện cấp 3, 534:Giáp tập luyện cấp 1 |
| 33 | 21 | Mảnh thẻ Rada (card) | `UseItem` case 33 "card"; id trùng với `radar.id` | 828:Mảnh Khủng long, 829:Mảnh Lợn lòi, 830:Mảnh Quỷ đất, 831:Mảnh Khủng long mẹ |
| 34 | 1 | Hồng ngọc (item) | suy ra từ tên | 861:Hồng ngọc |
| 36 | 21 | Danh hiệu | id trùng `data_badges.idItem` | 1286:Kẻ thao túng sói, 1287:Nước anh bao, 1288:Chiến thần khóa nick, 1289:Đại gia mới nhú |
| 37 | 25 | Sách kỹ năng (mới) | suy ra từ tên | 1319:Sách đấm Dragon, 1320:Sách Kamejoko, 1321:Thái Dương Hạ San, 1322:Kaioken |
| 75 | 158 | Vật phẩm sự kiện / mới thêm | suy ra từ tên và mô tả "Vật phẩm sự kiện" | 1787:Vé riêng tư, 1790:Mẹ Rồng, 1794:Con dấu, 1795:Bình hút năng lượng |

**Ô trang bị (`items_body`) theo `InventoryService.putItemBody()`:** 0 áo · 1 quần · 2 găng · 3 giày · 4 rada/nhẫn · 5 cải trang · 6 giáp tập luyện (type 32) · 7 type 27 · 8 đeo lưng (type 11) · 9 thú cưỡi (type 23/24) · 10 type 25 (đệ tử: type 25 vào ô 8).

### 3.2 Nhóm vật phẩm quan trọng

#### Ngọc rồng (TYPE 12) và ngọc rồng Namek/sao đen (49)

| id | Tên | TYPE | gender | level | power_require | Mô tả |
|---|---|---|---|---|---|---|
| 14 | Ngọc Rồng 1 sao | 12 | 3 | 0 | 0 | Thu thập để ước rồng thần |
| 15 | Ngọc Rồng 2 sao | 12 | 3 | 0 | 0 | Thu thập để ước rồng thần |
| 16 | Ngọc Rồng 3 sao | 12 | 3 | 0 | 0 | Thu thập để ước rồng thần |
| 17 | Ngọc Rồng 4 sao | 12 | 3 | 0 | 0 | Thu thập để ước rồng thần |
| 18 | Ngọc Rồng 5 sao | 12 | 3 | 0 | 0 | Thu thập để ước rồng thần |
| 19 | Ngọc Rồng 6 sao | 12 | 3 | 0 | 0 | Thu thập để ước rồng thần |
| 20 | Ngọc Rồng 7 sao | 12 | 3 | 0 | 0 | Thu thập để ước rồng thần |
| 353 | Ngọc Rồng Namek 1 Sao | 11 | 3 | 1 | 0 | Tập họp đủ 7 viên để ước |
| 354 | Ngọc Rồng Namek 2 Sao | 11 | 3 | 1 | 0 | Tập họp đủ 7 viên để ước |
| 355 | Ngọc Rồng Namek 3 Sao | 11 | 3 | 1 | 0 | Tập họp đủ 7 viên để ước |
| 356 | Ngọc Rồng Namek 4 Sao | 11 | 3 | 1 | 0 | Tập họp đủ 7 viên để ước |
| 357 | Ngọc Rồng Namek 5 Sao | 11 | 3 | 1 | 0 | Tập họp đủ 7 viên để ước |
| 358 | Ngọc Rồng Namek 6 Sao | 11 | 3 | 1 | 0 | Tập họp đủ 7 viên để ước |
| 359 | Ngọc Rồng Namek 7 Sao | 11 | 3 | 1 | 0 | Tập họp đủ 7 viên để ước |
| 360 | Ngọc Rồng Namek | 11 | 3 | 1 | 0 | Tập họp đủ 7 viên để ước |
| 362 | Hóa thạch Ngọc Rồng | 11 | 3 | 1 | 0 | Hóa thạch Ngọc Rồng |
| 372 | Ngọc rồng 1 sao đen | 11 | 3 | 1 | 0 | +15% sức đánh cho toàn bang |
| 373 | Ngọc rồng 2 sao đen | 11 | 3 | 1 | 0 | +20% HP và KI tối đa cho toàn bang |
| 374 | Ngọc rồng 3 sao đen | 11 | 3 | 1 | 0 | Mỗi giờ 10 hạt đậu thần cấp 8 cho toàn bang |
| 375 | Ngọc rồng 4 sao đen | 11 | 3 | 1 | 0 | Mỗi giờ 1 bùa 1h ngẫu nhiên cho toàn bang |
| 376 | Ngọc rồng 5 sao đen | 11 | 3 | 1 | 0 | Mỗi giờ 3 ngọc nâng cấp ngẫu nhiên cho toàn bang |
| 377 | Ngọc rồng 6 sao đen | 11 | 3 | 1 | 0 | Mỗi giờ 200.000 vàng cho toàn bang |
| 378 | Ngọc rồng 7 sao đen | 11 | 3 | 1 | 0 | Mỗi giờ 2 ngọc cho toàn bang |
| 702 | Bí ngô 1 sao | 12 | 3 | 0 | 0 | Thu thập để ước Rồng Xương |
| 703 | Bí ngô 2 sao | 12 | 3 | 0 | 0 | Thu thập để ước Rồng Xương |
| 704 | Bí ngô 3 sao | 12 | 3 | 0 | 0 | Thu thập để ước Rồng Xương |
| 705 | Bí ngô 4 sao | 12 | 3 | 0 | 0 | Thu thập để ước Rồng Xương |
| 706 | Bí ngô 5 sao | 12 | 3 | 0 | 0 | Thu thập để ước Rồng Xương |
| 707 | Bí ngô 6 sao | 12 | 3 | 0 | 0 | Thu thập để ước Rồng Xương |
| 708 | Bí ngô 7 sao | 12 | 3 | 0 | 0 | Thu thập để ước Rồng Xương |
| 807 | Ngọc đen 1 sao | 12 | 3 | 0 | 0 | Thu thập để ước Rồng Xương (tự động xóa khi kết thúc sự kiện) |
| 808 | Ngọc đen 2 sao | 12 | 3 | 0 | 0 | Thu thập để ước Rồng Xương (tự động xóa khi kết thúc sự kiện) |
| 809 | Ngọc đen 3 sao | 12 | 3 | 0 | 0 | Thu thập để ước Rồng Xương (tự động xóa khi kết thúc sự kiện) |
| 810 | Ngọc đen 4 sao | 12 | 3 | 0 | 0 | Thu thập để ước Rồng Xương (tự động xóa khi kết thúc sự kiện) |
| 811 | Ngọc đen 5 sao | 12 | 3 | 0 | 0 | Thu thập để ước Rồng Xương (tự động xóa khi kết thúc sự kiện) |
| 812 | Ngọc đen 6 sao | 12 | 3 | 0 | 0 | Thu thập để ước Rồng Xương (tự động xóa khi kết thúc sự kiện) |
| 813 | Ngọc đen 7 sao | 12 | 3 | 0 | 0 | Thu thập để ước Rồng Xương (tự động xóa khi kết thúc sự kiện) |
| 925 | Ngọc rồng băng 1 sao | 12 | 3 | 0 | 0 | Thu thập để ước Rồng Băng |
| 926 | Ngọc rồng băng 2 sao | 12 | 3 | 0 | 0 | Thu thập để ước Rồng Băng |
| 927 | Ngọc rồng băng 3 sao | 12 | 3 | 0 | 0 | Thu thập để ước Rồng Băng |
| 928 | Ngọc rồng băng 4 sao | 12 | 3 | 0 | 0 | Thu thập để ước Rồng Băng |
| 929 | Ngọc rồng băng 5 sao | 12 | 3 | 0 | 0 | Thu thập để ước Rồng Băng |
| 930 | Ngọc rồng băng 6 sao | 12 | 3 | 0 | 0 | Thu thập để ước Rồng Băng |
| 931 | Ngọc rồng băng 7 sao | 12 | 3 | 0 | 0 | Thu thập để ước Rồng Băng |
| 1015 | Ngọc rồng Siêu Cấp | 27 | 3 | 1 | 0 | Thu thập để ước rồng thần |
| 1115 | Máy dò Ngọc rồng sự kiện | 29 | 3 | 0 | 0 | Dùng để tìm kiếm Ngọc rồng sự kiện |
| 1560 | Rương ngọc rồng | 27 | 3 | 0 | 0 | Bên trong ẩn chứa nhiều thứ bí ẩn |
| 1822 | Rada ngọc rồng | 75 | 3 | 0 | 0 | Vật phẩm sự kiện |
| 1823 | Rada ngọc rồng Vip | 75 | 3 | 0 | 0 | Vật phẩm sự kiện |

#### Đậu thần (20)

| id | Tên | TYPE | gender | level | power_require | Mô tả |
|---|---|---|---|---|---|---|
| 13 | Đậu thần cấp 1 | 6 | 3 | 1 | 1 | Thức ăn phục hồi HP và KI |
| 60 | Đậu thần cấp 2 | 6 | 3 | 2 | 1 | Thức ăn phục hồi HP và KI |
| 61 | Đậu thần cấp 3 | 6 | 3 | 3 | 1 | Thức ăn phục hồi HP và KI |
| 62 | Đậu thần cấp 4 | 6 | 3 | 4 | 1 | Thức ăn phục hồi HP và KI |
| 63 | Đậu thần cấp 5 | 6 | 3 | 5 | 1 | Thức ăn phục hồi HP và KI |
| 64 | Đậu thần cấp 6 | 6 | 3 | 6 | 1 | Thức ăn phục hồi HP và KI |
| 65 | Đậu thần cấp 7 | 6 | 3 | 7 | 1 | Thức ăn phục hồi HP và KI |
| 293 | Gói 30 đậu thần cấp 1 | 27 | 3 | 1 | 0 | Gói 30 đậu thần cấp 1 |
| 294 | Gói 30 đậu thần cấp 2 | 27 | 3 | 1 | 0 | Gói 30 đậu thần cấp 2 |
| 295 | Gói 30 đậu thần cấp 3 | 27 | 3 | 1 | 0 | Gói 30 đậu thần cấp 3 |
| 296 | Gói 30 đậu thần cấp 4 | 27 | 3 | 1 | 0 | Gói 30 đậu thần cấp 4 |
| 297 | Gói 30 đậu thần cấp 5 | 27 | 3 | 1 | 0 | Gói 30 đậu thần cấp 5 |
| 298 | Gói 30 đậu thần cấp 6 | 27 | 3 | 1 | 0 | Gói 30 đậu thần cấp 6 |
| 299 | Gói 30 đậu thần cấp 7 | 27 | 3 | 1 | 0 | Gói 30 đậu thần cấp 7 |
| 352 | Đậu thần cấp 8 | 6 | 3 | 8 | 1 | Thức ăn phục hồi HP và KI |
| 523 | Đậu thần cấp 9 | 6 | 3 | 9 | 1 | Thức ăn phục hồi HP và KI |
| 595 | Đậu thần cấp 10 | 6 | 3 | 10 | 1 | Thức ăn phục hồi HP và KI |
| 596 | Gói 30 đậu thần cấp 8 | 27 | 3 | 1 | 0 | Gói 30 đậu thần cấp 8 |
| 597 | Gói 30 đậu thần cấp 9 | 27 | 3 | 1 | 0 | Gói 30 đậu thần cấp 9 |
| 1715 | Đậu thần cấp 11 | 6 | 3 | 0 | 0 | Thức ăn phục hồi HP và KI |

#### Capsule (20)

| id | Tên | TYPE | gender | level | power_require | Mô tả |
|---|---|---|---|---|---|---|
| 193 | Gói 10 viên Capsule | 27 | 3 | 0 | 0 | Tàu Vận Chuyển |
| 194 | Viên Capsule đặc biệt | 27 | 3 | 0 | 0 | Tàu Vận Chuyển VIP dùng không giới hạn số lần |
| 379 | Máy dò Capsule kì bí | 29 | 3 | 1 | 0 | Dùng để tìm kiếm Capsule kì bí ở tương lai |
| 380 | Viên Capsule kì bí | 27 | 3 | 1 | 0 | Bên trong ẩn chứa nhiều thứ bí ẩn |
| 573 | Capsule Bạc | 27 | 3 | 1 | 15000 | Giấu bên trong nhiều vật phẩm quý giá |
| 574 | Capsule Vàng | 27 | 3 | 1 | 15000 | Giấu bên trong nhiều vật phẩm quý giá |
| 627 | Capsule quà tặng | 27 | 3 | 1 | 0 | Giấu bên trong nhiều vật phẩm quý giá |
| 722 | Capsule hồng | 27 | 3 | 1 | 0 | VPSK |
| 737 | Capsule Trung Thu | 27 | 3 | 1 | 0 | VPSK |
| 758 | Capsule Tết 2024 | 27 | 3 | 1 | 0 | Giấu bên trong nhiều vật phẩm quý giá |
| 796 | Hộp Capsule | 27 | 3 | 1 | 0 | Vật phẩm nhiệm vụ |
| 818 | Capsule Halloween | 27 | 3 | 1 | 0 | VPSK |
| 869 | Capsule 1 sao | 27 | 3 | 1 | 0 | Giấu bên trong nhiều vật phẩm quý giá |
| 870 | Capsule 2 sao | 27 | 3 | 1 | 0 | Giấu bên trong nhiều vật phẩm quý giá |
| 871 | Capsule 3 sao | 27 | 3 | 1 | 0 | Giấu bên trong nhiều vật phẩm quý giá |
| 915 | Capsule Squid Game | 27 | 3 | 1 | 0 | VPSK |
| 984 | Capsule SEA games | 27 | 3 | 1 | 0 | VPSK |
| 1135 | Capsule World Cup | 27 | 3 | 0 | 0 | VPSK |
| 1136 | Capsule World Cup VIP | 27 | 3 | 0 | 0 | VPSK |
| 1559 | Capsule 1 món kích hoạt | 27 | 3 | 0 | 0 | Chứa trang bị kích hoạt bí ẩn |

#### Sao pha lê (TYPE 30) (25)

| id | Tên | TYPE | gender | level | power_require | Mô tả |
|---|---|---|---|---|---|---|
| 441 | Sao pha lê đỏ | 30 | 3 | 1 | 1 | Ép vào đồ sao pha lê để tăng chỉ số |
| 442 | Sao pha lê lam | 30 | 3 | 2 | 1 | Ép vào đồ sao pha lê để tăng chỉ số |
| 443 | Sao pha lê hồng | 30 | 3 | 3 | 1 | Ép vào đồ sao pha lê để tăng chỉ số |
| 444 | Sao pha lê tím | 30 | 3 | 4 | 1 | Ép vào đồ sao pha lê để tăng chỉ số |
| 445 | Sao pha lê cam | 30 | 3 | 5 | 1 | Ép vào đồ sao pha lê để tăng chỉ số |
| 446 | Sao pha lê vàng | 30 | 3 | 6 | 1 | Ép vào đồ sao pha lê để tăng chỉ số |
| 447 | Sao pha lê lục | 30 | 3 | 7 | 1 | Ép vào đồ sao pha lê để tăng chỉ số |
| 964 | Sao pha lê đen cấp 2 | 30 | 3 | 8 | 1 | Ép vào đồ sao pha lê để tăng chỉ số |
| 965 | Sao pha lê trắng cấp 2 | 30 | 3 | 9 | 1 | Ép vào đồ sao pha lê để tăng chỉ số |
| 1416 | Sao pha lê đỏ cấp 2 | 30 | 3 | 0 | 0 | Ép vào đồ sao pha lê để tăng chỉ số |
| 1417 | Sao pha lê lam cấp 2 | 30 | 3 | 0 | 0 | Ép vào đồ sao pha lê để tăng chỉ số |
| 1418 | Sao pha lê hồng cấp 2 | 30 | 3 | 0 | 0 | Ép vào đồ sao pha lê để tăng chỉ số |
| 1419 | Sao pha lê tím cấp 2 | 30 | 3 | 0 | 0 | Ép vào đồ sao pha lê để tăng chỉ số |
| 1420 | Sao pha lê cam cấp 2 | 30 | 3 | 0 | 0 | Ép vào đồ sao pha lê để tăng chỉ số |
| 1421 | Sao pha lê vàng cấp 2 | 30 | 3 | 0 | 0 | Ép vào đồ sao pha lê để tăng chỉ số |
| 1422 | Sao pha lê lục cấp 2 | 30 | 3 | 0 | 0 | Ép vào đồ sao pha lê để tăng chỉ số |
| 1426 | Sao pha lê đỏ lấp lánh | 30 | 3 | 0 | 0 | Ép vào đồ sao pha lê để tăng chỉ số |
| 1427 | Sao pha lê lam lấp lánh | 30 | 3 | 0 | 0 | Ép vào đồ sao pha lê để tăng chỉ số |
| 1428 | Sao pha lê hồng lấp lánh | 30 | 3 | 0 | 0 | Ép vào đồ sao pha lê để tăng chỉ số |
| 1429 | Sao pha lê tím lấp lánh | 30 | 3 | 0 | 0 | Ép vào đồ sao pha lê để tăng chỉ số |
| 1430 | Sao pha lê cam lấp lánh | 30 | 3 | 0 | 0 | Ép vào đồ sao pha lê để tăng chỉ số |
| 1431 | Sao pha lê vàng lấp lánh | 30 | 3 | 0 | 0 | Ép vào đồ sao pha lê để tăng chỉ số |
| 1432 | Sao pha lê lục lấp lánh | 30 | 3 | 0 | 0 | Ép vào đồ sao pha lê để tăng chỉ số |
| 1433 | Sao pha lê đen lấp lánh | 30 | 3 | 0 | 0 | Ép vào đồ sao pha lê để tăng chỉ số |
| 1434 | Sao pha lê trắng lấp lánh | 30 | 3 | 0 | 0 | Ép vào đồ sao pha lê để tăng chỉ số |

#### Đá nâng cấp / đá quý (21)

Code: `Item.isDaNangCap()` = id 1074–1078.

| id | Tên | TYPE | gender | level | power_require | Mô tả |
|---|---|---|---|---|---|---|
| 220 | Đá lục bảo | 14 | 3 | 0 | 0 | Gặp bà Hạt Mít để sử dụng |
| 221 | Đá Saphia | 14 | 3 | 0 | 0 | Gặp bà Hạt Mít để sử dụng |
| 222 | Đá Ruby | 14 | 3 | 0 | 0 | Gặp bà Hạt Mít để sử dụng |
| 223 | Đá Titan | 14 | 3 | 0 | 0 | Gặp bà Hạt Mít để sử dụng |
| 224 | Đá thạch anh tím | 14 | 3 | 0 | 0 | Gặp bà Hạt Mít để sử dụng |
| 225 | Mảnh đá vụn | 15 | 3 | 0 | 0 | Gặp bà Hạt Mít để sử dụng |
| 674 | Đá ngũ sắc | 27 | 3 | 1 | 0 | Loại đá quý hiếm nhất trong thế giới Ngọc Rồng. Dùng để đục lỗ để gắn Sao P |
| 935 | Đá xanh lam | 27 | 3 | 1 | 0 | Dùng để nâng cấp bông tai |
| 987 | Đá bảo vệ | 27 | 3 | 1 | 0 | Bảo vệ trang bị không bị rớt cấp |
| 1074 | Đá nâng cấp cấp 1 | 27 | 3 | 1 | 0 | Vật phẩm hổ trợ |
| 1075 | Đá nâng cấp cấp 2 | 27 | 3 | 2 | 0 | Vật phẩm hổ trợ |
| 1076 | Đá nâng cấp cấp 3 | 27 | 3 | 3 | 0 | Vật phẩm hổ trợ |
| 1077 | Đá nâng cấp cấp 4 | 27 | 3 | 4 | 0 | Vật phẩm hổ trợ |
| 1078 | Đá nâng cấp cấp 5 | 27 | 3 | 5 | 0 | Vật phẩm hổ trợ |
| 1079 | Đá may mắn cấp 1 | 27 | 3 | 1 | 0 | Vật phẩm hổ trợ |
| 1080 | Đá may mắn cấp 2 | 27 | 3 | 2 | 0 | Vật phẩm hổ trợ |
| 1081 | Đá may mắn cấp 3 | 27 | 3 | 3 | 0 | Vật phẩm hổ trợ |
| 1082 | Đá may mắn cấp 4 | 27 | 3 | 4 | 0 | Vật phẩm hổ trợ |
| 1083 | Đá may mắn cấp 5 | 27 | 3 | 5 | 0 | Vật phẩm hổ trợ |
| 1143 | Đá bảo vệ (Khóa) | 27 | 3 | 0 | 0 | Bảo vệ trang bị không bị rớt cấp |
| 1439 | Đá mài | 27 | 3 | 0 | 0 | Vật phẩm hổ trợ. Gặp bà Hạt Mít để sử dụng |

#### Trang bị Thần Linh (13)

Code: `Item.isDTL()` / `Item.isThanLinh()` = id 555–567.

| id | Tên | TYPE | gender | level | power_require | Mô tả |
|---|---|---|---|---|---|---|
| 555 | Áo Thần Linh | 0 | 0 | 13 | 0 | Giúp giảm sát thương |
| 556 | Quần Thần Linh | 1 | 0 | 13 | 0 | Giúp bạn tăng HP |
| 557 | Áo Thần Namếc | 0 | 1 | 13 | 0 | Giúp giảm sát thương |
| 558 | Quần Thần namếc | 1 | 1 | 13 | 0 | Giúp bạn tăng HP |
| 559 | Áo Thần Xayda | 0 | 2 | 13 | 0 | Giúp giảm sát thương |
| 560 | Quần Thần Xayda | 1 | 2 | 13 | 0 | Giúp bạn tăng HP |
| 561 | Nhẫn Thần Linh | 4 | 3 | 13 | 0 | Giúp tăng Chí Mạng |
| 562 | Găng Thần Linh | 2 | 0 | 13 | 0 | Giúp bạn tăng sức đánh |
| 563 | Giầy Thần Linh | 3 | 0 | 13 | 0 | Giúp bạn tăng MP |
| 564 | Găng Thần Namếc | 2 | 1 | 13 | 0 | Giúp bạn tăng sức đánh |
| 565 | Giầy Thần Namếc | 3 | 1 | 13 | 0 | Giúp bạn tăng MP |
| 566 | Găng Thần Xayda | 2 | 2 | 13 | 0 | Giúp bạn tăng sức đánh |
| 567 | Giầy Thần Xayda | 3 | 2 | 13 | 0 | Giúp bạn tăng MP |

#### Trang bị Hủy Diệt (13)

Code: `Item.isDHD()` = id 650–662.

| id | Tên | TYPE | gender | level | power_require | Mô tả |
|---|---|---|---|---|---|---|
| 650 | Áo Hủy Diệt | 0 | 0 | 14 | 0 | Giúp giảm sát thương |
| 651 | Quần Hủy Diệt | 1 | 0 | 14 | 0 | Giúp bạn tăng HP |
| 652 | Áo Hủy Diệt | 0 | 1 | 14 | 0 | Giúp giảm sát thương |
| 653 | Quần Hủy Diệt | 1 | 1 | 14 | 0 | Giúp bạn tăng HP |
| 654 | Áo Hủy Diệt | 0 | 2 | 14 | 0 | Giúp giảm sát thương |
| 655 | Quần Hủy Diệt | 1 | 2 | 14 | 0 | Giúp bạn tăng HP |
| 656 | Nhẫn Hủy Diệt | 4 | 3 | 14 | 0 | Giúp tăng Chí Mạng |
| 657 | Găng Hủy Diệt | 2 | 0 | 14 | 0 | Giúp bạn tăng sức đánh |
| 658 | Giầy Hủy Diệt | 3 | 0 | 14 | 0 | Giúp bạn tăng MP |
| 659 | Găng Hủy Diệt | 2 | 1 | 14 | 0 | Giúp bạn tăng sức đánh |
| 660 | Giầy Hủy Diệt | 3 | 1 | 14 | 0 | Giúp bạn tăng MP |
| 661 | Găng Hủy Diệt | 2 | 2 | 14 | 0 | Giúp bạn tăng sức đánh |
| 662 | Giầy Hủy Diệt | 3 | 2 | 14 | 0 | Giúp bạn tăng MP |

#### Trang bị Thiên Sứ và mảnh Thiên Sứ (20)

Code: `Item.isDTS()` = id 1048–1062; `Item.isManhTS()` = id 1066–1070.

| id | Tên | TYPE | gender | level | power_require | Mô tả |
|---|---|---|---|---|---|---|
| 1048 | Áo Thiên Sứ | 0 | 0 | 15 | 0 | Giúp giảm sát thương |
| 1049 | Áo Thiên Sứ | 0 | 1 | 15 | 0 | Giúp giảm sát thương |
| 1050 | Áo Thiên Sứ | 0 | 2 | 15 | 0 | Giúp giảm sát thương |
| 1051 | Quần Thiên Sứ | 1 | 0 | 15 | 0 | Giúp bạn tăng HP |
| 1052 | Quần Thiên Sứ | 1 | 1 | 15 | 0 | Giúp bạn tăng HP |
| 1053 | Quần Thiên Sứ | 1 | 2 | 15 | 0 | Giúp bạn tăng HP |
| 1054 | Găng Thiên Sứ | 2 | 0 | 15 | 0 | Giúp bạn tăng sức đánh |
| 1055 | Găng Thiên Sứ | 2 | 1 | 15 | 0 | Giúp bạn tăng sức đánh |
| 1056 | Găng Thiên Sứ | 2 | 2 | 15 | 0 | Giúp bạn tăng sức đánh |
| 1057 | Giầy Thiên Sứ | 3 | 0 | 15 | 0 | Giúp bạn tăng MP |
| 1058 | Giầy Thiên Sứ | 3 | 1 | 15 | 0 | Giúp bạn tăng MP |
| 1059 | Giầy Thiên Sứ | 3 | 2 | 15 | 0 | Giúp bạn tăng MP |
| 1060 | Nhẫn Thiên Sứ | 4 | 0 | 15 | 0 | Giúp tăng Chí Mạng |
| 1061 | Nhẫn Thiên Sứ | 4 | 1 | 15 | 0 | Giúp tăng Chí Mạng |
| 1062 | Nhẫn Thiên Sứ | 4 | 2 | 15 | 0 | Giúp tăng Chí Mạng |
| 1066 | Mảnh áo | 27 | 3 | 1 | 0 | Thu thập đủ 999 đến gặp Whis tại hành tinh Bill |
| 1067 | Mảnh quần | 27 | 3 | 1 | 0 | Thu thập đủ 999 đến gặp Whis tại hành tinh Bill |
| 1068 | Mảnh giầy | 27 | 3 | 1 | 0 | Thu thập đủ 999 đến gặp Whis tại hành tinh Bill |
| 1069 | Mảnh nhẫn | 27 | 3 | 1 | 0 | Thu thập đủ 999 đến gặp Whis tại hành tinh Bill |
| 1070 | Mảnh găng tay | 27 | 3 | 1 | 0 | Thu thập đủ 999 đến gặp Whis tại hành tinh Bill |

#### Bùa (TYPE 13) (14)

| id | Tên | TYPE | gender | level | power_require | Mô tả |
|---|---|---|---|---|---|---|
| 213 | Bùa Trí Tuệ | 13 | 3 | 0 | 0 | Tiềm năng và sức mạnh của bạn sẽ nhận được gấp đôi trong 1 khoảng thời gian |
| 214 | Bùa Mạnh Mẽ | 13 | 3 | 0 | 0 | Cú đấm của bạn sẽ mạnh hơn. Tăng 150% sức đánh hiện có khi bạn đánh Quái tr |
| 215 | Bùa Da Trâu | 13 | 3 | 0 | 0 | Tăng sức chịu đòn cho bạn. Khi bị quái đánh, máu sẽ mất ít hơn, chỉ còn 50% |
| 216 | Bùa Oai Hùng | 13 | 3 | 0 | 0 | Bạn sẽ oai vệ hơn. Quái Thủ Lĩnh sẽ sợ bạn và đánh bạn yếu đi, y như con qu |
| 217 | Bùa Bất Tử | 13 | 3 | 0 | 0 | Bạn sẽ không bao giờ bị quái đánh chết. Thay vào đó chỉ còn 1 máu. Tuy nhiê |
| 218 | Bùa Dẻo Dai | 13 | 3 | 0 | 0 | Thể lực của bạn sẽ không bao giờ giảm khi bùa này có tác dụng. Có tác dụng |
| 219 | Bùa Thu Hút | 13 | 3 | 0 | 0 | Vật phẩm bạn đánh văng ra nếu là của bạn, nó sẽ tự bay vào người. Bạn sẽ kh |
| 522 | Bùa Đệ Tử | 13 | 3 | 0 | 0 | Đệ tử bạn sẽ tự đánh quái không cần sư phụ, giảm 50% sát thương từ quái, tă |
| 671 | Bùa Trí Tuệ x3 | 13 | 3 | 0 | 0 | Tiềm năng và sức mạnh của bạn sẽ nhận được gấp ba trong 1 khoảng thời gian, |
| 672 | Bùa Trí Tuệ x4 | 13 | 3 | 0 | 0 | Tiềm năng và sức mạnh của bạn sẽ nhận được gấp bốn trong 1 khoảng thời gian |
| 797 | Bùa Trí Tuệ | 13 | 3 | 0 | 0 | Vật phẩm bang hội |
| 798 | Bùa Mạnh Mẽ | 13 | 3 | 0 | 0 | Vật phẩm bang hội |
| 799 | Bùa Da Trâu | 13 | 3 | 0 | 0 | Vật phẩm bang hội |
| 1522 | Bùa Trí tuệ Đệ Tử | 13 | 3 | 0 | 0 | Tiềm năng và sức mạnh của đệ tử bạn sẽ nhận được g... |

#### Item thời gian / buff (TYPE 29) (77)

| id | Tên | TYPE | gender | level | power_require | Mô tả |
|---|---|---|---|---|---|---|
| 379 | Máy dò Capsule kì bí | 29 | 3 | 1 | 0 | Dùng để tìm kiếm Capsule kì bí ở tương lai |
| 381 | Cuồng nộ | 29 | 3 | 1 | 0 | Trong vòng tối đa 10 phút +100% sức đánh gốc |
| 382 | Bổ huyết | 29 | 3 | 1 | 0 | Trong vòng tối đa 10 phút +100% HP |
| 383 | Bổ khí | 29 | 3 | 1 | 0 | Trong vòng tối đa 10 phút +100% KI |
| 384 | Giáp Xên bọ hung | 29 | 3 | 1 | 0 | Trong vòng tối đa 10 phút giảm 50% sát thương |
| 385 | Ẩn danh | 29 | 3 | 1 | 0 | +10 phút (tối đa 30 phút) hạ địch không để lại tên trong danh sách kẻ thù v |
| 579 | Đuôi khỉ | 29 | 3 | 1 | 15000 | VPSK, biến mất vào ngày 5/10 |
| 663 | Bánh Pudding | 29 | 3 | 14 | 0 | Thu thập đủ 99 đến gặp Bill tại hành tinh Kaio, hoặc ăn để tăng sức đánh ch |
| 664 | Xúc xích | 29 | 3 | 14 | 0 | Thu thập đủ 99 đến gặp Bill tại hành tinh Kaio, hoặc ăn để tăng sức đánh ch |
| 665 | Kem dâu | 29 | 3 | 14 | 0 | Thu thập đủ 99 đến gặp Bill tại hành tinh Kaio, hoặc ăn để tăng sức đánh ch |
| 666 | Mì ly | 29 | 3 | 14 | 0 | Thu thập đủ 99 đến gặp Bill tại hành tinh Kaio, hoặc ăn để tăng sức đánh ch |
| 667 | Sushi | 29 | 3 | 14 | 0 | Thu thập đủ 99 đến gặp Bill tại hành tinh Kaio, hoặc ăn để tăng sức đánh ch |
| 669 | Dưa Hấu | 29 | 3 | 1 | 0 |  |
| 670 | Cà rốt | 29 | 3 | 1 | 0 |  |
| 694 | Trái dừa | 29 | 3 | 1 | 0 | VPSK |
| 764 | Khẩu trang | 29 | 3 | 1 | 0 | VPSK |
| 880 | Cua rang me | 29 | 3 | 14 | 0 | Tặng cho Whis hoặc ăn vào để tăng 5% sức đánh trong vòng 10 phút |
| 881 | Bạch tuộc nướng | 29 | 3 | 14 | 0 | Tặng cho Whis hoặc ăn vào để tăng 5% sát thương khi chí mạng trong vòng 10 |
| 882 | Tôm tẩm bột chiên xù | 29 | 3 | 14 | 0 | Tặng cho Whis hoặc ăn vào để tăng 5% HP trong vòng 10 phút |
| 899 | Kẹo một mắt | 29 | 3 | 1 | 0 | VPSK. Ăn vào +5% chí mạng trong vòng 30 phút. |
| 900 | Súp bí hắc ám | 29 | 3 | 1 | 0 | VPSK. Ăn vào +10% HP trong vòng 30 phút. |
| 902 | Bánh gato nhện | 29 | 3 | 1 | 0 | VPSK. Ăn vào +10% giáp trong vòng 30 phút. |
| 903 | Hamburger sâu | 29 | 3 | 1 | 0 | VPSK. Ăn vào +10% sức đánh trong vòng 30 phút. |
| 1016 | Thuốc mỡ Ipana | 29 | 3 | 1 | 0 | Trong vòng tối đa 10 phút +10% sức đánh gốc và hồi... |
| 1017 | Thuốc mỡ Ipana đặc biệt | 29 | 3 | 1 | 0 | Trong vòng tối đa 30 phút +10% sức đánh gốc và hồi... |
| 1115 | Máy dò Ngọc rồng sự kiện | 29 | 3 | 0 | 0 | Dùng để tìm kiếm Ngọc rồng sự kiện |
| 1150 | Cuồng nộ 2 | 29 | 3 | 0 | 0 | Trong vòng tối đa 10 phút +120% sức đánh gốc |
| 1151 | Bổ khí 2 | 29 | 3 | 0 | 0 | Trong vòng tối đa 10 phút +120% KI |
| 1152 | Bổ huyết 2 | 29 | 3 | 0 | 0 | Trong vòng tối đa 10 phút +120% HP |
| 1153 | Giáp Xên bọ hung 2 | 29 | 3 | 0 | 0 | Trong vòng tối đa 10 phút giảm 60% sát thương |
| 1154 | Ẩn danh 2 | 29 | 3 | 0 | 0 | Khi bạn cần sự riêng tư +10 phút (tối đa 40 phút) ... |
| 1189 | Xúc xích xông khói | 29 | 3 | 0 | 0 | VPSK |
| 1190 | Dĩa thức ăn cho mèo | 29 | 3 | 0 | 0 | VPSK |
| 1195 | Xí muội Hoa đào | 29 | 3 | 0 | 0 | Trong vòng tối đa 10 phút +20% sức đánh gốc |
| 1196 | Xí muội Hoa mai | 29 | 3 | 0 | 0 | Trong vòng tối đa 10 phút +20% HP |
| 1232 | Bình chứa nhỏ | 29 | 1 | 0 | 0 | Trong vòng tối đa 30 phút +1 nạn nhân bị Ma phong ... |
| 1233 | Nồi cơm điện | 29 | 1 | 0 | 0 | Trong vòng tối đa 30 phút +3 nạn nhân bị Ma phong ... |
| 1264 | Máy dò linh hồn | 29 | 3 | 0 | 0 | VPSK |
| 1397 | Rương radar | 29 | 3 | 0 | 0 | Ngẫu nhiên nhận Rada theo cấp độ tự chọn |
| 1398 | Rương giày | 29 | 3 | 0 | 0 | Ngẫu nhiên nhận giày theo cấp độ tự chọn |
| 1399 | Rương nhẫn | 29 | 3 | 0 | 0 | Ngẫu nhiên nhận nhẫn theo cấp độ tự chọn |
| 1400 | Rương găng tay | 29 | 3 | 0 | 0 | Ngẫu nhiên nhận găng tay theo cấp độ tự chọn |
| 1401 | Rương áo | 29 | 3 | 0 | 0 | Ngẫu nhiên nhận áo theo cấp độ tự chọn |
| 1402 | Rương quần | 29 | 3 | 0 | 0 | Ngẫu nhiên nhận quần theo cấp độ tự chọn |
| 1403 | Rương bí kíp tuyệt kỹ | 29 | 3 | 0 | 0 | Mở ra ngẫu nhiên nhận 1 sách kỹ năng |
| 1404 | Chí mạng 2 | 29 | 3 | 0 | 0 | Ăn vào +10% chí mạng trong vòng 10 phút. |
| 1405 | Chí mạng 3 | 29 | 3 | 0 | 0 | Ăn vào +15% chí mạng trong vòng 10 phút. |
| 1406 | Né đòn | 29 | 3 | 0 | 0 | +10% né đòn trong 30 giây |
| 1407 | Né đòn 2 | 29 | 3 | 0 | 0 | +20% né đòn trong 30 giây |
| 1408 | Hồi skill | 29 | 3 | 0 | 0 | Giảm thời gian cooldown skill thành 0,1 giây |
| 1409 | Phản sát thương | 29 | 3 | 0 | 0 | Phản sát thương 3% trong 30 giây |
| 1410 | Phản sát thương 2 | 29 | 3 | 0 | 0 | Phản sát thương 6% trong 30 giây |
| 1411 | Phản sát thương 3 | 29 | 3 | 0 | 0 | Phản sát thương 12% trong 30 giây |
| 1412 | Kamejoko | 29 | 3 | 0 | 0 | Trong 30 giây +10% phạm vi sát thương của kỹ năng ... |
| 1413 | Kamejoko 2 | 29 | 3 | 0 | 0 | Trong 30 giây +20% phạm vi sát thương của kỹ năng ... |
| 1425 | Anti phong ấn | 29 | 3 | 0 | 0 | Giảm 1s thời gian phong ấn của Ma phong ba |
| 1480 | Mì thanh long | 29 | 3 | 0 | 0 | VPSK |
| 1481 | Cơm gà quay | 29 | 3 | 0 | 0 | VPSK |
| 1517 | Sát thương chuẩn | 29 | 3 | 0 | 0 | Trong vòng tối đa 10 phút có cơ hội gây sát thương... |
| 1518 | Sát thương chuẩn 2 | 29 | 3 | 0 | 0 | Trong vòng tối đa 10 phút có cơ hội gây sát thương... |
| 1532 | Rađa kho báu | 29 | 3 | 0 | 0 | Trong vòng 15 phút, tiềm năng và sức mạnh nhận đượ... |
| 1536 | Đệ Black Goku | 29 | 3 | 0 | 0 | Vật phẩm sự kiện |
| 1537 | Hộp thần linh | 29 | 3 | 0 | 0 | Vật phẩm sự kiện |
| 1552 | Xiên nướng | 29 | 3 | 0 | 0 | Trong vòng tối đa 10 phút có cơ hội gây sát thương... |
| 1614 | Ly mía khổng lồ | 29 | 3 | 0 | 0 | Hiệu lực 10 phút Chống Nóng + 10% HP |
| 1615 | Ly mía thơm | 29 | 3 | 0 | 0 | Hiệu lực 10 phút Chống Nóng + 10% HP + 10% Chí Mạn... |
| 1616 | Ly mía sầu riêng | 29 | 3 | 0 | 0 | Hiệu lực 10 phút Chống Nóng + 10% HP + 10% Giảm sá... |
| 1628 | Bùa x2 tn,sm đệ tử | 29 | 3 | 0 | 0 | Có thể cộng dồn thời gian |
| 1635 | Cỏ bốn lá | 29 | 3 | 0 | 0 | Trong 30 phút tăng 20% tỉ lệ SKH |
| 1672 | Sầu riêng 6 múi | 29 | 3 | 0 | 0 | Trong vòng tối đa 10 phút có cơ hội gây sát thương... |
| 1758 | Phở Linh Lang | 29 | 3 | 0 | 1500000 | Đổi Skill 2 Đệ Tử |
| 1759 | Phở Giải Phóng | 29 | 3 | 0 | 1500000 | Đổi Skill 3 Đệ Tử |
| 1760 | Phở Công viên Hòa Bình | 29 | 3 | 0 | 1500000 | Đổi Skill 4 Đệ Tử |
| 1774 | Đệ Black Goku | 29 | 3 | 0 | 0 | Vật phẩm sự kiện |
| 1775 | Hộp thần linh | 29 | 3 | 0 | 0 | Vật phẩm sự kiện |
| 1776 | Hộp quà sự kiện | 29 | 3 | 0 | 0 | Vật phẩm sự kiện |
| 1777 | Hộp quà sự kiện VIP | 29 | 3 | 0 | 0 | Vật phẩm sự kiện |

#### Thú cưỡi (TYPE 23, 24) (55)

| id | Tên | TYPE | gender | level | power_require | Mô tả |
|---|---|---|---|---|---|---|
| 346 | Cân đẩu vân | 23 | 0 | 1 | 0 | Bay không tốn KI |
| 347 | Phi Long | 23 | 1 | 1 | 0 | Bay không tốn KI |
| 348 | Ván bay | 23 | 2 | 1 | 0 | Bay không tốn KI |
| 349 | Cân đẩu vân VIP | 24 | 0 | 1 | 0 | Bay phục hồi KI |
| 350 | Phi Long VIP | 24 | 1 | 1 | 0 | Bay phục hồi KI |
| 351 | Ván bay VIP | 24 | 2 | 1 | 0 | Bay phục hồi KI |
| 396 | Thú cưỡi cực VIP | 24 | 3 | 1 | 0 | Dùng để bay phục hồi HP, KI |
| 532 | Quỷ Chim | 24 | 3 | 1 | 15000000 | Dùng để bay phục hồi HP, KI |
| 733 | Cân đẩu vân ngũ sắc | 23 | 3 | 1 | 0 | Bay không tốn KI |
| 734 | Ngọc Thố | 23 | 3 | 1 | 0 | Bay không tốn KI |
| 735 | Lồng đèn cá chép | 23 | 3 | 1 | 0 | Bay không tốn KI |
| 743 | Chổi bay Phù Thủy | 23 | 3 | 1 | 0 | Bay không tốn KI |
| 744 | Cột nhà | 23 | 3 | 1 | 0 | Bay không tốn KI |
| 746 | Xe tuần lộc | 23 | 3 | 1 | 0 | Bay không tốn KI |
| 795 | Ghế bay | 23 | 3 | 1 | 0 | Bay không tốn KI |
| 849 | Pháo Thăng Thiên | 23 | 3 | 1 | 0 | Bay không tốn KI |
| 897 | Rùa bay | 23 | 3 | 1 | 0 | Dùng để bay không tốn Mp |
| 920 | Gậy như ý | 23 | 3 | 1 | 0 | VPSK. Dùng để bay phục hồi HP, KI |
| 1092 | Gậy Quy Lão | 23 | 3 | 1 | 0 | VPSK. Dùng để bay phục hồi HP, KI |
| 1131 | Quả bóng siêu việt | 23 | 3 | 0 | 0 | VPSK |
| 1144 | Phượng hoàng lửa | 23 | 3 | 0 | 0 | VPSK |
| 1172 | Xe heo tuần lộc | 23 | 3 | 0 | 0 | VPSK |
| 1252 | Ve Sầu Xên | 23 | 3 | 0 | 0 | VPSK |
| 1253 | Ve Sầu Xên Tiến Hóa | 23 | 3 | 0 | 0 | VPSK |
| 1272 | Đài sen hồng | 23 | 3 | 0 | 0 | VPSK |
| 1273 | Đài sen vàng | 23 | 3 | 0 | 0 | VPSK |
| 1345 | Thú cưỡi mèo kéo xương | 23 | 3 | 0 | 0 | VPSK |
| 1346 | Thú cưỡi Xe bí ngô | 23 | 3 | 0 | 0 | VPSK |
| 1363 | Thú cưỡi con Cat hường | 23 | 3 | 0 | 0 | VPSK |
| 1443 | Phượng hoàng băng | 23 | 3 | 0 | 0 | VPSK |
| 1455 | Quả bóng tuyết | 23 | 3 | 0 | 0 | VPSK |
| 1465 | Máy bay trực thăng Noel | 23 | 3 | 0 | 0 | VPSK |
| 1466 | Tuần lộc Machine | 23 | 3 | 0 | 0 | VPSK |
| 1468 | Thú cưỡi Rồng PiLong | 23 | 3 | 0 | 0 | VPSK |
| 1477 | Thỏi vàng bay | 23 | 3 | 0 | 0 | VPSK |
| 1487 | Cá chép rồng | 23 | 3 | 0 | 0 | VPSK |
| 1513 | Song mã hoàng gia | 23 | 3 | 0 | 0 | VPSK |
| 1534 | Thuyền Âu Lạc | 23 | 3 | 0 | 0 | Tăng 5% giảm sát thương khi bay cùng với Mị Nương |
| 1541 | Môtô Bun ma | 23 | 3 | 0 | 0 | VPSK |
| 1554 | Tàu ngầm 19 Cam | 23 | 3 | 0 | 0 | VPSK |
| 1555 | Tàu ngầm 19 Vàng | 23 | 3 | 0 | 0 | VPSK |
| 1563 | Ván bay té nước | 23 | 3 | 0 | 0 | VPSK |
| 1578 | Mây mưa | 23 | 3 | 0 | 0 | VPSK |
| 1598 | Trực thăng thỏ đế | 23 | 3 | 0 | 0 | VPSK |
| 1603 | Tên lửa cá mập | 23 | 3 | 0 | 0 | VPSK |
| 1625 | Tivi bay | 23 | 3 | 0 | 0 | VPSK |
| 1676 | Phong hỏa luân | 23 | 3 | 0 | 0 | Đi cùng Hỏa Tiêm Thương +5% sức đánh, HP |
| 1677 | Xe xanh Chi Chi | 23 | 3 | 0 | 0 | VPSK |
| 1678 | Xe đỏ Bun ma | 23 | 3 | 0 | 0 | VPSK |
| 1704 | Bí Ngô Cánh Dơi | 23 | 3 | 0 | 0 | VPSK |
| 1711 | Cân Đẩu Vân Thơ Mộng | 23 | 3 | 0 | 0 | Đi cùng Chi Chi Võ Đài: +5% Giảm Sát Thương |
| 1724 | Ván bay Sọ Dừa | 23 | 3 | 0 | 0 | Đi Cùng cánh Thiên thần - Ác quỷ: Tăng 3% sức đánh... |
| 1733 | Thú cưỡi Thích Kim Quy | 23 | 3 | 0 | 0 | VPSK |
| 1734 | Thú cưỡi Phong Xích Lan | 23 | 3 | 0 | 0 | VPSK |
| 1749 | Thú cưỡi Cây thông | 23 | 3 | 0 | 0 | Đi cùng Diều Rồng Băng tăng 4% giảm sát thương |

#### Danh hiệu (TYPE 36) (21)

| id | Tên | TYPE | gender | level | power_require | Mô tả |
|---|---|---|---|---|---|---|
| 1286 | Kẻ thao túng sói | 36 | 3 | 0 | 0 | Danh hiệu |
| 1287 | Nước anh bao | 36 | 3 | 0 | 0 | Danh hiệu |
| 1288 | Chiến thần khóa nick | 36 | 3 | 0 | 0 | Danh hiệu |
| 1289 | Đại gia mới nhú | 36 | 3 | 0 | 0 | Danh hiệu |
| 1290 | Trùm ước rồng | 36 | 3 | 0 | 0 | Danh hiệu |
| 1291 | Trùm săn Boss | 36 | 3 | 0 | 0 | Danh hiệu |
| 1292 | Thánh đập đồ +7 | 36 | 3 | 0 | 0 | Danh hiệu |
| 1293 | Cao thủ siêu hạng | 36 | 3 | 0 | 0 | Danh hiệu |
| 1294 | Nông dân chăm chỉ | 36 | 3 | 0 | 0 | Danh hiệu |
| 1295 | Ông thần ve chai | 36 | 3 | 0 | 0 | Danh hiệu |
| 1296 | Bị móc sạch túi | 36 | 3 | 0 | 0 | Danh hiệu |
| 1297 | KOL | 36 | 3 | 0 | 0 | Danh hiệu |
| 1298 | Chuyên gia lượm nhặt | 36 | 3 | 0 | 0 | Danh hiệu |
| 1299 | Fan cứng | 36 | 3 | 0 | 0 | Danh hiệu |
| 1300 | Thánh ở dơ | 36 | 3 | 0 | 0 | Danh hiệu |
| 1392 | Gõ đầu trẻ | 36 | 3 | 0 | 0 | Danh hiệu |
| 1393 | Gõ đầu trẻ | 36 | 3 | 0 | 0 | Danh hiệu |
| 1394 | Gõ đầu trẻ | 36 | 3 | 0 | 0 | Danh hiệu |
| 1457 | Danh hiệu X-mas | 36 | 3 | 0 | 0 | Danh hiệu |
| 1514 | Em xinh, em đẹp | 36 | 3 | 0 | 0 | Danh hiệu |
| 1673 | Tay nhanh hơn não | 36 | 3 | 0 | 0 | Danh hiệu |

#### Một số item dùng quan trọng khác (xử lý riêng trong UseItem.java) (22)

Id lấy từ các `case` trong `nro/models/services_func/UseItem.java` (dòng ~650–915) và `PlayerDAO.createNewPlayer()` (thỏi vàng 457 xuất hiện trong giftcode `tanthu`).

| id | Tên | TYPE | gender | level | power_require | Mô tả |
|---|---|---|---|---|---|---|
| 193 | Gói 10 viên Capsule | 27 | 3 | 0 | 0 | Tàu Vận Chuyển |
| 194 | Viên Capsule đặc biệt | 27 | 3 | 0 | 0 | Tàu Vận Chuyển VIP dùng không giới hạn số lần |
| 380 | Viên Capsule kì bí | 27 | 3 | 1 | 0 | Bên trong ẩn chứa nhiều thứ bí ẩn |
| 401 | Đổi đệ tử | 27 | 3 | 1 | 0 | Đổi đệ tử khác hoàn toàn mới |
| 402 | Nâng kỹ năng 1 đệ tử | 27 | 3 | 1 | 0 | Nâng chiêu 1 của đệ tử lên 1 cấp |
| 403 | Nâng kỹ năng 2 đệ tử | 27 | 3 | 1 | 0 | Nâng chiêu 2 của đệ tử lên 1 cấp |
| 404 | Nâng kỹ năng 3 đệ tử | 27 | 3 | 1 | 0 | Nâng chiêu 3 của đệ tử lên 1 cấp |
| 454 | Bông tai Porata | 27 | 3 | 1 | 1500000 | Sử dụng để hợp thể với đệ tử |
| 457 | Thỏi vàng | 27 | 3 | 1 | 1500000 | VPSK |
| 521 | Tự động luyện tập | 27 | 3 | 1 | 0 | Tự động luyện tập, ăn đậu, thu hoạch đậu, và sử dụng các kĩ năng hiển thị t |
| 568 | Quả Trứng | 27 | 3 | 1 | 0 |  |
| 759 | Nâng kỹ năng 4 đệ tử | 27 | 3 | 1 | 0 | Nâng chiêu 4 của đệ tử lên 1 cấp |
| 861 | Hồng ngọc | 34 | 3 | 1 | 0 |  |
| 921 | Bông tai Porata | 27 | 3 | 1 | 1500000 | Sử dụng để hợp thể với đệ tử |
| 992 | Nhẫn thời không sai lệch | 27 | 3 | 1 | 0 | Vật phẩm nhiệm vụ |
| 1505 | Giấy màu | 27 | 3 | 0 | 0 | VPSK |
| 1569 | Kho báu hải tặc | 27 | 3 | 0 | 0 | VPSK |
| 1787 | Vé riêng tư | 75 | 3 | 0 | 0 | Vật phẩm để vào khu vực đánh quái riêng tư |
| 1795 | Bình hút năng lượng | 75 | 3 | 0 | 0 | Dùng để hút kilis |
| 1819 | Bông tai Porata | 27 | 3 | 1 | 1500000 | Sử dụng để hợp thể với đệ tử |
| 1822 | Rada ngọc rồng | 75 | 3 | 0 | 0 | Vật phẩm sự kiện |
| 1823 | Rada ngọc rồng Vip | 75 | 3 | 0 | 0 | Vật phẩm sự kiện |

### 3.3 Danh sách đầy đủ vật phẩm

Cột: `id`, `NAME`, `TYPE`, `gender` (0 TĐ/1 NM/2 XD/3 chung), `level`, `power_require` (sức mạnh yêu cầu), `part` (id part hiển thị), `icon_id`.

| id | Tên | TYPE | gender | level | power_require | part | icon_id |
|---|---|---|---|---|---|---|---|
| 0 | Áo vải 3 lỗ | 0 | 0 | 1 | 1200 | 14 | 390 |
| 1 | Áo sợi len | 0 | 1 | 1 | 1200 | 10 | 393 |
| 2 | Áo vải thô | 0 | 2 | 1 | 1200 | 16 | 392 |
| 3 | Áo vải dày | 0 | 0 | 3 | 26000 | 1 | 389 |
| 4 | Áo len Pico | 0 | 1 | 3 | 26000 | 12 | 394 |
| 5 | Áo giáp sắt | 0 | 2 | 3 | 26000 | 7 | 391 |
| 6 | Quần vải đen | 1 | 0 | 1 | 1000 | 15 | 396 |
| 7 | Quần sợi len | 1 | 1 | 1 | 1000 | 11 | 398 |
| 8 | Quần vải thô | 1 | 2 | 1 | 1000 | 17 | 400 |
| 9 | Quần vải dày | 1 | 0 | 3 | 22000 | 2 | 395 |
| 10 | Quần vải thô Pico | 1 | 1 | 3 | 22000 | 13 | 397 |
| 11 | Quần giáp sắt | 1 | 2 | 3 | 22000 | 8 | 399 |
| 12 | Rada cấp 1 | 4 | 3 | 1 | 1300 | -1 | 1089 |
| 13 | Đậu thần cấp 1 | 6 | 3 | 1 | 1 | -1 | 241 |
| 14 | Ngọc Rồng 1 sao | 12 | 3 | 0 | 0 | -1 | 419 |
| 15 | Ngọc Rồng 2 sao | 12 | 3 | 0 | 0 | -1 | 420 |
| 16 | Ngọc Rồng 3 sao | 12 | 3 | 0 | 0 | -1 | 421 |
| 17 | Ngọc Rồng 4 sao | 12 | 3 | 0 | 0 | -1 | 422 |
| 18 | Ngọc Rồng 5 sao | 12 | 3 | 0 | 0 | -1 | 423 |
| 19 | Ngọc Rồng 6 sao | 12 | 3 | 0 | 0 | -1 | 424 |
| 20 | Ngọc Rồng 7 sao | 12 | 3 | 0 | 0 | -1 | 425 |
| 21 | Găng vải đen | 2 | 0 | 1 | 1500 | -1 | 408 |
| 22 | Găng sợi len | 2 | 1 | 1 | 1500 | -1 | 411 |
| 23 | Găng vải thô | 2 | 2 | 1 | 1500 | -1 | 409 |
| 24 | Găng thun đen | 2 | 0 | 2 | 15000 | -1 | 408 |
| 25 | Găng len Pico | 2 | 1 | 3 | 50000 | -1 | 412 |
| 26 | Găng sắt | 2 | 2 | 3 | 50000 | -1 | 410 |
| 27 | Giầy nhựa | 3 | 0 | 1 | 800 | -1 | 401 |
| 28 | Giầy sợi len | 3 | 1 | 1 | 800 | -1 | 405 |
| 29 | Giầy vải thô | 3 | 2 | 1 | 800 | -1 | 403 |
| 30 | Giầy cao su | 3 | 0 | 2 | 8000 | -1 | 401 |
| 31 | Giầy nhựa Pico | 3 | 1 | 3 | 18000 | -1 | 406 |
| 32 | Giầy sắt | 3 | 2 | 3 | 18000 | -1 | 404 |
| 33 | Áo thun 3 lỗ | 0 | 0 | 2 | 12000 | 14 | 390 |
| 34 | Áo thun dày | 0 | 0 | 4 | 80000 | 1 | 389 |
| 35 | Quần thun đen | 1 | 0 | 2 | 10000 | 15 | 396 |
| 36 | Quần thun dày | 1 | 0 | 4 | 66000 | 2 | 395 |
| 37 | Găng vải dày | 2 | 0 | 3 | 50000 | -1 | 407 |
| 38 | Găng thun dày | 2 | 0 | 4 | 200000 | -1 | 407 |
| 39 | Giày nhựa đế dày | 3 | 0 | 3 | 18000 | -1 | 402 |
| 40 | Giày cao su đế dày | 3 | 0 | 4 | 53000 | -1 | 402 |
| 41 | Áo sợi gai | 0 | 1 | 2 | 12000 | 10 | 393 |
| 42 | Áo thun Pico | 0 | 1 | 4 | 80000 | 12 | 394 |
| 43 | Quần sợi gai | 1 | 1 | 2 | 10000 | 11 | 398 |
| 44 | Quần thun Pico | 1 | 1 | 4 | 66000 | 13 | 397 |
| 45 | Găng thun Pico | 2 | 1 | 4 | 200000 | -1 | 412 |
| 46 | Găng sợi gai | 2 | 1 | 2 | 15000 | -1 | 411 |
| 47 | Giầy sợi gai | 3 | 1 | 2 | 8000 | -1 | 405 |
| 48 | Giầy cao su Pico | 3 | 1 | 4 | 53000 | -1 | 406 |
| 49 | Áo thun thô | 0 | 2 | 2 | 12000 | 16 | 392 |
| 50 | Áo giáp đồng | 0 | 2 | 4 | 80000 | 7 | 391 |
| 51 | Quần thun thô | 1 | 2 | 2 | 10000 | 17 | 400 |
| 52 | Quần giáp đồng | 1 | 2 | 4 | 66000 | 8 | 399 |
| 53 | Găng thun thô | 2 | 2 | 2 | 15000 | -1 | 409 |
| 54 | Găng đồng | 2 | 2 | 4 | 200000 | -1 | 410 |
| 55 | Giầy cao su thô | 3 | 2 | 2 | 8000 | -1 | 403 |
| 56 | Giầy đồng | 3 | 2 | 4 | 53000 | -1 | 404 |
| 57 | Rada cấp 2 | 4 | 3 | 2 | 13000 | -1 | 433 |
| 58 | Rada cấp 3 | 4 | 3 | 3 | 28000 | -1 | 1022 |
| 59 | Rada cấp 4 | 4 | 3 | 4 | 86000 | -1 | 1023 |
| 60 | Đậu thần cấp 2 | 6 | 3 | 2 | 1 | -1 | 241 |
| 61 | Đậu thần cấp 3 | 6 | 3 | 3 | 1 | -1 | 241 |
| 62 | Đậu thần cấp 4 | 6 | 3 | 4 | 1 | -1 | 241 |
| 63 | Đậu thần cấp 5 | 6 | 3 | 5 | 1 | -1 | 241 |
| 64 | Đậu thần cấp 6 | 6 | 3 | 6 | 1 | -1 | 241 |
| 65 | Đậu thần cấp 7 | 6 | 3 | 7 | 1 | -1 | 241 |
| 66 | Sách đấm Dragon lv1 | 7 | 0 | 1 | 1 | -1 | 644 |
| 67 | Sách đấm Dragon lv2 | 7 | 0 | 2 | 1 | -1 | 645 |
| 68 | Sách đấm Dragon lv3 | 7 | 0 | 3 | 1 | -1 | 646 |
| 69 | Sách đấm Dragon lv4 | 7 | 0 | 4 | 1 | -1 | 647 |
| 70 | Sách đấm Dragon lv5 | 7 | 0 | 5 | 1 | -1 | 648 |
| 71 | Sách đấm Dragon lv6 | 7 | 0 | 6 | 1 | -1 | 649 |
| 72 | Sách đấm Dragon lv7 | 7 | 0 | 7 | 1 | -1 | 650 |
| 73 | Đùi gà | 8 | 3 | 1 | 1 | -1 | 643 |
| 74 | Đùi gà nướng | 27 | 3 | 1 | 1 | -1 | 643 |
| 75 | Đùi heo Xayda | 8 | 3 | 1 | 1 | -1 | 241 |
| 76 | Vàng | 9 | 3 | 1 | 0 | -1 | 927 |
| 77 | Ngọc | 10 | 3 | 1 | 0 | -1 | 932 |
| 78 | Đứa bé | 11 | 3 | 1 | 0 | 736 | 737 |
| 79 | Sách đấm Demon lv1 | 7 | 1 | 1 | 1 | -1 | 665 |
| 80 | Sách đấm Demon lv2 | 7 | 1 | 2 | 1 | -1 | 666 |
| 81 | Sách đấm Demon lv3 | 7 | 1 | 3 | 1 | -1 | 667 |
| 82 | Sách đấm Demon lv4 | 7 | 1 | 4 | 1 | -1 | 668 |
| 83 | Sách đấm Demon lv5 | 7 | 1 | 5 | 1 | -1 | 669 |
| 84 | Sách đấm Demon lv6 | 7 | 1 | 6 | 1 | -1 | 670 |
| 85 | Truyện tranh | 8 | 3 | 0 | 0 | -1 | 1041 |
| 86 | Sách đấm Demon lv7 | 7 | 1 | 7 | 1 | -1 | 671 |
| 87 | Sách đấm Galick lv1 | 7 | 2 | 1 | 1 | -1 | 679 |
| 88 | Sách đấm Galick lv2 | 7 | 2 | 2 | 1 | -1 | 680 |
| 89 | Sách đấm Galick lv3 | 7 | 2 | 3 | 1 | -1 | 681 |
| 90 | Sách đấm Galick lv4 | 7 | 2 | 4 | 1 | -1 | 682 |
| 91 | Sách đấm Galick lv5 | 7 | 2 | 5 | 1 | -1 | 683 |
| 92 | Sách đấm Galick lv6 | 7 | 2 | 6 | 1 | -1 | 684 |
| 93 | Sách đấm Galick lv7 | 7 | 2 | 7 | 1 | -1 | 685 |
| 94 | Sách Kamejoko lv1 | 7 | 0 | 1 | 0 | -1 | 651 |
| 95 | Sách Kamejoko lv2 | 7 | 0 | 2 | 0 | -1 | 652 |
| 96 | Sách Kamejoko lv3 | 7 | 0 | 3 | 0 | -1 | 653 |
| 97 | Sách Kamejoko lv4 | 7 | 0 | 4 | 0 | -1 | 654 |
| 98 | Sách Kamejoko lv5 | 7 | 0 | 5 | 0 | -1 | 655 |
| 99 | Sách Kamejoko lv6 | 7 | 0 | 6 | 0 | -1 | 656 |
| 100 | Sách Kamejoko lv7 | 7 | 0 | 7 | 0 | -1 | 657 |
| 101 | Sách Masenko lv1 | 7 | 1 | 1 | 0 | -1 | 672 |
| 102 | Sách Masenko lv2 | 7 | 1 | 2 | 0 | -1 | 673 |
| 103 | Sách Masenko lv3 | 7 | 1 | 3 | 0 | -1 | 674 |
| 104 | Sách Masenko lv4 | 7 | 1 | 4 | 0 | -1 | 675 |
| 105 | Sách Masenko lv5 | 7 | 1 | 5 | 0 | -1 | 676 |
| 106 | Sách Masenko lv6 | 7 | 1 | 6 | 0 | -1 | 677 |
| 107 | Sách Masenko lv7 | 7 | 1 | 7 | 0 | -1 | 678 |
| 108 | Sách Antomic lv1 | 7 | 2 | 1 | 0 | -1 | 686 |
| 109 | Sách Antomic lv2 | 7 | 2 | 2 | 0 | -1 | 687 |
| 110 | Sách Antomic lv3 | 7 | 2 | 3 | 0 | -1 | 688 |
| 111 | Sách Antomic lv4 | 7 | 2 | 4 | 0 | -1 | 689 |
| 112 | Sách Antomic lv5 | 7 | 2 | 5 | 0 | -1 | 690 |
| 113 | Sách Antomic lv6 | 7 | 2 | 6 | 0 | -1 | 691 |
| 114 | Sách Antomic lv7 | 7 | 2 | 7 | 0 | -1 | 692 |
| 115 | Thái Dương Hạ San lv1 | 7 | 0 | 1 | 0 | -1 | 658 |
| 116 | Thái Dương Hạ San lv2 | 7 | 0 | 2 | 0 | -1 | 659 |
| 117 | Thái Dương Hạ San lv3 | 7 | 0 | 3 | 0 | -1 | 660 |
| 118 | Thái Dương Hạ San lv4 | 7 | 0 | 4 | 0 | -1 | 661 |
| 119 | Thái Dương Hạ San lv5 | 7 | 0 | 5 | 0 | -1 | 662 |
| 120 | Thái Dương Hạ San lv6 | 7 | 0 | 6 | 0 | -1 | 663 |
| 121 | Thái Dương Hạ San lv7 | 7 | 0 | 7 | 0 | -1 | 664 |
| 122 | Sách học Trị thương lv1 | 7 | 1 | 1 | 0 | -1 | 1090 |
| 123 | Sách học Trị thương lv2 | 7 | 1 | 2 | 0 | -1 | 1091 |
| 124 | Sách học Trị thương lv3 | 7 | 1 | 3 | 0 | -1 | 1092 |
| 125 | Sách học Trị thương lv4 | 7 | 1 | 4 | 0 | -1 | 1093 |
| 126 | Sách học Trị thương lv5 | 7 | 1 | 5 | 0 | -1 | 1094 |
| 127 | Sách học Trị thương lv6 | 7 | 1 | 6 | 0 | -1 | 1095 |
| 128 | Sách học Trị thương lv7 | 7 | 1 | 7 | 0 | -1 | 1096 |
| 129 | Tái tạo năng lượng lv1 | 7 | 2 | 1 | 0 | -1 | 1097 |
| 130 | Tái tạo năng lượng lv2 | 7 | 2 | 2 | 0 | -1 | 1098 |
| 131 | Tái tạo năng lượng lv3 | 7 | 2 | 3 | 0 | -1 | 1099 |
| 132 | Tái tạo năng lượng lv4 | 7 | 2 | 4 | 0 | -1 | 1100 |
| 133 | Tái tạo năng lượng lv5 | 7 | 2 | 5 | 0 | -1 | 1101 |
| 134 | Tái tạo năng lượng lv6 | 7 | 2 | 6 | 0 | -1 | 1102 |
| 135 | Tái tạo năng lượng lv7 | 7 | 2 | 7 | 0 | -1 | 1103 |
| 136 | Áo vải Kame | 0 | 0 | 5 | 240000 | 65 | 938 |
| 137 | Áo thun Kame | 0 | 0 | 6 | 720000 | 65 | 938 |
| 138 | Áo võ Kame | 0 | 0 | 7 | 2200000 | 71 | 949 |
| 139 | Áo võ Goku | 0 | 0 | 8 | 6400000 | 71 | 949 |
| 140 | Quần vải Kame | 1 | 0 | 5 | 200000 | 66 | 939 |
| 141 | Quần thun Kame | 1 | 0 | 6 | 600000 | 66 | 939 |
| 142 | Quần võ Kame | 1 | 0 | 7 | 1800000 | 72 | 950 |
| 143 | Quần võ goku | 1 | 0 | 8 | 5300000 | 72 | 950 |
| 144 | Găng vải Kame | 2 | 0 | 5 | 800000 | -1 | 947 |
| 145 | Găng thun Kame | 2 | 0 | 6 | 3200000 | -1 | 947 |
| 146 | Găng võ Kame | 2 | 0 | 7 | 10000000 | -1 | 953 |
| 147 | Găng võ goku | 2 | 0 | 8 | 20000000 | -1 | 953 |
| 148 | Giày nhựa Kame | 3 | 0 | 5 | 160000 | -1 | 940 |
| 149 | Giày cao su Kame | 3 | 0 | 6 | 480000 | -1 | 940 |
| 150 | Giày võ kame | 3 | 0 | 7 | 1400000 | -1 | 952 |
| 151 | Giày võ goku | 3 | 0 | 8 | 4300000 | -1 | 952 |
| 152 | Áo choàng len | 0 | 1 | 5 | 240000 | 67 | 942 |
| 153 | Áo choàng thun | 0 | 1 | 6 | 720000 | 67 | 942 |
| 154 | Áo vải Pico | 0 | 1 | 7 | 2200000 | 75 | 959 |
| 155 | Áo da Pico | 0 | 1 | 8 | 6400000 | 75 | 959 |
| 156 | Quần len cứng | 1 | 1 | 5 | 200000 | 68 | 943 |
| 157 | Quần thun cứng | 1 | 1 | 6 | 600000 | 68 | 943 |
| 158 | Quần vải cứng Pico | 1 | 1 | 7 | 1800000 | 76 | 960 |
| 159 | Quần vải mềm Pico | 1 | 1 | 8 | 5300000 | 76 | 960 |
| 160 | Găng len cứng | 2 | 1 | 5 | 800000 | -1 | 948 |
| 161 | Găng thun cứng | 2 | 1 | 6 | 3200000 | -1 | 948 |
| 162 | Găng vải Pico | 2 | 1 | 7 | 10000000 | -1 | 961 |
| 163 | Găng da Pico | 2 | 1 | 8 | 20000000 | -1 | 961 |
| 164 | Giày nhựa cứng | 3 | 1 | 5 | 160000 | -1 | 945 |
| 165 | Giày cao su cứng | 3 | 1 | 6 | 480000 | -1 | 945 |
| 166 | Giày da Pico | 3 | 1 | 7 | 1400000 | -1 | 963 |
| 167 | Giày sắt Pico | 3 | 1 | 8 | 4300000 | -1 | 963 |
| 168 | Áo giáp bạc | 0 | 2 | 5 | 240000 | 69 | 934 |
| 169 | Áo giáp vàng | 0 | 2 | 6 | 720000 | 69 | 934 |
| 170 | Áo lông Xayda | 0 | 2 | 7 | 2200000 | 73 | 954 |
| 171 | Áo khoác Xayda | 0 | 2 | 8 | 6400000 | 73 | 954 |
| 172 | Quần giáp bạc | 1 | 2 | 5 | 200000 | 70 | 935 |
| 173 | Quần giáp vàng | 1 | 2 | 6 | 600000 | 70 | 935 |
| 174 | Quần lông Xayda | 1 | 2 | 7 | 1800000 | 74 | 955 |
| 175 | Quần da Xayda | 1 | 2 | 8 | 5300000 | 74 | 955 |
| 176 | Găng bạc | 2 | 2 | 5 | 800000 | -1 | 946 |
| 177 | Găng vàng | 2 | 2 | 6 | 3200000 | -1 | 946 |
| 178 | Găng lông Xayda | 2 | 2 | 7 | 10000000 | -1 | 956 |
| 179 | Găng da Xayda | 2 | 2 | 8 | 20000000 | -1 | 956 |
| 180 | Giày bạc | 3 | 2 | 5 | 160000 | -1 | 936 |
| 181 | Giày vàng | 3 | 2 | 6 | 480000 | -1 | 936 |
| 182 | Giày lông Xayda | 3 | 2 | 7 | 1400000 | -1 | 957 |
| 183 | Giày da Xayda | 3 | 2 | 8 | 4300000 | -1 | 957 |
| 184 | Rada cấp 5 | 4 | 3 | 5 | 260000 | -1 | 1019 |
| 185 | Rada cấp 6 | 4 | 3 | 6 | 780000 | -1 | 1020 |
| 186 | Rada cấp 7 | 4 | 3 | 7 | 2300000 | -1 | 1021 |
| 187 | Rada cấp 8 | 4 | 3 | 8 | 7000000 | -1 | 434 |
| 188 | Vàng | 9 | 3 | 1 | 0 | -1 | 928 |
| 189 | Vàng | 9 | 3 | 1 | 0 | -1 | 929 |
| 190 | Vàng | 9 | 3 | 1 | 0 | -1 | 930 |
| 191 | Cà chua | 27 | 3 | 0 | 0 | -1 | 931 |
| 192 | Cà rốt | 27 | 3 | 0 | 0 | -1 | 933 |
| 193 | Gói 10 viên Capsule | 27 | 3 | 0 | 0 | -1 | 1087 |
| 194 | Viên Capsule đặc biệt | 27 | 3 | 0 | 0 | -1 | 1088 |
| 195 | Siêu thánh thủy | 27 | 3 | 0 | 0 | -1 | 1088 |
| 196 | Avatar | 5 | 2 | 0 | 0 | 101 | 1383 |
| 197 | Avatar | 5 | 2 | 0 | 0 | 102 | 1380 |
| 198 | Avatar | 5 | 2 | 0 | 0 | 103 | 1379 |
| 199 | Avatar | 5 | 2 | 0 | 0 | 104 | 1378 |
| 200 | Avatar | 5 | 2 | 0 | 0 | 105 | 1381 |
| 201 | Avatar | 5 | 0 | 0 | 0 | 106 | 1382 |
| 202 | Avatar | 5 | 2 | 0 | 0 | 107 | 1377 |
| 203 | Avatar | 5 | 0 | 0 | 0 | 108 | 1387 |
| 204 | Avatar | 5 | 0 | 0 | 0 | 109 | 1385 |
| 205 | Avatar | 5 | 0 | 0 | 0 | 110 | 1389 |
| 206 | Avatar | 5 | 1 | 0 | 0 | 112 | 1384 |
| 207 | Avatar | 5 | 1 | 0 | 0 | 111 | 1386 |
| 208 | Avatar | 5 | 1 | 0 | 0 | 113 | 1388 |
| 209 | Avatar | 5 | 2 | 0 | 0 | 0 | 17 |
| 210 | Avatar | 5 | 0 | 0 | 0 | 0 | 17 |
| 211 | Nho tím | 27 | 3 | 0 | 0 | -1 | 1394 |
| 212 | Nho xanh | 27 | 3 | 0 | 0 | -1 | 1395 |
| 213 | Bùa Trí Tuệ | 13 | 3 | 0 | 0 | -1 | 1403 |
| 214 | Bùa Mạnh Mẽ | 13 | 3 | 0 | 0 | -1 | 1404 |
| 215 | Bùa Da Trâu | 13 | 3 | 0 | 0 | -1 | 1405 |
| 216 | Bùa Oai Hùng | 13 | 3 | 0 | 0 | -1 | 1406 |
| 217 | Bùa Bất Tử | 13 | 3 | 0 | 0 | -1 | 1407 |
| 218 | Bùa Dẻo Dai | 13 | 3 | 0 | 0 | -1 | 1408 |
| 219 | Bùa Thu Hút | 13 | 3 | 0 | 0 | -1 | 1409 |
| 220 | Đá lục bảo | 14 | 3 | 0 | 0 | -1 | 1420 |
| 221 | Đá Saphia | 14 | 3 | 0 | 0 | -1 | 1419 |
| 222 | Đá Ruby | 14 | 3 | 0 | 0 | -1 | 1416 |
| 223 | Đá Titan | 14 | 3 | 0 | 0 | -1 | 1417 |
| 224 | Đá thạch anh tím | 14 | 3 | 0 | 0 | -1 | 1418 |
| 225 | Mảnh đá vụn | 15 | 3 | 0 | 0 | -1 | 1421 |
| 226 | Bình nước phép | 16 | 3 | 0 | 0 | -1 | 1422 |
| 227 | Avatar VIP | 5 | 0 | 0 | 0 | 127 | 1477 |
| 228 | Avatar VIP | 5 | 1 | 0 | 0 | 128 | 1454 |
| 229 | Avatar VIP | 5 | 2 | 0 | 0 | 126 | 1478 |
| 230 | Áo bạc Goku | 0 | 0 | 9 | 20000000 | 155 | 1689 |
| 231 | Áo vàng Goku | 0 | 0 | 10 | 58000000 | 155 | 1689 |
| 232 | Áo da Calic | 0 | 0 | 11 | 170000000 | 157 | 1688 |
| 233 | Áo jean Calic | 0 | 0 | 12 | 520000000 | 157 | 1688 |
| 234 | Áo sắt Tron | 0 | 1 | 9 | 20000000 | 149 | 1692 |
| 235 | Áo đồng Tron | 0 | 1 | 10 | 58000000 | 149 | 1692 |
| 236 | Áo bạc Zealot | 0 | 1 | 11 | 170000000 | 153 | 1693 |
| 237 | Áo vàng Zealot | 0 | 1 | 12 | 520000000 | 153 | 1693 |
| 238 | Áo lông đỏ | 0 | 2 | 9 | 20000000 | 147 | 1690 |
| 239 | Áo siêu xayda | 0 | 2 | 10 | 58000000 | 147 | 1690 |
| 240 | Áo Kaio | 0 | 2 | 11 | 170000000 | 151 | 1691 |
| 241 | Áo lưỡng long | 0 | 2 | 12 | 520000000 | 151 | 1691 |
| 242 | Quần bạc Goku | 1 | 0 | 9 | 20000000 | 156 | 1695 |
| 243 | Quần vàng Goku | 1 | 0 | 10 | 58000000 | 156 | 1695 |
| 244 | Quần da Calic | 1 | 0 | 11 | 170000000 | 158 | 1694 |
| 245 | Quần jean Calic | 1 | 0 | 12 | 520000000 | 158 | 1694 |
| 246 | Quần sắt Tron | 1 | 1 | 9 | 20000000 | 150 | 1698 |
| 247 | Quần đồng Tron | 1 | 1 | 10 | 58000000 | 150 | 1698 |
| 248 | Quần bạc Zealot | 1 | 1 | 11 | 170000000 | 154 | 1699 |
| 249 | Quần vàng Zealot | 1 | 1 | 12 | 520000000 | 154 | 1699 |
| 250 | Quần lông đỏ | 1 | 2 | 9 | 20000000 | 148 | 1696 |
| 251 | Quần siêu Xayda | 1 | 2 | 10 | 58000000 | 148 | 1696 |
| 252 | Quần Kaio | 1 | 2 | 11 | 170000000 | 152 | 1697 |
| 253 | Quần lưỡng long | 1 | 2 | 12 | 520000000 | 152 | 1697 |
| 254 | Găng bạc Goku | 2 | 0 | 9 | 68000000 | -1 | 1707 |
| 255 | Găng vàng Goku | 2 | 0 | 10 | 200000000 | -1 | 1707 |
| 256 | Găng da Calic | 2 | 0 | 11 | 600000000 | -1 | 1706 |
| 257 | Găng jean Calic | 2 | 0 | 12 | 1500000000 | -1 | 1706 |
| 258 | Găng sắt Tron | 2 | 1 | 9 | 68000000 | -1 | 1711 |
| 259 | Găng đồng Tron | 2 | 1 | 10 | 200000000 | -1 | 1711 |
| 260 | Găng bạc Zealot | 2 | 1 | 11 | 600000000 | -1 | 1710 |
| 261 | Găng vàng Zealot | 2 | 1 | 12 | 1500000000 | -1 | 1710 |
| 262 | Găng lông đỏ | 2 | 2 | 9 | 68000000 | -1 | 1708 |
| 263 | Găng siêu Xayda | 2 | 2 | 10 | 200000000 | -1 | 1708 |
| 264 | Găng Kaio | 2 | 2 | 11 | 600000000 | -1 | 1709 |
| 265 | Găng lưỡng long | 2 | 2 | 12 | 1500000000 | -1 | 1709 |
| 266 | Giày bạc Goku | 3 | 0 | 9 | 13000000 | -1 | 1701 |
| 267 | Giày vàng Goku | 3 | 0 | 10 | 38000000 | -1 | 1701 |
| 268 | Giày da Calic | 3 | 0 | 11 | 120000000 | -1 | 1700 |
| 269 | Giày jean Calic | 3 | 0 | 12 | 340000000 | -1 | 1700 |
| 270 | Giày sắt Tron | 3 | 1 | 9 | 13000000 | -1 | 1704 |
| 271 | Giày đồng Tron | 3 | 1 | 10 | 38000000 | -1 | 1704 |
| 272 | Giày bạc Zealot | 3 | 1 | 11 | 120000000 | -1 | 1705 |
| 273 | Giày vàng Zealot | 3 | 1 | 12 | 340000000 | -1 | 1705 |
| 274 | Giày lông đỏ | 3 | 2 | 9 | 13000000 | -1 | 1702 |
| 275 | Giày siêu Xayda | 3 | 2 | 10 | 38000000 | -1 | 1702 |
| 276 | Giày Kaio | 3 | 2 | 11 | 120000000 | -1 | 1703 |
| 277 | Giày lưỡng long | 3 | 2 | 12 | 340000000 | -1 | 1703 |
| 278 | Rada cấp 9 | 4 | 3 | 9 | 42000000 | -1 | 1712 |
| 279 | Rada cấp 10 | 4 | 3 | 10 | 200000000 | -1 | 1712 |
| 280 | Rada cấp 11 | 4 | 3 | 11 | 1000000000 | -1 | 1713 |
| 281 | Rada cấp 12 | 4 | 3 | 12 | 1500000000 | -1 | 1713 |
| 282 | Cải trang | 5 | 3 | 1 | 0 | -1 | 2127 |
| 283 | Cải trang | 5 | 0 | 1 | 0 | -1 | 2128 |
| 284 | Cải trang | 5 | 3 | 1 | 0 | -1 | 1210 |
| 285 | Cải trang | 5 | 0 | 1 | 0 | -1 | 2126 |
| 286 | Cải trang | 5 | 1 | 1 | 0 | -1 | 2129 |
| 287 | Cải trang | 5 | 1 | 1 | 0 | -1 | 2133 |
| 288 | Cải trang | 5 | 2 | 1 | 0 | -1 | 2137 |
| 289 | Cải trang | 5 | 2 | 1 | 0 | -1 | 2125 |
| 290 | Cải trang | 5 | 3 | 1 | 0 | -1 | 2124 |
| 291 | Cải trang | 5 | 0 | 1 | 0 | -1 | 2131 |
| 292 | Cải trang | 5 | 3 | 1 | 0 | -1 | 2123 |
| 293 | Gói 30 đậu thần cấp 1 | 27 | 3 | 1 | 0 | -1 | 241 |
| 294 | Gói 30 đậu thần cấp 2 | 27 | 3 | 1 | 0 | -1 | 241 |
| 295 | Gói 30 đậu thần cấp 3 | 27 | 3 | 1 | 0 | -1 | 241 |
| 296 | Gói 30 đậu thần cấp 4 | 27 | 3 | 1 | 0 | -1 | 241 |
| 297 | Gói 30 đậu thần cấp 5 | 27 | 3 | 1 | 0 | -1 | 241 |
| 298 | Gói 30 đậu thần cấp 6 | 27 | 3 | 1 | 0 | -1 | 241 |
| 299 | Gói 30 đậu thần cấp 7 | 27 | 3 | 1 | 0 | -1 | 241 |
| 300 | Kaioken lv1 | 7 | 0 | 1 | 10000000 | -1 | 716 |
| 301 | Kaioken lv2 | 7 | 0 | 2 | 10000000 | -1 | 716 |
| 302 | Kaioken lv3 | 7 | 0 | 3 | 10000000 | -1 | 716 |
| 303 | Kaioken lv4 | 7 | 0 | 4 | 10000000 | -1 | 716 |
| 304 | Kaioken lv5 | 7 | 0 | 5 | 10000000 | -1 | 716 |
| 305 | Kaioken lv6 | 7 | 0 | 6 | 10000000 | -1 | 716 |
| 306 | Kaioken lv7 | 7 | 0 | 7 | 10000000 | -1 | 716 |
| 307 | Quả cầu Kênh Khi lv1 | 7 | 0 | 1 | 10000000 | -1 | 717 |
| 308 | Quả cầu Kênh Khi lv2 | 7 | 0 | 2 | 10000000 | -1 | 717 |
| 309 | Quả cầu Kênh Khi lv3 | 7 | 0 | 3 | 10000000 | -1 | 717 |
| 310 | Quả cầu Kênh Khi lv4 | 7 | 0 | 4 | 10000000 | -1 | 717 |
| 311 | Quả cầu Kênh Khi lv5 | 7 | 0 | 5 | 10000000 | -1 | 717 |
| 312 | Quả cầu Kênh Khi lv6 | 7 | 0 | 6 | 10000000 | -1 | 717 |
| 313 | Quả cầu Kênh Khi lv7 | 7 | 0 | 7 | 10000000 | -1 | 717 |
| 314 | Hóa khỉ khổng lồ lv1 | 7 | 2 | 1 | 10000000 | -1 | 718 |
| 315 | Hóa khỉ khổng lồ lv2 | 7 | 2 | 2 | 10000000 | -1 | 718 |
| 316 | Hóa khỉ khổng lồ lv3 | 7 | 2 | 3 | 10000000 | -1 | 718 |
| 317 | Hóa khỉ khổng lồ lv4 | 7 | 2 | 4 | 10000000 | -1 | 718 |
| 318 | Hóa khỉ khổng lồ lv5 | 7 | 2 | 5 | 10000000 | -1 | 718 |
| 319 | Hóa khỉ khổng lồ lv6 | 7 | 2 | 6 | 10000000 | -1 | 718 |
| 320 | Hóa khỉ khổng lồ lv7 | 7 | 2 | 7 | 10000000 | -1 | 718 |
| 321 | Bom hi sinh lv1 | 7 | 2 | 1 | 10000000 | -1 | 2248 |
| 322 | Bom hi sinh lv2 | 7 | 2 | 2 | 10000000 | -1 | 2248 |
| 323 | Bom hi sinh lv3 | 7 | 2 | 3 | 10000000 | -1 | 2248 |
| 324 | Bom hi sinh lv4 | 7 | 2 | 4 | 10000000 | -1 | 2248 |
| 325 | Bom hi sinh lv5 | 7 | 2 | 5 | 10000000 | -1 | 2248 |
| 326 | Bom hi sinh lv6 | 7 | 2 | 6 | 10000000 | -1 | 2248 |
| 327 | Bom hi sinh lv7 | 7 | 2 | 7 | 10000000 | -1 | 2248 |
| 328 | Makankosappo lv1 | 7 | 1 | 1 | 10000000 | -1 | 723 |
| 329 | Makankosappo lv2 | 7 | 1 | 2 | 10000000 | -1 | 723 |
| 330 | Makankosappo lv3 | 7 | 1 | 3 | 10000000 | -1 | 723 |
| 331 | Makankosappo lv4 | 7 | 1 | 4 | 10000000 | -1 | 723 |
| 332 | Makankosappo lv5 | 7 | 1 | 5 | 10000000 | -1 | 723 |
| 333 | Makankosappo lv6 | 7 | 1 | 6 | 10000000 | -1 | 723 |
| 334 | Makankosappo lv7 | 7 | 1 | 7 | 10000000 | -1 | 723 |
| 335 | Đẻ trứng lv1 | 7 | 1 | 1 | 10000000 | -1 | 722 |
| 336 | Đẻ trứng lv2 | 7 | 1 | 2 | 10000000 | -1 | 722 |
| 337 | Đẻ trứng lv3 | 7 | 1 | 3 | 10000000 | -1 | 722 |
| 338 | Đẻ trứng lv4 | 7 | 1 | 4 | 10000000 | -1 | 722 |
| 339 | Đẻ trứng lv5 | 7 | 1 | 5 | 10000000 | -1 | 722 |
| 340 | Đẻ trứng lv6 | 7 | 1 | 6 | 10000000 | -1 | 722 |
| 341 | Đẻ trứng lv7 | 7 | 1 | 7 | 10000000 | -1 | 722 |
| 342 | Vệ tinh trí lực | 22 | 3 | 1 | 0 | -1 | 2265 |
| 343 | Vệ tinh trí tuệ | 22 | 3 | 1 | 0 | -1 | 2266 |
| 344 | Vệ tinh phòng thủ | 22 | 3 | 1 | 0 | -1 | 2267 |
| 345 | Vệ tinh sinh lực | 22 | 3 | 1 | 0 | -1 | 2264 |
| 346 | Cân đẩu vân | 23 | 0 | 1 | 0 | -1 | 2273 |
| 347 | Phi Long | 23 | 1 | 1 | 0 | -1 | 2277 |
| 348 | Ván bay | 23 | 2 | 1 | 0 | -1 | 2275 |
| 349 | Cân đẩu vân VIP | 24 | 0 | 1 | 0 | -1 | 2274 |
| 350 | Phi Long VIP | 24 | 1 | 1 | 0 | -1 | 2278 |
| 351 | Ván bay VIP | 24 | 2 | 1 | 0 | -1 | 2276 |
| 352 | Đậu thần cấp 8 | 6 | 3 | 8 | 1 | -1 | 241 |
| 353 | Ngọc Rồng Namek 1 Sao | 11 | 3 | 1 | 0 | -1 | 2280 |
| 354 | Ngọc Rồng Namek 2 Sao | 11 | 3 | 1 | 0 | -1 | 2281 |
| 355 | Ngọc Rồng Namek 3 Sao | 11 | 3 | 1 | 0 | -1 | 2282 |
| 356 | Ngọc Rồng Namek 4 Sao | 11 | 3 | 1 | 0 | -1 | 2283 |
| 357 | Ngọc Rồng Namek 5 Sao | 11 | 3 | 1 | 0 | -1 | 2284 |
| 358 | Ngọc Rồng Namek 6 Sao | 11 | 3 | 1 | 0 | -1 | 2285 |
| 359 | Ngọc Rồng Namek 7 Sao | 11 | 3 | 1 | 0 | -1 | 2286 |
| 360 | Ngọc Rồng Namek | 11 | 3 | 1 | 0 | -1 | 2287 |
| 361 | Gói 10 Rađa dò ngọc | 25 | 3 | 1 | 0 | -1 | 2295 |
| 362 | Hóa thạch Ngọc Rồng | 11 | 3 | 1 | 0 | -1 | 2288 |
| 363 | Tháo cờ | 28 | 3 | 1 | 0 | -1 | 2761 |
| 364 | Cờ xanh | 28 | 3 | 1 | 0 | -1 | 2330 |
| 365 | Cờ đỏ | 28 | 3 | 1 | 0 | -1 | 2323 |
| 366 | Cờ tím | 28 | 3 | 1 | 0 | -1 | 2327 |
| 367 | Cờ vàng | 28 | 3 | 1 | 0 | -1 | 2326 |
| 368 | Cờ lục | 28 | 3 | 1 | 0 | -1 | 2324 |
| 369 | Cờ hồng | 28 | 3 | 1 | 0 | -1 | 2329 |
| 370 | Cờ cam | 28 | 3 | 1 | 0 | -1 | 2328 |
| 371 | Cờ xám | 28 | 3 | 1 | 0 | -1 | 2331 |
| 372 | Ngọc rồng 1 sao đen | 11 | 3 | 1 | 0 | -1 | 2315 |
| 373 | Ngọc rồng 2 sao đen | 11 | 3 | 1 | 0 | -1 | 2316 |
| 374 | Ngọc rồng 3 sao đen | 11 | 3 | 1 | 0 | -1 | 2317 |
| 375 | Ngọc rồng 4 sao đen | 11 | 3 | 1 | 0 | -1 | 2318 |
| 376 | Ngọc rồng 5 sao đen | 11 | 3 | 1 | 0 | -1 | 2319 |
| 377 | Ngọc rồng 6 sao đen | 11 | 3 | 1 | 0 | -1 | 2320 |
| 378 | Ngọc rồng 7 sao đen | 11 | 3 | 1 | 0 | -1 | 2321 |
| 379 | Máy dò Capsule kì bí | 29 | 3 | 1 | 0 | -1 | 2758 |
| 380 | Viên Capsule kì bí | 27 | 3 | 1 | 0 | -1 | 2759 |
| 381 | Cuồng nộ | 29 | 3 | 1 | 0 | -1 | 2754 |
| 382 | Bổ huyết | 29 | 3 | 1 | 0 | -1 | 2755 |
| 383 | Bổ khí | 29 | 3 | 1 | 0 | -1 | 2756 |
| 384 | Giáp Xên bọ hung | 29 | 3 | 1 | 0 | -1 | 2757 |
| 385 | Ẩn danh | 29 | 3 | 1 | 0 | -1 | 2760 |
| 386 | Nón Noel Xám | 5 | 0 | 1 | 0 | 277 | 2782 |
| 387 | Nón Noel Đỏ | 5 | 0 | 1 | 0 | 276 | 2783 |
| 388 | Nón Noel Xanh | 5 | 0 | 1 | 0 | 278 | 2781 |
| 389 | Nón Noel Xám | 5 | 1 | 1 | 0 | 274 | 2785 |
| 390 | Nón Noel Đỏ | 5 | 1 | 1 | 0 | 273 | 2786 |
| 391 | Nón Noel Xanh | 5 | 1 | 1 | 0 | 275 | 2766 |
| 392 | Nón Noel Xám | 5 | 2 | 1 | 0 | 280 | 2788 |
| 393 | Nón Noel Đỏ | 5 | 2 | 1 | 0 | 279 | 2780 |
| 394 | Nón Noel Xanh | 5 | 2 | 1 | 0 | 281 | 2787 |
| 395 | Nhân vật bí ẩn | 27 | 3 | 1 | 0 | -1 | 2760 |
| 396 | Thú cưỡi cực VIP | 24 | 3 | 1 | 0 | -1 | 2278 |
| 397 | Hộp quà tết | 27 | 3 | 1 | 0 | -1 | 2987 |
| 398 | Hộp quà tết | 27 | 3 | 1 | 0 | -1 | 2987 |
| 399 | Thiệp chúc tết | 27 | 3 | 1 | 0 | -1 | 2988 |
| 400 | Đặt tên đệ tử | 27 | 3 | 1 | 0 | -1 | 2989 |
| 401 | Đổi đệ tử | 27 | 3 | 1 | 0 | -1 | 737 |
| 402 | Nâng kỹ năng 1 đệ tử | 27 | 3 | 1 | 0 | -1 | 7098 |
| 403 | Nâng kỹ năng 2 đệ tử | 27 | 3 | 1 | 0 | -1 | 7099 |
| 404 | Nâng kỹ năng 3 đệ tử | 27 | 3 | 1 | 0 | -1 | 7100 |
| 405 | Cải trang | 5 | 3 | 1 | 0 | -1 | 2026 |
| 406 | Cải trang | 5 | 3 | 1 | 0 | -1 | 2057 |
| 407 | Cải trang | 5 | 3 | 1 | 0 | -1 | 2088 |
| 408 | Cải trang | 5 | 3 | 1 | 0 | -1 | 3047 |
| 409 | Cải trang | 5 | 3 | 1 | 0 | -1 | 3045 |
| 410 | Cải trang | 5 | 3 | 1 | 0 | -1 | 3014 |
| 411 | Cải trang | 5 | 3 | 1 | 0 | -1 | 2467 |
| 412 | Avatar | 5 | 0 | 1 | 0 | 64 | 19 |
| 413 | Avatar | 5 | 0 | 1 | 0 | 30 | 266 |
| 414 | Avatar | 5 | 0 | 1 | 0 | 31 | 268 |
| 415 | Avatar | 5 | 1 | 1 | 0 | 9 | 121 |
| 416 | Avatar | 5 | 1 | 1 | 0 | 29 | 263 |
| 417 | Avatar | 5 | 1 | 1 | 0 | 32 | 271 |
| 418 | Avatar | 5 | 2 | 1 | 0 | 6 | 91 |
| 419 | Avatar | 5 | 2 | 1 | 0 | 27 | 259 |
| 420 | Avatar | 5 | 2 | 1 | 0 | 28 | 261 |
| 421 | Cải trang | 5 | 3 | 1 | 0 | -1 | 3099 |
| 422 | Cải trang | 5 | 3 | 1 | 0 | -1 | 3069 |
| 423 | Cải trang | 5 | 3 | 1 | 0 | -1 | 3390 |
| 424 | Cải trang | 5 | 3 | 1 | 0 | -1 | 3245 |
| 425 | Cải trang | 5 | 3 | 0 | 0 | -1 | 3276 |
| 426 | Cải trang | 5 | 3 | 1 | 0 | -1 | 3310 |
| 427 | Cải trang | 5 | 3 | 1 | 0 | -1 | 3183 |
| 428 | Cải trang | 5 | 3 | 1 | 0 | -1 | 3215 |
| 429 | Cải trang | 5 | 3 | 1 | 0 | -1 | 1871 |
| 430 | Cải trang | 5 | 3 | 1 | 0 | -1 | 2123 |
| 431 | Cải trang | 5 | 3 | 1 | 0 | -1 | 2124 |
| 432 | Cải trang | 5 | 3 | 1 | 0 | -1 | 1964 |
| 433 | Cải trang | 5 | 3 | 1 | 0 | -1 | 1995 |
| 434 | Khiên năng lượng lv1 | 7 | 3 | 1 | 150000000 | -1 | 3784 |
| 435 | Khiên năng lượng lv2 | 7 | 3 | 2 | 150000000 | -1 | 3784 |
| 436 | Khiên năng lượng lv3 | 7 | 3 | 3 | 150000000 | -1 | 3784 |
| 437 | Khiên năng lượng lv4 | 7 | 3 | 4 | 150000000 | -1 | 3784 |
| 438 | Khiên năng lượng lv5 | 7 | 3 | 5 | 150000000 | -1 | 3784 |
| 439 | Khiên năng lượng lv6 | 7 | 3 | 6 | 150000000 | -1 | 3784 |
| 440 | Khiên năng lượng lv7 | 7 | 3 | 7 | 150000000 | -1 | 3784 |
| 441 | Sao pha lê đỏ | 30 | 3 | 1 | 1 | -1 | 3887 |
| 442 | Sao pha lê lam | 30 | 3 | 2 | 1 | -1 | 3888 |
| 443 | Sao pha lê hồng | 30 | 3 | 3 | 1 | -1 | 3889 |
| 444 | Sao pha lê tím | 30 | 3 | 4 | 1 | -1 | 3890 |
| 445 | Sao pha lê cam | 30 | 3 | 5 | 1 | -1 | 3891 |
| 446 | Sao pha lê vàng | 30 | 3 | 6 | 1 | -1 | 3892 |
| 447 | Sao pha lê lục | 30 | 3 | 7 | 1 | -1 | 3893 |
| 448 | Cải trang | 5 | 3 | 1 | 1500000 | -1 | 3496 |
| 449 | Cải trang | 5 | 3 | 1 | 1500000 | -1 | 3745 |
| 450 | Cải trang | 5 | 3 | 1 | 1500000 | -1 | 3469 |
| 451 | Cải trang | 5 | 3 | 1 | 1500000 | -1 | 3407 |
| 452 | Cải trang | 5 | 3 | 1 | 1500000 | -1 | 3438 |
| 453 | Chiến thuyền Tennis | 27 | 3 | 1 | 0 | -1 | 3894 |
| 454 | Bông tai Porata | 27 | 3 | 1 | 1500000 | -1 | 3896 |
| 455 | Cải trang | 5 | 3 | 1 | 1500000 | -1 | 3995 |
| 456 | Bình nước | 27 | 3 | 1 | 1500000 | -1 | 4029 |
| 457 | Thỏi vàng | 27 | 3 | 1 | 1500000 | -1 | 4028 |
| 458 | Cải trang | 5 | 3 | 1 | 1500000 | -1 | 3558 |
| 459 | Phiếu giảm giá | 27 | 3 | 1 | 0 | -1 | 4030 |
| 460 | Cục xương | 27 | 3 | 1 | 0 | -1 | 4032 |
| 461 | Cải trang | 5 | 3 | 1 | 1500000 | -1 | 3933 |
| 462 | Củ cà rốt | 27 | 3 | 1 | 1500000 | -1 | 4083 |
| 463 | Cải trang | 5 | 3 | 1 | 1500000 | 4118 | 4044 |
| 464 | Cải trang | 5 | 3 | 1 | 1500000 | -1 | 4116 |
| 465 | Bánh Trung Thu 1 trứng | 31 | 3 | 1 | 1500000 | -1 | 4042 |
| 466 | Bánh Trung Thu 2 trứng | 31 | 3 | 1 | 1500000 | -1 | 4043 |
| 467 | Lồng đèn Ông Sao | 11 | 3 | 1 | 1500000 | 32 | 4124 |
| 468 | Lồng đèn Cá chép | 11 | 3 | 1 | 1500000 | 33 | 4122 |
| 469 | Lồng đèn Kéo Quân | 11 | 3 | 1 | 1500000 | 34 | 4121 |
| 470 | Lồng đèn Ông trăng | 11 | 3 | 1 | 1500000 | 35 | 4123 |
| 471 | Lồng đèn Hội An | 11 | 3 | 1 | 1500000 | 36 | 4120 |
| 472 | Bánh Trung Thu Đặc Biệt | 31 | 3 | 1 | 1500000 | -1 | 4125 |
| 473 | Hộp bánh Trung Thu | 31 | 3 | 1 | 1500000 | -1 | 4126 |
| 474 | Sách Biến Sôcôla lv1 | 7 | 1 | 1 | 150000000 | -1 | 3780 |
| 475 | Sách Biến Sôcôla lv2 | 7 | 1 | 2 | 150000000 | -1 | 3780 |
| 476 | Sách Biến Sôcôla lv3 | 7 | 1 | 3 | 150000000 | -1 | 3780 |
| 477 | Sách Biến Sôcôla lv4 | 7 | 1 | 4 | 150000000 | -1 | 3780 |
| 478 | Sách Biến Sôcôla lv5 | 7 | 1 | 5 | 150000000 | -1 | 3780 |
| 479 | Sách Biến Sôcôla lv6 | 7 | 1 | 6 | 150000000 | -1 | 3780 |
| 480 | Sách Biến Sôcôla lv7 | 7 | 1 | 7 | 150000000 | -1 | 3780 |
| 481 | Sách Liên hoàn lv1 | 7 | 1 | 1 | 150000000 | -1 | 3778 |
| 482 | Sách Liên hoàn lv2 | 7 | 1 | 2 | 150000000 | -1 | 3778 |
| 483 | Sách Liên hoàn lv3 | 7 | 1 | 3 | 150000000 | -1 | 3778 |
| 484 | Sách Liên hoàn lv4 | 7 | 1 | 4 | 150000000 | -1 | 3778 |
| 485 | Sách Liên hoàn lv5 | 7 | 1 | 5 | 150000000 | -1 | 3778 |
| 486 | Sách Liên hoàn lv6 | 7 | 1 | 6 | 150000000 | -1 | 3778 |
| 487 | Sách Liên hoàn lv7 | 7 | 1 | 7 | 150000000 | -1 | 3778 |
| 488 | Sách Dịch Chuyển lv1 | 7 | 0 | 1 | 150000000 | -1 | 3783 |
| 489 | Sách Dịch Chuyển lv2 | 7 | 0 | 2 | 150000000 | -1 | 3783 |
| 490 | Sách Dịch Chuyển lv3 | 7 | 0 | 3 | 150000000 | -1 | 3783 |
| 491 | Sách Dịch Chuyển lv4 | 7 | 0 | 4 | 150000000 | -1 | 3783 |
| 492 | Sách Dịch Chuyển lv5 | 7 | 0 | 5 | 150000000 | -1 | 3783 |
| 493 | Sách Dịch Chuyển lv6 | 7 | 0 | 6 | 150000000 | -1 | 3783 |
| 494 | Sách Dịch Chuyển lv7 | 7 | 0 | 7 | 150000000 | -1 | 3783 |
| 495 | Sách Thôi Miên lv1 | 7 | 0 | 1 | 150000000 | -1 | 3782 |
| 496 | Sách Thôi Miên lv2 | 7 | 0 | 2 | 150000000 | -1 | 3782 |
| 497 | Sách Thôi Miên lv3 | 7 | 0 | 3 | 150000000 | -1 | 3782 |
| 498 | Sách Thôi Miên lv4 | 7 | 0 | 4 | 150000000 | -1 | 3782 |
| 499 | Sách Thôi Miên lv5 | 7 | 0 | 5 | 150000000 | -1 | 3782 |
| 500 | Sách Thôi Miên lv6 | 7 | 0 | 6 | 150000000 | -1 | 3782 |
| 501 | Sách Thôi Miên lv7 | 7 | 0 | 7 | 150000000 | -1 | 3782 |
| 502 | Sách Trói lv1 | 7 | 2 | 1 | 150000000 | -1 | 3779 |
| 503 | Sách Trói lv2 | 7 | 2 | 2 | 150000000 | -1 | 3779 |
| 504 | Sách Trói lv3 | 7 | 2 | 3 | 150000000 | -1 | 3779 |
| 505 | Sách Trói lv4 | 7 | 2 | 4 | 150000000 | -1 | 3779 |
| 506 | Sách Trói lv5 | 7 | 2 | 5 | 150000000 | -1 | 3779 |
| 507 | Sách Trói lv6 | 7 | 2 | 6 | 150000000 | -1 | 3779 |
| 508 | Sách Trói lv7 | 7 | 2 | 7 | 150000000 | -1 | 3779 |
| 509 | Sách Huýt Sáo lv1 | 7 | 2 | 1 | 150000000 | -1 | 3781 |
| 510 | Sách Huýt Sáo lv2 | 7 | 2 | 2 | 150000000 | -1 | 3781 |
| 511 | Sách Huýt Sáo lv3 | 7 | 2 | 3 | 150000000 | -1 | 3781 |
| 512 | Sách Huýt Sáo lv4 | 7 | 2 | 4 | 150000000 | -1 | 3781 |
| 513 | Sách Huýt Sáo lv5 | 7 | 2 | 5 | 150000000 | -1 | 3781 |
| 514 | Sách Huýt Sáo lv6 | 7 | 2 | 6 | 150000000 | -1 | 3781 |
| 515 | Sách Huýt Sáo lv7 | 7 | 2 | 7 | 150000000 | -1 | 3781 |
| 516 | Sôcôla | 27 | 3 | 1 | 0 | -1 | 4133 |
| 517 | Mở rộng hành trang | 27 | 3 | 1 | 150000000 | -1 | 4340 |
| 518 | Mở rộng rương đồ | 27 | 3 | 1 | 150000000 | -1 | 265 |
| 519 | Cờ Kaiô | 28 | 3 | 1 | 0 | -1 | 4386 |
| 520 | Cờ Mabư | 28 | 3 | 1 | 0 | -1 | 4385 |
| 521 | Tự động luyện tập | 27 | 3 | 1 | 0 | -1 | 4387 |
| 522 | Bùa Đệ Tử | 13 | 3 | 0 | 0 | -1 | 1403 |
| 523 | Đậu thần cấp 9 | 6 | 3 | 9 | 1 | -1 | 241 |
| 524 | Cải trang | 5 | 3 | 1 | 15000000 | -1 | 2560 |
| 525 | Cải trang | 5 | 3 | 0 | 15000000 | -1 | 2622 |
| 526 | Cải trang | 5 | 3 | 1 | 15000000 | -1 | 2345 |
| 527 | Cải trang | 5 | 3 | 1 | 15000000 | -1 | 2376 |
| 528 | Cải trang | 5 | 3 | 1 | 15000000 | -1 | 2407 |
| 529 | Giáp tập luyện cấp 1 | 32 | 3 | 1 | 0 | -1 | 4429 |
| 530 | Giáp tập luyện cấp 2 | 32 | 3 | 1 | 1500000 | -1 | 4430 |
| 531 | Giáp tập luyện cấp 3 | 32 | 3 | 1 | 15000000 | -1 | 4431 |
| 532 | Quỷ Chim | 24 | 3 | 1 | 15000000 | -1 | 4433 |
| 533 | Kẹo giáng sinh | 27 | 3 | 1 | 0 | -1 | 4432 |
| 534 | Giáp tập luyện cấp 1 | 32 | 3 | 1 | 0 | -1 | 4429 |
| 535 | Giáp tập luyện cấp 2 | 32 | 3 | 1 | 1500000 | -1 | 4430 |
| 536 | Giáp tập luyện cấp 3 | 32 | 3 | 1 | 15000000 | -1 | 4431 |
| 537 | Chữ giải | 27 | 3 | 0 | 0 | -1 | 1403 |
| 538 | Chữ khai | 27 | 3 | 0 | 0 | -1 | 1404 |
| 539 | Chữ phong | 27 | 3 | 0 | 0 | -1 | 1407 |
| 540 | Chữ ấn | 27 | 3 | 0 | 0 | -1 | 1406 |
| 541 | Quả Hồng Đào | 27 | 3 | 0 | 0 | -1 | 4547 |
| 542 | Quả Hồng Đào Chín | 27 | 3 | 0 | 0 | -1 | 4547 |
| 543 | Vòng Kim Cô | 27 | 3 | 0 | 0 | -1 | 4546 |
| 544 | Cải trang | 5 | 0 | 1 | 0 | -1 | 4543 |
| 545 | Cải trang | 5 | 1 | 1 | 0 | -1 | 4464 |
| 546 | Cải trang | 5 | 2 | 1 | 0 | -1 | 4466 |
| 547 | Cải trang | 5 | 3 | 1 | 0 | -1 | 4468 |
| 548 | Cải trang | 5 | 3 | 1 | 0 | -1 | 4498 |
| 549 | Cải trang | 5 | 3 | 1 | 15000000 | -1 | 2715 |
| 550 | Cải trang | 5 | 3 | 1 | 15000000 | -1 | 2592 |
| 551 | Cải trang | 5 | 3 | 1 | 15000000 | -1 | 2529 |
| 552 | Cải trang | 5 | 3 | 1 | 15000000 | -1 | 2684 |
| 553 | Pháo bông | 27 | 3 | 1 | 1000 | -1 | 4548 |
| 554 | Siêu Pháo bông x100 | 27 | 3 | 1 | 1000000 | -1 | 4548 |
| 555 | Áo Thần Linh | 0 | 0 | 13 | 0 | 472 | 4647 |
| 556 | Quần Thần Linh | 1 | 0 | 13 | 0 | 473 | 4648 |
| 557 | Áo Thần Namếc | 0 | 1 | 13 | 0 | 476 | 4657 |
| 558 | Quần Thần namếc | 1 | 1 | 13 | 0 | 477 | 4658 |
| 559 | Áo Thần Xayda | 0 | 2 | 13 | 0 | 474 | 4652 |
| 560 | Quần Thần Xayda | 1 | 2 | 13 | 0 | 475 | 4654 |
| 561 | Nhẫn Thần Linh | 4 | 3 | 13 | 0 | -1 | 6314 |
| 562 | Găng Thần Linh | 2 | 0 | 13 | 0 | -1 | 4650 |
| 563 | Giầy Thần Linh | 3 | 0 | 13 | 0 | -1 | 4649 |
| 564 | Găng Thần Namếc | 2 | 1 | 13 | 0 | -1 | 4659 |
| 565 | Giầy Thần Namếc | 3 | 1 | 13 | 0 | -1 | 4660 |
| 566 | Găng Thần Xayda | 2 | 2 | 13 | 0 | -1 | 4656 |
| 567 | Giầy Thần Xayda | 3 | 2 | 13 | 0 | -1 | 4655 |
| 568 | Quả Trứng | 27 | 3 | 1 | 0 | -1 | 2164 |
| 569 | Dưa Hấu | 27 | 3 | 1 | 0 | -1 | 4671 |
| 570 | Rương Gỗ | 27 | 3 | 1 | 1000 | -1 | 265 |
| 571 | Rương Bạc | 27 | 3 | 1 | 15000 | -1 | 5006 |
| 572 | Rương Vàng | 27 | 3 | 1 | 15000 | -1 | 5007 |
| 573 | Capsule Bạc | 27 | 3 | 1 | 15000 | -1 | 5005 |
| 574 | Capsule Vàng | 27 | 3 | 1 | 15000 | -1 | 5004 |
| 575 | Cải trang | 5 | 3 | 1 | 15000000 | -1 | 4354 |
| 576 | Cải trang | 5 | 3 | 1 | 15000000 | -1 | 4135 |
| 577 | Cải trang | 5 | 3 | 1 | 15000000 | -1 | 4165 |
| 578 | Cải trang | 5 | 3 | 1 | 15000000 | -1 | 2957 |
| 579 | Đuôi khỉ | 29 | 3 | 1 | 15000 | -1 | 5072 |
| 580 | Cải trang | 5 | 3 | 1 | 0 | -1 | 3047 |
| 581 | Cải trang | 5 | 3 | 1 | 0 | -1 | 3045 |
| 582 | Cải trang | 5 | 3 | 1 | 0 | -1 | 3014 |
| 583 | Cải trang Póc | 5 | 3 | 1 | 0 | -1 | 2467 |
| 584 | Cải trang | 5 | 3 | 1 | 1500000 | -1 | 4116 |
| 585 | Bí ngô | 27 | 3 | 1 | 0 | -1 | 5138 |
| 586 | Cải trang | 5 | 3 | 1 | 1500000 | -1 | 3469 |
| 587 | Cải trang | 5 | 3 | 1 | 1500000 | -1 | 3407 |
| 588 | Cải trang | 5 | 3 | 1 | 1500000 | -1 | 3496 |
| 589 | Bông hoa | 27 | 3 | 1 | 0 | -1 | 5206 |
| 590 | Bí kiếp | 27 | 3 | 1 | 1500000 | -1 | 5207 |
| 591 | Cải trang Siêu Thần | 5 | 3 | 1 | 0 | -1 | 5087 |
| 592 | Cải trang Yardrat | 5 | 0 | 1 | 1500000 | -1 | 5211 |
| 593 | Cải trang Yardrat | 5 | 1 | 1 | 1500000 | -1 | 5211 |
| 594 | Cải trang Yardrat | 5 | 2 | 1 | 1500000 | -1 | 5211 |
| 595 | Đậu thần cấp 10 | 6 | 3 | 10 | 1 | -1 | 241 |
| 596 | Gói 30 đậu thần cấp 8 | 27 | 3 | 1 | 0 | -1 | 241 |
| 597 | Gói 30 đậu thần cấp 9 | 27 | 3 | 1 | 0 | -1 | 241 |
| 598 | Avatar | 5 | 0 | 1 | 0 | 561 | 5235 |
| 599 | Avatar | 5 | 1 | 1 | 0 | 560 | 5233 |
| 600 | Avatar | 5 | 2 | 1 | 0 | 562 | 5237 |
| 601 | Cải trang Hợp Thể | 5 | 0 | 1 | 150000000 | -1 | 5273 |
| 602 | Cải trang Hợp Thể | 5 | 1 | 1 | 150000000 | -1 | 5306 |
| 603 | Cải trang Hợp Thể | 5 | 2 | 1 | 150000000 | -1 | 5242 |
| 604 | Cải trang VIP | 5 | 0 | 1 | 150000000 | -1 | 5304 |
| 605 | Cải trang VIP | 5 | 1 | 1 | 150000000 | -1 | 5075 |
| 606 | Cải trang VIP | 5 | 2 | 1 | 150000000 | -1 | 5079 |
| 607 | Cải trang Chan Xư | 5 | 0 | 1 | 150000000 | -1 | 3682 |
| 608 | Cải trang Lão Cận | 5 | 1 | 1 | 150000000 | -1 | 3964 |
| 609 | Cải trang Xayda | 5 | 2 | 1 | 150000000 | -1 | 4938 |
| 610 | Bông hoa | 27 | 3 | 1 | 0 | -1 | 5206 |
| 611 | Bản đồ kho báu | 27 | 3 | 1 | 0 | -1 | 5428 |
| 612 | Cải trang Arale | 5 | 3 | 1 | 150000000 | -1 | 5344 |
| 613 | Cải trang Gatchan | 5 | 3 | 1 | 150000000 | -1 | 5403 |
| 614 | Cải trang Obotchaman | 5 | 3 | 1 | 150000000 | -1 | 5372 |
| 615 | Cải trang | 5 | 3 | 1 | 0 | -1 | 1964 |
| 616 | Cải trang | 5 | 3 | 1 | 0 | -1 | 1871 |
| 617 | Cải trang | 5 | 3 | 1 | 0 | -1 | 1995 |
| 618 | Cải trang Luffy | 5 | 3 | 1 | 0 | -1 | 5432 |
| 619 | Cải trang Zoro | 5 | 3 | 1 | 0 | -1 | 5465 |
| 620 | Cải trang Sanji | 5 | 3 | 1 | 0 | -1 | 5495 |
| 621 | Cải trang Brook | 5 | 3 | 1 | 0 | -1 | 5526 |
| 622 | Cải trang Chopper | 5 | 3 | 1 | 0 | -1 | 5680 |
| 623 | Cải trang Nami | 5 | 3 | 1 | 0 | -1 | 5617 |
| 624 | Cải trang Franky | 5 | 3 | 1 | 0 | -1 | 5556 |
| 625 | Cải trang Usopp | 5 | 3 | 1 | 0 | -1 | 5587 |
| 626 | Cải trang Robin | 5 | 3 | 1 | 0 | -1 | 5649 |
| 627 | Capsule quà tặng | 27 | 3 | 1 | 0 | -1 | 2759 |
| 628 | Cải trang Hải Tặc | 5 | 3 | 1 | 0 | -1 | 5721 |
| 629 | Cải trang Fide vàng | 5 | 3 | 1 | 0 | 5722 | 4812 |
| 630 | Cải trang Frost 1 | 5 | 3 | 1 | 1500000000 | -1 | 4719 |
| 631 | Cải trang Frost 2 | 5 | 3 | 1 | 1500000000 | -1 | 4750 |
| 632 | Cải trang Frost 3 | 5 | 3 | 1 | 1500000000 | -1 | 4781 |
| 633 | Cải trang Píc | 5 | 3 | 1 | 150000000 | -1 | 1348 |
| 634 | Cải trang King kong | 5 | 3 | 1 | 150000000 | -1 | 2498 |
| 635 | Cải trang Pi láp | 5 | 3 | 1 | 0 | -1 | 5827 |
| 636 | Cải trang Mai | 5 | 3 | 1 | 0 | -1 | 5758 |
| 637 | Cải trang Su | 5 | 3 | 1 | 0 | -1 | 5789 |
| 638 | Bình chứa Commeson | 27 | 3 | 1 | 0 | -1 | 5829 |
| 639 | Cải trang Hợp Thể | 5 | 2 | 1 | 150000000 | -1 | 5834 |
| 640 | Cải trang Hợp Thể | 5 | 0 | 1 | 150000000 | -1 | 5865 |
| 641 | Cải trang Hợp Thể | 5 | 1 | 1 | 150000000 | -1 | 5898 |
| 642 | Cải trang ma trơi | 5 | 3 | 1 | 0 | -1 | 6091 |
| 643 | Cải trang dơi nhí | 5 | 3 | 1 | 0 | -1 | 6094 |
| 644 | Cải trang bộ xương | 5 | 0 | 1 | 0 | -1 | 5101 |
| 645 | Cải trang bộ xương | 5 | 1 | 1 | 0 | -1 | 5105 |
| 646 | Cải trang bộ xương | 5 | 2 | 1 | 0 | -1 | 5103 |
| 647 | Cải trang Saibamen | 5 | 3 | 1 | 0 | -1 | 5995 |
| 648 | Hộp quà giáng sinh | 27 | 3 | 1 | 0 | -1 | 6123 |
| 649 | Tất,vớ giáng sinh | 27 | 3 | 1 | 0 | -1 | 6122 |
| 650 | Áo Hủy Diệt | 0 | 0 | 14 | 0 | 674 | 6315 |
| 651 | Quần Hủy Diệt | 1 | 0 | 14 | 0 | 675 | 6316 |
| 652 | Áo Hủy Diệt | 0 | 1 | 14 | 0 | 676 | 6320 |
| 653 | Quần Hủy Diệt | 1 | 1 | 14 | 0 | 677 | 6321 |
| 654 | Áo Hủy Diệt | 0 | 2 | 14 | 0 | 672 | 6310 |
| 655 | Quần Hủy Diệt | 1 | 2 | 14 | 0 | 673 | 6311 |
| 656 | Nhẫn Hủy Diệt | 4 | 3 | 14 | 0 | -1 | 6323 |
| 657 | Găng Hủy Diệt | 2 | 0 | 14 | 0 | -1 | 6318 |
| 658 | Giầy Hủy Diệt | 3 | 0 | 14 | 0 | -1 | 6317 |
| 659 | Găng Hủy Diệt | 2 | 1 | 14 | 0 | -1 | 6318 |
| 660 | Giầy Hủy Diệt | 3 | 1 | 14 | 0 | -1 | 6322 |
| 661 | Găng Hủy Diệt | 2 | 2 | 14 | 0 | -1 | 6318 |
| 662 | Giầy Hủy Diệt | 3 | 2 | 14 | 0 | -1 | 6312 |
| 663 | Bánh Pudding | 29 | 3 | 14 | 0 | -1 | 6324 |
| 664 | Xúc xích | 29 | 3 | 14 | 0 | -1 | 6325 |
| 665 | Kem dâu | 29 | 3 | 14 | 0 | -1 | 6326 |
| 666 | Mì ly | 29 | 3 | 14 | 0 | -1 | 6327 |
| 667 | Sushi | 29 | 3 | 14 | 0 | -1 | 6328 |
| 668 | Hộp quà Tết 2019 | 27 | 3 | 1 | 0 | -1 | 2987 |
| 669 | Dưa Hấu | 29 | 3 | 1 | 0 | -1 | 1040 |
| 670 | Cà rốt | 29 | 3 | 1 | 0 | -1 | 4083 |
| 671 | Bùa Trí Tuệ x3 | 13 | 3 | 0 | 0 | -1 | 1406 |
| 672 | Bùa Trí Tuệ x4 | 13 | 3 | 0 | 0 | -1 | 1407 |
| 673 | abcd | 27 | 3 | 0 | 0 | -1 | 15313 |
| 674 | Đá ngũ sắc | 27 | 3 | 1 | 0 | -1 | 6467 |
| 675 | Cải trang | 5 | 3 | 1 | 0 | -1 | 6473 |
| 676 | Cải trang | 5 | 3 | 2 | 0 | -1 | 6369 |
| 677 | Cải trang | 5 | 3 | 3 | 0 | -1 | 6470 |
| 678 | Cải trang | 5 | 3 | 4 | 0 | -1 | 6400 |
| 679 | Cải trang | 5 | 3 | 5 | 0 | -1 | 6338 |
| 680 | Cải trang | 5 | 3 | 6 | 0 | -1 | 6431 |
| 681 | Cải trang | 5 | 3 | 7 | 0 | -1 | 6329 |
| 682 | Avatar | 5 | 1 | 1 | 0 | 112 | 1350 |
| 683 | Avatar | 5 | 1 | 2 | 0 | 111 | 1352 |
| 684 | Avatar | 5 | 1 | 3 | 0 | 113 | 1354 |
| 685 | Avatar | 5 | 2 | 1 | 0 | 101 | 1330 |
| 686 | Avatar | 5 | 2 | 2 | 0 | 103 | 1334 |
| 687 | Avatar | 5 | 2 | 3 | 0 | 107 | 1344 |
| 688 | Avatar | 5 | 0 | 1 | 0 | 106 | 1342 |
| 689 | Avatar | 5 | 0 | 2 | 0 | 108 | 1346 |
| 690 | Avatar | 5 | 0 | 3 | 0 | 110 | 1348 |
| 691 | Quần đi biển | 1 | 0 | 1 | 0 | 700 | 6569 |
| 692 | Quần đi biển | 1 | 1 | 1 | 0 | 702 | 6569 |
| 693 | Quần đi biển | 1 | 2 | 1 | 0 | 700 | 6569 |
| 694 | Trái dừa | 29 | 3 | 1 | 0 | -1 | 6574 |
| 695 | Vỏ ốc | 27 | 3 | 1 | 0 | -1 | 6570 |
| 696 | Vỏ sò | 27 | 3 | 1 | 0 | -1 | 6573 |
| 697 | Con cua | 27 | 3 | 1 | 0 | -1 | 6571 |
| 698 | Sao biển | 27 | 3 | 1 | 0 | -1 | 6572 |
| 699 | HP +400K | 27 | 3 | 1 | 0 | -1 | 2755 |
| 700 | Sức đánh +20K | 27 | 3 | 1 | 0 | -1 | 2754 |
| 701 | KI +400K | 27 | 3 | 1 | 0 | -1 | 2756 |
| 702 | Bí ngô 1 sao | 12 | 3 | 0 | 0 | -1 | 6579 |
| 703 | Bí ngô 2 sao | 12 | 3 | 0 | 0 | -1 | 6580 |
| 704 | Bí ngô 3 sao | 12 | 3 | 0 | 0 | -1 | 6581 |
| 705 | Bí ngô 4 sao | 12 | 3 | 0 | 0 | -1 | 6582 |
| 706 | Bí ngô 5 sao | 12 | 3 | 0 | 0 | -1 | 6583 |
| 707 | Bí ngô 6 sao | 12 | 3 | 0 | 0 | -1 | 6584 |
| 708 | Bí ngô 7 sao | 12 | 3 | 0 | 0 | -1 | 6585 |
| 709 | Bông hoa | 27 | 3 | 1 | 0 | -1 | 5206 |
| 710 | Cải trang | 5 | 3 | 1 | 0 | -1 | 5175 |
| 711 | Cải trang | 5 | 3 | 1 | 0 | -1 | 3527 |
| 712 | Truy | 27 | 3 | 1 | 0 | -1 | 1409 |
| 713 | Bắt | 27 | 3 | 1 | 0 | -1 | 1407 |
| 714 | Đại | 27 | 3 | 1 | 0 | -1 | 1406 |
| 715 | Vương | 27 | 3 | 1 | 0 | -1 | 1405 |
| 716 | Urôn | 27 | 3 | 1 | 0 | -1 | 1404 |
| 717 | Bao Lì Xì | 27 | 3 | 1 | 0 | -1 | 6764 |
| 718 | Vé tặng ngọc | 27 | 3 | 1 | 0 | -1 | 6765 |
| 719 | Cải trang | 5 | 3 | 1 | 0 | -1 | 6734 |
| 720 | Mảnh cải trang | 27 | 3 | 1 | 0 | -1 | 6777 |
| 721 | Phiếu giảm giá VIP | 27 | 3 | 1 | 0 | -1 | 4030 |
| 722 | Capsule hồng | 27 | 3 | 1 | 0 | -1 | 6782 |
| 723 | Bông hồng | 27 | 3 | 1 | 0 | -1 | 5206 |
| 724 | Cải trang | 5 | 3 | 1 | 0 | -1 | 6783 |
| 725 | Siêu thần thủy | 27 | 3 | 1 | 1000000 | -1 | 6848 |
| 726 | Mảnh giấy có chữ MA | 27 | 3 | 1 | 1000000 | -1 | 6847 |
| 727 | Siêu thần thủy | 27 | 3 | 1 | 100000000 | -1 | 6849 |
| 728 | Siêu thần thủy | 27 | 3 | 1 | 1000000000 | -1 | 6850 |
| 729 | Cải trang Hatchiyack | 5 | 3 | 1 | 0 | -1 | 5963 |
| 730 | Cải trang Sói Basil | 5 | 3 | 1 | 0 | -1 | 6914 |
| 731 | Cải trang Sói Lavender | 5 | 3 | 1 | 0 | -1 | 6917 |
| 732 | Cải trang Sói Bergamo | 5 | 3 | 1 | 0 | -1 | 6949 |
| 733 | Cân đẩu vân ngũ sắc | 23 | 3 | 1 | 0 | 1 | 6980 |
| 734 | Ngọc Thố | 23 | 3 | 1 | 0 | 2 | 6981 |
| 735 | Lồng đèn cá chép | 23 | 3 | 1 | 0 | 3 | 6982 |
| 736 | Hộp quà 5 sao | 27 | 3 | 1 | 0 | -1 | 6983 |
| 737 | Capsule Trung Thu | 27 | 3 | 1 | 0 | -1 | 6984 |
| 738 | Cải trang Dr Lychee | 5 | 3 | 1 | 0 | -1 | 6852 |
| 739 | Cải trang Bill Bí Ngô | 5 | 3 | 1 | 0 | -1 | 6985 |
| 740 | Lưỡi hái Thần Chết | 11 | 3 | 1 | 0 | 37 | 7019 |
| 741 | Cánh dơi Dracula | 11 | 3 | 1 | 0 | 38 | 7022 |
| 742 | Cải trang Caufila Dơi | 5 | 3 | 1 | 0 | -1 | 7023 |
| 743 | Chổi bay Phù Thủy | 23 | 3 | 1 | 0 | 4 | 7056 |
| 744 | Cột nhà | 23 | 3 | 1 | 0 | 5 | 7063 |
| 745 | Bông tuyết | 11 | 3 | 1 | 0 | 39 | 7066 |
| 746 | Xe tuần lộc | 23 | 3 | 1 | 0 | 6 | 7067 |
| 747 | Cờ xanh dương | 28 | 3 | 1 | 0 | -1 | 2325 |
| 748 | Thịt heo | 27 | 3 | 1 | 0 | -1 | 7068 |
| 749 | Thúng nếp | 27 | 3 | 1 | 0 | -1 | 7069 |
| 750 | Thúng đậu xanh | 27 | 3 | 1 | 0 | -1 | 7070 |
| 751 | Lá dong | 27 | 3 | 1 | 0 | -1 | 7071 |
| 752 | Bánh tét | 31 | 3 | 1 | 0 | -1 | 7079 |
| 753 | Bánh chưng | 31 | 3 | 1 | 0 | -1 | 7080 |
| 754 | Nón Chuột may mắn | 5 | 0 | 1 | 0 | 769 | 7085 |
| 755 | Nón Chuột may mắn | 5 | 1 | 1 | 0 | 771 | 7091 |
| 756 | Nón Chuột may mắn | 5 | 2 | 1 | 0 | 770 | 7088 |
| 757 | Túi Canh Tý 2020 | 27 | 3 | 1 | 0 | -1 | 7097 |
| 758 | Capsule Tết 2024 | 27 | 3 | 1 | 0 | -1 | 5004 |
| 759 | Nâng kỹ năng 4 đệ tử | 27 | 3 | 1 | 0 | -1 | 7101 |
| 760 | Cải trang VIP | 5 | 3 | 1 | 0 | -1 | 7102 |
| 761 | Avatar đeo khẩu trang | 5 | 0 | 1 | 0 | 775 | 7134 |
| 762 | Avatar đeo khẩu trang | 5 | 1 | 1 | 0 | 777 | 7140 |
| 763 | Avatar đeo khẩu trang | 5 | 2 | 1 | 0 | 776 | 7137 |
| 764 | Khẩu trang | 29 | 3 | 1 | 0 | -1 | 7149 |
| 765 | Cải trang Gohan Bư | 5 | 3 | 1 | 0 | -1 | 4261 |
| 766 | Cờ xám | 11 | 3 | 1 | 1500000 | 0 | 1027 |
| 767 | Cờ đen | 11 | 3 | 1 | 0 | 1 | 1026 |
| 768 | Cờ xanh lá | 11 | 3 | 1 | 0 | 2 | 1033 |
| 769 | Cờ xanh biển | 11 | 3 | 1 | 0 | 3 | 1032 |
| 770 | Cờ hồng | 11 | 3 | 1 | 0 | 4 | 1028 |
| 771 | Cờ cam | 11 | 3 | 1 | 0 | 5 | 1029 |
| 772 | Cờ vàng | 11 | 3 | 1 | 0 | 6 | 1031 |
| 773 | Cờ tím | 11 | 3 | 1 | 0 | 7 | 1030 |
| 774 | Cờ xanh dạ | 11 | 3 | 1 | 0 | 8 | 1025 |
| 775 | Cờ đỏ | 11 | 3 | 1 | 0 | 9 | 1034 |
| 776 | Khăn xanh lá | 11 | 3 | 1 | 0 | 10 | 1063 |
| 777 | Khăn xanh dương | 11 | 3 | 1 | 0 | 11 | 1064 |
| 778 | Khăn vàng | 11 | 3 | 1 | 0 | 12 | 1065 |
| 779 | Khăn tím | 11 | 3 | 1 | 0 | 13 | 1066 |
| 780 | Khăn nâu | 11 | 3 | 1 | 0 | 14 | 1067 |
| 781 | Khăn xám | 11 | 3 | 1 | 0 | 15 | 1070 |
| 782 | Khăn đỏ | 11 | 3 | 1 | 0 | 16 | 1072 |
| 783 | Khăn hồng | 11 | 3 | 1 | 0 | 17 | 1068 |
| 784 | Khăn xanh dạ | 11 | 3 | 1 | 0 | 18 | 1069 |
| 785 | Ba lô | 11 | 3 | 1 | 0 | 19 | 1038 |
| 786 | Đao | 11 | 3 | 1 | 0 | 20 | 1044 |
| 787 | Gậy | 11 | 3 | 1 | 0 | 21 | 1039 |
| 788 | Mai rùa | 11 | 3 | 1 | 0 | 22 | 1035 |
| 789 | Giỏ bơ | 11 | 3 | 1 | 0 | 23 | 1043 |
| 790 | Giỏ dưa hấu | 11 | 3 | 1 | 0 | 24 | 1040 |
| 791 | Giỏ củ cải trắng | 11 | 3 | 1 | 0 | 25 | 1036 |
| 792 | Giỏ cà rốt | 11 | 3 | 1 | 0 | 26 | 1037 |
| 793 | Giỏ chuối | 11 | 3 | 1 | 0 | 27 | 1042 |
| 794 | Gậy phép | 11 | 3 | 1 | 0 | 29 | 1084 |
| 795 | Ghế bay | 23 | 3 | 1 | 0 | 7 | 7221 |
| 796 | Hộp Capsule | 27 | 3 | 1 | 0 | -1 | 7222 |
| 797 | Bùa Trí Tuệ | 13 | 3 | 0 | 0 | -1 | 1403 |
| 798 | Bùa Mạnh Mẽ | 13 | 3 | 0 | 0 | -1 | 1404 |
| 799 | Bùa Da Trâu | 13 | 3 | 0 | 0 | -1 | 1405 |
| 800 | Lồng đèn Cô Vy | 11 | 3 | 1 | 0 | 40 | 7226 |
| 801 | Lồng đèn Con tàu | 11 | 3 | 1 | 0 | 41 | 7227 |
| 802 | Lồng đèn Con gà | 11 | 3 | 1 | 0 | 42 | 7230 |
| 803 | Lồng đèn Con bướm | 11 | 3 | 1 | 0 | 43 | 7233 |
| 804 | Lồng đèn Đôrêmon | 11 | 3 | 1 | 0 | 44 | 7236 |
| 805 | Vòng sáng thiên thần | 11 | 3 | 1 | 0 | 45 | 7334 |
| 806 | Cải trang Đôrêmon | 5 | 3 | 1 | 0 | -1 | 7189 |
| 807 | Ngọc đen 1 sao | 12 | 3 | 0 | 0 | -1 | 7337 |
| 808 | Ngọc đen 2 sao | 12 | 3 | 0 | 0 | -1 | 7338 |
| 809 | Ngọc đen 3 sao | 12 | 3 | 0 | 0 | -1 | 7339 |
| 810 | Ngọc đen 4 sao | 12 | 3 | 0 | 0 | -1 | 7340 |
| 811 | Ngọc đen 5 sao | 12 | 3 | 0 | 0 | -1 | 7341 |
| 812 | Ngọc đen 6 sao | 12 | 3 | 0 | 0 | -1 | 7342 |
| 813 | Ngọc đen 7 sao | 12 | 3 | 0 | 0 | -1 | 7343 |
| 814 | Ma trơi | 11 | 3 | 1 | 0 | 46 | 7352 |
| 815 | Hồn ma Goku | 11 | 3 | 1 | 0 | 47 | 7353 |
| 816 | Hồn ma Ca đíc | 11 | 3 | 1 | 0 | 48 | 7354 |
| 817 | Hồn ma Pôcôlô | 11 | 3 | 1 | 0 | 49 | 7355 |
| 818 | Capsule Halloween | 27 | 3 | 1 | 0 | -1 | 7356 |
| 819 | Cải trang Xuka | 5 | 3 | 1 | 0 | -1 | 7357 |
| 820 | Vé quay ngọc đen | 27 | 3 | 1 | 0 | -1 | 7389 |
| 821 | Vé quay ngọc vàng | 27 | 3 | 1 | 0 | -1 | 7390 |
| 822 | Cây thông | 11 | 3 | 1 | 0 | 50 | 7395 |
| 823 | Túi quà | 11 | 3 | 1 | 0 | 51 | 7396 |
| 824 | Cải trang Noel | 5 | 0 | 1 | 0 | -1 | 7426 |
| 825 | Cải trang Noel | 5 | 1 | 1 | 0 | -1 | 7430 |
| 826 | Cải trang Noel | 5 | 2 | 1 | 0 | -1 | 7428 |
| 827 | Cải trang Noel | 5 | 3 | 1 | 0 | -1 | 7435 |
| 828 | Mảnh Khủng long | 33 | 3 | 1 | 0 | -1 | 7467 |
| 829 | Mảnh Lợn lòi | 33 | 3 | 1 | 0 | -1 | 7468 |
| 830 | Mảnh Quỷ đất | 33 | 3 | 1 | 0 | -1 | 7469 |
| 831 | Mảnh Khủng long mẹ | 33 | 3 | 1 | 0 | -1 | 7470 |
| 832 | Mảnh Lợn lòi mẹ | 33 | 3 | 1 | 0 | -1 | 7471 |
| 833 | Mảnh Quỷ đất mẹ | 33 | 3 | 1 | 0 | -1 | 7472 |
| 834 | Mảnh Thằn lằn bay | 33 | 3 | 1 | 0 | -1 | 7473 |
| 835 | Mảnh Phi long | 33 | 3 | 1 | 0 | -1 | 7474 |
| 836 | Mảnh Quỷ bay | 33 | 3 | 1 | 0 | -1 | 7475 |
| 837 | Mảnh Lính độc nhãn | 33 | 3 | 1 | 0 | -1 | 7476 |
| 838 | Mảnh Lính độc nhãn | 33 | 3 | 1 | 0 | -1 | 7477 |
| 839 | Mảnh Sói xám | 33 | 3 | 1 | 0 | -1 | 7478 |
| 840 | Mảnh Trung úy Trắng | 33 | 3 | 1 | 0 | -1 | 7480 |
| 841 | Mảnh Ninja Áo Tím | 33 | 3 | 1 | 0 | -1 | 7481 |
| 842 | Mảnh Trung úy Xanh Lơ | 33 | 3 | 1 | 0 | -1 | 7479 |
| 843 | Cải trang Trâu vàng | 5 | 0 | 1 | 0 | -1 | 7489 |
| 844 | Cải trang Trâu đen | 5 | 1 | 1 | 0 | -1 | 7551 |
| 845 | Cải trang Trâu trắng | 5 | 2 | 1 | 0 | -1 | 7520 |
| 846 | Nón Trâu may mắn | 5 | 0 | 1 | 0 | 825 | 7585 |
| 847 | Nón Trâu may mắn | 5 | 1 | 1 | 0 | 827 | 7589 |
| 848 | Nón Trâu may mắn | 5 | 2 | 1 | 0 | 826 | 7587 |
| 849 | Pháo Thăng Thiên | 23 | 3 | 1 | 0 | 8 | 4548 |
| 850 | Cỏ tươi | 27 | 3 | 1 | 0 | -1 | 7594 |
| 851 | Cỏ tươi | 27 | 3 | 1 | 0 | -1 | 7594 |
| 852 | Cây trúc | 11 | 3 | 1 | 0 | 52 | 7597 |
| 853 | Cải trang Trâu Nâu | 5 | 0 | 1 | 0 | -1 | 7641 |
| 854 | Cải trang Trâu Nâu | 5 | 1 | 1 | 0 | -1 | 7645 |
| 855 | Cải trang Trâu Nâu | 5 | 2 | 1 | 0 | -1 | 7643 |
| 856 | Cải trang Trâu Đốm | 5 | 0 | 1 | 0 | -1 | 7600 |
| 857 | Cải trang Trâu Đốm | 5 | 1 | 1 | 0 | -1 | 7604 |
| 858 | Cải trang Trâu Đốm | 5 | 2 | 1 | 0 | -1 | 7602 |
| 859 | Mảnh Độc Nhãn | 33 | 3 | 1 | 0 | -1 | 1568 |
| 860 | Cải trang Mị Nương | 5 | 3 | 0 | 0 | -1 | 7711 |
| 861 | Hồng ngọc | 34 | 3 | 1 | 0 | -1 | 7743 |
| 862 | Cải trang Nobita | 5 | 3 | 1 | 0 | -1 | 7744 |
| 863 | Cải trang Xekô | 5 | 3 | 1 | 0 | -1 | 7808 |
| 864 | Cải trang Chaien | 5 | 3 | 1 | 0 | -1 | 7776 |
| 865 | Kiếm Z | 11 | 3 | 1 | 0 | 53 | 7842 |
| 866 | Avatar | 5 | 0 | 0 | 0 | 855 | 7847 |
| 867 | Avatar | 5 | 1 | 0 | 0 | 854 | 7846 |
| 868 | Avatar | 5 | 2 | 0 | 0 | 853 | 7843 |
| 869 | Capsule 1 sao | 27 | 3 | 1 | 0 | -1 | 7852 |
| 870 | Capsule 2 sao | 27 | 3 | 1 | 0 | -1 | 7853 |
| 871 | Capsule 3 sao | 27 | 3 | 1 | 0 | -1 | 7854 |
| 872 | Avatar Gohan 1 | 5 | 0 | 0 | 0 | 856 | 7855 |
| 873 | Avatar Gohan 2 | 5 | 0 | 0 | 0 | 857 | 7858 |
| 874 | Rùa con | 27 | 3 | 1 | 0 | -1 | 7861 |
| 875 | Cải trang Cadic | 5 | 2 | 1 | 0 | -1 | 7862 |
| 876 | Cải trang Gohan | 5 | 0 | 1 | 0 | -1 | 7894 |
| 877 | Cải trang Pocolo | 5 | 1 | 1 | 0 | -1 | 7926 |
| 878 | Cải trang Cooler vàng | 5 | 3 | 1 | 0 | -1 | 6587 |
| 879 | Cải trang Thống Chế Kilo | 5 | 3 | 1 | 0 | -1 | 6618 |
| 880 | Cua rang me | 29 | 3 | 14 | 0 | -1 | 8060 |
| 881 | Bạch tuộc nướng | 29 | 3 | 14 | 0 | -1 | 8061 |
| 882 | Tôm tẩm bột chiên xù | 29 | 3 | 14 | 0 | -1 | 8062 |
| 883 | Cải trang Black Gohan Rose | 5 | 3 | 1 | 0 | -1 | 8095 |
| 884 | Cải trang Hit | 5 | 3 | 1 | 0 | -1 | 4969 |
| 885 | CT Lích Tên béo | 5 | 3 | 1 | 0 | -1 | 8147 |
| 886 | Trứng vịt muối | 27 | 3 | 1 | 0 | -1 | 8130 |
| 887 | Gà quay nguyên con | 27 | 3 | 1 | 0 | -1 | 8129 |
| 888 | Bột mì | 27 | 3 | 1 | 0 | -1 | 7069 |
| 889 | Đậu xanh | 27 | 3 | 1 | 0 | -1 | 7070 |
| 890 | Bánh trung thu Gà quay | 27 | 3 | 1 | 0 | -1 | 8132 |
| 891 | Bánh trung thu thập cẩm | 27 | 3 | 1 | 0 | -1 | 8131 |
| 892 | Thỏ xám | 27 | 3 | 1 | 0 | -1 | 8139 |
| 893 | Thỏ trắng | 27 | 3 | 1 | 0 | -1 | 8146 |
| 894 | Avatar | 5 | 0 | 1 | 0 | 900 | 8201 |
| 895 | Avatar | 5 | 1 | 1 | 0 | 902 | 8205 |
| 896 | Avatar | 5 | 2 | 1 | 0 | 901 | 8203 |
| 897 | Rùa bay | 23 | 3 | 1 | 0 | 9 | 8210 |
| 898 | Cải trang Zamasu | 5 | 3 | 1 | 0 | -1 | 8211 |
| 899 | Kẹo một mắt | 29 | 3 | 1 | 0 | -1 | 8243 |
| 900 | Súp bí hắc ám | 29 | 3 | 1 | 0 | -1 | 8244 |
| 901 | Kẹo bàn tay | 27 | 3 | 1 | 0 | -1 | 8245 |
| 902 | Bánh gato nhện | 29 | 3 | 1 | 0 | -1 | 8246 |
| 903 | Hamburger sâu | 29 | 3 | 1 | 0 | -1 | 8247 |
| 904 | Cải trang Black Goku SSJ White | 5 | 3 | 1 | 0 | -1 | 8248 |
| 905 | Cải trang Siêu Thần Trái Đất | 5 | 0 | 1 | 0 | -1 | 8283 |
| 906 | Cải trang Mighty Mask | 5 | 3 | 1 | 0 | -1 | 8251 |
| 907 | Cải trang Siêu thần Namếc | 5 | 1 | 1 | 0 | -1 | 8315 |
| 908 | Ma phong ba | 27 | 3 | 1 | 0 | -1 | 8185 |
| 909 | Thần chết cute | 27 | 3 | 1 | 0 | -1 | 8200 |
| 910 | Bí ngô nhí nhảnh | 27 | 3 | 1 | 0 | -1 | 8192 |
| 911 | Cải trang Siêu Thần Xayda | 5 | 2 | 1 | 0 | -1 | 5087 |
| 912 | Cải trang Gohan áo xanh | 5 | 3 | 1 | 0 | -1 | 8347 |
| 913 | Cải trang Póc áo đỏ | 5 | 3 | 1 | 0 | -1 | 8379 |
| 914 | Cải trang Búp bê Arale | 5 | 3 | 1 | 0 | -1 | 8411 |
| 915 | Capsule Squid Game | 27 | 3 | 1 | 0 | -1 | 8443 |
| 916 | Lính bảo vệ tam giác | 27 | 3 | 1 | 0 | -1 | 8446 |
| 917 | Lính bảo vệ vuông | 27 | 3 | 1 | 0 | -1 | 8445 |
| 918 | Lính bảo vệ tròn | 27 | 3 | 1 | 0 | -1 | 8444 |
| 919 | Búp bê | 27 | 3 | 1 | 0 | -1 | 8472 |
| 920 | Gậy như ý | 23 | 3 | 1 | 0 | 10 | 8482 |
| 921 | Bông tai Porata | 27 | 3 | 1 | 1500000 | -1 | 7993 |
| 922 | Cải trang Goku Noel | 5 | 0 | 1 | 0 | -1 | 8483 |
| 923 | Cải trang Pico Noel | 5 | 1 | 1 | 0 | -1 | 8545 |
| 924 | Cải trang Ca đíc Noel | 5 | 2 | 1 | 0 | -1 | 8514 |
| 925 | Ngọc rồng băng 1 sao | 12 | 3 | 0 | 0 | -1 | 8579 |
| 926 | Ngọc rồng băng 2 sao | 12 | 3 | 0 | 0 | -1 | 8580 |
| 927 | Ngọc rồng băng 3 sao | 12 | 3 | 0 | 0 | -1 | 8581 |
| 928 | Ngọc rồng băng 4 sao | 12 | 3 | 0 | 0 | -1 | 8582 |
| 929 | Ngọc rồng băng 5 sao | 12 | 3 | 0 | 0 | -1 | 8583 |
| 930 | Ngọc rồng băng 6 sao | 12 | 3 | 0 | 0 | -1 | 8584 |
| 931 | Ngọc rồng băng 7 sao | 12 | 3 | 0 | 0 | -1 | 8585 |
| 932 | Cải trang Uub | 5 | 3 | 1 | 0 | -1 | 8586 |
| 933 | Mảnh vỡ bông tai | 27 | 3 | 1 | 0 | -1 | 8618 |
| 934 | Mảnh hồn bông tai | 27 | 3 | 1 | 0 | -1 | 8619 |
| 935 | Đá xanh lam | 27 | 3 | 1 | 0 | -1 | 8620 |
| 936 | Tuần lộc nhí | 27 | 3 | 1 | 0 | -1 | 8652 |
| 937 | Cải trang Mabư Noel | 5 | 3 | 1 | 0 | -1 | 8621 |
| 938 | Cải trang Goku thời trang | 5 | 0 | 1 | 0 | -1 | 8662 |
| 939 | Cải trang Pôcôlô thời trang | 5 | 1 | 1 | 0 | -1 | 8724 |
| 940 | Cải trang Ca Đíc thời trang | 5 | 2 | 1 | 0 | -1 | 8693 |
| 941 | Nón Hổ vàng | 5 | 3 | 1 | 0 | 965 | 8758 |
| 942 | Hổ mặp vàng | 27 | 3 | 1 | 0 | -1 | 8768 |
| 943 | Hổ mặp trắng | 27 | 3 | 1 | 0 | -1 | 8776 |
| 944 | Hổ mặp xanh | 27 | 3 | 1 | 0 | -1 | 8784 |
| 945 | Cải trang Goku Tarzan | 5 | 3 | 1 | 0 | -1 | 8785 |
| 946 | Nón Hổ trắng | 5 | 3 | 1 | 0 | 979 | 8818 |
| 947 | Nón Hổ xanh | 5 | 3 | 1 | 0 | 981 | 8824 |
| 948 | Cải trang Hổ vàng | 5 | 3 | 1 | 0 | -1 | 8828 |
| 949 | Thịt tươi | 27 | 3 | 1 | 0 | -1 | 7068 |
| 950 | Thịt tươi | 27 | 3 | 1 | 0 | -1 | 7068 |
| 951 | Cải trang Siêu Goku Tarzan | 5 | 3 | 1 | 0 | -1 | 8924 |
| 952 | Cải trang Hổ trắng | 5 | 3 | 1 | 0 | -1 | 8860 |
| 953 | Cải trang Hổ xanh | 5 | 3 | 1 | 0 | -1 | 8891 |
| 954 | Bó Hoa Hồng | 11 | 3 | 1 | 0 | 54 | 8933 |
| 955 | Bó Hoa Vàng | 11 | 3 | 1 | 0 | 55 | 8934 |
| 956 | Mảnh Đội trưởng Vàng | 33 | 3 | 1 | 0 | -1 | 8935 |
| 957 | Cải trang Goku võ sĩ | 5 | 0 | 1 | 0 | -1 | 8939 |
| 958 | Cải trang Pôcôlô võ sĩ | 5 | 1 | 1 | 0 | -1 | 9001 |
| 959 | Cải trang Cađíc võ sĩ | 5 | 2 | 1 | 0 | -1 | 8970 |
| 960 | Cờ trắng | 28 | 3 | 1 | 0 | -1 | 9066 |
| 961 | Cờ đen | 28 | 3 | 1 | 0 | -1 | 9065 |
| 962 | Cap thời trang 5 ngày | 27 | 3 | 1 | 0 | -1 | 9109 |
| 963 | Cap thời trang 7 ngày | 27 | 3 | 1 | 0 | -1 | 9109 |
| 964 | Sao pha lê đen cấp 2 | 30 | 3 | 8 | 1 | -1 | 9176 |
| 965 | Sao pha lê trắng cấp 2 | 30 | 3 | 9 | 1 | -1 | 9175 |
| 966 | Trái bóng | 11 | 3 | 1 | 0 | 56 | 9396 |
| 967 | Sao la | 27 | 3 | 1 | 0 | -1 | 9372 |
| 968 | Số 7 Goku | 5 | 3 | 1 | 0 | -1 | 9383 |
| 969 | Số 11 Gohan | 5 | 3 | 1 | 0 | -1 | 9387 |
| 970 | Số 6 Cađíc | 5 | 3 | 1 | 0 | -1 | 9385 |
| 971 | Số 2 Krilin | 5 | 3 | 1 | 0 | -1 | 9208 |
| 972 | Số 4 Thên Xin Hăng | 5 | 3 | 1 | 0 | -1 | 9326 |
| 973 | Số 10 Pôcôlô | 5 | 3 | 1 | 0 | -1 | 9207 |
| 974 | Số 1 Mabư | 5 | 3 | 1 | 0 | -1 | 9389 |
| 975 | Số 8 Biden | 5 | 3 | 1 | 0 | -1 | 9390 |
| 976 | Số 9 Quy lão Kamê | 5 | 3 | 1 | 0 | -1 | 9395 |
| 977 | Số 3 Poc | 5 | 3 | 1 | 0 | -1 | 9393 |
| 978 | Số 5 Pic | 5 | 3 | 1 | 0 | -1 | 9358 |
| 979 | Huy chương đồng | 27 | 3 | 1 | 0 | -1 | 9397 |
| 980 | Huy chương bạc | 27 | 3 | 1 | 0 | -1 | 9398 |
| 981 | Huy chương vàng | 27 | 3 | 1 | 0 | -1 | 9399 |
| 982 | Cúp vàng | 11 | 3 | 1 | 0 | 57 | 9402 |
| 983 | Cờ cổ động | 11 | 3 | 1 | 0 | 58 | 9403 |
| 984 | Capsule SEA games | 27 | 3 | 1 | 0 | -1 | 9109 |
| 985 | Cải trang Chill | 5 | 3 | 1 | 0 | -1 | 9405 |
| 986 | Cải trang Chill cấp 2 | 5 | 3 | 1 | 0 | -1 | 9142 |
| 987 | Đá bảo vệ | 27 | 3 | 1 | 0 | -1 | 9406 |
| 988 | Mở rộng túi vàng | 27 | 3 | 1 | 0 | -1 | 9432 |
| 989 | Cải trang Gohan Siêu Nhân | 5 | 3 | 1 | 0 | -1 | 9407 |
| 990 | Cải trang Biđen Siêu Nhân | 5 | 3 | 1 | 0 | -1 | 9433 |
| 991 | Cải trang Bản Cô Nương Siêu Nhân | 5 | 3 | 1 | 0 | -1 | 9458 |
| 992 | Nhẫn thời không sai lệch | 27 | 3 | 1 | 0 | -1 | 9067 |
| 993 | Giỏ thức ăn | 27 | 3 | 1 | 0 | -1 | 9489 |
| 994 | Vỏ ốc | 11 | 3 | 1 | 0 | 60 | 9602 |
| 995 | Cây kem | 11 | 3 | 1 | 0 | 61 | 9605 |
| 996 | Cá heo | 11 | 3 | 1 | 0 | 62 | 9608 |
| 997 | Con diều | 11 | 3 | 1 | 0 | 63 | 9611 |
| 998 | Diều rồng | 11 | 3 | 1 | 0 | 64 | 9614 |
| 999 | Mèo mun | 11 | 3 | 1 | 0 | 65 | 9624 |
| 1000 | Xiên cá | 11 | 3 | 1 | 0 | 66 | 9627 |
| 1001 | Phóng lợn | 11 | 3 | 1 | 0 | 67 | 9630 |
| 1002 | Cá nóc | 27 | 3 | 1 | 0 | -1 | 9595 |
| 1003 | Cá bảy màu | 27 | 3 | 1 | 0 | -1 | 9596 |
| 1004 | Cá diêu hồng | 27 | 3 | 1 | 0 | -1 | 9597 |
| 1005 | Xô cá xanh | 27 | 3 | 1 | 0 | -1 | 9598 |
| 1006 | Xô cá vàng | 27 | 3 | 1 | 0 | -1 | 9599 |
| 1007 | Ván lướt sóng | 11 | 3 | 1 | 0 | 68 | 9594 |
| 1008 | Cua đỏ | 27 | 3 | 1 | 0 | -1 | 9621 |
| 1009 | Rương Tranh ngọc Namếc | 27 | 3 | 1 | 15000 | -1 | 5007 |
| 1010 | Cải trang Áo vịt cam | 5 | 0 | 1 | 0 | -1 | 9497 |
| 1011 | Cải trang Áo trắng hoa | 5 | 2 | 1 | 0 | -1 | 9528 |
| 1012 | Cải trang Nón rơm mùa hè | 5 | 1 | 1 | 0 | -1 | 9556 |
| 1013 | Kiếm ánh sáng | 11 | 3 | 1 | 0 | 69 | 9649 |
| 1014 | Rađa dò ngọc Namếc | 27 | 3 | 1 | 0 | -1 | 9651 |
| 1015 | Ngọc rồng Siêu Cấp | 27 | 3 | 1 | 0 | -1 | 9650 |
| 1016 | Thuốc mỡ Ipana | 29 | 3 | 1 | 0 | -1 | 9068 |
| 1017 | Thuốc mỡ Ipana đặc biệt | 29 | 3 | 1 | 0 | -1 | 9068 |
| 1018 | Cải trang Broly | 5 | 0 | 1 | 0 | -1 | 9718 |
| 1019 | Cải trang Broly | 5 | 1 | 1 | 0 | -1 | 9780 |
| 1020 | Cải trang Broly | 5 | 2 | 1 | 0 | -1 | 9749 |
| 1021 | Búa Mjolnir | 11 | 3 | 1 | 0 | 70 | 9815 |
| 1022 | Búa Stormbreaker | 11 | 3 | 1 | 0 | 71 | 9814 |
| 1023 | Quạt ba tiêu | 11 | 3 | 1 | 0 | 72 | 9816 |
| 1024 | Như Ý Kim Cô Bổng | 11 | 3 | 1 | 0 | 73 | 9704 |
| 1025 | Cửu Xỉ Đinh Ba | 11 | 3 | 1 | 0 | 74 | 9706 |
| 1026 | Nguyệt Nha Sản | 11 | 3 | 1 | 0 | 75 | 9707 |
| 1027 | Cửu Hoàn Thiên Tích Trượng | 11 | 3 | 1 | 0 | 76 | 9705 |
| 1028 | Dao răng cưa | 11 | 3 | 1 | 0 | 77 | 9708 |
| 1029 | Lọ nước phép | 27 | 3 | 1 | 0 | -1 | 9838 |
| 1030 | Cờ Hoa đăng | 11 | 3 | 1 | 0 | 78 | 9843 |
| 1031 | Cờ Hoa sen | 11 | 3 | 1 | 0 | 79 | 9844 |
| 1032 | Giấy màu | 27 | 3 | 1 | 0 | -1 | 9846 |
| 1033 | Lời chúc | 27 | 3 | 1 | 0 | -1 | 9847 |
| 1034 | Nến | 27 | 3 | 1 | 0 | -1 | 9848 |
| 1035 | Khung tre | 27 | 3 | 1 | 0 | -1 | 9845 |
| 1036 | Pháo hoa | 27 | 3 | 1 | 0 | -1 | 9849 |
| 1037 | Hoa đăng | 27 | 3 | 1 | 0 | -1 | 9850 |
| 1038 | Hoa đăng có lời chúc | 27 | 3 | 1 | 0 | -1 | 9851 |
| 1039 | Pet Thỏ ốm | 27 | 3 | 1 | 0 | -1 | 9973 |
| 1040 | Pet Thỏ mập | 27 | 3 | 1 | 0 | -1 | 9974 |
| 1041 | Thỏ hồng Bun ma | 5 | 3 | 1 | 0 | -1 | 9975 |
| 1042 | Thỏ đỏ Chi Chi | 5 | 3 | 1 | 0 | -1 | 9976 |
| 1043 | Thỏ đen Android 18 | 5 | 3 | 1 | 0 | -1 | 9977 |
| 1044 | Sách tuyệt kỹ 1 | 25 | 0 | 1 | 0 | -1 | 11563 |
| 1045 | Đuôi khỉ | 27 | 3 | 1 | 0 | -1 | 5072 |
| 1046 | Pet Khỉ Bong Bóng | 27 | 3 | 1 | 0 | -1 | 9978 |
| 1047 | Lồng đèn lon | 11 | 3 | 1 | 0 | 80 | 9972 |
| 1048 | Áo Thiên Sứ | 0 | 0 | 15 | 0 | 1105 | 10165 |
| 1049 | Áo Thiên Sứ | 0 | 1 | 15 | 0 | 1115 | 10185 |
| 1050 | Áo Thiên Sứ | 0 | 2 | 15 | 0 | 1110 | 10175 |
| 1051 | Quần Thiên Sứ | 1 | 0 | 15 | 0 | 1106 | 10166 |
| 1052 | Quần Thiên Sứ | 1 | 1 | 15 | 0 | 1116 | 10186 |
| 1053 | Quần Thiên Sứ | 1 | 2 | 15 | 0 | 1111 | 10176 |
| 1054 | Găng Thiên Sứ | 2 | 0 | 15 | 0 | -1 | 10168 |
| 1055 | Găng Thiên Sứ | 2 | 1 | 15 | 0 | -1 | 10188 |
| 1056 | Găng Thiên Sứ | 2 | 2 | 15 | 0 | -1 | 10178 |
| 1057 | Giầy Thiên Sứ | 3 | 0 | 15 | 0 | -1 | 10167 |
| 1058 | Giầy Thiên Sứ | 3 | 1 | 15 | 0 | -1 | 10187 |
| 1059 | Giầy Thiên Sứ | 3 | 2 | 15 | 0 | -1 | 10177 |
| 1060 | Nhẫn Thiên Sứ | 4 | 0 | 15 | 0 | -1 | 10169 |
| 1061 | Nhẫn Thiên Sứ | 4 | 1 | 15 | 0 | -1 | 10189 |
| 1062 | Nhẫn Thiên Sứ | 4 | 2 | 15 | 0 | -1 | 10179 |
| 1063 | Avatar Thiên Sứ | 5 | 0 | 15 | 0 | 1104 | 9979 |
| 1064 | Avatar Thiên Sứ | 5 | 1 | 15 | 0 | 1114 | 10099 |
| 1065 | Avatar Thiên Sứ | 5 | 2 | 15 | 0 | 1109 | 10039 |
| 1066 | Mảnh áo | 27 | 3 | 1 | 0 | -1 | 10197 |
| 1067 | Mảnh quần | 27 | 3 | 1 | 0 | -1 | 10198 |
| 1068 | Mảnh giầy | 27 | 3 | 1 | 0 | -1 | 10199 |
| 1069 | Mảnh nhẫn | 27 | 3 | 1 | 0 | -1 | 10201 |
| 1070 | Mảnh găng tay | 27 | 3 | 1 | 0 | -1 | 10200 |
| 1071 | Công thức | 27 | 0 | 1 | 0 | -1 | 10210 |
| 1072 | Công thức | 27 | 1 | 1 | 0 | -1 | 10212 |
| 1073 | Công thức | 27 | 2 | 1 | 0 | -1 | 10211 |
| 1074 | Đá nâng cấp cấp 1 | 27 | 3 | 1 | 0 | -1 | 10202 |
| 1075 | Đá nâng cấp cấp 2 | 27 | 3 | 2 | 0 | -1 | 10203 |
| 1076 | Đá nâng cấp cấp 3 | 27 | 3 | 3 | 0 | -1 | 10204 |
| 1077 | Đá nâng cấp cấp 4 | 27 | 3 | 4 | 0 | -1 | 10205 |
| 1078 | Đá nâng cấp cấp 5 | 27 | 3 | 5 | 0 | -1 | 10195 |
| 1079 | Đá may mắn cấp 1 | 27 | 3 | 1 | 0 | -1 | 10206 |
| 1080 | Đá may mắn cấp 2 | 27 | 3 | 2 | 0 | -1 | 10207 |
| 1081 | Đá may mắn cấp 3 | 27 | 3 | 3 | 0 | -1 | 10208 |
| 1082 | Đá may mắn cấp 4 | 27 | 3 | 4 | 0 | -1 | 10209 |
| 1083 | Đá may mắn cấp 5 | 27 | 3 | 5 | 0 | -1 | 10196 |
| 1084 | Công thức VIP | 27 | 0 | 1 | 0 | -1 | 10213 |
| 1085 | Công thức VIP | 27 | 1 | 1 | 0 | -1 | 10215 |
| 1086 | Công thức VIP | 27 | 2 | 1 | 0 | -1 | 10214 |
| 1087 | Tanjiro | 5 | 3 | 1 | 0 | -1 | 10216 |
| 1088 | Inosuke Hashibira | 5 | 3 | 1 | 0 | -1 | 10247 |
| 1089 | Inosuke | 5 | 3 | 1 | 0 | -1 | 10342 |
| 1090 | Zenitsu | 5 | 3 | 1 | 0 | -1 | 10278 |
| 1091 | Nezuko | 5 | 3 | 1 | 0 | -1 | 10311 |
| 1092 | Gậy Quy Lão | 23 | 3 | 1 | 0 | 11 | 10485 |
| 1093 | Đất trồng cây | 27 | 3 | 0 | 0 | -1 | 10478 |
| 1094 | Phân bón | 27 | 3 | 0 | 1 | -1 | 10479 |
| 1095 | Hạt mầm | 27 | 3 | 0 | 1 | -1 | 10480 |
| 1096 | Thuốc tăng trưởng | 27 | 3 | 0 | 1 | -1 | 10481 |
| 1097 | Chậu sứ | 27 | 3 | 0 | 0 | -1 | 10483 |
| 1098 | Hoa hồng xanh | 27 | 3 | 0 | 0 | -1 | 10482 |
| 1099 | Chậu hoa hồng xanh | 27 | 3 | 0 | 0 | -1 | 10484 |
| 1100 | Chậu hoa ăn thịt | 11 | 3 | 0 | 0 | 81 | 10486 |
| 1101 | Lời chúc | 27 | 3 | 0 | 0 | -1 | 10489 |
| 1102 | Lời chúc VIP | 27 | 3 | 0 | 0 | -1 | 10490 |
| 1103 | Marron | 5 | 3 | 0 | 0 | -1 | 10440 |
| 1104 | Poc satan | 5 | 3 | 0 | 0 | -1 | 10491 |
| 1105 | Bun ma phù thủy | 5 | 3 | 0 | 0 | -1 | 10522 |
| 1106 | Biden dracula | 5 | 3 | 0 | 0 | -1 | 10553 |
| 1107 | Pet Bí Ma Vương | 27 | 3 | 0 | 0 | -1 | 10589 |
| 1108 | Đinh ba Satan | 11 | 3 | 0 | 0 | 82 | 10600 |
| 1109 | Chổi phù thủy | 11 | 3 | 0 | 0 | 83 | 10601 |
| 1110 | Cánh thiên thần | 11 | 3 | 0 | 0 | 84 | 10602 |
| 1111 | Cánh thiên thần 2 | 11 | 3 | 0 | 0 | 85 | 10603 |
| 1112 | Cây nắp ấm | 11 | 3 | 0 | 0 | 86 | 10614 |
| 1113 | Cá Chà Bá | 11 | 3 | 0 | 0 | 87 | 10617 |
| 1114 | Pet Ma vàng phù thủy | 27 | 3 | 0 | 0 | -1 | 10597 |
| 1115 | Máy dò Ngọc rồng sự kiện | 29 | 3 | 0 | 0 | -1 | 434 |
| 1116 | Hòm Halloween | 27 | 3 | 0 | 0 | -1 | 10626 |
| 1117 | Thiệp Halloween | 27 | 3 | 0 | 0 | -1 | 10627 |
| 1118 | W | 27 | 3 | 0 | 0 | -1 | 10628 |
| 1119 | O | 27 | 3 | 0 | 0 | -1 | 10629 |
| 1120 | R | 27 | 3 | 0 | 0 | -1 | 10630 |
| 1121 | L | 27 | 3 | 0 | 0 | -1 | 10631 |
| 1122 | D | 27 | 3 | 0 | 0 | -1 | 10632 |
| 1123 | C | 27 | 3 | 0 | 0 | -1 | 10633 |
| 1124 | U | 27 | 3 | 0 | 0 | -1 | 10634 |
| 1125 | P | 27 | 3 | 0 | 0 | -1 | 10635 |
| 1126 | 20 | 27 | 3 | 0 | 0 | -1 | 10636 |
| 1127 | 22 | 27 | 3 | 0 | 0 | -1 | 10637 |
| 1128 | Cờ logo Quatar 1 | 11 | 3 | 0 | 0 | 88 | 10638 |
| 1129 | Cờ logo Quatar 2 | 11 | 3 | 0 | 0 | 89 | 10639 |
| 1130 | Cờ giày vàng | 11 | 3 | 0 | 0 | 90 | 10640 |
| 1131 | Quả bóng siêu việt | 23 | 3 | 0 | 0 | 12 | 10664 |
| 1132 | Fan cuồng bóng đá | 27 | 3 | 0 | 0 | -1 | 10667 |
| 1133 | Fan gà nửa mùa | 27 | 3 | 0 | 0 | -1 | 10666 |
| 1134 | Thiệp World Cup | 27 | 3 | 0 | 0 | -1 | 10668 |
| 1135 | Capsule World Cup | 27 | 3 | 0 | 0 | -1 | 10678 |
| 1136 | Capsule World Cup VIP | 27 | 3 | 0 | 0 | -1 | 10679 |
| 1137 | Cờ GOAL | 11 | 3 | 0 | 0 | 91 | 10641 |
| 1138 | Cờ FIFA | 11 | 3 | 0 | 0 | 92 | 10642 |
| 1139 | Quả Bóng Vàng | 11 | 3 | 0 | 0 | 93 | 10643 |
| 1140 | Mèo mun đột biến | 11 | 3 | 0 | 0 | 94 | 10697 |
| 1141 | Phiếu giảm giá Black Friday | 27 | 3 | 0 | 0 | -1 | 4030 |
| 1142 | Bong bóng heo | 11 | 3 | 0 | 0 | 95 | 10706 |
| 1143 | Đá bảo vệ (Khóa) | 27 | 3 | 0 | 0 | -1 | 9406 |
| 1144 | Phượng hoàng lửa | 23 | 3 | 0 | 0 | 13 | 10710 |
| 1145 | Cải trang Black goku ssj 3 White | 5 | 3 | 0 | 0 | -1 | 10711 |
| 1146 | Avatar Gohan White | 5 | 0 | 0 | 0 | 1167 | 10686 |
| 1147 | Avatar Gohan White | 5 | 2 | 0 | 0 | 1167 | 10686 |
| 1148 | Avatar Gohan White | 5 | 1 | 0 | 0 | 1168 | 10707 |
| 1149 | Cánh thiên sứ hắc ám | 11 | 3 | 0 | 0 | 96 | 10694 |
| 1150 | Cuồng nộ 2 | 29 | 3 | 0 | 0 | -1 | 10716 |
| 1151 | Bổ khí 2 | 29 | 3 | 0 | 0 | -1 | 10715 |
| 1152 | Bổ huyết 2 | 29 | 3 | 0 | 0 | -1 | 10714 |
| 1153 | Giáp Xên bọ hung 2 | 29 | 3 | 0 | 0 | -1 | 10712 |
| 1154 | Ẩn danh 2 | 29 | 3 | 0 | 0 | -1 | 10717 |
| 1155 | Noel 2022 Goku | 5 | 0 | 0 | 0 | -1 | 10718 |
| 1156 | Noel 2022 Cađíc | 5 | 2 | 0 | 0 | -1 | 10749 |
| 1157 | Noel 2022 Pôcôlô | 5 | 1 | 0 | 0 | -1 | 10780 |
| 1158 | Chú lùn | 11 | 3 | 0 | 0 | 97 | 10840 |
| 1159 | Chú lùn | 11 | 3 | 0 | 0 | 98 | 10841 |
| 1160 | Chú lùn | 11 | 3 | 0 | 0 | 99 | 10842 |
| 1161 | Chú lùn | 11 | 3 | 0 | 0 | 100 | 10843 |
| 1162 | Chú lùn | 11 | 3 | 0 | 0 | 101 | 10844 |
| 1163 | Chú lùn | 11 | 3 | 0 | 0 | 102 | 10845 |
| 1164 | Chú lùn | 11 | 3 | 0 | 0 | 103 | 10846 |
| 1165 | Chuông đồng | 27 | 3 | 0 | 0 | -1 | 10847 |
| 1166 | Cá tuyết | 27 | 3 | 0 | 0 | -1 | 10848 |
| 1167 | Bánh quy | 27 | 3 | 0 | 0 | -1 | 10849 |
| 1168 | Kẹo đường | 27 | 3 | 0 | 0 | -1 | 10850 |
| 1169 | Kẹo người tuyết | 27 | 3 | 0 | 0 | -1 | 10851 |
| 1170 | Gói quà | 27 | 3 | 0 | 0 | -1 | 10852 |
| 1171 | Túi 7 chú lùn | 27 | 3 | 0 | 0 | -1 | 10853 |
| 1172 | Xe heo tuần lộc | 23 | 3 | 0 | 0 | 14 | 10855 |
| 1173 | Rương Mảnh Thiên Sứ | 27 | 3 | 0 | 0 | -1 | 10854 |
| 1174 | Kimono | 5 | 0 | 0 | 0 | -1 | 10915 |
| 1175 | Kimono | 5 | 1 | 0 | 0 | -1 | 10973 |
| 1176 | Kimono | 5 | 2 | 0 | 0 | -1 | 10944 |
| 1177 | Mãng cầu | 27 | 3 | 0 | 0 | -1 | 10884 |
| 1178 | Quả dừa | 27 | 3 | 0 | 0 | -1 | 10885 |
| 1179 | Đu đủ | 27 | 3 | 0 | 0 | -1 | 10886 |
| 1180 | Quả xoài | 27 | 3 | 0 | 0 | -1 | 10887 |
| 1181 | Trái sung | 27 | 3 | 0 | 0 | -1 | 10888 |
| 1182 | Mâm ngũ quả | 27 | 3 | 0 | 0 | -1 | 10889 |
| 1183 | Bao lì xì rồng | 27 | 3 | 0 | 0 | -1 | 12401 |
| 1184 | Gói quà đặc biệt | 27 | 3 | 0 | 0 | -1 | 12397 |
| 1185 | Cành mai | 11 | 3 | 0 | 0 | 104 | 10899 |
| 1186 | Cành đào | 11 | 3 | 0 | 0 | 105 | 10903 |
| 1187 | Hộp quà Tết 2024 | 27 | 3 | 0 | 0 | -1 | 12398 |
| 1188 | Pet mèo đen đuôi vàng | 27 | 3 | 0 | 0 | -1 | 10877 |
| 1189 | Xúc xích xông khói | 29 | 3 | 0 | 0 | -1 | 10890 |
| 1190 | Dĩa thức ăn cho mèo | 29 | 3 | 0 | 0 | -1 | 10891 |
| 1191 | Thiệp chúc tết | 27 | 3 | 0 | 0 | -1 | 10893 |
| 1192 | Thiệp chúc tết | 27 | 3 | 0 | 0 | -1 | 10894 |
| 1193 | Thiệp chúc tết | 27 | 3 | 0 | 0 | -1 | 10895 |
| 1194 | Phiếu bé ngoan | 27 | 3 | 0 | 0 | -1 | 11007 |
| 1195 | Xí muội Hoa đào | 29 | 3 | 0 | 0 | -1 | 10905 |
| 1196 | Xí muội Hoa mai | 29 | 3 | 0 | 0 | -1 | 10904 |
| 1197 | Bóng Vịt Vàng | 11 | 3 | 0 | 0 | 106 | 10876 |
| 1198 | Nón mèo | 5 | 0 | 0 | 0 | 1186 | 10910 |
| 1199 | Nón mèo | 5 | 1 | 0 | 0 | 1188 | 10914 |
| 1200 | Nón mèo | 5 | 2 | 0 | 0 | 1187 | 10912 |
| 1201 | Karin Kid Lân | 5 | 3 | 0 | 0 | -1 | 11008 |
| 1202 | Pet mèo trắng đuôi vàng | 27 | 3 | 0 | 0 | -1 | 11040 |
| 1203 | Pet mèo trắng đuôi vàng | 27 | 3 | 0 | 0 | -1 | 11040 |
| 1204 | Mảnh Rồng thần Namếc | 33 | 3 | 0 | 0 | -1 | 11048 |
| 1205 | Drabura Frost | 5 | 3 | 0 | 0 | -1 | 11053 |
| 1206 | Trái tim Valentine | 11 | 3 | 0 | 0 | 201 | 11047 |
| 1207 | Pet Minion | 27 | 3 | 0 | 0 | -1 | 9709 |
| 1208 | Bunma tóc xanh neon | 5 | 3 | 0 | 0 | -1 | 11153 |
| 1209 | Bunma tóc nâu băng đô | 5 | 3 | 0 | 0 | -1 | 11157 |
| 1210 | Bunma tóc tím thắt bím | 5 | 3 | 0 | 0 | -1 | 11161 |
| 1211 | Sách tuyệt kỹ 1 | 25 | 1 | 0 | 0 | -1 | 11567 |
| 1212 | Sách tuyệt kỹ 1 | 25 | 2 | 0 | 0 | -1 | 11565 |
| 1213 | Cờ mèo Hoàng Thượng | 11 | 3 | 0 | 0 | 206 | 11163 |
| 1214 | Cơm nếp | 27 | 3 | 0 | 0 | -1 | 11195 |
| 1215 | Ván cơm nếp | 27 | 3 | 0 | 0 | -1 | 11196 |
| 1216 | Tệp cơm nếp | 27 | 3 | 0 | 0 | -1 | 11197 |
| 1217 | Lá dong | 27 | 3 | 0 | 0 | -1 | 11198 |
| 1218 | Sợi Cói | 27 | 3 | 0 | 0 | -1 | 11199 |
| 1219 | Tệp bánh chưng | 27 | 3 | 0 | 0 | -1 | 11200 |
| 1220 | Ngà voi | 27 | 3 | 0 | 0 | -1 | 11203 |
| 1221 | Cựa gà | 27 | 3 | 0 | 0 | -1 | 11204 |
| 1222 | Hồng mao | 27 | 3 | 0 | 0 | -1 | 11205 |
| 1223 | Bóng khí Gas | 11 | 3 | 0 | 0 | 207 | 11216 |
| 1224 | Pet Voi Chín Ngà | 27 | 3 | 0 | 0 | -1 | 11232 |
| 1225 | Pet Gà Chín Cựa | 27 | 3 | 0 | 0 | -1 | 11233 |
| 1226 | Pet Ngựa Chín Hồng mao | 27 | 3 | 0 | 0 | -1 | 11234 |
| 1227 | Hộp quà thường | 27 | 3 | 0 | 0 | -1 | 11201 |
| 1228 | Hộp quà cao cấp | 27 | 3 | 0 | 0 | -1 | 11202 |
| 1229 | Bí kíp tuyệt kỹ | 27 | 3 | 0 | 0 | -1 | 11238 |
| 1230 | Hào Quang Rực Rỡ | 11 | 3 | 0 | 0 | 205 | 11239 |
| 1231 | Gậy thượng đế | 11 | 3 | 0 | 0 | 107 | 11245 |
| 1232 | Bình chứa nhỏ | 29 | 1 | 0 | 0 | -1 | 11192 |
| 1233 | Nồi cơm điện | 29 | 1 | 0 | 0 | -1 | 11173 |
| 1234 | Cải trang Pic Thợ Lặn | 5 | 3 | 0 | 0 | -1 | 11293 |
| 1235 | Cải trang Poc Bikini | 5 | 3 | 0 | 0 | -1 | 11259 |
| 1236 | Cải trang King Kong Sành Điệu | 5 | 3 | 0 | 0 | -1 | 11325 |
| 1237 | Cành khô | 27 | 3 | 0 | 0 | -1 | 11246 |
| 1238 | Nước Suối Tinh Khiết | 27 | 3 | 0 | 0 | -1 | 11247 |
| 1239 | Gỗ Lớn | 27 | 3 | 0 | 0 | -1 | 11248 |
| 1240 | Que đốt | 27 | 3 | 0 | 0 | -1 | 11249 |
| 1241 | Bồn tắm gỗ | 27 | 3 | 0 | 0 | -1 | 11250 |
| 1242 | Bồn tắm vàng | 27 | 3 | 0 | 0 | -1 | 11251 |
| 1243 | Pet bọ cánh cứng | 27 | 3 | 0 | 0 | -1 | 11371 |
| 1244 | Pet ngài đêm | 27 | 3 | 0 | 0 | -1 | 11372 |
| 1245 | Bọ Kiến Vương Hai Sừng | 27 | 3 | 0 | 0 | -1 | 11252 |
| 1246 | Bọ Hung Tê Giác | 27 | 3 | 0 | 0 | -1 | 11253 |
| 1247 | Bọ Kẹp Kìm | 27 | 3 | 0 | 0 | -1 | 11254 |
| 1248 | Bọ Cánh Cứng | 27 | 3 | 0 | 0 | -1 | 11255 |
| 1249 | Ngài Đêm | 27 | 3 | 0 | 0 | -1 | 11256 |
| 1250 | Hũ Mật Ong | 27 | 3 | 0 | 0 | -1 | 11257 |
| 1251 | Vợt Bắt Bọ | 27 | 3 | 0 | 0 | -1 | 11258 |
| 1252 | Ve Sầu Xên | 23 | 3 | 0 | 0 | 15 | 11373 |
| 1253 | Ve Sầu Xên Tiến Hóa | 23 | 3 | 0 | 0 | 16 | 11374 |
| 1254 | Búa hắc hường | 11 | 3 | 0 | 0 | 210 | 11422 |
| 1255 | Cải trang Mabư Còm | 5 | 3 | 0 | 0 | -1 | 11455 |
| 1256 | Pet heo bướm | 27 | 3 | 0 | 0 | -1 | 11462 |
| 1257 | Xe triều đình | 5 | 3 | 0 | 0 | -1 | 11463 |
| 1258 | Hồn ma | 27 | 3 | 0 | 0 | -1 | 11471 |
| 1259 | Bình phép | 27 | 3 | 0 | 0 | -1 | 11472 |
| 1260 | Lọ nước hồi sinh | 27 | 3 | 0 | 0 | -1 | 11473 |
| 1261 | Bùa hồi sinh | 27 | 3 | 0 | 0 | -1 | 11474 |
| 1262 | Lích Tên | 27 | 3 | 0 | 0 | -1 | 11475 |
| 1263 | Siêu Lích Tên | 27 | 3 | 0 | 0 | -1 | 11476 |
| 1264 | Máy dò linh hồn | 29 | 3 | 0 | 0 | -1 | 11493 |
| 1265 | Yajirobe Zoro | 5 | 3 | 0 | 0 | -1 | 11526 |
| 1266 | Avatar Gohan sẹo | 5 | 0 | 0 | 0 | 1164 | 10680 |
| 1267 | Avatar Gohan sẹo | 5 | 1 | 0 | 0 | 1274 | 11477 |
| 1268 | Avatar Gohan sẹo | 5 | 2 | 0 | 0 | 1164 | 10680 |
| 1269 | Avatar Gohan Blue sẹo | 5 | 0 | 0 | 0 | 1165 | 10682 |
| 1270 | Avatar Gohan Blue sẹo | 5 | 1 | 0 | 0 | 1275 | 11480 |
| 1271 | Avatar Gohan Blue sẹo | 5 | 2 | 0 | 0 | 1165 | 10682 |
| 1272 | Đài sen hồng | 23 | 3 | 0 | 0 | 17 | 11527 |
| 1273 | Đài sen vàng | 23 | 3 | 0 | 0 | 18 | 11528 |
| 1274 | Cải trang Cumber | 5 | 3 | 0 | 0 | -1 | 11529 |
| 1275 | Cải trang Cumber SSJ | 5 | 3 | 0 | 0 | -1 | 11530 |
| 1276 | Cải trang Nguyệt thần | 5 | 3 | 0 | 0 | -1 | 11531 |
| 1277 | Cải trang Nhật thần | 5 | 3 | 0 | 0 | -1 | 11641 |
| 1278 | Sách tuyệt kỹ 2 | 25 | 0 | 0 | 0 | -1 | 11564 |
| 1279 | Sách tuyệt kỹ 2 | 25 | 1 | 0 | 0 | -1 | 11568 |
| 1280 | Sách tuyệt kỹ 2 | 25 | 2 | 0 | 0 | -1 | 11566 |
| 1281 | Trang sách cũ | 27 | 3 | 0 | 0 | -1 | 11570 |
| 1282 | Bìa sách | 27 | 3 | 0 | 0 | -1 | 11571 |
| 1283 | Cuốn sách cũ | 27 | 3 | 0 | 0 | -1 | 11569 |
| 1284 | Bùa giám định | 27 | 3 | 0 | 0 | -1 | 11573 |
| 1285 | Kìm bấm giấy | 27 | 3 | 0 | 0 | -1 | 11572 |
| 1286 | Kẻ thao túng sói | 36 | 3 | 0 | 0 | 215 | 11611 |
| 1287 | Nước anh bao | 36 | 3 | 0 | 0 | 216 | 11609 |
| 1288 | Chiến thần khóa nick | 36 | 3 | 0 | 0 | 217 | 11607 |
| 1289 | Đại gia mới nhú | 36 | 3 | 0 | 0 | 218 | 11608 |
| 1290 | Trùm ước rồng | 36 | 3 | 0 | 0 | 219 | 11618 |
| 1291 | Trùm săn Boss | 36 | 3 | 0 | 0 | 220 | 11617 |
| 1292 | Thánh đập đồ +7 | 36 | 3 | 0 | 0 | 221 | 11615 |
| 1293 | Cao thủ siêu hạng | 36 | 3 | 0 | 0 | 222 | 11614 |
| 1294 | Nông dân chăm chỉ | 36 | 3 | 0 | 0 | 223 | 11612 |
| 1295 | Ông thần ve chai | 36 | 3 | 0 | 0 | 224 | 11613 |
| 1296 | Bị móc sạch túi | 36 | 3 | 0 | 0 | 225 | 11606 |
| 1297 | KOL | 36 | 3 | 0 | 0 | 226 | 13632 |
| 1298 | Chuyên gia lượm nhặt | 36 | 3 | 0 | 0 | 227 | 9399 |
| 1299 | Fan cứng | 36 | 3 | 0 | 0 | 228 | 11610 |
| 1300 | Thánh ở dơ | 36 | 3 | 0 | 0 | 229 | 11616 |
| 1301 | Cờ khỉ hoàng đế | 11 | 3 | 0 | 0 | 230 | 11678 |
| 1302 | Cải trang | 5 | 3 | 0 | 0 | -1 | 11621 |
| 1303 | Lồng đèn thỏ trắng | 11 | 3 | 0 | 0 | 231 | 11679 |
| 1304 | Kiếm gỗ | 27 | 3 | 0 | 0 | -1 | 11674 |
| 1305 | Ánh trăng tròn | 27 | 3 | 0 | 0 | -1 | 11673 |
| 1306 | Bánh dẻo thỏ trắng | 31 | 3 | 0 | 0 | -1 | 11675 |
| 1307 | Bánh dẻo thỏ xanh | 31 | 3 | 0 | 0 | -1 | 11676 |
| 1308 | Bánh dẻo thỏ hồng | 31 | 3 | 0 | 0 | -1 | 11677 |
| 1309 | Bunma Ả rập Xê út | 5 | 3 | 0 | 0 | -1 | 11680 |
| 1310 | Thỏ ngọc | 27 | 3 | 0 | 0 | -1 | 11712 |
| 1311 | Lồng đèn treo | 27 | 3 | 0 | 0 | -1 | 11713 |
| 1312 | Hạt sen | 27 | 3 | 0 | 0 | -1 | 11714 |
| 1313 | Bánh trung thu Hạt sen | 27 | 3 | 0 | 0 | -1 | 11715 |
| 1314 | Hộp giấy | 27 | 3 | 0 | 0 | -1 | 11717 |
| 1315 | Túi giấy | 27 | 3 | 0 | 0 | -1 | 11716 |
| 1316 | Giỏ bánh trung thu | 27 | 3 | 0 | 0 | -1 | 11718 |
| 1317 | Giỏ bánh trung thu độc đáo | 27 | 3 | 0 | 0 | -1 | 11719 |
| 1318 | Pet Mông Quỷ | 27 | 3 | 0 | 0 | -1 | 11726 |
| 1319 | Sách đấm Dragon | 37 | 0 | 0 | 0 | 0 | 644 |
| 1320 | Sách Kamejoko | 37 | 0 | 0 | 0 | 1 | 651 |
| 1321 | Thái Dương Hạ San | 37 | 0 | 0 | 0 | 6 | 658 |
| 1322 | Kaioken | 37 | 0 | 0 | 0 | 9 | 716 |
| 1323 | Quả cầu Kênh Khi | 37 | 0 | 0 | 0 | 10 | 717 |
| 1324 | Sách Dịch Chuyển | 37 | 0 | 0 | 0 | 20 | 3783 |
| 1325 | Sách Thôi Miên | 37 | 0 | 0 | 0 | 22 | 3782 |
| 1326 | Sách đấm Demon | 37 | 1 | 0 | 0 | 2 | 665 |
| 1327 | Sách Masenko | 37 | 1 | 0 | 0 | 3 | 672 |
| 1328 | Sách học Trị thương | 37 | 1 | 0 | 0 | 7 | 1090 |
| 1329 | Makankosappo | 37 | 1 | 0 | 0 | 11 | 723 |
| 1330 | Đẻ trứng | 37 | 1 | 0 | 0 | 12 | 722 |
| 1331 | Sách Biến Sôcôla | 37 | 1 | 0 | 0 | 18 | 3780 |
| 1332 | Sách Liên hoàn | 37 | 1 | 0 | 0 | 17 | 3778 |
| 1333 | Sách đấm Galick | 37 | 2 | 0 | 0 | 4 | 679 |
| 1334 | Sách Antomic | 37 | 2 | 0 | 0 | 5 | 686 |
| 1335 | Tái tạo năng lượng | 37 | 2 | 0 | 0 | 8 | 1097 |
| 1336 | Hóa khỉ khổng lồ | 37 | 2 | 0 | 0 | 13 | 718 |
| 1337 | Bom hi sinh | 37 | 2 | 0 | 0 | 14 | 2248 |
| 1338 | Sách Trói | 37 | 2 | 0 | 0 | 23 | 3779 |
| 1339 | Sách Huýt Sáo | 37 | 2 | 0 | 0 | 21 | 3781 |
| 1340 | Khiên năng lượng | 37 | 3 | 0 | 0 | 19 | 3784 |
| 1341 | Sách Super Kamejoko | 37 | 0 | 0 | 0 | 24 | 11162 |
| 1342 | Sách Ma phong ba | 37 | 1 | 0 | 0 | 26 | 11194 |
| 1343 | Sách Cađíc liên hoàn chưởng | 37 | 2 | 0 | 0 | 25 | 11193 |
| 1344 | Con Cat hường | 11 | 3 | 0 | 0 | 234 | 11727 |
| 1345 | Thú cưỡi mèo kéo xương | 23 | 3 | 0 | 0 | 19 | 11728 |
| 1346 | Thú cưỡi Xe bí ngô | 23 | 3 | 0 | 0 | 20 | 11730 |
| 1347 | Pet Mèo Phù Thủy | 27 | 3 | 0 | 0 | -1 | 11729 |
| 1348 | Giỏ đựng ngọc bí | 27 | 3 | 0 | 0 | -1 | 11748 |
| 1349 | Giỏ đựng | 27 | 3 | 0 | 0 | -1 | 11736 |
| 1350 | Kẹo não người | 27 | 3 | 0 | 0 | -1 | 11737 |
| 1351 | Dây buộc | 27 | 3 | 0 | 0 | -1 | 11738 |
| 1352 | Bó kẹo kinh dị | 27 | 3 | 0 | 0 | -1 | 11739 |
| 1353 | Giỏ kẹo kinh dị | 27 | 3 | 0 | 0 | -1 | 11740 |
| 1354 | Giấy trang trí Halloween | 27 | 3 | 0 | 0 | -1 | 11742 |
| 1355 | Giỏ đựng kẹo trái bí | 27 | 3 | 0 | 0 | -1 | 11741 |
| 1356 | Hộp Kẹo Ma Quỷ | 27 | 3 | 0 | 0 | -1 | 11743 |
| 1357 | Kẹo bí ngô | 27 | 3 | 0 | 0 | -1 | 11749 |
| 1358 | Cờ hồn Mabư | 11 | 3 | 0 | 0 | 237 | 11747 |
| 1359 | Cờ hồn Xên Bọ Hung | 11 | 3 | 0 | 0 | 236 | 11746 |
| 1360 | Cờ Ma đèn nhảy múa | 11 | 3 | 0 | 0 | 235 | 11744 |
| 1361 | Cờ Ma đèn hút hồn | 11 | 3 | 0 | 0 | 238 | 11745 |
| 1362 | Cải trang Thầy giáo Ca Lích | 5 | 0 | 0 | 0 | -1 | 11760 |
| 1363 | Thú cưỡi con Cat hường | 23 | 3 | 0 | 0 | 21 | 11792 |
| 1364 | Lá trà tươi | 27 | 3 | 0 | 0 | -1 | 11793 |
| 1365 | Nia tre | 27 | 3 | 0 | 0 | -1 | 11794 |
| 1366 | Que tre | 27 | 3 | 0 | 0 | -1 | 11795 |
| 1367 | Hoa cúc | 27 | 3 | 0 | 0 | -1 | 11796 |
| 1368 | Túi trà khô | 27 | 3 | 0 | 0 | -1 | 11797 |
| 1369 | Hộp trà | 27 | 3 | 0 | 0 | -1 | 11798 |
| 1370 | Hộp trà hoa cúc | 27 | 3 | 0 | 0 | -1 | 11799 |
| 1371 | Cải trang đệ tử Tí | 5 | 3 | 0 | 0 | -1 | 11878 |
| 1372 | Bao bì thiệp | 27 | 3 | 0 | 0 | -1 | 11800 |
| 1373 | Mảnh giấy | 27 | 3 | 0 | 0 | -1 | 11801 |
| 1374 | Keo dán | 27 | 3 | 0 | 0 | -1 | 11802 |
| 1375 | Lời chúc | 27 | 3 | 0 | 0 | -1 | 11803 |
| 1376 | Thiệp chúc thường | 27 | 3 | 0 | 0 | -1 | 11804 |
| 1377 | Thiệp chúc đặc biệt | 27 | 3 | 0 | 0 | -1 | 11805 |
| 1378 | Cải trang Thầy giáo Cađíc | 5 | 2 | 0 | 0 | -1 | 11913 |
| 1379 | Cải trang Thầy giáo Pô cô lô | 5 | 1 | 0 | 0 | -1 | 11946 |
| 1380 | Cải trang đệ tử Sửu | 5 | 3 | 0 | 0 | -1 | 11979 |
| 1381 | Cải trang đệ tử Dần | 5 | 3 | 0 | 0 | -1 | 12090 |
| 1382 | Kéo tỉa hoa đặc biệt | 27 | 3 | 0 | 0 | -1 | 11807 |
| 1383 | Cải trang Thầy giáo Raddit | 5 | 3 | 0 | 0 | -1 | 12024 |
| 1384 | Cải trang Thầy giáo Goku | 5 | 3 | 0 | 0 | -1 | 12057 |
| 1385 | Cải trang Thầy giáo Quy Lão | 5 | 3 | 0 | 0 | -1 | 11844 |
| 1386 | Bút chì hồng | 11 | 3 | 0 | 0 | 239 | 11879 |
| 1387 | Kéo tỉa hoa | 27 | 3 | 0 | 0 | -1 | 11807 |
| 1388 | Bó hoa hồng | 27 | 3 | 0 | 0 | -1 | 11808 |
| 1389 | Avatar Cử nhân | 5 | 0 | 0 | 0 | 1329 | 11989 |
| 1390 | Avatar Cử nhân | 5 | 1 | 0 | 0 | 1331 | 11991 |
| 1391 | Avatar Cử nhân | 5 | 2 | 0 | 0 | 1330 | 11990 |
| 1392 | Gõ đầu trẻ | 36 | 3 | 0 | 0 | 240 | 11880 |
| 1393 | Gõ đầu trẻ | 36 | 3 | 0 | 0 | 242 | 11880 |
| 1394 | Gõ đầu trẻ | 36 | 3 | 0 | 0 | 243 | 11880 |
| 1395 | Đóa hoa hồng | 27 | 3 | 0 | 0 | -1 | 11806 |
| 1396 | Cải trang Super Píc | 5 | 3 | 0 | 0 | -1 | 5932 |
| 1397 | Rương radar | 29 | 3 | 0 | 0 | -1 | 5007 |
| 1398 | Rương giày | 29 | 3 | 0 | 0 | -1 | 5007 |
| 1399 | Rương nhẫn | 29 | 3 | 0 | 0 | -1 | 5007 |
| 1400 | Rương găng tay | 29 | 3 | 0 | 0 | -1 | 5007 |
| 1401 | Rương áo | 29 | 3 | 0 | 0 | -1 | 5007 |
| 1402 | Rương quần | 29 | 3 | 0 | 0 | -1 | 5007 |
| 1403 | Rương bí kíp tuyệt kỹ | 29 | 3 | 0 | 0 | -1 | 5007 |
| 1404 | Chí mạng 2 | 29 | 3 | 0 | 0 | -1 | 8243 |
| 1405 | Chí mạng 3 | 29 | 3 | 0 | 0 | -1 | 8243 |
| 1406 | Né đòn | 29 | 3 | 0 | 0 | -1 | 10522 |
| 1407 | Né đòn 2 | 29 | 3 | 0 | 0 | -1 | 10522 |
| 1408 | Hồi skill | 29 | 3 | 0 | 0 | -1 | 1089 |
| 1409 | Phản sát thương | 29 | 3 | 0 | 0 | -1 | 10553 |
| 1410 | Phản sát thương 2 | 29 | 3 | 0 | 0 | -1 | 10553 |
| 1411 | Phản sát thương 3 | 29 | 3 | 0 | 0 | -1 | 10553 |
| 1412 | Kamejoko | 29 | 3 | 0 | 0 | -1 | 10553 |
| 1413 | Kamejoko 2 | 29 | 3 | 0 | 0 | -1 | 10553 |
| 1414 | Pet Shiba | 27 | 3 | 0 | 0 | -1 | 12096 |
| 1415 | Cải trang Dr Myuu | 5 | 3 | 0 | 0 | -1 | 12098 |
| 1416 | Sao pha lê đỏ cấp 2 | 30 | 3 | 0 | 0 | -1 | 12231 |
| 1417 | Sao pha lê lam cấp 2 | 30 | 3 | 0 | 0 | -1 | 12232 |
| 1418 | Sao pha lê hồng cấp 2 | 30 | 3 | 0 | 0 | -1 | 12233 |
| 1419 | Sao pha lê tím cấp 2 | 30 | 3 | 0 | 0 | -1 | 12234 |
| 1420 | Sao pha lê cam cấp 2 | 30 | 3 | 0 | 0 | -1 | 12235 |
| 1421 | Sao pha lê vàng cấp 2 | 30 | 3 | 0 | 0 | -1 | 12236 |
| 1422 | Sao pha lê lục cấp 2 | 30 | 3 | 0 | 0 | -1 | 12237 |
| 1423 | Hematite | 27 | 3 | 0 | 0 | -1 | 11759 |
| 1424 | Cải trang Goku Santa | 5 | 3 | 0 | 0 | -1 | 12099 |
| 1425 | Anti phong ấn | 29 | 3 | 0 | 0 | -1 | 11474 |
| 1426 | Sao pha lê đỏ lấp lánh | 30 | 3 | 0 | 0 | -1 | 12181 |
| 1427 | Sao pha lê lam lấp lánh | 30 | 3 | 0 | 0 | -1 | 12182 |
| 1428 | Sao pha lê hồng lấp lánh | 30 | 3 | 0 | 0 | -1 | 12183 |
| 1429 | Sao pha lê tím lấp lánh | 30 | 3 | 0 | 0 | -1 | 12184 |
| 1430 | Sao pha lê cam lấp lánh | 30 | 3 | 0 | 0 | -1 | 12185 |
| 1431 | Sao pha lê vàng lấp lánh | 30 | 3 | 0 | 0 | -1 | 12186 |
| 1432 | Sao pha lê lục lấp lánh | 30 | 3 | 0 | 0 | -1 | 12187 |
| 1433 | Sao pha lê đen lấp lánh | 30 | 3 | 0 | 0 | -1 | 12190 |
| 1434 | Sao pha lê trắng lấp lánh | 30 | 3 | 0 | 0 | -1 | 12189 |
| 1435 | Pet chim cánh cụt | 27 | 3 | 0 | 0 | -1 | 12138 |
| 1436 | Cải trang Chi Chi tuần lộc | 5 | 3 | 0 | 0 | -1 | 12173 |
| 1437 | Cải trang Bông Băng Vàng | 5 | 3 | 0 | 0 | -1 | 12223 |
| 1438 | Dùi đục | 27 | 3 | 0 | 0 | -1 | 12139 |
| 1439 | Đá mài | 27 | 3 | 0 | 0 | -1 | 12140 |
| 1440 | Rương sao pha lê | 27 | 3 | 0 | 0 | -1 | 12180 |
| 1441 | Hematite (khóa) | 27 | 3 | 0 | 0 | -1 | 11759 |
| 1442 | Cải trang Super Xayda God | 5 | 3 | 0 | 0 | -1 | 12305 |
| 1443 | Phượng hoàng băng | 23 | 3 | 0 | 0 | 22 | 12324 |
| 1444 | Mũi cà rốt | 27 | 3 | 0 | 0 | -1 | 12175 |
| 1445 | Tảng tuyết | 27 | 3 | 0 | 0 | -1 | 12174 |
| 1446 | Tay gỗ | 27 | 3 | 0 | 0 | -1 | 12176 |
| 1447 | Khăn choàng | 27 | 3 | 0 | 0 | -1 | 12177 |
| 1448 | Người tuyết | 27 | 3 | 0 | 0 | -1 | 12178 |
| 1449 | Người tuyết băng giá | 27 | 3 | 0 | 0 | -1 | 12179 |
| 1450 | Cải trang Broly Base | 5 | 3 | 0 | 0 | -1 | 12272 |
| 1451 | Nón Noel | 27 | 3 | 0 | 0 | -1 | 12314 |
| 1452 | Pet Ông già Noel | 27 | 3 | 0 | 0 | -1 | 12313 |
| 1453 | Rương sao pha lê VIP | 27 | 3 | 0 | 0 | -1 | 12180 |
| 1454 | Cải trang Super Black Goku Rose | 5 | 3 | 0 | 0 | -1 | 5173 |
| 1455 | Quả bóng tuyết | 23 | 3 | 0 | 0 | 25 | 12315 |
| 1456 | Kiếm Kitetsu | 11 | 3 | 0 | 0 | 246 | 12321 |
| 1457 | Danh hiệu X-mas | 36 | 3 | 0 | 0 | 247 | 12317 |
| 1458 | Pet Shiba đeo nơ | 27 | 3 | 0 | 0 | -1 | 12333 |
| 1459 | Chuông | 27 | 3 | 0 | 0 | -1 | 12316 |
| 1460 | Quả châu | 27 | 3 | 0 | 0 | -1 | 12325 |
| 1461 | Ngôi sao | 27 | 3 | 0 | 0 | -1 | 12323 |
| 1462 | Dây kim tuyến | 27 | 3 | 0 | 0 | -1 | 12318 |
| 1463 | Móc treo Noel | 27 | 3 | 0 | 0 | -1 | 12322 |
| 1464 | Kẹo giáng sinh | 27 | 3 | 0 | 0 | -1 | 4432 |
| 1465 | Máy bay trực thăng Noel | 23 | 3 | 0 | 0 | 24 | 12328 |
| 1466 | Tuần lộc Machine | 23 | 3 | 0 | 0 | 23 | 12320 |
| 1467 | Gấu bắc cực | 11 | 3 | 0 | 0 | 249 | 12319 |
| 1468 | Thú cưỡi Rồng PiLong | 23 | 3 | 0 | 0 | 26 | 12551 |
| 1469 | Avatar mũ rồng | 5 | 0 | 0 | 0 | 1374 | 12341 |
| 1470 | Avatar mũ rồng | 5 | 1 | 0 | 0 | 1376 | 12345 |
| 1471 | Avatar mũ rồng | 5 | 2 | 0 | 0 | 1375 | 12343 |
| 1472 | Dây pháo | 27 | 3 | 0 | 0 | -1 | 12329 |
| 1473 | Câu đối | 27 | 3 | 0 | 0 | -1 | 12334 |
| 1474 | Dây treo bánh | 27 | 3 | 0 | 0 | -1 | 12335 |
| 1475 | Đèn lồng treo cây | 27 | 3 | 0 | 0 | -1 | 12336 |
| 1476 | Cải trang Bunma rực rỡ | 5 | 3 | 0 | 0 | -1 | 12388 |
| 1477 | Thỏi vàng bay | 23 | 3 | 0 | 0 | 27 | 12400 |
| 1478 | Cờ bao lì xì | 11 | 3 | 0 | 0 | 250 | 12389 |
| 1479 | Cờ dây pháo | 11 | 3 | 0 | 0 | 251 | 12402 |
| 1480 | Mì thanh long | 29 | 3 | 0 | 0 | -1 | 12395 |
| 1481 | Cơm gà quay | 29 | 3 | 0 | 0 | -1 | 12396 |
| 1482 | Pet rồng Pikachu thanh long | 27 | 3 | 0 | 0 | -1 | 12548 |
| 1483 | Cải trang Bunma thanh lịch | 5 | 3 | 0 | 0 | -1 | 12541 |
| 1484 | Cải trang Thần tài | 5 | 0 | 0 | 0 | -1 | 12506 |
| 1485 | Cải trang Thần tài | 5 | 1 | 0 | 0 | -1 | 12508 |
| 1486 | Cải trang Thần tài | 5 | 2 | 0 | 0 | -1 | 12507 |
| 1487 | Cá chép rồng | 23 | 3 | 0 | 0 | 28 | 12549 |
| 1488 | Vạn | 27 | 3 | 0 | 0 | -1 | 12390 |
| 1489 | Sự | 27 | 3 | 0 | 0 | -1 | 12391 |
| 1490 | Như | 27 | 3 | 0 | 0 | -1 | 12392 |
| 1491 | Ý | 27 | 3 | 0 | 0 | -1 | 12393 |
| 1492 | 2024 | 27 | 3 | 0 | 0 | -1 | 12394 |
| 1493 | Phong bì Tết 2024 | 27 | 3 | 0 | 0 | -1 | 10895 |
| 1494 | Thiệp đỏ | 27 | 3 | 0 | 0 | -1 | 2988 |
| 1495 | Thiệp đỏ VIP | 27 | 3 | 0 | 0 | -1 | 2988 |
| 1496 | Thiệp chúc mừng | 27 | 3 | 0 | 0 | -1 | 12550 |
| 1497 | Pet rồng con thần tài | 27 | 3 | 0 | 0 | -1 | 12558 |
| 1498 | Cải trang Goku Dragon | 5 | 0 | 0 | 0 | -1 | 12653 |
| 1499 | Cải trang Pôcôlô Dragon | 5 | 1 | 0 | 0 | -1 | 12657 |
| 1500 | Cải trang Cađíc Dragon | 5 | 2 | 0 | 0 | -1 | 12655 |
| 1501 | Hộp quà Lucky 2024 | 27 | 3 | 0 | 0 | -1 | 6983 |
| 1502 | Thanh Long Yển Nguyệt đao | 11 | 3 | 0 | 0 | 252 | 12658 |
| 1503 | CT Lý Tiểu Nương hầu gái xanh | 5 | 3 | 0 | 0 | -1 | 12692 |
| 1504 | CT Lý Tiểu Nương hầu gái hồng | 5 | 3 | 0 | 0 | -1 | 12758 |
| 1505 | Giấy màu | 27 | 3 | 0 | 0 | -1 | 9846 |
| 1506 | Hộp đựng quà | 27 | 3 | 0 | 0 | -1 | 12759 |
| 1507 | Sôcôla Trái Tim | 27 | 3 | 0 | 0 | -1 | 12760 |
| 1508 | Hoa hồng giấy | 27 | 3 | 0 | 0 | -1 | 10482 |
| 1509 | Nơ trang trí | 27 | 3 | 0 | 0 | -1 | 12761 |
| 1510 | Hộp quà nhẹ nhàng | 27 | 3 | 0 | 0 | -1 | 12762 |
| 1511 | Hộp quà chỉn chu | 27 | 3 | 0 | 0 | -1 | 12763 |
| 1512 | CT Lý Tiểu Nương Bikini | 5 | 3 | 0 | 0 | -1 | 12725 |
| 1513 | Song mã hoàng gia | 23 | 3 | 0 | 0 | 29 | 12769 |
| 1514 | Em xinh, em đẹp | 36 | 3 | 0 | 0 | 253 | 12768 |
| 1515 | Chậu hoa hồng | 11 | 3 | 0 | 0 | 108 | 12774 |
| 1516 | Hộp Mù Tình Yêu | 27 | 3 | 0 | 0 | -1 | 10892 |
| 1517 | Sát thương chuẩn | 29 | 3 | 0 | 0 | -1 | 12766 |
| 1518 | Sát thương chuẩn 2 | 29 | 3 | 0 | 0 | -1 | 12767 |
| 1519 | Bong bóng thiên thần | 11 | 3 | 0 | 0 | 109 | 12783 |
| 1520 | Bong bóng hoa hồng | 11 | 3 | 0 | 0 | 110 | 12784 |
| 1521 | Thiệp mừng 8-3 | 27 | 3 | 0 | 0 | -1 | 11805 |
| 1522 | Bùa Trí tuệ Đệ Tử | 13 | 3 | 0 | 0 | -1 | 1403 |
| 1523 | Tự động luyện tập 2 | 27 | 3 | 0 | 0 | -1 | 4387 |
| 1524 | Tự động luyện tập 3 | 27 | 3 | 0 | 0 | -1 | 4387 |
| 1525 | Túi hạt giống Hoa Hồng | 27 | 3 | 0 | 0 | -1 | 12764 |
| 1526 | Đất trồng cây | 27 | 3 | 0 | 0 | -1 | 10478 |
| 1527 | Ống tre nước | 27 | 3 | 0 | 0 | -1 | 12765 |
| 1528 | Chậu đất | 27 | 3 | 0 | 0 | -1 | 10483 |
| 1529 | Thuốc tăng trưởng | 27 | 3 | 0 | 0 | -1 | 10481 |
| 1530 | Bông hoa hồng | 27 | 3 | 0 | 0 | -1 | 5206 |
| 1531 | Cánh thiên thần 3 | 11 | 3 | 0 | 0 | 111 | 12833 |
| 1532 | Rađa kho báu | 29 | 3 | 0 | 0 | -1 | 12834 |
| 1533 | Chuột mặp | 27 | 3 | 0 | 0 | -1 | 12831 |
| 1534 | Thuyền Âu Lạc | 23 | 3 | 0 | 0 | 30 | 12844 |
| 1535 | Thỏi vàng | 27 | 3 | 0 | 0 | -1 | 4028 |
| 1536 | Đệ Black Goku | 29 | 3 | 0 | 0 | 0 | 14334 |
| 1537 | Hộp thần linh | 29 | 3 | 0 | 0 | 0 | 14520 |
| 1538 | Hộp quà Set kích hoạt 5 sao | 27 | 3 | 0 | 0 | -1 | 11201 |
| 1539 | Bụi tre | 11 | 3 | 0 | 0 | 112 | 12843 |
| 1540 | Rađa phóng xạ | 27 | 3 | 0 | 0 | -1 | 12834 |
| 1541 | Môtô Bun ma | 23 | 3 | 0 | 0 | 31 | 12864 |
| 1542 | Bánh dầy | 27 | 3 | 0 | 0 | -1 | 12848 |
| 1543 | Bánh dầy | 27 | 3 | 0 | 0 | -1 | 12849 |
| 1544 | Chả lụa | 27 | 3 | 0 | 0 | -1 | 12850 |
| 1545 | Muối tiêu | 27 | 3 | 0 | 0 | -1 | 12851 |
| 1546 | Cơm nếp | 27 | 3 | 0 | 0 | -1 | 11195 |
| 1547 | Bột gạo | 27 | 3 | 0 | 0 | -1 | 7069 |
| 1548 | Đậu xanh | 27 | 3 | 0 | 0 | -1 | 7070 |
| 1549 | Thịt tươi | 27 | 3 | 0 | 0 | -1 | 7068 |
| 1550 | Pet Godzilla | 27 | 3 | 0 | 0 | -1 | 12852 |
| 1551 | Pet Kong | 27 | 3 | 0 | 0 | -1 | 12853 |
| 1552 | Xiên nướng | 29 | 3 | 0 | 0 | -1 | 12865 |
| 1553 | CT Goku SSJ4 | 5 | 3 | 0 | 0 | -1 | 12898 |
| 1554 | Tàu ngầm 19 Cam | 23 | 3 | 0 | 0 | 32 | 12934 |
| 1555 | Tàu ngầm 19 Vàng | 23 | 3 | 0 | 0 | 33 | 12933 |
| 1556 | Bánh chưng Lang Liêu | 27 | 3 | 0 | 0 | -1 | 11200 |
| 1557 | Hắc Mị Nương | 5 | 3 | 0 | 0 | -1 | 12901 |
| 1558 | Tem chứng nhận Mai An Tiêm | 27 | 3 | 0 | 0 | -1 | 12935 |
| 1559 | Capsule 1 món kích hoạt | 27 | 3 | 0 | 0 | -1 | 7223 |
| 1560 | Rương ngọc rồng | 27 | 3 | 0 | 0 | -1 | 12846 |
| 1561 | Chìa khóa vàng | 27 | 3 | 0 | 0 | -1 | 12845 |
| 1562 | Mặt trời tí hon | 11 | 3 | 0 | 0 | 113 | 12953 |
| 1563 | Ván bay té nước | 23 | 3 | 0 | 0 | 34 | 12996 |
| 1564 | Pet Po | 27 | 3 | 0 | 0 | -1 | 12997 |
| 1565 | Bản đồ truyền thuyết | 27 | 3 | 0 | 0 | -1 | 12954 |
| 1566 | CT Lích Tên béo | 5 | 3 | 0 | 0 | -1 | 8147 |
| 1567 | CT Frieren | 5 | 3 | 0 | 0 | -1 | 12987 |
| 1568 | Pet Shimo | 27 | 3 | 0 | 0 | -1 | 12993 |
| 1569 | Kho báu hải tặc | 27 | 3 | 0 | 0 | -1 | 12846 |
| 1570 | Chìa khóa bạc | 27 | 3 | 0 | 0 | -1 | 12847 |
| 1571 | Gậy phép của Frieren | 11 | 3 | 0 | 0 | 114 | 13006 |
| 1572 | Goku bay | 11 | 3 | 0 | 0 | 115 | 12998 |
| 1573 | Xe tăng Santa | 27 | 3 | 0 | 0 | -1 | 12999 |
| 1574 | Cánh sấm sét | 11 | 3 | 0 | 0 | 116 | 13032 |
| 1575 | Pháo bông | 27 | 3 | 0 | 0 | -1 | 4548 |
| 1576 | Pháo bông VIP | 27 | 3 | 0 | 0 | -1 | 4548 |
| 1577 | Cánh sấm sét hoàng kim | 11 | 3 | 0 | 0 | 117 | 13046 |
| 1578 | Mây mưa | 23 | 3 | 0 | 0 | 35 | 13053 |
| 1579 | 1 sao | 11 | 3 | 0 | 0 | 118 | 13063 |
| 1580 | 2 sao | 11 | 3 | 0 | 0 | 119 | 13118 |
| 1581 | 3 sao | 11 | 3 | 0 | 0 | 120 | 13080 |
| 1582 | 4 sao | 11 | 3 | 0 | 0 | 121 | 13090 |
| 1583 | 5 sao | 11 | 3 | 0 | 0 | 122 | 13099 |
| 1584 | 6 sao | 11 | 3 | 0 | 0 | 123 | 13108 |
| 1585 | 7 sao | 11 | 3 | 0 | 0 | 124 | 13117 |
| 1586 | Super 1 sao | 11 | 3 | 0 | 0 | 125 | 13175 |
| 1587 | CT Goku SSJ3 | 5 | 3 | 0 | 0 | -1 | 13209 |
| 1588 | CT Goku | 5 | 3 | 0 | 0 | -1 | 17 |
| 1589 | CT Goku SSJ | 5 | 3 | 0 | 0 | -1 | 1330 |
| 1590 | CT Goku SSJ Blue | 5 | 3 | 0 | 0 | -1 | 13223 |
| 1591 | Hộp quà Goku Day | 27 | 3 | 0 | 0 | -1 | 11006 |
| 1592 | Hộp quà Goku Day VIP | 27 | 3 | 0 | 0 | -1 | 11202 |
| 1593 | CT Goku SSJ God | 5 | 3 | 0 | 0 | -1 | 13298 |
| 1594 | Hộp quà Goku Day | 27 | 3 | 0 | 0 | -1 | 11201 |
| 1595 | CT Goku SSJ2 | 5 | 3 | 0 | 0 | -1 | 13215 |
| 1596 | Pet Albart | 27 | 3 | 0 | 0 | -1 | 13276 |
| 1597 | Pet Albart Cup | 27 | 3 | 0 | 0 | -1 | 13277 |
| 1598 | Trực thăng thỏ đế | 23 | 3 | 0 | 0 | 36 | 13176 |
| 1599 | CT Fide nhí | 5 | 3 | 0 | 0 | 1476 | 13302 |
| 1600 | CT Xên nhí | 5 | 3 | 0 | 0 | 1479 | 13304 |
| 1601 | CT Mabư nhí | 5 | 3 | 0 | 0 | 1482 | 13303 |
| 1602 | CT Android 21 kid | 5 | 3 | 0 | 0 | 1485 | 13430 |
| 1603 | Tên lửa cá mập | 23 | 3 | 0 | 0 | 37 | 13432 |
| 1604 | CT Sư | 5 | 3 | 0 | 0 | -1 | 13303 |
| 1605 | Hộp quà thiếu nhi | 27 | 3 | 0 | 0 | -1 | 11006 |
| 1606 | Hộp quà thiếu nhi | 27 | 3 | 0 | 0 | -1 | 11202 |
| 1607 | Hộp quà thiếu nhi | 27 | 3 | 0 | 0 | -1 | 11201 |
| 1608 | Hộp quà thiếu nhi | 27 | 3 | 0 | 0 | -1 | 12762 |
| 1609 | Kem trái cây | 27 | 3 | 0 | 0 | -1 | 9605 |
| 1610 | Que kem | 27 | 3 | 0 | 0 | -1 | 13444 |
| 1611 | Pet Minion Otto | 27 | 3 | 0 | 0 | -1 | 13451 |
| 1612 | Khúc mía | 27 | 3 | 0 | 0 | -1 | 13466 |
| 1613 | Nước đá | 27 | 3 | 0 | 0 | -1 | 13465 |
| 1614 | Ly mía khổng lồ | 29 | 3 | 0 | 0 | -1 | 13462 |
| 1615 | Ly mía thơm | 29 | 3 | 0 | 0 | -1 | 13463 |
| 1616 | Ly mía sầu riêng | 29 | 3 | 0 | 0 | -1 | 13464 |
| 1617 | Cá nóc nâu | 11 | 3 | 0 | 0 | 126 | 13468 |
| 1618 | Cá nóc vàng | 11 | 3 | 0 | 0 | 127 | 13467 |
| 1619 | Túi nước mía | 11 | 3 | 0 | 0 | 128 | 13258 |
| 1620 | Pet Baby Shark | 27 | 3 | 0 | 0 | -1 | 13487 |
| 1621 | Pet Baby Shark | 27 | 3 | 0 | 0 | -1 | 13487 |
| 1622 | Pet Baby Shark | 27 | 3 | 0 | 0 | -1 | 13487 |
| 1623 | Trái bóng Euro | 11 | 3 | 0 | 0 | 129 | 13500 |
| 1624 | Cup Euro | 11 | 3 | 0 | 0 | 130 | 13505 |
| 1625 | Tivi bay | 23 | 3 | 0 | 0 | 38 | 13506 |
| 1626 | Thẻ trọng tài | 11 | 3 | 0 | 0 | 131 | 13538 |
| 1627 | TT | 27 | 3 | 0 | 0 | -1 | 13539 |
| 1628 | Bùa x2 tn,sm đệ tử | 29 | 3 | 0 | 0 | -1 | 13540 |
| 1629 | Pet Capybara đeo ba lô | 27 | 3 | 0 | 0 | -1 | 13542 |
| 1630 | Pet Capybara xì mũi | 27 | 3 | 0 | 0 | -1 | 13541 |
| 1631 | Pet Zịt vàng bối rối | 27 | 3 | 0 | 0 | -1 | 13571 |
| 1632 | CT Himmel | 5 | 3 | 0 | 0 | -1 | 13604 |
| 1633 | Pet Mini Desutoron Gas | 27 | 3 | 0 | 0 | -1 | 13617 |
| 1634 | Cápsule Vỡ | 27 | 3 | 0 | 0 | -1 | 13560 |
| 1635 | Cỏ bốn lá | 29 | 3 | 0 | 0 | -1 | 13618 |
| 1636 | Spain | 27 | 3 | 0 | 0 | -1 | 13537 |
| 1637 | Georgia | 27 | 3 | 0 | 0 | -1 | 13537 |
| 1638 | Germany | 27 | 3 | 0 | 0 | -1 | 13537 |
| 1639 | Denmark | 27 | 3 | 0 | 0 | -1 | 13537 |
| 1640 | Portugal | 27 | 3 | 0 | 0 | -1 | 13537 |
| 1641 | Slovenia | 27 | 3 | 0 | 0 | -1 | 13537 |
| 1642 | France | 27 | 3 | 0 | 0 | -1 | 13537 |
| 1643 | Belgium | 27 | 3 | 0 | 0 | -1 | 13537 |
| 1644 | Romania | 27 | 3 | 0 | 0 | -1 | 13537 |
| 1645 | Netherlands | 27 | 3 | 0 | 0 | -1 | 13537 |
| 1646 | Austria | 27 | 3 | 0 | 0 | -1 | 13537 |
| 1647 | Turkey | 27 | 3 | 0 | 0 | -1 | 13537 |
| 1648 | England | 27 | 3 | 0 | 0 | -1 | 13537 |
| 1649 | Slovakia | 27 | 3 | 0 | 0 | -1 | 13537 |
| 1650 | Switzerland | 27 | 3 | 0 | 0 | -1 | 13537 |
| 1651 | Italy | 27 | 3 | 0 | 0 | -1 | 13537 |
| 1652 | Loa to thế giới | 27 | 3 | 0 | 0 | -1 | 13619 |
| 1653 | Loa to liên vũ trụ | 27 | 3 | 0 | 0 | -1 | 13620 |
| 1654 | Pet Cerberus | 27 | 3 | 0 | 0 | -1 | 13631 |
| 1655 | Cápsule Kích hoạt 1 món tự chọn | 27 | 3 | 0 | 0 | -1 | 9109 |
| 1656 | Khoáng tái chế | 27 | 3 | 0 | 0 | -1 | 13633 |
| 1657 | CT Gohan đi biển | 5 | 3 | 0 | 0 | -1 | 13696 |
| 1658 | Đầu trâu | 5 | 0 | 0 | 0 | 1531 | 13649 |
| 1659 | Đầu trâu | 5 | 1 | 0 | 0 | 1535 | 13657 |
| 1660 | Đầu trâu | 5 | 2 | 0 | 0 | 1533 | 13641 |
| 1661 | Mặt ngựa | 5 | 0 | 0 | 0 | 1537 | 13653 |
| 1662 | Mặt ngựa | 5 | 1 | 0 | 0 | 1541 | 13661 |
| 1663 | Mặt ngựa | 5 | 2 | 0 | 0 | 1539 | 13645 |
| 1664 | Vỏ Xên bọ hung | 27 | 3 | 0 | 0 | -1 | 13461 |
| 1665 | Túi đựng vỏ Xên bọ hung | 27 | 3 | 0 | 0 | -1 | 13748 |
| 1666 | Rađa dò vỏ Xên | 27 | 3 | 0 | 0 | -1 | 2758 |
| 1667 | CT Gohan kính mát | 5 | 3 | 0 | 0 | -1 | 13732 |
| 1668 | Pet Capybara hồng | 27 | 3 | 0 | 0 | -1 | 13740 |
| 1669 | Balo Capybara | 11 | 3 | 0 | 0 | 132 | 13743 |
| 1670 | Balo Capybara hồng | 11 | 3 | 0 | 0 | 133 | 13746 |
| 1671 | Rương mùa hè | 27 | 3 | 0 | 0 | -1 | 12846 |
| 1672 | Sầu riêng 6 múi | 29 | 3 | 0 | 0 | -1 | 13747 |
| 1673 | Tay nhanh hơn não | 36 | 3 | 0 | 0 | 254 | 13634 |
| 1674 | CT Pikkon | 5 | 3 | 0 | 0 | -1 | 13798 |
| 1675 | Lồng đèn kéo quân NRO | 11 | 3 | 0 | 0 | 134 | 13761 |
| 1676 | Phong hỏa luân | 23 | 3 | 0 | 0 | 39 | 13762 |
| 1677 | Xe xanh Chi Chi | 23 | 3 | 0 | 0 | 40 | 13801 |
| 1678 | Xe đỏ Bun ma | 23 | 3 | 0 | 0 | 41 | 13800 |
| 1679 | Cờ Olympic | 11 | 3 | 0 | 0 | 135 | 13806 |
| 1680 | Hỏa tiêm thương | 11 | 3 | 0 | 0 | 136 | 13852 |
| 1681 | Cờ Hải Ly xe máy | 11 | 3 | 0 | 0 | 137 | 13879 |
| 1682 | Pet Hải Ly | 27 | 3 | 0 | 0 | -1 | 13877 |
| 1683 | Pet Hải Ly Ong Vàng | 27 | 3 | 0 | 0 | -1 | 13878 |
| 1684 | CT lân | 5 | 3 | 0 | 0 | -1 | 13914 |
| 1685 | CT rồng | 5 | 3 | 0 | 0 | -1 | 13947 |
| 1686 | Pet Thỏ Ú | 27 | 3 | 0 | 0 | -1 | 13953 |
| 1687 | Cờ Goku bay | 11 | 3 | 0 | 0 | 138 | 13952 |
| 1688 | Cờ Hắc Vô Thường | 11 | 3 | 0 | 0 | 139 | 13052 |
| 1689 | Cờ Bạch Vô Thường | 11 | 3 | 0 | 0 | 140 | 13049 |
| 1690 | Phiếu đổi Cápsule | 27 | 3 | 0 | 0 | -1 | 13948 |
| 1691 | Bùa bình an | 27 | 3 | 0 | 0 | -1 | 1409 |
| 1692 | Bùa may mắn | 27 | 3 | 0 | 0 | -1 | 1407 |
| 1693 | CT Cađíc SSJ4 | 5 | 3 | 0 | 0 | -1 | 13992 |
| 1694 | Labubu | 11 | 3 | 0 | 0 | 141 | 13995 |
| 1695 | Hộp quà tháng 9 | 27 | 3 | 0 | 0 | -1 | 11006 |
| 1696 | Hộp quà tháng 9 VIP | 27 | 3 | 0 | 0 | -1 | 11202 |
| 1697 | CT Gogeta | 5 | 3 | 0 | 0 | -1 | 14047 |
| 1698 | CT Urôn Trư Bát Giới | 5 | 3 | 0 | 0 | -1 | 14082 |
| 1699 | Bồ cào 9 răng | 11 | 3 | 0 | 0 | 142 | 14080 |
| 1700 | CT Hằng Nga | 5 | 3 | 0 | 0 | -1 | 14115 |
| 1701 | Hộp bánh Trung Thu đặc biệt | 27 | 3 | 0 | 0 | -1 | 11717 |
| 1702 | Lưỡi hái hồng | 11 | 3 | 0 | 0 | 143 | 14010 |
| 1703 | Gia hạn mầm | 27 | 3 | 0 | 0 | -1 | 5427 |
| 1704 | Bí Ngô Cánh Dơi | 23 | 3 | 0 | 0 | 43 | 14117 |
| 1705 | Cải Trang Chi Chi Võ Đài | 5 | 3 | 0 | 0 | -1 | 14149 |
| 1706 | Lưỡi hái | 11 | 3 | 0 | 0 | 300 | 14010 |
| 1707 | <Cập nhật phiên bản mới để xem> | 27 | 3 | 0 | 0 | -1 | 544 |
| 1708 | CT Android 21 Thân Thiện | 5 | 3 | 0 | 0 | -1 | 14186 |
| 1709 | CT Android 21 Evil | 5 | 3 | 0 | 0 | -1 | 14194 |
| 1710 | CT Android 21 Tiến Sĩ | 5 | 3 | 0 | 0 | -1 | 14241 |
| 1711 | Cân Đẩu Vân Thơ Mộng | 23 | 3 | 0 | 0 | 44 | 14203 |
| 1712 | Pet Rồng xương | 27 | 3 | 0 | 0 | -1 | 14258 |
| 1713 | Khủng Long Thơ Mộng | 11 | 3 | 0 | 0 | 144 | 14242 |
| 1714 | Pet Giru | 27 | 3 | 0 | 0 | -1 | 14204 |
| 1715 | Đậu thần cấp 11 | 6 | 3 | 0 | 0 | -1 | 14259 |
| 1716 | Giáp tập luyện cấp 4 | 32 | 3 | 0 | 1500000 | -1 | 14260 |
| 1717 | Gia hạn mầm | 27 | 3 | 0 | 0 | -1 | 5427 |
| 1718 | Thiệp chúc VIP | 27 | 3 | 0 | 0 | -1 | 11805 |
| 1719 | Hộp quà 20/10 | 27 | 3 | 0 | 0 | -1 | 12762 |
| 1720 | Hộp quà 20/10 | 27 | 3 | 0 | 0 | -1 | 12762 |
| 1721 | Cá Koi zombie | 11 | 3 | 0 | 0 | 145 | 14261 |
| 1722 | Cánh Thiên thần - Ác quỷ | 11 | 3 | 0 | 0 | 146 | 14271 |
| 1723 | Cải trang Chan Xư | 5 | 3 | 0 | 0 | -1 | 3682 |
| 1724 | Ván bay Sọ Dừa | 23 | 3 | 0 | 0 | 45 | 14272 |
| 1725 | Gói quà Halloween | 27 | 3 | 0 | 0 | -1 | 11739 |
| 1726 | Kẹo mù Halloween | 27 | 3 | 0 | 0 | -1 | 11749 |
| 1727 | Pet Ma cầu mưa | 27 | 3 | 0 | 0 | -1 | 14282 |
| 1728 | Túi mù Halloween | 27 | 3 | 0 | 0 | -1 | 14284 |
| 1729 | Pet Rồng Xanh | 27 | 3 | 0 | 0 | -1 | 14294 |
| 1730 | Vé ước miễn phí | 27 | 3 | 0 | 0 | -1 | 11474 |
| 1731 | CT Black Goku Rose | 5 | 3 | 0 | 0 | -1 | 14336 |
| 1732 | CT Black Goku | 5 | 3 | 0 | 0 | -1 | 14334 |
| 1733 | Thú cưỡi Thích Kim Quy | 23 | 3 | 0 | 0 | 47 | 14343 |
| 1734 | Thú cưỡi Phong Xích Lan | 23 | 3 | 0 | 0 | 46 | 14342 |
| 1735 | Đeo lưng dụng cụ học tập | 11 | 3 | 0 | 0 | 147 | 14337 |
| 1736 | Hộp quà FA | 27 | 3 | 0 | 0 | -1 | 12763 |
| 1737 | Rađa dò Zombie | 27 | 3 | 0 | 0 | -1 | 2758 |
| 1738 | Hộp quà FA | 27 | 3 | 0 | 0 | -1 | 12763 |
| 1739 | Hộp quà 20-11 | 27 | 3 | 0 | 0 | -1 | 12763 |
| 1740 | Sách giáo khoa | 27 | 3 | 0 | 0 | -1 | 14344 |
| 1741 | CT Cađíc | 5 | 3 | 0 | 0 | -1 | 14376 |
| 1742 | CT Cađíc SSJ | 5 | 3 | 0 | 0 | -1 | 14377 |
| 1743 | CT Cađíc SSJ2 | 5 | 3 | 0 | 0 | -1 | 14378 |
| 1744 | CT Cađíc SSJ2 M | 5 | 3 | 0 | 0 | -1 | 14379 |
| 1745 | CT Cađíc SSJ3 | 5 | 3 | 0 | 0 | -1 | 14380 |
| 1746 | CT Cađíc SSJ Blue | 5 | 3 | 0 | 0 | -1 | 14381 |
| 1747 | Hộp quà Black Friday | 27 | 3 | 0 | 0 | -1 | 14514 |
| 1748 | Pet tuần lộc | 27 | 3 | 0 | 0 | -1 | 14509 |
| 1749 | Thú cưỡi Cây thông | 23 | 3 | 0 | 0 | 48 | 14510 |
| 1750 | Pet Khủng Long ngok | 27 | 3 | 0 | 0 | -1 | 13259 |
| 1751 | Diều rồng băng | 11 | 3 | 0 | 0 | 148 | 14511 |
| 1752 | Đai lưng | 17 | 3 | 0 | 0 | -1 | 14524 |
| 1753 | Đai lưng | 17 | 3 | 0 | 0 | -1 | 14523 |
| 1754 | Đai lưng | 17 | 3 | 0 | 0 | -1 | 14525 |
| 1755 | Hộp quà Cađíc | 27 | 3 | 0 | 0 | -1 | 14520 |
| 1756 | Hộp quà Cađíc | 27 | 3 | 0 | 0 | -1 | 14520 |
| 1757 | Hộp quà Cađíc VIP | 27 | 3 | 0 | 0 | -1 | 14521 |
| 1758 | Phở Linh Lang | 29 | 3 | 0 | 1500000 | 0 | 16282 |
| 1759 | Phở Giải Phóng | 29 | 3 | 0 | 1500000 | 0 | 16323 |
| 1760 | Phở Công viên Hòa Bình | 29 | 3 | 0 | 1500000 | 0 | 16324 |
| 1761 | Mị Hoàng Kim | 5 | 3 | 0 | 0 | 1656 | 15004 |
| 1762 | hùng vương | 5 | 3 | 0 | 0 | 1659 | 15068 |
| 1763 | Sơn tinh | 5 | 3 | 0 | 0 | 1683 | 15163 |
| 1764 | Thủy Tinh | 5 | 3 | 0 | 0 | 1686 | 15196 |
| 1765 | Bé Rồng Cute | 27 | 3 | 0 | 0 | 1662 | 15077 |
| 1766 | Bé Rồng Cute | 27 | 3 | 0 | 0 | 1665 | 15085 |
| 1767 | Bé Rồng Cute | 27 | 3 | 0 | 0 | 1668 | 15093 |
| 1768 | Bé Rồng Cute | 27 | 3 | 0 | 0 | 1671 | 15101 |
| 1769 | Bé Rồng Cute | 27 | 3 | 0 | 0 | 1674 | 15109 |
| 1770 | Bé Rồng Cute | 27 | 3 | 0 | 0 | 1677 | 15117 |
| 1771 | Bé Rồng Cute | 27 | 3 | 0 | 0 | 1680 | 16187 |
| 1772 | búa sơn tinh | 11 | 3 | 0 | 0 | 148 | 15001 |
| 1773 | bút thủy tinh | 11 | 3 | 0 | 0 | 149 | 14993 |
| 1774 | Đệ Black Goku | 29 | 3 | 0 | 0 | 0 | 14334 |
| 1775 | Hộp thần linh | 29 | 3 | 0 | 0 | 0 | 14520 |
| 1776 | Hộp quà sự kiện | 29 | 3 | 0 | 0 | 0 | 15131 |
| 1777 | Hộp quà sự kiện VIP | 29 | 3 | 0 | 0 | 0 | 15130 |
| 1778 | Cuốn chả giò | 27 | 3 | 0 | 0 | -1 | 15294 |
| 1779 | Cải trang buma | 5 | 3 | 0 | 0 | 1689 | 14865 |
| 1780 | Cải trang goku | 5 | 3 | 0 | 0 | 1692 | 14952 |
| 1781 | Cải trang gohan | 5 | 3 | 0 | 0 | 1697 | 15283 |
| 1782 | Cải trang chichi | 5 | 3 | 0 | 0 | 1702 | 14863 |
| 1783 | Cải trang broly | 5 | 3 | 0 | 0 | 1705 | 14712 |
| 1784 | Cải trang broly red | 5 | 3 | 0 | 0 | 1710 | 14736 |
| 1785 | Cải trang baby | 5 | 3 | 0 | 0 | 1715 | 14567 |
| 1786 | Cải trang vegeta baby | 5 | 3 | 0 | 0 | 1718 | 14600 |
| 1787 | Vé riêng tư | 75 | 3 | 0 | 0 | 0 | 13948 |
| 1788 | Cải trang baby khỉ | 5 | 3 | 0 | 0 | 1721 | 14631 |
| 1789 | Thỏ may mắn | 27 | 3 | 0 | 0 | 0 | 15247 |
| 1790 | Mẹ Rồng | 75 | 3 | 0 | 0 | 0 | 15358 |
| 1791 | Mảnh khỉ Oorazu | 33 | 3 | 0 | 0 | 0 | 14247 |
| 1792 | Mảnh khỉ Oorazu 1 | 33 | 3 | 0 | 0 | 0 | 14248 |
| 1793 | Mảnh khỉ Oorazu 2 | 33 | 3 | 0 | 0 | 0 | 14249 |
| 1794 | Con dấu | 75 | 3 | 0 | 0 | 0 | 15412 |
| 1795 | Bình hút năng lượng | 75 | 3 | 0 | 0 | 0 | 15359 |
| 1796 | Đệ tử vegeta | 5 | 3 | 0 | 0 | 1743 | 18602 |
| 1797 | Cải trang Picolo | 5 | 3 | 0 | 0 | 1746 | 15232 |
| 1798 | Tayaki | 75 | 3 | 0 | 0 | 0 | 15416 |
| 1799 | Kẹo táo | 75 | 3 | 0 | 0 | 0 | 15414 |
| 1800 | Kem que đôi | 75 | 3 | 0 | 0 | 0 | 15415 |
| 1801 | Mochi | 75 | 3 | 0 | 0 | 0 | 15418 |
| 1802 | Ramen | 75 | 3 | 0 | 0 | 0 | 15417 |
| 1803 | Cadic đi biển | 5 | 3 | 0 | 0 | 1749 | 15450 |
| 1804 | Buma đi biển | 5 | 3 | 0 | 0 | 1752 | 15483 |
| 1805 | Phiếu thức ăn | 75 | 3 | 0 | 0 | 0 | 15485 |
| 1806 | Kem que | 11 | 3 | 0 | 0 | 150 | 15495 |
| 1807 | Goku ssj 4 kid | 5 | 3 | 0 | 0 | 1755 | 14985 |
| 1808 | Kid jiren | 5 | 3 | 0 | 0 | 876 | 8063 |
| 1809 | Super 2 sao | 11 | 3 | 0 | 0 | 151 | 13175 |
| 1810 | Super 3 sao | 11 | 3 | 0 | 0 | 152 | 13175 |
| 1811 | Super 4 sao | 11 | 3 | 0 | 0 | 153 | 13175 |
| 1812 | Guardian Angel | 11 | 3 | 0 | 0 | 154 | 16835 |
| 1813 | Fallen Angel | 11 | 3 | 0 | 0 | 155 | 16223 |
| 1814 | Eternal Hope | 11 | 3 | 0 | 0 | 156 | 16174 |
| 1815 | ct dep vcl | 5 | 3 | 0 | 0 | 1851 | 15537 |
| 1816 | td | 5 | 3 | 0 | 0 | 1866 | 15622 |
| 1817 | nm | 5 | 3 | 0 | 0 | 1861 | 15611 |
| 1818 | xd | 5 | 3 | 0 | 0 | 1856 | 15574 |
| 1819 | Bông tai Porata | 27 | 3 | 1 | 1500000 | -1 | 15614 |
| 1820 | Mảnh vỡ bông tai cấp 3 | 27 | 3 | 1 | 0 | -1 | 15613 |
| 1821 | Trứng vàng rồng nhí | 75 | 3 | 0 | 0 | 0 | 15128 |
| 1822 | Rada ngọc rồng | 75 | 3 | 0 | 0 | 0 | 15002 |
| 1823 | Rada ngọc rồng Vip | 75 | 3 | 0 | 0 | 0 | 15003 |
| 1824 | Cậu Vàng | 75 | 3 | 0 | 1500000 | 0 | 16290 |
| 1825 | Vé Nhệm Vụ KOL Vip | 75 | 3 | 0 | 0 | 0 | 15293 |
| 1826 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1827 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1828 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1829 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1830 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1831 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1832 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1833 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1834 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1835 | Vé nhiệm vụ tháng Vip | 75 | 3 | 0 | 0 | 0 | 15293 |
| 1836 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1837 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1838 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1839 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1840 | Hộp quà Goku Day VIP | 75 | 3 | 0 | 0 | 0 | 14519 |
| 1841 | Cải trang Gốc | 5 | 3 | 0 | 1500000 | -1 | 21524 |
| 1842 | Cải trang Gốc | 5 | 3 | 0 | 1500000 | -1 | 21472 |
| 1843 | Cải trang Gốc | 5 | 3 | 0 | 1500000 | -1 | 21414 |
| 1844 | Cải trang Gốc | 5 | 3 | 0 | 1500000 | -1 | 21411 |
| 1845 | Cải trang Gốc | 5 | 3 | 0 | 1500000 | -1 | 21376 |
| 1846 | Cải trang Gốc | 5 | 3 | 0 | 1500000 | -1 | 21308 |
| 1847 | Cải trang Gốc | 5 | 3 | 0 | 1500000 | -1 | 21304 |
| 1848 | Cải trang Gốc | 5 | 3 | 0 | 1500000 | -1 | 21267 |
| 1849 | Cải trang Gốc | 5 | 3 | 0 | 1500000 | -1 | 20444 |
| 1850 | Cải trang Gốc | 5 | 3 | 0 | 1500000 | -1 | 20448 |
| 1851 | Broly Nes | 5 | 3 | 0 | 1200 | -1 | 20097 |
| 1852 | Cải trang Gốc | 5 | 3 | 0 | 1500000 | -1 | 16433 |
| 1853 | Cải trang Gốc | 5 | 3 | 0 | 1500000 | -1 | 16398 |
| 1854 | Cải trang Gốc | 5 | 3 | 0 | 1500000 | -1 | 16367 |
| 1855 | Cải trang Gốc | 5 | 3 | 0 | 1500000 | -1 | 16318 |
| 1856 | Cải trang Gốc | 5 | 3 | 0 | 1500000 | -1 | 16280 |
| 1857 | Cải trang Gốc | 5 | 3 | 0 | 1500000 | -1 | 16252 |
| 1858 | Cải trang Gốc | 5 | 3 | 0 | 1500000 | -1 | 16131 |
| 1859 | Cải trang Gốc | 5 | 3 | 0 | 1500000 | -1 | 16102 |
| 1860 | Cải trang Gốc 1 | 5 | 3 | 0 | 1500000 | -1 | 16520 |
| 1861 | Cải trang Gốc 2 | 5 | 3 | 0 | 1500000 | -1 | 16614 |
| 1862 | Cải trang Gốc 3 | 5 | 3 | 0 | 1500000 | -1 | 16643 |
| 1863 | Cải trang Gốc 4 | 5 | 3 | 0 | 1500000 | -1 | 16763 |
| 1864 | Cải trang Gốc 5 | 5 | 3 | 0 | 1500000 | -1 | 16819 |
| 1865 | Cải trang Gốc 6 | 5 | 3 | 0 | 1500000 | -1 | 16862 |
| 1866 | Cải trang Gốc 7 | 5 | 3 | 0 | 1500000 | -1 | 16891 |
| 1867 | Cải trang Gốc 8 | 5 | 3 | 0 | 1500000 | -1 | 16064 |
| 1868 | Cải trang Gốc 9 | 5 | 3 | 0 | 1500000 | -1 | 16482 |
| 1869 | Giáp tập luyện cấp 5 | 32 | 3 | 0 | 1500000 | -1 | 16897 |
| 1870 | Cải trang Gốc 10 | 5 | 3 | 0 | 1500000 | -1 | 16976 |
| 1871 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1872 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1873 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1874 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1875 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1876 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1877 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1878 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1879 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1880 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1881 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1882 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1883 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1884 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1885 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1886 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1887 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1888 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1889 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1890 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1891 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1892 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1893 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1894 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1895 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1896 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1897 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1898 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1899 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1900 | Ghost Rider | 27 | 3 | 0 | 1500000 | 1968 | 16187 |
| 1901 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1902 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1903 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1904 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1905 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1906 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1907 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1908 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1909 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1910 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1911 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1912 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1913 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1914 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1915 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1916 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1917 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1918 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1919 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1920 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1921 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1922 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1923 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1924 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1925 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1926 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1927 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1928 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1929 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1930 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1931 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1932 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1933 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1934 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1935 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1936 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1937 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1938 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1939 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1940 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1941 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1942 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1943 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1944 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1945 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1946 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1947 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1948 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1949 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1950 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1951 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1952 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1953 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1954 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1955 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1956 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1957 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1958 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1959 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1960 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1961 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1962 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1963 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1964 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1965 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1966 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1967 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1968 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1969 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1970 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1971 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1972 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1973 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1974 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1975 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1976 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1977 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1978 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1979 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1980 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1981 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1982 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1983 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1984 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1985 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1986 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1987 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1988 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1989 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1990 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1991 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1992 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1993 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1994 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1995 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1996 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1997 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1998 |  | 75 | 3 | 0 | 0 | 0 | 0 |
| 1999 |  | 75 | 3 | 0 | 0 | 0 | 0 |

## 4. skill_template – kỹ năng

Tổng: **27** template kỹ năng. Nạp tại `Manager.loadDatabase()` (`select * from skill_template order by nclass_id, slot`, dòng 413) → `Manager.NCLASS` (mỗi nclass một `NClass` chứa danh sách `SkillTemplate`). Cột `skills` là mảng JSON các cấp kỹ năng, mỗi phần tử là object `{id, point, power_require, mana_use, cool_down, dx, dy, max_fight, damage, price, info}` (code thay thế dấu nháy bao ngoài trước khi parse).

Bộ kỹ năng mặc định khi tạo nhân vật (`PlayerDAO.createNewPlayer()` dòng 254–256): Trái Đất `[0,1,6,9,10,20,22,19]`, Namếc `[2,3,7,11,12,17,18,19]`, Xayda `[4,5,8,13,14,21,23,19]`.

| nclass_id | id | Tên | slot | max_point | mana_use_type | TYPE | icon_id | dam_info | Số cấp |
|---|---|---|---|---|---|---|---|---|---|
| 0 (Trái Đất) | 0 | Chiêu đấm Dragon | 0 | 7 | 0 | 1 | 539 | Tăng sức đánh: #% | 7 |
| 0 (Trái Đất) | 1 | Chiêu Kamejoko | 1 | 7 | 0 | 1 | 540 | Tăng sức đánh: #% | 7 |
| 0 (Trái Đất) | 6 | Thái Dương Hạ San | 2 | 7 | 1 | 3 | 717 | Thời gian tác dụng: # mili giây | 7 |
| 0 (Trái Đất) | 9 | Kaioken | 3 | 7 | 0 | 1 | 716 | Tăng sức đánh: #% | 7 |
| 0 (Trái Đất) | 10 | Quả cầu kênh khi | 4 | 7 | 1 | 1 | 711 | Gây sát thương #% | 7 |
| 0 (Trái Đất) | 20 | Dịch chuyển tức thời | 5 | 7 | 0 | 1 | 3783 | Dịch chuyển tức thời và gây choáng kẻ thù | 7 |
| 0 (Trái Đất) | 22 | Thôi miên | 6 | 7 | 0 | 1 | 3782 | Ru ngủ kẻ thù # giây | 7 |
| 0 (Trái Đất) | 19 | Khiên năng lượng | 7 | 7 | 1 | 3 | 3784 | Vô hiệu các đòn tấn công | 7 |
| 0 (Trái Đất) | 24 | Super Kamejoko | 8 | 9 | 1 | 4 | 11162 | Tăng sức đánh: #% | 10 |
| 1 (Namếc) | 2 | Chiêu đấm Demon | 0 | 7 | 0 | 1 | 539 | Tăng sức đánh: #% | 7 |
| 1 (Namếc) | 3 | Chiêu Masenko | 1 | 7 | 0 | 1 | 540 | Tăng sức đánh: #% | 7 |
| 1 (Namếc) | 7 | Trị thương | 2 | 7 | 1 | 2 | 724 | Phục hồi #% HP và KI cho đồng đội | 7 |
| 1 (Namếc) | 11 | Makankosappo | 3 | 7 | 2 | 1 | 723 | Gây sát thương #% | 7 |
| 1 (Namếc) | 12 | Đẻ trứng | 4 | 7 | 1 | 3 | 722 | Tạo quái đi theo hỗ trợ | 7 |
| 1 (Namếc) | 17 | Liên hoàn | 5 | 7 | 0 | 1 | 3778 | Tăng sức đánh: #% | 7 |
| 1 (Namếc) | 18 | Biến Sôcôla | 6 | 7 | 1 | 1 | 3780 | Biến quái thành Sôcôla | 7 |
| 1 (Namếc) | 19 | Khiên năng lượng | 7 | 7 | 1 | 3 | 3784 | Vô hiệu các đòn tấn công | 7 |
| 1 (Namếc) | 26 | Ma phong ba | 8 | 9 | 1 | 4 | 11194 | Nhốt đối thủ vào bình chứa | 10 |
| 2 (Xayda) | 4 | Chiêu đấm Galick | 0 | 7 | 0 | 1 | 539 | Tăng sức đánh: #% | 7 |
| 2 (Xayda) | 5 | Chiêu Antomic | 1 | 7 | 0 | 1 | 540 | Tăng sức đánh: #% | 7 |
| 2 (Xayda) | 8 | Tái tạo năng lượng | 2 | 7 | 1 | 3 | 720 | Tự tái tạo HP MP #%/s | 7 |
| 2 (Xayda) | 13 | Biến hình | 3 | 7 | 1 | 3 | 718 | Tăng sức đánh, HP và tốc độ | 7 |
| 2 (Xayda) | 14 | Tự phát nổ | 4 | 7 | 1 | 3 | 2248 | Hy sinh, gây sát thương lớn cho kẻ thù | 7 |
| 2 (Xayda) | 21 | Huýt sáo | 5 | 7 | 0 | 3 | 3781 | Tăng tạm thời +#%HP cho mọi người xung quanh và +1 đòn chí mạng | 7 |
| 2 (Xayda) | 23 | Trói | 6 | 7 | 0 | 1 | 3779 | Trói kẻ thù | 7 |
| 2 (Xayda) | 19 | Khiên năng lượng | 7 | 7 | 1 | 3 | 3784 | Vô hiệu các đòn tấn công | 7 |
| 2 (Xayda) | 25 | Cađíc liên hoàn chưởng | 8 | 9 | 1 | 4 | 11193 | Tăng sức đánh: #% | 10 |

### 4.1 Chi tiết từng cấp kỹ năng

Cột: `skillId` (id cấp), `point` (cấp), `power_require`, `mana_use`, `cool_down` (ms), `damage`, `dx`/`dy` (tầm), `max_fight`, `price`, `info`.

**[0-0] Chiêu đấm Dragon**

| skillId | point | power_require | mana_use | cool_down | damage | dx | dy | max_fight | price | info |
|---|---|---|---|---|---|---|---|---|---|---|
| 0 | 1 | 1000 | 1 | 500 | 100 | 32 | 18 | 1 | 0 | tại ông nội ngay lúc đầu |
| 1 | 2 | 10000 | 2 | 500 | 110 | 34 | 18 | 1 | 10 | tại ông nội |
| 2 | 3 | 22000 | 4 | 500 | 120 | 36 | 18 | 1 | 50 | tại Quy Lão Kame |
| 3 | 4 | 66000 | 8 | 500 | 130 | 38 | 18 | 1 | 100 | tại Quy Lão Kame |
| 4 | 5 | 200000 | 16 | 500 | 140 | 40 | 18 | 1 | 500 | tại Quy Lão Kame |
| 5 | 6 | 600000 | 32 | 500 | 150 | 42 | 18 | 1 | 1000 | tại Quy Lão Kame |
| 6 | 7 | 1800000 | 70 | 500 | 160 | 44 | 18 | 1 | 2000 | tại Quy Lão Kame |

**[0-1] Chiêu Kamejoko**

| skillId | point | power_require | mana_use | cool_down | damage | dx | dy | max_fight | price | info |
|---|---|---|---|---|---|---|---|---|---|---|
| 7 | 1 | 10000 | 30 | 2000 | 150 | 160 | 160 | 1 | 500 | (Kame joko) Học tại Sư Phụ |
| 8 | 2 | 20000 | 60 | 2500 | 200 | 170 | 170 | 1 | 1000 | (Kame joko) Học tại Sư Phụ |
| 9 | 3 | 60000 | 120 | 3000 | 250 | 180 | 180 | 1 | 2000 | (Kame joko) Học tại Sư Phụ |
| 10 | 4 | 180000 | 240 | 3500 | 300 | 190 | 190 | 1 | 4000 | (Kame joko) Học tại Sư Phụ |
| 11 | 5 | 540000 | 480 | 4000 | 350 | 200 | 200 | 1 | 8000 | (Kame joko) Học tại Sư Phụ |
| 12 | 6 | 1600000 | 960 | 4500 | 400 | 210 | 210 | 1 | 9999 | (Kame joko) Học tại Sư Phụ |
| 13 | 7 | 4800000 | 1280 | 5000 | 450 | 220 | 220 | 1 | 9999 | (Kame joko) Học tại Sư Phụ |

**[0-6] Thái Dương Hạ San**

| skillId | point | power_require | mana_use | cool_down | damage | dx | dy | max_fight | price | info |
|---|---|---|---|---|---|---|---|---|---|---|
| 42 | 1 | 60000 | 45 | 60000 | 3000 | 150 | 150 | 1 | 500 | (TDHS 1) Học tại Quy Lão Kame |
| 43 | 2 | 120000 | 40 | 55000 | 4000 | 180 | 180 | 1 | 1000 | (TDHS 2) Học tại Quy Lão Kame |
| 44 | 3 | 360000 | 35 | 50000 | 5000 | 210 | 210 | 1 | 2000 | (TDHS 3) Học tại Quy Lão Kame |
| 45 | 4 | 1000000 | 30 | 45000 | 6000 | 240 | 240 | 1 | 4000 | (TDHS 4) Học tại Thần Vũ Trụ |
| 46 | 5 | 3200000 | 25 | 40000 | 7000 | 270 | 270 | 1 | 8000 | (TDHS 5) Học tại Thần Vũ Trụ |
| 47 | 6 | 10000000 | 20 | 35000 | 8000 | 300 | 300 | 1 | 9999 | (TDHS 6) Học tại Thần Vũ Trụ |
| 48 | 7 | 30000000 | 15 | 30000 | 9000 | 330 | 330 | 1 | 9999 | (TDHS 7) Học tại Thần Vũ Trụ |

**[0-9] Kaioken**

| skillId | point | power_require | mana_use | cool_down | damage | dx | dy | max_fight | price | info |
|---|---|---|---|---|---|---|---|---|---|---|
| 63 | 1 | 150000000 | 9000 | 500 | 160 | 32 | 32 | 1 | 9999 | (Kaioken 1) |
| 64 | 2 | 200000000 | 13000 | 500 | 170 | 32 | 32 | 1 | 9999 | (Kaioken 2) |
| 65 | 3 | 250000000 | 15000 | 500 | 180 | 32 | 32 | 1 | 9999 | (Kaioken 3) |
| 66 | 4 | 300000000 | 18000 | 500 | 190 | 32 | 32 | 1 | 9999 | (Kaioken 4) |
| 67 | 5 | 350000000 | 21000 | 500 | 200 | 32 | 32 | 1 | 9999 | (Kaioken 5) |
| 68 | 6 | 400000000 | 24000 | 500 | 210 | 32 | 32 | 1 | 9999 | (Kaioken 6) |
| 69 | 7 | 450000000 | 27000 | 500 | 220 | 32 | 32 | 1 | 9999 | (Kaioken 7) |

**[0-10] Quả cầu kênh khi**

| skillId | point | power_require | mana_use | cool_down | damage | dx | dy | max_fight | price | info |
|---|---|---|---|---|---|---|---|---|---|---|
| 70 | 1 | 500000000 | 50 | 360000 | 500 | 300 | 300 | 1 | 9999 | (Quả cầu kênh khi 1) |
| 71 | 2 | 600000000 | 55 | 350000 | 600 | 400 | 400 | 1 | 9999 | (Quả cầu kênh khi 2) |
| 72 | 3 | 700000000 | 60 | 340000 | 700 | 500 | 500 | 1 | 9999 | (Quả cầu kênh khi 3) |
| 73 | 4 | 800000000 | 65 | 330000 | 800 | 600 | 600 | 1 | 9999 | (Quả cầu kênh khi 4) |
| 74 | 5 | 900000000 | 70 | 320000 | 900 | 700 | 700 | 1 | 9999 | (Quả cầu kênh khi 5) |
| 75 | 6 | 1000000000 | 75 | 310000 | 1000 | 800 | 800 | 1 | 9999 | (Quả cầu kênh khi 6) |
| 76 | 7 | 1100000000 | 80 | 300000 | 1100 | 900 | 900 | 1 | 9999 | (Quả cầu kênh khi 7) |

**[0-20] Dịch chuyển tức thời**

| skillId | point | power_require | mana_use | cool_down | damage | dx | dy | max_fight | price | info |
|---|---|---|---|---|---|---|---|---|---|---|
| 128 | 1 | 10000000 | 5000 | 20000 | 1000 | 5000 | 5000 | 1 | 9999 | Dịch chuyển tức thời |
| 129 | 2 | 25000000 | 7000 | 19000 | 1500 | 5000 | 5000 | 1 | 9999 | Dịch chuyển tức thời |
| 130 | 3 | 50000000 | 10000 | 18000 | 2000 | 5000 | 5000 | 1 | 9999 | Dịch chuyển tức thời |
| 131 | 4 | 125000000 | 15000 | 17000 | 2500 | 5000 | 5000 | 1 | 9999 | Dịch chuyển tức thời |
| 132 | 5 | 625000000 | 20000 | 16000 | 3000 | 5000 | 5000 | 1 | 9999 | Dịch chuyển tức thời |
| 133 | 6 | 3125000000 | 25000 | 15000 | 3500 | 5000 | 5000 | 1 | 9999 | Dịch chuyển tức thời |
| 134 | 7 | 15625000000 | 30000 | 14000 | 4000 | 5000 | 5000 | 1 | 9999 | Dịch chuyển tức thời |

**[0-22] Thôi miên**

| skillId | point | power_require | mana_use | cool_down | damage | dx | dy | max_fight | price | info |
|---|---|---|---|---|---|---|---|---|---|---|
| 142 | 1 | 10000000 | 10000 | 30000 | 5 | 200 | 200 | 1 | 9999 | Thôi Miên |
| 143 | 2 | 25000000 | 10000 | 32000 | 6 | 200 | 200 | 1 | 9999 | Thôi Miên |
| 144 | 3 | 50000000 | 10000 | 34000 | 7 | 200 | 200 | 1 | 9999 | Thôi Miên |
| 145 | 4 | 125000000 | 10000 | 36000 | 8 | 200 | 200 | 1 | 9999 | Thôi Miên |
| 146 | 5 | 625000000 | 10000 | 38000 | 9 | 200 | 200 | 1 | 9999 | Thôi Miên |
| 147 | 6 | 3125000000 | 10000 | 40000 | 10 | 200 | 200 | 1 | 9999 | Thôi Miên |
| 148 | 7 | 15625000000 | 10000 | 42000 | 11 | 200 | 200 | 1 | 9999 | Thôi Miên |

**[0-19] Khiên năng lượng**

| skillId | point | power_require | mana_use | cool_down | damage | dx | dy | max_fight | price | info |
|---|---|---|---|---|---|---|---|---|---|---|
| 121 | 1 | 10000000 | 51 | 75000 | 15 | 0 | 0 | 1 | 9999 | Khiên năng lượng 1 |
| 122 | 2 | 25000000 | 48 | 80000 | 20 | 0 | 0 | 1 | 9999 | Khiên năng lượng 2 |
| 123 | 3 | 50000000 | 45 | 85000 | 25 | 0 | 0 | 1 | 9999 | Khiên năng lượng 3 |
| 124 | 4 | 125000000 | 42 | 90000 | 30 | 0 | 0 | 1 | 9999 | Khiên năng lượng 4 |
| 125 | 5 | 625000000 | 39 | 95000 | 35 | 0 | 0 | 1 | 9999 | Khiên năng lượng 5 |
| 126 | 6 | 3125000000 | 36 | 100000 | 40 | 0 | 0 | 1 | 9999 | Khiên năng lượng 6 |
| 127 | 7 | 15625000000 | 33 | 105000 | 45 | 0 | 0 | 1 | 9999 | Khiên năng lượng 7 |

**[0-24] Super Kamejoko**

| skillId | point | power_require | mana_use | cool_down | damage | dx | dy | max_fight | price | info |
|---|---|---|---|---|---|---|---|---|---|---|
| 156 | 1 | 60000000000 | 80 | 170000 | 550 | 190 | 25 | 1 | 9999 | Chưởng 1 |
| 157 | 2 | 60000000000 | 75 | 160000 | 600 | 200 | 30 | 1 | 9999 | Chưởng 2 |
| 158 | 3 | 60000000000 | 70 | 150000 | 650 | 210 | 35 | 1 | 9999 | Chưởng 3 |
| 159 | 4 | 60000000000 | 65 | 140000 | 700 | 230 | 40 | 1 | 9999 | Chưởng 4 |
| 160 | 5 | 60000000000 | 60 | 130000 | 750 | 250 | 45 | 1 | 9999 | Chưởng 5 |
| 161 | 6 | 60000000000 | 55 | 120000 | 800 | 270 | 50 | 1 | 9999 | Chưởng 6 |
| 162 | 7 | 60000000000 | 50 | 110000 | 850 | 290 | 55 | 1 | 9999 | Chưởng 7 |
| 163 | 8 | 60000000000 | 45 | 100000 | 900 | 310 | 60 | 1 | 9999 | Chưởng 8 |
| 164 | 9 | 60000000000 | 40 | 90000 | 950 | 330 | 65 | 1 | 9999 | Chưởng 9 |
| 165 | 10 | 60000000000 | 35 | 80000 | 1000 | 350 | 70 | 1 | 9999 | Chưởng 10 |

**[1-2] Chiêu đấm Demon**

| skillId | point | power_require | mana_use | cool_down | damage | dx | dy | max_fight | price | info |
|---|---|---|---|---|---|---|---|---|---|---|
| 14 | 1 | 1000 | 1 | 400 | 95 | 24 | 18 | 1 | 0 | (Đấm Demon 1) Học tại Sư Phụ |
| 15 | 2 | 10000 | 2 | 400 | 105 | 26 | 18 | 1 | 10 | (Đấm Demon 2) Học tại Sư Phụ |
| 16 | 3 | 22000 | 4 | 400 | 115 | 28 | 18 | 1 | 50 | (Đấm Demon 3) Học tại Sư Phụ |
| 17 | 4 | 66000 | 8 | 400 | 125 | 30 | 18 | 1 | 100 | (Đấm Demon 4) Học tại Sư Phụ |
| 18 | 5 | 200000 | 16 | 400 | 135 | 32 | 18 | 1 | 1000 | (Đấm Demon 5) Học tại Sư Phụ |
| 19 | 6 | 600000 | 32 | 400 | 145 | 34 | 18 | 1 | 2000 | (Đấm Demon 6) Học tại Sư Phụ |
| 20 | 7 | 1800000 | 70 | 400 | 155 | 36 | 18 | 1 | 4000 | (Đấm Demon 7) Học tại Sư Phụ |

**[1-3] Chiêu Masenko**

| skillId | point | power_require | mana_use | cool_down | damage | dx | dy | max_fight | price | info |
|---|---|---|---|---|---|---|---|---|---|---|
| 21 | 1 | 10000 | 8 | 800 | 100 | 140 | 140 | 1 | 500 | (Masenko 1) Học tại Sư Phụ sau khi làm nhiệm vụ tìm truyện Doremon |
| 22 | 2 | 20000 | 16 | 790 | 110 | 150 | 150 | 1 | 1000 | (Masenko 2) Học tại Sư Phụ |
| 23 | 3 | 60000 | 32 | 780 | 120 | 160 | 160 | 1 | 2000 | (Masenko 3) Học tại Sư Phụ |
| 24 | 4 | 180000 | 64 | 760 | 130 | 170 | 170 | 1 | 4000 | (Masenko 4) Học tại Sư Phụ |
| 25 | 5 | 540000 | 128 | 740 | 140 | 180 | 180 | 1 | 8000 | (Masenko 5) Học tại Sư Phụ |
| 26 | 6 | 1600000 | 256 | 720 | 150 | 190 | 190 | 1 | 9999 | (Masenko 6) Học tại Sư Phụ |
| 27 | 7 | 4800000 | 512 | 700 | 160 | 200 | 200 | 1 | 9999 | (Masenko 7) Học tại Sư Phụ |

**[1-7] Trị thương**

| skillId | point | power_require | mana_use | cool_down | damage | dx | dy | max_fight | price | info |
|---|---|---|---|---|---|---|---|---|---|---|
| 49 | 1 | 60000 | 40 | 30000 | 50 | 100 | 100 | 1 | 500 | (Phục hồi Namek 1) Học tại sư phụ |
| 50 | 2 | 120000 | 35 | 32000 | 55 | 105 | 105 | 1 | 1000 | (Phục hồi Namek 2) Học tại sư phụ |
| 51 | 3 | 360000 | 30 | 34000 | 60 | 110 | 110 | 1 | 2000 | (Phục hồi Namek 3) Học tại sư phụ |
| 52 | 4 | 1000000 | 25 | 38000 | 65 | 115 | 115 | 1 | 4000 | (Phục hồi Namek 4) Học tại sư phụ |
| 53 | 5 | 3200000 | 20 | 40000 | 70 | 120 | 120 | 1 | 8000 | (Phục hồi Namek 5) Học tại sư phụ |
| 54 | 6 | 10000000 | 15 | 42000 | 75 | 125 | 125 | 1 | 9999 | (Phục hồi Namek 6) Học tại sư phụ |
| 55 | 7 | 30000000 | 10 | 44000 | 80 | 130 | 130 | 1 | 9999 | (Phục hồi Namek 7) Học tại sư phụ |

**[1-11] Makankosappo**

| skillId | point | power_require | mana_use | cool_down | damage | dx | dy | max_fight | price | info |
|---|---|---|---|---|---|---|---|---|---|---|
| 77 | 1 | 150000000 | 0 | 360000 | 70 | 20000 | 20000 | 1 | 9999 | (Makankosappo 1) |
| 78 | 2 | 200000000 | 0 | 350000 | 80 | 20000 | 20000 | 1 | 9999 | (Makankosappo 2) |
| 79 | 3 | 250000000 | 0 | 340000 | 90 | 20000 | 20000 | 1 | 9999 | (Makankosappo 3) |
| 80 | 4 | 300000000 | 0 | 330000 | 100 | 20000 | 20000 | 1 | 9999 | (Makankosappo 4) |
| 81 | 5 | 350000000 | 0 | 320000 | 110 | 20000 | 20000 | 1 | 9999 | (Makankosappo 5) |
| 82 | 6 | 400000000 | 0 | 310000 | 120 | 20000 | 20000 | 1 | 9999 | (Makankosappo 6) |
| 83 | 7 | 450000000 | 0 | 300000 | 130 | 20000 | 20000 | 1 | 9999 | (Makankosappo 7) |

**[1-12] Đẻ trứng**

| skillId | point | power_require | mana_use | cool_down | damage | dx | dy | max_fight | price | info |
|---|---|---|---|---|---|---|---|---|---|---|
| 84 | 1 | 500000000 | 20 | 360000 | 50 | 200 | 200 | 1 | 9999 | (Đẻ trứng 1) |
| 85 | 2 | 600000000 | 30 | 390000 | 55 | 200 | 200 | 1 | 9999 | (Đẻ trứng 2) |
| 86 | 3 | 700000000 | 40 | 420000 | 60 | 200 | 200 | 1 | 9999 | (Đẻ trứng 3) |
| 87 | 4 | 800000000 | 50 | 450000 | 65 | 200 | 200 | 1 | 9999 | (Đẻ trứng 4) |
| 88 | 5 | 900000000 | 60 | 480000 | 70 | 200 | 200 | 1 | 9999 | (Đẻ trứng 5) |
| 89 | 6 | 1000000000 | 70 | 510000 | 75 | 200 | 200 | 1 | 9999 | (Đẻ trứng 6) |
| 90 | 7 | 1100000000 | 80 | 540000 | 80 | 200 | 200 | 1 | 9999 | (Đẻ trứng 7) |

**[1-17] Liên hoàn**

| skillId | point | power_require | mana_use | cool_down | damage | dx | dy | max_fight | price | info |
|---|---|---|---|---|---|---|---|---|---|---|
| 107 | 1 | 10000000 | 100 | 350 | 160 | 30 | 30 | 1 | 9999 | (Combo 1) |
| 108 | 2 | 25000000 | 200 | 345 | 165 | 35 | 35 | 1 | 9999 | (Combo 2) |
| 109 | 3 | 50000000 | 300 | 340 | 170 | 40 | 40 | 1 | 9999 | (Combo 3) |
| 110 | 4 | 125000000 | 400 | 335 | 175 | 45 | 45 | 1 | 9999 | (Combo 4) |
| 111 | 5 | 625000000 | 500 | 330 | 180 | 50 | 50 | 1 | 9999 | (Combo 5) |
| 112 | 6 | 3125000000 | 600 | 335 | 185 | 55 | 55 | 1 | 9999 | (Combo 6) |
| 113 | 7 | 15625000000 | 700 | 330 | 190 | 60 | 60 | 1 | 9999 | (Combo 7) |

**[1-18] Biến Sôcôla**

| skillId | point | power_require | mana_use | cool_down | damage | dx | dy | max_fight | price | info |
|---|---|---|---|---|---|---|---|---|---|---|
| 114 | 1 | 10000000 | 22 | 30000 | 15 | 500 | 500 | 1 | 9999 | Biến Sôcôla 1 |
| 115 | 2 | 25000000 | 20 | 29000 | 17 | 500 | 500 | 1 | 9999 | Biến Sôcôla 2 |
| 116 | 3 | 50000000 | 18 | 28000 | 19 | 500 | 500 | 1 | 9999 | Biến Sôcôla 3 |
| 117 | 4 | 125000000 | 16 | 27000 | 21 | 500 | 500 | 1 | 9999 | Biến Sôcôla 4 |
| 118 | 5 | 625000000 | 14 | 26000 | 23 | 500 | 500 | 1 | 9999 | Biến Sôcôla 5 |
| 119 | 6 | 3125000000 | 12 | 25000 | 25 | 500 | 500 | 1 | 9999 | Biến Sôcôla 6 |
| 120 | 7 | 15625000000 | 10 | 24000 | 27 | 500 | 500 | 1 | 9999 | Biến Sôcôla 7 |

**[1-19] Khiên năng lượng**

| skillId | point | power_require | mana_use | cool_down | damage | dx | dy | max_fight | price | info |
|---|---|---|---|---|---|---|---|---|---|---|
| 121 | 1 | 10000000 | 51 | 75000 | 15 | 0 | 0 | 1 | 9999 | Khiên năng lượng 1 |
| 122 | 2 | 25000000 | 48 | 80000 | 20 | 0 | 0 | 1 | 9999 | Khiên năng lượng 2 |
| 123 | 3 | 50000000 | 45 | 85000 | 25 | 0 | 0 | 1 | 9999 | Khiên năng lượng 3 |
| 124 | 4 | 125000000 | 42 | 90000 | 30 | 0 | 0 | 1 | 9999 | Khiên năng lượng 4 |
| 125 | 5 | 625000000 | 39 | 95000 | 35 | 0 | 0 | 1 | 9999 | Khiên năng lượng 5 |
| 126 | 6 | 3125000000 | 36 | 100000 | 40 | 0 | 0 | 1 | 9999 | Khiên năng lượng 6 |
| 127 | 7 | 15625000000 | 33 | 105000 | 45 | 0 | 0 | 1 | 9999 | Khiên năng lượng 7 |

**[1-26] Ma phong ba**

| skillId | point | power_require | mana_use | cool_down | damage | dx | dy | max_fight | price | info |
|---|---|---|---|---|---|---|---|---|---|---|
| 166 | 1 | 60000000000 | 80 | 170000 | 550 | 83 | 83 | 1 | 9999 | Chưởng 1 |
| 167 | 2 | 60000000000 | 75 | 160000 | 600 | 95 | 95 | 1 | 9999 | Chưởng 2 |
| 168 | 3 | 60000000000 | 70 | 150000 | 650 | 107 | 107 | 1 | 9999 | Chưởng 3 |
| 169 | 4 | 60000000000 | 65 | 140000 | 700 | 119 | 119 | 1 | 9999 | Chưởng 4 |
| 170 | 5 | 60000000000 | 60 | 130000 | 750 | 130 | 130 | 1 | 9999 | Chưởng 5 |
| 171 | 6 | 60000000000 | 55 | 120000 | 800 | 142 | 142 | 1 | 9999 | Chưởng 6 |
| 172 | 7 | 60000000000 | 50 | 110000 | 850 | 154 | 154 | 1 | 9999 | Chưởng 7 |
| 173 | 8 | 60000000000 | 45 | 100000 | 900 | 165 | 165 | 1 | 9999 | Chưởng 8 |
| 174 | 9 | 60000000000 | 40 | 90000 | 950 | 177 | 177 | 1 | 9999 | Chưởng 9 |
| 175 | 10 | 60000000000 | 35 | 80000 | 1000 | 188 | 188 | 1 | 9999 | Chưởng 10 |

**[2-4] Chiêu đấm Galick**

| skillId | point | power_require | mana_use | cool_down | damage | dx | dy | max_fight | price | info |
|---|---|---|---|---|---|---|---|---|---|---|
| 28 | 1 | 1000 | 1 | 500 | 110 | 36 | 18 | 1 | 0 | (Đấm Galick 1) Học tại ông nội ngay lúc đầu |
| 29 | 2 | 10000 | 2 | 500 | 120 | 37 | 18 | 1 | 10 | (Đấm Galick 2) Sau khi làm nhiệm vụ tiêu diệt Heo Rừng sẽ học được tại ông nội |
| 30 | 3 | 22000 | 4 | 500 | 130 | 38 | 18 | 1 | 50 | (Đấm Galick 3) Học tại Sư Phụ |
| 31 | 4 | 66000 | 8 | 500 | 140 | 39 | 18 | 1 | 100 | (Đấm Galick 4) Học tại Sư Phụ |
| 32 | 5 | 200000 | 16 | 500 | 150 | 40 | 18 | 1 | 1000 | (Đấm Galick 5) Học tại Sư Phụ |
| 33 | 6 | 600000 | 32 | 500 | 160 | 41 | 18 | 1 | 2000 | (Đấm Galick 6) Học tại Sư Phụ |
| 34 | 7 | 1800000 | 70 | 500 | 170 | 42 | 18 | 1 | 4000 | (Đấm Galick 7) Học tại Sư Phụ |

**[2-5] Chiêu Antomic**

| skillId | point | power_require | mana_use | cool_down | damage | dx | dy | max_fight | price | info |
|---|---|---|---|---|---|---|---|---|---|---|
| 35 | 1 | 10000 | 18 | 1000 | 110 | 150 | 150 | 1 | 500 | (Antomic 1) Học tại Sư Phụ sau khi làm nhiệm vụ tìm truyện Doremon |
| 36 | 2 | 20000 | 34 | 1200 | 140 | 160 | 160 | 1 | 1000 | (Antomic 2) Học tại Sư Phụ |
| 37 | 3 | 60000 | 68 | 1400 | 170 | 170 | 170 | 1 | 2000 | (Antomic 3) Học tại Sư Phụ |
| 38 | 4 | 180000 | 136 | 1600 | 200 | 180 | 180 | 1 | 4000 | (Antomic 4) Học tại Sư Phụ |
| 39 | 5 | 540000 | 258 | 1800 | 230 | 190 | 190 | 1 | 8000 | (Antomic 5) Học tại Sư Phụ |
| 40 | 6 | 1600000 | 514 | 2000 | 260 | 200 | 200 | 1 | 9999 | (Antomic 6) Học tại Sư Phụ |
| 41 | 7 | 4800000 | 1026 | 2200 | 290 | 210 | 210 | 1 | 9999 | (Antomic 7) Học tại Sư Phụ |

**[2-8] Tái tạo năng lượng**

| skillId | point | power_require | mana_use | cool_down | damage | dx | dy | max_fight | price | info |
|---|---|---|---|---|---|---|---|---|---|---|
| 56 | 1 | 60000 | 0 | 55000 | 4 | 0 | 0 | 1 | 500 | (Tái tạo Xayda 1) Học tại sư phụ |
| 57 | 2 | 120000 | 0 | 50000 | 5 | 0 | 0 | 1 | 1000 | (Tái tạo Xayda 2) Học tại sư phụ |
| 58 | 3 | 360000 | 0 | 45000 | 6 | 0 | 0 | 1 | 2000 | (Tái tạo Xayda 3) Học tại sư phụ |
| 59 | 4 | 1000000 | 0 | 40000 | 7 | 0 | 0 | 1 | 4000 | (Tái tạo Xayda 4) Học tại sư phụ |
| 60 | 5 | 3200000 | 0 | 35000 | 8 | 0 | 0 | 1 | 8000 | (Tái tạo Xayda 5) Học tại sư phụ |
| 61 | 6 | 10000000 | 0 | 30000 | 9 | 0 | 0 | 1 | 9999 | (Tái tạo Xayda 6) Học tại sư phụ |
| 62 | 7 | 30000000 | 0 | 25000 | 10 | 0 | 0 | 1 | 9999 | (Tái tạo Xayda 7) Học tại sư phụ |

**[2-13] Biến hình**

| skillId | point | power_require | mana_use | cool_down | damage | dx | dy | max_fight | price | info |
|---|---|---|---|---|---|---|---|---|---|---|
| 91 | 1 | 250000000 | 10 | 300000 | 100 | 200 | 200 | 1 | 9999 | (Biến hình 1) |
| 92 | 2 | 350000000 | 10 | 310000 | 100 | 200 | 200 | 1 | 9999 | (Biến hình 2) |
| 93 | 3 | 450000000 | 10 | 320000 | 100 | 200 | 200 | 1 | 9999 | (Biến hình 3) |
| 94 | 4 | 550000000 | 10 | 330000 | 100 | 200 | 200 | 1 | 9999 | (Biến hình 4) |
| 95 | 5 | 650000000 | 10 | 340000 | 100 | 200 | 200 | 1 | 9999 | (Biến hình 5) |
| 96 | 6 | 750000000 | 10 | 350000 | 100 | 200 | 200 | 1 | 9999 | (Biến hình 6) |
| 97 | 7 | 850000000 | 10 | 360000 | 100 | 200 | 200 | 1 | 9999 | (Biến hình 7) |

**[2-14] Tự phát nổ**

| skillId | point | power_require | mana_use | cool_down | damage | dx | dy | max_fight | price | info |
|---|---|---|---|---|---|---|---|---|---|---|
| 98 | 1 | 250000000 | 50 | 120000 | 100 | 200 | 200 | 1 | 9999 | (Tự phát nổ 1) |
| 99 | 2 | 300000000 | 50 | 120000 | 105 | 300 | 300 | 1 | 9999 | (Tự phát nổ 2) |
| 100 | 3 | 350000000 | 50 | 120000 | 110 | 400 | 400 | 1 | 9999 | (Tự phát nổ 3) |
| 101 | 4 | 400000000 | 50 | 120000 | 115 | 500 | 500 | 1 | 9999 | (Tự phát nổ 4) |
| 102 | 5 | 450000000 | 50 | 120000 | 120 | 600 | 600 | 1 | 9999 | (Tự phát nổ 5) |
| 103 | 6 | 500000000 | 50 | 120000 | 125 | 700 | 700 | 1 | 9999 | (Tự phát nổ 6) |
| 104 | 7 | 550000000 | 50 | 120000 | 130 | 900 | 900 | 1 | 9999 | (Tự phát nổ 7) |

**[2-21] Huýt sáo**

| skillId | point | power_require | mana_use | cool_down | damage | dx | dy | max_fight | price | info |
|---|---|---|---|---|---|---|---|---|---|---|
| 135 | 1 | 10000000 | 50 | 210000 | 40 | 500 | 500 | 1 | 9999 | Huýt sáo |
| 136 | 2 | 25000000 | 45 | 205000 | 50 | 500 | 500 | 1 | 9999 | Huýt sáo |
| 137 | 3 | 50000000 | 40 | 200000 | 60 | 500 | 500 | 1 | 9999 | Huýt sáo |
| 138 | 4 | 125000000 | 35 | 195000 | 70 | 500 | 500 | 1 | 9999 | Huýt sáo |
| 139 | 5 | 625000000 | 30 | 190000 | 80 | 500 | 500 | 1 | 9999 | Huýt sáo |
| 140 | 6 | 3125000000 | 25 | 185000 | 90 | 500 | 500 | 1 | 9999 | Huýt sáo |
| 141 | 7 | 15625000000 | 20 | 180000 | 100 | 500 | 500 | 1 | 9999 | Huýt sáo |

**[2-23] Trói**

| skillId | point | power_require | mana_use | cool_down | damage | dx | dy | max_fight | price | info |
|---|---|---|---|---|---|---|---|---|---|---|
| 149 | 1 | 10000000 | 5000 | 15000 | 5 | 150 | 150 | 1 | 9999 | Trói |
| 150 | 2 | 25000000 | 10000 | 20000 | 10 | 150 | 150 | 1 | 9999 | Trói |
| 151 | 3 | 50000000 | 15000 | 25000 | 15 | 150 | 150 | 1 | 9999 | Trói |
| 152 | 4 | 125000000 | 20000 | 30000 | 20 | 150 | 150 | 1 | 9999 | Trói |
| 153 | 5 | 625000000 | 25000 | 35000 | 25 | 150 | 150 | 1 | 9999 | Trói |
| 154 | 6 | 3125000000 | 30000 | 40000 | 30 | 150 | 150 | 1 | 9999 | Trói |
| 155 | 7 | 15625000000 | 32000 | 45000 | 35 | 150 | 150 | 1 | 9999 | Trói |

**[2-19] Khiên năng lượng**

| skillId | point | power_require | mana_use | cool_down | damage | dx | dy | max_fight | price | info |
|---|---|---|---|---|---|---|---|---|---|---|
| 121 | 1 | 10000000 | 51 | 75000 | 15 | 0 | 0 | 1 | 9999 | Khiên năng lượng 1 |
| 122 | 2 | 25000000 | 48 | 80000 | 20 | 0 | 0 | 1 | 9999 | Khiên năng lượng 2 |
| 123 | 3 | 50000000 | 45 | 85000 | 25 | 0 | 0 | 1 | 9999 | Khiên năng lượng 3 |
| 124 | 4 | 125000000 | 42 | 90000 | 30 | 0 | 0 | 1 | 9999 | Khiên năng lượng 4 |
| 125 | 5 | 625000000 | 39 | 95000 | 35 | 0 | 0 | 1 | 9999 | Khiên năng lượng 5 |
| 126 | 6 | 3125000000 | 36 | 100000 | 40 | 0 | 0 | 1 | 9999 | Khiên năng lượng 6 |
| 127 | 7 | 15625000000 | 33 | 105000 | 45 | 0 | 0 | 1 | 9999 | Khiên năng lượng 7 |

**[2-25] Cađíc liên hoàn chưởng**

| skillId | point | power_require | mana_use | cool_down | damage | dx | dy | max_fight | price | info |
|---|---|---|---|---|---|---|---|---|---|---|
| 176 | 1 | 60000000000 | 80 | 170000 | 550 | 120 | 120 | 1 | 9999 | Chưởng 1 |
| 177 | 2 | 60000000000 | 75 | 160000 | 600 | 130 | 130 | 1 | 9999 | Chưởng 2 |
| 178 | 3 | 60000000000 | 70 | 150000 | 650 | 140 | 140 | 1 | 9999 | Chưởng 3 |
| 179 | 4 | 60000000000 | 65 | 140000 | 700 | 150 | 150 | 1 | 9999 | Chưởng 4 |
| 180 | 5 | 60000000000 | 60 | 130000 | 750 | 160 | 160 | 1 | 9999 | Chưởng 5 |
| 181 | 6 | 60000000000 | 55 | 120000 | 800 | 170 | 170 | 1 | 9999 | Chưởng 6 |
| 182 | 7 | 60000000000 | 50 | 110000 | 850 | 180 | 180 | 1 | 9999 | Chưởng 7 |
| 183 | 8 | 60000000000 | 45 | 100000 | 900 | 190 | 190 | 1 | 9999 | Chưởng 8 |
| 184 | 9 | 60000000000 | 40 | 90000 | 950 | 200 | 200 | 1 | 9999 | Chưởng 9 |
| 185 | 10 | 60000000000 | 35 | 80000 | 1000 | 210 | 210 | 1 | 9999 | Chưởng 10 |

## 5. map_template – bản đồ

Tổng: **169** map (id 0 → 185, có khoảng trống id). Nạp tại `Manager.loadDatabase()` dòng 794–870. `mobs` = mảng `[mob_template_id, level, hp, x, y]`; `npcs` = mảng `[npc_id, x, y]`; `waypoints` = mảng `[tên, minX, minY, maxX, maxY, isEnter, isOffline, goMap, goX, goY]`.

| id | Tên | Hành tinh | type | zones | max_player | Số mob | Quái (template, level) | NPC |
|---|---|---|---|---|---|---|---|---|
| 0 | Làng Aru | Trái Đất | 0 (MAP_NORMAL) | 10 | 15 | 4 | Mộc nhân(lv1) | Bunma, Mr Popo, Tori-Bot, Xe nước mía |
| 1 | Đồi hoa cúc | Trái Đất | 0 (MAP_NORMAL) | 10 | 15 | 4 | Khủng long(lv2) | Khu vực |
| 2 | Thung lũng tre | Trái Đất | 0 (MAP_NORMAL) | 10 | 15 | 4 | Khủng long(lv2), Khủng long mẹ(lv3) | Khu vực |
| 3 | Rừng nấm | Trái Đất | 0 (MAP_NORMAL) | 10 | 15 | 8 | Khủng long(lv2), Khủng long mẹ(lv3), Thằn lằn bay(lv4) | Khu vực |
| 4 | Rừng xương | Trái Đất | 0 (MAP_NORMAL) | 10 | 15 | 7 | Khủng long mẹ(lv3), Thằn lằn bay(lv4), Thằn lằn mẹ(lv5) | Khu vực |
| 5 | Đảo Kamê | Trái Đất | 0 (MAP_NORMAL) | 10 | 15 | 3 | Ốc mượn hồn(lv1) | Bà Hạt Mít, Chi Chi, Quy Lão Kame, Santa |
| 6 | Đông Karin | Trái Đất | 0 (MAP_NORMAL) | 10 | 15 | 4 | Heo rừng mẹ(lv8), Tambourine(lv8) | Khu vực |
| 7 | Làng Mori | Namếc | 0 (MAP_NORMAL) | 10 | 15 | 4 | Mộc nhân(lv1) | Dende, Khu vực, Tori-Bot, Xe nước mía |
| 8 | Đồi nấm tím | Namếc | 0 (MAP_NORMAL) | 10 | 15 | 4 | Lợn lòi(lv2) | Khu vực |
| 9 | Thị trấn Moori | Namếc | 0 (MAP_NORMAL) | 10 | 15 | 5 | Lợn lòi(lv2), Lợn lòi mẹ(lv3) | Khu vực |
| 10 | Thung lũng Namếc | Namếc | 0 (MAP_NORMAL) | 10 | 15 | 4 | Heo xanh mẹ(lv8), Drum(lv8) | Khu vực |
| 11 | Thung lũng Maima | Namếc | 0 (MAP_NORMAL) | 10 | 15 | 8 | Lợn lòi(lv2), Lợn lòi mẹ(lv3), Phi long(lv4) | Khu vực |
| 12 | Vực maima | Namếc | 0 (MAP_NORMAL) | 10 | 15 | 7 | Lợn lòi mẹ(lv3), Phi long(lv4), Phi long mẹ(lv5) | Khu vực |
| 13 | Đảo Guru | Namếc | 0 (MAP_NORMAL) | 10 | 15 | 3 | Ốc sên(lv7) | Santa, Trưởng lão Guru, Trọng tài |
| 14 | Làng Kakarot | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 4 | Mộc nhân(lv1) | Appule, Khu vực, Tori-Bot, Xe nước mía |
| 15 | Đồi hoang | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 5 | Quỷ đất(lv2) | Khu vực |
| 16 | Làng Plant | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 4 | Quỷ đất(lv2), Quỷ đất mẹ(lv3) | Khu vực |
| 17 | Rừng nguyên sinh | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 8 | Quỷ đất(lv2), Quỷ đất mẹ(lv3), Quỷ bay(lv4) | Khu vực |
| 18 | Rừng thông Xayda | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 7 | Quỷ đất mẹ(lv3), Quỷ bay(lv4), Quỷ bay mẹ(lv5) | Khu vực |
| 19 | Thành phố Vegeta | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 4 | Alien(lv8), Akkuman(lv8) | Cui, Tapion |
| 20 | Vách núi đen | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 3 | Heo Xayda mẹ(lv7) | Khu vực, Santa, Vua Vegeta |
| 21 | Nhà Gôhan | Trái Đất | 1 (MAP_OFFLINE) | 10 | 15 | 0 |  | Bò Mộng, Dưa hấu, Quả trứng, Rương đồ, Ông Gôhan, Đậu thần |
| 22 | Nhà Moori | Namếc | 1 (MAP_OFFLINE) | 10 | 15 | 0 |  | Bò Mộng, Dưa hấu, Quả trứng, Rương đồ, Ông Moori, Đậu thần |
| 23 | Nhà Broly | Xayda | 1 (MAP_OFFLINE) | 10 | 15 | 0 |  | Bò Mộng, Dưa hấu, Quả trứng, Rương đồ, Ông Paragus, Đậu thần |
| 24 | Trạm tàu vũ trụ | Trái Đất | 0 (MAP_NORMAL) | 10 | 15 | 0 |  | Dr. Brief, Jaco, Khu vực, Rồng Omega, Uron |
| 25 | Trạm tàu vũ trụ | Namếc | 0 (MAP_NORMAL) | 10 | 15 | 0 |  | Cargo, Khu vực, Rồng Omega, Uron |
| 26 | Trạm tàu vũ trụ | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 0 |  | Cui, Khu vực, Rồng Omega, Uron |
| 27 | Rừng Bamboo | Trái Đất | 0 (MAP_NORMAL) | 10 | 15 | 6 | Thằn lằn mẹ(lv5), Heo rừng(lv6) | Ca Lích, Lính canh |
| 28 | Rừng dương xỉ | Trái Đất | 0 (MAP_NORMAL) | 10 | 15 | 10 | Thằn lằn mẹ(lv5), Heo rừng(lv6) | Khu vực |
| 29 | Nam Kamê | Trái Đất | 0 (MAP_NORMAL) | 10 | 15 | 7 | Ốc mượn hồn(lv7), Không tặc(lv7) | Khu vực |
| 30 | Đảo Bulông | Trái Đất | 0 (MAP_NORMAL) | 10 | 15 | 6 | Bulon(lv8), Không tặc(lv7) | Khu vực |
| 31 | Núi hoa vàng | Namếc | 0 (MAP_NORMAL) | 10 | 15 | 6 | Phi long mẹ(lv5), Heo da xanh(lv6) | Khu vực |
| 32 | Núi hoa tím | Namếc | 0 (MAP_NORMAL) | 10 | 15 | 9 | Phi long mẹ(lv5), Heo da xanh(lv6) | Khu vực |
| 33 | Nam Guru | Namếc | 0 (MAP_NORMAL) | 10 | 15 | 7 | Ốc sên(lv7), Quỷ đầu to(lv7) | Khu vực |
| 34 | Đông Nam Guru | Namếc | 0 (MAP_NORMAL) | 10 | 15 | 6 | Ukulele(lv8), Quỷ đầu to(lv7) | Khu vực |
| 35 | Rừng cọ | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 6 | Quỷ bay mẹ(lv5), Heo Xayda(lv6) | Khu vực |
| 36 | Rừng đá | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 9 | Quỷ bay mẹ(lv5), Heo Xayda(lv6) | Khu vực |
| 37 | Thung lũng đen | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 7 | Heo Xayda mẹ(lv7), Quỷ địa ngục(lv7) | Khu vực |
| 38 | Bờ vực đen | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 6 | Quỷ mập(lv8), Quỷ địa ngục(lv7) | Khu vực |
| 39 | Vách núi Aru | Trái Đất | 1 (MAP_OFFLINE) | 10 | 15 | 0 |  |  |
| 40 | Vách núi Moori | Namếc | 1 (MAP_OFFLINE) | 10 | 15 | 0 |  |  |
| 41 | Vực Plant | Xayda | 1 (MAP_OFFLINE) | 10 | 15 | 0 |  |  |
| 42 | Vách núi Aru | Trái Đất | 0 (MAP_NORMAL) | 10 | 15 | 1 | Máy đo sức mạnh(lv1) | Bà Hạt Mít, Ghi danh |
| 43 | Vách núi Moori | Namếc | 0 (MAP_NORMAL) | 10 | 15 | 1 | Máy đo sức mạnh(lv1) | Bà Hạt Mít, Ghi danh, Quốc Vương |
| 44 | Vách núi Kakarot | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 1 | Máy đo sức mạnh(lv1) | Bà Hạt Mít, Ghi danh |
| 45 | Thần điện | Trái Đất | 1 (MAP_OFFLINE) | 10 | 15 | 0 |  | Thượng Đế |
| 46 | Tháp Karin | Trái Đất | 1 (MAP_OFFLINE) | 10 | 15 | 0 |  | Thần mèo Karin |
| 47 | Rừng Karin | Trái Đất | 0 (MAP_NORMAL) | 10 | 15 | 0 |  | Bò Mộng |
| 48 | Hành tinh Kaio | Trái Đất | 1 (MAP_OFFLINE) | 10 | 15 | 0 |  | Bill, Thần Vũ Trụ, Whis |
| 49 | Phòng tập thời gian | Trái Đất | 1 (MAP_OFFLINE) | 10 | 15 | 0 |  |  |
| 50 | Thánh địa Kaio | Trái Đất | 1 (MAP_OFFLINE) | 10 | 15 | 0 |  | Kibit, Tổ Sư Kaio, Ôsin |
| 51 | Đấu trường | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 0 |  |  |
| 52 | Đại hội võ thuật | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 2 | Mộc nhân(lv1) | Ghi danh, Kibit, Ôsin |
| 53 | Tường thành 1 | Xayda | 2 (MAP_DOANH_TRAI) | 10 | 15 | 7 | Lính độc nhãn(lv8) |  |
| 54 | Tầng 3 | Trái Đất | 2 (MAP_DOANH_TRAI) | 10 | 15 | 0 |  |  |
| 55 | Tầng 1 | Trái Đất | 2 (MAP_DOANH_TRAI) | 10 | 15 | 4 | Lính độc nhãn(lv8) |  |
| 56 | Tầng 2 | Trái Đất | 2 (MAP_DOANH_TRAI) | 10 | 15 | 5 | Lính độc nhãn(lv8), Lính độc nhãn(lv8), Robot thép(lv8) |  |
| 57 | Tầng 4 | Trái Đất | 2 (MAP_DOANH_TRAI) | 10 | 15 | 5 | Robot thép(lv8) | Độc Nhãn |
| 58 | Tường thành 2 | Trái Đất | 2 (MAP_DOANH_TRAI) | 10 | 15 | 12 | Không tặc(lv7), Lính độc nhãn(lv8), Lính độc nhãn(lv8) |  |
| 59 | Tường thành 3 | Trái Đất | 2 (MAP_DOANH_TRAI) | 10 | 15 | 15 | Bulon(lv8), Không tặc(lv7), Lính độc nhãn(lv8) |  |
| 60 | Trại độc nhãn 1 | Trái Đất | 2 (MAP_DOANH_TRAI) | 10 | 15 | 8 | Lính độc nhãn(lv8), Sói xám(lv8) |  |
| 61 | Trại độc nhãn 2 | Trái Đất | 2 (MAP_DOANH_TRAI) | 10 | 15 | 15 | Sói xám(lv8), Robot bay(lv8) |  |
| 62 | Trại độc nhãn 3 | Trái Đất | 2 (MAP_DOANH_TRAI) | 10 | 15 | 24 | Lính độc nhãn(lv8), Lính độc nhãn(lv8), Sói xám(lv8), Robot bay(lv8) |  |
| 63 | Trại lính Fide | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 16 | Quỷ đầu vàng(lv10), Quỷ da tím(lv10), Dơi da xanh(lv11) |  |
| 64 | Núi dây leo | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 11 | Thằn lằn xanh(lv10), Quỷ đầu nhọn(lv10) |  |
| 65 | Núi cây quỷ | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 9 | Thằn lằn xanh(lv10), Quỷ đầu nhọn(lv10), Quỷ đầu vàng(lv10) |  |
| 66 | Trại qủy già | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 8 | Quỷ da tím(lv10), Quỷ già(lv11) |  |
| 67 | Vực chết | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 12 | Quỷ già(lv11), Dơi da xanh(lv11) |  |
| 68 | Thung lũng Nappa | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 9 | Tambourine(lv8), Nappa(lv9) | Cui |
| 69 | Vực cấm | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 8 | Tambourine(lv8), Nappa(lv9), Soldier(lv9) |  |
| 70 | Núi Appule | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 12 | Nappa(lv9), Soldier(lv9), Appule(lv9) |  |
| 71 | Căn cứ Raspberry | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 11 | Appule(lv9), Raspberry(lv9) |  |
| 72 | Thung lũng Raspberry | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 10 | Appule(lv9), Raspberry(lv9), Thằn lằn xanh(lv10) |  |
| 73 | Thung lũng chết | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 9 | Cá sấu(lv11), Dơi da xanh(lv11) |  |
| 74 | Đồi cây Fide | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 8 | Dơi da xanh(lv11), Lính đầu trọc(lv11), Lính tai dài(lv12) |  |
| 75 | Khe núi tử thần | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 9 | Lính đầu trọc(lv11), Lính tai dài(lv12), Lính vũ trụ(lv12) |  |
| 76 | Núi đá | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 8 | Quỷ chim(lv12), Lính tai dài(lv12), Lính vũ trụ(lv12) |  |
| 77 | Rừng đá | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 10 | Quỷ chim(lv12), Lính vũ trụ(lv12) |  |
| 78 | Lãnh địa Fize | Trái Đất | 0 (MAP_NORMAL) | 10 | 15 | 4 | Mộc nhân(lv1) | Bunma, Khu vực, Thiên Sứ Whis, Thượng Đế, Thần Vũ Trụ, Đường Tăng |
| 79 | Núi khỉ đỏ | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 8 | Khỉ lông đỏ(lv15) |  |
| 80 | Núi khỉ vàng | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 7 | Khỉ lông vàng(lv16) | Goku SSJ |
| 81 | Hang quỷ chim | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 9 | Quỷ chim(lv12), Lính vũ trụ(lv12), Khỉ lông đen(lv13) |  |
| 82 | Núi khỉ đen | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 12 | Quỷ chim(lv12), Khỉ lông đen(lv13), Khỉ giáp sắt(lv14) |  |
| 83 | Hang khỉ đen | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 10 | Khỉ giáp sắt(lv14), Khỉ lông đỏ(lv15) |  |
| 84 | Siêu Thị | Trái Đất | 0 (MAP_NORMAL) | 10 | 15 | 0 |  | Appule, Bunma, Bà Hạt Mít, Bò Mộng, Cửa hàng ký gửi, Dende, Dr. Brief, Uron |
| 85 | Hành tinh M-2 | Xayda | 3 (MAP_BLACK_BALL_WAR) | 10 | 15 | 4 | Quỷ chim(lv12), Khỉ lông vàng(lv16) | Rồng 1 sao |
| 86 | Hành tinh Polaris | Xayda | 3 (MAP_BLACK_BALL_WAR) | 10 | 15 | 4 | Quỷ chim(lv12) | Rồng 2 sao |
| 87 | Hành tinh Cretaceous | Xayda | 3 (MAP_BLACK_BALL_WAR) | 10 | 15 | 4 | Quỷ chim(lv12), Khỉ lông đỏ(lv15), Khỉ lông vàng(lv16) | Rồng 3 sao |
| 88 | Hành tinh Monmaasu | Xayda | 3 (MAP_BLACK_BALL_WAR) | 10 | 15 | 6 | Quỷ chim(lv12), Khỉ lông đỏ(lv15) | Rồng 4 sao |
| 89 | Hành tinh Rudeeze | Xayda | 3 (MAP_BLACK_BALL_WAR) | 10 | 15 | 2 | Khỉ lông vàng(lv16) | Rồng 5 sao |
| 90 | Hành tinh Gelbo | Xayda | 3 (MAP_BLACK_BALL_WAR) | 10 | 15 | 4 | Quỷ chim(lv12), Khỉ lông vàng(lv16) | Rồng 6 sao |
| 91 | Hành tinh Tigere | Xayda | 3 (MAP_BLACK_BALL_WAR) | 10 | 15 | 4 | Khỉ lông đỏ(lv15), Khỉ lông vàng(lv16) | Rồng 7 sao |
| 92 | Thành phố phía đông | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 6 | Xên con cấp 1(lv11) |  |
| 93 | Thành phố phía nam | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 7 | Xên con cấp 2(lv12) |  |
| 94 | Đảo Balê | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 7 | Xên con cấp 3(lv13) |  |
| 95 | 95 | Namếc | 1 (MAP_OFFLINE) | 10 | 15 | 0 |  |  |
| 96 | Cao nguyên | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 4 | Xên con cấp  4(lv14) |  |
| 97 | Thành phố phía bắc | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 6 | Xên con cấp  5(lv15) |  |
| 98 | Ngọn núi phía bắc | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 6 | Xên con cấp  6(lv16) |  |
| 99 | Thung lũng phía bắc | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 8 | Xên con cấp  7(lv17) |  |
| 100 | Thị trấn Ginder | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 9 | Xên con cấp  8(lv18) |  |
| 101 | 101 | Namếc | 1 (MAP_OFFLINE) | 10 | 15 | 0 |  |  |
| 102 | Nhà Bunma | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 0 |  | Bunma, Ca Lích, Rương Sưu Tầm |
| 103 | Võ đài Xên bọ hung | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 0 |  |  |
| 104 | Sân sau siêu thị | Trái Đất | 0 (MAP_NORMAL) | 10 | 15 | 0 |  |  |
| 105 | Cánh đồng tuyết | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 8 | Tai tím(lv19) |  |
| 106 | Rừng tuyết | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 7 | Tai tím(lv19), Abo(lv20) |  |
| 107 | Núi tuyết | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 10 | Abo(lv20) |  |
| 108 | Dòng sông băng | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 9 | Abo(lv20), Kado(lv21) |  |
| 109 | Rừng băng | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 11 | Kado(lv21), Da xanh(lv22) |  |
| 110 | Hang băng | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 11 | Kado(lv21), Da xanh(lv22) |  |
| 111 | Đông Nam Karin | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 0 |  |  |
| 112 | Võ đài Hạt Mít | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 0 |  | Bà Hạt Mít |
| 113 | Đại hội võ thuật | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 0 |  | Trọng tài |
| 114 | Cổng phi thuyền | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 0 |  | Babiđây, Kibit, Ôsin |
| 115 | Phòng chờ | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 0 |  | Babiđây, Ôsin |
| 116 | Thánh địa Kaio | Trái Đất | 1 (MAP_OFFLINE) | 10 | 15 | 0 |  | Kibit, Tổ Sư Kaio, Ôsin |
| 117 | Cửa Ải 1 | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 0 |  | Babiđây, Ôsin |
| 118 | Cửa Ải 2 | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 0 |  | Babiđây, Ôsin |
| 119 | Cửa Ải 3 | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 0 |  | Babiđây, Ôsin |
| 120 | Phòng chỉ huy | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 0 |  | Babiđây, Ôsin |
| 121 | 121 | Namếc | 1 (MAP_OFFLINE) | 10 | 15 | 0 |  |  |
| 122 | Ngũ Hành Sơn | Xayda | 1 (MAP_OFFLINE) | 10 | 15 | 6 | Quỷ chim(lv20), Khỉ lông vàng(lv20) | Khu vực, Ngộ Không, Đường Tăng |
| 123 | Ngũ Hành Sơn | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 4 | Khỉ lông đỏ(lv20) | Khu vực, Đường Tăng |
| 124 | Ngũ Hành Sơn | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 4 | Khỉ lông vàng(lv20) | Khu vực |
| 125 | 125 | Namếc | 1 (MAP_OFFLINE) | 10 | 15 | 0 |  |  |
| 126 | Thành phố Santa | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 7 | Quỷ chim(lv12), Hirudegarn(lv0) | Tapion |
| 127 | Cổng phi thuyền | Xayda | 9 (MAP_MABU_14H) | 10 | 15 | 0 |  | Ôsin |
| 128 | Bụng Mabư | Trái Đất | 9 (MAP_MABU_14H) | 10 | 15 | 0 |  |  |
| 129 | Đại hội võ thuật | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 0 |  | Ghi danh |
| 130 | 130 | Namếc | 1 (MAP_OFFLINE) | 10 | 15 | 0 |  |  |
| 131 | Hành Tinh Yardart | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 0 |  | Goku SSJ |
| 132 | Hành Tinh Yardart 2 | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 0 |  |  |
| 133 | Hành Tinh Yardart 3 | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 0 |  | Goku SSJ |
| 134 | 134 | Namếc | 1 (MAP_OFFLINE) | 10 | 15 | 0 |  |  |
| 135 | Động hải tặc | Trái Đất | 4 (MAP_BAN_DO_KHO_BAU) | 10 | 15 | 13 | Lính độc nhãn(lv8), Lính độc nhãn(lv8), Sói xám(lv8) |  |
| 136 | Hang Bạch Tuộc | Trái Đất | 4 (MAP_BAN_DO_KHO_BAU) | 10 | 15 | 8 | Sói xám(lv8), Robot bay(lv8), Vua Bạch Tuộc(lv0) |  |
| 137 | Động kho báu | Trái Đất | 4 (MAP_BAN_DO_KHO_BAU) | 10 | 15 | 8 | Robot bay(lv8), Robot thép(lv8) |  |
| 138 | Cảng hải tặc | Trái Đất | 4 (MAP_BAN_DO_KHO_BAU) | 10 | 15 | 5 | Robot bay(lv8), Rôbốt bảo vệ(lv0) |  |
| 139 | Hành tinh Potaufeu | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 0 |  | Jaco |
| 140 | Hang động Potaufeu | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 0 |  | Potage |
| 141 | Con đường rắn độc | Trái Đất | 6 (MAP_CON_DUONG_RAN_DOC) | 10 | 15 | 7 | Quỷ mập(lv8), Quỷ địa ngục(lv8) | Thượng Đế |
| 142 | Con đường rắn độc | Trái Đất | 6 (MAP_CON_DUONG_RAN_DOC) | 10 | 15 | 7 | Tambourine(lv8), Drum(lv8) |  |
| 143 | Con đường rắn độc | Trái Đất | 6 (MAP_CON_DUONG_RAN_DOC) | 10 | 15 | 7 | Dơi da xanh(lv11), Quỷ chim(lv12) |  |
| 144 | Hoang mạc | Xayda | 6 (MAP_CON_DUONG_RAN_DOC) | 10 | 15 | 0 |  |  |
| 145 | Võ Đài Siêu Cấp | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 0 |  | Thiên Sứ Whis |
| 146 | Tây Karin | Trái Đất | 8 (MAP_TAY_KARIN) | 10 | 15 | 3 | Quỷ đất mẹ(lv3), Tambourine(lv8), Drum(lv8) |  |
| 147 | Sa mạc | Trái Đất | 7 (MAP_KHI_GAS_HUY_DIET) | 10 | 15 | 17 | Kawazu(lv9), Arbee(lv9), Cỗ máy hủy diệt(lv10) |  |
| 148 | Lâu đài Lychee | Xayda | 7 (MAP_KHI_GAS_HUY_DIET) | 10 | 15 | 11 | Kawazu(lv9), Kinkarn(lv9), Arbee(lv9) |  |
| 149 | Thành phố Santa | Xayda | 7 (MAP_KHI_GAS_HUY_DIET) | 10 | 15 | 14 | Kinkarn(lv9), Arbee(lv9), Cỗ máy hủy diệt(lv10) |  |
| 151 | Hành tinh bóng tối | Xayda | 7 (MAP_KHI_GAS_HUY_DIET) | 10 | 15 | 9 | Kawazu(lv9), Kinkarn(lv9), Arbee(lv9), Cỗ máy hủy diệt(lv10) |  |
| 152 | Vùng đất băng giá | Xayda | 7 (MAP_KHI_GAS_HUY_DIET) | 10 | 15 | 30 | Kawazu(lv9), Kinkarn(lv9), Arbee(lv9), Cỗ máy hủy diệt(lv10) |  |
| 153 | Lãnh địa Bang Hội | Trái Đất | 0 (MAP_NORMAL) | 10 | 15 | 0 |  | Giu-ma Đầu Bò |
| 154 | Hành tinh Bill | Trái Đất | 0 (MAP_NORMAL) | 10 | 15 | 0 |  | Bill, Whis, Ôsin |
| 155 | Hành tinh ngục tù | Trái Đất | 0 (MAP_NORMAL) | 10 | 15 | 12 | Khỉ lông xanh(lv1), Taburine Đỏ(lv1) | Ôsin |
| 156 | Tây thánh địa | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 12 | Nappa(lv20), Dơi da xanh(lv20) |  |
| 157 | Đông thánh Địa | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 10 | Soldier(lv21), Thằn lằn xanh(lv21), Quỷ chim(lv21) |  |
| 158 | Bắc thánh địa | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 11 | Tai tím(lv21), Abo(lv21), Da xanh(lv21) |  |
| 159 | Nam thánh Địa | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 14 | Kado(lv22), Da xanh(lv22) |  |
| 160 | Khu hang động | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 18 | Cabira(lv21), Tobi(lv21) | Bardock, Berry |
| 161 | Bìa rừng nguyên thủy | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 17 | Cabira(lv21), Tobi(lv21) |  |
| 162 | Rừng nguyên thủy | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 19 | Cabira(lv21), Tobi(lv21) |  |
| 163 | Làng Plant nguyên thủy | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 10 | Cabira(lv21), Tobi(lv21) |  |
| 164 | Map riêng tư | Trái Đất | 0 (MAP_NORMAL) | 10 | 15 | 8 | Khủng long(lv2), Khủng long mẹ(lv3), Thằn lằn bay(lv4) | Khu vực |
| 165 | Sa mạc hoang vu | Xayda | 9 (MAP_MABU_14H) | 10 | 15 | 5 | Cadic M(lv1) | Ôsin |
| 166 | Phòng thí nghiệm Myuu | Xayda | 0 (MAP_NORMAL) | 10 | 15 | 0 |  |  |
| 183 | Thành cổ 1 | Trái Đất | 0 (MAP_NORMAL) | 10 | 15 | 4 | Heo Xayda mẹ(lv21), Khỉ lông đen(lv21) | Hùng Vương |
| 184 | Thành cổ 2 | Trái Đất | 0 (MAP_NORMAL) | 10 | 15 | 6 | Quỷ chim(lv21), Khỉ lông đỏ(lv21), Khỉ lông vàng(lv21) | Hùng Vương |
| 185 | Đấu trường thành cổ | Trái Đất | 0 (MAP_NORMAL) | 10 | 15 | 0 |  | Hùng Vương |

## 6. mob_template – quái

Tổng: **119** quái. Nạp tại `Manager.loadDatabase()` dòng 741–756; gửi xuống client trong `DataGame` (type, name, hp, rangeMove, speed, dartType). **Bảng không có cột level** – level quái được đặt theo từng map trong `map_template.mobs` (phần tử thứ 2). `percent_dame`, `percent_tiem_nang` dùng khi tạo mob trên map (`Map.java` dòng ~206: `mob.pTiemNang = temp.percentTiemNang`).

| id | Tên | TYPE | hp | range_move | speed | dart_Type | percent_dame | percent_tiem_nang |
|---|---|---|---|---|---|---|---|---|
| 0 | Mộc nhân | 0 | 20 | 0 | 1 | 25 | 5 | 10 |
| 1 | Khủng long | 1 | 200 | 33 | 1 | 25 | 5 | 50 |
| 2 | Lợn lòi | 1 | 200 | 33 | 1 | 9 | 5 | 50 |
| 3 | Quỷ đất | 1 | 200 | 33 | 1 | 5 | 5 | 50 |
| 4 | Khủng long mẹ | 1 | 500 | 33 | 2 | 26 | 5 | 50 |
| 5 | Lợn lòi mẹ | 1 | 500 | 33 | 1 | 12 | 5 | 50 |
| 6 | Quỷ đất mẹ | 1 | 500 | 33 | 2 | 27 | 5 | 50 |
| 7 | Thằn lằn bay | 4 | 600 | 33 | 1 | 26 | 5 | 50 |
| 8 | Phi long | 4 | 600 | 33 | 2 | 12 | 5 | 50 |
| 9 | Quỷ bay | 4 | 600 | 33 | 1 | 27 | 5 | 50 |
| 10 | Thằn lằn mẹ | 4 | 1000 | 33 | 2 | 28 | 5 | 50 |
| 11 | Phi long mẹ | 4 | 1000 | 33 | 1 | 28 | 5 | 50 |
| 12 | Quỷ bay mẹ | 4 | 1000 | 33 | 2 | 28 | 5 | 50 |
| 13 | Ốc mượn hồn | 1 | 3000 | 33 | 2 | 22 | 5 | 25 |
| 14 | Ốc sên | 1 | 3000 | 33 | 2 | 22 | 5 | 25 |
| 15 | Heo Xayda mẹ | 1 | 3000 | 33 | 2 | 22 | 5 | 25 |
| 16 | Heo rừng | 1 | 1500 | 33 | 1 | 13 | 5 | 25 |
| 17 | Heo da xanh | 1 | 1500 | 33 | 1 | 13 | 5 | 25 |
| 18 | Heo Xayda | 1 | 1500 | 33 | 1 | 13 | 5 | 25 |
| 19 | Heo rừng mẹ | 1 | 12000 | 33 | 2 | 16 | 5 | 25 |
| 20 | Heo xanh mẹ | 1 | 12000 | 33 | 2 | 16 | 5 | 25 |
| 21 | Alien | 4 | 12000 | 33 | 2 | 41 | 5 | 25 |
| 22 | Bulon | 1 | 6000 | 33 | 2 | 30 | 5 | 25 |
| 23 | Ukulele | 1 | 6000 | 33 | 2 | 23 | 5 | 25 |
| 24 | Quỷ mập | 1 | 6000 | 33 | 2 | 17 | 5 | 25 |
| 25 | Tambourine | 4 | 20000 | 33 | 2 | 34 | 5 | 25 |
| 26 | Drum | 1 | 20000 | 33 | 2 | 24 | 5 | 25 |
| 27 | Akkuman | 1 | 20000 | 33 | 2 | 29 | 5 | 25 |
| 28 | Thằn lằn bay 2 | 4 | 1500 | 33 | 2 | 19 | 5 | 25 |
| 29 | Phi long 2 | 4 | 1500 | 33 | 2 | 7 | 5 | 25 |
| 30 | Quỷ bay 2 | 4 | 1500 | 33 | 2 | 20 | 5 | 25 |
| 31 | Không tặc | 4 | 3000 | 33 | 2 | 40 | 5 | 25 |
| 32 | Quỷ đầu to | 4 | 3000 | 33 | 2 | 15 | 5 | 25 |
| 33 | Quỷ địa ngục | 4 | 3000 | 33 | 2 | 17 | 5 | 25 |
| 34 | Lính độc nhãn | 1 | 30000 | 33 | 2 | 13 | 5 | 25 |
| 35 | Lính độc nhãn | 1 | 30000 | 33 | 2 | 13 | 5 | 25 |
| 36 | Sói xám | 1 | 30000 | 33 | 2 | 13 | 5 | 25 |
| 37 | Robot bay | 4 | 30000 | 33 | 2 | 13 | 5 | 25 |
| 38 | Robot thép | 1 | 30000 | 33 | 2 | 13 | 5 | 25 |
| 39 | Nappa | 1 | 40000 | 33 | 2 | 10 | 5 | 10 |
| 40 | Soldier | 1 | 50000 | 33 | 2 | 10 | 5 | 10 |
| 41 | Appule | 1 | 60000 | 33 | 2 | 42 | 5 | 10 |
| 42 | Raspberry | 1 | 70000 | 33 | 2 | 42 | 5 | 10 |
| 43 | Thằn lằn xanh | 4 | 80000 | 33 | 2 | 43 | 5 | 10 |
| 44 | Quỷ đầu nhọn | 1 | 90000 | 33 | 2 | 43 | 5 | 10 |
| 45 | Quỷ đầu vàng | 1 | 100000 | 33 | 2 | 44 | 5 | 10 |
| 46 | Quỷ da tím | 1 | 110000 | 33 | 2 | 44 | 5 | 10 |
| 47 | Quỷ già | 1 | 120000 | 33 | 2 | 45 | 5 | 10 |
| 48 | Cá sấu | 1 | 130000 | 33 | 2 | 45 | 5 | 10 |
| 49 | Dơi da xanh | 4 | 140000 | 33 | 2 | 46 | 5 | 10 |
| 50 | Quỷ chim | 4 | 180000 | 33 | 2 | 48 | 5 | 10 |
| 51 | Lính đầu trọc | 1 | 150000 | 33 | 1 | 46 | 5 | 10 |
| 52 | Lính tai dài | 1 | 160000 | 33 | 2 | 47 | 5 | 10 |
| 53 | Lính vũ trụ | 1 | 170000 | 33 | 2 | 47 | 5 | 10 |
| 54 | Khỉ lông đen | 1 | 300000 | 33 | 2 | 47 | 5 | 10 |
| 55 | Khỉ giáp sắt | 1 | 350000 | 33 | 2 | 47 | 5 | 10 |
| 56 | Khỉ lông đỏ | 1 | 400000 | 33 | 2 | 47 | 5 | 10 |
| 57 | Khỉ lông vàng | 1 | 450000 | 33 | 2 | 47 | 5 | 10 |
| 58 | Xên con cấp 1 | 1 | 200000 | 33 | 3 | 47 | 5 | 10 |
| 59 | Xên con cấp 2 | 1 | 250000 | 33 | 3 | 47 | 5 | 10 |
| 60 | Xên con cấp 3 | 1 | 300000 | 33 | 3 | 47 | 5 | 10 |
| 61 | Xên con cấp  4 | 1 | 350000 | 33 | 3 | 47 | 5 | 10 |
| 62 | Xên con cấp  5 | 1 | 400000 | 33 | 3 | 47 | 5 | 10 |
| 63 | Xên con cấp  6 | 1 | 450000 | 33 | 3 | 47 | 5 | 10 |
| 64 | Xên con cấp  7 | 1 | 500000 | 33 | 3 | 47 | 5 | 10 |
| 65 | Xên con cấp  8 | 1 | 550000 | 33 | 3 | 47 | 5 | 10 |
| 66 | Tai tím | 1 | 350000 | 33 | 2 | 45 | 5 | 10 |
| 67 | Abo | 1 | 400000 | 33 | 2 | 10 | 5 | 10 |
| 68 | Kado | 1 | 450000 | 33 | 2 | 46 | 5 | 10 |
| 69 | Da xanh | 4 | 500000 | 33 | 2 | 43 | 5 | 10 |
| 70 | Hirudegarn | 1 | 40000000 | 33 | 1 | 43 | 5 | 10 |
| 71 | Vua Bạch Tuộc | 1 | 1500000 | 33 | 1 | 43 | 5 | 10 |
| 72 | Rôbốt bảo vệ | 1 | 1000000 | 33 | 1 | 43 | 5 | 10 |
| 73 | Kawazu | 1 | 50000 | 33 | 2 | 30 | 5 | 10 |
| 74 | Kinkarn | 1 | 55000 | 33 | 2 | 30 | 5 | 10 |
| 75 | Arbee | 4 | 60000 | 33 | 2 | 30 | 5 | 10 |
| 76 | Cỗ máy hủy diệt | 0 | 80000000 | 0 | 1 | 25 | 5 | 10 |
| 77 | Gấu tướng cướp | 1 | 2000000000 | 33 | 2 | 43 | 5 | 10 |
| 78 | Khỉ lông xanh | 1 | 2000000 | 33 | 2 | 47 | 5 | 10 |
| 79 | Taburine Đỏ | 4 | 3000000 | 33 | 3 | 34 | 5 | 10 |
| 80 | Cabira | 1 | 4000000 | 33 | 2 | 10 | 5 | 10 |
| 81 | Tobi | 1 | 5000000 | 33 | 2 | 47 | 5 | 10 |
| 82 | Voi Chín Ngà | 1 | 20000000 | 33 | 2 | 43 | 5 | 10 |
| 83 | Gà Chín Cựa | 1 | 12000000 | 33 | 2 | 43 | 5 | 10 |
| 84 | Ngựa Chín Lmao | 1 | 15000000 | 33 | 2 | 43 | 5 | 10 |
| 85 | Piano | 1 | 2000000000 | 33 | 1 | 43 | 5 | 10 |
| 86 | Ếch mặt đỏ | 1 | 4000000 | 33 | 2 | 62 | 5 | 10 |
| 87 | Jinai | 4 | 6000000 | 33 | 2 | 66 | 5 | 10 |
| 88 | Quỷ đỏ | 1 | 1000000 | 33 | 2 | 66 | 5 | 10 |
| 89 | Quỷ xanh | 1 | 1500000 | 33 | 2 | 62 | 5 | 10 |
| 90 | Quỷ xanh lá | 1 | 1000000 | 33 | 2 | 66 | 5 | 10 |
| 91 | Quỷ vàng | 1 | 1500000 | 33 | 2 | 62 | 5 | 10 |
| 92 | Godzila | 1 | 100000000 | 33 | 2 | 43 | 5 | 10 |
| 93 |  | 1 | 1 | 33 | 2 | 5 | 5 | 10 |
| 94 | Toppo | 1 | 4000000 | 33 | 1 | 47 | 5 | 10 |
| 95 | Cadic M | 1 | 6000000 | 0 | 2 | 66 | 5 | 10 |
| 96 | Janemba | 1 | 4000000 | 33 | 1 | 47 | 5 | 10 |
| 97 | MEZ | 1 | 4000000 | 33 | 1 | 47 | 5 | 10 |
| 98 | GOZ | 1 | 4000000 | 33 | 1 | 47 | 5 | 10 |
| 99 | Đá đỏ | 1 | 4000000 | 33 | 1 | 47 | 5 | 10 |
| 100 | Đá vàng | 1 | 4000000 | 33 | 1 | 47 | 5 | 10 |
| 101 | Đá xanh | 1 | 4000000 | 33 | 1 | 47 | 5 | 10 |
| 102 | Thây ma | 1 | 4000000 | 33 | 1 | 47 | 5 | 10 |
| 103 | Bù nhìn ma quái | 0 | 20 | 0 | 1 | 25 | 5 | 10 |
| 104 | Phù thủy | 4 | 4000000 | 33 | 1 | 47 | 5 | 10 |
| 105 | Frostbite | 1 | 500 | 33 | 2 | 30 | 5 | 10 |
| 106 | Snowy Tangerine | 1 | 550 | 33 | 2 | 30 | 5 | 10 |
| 107 | Deinonychus | 1 | 550 | 33 | 2 | 30 | 5 | 10 |
| 108 | Snake | 1 | 550 | 33 | 2 | 66 | 5 | 10 |
| 109 | Blizzard bird | 1 | 550 | 33 | 2 | 66 | 5 | 10 |
| 110 | Snowman | 1 | 55 | 33 | 2 | 30 | 5 | 10 |
| 111 | Yeti | 1 | 11 | 33 | 2 | 30 | 5 | 10 |
| 112 | Grim Reaper | 1 | 11 | 33 | 2 | 22 | 5 | 10 |
| 113 | Demon 3 | 1 | 11 | 33 | 2 | 30 | 5 | 10 |
| 114 | Golem | 1 | 11 | 50 | 2 | 11 | 5 | 10 |
| 115 | Dazing Stone | 1 | 11 | 50 | 2 | 11 | 5 | 10 |
| 116 | Demon 2 | 1 | 11 | 50 | 2 | 11 | 5 | 10 |
| 117 | Máy đo sức mạnh | 1 | 2000000000 | 1 | 1 | 25 | 5 | 0 |
| 118 | Cadic M | 1 | 6000000 | 33 | 2 | 66 | 5 | 10 |

## 7. npc_template – NPC

Tổng: **93** NPC (id 0 → 110). Nạp tại `Manager.loadDatabase()` dòng 759–772; dùng trong `DataGame` và `NpcFactory`.

| id | Tên | head | body | leg | avatar |
|---|---|---|---|---|---|
| 0 | Ông Gôhan | 18 | 19 | 20 | 349 |
| 1 | Ông Paragus | 24 | 25 | 26 | 348 |
| 2 | Ông Moori | 21 | 22 | 23 | 347 |
| 3 | Rương đồ | 74 | 75 | 265 | 0 |
| 4 | Đậu thần | 84 | 51 | 84 | 0 |
| 5 | Con mèo | 75 | -1 | -1 | 0 |
| 6 | Khu vực | -1 | -1 | -1 | 0 |
| 7 | Bunma | 42 | 43 | 44 | 562 |
| 8 | Dende | 45 | 46 | 47 | 350 |
| 9 | Appule | 3 | 4 | 5 | 565 |
| 10 | Dr. Brief | 784 | 785 | 786 | 7184 |
| 11 | Cargo | 54 | 55 | 56 | 641 |
| 12 | Cui | 48 | 49 | 50 | 639 |
| 13 | Quy Lão Kame | 33 | 34 | 35 | 564 |
| 14 | Trưởng lão Guru | 39 | 40 | 41 | 566 |
| 15 | Vua Vegeta | 36 | 37 | 38 | 563 |
| 16 | Uron | 61 | 62 | 63 | 728 |
| 17 | Bò Mộng | 80 | 81 | 82 | 1142 |
| 18 | Thần mèo Karin | 89 | 90 | 91 | 1209 |
| 19 | Thượng Đế | 86 | 87 | 88 | 1356 |
| 20 | Thần Vũ Trụ | 98 | 99 | 100 | 1357 |
| 21 | Bà Hạt Mít | 117 | 118 | 119 | 1410 |
| 22 | Trọng tài | 114 | 115 | 116 | 1411 |
| 23 | Ghi danh | 120 | 121 | 122 | 1415 |
| 24 | Rồng Thiêng | 103 | 104 | 105 | 0 |
| 25 | Lính canh | 132 | 133 | 134 | 1468 |
| 26 | Độc Nhãn | 144 | 145 | 146 | 1571 |
| 27 | Rồng Thần Namec | 0 | 0 | 0 | 0 |
| 28 | Cửa hàng ký gửi | 120 | 121 | 122 | 1415 |
| 29 | Rồng Omega | 204 | 205 | 206 | 2332 |
| 30 | Rồng 2 sao | 207 | 208 | 209 | 2333 |
| 31 | Rồng 3 sao | 210 | 211 | 212 | 2334 |
| 32 | Rồng 4 sao | 213 | 214 | 215 | 2335 |
| 33 | Rồng 5 sao | 216 | 217 | 218 | 2336 |
| 34 | Rồng 6 sao | 219 | 220 | 221 | 2337 |
| 35 | Rồng 7 sao | 222 | 223 | 224 | 2338 |
| 36 | Rồng 1 sao | 225 | 226 | 227 | 2344 |
| 37 | Bunma | 267 | 268 | 269 | 2752 |
| 38 | Ca Lích | 270 | 271 | 272 | 1364 |
| 39 | Santa | 300 | 301 | 302 | 2993 |
| 40 | Mabư mập | 297 | 298 | 299 | 0 |
| 41 | Trung thu | 120 | 121 | 122 | 0 |
| 42 | Quốc Vương | 442 | 443 | 444 | 4335 |
| 43 | Tổ Sư Kaio | 448 | 449 | 450 | 4389 |
| 44 | Ôsin | 433 | 434 | 435 | 4390 |
| 45 | Kibit | 436 | 437 | 438 | 4391 |
| 46 | Babiđây | 430 | 431 | 432 | 4388 |
| 47 | Giu-ma Đầu Bò | 445 | 446 | 447 | 4339 |
| 48 | Ngộ Không | 462 | 470 | 471 | 0 |
| 49 | Đường Tăng | 467 | 468 | 469 | 4544 |
| 50 | Quả trứng | -1 | -1 | -1 | 0 |
| 51 | Dưa hấu | -1 | -1 | -1 | 4672 |
| 52 | Hùng Vương | 484 | 485 | 486 | 4678 |
| 53 | Tapion | 481 | 482 | 483 | 4668 |
| 54 | Lý Tiểu Nương | 487 | 488 | 489 | 3049 |
| 55 | Bill | 508 | 509 | 510 | 5067 |
| 56 | Whis | 505 | 506 | 507 | 5073 |
| 57 | Champa | 511 | 512 | 513 | 0 |
| 58 | Vados | 530 | 531 | 532 | 5074 |
| 59 | Trọng tài | 533 | 534 | 535 | 0 |
| 60 | Goku SSJ | 101 | 57 | 66 | 1359 |
| 61 | Goku SSJ | 0 | 523 | 524 | 516 |
| 62 | Potage | 621 | 622 | 623 | 5828 |
| 63 | Jaco | 624 | 625 | 626 | 5833 |
| 64 | Thiên Sứ Whis | 505 | 506 | 507 | 5073 |
| 65 | Yarirobe | 77 | 78 | 79 | 0 |
| 66 | Nồi bánh | 766 | 767 | 768 | 7084 |
| 67 | Mr Popo | 83 | 84 | 85 | 2132 |
| 68 | Panchy | 787 | 788 | 789 | 0 |
| 69 | Thỏ Đại Ca | 403 | 404 | 405 | 0 |
| 70 | Bardock | 1012 | 1013 | 1014 | 9075 |
| 71 | Berry | 1015 | 1016 | 1017 | 9076 |
| 72 | Đặc Cầu | 1143 | 1144 | 1145 | 10477 |
| 73 | Fide | 1062 | 1063 | 1064 | 9493 |
| 74 | Tori-Bot | 1143 | 1144 | 1145 | 10477 |
| 75 | Thỏ Đỏ ChiChi | 1098 | 1099 | 1100 | 9976 |
| 76 | Granola | 2018 | 2019 | 2020 | 15233 |
| 77 | Quả trứng linh thú | 1997 | 1998 | 1999 | 15072 |
| 78 | Ông già Noel | 657 | 658 | 659 | 0 |
| 79 | Cây thông Noel | 2003 | 2004 | 2005 | 0 |
| 80 | Npc | 391 | 392 | 393 | 0 |
| 81 | Chi Chi | 1098 | 1099 | 1100 | 9966 |
| 82 | Rương Sưu Tầm | 1999 | 1999 | 1999 | 15288 |
| 83 | Dr. Myuu | 258 | 259 | 260 | 12097 |
| 84 | Xe nước mía | 1758 | 1759 | 1760 | 13267 |
| 103 | Chú Bé Đần | 844 | 845 | 846 | 7775 |
| 104 | Khá BảnH | 2028 | 1066 | 1067 | 15273 |
| 105 | Tiến Bry | 2027 | 253 | 254 | 15272 |
| 106 | Bulma Tết Nguyên Đán | 1380 | 1381 | 1382 | 10477 |
| 107 | Bill Bí Ngô | 754 | 755 | 756 | 7016 |
| 108 | Heart | 2109 | 2110 | 2111 | 16165 |
| 109 | Bulma Bunny | 409 | 410 | 411 | 4119 |
| 110 | Bunma Rực Rỡ | 2123 | 2124 | 2125 | 16269 |

## 8. intrinsic – nội tại

Dump có **208** dòng nhưng chỉ **26** dòng khác nhau: mỗi nội tại bị **lặp 8 lần** (bảng không có PRIMARY KEY). Nạp tại `Manager.loadDatabase()` dòng 494–523 vào `INTRINSICS` và `INTRINSIC_TD/NM/XD` theo gender. Trong tên, `p0`,`p1` = khoảng `param_from_1`–`param_to_1`; `p2`,`p3` = khoảng `param_from_2`–`param_to_2`. Chỉ số người chơi đang có lưu ở `player.data_intrinsic`.

| id | Tên | param_from_1 | param_to_1 | param_from_2 | param_to_2 | icon | gender | Số bản sao |
|---|---|---|---|---|---|---|---|---|
| 0 | Chưa kích hoạt nội tại<br>Bấm vào để xem chi tiết | 0 | 0 | 0 | 0 | 5223 | 3 (Tất cả) | 8 |
| 1 | Chiêu đấm Dragon +p0% đến p1% sát thương | 5 | 25 | 0 | 0 | 569 | 0 (Trái Đất) | 8 |
| 2 | Chiêu Kamejoko +p0% đến p1% sát thương | 5 | 25 | 0 | 0 | 569 | 0 (Trái Đất) | 8 |
| 3 | Thái Dương Hạ San +p0% đến p1% tốc độ -p2% đến p3% KI | 10 | 35 | 10 | 35 | 5222 | 0 (Trái Đất) | 8 |
| 4 | Quả cầu kênh khi +p0% đến p1% tốc độ hồi phục | 15 | 55 | 0 | 0 | 5222 | 0 (Trái Đất) | 8 |
| 5 | Khiên năng lượng +p0% đến p1% tốc độ hồi phục | 15 | 55 | 0 | 0 | 5222 | 0 (Trái Đất) | 8 |
| 6 | Dịch chuyển tức thời +p0% đến p1% sát thương đòn kế | 50 | 150 | 0 | 0 | 568 | 0 (Trái Đất) | 8 |
| 7 | Thôi miên +p0% đến p1% sát thương đòn kế | 50 | 150 | 0 | 0 | 568 | 0 (Trái Đất) | 8 |
| 8 | Chiêu đấm Demon +p0% đến p1% sát thương | 5 | 25 | 0 | 0 | 569 | 1 (Namếc) | 8 |
| 9 | Chiêu Masenko +p0% đến p1% sát thương | 2 | 25 | 0 | 0 | 569 | 1 (Namếc) | 8 |
| 10 | Trị thương +p0% đến p1% tốc độ hồi phục | 15 | 65 | 0 | 0 | 5222 | 1 (Namếc) | 8 |
| 11 | Makankosappo +p0% đến p1% tốc độ hồi phục | 15 | 55 | 0 | 0 | 5222 | 1 (Namếc) | 8 |
| 12 | Đẻ trứng +p0% đến p1% tốc độ hồi phục | 15 | 65 | 0 | 0 | 5222 | 1 (Namếc) | 8 |
| 13 | Liên hoàn +p0% đến p1% sát thương | 5 | 25 | 0 | 0 | 569 | 1 (Namếc) | 8 |
| 14 | Biến Sôcôla +p0% đến p1% sát thương đòn kế | 50 | 150 | 0 | 0 | 568 | 1 (Namếc) | 8 |
| 15 | Khiên năng lượng +p0% đến p1% tốc độ hồi phục | 15 | 55 | 0 | 0 | 5222 | 1 (Namếc) | 8 |
| 16 | Chiêu đấm Galick +p0% đến p1% sát thương | 5 | 25 | 0 | 0 | 569 | 2 (Xayda) | 8 |
| 17 | Chiêu Antomic +p0% đến p1% sát thương | 5 | 25 | 0 | 0 | 569 | 2 (Xayda) | 8 |
| 18 | Biến hình +p0% đến p1% sát thương | 5 | 25 | 0 | 0 | 569 | 2 (Xayda) | 8 |
| 19 | Tự phát nổ +p0% đến p1% tốc độ hồi phục | 15 | 65 | 0 | 0 | 5222 | 2 (Xayda) | 8 |
| 20 | Khiên năng lượng +p0% đến p1% tốc độ hồi phục | 15 | 55 | 0 | 0 | 5222 | 2 (Xayda) | 8 |
| 21 | Huýt sáo +p0% đến p1% tốc độ hồi phục | 15 | 65 | 0 | 0 | 5222 | 2 (Xayda) | 8 |
| 22 | Trói +p0% đến p1% sát thương đòn kế | 50 | 150 | 0 | 0 | 568 | 2 (Xayda) | 8 |
| 23 | Vàng rơi từ quái +p0% đến p1% | 25 | 300 | 0 | 0 | 930 | 3 (Tất cả) | 8 |
| 24 | Sức mạnh và tiềm năng khi đánh quái +p0% đến p1% | 5 | 35 | 0 | 0 | 3783 | 3 (Tất cả) | 8 |
| 25 | Chí mạng liên tục khi HP dưới p0% đến p1% | 20 | 50 | 0 | 0 | 716 | 3 (Tất cả) | 8 |

## 9. radar – thẻ sưu tầm (Rada)

Tổng: **21** thẻ. Nạp tại `Manager.loadDatabase()` dòng 873–908 vào `RadarService.gI().RADAR_TEMPLATE`. `id` trùng id vật phẩm mảnh thẻ (item TYPE 33). `options` = mảng `{id (option_id), param, activeCard (cấp kích hoạt)}`; `body` = `{head, body, leg, bag}` để hiển thị. `mob_id` = id mob_template (−1 nếu là boss).

| id | Tên | rank | max | type | mob_id | require | require_level | aura_id | Options (cấp kích hoạt: chỉ số) |
|---|---|---|---|---|---|---|---|---|---|
| 828 | Thẻ Khủng long | 0 | 120 | 0 | 1 (Khủng long) | -1 | 0 | 1 | lv0: HP+1000; lv1: HP+2000; lv2: HP+3000 |
| 829 | Thẻ Lợn lòi | 0 | 120 | 0 | 2 (Lợn lòi) | -1 | 0 | -1 | lv0: KI+1000; lv1: KI+2000; lv2: KI+3000 |
| 830 | Thẻ Quỷ đất | 0 | 120 | 0 | 3 (Quỷ đất) | -1 | 0 | -1 | lv0: Tấn công+10; lv1: Tấn công+20; lv2: Tấn công+30 |
| 831 | Thẻ Khủng long mẹ | 1 | 120 | 0 | 4 (Khủng long mẹ) | -1 | 0 | -1 | lv0: HP+10000; lv1: HP+20000; lv2: HP+30000 |
| 832 | Thẻ Lợn lòi mẹ | 1 | 120 | 0 | 5 (Lợn lòi mẹ) | -1 | 0 | -1 | lv0: KI+10000; lv1: KI+20000; lv2: KI+30000 |
| 833 | Thẻ Quỷ đất mẹ | 1 | 120 | 0 | 6 (Quỷ đất mẹ) | -1 | 0 | -1 | lv0: Tấn công+100; lv1: Tấn công+200; lv2: Tấn công+300 |
| 834 | Thẻ Thằn lằn bay | 2 | 120 | 0 | 7 (Thằn lằn bay) | -1 | 0 | -1 | lv0: HP+2%/30s; lv1: HP+3%/30s; lv2: HP+5%/30s |
| 835 | Thẻ Phi long | 2 | 120 | 0 | 8 (Phi long) | -1 | 0 | -1 | lv0: KI+2%/30s; lv1: KI+3%/30s; lv2: KI+5%/30s |
| 836 | Thẻ Quỷ bay | 2 | 120 | 0 | 9 (Quỷ bay) | -1 | 0 | -1 | lv0: Giáp+50; lv1: Giáp+100; lv2: Giáp+150 |
| 837 | Thẻ Lính độc nhãn | 3 | 120 | 0 | 34 (Lính độc nhãn) | -1 | 0 | -1 | lv0: HP+10000; lv1: HP+20000; lv2: HP+30000 |
| 838 | Thẻ lính độc nhãn | 3 | 120 | 0 | 35 (Lính độc nhãn) | -1 | 0 | -1 | lv0: KI+10000; lv1: KI+20000; lv2: KI+30000 |
| 839 | Thẻ sói xám | 3 | 120 | 0 | 36 (Sói xám) | -1 | 0 | -1 | lv0: Tấn công+500; lv1: Tấn công+700; lv2: Tấn công+900 |
| 840 | Thẻ trung úy trắng | 4 | 120 | 1 | 1 (Khủng long) | -1 | 0 | -1 | lv0: Giảm 5% sát thương; lv1: HP+5%; lv2: KI +5% |
| 841 | Thẻ ninja tím | 4 | 120 | 1 | 1 (Khủng long) | -1 | 0 | -1 | lv0: Giảm 5% sát thương; lv1: HP+5%; lv2: KI +5% |
| 842 | Thẻ trung úy xanh lơ | 4 | 120 | 1 | 1 (Khủng long) | -1 | 0 | -1 | lv0: Giảm 5% sát thương; lv1: HP+5%; lv2: KI +5% |
| 859 | Thẻ Độc Nhãn | 4 | 120 | 1 | 1 (Khủng long) | -1 | 0 | -1 | lv0: Giảm 5% sát thương; lv1: HP+5%; lv2: KI +5% |
| 956 | Thẻ Đội Trưởng Vàng | 4 | 120 | 1 | 1 (Khủng long) | -1 | 0 | 0 | lv0: Giảm 5% sát thương; lv1: HP+5%; lv2: KI +5% |
| 1204 | Thẻ Rồng Thần Namek | 5 | 120 | 1 | -1 (-) | -1 | 0 | 1 | lv0: Sức đánh+5%; lv1: HP+5%; lv2: KI +5% |
| 1791 | Thẻ Oozaru | 5 | 120 | 1 | -1 (-) | -1 | 0 | 2 | lv0: Chí mạng+5%; lv1: Sức đánh+5%; lv2: HP+10%; lv2: KI +5%; lv2: Giảm 3% sát thương |
| 1792 | Thẻ Oozarun 1 | 6 | 120 | 1 | -1 (-) | -1 | 0 | 3 | lv0: Chí mạng+5%; lv1: Sức đánh+7%; lv2: HP+12%; lv2: KI +7%; lv2: Giảm 7% sát thương |
| 1793 | Thẻ Oozarun 2 | 6 | 120 | 1 | -1 (-) | -1 | 0 | 4 | lv0: Chí mạng+5%; lv1: Sức đánh+10%; lv2: HP+15%; lv2: KI +10%; lv2: Giảm 7% sát thương |

## 10. shop / tab_shop / item_shop – cửa hàng

Nạp tại `ShopDAO.getShops()` → `loadShopTab()` → `loadItemShop()` (chỉ lấy `is_sell = 1`, sắp theo `create_time desc`) → `loadItemShopOption()`. Tổng: **32** shop, **58** tab, **826** item_shop, **1420** item_shop_option. Shop được mở theo `tag_name` (`ShopService.getShop(tagName)`).

> Lưu ý code: `ShopDAO.loadItemShop()` đổi `tab_id` 41–43 thành 10–12 (tab sách của shop `QUY_LAO` dùng chung item với tab sách của `URON`).

### 10.1 Danh sách shop

| shop id | tag_name | npc_id (tên NPC) | type_shop | Các tab (id:tên (số item đang bán)) |
|---|---|---|---|---|
| 1 | BUNMA | 7 (Bunma) | 0 (NORMAL) | 1:Áo Quần (24 item); 2:Phụ kiện (42 item); 3:Đặc biệt (3 item) |
| 2 | DENDE | 8 (Dende) | 0 (NORMAL) | 4:Áo Quần (24 item); 5:Phụ kiện (42 item); 6:Đặc biệt (3 item) |
| 3 | APPULE | 9 (Appule) | 0 (NORMAL) | 7:Áo Quần (24 item); 8:Phụ kiện (42 item); 9:Đặc biệt (3 item) |
| 4 | URON | 16 (Uron) | 0 (NORMAL) | 10:Sách Võ (41 item); 11:Sách Chưởng (56 item); 12:Sách Đặc biệt (56 item); 19:Phụ kiện (50 item) |
| 5 | SANTA_HEAD | 39 (Santa) | 0 (NORMAL) | 13:Tiệm Hớt tóc (9 item) |
| 6 | BUA_1H | 21 (Bà Hạt Mít) | 0 (NORMAL) | 14:Bùa 1 giờ (10 item) |
| 7 | BUA_8H | 21 (Bà Hạt Mít) | 0 (NORMAL) | 15:Bùa 8 giờ (10 item) |
| 8 | BUA_1M | 21 (Bà Hạt Mít) | 0 (NORMAL) | 16:Bùa 1 tháng (10 item) |
| 9 | SANTA | 39 (Santa) | 0 (NORMAL) | 17:Cửa Hàng (15 item); 18:Cải Trang (74 item); 34:Hỗ Trợ (13 item) |
| 10 | BUNMA_FUTURE | 37 (Bunma) | 0 (NORMAL) | 20:Cửa Hàng (2 item) |
| 11 | BILL | 55 (Bill) | 0 (NORMAL) | 21:Trái Đất (5 item); 22:Namếc (5 item); 23:Xay da (5 item) |
| 12 | SANTA_RUONG | 39 (Santa) | 0 (NORMAL) | 29:Cửa   Hàng (0 item) |
| 13 | SANTA_HSD | 39 (Santa) | 0 (NORMAL) | 35:Ngọc (0 item); 36:Vàng (0 item) |
| 14 | OSIN | 44 (Ôsin) | 0 (NORMAL) | 39:Cửa hàng (0 item) |
| 15 | BUNMA_LINHTHU | 37 (Bunma) | 0 (NORMAL) | 31:Cửa hàng (0 item) |
| 16 | KARIN | 18 (Thần mèo Karin) | 3 (SPEC) | 32:Cửa hàng (0 item) |
| 17 | BULMA_TL | 37 (Bunma) | 0 (NORMAL) | 33:Của Hàng Tương Lai (1 item) |
| 19 | CHUBEDAN | 103 (Chú Bé Đần) | 3 (SPEC) |  |
| 20 | THIEN_SU | 56 (Whis) | 3 (SPEC) | 37:Cửa hàng (16 item) |
| 21 | BULMA_EVENT | 106 (Bulma Tết Nguyên Đán) | 3 (SPEC) | 30:Cửa hàng EVENT (9 item); 38:Điểm Sự Kiện (0 item); 40:Vật Phẩm Đeo Lưng (0 item) |
| 22 | SANTA_MO_RONG_HANH_TRANG | 39 (Santa) | 0 (NORMAL) | 46:Cửa   Hàng (2 item) |
| 23 | SANTA_HAN_SU_DUNG | 39 (Santa) | 0 (NORMAL) | 47:Ngọc (6 item); 48:Vàng (3 item) |
| 25 | QUY_LAO | 13 (Quy Lão Kame) | 1 (KINANG) | 41:Sách Võ (41 item); 42:Sách Chưởng (56 item); 43:Sách Đặc biệt (56 item) |
| 26 | SANTA_DANH_HIEU | 39 (Santa) | 0 (NORMAL) | 44:Danh Hiệu (19 item); 45:Sở Hữu  (19 item) |
| 29 | QDDN | 13 (Quy Lão Kame) | 3 (SPEC) | 49:Đổi Thưởng (12 item); 51:Event (6 item) |
| 30 | SANTA_GIAM_GIA_1 | 39 (Santa) | 0 (NORMAL) | 50:Giảm giá 80% (18 item) |
| 31 | SHOP_VIP | 39 (Santa) | 3 (SPEC) | 52:Cải trang (19 item); 53:Deo Lưng (21 item); 54:Ván bay (5 item); 55:Pet (5 item); 56:glt (1 item) |
| 32 | DOI_SKILL_DE | 75 (Thỏ Đỏ ChiChi) | 3 (SPEC) | 57:Đổi Skill Đệ (3 item) |
| 33 | SHOP_CHI_CHI | 81 (Chi Chi) | 0 (NORMAL) | 58:Shop sự kiện (3 item) |
| 34 | SHOP_DOI_DIEM | 13 (Quy Lão Kame) | 3 (SPEC) | 59:Đổi thưởng (10 item) |
| 35 | SHOP_CLAN | 47 (Giu-ma Đầu Bò) | 3 (SPEC) | 60:Cừa Hàng Item (11 item); 61:Của Hàng Pet (15 item); 62:Cửa Hàng Ván Bay (6 item) |
| 36 | SHOP_SU_KIEN_VL | 55 (Bill) | 3 (SPEC) | 63:Đổi Thưởng (5 item) |

### 10.2 Thống kê item_shop theo tab

| tab_id | Tên tab | shop_id | Tổng item | is_sell=1 | type_sell:số lượng | Vật phẩm (rút gọn) |
|---|---|---|---|---|---|---|
| 1 | Áo Quần | 1 | 24 | 24 | 0:24 | Quần bạc Goku, Quần da Calic, Quần jean Calic, Quần thun Kame, Quần thun dày, Quần thun đen, Quần vàng Goku, Quần võ Kame, Quần võ goku, Quần vải Kame, Quần vải dày, Quần vải đen, Áo bạc Goku, Áo da Calic, Áo jean Calic, Áo thun 3 lỗ, Áo thun Kame, Áo thun dày, Áo vàng Goku, Áo võ Goku, Áo võ Kame, |
| 2 | Phụ kiện | 1 | 42 | 42 | 0:39, 1:3 | Giày bạc Goku, Giày cao su Kame, Giày cao su đế dày, Giày da Calic, Giày jean Calic, Giày nhựa Kame, Giày nhựa đế dày, Giày vàng Goku, Giày võ goku, Giày võ kame, Giáp tập luyện cấp 1, Giáp tập luyện cấp 2, Giáp tập luyện cấp 3, Giầy cao su, Giầy nhựa, Găng bạc Goku, Găng da Calic, Găng jean Calic, |
| 3 | Đặc biệt | 1 | 3 | 3 | 0:1, 1:2 | Gói 10 Rađa dò ngọc, Tự động luyện tập, Viên Capsule đặc biệt |
| 4 | Áo Quần | 2 | 24 | 24 | 0:24 | Quần bạc Zealot, Quần len cứng, Quần sắt Tron, Quần sợi gai, Quần sợi len, Quần thun Pico, Quần thun cứng, Quần vàng Zealot, Quần vải cứng Pico, Quần vải mềm Pico, Quần vải thô Pico, Quần đồng Tron, Áo bạc Zealot, Áo choàng len, Áo choàng thun, Áo da Pico, Áo len Pico, Áo sắt Tron, Áo sợi gai, Áo sợ |
| 5 | Phụ kiện | 2 | 42 | 42 | 0:39, 1:3 | Giày bạc Zealot, Giày cao su cứng, Giày da Pico, Giày nhựa cứng, Giày sắt Pico, Giày sắt Tron, Giày vàng Zealot, Giày đồng Tron, Giáp tập luyện cấp 1, Giáp tập luyện cấp 2, Giáp tập luyện cấp 3, Giầy cao su Pico, Giầy nhựa Pico, Giầy sợi gai, Giầy sợi len, Găng bạc Zealot, Găng da Pico, Găng len Pic |
| 6 | Đặc biệt | 2 | 3 | 3 | 0:1, 1:2 | Gói 10 Rađa dò ngọc, Tự động luyện tập, Viên Capsule đặc biệt |
| 7 | Áo Quần | 3 | 24 | 24 | 0:24 | Quần Kaio, Quần da Xayda, Quần giáp bạc, Quần giáp sắt, Quần giáp vàng, Quần giáp đồng, Quần lông Xayda, Quần lông đỏ, Quần lưỡng long, Quần siêu Xayda, Quần thun thô, Quần vải thô, Áo Kaio, Áo giáp bạc, Áo giáp sắt, Áo giáp vàng, Áo giáp đồng, Áo khoác Xayda, Áo lông Xayda, Áo lông đỏ, Áo lưỡng lon |
| 8 | Phụ kiện | 3 | 42 | 42 | 0:39, 1:3 | Giày Kaio, Giày bạc, Giày da Xayda, Giày lông Xayda, Giày lông đỏ, Giày lưỡng long, Giày siêu Xayda, Giày vàng, Giáp tập luyện cấp 1, Giáp tập luyện cấp 2, Giáp tập luyện cấp 3, Giầy cao su thô, Giầy sắt, Giầy vải thô, Giầy đồng, Găng Kaio, Găng bạc, Găng da Xayda, Găng lông Xayda, Găng lông đỏ, Găn |
| 9 | Đặc biệt | 3 | 3 | 3 | 0:1, 1:2 | Gói 10 Rađa dò ngọc, Tự động luyện tập, Viên Capsule đặc biệt |
| 10 | Sách Võ | 4 | 41 | 41 | 1:41 | Kaioken lv1, Kaioken lv2, Kaioken lv3, Kaioken lv4, Kaioken lv5, Kaioken lv6, Kaioken lv7, Sách Dịch Chuyển lv1, Sách Dịch Chuyển lv2, Sách Dịch Chuyển lv3, Sách Dịch Chuyển lv4, Sách Dịch Chuyển lv5, Sách Dịch Chuyển lv6, Sách Dịch Chuyển lv7, Sách Liên hoàn lv1, Sách Liên hoàn lv2, Sách Liên hoàn |
| 11 | Sách Chưởng | 4 | 56 | 56 | 1:56 | Bom hi sinh lv1, Bom hi sinh lv2, Bom hi sinh lv3, Bom hi sinh lv4, Bom hi sinh lv5, Bom hi sinh lv6, Bom hi sinh lv7, Makankosappo lv1, Makankosappo lv2, Makankosappo lv3, Makankosappo lv4, Makankosappo lv5, Makankosappo lv6, Makankosappo lv7, Sách Antomic lv1, Sách Antomic lv2, Sách Antomic lv3, S |
| 12 | Sách Đặc biệt | 4 | 56 | 56 | 1:56 | Hóa khỉ khổng lồ lv1, Hóa khỉ khổng lồ lv2, Hóa khỉ khổng lồ lv3, Hóa khỉ khổng lồ lv4, Hóa khỉ khổng lồ lv5, Hóa khỉ khổng lồ lv6, Hóa khỉ khổng lồ lv7, Khiên năng lượng lv1, Khiên năng lượng lv2, Khiên năng lượng lv3, Khiên năng lượng lv4, Khiên năng lượng lv5, Khiên năng lượng lv6, Khiên năng lượ |
| 13 | Tiệm Hớt tóc | 5 | 17 | 9 | 0:17 | Avatar |
| 14 | Bùa 1 giờ | 6 | 10 | 10 | 1:10 | Bùa Bất Tử, Bùa Da Trâu, Bùa Dẻo Dai, Bùa Mạnh Mẽ, Bùa Oai Hùng, Bùa Thu Hút, Bùa Trí Tuệ, Bùa Trí Tuệ x3, Bùa Trí Tuệ x4, Bùa Đệ Tử |
| 15 | Bùa 8 giờ | 7 | 10 | 10 | 1:10 | Bùa Bất Tử, Bùa Da Trâu, Bùa Dẻo Dai, Bùa Mạnh Mẽ, Bùa Oai Hùng, Bùa Thu Hút, Bùa Trí Tuệ, Bùa Trí Tuệ x3, Bùa Trí Tuệ x4, Bùa Đệ Tử |
| 16 | Bùa 1 tháng | 8 | 10 | 10 | 1:10 | Bùa Bất Tử, Bùa Da Trâu, Bùa Dẻo Dai, Bùa Mạnh Mẽ, Bùa Oai Hùng, Bùa Thu Hút, Bùa Trí Tuệ, Bùa Trí Tuệ x3, Bùa Trí Tuệ x4, Bùa Đệ Tử |
| 17 | Cửa Hàng | 9 | 15 | 15 | 0:5, 1:10 | Bìa sách, Bùa giám định, Cap thời trang 5 ngày, Cap thời trang 7 ngày, Kìm bấm giấy, Mảnh vỡ bông tai, Nâng kỹ năng 1 đệ tử, Nâng kỹ năng 2 đệ tử, Nâng kỹ năng 3 đệ tử, Nâng kỹ năng 4 đệ tử, Rađa kho báu, Trang sách cũ, Đá xanh lam, Đặt tên đệ tử, Đổi đệ tử |
| 18 | Cải Trang | 9 | 74 | 74 | 0:4, 1:70 | CT Lích Tên béo, Cải trang, Cải trang Arale, Cải trang Chan Xư, Cải trang Cooler vàng, Cải trang Frost 1, Cải trang Frost 2, Cải trang Frost 3, Cải trang Gatchan, Cải trang Hợp Thể, Cải trang King kong, Cải trang Lão Cận, Cải trang Mabư Còm, Cải trang Obotchaman, Cải trang Píc, Cải trang Póc, Cải tr |
| 19 | Phụ kiện | 4 | 59 | 50 | 1:59 | Avatar, Avatar đeo khẩu trang, Bình nước phép, Bông tai Porata, Chiến thuyền Tennis, Cân đẩu vân, Cân đẩu vân VIP, Cột nhà, Cục xương, Ghế bay, Gói 30 đậu thần cấp 1, Gói 30 đậu thần cấp 2, Gói 30 đậu thần cấp 3, Gói 30 đậu thần cấp 4, Gói 30 đậu thần cấp 5, Gói 30 đậu thần cấp 6, Gói 30 đậu thần cấ |
| 20 | Cửa Hàng | 10 | 2 | 2 | 0:1, 1:1 | Gói 30 đậu thần cấp 9, Máy dò Capsule kì bí |
| 21 | Trái Đất | 11 | 5 | 5 | 0:5 | Giầy Hủy Diệt, Găng Hủy Diệt, Nhẫn Hủy Diệt, Quần Hủy Diệt, Áo Hủy Diệt |
| 22 | Namếc | 11 | 5 | 5 | 0:5 | Giầy Hủy Diệt, Găng Hủy Diệt, Nhẫn Hủy Diệt, Quần Hủy Diệt, Áo Hủy Diệt |
| 23 | Xay da | 11 | 5 | 5 | 0:5 | Giầy Hủy Diệt, Găng Hủy Diệt, Nhẫn Hủy Diệt, Quần Hủy Diệt, Áo Hủy Diệt |
| 24 | (tab không tồn tại) | ? | 21 | 21 | 0:8, 1:13 | Búa Mjolnir, Búa Stormbreaker, Cải trang, Cải trang Chaien, Cờ Hoa sen, Cờ Hoa đăng, Dao răng cưa, Inosuke, Inosuke Hashibira, Kiếm ánh sáng, Lồng đèn lon, Mèo mun, Nezuko, Nến, Phóng lợn, Quạt ba tiêu, Tanjiro, Ván lướt sóng, Xiên cá, Zenitsu, Áo Thần Xayda |
| 26 | (tab không tồn tại) | ? | 5 | 5 | 0:5 | Giầy Thần Linh, Găng Hủy Diệt, Nhẫn Thần Linh, Quần Thần Linh, Áo Thần Linh |
| 30 | Cửa hàng EVENT | 21 | 9 | 9 | 0:6, 1:3 | Kẹo đường, Nón Noel Xanh, Nón Noel Xám, Túi 7 chú lùn, Tất,vớ giáng sinh |
| 33 | Của Hàng Tương Lai | 17 | 1 | 1 | 1:1 | Máy dò Capsule kì bí |
| 34 | Hỗ Trợ | 9 | 13 | 13 | 0:3, 1:10 | Bùa x2 tn,sm đệ tử, Cỏ bốn lá, Giáp tập luyện cấp 1, Giáp tập luyện cấp 2, Giáp tập luyện cấp 3, Giáp tập luyện cấp 4, Loa to liên vũ trụ, Loa to thế giới, Tự động luyện tập, Vé riêng tư |
| 37 | Cửa hàng | 20 | 16 | 16 | 1:16 | Công thức, Công thức VIP, Đá may mắn cấp 1, Đá may mắn cấp 2, Đá may mắn cấp 3, Đá may mắn cấp 4, Đá may mắn cấp 5, Đá nâng cấp cấp 1, Đá nâng cấp cấp 2, Đá nâng cấp cấp 3, Đá nâng cấp cấp 4, Đá nâng cấp cấp 5 |
| 44 | Danh Hiệu | 26 | 19 | 19 | 1:19 | Bị móc sạch túi, Cao thủ siêu hạng, Danh hiệu X-mas, Em xinh, em đẹp, Fan cứng, Gõ đầu trẻ, KOL, Kẻ thao túng sói, Mẹ Rồng, Nông dân chăm chỉ, Nước anh bao, Thánh đập đồ +7, Thánh ở dơ, Trùm săn Boss, Trùm ước rồng, Ông thần ve chai, Đại gia mới nhú |
| 45 | Sở Hữu | 26 | 19 | 19 | 1:19 | Bị móc sạch túi, Cao thủ siêu hạng, Danh hiệu X-mas, Em xinh, em đẹp, Fan cứng, Gõ đầu trẻ, KOL, Kẻ thao túng sói, Mẹ Rồng, Nông dân chăm chỉ, Nước anh bao, Thánh đập đồ +7, Thánh ở dơ, Trùm săn Boss, Trùm ước rồng, Ông thần ve chai, Đại gia mới nhú |
| 46 | Cửa   Hàng | 22 | 2 | 2 | 0:1, 1:1 | Mở rộng hành trang, Mở rộng rương đồ |
| 47 | Ngọc | 23 | 6 | 6 | 1:6 | Cải trang Mabư Noel, Marron, Số 1 Mabư, Thỏ hồng Bun ma, Thỏ đen Android 18, Thỏ đỏ Chi Chi |
| 48 | Vàng | 23 | 3 | 3 | 0:3 | CT Goku SSJ4, Cánh thiên thần 3, Cải trang Gohan Bư |
| 49 | Đổi Thưởng | 29 | 12 | 12 | 1:12 | Búa Mjolnir, Búa Stormbreaker, CT Black Goku Rose, CT Goku SSJ Blue, CT Goku SSJ3, CT Himmel, Máy bay trực thăng Noel, Pet Bí Ma Vương, Pet Ma vàng phù thủy, Phóng lợn, Quạt ba tiêu, Thỏi vàng bay |
| 50 | Giảm giá 80% | 30 | 18 | 18 | 1:18 | Cải trang |
| 51 | Event | 29 | 6 | 6 | 0:1, 1:5 | Capsule hồng, Chậu đất, Nơ trang trí, Thiệp mừng 8-3, Thuốc tăng trưởng, Túi hạt giống Hoa Hồng |
| 52 | Cải trang | 31 | 19 | 19 | 1:19 | CT Black Goku, CT Black Goku Rose, CT Cađíc, CT Cađíc SSJ, CT Cađíc SSJ Blue, CT Cađíc SSJ2, CT Cađíc SSJ2 M, CT Cađíc SSJ3, CT Cađíc SSJ4, CT Gohan kính mát, CT Gohan đi biển, CT Goku SSJ Blue, CT Goku SSJ3, CT Goku SSJ4, CT Himmel, CT Lý Tiểu Nương hầu gái hồng, CT Lý Tiểu Nương hầu gái xanh, Cải |
| 53 | Deo Lưng | 31 | 21 | 21 | 1:21 | Balo Capybara, Balo Capybara hồng, Bụi tre, Cánh Thiên thần - Ác quỷ, Cánh sấm sét, Cánh sấm sét hoàng kim, Cánh thiên thần 3, Cờ Bạch Vô Thường, Cờ Goku bay, Cờ Hải Ly xe máy, Cờ Hắc Vô Thường, Cờ Olympic, Diều rồng băng, Gấu bắc cực, Hỏa tiêm thương, Khủng Long Thơ Mộng, Lưỡi hái hồng, Lồng đèn ké |
| 54 | Ván bay | 31 | 5 | 5 | 1:5 | Cân Đẩu Vân Thơ Mộng, Thú cưỡi Phong Xích Lan, Thú cưỡi Thích Kim Quy, Tên lửa cá mập, Ván bay Sọ Dừa |
| 55 | Pet | 31 | 5 | 5 | 1:5 | Pet Godzilla, Pet Kong, Pet Shiba đeo nơ, Pet Zịt vàng bối rối, Xe tăng Santa |
| 56 | glt | 31 | 1 | 1 | 1:1 | Giáp tập luyện cấp 4 |
| 57 | Đổi Skill Đệ | 32 | 3 | 3 | 1:3 | Phở Công viên Hòa Bình, Phở Giải Phóng, Phở Linh Lang |
| 58 | Shop sự kiện | 33 | 3 | 3 | 0:1, 1:2 | Kem trái cây, Nước đá, Pet Zịt vàng bối rối |
| 59 | Đổi thưởng | 34 | 10 | 10 | 1:10 | CT Black Goku Rose, CT Frieren, CT Urôn Trư Bát Giới, Cân Đẩu Vân Thơ Mộng, Hộp quà Cađíc VIP, Hộp quà Goku Day VIP, Hộp quà thiếu nhi, Khủng Long Thơ Mộng, Pet Hải Ly, Trứng vàng rồng nhí |
| 60 | Cừa Hàng Item | 35 | 11 | 11 | 1:11 | Con dấu, Cápsule Vỡ, Cỏ bốn lá, Dùi đục, Hematite, Mảnh Rồng thần Namếc, Mảnh khỉ Oorazu, Mảnh khỉ Oorazu 1, Mảnh khỉ Oorazu 2, Đá bảo vệ, Đá mài |
| 61 | Của Hàng Pet | 35 | 15 | 15 | 1:15 | Pet Baby Shark, Pet Capybara hồng, Pet Capybara xì mũi, Pet Capybara đeo ba lô, Pet Giru, Pet Godzilla, Pet Hải Ly, Pet Hải Ly Ong Vàng, Pet Khủng Long ngok, Pet Kong, Pet Ma cầu mưa, Pet Rồng Xanh, Pet Zịt vàng bối rối, Pet tuần lộc, Xe tăng Santa |
| 62 | Cửa Hàng Ván Bay | 35 | 6 | 6 | 1:6 | Môtô Bun ma, Thú cưỡi Cây thông, Thú cưỡi Phong Xích Lan, Thú cưỡi Thích Kim Quy, Ván bay Sọ Dừa, Ván bay té nước |
| 63 | Đổi Thưởng | 36 | 5 | 5 | 1:5 | Hộp thần linh, Kem que, Kho báu hải tặc, Pet Baby Shark, Rada ngọc rồng |

## 11. Nhiệm vụ

### 11.1 task_main_template + task_sub_template (nhiệm vụ chính)

Nạp bằng JOIN tại `Manager.loadDatabase()` dòng 524–550 vào `Manager.TASKS`. Tiến độ người chơi lưu ở `player.data_task = [taskId, subIndex, count, lastTime]`. Trong tên, `%1`, `%2`... là placeholder được thay theo hành tinh khi gửi client. `npc_id`/`map` âm là giá trị đặc biệt (placeholder theo hành tinh).

| id | Tên nhiệm vụ | Số bước | Các bước (index, tên, max_count, npc_id, map) |
|---|---|---|---|
| 0 | Nhiệm vụ đầu tiên | 6 | [0] Di chuyển tới mũi tên chỉ dẫn (x1, npc -1, map -1) → [1] Hãy đi đến nhà %2 ở bên phải (x1, npc -2, map -2) → [2] Nói chuyện với %2 (x1, npc -2, map -2) → [3] Mở rương đồ (x1, npc 3, map -2) → [4] Thu hoạch đậu thần (x1, npc 4, map -2) → [5] Báo cáo với %2 (x1, npc -2, map -2) |
| 1 | Nhiệm vụ tập luyện | 2 | [0] Đánh ngã 5 mộc nhân (x5, npc -1, map -1) → [1] Báo cáo với %2 (x1, npc -2, map -2) |
| 2 | Nhiệm vụ tìm thức ăn | 2 | [0] Thu thập 10 đùi gà (x10, npc -1, map -3) → [1] Báo cáo với %2 (x1, npc -2, map -2) |
| 3 | Nhiệm vụ sao băng | 3 | [0] Sử dụng tiềm năng (x1, npc -1, map -1) → [1] Đi khám phá vật thể lạ (x1, npc -1, map -4) → [2] Báo cáo với %2 (x1, npc -2, map -2) |
| 4 | Nhiệm vụ thử thách | 4 | [0] Đánh 3 con khủng long mẹ (x3, npc -1, map -5) → [1] Đánh 3 con lợn lòi mẹ (x3, npc -1, map -5) → [2] Đánh 3 con quỷ đất mẹ (x3, npc -1, map -5) → [3] Báo cáo với %2 (x1, npc -2, map -2) |
| 5 | Nhiệm vụ thử thách | 4 | [0] Đánh 3 con lợn lòi mẹ (x3, npc -1, map -5) → [1] Đánh 3 con khủng long mẹ (x3, npc -1, map -5) → [2] Đánh 3 con quỷ đất mẹ (x3, npc -1, map -5) → [3] Báo cáo với %2 (x1, npc -2, map -2) |
| 6 | Nhiệm vụ thử thách | 4 | [0] Đánh 3 con quỷ đất mẹ (x3, npc -1, map -5) → [1] Đánh 3 con khủng long mẹ (x3, npc -1, map -5) → [2] Đánh 3 con lợn lòi mẹ (x3, npc -1, map -5) → [3] Báo cáo với %2 (x1, npc -2, map -2) |
| 7 | Nhiệm vụ giải cứu | 4 | [0] Đạt 16.000 sức mạnh (x1, npc -1, map -1) → [1] Đánh bại 20 con %9 (x20, npc -1, map -7) → [2] Nói chuyện với %8 (x1, npc -4, map -8) → [3] Báo cáo với %2 (x1, npc -2, map -2) |
| 8 | Nhiệm vụ tìm ngọc | 3 | [0] Đạt 40.000 sức mạnh (x1, npc -1, map -1) → [1] Tìm viên ngọc rồng 7 sao (x1, npc -1, map -3) → [2] Đem ngọc về cho %2 (x1, npc -2, map -2) |
| 9 | Nhiệm vụ bái sư | 2 | [0] Lên đường (x1, npc -1, map -9) → [1] Chào hỏi %10 (x1, npc -5, map -9) |
| 10 | Nhiệm vụ thử sức | 3 | [0] Đạt 200k sức mạnh (x1, npc -1, map -1) → [1] Diệt 10 con %12 (x10, npc -1, map -9) → [2] Báo cáo với %10 (x1, npc -5, map -9) |
| 11 | Nhiệm vụ gia tăng sức mạnh | 4 | [0] Đạt 500k sức mạnh (x1, npc 54, map 5) → [1] Đạt 550k sức mạnh (x1, npc -1, map -1) → [2] Đạt 600k sức mạnh (x1, npc -1, map -1) → [3] Đi về nhà ông %2 (x1, npc 105, map 42) |
| 12 | Nhiệm vụ xin phép | 3 | [0] Đi về nhà %2 (x1, npc -1, map -2) → [1] Nói chuyện - xin phép gia nhập bang hội (x1, npc -2, map -2) → [2] Báo cáo lại cho %10 (x1, npc -5, map -9) |
| 13 | Nhiệm vụ gia nhập bang hội | 2 | [0] Tạo hoặc gia nhập bang hội có 2 thành viên (x1, npc 13, map 5) → [1] Báo cáo cho %10 (x1, npc -5, map -9) |
| 14 | Nhiệm vụ bang hội đầu tiên | 4 | [0] Tiêu diệt 30 con heo rừng (x30, npc -1, map 27) → [1] Tiêu diệt 30 con heo da xanh (x30, npc -1, map 31) → [2] Tiêu diệt 30 con heo xayda (x30, npc -1, map 35) → [3] Quay về %11 báo cáo nhiệm vụ (x1, npc -5, map -9) |
| 15 | Nhiệm vụ bang hội thứ 2 | 4 | [0] Tiêu diệt 30 bulon (x30, npc -1, map 30) → [1] Tiêu diệt 30 ukulele (x30, npc -1, map 34) → [2] Tiêu diệt 30 quỷ mập (x30, npc -1, map 38) → [3] Quay về %11 báo cáo nhiệm vụ (x1, npc -5, map -9) |
| 16 | Tiêu diệt quái vật | 4 | [0] Tiêu diệt Tambourine (x1, npc -1, map 6) → [1] Tiêu diệt Drum (x1, npc -1, map 10) → [2] Tiêu diệt Akkuman (x1, npc -1, map 19) → [3] Quay về %11 báo cáo nhiệm vụ (x1, npc -5, map -9) |
| 17 | Nhiệm vụ giúp đỡ Cui | 2 | [0] Tới Thành Phố Vegeta (x1, npc -1, map 19) → [1] Nói chuyện với Cui (x1, npc 12, map 19) |
| 18 | Nhiệm vụ bất khả thi | 6 | [0] Tiêu diệt 500 Nappa (x500, npc -1, map -1) → [1] Tiêu diệt 400 Soldier (x400, npc -1, map -1) → [2] Tiêu diệt 300 Appule (x300, npc -1, map -1) → [3] Tiêu diệt 200 Raspberry (x200, npc -1, map -1) → [4] Tiêu diệt 100 Thằn lằn xanh (x100, npc -1, map -1) → [5] Báo cáo với %10 (x1, npc -5, map -1) |
| 19 | Nhiệm vụ tìm diệt đệ tử | 4 | [0] Tiêu diệt Kuku (x1, npc -1, map -1) → [1] Tiêu diệt Mập đầu đinh (x1, npc -1, map -1) → [2] Tiêu diệt Rambo (x1, npc -1, map -1) → [3] Trả nhiệm vụ cho %10 (x1, npc -5, map -1) |
| 20 | Nhiệm vụ Tiểu đội sát thủ | 7 | [0] Đạt 600 tr sức mạnh (x1, npc -1, map -1) → [1] Tiêu diệt Số 4 (x1, npc -1, map -1) → [2] Tiêu diệt Số 3 (x1, npc -1, map -1) → [3] Tiêu diệt Số 2 (x1, npc -1, map -1) → [4] Tiêu diệt Số 1 (x1, npc -1, map -1) → [5] Tiêu diệt Tiểu Đội Trưởng (x1, npc -1, map -1) → [6] Báo cáo với %10 (x1, npc -5, map -1) |
| 21 | Nhiệm vụ chạm trán Fide đại ca | 5 | [0] Đạt 2 tỷ sức mạnh (x1, npc -1, map -1) → [1] Tiêu diệt Fide cấp 1 (x1, npc -1, map -1) → [2] Tiêu diệt Fide cấp 2 (x1, npc -1, map -1) → [3] Tiêu diệt Fide cấp 3 (x1, npc -1, map -1) → [4] Báo cáo với %10 (x1, npc -5, map -1) |
| 22 | Chú bé đến từ tương lai | 6 | [0] Báo cáo với %2 (x1, npc -2, map -2) → [1] Đi tìm vị khách lạ (x1, npc 38, map -1) → [2] Đưa thuốc trợ tim cho Quy Lão (x1, npc 13, map 5) → [3] Đến tương lai gặp Bunma (x1, npc 37, map 102) → [4] Diệt 1000 xên con cấp 1 (x1000, npc -1, map -1) → [5] Báo với Bunma tương lai (x1, npc 37, map 102) |
| 23 | Chạm chán Robot sát thủ lần 1 | 5 | [0] Đến điểm hẹn tìm Rôbốt Sát Thủ (x1, npc -1, map 97) → [1] Tiêu diệt Số 2 (Android 19) (x1, npc -1, map -1) → [2] Tiêu diệt Số 1 (Android 20) (x1, npc -1, map 97) → [3] Diệt 900 xên con cấp 3 (x900, npc -1, map -1) → [4] Báo với Bunma tương lai (x1, npc 37, map 102) |
| 24 | Chạm trán Robot sát thủ lần 2 | 5 | [0] Đến sân sau siêu thị (x1, npc -1, map 104) → [1] Tiêu diệt Android 15 (x1, npc -1, map -1) → [2] Tiêu diệt Android 14 (x1, npc -1, map -1) → [3] Tiêu diệt Android 13 (x1, npc -1, map -1) → [4] Báo với Bunma tương lai (x1, npc 37, map 102) |
| 25 | Chạm trán Robot sát thủ lần 3 | 6 | [0] Đi tìm Píc Póc (x1, npc -1, map -1) → [1] Tiêu diệt Póc (x1, npc -1, map -1) → [2] Tiêu diệt Píc (x1, npc -1, map -1) → [3] Tiêu Diệt Kinh Kong (x1, npc -1, map -1) → [4] Diệt 800 xên con cấp 5 (x800, npc -1, map -1) → [5] Báo với Bunma tương lai (x1, npc 37, map 102) |
| 26 | Chạm trán Xên bọ hung | 6 | [0] Đến thị trấn Ginder (x1, npc -1, map -1) → [1] Tiêu diệt Xên Bọ Hung cấp 1 (x1, npc -1, map -1) → [2] Tiêu diệt Xên Bọ Hung cấp 2 (x1, npc -1, map -1) → [3] Tiêu diệt Xên Bọ Hung hoàn thiện (x1, npc -1, map -1) → [4] Diệt 700 xên con cấp 8 (x700, npc -1, map -1) → [5] Báo với Bunma tương lai (x1, npc 37, map 102) |
| 27 | Cuộc dạo chơi của xên | 6 | [0] Nâng sức đánh gốc lên 10K (x1, npc -1, map -1) → [1] Thu thập Capsule kì bí (x50, npc -1, map -1) → [2] Đến võ đài xên bọ hung (x1, npc -1, map -1) → [3] Tiêu diệt 7 đứa con của xên (x7, npc -1, map -1) → [4] Tiêu diệt Siêu Bọ Hung (x1, npc -1, map -1) → [5] Báo với Bunma tương lai (x1, npc 37, map 102) |
| 28 | Cuộc đối đầu không cân sức | 8 | [0] Đi theo Ôsin (x1, npc 44, map -1) → [1] Hạ vua địa ngục Drabura (x10, npc -1, map -1) → [2] Hạ Pui Pui (x10, npc -1, map -1) → [3] Hạ Pui Pui lần 2 (x10, npc -1, map -1) → [4] Hạ Yacôn (x10, npc -1, map -1) → [5] Hạ Drabura lần 2 (x10, npc -1, map -1) → [6] Hạ Mabư (x10, npc -1, map -1) → [7] Báo cáo với Ôsin (x1, npc 44, map -1) |
| 29 | .. | 1 | [0] bạn đã xong nhiệm vụ rồi (x-1, npc -1, map -1) |

### 11.2 side_task_template (nhiệm vụ hàng ngày) và clan_task_template (nhiệm vụ bang)

Nạp tại `Manager.loadDatabase()` dòng 552–576 và 591–615. Cột `max_count_lvN` dạng `"min-max"` → `count[N-1][0..1]` (số lượng cần làm được random trong khoảng theo mức độ). Tiến độ lưu ở `player.data_side_task` / `player.data_clan_task`.

| id | side_task NAME | lv1 | lv2 | lv3 | lv4 | lv5 | clan_task NAME |
|---|---|---|---|---|---|---|---|
| 0 | Tiêu diệt %1 khủng long | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 | Hạ %1 khủng long |
| 1 | Tiêu diệt %1 lợn lòi | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 | Hạ %1 lợn lòi |
| 2 | Tiêu diệt %1 quỷ đất | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 | Hạ %1 quỷ đất |
| 3 | Tiêu diệt %1 khủng long mẹ | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 | Hạ %1 khủng long mẹ |
| 4 | Tiêu diệt %1 lợn lòi mẹ | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 | Hạ %1 lợn lòi mẹ |
| 5 | Tiêu diệt %1 quỷ đất mẹ | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 | Hạ %1 quỷ đất mẹ |
| 6 | Tiêu diệt %1 thằn lằn bay | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 | Hạ %1 thằn lằn bay |
| 7 | Tiêu diệt %1 phi long | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 | Hạ %1 phi long |
| 8 | Tiêu diệt %1 quỷ bay | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 | Hạ %1 quỷ bay |
| 9 | Tiêu diệt %1 thằn lằn mẹ | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 | Hạ %1 thằn lằn mẹ |
| 10 | Tiêu diệt %1 phi long mẹ | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 | Hạ %1 phi long mẹ |
| 11 | Tiêu diệt %1 quỷ bay mẹ | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 | Hạ %1 quỷ bay mẹ |
| 12 | Tiêu diệt %1 heo rừng | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 | Hạ %1 heo rừng |
| 13 | Tiêu diệt %1 heo da xanh | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 | Hạ %1 heo da xanh |
| 14 | Tiêu diệt %1 heo xayda | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 | Hạ %1 heo xayda |
| 15 | Tiêu diệt %1 ốc mượn hồn | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 | Hạ %1 ốc mượn hồn |
| 16 | Tiêu diệt %1 ốc sên | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 | Hạ %1 ốc sên |
| 17 | Tiêu diệt %1 heo xayda mẹ | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 | Hạ %1 heo xayda mẹ |
| 18 | Tiêu diệt %1 không tặc | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 | Hạ %1 không tặc |
| 19 | Tiêu diệt %1 quỷ đầu to | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 | Hạ %1 quỷ đầu to |
| 20 | Tiêu diệt %1 quỷ địa ngục | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 | Hạ %1 quỷ địa ngục |
| 21 | Tiêu diệt %1 heo rừng mẹ | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 | Hạ %1 heo rừng mẹ |
| 22 | Tiêu diệt %1 heo xanh mẹ | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 | Hạ %1 heo xanh mẹ |
| 23 | Tiêu diệt %1 alien | 1-20 | 20-100 | 100-500 | 500-2000 | 2000-5000 | Hạ %1 alien |
| 24 | Tiêu diệt %1 tambourine | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 tambourine |
| 25 | Tiêu diệt %1 drum | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 drum |
| 26 | Tiêu diệt %1 akkuman | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 akkuman |
| 27 | Tiêu diệt %1 nappa | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 nappa |
| 28 | Tiêu diệt %1 soldier | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 soldier |
| 29 | Tiêu diệt %1 appule | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 appule |
| 30 | Tiêu diệt %1 raspberry | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 raspberry |
| 31 | Tiêu diệt %1 thằn lằn xanh | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 thằn lằn xanh |
| 32 | Tiêu diệt %1 quỷ đầu nhọn | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 quỷ đầu nhọn |
| 33 | Tiêu diệt %1 quỷ đầu vàng | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 quỷ đầu vàng |
| 34 | Tiêu diệt %1 quỷ da tím | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 quỷ da tím |
| 35 | Tiêu diệt %1 quỷ già | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 quỷ già |
| 36 | Tiêu diệt %1 cá sấu | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 cá sấu |
| 37 | Tiêu diệt %1 dơi da xanh | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 dơi da xanh |
| 38 | Tiêu diệt %1 quỷ chim | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 quỷ chim |
| 39 | Tiêu diệt %1 lính đầu trọc | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 lính đầu trọc |
| 40 | Tiêu diệt %1 lính tai dài | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 lính tai dài |
| 41 | Tiêu diệt %1 lính vũ trụ | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 lính vũ trụ |
| 42 | Tiêu diệt %1 khỉ lông đen | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 khỉ lông đen |
| 43 | Tiêu diệt %1 khỉ giáp sắt | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 khỉ giáp sắt |
| 44 | Tiêu diệt %1 khỉ lông đỏ | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 khỉ lông đỏ |
| 45 | Tiêu diệt %1 khỉ lông vàng | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 khỉ lông vàng |
| 46 | Tiêu diệt %1 xên con cấp 1 | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 xên con cấp 1 |
| 47 | Tiêu diệt %1 xên con cấp 2 | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 xên con cấp 2 |
| 48 | Tiêu diệt %1 xên con cấp 3 | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 xên con cấp 3 |
| 49 | Tiêu diệt %1 xên con cấp 4 | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 xên con cấp 4 |
| 50 | Tiêu diệt %1 xên con cấp 5 | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 xên con cấp 5 |
| 51 | Tiêu diệt %1 xên con cấp 6 | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 xên con cấp 6 |
| 52 | Tiêu diệt %1 xên con cấp 7 | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 xên con cấp 7 |
| 53 | Tiêu diệt %1 xên con cấp 8 | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 xên con cấp 8 |
| 54 | Tiêu diệt %1 tai tím | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 tai tím |
| 55 | Tiêu diệt %1 abo | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 abo |
| 56 | Tiêu diệt %1 kado | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 kado |
| 57 | Tiêu diệt %1 da xanh | 1-5 | 5-20 | 20-100 | 100-500 | 500-1000 | Hạ %1 da xanh |
| 58 | Nhặt %1 vàng | 1000-3000 | 3000-20000 | 20000-100000 | 100000-10000000 | 10000000-100000000 | Nhặt %1 vàng |

## 12. Thành tựu và danh hiệu

### 12.1 achievement_template

Nạp tại `Manager.loadDatabase()` dòng 617–624 vào `Manager.ACHIEVEMENT_TEMPLATE` (thứ tự dòng = index trong `player.data_achievement`). `money` = phần thưởng, `max_count` = mốc cần đạt; `%1` trong `info2` thay bằng `max_count`.

| id | info1 | info2 | money | max_count |
|---|---|---|---|---|
| 1 | Gia nhập Vệ Binh | Đạt cấp Vệ Binh | 10000 | 340000 |
| 2 | Sức mạnh siêu cấp | Đạt cấp %1 | 50 | 1500000 |
| 3 | Nông dân chăm chỉ | Cây đậu thần đạt cấp 5 | 20 | 5 |
| 4 | Trăm trận trăm thắng | Thắng 100 người khác nhau | 20 | 100 |
| 5 | Nội công cao cường | Chưởng 2.000 phát | 10 | 2000 |
| 6 | Khinh công thành thạo | Bay 20.000 mét | 10 | 20000 |
| 7 | Thợ săn thiện xạ | Hạ 1.000 quái trên không | 10 | 1000 |
| 8 | Tập luyện bài bản | Hạ 1.000 người rơm | 10 | 1000 |
| 9 | Hoạt động chăm chỉ | Chơi hơn 120 giờ | 20 | 120 |
| 10 | Hỗ trợ đồng đội | Cho 10.000 đậu thần | 50 | 10000 |
| 11 | Trùm nhặt ve chai | Bán cho %2 200 món đồ | 10 | 200 |
| 12 | Lần đầu nạp ngọc | Nạp ít nhất 150 ngọc | 50 | 150 |
| 13 | Đánh bại siêu quái | Hạ 100 siêu quái | 10 | 100 |
| 14 | Thánh hồi sinh | Hồi sinh tại chỗ 200 lần | 50 | 200 |
| 15 | Kỹ năng thành thạo | Dùng chiêu đặc biệt 1000 lần | 20 | 1000 |
| 16 | Trùm nhặt ngọc | Nhặt 1000 ngọc | 20 | 1000 |
| 17 | Đạt 15 triệu sức mạnh | Dành cho tân thủ từ 19/7/2023 | 100 | 15000000 |
| 18 | Tuyệt kỹ thành thạo | Dùng tuyệt kỹ (skill thứ 9) 7749 lần | 50000 | 7749 |
| 19 | Chăm sóc đặc biệt | Được Namếc hồi sinh 2K lần | 15 | 2000 |
| 20 | Trùm kết liễu Boss | Đánh đòn cuối hạ Boss 2K lần | 20 | 2000 |

### 12.2 data_badges (danh hiệu) và task_badges_template (nhiệm vụ nhận danh hiệu)

`data_badges` nạp tại `Manager.loadDatabase()` dòng 772–792 → `Manager.BAGES_TEMPLATES`; `idEffect` là id danh hiệu (khớp `BadgesData.idBadGes` trong `player.dataBadges`), `idItem` là item TYPE 36 tương ứng (`BagesTemplate.findIdItemByIdIdEffect()`). `task_badges_template` nạp tại dòng 578–589; `idBadgesReward` = `idEffect` được thưởng khi hoàn thành (`BadgesTaskService` dòng 24–37, thời hạn 30 ngày).

| id | idEffect | idItem | NAME | Options |
|---|---|---|---|---|
| 1 | 218 | 1289 | Đại gia mới nhú | Sức đánh+15%; Hạn sử dụng 30 ngày |
| 2 | 219 | 1290 | Trùm ước rồng | HP+6%; Hạn sử dụng 30 ngày |
| 3 | 220 | 1291 | Trùm săn boss | Sức đánh+5%; Hạn sử dụng 30 ngày |
| 4 | 221 | 1292 | Thánh đập đồ +7 | HP+10%; KI +10%; Hạn sử dụng 30 ngày |
| 5 | 222 | 1293 | Cao thủ siêu hạng | HP+8%; Hạn sử dụng 30 ngày |
| 6 | 223 | 1294 | Nông dân chăm chỉ | HP+5%; KI +5%; Hạn sử dụng 30 ngày |
| 7 | 224 | 1295 | Ông thần ve chai | 3% Né đòn; Hạn sử dụng 30 ngày |
| 8 | 225 | 1296 | Bị móc sạch túi | 5% Né đòn; HP+5%; KI +5%; Hạn sử dụng 30 ngày |
| 9 | 228 | 1299 | Fan cứng | Sức đánh+3%; HP+3%; KI +3%; Hạn sử dụng 30 ngày |
| 12 | 242 | 1392 | Gõ đầu trẻ | HP+10%; KI +10%; +10% sức đánh chí mạng; Hạn sử dụng 30 ngày |
| 13 | 243 | 1393 | Gõ đầu trẻ | HP+10%; KI +10%; +10% sức đánh chí mạng; Hạn sử dụng 30 ngày |
| 14 | 240 | 1394 | Gõ đầu trẻ | HP+10%; KI +10%; +10% sức đánh chí mạng; Hạn sử dụng 30 ngày |
| 15 | 247 | 1457 | X-mas | Sức đánh+12%; HP+12%; KI +12%; Hạn sử dụng 30 ngày |
| 16 | 253 | 1514 | Em xinh, em đẹp | Sức đánh+11%; HP+11%; KI +11%; Đẹp +5% SĐ cho mình và người xung quanh; Hạn sử dụng 30 ngày |
| 17 | 256 | 1790 | Mẹ Rồng | Sức đánh+13%; HP+13%; +7% sức đánh chí mạng; Hạn sử dụng 30 ngày |
| 18 | 226 | 1297 | KOL | Sức đánh+10%; HP+10%; Sát thương chuẩn 103%; Hạn sử dụng 30 ngày |

| id | NAME | maxCount | idBadgesReward |
|---|---|---|---|
| 1 | Nạp Tích luỹ 1 Triệu Trong Ngày | 1000000 | 218 |
| 2 | Ước Rồng Thần 1 Sao X100 Lần | 100 | 219 |
| 3 | Hạ Gục Cumber, Black Goku, Cooler, Xên ( 300 Lần ) | 300 | 220 |
| 4 | Đập 5 Trang Bị +7 Trong Ngày | 5 | 221 |
| 5 | Top 1 Đại Hội Võ Đài Siêu Hạng | 1 | 222 |
| 6 | Hoàn Thành 10 Nhiệm Vụ Siêu Khó Tại Bò Mộng | 10 | 223 |
| 7 | Đánh Bại, Hoặc Cho Xương Sói 20 Lần | 20 | 1286 |
| 8 | Hoàn Thành Nhiệm Vụ 5 Lần Cho Xinbato Nước | 5 | 1287 |
| 9 | Nhặt Đồ Trong Ngày 500 Lần | 500 | 224 |
| 10 | Tiêu diệt 30 Boss Ăn Trộm | 30 | 225 |
| 11 | Tiêu Diệt 30 Boss Ở Dơ | 30 | 1300 |
| 12 | Mở Rương Gỗ Cấp 12 | 10 | 240 |
| 13 | Mở Rương Gỗ Cấp 12 | 20 | 242 |
| 14 | Mở Rương Gỗ Cấp 12 | 30 | 243 |
| 15 | Đạt 500 Điểm Sự Kiện | 500 | 247 |
| 16 | Nạp tích lũy 2 triệu trong ngày | 2000000 | 253 |
| 17 | Sở hữu 7 rồng nhí vĩnh viễn | 7 | 256 |
| 18 | Fan cứng KOL | 1 | 226 |

## 13. Ghi chú / điểm cần lưu ý

- **`intrinsic` bị nhân bản 8 lần** (208 dòng / 26 nội tại thực) do bảng không có PRIMARY KEY. `Manager.loadDatabase()` thêm tất cả vào `INTRINSICS`/`INTRINSIC_TD/NM/XD` → danh sách trong RAM bị lặp; `IntrinsicService.getIntrinsicById()` vẫn trả đúng (lấy bản đầu tiên) nhưng tốn bộ nhớ và có thể lệch khi random/hiển thị danh sách. Nên dedupe và thêm PK.
- **`part` có id trùng**: ['1949'] (bảng không có PK). `Manager.loadDatabase()` ghi tuần tự ra file `data/update_data/part` → client có thể nhận thừa phần tử.
- `item_option_template` có 1 option tên rỗng (id: 73) – thường là option ẩn dùng nội bộ (ví dụ 73).
- `item_shop` tham chiếu `temp_id` không tồn tại trong item_template: không có; `tab_id` không tồn tại trong tab_shop: ['24', '26'] (một phần do cơ chế remap 41–43 → 10–12).
- `mob_template.id`, `npc_template.id`, `intrinsic.id`, `skill_template.id`, `map_template.mobs[].tempId`, `shop.npc_id` đều được đọc bằng `getByte()` → giới hạn 127. Hiện max id mob = 118, npc = 110 (an toàn nhưng sắp chạm giới hạn).
- `map_template.zones` và `max_player` đọc bằng `getByte()`; dump `team2026` đặt zones=10, max_player=15 cho phần lớn map (dump `SRC/sql/nro1.sql` cũ là 20/12).
- `item_template.description` chỉ `varchar(75)` – mô tả dài bị cắt (ví dụ id 521).
- Có 98 tên vật phẩm bị trùng (ví dụ: Vàng, Cà rốt, Avatar, Bùa Trí Tuệ, Bùa Mạnh Mẽ, Bùa Da Trâu, Avatar VIP, Cải trang, Cờ đỏ, Cờ tím) – khi tra cứu nên dùng id.
- `MrBlue.loadPlayer()` (dòng 415 và 473) có xử lý thu hồi riêng cho item id 2132 và 2322 nhưng `item_template` chỉ có id tới 1999 → code chết/không khớp dữ liệu.
- TYPE 8/9/10/16/17/28/31/34/37/75 không có nhánh xử lý theo TYPE rõ ràng trong code đã khảo sát; ý nghĩa ghi trong bảng 3.1 là **suy ra từ tên vật phẩm**.
