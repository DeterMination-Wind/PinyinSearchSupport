# PinyinSearchSupport Changelog

## v2.2.0

修复通用拼音搜索在部分 Mindustry 搜索框中的误过滤与首字母匹配问题。

### 搜索框兼容性

- **战役区块搜索**：新增战役区块列表适配，支持按区块名拼音和首字母过滤，避免正确区块名仍显示"未找到"。
- **地图编辑器方块搜索**：继续走通用搜索框分发逻辑，修复首字母查询无法命中的问题。
- **Mod列表搜索**：识别并跳过 Mindustry 原生 mod 列表搜索框，避免拼音后过滤把结果清空。
- **查询判定收紧**：空查询和非字母查询交回原版处理，降低对普通 UI 搜索行为的干扰。

## v2.1.0

UI布局检测与搜索过滤系统优化，使得mod正式可用。

### 布局检测改进

- **按钮布局识别**：新增按钮计数和网格布局检测，改进 `LayoutMode.GRID` 和 `LayoutMode.SECTIONED` 的判断逻辑。
- **水平芯片行检测修复**：修复了水平芯片行检测逻辑，避免误排除真实结果面板。
- **布局模式检测增强**：支持更复杂的UI结构，包括按钮密集型和卡片布局。

### 搜索过滤系统重构

- **递归过滤**：`filterSectioned` 支持递归过滤子表格，保留头部结构。
- **匹配计数**：过滤方法现在返回匹配数量，便于判断是否显示"无结果"提示。
- **通用方法提取**：提取 `addOriginal`、`matchesActor`、`detectColumns` 等辅助方法，提高代码复用性。
- **头部保留**：新增 `isHeaderTable` 和 `isControlTable` 方法，智能识别和保留表格头部。

### 文本提取优化

- **统一文本拼接**：使用新的 `appendText` 方法统一处理文本拼接，减少重复代码。
- **工具提示集成**：`Label` 的文本提取现在同时包含工具提示内容。
- **简化提取逻辑**：优化 `Button` 和 `Group` 的文本提取逻辑。

### 内部优化

- **CellSnapshot字段过滤**：跳过计算布局位置和内部记账字段（如 `elementX`、`elementY`、`computedPadTop` 等），减少不必要的反射开销。

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
