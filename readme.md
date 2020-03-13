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


Si ca fonctionne pas, passer par l'interface **HUE**

## Spark

Cf le projet dans le dossier `spark`

On prend en paramètre du programme l'intervalle à utiliser (from/to)

Ensuite une recherche est faite dans Hive pour filtrer les tweets

Il y a ensuite un comptage et classement des hashtags

On insère dans la table statistics de la base les données qu'on vient de calculer

On affiche le top 10 sur l'intervalle demandé

`spark-submit --deploy-mode client --class fr.xebia.xke.SparkMetricsExample spark_metrics-1.0-SNAPSHOT-shaded.jar` pour lancer le jar, on peut aussi le faire en mode cluster pour faire ça bien
