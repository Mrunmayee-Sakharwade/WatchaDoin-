package recommender;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CollaborativeRecommender {

    static class Rating {
        String user;
        String title;
        double rating;

        Rating(String user, String title, double rating) {
            this.user = user;
            this.title = title;
            this.rating = rating;
        }
    }

    public static List<String> recommendForUser(String targetUserId, int k) throws Exception {
        List<Rating> ratings = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader("data/user_ratings.csv"))) {
            String line;
            br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    try {
                        String user = parts[0].trim();
                        String title = parts[1].trim();
                        double rating = Double.parseDouble(parts[2].trim());
                        ratings.add(new Rating(user, title, rating));
                    } catch (NumberFormatException e) {
                        System.out.println("Skipping bad line: " + Arrays.toString(parts));
                    }
                }
            }
        }

        // Step 2: Matrix creation
        Set<String> users = new HashSet<>();
        Set<String> titles = new HashSet<>();
        for (Rating r : ratings) {
            users.add(r.user);
            titles.add(r.title);
        }

        List<String> userList = new ArrayList<>(users);
        List<String> titleList = new ArrayList<>(titles);
        Collections.sort(userList);
        Collections.sort(titleList);

        double[][] matrix = new double[userList.size()][titleList.size()];
        for (Rating r : ratings) {
            int i = userList.indexOf(r.user);
            int j = titleList.indexOf(r.title);
            matrix[i][j] = r.rating;
        }

        // Step 3: Similarity
        int targetIndex = userList.indexOf(targetUserId);
        if (targetIndex == -1) throw new IllegalArgumentException("User not found: " + targetUserId);

        Map<Integer, Double> similarities = new HashMap<>();
        for (int i = 0; i < userList.size(); i++) {
            if (i == targetIndex) continue;
            double sim = cosineSimilarity(matrix[targetIndex], matrix[i]);
            similarities.put(i, sim);
        }

        // Step 4: Predict ratings
        Map<Integer, Double> predictedRatings = new HashMap<>();
        for (int j = 0; j < titleList.size(); j++) {
            if (matrix[targetIndex][j] > 0) continue;
            double weightedSum = 0, sumSim = 0;
            for (Map.Entry<Integer, Double> entry : similarities.entrySet()) {
                int otherIndex = entry.getKey();
                double sim = entry.getValue();
                double rating = matrix[otherIndex][j];
                if (rating > 0) {
                    weightedSum += sim * rating;
                    sumSim += sim;
                }
            }
            if (sumSim > 0) {
                predictedRatings.put(j, weightedSum / sumSim);
            }
        }

        // Step 5: Sort top k
        List<Map.Entry<Integer, Double>> sorted = new ArrayList<>(predictedRatings.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        List<String> recommendations = new ArrayList<>();
        for (int i = 0; i < Math.min(k, sorted.size()); i++) {
            int index = sorted.get(i).getKey();
            recommendations.add(titleList.get(index));
        }

        return recommendations;
    }

    // ➕ Metadata loader for enrichment
    public static Map<String, ShowInfo> loadMetadata(String csvFilePath) throws Exception {
        Map<String, ShowInfo> map = new HashMap<>();
        List<String> lines = Files.readAllLines(Paths.get(csvFilePath));
        lines.remove(0); // skip header

        for (String line : lines) {
            String[] parts = line.split(",", -1);
            if (parts.length < 4) continue;

            String title = parts[0].trim().toLowerCase();
            String platform = parts[1].trim();
            String releaseYear = parts[2].trim();
            String genres = parts[3].trim();

            map.put(title, new ShowInfo(title, platform, releaseYear, genres));
        }

        return map;
    }

    // ➕ Cosine similarity function
    private static double cosineSimilarity(double[] a, double[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return (normA == 0 || normB == 0) ? 0 : dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // 🧪 Enrich and print results
    public static void main(String[] args) throws Exception {
        List<String> recs = recommendForUser("mrunmayee", 5);
        Map<String, ShowInfo> metadata = loadMetadata("data/master_ott.csv");

        try (PrintWriter writer = new PrintWriter("data/collab_output.txt")) {
            for (String r : recs) {
                ShowInfo info = metadata.get(r.toLowerCase());
                if (info != null) {
                    writer.println(info.title + " (" + info.releaseYear + ") - " + info.platform + " | Genres: " + info.genres);
                } else {
                    writer.println(r);
                }
            }
        }

        System.out.println("Top recommendations written to collab_output.txt");
    }
}
