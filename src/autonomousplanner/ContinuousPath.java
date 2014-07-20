
package autonomousplanner;

import autonomousplanner.IO.IO;
import autonomousplanner.geometry.Segment;
import autonomousplanner.geometry.SegmentGroup;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 * The Robot! Does math involving time, acceleration, velocity, and jerk. Splits
 * into segments of equal time length.
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
            max_acc = Double.valueOf(JOptionPane.showInputDialog(null, "Maximum Acceleration", "Path", JOptionPane.PLAIN_MESSAGE));
            max_dcc = Double.valueOf(JOptionPane.showInputDialog(null, "Maximum Deceleration", "Path", JOptionPane.PLAIN_MESSAGE));
            width = Double.valueOf(JOptionPane.showInputDialog(null, "Robot Width, feet", "Waypoint", JOptionPane.PLAIN_MESSAGE));

            max_vel = Double.valueOf(JOptionPane.showInputDialog(null, "Maximum Velocity", "Path", JOptionPane.PLAIN_MESSAGE));
        } catch (NumberFormatException ex) {
            max_acc = 10;
            max_dcc = 10;
            width = 2;
            max_vel = 10;
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
        print("     Finding Second Derivative of " + pathSegments.s.size() + " segments");
        for (int i = 0; i < pathSegments.s.size(); i++) {
            if (i == 0) {
                pathSegments.s.get(i).d2ydx2 = 0;  //at the first point
                //second derivative is zero.
            } else {
                double d1, d2;
                d2 = pathSegments.s.get(i).dydx;
                d1 = pathSegments.s.get(i - 1).dydx;
                double t1, t2;
                t2 = pathSegments.s.get(i).x;
                t1 = pathSegments.s.get(i - 1).x;
                //the change in the first derivative over the change in x
                pathSegments.s.get(i).d2ydx2 = ((d2 - d1) / (t2 - t1));
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
        for (int i = 0; i < path.group.s.size(); i++) {
            double v = Math.sqrt(max_acc * radiusOfCurvature(pathSegments.s.get(i)));
            pathSegments.s.get(i).vel = v;
        }

    }

   

    /**
     * Limit the velocity on all points.
     */
    public void limitVelocity() {
        print("     Limiting Velocity Part 1/2");
        for (int i = 0; i < pathSegments.s.size(); i++) {
            if (pathSegments.s.get(i).vel > max_vel) {
                pathSegments.s.get(i).vel = max_vel;
            }
        }
    }

    /**
     * Slows down robot at the end of the path.
     */
    private void setEndVelocity() {
        double vLast = 0;
        print("     Adjusting velocity for path end.");
        double adjustNum = 0; //number of adjusted segments.
        ArrayList<Segment> s = pathSegments.s;
        //backward for loop to start at end with velocity
        //and acceleration equal to zero, then increase by stepping
        //backward through the path
        for (int i = s.size() - 2; i >= 0; i--) {
            if (i == s.size() - 2) {
                //last segment, set stuff to zero
                s.get(i).acc = 0;
                s.get(i).vel = 0;
                s.get(i).jerk = 0;
            } else {

                double dx = -s.get(i).posit + s.get(i + 1).posit;
                //vf^2  = 2adx + v0^2
                double vf = Math.sqrt(2 * max_acc * s.get(i).dx + vLast * vLast);
                //vf = (s.get(i+1).vel + dx);
                double segmentVelocity = vf;
                if (s.get(i + 1).vel < 2) {
                    segmentVelocity = vf;
                    //System.out.println(vf);
                }
                adjustNum++;//increment number of adjusted segments.
                if (segmentVelocity > s.get(i).vel) {
                    //LOOK, I'M USING BREAK!!!!
                    //if we've back up far enough so that we're
                    //going faster than the original speed,
                    //we've met our goal of slowing the robot at the end, and we
                    //can quit.
                    break;
                }
                //set the velocity.
                s.get(i).vel = vf;
                vLast = vf;
            }

        }
        print("     Adjusted " + adjustNum + " points.");
    }

    /**
     * Calculates the velocity at every point. Keeping in mind jerk,
     * acceleration, and velocity limits.
     */
    public void calculateVelocity() {
        pathSegments.s.get(0).vel = 0; //not moving at beginning
        pathSegments.s.get(pathSegments.s.size() - 1).vel = 0; //or end
        print("     Limiting Velocity Part 2/2");
        double minAcc;
        double maxAcc;
        double tSum = 0;

        //figure out the farthest back where we need to start stopping.
        //vf^0 = v0^2 + 2adx.
        //max_vel^2 = 2*a*dx
        //max_vel^2/(2*a) = dx
        double stopDX = max_vel * max_vel / (2 * max_acc);
        System.out.println("Stopping Distance " + stopDX);
        double stopPosit = path.length - stopDX;
        System.out.println(stopPosit + " place");
        for (int i = 1; i < pathSegments.s.size() - 1; i++) {
            //find change in position.
            double dx = pathSegments.s.get(i).posit - pathSegments.s.get(i - 1).posit;
            //set minimum and maximum acceleration
            maxAcc = max_acc;
            minAcc = -max_dcc;
            //if we're out of bounds for acceleration, limit it!
            if (pathSegments.s.get(i - 1).acc > max_acc) {
                maxAcc = pathSegments.s.get(i - 1).acc;
            }
            if (pathSegments.s.get(i - 1).acc < -max_dcc) {
                minAcc = pathSegments.s.get(i - 1).acc;
            }
            //minAcc = -40;
            //how fast were we going previously?
            double lastVel = pathSegments.s.get(i - 1).vel;
            //how fast do we want to go?
            double desVelocity = pathSegments.s.get(i).vel;
            //pathSegments.s.get(i).jerk = desVelocity;
            //we want to speed up.
            //but can we do it this quickly in this short of a distance?
            //vf^2 = v0^2 + 2adx
            //remember, lastVel is final, but desVel can be changed.
            //how far will we go?
            //differences of squares
            double potato = (desVelocity * desVelocity) - (lastVel * lastVel);
            //divide by 2*dx
            //this is our desired acceleration
            double a = potato / (2 * dx);
            //do stop check
            boolean canStop = true;
            double distanceLeft;
            distanceLeft = path.length - pathSegments.s.get(i).posit;
            if(lastVel * lastVel / (2 * max_acc) > distanceLeft){
                canStop = false;
            }
            if (!canStop) {
                //we need to stop now
                a = -9.999;

            }
            
            //is it within our limits?
            if ((minAcc < a) && (maxAcc > a)) {
                //yes.  we can accelerate this fast. !!!
                //how much seconds does it take to do this?
                //dv/dt = a
                //dv/a = t.
                //vf^2 = v0^2 + 2adx
                
                double p = 2*a*pathSegments.s.get(i).dx + lastVel*lastVel;
                //desVelocity = Math.sqrt(p);
                desVelocity = Math.min(Math.sqrt(p), desVelocity);
                double time = (dx) / (desVelocity);
                pathSegments.s.get(i).jerk = desVelocity;
                pathSegments.s.get(i).dt = time;
                pathSegments.s.get(i).acc = a;
                tTotal2 += time; //keep track of time
                tSum += time;
                pathSegments.s.get(i).time = tSum;
                double dv = a * time;
                pathSegments.s.get(i).vel = dv + pathSegments.s.get(i - 1).vel;
                //leave velocity the way it is.
            } else {

                //no. we cannot accelerate this fast ): !!!
                //so, if we were to accelerate as fast as possible
                //how fast would we go?
                //using vf = sqrt (v0^2 + 2ax)
                double vf;
                //pathSegments.s.get(i).jerk = desVelocity;
                //pick logically whether we want to go quickly or slowly.
                if (lastVel < desVelocity) {
                    vf = Math.sqrt((lastVel * lastVel) + 2 * maxAcc * dx);
                } else {
                    vf = Math.sqrt((lastVel * lastVel) + 2 * minAcc * dx);

                }
                //how long does that take?
                //vf = v0 + at
                //(vf-v0)/a = t
                double time = (2 * dx) / (vf + lastVel);
                pathSegments.s.get(i).vel = vf;
                pathSegments.s.get(i).dt = time;
                //calculate the actual acceleration wrt time
                pathSegments.s.get(i).acc = (vf - lastVel) / time;
                tSum += time; //keep track of time
                pathSegments.s.get(i).time = tSum;
                tTotal3 += time;
            }
        }

        double totalTime = tTotal3 + tTotal2;
        print("     Time to drive spline: " + totalTime);
        print("     Was written to file?  ");

    }

    /**
     * Splits the segment group up into equally timed segments.
     */
    private void splitGroupByTime() {
        print("     Time dividing segments.");
        int segNum = 0; //the current time segment
        int numSeg = 0; //number of time segments
        int numMessySeg = 0; //number of segments that need to be interpolated
        double timeSoFar = 0; //time elapsed so far
        for (int i = 0; i < pathSegments.s.size(); i++) {
            //at the start, make a new segment, time = 0
            if (i == 0) {
                timeSegments.s.add(pathSegments.s.get(0));
                segNum++; //move to the next segment
            }
            //after iterating, we've gone far enough to make another time segment
            if (pathSegments.s.get(i).time > segmentTime(segNum)) {
                numSeg++; //increment number of segments
                timeSegments.s.add(pathSegments.s.get(i)); //add the existing segment
                //as a new time segment
                //dt will need to be updated
                //look at the last and second to last time segments to 
                //compute dt
                double newDT
                        = timeSegments.s.get(timeSegments.s.size() - 1).time
                        - timeSegments.s.get(timeSegments.s.size() - 2).time;
                //put dt in the time segment
                timeSegments.s.get(timeSegments.s.size() - 1).dt = newDT;
                timeSegments.s.get(timeSegments.s.size() - 1).jerk = pathSegments.s.get(i).jerk;
                //find out if we have a messy segment
                if (Math.abs(pathSegments.s.get(i).time - segmentTime(segNum)) > 0.0105) {
                    numMessySeg++;
                }
                segNum++;
            }
        }
        print("     Divided into " + numSeg + " segments, with a total of "
                + numMessySeg + " interpolated segments.");
        print("   STATISTICS:");
        print("     Time: " + timeSegments.s.get(timeSegments.s.size() - 1).time);
        print("     Distance: " + timeSegments.s.get(timeSegments.s.size() - 1).posit);
        print("     Average Speed: " + (timeSegments.s.get(timeSegments.s.size() - 1).posit / timeSegments.s.get(timeSegments.s.size() - 1).time));
    }

    public void recalculateValues() {
        for (int i = 0; i < timeSegments.s.size(); i++) {
            if (i != 0) {
                Segment now = timeSegments.s.get(i);
                Segment past = timeSegments.s.get(i - 1);
                now.vel = (now.posit - past.posit) / now.dt;
                now.acc = (now.vel - past.vel) / now.dt;
            }
        }
    }

    /**
     * Split a single robot path into paths for separate sides. Math taken from
     * Team 254's 2014 Code.
     */
    public void splitLeftRight() {
        for (int i = 0; i < timeSegments.s.size(); i++) {
            //left.
            Segment s = timeSegments.s.get(i);
            Segment l = new Segment();
            ArrayList<Segment> lg = left.s;
            left.s.add(l);
            l = left.s.get(i);
//            double adjustedSine;
//            double adjustedCosine;
//            if(s.dydx > 1000){
//                adjustedSine = 1;
//                adjustedCosine = 0;
//            }else{
//                adjustedSine = Math.sin(Math.tan(s.dydx));
//                adjustedCosine = Math.cos(Math.tan(s.dydx));
//            }
            l.x = s.x - width / 2 * Math.sin(Math.atan(s.dydx));
            l.y = s.y + width / 2 * Math.cos(Math.atan(s.dydx));

            if (i != 0) {
                double dp = Math.sqrt((l.x - lg.get(i - 1).x)
                        * (l.x - lg.get(i - 1).x)
                        + (l.y - lg.get(i - 1).y)
                        * (l.y - lg.get(i - 1).y));
                l.posit = lg.get(i - 1).posit + dp;
                l.vel = dp / s.dt;
                l.acc = (l.vel - lg.get(i - 1).vel) / s.dt;
                l.time = s.time;
                l.dydx = s.dydx;
            }

            Segment r = new Segment();
            ArrayList<Segment> rg = right.s;
            right.s.add(r);
            r = right.s.get(i);
            r.x = s.x + width / 2 * Math.sin(Math.atan(s.dydx));
            r.y = s.y - width / 2 * Math.cos(Math.atan(s.dydx));

            if (i != 0) {
                double dp = Math.sqrt((r.x - rg.get(i - 1).x)
                        * (r.x - rg.get(i - 1).x)
                        + (r.y - rg.get(i - 1).y)
                        * (r.y - rg.get(i - 1).y));
                r.posit = rg.get(i - 1).posit + dp;
                r.vel = dp / s.dt;
                if (r.vel > 15) {
                }
                //r.vel = width/2*Math.sin(Math.tan(s.dydx));
                r.acc = (r.vel - rg.get(i - 1).vel) / s.dt;
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
        IO.writeFile(file, (left.toString() + '\n' + "A" + '\n'
                + right.toString() + '\n' + "B" + '\n'
                + timeSegments.toString()));
        print(
                "Wrote file at " + file + " in "
                + (System.currentTimeMillis() - start) + " ms");
    }

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
     * Makes a simple linear function to solve for all values of the segment at
     * any point t within [a.time, b.time]. Not very useful for little gaps,
     * hella useful for big jumps.
     *
     * @param a The segment before.
     * @param b The segment after.
     * @param t The t value in between the segments for the interpolated segment
     * to have.
     * @return
     */
    private Segment interpolateSegment(Segment a, Segment b, double t) {
        return null;
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
    
    public void print(Object o){
        System.out.println(o);
    }

}