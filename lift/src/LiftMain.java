import lift.LiftMonitor;
import lift.LiftThread;
import lift.LiftView;
import lift.PassengerThread;

public class LiftMain {

    public static void main(String[] args) {
        LiftView view = new LiftView(7, 4);
        LiftMonitor monitor = new LiftMonitor(view);

        PassengerThread[] passengers = new PassengerThread[15];

        LiftThread lift = new LiftThread(view, monitor);

        for (PassengerThread p: passengers) {
            p = new PassengerThread(view, monitor);
            p.start();
        }

        lift.start();
    }
}