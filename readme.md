# Hadoop/Spark Workshop

Groupe : **PAFFME***

## Récupération des tweets

On a fait un utilitaire en Node.js qui utilise l'API Streaming de Twitter :

```js
const Twit = require('twit')

const T = new Twit({
  consumer_key: '',
  consumer_secret: '',
  access_token: '',
  access_token_secret: '',
  timeout_ms: 60*1000,
  strictSSL: true
})

const fs = require('fs');

const stream = T.stream('statuses/sample', { language: 'en' })
let i = 0;

stream.on('tweet', function (tweet) {
  console.log(++i);
  fs.appendFileSync('tweets.txt', JSON.stringify({created_at: tweet.created_at, text: tweet.text}) + '\n');
})

stream.on('error', function(err) {
  console.error(err);
});
 ```
 
 Cela créé un fichier JSON qui est mis à jour pour chaque nouveau tweet reçu. On a galéré à mettre ce JSON dans Hive, du coup on a fait un autre utilitaire pour mettre ça dans un CSV :
 
 ```js
const createCsvWriter = require('csv-writer').createObjectCsvWriter;
const csvWriter = createCsvWriter({
  path: 'out.csv',
  header: [
    {id: 'text', title: 'text'},
    {id: 'tdate', title: 'tdate'},
    {id: 'hashtags', title: 'hashtags'},
  ]
});

const fs = require('fs');
const readline = require('readline');
const lineReader = readline.createInterface({
  input: fs.createReadStream('tweets.txt'),
});

const data = [];

lineReader.on('line', function(line) {
 const parsedLine = JSON.parse(line);
  data.push({
    tdate: Math.round(new Date(parsedLine.created_at).getTime() / 1000),
    text: parsedLine.text.replace(/(\r\n|\n|\r)/gm, ""),
    hashtags: parsedLine.text.match(/#\w+/gm)
  });
});

lineReader.on('close', function() {
  csvWriter.writeRecords(data).then(() => console.log('done'));
});
 ```
 
Dans ce script pour transformer le JSON en CSV on en profite pour extraire les hashtags et retirer les \n du texte du tweet (sinon il y a beaucoup de ligne à NULL dans Hive).

On a dû récuperer 180k tweets en 5/6h qui sont aujourd'hui dans Hive.

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

# checklist

- [x] Récupérer les tweet en anglais et les stocker sur HDFS **Node.js**
- [x] Préparer les tweet pour un traitement optimal par la suite **Node.js**
- [ ] implémenter l’algorithme de kmeans (interdit d’utiliser celui fourni dans spark)
- [x] Chaque Heure -> **on arrive pas à vérifier via les logs que c'est ok, la config est faite**
- [x] Calculer le Top 10 des hashtag les plus utilisé **dans le code Spark**
- [ ] Pour chaque hashtag calculer son évolution par rapport à l’heure précédente **On a les infos dans la table statistics, on a pas eu le temps de créer le code pour calculer l'évolution**
- [ ] Appliquer le kmeans sur la latitude et longitude des tweet et/ou appliquer le kmeans sur le nombre de mots et le nombre de hashtag dans les tweet
