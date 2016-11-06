package centralized;

import logist.LogistSettings;
import logist.agent.Agent;
import logist.behavior.CentralizedBehavior;
import logist.config.Parsers;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 */

public class CentralizedAgent implements CentralizedBehavior {



    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private long timeout_setup;
    private long timeout_plan;





    @Override
    public void setup(Topology topology,
                      TaskDistribution distribution,
                      Agent agent) {

        // this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config/settings_default.xml");
        } catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }

        // the setup method cannot last more than timeout_setup milliseconds
        timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
        // the plan method cannot execute more than timeout_plan milliseconds
        timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);

        this.topology = topology;
        this.distribution = distribution;
        this.agent = agent;
    }






    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {

        long time_start = System.currentTimeMillis();

        Solution solution = null;
        try {
             solution = Solution.dummySolution(tasks,vehicles);
        } catch (NoSolutionException e) {
            e.printStackTrace();
        }

        for(int i = 0; i<20000; i++){
            solution = solution.localChoice(solution.generateNeighbours());
        }


        Map<Vehicle, Plan> vehicleToPlan = new HashMap<>();

        for(centralized.Plan p : solution.getPlans()){
            vehicleToPlan.put(p.getVehicle(),p.toLogistPlan());
        }

        List<Plan> result = new ArrayList<>();
        for(Vehicle v : vehicles){
            result.add(vehicleToPlan.get(v));
        }


        long time_end = System.currentTimeMillis();
        long duration = time_end - time_start;
        System.out.println("The plan was generated in " + duration + " milliseconds.");

        return result;
    }






    private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
        Topology.City current = vehicle.getCurrentCity();
        Plan plan = new Plan(current);

        for (Task task : tasks) {
            // move: current city => pickup location
            for (Topology.City city : current.pathTo(task.pickupCity)) {
                plan.appendMove(city);
            }

            plan.appendPickup(task);

            // move: pickup location => delivery location
            for (Topology.City city : task.path()) {
                plan.appendMove(city);
            }

            plan.appendDelivery(task);

            // set current city
            current = task.deliveryCity;
        }
        return plan;
    }





}
