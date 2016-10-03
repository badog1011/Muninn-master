package studio.bachelor.draft.utility.renderer.primitive;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import studio.bachelor.draft.utility.Position;
import studio.bachelor.draft.utility.Renderable;
import studio.bachelor.muninn.Muninn;
import studio.bachelor.muninn.R;

/**
 * Created by BACHELOR on 2016/03/01.
 */
public class Point implements Renderable {
    private float radius;
    public final Position position;
    public final Paint paint = new Paint();

    {
        radius = Muninn.getSizeSetting(R.string.key_marker_point_radius, R.string.default_marker_point_radius);
        String color = Muninn.getColorSetting(R.string.key_marker_point_color, R.string.default_marker_point_color);
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(Color.parseColor(color));
        setRadius(radius);
    }

    public Point() {
        position = new Position();
    }

    public Point(Position position) {
        this.position = position;
    }

    public void setRadius(float radius) {
        this.radius = radius;
        paint.setStrokeWidth(this.radius * 2);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawPoint((float)position.x, (float)position.y, paint);
    }
}
