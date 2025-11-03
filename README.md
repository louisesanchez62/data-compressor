# Data-Compressor

Projet de compression de données pour optimiser la transmission et le stockage de tableaux d'entiers.

## Description du projet

Data-Compressor implémente trois algorithmes de compression par bitpacking, chacun adapté à différents scénarios d'utilisation. Le projet permet de compresser des tableaux d'entiers en utilisant seulement le nombre de bits nécessaire pour représenter les valeurs, réduisant ainsi significativement l'espace mémoire utilisé.

### Algorithmes implémentés

**BitPacking Aligned (ALIGNED)**
- Les valeurs sont alignées sur les limites de mots de 32 bits
- Accès direct ultra-rapide sans calcul complexe
- Peut laisser quelques bits inutilisés à la fin de chaque mot
- Recommandé pour : bases de données, caches, accès aléatoire fréquent

**BitPacking Overlapped (OVERLAPPED)**
- Les valeurs peuvent chevaucher plusieurs mots de 32 bits
- Aucun bit gaspillé, compression maximale
- Accès légèrement plus lent que ALIGNED
- Recommandé pour : transmission réseau, stockage long terme, compression maximale

**BitPacking with Overflow (OVERFLOW)**
- Utilise un nombre réduit de bits pour les valeurs courantes
- Table de débordement pour les valeurs exceptionnelles
- Optimisation automatique du nombre de bits via algorithme `findBestBitSize`
- Recommandé pour : données avec outliers, distributions inégales

### Architecture

Le projet suit les principes du Domain-Driven Design (DDD) avec une séparation claire des responsabilités :

```
src/main/java/
├── application/              # Couche application
│   ├── Main.java            # Point d'entrée du programme
│   └── utils/
│       ├── Benchmark.java   # Tests de performance comparatifs
│       ├── Statistics.java  # Statistiques de compression
│       └── TestCases.java   # Tests fonctionnels
│
└── domain/                   # Couche domaine
    ├── BitPacking.java      # Interface principale
    │
    ├── entities/
    │   ├── PackedData.java      # Données compressées
    │   └── UnpackedData.java    # Données non compressées
    │
    ├── exception/
    │   ├── CompressionException.java
    │   ├── InvalidDataException.java
    │   ├── PackedDataException.java
    │   └── UnpackedDataException.java
    │
    └── factory/
        ├── BitPackingFactory.java      # Factory pour créer les compresseurs
        ├── BitPackingRegistry.java     # Registre des implémentations
        ├── CompressionTypeEnum.java    # Types de compression disponibles
        │
        └── products/
            ├── BitpackingAligned.java
            ├── BitpackingOverlapped.java
            └── BitpackingWithOverflow.java
```

### Patterns de conception utilisés

**Factory Pattern** : La classe `BitPackingFactory` centralise la création des instances de compresseurs.

**Registry Pattern** : `BitPackingRegistry` permet l'enregistrement dynamique des implémentations via des blocs static dans chaque classe de bitpacking.

**Strategy Pattern** : L'interface `BitPacking` définit le contrat, les implémentations concrètes fournissent différentes stratégies de compression.

## Prérequis

- Java JDK 21 ou supérieur
- Maven 3.6+ installé
- Un IDE Java (IntelliJ IDEA recommandé)

## Installation et compilation

### Avec Maven en ligne de commande

```bash
# Naviguer vers le répertoire du projet
cd C:\Users\louis\IdeaProjects\data-compressor

# Compiler le projet
mvn clean compile

# Exécuter la classe Main
java -cp target\classes application.Main
```

Ou en une seule commande :
```bash
mvn clean compile exec:java -Dexec.mainClass="application.Main"
```

### Avec IntelliJ IDEA

1. Ouvrir le fichier `Main.java`
2. Clic droit puis "Run 'Main.main()'"
3. Ou cliquer sur le bouton vert à côté de la méthode main

### Créer un JAR exécutable

```bash
# Créer le JAR
mvn clean package

# Exécuter le JAR
java -jar target\data-compressor-1.0-SNAPSHOT.jar
```

## Licence

Ce projet est développé à des fins éducatives.

## Auteur

Louise SANCHEZ - 2025
