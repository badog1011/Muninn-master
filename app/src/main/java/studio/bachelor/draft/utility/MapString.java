package studio.bachelor.draft.utility;

/**
 * Created by BACHELOR on 2016/03/08.
 */
public class MapString {
    private final MapStringSupport referenceObject;

    public MapString(MapStringSupport reference) {
        this.referenceObject = reference;
    }

    public String getString() {
        return referenceObject.getObjectMappedString();
    }
}
