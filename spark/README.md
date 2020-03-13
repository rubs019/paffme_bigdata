# Spark Metrics Example
Use Spark EventListener and groupon spark-metrics to send metrics in graphite and recreate spark UI in grafana
## Build
```shell script
mvn clean package
```
## Run

```shell script
docker run -d\
 --name graphite\
 -p 80:80\
 -p 81:81\
 -p 2003-2004:2003-2004\
 -p 2023-2024:2023-2024\
 -p 8125:8125/udp\
 -p 8126:8126\
 hopsoft/graphite-statsd

mvn clean package && spark-submit --conf spark.metrics.conf=metrics.properties --class fr.xebia.xke.SparkMetricsExample target/spark_metrics-1.0-SNAPSHOT-shaded.jar
```