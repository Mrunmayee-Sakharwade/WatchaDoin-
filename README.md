# 🎬 WatchaDoin'?

**WatchaDoin'?** is an AI-powered OTT content recommender system built with Flask and pandas. It helps users discover shows across platforms like Netflix, Prime, and Hotstar using personalized preferences like genre, mood, language, and platform.

## 🚀 Features

- Personalized recommendations based on user mood, genre, language, and platform
- Reuse past preferences
- View recommendation history
- Content-based scoring using NLP-like filters
- Clean and intuitive web UI

## 📊 Power BI Dashboard

The initial analysis and insights were built in Power BI to understand user preferences and platform trends.

## 📁 Folder Structure

├── app/
│ ├── routes.py
│ ├── user_profile.py
│ ├── static/style.css
│ └── templates/
│ ├── index.html
│ ├── preferences.html
│ ├── recommendations.html
│ └── history.html
├── recommender/
│ ├── ContentBasedRecommender.java
│ ├── CollaborativeRecommender.java
│ ├── Show.java
│ └── ShowInfo.java
├── data/master_ott.csv
├── user_data.json
├── run.py
├── requirements.txt
├── ott dashboard.pbix
└── README.md


## ⚙️ How to Run

1. Clone the repo  
   `git clone https://github.com/yourusername/WatchaDoin.git && cd WatchaDoin`

2. Install dependencies  
   `pip install -r requirements.txt`

3. Run the app  
   `python run.py`

4. Open in browser: [http://127.0.0.1:5000](http://127.0.0.1:5000)

## 💡 Dataset

Used: `data/master_ott.csv`  
Make sure the dataset contains:
- `title`, `release_year`, `listed_in`, `platform`, `country` or `language`, `description`

## 👩‍💻 Made with ❤️ by:
Mrunmayee Sakharwade
Aniruddha Joshi
