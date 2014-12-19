AutonomousPlanner
=================

The second (and improved) version of path planner


This software has a graphical interface to drag and drop points on a robot's path.  You can right click on a point to constrain it to a certain spot.  Also, for each section (the path between two waypoints), you can select either a line or a curved path that's constructed from two 5th degree polynomials.  If you connect a line and a line, your path may have a corner where the robot must stop and turn.  To avoid this, use a line and a parametric quintic, or a parametric quintic and a parametric quintic.  If a line is joined to a curved path, and the heading of the line is changed, the curved path will automatically adjust itself to remove corners from the path.


How it Works
============
The program starts by generating all line paths.  Each path is an array of points, called PathSegments.  Each pathSegment contains x and y location and first and second derivatives of y with respect to x (used to calculate heading, and rate of change of heading with respect to distance traveled).  The inverse tangent of the first derivative gives the heading, and the second derivative will always be zero for the lines because the heading never changes.

Next, the curved paths are generated, starting from the first path, and moving to the end.  Each section is scaled down so that the robot's total displacement is the x direction is 1 unit.  Then, two 5th degree polynomial functions are interpolated to represent the path.  One polynomial represents x position, and the other represents y position, and both are functions of a parameter s.  For more information on parametric functions, see this wikipedia page
http://en.wikipedia.org/wiki/Parametric_equation

