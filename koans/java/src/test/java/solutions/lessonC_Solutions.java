package solutions;

import hu.akarnokd.rxjava2.math.MathObservable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.functions.Function3;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.TestObserver;
import org.junit.Test;
import util.LessonResources;
import util.LessonResources.ComcastNetworkAdapter;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static util.LessonResources.Elevator;
import static util.LessonResources.ElevatorPassenger;


public class lessonC_Solutions {

    private static final Observable<?> ________ = null;
    public Boolean mBooleanValue;

    private int ____;
    private String _____;
    private Object ______ = "";
    final Predicate<ElevatorPassenger> _______ = elevatorPassenger -> false;
    private Object mThrowable;

    /**
     * In this section we will learn about boolean logic we can apply to our pipelines of data.
     * Our first stop on the tour is takeWhile(), similar in concept to the while loop you may already be familiar with.
     * http://reactivex.io/documentation/operators/takewhile.html
     * <p>
     * In this experiment, we will load elevators with passengers eager to reach their destinations. One thing:
     * Our elevator has a maximum capacity. If we overload it, our passengers may be injured or even die!
     * We will use takeWhile to ensure no elevator is overloaded.
     */
    @Test
    public void takeWhileEvaluatesAnExpressionAndEmitsEventsUntilItReturnsFalse() {
        final LessonResources.Elevator elevator = new LessonResources.Elevator();

        final Observable<ElevatorPassenger> elevatorQueueOne = Observable.fromIterable(Arrays.asList(
                new ElevatorPassenger("Max", 168),
                new ElevatorPassenger("Mike", 234),
                new ElevatorPassenger("Ronald", 192),
                new ElevatorPassenger("William", 142),
                new ElevatorPassenger("Jacqueline", 114)));

        final Observable<ElevatorPassenger> elevatorQueueTwo = Observable.fromIterable(Arrays.asList(
                new ElevatorPassenger("Randy", 320),
                new ElevatorPassenger("Jerome", 125),
                new ElevatorPassenger("Sally-Joe", 349),
                new ElevatorPassenger("Little Eli", 54)));

        /*
         * the takeWhile operator evaluates an expression each time a new item is emitted in the stream.
         * As long as it returns true, the Observable stream of data takeWhile operates on continues to emit more data/events.
         *
         * Lets define our elevator rule: the total weight of all passengers aboard an elevator may not be larger than 500 pounds
         */

        final Predicate<ElevatorPassenger> elevatorRule =
                passenger -> elevator.getTotalWeightInPounds() + passenger.weightInPounds < Elevator.MAX_CAPACITY_POUNDS;
        /*
         * Now all we need to do is to plug in the rule in takeWhile()
         */
        final TestObserver<ElevatorPassenger> testObserver1 =
                elevatorQueueOne
                        .takeWhile(elevatorRule)
                        .doOnNext(elevator::addPassenger)
                        .test();

        assertThat(elevator.getPassengerCount()).isGreaterThan(0);
        assertThat(elevator.getTotalWeightInPounds()).isLessThan(Elevator.MAX_CAPACITY_POUNDS);
        assertThat(elevator.getPassengerCount()).isEqualTo(2);

        System.out.println("elevator stats: " + elevator);

        /*
         * One of the great advantages of using RxJava is that functions become composable:
         * we can easily reuse existing pieces of the pipeline by plugging them into other pipelines.
         * takeWhile() accepts a predicate or rule for determining
         */
        elevator.unload();

        elevatorQueueTwo
                .takeWhile(elevatorRule)
                .subscribe(elevator::addPassenger);

        assertThat(elevator.getPassengerCount()).isGreaterThan(0);
        assertThat(elevator.getTotalWeightInPounds()).isLessThan(Elevator.MAX_CAPACITY_POUNDS);
        assertThat(elevator.getPassengerCount()).isEqualTo(2);

        /*
         * an Extra Challenge!
         * Using what we've learned of rxJava so far, how could we get a list of passengers from the elevator that didn't make it
         * into the elevator in the last queue?
         */
        TestObserver<ElevatorPassenger> testObserver2 =
                elevatorQueueTwo
                        .filter(passenger -> !elevator.contains(passenger))
                        .test();

        System.out.println("left behind: " + testObserver2.values());
    }

    /**
     * Next on our tour, we will see .amb(). Stands for Ambiguous - a somewhat mysterious name (traces its historical roots to the 60's)!
     * What it does is it moves forward with the first of a set of Observables to emit an event.
     * <p>
     * Useful in this situation below : 3 servers with the same data, but different response times.
     * Give us the fastest!
     */
    @Test
    public void test() {
        final TestObserver<String> testObserver =
                Observable.ambArray(
                        Observable
                                .just("FOO")
                                .delay(100l, TimeUnit.MILLISECONDS),
                        Observable
                                .just("BAR")
                                .delay(200l, TimeUnit.MILLISECONDS)
                ).test();

        System.out.println(testObserver.values());
    }

    @Test
    public void AmbStandsForAmbiguousAndTakesTheFirstOfTwoObservablesToEmitData() {

        final Integer randomInt = new Random().nextInt(100);
        final Integer randomInt2 = new Random().nextInt(100);
        final Integer randomInt3 = new Random().nextInt(100);

        // bonus - there's a MathObservable object that knows how to do math type things to numbers!
        //
        // here, we're getting the smallest of 3 numbers!
        final Integer smallestNetworkLatency =
                Observable.just(randomInt, randomInt2, randomInt3)
                        .to(MathObservable::min)
                        .blockingLast();

        final Observable<String> networkA =
                Observable.just("request took : " + randomInt + " millis")
                        .delay(randomInt, TimeUnit.MILLISECONDS);

        final Observable<String> networkB =
                Observable.just("request took : " + randomInt2 + " millis")
                        .delay(randomInt2, TimeUnit.MILLISECONDS);

        final Observable<String> networkC =
                Observable.just("request took : " + randomInt3 + " millis")
                        .delay(randomInt3, TimeUnit.MILLISECONDS);

        /**
         * Do we have several servers that give the same data and we want the fastest of the three?
         */

        final TestObserver<String> testObserver =
                Observable.ambArray(networkA, networkB, networkC)
                        .test();

        testObserver.awaitTerminalEvent();

        System.out.println(testObserver.values());

        final List<String> onNextEvents = testObserver.values();
        assertThat(onNextEvents).contains("request took : " + smallestNetworkLatency + " millis");
        assertThat(onNextEvents).hasSize(1);

        // bonus! we can call .cache() on an operation that takes a while. It will save the pipeline's events
        // up to the point that .cache() was called, saving them for use again.
        // http://reactivex.io/RxJava/javadoc/rx/Observable.html#cache()
        // networkA.cache().first();
    }

    /**
     * The all operator collects everything emitted in the Observable, and then evaluates a predicate,
     * which then emits true or false.
     */
    @Test
    public void checkingEverything() {
        Observable.just(2, 4, 6, 8, 9)
                .all(integer -> integer % 2 == 0)
                .subscribe(aBoolean -> mBooleanValue = aBoolean);

        assertThat(mBooleanValue).isEqualTo(false);
    }

    /**
     * OK, it's time for a challenge!
     * Given the range below and what we've learned of rxjava so far, how can we produce an mSum equal to 19??
     * Hint: There are a couple ways you could do this, but the most readable will involve 2 operations.
     */
    @Test
    public void challenge_compositionMeansTheSumIsGreaterThanTheParts() {
        //one way to do it! another might be using filter & reduce
        final Observable<Integer> filter =
                Observable.range(1, 10)
                        .filter(integer -> integer >= 9);

        final Integer sum =
                MathObservable.sumInt(filter)
                        .blockingLast();

        assertThat(sum).isEqualTo(19);
    }

    /**
     * So far we've dealt with a perfect world. Unfortunately the real world involves exceptions!
     * <p>
     * How do we respond to those exceptions in our program? Fortunately rxJava comes with many ways of handling these problems.
     * Our first means to do this is with the .onError() event we can implement in our pipeline. This will receive whatever
     * exception was emitted, so that we can log about it, take action, or notify the user for example.
     */
    @Test
    public void onErrorIsCalledWhenErrorsOccur() {
        final String item1 = "aaa";
        final String item2 = "bbbb";
        final String item3 = "ccccc";

        final TestObserver<Integer> testObserver =
                Observable.just(item1, item2, item3)
                        .map(strings -> {
                            final int length = strings.length();

                            if (length > 4) {
                                throw new Exception("String too long!");
                            }

                            return length;
                        })
                        .doOnError(oops -> mThrowable = oops)
                        .test();

        testObserver.awaitTerminalEvent();

        assertThat(mThrowable).isInstanceOf(Throwable.class);
    }

    /**
     * In this test, our flaky comcast modem is on the blink again unfortunately.
     * .retry(long numberOfAttempts) can keep resubscribing to an Observable until a different non-error result occurs.
     * http://reactivex.io/documentation/operators/retry.html
     */
    @Test
    public void retryCanAttemptAnOperationWhichFailsMultipleTimesInTheHopesThatItMaySucceeed() {
        final Observable<String> networkRequestObservable =
                Observable.just(new ComcastNetworkAdapter())
                        .map(ComcastNetworkAdapter::getData)
                        .doOnNext(System.out::println)
                        .repeat(100);

        final String message =
                networkRequestObservable
                        .retry(43)
                        .blockingLast();

        assertThat(message).isEqualTo("extremely important data");
    }

    /**
     * In this experiment, we will use RxJava to pick a lock. Our lock has three tumblers. We will need them all to be up to unlock the lock!
     */
    @Test
    public void combineLatestTakesTheLastEventsOfASetOfObservablesAndCombinesThem() {
        final Observable<Boolean> tumbler1Observable =
                Observable.just(20)
                        .map(integer -> new Random().nextInt(20) > 15)
                        .delay(new Random().nextInt(20), TimeUnit.MILLISECONDS)
                        .repeat(1000);

        final Observable<Boolean> tumbler2Observable =
                Observable.just(20)
                        .map(integer -> new Random().nextInt(20) > 15)
                        .delay(new Random().nextInt(20), TimeUnit.MILLISECONDS)
                        .repeat(1000);

        final Observable<Boolean> tumbler3Observable =
                Observable.just(20)
                        .map(integer -> new Random().nextInt(20) > 15)
                        .delay(new Random().nextInt(20), TimeUnit.MILLISECONDS)
                        .repeat(1000);

        final Function3<Boolean, Boolean, Boolean, Boolean> combineTumblerStatesFunction =
                (tumblerOneUp, tumblerTwoUp, tumblerThreeUp) -> tumblerOneUp && tumblerTwoUp && tumblerThreeUp;

        final Maybe<Boolean> lockIsPickedMaybe =
                Observable.combineLatest(tumbler1Observable, tumbler2Observable, tumbler3Observable, combineTumblerStatesFunction)
                        .takeUntil(unlocked -> unlocked)
                        .lastElement();

        final TestObserver<Boolean> testObserver =
                lockIsPickedMaybe.test();

        testObserver.awaitTerminalEvent();

        final List<Boolean> onNextEvents = testObserver.values();
        assertThat(onNextEvents.size()).isEqualTo(1);
        assertThat(onNextEvents.get(0)).isEqualTo(true);
    }
}
