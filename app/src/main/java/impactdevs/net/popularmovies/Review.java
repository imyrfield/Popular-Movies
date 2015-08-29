package impactdevs.net.popularmovies;

/**
 * Created by Ian on 8/22/2015.
 */
public class Review {

    private String author, comment;

    public Review() {

    }

    public Review(String author, String comment){
        this.author = author;
        this.comment = comment;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
