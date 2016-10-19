package studio.bachelor.draft.marker;

import studio.bachelor.draft.utility.Position;

/**
 * <code>ControlMarker</code>用以輔助、控制其他{@link studio.bachelor.draft.marker.Marker}的結構。
 * 因此，<code>ControlMarker</code>必須總是附屬在其他{@link studio.bachelor.draft.marker.Marker}的控制之下；
 * 其中一個表現就是<code>ControlMarker</code>無法透過自身的Instance被移除。
 */
public class ControlMarker extends Marker {

    private Marker linksFatherMarker; // recording who is your daddy, link's relationship marker

    public ControlMarker() {
        super();
    }

    public ControlMarker(Position position) {
        super(position);
    }

    /**
     * <code>ControlMarker</code>並無法透過自身移除，必須藉由第三方進行。
     */
    @Override
    public void remove() {

    }

    public String getElementName() {
        return "ControlMarker";
    }

    public void setMarker(Marker linkFather) {
        this.linksFatherMarker = linkFather; //record link's father, marker which is MeasureMarker-type
    }

    public Marker getLinksFatherMarker() {
        return this.linksFatherMarker;
    }
}
