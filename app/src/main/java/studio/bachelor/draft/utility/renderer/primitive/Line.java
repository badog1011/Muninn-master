package studio.bachelor.draft.utility.renderer.primitive;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.PreferenceManager;

import studio.bachelor.draft.utility.Position;
import studio.bachelor.draft.utility.Renderable;
import studio.bachelor.muninn.Muninn;
import studio.bachelor.muninn.R;

/**
 * Created by BACHELOR on 2016/03/01.
 */
public class Line implements Renderable {
    private float width;
    public final Position head;
    public final Position tail;
    public final Paint paint = new Paint();

    {
        width = Muninn.getSizeSetting(R.string.key_marker_line_width, R.string.default_marker_line_width);
        String color = Muninn.getColorSetting(R.string.key_marker_line_color, R.string.default_marker_line_color);
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(Color.parseColor(color));
        setWidth(width);
    }

    public Line() {
        head = new Position();
        tail = new Position();
    }

    public Line(Position head, Position tail) {
        this.head = head;
        this.tail = tail;
    }

    public void setWidth(float width) {
        this.width = width;
        paint.setStrokeWidth(this.width);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawLine((float)head.x, (float)head.y, (float)tail.x, (float)tail.y, paint);
    }
}
