package autonomousplanner;

import autonomousplanner.IO.IO;
import autonomousplanner.geometry.Segment;
import autonomousplanner.geometry.SegmentGroup;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 * Does math involving time, acceleration, velocity, and jerk. Splits into
 * segments of equal time length.
 *
 * @author Team 236
 */
public final class ContinuousPath {

    double tTotal2 = 0;
    double tTotal3 = 0;
    double max_acc = 8;
    double max_dcc = 20; //to decelerate for curves.  The poofs do it this fast.
    double max_vel = 12;
    double max_jerk = 200;
    double width;// as of right now, is max da/dx
    double bigT;
    double segmentTime = 0.01;
    Path path;
    public SegmentGroup pathSegments;
    public SegmentGroup timeSegments = new SegmentGroup();
    public SegmentGroup left = new SegmentGroup();
    public SegmentGroup right = new SegmentGroup();
    ArrayList<Integer> dtIndex = new ArrayList<>();
    ArrayList<Integer> minIndex = new ArrayList<>();

    /**
     * Do the math for time related functions.
     *
     * @param path
     */
    public ContinuousPath(Path path) {
        try {
            max_acc = Double.valueOf(JOptionPane.showInputDialog(null, "Maximum Acceleration.  Input nothing to use defaults", "Path", JOptionPane.PLAIN_MESSAGE));
            max_dcc = Double.valueOf(JOptionPane.showInputDialog(null, "Maximum Deceleration", "Path", JOptionPane.PLAIN_MESSAGE));
            width = Double.valueOf(JOptionPane.showInputDialog(null, "Robot Width, feet", "Waypoint", JOptionPane.PLAIN_MESSAGE));
            max_jerk = Util.messageBoxDouble("Maximum Jerk", "Path");
            max_vel = Double.valueOf(JOptionPane.showInputDialog(null, "Maximum Velocity", "Path", JOptionPane.PLAIN_MESSAGE));
        } catch (NumberFormatException ex) {
            max_acc = 10;
            max_dcc = 10;
            width = 2;
            max_vel = 10;
            max_jerk = 200; //is this okay?
            print("Input format error.  Using default robot values");
        }
        print(" ");
        print(" ");
        print(" ");
        print("CALCULATING ROBOT DATA");
        long start = System.currentTimeMillis();
        this.path = path;
        pathSegments = path.group;
        computeSecondDerivative();
        calculateMaxVel();
        limitVelocity();
        //setEndVelocity();
        calculateVelocity();
        //doJerkStuff();
        splitGroupByTime();
        recalculateValues();
        splitLeftRight();
        print("ROBOT CALCULATE TIME: " + (System.currentTimeMillis() - start));

    }

    /**
     * Compute the second derivative of all points on the path. This dy/dx not
     * dp/dt.
     */
    private void computeSecondDerivative() {
        print("     Finding Second Derivative of " + pathSegments.size() + " segments");
        for (int i = 0; i < pathSegments.size(); i++) {
            if (i == 0) {
                pathSegments.get(i).d2ydx2 = 0;  //at the first point
                //second derivative is zero.
            } else {
                double d1, d2;
                d2 = pathSegments.get(i).dydx;
                d1 = pathSegments.get(i - 1).dydx;
                double t1, t2;
                t2 = pathSegments.get(i).x;
                t1 = pathSegments.get(i - 1).x;
                //the change in the first derivative over the change in x
                pathSegments.get(i).d2ydx2 = ((d2 - d1) / (t2 - t1));

            }
        }
    }

    /**
     * Calculate the maximum LINEAR velocity at each point. A sharper turn
     * lowers max velocity.
     */
    public void calculateMaxVel() {
        print("     Calculating Maximum Possible Velocity");
        //a = v^2/r
        //sqrt(ar) = v
        for (int i = 0; i < path.group.size(); i++) {
            double r = radiusOfCurvature(pathSegments.get(i));
            double v_max_curve = Math.sqrt(max_acc * r);
            double big_r = r + width / 2;
            double v_max_wheel = (r / big_r) * max_vel;
            pathSegments.get(i).vel = Math.min(v_max_curve, Math.min(v_max_wheel, max_vel));
        }

    }

    /**
     * Limit the velocity on all points.
     */
    public void limitVelocity() {
        print("     Limiting Velocity to Robot Maximum...");
//        for (int i = 0; i < pathSegments.s.size(); i++) {
//            if (pathSegments.s.get(i).vel > max_vel) {
//                pathSegments.s.get(i).vel = max_vel;
//            }
//        }
    }

    /**
     * Calculates the velocity at every point. Keeping in mind jerk,
     * acceleration, and velocity limits.
     */
    public void calculateVelocity() {
        for (int i = 1; i < pathSegments.size(); i++) {
            if (pathSegments.get(i).dx == 0) {
            	pathSegments.remove(i);
            }
        }
        pathSegments.get(0).vel = 0;
        double time = 0;
        for (int i = 1; i < pathSegments.size(); i++) {
            //what is the maximum our v_f can be?
            //sqrt(v_0^2 + 2*a_max*dx)
            double v_0 = pathSegments.get(i - 1).vel;
            double dx = pathSegments.get(i - 1).dx;
            if (dx != 0) {
                double v_max = Math.sqrt(Math.abs(v_0 * v_0 + 2 * max_acc * dx));
                double v = Math.min(v_max, pathSegments.get(i).vel);
                if (Double.isNaN(v)) {
                    v = pathSegments.get(i - 1).vel;
                }
                pathSegments.get(i).vel = v;
            } else {
            	pathSegments.get(i).vel = pathSegments.get(i - 1).vel;
            }
        }
        pathSegments.get(pathSegments.size() - 1).vel = 0;
        for (int i = pathSegments.size() - 2; i > 1; i--) {
            double v_0 = pathSegments.get(i + 1).vel;
            double dx = pathSegments.get(i + 1).dx;
            double v_max = Math.sqrt(Math.abs(v_0 * v_0 + 2 * max_dcc * dx));
            double v = Math.min((Double.isNaN(v_max) ? max_vel : v_max), pathSegments.get(i).vel);
            pathSegments.get(i).vel = v;
        }

        for (int i = 1; i < pathSegments.size(); i++) {
            double v = pathSegments.get(i).vel;
            double dx = pathSegments.get(i - 1).dx;
            double v_0 = pathSegments.get(i - 1).vel;
            time = time + (2 * dx) / (v + v_0);
            time = (Double.isNaN(time)) ? 0 : time;
            pathSegments.get(i).time = time;

        }
        //get rid of no dt segs
        for (int i = 1; i < pathSegments.size(); i++) {
            double dt = pathSegments.get(i).time - pathSegments.get(i - 1).time;
            if (dt == 0 || Double.isInfinite(dt)) {
            	pathSegments.remove(i);
            }
        }
        //reference calc of acc.
        for (int i = 1; i < pathSegments.size(); i++) {
            double dv = pathSegments.get(i).vel - pathSegments.get(i - 1).vel;
            double dt = pathSegments.get(i).time - pathSegments.get(i - 1).time;
            if (dt == 0) {
            	pathSegments.get(i).acc = 0;
            } else {
            	pathSegments.get(i).acc = dv / dt;
            }
        }
        //Util.makeGraph(pathSegments, "Step 1", "acceleration");
        //Util.makeGraph(pathSegments, "test", "tet");
    }

    public void doJerkStuff() {
        double time = 0;
        //calculate acceleration
        //peform Jared Russell's method on acc/position curve.
        for (int i = 1; i < pathSegments.size(); i++) {
            double a_0 = pathSegments.get(i - 1).acc;
            double dx = pathSegments.get(i - 1).dx;
            if (dx != 0) {
                double a_max = Math.sqrt(Math.abs(a_0 * a_0 + 2 * max_jerk * dx));
                double a = Math.min(a_max, pathSegments.get(i).acc);
                if (Double.isNaN(a)) {
                    a = 0;
                }
                pathSegments.get(i).acc = a;
            } else {
                pathSegments.get(i).acc = 0;
            }
        }

        for (int i = pathSegments.size() - 2; i > 1; i--) {
            double a_0 = pathSegments.get(i + 1).acc;
            double dx = pathSegments.get(i + 1).dx;
            double a_max = Math.sqrt(Math.abs(a_0 * a_0 + 2 * max_jerk * dx));
            double a = Math.min((Double.isNaN(a_max) ? max_acc : a_max), pathSegments.get(i).acc);
            pathSegments.get(i).acc = a;
        }
        //recalculate velocity, using only dx and acceleration, as velocity
        //is max velocity with acc. limits, and may not always be achieved
        //with jerk limits in place.
        pathSegments.get(0).vel = 0;
        for (int i = 1; i < pathSegments.size(); i++) {
            double v_0 = pathSegments.get(i - 1).vel;
            double dx = pathSegments.get(i).posit - pathSegments.get(i - 1).posit;
            double a = (pathSegments.get(i).acc + pathSegments.get(i - 1).acc) / 2;
            a = (Double.isNaN(a)) ? 0 : a;
            double v_f = Math.sqrt(Math.abs(v_0 * v_0 + 2 * a * dx));
            pathSegments.get(i).vel = v_f;
            //System.out.println(v_f);
        }
        //recalculate time
        for (int i = 1; i < pathSegments.size(); i++) {
            double v = pathSegments.get(i).vel;
            double dx = pathSegments.get(i - 1).dx;
            double v_0 = pathSegments.get(i - 1).vel;
            time = time + (2 * dx) / (v + v_0);
            time = (Double.isNaN(time)) ? 0 : time;
            pathSegments.get(i).time = time;
            //System.out.println(time);
        }
        //Util.makeGraph(pathSegments, "test", "tet");
    }

    /**
     * Splits the segment group up into equally timed segments.
     */
    private void splitGroupByTime() {
        
        print("     Time dividing segments.");
        int segNum = 0; //the current time segment
        int numMessySeg = 0; //number of segments that need to be interpolated
        for (int i = 0; i < pathSegments.size(); i++) {
            //at the start, make a new segment, time = 0
            if (i == 0) {
                timeSegments.add(pathSegments.get(0));
                segNum++; //move to the next segment
            }
            //after iterating, we've gone far enough to make another time segment
            if (pathSegments.get(i).time > segmentTime(segNum)) {
                timeSegments.add(pathSegments.get(i)); //add the existing segment
                //as a new time segment
                //dt will need to be updated
                //look at the last and second to last time segments to 
                //compute dt
                double newDT = timeSegments.get(timeSegments.size() - 1).time
                        - timeSegments.get(timeSegments.size() - 2).time;
                //put dt in the time segment
                timeSegments.get(timeSegments.size() - 1).dt = newDT;
                //timeSegments.s.get(timeSegments.s.size() - 1).jerk = pathSegments.s.get(i).jerk;
                //find out if we have a messy segment
                if (Math.abs(pathSegments.get(i).time - segmentTime(segNum)) > 0.01005) {
                    numMessySeg++;
                }
                segNum++;
            }
        }
        print("     Time dividing segments.");
        //double segNum = 0;
        //follow the path.

        print("     Divided into " + segNum + " segments, with " + numMessySeg + " messy segments.");
        print("   STATISTICS:");
        print("     Time: " + timeSegments.get(timeSegments.size() - 1).time);
        print("     Distance: " + timeSegments.get(timeSegments.size() - 1).posit);
        print("     Average Speed: " + (timeSegments.get(timeSegments.size() - 1).posit / timeSegments.get(timeSegments.size() - 1).time));
        //Util.makeGraph(timeSegments, "Step 1", "acceleration");
    }

    /**
     * Recalculate values. A double check of my previous math.
     */
    public void recalculateValues() {
        System.out.println("     Verifying values");
        for (int i = 0; i < timeSegments.size(); i++) {
            if (i != 0) {
                Segment now = timeSegments.get(i);
                Segment past = timeSegments.get(i - 1);
                now.vel = (now.posit - past.posit) / (now.time - past.time);
                now.acc = (now.vel - past.vel) / (now.time - past.time);
            }
        }
        Util.makeGraph(timeSegments, "Step 1", "acceleration");
    }

    /**
     * Split a single robot path into paths for separate sides. Math taken from
     * Team 254's 2014 Code. Relocate x and y based off of robot width and robot
     * angle, then recalculate all values for the new path.
     */
    public void splitLeftRight() {
        System.out.println("     Generating paths for robot sides");
        for (int i = 0; i < timeSegments.size(); i++) {
            //left.
            Segment s = timeSegments.get(i);
            Segment l = new Segment();
            left.add(l);
            l = left.get(i);
            l.x = s.x - width / 2 * Math.sin(Math.atan(s.dydx));
            l.y = s.y + width / 2 * Math.cos(Math.atan(s.dydx));

            if (i != 0) {
                double dp = Math.sqrt((l.x - left.get(i - 1).x)
                        * (l.x - left.get(i - 1).x)
                        + (l.y - left.get(i - 1).y)
                        * (l.y - left.get(i - 1).y));
                l.posit = left.get(i - 1).posit + dp;
                l.vel = dp / s.dt;
                l.acc = (l.vel - left.get(i - 1).vel) / s.dt;
                l.time = s.time;
                l.dydx = s.dydx;
            }

            Segment r = new Segment();
            right.add(r);
            r = right.get(i);
            r.x = s.x + width / 2 * Math.sin(Math.atan(s.dydx));
            r.y = s.y - width / 2 * Math.cos(Math.atan(s.dydx));

            if (i != 0) {
                double dp = Math.sqrt((r.x - right.get(i - 1).x)
                        * (r.x - right.get(i - 1).x)
                        + (r.y - right.get(i - 1).y)
                        * (r.y - right.get(i - 1).y));
                r.posit = right.get(i - 1).posit + dp;
                r.vel = dp / s.dt;
                r.acc = (r.vel - right.get(i - 1).vel) / s.dt;
                r.time = s.time;
                r.dydx = s.dydx;
            }

        }
    }

    /**
     * Save the calculated paths to file.
     *
     * @param file
     */
    public void savePath(File file) {
        long start = System.currentTimeMillis();
        print("Writing file...");
        IO.writeFile(file, (getPathFileData()));
        print(
                "Wrote file at " + file + " in "
                + (System.currentTimeMillis() - start) + " ms");
    }
    
    public String getPathFileData() {
    	StringBuilder builder = new StringBuilder();
    	for (int i = 0; i < timeSegments.size(); i++) {
    		builder.append(timeSegments.get(i).time).append('\t')
    		.append(left.get(i).vel).append('\t')
    		.append(right.get(i).vel).append('\n');
    	}
    	return builder.toString();
    }

    /**
     * Reset for a new path.
     */
    public void reset() {
        timeSegments = new SegmentGroup();
        left = new SegmentGroup();
        right = new SegmentGroup();
        dtIndex = new ArrayList<>();
        minIndex = new ArrayList<>();
    }

    /**
     * Return the radius of curvature!
     *
     * @param s segment to calculate
     * @return radius (in feet)
     */
    private double radiusOfCurvature(Segment s) {
        //formula off the interent.
        double b, c, r;
        c = s.dydx * s.dydx;
        b = Math.pow((c + 1), 1.5);
        r = b / Math.abs(s.d2ydx2);
        return r;

    }

    /**
     * Returns the correct time value for the segment.
     *
     * @param segNum The segment.
     * @return What time it should happen.
     */
    private double segmentTime(int segNum) {
        return (segNum) * segmentTime;
    }

    public void print(Object o) {
        System.out.println(o);
    }
}