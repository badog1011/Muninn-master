package studio.bachelor.draft.marker;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import studio.bachelor.draft.utility.MapStringSupport;
import studio.bachelor.draft.utility.renderer.layer.Layer;

/**
 * Created by BACHELOR on 2016/03/10.
 */
public class LabelMarker extends Marker implements MapStringSupport {
    private String label;

    public LabelMarker() {
        label = new String();
    }

    public LabelMarker(String string) {
        label = string;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String getObjectMappedString() {
        return String.valueOf(label);
    }

    public String getElementName() {
        return "LabelMarker";
    }

    @Override
    public Node transformStateToDOMNode(Document document) {
        Node node = super.transformStateToDOMNode(document);
        Element element = document.createElement("label");
        element.appendChild(document.createTextNode(label));
        node.appendChild(element);
        return node;
    }
}
