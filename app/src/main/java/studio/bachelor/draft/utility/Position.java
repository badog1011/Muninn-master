package studio.bachelor.draft.utility;

import android.view.MotionEvent;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Created by BACHELOR on 2016/02/24.
 */
public class Position implements Metadata {
    public double x;
    public double y;

    public Position() {
        x = 0;
        y = 0;
    }

    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Position(MotionEvent event) {
        this.x = event.getX();
        this.y = event.getY();
    }

    public void set(Position position) {
        x = position.x;
        y = position.y;
    }

    public float getDistanceTo(Position point) {
        double dx = x - point.x;
        double dy = y - point.y;
        return (float)Math.sqrt(dx * dx + dy * dy);
    }

    public Node transformStateToDOMNode(Document document) {//?
        Element node = document.createElement(getElementName());
        setPositionNode(document, node);
        return node;
    }

    public String getElementName() {
        return "position";
    }

    private void setPositionNode(Document document, Element root) {
        Element node = document.createElement("x");
        node.appendChild(document.createTextNode("" + x));
        root.appendChild(node);
        node = document.createElement("y");
        node.appendChild(document.createTextNode("" + y));
        root.appendChild(node);
    }
}
