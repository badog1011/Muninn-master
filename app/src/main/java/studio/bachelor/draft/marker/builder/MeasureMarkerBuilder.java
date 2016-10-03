package studio.bachelor.draft.marker.builder;

import studio.bachelor.draft.marker.LinkMarker;
import studio.bachelor.draft.marker.Marker;
import studio.bachelor.draft.marker.MeasureMarker;
import studio.bachelor.draft.utility.Position;

/**
 * Created by BACHELOR on 2016/03/08.
 */
public class MeasureMarkerBuilder extends LinkMarkerBuilder {
    /**
     * {@inheritDoc}
     */
    protected void createProductIfNull() {
        if(product == null)
            product = new MeasureMarker();
    }
}
