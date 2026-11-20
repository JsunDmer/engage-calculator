# 纹章士技能图标 + 职业页增强 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 纹章士详情页全部技能行显示游戏内技能图标（xlsx 内嵌图离线提取）；职业页增加武器类型筛选 chips 与成长率对比表格；推送 GitHub 并发布 v1.3.0 Release。

**Architecture:** build_assets.py 解析 xlsx 两个 drawing 的图片锚点→技能名映射，去重转 webp 输出到 assets/skills/，engage.json 增加顶层 skillIcons 映射；core Dataset 增加对应字段；UI 在 SkillLine 与 ChainStep 中经 AvatarCache 加载显示，缺图显示占位。

**Tech Stack:** Python3 + Pillow（本机已装 11.3.0）、Kotlin + Jetpack Compose、GitHub Actions（android.yml 已有 tag→Release 流程）。

**Spec:** docs/superpowers/specs/2026-11-20-skill-icons-design.md

## Global Constraints

- 图标来源仅限仓库内 xlsx 内嵌图片，不引入网络图源。
- 名称归一规则与 EmblemSupport.kt normalizeName 一致：去 `.`、去空格、`＋`→`+`、lowercase。
- 输出 `app/src/main/assets/skills/skill_<md5前8位>.webp`（无损 webp，按图片内容去重，299 张 ≈ 0.9MB）。
- engage.json 顶层新增 `skillIcons: {归一技能名: "skills/skill_xxx.webp"}`；Dataset 字段默认空 map，向后兼容旧 JSON。
- 覆盖预期：505 条技能中 375 条命中图标（归一后），其余显示占位、不崩溃。
- 提交信息遵循 `<type>(<scope>): <subject>`，结尾带 Co-Authored-By: Claude Code <noreply@anthropic.com>。
- 不改列表页/资质检索页/牵绊戒指页/计算器。
- 武器匹配按词边界：「剑」不命中「短剑」；武器类型全集固定为 剑/枪/斧/弓/短剑/体术/魔道书/杖/龙石/炮弹。
- 成长率着色沿用 GrowthScale.classStat / classTotal，不新增阈值。
- ClassesScreen.kt 已 332 行，成长率表格拆到新文件避免超 400 行。

## Review Focus

- 旧版 engage.json（无 skillIcons 字段）仍能被 DatasetLoader 解析 → Task 2 测试。
- 归一差异名（sp转换/SP转换、剑术对策Lv5/Lv.5）能命中图标 → Task 1 验证脚本断言。
- 无图技能（结合武器等 130 条）显示占位而非空白崩溃 → Task 3 fallback 分支 + assembleDebug。
- 同一图标被多技能共用（如「斩不断的牵绊」）只存一份文件 → Task 1 md5 去重断言。
- 图标为 RGBA 透明 PNG，深色/浅色主题下均可见 → Task 3 用 surfaceVariant 底、不加金边。
- 搜「剑」不误中「短剑B、体术B」类职业；「炮弹S（无需资质）」能被炮弹 chip 命中 → Task 4 单测。
- 武器筛选 + tier/section + 文本搜索叠加时结果正确；成长率视图与列表视图共用同一 filtered 集合 → Task 4/5 验证。

---

### Task 1: 图标提取脚本 + 资产生成

**Files:**
- Modify: `tools/build_assets.py`（新增函数与 main 接线；常量区加 SKILL_ICON_DIR）
- Create: `app/src/main/assets/skills/*.webp`（脚本产物，入库）
- Modify: `app/src/main/assets/engage.json`（脚本产物）

**Interfaces:**
- Produces: engage.json 顶层 `skillIcons: dict[str, str]`，key 为归一技能名，value 为 `skills/skill_<hash8>.webp` 相对路径。
- Produces: `normalize_skill_name(name: str) -> str`、`extract_skill_icons(z: zipfile.ZipFile, sheets: dict) -> dict[str, str]`（build_assets.py 内部函数）。

- [ ] **Step 1: 实现提取逻辑**

在 build_assets.py 中：
- `normalize_skill_name(name)`：`name.replace(".", "").replace(" ", "").replace("＋", "+").lower()`。
- `extract_skill_icons(z, sheets)`：
  - 解析 `xl/drawings/_rels/drawing{1,2}.xml.rels` 得 rId→media 路径；解析 drawing XML 的 `<xdr:oneCellAnchor>`，取 `<xdr:col>`/`<xdr:row>`（0 基）与 `r:embed`。
  - drawing2 对应 sheets[5]（进化链表）：技能名取锚点单元格（col 3–8 → D–I）文本首行；drawing1 对应 sheets[4]（技能表）：取该行 D 列文本。注意 sheets 的行 dict 不含行号，需改用带行号的解析（load_sheets 返回 rows 列表时行号即 xlsx 行序——实现时按 drawing 锚点行号直接重读对应 sheet XML 或让 load_sheets 附带行号，取最小改动方案）。
  - 同名先到先得（setdefault）；用 md5(raw) 去重图片；Pillow 打开转 RGBA 存无损 webp 到 SKILL_ICON_DIR；返回 {归一名: 相对路径}。
  - 每次运行前清空 SKILL_ICON_DIR 旧文件，避免残留。
- main() 中 `payload["skillIcons"] = extract_skill_icons(...)`，打印图标数与字节数。

- [ ] **Step 2: 运行脚本并断言产物**

Run: `python3 -I tools/build_assets.py`，随后用独立校验脚本断言：
- skills 目录文件数 == 299，全部 .webp；
- skillIcons 键数 == 302；
- 505 条技能按归一名命中数 == 375；
- `"sp转换" in skillIcons`、`"剑术对策lv5" in skillIcons`；
- 每个 value 指向的文件存在。
Expected: 全部断言通过。

- [ ] **Step 3: Commit**

```bash
git add tools/build_assets.py app/src/main/assets/skills app/src/main/assets/engage.json
git commit -m "feat(assets): 从数据表提取技能图标并生成 skillIcons 映射"
```

### Task 2: core 数据模型

**Files:**
- Modify: `core/src/main/kotlin/com/engage/core/Model.kt:120-131`（Dataset）
- Test: `core/src/test/kotlin/com/engage/core/DatasetIconsTest.kt`（新建）

**Interfaces:**
- Consumes: Task 1 的 engage.json `skillIcons`。
- Produces: `Dataset.skillIcons: Map<String, String>`（@SerialName("skillIcons")，默认 emptyMap()）；`fun Dataset.skillIcon(name: String): String`（归一查找，miss 返回 ""）。归一函数 `normalizeSkillName(name: String): String` 放 Model.kt 或 DatasetLoader.kt（internal 可见性按现有风格，UI 需调用则 public）。

- [ ] **Step 1: 写失败测试**

DatasetIconsTest（参照 GoldenTest 的加载方式，`File("../app/src/main/assets/engage.json")`）：
- `skillIcons 加载自资产`：dataset.skillIcons.isNotEmpty()；`dataset.skillIcon("SP转换")` 非空且以 "skills/" 开头。
- `归一命中变体写法`：skillIcon("剑术对策Lv.5") == skillIcon("剑术对策Lv5")，均非空。
- `未知技能返回空串`：skillIcon("不存在的技能") == ""。
- `旧 JSON 向后兼容`：DatasetLoader.parse 一段不含 skillIcons 的最小 JSON（stats/starGrowth/chars/classes 四必填字段给空值），断言 skillIcons.isEmpty() 不抛异常。

- [ ] **Step 2: 跑测试确认失败**

Run: `./gradlew :core:test --tests "com.engage.core.DatasetIconsTest"`
Expected: 编译失败（skillIcons 未定义）。

- [ ] **Step 3: 实现模型字段与查找函数**

Dataset 增加字段与 `skillIcon(name)`；归一规则照抄 Global Constraints。EmblemSupport.kt 的私有 normalizeName 改为复用 core 的 normalizeSkillName（删除重复实现）。

- [ ] **Step 4: 跑全部 core 测试**

Run: `./gradlew :core:test`
Expected: GoldenTest 82 组 + 新增 4 测试全部 PASS。

- [ ] **Step 5: Commit**

```bash
git add core/src/main/kotlin/com/engage/core/Model.kt core/src/test/kotlin/com/engage/core/DatasetIconsTest.kt app/src/main/kotlin/com/engage/app/ui/emblems/EmblemSupport.kt
git commit -m "feat(core): Dataset 增加 skillIcons 映射与归一查找"
```

### Task 3: UI 显示技能图标

**Files:**
- Modify: `app/src/main/kotlin/com/engage/app/ui/common/Components.kt`（新增 SkillIcon 组件，复用 AvatarCache）
- Modify: `app/src/main/kotlin/com/engage/app/ui/emblems/EmblemsScreen.kt`（SkillLine、ChainStep、ChainRow/ChainSection/EngageGroup 传递 dataset）

**Interfaces:**
- Consumes: `Dataset.skillIcon(name: String): String`（Task 2）、`AvatarCache.load(context, path): Bitmap?`（Components.kt 私有 object，同文件可用）。
- Produces: `@Composable fun SkillIcon(path: String, size: Dp, modifier: Modifier = Modifier)`：Box + surfaceVariant 底 + RoundedCornerShape(4.dp)，无金边；path 空或加载失败时仅显示底色占位；加载成功 Image(ContentScale.Fit)。

- [ ] **Step 1: 实现 SkillIcon 组件**

参照 AssetAvatar 的 LaunchedEffect + AvatarCache 模式；path.isBlank() 时跳过加载。

- [ ] **Step 2: 接线详情页**

- `SkillLine` 增加 `iconPath: String` 参数：名称行最左 `SkillIcon(iconPath, 22.dp)` + 6.dp 间距，其余不变；调用处（资质、结合组、解锁功能）传 `dataset.skillIcon(skill.name)`。
- `ChainStep` 增加 `iconPath: String`：Column 顶部 `SkillIcon(iconPath, 26.dp)`，名称/元信息在下；调用处 ChainRow 需拿到 dataset（ChainRow/ChainSection 增加 dataset 参数，EmblemDetailScreen 传入）。
- 缺图技能（iconPath == ""）显示占位方块，布局不跳动。

- [ ] **Step 3: 构建验证**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL，无编译警告级错误。

- [ ] **Step 4: Commit**

```bash
git add app/src/main/kotlin/com/engage/app/ui/common/Components.kt app/src/main/kotlin/com/engage/app/ui/emblems/EmblemsScreen.kt
git commit -m "feat(emblems): 详情页技能行与技能链各阶显示技能图标"
```

### Task 4: 职业武器筛选

**Files:**
- Create: `app/src/main/kotlin/com/engage/app/ui/classes/ClassWeapons.kt`（武器解析与匹配，纯逻辑）
- Modify: `app/src/main/kotlin/com/engage/app/ui/classes/ClassesScreen.kt`（chips 行 + 过滤接线 + ClassRow 副标题）
- Test: `core/src/test/kotlin/com/engage/core/` 不放（逻辑在 app 模块）→ 新建 `app/src/test/kotlin/com/engage/app/ui/classes/ClassWeaponsTest.kt`（若 app 模块无 test 配置则补 `testImplementation(kotlin("test"))`，参照 core 的 build.gradle.kts）

**Interfaces:**
- Produces: `val WeaponTypes: List<String>` = 剑/枪/斧/弓/短剑/体术/魔道书/杖/龙石/炮弹（固定顺序）。
- Produces: `fun weaponGrades(weapons: String): Map<String, String>`（解析「剑A、杖C（无法剑S）」→ {剑: "剑A", 杖: "杖C"}；按 、/／// 与换行切分，每项取武器名+等级字母，括号备注忽略）。
- Produces: `fun hasWeapon(weapons: String, type: String): Boolean`（weaponGrades 的 key 精确等于 type，天然词边界）。

- [ ] **Step 1: 写失败测试**

ClassWeaponsTest：
- `解析多武器与括号备注`：weaponGrades("剑A、斧C（无法剑S）") == mapOf("剑" to "剑A", "斧" to "斧C")。
- `剑不误中短剑`：hasWeapon("短剑B、体术B", "剑") == false；hasWeapon("短剑B、体术B", "短剑") == true。
- `炮弹无需资质备注可命中`：hasWeapon("炮弹S（无需资质）", "炮弹") == true。
- `魔道书与杖复合`：hasWeapon("魔道书B、杖S、体术C", "杖") == true。
- `全数据集武器类型均被覆盖`：加载 engage.json，所有 classes 的 weaponGrades key ⊆ WeaponTypes。

- [ ] **Step 2: 跑测试确认失败**

Run: `./gradlew :app:testDebugUnitTest --tests "com.engage.app.ui.classes.ClassWeaponsTest"`
Expected: 编译失败（ClassWeapons 未定义）。

- [ ] **Step 3: 实现 ClassWeapons.kt 并接线 UI**

- ClassesScreen 增加 `var weaponFilter by remember { mutableStateOf<String?>(null) }`；filtered 条件追加 `(weaponFilter == null || hasWeapon(cls.weapons, weaponFilter))`。
- 搜索框与 tier chips 之间加一行武器 chips（horizontalScroll，单选再点取消，样式同现有 FilterChip）。
- ClassRow 增加 `weaponFocus: String?` 参数：非空时副标题改为该职业全部武器资质串（weaponGrades values joinToString "、"），让用户直接看到等级；为空维持现状。

- [ ] **Step 4: 跑测试与构建**

Run: `./gradlew :app:testDebugUnitTest :app:assembleDebug`
Expected: 测试 PASS，BUILD SUCCESSFUL。

- [ ] **Step 5: Commit**

```bash
git add app/src/main/kotlin/com/engage/app/ui/classes/ app/src/test/ app/build.gradle.kts
git commit -m "feat(classes): 武器类型筛选 chips 与资质等级展示"
```

### Task 5: 成长率对比表格视图

**Files:**
- Create: `app/src/main/kotlin/com/engage/app/ui/classes/GrowthTable.kt`
- Modify: `app/src/main/kotlin/com/engage/app/ui/classes/ClassesScreen.kt`（视图切换 chips + 分支渲染）

**Interfaces:**
- Consumes: Task 4 的 filtered 集合与 weaponFilter；`GrowthScale.classStat(Int): GrowthTier`、`GrowthScale.classTotal(Int): GrowthTier`、`growthColor(GrowthTier): Color`（ui/common/GrowthScale.kt 现有）。
- Produces: `@Composable fun GrowthTable(classes: List<ClassInfo>, stats: List<String>, onSelect: (ClassInfo) -> Unit)`。

- [ ] **Step 1: 实现 GrowthTable**

结构：外层 `Row`，左列固定宽 96.dp 的 `LazyColumn`（表头「职业」+ 每行职业名，行高固定 40.dp，点击 onSelect），右侧 `Box(Modifier.horizontalScroll(...))` 内同步行数的表格列（9 项 + 合计，每列宽 44.dp）。两列共用同一 `LazyListState` 保证纵向同步滚动；表头行显示 stats 名称与「合计」。数值用 growthColor(GrowthScale.classStat(v)) 着色，合计用 classTotal；行背景隔行 surfaceVariant.copy(alpha=0.3f) 便于横向追读。职业名超长 maxLines=1 + ellipsis。

- [ ] **Step 2: 接线视图切换**

ClassesScreen 顶部（搜索框上方）加 `var view by remember { mutableStateOf("列表") }`，FilterChip 两项「列表 / 成长率」（风格同 EmblemsScreen ViewFilters）；view=="成长率" 时渲染 GrowthTable(filtered, dataset.stats) { selected = it }，否则现有 LazyColumn。筛选区两种视图都保留。

- [ ] **Step 3: 构建验证**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL。

- [ ] **Step 4: Commit**

```bash
git add app/src/main/kotlin/com/engage/app/ui/classes/
git commit -m "feat(classes): 成长率对比表格视图"
```

### Task 6: 推送与发布 v1.3.0

**Files:**
- Modify: `README.md`（功能列表提一句技能图标）
- Create: git tag `v1.3.0`

**Interfaces:**
- Consumes: Task 1–5 全部提交；`.github/workflows/android.yml` 现有 tag 触发 Release 流程（versionName=1.3.0，versionCode=10300）。

- [ ] **Step 1: README 更新并提交**

纹章士图鉴条目补「技能图标」、职业图鉴条目补「武器筛选与成长率对比」描述；`git commit -m "docs: 说明技能图标与职业页新视图"`。

- [ ] **Step 2: 推送 main 与 tag**

```bash
git push origin main
git tag v1.3.0 && git push origin v1.3.0
```

- [ ] **Step 3: 验证 CI 与 Release**

Run: `gh run watch`（或轮询 `gh run list --limit 3`）直到 release job 完成；`gh release view v1.3.0` 确认 APK 资产 engage-calculator-1.3.0.apk 已上传。
Expected: build + release 两个 job 均 success，Release 含 APK。