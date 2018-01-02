package util;

import io.reactivex.Observable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LessonResources {

    public static class ElevatorPassenger {
        private String name;
        public int weightInPounds;

        public ElevatorPassenger(String name, int weightInPounds) {
            this.name = name;
            this.weightInPounds = weightInPounds;
        }

        public String toString() {
            return "ElevatorPassenger{" +
                    "name='" + name + '\'' +
                    ", weightInPounds=" + weightInPounds +
                    '}';
        }
    }


    public static class ComcastNetworkAdapter {
        private int reconnectAttempts;

        public String getData() {
            if (reconnectAttempts < 42) {
                reconnectAttempts++;
                return "network issues!! please reboot your computer!";
            }
            return "extremely important data";
        }
    }


    public static class Elevator {
        public static final int MAX_CAPACITY_POUNDS = 500;
        List<ElevatorPassenger> passengers = new ArrayList<>();

        public void addPassenger(ElevatorPassenger passenger) {
            passengers.add(passenger);
        }

        public int getTotalWeightInPounds() {
            return Observable
                    .fromIterable(passengers)
                    .reduce(0, (accumulatedWeight, elevatorPassenger) ->
                            elevatorPassenger.weightInPounds + accumulatedWeight)
                    .blockingGet();
        }

        public boolean contains(final ElevatorPassenger passenger) {
            return passengers.contains(passenger);
        }

        public List<ElevatorPassenger> getPassengers() {
            return passengers;
        }

        public int getPassengerCount() {
            return passengers.size();
        }

        @Override
        public String toString() {
            return "Elevator{" +
                    "passengers=" + passengers + "\n" +
                    "totalWeight=" + getTotalWeightInPounds() +
                    '}';
        }

        public void unload() {
            passengers = new ArrayList<>();
        }
    }

    //A Carnival Food Object...
    public static class CarnivalFood {
        private String name;
        public Double price;

        public CarnivalFood(String name, Double price) {
            this.name = name;
            this.price = price;
        }

        @Override
        public String toString() {
            return "Food{" +
                    "name='" + name + '\'' +
                    ", price=" + price +
                    "\n}";
        }
    }
}
