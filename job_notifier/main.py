import asyncio
import logging
import time
from scraper import Scraper
from database import init_db
from notifier import TelegramNotifier

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

async def job_scan():
    logger.info("Starting job scan...")
    init_db()
    
    poster = TelegramNotifier()
    
    async with Scraper() as scraper:
        jobs = await scraper.run()
        
    logger.info(f"Found {len(jobs)} new jobs.")
    
    for job in jobs:
        await poster.send_job_alert(job)
        # Small delay to avoid spamming/limits
        await asyncio.sleep(1)

    # Optional: Send summary
    if len(jobs) > 0:
        await poster.send_stats_report(len(jobs), "ALL")

async def main():
    start_time = time.time()
    await job_scan()
    duration = time.time() - start_time
    logger.info(f"Scan finished in {duration:.2f} seconds")

if __name__ == "__main__":
    asyncio.run(main())
