package solutions;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.observables.GroupedObservable;
import org.junit.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;


public class lessonD_Solutions {

    public String mReceived = "";
    public String _____;
    public Integer _______;

    private String evenNums = "";
    private String oddNums = "";
    private Observable<Double> ________;
    private Function<? super Double, ?> ____________;

    /*
    So far everything has been pretty linear. Our pipelines all took the form:
    "do this, then do this, then do this, then end". In reality we can combine pipelines. We can take two streams
    and turn them into a single stream.

    Now its worth nothing this is different from what we did when we nested Observables. In that case we always had one stream.
    Lets take a stream of integers and a stream of strings and join them.
    */
    @Test
    public void merging() {
        final Observable<String> numbers =
                Observable.just("1", "2", "3");

        final Observable<String> letters =
                Observable.just("A", "B", "C");

        numbers.mergeWith(letters)
                .subscribe(string -> mReceived = join(mReceived, string));

        assertThat(mReceived).isEqualTo("1 2 3 A B C");
    }

    static String join(final String first, final String second) {
        if (first.isEmpty())
            return second;
        if (second.isEmpty())
            return first;
        return first + " " + second;
    }

    /*
    We can also split up a single stream into two streams. We are going to to use the groupBy() action.
    This action can be a little tricky because it emits an observable of observables. So we need to subscribe to the
    "parent" observable and each emitted observable.

    We encourage you to read more from the wiki: http://reactivex.io/documentation/operators/groupby.html

    Lets split up a single stream of integers into two streams: even and odd numbers.
    */
    @Test
    public void splittingUp() {
        Observable.range(1, 9)
                .groupBy(integer -> {
                    if (integer % 2 == 0) {
                        return "even";
                    } else {
                        return "odd";
                    }
                })
                .subscribe(group -> group.subscribe(integer -> {
                    String key = group.getKey();
                    if (Objects.equals(key, "even")) {
                        evenNums = evenNums + integer;
                    } else if (Objects.equals(key, "odd")) {
                        oddNums = oddNums + integer;
                    }
                }));

        assertThat(evenNums).isEqualTo("2468");
        assertThat(oddNums).isEqualTo("13579");
    }


    /*
    Lets take what we know now and do some cool stuff. We've setup an observable and a function for you. Lets combine
    them together to average some numbers.

    Also see that we need to subscribe first to the "parent" observable but that the pipeline still cold until we
    subscribe to each subset observable. Don't forget to do that.
     */
    @Test
    public void challenge_needToSubscribeImmediatelyWhenSplitting() {
        final double[] averages = {0, 0};
        final Observable<Integer> numbers =
                Observable.just(22, 22, 99, 22, 101, 22);

        final Function<Integer, Integer> keySelector =
                integer -> integer % 2;

        final Observable<GroupedObservable<Integer, Integer>> split =
                numbers.groupBy(keySelector);
    }
}