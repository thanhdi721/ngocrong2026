# 24a — Sửa lỗi kinh tế (đã thi công)

> Nhật ký thi công nhóm **lỗi kinh tế**. Chủ dự án đã duyệt sửa luôn.
> Nguồn mô tả lỗi: [07 — Shop, Giao dịch, Ký gửi](../1-he-thong-hien-tai/07-shop-giao-dich-ky-gui.md) · [10 — Rồng thần](../1-he-thong-hien-tai/10-rong-than.md) · [16 — Sự kiện, Minigame, Giải đấu](../1-he-thong-hien-tai/16-su-kien-minigame-giai-dau.md) · [19 — VIP, Nạp tiền, Tiền tệ](../1-he-thong-hien-tai/19-vip-nap-tien-tien-te.md)
> Đường dẫn Java viết tắt `models/` = `SRC/src/nro/models/`.
> **Chưa biên dịch kiểm tra** (máy thi công chưa có JDK) — cần `ant`/`javac` chạy lại trước khi lên server.
> Mọi chỗ sửa đều có comment `// FIX: …` ngay tại dòng để tra ngược.

## Mục lục

1. [Ký gửi: người bán nhận Thỏi vàng thay vì vàng](#1-ký-gửi-người-bán-nhận-thỏi-vàng-thay-vì-vàng)
2. [Ký gửi: giới hạn giá và số lượng dùng `&&` nên vô hiệu](#2-ký-gửi-giới-hạn-giá-và-số-lượng-dùng--nên-vô-hiệu)
3. [Bán Thỏi vàng qua gói tin được 500 triệu thay vì 37 triệu](#3-bán-thỏi-vàng-qua-gói-tin-được-500-triệu-thay-vì-37-triệu)
4. [Rồng thần: điều ước vàng gán thẳng thay vì cộng thêm](#4-rồng-thần-điều-ước-vàng-gán-thẳng-thay-vì-cộng-thêm)
5. [Rồng thần: giảm thưởng ngọc xuống 2.000 mỗi lần ước](#5-rồng-thần-giảm-thưởng-ngọc-xuống-2000-mỗi-lần-ước)
6. [Mua VIP không lưu việc trừ VND vào DB](#6-mua-vip-không-lưu-việc-trừ-vnd-vào-db)
7. [Con số may mắn nhân thưởng theo số vé đã mua](#7-con-số-may-mắn-nhân-thưởng-theo-số-vé-đã-mua)
8. [Thách đấu PVP không trừ tiền cược lúc bắt đầu trận](#8-thách-đấu-pvp-không-trừ-tiền-cược-lúc-bắt-đầu-trận)
9. [Tổng hợp file đã đụng](#9-tổng-hợp-file-đã-đụng)
10. [Điểm cần chủ dự án xác nhận](#10-điểm-cần-chủ-dự-án-xác-nhận)

---

## 1. Ký gửi: người bán nhận Thỏi vàng thay vì vàng

| | |
|---|---|
| **File** | `models/shop_ky_gui/ConsignShopService.java` |
| **Hàm** | `claimOrDel(Player pl, byte action, int id)` — nhánh `case 2` (nhận tiền) |
| **Dòng** | ~308–318 (sau sửa) |
| **Mức độ** | Nghiêm trọng nhất — tạo vàng vô hạn |

### Code trước

```java
if (it.goldSell > 0) {
    Item tvAdd = ItemService.gI().createNewItem((short) 457);
    tvAdd.quantity = it.goldSell - it.goldSell * 10 / 100;
    InventoryService.gI().addItemBag(pl, tvAdd);
} else if (it.gemSell > 0) {
    pl.inventory.gem += it.gemSell - it.gemSell * 10 / 100;
}
```

### Code sau

```java
if (it.goldSell > 0) {
    // FIX: người mua trả vàng nên người bán phải nhận lại vàng (trước đây nhận Thỏi vàng => tạo vàng vô hạn)
    long goldReceive = (long) it.goldSell - (long) it.goldSell * 10 / 100;
    pl.inventory.gold += goldReceive;
    if (pl.inventory.gold > Inventory.LIMIT_GOLD) {
        pl.inventory.gold = Inventory.LIMIT_GOLD;
    }
} else if (it.gemSell > 0) {
    pl.inventory.gem += it.gemSell - it.gemSell * 10 / 100;
}
```

(thêm `import nro.models.player.Inventory;`)

### Vì sao sửa vậy

`buyItem` trừ của người mua đúng `it.goldSell` **vàng** (`pl.inventory.gold -= it.goldSell`). Nhưng khi người bán bấm "nhận tiền", server lại tạo ra **item 457 Thỏi vàng** với `quantity` bằng số vàng đó. 1 Thỏi vàng bán lại được 37.000.000 vàng ⇒ hệ số nhân **≈ 33.300.000 lần**. Hai tài khoản cùng chủ chỉ cần ký gửi qua lại 1 viên ngọc rồng giá 1.000 vàng là sinh ra 900 Thỏi vàng ≈ 33 tỷ vàng từ 1.000 vàng.

Sửa thành cộng đúng loại tiền (vàng) và đúng số người mua đã trả, trừ 10% phí bán như cũ (`x − x*10/100`, làm tròn xuống). Chặn trần `Inventory.LIMIT_GOLD` (200 tỷ) để không tràn quá giới hạn vàng của game.

### Rủi ro còn lại

- Phần ghi vào ô "vàng" không giới hạn theo ô trống túi nữa ⇒ tin ký gửi cũ trong DB (`shop_ky_gui`) đang chờ nhận tiền sẽ trả bằng **vàng**, không phải thỏi vàng. Đây là điều mong muốn, nhưng người chơi đã "đặt gạch" chờ ăn thỏi vàng sẽ thắc mắc.
- **Vàng đã bị bơm ra trước đây không tự mất đi.** Cần rà `shop_ky_gui` và số Thỏi vàng bất thường trong DB (xem mục 10).
- Nếu vàng người bán đã sát 200 tỷ, phần vượt bị cắt (mất). Trước đây thỏi vàng vào túi nên không bị cắt.

### Cách test thủ công trong game

1. Dùng 2 tài khoản A, B (B có sức mạnh ≥ 17 tỷ để mua được).
2. A đến NPC **Cửa hàng ký gửi** (npc 28), ký gửi 1 Ngọc Rồng 1 sao giá **10.000 vàng** (mất phí 1 Thỏi vàng).
3. B mua tin đó ⇒ B mất đúng 10.000 vàng.
4. A vào tab "của tôi" bấm **nhận tiền** ⇒ A phải nhận **9.000 vàng** (10.000 − 10%), **không** nhận item Thỏi vàng nào.
5. Kiểm tra số Thỏi vàng trong túi A không đổi.

---

## 2. Ký gửi: giới hạn giá và số lượng dùng `&&` nên vô hiệu

| | |
|---|---|
| **File** | `models/shop_ky_gui/ConsignShopService.java` |
| **Hàm** | `KiGui(Player pl, int id, int money, byte moneyType, int quantity)` |
| **Dòng** | 29–31 (hằng số), 406–410 (số lượng), 414–415 (giá vàng), 429–430 (giá ngọc) |

### Code trước

```java
if (quantity > 99 && quantity < 0) {
    Service.gI().sendThongBao(pl, "Ký gửi tối đa x99");
    ...
}
switch (moneyType) {
    case 0:// vàng
        if (money > 100000 && money < 0) {
            Service.gI().sendThongBao(pl, "không thể ký gửi quá 100000 thỏi vàng");
        } else { ... }
    case 1:// Ngọc Xanh
        if (money > 1000000 && money < 0) {
            Service.gI().sendThongBao(pl, "không thể ký gửi quá 1000000 ngọc");
        } else { ... }
```

### Code sau

```java
// (đầu lớp)
// FIX: trần giá ký gửi (trước đây các điều kiện kiểm tra dùng && nên vô hiệu)
private static final int MAX_GOLD_CONSIGN = 200_000_000;
private static final int MAX_GEM_CONSIGN = 1_000_000;

// FIX: điều kiện cũ dùng && nên không bao giờ đúng => giới hạn số lượng vô hiệu
if (quantity < 1 || quantity > 99) {
    Service.gI().sendThongBao(pl, "Ký gửi tối đa x99");
    ...
}
switch (moneyType) {
    case 0:// vàng
        // FIX: điều kiện cũ dùng && nên không bao giờ đúng => không có giới hạn giá.
        // Mốc trần lấy theo hướng dẫn của NPC Ký gửi ("10k-200Tr vàng").
        if (money < 1 || money > MAX_GOLD_CONSIGN) {
            Service.gI().sendThongBao(pl, "Giá ký gửi bằng vàng phải từ 1 đến 200.000.000");
        } else { ... }
    case 1:// Ngọc Xanh
        // FIX: điều kiện cũ dùng && nên không bao giờ đúng => không có giới hạn giá
        if (money < 1 || money > MAX_GEM_CONSIGN) {
            Service.gI().sendThongBao(pl, "không thể ký gửi quá 1000000 ngọc");
        } else { ... }
```

### Vì sao sửa vậy

`x > 99 && x < 0` là điều kiện **không bao giờ thoả** (một số không thể vừa > 99 vừa < 0). Tác giả muốn viết `||`. Kết quả: không có giới hạn nào cả — client tự chế có thể gửi `quantity` âm, `quantity` rất lớn, hoặc `money` sát `Integer.MAX_VALUE`.

- **Số lượng**: đổi thành `quantity < 1 || quantity > 99`, đúng với thông báo "Ký gửi tối đa x99". Chặn thêm `quantity <= 0` mà kiểm tra `money <= 0 || quantity > it.quantity` ở trên không bắt được (ký gửi x0 / x−5).
- **Giá ngọc**: giữ nguyên hằng số của code (1.000.000 ngọc) — đủ rộng, không ảnh hưởng người chơi thường.
- **Giá vàng**: hằng số gốc `100000` (100 nghìn vàng) quá thấp so với kinh tế hiện tại (đồ shop tới 1 tỷ vàng) ⇒ nếu áp đúng sẽ **chặn cả giao dịch hợp lệ**. Đã lấy mốc **200.000.000** theo đúng hướng dẫn NPC `KyGui.java` hiển thị cho người chơi: *"Giá trị ký gửi 10k-200Tr vàng"*. Thông báo lỗi được sửa lại cho khớp (thông báo cũ ghi "100000 thỏi vàng" là sai đơn vị — `money` là **vàng**, không phải thỏi).

### Rủi ro còn lại

- **Đây là quyết định về con số, không phải quyết định kỹ thuật.** Nếu chủ dự án muốn trần khác, sửa 2 hằng số `MAX_GOLD_CONSIGN` / `MAX_GEM_CONSIGN` ở đầu `ConsignShopService.java` là đủ.
- Tin ký gửi cũ trong DB có giá > 200 triệu vẫn tồn tại và vẫn mua được; giới hạn chỉ áp lúc **đăng bán mới**.
- Vẫn **không giới hạn số tin đăng mỗi người** và tin **không có hạn dùng** — nằm ngoài phạm vi đợt sửa này.

### Cách test thủ công trong game

1. Tại NPC Ký gửi, chọn 1 item ký gửi được, nhập giá vàng **300.000.000** ⇒ phải báo *"Giá ký gửi bằng vàng phải từ 1 đến 200.000.000"* và **không** bị trừ item.
2. Nhập giá **50.000.000** ⇒ đăng bán thành công.
3. Nhập số lượng **0** ⇒ báo *"Ký gửi tối đa x99"*, không mất item.
4. Ký gửi bằng ngọc giá **2.000.000** ⇒ báo lỗi; giá **500** ⇒ thành công.
5. *Lưu ý phí đăng bán (1 Thỏi vàng) bị trừ **trước** các kiểm tra này (lỗi cũ, chưa sửa trong đợt này).*

---

## 3. Bán Thỏi vàng qua gói tin được 500 triệu thay vì 37 triệu

| | |
|---|---|
| **File** | `models/shop/ShopService.java` |
| **Hàm** | `sellItem(Player pl, int where, int index)` |
| **Dòng** | ~1089–1098 (sau sửa) |

### Code trước

```java
int quantity = item.quantity;
int cost = item.template.gold;
if (item.template.id == 457) {
    quantity = 1;
} else {
    cost /= 4;
}
if (cost == 0) {
    cost = 1;
}
cost *= quantity;
```

### Code sau

```java
// FIX: Thỏi vàng chỉ được bán qua form BANSLL (37.000.000/thỏi).
// Trước đây client gửi thẳng gói bán 457 sẽ ăn template.gold = 500.000.000/thỏi.
if (item.template.id == 457) {
    Input.gI().createFormBanSLL(pl);
    return;
}
int quantity = item.quantity;
long cost = item.template.gold;
cost /= 4;
if (cost == 0) {
    cost = 1;
}
cost *= quantity;
```

### Vì sao sửa vậy

Luồng đúng: `showConfirmSellItem` thấy item 457 thì **mở form `BANSLL`** (`Input.java`), và form này mới là nơi tính giá — **37.000.000 vàng/thỏi**, có kiểm tra trần 200 tỷ.

Nhưng gói tin `7` với `action != 0` đi thẳng vào `sellItem`, và ở đó Thỏi vàng được tính bằng `template.gold` = **500.000.000**. Client tự chế bỏ qua bước xác nhận là ăn chênh **13,5 lần** mỗi thỏi.

Cách sửa: `sellItem` **không tự định giá Thỏi vàng nữa** mà bật lại form `BANSLL` giống `showConfirmSellItem`. Như vậy giá bán thỏi vàng chỉ còn **một nguồn sự thật duy nhất** (`Input.BANSLL`, 37.000.000), server không tin bất kỳ đường tắt nào từ client. Người chơi thường không thấy khác biệt gì vì họ vốn luôn đi qua form này.

Đồng thời đổi `int cost` → `long cost` để phép `cost *= quantity` không tràn số với chồng item số lượng lớn (`Util.numberToMoney` và `Inventory.LIMIT_GOLD` đều nhận `long`).

### Rủi ro còn lại

- Nếu sau này ai đó sửa giá trong `Input.BANSLL` thì phải nhớ đó là **nơi duy nhất** định giá thỏi vàng.
- `Input.java` do nhóm khác phụ trách ⇒ **không đụng** trong đợt này. Nếu nhóm đó đổi tên `createFormBanSLL` thì `ShopService` sẽ lỗi biên dịch (2 chỗ gọi: dòng ~1034 và ~1092).
- Bán thỏi vàng vẫn **không** được lưu vào danh sách mua lại (như cũ, đúng ý đồ).

### Cách test thủ công trong game

1. Mở shop bất kỳ (Bunma/Dende/Appule), bấm bán 1 chồng **Thỏi vàng** ⇒ hiện form *"Bạn muốn bán bao nhiêu [Thỏi vàng] ?"*, nhập 1 ⇒ nhận đúng **37.000.000 vàng**, mất 1 thỏi.
2. Bán một món thường (vd. Áo vải 3 lỗ, `template.gold` = 500) ⇒ nhận `500/4 = 125` vàng mỗi cái × số lượng, như cũ.
3. Nếu có công cụ giả gói tin: gửi thẳng gói `7` action ≠ 0 trỏ vào ô Thỏi vàng ⇒ chỉ được mở form, **không** cộng 500 triệu.

---

## 4. Rồng thần: điều ước vàng gán thẳng thay vì cộng thêm

| | |
|---|---|
| **File** | `models/services/shenron/SummonDragon.java` |
| **Hàm** | `confirmWish()` + hàm phụ mới `addGold(Player, long)` |
| **Dòng** | 289–299 (hàm `addGold`), 305–308 (rồng 1 sao), 451–454 (rồng 2 sao), 467–470 (rồng 3 sao) |

### Code trước

```java
// SHENRON_1_1, select 0 — nút "Giàu có +2 Tỏi Vàng"
case 0: //20 tr vàng
    this.playerSummonShenron.inventory.gold = 2000000000;

// SHENRON_2, select 2 — nút "Giàu có +200 Tr Vàng"
case 2: //2 tr vàng
    if (this.playerSummonShenron.inventory.gold > 1800000000) {
        this.playerSummonShenron.inventory.gold = Inventory.LIMIT_GOLD;
    } else {
        this.playerSummonShenron.inventory.gold += 200000000;
    }

// SHENRON_3, select 2 — nút "Giàu có +20 Tr Vàng"
case 2: //200k vàng
    if (this.playerSummonShenron.inventory.gold > (2000000000 - 20000000)) {
        this.playerSummonShenron.inventory.gold = Inventory.LIMIT_GOLD;
    } else {
        this.playerSummonShenron.inventory.gold += 20000000;
    }
```

### Code sau

```java
// FIX: cộng vàng cho điều ước, chặn tại LIMIT_GOLD thay vì gán thẳng LIMIT_GOLD
private void addGold(Player pl, long amount) {
    if (pl == null || amount <= 0) {
        return;
    }
    if (pl.inventory.gold > Inventory.LIMIT_GOLD - amount) {
        pl.inventory.gold = Inventory.LIMIT_GOLD;
    } else {
        pl.inventory.gold += amount;
    }
}

case 0: //+2 tỏi vàng
    // FIX: cộng thêm thay vì gán thẳng vàng = 2 tỷ
    addGold(this.playerSummonShenron, 2000000000L);

case 2: //+200 tr vàng
    // FIX: trước đây người có > 1,8 tỷ vàng được gán thẳng 200 tỷ; nay chỉ cộng đúng 200 triệu
    addGold(this.playerSummonShenron, 200000000L);

case 2: //+20 tr vàng
    // FIX: trước đây người có > 1,98 tỷ vàng được gán thẳng 200 tỷ; nay chỉ cộng đúng 20 triệu
    addGold(this.playerSummonShenron, 20000000L);
```

### Vì sao sửa vậy

Ba lỗi khác nhau, cùng bản chất "gán" thay vì "cộng":

1. **Rồng 1 sao, nút "+2 Tỏi Vàng"** (2 tỏi = 2 tỷ vàng): code **gán** `gold = 2.000.000.000`. Người có **hơn** 2 tỷ vàng đi ước sẽ **bị mất tiền** (vd. 50 tỷ → còn 2 tỷ).
2. **Rồng 2 sao, nút "+200 Tr Vàng"**: ai đang có > 1,8 tỷ vàng được **gán thẳng 200 tỷ** — nhận 200 tỷ từ một điều ước đáng lẽ chỉ cho 200 triệu (**hệ số 1.000 lần**).
3. **Rồng 3 sao, nút "+20 Tr Vàng"**: tương tự, ai có > 1,98 tỷ vàng được **gán thẳng 200 tỷ** (**hệ số 10.000 lần**).

Người chơi chỉ cần giữ vàng trên ngưỡng rồi gom ngọc rồng gọi rồng 3 sao (rẻ nhất, chỉ cần ngọc 3→7 sao) là chạm trần vàng 200 tỷ. Rồng thần dùng chung toàn server, mỗi 10 phút một lượt, nên đây là vòi bơm vàng thực sự.

Cách sửa: gom logic vào `addGold()` — **cộng đúng số tiền ghi trên nút**, nếu cộng xong vượt `LIMIT_GOLD` thì chặn tại `LIMIT_GOLD` (`gold > LIMIT_GOLD - amount` để phép cộng không tràn `long`). Giữ nguyên tên hàm công khai, không đổi kiến trúc.

### Rủi ro còn lại

- Người chơi đang sát trần 200 tỷ ước "+vàng" thì phần dư vẫn bị cắt — đúng luật trần vàng của game, nhưng nên nói rõ trong thông báo (chưa làm, tránh đổi text ngoài phạm vi).
- Text hướng dẫn `SUMMON_SHENRON_TUTORIAL` vẫn ghi sai ("rồng 1 sao … 20 triệu vàng", "rồng 2 sao … 2 triệu vàng", "rồng 3 sao … 200k vàng") trong khi nhãn nút và code là 2 tỷ / 200 triệu / 20 triệu. **Không sửa** vì là thay đổi nội dung game, cần chủ dự án chốt con số nào mới đúng (xem mục 10).
- Các comment cũ trong code (`//20 tr vàng`, `//2 tr vàng`, `//200k vàng`) đã được sửa lại cho khớp với hành vi thực.

### Cách test thủ công trong game

1. Dùng lệnh GM nạp vàng cho nhân vật lên **5 tỷ**.
2. Gom ngọc rồng 3→7 sao, gọi **Rồng 3 sao**, ước *"Giàu có +20 Tr Vàng"* ⇒ vàng phải là **5.000.020.000**, **không** phải 200 tỷ.
3. Nạp vàng lên **3 tỷ**, gọi **Rồng 2 sao**, ước *"Giàu có +200 Tr Vàng"* ⇒ vàng = **3.200.000.000**.
4. Nạp vàng lên **10 tỷ**, gọi **Rồng 1 sao**, ước *"Giàu có +2 Tỏi Vàng"* ⇒ vàng = **12.000.000.000** (trước đây tụt còn 2 tỷ).
5. Nạp vàng lên **199,9 tỷ**, ước "+2 Tỏi Vàng" ⇒ vàng dừng đúng ở **200.000.000.000**, không âm, không tràn.

---

## 5. Rồng thần: giảm thưởng ngọc xuống 2.000 mỗi lần ước

| | |
|---|---|
| **File** | `models/services/shenron/SummonDragon.java` |
| **Hàm** | `confirmWish()` — `SHENRON_1_2` `select == 1`; và mảng nhãn `SHENRON_1_STAR_WISHES_2` |
| **Dòng** | 64 (nhãn nút), 391–393 (code cộng ngọc) |
| **Căn cứ** | Quyết định chủ dự án — [22 — mục 0, dòng 1](22-san-sang-code.md) |

### Code trước

```java
public static final String[] SHENRON_1_STAR_WISHES_2
        = new String[]{"Đẹp trai\nnhất\nVũ trụ", "Giàu có\n+10K\nNgọc", ...};
...
case 1: //+1,5 ngọc
    this.playerSummonShenron.inventory.gem += 10000;
```

### Code sau

```java
public static final String[] SHENRON_1_STAR_WISHES_2
        = new String[]{"Đẹp trai\nnhất\nVũ trụ", "Giàu có\n+2K\nNgọc", ...};
...
case 1: //+2K ngọc
    // FIX: giảm thưởng ngọc của điều ước rồng thần xuống tối đa 2.000 ngọc/lần ước
    this.playerSummonShenron.inventory.gem += 2000;
```

### Vì sao sửa vậy

Đã rà **toàn bộ** điều ước rồng thần đang cộng ngọc (`grep "gem" models/services/shenron/*.java`):

| Rồng / nút | Trước | Sau | Ghi chú |
|---|---|---|---|
| Rồng 1 sao – trang 2, nút 1 "Giàu có +10K Ngọc" | 10.000 | **2.000** | Đã sửa; nhãn nút đổi thành "+2K Ngọc" |
| Rồng 2 sao, nút 0 "Giàu có +2K Ngọc" | 2.000 | 2.000 | **Giữ nguyên** — đã đúng mức trần |
| Rồng 3 sao, nút 0 "Giàu có +200 Ngọc" | 200 | 200 | **Giữ nguyên** — thấp hơn mức trần |

`SummonDragonNamek.java`, `Shenron_Event.java` (Rồng Băng), `Shenron_Service.java` **không có điều ước nào cộng ngọc** ⇒ không phải sửa.

Nhãn nút được đổi theo để người chơi không bị hiển thị sai ("+10K Ngọc" nhưng chỉ nhận 2K).

### Rủi ro còn lại

- Đây là thay đổi **cân bằng game**, không phải vá lỗi: người chơi quen ước rồng 1 sao lấy 10K ngọc sẽ phản ứng. Nên thông báo trước khi cập nhật server.
- Nút "Giàu có +2K Ngọc" của rồng 1 sao giờ **bằng đúng** rồng 2 sao ⇒ ước rồng 1 sao lấy ngọc không còn lợi hơn. Nếu muốn giữ chênh lệch, chủ dự án cần chốt mức mới cho rồng 2/3 sao.
- Nguồn ngọc lớn khác (điểm danh Bò Mộng 10.000 ngọc/ngày — ghi ở [16 mục 15.2](../1-he-thong-hien-tai/16-su-kien-minigame-giai-dau.md)) **chưa được đụng tới**, nằm ngoài danh sách được giao.

### Cách test thủ công trong game

1. Ghi lại số ngọc xanh hiện có.
2. Gọi **Rồng 1 sao** (cần đủ ngọc rồng 1→7 sao), chọn *"Điều ước khác"* để sang trang 2.
3. Kiểm tra nút thứ 2 hiển thị **"Giàu có +2K Ngọc"**.
4. Bấm ước ⇒ ngọc tăng đúng **2.000**.
5. Gọi **Rồng 2 sao**, ước "+2K Ngọc" ⇒ vẫn **+2.000**; **Rồng 3 sao** ước "+200 Ngọc" ⇒ vẫn **+200**.

---

## 6. Mua VIP không lưu việc trừ VND vào DB

| | |
|---|---|
| **File** | `models/npc_list/ToriBot.java` |
| **Hàm** | `BuyVip(Player pl, int vipLevel, int cost)` |
| **Dòng** | 3 (import), 131–136 |
| **Cột DB** | `account.vnd` |

### Code trước

```java
if (pl.getSession().vnd < cost) {
    Service.gI().sendThongBao(pl, "Không đủ tiền (" + (cost / 1000) + "k VND) để mua VIP!");
    return;
}

pl.getSession().vnd -= cost;
if (vipLevel > pl.vip) {
```

### Code sau

```java
if (pl.getSession().vnd < cost) {
    Service.gI().sendThongBao(pl, "Không đủ tiền (" + (cost / 1000) + "k VND) để mua VIP!");
    return;
}

// FIX: trừ VND qua PlayerDAO để ghi xuống bảng account, trước đây chỉ trừ trong session
// nên đăng nhập lại là tiền quay về (mua VIP miễn phí không giới hạn).
if (!PlayerDAO.subvnd(pl, cost)) {
    Service.gI().sendThongBao(pl, "Không thể trừ điểm mùa, vui lòng thử lại!");
    return;
}
if (vipLevel > pl.vip) {
```

(thêm `import nro.models.database.PlayerDAO;`)

### Vì sao sửa vậy

`MySession.vnd` chỉ là **bản sao trong RAM**, nạp một lần lúc đăng nhập (`MrBlue.java` dòng 76: `session.vnd = rs.getInt("vnd")`). `pl.getSession().vnd -= cost` **không hề chạm vào DB** ⇒ thoát game vào lại là số dư quay về nguyên vẹn, trong khi phần thưởng VIP (200–1000 Thỏi vàng, cải trang vĩnh viễn, pet, capsule…) **đã vào túi và được lưu**.

Mỗi vòng đăng xuất / đăng nhập là một lần mua VIP miễn phí. Bộ đếm `pl.vipPurchaseCount` (tối đa 4 lần/mùa) là hàng rào duy nhất còn lại, và nó cũng chỉ là trường của `Player`.

Server **đã có sẵn** `PlayerDAO.subvnd(Player, int)` — hàm này chạy `update account set vnd = vnd - ? where id = ?` **rồi mới** trừ `session.vnd`, đúng thứ tự an toàn, và tự kiểm tra lại số dư. Các chỗ khác trong dự án (`Input.java` dòng 158, 190 — đổi VND lấy thỏi vàng / ngọc) đã dùng đúng hàm này; `ToriBot` là chỗ duy nhất bỏ sót.

Dùng lại hàm sẵn có thay vì viết SQL mới: không đổi kiến trúc, và nếu `subvnd` trả `false` (mất kết nối DB, số dư không đủ) thì **thoát sớm, không phát thưởng**.

### Rủi ro còn lại

- **Không có transaction**: nếu server sập đúng giữa `subvnd` thành công và lúc phát item, người chơi mất VND mà không nhận quà. Xác suất rất thấp, và vá triệt để sẽ phải đổi kiến trúc DAO (ngoài phạm vi).
- `PlayerDAO.subvnd` dùng `vnd = vnd - ?` nên **an toàn khi có nhiều phiên**, nhưng có thể làm `account.vnd` **âm** nếu 2 phiên cùng tài khoản mua đồng thời (hàm chỉ so `session.vnd >= num` trong RAM). Đã có `MrBlue.java` dòng 1320–1322 kéo `vnd` âm về 0 khi đăng nhập.
- `pl.vipPurchaseCount` vẫn phụ thuộc cơ chế lưu `Player` thông thường — chưa kiểm chứng trong đợt này.
- Nếu DB lỗi, người chơi sẽ thấy thông báo *"Không thể trừ điểm mùa, vui lòng thử lại!"* — đây là text mới, cần dịch/chỉnh nếu chủ dự án muốn chữ khác.

### Cách test thủ công trong game

1. Chọn 1 tài khoản, đặt `account.vnd = 200000` bằng SQL.
2. Vào game, gặp NPC **ToriBot**, mua **VIP 1** (50.000 điểm mùa).
3. Kiểm tra SQL: `select vnd from account where id = <id>` ⇒ phải là **150000**.
4. Mở lại menu ToriBot ⇒ số hiển thị trong ngoặc phải là **150000**.
5. **Thoát game, đăng nhập lại**, mở NPC ⇒ vẫn **150000** (trước đây quay về 200000).
6. Đặt `vnd = 10000` rồi thử mua VIP 1 ⇒ báo *"Không đủ tiền (50k VND)…"*, không nhận quà, `vnd` không đổi.

---

## 7. Con số may mắn nhân thưởng theo số vé đã mua

| | |
|---|---|
| **File** | `models/minigame/ConSoMayManGold.java` và `models/minigame/ConSoMayManGem.java` |
| **Hàm** | `ResetGame(int result)` |
| **Dòng** | 3–6 và 239–244 (bản Vàng); 3–6 và 230–235 (bản Ngọc) |

### Code trước (giống nhau ở cả 2 file)

```java
for (ConSoMayManData g : players) {
    Player player = Client.gI().getPlayer(g.id);
    if (player != null) {
        Service.gI().showYourNumber(player, "", result + "", strFinish(g.id), 1);
    }

    if (g.point == result) {
        ...
    }
}
```

### Code sau

```java
// FIX: trước đây strFinish(g.id) được gọi cho TỪNG vé => người trúng có N vé nhận thưởng N lần.
// Mỗi người chỉ được trả thưởng/thông báo đúng 1 lần trong 1 ván.
Set<Integer> rewarded = new HashSet<>();
for (ConSoMayManData g : players) {
    Player player = Client.gI().getPlayer(g.id);
    if (player != null && rewarded.add(g.id)) {
        Service.gI().showYourNumber(player, "", result + "", strFinish(g.id), 1);
    }

    if (g.point == result) {
        ...
    }
}
```

(thêm `import java.util.HashSet;` và `import java.util.Set;` ở cả 2 file)

### Vì sao sửa vậy

`players` là danh sách **vé**, không phải danh sách người chơi. Vòng lặp chạy qua **từng vé**; mỗi vé của người X đều gọi `strFinish(X)`. Mà `strFinish(id)` lại tự đi tìm vé trúng của id đó **và cộng thưởng ngay tại chỗ** (`addItemBag` 90 Thỏi vàng / `gem += 450`). Kết quả: người mua **N** vé mà trúng sẽ nhận thưởng **N lần** — không phụ thuộc trúng mấy vé.

Luật chơi cho phép tối đa **10 số/người/ván** và **không được trùng số** ⇒ một người chỉ có thể trúng **tối đa 1 vé**, nên trả thưởng đúng **một lần mỗi người mỗi ván** là đúng luật.

Với luật hiện tại, mua đủ 10 số (10 thỏi vàng) có xác suất trúng 10/101, nhận 10 × 90 = **900 thỏi vàng** ⇒ kỳ vọng ≈ **89 thỏi cho 10 thỏi bỏ ra** (bản ngọc: 4.500 ngọc cho 50 ngọc). Sau khi sửa, kỳ vọng trở về đúng thiết kế: `90 × 10/101 ≈ 8,9 thỏi` cho 10 thỏi ⇒ thuế nhà cái ~10,9%.

Cách sửa dùng `Set.add()` trả `false` khi id đã có — vừa chặn nhân thưởng, vừa hết luôn việc người chơi bị bắn **10 popup kết quả** liên tiếp. Không đổi chữ ký `strFinish` (vẫn `public String strFinish(int id)`), không đổi kiến trúc; phần gom `result_name` (danh sách người thắng) vẫn nằm **ngoài** điều kiện nên không bị ảnh hưởng — quan trọng, vì vé trúng của một người có thể nằm ở vị trí bất kỳ trong danh sách.

### Rủi ro còn lại

- **Không chặn được tận gốc**: `strFinish` vẫn là hàm `public` và vẫn tự cộng thưởng. Nếu sau này có chỗ khác gọi nó, lỗi sẽ quay lại. Sửa triệt để là tách phần trả thưởng ra khỏi hàm sinh chuỗi — đã **không làm** để giữ chữ ký hàm công khai theo yêu cầu.
- Chỉ người **đang online** mới nhận thưởng; người offline chỉ được xướng tên (lỗi cũ, không nằm trong danh sách được giao).
- NPC **Lý Tiểu Nương** (npc 54) hiện **không có trong `map_template`** ⇒ minigame thực tế chưa mở được. Bản vá này là để an toàn khi bật NPC lên.
- `dataKQ_CSMM` vẫn phình vô hạn (~1.440 phần tử/ngày mỗi bản) — lỗi rò bộ nhớ, ngoài phạm vi.

### Cách test thủ công trong game

*Phải mở được NPC Lý Tiểu Nương trước (thêm npc 54 vào `map_template`).*

1. Chuẩn bị 1 nhân vật có **20 Thỏi vàng**, ghi lại số lượng.
2. Vào **Con số may mắn (Vàng)**, chọn thủ công **10 số khác nhau** (mất 10 thỏi).
3. Chờ hết ván (~60 giây), xem kết quả.
4. Nếu trúng ⇒ nhận đúng **90 Thỏi vàng** (trước đây nhận 900) và chỉ hiện **1** popup kết quả.
5. Nếu trượt ⇒ không nhận gì, chỉ 1 popup.
6. Lặp lại với bản **Ngọc** (5 ngọc/số, thưởng **450 ngọc** đúng 1 lần).

---

## 8. Thách đấu PVP không trừ tiền cược lúc bắt đầu trận

| | |
|---|---|
| **File** | `models/matches/ThachDau.java` |
| **Hàm** | constructor, `start()`, `dispose()`, `reward()`, `sendResult()` + 3 hàm phụ mới |
| **Dòng** | 7 (import), 17–19 (cờ), 21–28 (constructor), 30–60 (hàm phụ), 62–66 (`start`), 73–82 (`dispose`), 88–95 (`reward`), 107 và 111 (`sendResult`) |

### Code trước

```java
private int goldThachDau;
private long goldReward;

public ThachDau(Player p1, Player p2, int goldThachDau) {
    super(TYPE_PVP.THACH_DAU, p1, p2);
    this.goldThachDau = goldThachDau;
    this.goldReward = goldThachDau / 100 * 80;
}

@Override
public void start() {
    this.p1.inventory.gold -= this.goldThachDau;
    this.p2.inventory.gold -= this.goldThachDau;
    Service.gI().sendMoney(this.p1);
    Service.gI().sendMoney(this.p2);
    super.start();
}

@Override
public void dispose() {
    super.dispose();
}

@Override
public void reward(Player plWin) {
    plWin.inventory.gold += this.goldReward;
    Service.gI().sendMoney(plWin);
}

@Override
public void sendResult(Player plLose, TYPE_LOSE_PVP typeLose) {
    ...
        Service.gI().sendThongBao(..., "Bạn bị xử thua vì đã bỏ chạy");
        (p1.equals(plLose) ? p1 : p2).inventory.gold -= this.goldThachDau;
    } else if (typeLose == TYPE_LOSE_PVP.DEAD) {
        ...
        (p1.equals(plLose) ? p1 : p2).inventory.gold -= this.goldThachDau;
    }
```

### Code sau

```java
private int goldThachDau;
private long goldReward;
// FIX: cờ theo dõi tiền cược đã được giữ / đã được quyết toán
private boolean goldTaken;
private boolean settled;

public ThachDau(Player p1, Player p2, int goldThachDau) {
    super(TYPE_PVP.THACH_DAU, p1, p2);
    this.goldThachDau = goldThachDau;
    this.goldReward = goldThachDau / 100 * 80;
    // FIX: super(...) gọi start() TRƯỚC khi goldThachDau được gán nên trước đây luôn trừ 0 vàng.
    // Trừ tiền cược của cả hai ngay tại đây, sau khi các trường đã có giá trị.
    takeGold();
}

private void takeGold() {
    if (this.goldTaken || this.goldThachDau <= 0) {
        return;
    }
    subGold(this.p1);
    subGold(this.p2);
    this.goldTaken = true;
}

private void subGold(Player pl) {
    if (pl == null || pl.inventory == null) {
        return;
    }
    pl.inventory.gold -= this.goldThachDau;
    if (pl.inventory.gold < 0) {
        pl.inventory.gold = 0;
    }
    Service.gI().sendMoney(pl);
}

private void addGold(Player pl, long amount) {
    if (pl == null || pl.inventory == null || amount <= 0) {
        return;
    }
    if (pl.inventory.gold > Inventory.LIMIT_GOLD - amount) {
        pl.inventory.gold = Inventory.LIMIT_GOLD;
    } else {
        pl.inventory.gold += amount;
    }
    Service.gI().sendMoney(pl);
}

@Override
public void start() {
    // FIX: không trừ cược ở đây nữa (xem takeGold() trong constructor)
    super.start();
}

@Override
public void dispose() {
    // FIX: trận kết thúc mà chưa quyết toán (huỷ / một bên rời game) => hoàn lại tiền cược cho cả hai
    if (this.goldTaken && !this.settled) {
        this.settled = true;
        addGold(this.p1, this.goldThachDau);
        addGold(this.p2, this.goldThachDau);
    }
    super.dispose();
}

@Override
public void reward(Player plWin) {
    // FIX: hoàn lại tiền cược người thắng đã đặt + 80% cược lấy từ tiền người thua đã đặt.
    // Kết quả ròng vẫn như cũ (thắng +80% cược, thua −100% cược) nhưng tiền lấy từ quỹ cược
    // chứ không sinh ra từ không khí.
    this.settled = true;
    addGold(plWin, (this.goldTaken ? this.goldThachDau : 0) + this.goldReward);
}

// trong sendResult: bỏ 2 dòng trừ vàng lần 2
    Service.gI().sendThongBao(..., "Bạn bị xử thua vì đã bỏ chạy");
    // FIX: bỏ trừ lần 2 - tiền cược đã bị giữ từ lúc bắt đầu trận
```

(thêm `import nro.models.player.Inventory;`)

### Vì sao sửa vậy

**Gốc lỗi là thứ tự khởi tạo của Java.** `PVP` (lớp cha) gọi `this.start()` **ngay trong constructor** (`PVP.java` dòng 25). Khi đó `ThachDau.start()` đã được gọi nhưng dòng `this.goldThachDau = goldThachDau;` ở lớp con **chưa chạy** ⇒ `goldThachDau` vẫn là **0** ⇒ `start()` trừ đúng **0 vàng** của cả hai người.

Hệ quả dòng tiền trước khi sửa: thắng **+80%** cược (vàng **sinh ra từ không khí**), thua **−100%** cược. Vì `PVPService.acceptPVP` chỉ kiểm tra cả hai **đủ** vàng chứ không giữ tiền, hai tài khoản cùng chủ có thể so kè liên tục và mỗi trận vẫn có dòng vàng mới xuất hiện ở tay người thắng.

Cách sửa gồm 3 phần:

1. **Giữ cược đúng lúc**: chuyển việc trừ vàng từ `start()` sang cuối constructor (`takeGold()`), tức là **sau khi** các trường đã có giá trị. Giữ `start()` override để không đổi cấu trúc lớp, chỉ còn gọi `super.start()`.
2. **Quyết toán từ quỹ cược**: `reward()` trả cho người thắng **cược của chính họ + 80% cược** (lấy từ tiền người thua đã nộp), và `sendResult()` **bỏ** dòng trừ lần 2 của người thua (họ đã nộp từ đầu trận). **Kết quả ròng của người chơi không đổi** so với trước: thắng +80% cược, thua −100% cược. Khác biệt duy nhất là 80% đó nay **lấy từ quỹ**, và 20% còn lại trở thành **cửa thoát vàng** (gold sink) thay vì vàng mới.
3. **Hoàn cược khi huỷ**: `dispose()` được gọi trực tiếp (không qua `lose()`) ở `Player.dispose()` dòng 1764 khi một bên rời game/đăng xuất. Trước đây không có gì để hoàn vì cũng chẳng trừ gì. Nay thêm cờ `settled` — nếu trận kết thúc mà **chưa** quyết toán thì **trả lại đủ cược cho cả hai**.

Cờ `goldTaken` / `settled` đảm bảo không trừ 2 lần, không hoàn 2 lần. `subGold` kẹp sàn 0, `addGold` kẹp trần `LIMIT_GOLD` để không tràn số.

Thứ tự trong `PVP.lose()` là `finish() → reward() → sendResult() → dispose()`; `reward()` đặt `settled = true` trước nên `dispose()` sau đó **không** hoàn cược nữa.

### Rủi ro còn lại

- **Trường hợp đăng xuất giữa trận**: `Player.dispose()` hoàn cược vào `inventory.gold` của đối tượng `Player` **đang bị huỷ**. Nếu dữ liệu đã được lưu xuống DB **trước** bước này thì người rời game **mất cược** (đối thủ vẫn được hoàn đúng). Chưa kiểm chứng được thứ tự lưu/huỷ vì không build được. **Cần xác nhận khi test.**
- Đổi map giữa trận đi qua `ChangeMapService` dòng 781–782 → `lose(RUNS_AWAY)` ⇒ đi đường quyết toán bình thường, không phải đường hoàn cược.
- `goldReward = goldThachDau / 100 * 80` là phép chia nguyên: mức cược 1.000.000 / 10.000.000 / 100.000.000 đều chia hết cho 100 nên không sai lệch.
- Khoảng thời gian rất ngắn giữa `super(...)` (đã gán `p1.pvp`, đã đổi cờ PK) và `takeGold()` về lý thuyết có thể bị xen kẽ bởi thread khác. Rủi ro thực tế gần như bằng 0 và sửa triệt để sẽ phải đổi kiến trúc `PVP` (lớp cha dùng chung với ĐHVT, Siêu hạng, Võ đài) — **cố tình không đụng**.

### Cách test thủ công trong game

1. Hai nhân vật A và B cùng map, mỗi người có đúng **20.000.000 vàng**. Ghi lại tổng = 40.000.000.
2. A thách đấu B mức cược **10.000.000**, B đồng ý.
3. **Ngay khi trận bắt đầu**: cả hai phải còn **10.000.000 vàng** (trước đây vẫn nguyên 20.000.000).
4. Đánh cho B chết ⇒ A nhận **10.000.000 + 8.000.000 = 18.000.000** vàng; B còn **10.000.000**.
5. Tổng cuối = **28.000.000** (< 40.000.000) ⇒ **vàng bị tiêu bớt**, không sinh thêm. Trước khi sửa tổng sẽ là **38.000.000** với 8.000.000 vàng mới xuất hiện ở tay A.
6. **Test hoàn cược**: lặp lại bước 1–3, rồi cho **B thoát game** (đăng xuất, không đổi map). Kiểm tra A được hoàn **10.000.000** về đủ 20.000.000. Đăng nhập lại B, kiểm tra vàng của B (xem mục rủi ro ở trên).
7. **Test đổi map**: lặp lại bước 1–3 rồi cho B **đổi map** ⇒ B bị xử thua bỏ chạy, A nhận 18.000.000 như bước 4.
8. Thử mức cược **1.000.000** và **100.000.000** để chắc con số 80% đúng.

---

## 9. Tổng hợp file đã đụng

| File | Lỗi | Số dòng thay đổi |
|---|---|---|
| `models/shop_ky_gui/ConsignShopService.java` | 1, 2 | +19 / −7 |
| `models/shop/ShopService.java` | 3 | +7 / −5 |
| `models/services/shenron/SummonDragon.java` | 4, 5 | +25 / −17 |
| `models/npc_list/ToriBot.java` | 6 | +7 / −1 |
| `models/minigame/ConSoMayManGold.java` | 7 | +6 / −1 |
| `models/minigame/ConSoMayManGem.java` | 7 | +6 / −1 |
| `models/matches/ThachDau.java` | 8 | +53 / −8 |

**Không đụng** (thuộc nhóm khác): `boss/**`, `npc_list/Osin.java`, `npc_list/DaiThienSu.java`, `services/PlayerService.java`, `server/Controller.java`, `services_func/Input.java`, `services/Service.java`.

**Import mới thêm**

| File | Import |
|---|---|
| `ConsignShopService.java` | `nro.models.player.Inventory` |
| `SummonDragon.java` | (không — `Inventory` đã có sẵn) |
| `ToriBot.java` | `nro.models.database.PlayerDAO` |
| `ConSoMayManGold.java`, `ConSoMayManGem.java` | `java.util.HashSet`, `java.util.Set` |
| `ThachDau.java` | `nro.models.player.Inventory` |

> Hai file `ConSoMayMan*.java` dùng **CRLF**; đã giữ nguyên định dạng xuống dòng gốc.

---

## 10. Điểm cần chủ dự án xác nhận

| # | Vấn đề | Đề xuất |
|---|---|---|
| 1 | **Trần giá ký gửi bằng vàng** — hằng số gốc trong code là 100.000 (quá thấp), đã đặt **200.000.000** theo text NPC | Chốt con số; sửa `MAX_GOLD_CONSIGN` ở đầu `ConsignShopService.java` |
| 2 | **Dọn hậu quả lỗi #1** — vàng/thỏi vàng đã bị bơm ra từ lỗi ký gửi vẫn nằm trong DB | Rà `shop_ky_gui` (tin `isBuy = 1` chờ nhận tiền) và số Thỏi vàng bất thường trong `player.items_bag`; cân nhắc reset |
| 3 | **Text hướng dẫn rồng thần sai** (`SUMMON_SHENRON_TUTORIAL` ghi 20 triệu / 2 triệu / 200k vàng, thực tế 2 tỷ / 200 triệu / 20 triệu) | Chốt: sửa text theo code, hay hạ phần thưởng theo text? |
| 4 | **Chênh lệch thưởng ngọc giữa các rồng bị mất** — sau khi hạ xuống 2.000, rồng 1 sao và rồng 2 sao cho ngọc bằng nhau | Có muốn hạ tiếp rồng 2 sao xuống < 2.000 không? |
| 5 | **Hoàn cược PVP khi đăng xuất giữa trận** — chưa chắc vàng hoàn có kịp lưu vào DB | Xác nhận khi test (mục 8, bước 6) |
| 6 | **Chưa biên dịch** — máy thi công không có JDK | Chạy `ant`/`javac` trước khi lên server |

