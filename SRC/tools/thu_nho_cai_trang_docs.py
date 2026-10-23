#!/usr/bin/env python3
"""Thu 7 cải trang của patch 94 cho cao BẰNG "Cải trang Hắc Y Song Đao", giữ nguyên tỉ lệ gốc.

Dùng:
    /usr/bin/python3 tools/thu_nho_cai_trang_docs.py            # chỉ in báo cáo
    /usr/bin/python3 tools/thu_nho_cai_trang_docs.py --apply    # ghi ảnh, sửa patch 94/96, ghi patch mới

Ảnh Vương Lâm và 6 bộ x4 là ảnh HD, vào game to gần gấp đôi; Hắc Y Song Đao là ảnh chuẩn của game.
Mỗi bộ dùng MỘT hệ số cho cả đầu / thân / chân (chủ dự án chốt: không thu riêng từng phần):

  1. Hệ số = chiều cao dáng đứng (đầu + thân + chân, như client ghép) của Hắc Y / của bộ đó, nhân thêm
     PHONG (1.15): chiều cao trên tính cả tóc dựng + hào quang nên bằng đúng thì người trông nhỏ hơn Hắc Y.
  2. Thu mọi mảnh theo hệ số đó; dx/dy thu quanh điểm chân nhân vật: d' = s*(CI + d) - CI, với CI là
     độ lệch của từng loại mảnh ở dáng đứng (đầu -13,34 / thân -9,16 / chân -8,10). Cách ghép giữa
     đầu, thân, chân giữ y bản gốc.
  2b. Hạ / nâng CẢ BỘ (mọi mảnh, mọi khung) cho đáy chân dáng đứng chạm đất như Hắc Y; đo bằng đáy
     phần đặc (alpha > 250) của khung chân rồi cộng CHINH_DAT (bộ có hào quang dưới chân).
  2c. Hạ đầu cho khỏi lơ lửng: ghép các khung 0..7 (đứng, chạy, nhảy) theo bảng CharInfo của client,
     đo khe giữa đáy đầu và mép trên thân ngay bên dưới; mảnh đầu nào còn hở (khe > 0) ở khung dùng
     nó thì hạ mảnh đó đến khi hết hở. Bảng CHARINFO dưới đây đã thử trên Hắc Y + Goku SSJ4 (dx/dy
     gốc) ghép liền mạch; khung 8 trở đi chưa chắc nên không dùng.
  3. Luôn đọc ảnh HD gốc + dx/dy gốc từ commit GOC (trước mọi lần thu), nên chạy lại vẫn ra cùng kết quả.
  4. Ảnh thu mang SỐ ICON MỚI: server gửi phiên bản ảnh nhỏ toàn 0 (DataGame.sendSmallVersion) nên
     client không biết ảnh đổi, giữ số cũ thì ai đã vào game vẫn thấy ảnh cũ trong cache.
     Ảnh của lần thu trước bị xoá khỏi data/icon và docs.
"""

import io
import os
import re
import subprocess
import sys

import numpy as np
from PIL import Image

HERE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))          # .../SRC
ROOT = os.path.dirname(HERE)
sys.path.insert(0, os.path.join(HERE, "tools"))

from port_cai_trang_tu_docs import TRANSPARENT, MAX_ICON_ID   # noqa: E402

GOC = "2027d8c02"              # commit mang 8 cải trang vào, ảnh HD chưa thu
OUR_ICON = HERE + "/data/icon"
PATCH_DIR = HERE + "/sql/patch"
P94 = PATCH_DIR + "/94-cai-trang-tu-docs.sql"
P96 = PATCH_DIR + "/96-thu-nho-cai-trang-tu-docs.sql"
P97 = PATCH_DIR + "/97-cai-trang-cao-bang-hac-y.sql"
P98 = PATCH_DIR + "/98-cai-trang-to-them-cham-dat.sql"
P99 = PATCH_DIR + "/99-vuong-lam-kiem-si-to-them.sql"
P100 = PATCH_DIR + "/100-ha-dau-cai-trang-tu-docs.sql"
P101 = PATCH_DIR + "/101-chinh-dau-theo-chu-du-an.sql"
PATCH_CU = (P96, P97, P98, P99, P100)               # các patch UPDATE trước, sửa theo bản cuối
PHONG = 1.15
# Vương Lâm thân hẹp + tóc dài, Kiếm Sĩ có kiếm + sấm tính vào chiều cao -> người nhỏ hơn các bộ khác
PHONG_RIENG = {2291: 1.2, 2292: 1.15}
CHINH_DAT = {2293: 1, 2294: 1}           # hạ thêm (đơn vị x1)
# Chủ dự án xem hình rồi chỉnh tay đầu (cả 2 mảnh đầu), cộng thêm SAU bước tự hạ đầu: (dx, dy), dx âm = sang
# trái, dy dương = hạ xuống. Patch 101.
CHINH_DAU = {
    2291: (0, 3),       # Vương Lâm: hạ 3
    2292: (-1, 1),      # Kiếm Sĩ: hạ 1, sang trái 1
    2293: (1, -1),      # Huyết Nhẫn: lên 1, sang phải 1
    2294: (1, -1),      # Lam Hồn: lên 1, sang phải 1
    2295: (0, 1),       # Tử Ma: hạ 1
}
DATAGAME = HERE + "/src/nro/models/data/DataGame.java"
ZOOMS = (1, 2, 3, 4)
MAU = 2661                     # part đầu của Hắc Y Song Đao
CI = {0: (0, -13, 34), 1: (1, -9, 16), 2: (1, -8, 10)}   # dáng đứng: (mảnh, x, y) của đầu / thân / chân
# Khung 0..7 của Char.CharInfo bên client: [đầu, chân, thân] = (mảnh, x, y), vẽ tại (x + dx, -y + dy)
CHARINFO = [
    [(0, -13, 34), (1, -8, 10), (1, -9, 16)],
    [(0, -13, 35), (1, -8, 10), (1, -9, 17)],
    [(1, -10, 33), (2, -10, 11), (2, -8, 16)],
    [(1, -10, 32), (3, -11, 9), (3, -11, 16)],
    [(1, -10, 34), (4, -9, 9), (4, -8, 16)],
    [(1, -10, 34), (5, -11, 11), (5, -10, 17)],
    [(1, -10, 33), (6, -9, 9), (6, -8, 16)],
    [(0, -9, 36), (7, -5, 17), (7, -11, 25)],
]
DOCS = ROOT + "/docs/"
# id vật phẩm, part đầu, thư mục docs
BO = [
    (2291, 2664, DOCS + "vương lâm (tao)/"),
    (2292, 2667, DOCS + "x4/"),
    (2293, 2670, DOCS + "x4/"),
    (2294, 2673, DOCS + "x4/"),
    (2295, 2676, DOCS + "x4/"),
    (2296, 2679, DOCS + "x4/"),
    (2297, 2682, DOCS + "x4/"),
]


def git_show(path):
    return subprocess.run(["git", "show", "%s:%s" % (GOC, path)], cwd=ROOT,
                          capture_output=True, check=True).stdout


def doc_part(txt):
    return {int(i): [[int(a), int(b), int(c)] for a, b, c in re.findall(r"\[(-?\d+),(-?\d+),(-?\d+)\]", d)]
            for i, d in re.findall(r"^\((\d+), \d, '(\[.*?\])'\)", txt, re.M)}


_anh = {}


def anh_goc(i):
    if i not in _anh:
        _anh[i] = Image.open(io.BytesIO(git_show("SRC/data/icon/x4/%d.png" % i))).convert("RGBA")
    return _anh[i]


def chieu_cao(parts, p0):
    """Chiều cao (px x4) dáng đứng ghép đủ đầu + thân + chân."""
    can = Image.new("RGBA", (1600, 1600))
    for t in (0, 2, 1):
        idx, x, y = CI[t]
        ic, dx, dy = parts[p0 + t][idx]
        can.alpha_composite(anh_goc(ic), (800 + (x + dx) * 4, 1300 + (-y + dy) * 4))
    ys = np.nonzero((np.asarray(can)[:, :, 3] > 60).any(1))[0]
    return ys.max() - ys.min() + 1


def khe_co(head, body, hims, bims, cf):
    """Khe (px x4) giữa đáy đầu và mép trên thân ở khung cf; dương = đầu lơ lửng."""
    (hi, hx, hy), _, (bi, bx, by) = CHARINFO[cf]
    a = np.asarray(hims[hi])[:, :, 3] > 200
    b = np.asarray(bims[bi])[:, :, 3] > 200
    ax, ay = (hx + head[hi][1]) * 4, (-hy + head[hi][2]) * 4
    bx_, by_ = (bx + body[bi][1]) * 4, (-by + body[bi][2]) * 4
    cols = np.nonzero(a.any(0))[0]
    w = cols.max() - cols.min()
    vals = []
    for c in range(int(cols.min() + .3 * w), int(cols.max() - .3 * w) + 1):
        hc = np.nonzero(a[:, c])[0]
        cb = c + ax - bx_
        if len(hc) == 0 or not 0 <= cb < b.shape[1]:
            continue
        bc = np.nonzero(b[:, cb])[0]
        if len(bc):
            vals.append((by_ + bc.min()) - (ay + hc.max()))
    return int(np.median(vals)) if vals else None


def ha_dau(moi, anh_moi, n_dau, n_than):
    """Hạ từng mảnh đầu cho hết hở ở các khung dùng nó (đơn vị x1). Trả về số hạ của mảnh 0, 1."""
    hims = [im for _, im in anh_moi[:n_dau]]
    bims = [im for _, im in anh_moi[n_dau:n_dau + n_than]]
    ha = []
    for i in range(n_dau):
        khe = [khe_co(moi[0], moi[1], hims, bims, cf) for cf in range(len(CHARINFO)) if CHARINFO[cf][0][0] == i]
        khe = [k for k in khe if k is not None]
        d = max(0, -(-max(khe) // 4)) if khe else 0
        moi[0][i][2] += d
        ha.append(d)
    return ha


def thu(im, f):
    return im.resize((max(1, round(im.width * f)), max(1, round(im.height * f))), Image.LANCZOS)


def main(argv):
    apply = "--apply" in argv
    goc = doc_part(git_show("SRC/sql/patch/94-cai-trang-tu-docs.sql").decode("utf-8"))
    hien = doc_part(open(P94, encoding="utf-8").read())
    h_mau = chieu_cao(goc, MAU)

    taken = set()
    for z in ZOOMS:
        taken |= {int(f[:-4]) for f in os.listdir(OUR_ICON + "/x%d" % z) if f[:-4].isdigit()}
    from port_item_from_sumo import our_data
    taken |= our_data()[1]
    # số từng mang ảnh của các bộ này (bản HD đầu tiên, bản thu lần trước): client có thể còn cache,
    # cấp lại là hiện ảnh cũ
    taken |= {ic for p in goc.values() for ic, _, _ in p}
    taken |= {ic for p in hien.values() for ic, _, _ in p}
    taken |= set(range(20257, 21587))

    def cap_dai(n):
        run = 0
        for i in range(20000, MAX_ICON_ID + 1):
            run = run + 1 if i not in taken else 0
            if run == n:
                taken.update(range(i - n + 1, i + 1))
                return i - n + 1
        raise SystemExit("Hết dải icon trống")

    ket_qua = []           # (item, part đầu, hệ số, [3 part mới], [(số mới, ảnh x4)], [số đang dùng], thư mục)
    for item, p0, folder in BO:
        s = h_mau / chieu_cao(goc, p0) * PHONG * PHONG_RIENG.get(item, 1)
        cu = [[p for p in goc[p0 + t] if p[0] != TRANSPARENT] for t in range(3)]
        base, moi, anh_moi, k = 0, [], [], 0
        for t in range(3):
            _, x, y = CI[t]
            pcs = []
            for ic, dx, dy in cu[t]:
                pcs.append([base + k, round(s * (x + dx) - x), round(s * (-y + dy) + y)])
                anh_moi.append((base + k, thu(anh_goc(ic), s)))
                k += 1
            moi.append(pcs)
        # chạm đất: đáy phần đặc của khung chân đứng (mảnh 1) về y = 0
        _, x, y = CI[2]
        ic, dx, dy = moi[2][CI[2][0]]
        a = np.asarray(anh_moi[len(cu[0]) + len(cu[1]) + CI[2][0]][1])[:, :, 3] > 250
        day = (-y + dy) * 4 + np.nonzero(a.any(1))[0].max()
        ha = -round(day / 4) + CHINH_DAT.get(item, 0)
        moi = [[[i, dx, dy + ha] for i, dx, dy in pcs] for pcs in moi]
        hd = ha_dau(moi, anh_moi, len(cu[0]), len(cu[1]))
        cx_, cy_ = CHINH_DAU.get(item, (0, 0))
        for pc in moi[0]:
            pc[1] += cx_
            pc[2] += cy_
        moi = [pcs + [[TRANSPARENT, 0, 0]] for pcs in moi]
        dang_dung = [ic for t in range(3) for ic, _, _ in hien[p0 + t] if ic != TRANSPARENT]
        geo = lambda ps: [[d[1:] for d in p] for p in ps]
        # Thân + chân y hệt bản đang dùng -> cùng hệ số, ảnh không đổi: giữ nguyên số icon, chỉ sửa dx/dy
        if geo(moi)[1:] == geo([hien[p0 + 1], hien[p0 + 2]]) and len(moi[0]) == len(hien[p0]):
            moi = [[[h[0], d[1], d[2]] for h, d in zip(hien[p0 + t], moi[t])] for t in range(3)]
            if moi == [hien[p0 + t] for t in range(3)]:
                print("  %d  part %d-%d  hệ số %.2f  hạ %+d  hạ đầu %s  (không đổi, giữ nguyên)"
                      % (item, p0, p0 + 2, s, ha, hd))
                continue
            ket_qua.append((item, p0, s, moi, [], [], folder))
            print("  %d  part %d-%d  hệ số %.2f  hạ %+d  hạ đầu %s  (chỉ sửa dx/dy, giữ số icon)"
                  % (item, p0, p0 + 2, s, ha, hd))
            continue
        base = cap_dai(k)
        moi = [[[i + base if i != TRANSPARENT else i, dx, dy] for i, dx, dy in pcs] for pcs in moi]
        anh_moi = [(i + base, im) for i, im in anh_moi]
        ket_qua.append((item, p0, s, moi, anh_moi, dang_dung, folder))
        print("  %d  part %d-%d  hệ số %.2f  hạ %+d  hạ đầu %s  icon mới %d..%d"
              % (item, p0, p0 + 2, s, ha, hd, base, base + k - 1))

    vs_data = int(re.search(r"public static byte vsData = (\d+);",
                            open(DATAGAME, encoding="utf-8").read()).group(1)) + 1
    print("vsData -> %d" % vs_data)
    if not apply:
        print("\n(chạy lại với --apply để ghi)")
        return 0

    for _, _, _, _, anh_moi, dang_dung, folder in ket_qua:
        for ic in dang_dung:                       # ảnh của lần thu trước
            for z in ZOOMS:
                p = OUR_ICON + "/x%d/%d.png" % (z, ic)
                if os.path.exists(p):
                    os.remove(p)
            if os.path.exists(folder + "%d.png" % ic):
                os.remove(folder + "%d.png" % ic)
        for new, im4 in anh_moi:
            for z in ZOOMS:
                (im4 if z == 4 else thu(im4, z / 4)).save(OUR_ICON + "/x%d/%d.png" % (z, new), optimize=True)
            im4.save(folder + "%d.png" % new, optimize=True)

    def data(pcs):
        return "[" + ",".join("[%d,%d,%d]" % tuple(p) for p in pcs) + "]"

    if not ket_qua:
        print("Không bộ nào đổi.")
        return 0

    # Patch 94 (dòng INSERT) và các patch UPDATE trước sửa theo -> chạy lại patch nào cũng ra bản cuối
    t94 = open(P94, encoding="utf-8").read()
    cu_txt = {f: open(f, encoding="utf-8").read() for f in PATCH_CU}
    for _, p0, _, moi, _, _, _ in ket_qua:
        for t in range(3):
            t94, n = re.subn(r"^\(%d, %d, '\[.*?\]'\)" % (p0 + t, t),
                             "(%d, %d, '%s')" % (p0 + t, t, data(moi[t])), t94, flags=re.M)
            assert n == 1, p0 + t
            for f in PATCH_CU:
                cu_txt[f], n = re.subn(r"SET `DATA` = '\[.*?\]' WHERE `id` = %d AND" % (p0 + t),
                                       "SET `DATA` = '%s' WHERE `id` = %d AND" % (data(moi[t]), p0 + t), cu_txt[f])
                assert n <= 1, (f, p0 + t)
    t94 = t94.replace("-- * Patch 98, 99 thu 7 bộ không có dx/dy theo cỡ Hắc Y Song Đao, chạm đất; part dưới đây\n",
                      "-- * Patch 98, 99, 100 thu 7 bộ không có dx/dy theo cỡ Hắc Y, chạm đất, liền cổ; part dưới đây\n")
    for f in PATCH_CU:
        if "patch 101" not in cu_txt[f]:
            cu_txt[f] = cu_txt[f].replace(
                "-- Ảnh ", "-- Patch 101 chỉnh tay đầu 5 bộ; các dòng dưới đây đã sửa theo bản 101.\n-- Ảnh ", 1)
    open(P94, "w", encoding="utf-8").write(t94)
    for f in PATCH_CU:
        open(f, "w", encoding="utf-8").write(cu_txt[f])

    with open(P101, "w", encoding="utf-8") as f:
        f.write(HEADER % dict(vd=vs_data, ds="\n".join(
            "--   %d  part %d-%d  hệ số %.2f" % (it, p0, p0 + 2, s) for it, p0, s, _, _, _, _ in ket_qua)))
        for _, p0, _, moi, _, _, _ in ket_qua:
            for t in range(3):
                f.write("UPDATE `part` SET `DATA` = '%s' WHERE `id` = %d AND `TYPE` = %d;\n"
                        % (data(moi[t]), p0 + t, t))
        f.write("\nSELECT `id`, LEFT(`DATA`, 60) FROM `part` WHERE `id` IN (%s);\n"
                % ", ".join(str(p0 + t) for _, p0, _, _, _, _, _ in ket_qua for t in range(3)))
    print("Đã ghi ảnh, sửa patch 94 / 96–100, ghi patch 101.")
    return 0


HEADER = """-- =====================================================================
-- 101 — CHỈNH ĐẦU 5 CẢI TRANG THEO CHỦ DỰ ÁN (2026-10-23)
--      (sinh bởi tools/thu_nho_cai_trang_docs.py)
-- =====================================================================
-- Chỉnh cả 2 mảnh đầu (mọi động tác), cộng thêm sau bước tự hạ đầu của patch 100:
--   2291 Vương Lâm   : hạ 3
--   2292 Kiếm Sĩ     : hạ 1, sang trái 1
--   2293 Huyết Nhẫn  : lên 1, sang phải 1
--   2294 Lam Hồn     : lên 1, sang phải 1
--   2295 Tử Ma       : hạ 1
-- Chỉ đổi dx/dy của đầu; ảnh và số icon giữ nguyên. Hệ số thu (không đổi):
%(ds)s
--
-- Chạy SAU patch 94 (94, 96–100 cũng đã sửa theo -> chạy thứ tự nào cũng ra kết quả này).
-- Cùng jar có DataGame.vsData = %(vd)d. Chạy lại vẫn an toàn. Khởi động lại server.
-- =====================================================================

"""


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
