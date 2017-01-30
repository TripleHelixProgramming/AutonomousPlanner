
package autonomousplanner;

import autonomousplanner.geometry.RobotSegmentGroup;
import autonomousplanner.geometry.Segment;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A time only follower for testing without feedback.
 * @author Jared
 */
public class DumbFollower extends TimerTask{
    RobotSegmentGroup rsg;
    DumbFollowerOutput out;
    int i;
    double kV = .06, kAcc = 0;
    
    public DumbFollower(RobotSegmentGroup rsg, DumbFollowerOutput out){
        this.rsg = rsg;
        this.out = out;
        new Timer().scheduleAtFixedRate(this, 0L, 10);
    }
    
    @Override
    public void run() {
        
        //first we need to do some math to figure out a good approximation
        //of our output power
        Segment l = rsg.left.get(i);
        Segment r = rsg.right.get(i);
        
        out.setLeftPower(l.vel * kV + l.acc * kAcc);
        out.setRightPower(r.vel * kV + r.acc * kAcc);
        i++;
        if(i == rsg.left.size()){
            i = 0; //restart, for now.
        }
    }
    
}
