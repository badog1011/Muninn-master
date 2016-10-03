package studio.bachelor.draft.utility.renderer.primitive;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

import studio.bachelor.draft.utility.Position;
import studio.bachelor.draft.utility.Renderable;
import studio.bachelor.muninn.Muninn;

/**
 * Created by BACHELOR on 2016/03/01.
 */
public class Icon implements Renderable {
    private int resource;
    private float scale = 1.0f;
    public final Position position;
    private Bitmap bitmap;
    public final Paint paint = new Paint();

    {
        paint.setAntiAlias(true);
    }

    public Icon(int resource) {
        position = new Position();
        this.resource = resource;
        createBitmap();
    }

    public Icon(Position position, int resource) {
        this.position = position;
        this.resource = resource;
        createBitmap();
    }

    private void createBitmap() {
        bitmap = BitmapFactory.decodeResource(Muninn.getContext().getResources(), this.resource);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, (float)position.x - bitmap.getWidth() / 2, (float)position.y - bitmap.getHeight() / 2, paint);
    }
}
