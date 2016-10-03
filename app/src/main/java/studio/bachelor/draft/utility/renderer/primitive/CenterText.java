package studio.bachelor.draft.utility.renderer.primitive;
import android.graphics.Canvas;

import studio.bachelor.draft.utility.MapString;
import studio.bachelor.draft.utility.renderer.primitive.Text;

import java.util.ArrayList;
import java.util.List;
import studio.bachelor.draft.utility.Position;

/**
 * Created by bachelor on 2016/3/8.
 */
public class CenterText extends Text {
    private final List<Position> positions = new ArrayList<Position>();

    public CenterText(String string) {
        super(string);
    }

    public CenterText(String string, List<Position> positions) {
        super(string);
        this.positions.addAll(positions);
    }

    public CenterText(MapString string) {
        super(string);
    }

    public CenterText(MapString string, List<Position> positions) {
        super(string);
        this.positions.addAll(positions);
    }

    private void updatePosition() {
        double x_sum = 0;
        double y_sum = 0;
        for (Position position : positions) {
            x_sum += position.x;
            y_sum += position.y;
        }
        double ax = x_sum / positions.size();
        double ay = y_sum / positions.size();
        position.set(new Position(ax, ay));
    }

    @Override
    public void onDraw(Canvas canvas) {
        this.updatePosition();
        super.onDraw(canvas);
    }
}
