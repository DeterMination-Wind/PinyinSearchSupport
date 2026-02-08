# PinyinSearchSupport Changelog

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
