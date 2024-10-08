package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;
import wash.io.WashingIO.Spin;

public class SpinController extends ActorThread<WashingMessage> {

    // TODO: add attributes
    private WashingIO io;
    private boolean slowMode = false;
    private Spin currentSpinDirection = Spin.LEFT;

    public SpinController(WashingIO io) {
        // TODO
        this.io = io;
    }

    @Override
    public void run() {

        // this is to demonstrate how to control the barrel spin:
        // io.setSpinMode(Spin.IDLE);

        try {

            // ... TODO ...

            while (true) {
                // wait for up to a (simulated) minute for a WashingMessage
                WashingMessage m = receiveWithTimeout(60000 / Settings.SPEEDUP);
                WashingMessage ack = new WashingMessage(this, WashingMessage.Order.ACKNOWLEDGMENT);

         
                if (m != null) {
                    switch (m.order()) {
                        case SPIN_SLOW:
                            slowMode = true;
                            currentSpinDirection = Spin.LEFT;
                            io.setSpinMode(Spin.LEFT);
                            m.sender().send(ack);
                            System.out.println("Starting slow spin, direction: LEFT");
                            break;
                    
                        case SPIN_FAST:
                            slowMode = false;
                            io.setSpinMode(Spin.FAST);
                            m.sender().send(ack);
                            System.out.println("Switching to fast spin");
                            break;
                        case SPIN_OFF:
                            // Stop spinning
                            io.setSpinMode(Spin.IDLE);
                            slowMode = false;  // Stop slow spin
                            m.sender().send(ack);
                            System.out.println("Stopping spin");
                            break;

                        default:
                            break;
                    }

                    System.out.println("got " + m);
                }

                //Switch direction every minute 
                if(slowMode){
                    if(currentSpinDirection == Spin.LEFT)
                    currentSpinDirection = Spin.RIGHT;
                    else if(currentSpinDirection == Spin.RIGHT)
                    currentSpinDirection = Spin.LEFT;

                    io.setSpinMode(currentSpinDirection);
                    //System.out.println("Switched to " + currentSpinDirection);
                }

                // ... TODO ...
            }
        } catch (InterruptedException unexpected) {
            // we don't expect this thread to be interrupted,
            // so throw an error if it happens anyway
            throw new Error(unexpected);
        }
    }
}
