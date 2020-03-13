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