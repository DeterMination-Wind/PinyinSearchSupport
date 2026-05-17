package pinyinsearchsupport.ui;

import arc.scene.Element;
import arc.scene.Group;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
import pinyinsearchsupport.match.MatchEngine;

public final class ScopeTree{
    public enum LayoutMode{ LIST, GRID, SECTIONED }

    private final Seq<SubScope> scopes;

    private ScopeTree(Seq<SubScope> scopes){
        this.scopes = scopes;
    }

    public boolean isValid(){
        if(scopes == null || scopes.isEmpty()) return false;
        for(int i = 0; i < scopes.size; i++){
            if(!scopes.get(i).isValid()) return false;
        }
        return true;
    }

    public static ScopeTree locate(TextField field){
        if(field == null || field.getScene() == null) return null;

        // Walk up to find a dialog root that contains at least one ScrollPane with a Table widget
        Group dialogRoot = null;
        Group cursor = field.parent;
        for(int depth = 0; depth < 12 && cursor != null; depth++){
            if(hasQualifyingScrollPane(cursor)){
                dialogRoot = cursor;
                break;
            }
            cursor = cursor.parent;
        }
        if(dialogRoot == null) return null;

        // BFS collect all ScrollPanes under dialogRoot
        Seq<ScrollPane> allPanes = new Seq<>();
        collectScrollPanes(dialogRoot, allPanes);

        // Filter: widget must be Table with >= 2 cells; skip horizontal chip rows
        Seq<ScrollPane> candidates = new Seq<>();
        for(int i = 0; i < allPanes.size; i++){
            ScrollPane sp = allPanes.get(i);
            Element w = sp.getWidget();
            if(!(w instanceof Table)) continue;
            Table t = (Table)w;
            if(t.getCells().size < 2) continue;
            // skip horizontal chip rows: height < 80 and not scrolling vertically
            if(sp.getHeight() < 80f && !sp.isScrollingDisabledY()) continue;
            candidates.add(sp);
        }

        if(candidates.isEmpty()) return null;

        // Sort by descendant count descending, take top 3
        candidates.sort((a, b) -> countDescendants(b.getWidget()) - countDescendants(a.getWidget()));
        int take = Math.min(3, candidates.size);

        Seq<SubScope> scopes = new Seq<>();
        for(int i = 0; i < take; i++){
            ScrollPane sp = candidates.get(i);
            Table t = (Table)sp.getWidget();
            LayoutMode mode = detectMode(t);
            scopes.add(new SubScope(sp, t, mode));
        }

        return new ScopeTree(scopes);
    }

    public void postFilter(String query, MatchEngine.MatchOptions opts){
        if(query == null || query.isEmpty()) return;
        SearchTextExtractor.invalidate();
        for(int i = 0; i < scopes.size; i++){
            scopes.get(i).filter(query, opts);
        }
    }

    // ---- helpers ----

    private static boolean hasQualifyingScrollPane(Group root){
        Seq<ScrollPane> panes = new Seq<>();
        collectScrollPanes(root, panes);
        for(int i = 0; i < panes.size; i++){
            ScrollPane sp = panes.get(i);
            Element w = sp.getWidget();
            if(w instanceof Table && ((Table)w).getCells().size >= 4) return true;
        }
        return false;
    }

    private static void collectScrollPanes(Element root, Seq<ScrollPane> out){
        if(root instanceof ScrollPane) out.add((ScrollPane)root);
        if(root instanceof Group){
            Seq<Element> ch = ((Group)root).getChildren();
            for(int i = 0; i < ch.size; i++) collectScrollPanes(ch.get(i), out);
        }
    }

    private static int countDescendants(Element root){
        if(root == null) return 0;
        if(!(root instanceof Group)) return 1;
        int count = 1;
        Seq<Element> ch = ((Group)root).getChildren();
        for(int i = 0; i < ch.size; i++) count += countDescendants(ch.get(i));
        return count;
    }

    private static LayoutMode detectMode(Table t){
        Seq<Cell> cells = t.getCells();
        if(cells.isEmpty()) return LayoutMode.LIST;

        // Check if most top-level actors are Tables (SECTIONED)
        int tableCount = 0;
        for(int i = 0; i < cells.size; i++){
            if(cells.get(i).get() instanceof Table) tableCount++;
        }
        if(tableCount > cells.size / 2) return LayoutMode.SECTIONED;

        // Check for GRID: any consecutive cells without endRow
        for(int i = 0; i < cells.size - 1; i++){
            if(!cells.get(i).isEndRow()) return LayoutMode.GRID;
        }
        return LayoutMode.LIST;
    }

    // ---- SubScope ----

    private static final class SubScope{
        final ScrollPane pane;
        final Table table;
        final LayoutMode mode;

        SubScope(ScrollPane pane, Table table, LayoutMode mode){
            this.pane = pane;
            this.table = table;
            this.mode = mode;
        }

        boolean isValid(){
            return pane != null && pane.getScene() != null
                && table != null && table.getScene() != null;
        }

        void filter(String query, MatchEngine.MatchOptions opts){
            if(!isValid()) return;

            Seq<Cell> cells = table.getCells();
            if(cells.isEmpty()) return;

            // Snapshot all cells
            int n = cells.size;
            Element[] actors = new Element[n];
            CellSnapshot[] snaps = new CellSnapshot[n];
            boolean[] endRows = new boolean[n];
            for(int i = 0; i < n; i++){
                Cell<?> c = cells.get(i);
                actors[i] = c.get();
                snaps[i] = CellSnapshot.capture(c);
                endRows[i] = c.isEndRow();
            }

            float scrollY = pane.getScrollY();

            table.clearChildren();

            if(mode == LayoutMode.SECTIONED){
                filterSectioned(actors, snaps, endRows, n, query, opts);
            }else if(mode == LayoutMode.GRID){
                filterGrid(actors, snaps, endRows, n, query, opts);
            }else{
                filterList(actors, snaps, endRows, n, query, opts);
            }

            table.invalidateHierarchy();
            try{
                pane.layout();
            }catch(Throwable t){
                Log.warn("[PinyinSearchSupport] pane.layout() threw: @", t.getMessage());
            }
            float maxY = pane.getMaxY();
            pane.setScrollYForce(Math.max(0f, Math.min(scrollY, maxY)));
            pane.updateVisualScroll();
        }

        private void filterList(Element[] actors, CellSnapshot[] snaps, boolean[] endRows, int n,
                                String query, MatchEngine.MatchOptions opts){
            int added = 0;
            for(int i = 0; i < n; i++){
                Element actor = actors[i];
                if(actor == null) continue;
                String text = SearchTextExtractor.extract(actor);
                boolean keep = text == null || MatchEngine.accepts(text, query, opts);
                if(keep){
                    Cell<?> cell = table.add(actor);
                    if(snaps[i] != null) snaps[i].applyTo(cell);
                    if(endRows[i]) table.row();
                    added++;
                }
            }
            if(added == 0){
                table.add("@none.found").padLeft(54f).padTop(10f);
            }
        }

        private void filterGrid(Element[] actors, CellSnapshot[] snaps, boolean[] endRows, int n,
                                String query, MatchEngine.MatchOptions opts){
            // Detect column count from original layout
            int cols = 1;
            for(int i = 0; i < n; i++){
                if(endRows[i]){ cols = i + 1; break; }
            }
            if(cols < 1) cols = 1;

            int added = 0;
            int col = 0;
            for(int i = 0; i < n; i++){
                Element actor = actors[i];
                if(actor == null) continue;
                String text = SearchTextExtractor.extract(actor);
                boolean keep = text == null || MatchEngine.accepts(text, query, opts);
                if(keep){
                    Cell<?> cell = table.add(actor);
                    if(snaps[i] != null) snaps[i].applyTo(cell);
                    col++;
                    if(col % cols == 0){
                        table.row();
                        col = 0;
                    }
                    added++;
                }
            }
            if(col > 0) table.row();
            if(added == 0){
                table.add("@none.found").padLeft(54f).padTop(10f);
            }
        }

        private void filterSectioned(Element[] actors, CellSnapshot[] snaps, boolean[] endRows, int n,
                                     String query, MatchEngine.MatchOptions opts){
            // Each top-level actor that is a Table is a section; recurse into it
            int added = 0;
            for(int i = 0; i < n; i++){
                Element actor = actors[i];
                if(actor == null) continue;
                if(actor instanceof Table){
                    Table section = (Table)actor;
                    boolean sectionHasMatch = filterSectionTable(section, query, opts);
                    if(sectionHasMatch){
                        Cell<?> cell = table.add(actor);
                        if(snaps[i] != null) snaps[i].applyTo(cell);
                        if(endRows[i]) table.row();
                        added++;
                    }
                }else{
                    // non-table cell (e.g. section header label): keep if any section below matches
                    // we keep it unconditionally to preserve headers
                    Cell<?> cell = table.add(actor);
                    if(snaps[i] != null) snaps[i].applyTo(cell);
                    if(endRows[i]) table.row();
                    added++;
                }
            }
            if(added == 0){
                table.add("@none.found").padLeft(54f).padTop(10f);
            }
        }

        private boolean filterSectionTable(Table section, String query, MatchEngine.MatchOptions opts){
            Seq<Cell> cells = section.getCells();
            if(cells.isEmpty()) return false;
            for(int i = 0; i < cells.size; i++){
                Element actor = cells.get(i).get();
                if(actor == null) continue;
                String text = SearchTextExtractor.extract(actor);
                if(text == null || MatchEngine.accepts(text, query, opts)) return true;
            }
            return false;
        }
    }
}
