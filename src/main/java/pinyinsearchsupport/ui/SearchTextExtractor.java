package pinyinsearchsupport.ui;

import arc.scene.Element;
import arc.scene.Group;
import arc.scene.event.EventListener;
import arc.scene.ui.Label;
import arc.scene.ui.Tooltip;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;

public final class SearchTextExtractor{
    private SearchTextExtractor(){
    }

    public static String extract(Element element){
        if(element == null) return null;

        String tip = extractTooltipText(element);
        if(tip != null && !tip.isEmpty()) return tip;

        if(element instanceof Label){
            Label label = (Label)element;
            return label.getText().toString();
        }

        if(element instanceof Group){
            Group group = (Group)element;
            return findFirstLabelText(group);
        }

        return null;
    }

    private static String extractTooltipText(Element element){
        Seq<EventListener> listeners = element.getListeners();
        for(int i = 0; i < listeners.size; i++){
            EventListener l = listeners.get(i);
            if(l instanceof Tooltip){
                Tooltip tooltip = (Tooltip)l;
                Table cont = tooltip.getContainer();
                String text = findFirstLabelText(cont);
                if(text != null && !text.isEmpty()) return text;
            }
        }

        return null;
    }

    private static String findFirstLabelText(Element root){
        if(root instanceof Label){
            Label label = (Label)root;
            String t = label.getText().toString();
            if(t != null && !t.isEmpty()) return t;
        }

        if(root instanceof Group){
            Group group = (Group)root;
            Seq<Element> children = group.getChildren();
            for(int i = 0; i < children.size; i++){
                String found = findFirstLabelText(children.get(i));
                if(found != null && !found.isEmpty()) return found;
            }
        }

        return null;
    }
}
