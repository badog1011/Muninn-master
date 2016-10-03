package studio.bachelor.draft.utility;

/**
 * Created by BACHELOR on 2016/03/01.
 */
public interface Lockable {
    void lock();
    void unlock();
    boolean isLocked();
}
