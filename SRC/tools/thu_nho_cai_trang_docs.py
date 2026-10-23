#!/usr/bin/env python3
"""Thu 7 cải trang của patch 94 cho cao BẰNG "Cải trang Hắc Y Song Đao", giữ nguyên tỉ lệ gốc.

Dùng:
    /usr/bin/python3 tools/thu_nho_cai_trang_docs.py            # chỉ in báo cáo
    /usr/bin/python3 tools/thu_nho_cai_trang_docs.py --apply    # ghi ảnh, sửa patch 94/96, ghi patch mới

Ảnh Vương Lâm và 6 bộ x4 là ảnh HD, vào game to gần gấp đôi; Hắc Y Song Đao là ảnh chuẩn của game.
Mỗi bộ dùng MỘT hệ số cho cả đầu / thân / chân (chủ dự án chốt: không thu riêng từng phần):

  1. Hệ số = chiều cao dáng đứng (đầu + thân + chân, như client ghép) của Hắc Y / của bộ đó.
  2. Thu mọi mảnh theo hệ số đó; dx/dy thu quanh điểm chân nhân vật: d' = s*(CI + d) - CI, với CI là
     độ lệch của từng loại mảnh ở dáng đứng (đầu -13,34 / thân -9,16 / chân -8,10). Cách ghép giữa
     đầu, thân, chân giữ y bản gốc.
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
DATAGAME = HERE + "/src/nro/models/data/DataGame.java"
ZOOMS = (1, 2, 3, 4)
MAU = 2661                     # part đầu của Hắc Y Song Đao
CI = {0: (0, -13, 34), 1: (1, -9, 16), 2: (1, -8, 10)}   # dáng đứng: (mảnh, x, y) của đầu / thân / chân
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
    taken |= set(range(20257, 20831))

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
        s = h_mau / chieu_cao(goc, p0)
        cu = [[p for p in goc[p0 + t] if p[0] != TRANSPARENT] for t in range(3)]
        base = cap_dai(sum(len(c) for c in cu))
        moi, anh_moi, k = [], [], 0
        for t in range(3):
            _, x, y = CI[t]
            pcs = []
            for ic, dx, dy in cu[t]:
                pcs.append([base + k, round(s * (x + dx) - x), round(s * (-y + dy) + y)])
                anh_moi.append((base + k, thu(anh_goc(ic), s)))
                k += 1
            moi.append(pcs + [[TRANSPARENT, 0, 0]])
        dang_dung = [ic for t in range(3) for ic, _, _ in hien[p0 + t] if ic != TRANSPARENT]
        ket_qua.append((item, p0, s, moi, anh_moi, dang_dung, folder))
        print("  %d  part %d-%d  hệ số %.2f  icon mới %d..%d" % (item, p0, p0 + 2, s, base, base + k - 1))

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

    # Patch 94 (dòng INSERT) và 96 (dòng UPDATE) sửa theo -> chạy lại patch nào cũng ra bản cuối
    t94 = open(P94, encoding="utf-8").read()
    t96 = open(P96, encoding="utf-8").read()
    for _, p0, _, moi, _, _, _ in ket_qua:
        for t in range(3):
            t94, n = re.subn(r"^\(%d, %d, '\[.*?\]'\)" % (p0 + t, t),
                             "(%d, %d, '%s')" % (p0 + t, t, data(moi[t])), t94, flags=re.M)
            assert n == 1, p0 + t
            t96, n = re.subn(r"SET `DATA` = '\[.*?\]' WHERE `id` = %d AND" % (p0 + t),
                             "SET `DATA` = '%s' WHERE `id` = %d AND" % (data(moi[t]), p0 + t), t96)
            assert n == 1, p0 + t
    t94 = t94.replace("-- * Patch 96 thu nhỏ 7 bộ không có dx/dy cho bằng cỡ Hắc Y Song Đao; part dưới đây\n",
                      "-- * Patch 97 thu 7 bộ không có dx/dy cho cao bằng Hắc Y Song Đao; part dưới đây\n")
    if "Patch 97" not in t96:
        t96 = t96.replace("-- Ảnh nhỏ mang SỐ ICON MỚI",
                          "-- ĐÃ THAY bằng patch 97 (thu đều cả người, giữ tỉ lệ gốc); các dòng dưới đã sửa theo\n"
                          "-- bản 97 nên chạy 96 hay 97 đều ra kết quả cuối.\n-- Ảnh nhỏ mang SỐ ICON MỚI", 1)
    open(P94, "w", encoding="utf-8").write(t94)
    open(P96, "w", encoding="utf-8").write(t96)

    with open(P97, "w", encoding="utf-8") as f:
        f.write(HEADER % dict(vd=vs_data, ds="\n".join(
            "--   %d  part %d-%d  hệ số %.2f" % (it, p0, p0 + 2, s) for it, p0, s, _, _, _, _ in ket_qua)))
        for _, p0, _, moi, _, _, _ in ket_qua:
            for t in range(3):
                f.write("UPDATE `part` SET `DATA` = '%s' WHERE `id` = %d AND `TYPE` = %d;\n"
                        % (data(moi[t]), p0 + t, t))
        f.write("\nSELECT `id`, LEFT(`DATA`, 60) FROM `part` WHERE `id` BETWEEN %d AND %d;\n"
                % (BO[0][1], BO[-1][1] + 2))
    print("Đã ghi ảnh, sửa patch 94 / 96, ghi patch 97.")
    return 0


HEADER = """-- =====================================================================
-- 97 — 7 CẢI TRANG CỦA PATCH 94 CAO BẰNG HẮC Y SONG ĐAO, GIỮ TỈ LỆ GỐC (2026-10-23)
--      (sinh bởi tools/thu_nho_cai_trang_docs.py)
-- =====================================================================
-- Patch 96 thu riêng đầu / thân / chân nên vào game trông nhỏ và lệch tỉ lệ. Chủ dự án chốt:
-- mỗi bộ thu ĐỀU cả người một hệ số, cho chiều cao dáng đứng bằng Hắc Y Song Đao (2290).
-- Hệ số:
%(ds)s
--
-- Ảnh mang SỐ ICON MỚI (client không tải lại ảnh cũ cùng số vì phiên bản ảnh nhỏ luôn 0).
-- Icon vật phẩm và avatar không đổi.
--
-- Chạy SAU patch 94 (94 và 96 cũng đã sửa theo -> chạy thứ tự nào cũng ra kết quả này).
-- Cùng jar có DataGame.vsData = %(vd)d. Chạy lại vẫn an toàn. Khởi động lại server.
-- =====================================================================

"""


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
