/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package autonomousplanner;

import autonomousplanner.geometry.Point;
import java.awt.TextArea;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.JOptionPane;

/**
 * A few useful methods.
 * @author Jared
 */
public class Util {
    
    /**
     * Create a window asking for a number.
     * @param message Prompt for the number
     * @param title Title of window
     * @return The number
     */
    public static double messageBoxDouble(String message, String title){
        double x;
        try{
        x = Double.valueOf(JOptionPane.showInputDialog(
                null, message, title, JOptionPane.PLAIN_MESSAGE));
        } catch (NumberFormatException e){
            x = 0;
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            JOptionPane.showMessageDialog(null, new TextArea(sw.toString()),
                    "Number Error", 3);
        }
        return x;
    }
    
    /**
     * Create a window asking for a string.
     * @param message prompt
     * @param title title of window
     * @return the string
     */
    public static String messageBoxString(String message, String title){
        return JOptionPane.showInputDialog(
                null, message, title, JOptionPane.PLAIN_MESSAGE);
    }
    /**
     * Display an error message.
     * @param message The message
     * @param title Title of the window.
     */
    public static void displayMessage(String message, String title){
        JOptionPane.showMessageDialog(null, message, title, 2);
    }
    
     /**
     * Return slope between the two points.
     * @param a
     * @param b
     * @return
     */
    public static double slope(Point a, Point b){
        double dy = a.y - b.y;
        double dx = a.x - b.x;
        return dy/dx;
    }
    

    
    /**
     * Slope to radian.  It's atan2.
     * @param x
     * @param y
     * @return
     */
    public static double slopeToRadians(double x, double y){
        return Math.atan2(y, x);
    }
    
    /**
     * Angle to slope.  It's tangent(theta).
     * @param theta
     * @return
     */
    public static double angleToSlope(double theta){
        return Math.tan(theta);
    }
    
}
