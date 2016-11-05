package centralized;

import logist.simulation.Vehicle;
import logist.task.TaskSet;
import logist.topology.Topology.City;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

enum ActionType {
    PICKUP,
    DELIVERY
}

/**
 * Represents a given action that a vehicle can make: pickup or deliver a task
 */
class Action {
    // the origin or destination city of a task
    private City focalCity;

    // the type of the task
    private ActionType type;

    public Action(City focalCity, ActionType type) {
        this.focalCity = focalCity;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Action action = (Action) o;

        if (!focalCity.equals(action.focalCity)) return false;
        return type == action.type;

    }

    @Override
    public int hashCode() {
        int result = focalCity.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}

class Plan {
    private LinkedList<Action> actions;

    public Plan() {
        actions = new LinkedList<Action>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Plan plan = (Plan) o;

        return actions.equals(plan.actions);

    }

    @Override
    public int hashCode() {
        return actions.hashCode();
    }

    // the cost of a given plan
    public float cost(City vehicleStartingPoint, float costPerKm) {
        throw new NotImplementedException();
    }

    //TODO implement local constraints (constraints that can be checked in a single plan)
}

/**
 * A solution is represented by each vehicle having a dedicated plan
 */
class Solution {
    // the set of pickup and delivery actions
    private static List<Action> pickups, deliveries;
    private List<Plan> plans;
    // the list of vehicles in the simulation
    private List<Vehicle> vehicles;

    public static Solution initialSolution(TaskSet tasks, List<Vehicle> vehicles) {
        // an initial solution that respects the constraints: all tasks to the biggest vehicle
        throw new NotImplementedException();
    }

    /**
     * Generate neighbours of a given solution
     *
     * @param old the solution we want to generate neighbours from
     * @return a set of new solutions
     */
    public static Set<Solution> generateNeighbours(Solution old) {
        throw new NotImplementedException();
    }

    /**
     * Choose the solution that gives the best improvement in the objective function
     *
     * @param neighbours the set of neighbours from which we will choose the best solution
     * @return the solution with the best improvement with random choice among the best if there is a tie
     */
    public static Solution localChoice(Set<Solution> neighbours) {
        throw new NotImplementedException();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Solution solution = (Solution) o;

        return plans.equals(solution.plans);

    }

    @Override
    public int hashCode() {
        return plans.hashCode();
    }

    /**
     * The objective function
     *
     * @return the total cost of a given solution
     */
    public float cost() {
        assert plans.size() == vehicles.size();

        float cost = 0;
        for (int i = 0; i < plans.size(); i++) {
            Vehicle currentVehicle = vehicles.get(i);
            cost += plans.get(i).cost(currentVehicle.homeCity(), currentVehicle.costPerKm());
        }
        return cost;
    }

    //TODO implement global constraints
}
