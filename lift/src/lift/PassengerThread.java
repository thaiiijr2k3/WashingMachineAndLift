package lift;


public class PassengerThread extends Thread{
    private LiftView view;
    private LiftMonitor monitor;

    public PassengerThread(LiftView view, LiftMonitor monitor)
    {
        this.view = view;
        this.monitor = monitor;
    }
    

    public void run(){
        while (true) {  // Infinite loop to create and manage passengers
            // Wait for a random delay before creating a new passenger
            monitor.waitTime();

            // Create and process the passenger
            Passenger pass = view.createPassenger();
            pass.begin();
            monitor.increaseWaitEntry(pass.getStartFloor());
            monitor.enterLift(pass.getStartFloor(), pass.getDestinationFloor());
            pass.enterLift();
            monitor.enterCompleted();
            monitor.exitLift(pass.getDestinationFloor());
            pass.exitLift();
            monitor.exitCompleted();
            pass.end();

            // Signal the monitor that the next passenger can be created after the current one finishes
            monitor.signalNextPassenger();
        }
        }
    }


