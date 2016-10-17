package studio.bachelor.draft.utility.renderer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.LinkedList;
import java.util.List;

import studio.bachelor.draft.marker.Marker;
import studio.bachelor.draft.utility.Renderable;
import studio.bachelor.draft.utility.renderer.layer.Layer;
import studio.bachelor.muninn.Muninn;
import studio.bachelor.muninn.R;

/**
 * Created by BACHELOR on 2016/02/24.
 * 處裡線條參數
 */
public class MarkerRenderer implements Renderable {
    private Marker reference;
    //private Layer layer;
    public final List<Renderable> primitives = new LinkedList<Renderable>();
    private final Paint paint = new Paint();
    private float selectionWidth;
    private int selectingColor;
    private int selectedColor;

    {
        selectionWidth = Muninn.getSizeSetting(R.string.key_marker_selection_radius, R.string.default_marker_selection_radius);
        String color = Muninn.getColorSetting(R.string.key_marker_selecting_color, R.string.default_marker_selecting_color);
        selectingColor = Color.parseColor(color);
        color = Muninn.getColorSetting(R.string.key_marker_selected_color, R.string.default_marker_selected_color);
        selectedColor = Color.parseColor(color);
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(selectionWidth);
    }

    public MarkerRenderer() {

    }

    public void setReference(Marker reference) {
        this.reference = reference;
    }

    public void onDraw(Canvas canvas) {
        reference.update();
        for(Renderable primitive : primitives) {
            primitive.onDraw(canvas);
        }
        if(reference != null) {
            switch (reference.getSelectionState()) {
                case SELECTING:
                    paint.setColor(selectingColor);
                    canvas.drawPoint((float) reference.position.x, (float) reference.position.y, paint);
                    break;
                case SELECTED:
                    paint.setColor(selectedColor);
                    canvas.drawPoint((float) reference.position.x, (float) reference.position.y, paint);
                    break;
                case UNSELECTED:
                    break;
            }
        }
    }
}
