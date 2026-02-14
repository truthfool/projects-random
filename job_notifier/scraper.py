import asyncio
import aiohttp
import logging
import json
from bs4 import BeautifulSoup
from config import KEYWORDS, USER_AGENT
from database import is_job_seen, add_job

logger = logging.getLogger(__name__)

class Scraper:
    def __init__(self, companies_db_path="companies_db.json"):
        self.companies_db_path = companies_db_path
        self.session = None

    async def __aenter__(self):
        self.session = aiohttp.ClientSession(headers={"User-Agent": USER_AGENT})
        return self

    async def __aexit__(self, exc_type, exc, tb):
        await self.session.close()

    def load_companies(self):
        try:
            with open(self.companies_db_path, 'r') as f:
                data = json.load(f)
                # Filter for active companies with known ATS
                return [c for c in data if c.get('active')]
        except FileNotFoundError:
            return []

    def filter_job(self, title):
        title_lower = title.lower()
        # Must have at least one keyword
        matches = any(k.lower() in title_lower for k in KEYWORDS)
        return matches

    async def fetch_page(self, url):
        try:
            async with self.session.get(url, timeout=10) as response:
                if response.status == 200:
                    return await response.text()
        except Exception as e:
            logger.error(f"Failed to fetch {url}: {e}")
        return None

    # --- Drivers ---

    async def scrape_greenhouse(self, company):
        # Greenhouse often needs the specific board ID from the URL
        # URL usually: https://boards.greenhouse.io/company
        # API is cleaner: https://boards-api.greenhouse.io/v1/boards/{company_token}/jobs
        # But for now let's scrape HTML to be safe against API changes/auth
        
        html = await self.fetch_page(company['url'])
        if not html: return []
        
        soup = BeautifulSoup(html, 'html.parser')
        jobs = []
        
        # Look for job divs
        # Greenhouse structure varies but often has <div class="opening">
        for opening in soup.find_all('div', class_='opening'):
            link = opening.find('a')
            if not link: continue
            
            title = link.text.strip()
            href = link.get('href')
            if not href.startswith('http'):
                href = f"https://boards.greenhouse.io{href}"
                
            if self.filter_job(title):
                job_id = href # Use URL as ID
                jobs.append({
                    "id": job_id,
                    "company": company['name'],
                    "title": title,
                    "url": href
                })
        return jobs

    async def scrape_lever(self, company):
        # Lever API is public: https://api.lever.co/v0/postings/{company}?mode=json
        # Extract company name from URL: https://jobs.lever.co/foo -> foo
        company_slug = company['url'].rstrip('/').split('/')[-1]
        api_url = f"https://api.lever.co/v0/postings/{company_slug}?mode=json"
        
        try:
            async with self.session.get(api_url) as response:
                if response.status == 200:
                    data = await response.json()
                    jobs = []
                    for job in data:
                        title = job['text']
                        if self.filter_job(title):
                            jobs.append({
                                "id": job['id'],
                                "company": company['name'],
                                "title": title,
                                "url": job['hostedUrl']
                            })
                    return jobs
        except Exception as e:
            logger.error(f"Lever API failed for {company['name']}: {e}")
        return []

    async def scrape_ashby(self, company):
        # Ashby HTML usually has <script id="__NEXT_DATA__"> with JSON
        # Or we can just parse the generic specific meta tags or API.
        # Simplest Ashby API: https://api.ashbyhq.com/posting-api/job-board/{company}
        
        company_slug = company['url'].rstrip('/').split('/')[-1]
        api_url = f"https://api.ashbyhq.com/posting-api/job-board/{company_slug}"
        
        try:
            async with self.session.get(api_url) as response:
                if response.status == 200:
                    data = await response.json()
                    jobs = []
                    for job in data.get('jobs', []):
                        title = job['title']
                        if self.filter_job(title):
                            jobs.append({
                                "id": job['id'],
                                "company": company['name'],
                                "title": title,
                                "url": job['jobUrl']
                            })
                    return jobs
        except Exception as e:
            logger.error(f"Ashby API failed for {company['name']}: {e}")
        return []

    async def scrape_company(self, company):
        try:
            if company['ats'] == 'greenhouse':
                return await self.scrape_greenhouse(company)
            elif company['ats'] == 'lever':
                return await self.scrape_lever(company)
            elif company['ats'] == 'ashby':
                return await self.scrape_ashby(company)
            # Add more ATS here
        except Exception as e:
            logger.error(f"Error scraping {company['name']}: {e}")
        return []

    async def run(self):
        companies = self.load_companies()
        logger.info(f"Targeting {len(companies)} active companies.")
        
        results = []
        for company in companies:
            new_jobs = await self.scrape_company(company)
            for job in new_jobs:
                if not is_job_seen(job['id']):
                    add_job(job['id'], job['company'], job['title'], job['url'])
                    results.append(job)
        
        return results
