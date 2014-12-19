AutonomousPlanner
=================

The second (and improved) version of path planner


This software has a graphical interface to drag and drop points on a robot's path.  You can right click on a point to constrain it to a certain spot.  Also, for each segment (the path between two waypoints), you can select either a line or a curved path that's constructed from two 5th degree polynomials.  If you connect a line and a line, your path may have a corner where the robot must stop and turn.  To avoid this, use a line and a parametric quintic, or a parametric quintic and a parametric quintic.  If a line is joined to a curved path, and the heading of the line is changed, the curved path will automatically adjust itself to remove corners from the path.


How it Works
============
