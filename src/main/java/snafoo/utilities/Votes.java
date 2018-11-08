package snafoo.utilities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Votes {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer voteId;

    private Integer snackId;

    public Integer getVoteId() {
        return voteId;
    }

    public void setVoteId(Integer voteId) {
        this.voteId = voteId;
    }

    public Integer getSnackId() {
        return snackId;
    }

    public void setSnackId(Integer snackId) {
        this.snackId = snackId;
    }
}
