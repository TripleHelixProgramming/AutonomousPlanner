
package autonomousplanner.geometry;

/**
 * A group of splines!
 * @author Jared
 */
public interface SplineGroup {
    public void setPoint(int x, int y, int i);
    public void calculateSpline();
    public SegmentGroup getSegments();
    public int getStartingIndex();
    public void setStartingIndex(int i);
    public double getStartDYDX();
    public double getEndDYDX();
}
