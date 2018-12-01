# Test technique Scala / Play framework

## Introduction

Sujet original du test technique: [documentation/sujet-original.md]

Deux epics sur trois sont à réaliser. J'ai choisi:

- Epic 2 : Récupération de statistiques sur Github
- Epic 3 : Indicateurs temps réel


## Pré-requis

Utiliser GNU/Linux et avoir installé:

- Openjdk >= 8
- sbt

Voir: [documentation/installation-preprequis.md]


## Commandes de base

Démarrer l'application:

    $ sbt clean run
    
Rapport de couverture de test:
    
    $ sbt clean coverage test coverageReport

Voir ensuite: [target/scala-2.12/scoverage-report/index.html]
    
Construction du projet en Fatjar et lancement:

    $ sbt assembly
    $ java -Dplay.http.secret.key=cb55e60f67ed735e60a551b58e4d61f1628ae3d3848509c539 -jar target/scala-2.12/play-scala-starter-example-assembly-1.0-SNAPSHOT.jar


## Ressources

- Template Play: https://github.com/playframework/play-scala-starter-example


