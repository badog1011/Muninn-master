package studio.bachelor.draft.marker.builder;

import studio.bachelor.draft.DraftDirector;
import studio.bachelor.draft.marker.Marker;
import studio.bachelor.draft.utility.Builder;
import studio.bachelor.draft.utility.Position;

/**
 * <code>MarkerBuilder</code>負責生產{@link studio.bachelor.draft.marker.Marker}的Builder。
 */
public abstract class MarkerBuilder implements Builder {
    static protected final DraftDirector director = DraftDirector.instance;
    /**
     * Builder生成的{@link studio.bachelor.draft.marker.Marker}。
     */
    protected Marker product;

    protected MarkerBuilder() {

    }

    /**
     * 用以確保在生成物件時，能夠清除生成物的內部Cache，讓下次Builder再次生成新的物件。
     * 所有繼承<code>MarkerBuilder</code>的Class，皆建議Override本Function，將Signature向下轉換為自身型別。
     * @return <code>MarkerBuilder</code>自身。
     */
    public MarkerBuilder clearProductCache() {
        product = null;
        return this;
    }

    /**
     * 使用{@link #product}的時候，檢查是否存在Instance，若無則建立。
     */
    protected abstract void createProductIfNull();

    /**
     * 回傳Builder生成結果。
     * @return {@link #product}的複製品。
     */
    public Marker build() {
        createProductIfNull(); //呼叫子function
        Marker return_value = product;
        clearProductCache(); //assign product as null
        return return_value;
    }

    /**
     * 設定生成{@link studio.bachelor.draft.marker.Marker}的{@link studio.bachelor.draft.marker.Marker#position}。
     * @param position 欲設定的座標。
     */
    public MarkerBuilder setPosition(Position position) {
        createProductIfNull();
        product.position.set(position);
        product.refreshed_tap_position.set(position);
        return this;
    }
}
