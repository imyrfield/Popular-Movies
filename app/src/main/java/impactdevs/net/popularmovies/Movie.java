package impactdevs.net.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Movie Object Class.
 * Created by Ian on 7/20/2015.
 */
public class Movie implements Parcelable{

    private String movieTitle, thumbnailUrl, movieSynopsis, releaseDate, id;
    private int duration;
    private double rating;


    public Movie() {
    }

    public Movie(String title, String thumbnailUrl, String movieSynopsis, String
            releaseDate, String id, int duration, double rating) {

        this.movieTitle = title;
        this.thumbnailUrl = thumbnailUrl;
        this.movieSynopsis = movieSynopsis;
        this.releaseDate = releaseDate;
        this.id = id;
        this.duration = duration;
        this.rating = rating;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getMovieSynopsis() {
        return movieSynopsis;
    }

    public void setMovieSynopsis(String movieSynopsis) {
        this.movieSynopsis = movieSynopsis;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

//  Methods used for Parcelable implementation
    private Movie(Parcel in) {
        movieTitle = in.readString();
        thumbnailUrl = in.readString();
        movieSynopsis = in.readString();
        releaseDate = in.readString();
        id = in.readString();
        duration = in.readInt();
        rating = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(movieTitle);
        parcel.writeString(thumbnailUrl);
        parcel.writeString(movieSynopsis);
        parcel.writeString(releaseDate);
        parcel.writeString(id);
        parcel.writeInt(duration);
        parcel.writeDouble(rating);
    }

    public final Parcelable.Creator<Movie> CREATOR = new Parcelable
            .Creator<Movie>() {

        @Override
        public Movie createFromParcel(Parcel parcel) {
            return new Movie(parcel);
        }

        @Override
        public Movie[] newArray(int i) {
            return new Movie[i];
        }
    };
}
