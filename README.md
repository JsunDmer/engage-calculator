# Engage 转职计算器（Android）

《火焰纹章 Engage》固定成长模式转职路线计算器与资料图鉴，原生 Jetpack Compose 实现，适配手机屏幕。

## 功能

- **计算器**：选择人物 → 编辑转职路线（最多 4 段，可增删、逐段设置职业与等级）→ 星玉开关 → 实时结算最终能力值。
  - 固定成长算法与原网页版逐级结算完全一致：`个人成长率 + 职业成长补正（贾恩 ×2）+ 星玉`，满 100 结算 +1 并结转余数。
  - 转职时低于新职业基础值抬升到基础值；超过「职业上限 + 角色上限修正」的部分按游戏规则作废并在提示里列出。
  - 职业下拉框按角色专属限制过滤，并提示缺失的武器资质。
- **职业图鉴**：53 个职业，支持按基础 / 高级 / 特殊分段、「专属 / 通用」筛选、武器类型筛选（剑 / 枪 / 斧 / 弓 / 短剑 / 体术 / 魔道书 / 杖 / 龙石 / 炮弹，行内显示资质等级）与关键字搜索；「成长率」视图以冻结职业名列的对比表格同屏展示 9 项成长率与合计；详情含基础值、成长率、上限、特技、兵种类型。
- **纹章士图鉴**：23 位纹章士（本篇 + DLC），含获得章节、满级牵绊加成、解锁流程、全部 505 条技能（按同步 / 结合 / 继承分类）与 104 条技能进化链；同步 / 继承技能链各阶显示游戏内技能图标（提取自数据表内嵌图）；牵绊戒指在独立视图查看。
- **基础说明**：结算规则、等级上限、专属职业限制、8 种兵种类型说明。
- 深色 / 浅色主题跟随系统。

## 项目结构

```
app/                     Android 应用（Compose UI）
  src/main/assets/       engage.json + avatars/*.webp（构建产物，已入库）
core/                    纯 Kotlin JVM 模块：数据模型与成长算法，可在 JVM 上单元测试
core/src/test/           GoldenTest：与网页版 JS 引擎逐条比对 82 组用例
tools/build_assets.py    从 xlsx + HTML 重新生成 assets
.github/workflows/       CI 构建与 Release 发布
```

## 本地构建

前置：JDK 17、Android SDK（platform 35、build-tools 35.0.0）。

```bash
# local.properties 中写入 SDK 路径
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties

./gradlew :core:test          # 跑算法回归测试
./gradlew :app:assembleDebug  # 产物：app/build/outputs/apk/debug/app-debug.apk
```

如需内网 Maven 镜像，在 `local.properties` 增加：

```properties
mirror.url=http://artifactory.example.com/artifactory/maven-virtual/
```

`settings.gradle.kts` 会把它插到 `google()` / `mavenCentral()` 之前。

### 签名发布包

设置环境变量后执行 `assembleRelease`，签名配置会自动生效；未设置时产物为未签名 APK。

```bash
export KEYSTORE_PATH=/path/to/release.keystore
export KEYSTORE_PASSWORD=...
export KEY_ALIAS=...
export KEY_PASSWORD=...
./gradlew :app:assembleRelease
```

## 数据更新

`app/src/main/assets/engage.json` 由脚本从原始资料生成，不需要手改：

```bash
python3 tools/build_assets.py
```

脚本合并两个数据源：

- `火焰纹章Engage_数据表.xlsx`：职业特技与说明、成长/上限合计、兵种类型、纹章士、技能、进化链。
- `Engage转职计算器.html`：角色数据（含头像 base64）、职业基础值（Excel 缺此项）、成长率与上限。

生成 41 个角色、53 个职业、23 位纹章士、505 条技能、104 条进化链，并把头像抽出为 `avatars/char_NN.webp`。

更新数据后建议先跑 `./gradlew :core:test`。`golden.json` 是网页版 JS 引擎的输出快照，
若数据本身发生变化（而非算法），需要重新生成它；否则只要算法未改动，测试应保持全绿。

## CI / 发布

`.github/workflows/android.yml`：

- **push / PR**：跑 `:core:test`，构建 debug APK，上传为 artifact `engage-calculator-debug`（保留 14 天）。
- **推送 `v*` tag**：构建签名 release APK（`-PappVersionName` / `-PappVersionCode` 由 tag 推导），
  重命名为 `engage-calculator-<版本>.apk` 并发布 GitHub Release。

签名密钥通过以下 Secrets 提供（均为可选）：

| Secret | 说明 |
| --- | --- |
| `KEYSTORE_BASE64` | release keystore 的 base64 内容（`base64 -i release.keystore \| pbcopy`） |
| `KEYSTORE_PASSWORD` | keystore 密码 |
| `KEY_ALIAS` | key 别名 |
| `KEY_PASSWORD` | key 密码 |

未配置 `KEYSTORE_BASE64` 时，CI 会临时生成一个签名密钥，流程照样能跑通；
但每次构建的签名不同，仅适合内测，正式发布请配置固定密钥。

```bash
git tag v1.0.0 && git push origin v1.0.0
```

## 说明

本工具为非官方资料整理，数值以游戏内为准。计算结果为固定成长的逐级结算，不含随机成长。