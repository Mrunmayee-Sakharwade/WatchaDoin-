import java.util.*;

public class Show {
    public String source;
    public String title;
    public List<String> genres;
    public List<String> languages;
    public String description; // For TF-IDF/mood

    public Show(String source, String title, List<String> genres, List<String> languages, String description) {
        this.source = source;
        this.title = title;
        this.genres = genres;
        this.languages = languages;
        this.description = description;
    }

    @Override
    public String toString() {
        return "[" + source + "] " + title + " | Genres: " + genres + " | Languages: " + languages;
    }
}
