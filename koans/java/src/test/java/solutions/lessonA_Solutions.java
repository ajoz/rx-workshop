package solutions;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class lessonA_Solutions {

    private int ____;
    private String _____;
    private Object ______ = "";
    private Integer sum;
    private int onNextCount;
    private int onCompleteCount;
    private int onErrorCount;


    /**
     * Observables are ultimately about handling "streams" of items (i.e. more
     * than one item) in a "data pipeline".
     * <p>
     * Each item is called an "event" of "data". Here we have the creation of a
     * new stream of data/events, called an Observable.
     * (http://reactivex.io/RxJava/javadoc/rx/Observable.html)
     * <p>
     * We also have a subscription, which finally takes the values from the
     * pipeline and consumes them.
     * <p>
     * For our RxJava tests, we will be working with an object called
     * TestSubscriber which the framework includes.
     * It gives us an easy way to check what was emitted on the pipeline.
     */
    @Test
    public void anObservableStreamOfEventsEmitsEachItemInOrder() {
        final Observable<String> pipelineOfData = Observable.just("Foo", "Bar");

        final TestObserver<String> testObserver = pipelineOfData.test();

        final List<String> dataEmitted = testObserver.values();

        assertThat(dataEmitted).hasSize(2);
        assertThat(dataEmitted).containsOnlyOnce("Foo");
        assertThat(dataEmitted).containsOnlyOnce("Bar");
    }


    /**
     * An observable stream calls 3 major lifecycle methods as it does it's work:
     * onNext(), onCompleted(), and onError().
     * <p>
     * onNext():
     * An Observable calls this method whenever the Observable emits an item.
     * This method takes as a parameter the item emitted by the Observable.
     * <p>
     * onError():
     * An Observable calls this method to indicate that it has failed to generate the expected data
     * or has encountered some other error.
     * This stops the Observable and it will not make further calls to onNext or onCompleted.
     * The onError method takes as its parameter an indication of what caused the error.
     * <p>
     * onCompleted():
     * An Observable calls this method after it has called onNext for the final time,
     * if it has not encountered any errors.
     */
    @Test
    public void anObservableStreamEmitsThreeMajorEventTypes() {
        final Observable<Integer> pipelineOfData = Observable.just(1, 2, 3, 4, 5);

        final TestObserver<Integer> testObserver =
                pipelineOfData
                        .doOnNext(integer -> onNextCount++)
                        .doOnComplete(() -> onCompleteCount++)
                        .doOnError(throwable -> onErrorCount++)
                        .test();

        testObserver.awaitTerminalEvent();

        assertThat(onNextCount).isEqualTo(5);
        assertThat(onCompleteCount).isEqualTo(1);
        assertThat(onErrorCount).isEqualTo(0);
    }

    /**
     * In the test above, we saw Observable.just(), which takes one or several Java objects
     * and converts them into an Observable which emits those objects. (http://reactivex.io/RxJava/javadoc/rx/Observable.html#just(T))
     * Let's build our own this time.
     */
    @Test
    public void justCreatesAnObservableEmittingItsArguments() {
        final String stoogeOne = "Larry";
        final String stoogeTwo = "Moe";
        final String stoogeThree = "Curly";
        final Integer stoogeAge = 38;

        final Observable<Object> stoogeDataObservable =
                Observable.just(stoogeOne, stoogeTwo, stoogeThree, stoogeAge);

        final TestObserver<Object> testObserver =
                stoogeDataObservable.test();

        /*
         * As we've seen, the TestSubscriber's getOnNextEvents() method gives a list of all the events emitted by the observable stream in a blocking fashion.
         * This makes it possible for us to test what was emitted by the stream.
         * Without the TestSubscriber, the events would have been emitted asynchronously and our assertion would have failed.
         */
        final List<Object> events =
                testObserver.values();

        assertThat(events).containsOnlyOnce(stoogeOne);
        assertThat(events).containsOnlyOnce(stoogeTwo);
        assertThat(events).containsOnlyOnce(stoogeThree);
        assertThat(events).containsOnlyOnce(stoogeAge);
        assertThat(events).hasSize(4);
    }

    /**
     * Observable.from() is another way to create an Observable. It's different than .just() - it is specifically designed to work
     * with Collections. When just is given a collection, it converts it into an Observable that emits each item from the list.
     * Let's understand how the two are different more clearly.
     */
    @Test
    public void fromCreatesAnObservableThatEmitsEachElementFromAnIterable() {
        final List<String> sandwichIngredients =
                Arrays.asList("bread (one)", "bread (two)", "cheese", "mayo", "turkey", "lettuce", "pickles", "jalapenos", "Sriracha sauce");

        final Observable<String> favoriteFoodsObservable =
                Observable.fromIterable(sandwichIngredients);

        final TestObserver<String> testObserver = favoriteFoodsObservable.test();

        final List<String> emittedValues = testObserver.values();

        assertThat(emittedValues).hasSize(9);
        assertThat(emittedValues).containsAll(sandwichIngredients);

        final List<List<String>> emittedValue =
                Observable
                        .just(sandwichIngredients)
                        .test()
                        .values();

        assertThat(emittedValue).hasSize(1);
        assertThat(emittedValue).contains(sandwichIngredients);
        /*
         * ^^  As you can see here, from() & just() do very different things!
         */
    }

    /**
     * So far we've created observables and immediately "subscribed" to them. Its only when we subscribe to an
     * observable that it is fully wired up. This observable is now considered "hot". Until then it is "cold"
     * and doesn't really do anything, it won't emit any events.
     * <p>
     * So if we are going to build an observable and not subscribe to it until later on, how can we include the all
     * of the functionality as before? Do we have to put all the work inside subscribe() ? No we don't!
     * <p>
     * If we peek at the Observer interface we see it has three methods:
     * <p>
     * public interface Observer<T> {
     * void onCompleted();
     * void onError(Throwable var1);
     * void onNext(T var1);
     * <p>
     * When we subscribe to an Observable, the code we put inside subscribe() is getting handed off to the Observer's onNext() method.
     * However, we can manually pass code right to onNext() ourselves with Observable.doOnNext()
     * <p>
     * Lets setup an Observable with all the functionality we need to sum a range of Integers. Then lets subscribe to it later on.
     */
    @Test
    public void nothingListensUntilYouSubscribe() {
        sum = 0;
        /*
         * Observable.range() creates a sequential list from a starting number of a particular size.
         * (http://reactivex.io/RxJava/javadoc/rx/Observable.html#range(int,%20int))
         *
         * We also haven't seen doOnNext() yet - its one way we can take action based on one of a series of Observable lifecycle events.
         * http://reactivex.io/documentation/operators/do.html
         */

        final TestObserver<Integer> testObserver =
                Observable.range(1, 10)
                        .doOnNext(integer -> sum += integer)
                        .test();


        assertThat(sum).isEqualTo(1 + 2 + 3 + 4 + 5 + 6 + 7 + 8 + 9 + 10);
        //Hint: what would we need to do to get our Observable to start emitting things?
    }

}