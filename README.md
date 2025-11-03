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

## Utilisation

### Exemple basique

```java
import domain.BitPacking;
import domain.factory.BitPackingFactory;
import domain.factory.CompressionTypeEnum;
import domain.entities.PackedData;
import domain.entities.UnpackedData;

// Créer les données
int[] data = {1, 2, 3, 4, 5, 100, 200, 300};
UnpackedData unpacked = UnpackedData.from(data);

// Choisir l'algorithme
BitPacking compressor = BitPackingFactory.createBitPacking(
    CompressionTypeEnum.ALIGNED
);

// Compresser
PackedData packed = PackedData.empty();
compressor.compress(unpacked, packed);

// Décompresser
UnpackedData result = UnpackedData.empty();
compressor.decompress(packed, result);

// Accès direct sans décompression complète
int value = compressor.get(2);
```

### Lancer les benchmarks

```java
import application.utils.Benchmark;

public class Main {
    public static void main(String[] args) {
        Benchmark benchmark = new Benchmark();
        benchmark.run();
    }
}
```

## Fichier de configuration

Le programme peut lire un fichier `test-data.txt` placé dans `src/main/resources/`. Format :

```
# Tests BitPackingAligned
ALIGNED|1,2,3,4,5,6,7,8,9
ALIGNED|100,200,300,400,500

# Tests BitPackingOverlapped
OVERLAPPED|1,2,3,4,5,6,7,8,9

# Tests BitPackingOverflow
OVERFLOW|10,20,30,40,50,60,70,80
OVERFLOW|100,2000,300,5000,400

# Configuration du test de performance (taille|maxValue)
PERFORMANCE|1000|1000
```

## Métriques et performances

Le système de benchmark mesure automatiquement :

- Temps de compression (millisecondes)
- Temps de décompression (millisecondes)
- Temps d'accès direct via `get()` (nanosecondes)
- Taille originale vs compressée (bytes)
- Ratio de compression (pourcentage)
- Espace économisé (bytes et pourcentage)
- Validation de l'intégrité des données

### Cas de test inclus

Le benchmark exécute automatiquement 15 cas de test :

1. Petites valeurs uniformes (0-15)
2. Valeurs moyennes (0-255)
3. Valeurs moyennes (0-1000)
4. Grandes valeurs (0-100000)
5. Très grandes valeurs (0-1000000)
6. Données avec outliers (95% petites, 5% grandes)
7. Données avec outliers (90% petites, 10% grandes)
8. Données avec outliers (80% petites, 20% grandes)
9. Données séquentielles (0 à 999)
10. Données séquentielles (0 à 9999)
11. Valeurs répétitives (10 valeurs uniques)
12. Valeurs répétitives (100 valeurs uniques)
13. Distribution exponentielle
14. Grand tableau (10000 éléments)
15. Grand tableau (50000 éléments)

## Comparaison des algorithmes

| Critère | ALIGNED | OVERLAPPED | OVERFLOW |
|---------|---------|------------|----------|
| Compression | Bon | Excellent | Variable |
| Vitesse compression | Très rapide | Rapide | Moyen |
| Vitesse décompression | Très rapide | Rapide | Moyen |
| Accès direct | Très rapide | Rapide | Rapide |
| Données uniformes | Excellent | Excellent | Bon |
| Données avec outliers | Mauvais | Mauvais | Excellent |
| Complexité | Simple | Moyenne | Complexe |
| Mémoire overflow | Non | Non | Oui |

## API principale

### Interface BitPacking

```java
public interface BitPacking {
    void compress(UnpackedData from, PackedData to);
    void decompress(PackedData from, UnpackedData to);
    int get(int index);
    CompressionTypeEnum getType();
    default int calculateRequiredBits(int maxValue);
    default int calculateRequiredWords(int elements, int bitsPerElement);
}
```

### Factory

```java
BitPacking compressor = BitPackingFactory.createBitPacking(
    CompressionTypeEnum.ALIGNED  // ou OVERLAPPED, OVERFLOW
);
```

### Entités de données

```java
// Données non compressées
UnpackedData unpacked = UnpackedData.from(int[] data);
int maxValue = unpacked.getMaxValue();
int size = unpacked.getSize();

// Données compressées
PackedData packed = PackedData.empty();
int originalSize = packed.getOriginalSize();
int compressedSize = packed.getCompressedSize();
int bitsPerValue = packed.getBitsPerValue();
```

## Dépannage

### Erreur "Cannot find Main class"

```bash
# Vérifier la compilation
mvn clean compile -X

# Vérifier l'existence du fichier .class
dir target\classes\application\Main.class
```

### Erreur "BUILD FAILURE"

```bash
# Vérifier la version de Java
java -version

# Nettoyer complètement
mvn clean
del /s target

# Recompiler
mvn compile
```

### Fichier test-data.txt non trouvé

- Vérifier que le fichier est dans `src\main\resources\`
- Forcer la copie des resources : `mvn clean resources:resources compile`

## Commandes Maven utiles

| Commande | Description |
|----------|-------------|
| `mvn clean` | Nettoie le répertoire target/ |
| `mvn compile` | Compile les sources |
| `mvn test` | Exécute les tests unitaires |
| `mvn package` | Crée le JAR |
| `mvn clean install` | Compile, teste et installe |
| `mvn dependency:tree` | Affiche l'arbre des dépendances |

## Technologies et dépendances

- Java 21
- Maven 3.x
- Aucune dépendance externe (projet standalone)

## Licence

Ce projet est développé à des fins éducatives.

## Auteur

Louis - 2025
