package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

import static wash.control.WashingMessage.Order.*;

/**
 * Program 3 for washing machine. This also serves as an example of how washing
 * programs can be structured.
 * 
 * This short program stops all regulation of temperature and water levels,
 * stops the barrel from spinning, and drains the machine of water.
 * 
 * It can be used after an emergency stop (program 0) or a power failure.
 */
public class WashingProgram1 extends ActorThread<WashingMessage> {

    private WashingIO io;
    private ActorThread<WashingMessage> temp;
    private ActorThread<WashingMessage> water;
    private ActorThread<WashingMessage> spin;

    public WashingProgram1(WashingIO io,
            ActorThread<WashingMessage> temp,
            ActorThread<WashingMessage> water,
            ActorThread<WashingMessage> spin) {
        this.io = io;
        this.temp = temp;
        this.water = water;
        this.spin = spin;
    }

    @Override
    public void run() {
        try {
            // Lock the hatch
            io.lock(true);
            
            //Fill water 
            water.send(new WashingMessage(this, WashingMessage.Order.WATER_FILL));
            WashingMessage ack1 = receive();
            System.out.println("got " + ack1);

            //Stop water
            water.send(new WashingMessage(this, WashingMessage.Order.WATER_IDLE));
            WashingMessage ack0 = receive();
            System.out.println("got " + ack0);
            
            //SR1 check water level before heat
            if(io.getWaterLevel() <= 0)
            throw new InterruptedException("There is no water!");
            temp.send(new WashingMessage(this, WashingMessage.Order.TEMP_SET_40));
            WashingMessage ack2 = receive();
            System.out.println("gotTemp " + ack2);


            // Instruct SpinController to rotate barrel slowly, back and forth
            // Expect an acknowledgment in response.
            System.out.println("setting SPIN_SLOW...");
            spin.send(new WashingMessage(this, SPIN_SLOW));
            WashingMessage ack3 = receive();
            System.out.println("washing program 1 got " + ack3);

            // Spin for 30 simulated minutes (one minute == 60000 milliseconds)
            Thread.sleep(30 * 60000 / Settings.SPEEDUP);

            //Turn off heat
            temp.send(new WashingMessage(this, WashingMessage.Order.TEMP_IDLE));
            WashingMessage ack5 = receive();
            System.out.println("washing program 1 got" + ack5);

            //Drain water
            water.send(new WashingMessage(this, WashingMessage.Order.WATER_DRAIN));
            WashingMessage ack4 = receive();
            System.out.println("washing program 1 got " + ack4);

            // Rinse 5 times 2minutes

            for(int i = 0; i < 5; i++){
                //Fill cold water
                water.send(new WashingMessage(this, WashingMessage.Order.WATER_FILL));
                WashingMessage ack6 = receive();
                System.out.println("washing program 1 got" + ack6);

                //Stop water
                water.send(new WashingMessage(this, WashingMessage.Order.WATER_IDLE));
                WashingMessage ack8 = receive();
                System.out.println("got " + ack8);

                //Spin for 2 minutes 
                Thread.sleep(2 * 60000/ Settings.SPEEDUP);

                //Drain
                water.send(new WashingMessage(this, WashingMessage.Order.WATER_DRAIN));
                WashingMessage ack7 = receive();
                System.out.println("washing program 1 got " + ack7);
            }

            //Drain water for centrifuge
            water.send(new WashingMessage(this, WashingMessage.Order.WATER_DRAIN));
            WashingMessage ack10 = receive();
            System.out.println("washing program 1 got " + ack10);

            spin.send(new WashingMessage(this, WashingMessage.Order.SPIN_FAST));
            WashingMessage ack7 = receive();
            System.out.println("washing program 1 got " + ack7);

            // Spin for five simulated minutes (one minute == 60000 milliseconds)
            Thread.sleep(5 * 60000 / Settings.SPEEDUP);

            water.send(new WashingMessage(this, WashingMessage.Order.WATER_IDLE));
            WashingMessage ack9 = receive();
            System.out.println("washing program 1 got " + ack9);


            
            // Instruct SpinController to stop spin barrel spin.
            // Expect an acknowledgment in response.
            System.out.println("setting SPIN_OFF...");
            spin.send(new WashingMessage(this, SPIN_OFF));
            WashingMessage ack8 = receive();
            System.out.println("washing program 1 got " + ack8);

            // Now that the barrel has stopped, it is safe to open the hatch.
            io.lock(false);
            System.out.println("Washing program 1 FINISHED");
        } catch (InterruptedException e) {

            // If we end up here, it means the program was interrupt()'ed:
            // set all controllers to idle

            temp.send(new WashingMessage(this, TEMP_IDLE));
            water.send(new WashingMessage(this, WATER_IDLE));
            spin.send(new WashingMessage(this, SPIN_OFF));
            System.out.println("washing program terminated");
        }
    }
}