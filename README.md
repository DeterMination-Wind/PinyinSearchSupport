# PinyinSearchSupport

A Mindustry Java mod that adds pinyin search support for Chinese entries in in-game search fields.

## Features

- Supports fuzzy pinyin matching (configurable in settings)
- Supports split pinyin input (`pinyin` and `pin'yin` are treated the same)
- Supports mixed pinyin + numbers (`lan'tu4` -> `蓝图4`)
- Supports configurable delayed search trigger (search runs after typing stops)

## Build

```bash
gradle clean zipMod jarMod jarAndroid
```

Generated files:

- `build/libs/PinyinSearchSupport.zip`
- `build/libs/PinyinSearchSupport.jar`
- `build/libs/PinyinSearchSupport-android.jar`

When built locally, the artifact is also copied to the workspace-level `构建` folder as:

- `PinyinSearchSupport-<version>.zip`
- `PinyinSearchSupport-<version>.jar`
- `PinyinSearchSupport-<version>-android.jar`
