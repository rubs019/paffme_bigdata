package fr.xebia.xke

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{from_unixtime, length, lit, size, split}
import java.text.SimpleDateFormat
import scala.collection.{immutable, mutable}

object SparkMetricsExample {
  val APP_NAME = "spark-metrics-example"
  def main(args: Array[String]): Unit = {
    implicit val spark = SparkSession.builder()
      .appName(APP_NAME)
      .enableHiveSupport()
      .getOrCreate()

    import spark.implicits._

    val sdf = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss.SSS")
    val rawFrom: Long = sdf.parse("03-11-2020 16:00:00.000" /* args[3] */).getTime() / 1000
    val rawTo: Long = sdf.parse("03-11-2020 17:00:00.000" /* args[4] */).getTime() / 1000
    val from = from_unixtime(lit(rawFrom))
    val to = from_unixtime(lit(rawTo))

    spark.sql("USE paffme")

    val hashtagOccurence = collection.mutable.Map[String, Int]().withDefaultValue(0)

   spark.read.table("tweet_v6")
    .withColumn("timestamp", from_unixtime($"tdate")) // create a usable timestamp
    .filter($"timestamp" > from && $"timestamp" < to) // remove rows outside of time interval
    .filter(length($"hashtags") > 0) // remove rows without hashtags
    .withColumn("splittedHashtags", split($"hashtags", ",")) // split hashtags into an array
    .filter(size($"splittedHashtags") > 0) // remove tweets without hashtags
    .collect.foreach { row =>
      // Update a map which contains hashtags occurences
      val hashtags = row.getAs[Seq[String]]("splittedHashtags")

      for (hashtag <- hashtags) {
        hashtagOccurence.update(hashtag, hashtagOccurence(hashtag) + 1)
      }
    }

    // Insert the computed data into Hive
    val dataToInsert: mutable.ListBuffer[(String, Int, Long, Long)] = mutable.ListBuffer() // pas trouvé moyen de faire un map ici, je connais mal Scala

    for ((k, v) <- hashtagOccurence) {
      dataToInsert .+= ((k, v, rawFrom, rawTo))
    }

    val data = spark.sqlContext.createDataFrame(dataToInsert).toDF("hashtag", "occurences", "from", "to")
    data.write.mode("append").format("hive").saveAsTable("statistics")

    // Print the top 10 hashtags of the time interval
    val sortedMap = immutable.ListMap(hashtagOccurence.toList.sortBy(_._2):_*).takeRight(10)

    println("TOP 10")

    for ((k, v) <- sortedMap) {
      print(k)
      print(" = ")
      println(v)
    }

    println("JOB DONE")

    spark.stop()
  }
}