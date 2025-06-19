package recommender;

public class ShowInfo {
    public String title;
    public String platform;
    public String releaseYear;
    public String genres;

    public ShowInfo(String title, String platform, String releaseYear, String genres) {
        this.title = title;
        this.platform = platform;
        this.releaseYear = releaseYear;
        this.genres = genres;
    }

    @Override
    public String toString() {
        return title + " (" + releaseYear + ") - " + platform + " | Genres: " + genres;
    }
}
