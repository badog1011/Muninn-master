package studio.bachelor.draft.marker;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.LinkedList;

import studio.bachelor.draft.utility.MapStringSupport;
import studio.bachelor.draft.utility.Position;

/**
 * Created by bachelor on 2016/3/8.
 */
public class AnchorMarker extends LinkMarker implements MapStringSupport {
    static private final AnchorMarker instance = new AnchorMarker();
    static public AnchorMarker getInstance() {return instance;}
    private double realDistance;
    public static LinkedList<Double> historyDistancesUndo = new LinkedList<Double>();
    public static LinkedList<Double> historyDistancesRedo = new LinkedList<Double>();

    private AnchorMarker() {
        super();
        this.link = new ControlMarker();
    }

    @Override
    public String getObjectMappedString() {
        return String.valueOf(realDistance);
    }

    public double getScale() {
        return realDistance / position.getDistanceTo(this.link.position);
    }

    public void setRealDistance(double real_distance) {
        this.realDistance = real_distance > 0.0 ? real_distance : 0.0;
    }

    public double getRealDistance() {
        return this.realDistance;
    }

    public String getElementName() {
        return "AnchorMarker";
    }

    @Override
    public Node transformStateToDOMNode(Document document) {
        Node node = super.transformStateToDOMNode(document);
        Element real_distance = document.createElement("real_distance");
        real_distance.appendChild(document.createTextNode("" + realDistance));
        node.appendChild(real_distance);
        return node;
    }
}
