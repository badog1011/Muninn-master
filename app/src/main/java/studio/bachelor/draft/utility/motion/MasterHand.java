package studio.bachelor.draft.utility.motion;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import studio.bachelor.draft.Draft;
import studio.bachelor.draft.DraftDirector;
import studio.bachelor.draft.marker.Marker;
import studio.bachelor.draft.toolbox.Toolbox;
import studio.bachelor.draft.utility.Position;

/**
 * Created by BACHELOR on 2016/03/01.
 */
public class MasterHand implements
        View.OnTouchListener,
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener,
        ScaleGestureDetector.OnScaleGestureListener {
    private static final MotionHandler handler = MotionHandler.getInstance();
    private static final DraftDirector director = DraftDirector.instance;
    private final GestureDetector gestureDetector;
    private final ScaleGestureDetector scaleGestureDetector;


    public MasterHand(Context context) {
        gestureDetector = new GestureDetector(context, this);
        scaleGestureDetector = new ScaleGestureDetector(context, this);
    }

    private void postMotion(MotionHandler.Motion motion, MotionEvent event1, MotionEvent event2) {
        Position position_first = null;
        Position position_second = null;
        if(event1 != null)
            position_first = new Position(event1);

        if(event2 != null)
            position_second = new Position(event2);

        Toolbox.Tool tool = null;
        Marker marker = null;
        if(position_first != null) {
            tool = director.getNearestTool(position_first);
            marker = director.getNearestMarker(position_first);
        }
        handler.postMotion(motion, tool, marker, position_first, position_second);
    }

    private void postMotion(MotionHandler.Motion motion, Position position_first, Position position_second) {
        Toolbox.Tool tool = null;
        Marker marker = null;
        if(position_first != null) {
            tool = director.getNearestTool(position_first);
            marker = director.getNearestMarker(position_first);
        }
        handler.postMotion(motion, tool, marker, position_first, position_second);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                postMotion(MotionHandler.Motion.DOWN, event, null);
                break;
            case MotionEvent.ACTION_MOVE:
                postMotion(MotionHandler.Motion.MOVE, event, null);
                break;
            case MotionEvent.ACTION_UP:
                postMotion(MotionHandler.Motion.UP, event, null);
                break;
        }
        gestureDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocity_x, float velocity_y) {
        Position zero = new Position(); //(x, y) = (0, 0)
        double velocity = zero.getDistanceTo(new Position(velocity_x, velocity_y)); //?Jonas 每秒在x, y 軸移動的距離(pixels)
        if(velocity > 5000)
            postMotion(MotionHandler.Motion.FlING, event1, event2);
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        postMotion(MotionHandler.Motion.LONG_PRESS, event, null);
    }

    @Override
    public boolean onScroll(MotionEvent event1, MotionEvent event2, float distance_x, float distance_y) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent event) { //The user has performed a down MotionEvent and not performed a move or up yet
        postMotion(MotionHandler.Motion.LONG_PRESS_READY, event, null);
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {

        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        postMotion(MotionHandler.Motion.DOUBLE_TAP, event, null);
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {

        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        postMotion(MotionHandler.Motion.SINGLE_TAP, event, null);
        return true;
    }

    private Position getFocusPosition(ScaleGestureDetector detector) {
        float x = detector.getCurrentSpanX();
        float y = detector.getCurrentSpanX();
        return new Position(x, y);
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector)
    {
        float scale = detector.getScaleFactor();
        Position position = getFocusPosition(detector);
        Log.d("EVENT", "onScale = " + scale);
        if(scale > 1.2) {
            postMotion(MotionHandler.Motion.PINCH_IN, position, null); //放大，雙指拉開
        }
        else if(scale < 0.8) {
            postMotion(MotionHandler.Motion.PINCH_OUT, position, null); //縮小，雙指拉近
        }
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector)
    {

        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector)
    {

    }
}
