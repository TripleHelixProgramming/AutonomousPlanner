package autonomousplanner.UI;

import autonomousplanner.Util;
import autonomousplanner.geometry.Line;
import autonomousplanner.geometry.Point;
import autonomousplanner.geometry.Segment;
import autonomousplanner.geometry.Spline;
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
 * The spline editor window
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
    double startX, startY, startH;
    
    int LOW_RES = 100;
    /**
     * Make new editor.
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

    final class Editor extends JComponent implements MouseInputListener {

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
            s.x = startX*20 + 250;
            s.y = startY*20 + 250;
            s.h = startH;
            System.out.println(startX + " " + startY);
            waypoints.add(s);
        }

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
            drawSplines(g, splines);
        }
        /**
         * Draw some waypoints.
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
         * Draw some splines
         * @param g graphics
         * @param splines splines to draw
         */
        public void drawSplines(Graphics g, ArrayList<Spline> splines){
            for(int i = 0; i < splines.size(); i++){
                Spline sp = splines.get(i);
                
                for(int j = 1; j < sp.getSegments().s.size()- 1; j++){
                    //look ahead line draw
                    Segment s1 = sp.getSegments().s.get(j-1);
                    Segment s2 = sp.getSegments().s.get(j);
                    g.drawLine((int)s1.x, (int)s1.y, (int)s2.x, (int)s2.y);
                }
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
            recalculateAllSplines(splines);
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
                if (pointMover < 6) {
                    waypoints.get(pointMover).move(me.getX(), me.getY());
                    repaint();
                }
            }
            recalculateAllSplines(splines);
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
                int i = waypoints.size()-1;
                Line line = new Line(waypoints.get(i-1).x, x,
                        waypoints.get(i-1).y, y, waypoints.get(i-1).h, 0);
                line.calculateSegments(LOW_RES);
                splines.add(line);
                
            } else if ("Cubic".equals(type)) {

            } else if ("Quintic".equals(type)) {

            }
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
        }
        /**
         * If splines exist, recalculate.
         * @param splines 
         */
        public void recalculateAllSplines(ArrayList<Spline> splines){
            if(splines.size() > 0){
                for(int i = 0; i < splines.size(); i++){
                splines.get(i).setEndPoint(
                        waypoints.get(i).x, waypoints.get(i).y);
                splines.get(i).setStartPoint(
                        waypoints.get(i+1).x, waypoints.get(i+1).y);
                splines.get(i).calculateSegments(LOW_RES);
            }
            }
            
        }
    }

}
