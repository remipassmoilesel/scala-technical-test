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


## Utiliser l'application

**Remarque**:

Pour éviter d'atteindre trop rapidement la limite maximum de requêtes sur des dépôts volumineux,
vous pouvez définir une variable AUTHORIZATION_HEADER pour vous connecter à l'API Github:

    $ export AUTHORIZATION_HEADER="Basic XXXXXXXXXXXX"
    $ sbt run
    

US 2-1: Principaux participants d'un projet:

    GET localhost:9000/github/statistics/project/:owner/:repository/top-committers
    
    {
        "comitters": [
            {
                "name": "Lukas Rytz",
                "email": "lukas.rytz@gmail.com",
                "commits": 21
            },
            {
                "name": "Seth Tisue",
                "email": "seth@tisue.net",
                "commits": 14
            },    
            ...


US 2-2: Langages les plus utilisés par un utilisateur:

    GET localhost:9000/github/statistics/user/KouglofKabyle/top-languages

    {
        "languages": [
            {
                "name": "JavaScript",
                "bytes": 2370345
            },
            {
                "name": "CSS",
                "bytes": 811499
            },
            {
                "name": "TypeScript",
                "bytes": 81572
            },
            ...


US 2-3: Nombre d'issues par jour pour un projet:

    GET localhost:9000/github/statistics/project/kubernetes/kubernetes/issues
    
    {
        "issuesPerDay": [
            {
                "date": "27/11",
                "issues": 43,
                "x": 0,
                "y": 43
            },
            {
                "date": "28/11",
                "issues": 52,
                "x": 1,
                "y": 52
            },
            {
                "date": "29/11",
                "issues": 44,
                "x": 2,
                "y": 44
            },
            ...
            
            
US 3-1, 3-2, 3-3: Surveiller en temps réel le nombre d'étoiles d'un dépôt à travers un canal Websocket:

    Utiliser un client comme: https://software.hixie.ch/utilities/js/websocket/

    GET ws://localhost:9000/github/statistics/project/watch-stars

    Exemples de requêtes:
        
        {"action": "subscribe",     "repository": "kubernetes/kubernetes", "intervalSec": 10}
        {"action": "unsubscribe",   "repository": "kubernetes/kubernetes"}
        


## Qu'est-ce que j'aurai aimé faire avec un peu plus de temps ?

- Intégrer Swagger
- Configurer Akka pour clusterisation entre JVMs
- Puis déploiement sur Kubernetes avec auto-scaling, ooohh yeeeahh !


## Ressources utilisées

- Applied akka patterns: https://www.oreilly.com/library/view/applied-akka-patterns/9781491934876/
- Doc scala: https://docs.scala-lang.org
- Scala best practices: https://github.com/alexandru/scala-best-practices
- Template Play: https://github.com/playframework/play-scala-starter-example
- Liste des liens Stackoverflow [en téléchargement au format CSV (38Mo)](http://bitly.com/98K8eH)



