package autonomousplanner.geometry;

/**
 * The most boring of splines.
 *
 * @author Jared
 */
public class Line implements Spline {

    double x1, x2, y1, y2, h1, h2;
    int length = 0;
    SegmentGroup sg = new SegmentGroup();

    /**
     * Makes a new line with these points. In the future, the heading can be
     * used to implement a stop and turn.
     *
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     * @param h1
     * @param h2
     */
    public Line(double x1, double x2, double y1, double y2, double h1, double h2) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.h1 = h1;
        this.h2 = h2;
    }

    /**
     * Set the start point of the line.
     *
     * @param x
     * @param y
     */
    public void setStartPoint(double x, double y) {
        x1 = x;
        y1 = y;
    }

    /**
     * Set the end point of the line.
     *
     * @param x
     * @param y
     */
    public void setEndPoint(double x, double y) {
        x2 = x;
        y2 = y;
    }

    /**
     * Get the segments.
     *
     * @return Most recently calculated values.
     */
    @Override
    public SegmentGroup getSegments() {
        return sg;
    }

    /**
     * Calculate the group.
     *
     * @param resolution Number of segments.
     */
    @Override
    public void calculateSegments(int resolution) {
        sg = new SegmentGroup();
        //first find dy and dx
        double dy = y2 - y1;
        double dx = x2 - x1;
        //now dy/dx
        double dydx = dy / dx;
        //y1 = dydx (x1) + b
        //y1-(dydx*x1) = b
        double b = y2 - (dydx * x2);
        length = resolution;
        //how much do we travel in each segment
        double dSeg = dx / (resolution-1);
        for (int i = 0; i < resolution; i++) {
            
            //make a segment
            Segment s = new Segment();
            double x = x1 + i * dSeg; //x to evaulate at
            s.x = x;
            s.y = dydx * x + b; //evaulate
            sg.add(s);
        }
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public void setExtremePoints(double x0, double y0, double x1, double y1) {
        setStartPoint(x0, y0);
        setEndPoint(x1, y1);
    }

}
