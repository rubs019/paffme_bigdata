# Hadoop/Spark Workshop
*groupe : **PAFFME***

## Espace de travail
Se connecter au SSH : 

    ssh paffme@edge3.sagean.fr

### Copie du .csv
Copie du .csv en local vers le serveur distant :

    scp ./final_out.csv paffme@edge3.sagean.fr:~/

### Création de l'espace de travail
    hdfs dfs -mkdir data && hdfs dfs -mkdir data/tweet_v2

### Copie du .csv vers le HDFS
    hdfs dfs -put final_out.csv /user/paffme/data/tweet_v2

## Hive
En-tête : 

    from pyspark.sql import SparkSession
    from pyspark.sql import Row

    spark = SparkSession \
    .builder \
    .appName("Twitter-App") \
    .enableHiveSupport() \
    .getOrCreate()

Creation de la table pour les tweets

text STRING

tdate STRING

hashtags STRING

    spark.sql("CREATE EXTERNAL TABLE IF NOT EXISTS paffme.tweet_v7(text STRING, tdate STRING, hashtags STRING) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE Location '/user/paffme/data/tweet_v2'")

Creation de la table pour les statistiques

text STRING

tdate STRING

hashtags STRING



    spark.sql("CREATE EXTERNAL TABLE IF NOT EXISTS paffme.tweet_v9(hashtag STRING, occurences INT, from BIGINT, to BIGINT) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE")


Si ca fonctionne passer par l'interface **HUE**