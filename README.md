
# Implémentation d'un cluster Akka pour forwarder des messages dans un graphe
BARCHID Sami
01/04/2019

## Introduction
Le contenu de ce projet, développé en Java avec le framework de programmation distribuée Akka, implémente un cluster d'acteurs qui communiquent ensemble pour se forwarder des messages selon un graphe précis.

## Guide d'utilisation
### Exécution
Lancer la commande suivante (à la racine du projet) :
```mvn compile exec:exec```

Du fait que l'approche du clustering de Akka a été utilisée, le programme ne peut pas fonctionner sans le fonctionnement de deux JVM simultanées. En effet, il faut, une première fois, démarrer le projet en mode "Seed-nodes", permettant de créer les liens du Cluster, et une deuxième fois (voire +) en mode fonctionnel.

### Configurer le graphe de noeuds du cluster
Le graphe de noeuds d'acteurs du cluster peut être configurer grâce à un fichier "actors-full.properties" modifiable à chaque démarrage d'une nouvelle JVM pour le Cluster.

```properties
1=2,5
2=3,4
5=6
6=4
4=NO
3=NO
```
##### Syntaxe 
```properties
{numeroDuNoeudACreer}={numeroDuFils1},{numeroDuFils2},.....
{numeroDuNoeudSansFils}=NO
```
*Important* : les noeuds fils ne seront pas créés, seuls les noeuds déclarés en clé seront effectivement créés.

### Usage
Un client en invite de commande interactif est présent à la fin du chargement d'une JVM pour pouvoir contrôler les noeuds liés à la JVM dans le cluster. Les opérations possibles :
- Créer un noeud
- Envoyer un message
- Supprimer un lien entre deux noeuds 
- Ajouter un lien entre deux noeuds 
- Stopper un noeud


## Architecture

### Organisation en packages
Deux packages composent l'application
- **akka** : package principale (où se trouve la classe Main) et qui sert à l'interaction et  au démarrage de l'application avec le client interactif.
- **akka.actors** : package chargé de la gestion des acteurs, du chargement de leurs configurations initiales ainsi que des messages des acteurs eux-mêmes.

## Gestion d'erreurs
La spécificité du mode Clustering de Akka permet de ne pas avoir à gérer les erreurs liées à la liaison entre acteurs par le réseau (et donc à ne pas avoir à gerer d'exception). Comme tout est géré du côté du framework, il n'y a aucune gestion d'erreur pour ce projet.