import asyncio
import os
import json
import logging
import random
from duckduckgo_search import DDGS
from config import COMPANIES_FILE, COMPANIES_DB

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

ATS_DOMAINS = {
    "greenhouse.io": "greenhouse",
    "boards.greenhouse.io": "greenhouse",
    "lever.co": "lever",
    "jobs.lever.co": "lever",
    "ashbyhq.com": "ashby",
    "api.ashbyhq.com": "ashby"
}

def clean_company_name(name):
    return name.strip()

def search_company_ats(company_name):
    """
    Searches for the company's ATS page using DuckDuckGo.
    Query: "{company_name} careers greenhouse lever ashby"
    """
    query = f"{company_name} careers software engineer greenhouse lever"
    logger.info(f"Searching for: {company_name}")
    
    try:
        with DDGS() as ddgs:
            # Get top 3 results
            results = list(ddgs.text(query, max_results=3))
            
            for res in results:
                url = res['href']
                for domain, ats_type in ATS_DOMAINS.items():
                    if domain in url:
                        logger.info(f"Found {ats_type} for {company_name}: {url}")
                        return {"name": company_name, "url": url, "ats": ats_type, "active": True}
            
            logger.warning(f"No ATS found for {company_name}")
            return {"name": company_name, "url": "", "ats": "unknown", "active": False}
            
    except Exception as e:
        logger.warning(f"Search failed for {company_name}: {e}")

    # Fallback: Direct Hit Strategy
    # Try guessing common URL patterns
    clean_name = company_name.lower().replace(" ", "").replace(".", "")
    guesses = [
        (f"https://boards.greenhouse.io/{clean_name}", "greenhouse"),
        (f"https://jobs.lever.co/{clean_name}", "lever"),
        (f"https://jobs.ashbyhq.com/{clean_name}", "ashby"),
        # Try with hyphens
        (f"https://boards.greenhouse.io/{company_name.lower().replace(' ', '-')}", "greenhouse"),
        (f"https://jobs.lever.co/{company_name.lower().replace(' ', '-')}", "lever")
    ]

    import requests
    for url, ats in guesses:
        try:
            # Short timeout, we just want to check existence
            r = requests.head(url, timeout=3)
            if r.status_code == 200:
                logger.info(f"Direct hit found: {url}")
                return {"name": company_name, "url": url, "ats": ats, "active": True}
        except:
            pass
    
    return {"name": company_name, "url": "", "ats": "unknown", "active": False}

async def process_batch(companies):
    """
    DDGS is synchronous, but we can wrap it or just run simple loop 
    since we have rate limits anyway. To be safe, we'll process sequentially
    with delays to avoid bans.
    """
    results = []
    for company in companies:
        if not company: continue
        res = search_company_ats(company)
        results.append(res)
        await asyncio.sleep(random.uniform(1.0, 3.0)) # Be nice to DDG
    return results

async def main():
    # 1. Load companies
    try:
        with open(COMPANIES_FILE, 'r') as f:
            content = f.read()
            # Handle comma separated or newline separated
            if ',' in content:
                raw_companies = [c.strip() for c in content.split(',')]
            else:
                raw_companies = [line.strip() for line in content.splitlines()]
            
            # Filter empty strings
            raw_companies = [c for c in raw_companies if c]
    except FileNotFoundError:
        logger.error(f"{COMPANIES_FILE} not found!")
        return

    logger.info(f"Loaded {len(raw_companies)} companies.")
    
    # 2. Check if DB exists to skip already found ones
    existing_db = []
    processed_names = set()
    if os.path.exists(COMPANIES_DB):
        with open(COMPANIES_DB, 'r') as f:
            existing_db = json.load(f)
            processed_names = {item['name'] for item in existing_db}
    
    companies_to_process = [c for c in raw_companies if c not in processed_names]
    logger.info(f"New companies to process: {len(companies_to_process)}")
    
    # 3. Process
    # User requested all companies. 
    # Warning: synchronous loop with sleeps will take a long time for 1000 items (1-3s each -> ~50 mins).
    # We will process ALL but maybe faster sleep or batch saving.
    
    batch_size = 10
    total_processed = 0
    
    # Process in chunks to save progress
    for i in range(0, len(companies_to_process), batch_size):
        subset = companies_to_process[i:i+batch_size]
        new_results = await process_batch(subset)
        
        # Append and Save immediately
        existing_db.extend(new_results)
        with open(COMPANIES_DB, 'w') as f:
            json.dump(existing_db, f, indent=2)
            
        total_processed += len(subset)
        logger.info(f"Progress: {total_processed}/{len(companies_to_process)} saved.")

if __name__ == "__main__":
    asyncio.run(main())
