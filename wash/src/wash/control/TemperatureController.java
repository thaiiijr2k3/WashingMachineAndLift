package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

public class TemperatureController extends ActorThread<WashingMessage> {

    // TODO: add attributes

    private WashingIO io;
    private double mu;   //upper bound
    private double ml;   //lower bound
    private static final double SAFETY_MARGIN = 0.2;
    private double wantedTemp;
    private boolean setTemp;
    private boolean heatUpDir;
    private ActorThread<WashingMessage> t;
    private boolean hasSentAck;
    public TemperatureController(WashingIO io) {
        // TODO
        this.io = io;
        mu = 10 * 0.0478 + SAFETY_MARGIN;
        ml = 10 * 0.00952 + SAFETY_MARGIN;
        wantedTemp = 0;
        setTemp = false;
        heatUpDir = true;
        hasSentAck = false;
    }

    @Override
    public void run() {
        // TODO
        try {
            while(true)
            {
                
                WashingMessage m = receiveWithTimeout(10000/ Settings.SPEEDUP);
                WashingMessage ack = new WashingMessage(this, WashingMessage.Order.ACKNOWLEDGMENT);
    
                //Get a message for temp
                if(m != null){
                    switch (m.order()) {
                        case TEMP_IDLE:
                            setTemp = false;
                            io.heat(false);
                            m.sender().send(ack);
                            break;
                    
                        case TEMP_SET_40:
                            wantedTemp = 40;
                            setTemp = true;
                            hasSentAck = false;
                            t = m.sender();
                            break;
    
                        case TEMP_SET_60:
                            wantedTemp = 60;
                            setTemp = true;
                            hasSentAck = false;
                            t = m.sender();
                            break;
    
                        default:
                            break;
                    }
                }

                //M is null, no message was receive control heat every 10seconds
                if(setTemp){
                    if(hasSentAck == false)
                    {
                        if(io.getTemperature() > wantedTemp - 2)
                        {
                            if(t !=null)
                            {
                                System.out.println("SEnt ark now");
                                t.send(ack);
                                hasSentAck = true;
                            }
                        }
                    }
                    System.out.println(io.getTemperature());
                    if(heatUpDir)
                    {
                        if(io.getTemperature() < (wantedTemp - mu)){
                            io.heat(true);
                            System.out.println("Heat on");
                        }
                        else
                        {
                            System.out.println("Turn");
                            io.heat(false);
                            heatUpDir = false;
                        }
                        
                    }
                    
                    else{
                        if(io.getTemperature() < (wantedTemp - 2 + ml))
                        {
                            io.heat(true);
                            heatUpDir = true;
                            System.out.println("Heat off");
                        }

                    }
                }
            }
            


        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
