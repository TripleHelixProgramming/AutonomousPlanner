
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
    public void setStartingWaypointIndex(int i);
    public int getWaypointIndex();
    public double startDYDX();
    public double endDYDX();
    public void setStartDYDX(double dydx);
    public void setEndDYDX(double dydx);
    public boolean isContinuousAtEnd();
    public boolean isContinuousAtBeginning();
    public boolean leftEndIsAbsoluteEnd();
    public boolean rightEngIsAbsoluteEnd();
    public void setLeftEndAbsolute(boolean isAbsolute);
    public void setRightEndAbsolute(boolean isAbsolute);
    public String getType();
    public int splineID();
    public void setSplineID(int id);
    public SegmentGroup getParametricData(boolean isY);
}
