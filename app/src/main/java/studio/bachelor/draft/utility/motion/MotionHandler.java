package studio.bachelor.draft.utility.motion;

import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import studio.bachelor.draft.DraftDirector;
import studio.bachelor.draft.marker.LinkMarker;
import studio.bachelor.draft.marker.Marker;
import studio.bachelor.draft.toolbox.Toolbox;
import studio.bachelor.draft.utility.DataStepByStep;
import studio.bachelor.draft.utility.Position;
import studio.bachelor.draft.utility.Selectable;
import studio.bachelor.muninn.Muninn;

/**
 * Created by BACHELOR on 2016/03/02.
 */
public class MotionHandler {
    private final String TAG = "MotionHandler";
    private static final DraftDirector director = DraftDirector.instance;
    public static MotionHandler getInstance() {
        return instance;
    }
    private static final MotionHandler instance = new MotionHandler();

    private MotionHandler() {

    }

    public enum Motion {
        DOWN,
        LONG_PRESS,
        LONG_PRESS_READY,
        SINGLE_TAP,
        DOUBLE_TAP,
        MOVE,
        PINCH_IN,
        PINCH_OUT,
        UP,
        FlING //按下，拋擲的動作
    }

    public void postMotion(Motion motion, Toolbox.Tool tool, Marker marker, Position position_first, Position position_second) {
        switch (motion) {
            case DOWN:
                director.createPathIfPathMode(position_first); //for PATH_MODE
                if (tool == null && marker == null) //當沒有選到tool與marker視為移動layer
                    director.moveLayerCreate(position_first);
                break;
            case MOVE:
                director.moveHoldMarker(position_first);
                director.recordPath(position_first); //for PATH_MODE
                director.moveLayerStart(position_first); //移動整個Layer
                break;
            case UP:
                director.endPath(position_first); //for PATH_MODE
                director.moveLayerStop();
                director.releaseMarker();
                director.deselectMarker();
                break;
            case LONG_PRESS:
                Log.d(TAG, "LONG_PRESS"); //After onShowPress()
                if(director.getTool() == Toolbox.Tool.DELETER) {
                    Log.d(TAG, "Delete Marker");
                    if(marker != null) {
                        DraftDirector.StepByStepUndo.addLast(new DataStepByStep(marker, Selectable.CRUD.DELETE));

                        marker.remove();
                    }

                }
                else {
                    Log.d(TAG, "Select Marker");
                    director.selectMarker(); //Step2
                    director.holdMarker(marker); //Step3
                }
                break;
            case LONG_PRESS_READY: //onShowPress
                director.selectingMarker(marker); //Step1: 在Motion: LongPress之前，會找最近的Marker
                Log.d(TAG, "LONG_PRESS_READY");
                break;
            case DOUBLE_TAP:
                director.addMarker(position_first);
                break;
            case SINGLE_TAP:
                if(tool != null) {
                    Muninn.sound_Ding.seekTo(0); //重至0毫秒
                    Muninn.sound_Ding.start();
                    director.selectTool(tool);

                }

                break;
            case PINCH_IN:
                director.zoomDraft(0.05f);
                break;
            case PINCH_OUT:
                director.zoomDraft(-0.025f);
                break;
            case FlING:
                Log.d(TAG, "FlING");
                double x = position_second.x - position_first.x;
                double y = position_second.y - position_first.y;
                Position offset = new Position(x, y);
                director.moveDraft(offset);
                break;
        }
    }
}
