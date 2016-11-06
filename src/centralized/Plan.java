package centralized;


import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * represents a Plan for a given vehicle (or agent).
 */
class Plan implements Iterable<Action> {



    private Vehicle vehicle;
    private ArrayList<Action> actions;



    public Plan(Vehicle vehicle) {
        this.vehicle = vehicle;
        this.actions = new ArrayList<Action>();
    }


    public Plan(Vehicle vehicle, ArrayList<Action> actions) {
        this.actions = actions;
        this.vehicle = vehicle;
    }



    /**
     * @return the cost needed to execute the plan
     */
    public float cost() {

        int totalDistance = 0;

        Topology.City currCity = vehicle.homeCity();
        for(Action toDoAction : this){
            totalDistance += currCity.distanceTo(toDoAction.getCity());
            currCity = toDoAction.getCity();
        }

        return totalDistance*vehicle.costPerKm();
    }


    /**
     * @return the vehicle
     */
    public Vehicle getVehicle(){
        return vehicle;
    }


    /**
     * @param i
     * @return the action to process at step i
     */
    public Action get(int i){
        return actions.get(i);
    }


    /**
     * @return a new plan with the given action at the given position
     */
    public Plan add(int id, Action that){

        ArrayList<Action> newActions = (ArrayList<Action>) actions.clone();
        newActions.add(id,that);

        return new Plan(this.vehicle, newActions);
    }


    /**
     * return a new plan with the given action removed
     */
    public Plan remove(int id){

        ArrayList<Action> newActions = (ArrayList<Action>) actions.clone();
        newActions.remove(id);

        return new Plan(this.vehicle, newActions);
    }


    /**
     * @return the total number of action in the plan
     */
    public int size(){
        return actions.size();
    }


    /**
     * @return the Plan converted for the logistic api
     */
    public logist.plan.Plan toLogistPlan() {

        Topology.City current = vehicle.getCurrentCity();
        logist.plan.Plan plan = new logist.plan.Plan(current);

        for(Action a : this){

            if(a.getAction() == ActionType.PICKUP){

                for(Topology.City c : current.pathTo(a.getCity())){
                    plan.appendMove(c);
                }
                plan.appendPickup(a.getTask());

                current = a.getCity();
            }
            else {

                for(Topology.City c : current.pathTo(a.getCity())){
                    plan.appendMove(c);
                }
                plan.appendDelivery(a.getTask());

                current = a.getCity();
            }

        }
        return plan;
    }






    /**
     * @return a new plan with one permutation over the actions
     */
    public Plan bestModification(){



        // for each task in the current plan : we extract the task and it's position in the plan
        // and classify them into the following map.
        Map<Task,Integer> pickupTaskId = new HashMap<>();
        Map<Task,Integer> deliverTaskId = new HashMap<>();

        int id = 0;
        for(Action a : this){
            if(a.getAction() == ActionType.DELIVERY){
                deliverTaskId.put(a.getTask(),id);
            }
            else {
                pickupTaskId.put(a.getTask(),id);
            }
            id++;
        }

        // we have to choose the best modification with respect the following constraints :
        // 1. the vehicle still carry out all tasks without capacity issues
        // 2. the pickup of a given task T remains before the deliver of the same task T
        // a modification is just an action we want to execute latter, or, sooner in the plan

        // working plan help to delay/advance an action
        WorkingPlan workingPlan = new WorkingPlan(this.vehicle, this.actions);

        int bestIdFrom = -1;
        int bestIdTo = -1;
        Float bestCost = Float.MAX_VALUE;


        // fromId : is the id of action we want to do sooner/latter
        int fromId= 0;

        while(fromId < actions.size()){

            Action action1 = actions.get(fromId);

            if(action1.getAction() == ActionType.PICKUP){


                // so we have to find all positions in the plans with respect with our constraints

                // we try to take our pickup sooner and sooner until done or until the capacity exceeded
                // note : i begin to rewind 2 step before (not just 1) to avoid duplication
                for(int toId = pickupTaskId.get(action1.getTask())-2; toId > 0; toId--){

                    workingPlan.setActionDisplacement(fromId,toId);

                    if(workingPlan.hasVehicleCapacitySufficient()){
                        if(workingPlan.cost()<bestCost){
                            bestCost = workingPlan.cost();
                            bestIdFrom = fromId;
                            bestIdTo = toId;
                        }
                    }
                    else {
                        // here capacity exceeded : is not necessary to continue
                        break;
                    }
                }

                // here we begin 1 step after the fixed one until the deliverTaskId
                // function isVehicleCapacityAllowingChange is not necessary anymore
                // (because all the path support the task weight
                // and because we delayed the pickup to be closer to the deliver point)
                for(int toId = pickupTaskId.get(action1.getTask())+1; toId< deliverTaskId.get(action1.getTask()); toId++){

                    workingPlan.setActionDisplacement(fromId,toId);

                    if(workingPlan.cost()<bestCost){
                        bestCost = workingPlan.cost();
                        bestIdFrom = fromId;
                        bestIdTo = toId;
                    }
                }


            }
            else { // DELIVER (same as before)


                // to delay deliver we need to have sufficient capacity
                for(int toId = deliverTaskId.get(action1.getTask())+1; toId <actions.size(); toId++){

                    workingPlan.setActionDisplacement(fromId,toId);

                    if(workingPlan.hasVehicleCapacitySufficient()){
                        if(workingPlan.cost()<bestCost){
                            bestCost = workingPlan.cost();
                            bestIdFrom = fromId;
                            bestIdTo = toId;
                        }
                    }
                    else {
                        // here capacity exceeded : is not necessary to continue
                        break;
                    }
                }

                try {
                    // to deliver sooner : no need to check
                    // toId<deliverTaskId.get(action1.getTask())-1 => avoid duplication (pickup already delayed to this point)
                    for (int toId = pickupTaskId.get(action1.getTask()) + 1; toId < deliverTaskId.get(action1.getTask()) - 1; toId++) {

                        workingPlan.setActionDisplacement(fromId, toId);

                        if (workingPlan.cost() < bestCost) {
                            bestCost = workingPlan.cost();
                            bestIdFrom = fromId;
                            bestIdTo = toId;
                        }
                    }
                } catch (NullPointerException e ){
                    System.out.println("find : " +action1.getTask()  );
                    for(Action a : this){
                        System.out.println( a.getAction() + " >> " +a.getTask() );
                    }
                }

            }


            fromId++;
        }

        workingPlan.setActionDisplacement(bestIdFrom, bestIdTo);
        ArrayList<Action> newPlan = workingPlan.toList();

        return new Plan(vehicle,newPlan);
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


    @Override
    public Iterator<Action> iterator() {
        return actions.iterator();
    }


}
