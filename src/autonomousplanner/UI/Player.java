
package autonomousplanner.UI;

import autonomousplanner.ContinuousPath;
import java.awt.Graphics;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 * Class to visually display a movement path.
 * @author Team 236
 */
public class Player extends TimerTask {
    
    Timer timer = new Timer();
    Window test;
    ContinuousPath robot;
    JFrame window;

    /**
     * View path of a robot.
     * @param robot
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public Player(ContinuousPath robot) {
        this.robot = robot;
         window = new JFrame();
		//make window go away when closed
        window.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        window.setBounds(30, 30, 800, 800);
        test = new Window(robot);
        window.getContentPane().add(test);
        window.setVisible(true);
		//start timertask to repaint the Draw window
        timer.scheduleAtFixedRate(this, 0, 20);
    }

    /**
     * Repaint the window
     */
    @Override
    public void run() {
        test.repaint();
    }

    void reset() {
       this.window.setVisible(false);
    }
}

class Window extends JComponent {

    double i;
    ContinuousPath robot;
    int k_left = 50;
    

    public Window(ContinuousPath robot) {
        this.robot = robot;
    }
    /**
     * Takes care of drawing the path to the screen.
     * @param g 
     */
    public void showPath(Graphics g) {
	//loop through segments, and plot x and y, scaled and centered.
        for (int j = 0; j < robot.timeSegments.s.size(); j++) {
            g.drawRect((int) (robot.timeSegments.s.get(j).x * 15 + k_left),
                    (int) (robot.timeSegments.s.get(j).y * 15 + (27 * 15)), 1, 1);
        }
        for (int j = 0; j < robot.right.s.size(); j++) {
            g.drawRect((int) (robot.right.s.get(j).x * 15+ k_left),
                    (int) (robot.right.s.get(j).y * 15 + (27 * 15)), 1, 1);
        }
        for (int j = 0; j < robot.left.s.size(); j++) {
            g.drawRect((int) (robot.left.s.get(j).x * 15+ k_left),
                    (int) (robot.left.s.get(j).y * 15 + (27 * 15)), 1, 1);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        boolean isAutomated = true;

			//draw the path
            showPath(g);
			//get x, y, and theta values for this point in time
            double x = robot.timeSegments.s.get((int) i).x * 15;
            double y = robot.timeSegments.s.get((int) i).y * 15 + (27 * 15);
            double theta = robot.timeSegments.s.get((int) i).dydx;
            theta = Math.atan(theta);
            
            g.drawRect(0, 0, 27 * 30, 27 * 30);
            
            g.drawChars("Velocity".toCharArray(), 0, 8, 40, 20);
            g.drawChars((String.valueOf(robot.timeSegments.s.get((int) i).vel).toCharArray()), 0, 3, 40, 40);

            g.drawChars("Accel".toCharArray(), 0, 5, 100, 20);
            g.drawChars((String.valueOf(robot.timeSegments.s.get((int) i).acc).toCharArray()), 0, 3, 100, 40);

            g.drawChars("Time".toCharArray(), 0, 4, 160, 20);
            g.drawChars((String.valueOf(robot.timeSegments.s.get((int) i).time).toCharArray()), 0, 3, 160, 40);

            g.drawChars("Distance".toCharArray(), 0, 8, 220, 20);
            g.drawChars((String.valueOf(robot.timeSegments.s.get((int) i).posit).toCharArray()), 0, 3, 220, 40);

            //draw line that indicates heading
            g.drawLine((int) x+ k_left, (int) (y + (0)), (int) (100 * Math.cos(theta) + x)+ k_left, (int) (100 * Math.sin(theta) + y + (0)));
            g.drawLine((int) x+ k_left, (int) (y + (0)), (int) (-100 * Math.cos(theta) + x)+ k_left, (int) (-100 * Math.sin(theta) + y + (0)));
			//draw oval centered at x,y
            g.drawOval((int) x - 40+ k_left, (int) y - 40, 80, 80);
			//get and scale velocity
            double v = robot.timeSegments.s.get((int) i).vel;
            v *= 8;
			//draw circle centered at x,y ,with diameter v
            g.drawOval((int) (x - v / 2)+ k_left, (int) (y - v / 2), (int) v, (int) v);
            g.drawOval((int) x - 40+ k_left, (int) y - 40, 80, 80);
			//automatically increment to next frame if auto playing
            if(isAutomated){
            i += 2;
			//restart if needed.
            if (i > robot.timeSegments.s.size()- 2) {
                i = 0;
            }
            }
    }

}