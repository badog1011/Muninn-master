package studio.bachelor.draft.utility;

import java.util.ArrayList;
import java.util.LinkedList;

import studio.bachelor.draft.marker.Marker;
import studio.bachelor.draft.utility.Selectable.CRUD;

/**
 * Created by 奕豪 on 2016/10/17.
 */
public class DataStepByStep {
    private Marker marker = null;
    private CRUD state = CRUD.UNKNOWN;
//    public LinkedList<Position> historyTapPositionsUndo = new LinkedList<Position>();
//    public int historyIndex = 0;
    public DataStepByStep(Marker marker, CRUD state) {
        this.marker = marker;
        this.state = state;
    }

    public Marker getMarker() {
        return marker;
    }

    public CRUD getCRUDstate() {
        return state;
    }

    public void setCRUDstate(CRUD newState) {
        this.state = newState;
    }



}
