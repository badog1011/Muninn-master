package studio.bachelor.draft.utility;

/**
 * Created by BACHELOR on 2016/03/02.
 */
public interface TouchableGroup extends Touchable {
    boolean inGroupRange(Position position);
    boolean canBeTouched(Position position, double threshold);
    double getDistanceTo(Position position);
    Object getInstance(Position position, double threshold);
}
