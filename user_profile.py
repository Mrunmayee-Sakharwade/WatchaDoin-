import json
import os

class UserProfileManager:
    def __init__(self, path='user_data.json'):
        self.USER_DATA_FILE = path

        if not os.path.exists(self.USER_DATA_FILE):
            with open(self.USER_DATA_FILE, 'w') as f:
                json.dump({}, f)

        with open(self.USER_DATA_FILE, 'r') as f:
            self.user_data = json.load(f)

    def save_data(self):
        with open(self.USER_DATA_FILE, 'w') as f:
            json.dump(self.user_data, f, indent=4)

    def create_user(self, username):
        username = username.lower()
        if username not in self.user_data:
            self.user_data[username] = {
                "favorite_genres": [],
                "favorite_languages": [],
                "mood_keywords": [],
                "platforms": [],
                "old_recommendations": []
            }
            self.save_data()

    def update_preferences(self, username, genres, countries, moods, platforms):
        username = username.lower()
        old_recs = self.user_data.get(username, {}).get("old_recommendations", [])

        self.user_data[username] = {
            "favorite_genres": genres,
            "favorite_languages": countries,
            "mood_keywords": moods,
            "platforms": platforms,
            "old_recommendations": old_recs
        }
        self.save_data()

    def get_user(self, username):
        return self.user_data.get(username.lower())
