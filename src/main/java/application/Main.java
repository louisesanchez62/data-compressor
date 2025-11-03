package application;

import application.utils.Benchmark;
import domain.BitPacking;
import domain.entities.PackedData;
import domain.entities.UnpackedData;
import domain.factory.BitPackingFactory;
import domain.factory.CompressionTypeEnum;

import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean continuer = true;

        while (continuer) {
            montrerMenu();

            if (!scanner.hasNextInt()) {
                System.out.println("\nVeuillez entrer un chiffre valide");
                scanner.nextLine();
                continue;
            }
            int selection = scanner.nextInt();

            switch (selection) {
                case 1 -> lancerBenchmark();
                case 2 -> essayerCompression(scanner, CompressionTypeEnum.ALIGNED);
                case 3 -> essayerCompression(scanner, CompressionTypeEnum.OVERLAPPED);
                case 4 -> essayerCompression(scanner, CompressionTypeEnum.OVERFLOW);
                case 5 -> {
                    System.out.println("\nFermeture du programme");
                    continuer = false;
                }
                default -> System.out.println("\nOption non reconnue");
            }
        }

        scanner.close();
    }

    private static void montrerMenu() {
        System.out.println("\n========================================");
        System.out.println("    BitPacking - Interface");
        System.out.println("========================================");
        System.out.println(" 1 - Exécuter le benchmark complet");
        System.out.println(" 2 - Essayer la méthode Aligned");
        System.out.println(" 3 - Essayer la méthode Overlapped");
        System.out.println(" 4 - Essayer la méthode Overflow");
        System.out.println(" 5 - Quitter le programme");
        System.out.println("========================================");
        System.out.print("Sélection : ");
    }

    private static void lancerBenchmark() {
        Benchmark bench = new Benchmark();
        bench.run();
    }

    private static void essayerCompression(Scanner scanner, CompressionTypeEnum typeCompression) {
        BitPacking compresseur = BitPackingFactory.createBitPacking(typeCompression);

        System.out.println("\n========================================");
        System.out.println("Mode : " + typeCompression.name());
        System.out.println("========================================");
        System.out.println("\nSaisie des données :");
        System.out.println("Format attendu : val1,val2,val3,...");
        System.out.println("Exemple : 4,10,255,1024,16");
        System.out.print("\nVos valeurs : ");
        scanner.nextLine();
        String saisie = scanner.nextLine();

        String[] elements = saisie.split(",");
        int[] valeurs = new int[elements.length];
        try {
            for (int i = 0; i < elements.length; i++) {
                valeurs[i] = Integer.parseInt(elements[i].trim());
            }
        } catch (NumberFormatException e) {
            System.out.println("\nErreur de format dans la saisie");
            return;
        }

        System.out.println("\nDonnées saisies : " + Arrays.toString(valeurs));
        System.out.printf("Volume : %d valeurs -> %d bits\n", valeurs.length, valeurs.length * 32);

        UnpackedData origine = UnpackedData.from(valeurs);
        PackedData resultat = PackedData.empty();

        try {
            compresseur.compress(origine, resultat);
            System.out.println("\nCompression effectuée");

            int tailleResultat = resultat.getData().length;
            int bitsResultat = tailleResultat * 32;
            int economie = (valeurs.length * 32) - bitsResultat;
            double taux = (100.0 * economie) / (valeurs.length * 32);

            System.out.printf("Données compressées : %d ints -> %d bits\n", tailleResultat, bitsResultat);
            System.out.printf("Réduction : %d bits (%.1f%%)\n", economie, taux);
        } catch (Exception e) {
            System.out.println("\nÉchec de la compression : " + e.getMessage());
            return;
        }

        gererOperations(scanner, compresseur, resultat, valeurs);
    }

    private static void gererOperations(Scanner scanner, BitPacking compresseur,
                                        PackedData donnees, int[] reference) {
        boolean revenir = false;

        while (!revenir) {
            System.out.println("\n--- Actions disponibles ---");
            System.out.println("1 - Décompresser les données");
            System.out.println("2 - Consulter une valeur");
            System.out.println("3 - Voir les données compressées");
            System.out.println("4 - Menu principal");
            System.out.print("\nAction : ");

            if (!scanner.hasNextInt()) {
                System.out.println("\nSaisie incorrecte");
                scanner.nextLine();
                continue;
            }
            int choixAction = scanner.nextInt();

            switch (choixAction) {
                case 1 -> effectuerDecompression(compresseur, donnees, reference);
                case 2 -> consulterValeur(scanner, compresseur, reference);
                case 3 -> visualiserDonnees(donnees);
                case 4 -> revenir = true;
                default -> System.out.println("\nChoix non valide");
            }
        }
    }

    private static void effectuerDecompression(BitPacking compresseur, PackedData donnees, int[] reference) {
        try {
            UnpackedData sortie = UnpackedData.empty();
            compresseur.decompress(donnees, sortie);

            System.out.println("\nDécompression réalisée");
            System.out.println("Données obtenues : " + Arrays.toString(sortie.getData()));

            boolean identique = Arrays.equals(reference, sortie.getData());
            if (identique) {
                System.out.println("Contrôle : correspondance avec l'original");
            } else {
                System.out.println("Alerte : non conforme à l'original !");
            }
        } catch (Exception e) {
            System.out.println("\nProblème durant la décompression : " + e.getMessage());
        }
    }

    private static void consulterValeur(Scanner scanner, BitPacking compresseur, int[] reference) {
        System.out.printf("\nPosition souhaitée (0 à %d) : ", reference.length - 1);

        if (!scanner.hasNextInt()) {
            System.out.println("\nPosition invalide");
            scanner.nextLine();
            return;
        }
        int position = scanner.nextInt();

        if (position < 0 || position >= reference.length) {
            System.out.println("\nPosition en dehors des limites");
            return;
        }

        try {
            int valeurLue = compresseur.get(position);
            System.out.printf("\nPosition [%d] : %d\n", position, valeurLue);
            System.out.printf("Référence : %d\n", reference[position]);

            if (valeurLue == reference[position]) {
                System.out.println("Validation : valeur exacte");
            } else {
                System.out.println("Erreur : valeur erronée !");
            }
        } catch (Exception e) {
            System.out.println("\nProblème d'accès : " + e.getMessage());
        }
    }

    private static void visualiserDonnees(PackedData donnees) {
        int[] contenu = donnees.getData();
        System.out.printf("\nContenu compressé (%d éléments) :\n", contenu.length);

        int affichageMax = 18;
        System.out.print("[");
        for (int i = 0; i < Math.min(contenu.length, affichageMax); i++) {
            System.out.print(contenu[i]);
            if (i < contenu.length - 1) System.out.print(", ");
        }
        if (contenu.length > affichageMax) {
            System.out.printf(", ... (encore %d valeurs)", contenu.length - affichageMax);
        }
        System.out.println("]");
    }
}
