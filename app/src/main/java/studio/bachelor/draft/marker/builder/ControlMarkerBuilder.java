package studio.bachelor.draft.marker.builder;

import studio.bachelor.draft.marker.ControlMarker;
import studio.bachelor.draft.utility.Position;

/**
 * <code>ControlMarkerBuilder</code>為負責製造{@link studio.bachelor.draft.marker.ControlMarker}的Builder，目前並無專用之介面。
 */
public class ControlMarkerBuilder extends MarkerBuilder {
    /**
     * {@inheritDoc}
     */
    public ControlMarkerBuilder clearProductCache() {
        return (ControlMarkerBuilder)super.clearProductCache();
    }

    /**
     * {@inheritDoc}
     */
    protected void createProductIfNull() {
        if(product == null)
            product = new ControlMarker();
    }

    /**
     * {@inheritDoc}
     * @param position {@inheritDoc}
     * @return {@inheritDoc}
     */
    public ControlMarkerBuilder setPosition(Position position) {
        return (ControlMarkerBuilder)super.setPosition(position);
    }
}