package studio.bachelor.draft.utility.renderer.layer;

import android.util.Log;

import studio.bachelor.draft.utility.Position;

/**
 * Created by BACHELOR on 2016/03/03.
 */
public class ScaleLayer extends Layer {
    private final String TAG = "ScaleLayer";
    private float currentScale = 1.0f;

    public ScaleLayer(float width, float height) {
        super(width, height);
    }

    @Override
    public Position getPositionOfLayer(final Position screen_position) {
        Position original = super.getPositionOfLayer(screen_position); //取得目前screen與中心點的關係
        Position shift = super.getCenterOffset(); //取得中心點位移
        double x = (original.x - shift.x) / currentScale;
        double y = (original.y - shift.y) / currentScale;
        return new Position(x, y);
    }

    public void scale(float factor) {
        currentScale = currentScale + factor > 0.0f ? currentScale + factor : currentScale;
        Log.d(TAG, "currentScale: " + currentScale);
    }

    public float getScale() {
        return currentScale;
    }
}
