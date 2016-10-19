package studio.bachelor.draft.marker;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import studio.bachelor.draft.utility.Position;

/**
 * <code>LinkMarker</code>可連結單一{@link studio.bachelor.draft.marker.Marker}物件。
 */
public class LinkMarker extends Marker {
    /**
     * 連結的{@link studio.bachelor.draft.marker.Marker}。
     */
    protected Marker link; //此Marker，可以由子類別修改
//    private Marker linksFatherMarker; // recording who is your daddy, link's relationship marker

    public LinkMarker() {
        super();
    }

    public LinkMarker(Position position) {
        super(position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove() {
        link.remove(); //? by Jonas
        director.removeMarker(link); //delete by Jonas
        super.remove();
    }

    /**
     * 設定<code>LinkMarker</code>的連結。
     * @param marker 欲設定之連結。
     */
    public void setLink(Marker marker) {
        this.link = marker;
    }

//    public void setMarker(Marker linkFather) {
//        this.linksFatherMarker = linkFather; //record link's father, marker which is MeasureMarker-type
//    }

    public Marker getLink() {return this.link;}

    public String getElementName() {
        return "LinkMarker";
    }

    @Override
    public Node transformStateToDOMNode(Document document) {
        Node node = super.transformStateToDOMNode(document);
        Element element = document.createElement("link");
        element.appendChild(document.createTextNode("" + this.link.getID()));
        node.appendChild(element);
        return node;
    }
}
