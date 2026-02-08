package pinyinsearchsupport.ui;

import arc.Core;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import pinyinsearchsupport.PinyinSupport;

public class SearchTarget{
    private final TextField field;
    private final ScrollPane pane;
    private final Table table;
    private Seq<Entry> baseEntries = new Seq<>();

    private SearchTarget(TextField field, ScrollPane pane, Table table){
        this.field = field;
        this.pane = pane;
        this.table = table;
    }

    public boolean isValid(){
        return field != null && field.getScene() != null
            && pane != null && pane.getScene() != null
            && table != null && table.getScene() != null;
    }

    public void captureCurrentAsBase(){
        if(!isValid()) return;
        baseEntries = snapshotEntries();
    }

    public void captureCurrentAsBaseIfMissing(){
        if(baseEntries.isEmpty()){
            captureCurrentAsBase();
        }
    }

    public static SearchTarget find(TextField field){
        if(field == null || field.getScene() == null) return null;

        // Walk up a few levels and look for a ScrollPane whose widget is a Table.
        Group cursor = field.parent;
        for(int depth = 0; depth < 8 && cursor != null; depth++){
            Seq<ScrollPane> panes = new Seq<>();
            collectScrollPanes(cursor, panes);

            ScrollPane bestPane = null;
            Table bestTable = null;
            int bestSize = 0;

            for(ScrollPane sp : panes){
                if(sp == null) continue;
                Element w = sp.getWidget();
                if(!(w instanceof Table)) continue;
                Table t = (Table)w;

                // Avoid accidentally selecting a tooltip container.
                if(t.parent == null) continue;

                int size = t.getChildren().size;
                if(size > bestSize){
                    bestSize = size;
                    bestPane = sp;
                    bestTable = t;
                }
            }

            if(bestPane != null && bestTable != null){
                return new SearchTarget(field, bestPane, bestTable);
            }

            cursor = cursor.parent;
        }

        return null;
    }

    private static void collectScrollPanes(Element root, Seq<ScrollPane> out){
        if(root instanceof ScrollPane){
            out.add((ScrollPane)root);
        }
        if(root instanceof Group){
            Group g = (Group)root;
            Seq<Element> children = g.getChildren();
            for(int i = 0; i < children.size; i++){
                collectScrollPanes(children.get(i), out);
            }
        }
    }

    public void applyFilter(String rawQuery, boolean fuzzy, boolean schematicGridMode){
        if(!isValid()) return;
        if(rawQuery == null) rawQuery = "";

        // If query is empty, let the vanilla list (rebuilt by the original callback) show everything.
        if(rawQuery.isEmpty()) return;

        captureCurrentAsBaseIfMissing();
        if(baseEntries.isEmpty()) return;

        float scrollY = pane.getScrollY();

        table.clearChildren();

        int added = 0;
        int cols = schematicGridMode ? Math.max((int)(Core.graphics.getWidth() / Scl.scl(230f)), 1) : 1;
        int col = 0;
        for(int i = 0; i < baseEntries.size; i++){
            Entry entry = baseEntries.get(i);
            Element child = entry.element;
            if(child == null) continue;

            String text = SearchTextExtractor.extract(child);

            // Keep elements with no searchable text (rare) to avoid breaking layout.
            boolean keep = text == null || PinyinSupport.matches(rawQuery, text, fuzzy);

            if(keep){
                Cell<?> cell = table.add(child);
                cell.set(entry.constraints);
                added++;

                if(schematicGridMode){
                    if(++col % cols == 0){
                        table.row();
                    }
                }else if(entry.endRow){
                    table.row();
                }
            }
        }

        // For very small selection tables, show a consistent "none found" label.
        if(added == 0){
            table.add("@none.found").padLeft(54f).padTop(10f);
        }

        table.invalidateHierarchy();
        pane.layout();
        pane.setScrollYForce(Math.max(0f, Math.min(scrollY, pane.getMaxY())));
        pane.updateVisualScroll();
    }

    private Seq<Entry> snapshotEntries(){
        Seq<Entry> out = new Seq<>();
        Seq<Cell> cells = table.getCells();
        for(int i = 0; i < cells.size; i++){
            Cell<?> cell = cells.get(i);
            if(cell == null || cell.get() == null) continue;

            Cell<?> constraints = new Cell<>();
            constraints.set(cell);
            out.add(new Entry(cell.get(), constraints, cell.isEndRow()));
        }
        return out;
    }

    private static class Entry{
        final Element element;
        final Cell<?> constraints;
        final boolean endRow;

        Entry(Element element, Cell<?> constraints, boolean endRow){
            this.element = element;
            this.constraints = constraints;
            this.endRow = endRow;
        }
    }
}
