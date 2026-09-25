import re, io

def split_rows(values):
    rows, cur, depth, q, esc = [], [], 0, None, False
    buf = ""
    for ch in values:
        if esc:
            buf += ch; esc = False; continue
        if ch == "\\" and q:
            buf += ch; esc = True; continue
        if q:
            buf += ch
            if ch == q: q = None
            continue
        if ch in "'\"":
            q = ch; buf += ch; continue
        if ch == "(":
            depth += 1
            if depth == 1: buf = ""; continue
        if ch == ")":
            depth -= 1
            if depth == 0:
                rows.append(buf); buf = ""; continue
        if depth >= 1: buf += ch
    return rows

def split_fields(row):
    out, buf, q, esc, depth = [], "", None, False, 0
    for ch in row:
        if esc: buf += ch; esc = False; continue
        if ch == "\\" and q: buf += ch; esc = True; continue
        if q:
            buf += ch
            if ch == q: q = None
            continue
        if ch in "'\"": q = ch; buf += ch; continue
        if ch == "(": depth += 1
        if ch == ")": depth -= 1
        if ch == "," and depth == 0:
            out.append(buf.strip()); buf = ""; continue
        buf += ch
    out.append(buf.strip())
    return out

def unq(v):
    v = v.strip()
    if len(v) >= 2 and v[0] in "'\"" and v[-1] == v[0]:
        v = v[1:-1]
        v = v.replace("\\'", "'").replace('\\"', '"').replace("\\\\", "\\").replace("\\n","\n")
    return v

def load_table(path, table):
    """Trả về (cols, [row_dict]) cho bảng cần lấy."""
    txt = open(path, encoding="utf8", errors="replace").read()
    cols = None
    m = re.search(r"CREATE TABLE `%s` \((.*?)\n\) ENGINE" % table, txt, re.S)
    if m:
        cols = [c.group(1) for c in re.finditer(r"^\s*`([^`]+)`\s", m.group(1), re.M)]
    rows = []
    for ins in re.finditer(r"INSERT INTO `%s`\s*(\([^)]*\))?\s*VALUES\s*(.*?);\s*\n" % table, txt, re.S):
        c = cols
        if ins.group(1):
            c = [x.strip(" `") for x in ins.group(1).strip("()").split(",")]
        for r in split_rows(ins.group(2)):
            f = [unq(x) for x in split_fields(r)]
            if c and len(f) == len(c):
                rows.append(dict(zip(c, f)))
            else:
                rows.append({str(i): v for i, v in enumerate(f)})
    return cols, rows
