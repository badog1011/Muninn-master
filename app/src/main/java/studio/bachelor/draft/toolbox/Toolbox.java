package studio.bachelor.draft.toolbox;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import studio.bachelor.draft.DraftDirector;

/**
 * Created by BACHELOR on 2016/02/24.
 */
public class Toolbox{
    static private final DraftDirector director = DraftDirector.instance;
    static private final Toolbox instance = new Toolbox();
    static public Toolbox getInstance() {
        return instance;
    }
    public enum Tool {
        DELETER, MAKER_TYPE_LINK, MAKER_TYPE_ANCHOR, MARKER_TYPE_LABEL,
        PATH_MODE, CLEAR_PATH, EDIT_UNDO, EDIT_REDO
    }

    public final ArrayList<Tool> tools = new ArrayList<>(Arrays.asList(Tool.values()));

    private Toolbox() {

    }
}
