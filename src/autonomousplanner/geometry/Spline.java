
package autonomousplanner.geometry;


/**
 * Something that interpolates points!
 * @author Jared
 */
public interface Spline {
    public void setExtremePoints(double x0, double y0, double x1, double y1);
    public SegmentGroup getSegments();
    public void calculateSegments(int resolution);
    int length();
}
