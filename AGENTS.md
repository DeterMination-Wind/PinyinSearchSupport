# AGENTS.md - PinyinSearchSupport-repo 模组说明

## 文件结构（当前仓库）
```text
PinyinSearchSupport-repo/
|-- .github/
|   \-- workflows/
|       \-- release.yml
|-- Detail.md
|-- dist/
|-- gradlew
|-- src/
|   \-- main/
|       |-- java/
|       \-- resources/
|           \-- bundles/
|-- .gitignore
|-- AGENTS.md
|-- build.gradle
|-- LICENSE
|-- mod.json
|-- README.md
|-- RELEASE_NOTES.md
\-- settings.gradle
```

## 维护约束
- 保持 Java 8 兼容（如本项目包含 Java 源码）。
- 变更优先聚焦性能与可读性，不做无关重构。
- 用户可见文案优先走 bundle/资源文件，不硬编码。

命令操作请使用 PowerShell 7（`pwsh`）。
