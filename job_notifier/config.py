import os

# Telegram Settings
TELEGRAM_BOT_TOKEN = os.getenv("TELEGRAM_BOT_TOKEN", "")
TELEGRAM_CHAT_ID = os.getenv("TELEGRAM_CHAT_ID", "")

# Job Filtering
KEYWORDS = [
    "Software", "Engineer", "Developer", 
    "Backend", "Frontend", "Fullstack", 
    "DevOps", "SRE", "Systems",
    "Python", "Java", "Go", "Golang", "Rust", 
    "C++", "JavaScript", "TypeScript", "React", "Node"
]

# Scraping Settings
MAX_CONCURRENT_REQUESTS = 10
USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

# Database
DB_PATH = "jobs.db"
COMPANIES_FILE = "companies.txt"
COMPANIES_DB = "companies_db.json"
