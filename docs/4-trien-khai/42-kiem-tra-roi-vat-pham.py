#!/usr/bin/env python3
"""Đối chiếu: mọi bước 'nhặt vật phẩm' trong 02-nhiem-vu-moi.sql phải có nguồn rơi đúng vật phẩm, đúng bước,
ở nơi người chơi tới được. In danh sách thiếu (kỳ vọng 0)."""
import re, os, sys, glob
ROOT = os.path.normpath(os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..', 'SRC'))
M = ROOT + '/src/nro/models'
rd = lambda p: open(p, encoding='utf-8', errors='replace').read()

# 1) Các bước trong 02: TASK_x_y -> (tên, map)
sql = rd(ROOT + '/sql/patch/02-nhiem-vu-moi.sql')
steps = {}
for m in re.finditer(r"^\((\d+), '((?:[^'\\]|\\.)*)', (\d+), '(?:[^'\\]|\\.)*', (-?\d+), (-?\d+), (\d+)\)[,;]?\s*-- (TASK_\d+_\d+)", sql, re.M):
    steps[m.group(7)] = dict(name=m.group(2), map=int(m.group(5)))
print(f'02: {len(steps)} bước')

# 2) Hằng ConstTask
ct = rd(M + '/consts/ConstTask.java')
TASKVAL = {k: int(v) for k, v in re.findall(r'int (TASK_\d+_\d+) = (\d+);', ct)}

# 3) checkDoneTaskPickItem: item -> bước
ts = rd(M + '/services/TaskService.java')
body = ts[ts.index('public void checkDoneTaskPickItem'):]
body = body[:body.index('\n    }\n')]
pick = {}   # item -> [TASK]
pending = []
for line in body.split('\n'):
    for c in re.findall(r'case (\d+):', line):
        pending.append(int(c))
    t = re.findall(r'ConstTask\.(TASK_\d+_\d+)', line)
    if t and pending:
        for it in pending:
            pick.setdefault(it, set()).update(t)
    if 'break;' in line:
        pending = []
EXCLUDE = {14, 15, 16, 17, 18, 19, 20, 77}  # Ngọc Rồng (39.1 đếm hành trang), 77 thành tích
pick_steps = {(it, t) for it, ts_ in pick.items() if it not in EXCLUDE for t in ts_}
print(f'checkDoneTaskPickItem: {len(pick_steps)} cặp (vật phẩm, bước)')

# 4) Nguồn: bảng QuestDrop + 2 nguồn gốc NV 2/3
qd = rd(M + '/task/QuestDrop.java')
bossid = {k: int(v) for k, v in re.findall(r'int (\w+) = (-?\d+);', rd(M + '/boss/BossID.java'))}
constmob = {k: int(v) for k, v in re.findall(r'byte (\w+) = (\d+);', rd(M + '/consts/ConstMob.java'))}
rules = []
for m in re.finditer(r'new Rule\(ConstTask\.(TASK_\d+_\d+), SourceType\.(\w+),\s*(null|ids\(([^)]*)\)),\s*(ANY_MAP|ids\(([^)]*)\)),\s*(\d+),\s*(\d+),\s*(\d+)\)', qd, re.S):
    srcs = [s.strip() for s in (m.group(4) or '').split(',') if s.strip()]
    maps = [int(s) for s in (m.group(6) or '').split(',') if s.strip()]
    rules.append(dict(step=m.group(1), type=m.group(2), srcs=srcs, maps=maps, item=int(m.group(7)), rate=int(m.group(8)), qty=int(m.group(9)), origin='QuestDrop'))
mob = rd(M + '/mob/Mob.java')
if re.search(r'TASK_2_0\)\s*\{\s*itemMap = new ItemMap\(zone, 73,', mob):
    rules.append(dict(step='TASK_2_0', type='MOB', srcs=['ConstMob.KHUNG_LONG', 'ConstMob.LON_LOI', 'ConstMob.QUY_DAT'], maps=[], item=73, rate=100, qty=1, origin='Mob.dropItemTask'))
mapj = rd(M + '/map/Map.java')
if re.search(r'case 42 ->\s*itemMap = new ItemMap\(zone, 78', mapj):
    rules.append(dict(step='TASK_3_1', type='MAP_INIT', srcs=[], maps=[42, 43, 44], item=78, rate=100, qty=1, origin='Map.initItem'))
print(f'nguồn rơi: {len(rules)} dòng')

# 5) Dữ liệu map: mob trong từng map
nro = rd(ROOT + '/sql/nro1.sql')
mt = ''
for seg in nro.split('INSERT INTO `map_template`')[1:]:
    mt += '\n' + seg[:seg.index(';\n')]
mapmobs = {}
for m in re.finditer(r"\n\((\d+), '[^']*', \d+, \d+, '[^']*', \d+, \d+, \d+, \d+, \d+, '(?:[^'\\]|\\.)*', '((?:[^'\\]|\\.)*)'", mt):
    mapmobs[int(m.group(1))] = {int(x) for x in re.findall(r'\[(\d+),', m.group(2).replace('\\"', ''))}

# 6) Boss: có lớp tạo được + gọi checkDoneTaskKillBoss (điểm móc QuestDrop.onBossKilled)
bm = rd(M + '/boss/Boss_Manager/BossManager.java')
cls_of = dict(re.findall(r'case BossID\.(\w+) ->\s*new (\w+)\(', bm))
java = {os.path.basename(p)[:-5]: p for p in glob.glob(M + '/**/*.java', recursive=True)}
def boss_hooked(name):
    c = cls_of.get(name)
    if not c or c not in java:
        return False, f'không có lớp tạo trong BossManager'
    src = rd(java[c])
    if 'checkDoneTaskKillBoss' in src or 'extends QuestBoss' in src:
        return True, c
    if 'void reward(' not in src:
        return True, c + ' (Boss.reward mặc định)'
    return False, f'{c}.reward không gọi checkDoneTaskKillBoss'
def boss_spawned(name):
    if re.search(r'createBoss\(BossID\.%s\b' % name, bm):
        return True
    # boss sinh theo đàn / theo sự kiện / theo boss khác
    for p in glob.glob(M + '/**/*.java', recursive=True):
        if p.endswith(('BossID.java', 'TaskService.java', 'QuestDrop.java', 'BossManager.java')):
            continue
        if re.search(r'BossID\.%s\b' % name, rd(p)):
            return True
    return False

missing, rows = [], []
for (item, step) in sorted(pick_steps, key=lambda x: TASKVAL.get(x[1], 0)):
    if step not in steps:
        continue  # bước không có trong 02 (không thuộc tuyến)
    cand = [r for r in rules if r['step'] == step and r['item'] == item]
    ok_src = []
    problems = []
    for r in cand:
        if r['type'] == 'BOSS':
            for s in r['srcs']:
                n = s.split('.')[-1]
                h, why = boss_hooked(n)
                if h and boss_spawned(n):
                    ok_src.append(f'boss {n}({bossid.get(n)})')
                else:
                    problems.append(f'boss {n}: {why if not h else "không được sinh ra"}')
        elif r['type'] == 'MOB':
            for s in r['srcs']:
                n = s.split('.')[-1]
                tid = constmob.get(n)
                maps = r['maps'] or [mid for mid, ms in mapmobs.items() if tid in ms]
                where = [mid for mid in maps if tid in mapmobs.get(mid, set())]
                if where:
                    ok_src.append(f'quái {n}({tid}) @ {where[:6]}{"…" if len(where) > 6 else ""}')
                else:
                    problems.append(f'quái {n}({tid}) không có ở map {r["maps"] or "nào"}')
        elif r['type'] in ('MAP_SPAWN', 'MAP_INIT'):
            for mid in r['maps']:
                if mid in mapmobs:
                    ok_src.append(f'{r["type"].lower()} map {mid}')
                else:
                    problems.append(f'map {mid} không tồn tại')
    status = 'OK' if ok_src else 'THIẾU'
    if not ok_src:
        missing.append((step, item, problems or ['không có dòng nguồn rơi']))
    rate = ', '.join(sorted({f'{r["rate"]}%' for r in cand})) or '-'
    rows.append((step, item, steps[step]['name'], rate, '; '.join(ok_src) or '; '.join(problems) or '-', status))

print()
print('| Bước | Vật phẩm | Tên bước | Tỉ lệ | Nguồn tới được | Kết quả |')
print('|---|---|---|---|---|---|')
for r in rows:
    print('| %s | %d | %s | %s | %s | %s |' % r)
print()
# bước 'nhặt' trong 02 mà TaskService KHÔNG xử lý (tên có chữ Nhặt)
unhandled = [t for t, v in steps.items() if v['name'].startswith('Nhặt') and t not in {s for _, s in pick_steps} and t != 'TASK_39_1']
print('Bước tên "Nhặt…" trong 02 mà checkDoneTaskPickItem không xử lý:', unhandled or 0)
print('SỐ BƯỚC NHẶT THIẾU NGUỒN RƠI:', len(missing))
for m in missing:
    print('  -', m)
sys.exit(1 if missing or unhandled else 0)
