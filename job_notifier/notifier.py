import logging
import asyncio
from telegram import Bot
from config import TELEGRAM_BOT_TOKEN, TELEGRAM_CHAT_ID

logger = logging.getLogger(__name__)

class TelegramNotifier:
    def __init__(self):
        self.enabled = False
        if TELEGRAM_BOT_TOKEN and TELEGRAM_CHAT_ID:
            self.bot = Bot(token=TELEGRAM_BOT_TOKEN)
            self.chat_id = TELEGRAM_CHAT_ID
            self.enabled = True
            logger.info("Telegram Notifier enabled.")
        else:
            logger.warning("Telegram settings missing. Notifications disabled.")

    async def send_job_alert(self, job):
        if not self.enabled:
            logger.info(f"[Mock Notify] New Job: {job['title']} at {job['company']}")
            return

        message = (
            f"🚨 *New Job Alert* 🚨\n\n"
            f"*Company*: {job['company']}\n"
            f"*Role*: {job['title']}\n"
            f"[Apply Here]({job['url']})"
        )
        
        try:
            await self.bot.send_message(
                chat_id=self.chat_id, 
                text=message, 
                parse_mode='Markdown'
            )
            logger.info(f"Sent alert for {job['title']}")
        except Exception as e:
            logger.error(f"Failed to send Telegram message: {e}")

    async def send_stats_report(self, new_count, total_scanned):
        if not self.enabled: return
        
        msg = f"📊 *Scan Complete*\nScanned: {total_scanned}\nNew Jobs: {new_count}"
        try:
            await self.bot.send_message(chat_id=self.chat_id, text=msg, parse_mode='Markdown')
        except:
            pass
