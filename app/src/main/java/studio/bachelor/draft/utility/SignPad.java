package studio.bachelor.draft.utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by BACHELOR on 2016/04/06.
 */
public class SignPad extends View {
    private Path signPath = new Path();
    private Paint paint = new Paint();

    {
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5.0f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    public SignPad(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SignPad(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(signPath, paint);
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touch_x = event.getX();
        float touch_y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                signPath.moveTo(touch_x, touch_y);
                break;
            case MotionEvent.ACTION_MOVE:
                signPath.lineTo(touch_x, touch_y);
                break;
            case MotionEvent.ACTION_UP:
                signPath.lineTo(touch_x, touch_y);
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    public Bitmap exportBitmapRenderedOnCanvas() {
        Bitmap return_bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(return_bitmap);
        canvas.drawPath(signPath, paint);
        return return_bitmap;
    }
}