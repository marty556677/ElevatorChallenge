import java.util.Random;

public class Person {
    public Person(int numberOfFloors, int index) {
        Random rn = new Random();
        currentFloor = rn.nextInt(numberOfFloors) + 1;
        targetFloor = rn.nextInt(numberOfFloors) + 1;
        if (currentFloor == targetFloor) //if they happen to be the same, reroll the targetfloor
            targetFloor = rn.nextInt(numberOfFloors) + 1;
        id = "Person #" + (index + 1);
        priority = (index + 1); //used to resolve call requests in order

        //testing code
//        switch (index){
//            case 0:
//                currentFloor = 1;
//                targetFloor = 1;
//                break;
//            case 1:
//                currentFloor = 1;
//                break;
//        }
    }

    String id;
    public Integer currentFloor;
    public Integer targetFloor;
    public Integer priority;

    public Elevator.Direction GetDirection()
    {
        Elevator.Direction retVal = Elevator.Direction.Idle;
        if (currentFloor > targetFloor)
            retVal = Elevator.Direction.Down;
        else
            retVal = Elevator.Direction.Up;
        return retVal;
    }
}
