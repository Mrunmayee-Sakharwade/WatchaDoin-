from flask import Flask, render_template, request, redirect, url_for
from user_profile import UserProfileManager
import pandas as pd
import os
from datetime import datetime  # For saving timestamps

app = Flask(__name__)
USER_DATA_PATH = os.path.join(os.getcwd(), "user_data.json")
user_manager = UserProfileManager(USER_DATA_PATH)

# Load dataset
df = pd.read_csv("data/master_ott.csv")
df['listed_in'] = df['listed_in'].fillna("").str.lower()
df['country'] = df['country'].fillna("").str.lower()
df['description'] = df['description'].fillna("").str.lower()

@app.route("/", methods=["GET", "POST"])
def index():
    if request.method == "POST":
        username = request.form["username"].strip().lower()
        user = user_manager.get_user(username)
        if user:
            return redirect(url_for("reuse_preferences", username=username))
        else:
            return redirect(url_for("preferences", username=username))
    return render_template("index.html")

@app.route("/preferences/<username>", methods=["GET", "POST"])
def preferences(username):
    if request.method == "POST":
        genres = [g.strip() for g in request.form.get("genres", "").lower().split(",") if g.strip()]
        countries = [c.strip() for c in request.form.get("country", "").lower().split(",") if c.strip()]
        moods = [m.strip() for m in request.form.get("moods", "").lower().split(",") if m.strip()]
        platforms = [p.strip() for p in request.form.get("platforms", "").lower().split(",") if p.strip()]

        user_manager.create_user(username)
        user_manager.update_preferences(username, genres, countries, moods, platforms)

        return redirect(url_for("recommend", username=username))

    return render_template("preferences.html", username=username)

@app.route("/recommend/<username>")
def recommend(username):
    user = user_manager.get_user(username)
    if not user or df.empty:
        return "❌ No user or data", 500

    genres = set(user.get("favorite_genres", []))
    languages = set(user.get("favorite_languages", []))
    moods = set(user.get("mood_keywords", []))
    platforms = set(user.get("platforms", ["All"]))

    def score(row):
        return (
            sum(g in row["listed_in"] for g in genres) +
            sum(l in row["country"] for l in languages) +
            sum(m in row["description"] for m in moods) +
            (1 if "All" in platforms or row["platform"] in platforms else 0)
        )

    df["score"] = df.apply(score, axis=1)
    recs = df[df["score"] > 0].sort_values(by="score", ascending=False).head(10)
    final_recs = recs[["title", "release_year", "listed_in", "platform"]].to_dict(orient="records")

    # Save new recommendations to current and history
    if final_recs:
        user["old_recommendations"] = final_recs
        user.setdefault("history", []).append({
            "timestamp": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
            "recommendations": final_recs
        })
        user_manager.save_data()
    else:
        final_recs = user.get("old_recommendations", [])

    return render_template("recommendations.html", username=username, recommendations=final_recs, collaborative=[])

@app.route("/reuse/<username>", methods=["GET", "POST"])
def reuse_preferences(username):
    if request.method == "POST":
        use_previous = request.form.get("use_previous")
        if use_previous == "yes":
            return redirect(url_for("recommend", username=username))
        else:
            return redirect(url_for("preferences", username=username))
    return render_template("reuse.html", username=username)

@app.route("/history/<username>")
def history(username):
    user = user_manager.get_user(username)
    if not user:
        return "User not found", 404

    history_data = user.get("history", [])
    return render_template("history.html", username=username, history=history_data)
