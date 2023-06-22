import java.util.Scanner;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    //Properties
    private static Building myBuilding;
    private static Integer numberOfFloors;
    private static Integer numberOfElevators;
    private static Integer numberOfPeople;
    private static Boolean showDetailedLogging;
    private static Integer cycleDelayInMilliseconds;

    public static Building getMyBuilding(){
        return myBuilding;
    }
    public static Boolean getShowDetailedLogging(){
        return showDetailedLogging;
    }
    public static void setMyBuilding(Building building){
        myBuilding = building;
    }
    public static void main(String[] args) {
        System.out.println("Welcome To The Elevator Simulator");

        SetConfigProperties();

        Scanner reader = new Scanner(System.in);  // Reading from System.in

        //log extra details
        if (showDetailedLogging) {
            System.out.println("Detailed Logs:");
            System.out.println("Number of Floors: " + numberOfFloors);
            System.out.println("Number of Elevators: " + numberOfElevators);
            System.out.println("Number of People: " + numberOfPeople);
        }

        //set up building
        setMyBuilding(new Building(numberOfFloors, numberOfElevators, numberOfPeople));
        DisplayFloorStatus(true);
        //have people call elevators to their starting floors (should this happen all at once?)
        myBuilding.InitialFloorCalling();

        //this is the main logic loop
        while(true) {
            //stop trigger is all people have reached their destinations
            if (IsSimulationDone()){
                System.out.println("Elevator Simulator Ending");
                break; //test this
            }

            //this is a configured delay cycle, to mo~re accurately simulate an elevator
            try {
                Thread.sleep(cycleDelayInMilliseconds);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            //now call any idle elevators to the nearest called floor
            myBuilding.QueueIdleElevators(); //should this happen every iteration?

            //now move the moving elevators to the next floors
            myBuilding.MoveElevators();

            //displays the current status of each elevator
            DisplayElevatorStatus();

            //now check the new current floor for a floor call
            //check if it is the destination, if so transfer people in or out and clear that destination
            myBuilding.ElevatorsFloorCheck();
        }

        DisplayFloorStatus(false);
    }

    private static void SetConfigProperties(){
        try (InputStream input = Main.class.getClassLoader().getResourceAsStream("config.properties")) {

            Properties prop = new Properties();
            //load a properties file from class path, inside static method
            prop.load(input);

            numberOfFloors = Integer.parseInt(prop.getProperty("numberOfFloors"));
            numberOfElevators = Integer.parseInt(prop.getProperty("numberOfElevators"));
            numberOfPeople = Integer.parseInt(prop.getProperty("numberOfPeople"));
            showDetailedLogging = Boolean.parseBoolean(prop.getProperty("detailedLogging"));
            cycleDelayInMilliseconds = Integer.parseInt(prop.getProperty("cycleDelayInMilliseconds"));
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static boolean IsSimulationDone(){
        AtomicBoolean retVal = new AtomicBoolean(true);

        //all elevators have to be empty first
        if (myBuilding.Elevators.stream().filter(elevator -> !elevator.People.isEmpty()).count() == 0) {
            //all people have to be at their target floor
            myBuilding.Floors.forEach(floor -> {
                for (Person person : floor.People) {
                    if (person.currentFloor != person.targetFloor) {
                        retVal.set(false);
                        break;
                    }
                }
            });
        }
        else
            retVal.set(false);
        return retVal.get();
    }
    private static void DisplayElevatorStatus(){
        myBuilding.Elevators.forEach(elevator -> {
            if (Main.getShowDetailedLogging()) {
                if (elevator.IsIdle() || elevator.IsStopped())
                    System.out.println(elevator.id + " " + elevator.directionMoving + " on floor " + elevator.currentFloor);
                else
                    System.out.println(elevator.id + " moving " + elevator.directionMoving + " to floor " + elevator.currentFloor);
            }
        });
    }

    private static void DisplayFloorStatus(Boolean initial){
        if(initial)
            System.out.println("Initial Status:");
        else
            System.out.println("Final Status:");

        Floor floor;
        String namesString;
        AtomicReference<String> elevatorsString = new AtomicReference<>("");
        for (int i = (int) myBuilding.Floors.stream().count() - 1; i >= 0 ; i--) {
            floor = myBuilding.Floors.get(i);
            System.out.print("Floor " + floor.floorNumber);
            namesString = floor.People.stream().flatMap(person -> Stream.of(person.id + " Dest: " + person.targetFloor)).collect(Collectors.joining(" "));
            Floor finalFloor = floor;
            elevatorsString.set("");
            myBuilding.Elevators.forEach(elevator -> {
                if (elevator.currentFloor == finalFloor.floorNumber)
                    elevatorsString.set(elevator.id);
            });
            System.out.println(" [" + namesString + "]{" + elevatorsString + "}");
        }
    }
}