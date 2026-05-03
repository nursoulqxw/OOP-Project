package core.strategy;

import java.util.List;

public interface SortStrategy {
    <T> List<T> sort(List<T> items);
}
