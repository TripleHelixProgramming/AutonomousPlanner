AutonomousPlanner
=================

The second (and improved) version of path planner


This software has a graphical interface to drag and drop points on a robot's path.  You can right click on a point to constrain it to a certain spot.  Also, for each section (the path between two waypoints), you can select either a line or a curved path that's constructed from two 5th degree polynomials.  If you connect a line and a line, your path may have a corner where the robot must stop and turn.  To avoid this, use a line and a parametric quintic, or a parametric quintic and a parametric quintic.  If a line is joined to a curved path, and the heading of the line is changed, the curved path will automatically adjust itself to remove corners from the path.


How it Works
============
The program starts by generating all line paths.  Each path is an array of points, called PathSegments.  Each pathSegment contains x and y location and first and second derivatives of y with respect to x (used to calculate heading, and rate of change of heading with respect to distance traveled).  This code is located in AutonomousMode.java, which is in the src/UI package.  The inverse tangent of the first derivative gives the heading, and the second derivative will always be zero for the lines because the heading never changes.

Next, the curved paths are generated, starting from the first path, and moving to the end.  Each section is scaled down so that the robot's total displacement is the x direction is 1 unit, and scaled up to real size after being calculated.  Then, two 5th degree polynomial functions are interpolated to represent the path.  One polynomial represents x position, and the other represents y position, and both are functions of a parameter s.  In my implementation, x goes from 0 to 1 as the robot moves across the section of the path. A list of all points along the   For more information on parametric functions, see this wikipedia page
http://en.wikipedia.org/wiki/Parametric_equation

It turns out that given a starting and ending x position, y position, first derivative, and second derivative, there is only one 5th degree polynomial that satisfies these conditions.  However, before we can generate the x position and y position polynomials, we must supply the start/end conditions.  The x and y position are given by the location of the waypoints, and the heading is given either by the user or is driven by another segment, such as a line.  The second derivative is set to zero, making it a "relaxed" spline.  You can read more about this in the second paper.  Unfortunetly, we have no way to determine the individual first derivative of the x position and the y position functions.  Instead, we only know the heading, which can be used to compute the ratio of the x position's first derivative (with respect to our parameter variable, s) to the y position's first derivative. To solve this problem, the user can slide the curviness slider, which sets the x position's derivative, and from that, the y-position's derivative is set.  Additionally, negating both derivatives has the effect of "twisting" the path.  It would change a path shaped like a C to a path shaped like an S.  Both start and end in the same spot (but with one heading reversed!), but the S has an additional twist.  It turns out that this helps create a smoother curve, as not every pair of x position and y position derivatives will result in an efficient path.  

The process to generate these curves is run in a lower resolution mode continuously so that the user can drag around points and curviness sliders to see how the path will update as he adjusts things.

https://www.rose-hulman.edu/~finn/CCLI/Notes/day09.pdf
This paper outlines the approach I took, which used the quintic hermite basis functions to construct the polynomial.  This method limits you to paths where y position is a function of x position, which removes the functionality for paths that are circles, or loop back on themselves.  I took it a step further, and used two polynomials to represent the path.  This parametric configuration allows for any shape of path.

http://www.math.ucla.edu/~baker/149.1.02w/handouts/dd_splines.pdf
This paper is also very helpful.  It discusses how we must also pay attention to the second derivative when creating our paths.  A path may be smooth and first derivative continous, but still very difficult to drive, due to discontinuities in the second derivative.  Imagine driving a car, and having to immediately transition from a sharp left turn to a sharp right turn.  This happens if the second derivative of your path (meaning second derivative of y position with respect to x position) has jumps or discontinuities.   This paper also discusses the parametric/non-parametric difference nicely.



