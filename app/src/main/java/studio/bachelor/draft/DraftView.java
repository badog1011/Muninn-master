package studio.bachelor.draft;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import studio.bachelor.draft.utility.motion.MasterHand;
import studio.bachelor.draft.utility.Position;

/**
 * Created by BACHELOR on 2016/02/24.
 */
public class DraftView extends View{
    private static final DraftDirector director = DraftDirector.instance;
    private MasterHand masterHand;

    public DraftView(Context context) {
        super(context);
        initializeMasterHand(context);
    }

    public DraftView(Context context, AttributeSet attribute_set) {
        super(context, attribute_set);
        initializeMasterHand(context);
    }

    private void initializeMasterHand(Context context) {
        director.setViewContext(context);
        masterHand = new MasterHand(context);
        setOnTouchListener(masterHand);
        setLongClickable(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int old_w, int old_h) {
        super.onSizeChanged(w, h, old_w, old_h);
        director.setToolboxRenderer(new Position(w / 10, 0), w - w / 10, h / 7.5f); //position, width of toolbox, height of toolbox
        director.setWidthAndHeight(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        director.render(canvas);
        invalidate(); //要求Android系統執行onDraw()
        return;
    }
}
