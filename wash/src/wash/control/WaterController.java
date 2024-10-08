package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

public class WaterController extends ActorThread<WashingMessage> {

    // TODO: add attributes
    WashingIO io;
    int currentMode;  // idle : 0, 1:fill, 2: drain
    ActorThread<WashingMessage> t;
    private boolean hasSentAck = false;

    public WaterController(WashingIO io) {
        // TODO
        this.io = io;
        currentMode = 0;
        hasSentAck = false;
    }

    @Override
    public void run() {
        try {
            while (true) {
            WashingMessage m = receiveWithTimeout(5000 / Settings.SPEEDUP);
            WashingMessage ack = new WashingMessage(this, WashingMessage.Order.ACKNOWLEDGMENT);
            if(m != null){
                switch (m.order()) {
                    case WATER_IDLE:
                        currentMode = 0;
                        t = m.sender();
                        hasSentAck = false;
                        break;
                    case WATER_FILL:
                        currentMode = 1;
                        t = m.sender();
                        hasSentAck = false;
                        break;
                    
                    case WATER_DRAIN:
                    t = m.sender();
                        currentMode = 2;
                        hasSentAck = false;
                        break;
                    default:
                        break;
                }
            }

            //M is null, no message, update every 5sec
            switch (currentMode) {
                case 0:
                    io.fill(false);
                    io.drain(false);
                    if(t != null && hasSentAck == false)
                    {
                        hasSentAck = true;
                        t.send(ack);
                    }
                    
                    break;
                case 1:
                    if(io.getWaterLevel() <10){
                        io.drain(false);
                        io.fill(true);
                        System.out.println("Fill");
                    }
                    else{
                        io.fill(false);
                        System.out.println("FillCompleted");
                        if(t != null && hasSentAck == false)
                    {
                        hasSentAck = true;
                        t.send(ack);
                    }
                    }
                    break;
                case 2:
                System.out.println("Drain");
                io.fill(false);
                io.drain(true);

                if(io.getWaterLevel() == 0){
                        System.out.println("DrainCompleted");
                        if(t != null && hasSentAck == false)
                    {
                        hasSentAck = true;
                        t.send(ack);
                    }
                }
                    break;
            
                default:
                    break;
            }
            }
            


        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
}