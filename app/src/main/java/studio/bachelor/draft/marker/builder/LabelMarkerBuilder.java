package studio.bachelor.draft.marker.builder;

import studio.bachelor.draft.marker.LabelMarker;

/**
 * Created by BACHELOR on 2016/03/10.
 */
public class LabelMarkerBuilder extends MarkerBuilder {
    protected void createProductIfNull() {
        if(product == null)
            product = new LabelMarker();
    }
}
