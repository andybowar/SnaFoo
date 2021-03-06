package snafoo.utilities;

import java.util.Comparator;

public class SortByVote implements Comparator<Snacks> {
    // Sort suggested snacks in descending order by number of votes
    public int compare(Snacks a, Snacks b) {
        return b.getNumVotes() - a.getNumVotes();
    }
}
