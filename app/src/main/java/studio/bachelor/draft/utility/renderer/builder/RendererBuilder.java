package studio.bachelor.draft.utility.renderer.builder;

import studio.bachelor.draft.DraftDirector;
import studio.bachelor.draft.utility.Builder;
import studio.bachelor.draft.utility.Renderable;

/**
 * Created by BACHELOR on 2016/02/25.
 */
public abstract class RendererBuilder implements Builder {
    static protected final DraftDirector director = DraftDirector.instance;
    protected Renderable product;

    protected abstract void createProductIfNull();
    protected RendererBuilder() {

    }

    public RendererBuilder clearProductCache() {
        product = null;
        return this;
    }

    public Renderable build() {
        createProductIfNull();
        Renderable return_value = product;
        clearProductCache();
        return return_value;
    }
}
