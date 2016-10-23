package studio.bachelor.draft.utility.renderer.layer;

import android.util.Log;

import studio.bachelor.draft.marker.Marker;
import studio.bachelor.draft.marker.MarkerManager;
import studio.bachelor.draft.utility.Position;

/**
 * Created by BACHELOR on 2016/03/03.
 */
public class Layer{
    private String TAG = this.getClass().toString();
    private float width;
    private float height;
    private final Position center;
    private final Position centerOffset = new Position();
    public final MarkerManager markerManager = new MarkerManager();

    public Position getPositionOfLayer(final Position screen_position) {
        Log.d(TAG, "(getPositionOfLayer) center: (" + center.x + ", " + center.y + ")");
        double x = screen_position.x - center.x;
        double y = screen_position.y - center.y;
        Log.d(TAG, "(getPositionOfLayer) (x,y): (" + x + ", " + y + ")");
        return new Position(x, y);
    }

    public Layer(float width, float height) {
        this.height = height;
        this.width = width;
        center = new Position(width / 2, height / 2);
        Log.d(TAG, "Layer) center: (" + center.x + ", " + center.y + ")");
    }

    public void setWidthAndHeight(float width, float height) { //包含Marker位置
        this.height = height;
        this.width = width;
        center.set(new Position(this.width / 2, this.height / 2));
        Log.d(TAG, "setWidthAndHeight) center: (" + center.x + ", " + center.y + ")");
        for(Marker marker : markerManager.markers) {
            marker.position.set(getPositionOfLayer(marker.position));
        }

    }

    public Position getCenter() {
        return center;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public Position getCenterOffset() {
        return centerOffset;
    }

    public Position getTranslate() { //中心點的總位移
        return new Position(center.x + centerOffset.x, center.y + centerOffset.y);
    }

    public void moveLayer(Position offset) {
        double x = offset.x + centerOffset.x;
        double y = offset.y + centerOffset.y;
        Position stack_offset = new Position(x, y);
        centerOffset.set(stack_offset);
    }
}
