package lift;

import java.util.concurrent.ThreadLocalRandom;

public class LiftMonitor {
    private int currentFloor;
    private int[] toEnter;  //waiting to enter
    private int[] toExit;   //waiting to exit
    private boolean moving; 
    private int direction; // going up +1, going down -1
    private int passengerNbr;
    private LiftView view;
    private boolean doorOpened;
    private int passengersEntering; // Counter of number of passengers currently entering the elevator
    private int passengersExiting; // Counter of number of passengers currently exiting the elevator


    public LiftMonitor(LiftView view)
    {
        currentFloor = 0;
        moving = true;
        toEnter = new int[7];
        toExit = new int[7];
        direction = 1;
        passengerNbr = 0;
        this.view = view;
        doorOpened = false;
        passengersEntering = 0;
        passengersExiting = 0;
    }


    public synchronized void waitTime() {
        try {
            // Generate a random delay time between 0 and 45000 milliseconds (0 to 45 seconds)
            long randomDelay = ThreadLocalRandom.current().nextInt(0, 45000);
            
            // Wait for the random delay duration
            wait(randomDelay);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void signalNextPassenger() {
        notifyAll();  // Wake up any thread waiting for a new passenger
    }



    public synchronized void increaseWaitEntry(int passsengerFloor){
        toEnter[passsengerFloor]++;
        notifyAll();
    }

    public synchronized int[] liftHandlers(){

        while (true) {
            // Always open doors when arriving at a floor
            if (!doorOpened) {
                view.openDoors(currentFloor);
                doorOpened = true;
                moving = false;
            }

        
        while (toEnter[currentFloor] > 0 && passengerNbr != 4 || toExit[currentFloor] > 0 || passengersEntering > 0 || passengersExiting > 0) {
            
            notifyAll();
            try {
                wait();
            } catch (InterruptedException e) {
                throw new Error("Monitor.liftContinue interrupted " + e);
            }
        }

        if ((!moving && doorOpened) || passengerNbr == 4) {
            view.closeDoors();
            doorOpened = false;
            moving = true;
        }
        
    


        int[] movingPos = new int[2];
        movingPos[0] = currentFloor;
        calNextFloor();
        movingPos[1] = currentFloor + direction;
        
     
        return movingPos;
    }
    }


    //Go up and down regardless
    public synchronized void incrementFloor(){
        
        currentFloor = currentFloor + direction;
    }


    public void calNextFloor()
    {
        if(currentFloor == 6)
        direction = -1;
        else if(currentFloor == 0)
        direction = 1;
    }

    
    // Handles passengers waiting to enter the lift
    public synchronized void enterLift(int passengerFloor, int passengerDestination) {
        // Wait until the lift reaches the passenger's floor, lift has space, and no one else is entering/exiting
        while (currentFloor != passengerFloor || passengerNbr == 4 || moving || passengersEntering >= 1 || passengersExiting >= 1 || !doorOpened) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new Error("Monitor.enterLift interrupted " + e);
            }
        }

        // Passenger enters the lift
        toEnter[currentFloor]--;
        toExit[passengerDestination]++;
        passengerNbr++;
        passengersEntering++;  // Track entering passengers
        notifyAll();
    }

    // Called after passengers have completed entering the lift
    public synchronized void enterCompleted() {
        passengersEntering--;
        notifyAll();
    }

    // Handles passengers waiting to exit the lift
    public synchronized void exitLift(int passengerDestination) {
        // Wait until the lift is at the correct destination, the door is open, and no one else is exiting/entering
        while (currentFloor != passengerDestination || passengersExiting >= 1 || moving || !doorOpened) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new Error("Monitor.exitLift interrupted " + e);
            }
        }

        // Passenger exits the lift
        toExit[passengerDestination]--;
        passengerNbr--;
        passengersExiting++;  // Track exiting passengers
        notifyAll();
    }

    // Called after passengers have completed exiting the lift
    public synchronized void exitCompleted() {
        passengersExiting--;
        notifyAll();
    }





}
