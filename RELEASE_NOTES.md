# PinyinSearchSupport v2.3.2

## 中文

- 修复游戏内编辑器 HUD 方块搜索的拼音过滤：局部搜索面板现在可在 `hudGroup` / `menuGroup` 内安全定位结果列表，不再因全局 UI 边界而回退原版搜索。
- 搜索作用域会绑定输入框、父容器、可见状态、焦点和所属 UI 边界；界面切换、重挂载或候选结果面板不明确时会安全交回原版。
- 蓝图搜索同步采用同一作用域校验，避免延迟输入或弹窗切换时过滤到错误列表。

## English

- Fixed pinyin filtering for the in-game editor HUD block search. Local search panels can now safely locate their result list inside `hudGroup` / `menuGroup` instead of falling back to vanilla search at the global UI boundary.
- Search scopes now bind the input field, parent container, visibility, focus, and owning UI boundary. UI changes, reparenting, or ambiguous result panes safely fall back to vanilla behavior.
- Schematic search now uses the same scope validation, preventing delayed input or dialog changes from filtering the wrong list.
