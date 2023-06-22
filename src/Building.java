import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Building {
    public Building()
    {}

    public Building(int numberOfFloors, int numberOfElevators, int numberOfPeople)
    {
        NumberOfFloors = numberOfFloors;
        Elevators = new ArrayList<Elevator>();
        for (int i = 0; i < numberOfElevators; i++) {
            Elevators.add((new Elevator(numberOfFloors, i)));
        }

        Floors = new ArrayList<Floor>();
        for (int i = 0; i < numberOfFloors; i++) {
            Floors.add((new Floor(i+1)));
        }

        //set up people to correct starting floors
        for (int i = 0; i < numberOfPeople; i++) {
//            people.add((new Person(numberOfFloors, i)));
            AddPersonToFloor(new Person(numberOfFloors, i));
        }

        FloorCallQueue = new ArrayList<FloorQueue>();
    }

    public void InitialFloorCalling(){
        //this is the logic that queue's the people in priority order for their starting floors.
        GetPersonPriorityList().forEach(person -> CallElevator(person));
        //need the list of all people sorted by priority.
    }

    public List<Person> GetPersonPriorityList(){
        List<Person> retVal = new ArrayList<Person>();

        Floors.forEach(floor -> floor.People.forEach(person -> {
            retVal.add(person);
        }));

        retVal.sort(Comparator.comparingInt(o -> o.priority));

        return retVal;
    }

    public void AddPersonToFloor(Person person)
    {
        Floors.stream().filter(floor -> floor.floorNumber == person.currentFloor).findFirst().get().People.add(person);
    }

    private boolean FloorHasBeenCalled(Floor floor) {
        boolean retVal = false;

        retVal = Elevators.stream().filter(elevator -> elevator.destinationFloors.contains(floor.floorNumber) && elevator.People.isEmpty()).count() > 0;

        return retVal;
    }

    public void QueueIdleElevators() //this isn't quite right, needs to be more person centric
    {//add logic here to not que multiple elevators to the same floor
        FloorCallQueue.forEach(floorQueue -> {//this goes top to bottom on the FloorCallQueue to handle the oldest calls first.
            if (HasIdleElevators()){//only bother if there are idle elevators to call
                //find the floor from the que item
                Floors.stream().filter(floor -> floor.floorNumber == floorQueue.floorNumber).findFirst().get().CallIdleElevator(floorQueue.direction);
            }
        });
    }

    public void TransferPerson(Person person, List<Person> originalCollection, List<Person> newCollection)
    {
        newCollection.add(person);
        originalCollection.remove(person);
    }

    public void CallElevator(Person person)
    {//method used to call an available elevator to a given floor to travel in a given direction
        Floors.stream().filter(floor -> floor.floorNumber == person.currentFloor).findFirst().get().AddFloorCall(person.GetDirection());
    }

    public boolean HasIdleElevators(){
        return Elevators.stream().filter(elevator -> elevator.IsIdle()).count() > 0;
    }

    public void MoveElevators()
    {
        Elevators.stream().filter(elevator -> elevator.destinationFloors.stream().count() > 0).forEach(elevator -> {
            elevator.MoveToNextFloor();
        });
    }

    public void ElevatorsFloorCheck()
    {
        Elevators.stream().filter(elevator -> elevator.destinationFloors.stream().count() > 0).forEach(elevator -> {
            elevator.FloorCheck(Floors.stream().filter(floor -> floor.floorNumber == elevator.currentFloor).findFirst().get());
        });
    }

    public Integer NumberOfFloors; //perhaps config this

    public List<Elevator> Elevators;

    public List<Floor> Floors;

    public List<FloorQueue> FloorCallQueue;
}

class FloorQueue{
    int floorNumber;
    Elevator.Direction direction;

    public FloorQueue(int floorNumber, Elevator.Direction direction) {
        this.floorNumber = floorNumber;
        this.direction = direction;
    }
}
