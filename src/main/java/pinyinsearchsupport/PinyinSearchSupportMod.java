package pinyinsearchsupport;

import arc.Events;
import arc.util.Timer;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.gen.Icon;
import mindustry.mod.Mod;
import mindustry.ui.dialogs.SettingsMenuDialog;
import pinyinsearchsupport.ui.SearchFieldPatcher;

import static mindustry.Vars.headless;
import static mindustry.Vars.ui;

public class PinyinSearchSupportMod extends Mod{
    // Settings keys (stored in Core.settings).
    public static final String keyEnabled = "pss-enabled";
    public static final String keyFuzzy = "pss-fuzzy";
    public static final String keyDelayMs = "pss-delay-ms";

    public static final int defaultDelayMs = 180;

    private final SearchFieldPatcher patcher = new SearchFieldPatcher();

    public PinyinSearchSupportMod(){
        Events.on(ClientLoadEvent.class, e -> {
            if(headless) return;

            registerSettings();

            // Patch search fields that already exist, and keep patching newly created ones.
            patcher.patchNow();
            Timer.schedule(() -> patcher.patchNow(), 0.25f, 0.5f);
        });
    }

    private void registerSettings(){
        if(ui == null || ui.settings == null) return;

        ui.settings.addCategory("@pss.category", Icon.zoom, table -> {
            SettingsMenuDialog.SettingsTable st = (SettingsMenuDialog.SettingsTable)table;

            st.checkPref(keyEnabled, true);
            st.checkPref(keyFuzzy, true);
            st.sliderPref(keyDelayMs, defaultDelayMs, 0, 1500, 10, value -> value + " ms");
        });
    }
}
