import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Elevator {

    public Elevator(int numberOfFloors, int index) {
        Random rn = new Random();
        currentFloor = rn.nextInt(numberOfFloors) + 1; //decision to randomize elevator starting floor
        directionMoving = Direction.Idle;
        destinationFloors = new ArrayList<Integer>();
        People = new ArrayList<Person>();
        id = "Elevator #"+(index + 1);

        //testing code
//        switch (index){
//            case 0:
//                currentFloor = 8;
//                break;
//            case 1:
//                currentFloor = 2;
//                break;
//        }
    }

    //Properties
    String id;
    List<Integer> destinationFloors;
    Direction directionMoving;
    public Integer currentFloor;
    public List<Person> People;
    public enum Direction
    {
        Up, Down, Idle, Stopped;
    }

    //Methods
    public void MoveToNextFloor()
    {
        if (directionMoving == Direction.Up)
            currentFloor++;
        else if (directionMoving == Direction.Down)
            currentFloor--;

        if (IsMoving())
            People.forEach(person -> person.currentFloor = currentFloor);
    }

    public void FloorCheck(Floor floor)
    {
        //if current floor is a destination floor for this elevator, transfer that person (out or in)
        if (destinationFloors.contains(floor.floorNumber)) {
            //if a person in the elevator has this as their destination, unload them and clear destination
            if (floor.IsCalled() && (/* floor.Calls.contains(directionMoving) ||*/ People.stream().count() == 0)){
                //this is the "pickup" scenario

                //always use the first call in the floor list
                directionMoving = floor.Calls.get(0);

                //remove the destination set by the floor call
                destinationFloors.remove(floor.floorNumber);
                //clear out the call from the floor
                floor.RemoveFloorCall(directionMoving);

                //transfer people in from the floor who want to go in the same direction the elevator is already moving
                List<Person> peopleToTransfer = floor.People.stream().filter(person -> person.GetDirection() == directionMoving).collect(Collectors.toList());
                People.addAll(peopleToTransfer);
                floor.People.removeAll(peopleToTransfer);

                String peeps = peopleToTransfer.stream().flatMap(person -> Stream.of(person.id)).collect(Collectors.joining(" and "));
                if (Main.getShowDetailedLogging())
                    System.out.println(id + " Pickup of " + peeps + " on floor " + currentFloor);

                //have the people set their destinations in the elevator
                peopleToTransfer.forEach(person -> {
                    if (!destinationFloors.contains(person.targetFloor))
                        destinationFloors.add(person.targetFloor);
                    if (Main.getShowDetailedLogging())
                        System.out.println(person.id + " entered floor " + person.targetFloor + " as their destination in " + id );
                });
            }

            //This is the "drop off" scenario
            if (People.stream().filter(person -> person.targetFloor == floor.floorNumber).count() > 0){
                List<Person> peopleToTransfer = People.stream().filter(person -> person.targetFloor == floor.floorNumber).collect(Collectors.toList());
                floor.People.addAll(peopleToTransfer);
                People.removeAll(peopleToTransfer);
                destinationFloors.remove(floor.floorNumber);

                if (Main.getShowDetailedLogging()) {
                    String peeps = peopleToTransfer.stream().flatMap(person -> Stream.of(person.id)).collect(Collectors.joining(" and "));
                    System.out.println(id + " Drop off of " + peeps + " on floor " + currentFloor);
                }

                if (destinationFloors.stream().count() == 0){
                    directionMoving = Direction.Idle;
                    if (Main.getShowDetailedLogging())
                        System.out.println(id + " went Idle on floor " + currentFloor);
                }
            }
            else if (floor.IsCalled() && floor.Calls.contains(directionMoving) && !destinationFloors.isEmpty()){
                //this is the scenario where the elevator is moving to a floor with a person in it, it needs to check each floor it comes to for a person wanting to go the same direction.
                List<Person> peopleToTransfer = floor.People.stream().filter(person -> person.GetDirection() == directionMoving).collect(Collectors.toList());
                People.addAll(peopleToTransfer);
                floor.People.removeAll(peopleToTransfer);

                if (Main.getShowDetailedLogging()) {
                    String peeps = peopleToTransfer.stream().flatMap(person -> Stream.of(person.id)).collect(Collectors.joining(" and "));
                    System.out.println(id + " Pickup of " + peeps + " on floor " + currentFloor);
                }

                //have the people set their destinations in the elevator
                peopleToTransfer.forEach(person -> {
                    if (!destinationFloors.contains(person.targetFloor))
                        destinationFloors.add(person.targetFloor);
                    if (Main.getShowDetailedLogging())
                        System.out.println(person.id + " entered floor " + person.targetFloor + " as their destination in " + id);
                });
            }
        }
    }

    public boolean IsIdle(){
        return directionMoving == Direction.Idle;
    }

    public boolean IsStopped(){
        return directionMoving == Direction.Stopped;
    }

    public boolean IsMoving(){
        return directionMoving == Direction.Up || directionMoving == Direction.Down;
    }

    public boolean IsEmpty(){
        return People.isEmpty();
    }
}
