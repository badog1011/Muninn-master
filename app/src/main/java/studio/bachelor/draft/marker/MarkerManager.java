package studio.bachelor.draft.marker;

import java.util.LinkedList;
import java.util.List;

import studio.bachelor.draft.DraftDirector;
import studio.bachelor.draft.marker.builder.ControlMarkerBuilder;
import studio.bachelor.draft.marker.builder.LinkMarkerBuilder;
import studio.bachelor.draft.utility.Position;

/**
 * <code>MarkerManager</code>管理所有<code>Marker</code>物件。
 */
public class MarkerManager {
    /**
     * 儲存所有Marker的List，
     */
    public final List<Marker> markers = new LinkedList<Marker>();

    public MarkerManager() {

    }

    /**
     *將Marker將入<code>markers</code>中。
     * @param marker 欲增加之Marker。
     */
    public void addMarker(final Marker marker) {
        if(marker != null && !markers.contains(marker))
            markers.add(marker);
    }

    /**
     * 自<code>markers</code>中移除Marker
     * @param marker 欲移除之Marker。
     */
    public void removeMarker(final Marker marker) {
        if(marker != null)
            markers.remove(marker);
    }

    /**
     * 取得距離<code>position</code>最近的Marker。
     * @param threshold 與<code>position</code>距離的最大值，唯有低於此值的<code>Marker</code>會被<code>return</code>。
     * @return 與<code>position</code>距離最近的<code>Marker</code>，若不存在滿足條件的<code>Marker</code>則為<code>null</code>。
     */
    public Marker getNearestMarker(final Position position, double threshold) {
        Marker min_distance_marker = null;
        double min_distance = Double.MAX_VALUE;

        for (final Marker marker : markers) {
            if(marker.canBeTouched(position, threshold) && position.getDistanceTo(marker.position) < min_distance) {
                min_distance_marker = marker;
                min_distance = marker.getDistanceTo(position);
            }
        }
        return min_distance_marker;
    }
}
