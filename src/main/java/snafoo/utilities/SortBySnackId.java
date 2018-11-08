package snafoo.utilities;

import java.util.Comparator;

public class SortBySnackId implements Comparator<Votes> {
    // Sort suggested snacks in ascending order by snack ID
    public int compare(Votes a, Votes b) {
        return a.getSnackId() - b.getSnackId();
    }
}
