package application.utils;

import domain.BitPacking;
import domain.entities.PackedData;
import domain.entities.UnpackedData;
import domain.factory.BitPackingFactory;
import domain.factory.CompressionTypeEnum;

import java.util.Random;

public class Benchmark {

    public void run() {
        System.out.println("\n========================================");
        System.out.println("    Analyse de Performance");
        System.out.println("========================================");

        int[] taillesTableaux = {1000, 10000, 100000};

        for (int taille : taillesTableaux) {
            executerTests(taille);
        }
    }

    private void executerTests(int taille) {
        System.out.println("\n--- Taille du dataset : " + String.format("%,d", taille) + " valeurs ---");

        System.out.println("\nScénario 1 : Distribution uniforme (0-100)");
        analyserPerformance(genererUniforme(taille, 100));

        System.out.println("\nScénario 2 : Présence de valeurs aberrantes (2%)");
        analyserPerformance(genererAvecOutliers(taille, 100, 1000000, 0.02));

        System.out.println("\nScénario 3 : Plage de valeurs étendue (0-100000)");
        analyserPerformance(genererUniforme(taille, 100000));
    }

    private void analyserPerformance(int[] donnees) {
        System.out.println("\nAlgorithme          | Compression | Décompression | Accès      | Réduction");
        System.out.println("--------------------+-------------+---------------+------------+-----------------");

        testerAlgorithme(CompressionTypeEnum.ALIGNED, "Aligned", donnees);
        testerAlgorithme(CompressionTypeEnum.OVERLAPPED, "Overlapped", donnees);
        testerAlgorithme(CompressionTypeEnum.OVERFLOW, "Overflow", donnees);
    }

    private void testerAlgorithme(CompressionTypeEnum type, String nom, int[] donnees) {
        BitPacking methode = BitPackingFactory.createBitPacking(type);
        UnpackedData source = UnpackedData.from(donnees);

        // Préchauffage
        for (int i = 0; i < 150; i++) {
            PackedData temp = PackedData.empty();
            methode.compress(source, temp);
        }

        // Mesure : Compression
        long totalCompression = 0;
        PackedData resultat = PackedData.empty();
        for (int i = 0; i < 400; i++) {
            PackedData temp = PackedData.empty();
            long debut = System.nanoTime();
            methode.compress(source, temp);
            totalCompression += System.nanoTime() - debut;
            if (i == 399) resultat = temp;
        }
        double moyenneCompression = totalCompression / 400.0;

        // Mesure : Décompression
        long totalDecompression = 0;
        for (int i = 0; i < 400; i++) {
            UnpackedData temp = UnpackedData.empty();
            long debut = System.nanoTime();
            methode.decompress(resultat, temp);
            totalDecompression += System.nanoTime() - debut;
        }
        double moyenneDecompression = totalDecompression / 400.0;

        // Mesure : Accès direct
        Random generateur = new Random(123);
        long totalAcces = 0;
        int iterations = 400;
        int accesParIteration = 10;
        for (int i = 0; i < iterations; i++) {
            for (int j = 0; j < accesParIteration; j++) {
                int index = generateur.nextInt(donnees.length);
                long debut = System.nanoTime();
                methode.get(index);
                totalAcces += System.nanoTime() - debut;
            }
        }
        double moyenneAcces = totalAcces / (double)(iterations * accesParIteration);

        // Statistiques
        int bitsOriginaux = donnees.length * 32;
        int bitsComprimes = resultat.getData().length * 32;
        int bitsEconomises = bitsOriginaux - bitsComprimes;
        double pourcentageReduction = (100.0 * bitsEconomises) / bitsOriginaux;

        System.out.printf("%-19s | %8.2f µs | %10.2f µs | %7.2f ns | %d bits (%.1f%%)%n",
                nom,
                moyenneCompression / 1000.0,
                moyenneDecompression / 1000.0,
                moyenneAcces,
                bitsEconomises,
                pourcentageReduction);
    }

    public static int[] genererUniforme(int taille, int valeurMax) {
        int[] tableau = new int[taille];
        Random rand = new Random(42);
        for (int i = 0; i < taille; i++) {
            tableau[i] = rand.nextInt(valeurMax + 1);
        }
        return tableau;
    }

    public static int[] genererAvecOutliers(int taille, int maxNormal, int maxOutlier, double ratioOutlier) {
        int[] tableau = new int[taille];
        Random rand = new Random(42);
        for (int i = 0; i < taille; i++) {
            if (rand.nextDouble() < ratioOutlier) {
                tableau[i] = maxNormal + rand.nextInt(maxOutlier - maxNormal + 1);
            } else {
                tableau[i] = rand.nextInt(maxNormal + 1);
            }
        }
        return tableau;
    }
}
