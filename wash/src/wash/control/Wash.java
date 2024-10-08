package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;
import wash.simulation.WashingSimulator;

public class Wash {

    public static void main(String[] args) throws InterruptedException {
        WashingSimulator sim = new WashingSimulator(Settings.SPEEDUP);

        WashingIO io = sim.startSimulation();

        ActorThread<WashingMessage> temp = new TemperatureController(io);
        ActorThread<WashingMessage> water = new WaterController(io);
        ActorThread<WashingMessage> spin = new SpinController(io);
        ActorThread<WashingMessage> currentProgram = null;


        temp.start();
        water.start();
        spin.start();

        while (true) {
            int n = io.awaitButton();
            System.out.println("user selected program " + n);

            // Handle STOP button press (0) if a program is running
            if (n == 0 && currentProgram != null) {
                System.out.println("Stopping the current program...");
                currentProgram.interrupt();  // Terminate the current program
                currentProgram = null;  // Reset the current program
            }

            // Handle starting a new program
            if (currentProgram == null) {
                switch (n) {
                    case 1:
                        currentProgram = new WashingProgram1(io, temp, water, spin);
                        currentProgram.start();
                        break;
                    case 2:
                        currentProgram = new WashingProgram2(io, temp, water, spin);
                        currentProgram.start();
                        break;
                    case 3:
                        currentProgram = new WashingProgram3(io, temp, water, spin);
                        currentProgram.start();
                        break;
                }
            }
            

            // TODO:
            // if the user presses buttons 1-3, start a washing program
            // if the user presses button 0, and a program has been started, stop it
        }
    }
};
