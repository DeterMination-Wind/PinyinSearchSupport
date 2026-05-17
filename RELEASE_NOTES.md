# PinyinSearchSupport Changelog

## v2.0.0

架构重写：从"逐对话框打补丁"换成"一次性接管所有搜索框"。

### 新架构

- **通用搜索框分发器（FieldDispatcher）**：周期性扫描场景图，识别所有 `TextField` 类型的搜索框（按 `name` 含 search、`messageText` 命中翻译键、或同级有 `Icon.zoom` 三种启发判定），无需为每个对话框单独适配。
- **监听器拦截 + 反射换文本**：保留原版 `ChangeListener` 的所有行为，临时把 `TextField.text` 反射置空触发原版"空查询=显示全部"逻辑，再用我们自己的拼音匹配做后过滤。原版的搜索语义、滚动、外观全部不动。
- **Cell 全字段反射快照（CellSnapshot）**：替代旧版"new Cell().set()"的字段挑选式恢复，过滤后保留 `uniform/expand/colspan/pad` 等所有布局约束，蓝图等网格界面不会再错位。
- **作用域树（ScopeTree）**：自动定位搜索框对应的 `ScrollPane + Table`，区分 LIST / GRID / SECTIONED 三种布局模式分别过滤。

### 匹配增强

- **多音字（Heteronym）**：`重` 既能用 `chong` 也能用 `zhong` 命中。
- **首字母（Initials）**：例如 `lt` 命中"蓝图"、`fdj` 命中"发电机"，2–8 字符长度内启用，避免误判。
- **完整拼音 / 模糊（zh-z、ng-n）**：保留 1.x 行为。
- **PinyinIndex 缓存**：每个候选文本的 lower / 全拼 / 首字母 / 多音字 token 一次构建、复用。

### 设置

- 新增 `pss-initials`（首字母匹配）和 `pss-heteronym`（多音字匹配）两个开关，默认开启。
- 启用、模糊、延迟（毫秒）三项保持兼容。

### 兼容性

- 目标 Mindustry v154+，桌面 + 安卓双平台单一 jar。
- 移除旧实现 `PinyinSupport.java` / `SearchFieldPatcher.java` / `SearchTarget.java`。

### 已知限制

- 通过 `getListeners()` 接管搜索；如果某对话框的过滤逻辑不挂在 `ChangeListener` 上（极少见），将退回原版行为，不会破坏。

## v1.1.1

- 蓝图搜索界面特化：按原版列数重排过滤结果，修复拼音搜索后蓝图卡片错位/挤出屏幕问题。
- 构建产物命名统一为 `PinyinSearchSupport-V<version>` 风格，安卓包后缀保持 `-android.jar`。

## v1.1.0

- 修复拼音搜索导致列表滚动错位的问题（如蓝图搜索结果可能跑出屏幕）。
- 修复连续快速输入时可能出现的字符顺序错乱问题。
- 新增可配置搜索延迟：停止输入指定毫秒后才执行拼音搜索。
- 构建流程升级为同时产出 `zip`、桌面 `jar` 与 `android jar`，并用于自动发布。

## v1.0.0

- Added pinyin search support for in-game search fields.
- Added optional fuzzy pinyin matching (toggle in settings).
- Added split-syllable support (`pin'yin` == `pinyin`).
- Added pinyin+number mixed query support (`lan'tu4` -> `蓝图4`).
- Added automatic filtering hook for Mindustry search UI.
