package studio.bachelor.draft.utility.renderer.builder;

import java.util.List;

import studio.bachelor.draft.marker.LinkMarker;
import studio.bachelor.draft.marker.Marker;
import studio.bachelor.draft.utility.MapString;
import studio.bachelor.draft.utility.Position;
import studio.bachelor.draft.utility.renderer.primitive.CenterText;
import studio.bachelor.draft.utility.renderer.primitive.Icon;
import studio.bachelor.draft.utility.renderer.primitive.Line;
import studio.bachelor.draft.utility.renderer.primitive.Point;
import studio.bachelor.draft.utility.renderer.primitive.Text;
import studio.bachelor.draft.utility.renderer.MarkerRenderer;

/**
 * Created by BACHELOR on 2016/02/25.
 */
public class MarkerRendererBuilder extends RendererBuilder {
    public MarkerRendererBuilder() {
        super();
    }

    protected void createProductIfNull() {
        if(product == null)
            product = new MarkerRenderer();
    }

    public MarkerRendererBuilder setReference(Marker marker) {
        createProductIfNull();
        MarkerRenderer renderer = (MarkerRenderer)product;
        renderer.setReference(marker);
        return this;
    }

    public MarkerRendererBuilder setPoint(Marker marker) {
        createProductIfNull();
        MarkerRenderer renderer = (MarkerRenderer)product;
        Point primitive = new Point(marker.position);
        renderer.primitives.add(primitive);
        return this;
    }

    public MarkerRendererBuilder setLinkLine(LinkMarker marker) {
        createProductIfNull();
        MarkerRenderer renderer = (MarkerRenderer)product;
        Line primitive = new Line(marker.position, marker.getLink().position);
        renderer.primitives.add(primitive);
        return this;
    }

    public MarkerRendererBuilder setIcon(Marker marker, int resource) {
        createProductIfNull();
        MarkerRenderer renderer = (MarkerRenderer)product;
        Icon primitive = new Icon(marker.position, resource);
        renderer.primitives.add(primitive);
        return this;
    }

    public MarkerRendererBuilder setText(String string, Position position) {
        createProductIfNull();
        MarkerRenderer renderer = (MarkerRenderer)product;
        Text text = new Text(string, position);
        renderer.primitives.add(text);
        return this;
    }

    public MarkerRendererBuilder setText(String string, List<Position> positions) {
        createProductIfNull();
        MarkerRenderer renderer = (MarkerRenderer)product;
        Text text = new CenterText(string, positions);
        renderer.primitives.add(text);
        return this;
    }

    public MarkerRendererBuilder setText(MapString string, Position position) {
        createProductIfNull();
        MarkerRenderer renderer = (MarkerRenderer)product;
        Text text = new Text(string, position);
        renderer.primitives.add(text);
        return this;
    }

    public MarkerRendererBuilder setText(MapString string, List<Position> positions) {
        createProductIfNull();
        MarkerRenderer renderer = (MarkerRenderer)product;
        Text text = new CenterText(string, positions);
        renderer.primitives.add(text);
        return this;
    }

    public MarkerRendererBuilder clearProductCache() {
        super.clearProductCache();
        return (MarkerRendererBuilder)this;
    }
}
