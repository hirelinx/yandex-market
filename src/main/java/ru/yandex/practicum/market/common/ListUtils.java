package ru.yandex.practicum.market.common;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class ListUtils {
    public static <T> List<List<T>> groupByNumber(List<T> from, int number, Supplier<T> placeholderSupplier) {

        return IntStream.range(0, (from.size() + number - 1) / number)
                .mapToObj(i -> {
                    List<T> group = new ArrayList<>(
                            from.subList(i * number, Math.min((i + 1) * number, from.size()))
                    );
                    while (group.size() < number) {
                        group.add(placeholderSupplier.get());
                    }
                    return group;
                })
                .toList();
    }
}
