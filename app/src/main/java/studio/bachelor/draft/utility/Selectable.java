package studio.bachelor.draft.utility;

/**
 * Created by BACHELOR on 2016/03/02.
 */
public interface Selectable extends Targetable {
    enum State {
        UNSELECTED, SELECTED, SELECTING
    }

    enum CRUD {
        CREATE, READ, UPDATE, DELETE, UNKNOWN
    }

    State getSelectionState();
    void select();
    void deselect();
    void selecting();
}
