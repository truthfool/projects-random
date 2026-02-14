import sqlite3
import datetime
import logging
from config import DB_PATH

logger = logging.getLogger(__name__)

def init_db():
    conn = sqlite3.connect(DB_PATH)
    c = conn.cursor()
    c.execute('''
        CREATE TABLE IF NOT EXISTS jobs (
            id TEXT PRIMARY KEY,
            company TEXT,
            title TEXT,
            url TEXT,
            first_seen_at TIMESTAMP
        )
    ''')
    conn.commit()
    conn.close()

def is_job_seen(job_id):
    conn = sqlite3.connect(DB_PATH)
    c = conn.cursor()
    c.execute('SELECT 1 FROM jobs WHERE id = ?', (job_id,))
    result = c.fetchone()
    conn.close()
    return result is not None

def add_job(job_id, company, title, url):
    conn = sqlite3.connect(DB_PATH)
    c = conn.cursor()
    try:
        c.execute('''
            INSERT INTO jobs (id, company, title, url, first_seen_at)
            VALUES (?, ?, ?, ?, ?)
        ''', (job_id, company, title, url, datetime.datetime.now()))
        conn.commit()
    except sqlite3.IntegrityError:
        pass # Already exists
    conn.close()
