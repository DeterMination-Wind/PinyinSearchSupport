package pinyinsearchsupport.ui;

import arc.Core;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.event.ChangeListener;
import arc.scene.event.ChangeListener.ChangeEvent;
import arc.scene.event.EventListener;
import arc.scene.ui.Image;
import arc.scene.ui.TextField;
import arc.struct.ObjectMap;
import arc.struct.ObjectSet;
import arc.struct.Seq;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.gen.Icon;
import pinyinsearchsupport.PinyinSearchSupportMod;
import pinyinsearchsupport.PinyinSupport;

import java.util.Locale;

public class SearchFieldPatcher{
    private final ObjectSet<TextField> patched = new ObjectSet<>();
    private final ObjectMap<TextField, SearchTarget> targetCache = new ObjectMap<>();
    private final ObjectMap<TextField, Timer.Task> pendingFilters = new ObjectMap<>();

    public void patchNow(){
        if(Vars.headless || Core.scene == null || Core.scene.root == null) return;

        // Drop stale references from closed dialogs.
        Seq<TextField> stale = new Seq<>();
        for(TextField field : patched){
            if(field == null || field.getScene() == null){
                stale.add(field);
            }
        }
        for(TextField field : stale){
            cancelPendingFilter(field);
            patched.remove(field);
            targetCache.remove(field);
        }

        Seq<TextField> fields = new Seq<>();
        collectTextFields(Core.scene.root, fields);

        for(TextField field : fields){
            if(field == null) continue;
            if(patched.contains(field)) continue;
            if(!shouldPatch(field)) continue;

            patchField(field);
            patched.add(field);
        }
    }

    private static void collectTextFields(Element root, Seq<TextField> out){
        if(root instanceof TextField){
            out.add((TextField)root);
        }
        if(root instanceof Group){
            Group g = (Group)root;
            Seq<Element> children = g.getChildren();
            for(int i = 0; i < children.size; i++){
                collectTextFields(children.get(i), out);
            }
        }
    }

    private static boolean shouldPatch(TextField field){
        // Prefer explicit names.
        if(field.name != null){
            String n = field.name.toLowerCase(Locale.ROOT);
            if(n.contains("search")) return true;
        }

        // Fall back to known message strings.
        String msg = field.getMessageText();
        if(msg != null && !msg.isEmpty()){
            String players = Core.bundle.get("players.search", "");
            String editor = Core.bundle.get("editor.search", "");
            String save = Core.bundle.get("save.search", "");
            String schem = Core.bundle.get("schematic.search", "");
            if(msg.equals(players) || msg.equals(editor) || msg.equals(save) || msg.equals(schem)) return true;
        }

        // Heuristic for most search bars: a zoom icon in the same row/table.
        return hasZoomSibling(field);
    }

    private static boolean hasZoomSibling(TextField field){
        if(field == null || field.parent == null) return false;

        Seq<Element> siblings = field.parent.getChildren();
        for(int i = 0; i < siblings.size; i++){
            Element element = siblings.get(i);
            if(!(element instanceof Image)) continue;

            Image image = (Image)element;
            if(image.getDrawable() == Icon.zoom){
                return true;
            }
        }
        return false;
    }

    private void patchField(TextField field){
        ChangeListener hook = new ChangeListener(){
            @Override
            public void changed(ChangeEvent event, Element actor){
                if(actor != field) return;
                cancelPendingFilter(field);
                if(!Core.settings.getBool(PinyinSearchSupportMod.keyEnabled, true)) return;

                SearchTarget target = getOrFindTarget(field);
                if(target == null) return;

                String raw = field.getText();
                if(raw == null || raw.isEmpty()){
                    Core.app.post(() -> {
                        SearchTarget current = getOrFindTarget(field);
                        if(current != null) current.captureCurrentAsBase();
                    });
                    return;
                }

                if(!PinyinSupport.looksLikePinyinQuery(raw)) return;

                target.captureCurrentAsBaseIfMissing();

                int delayMs = Math.max(0, Core.settings.getInt(PinyinSearchSupportMod.keyDelayMs, PinyinSearchSupportMod.defaultDelayMs));
                String query = raw;

                Timer.Task task = Timer.schedule(new Timer.Task(){
                    @Override
                    public void run(){
                        Core.app.post(() -> {
                            if(pendingFilters.get(field) != this) return;
                            pendingFilters.remove(field);

                            if(field.getScene() == null) return;
                            if(!Core.settings.getBool(PinyinSearchSupportMod.keyEnabled, true)) return;

                            String live = field.getText();
                            if(live == null || live.isEmpty() || !live.equals(query)) return;
                            if(!PinyinSupport.looksLikePinyinQuery(live)) return;

                            SearchTarget latest = getOrFindTarget(field);
                            if(latest == null) return;

                            latest.applyFilter(live, Core.settings.getBool(PinyinSearchSupportMod.keyFuzzy, true));
                        });
                    }
                }, delayMs / 1000f);

                pendingFilters.put(field, task);
            }
        };

        // Ensure this runs before the default Table.field change listener.
        Seq<EventListener> listeners = field.getListeners();
        listeners.insert(0, hook);

        SearchTarget target = getOrFindTarget(field);
        if(target != null){
            target.captureCurrentAsBaseIfMissing();
        }
    }

    private void cancelPendingFilter(TextField field){
        Timer.Task task = pendingFilters.remove(field);
        if(task != null){
            task.cancel();
        }
    }

    private SearchTarget getOrFindTarget(TextField field){
        SearchTarget cached = targetCache.get(field);
        if(cached != null && cached.isValid()) return cached;

        SearchTarget found = SearchTarget.find(field);
        if(found != null) targetCache.put(field, found);
        return found;
    }
}
