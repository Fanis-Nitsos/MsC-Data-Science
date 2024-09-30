
from pyspark.sql import SparkSession
from pyspark.sql.types import StructType,StructField,LongType,IntegerType,FloatType,StringType,TimestampType
from pyspark.sql.functions import split,from_json,col

#kafka json message schema
songSchema = StructType([
                StructField("personname", StringType(),False),
                StructField("listenattime", TimestampType(),False),
                StructField("song", StringType(),False),
            ])

spark = SparkSession.builder.appName("SSKafka").config("spark.jars.packages","org.apache.spark:spark-sql-kafka-0-10_2.12:3.5.0").getOrCreate()
spark.sparkContext.setLogLevel("ERROR")
songs_df = spark.read.csv("spotify-songs.csv", header=True).cache()

df = spark.readStream.format("kafka").option("kafka.bootstrap.servers", "localhost:29092").option("subscribe", "test").option("startingOffsets", "latest").load() 


sdf = df.selectExpr("CAST(value AS STRING)").select(from_json(col("value"), songSchema).alias("data")).select("data.*")
joined_df = sdf.join(songs_df, sdf["song"] == songs_df["name"], how="inner")

#Print stream
#joined_df.writeStream \
#  .outputMode("update") \
#  .format("console") \
#  .option("truncate", False) \
#  .start() \
#  .awaitTermination()

def writeToCassandra(writeDF, _):
  #changed table name
  writeDF.write.format("org.apache.spark.sql.cassandra").mode('append').options(table="fanis_records_2", keyspace="spotify").save()

result = None
while result is None:
    try:
        # connect, interval 30 sec added
        result = joined_df.writeStream.trigger(processingTime='30 seconds').option("spark.cassandra.connection.host","localhost:9042").foreachBatch(writeToCassandra).outputMode("update").start().awaitTermination()
    except:
         pass