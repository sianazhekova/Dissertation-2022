package utils;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;

public class IterableUtils {

    public static <T> void iterateTuples(@NotNull Iterable<T> iterable, BiConsumer<T, T> consumer) {
        Iterator<T> iterator = iterable.iterator();
        if (!iterator.hasNext()) return;
        T firstElem = iterator.next();

        while (iterator.hasNext()) {
            T nextElem = iterator.next();
            consumer.accept(firstElem, nextElem);
            firstElem = nextElem;
        }
    }
}
