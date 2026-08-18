# PinyinSearchSupport

> Search Chinese Mindustry content with the input you already use.

PinyinSearchSupport adds pinyin matching to in-game search fields. It is useful when you remember what an item or block is called but do not want to switch input methods or type the exact Chinese name.

The search enhancement stays in the client and does not change game content. It works especially well for players who use a non-Chinese keyboard layout or frequently search large lists of blocks, items, units, and schematics.

## Install

Download the release package and put it in Mindustry's mods directory, then enable it in-game.

## Build

~~~powershell
gradle clean zipMod jarMod jarAndroid
~~~

The generated desktop, universal, and Android artifacts are written to the project build output.
