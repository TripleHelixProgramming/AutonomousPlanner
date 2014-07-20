package autonomousplanner.UI;

import autonomousplanner.Util;
import autonomousplanner.geometry.Cubic;
import autonomousplanner.geometry.Line;
import autonomousplanner.geometry.Point;
import autonomousplanner.geometry.Quintic;
import autonomousplanner.geometry.Segment;
import autonomousplanner.geometry.Spline;
import autonomousplanner.geometry.SplineGroup;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.event.MouseInputListener;

/**
 * An auto mode and its spline editor window
 *
 * @author Jared
 */
public class AutonomousMode extends TimerTask {

    Point startPoint;
    Timer timer = new Timer();
    Editor display;
    public JFrame jf;
    boolean isDragging;
    ArrayList<Spline> splines = new ArrayList<Spline>();
    ArrayList<SplineGroup> sGroups = new ArrayList<SplineGroup>();
    double startX, startY, startH;

    int LOW_RES = 100;

    /**
     * Make new auto mode.
     *
     * @param x start x value
     * @param y start y value
     * @param h start heading (radians pls)
     * @param name name of auto mode.
     */
    public AutonomousMode(double x, double y, double h, String name) {
        startX = x;
        startY = y;
        startH = h;
        startPoint = new Point(x, y);
        startPoint.setHeading(h);
        jf = new JFrame();
        jf.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        jf.setBounds(120, 30, 800, 800);
        jf.setLocation(400, 50);//consider making bigger
        display = new Editor();
        jf.getContentPane().add(display);
        jf.setVisible(true);
        timer.scheduleAtFixedRate(this, 0, 20);

    }

    /**
     * Causes the paintComponent() method to run often and update the display.
     */
    @Override
    public void run() {
        display.repaint();
    }

    /**
     * The editor to go with an auto mode.
     */
    public class Editor extends JComponent implements MouseInputListener {

        //THIS VALUE FOR RESOLUTION!!!
        int pointMover;
        boolean isDragging; //java mouse listeners );
        @SuppressWarnings("LeakingThisInConstructor")
        ArrayList<Point> waypoints = new ArrayList<>();
        Point lastClicked = new Point(0, 0);
        Point waypointInFocus = new Point(-9999, -9999);

        @SuppressWarnings("LeakingThisInConstructor")
        public Editor() {
            addMouseListener(this); //leaking this in constructor.  I don't care.
            addMouseMotionListener(this);
            Point s = new Point(0, 0);
            s.x = startX * 20 + 250;
            s.y = startY * 20 + 250;
            s.h = startH;
            waypoints.add(s);
        }

        /**
         * Draw the graphics to the screen
         *
         * @param g
         */
        @Override
        public void paintComponent(Graphics g) {
            //origin
            g.drawOval(250 - 5, 250 - 5, 10, 10);
            //last click
            Drawing.drawFilledRectOn(lastClicked.x, lastClicked.y, 5, g);
            drawWaypoints(g, waypoints);
            //focused point
            Drawing.drawFocusedPointAt(
                    waypointInFocus.x, waypointInFocus.y, 10, g);
            drawSplines(g, splines, sGroups);
        }

        /**
         * Draw some waypoints.
         *
         * @param g graphics
         * @param waypoints points to draw
         */
        public void drawWaypoints(Graphics g, ArrayList<Point> waypoints) {
            for (int i = 0; i < waypoints.size(); i++) {
                Drawing.drawFilledCircleAt(
                        waypoints.get(i).x, waypoints.get(i).y, 7, g);
            }
        }

        /**
         * Draw some splines. Uses a bunch of tiny lines
         *
         * @param g graphics
         * @param splines splines to draw
         * @param sGroups groups
         */
        public void drawSplines(Graphics g, ArrayList<Spline> splines,
                ArrayList<SplineGroup> sGroups) {
            for (int i = 0; i < splines.size(); i++) {
                Spline sp = splines.get(i);

                for (int j = 1; j < sp.getSegments().s.size() - 1; j++) {
                    //look ahead line draw
                    Segment s1 = sp.getSegments().s.get(j - 1);
                    Segment s2 = sp.getSegments().s.get(j);
                    g.drawLine((int) s1.x, (int) s1.y, (int) s2.x, (int) s2.y);
                }
            }
            //now sgroups
            for (int i = 0; i < sGroups.size(); i++) {
                SplineGroup sp = sGroups.get(i);

                for (int j = 1; j < sp.getSegments().s.size(); j++) {
                    //look ahead line draw
                    Segment s1 = sp.getSegments().s.get(j - 1);
                    Segment s2 = sp.getSegments().s.get(j);
                    g.drawLine((int) s1.x, (int) s1.y, (int) s2.x, (int) s2.y);
                }
                Segment max = sp.getSegments().s.get(sp.getSegments().s.size()-1);
                //g.drawRect((int)max.x, (int)max.y, 2, 2);
            }
        }

        @Override
        public void mouseClicked(MouseEvent me) {

        }

        @Override
        public void mousePressed(MouseEvent me) {

            //check if we hit near a point.  If so,
            //set the move flag
            //if rt. click, do waypoint box.
            waypointInFocus.x = -9999;
            int x = me.getX();
            int y = me.getY();
            for (int i = 0; i < waypoints.size(); i++) {
                double dx = Math.abs(waypoints.get(i).getX() - x);
                double dy = Math.abs(waypoints.get(i).getY() - y);
                if ((dx < 4) && (dy < 4)) {
                    if (me.getButton() == 3) {
                        //right clicked near point i.
                        //go through input box steps
                        double xNew = 20 * Double.valueOf(JOptionPane.showInputDialog(null, "X Value", "Waypoint", JOptionPane.PLAIN_MESSAGE));
                        double yNew = 20 * Double.valueOf(JOptionPane.showInputDialog(null, "Y Value", "Waypoint", JOptionPane.PLAIN_MESSAGE));
                        waypoints.set(i, coordinateTransform(new Point(250 + xNew, 250 + yNew)));
                    } else {
                        //left clicked near point i.
                        waypointInFocus.x = waypoints.get(i).x;
                        waypointInFocus.y = waypoints.get(i).y;
                        waypointInFocus.h = i;
                        pointMover = i;
                        isDragging = true;
                    }

                }
            }
            //set editor focus to point
            lastClicked.x = x;
            lastClicked.y = y;
        }

        public Point coordinateTransform(Point in) {
            in.move((in.x), (in.y));
            return in;
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            //kill move flag when mouse is released.
            //it doesn't always seem to catch?
            pointMover = 6;
            isDragging = false;
            recalculateAllSplines(splines, sGroups);
        }

        @Override
        public void mouseEntered(MouseEvent me) {
        }

        @Override
        public void mouseExited(MouseEvent me) {
        }

        @Override
        public void mouseDragged(MouseEvent me) {
            //hackish way with dealing with mousedragged called before
            //mouse pressed
            if (isDragging) {
                if (pointMover < waypoints.size()) {
                    waypoints.get(pointMover).move(me.getX(), me.getY());
                    waypointInFocus.x = me.getX();
                    waypointInFocus.y = me.getY();
                    repaint();
                }
            }
            recalculateAllSplines(splines, sGroups);
            //move the point!
            //this gets called a bunch to update fast.

        }

        @Override
        public void mouseMoved(MouseEvent me) {
        }

        /**
         * Adds a segment ending at the given point.
         *
         * @param type
         * @param p
         */
        public void addSegment(String type, double x, double y) {
            waypoints.add(new Point(x, y));
            if ("Line".equals(type)) {
                int i = waypoints.size() - 1;
                Line line = new Line(waypoints.get(i - 1).x, x,
                        waypoints.get(i - 1).y, y, waypoints.get(i - 1).h, 0);
                line.calculateSegments(LOW_RES);
                line.setStartingWaypointIndex(waypoints.size() - 1);
                splines.add(line);

            } else if ("Piecewise Cubic".equals(type)) {
                //placeholder test
                //add six new waypoints.

                Cubic c = new Cubic();
                c.setStartingIndex(waypoints.size()-2);
                Point p = waypoints.get(waypoints.size() - 2);
                addCubicWaypoints(p.x, p.y, lastClicked.x, lastClicked.y, waypoints);
                sGroups.add(c);
                recalculateAllSplines(splines, sGroups);

            } else if ("Quintic".equals(type)) {
                int i = waypoints.size() - 1;
                Quintic q = new Quintic(waypoints.get(i - 1).x, x,
                        waypoints.get(i - 1).y, y, waypoints.get(i - 1).h, 0);
                q.setStartingWaypointIndex(waypoints.size() - 1);
                splines.add(q);
                recalculateAllSplines(splines, sGroups);

            }
        }

        /**
         * Adds some cool equidistant points.
         *
         * @param x0
         * @param y0
         * @param x1
         * @param y1
         * @param s segment group to add to.
         */
        public void addCubicWaypoints(double x0, double y0, double x1,
                double y1, ArrayList<Point> s) {
            double dx = x1 - x0;
            double dy = y1 - y0;
            waypoints.add(new Point(x0, y0));
            System.out.println(x0 +" " + y0 + " " + x1 + " " + y1);
            for (int i = 1; i < 4; i++) {
                waypoints.add(new Point(i*(dx/4) + x0, i*(dy/4) + y0));
            }
            //waypoints.add(new Point(x1, y1));
        }

        /**
         * Toolbox move point.
         */
        void movePoint() {
            if (waypointInFocus.x == -9999) {
                //you don't have it in focus.
                Util.displayMessage("Click on a waypoint first.", "Error");
            } else {
                double xNew = 20 * Double.valueOf(JOptionPane.showInputDialog(null, "X Value", "Waypoint", JOptionPane.PLAIN_MESSAGE));
                double yNew = 20 * Double.valueOf(JOptionPane.showInputDialog(null, "Y Value", "Waypoint", JOptionPane.PLAIN_MESSAGE));
                waypoints.set((int) waypointInFocus.h, coordinateTransform(new Point(250 + xNew, 250 + yNew)));
            }
            recalculateAllSplines(splines, sGroups);
        }

        /**
         * If splines exist, recalculate.
         *
         * @param splines
         */
        public void recalculateAllSplines(ArrayList<Spline> splines, ArrayList<SplineGroup> sGroups) {
            //do groups first
            if (sGroups.size() > 0) {
                for (int i = 0; i < sGroups.size(); i++) {
                    int startPoint = sGroups.get(i).getStartingIndex();
                    for (int j = 0; j < 6; j++) {
                        sGroups.get(i).setPoint(
                                (int)waypoints.get(startPoint + j).x, 
                                (int)waypoints.get(startPoint+j).y, j);
                        //System.out.println(waypoints.get(waypoints.size()-1).x);
                    }
                    sGroups.get(i).calculateSpline();
                }
            }
            if (splines.size() > 0) {
                for (int i = 0; i < splines.size(); i++) {

                    double x0, x1, y0, y1;
                    x0 = waypoints.get(splines.get(i).getWaypointIndex()).x;
                    x1 = waypoints.get(splines.get(i).getWaypointIndex() - 1).x;
                    y0 = waypoints.get(splines.get(i).getWaypointIndex()).y;
                    y1 = waypoints.get(splines.get(i).getWaypointIndex() - 1).y;
                    splines.get(i).setExtremePoints(x0, y0, x1, y1);

                    splines.get(i).calculateSegments(LOW_RES);
                }
            }

        }
    }

}
