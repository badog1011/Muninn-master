package studio.bachelor.draft.marker;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import studio.bachelor.draft.utility.MapStringSupport;
import studio.bachelor.draft.utility.Position;

/**
 * Created by BACHELOR on 2016/03/08.
 */
public class MeasureMarker extends LinkMarker implements MapStringSupport {
    private double distance;

    public MeasureMarker() {
        super();
    }

    public MeasureMarker(Position position) {
        super(position);
    }

    @Override
    public String getObjectMappedString() {
        String distance_str = null;
        if(!Double.isNaN(distance)) {
            distance_str = String.format("%.2f", distance);
        }
        else {
            distance_str = " ";
        }
        return distance_str;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove() {
        super.remove();
    }

    @Override
    public void update() {
        super.update();
        AnchorMarker anchor = AnchorMarker.getInstance();
        distance = position.getDistanceTo(link.position) * anchor.getScale();
    }

    public String getElementName() {
        return "MeasureMarker";
    }

    @Override
    public Node transformStateToDOMNode(Document document) {
        Node node = super.transformStateToDOMNode(document);
        Element element = document.createElement("distance");
        element.appendChild(document.createTextNode(""+ distance));
        node.appendChild(element);
        return node;
    }
}
