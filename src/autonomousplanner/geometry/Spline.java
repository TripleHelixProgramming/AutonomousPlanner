
package autonomousplanner.geometry;


/**
 * Something that interpolates points!
 * @author Jared
 */
public interface Spline {
    public void setStartPoint(double x, double y);
    public void setEndPoint(double x, double y);
    public SegmentGroup getSegments();
    public void calculateSegments(int resolution);
    int length();
}
