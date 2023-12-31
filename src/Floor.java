import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Floor { //this class might be unnecessary
    public Floor(int floorNum) {
        floorNumber = floorNum;
        People = new ArrayList<>();

        Calls = new ArrayList<>();
    }

    //Properties
    public List<Elevator.Direction> Calls;
    public List<Person> People;
    public Integer floorNumber;

    //Methods
    public void AddFloorCall(Elevator.Direction direction)
    {
        Calls.add(direction);
        Main.getMyBuilding().FloorCallQueue.add(new FloorQueue(floorNumber, direction));
        //add trigger here to call the closest empty elevator, in the order the calls were made.
        CallIdleElevator();
    }

    public void RemoveFloorCall(Elevator.Direction direction){
        Calls.remove(direction);
        Main.getMyBuilding().FloorCallQueue.removeIf(floorQueue -> floorQueue.direction == direction && floorQueue.floorNumber == floorNumber);
    }

    public Boolean IsCalled()
    {
        return (long) Calls.size() > 0;
    }

    public boolean IdleElevatorDispatchedToFloor(){
        boolean retVal;
        //checks the elevators for an empty one headed for this floor.
        retVal = Main.getMyBuilding().Elevators.stream().anyMatch(elevator -> elevator.IsEmpty() && elevator.destinationFloors.contains(floorNumber));

        return retVal;
    }

    public void CallIdleElevator() {//method used to call an available elevator to a given floor to travel in a given direction
        //determine which elevator
        //add to the elevator queue

        if (Main.getMyBuilding().HasIdleElevators() && !IdleElevatorDispatchedToFloor()) {
            //attempt to call the closest idle elevator to this floor

            //find closest idle elevator
            Elevator closestEle = FindClosestIdleElevator();

            closestEle.destinationFloors.add(floorNumber);
            if (closestEle.currentFloor > floorNumber)
                closestEle.directionMoving = Elevator.Direction.Down;
            else if (closestEle.currentFloor < floorNumber)
                closestEle.directionMoving = Elevator.Direction.Up;
            else if (closestEle.currentFloor.equals(floorNumber))
                closestEle.directionMoving = Elevator.Direction.Stopped;

            if (Main.getShowDetailedLogging()) {
                if (closestEle.directionMoving == Elevator.Direction.Stopped)
                    System.out.println(closestEle.id + " called to floor " + floorNumber + " for pickup, it was already there");
                else
                    System.out.println(closestEle.id + " called " + closestEle.directionMoving + " to floor " + floorNumber + " for pickup");
            }
        }
    }
    public Elevator FindClosestIdleElevator(){
        Elevator retVal;
        retVal = Main.getMyBuilding().Elevators.stream().filter(Elevator::IsIdle).min(Comparator.comparingInt(i -> Math.abs(floorNumber - i.currentFloor))).get();

        return retVal;
    }
}
