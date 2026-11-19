#!/usr/bin/env python3
"""从《火焰纹章Engage_数据表.xlsx》与计算器 HTML 生成 Android assets 数据。"""
import html
import json
import os
import re
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
XLSX = os.path.join(ROOT, "火焰纹章Engage_数据表.xlsx")
HTML = os.path.join(ROOT, "Engage转职计算器.html")
OUT_DIR = os.path.join(ROOT, "app", "src", "main", "assets")
OUT_JSON = os.path.join(OUT_DIR, "engage.json")
AVATAR_DIR = os.path.join(OUT_DIR, "avatars")

STATS = ["HP", "力", "魔", "技", "速", "幸", "防", "魔防", "体"]


def load_sheets(path):
    z = zipfile.ZipFile(path)
    shared = z.read("xl/sharedStrings.xml").decode("utf-8")
    strings = ["".join(re.findall(r"<t[^>]*>(.*?)</t>", si, re.S))
               for si in re.findall(r"<si>(.*?)</si>", shared, re.S)]
    sheets = {}
    for idx in range(1, 8):
        xml = z.read(f"xl/worksheets/sheet{idx}.xml").decode("utf-8")
        rows = []
        for row_xml in re.findall(r"<row[^>]*>.*?</row>", xml, re.S):
            cells = {}
            for col, _row, attrs, value in re.findall(
                    r'<c r="([A-Z]+)(\d+)"([^>]*)>(?:<v>(.*?)</v>)?', row_xml):
                val = value
                if 't="s"' in attrs and value and value.isdigit():
                    val = strings[int(value)]
                if val is not None:
                    val = html.unescape(val).replace("\r", "").strip()
                cells[col] = val
            rows.append(cells)
        sheets[idx] = rows
    return sheets


def num(cell):
    try:
        return int(float(cell))
    except (TypeError, ValueError):
        return 0


def col_letter(i):
    return chr(ord("A") + i)


def build_classes(rows):
    out = []
    for row in rows[2:]:
        if not row.get("B"):
            continue
        out.append({
            "sec": row.get("A", ""),
            "n": row.get("B", ""),
            "lv": row.get("C", ""),
            "mv": num(row.get("D")),
            "w": row.get("E", ""),
            "skill": row.get("F", ""),
            "sd": row.get("G", ""),
            "g": [num(row.get(col_letter(7 + i))) for i in range(9)],
            "gt": num(row.get("Q")),
            "cap": [num(row.get(col_letter(17 + i))) for i in range(9)],
            "ct": num(row.get("AA")),
            "type": row.get("AB", ""),
            "note": row.get("AC", ""),
        })
    return out


def build_emblems(rows):
    out = []
    for row in rows[1:]:
        if not row.get("A"):
            continue
        out.append({
            "n": row.get("A", ""), "sec": row.get("B", ""), "ch": row.get("C", ""),
            "join": row.get("D", ""), "bonus": row.get("E", ""), "sync": row.get("F", ""),
            "inherit": row.get("G", ""), "engage": row.get("H", ""), "combo": row.get("I", ""),
            "weapon": row.get("J", ""), "unlock": row.get("K", ""),
        })
    return out


def build_skills(rows):
    out = []
    for row in rows[1:]:
        if not row.get("A"):
            continue
        out.append({
            "e": row.get("A", ""), "lv": row.get("B", ""), "kind": row.get("C", ""),
            "n": row.get("D", ""), "d": row.get("E", ""), "sp": row.get("F", ""),
            "note": row.get("G", ""),
        })
    return out


def build_chains(rows):
    out, current, current_kind = [], "", ""
    for row in rows[3:]:
        if row.get("A"):
            current = row["A"]
        if row.get("B"):
            current_kind = row["B"]
        if not row.get("C") and not row.get("D"):
            continue
        out.append({
            "e": current, "kind": current_kind, "fam": row.get("C", ""),
            "steps": [row[c] for c in ("D", "E", "F", "G", "H", "I") if row.get(c)],
            "final": row.get("J", ""), "note": row.get("K", ""), "sp": row.get("L", ""),
        })
    return out


def build_types(rows):
    out = []
    for row in rows[1:]:
        if not row.get("A"):
            continue
        out.append({"n": row["A"], "d": row.get("B", "")})
    return out


def load_html_chars():
    with open(HTML, encoding="utf-8") as f:
        for line in f:
            if line.startswith("const DATA = "):
                body = line[len("const DATA = "):].strip()
                data = json.loads(body.rstrip(";"))
                return data["chars"], data["classes"]
    raise RuntimeError("DATA not found in HTML")


def dump_avatars(chars):
    os.makedirs(AVATAR_DIR, exist_ok=True)
    total = 0
    for idx, ch in enumerate(chars):
        uri = ch.get("p") or ""
        if not uri.startswith("data:image/webp;base64,"):
            ch["avatar"] = ""
            continue
        import base64
        raw = base64.b64decode(uri.split(",", 1)[1])
        name = f"char_{idx:02d}.webp"
        with open(os.path.join(AVATAR_DIR, name), "wb") as f:
            f.write(raw)
        ch["avatar"] = f"avatars/{name}"
        ch.pop("p", None)
        total += len(raw)
    return total


def rename_skill(raw):
    """Excel 用「--」表示无职业特技，统一成空串方便 UI 判空。"""
    raw = (raw or "").strip()
    return "" if raw in ("--", "-", "—") else raw


def merge_classes(html_classes, sheet_classes):
    """HTML 提供职业基础值/成长/上限，Excel 提供特技说明与战斗类型等补充字段。"""
    extra = {c["n"]: c for c in sheet_classes}
    out = []
    for c in html_classes:
        add = extra.get(c["n"], {})
        out.append({
            "sec": c.get("sec", ""), "n": c["n"], "lv": c.get("lv", ""),
            "mv": c.get("mv", 0), "w": c.get("w", ""), "skill": rename_skill(add.get("skill", c.get("s", ""))),
            "sd": add.get("sd", ""), "b": c.get("b", []),
            "g": c.get("g", []), "gt": add.get("gt", 0),
            "cap": c.get("cap", []), "ct": add.get("ct", 0),
            "type": add.get("type", ""), "note": c.get("nt", add.get("note", "")),
        })
    return out


def main():
    sheets = load_sheets(XLSX)
    chars, html_classes = load_html_chars()
    avatar_bytes = dump_avatars(chars)
    payload = {
        "stats": STATS,
        "starGrowth": 15,
        "chars": chars,
        "classes": merge_classes(html_classes, build_classes(sheets[2])),
        "emblems": build_emblems(sheets[3]),
        "skills": build_skills(sheets[4]),
        "chains": build_chains(sheets[5]),
        "types": build_types(sheets[6]),
    }
    os.makedirs(OUT_DIR, exist_ok=True)
    with open(OUT_JSON, "w", encoding="utf-8") as f:
        json.dump(payload, f, ensure_ascii=False, separators=(",", ":"))
    print("chars", len(payload["chars"]), "classes", len(payload["classes"]),
          "emblems", len(payload["emblems"]), "skills", len(payload["skills"]),
          "chains", len(payload["chains"]), "types", len(payload["types"]))
    print("json bytes", os.path.getsize(OUT_JSON), "avatar bytes", avatar_bytes)


if __name__ == "__main__":
    main()