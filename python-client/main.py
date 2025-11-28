import json
import logging
from multiprocessing import Process
import redis
import requests
import time

url = "https://api.airplanes.live/v2/point/36.1716/-115.1391/200"
broker = "localhost"
broker_port = 6379
topics = ["traffic"]     

def data_scraper(broker, topic):
    broker = redis.Redis(host=broker, port=broker_port, db=0)
    while True:
        air_traffic = None
        data = requests.get(url)
        json_data = json.loads(data.text)
        air_traffic = json_data["ac"]

        if air_traffic:
            try:
                for plot in air_traffic:
                    logging.warning(f'''Publishing to topic {topic}''')
                    broker.publish(topic, json.dumps(plot))
            except Exception as e:
                logging.error(e)
                # Reset connection
                broker = redis.Redis(host="redis", port=broker_port, db=0)
        
        time.sleep(10)

if __name__ == "__main__":
    client = redis.Redis(host=broker, port=broker_port, db=0)
    consumer = client.pubsub()
    for topic in topics:
        adsb_scraper = Process(target=data_scraper, args=(broker,topic,))
        adsb_scraper.start()
        consumer.subscribe(topic)

    for message in consumer.listen():
        logging.warning(f'''Received message: {message}''')

