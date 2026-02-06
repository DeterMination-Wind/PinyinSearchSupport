# PinyinSearchSupport

A Mindustry Java mod that adds pinyin search support for Chinese entries in in-game search fields.

## Features

- Supports fuzzy pinyin matching (configurable in settings)
- Supports split pinyin input (`pinyin` and `pin'yin` are treated the same)
- Supports mixed pinyin + numbers (`lan'tu4` -> `蓝图4`)

## Build

```bash
gradle jar
```

Generated file:

- `build/libs/PinyinSearchSupport.zip`

When built locally, the artifact is also copied to the workspace-level `构建` folder as:

- `PinyinSearchSupport-<version>.zip`
