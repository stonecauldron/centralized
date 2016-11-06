package centralized;


import logist.task.Task;
import logist.topology.Topology;

/**
 * Represents a given action that a vehicle can make: pickup or deliver a task
 */
class Action {



    // next city to perform an action
    private Topology.City focalCity;

    // action to perform in the city : PICKUP/DELIVER
    private ActionType type;

    // the corresponding task
    private Task task;



    public Action(Topology.City focalCity, ActionType type, Task task) {
        this.focalCity = focalCity;
        this.type = type;
        this.task = task;
    }



    /**
     * @return the corresponding city
     */
    public Topology.City getCity(){
        return focalCity;
    }


    /**
     * @return the corresponding action to do in that city
     */
    public ActionType getAction(){
        return type;
    }


    /**
     * @return the corresponding task
     */
    public Task getTask(){
        return task;
    }



    @Override
    public boolean equals(Object that) {
        return !(that instanceof Action) ? false :
                ((Action)that).focalCity != this.focalCity ? false :
                        this.type == ((Action)that).type;
    }

    @Override
    public int hashCode() {
        return  31 * focalCity.hashCode() + type.hashCode();
    }


}
