package studio.bachelor.draft.marker;

import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.LinkedList;

import studio.bachelor.draft.DraftDirector;
import studio.bachelor.draft.utility.Lockable;
import studio.bachelor.draft.utility.Metadata;
import studio.bachelor.draft.utility.Removable;
import studio.bachelor.draft.utility.Selectable;
import studio.bachelor.draft.utility.Touchable;
import studio.bachelor.draft.utility.Position;
import studio.bachelor.draft.utility.renderer.layer.Layer;

/**
 * <code>Marker</code>，作為<code>Draft</code>上所顯示的標記。
 */
public abstract class Marker implements Lockable, Touchable, Selectable, Removable, Metadata {
    /** <code>position</code>將以<code>Draft</code>中心為基準點。 */
    public final Position position = new Position();
    public Position refreshed_tap_position = new Position();
    public LinkedList<Position> historyTapPositionsUndo = new LinkedList<Position>();
    public LinkedList<Position> historyTapPositionsRedo = new LinkedList<Position>();

    public Position refreshed_Layer_position = new Position();
    public LinkedList<Position> historyLayerPositionsUndo = new LinkedList<Position>();
    public LinkedList<Position> historyLayerPositionsRedo = new LinkedList<Position>();

    protected static DraftDirector director = DraftDirector.instance;
    private boolean locked = false;
    private int ID;
    /**
     * 目前的選取狀態({@link studio.bachelor.draft.utility.Selectable.State})，預設為未選取。
     */
    private State selectionState = State.UNSELECTED;

//    private CRUD crud = CRUD.UNKNOWN; //此Marker處裡狀況

    public Marker() {
        ID = director.allocateObjectID();
    }

    public Marker(Position position) {
        this.position.x = position.x;
        this.position.y = position.y;
    }

//    public void updateCRUDstate(CRUD state) {
//        this.crud = state;
//    }
//
//    public CRUD getCRUDstate() {
//        return this.crud;
//    }

    public void update() {
        return;
    }

    /**
     * 自{@link studio.bachelor.draft.marker.MarkerManager}移除自身instance。
     */
    public void remove() {
        director.removeMarker(this);
        Log.d("Marker", "remove()");
    }

    /**
     * 將<code>Marker</code>移動到特定座標。
     * @param position 欲設定之座標。
     */
    public void move(Position position) {
        if(locked)
            return;
        this.position.set(position);
    }

    /**
     * 確認該座標是否能夠觸碰到<code>Marker</code>。
     * @param position 測試座標。
     * @param threshold 觸碰<code>Marker</code>的門檻值，距離必須低於本數值才會回傳為<code>True</code>。
     */
    public boolean canBeTouched(Position position, double threshold) {
        return getDistanceTo(position) < threshold;
    }

    /**
     * 取得<code>Marker</code>與座標之距離。
     */
    public double getDistanceTo(Position position) {
        return this.position.getDistanceTo(position);
    }

    @Override
    public void lock() {
        locked = true;
    }

    @Override
    public void unlock() {
        locked = false;
    }

    public boolean isLocked() {
        return locked;
    }

    /**
     * 取得目前<code>Marker</code>的選取狀態。
     */
    public State getSelectionState() {
        return selectionState;
    }

    /**
     * 選擇本<code>Marker</code>。
     */
    public void select() {
        selectionState = State.SELECTED;
    }

    /**
     * 解除本<code>Marker</code>的選取。
     */
    public void deselect() {
        selectionState = State.UNSELECTED;
    }

    /**
     * 正在嘗試選取本Marker，但尚未選取成功。
     */
    public void selecting() {
        selectionState = State.SELECTING;
    }

    public Node transformStateToDOMNode(Document document) {
        Element node = document.createElement(getElementName());
        node.setAttribute("ID", "" + ID);
        node.appendChild(position.transformStateToDOMNode(document));
        return node;
    }

    public int getID() {
        return ID;
    }

//    public void changeCRUDstate(CRUD state) {
//        this.crud = state;
//    }
}
