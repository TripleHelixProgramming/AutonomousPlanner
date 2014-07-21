package autonomousplanner.geometry;

import autonomousplanner.UI.CurveAdjustment;

/**
 * An attempt at going parametric with the 1st/2nd derivative controllable
 * spline.
 *
 * @author Jared
 */
public class ParametricQuintic implements Spline {

    private Quintic xt, yt;
    double pLow = 0, pHigh = 1;
    boolean isFlipped = true;
    public double curveSize = 100;
            boolean canRecalculate = true;


    public ParametricQuintic(double a, double b, double c, double d, double e, double f) {
        //t, the parameter is from 0 to 1, is x inputs of the normal quintic
        //the x output of the parametric spline is the y output of the normal quintic
        //the y output of the parametric spline is the y output of the other normal quintic
        xt = new Quintic(0, 0, 0, 1, 0, 0);
        yt = new Quintic(0, 0, 0, 1, 0, 0);
        new CurveAdjustment(this).setVisible(true);
    }

    @Override
    public void setExtremePoints(double x0, double y0, double x1, double y1) {
        //parameter 0 to 1
        if (isFlipped) {
            xt.setExtremePoints(pLow, x1, pHigh, x0);
            yt.setExtremePoints(pLow, y1, pHigh, y0);
        } else {
            xt.setExtremePoints(pLow, x0, pHigh, x1);
            yt.setExtremePoints(pLow, y0, pHigh, y1);

        }
    }

    /**
     * Build parametric spline from xt and yt.
     *
     * @return
     */
    @Override
    public SegmentGroup getSegments() {
        SegmentGroup output = new SegmentGroup();
        SegmentGroup xSegs, ySegs;
        xSegs = xt.getSegments();
        ySegs = yt.getSegments();
        if (!isFlipped) {
            for (int i = 0; i < xSegs.s.size(); i++) {
                Segment out = new Segment();
                out.x = xSegs.s.get(i).y; //not really y.  Just output
                out.y = ySegs.s.get(i).y; //y this time.
                output.add(out);
            }
        } else {
            for (int i = xSegs.s.size() - 1; i > -1; i--) {
                Segment out = new Segment();
                out.x = xSegs.s.get(i).y; //not really y.  Just output
                out.y = ySegs.s.get(i).y; //y this time.
                output.add(out);
            }
        }
       
        return output;
    }

    @Override
    public void calculateSegments(int resolution) {
        //System.out.println("res " + resolution);
        xt.calculateSegments(resolution);
        yt.calculateSegments(resolution);
        
    }
    
    public void setParameters(double min, double max){
        
    }

    @Override
    public int length() {
        return xt.length();
    }

    @Override
    public void setStartingWaypointIndex(int i) {
        xt.setStartingWaypointIndex(i);
        yt.setStartingWaypointIndex(i);
    }

    @Override
    public int getWaypointIndex() {
        return xt.getWaypointIndex();
    }

    @Override
    public double startDYDX() {

        return yt.startDYDX();
    }

    @Override
    public double endDYDX() {
        return yt.endDYDX();
    }

    @Override
    public void setStartDYDX(double dydx) {
        //dy/dx of parametric thing is (dy/dt)/(dx/dt)
        //to make things easier, I'll make dt = 0
        //this means that dy/dt = dy/dx
        //and dx/dt = 1.
        if (!isFlipped) {
            yt.setStartDYDX(curveSize * dydx);
            xt.setStartDYDX(curveSize);
        } else {
            yt.setEndDYDX(curveSize * dydx);
            xt.setEndDYDX(curveSize);
        }

    }

    @Override
    public void setEndDYDX(double dydx) {
        if (isFlipped) {
            yt.setStartDYDX(curveSize * dydx);
            xt.setStartDYDX(curveSize);
        } else {
            yt.setEndDYDX(-curveSize * dydx);
            xt.setEndDYDX(-curveSize);
        }
        //same as start dydx

    }

    @Override
    public String getType() {
        return "Parametric Quintic";
    }

    @Override
    public int splineID() {
        return xt.splineID();
    }

    @Override
    public void setSplineID(int id) {
        xt.setSplineID(id);
        yt.setSplineID(id);
    }

    @Override
    public SegmentGroup getParametricData(boolean isY) {
        if (isY) {
            return yt.getSegments();
        } else {
            return xt.getSegments();
        }
    }

    @Override
    public boolean isFlipped() {
        return isFlipped;
    }

    @Override
    public void setFlipped(boolean isFlipped) {
        this.isFlipped = isFlipped;
    }

}
