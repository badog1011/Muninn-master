package studio.bachelor.draft.marker.builder;

import studio.bachelor.draft.marker.ControlMarker;
import studio.bachelor.draft.marker.LinkMarker;
import studio.bachelor.draft.marker.Marker;
import studio.bachelor.draft.utility.Builder;
import studio.bachelor.draft.utility.Position;

/**
 * <code>LinkMarkerBuilder</code>為負責製造{@link studio.bachelor.draft.marker.LinkMarker}的Builder，具備專門的介面{@link #setLink(Marker)}。
 */
public class LinkMarkerBuilder extends MarkerBuilder{
    /**
     * {@inheritDoc}
     */
    protected void createProductIfNull() {
        if(product == null)
            product = new LinkMarker();
    }

    /**
     * {@inheritDoc}
      * @param position {@inheritDoc}
     * @return {@inheritDoc}
     */
    public LinkMarkerBuilder setPosition(Position position){
        return (LinkMarkerBuilder)super.setPosition(position);
    }

    /**
     *為{@link studio.bachelor.draft.marker.LinkMarker}設定連結對象。
     * @param marker 欲連結之{@link studio.bachelor.draft.marker.Marker}。
     * @return Builder自身。
     */
    public LinkMarkerBuilder setLink(Marker marker){
        createProductIfNull();
        ((LinkMarker)product).setLink(marker);
        ((ControlMarker)marker).setMarker(product); //tell linked marker who is his daddy
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public LinkMarkerBuilder clearProductCache() {
        return (LinkMarkerBuilder)super.clearProductCache();
    }
}
