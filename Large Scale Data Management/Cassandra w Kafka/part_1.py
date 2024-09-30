import json
import asyncio
from aiokafka import AIOKafkaProducer
import pandas as pd
import json
import random
import time
from faker import Faker
from datetime import datetime
import numpy as np

# Used for serializing certain datatypes
class NpEncoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, np.integer):
            return int(obj)
        if isinstance(obj, np.floating):
            return float(obj)
        if isinstance(obj, np.ndarray):
            return obj.tolist()
        return super(NpEncoder, self).default(obj)

def serializer(value):
    return json.dumps(value, cls=NpEncoder).encode()
    
    
async def produce():
    producer = AIOKafkaProducer(
        bootstrap_servers='localhost:29092',
        value_serializer=serializer,
        compression_type="gzip")
    await producer.start()
    counter = 0
    while True:
        counter = counter + 1
        await asyncio.sleep(1) # Consider uncommenting this line for introducing delay
        # random indexes
        random_song_index = random.randint(0, song_rows - 1)
        random_name_index = random.randint(0, names_array_len)
        # json message creation
        data = dict()  # Fix indentation here
        data['song'] = df.loc[random_song_index, 'name']
        data['personname'] = random_names[random_name_index]
        data['listenattime'] = datetime.now().isoformat()
        await producer.send(topic, data)
        # break limit
        if counter > 1000:
            break
    await producer.stop()

# Create array of 10 random names
fake = Faker()
names_array_len = 10
random_names = [fake.name() for _ in range(names_array_len)]
random_names.append('Mr. Fanis Nitsos')

# Read songs
df = pd.read_csv('spotify-songs.csv')
song_rows = len(df)

topic = 'test'
loop = asyncio.get_event_loop()
result = loop.run_until_complete(produce())


