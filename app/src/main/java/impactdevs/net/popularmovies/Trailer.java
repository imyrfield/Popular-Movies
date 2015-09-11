package impactdevs.net.popularmovies;

/**
 * Created by Ian on 8/22/2015.
 */
public class Trailer {

    private String title, videoId;

    public Trailer(){

    }

    public Trailer (String title, String videoId){
        this.title = title;
        this.videoId = videoId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }
}
