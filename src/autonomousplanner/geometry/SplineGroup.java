
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
    public boolean isContinuousAtEnd();
    public boolean isContinuousAtBeginning();
    public boolean leftEndIsAbsoluteEnd();
    public boolean rightEngIsAbsoluteEnd();
    public void setLeftEndAbsolute(boolean isAbsolute);
    public void setRightEndAbsolute(boolean isAbsolute);
    public int splineID();
    public void setSplineID(int id);
}
