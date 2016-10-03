package studio.bachelor.draft.utility.renderer.primitive;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import studio.bachelor.draft.utility.MapString;
import studio.bachelor.draft.utility.Position;
import studio.bachelor.draft.utility.Renderable;
import studio.bachelor.muninn.Muninn;
import studio.bachelor.muninn.R;

/**
 * Created by bachelor on 2016/3/8.
 */
public class Text implements Renderable {
    private float textSize;
    public final Position position;
    public final Paint paint = new Paint();
    public final String string;
    public final MapString mapString;

    {
        textSize = Muninn.getSizeSetting(R.string.key_marker_text_size, R.string.default_marker_text_size);
        String color = Muninn.getColorSetting(R.string.key_marker_text_color, R.string.default_marker_text_color);
        paint.setColor(Color.parseColor(color));
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        setTextSize(textSize);
    }

    public Text(String string) {
        position = new Position();
        this.string = string;
        mapString = null;
    }

    public Text(String string, Position position) {
        this.position = position;
        this.string = string;
        mapString = null;
    }

    public Text(MapString string) {
        position = new Position();
        this.string = null;
        mapString = string;
    }

    public Text(MapString string, Position position) {
        this.position = position;
        this.string = null;
        mapString = string;
    }

    public void setTextSize(float size) {
        this.textSize = size;
        paint.setTextSize(this.textSize);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if(mapString != null)
            canvas.drawText(mapString.getString(), (float)position.x, (float)position.y, paint);
        else
            canvas.drawText(string, (float)position.x, (float)position.y, paint);
    }
}
