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
EMBLEM_DIR = os.path.join(OUT_DIR, "emblems")
SKILL_ICON_DIR = os.path.join(OUT_DIR, "skills")
BOND_RINGS = os.path.join(ROOT, "tools", "bond_rings.json")

STATS = ["HP", "力", "魔", "技", "速", "幸", "防", "魔防", "体"]

# 纹章士立绘来自腾讯文档「03-纹章士信息」单元格图片，按图源编号命名。
EMBLEM_ART = {
    "马尔斯": "21", "赛莉卡": "02", "西格尔特": "12", "里弗": "05", "罗伊": "19",
    "琳": "23", "艾莉可": "16", "艾克": "03", "米卡雅": "10", "露琪娜": "09",
    "神威": "06", "贝雷特": "13", "琉尔": "17", "艾黛尔贾特": "15", "帝弥托利": "18",
    "库罗德": "07", "琪姬": "04", "海克托尔": "08", "塞涅里欧": "14", "卡美拉": "01",
    "库洛武": "11", "鲁弗莱": "20", "维洛妮卡": "00",
}


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
        name = row.get("A", "")
        art = EMBLEM_ART.get(name)
        out.append({
            "n": name, "sec": row.get("B", ""), "ch": row.get("C", ""),
            "join": row.get("D", ""), "bonus": row.get("E", ""), "sync": row.get("F", ""),
            "inherit": row.get("G", ""), "engage": row.get("H", ""), "combo": row.get("I", ""),
            "weapon": row.get("J", ""), "unlock": row.get("K", ""),
            "avatar": f"emblems/emblem_{art}.webp" if art else "",
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


def load_bond_rings():
    """牵绊戒指表来自腾讯文档「04-牵绊戒指」，已离线解析为固定结构。"""
    with open(BOND_RINGS, encoding="utf-8") as f:
        return json.load(f)


def normalize_skill_name(name):
    """技能名在不同表里有「Lv.5 / Lv5 / SP转换 / sp转换」写法差异，与 App 端归一规则保持一致。"""
    return name.replace(".", "").replace(" ", "").replace("＋", "+").lower()


def _sheet_cells_by_row(z, sheet_idx):
    """drawing 锚点用绝对行号定位，load_sheets 的顺序列表可能因空行缺位，这里按 r 属性重读。"""
    shared = z.read("xl/sharedStrings.xml").decode("utf-8")
    strings = ["".join(re.findall(r"<t[^>]*>(.*?)</t>", si, re.S))
               for si in re.findall(r"<si>(.*?)</si>", shared, re.S)]
    xml = z.read(f"xl/worksheets/sheet{sheet_idx}.xml").decode("utf-8")
    rows = {}
    for row_xml in re.findall(r"<row[^>]*>.*?</row>", xml, re.S):
        for col, row, attrs, value in re.findall(
                r'<c r="([A-Z]+)(\d+)"([^>]*)>(?:<v>(.*?)</v>)?', row_xml):
            val = value
            if 't="s"' in attrs and value and value.isdigit():
                val = strings[int(value)]
            if val:
                rows.setdefault(int(row), {})[col] = html.unescape(val).replace("\r", "").strip()
    return rows


def _drawing_anchors(z, drawing_idx):
    """解析 drawing 的图片锚点：返回 (0基列, 1基行, media 路径) 列表。"""
    rels = z.read(f"xl/drawings/_rels/drawing{drawing_idx}.xml.rels").decode("utf-8")
    rel_map = dict(re.findall(r'Id="(rId\d+)"[^>]*Target="\.\./(media/[^"]+)"', rels))
    xml = z.read(f"xl/drawings/drawing{drawing_idx}.xml").decode("utf-8")
    out = []
    for anchor in re.findall(r"<xdr:oneCellAnchor>.*?</xdr:oneCellAnchor>", xml, re.S):
        pos = re.search(r"<xdr:col>(\d+)</xdr:col>.*?<xdr:row>(\d+)</xdr:row>", anchor, re.S)
        embed = re.search(r'r:embed="(rId\d+)"', anchor)
        if pos and embed and embed.group(1) in rel_map:
            out.append((int(pos.group(1)), int(pos.group(2)) + 1, rel_map[embed.group(1)]))
    return out


def extract_skill_icons(z):
    """技能图标内嵌在数据表里：drawing1 锚定技能表 D 列，drawing2 锚定进化链表 D–I 列各阶。

    同名技能先到先得；图片按内容 md5 去重后转无损 webp，避免「斩不断的牵绊」这类共用图重复入库。
    """
    import hashlib
    import io
    from PIL import Image

    names = {}
    # 进化链表：锚点单元格首行即该阶技能名
    chain_rows = _sheet_cells_by_row(z, 5)
    for col, row, media in _drawing_anchors(z, 2):
        cell = chain_rows.get(row, {}).get(col_letter(col), "")
        name = normalize_skill_name(cell.split("\n")[0].strip())
        if name:
            names.setdefault(name, media)
    # 技能表：图标统一锚在 A 列，技能名取该行 D 列
    skill_rows = _sheet_cells_by_row(z, 4)
    for _col, row, media in _drawing_anchors(z, 1):
        name = normalize_skill_name(skill_rows.get(row, {}).get("D", ""))
        if name:
            names.setdefault(name, media)

    os.makedirs(SKILL_ICON_DIR, exist_ok=True)
    for old in os.listdir(SKILL_ICON_DIR):
        os.remove(os.path.join(SKILL_ICON_DIR, old))
    by_hash, icons, total = {}, {}, 0
    for name, media in sorted(names.items()):
        raw = z.read("xl/" + media)
        digest = hashlib.md5(raw).hexdigest()[:8]
        if digest not in by_hash:
            image = Image.open(io.BytesIO(raw)).convert("RGBA")
            # 技能表部分锚点是「图标+名称文字」的横幅图，直接缩进方格会变成不可读细条；
            # 左侧方形区域即独立图标，裁掉右侧文字。
            if image.width > image.height * 1.2:
                image = image.crop((0, 0, image.height, image.height))
            buf = io.BytesIO()
            image.save(buf, "WEBP", lossless=True)
            data = buf.getvalue()
            with open(os.path.join(SKILL_ICON_DIR, f"skill_{digest}.webp"), "wb") as f:
                f.write(data)
            by_hash[digest] = len(data)
        icons[name] = f"skills/skill_{digest}.webp"
        total += by_hash[digest]
    print("skill icons", len(icons), "unique files", len(by_hash))
    return icons


def main():
    sheets = load_sheets(XLSX)
    chars, html_classes = load_html_chars()
    avatar_bytes = dump_avatars(chars)
    with zipfile.ZipFile(XLSX) as z:
        skill_icons = extract_skill_icons(z)
    payload = {
        "stats": STATS,
        "starGrowth": 15,
        "chars": chars,
        "classes": merge_classes(html_classes, build_classes(sheets[2])),
        "emblems": build_emblems(sheets[3]),
        "skills": build_skills(sheets[4]),
        "chains": build_chains(sheets[5]),
        "types": build_types(sheets[6]),
        "rings": load_bond_rings(),
        "skillIcons": skill_icons,
    }
    os.makedirs(OUT_DIR, exist_ok=True)
    with open(OUT_JSON, "w", encoding="utf-8") as f:
        json.dump(payload, f, ensure_ascii=False, separators=(",", ":"))
    print("chars", len(payload["chars"]), "classes", len(payload["classes"]),
          "emblems", len(payload["emblems"]), "skills", len(payload["skills"]),
          "chains", len(payload["chains"]), "types", len(payload["types"]),
          "rings", len(payload["rings"]))
    print("json bytes", os.path.getsize(OUT_JSON), "avatar bytes", avatar_bytes)


if __name__ == "__main__":
    main()