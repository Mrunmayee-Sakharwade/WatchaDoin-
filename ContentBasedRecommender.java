import java.io.*;
import java.util.*;

class SimpleTokenizer {
    public static final SimpleTokenizer INSTANCE = new SimpleTokenizer();

    public String[] split(String text) {
        return text.toLowerCase().replaceAll("[^a-zA-Z0-9 ]", "").split("\\s+");
    }
}

public class Main {
    static List<Show> shows = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        loadData(); // No parameters needed now

        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("\nEnter genre:");
            String genre = sc.nextLine();

            System.out.println("Enter language:");
            String lang = sc.nextLine();

            System.out.println("Enter mood or description:");
            String mood = sc.nextLine();

            List<Show> filtered = new ArrayList<>();
            List<String> allDocs = new ArrayList<>();

            String genreInput = genre.toLowerCase().trim();
            String langInput = lang.toLowerCase().trim();

            for (Show s : shows) {
                String showGenre = s.genre.toLowerCase();
                String showLang = s.lang.toLowerCase();

                if ((genreInput.isEmpty() || showGenre.contains(genreInput)) &&
                    (langInput.isEmpty() || showLang.contains(langInput))) {
                    
                    filtered.add(s);
                    allDocs.add(s.title + " " + s.genre + " " + s.lang);
                }
            }

            if (filtered.isEmpty()) {
                System.out.println("No shows found.");
                return;
            }

            allDocs.add(mood); // add user query

            List<Map<String, Double>> tfidfVectors = computeTfIdf(allDocs);
            Map<String, Double> userVec = tfidfVectors.get(tfidfVectors.size() - 1); // last = user

            Map<Show, Double> scores = new HashMap<>();

            for (int i = 0; i < filtered.size(); i++) {
                Map<String, Double> showVec = tfidfVectors.get(i);
                double sim = cosineSimilarity(showVec, userVec);
                scores.put(filtered.get(i), sim);
            }

            System.out.println("\nTop recommendations:");
            scores.entrySet().stream()
                .sorted(Map.Entry.<Show, Double>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> System.out.println(e.getKey()));
        }
    }

    public static void loadData() throws Exception {
        String[] files = {
            "data/netflix_titles.csv",
            "data/amazon_prime_titles.csv",
            "data/disney_plus_titles.csv"
        };

        for (String filePath : files) {
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String header = br.readLine(); // Skip header

                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",", -1);

                    // Make sure we have enough columns (based on sample CSVs)
                    if (parts.length >= 6) {
                        String source = filePath.contains("netflix") ? "Netflix" :
                                        filePath.contains("amazon") ? "Amazon" : "Disney";

                        String title = parts[1].trim();   // title column
                        String genre = parts[2].trim();   // listed_in
                        String lang = parts[5].trim();    // language (or audio language)

                        if (!title.isEmpty() && !genre.isEmpty() && !lang.isEmpty()) {
                            shows.add(new Show(source, title, genre, lang));
                        }
                    }
                }
            }
        }

        System.out.println("\nTotal shows loaded: " + shows.size());

        // Debug sample records
        System.out.println("Sample loaded shows:");
        for (int i = 0; i < Math.min(5, shows.size()); i++) {
            System.out.println(shows.get(i));
        }
    }

    static List<Map<String, Double>> computeTfIdf(List<String> docs) {
        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
        List<List<String>> tokens = new ArrayList<>();
        Map<String, Integer> df = new HashMap<>();

        for (String doc : docs) {
            String[] words = tokenizer.split(doc);
            Set<String> unique = new HashSet<>();
            List<String> list = new ArrayList<>();

            for (String w : words) {
                if (!w.isEmpty()) {
                    list.add(w);
                    unique.add(w);
                }
            }

            tokens.add(list);

            for (String w : unique) {
                df.put(w, df.getOrDefault(w, 0) + 1);
            }
        }

        int N = docs.size();
        List<Map<String, Double>> vectors = new ArrayList<>();

        for (List<String> docTokens : tokens) {
            Map<String, Double> tfidf = new HashMap<>();
            Map<String, Integer> tf = new HashMap<>();

            for (String w : docTokens) {
                tf.put(w, tf.getOrDefault(w, 0) + 1);
            }

            for (String w : tf.keySet()) {
                double tfVal = tf.get(w) / (double) docTokens.size();
                double idf = Math.log((double) N / (1 + df.get(w)));
                tfidf.put(w, tfVal * idf);
            }

            vectors.add(tfidf);
        }

        return vectors;
    }

    static double cosineSimilarity(Map<String, Double> v1, Map<String, Double> v2) {
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(v1.keySet());
        allKeys.addAll(v2.keySet());

        double dot = 0.0, norm1 = 0.0, norm2 = 0.0;

        for (String key : allKeys) {
            double a = v1.getOrDefault(key, 0.0);
            double b = v2.getOrDefault(key, 0.0);
            dot += a * b;
            norm1 += a * a;
            norm2 += b * b;
        }

        if (norm1 == 0 || norm2 == 0) return 0.0;
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
