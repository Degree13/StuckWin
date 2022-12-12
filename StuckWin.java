import java.nio.channels.NonReadableChannelException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import java.util.Scanner;
import java.util.InputMismatchException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;


import javax.swing.text.StyledEditorKit.StyledTextAction;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import com.oracle.webservices.internal.impl.internalspi.encoding.StreamDecoder;

public class StuckWin {

  static final Scanner input = new Scanner(System.in);

  private static final double BOARD_SIZE = 7;

  enum Result {
    OK, BAD_COLOR, DEST_NOT_FREE, EMPTY_SRC, TOO_FAR, EXT_BOARD, EXIT
  }

  enum ModeMvt {
    REAL, SIMU
  }

  final char[] joueurs = { 'B', 'R' };

  final int SIZE = 8;

  final int SCALE = 10;

  final char VIDE = '.';

  int gamemode = 0;
  public static boolean COLLECTING_DATA = true;
  public static int affichageG = -1;

  // 'B'=bleu 'R'=rouge '.'=vide '-'=n'existe pas

  char[][] state = {

      { '-', '-', '-', '-', 'R', 'R', 'R', 'R' },

      { '-', '-', '-', 'R', 'R', 'R', 'R', '.' },

      { '-', '-', 'R', 'R', 'R', '.', '.', '.' },

      { '-', 'R', 'R', '.', '.', '.', 'B', 'B' },

      { '-', '.', '.', '.', 'B', 'B', 'B', '-' },

      { '-', '.', 'B', 'B', 'B', 'B', '-', '-' },

      { '-', 'B', 'B', 'B', 'B', '-', '-', '-' },

  };
  ArrayList<String> stateData = new ArrayList<>(10);
  ArrayList<Character> stateDataColor = new ArrayList<>(10);
  ArrayList<String> virtualStates = new ArrayList<>();
  //HashMap<String, Character> stateData = new HashMap<>();
  double coordsTab[][][] = new double[7][8][2];


    int [][][] state3 = {
      {{-1,-1}, {-1,-1}, {-1,-1}, {0,4,0,7}, {-1,-1}, {-1,-1}, {-1,-1}},
      {{-1,-1}, {-1,-1}, {1,3,0,6}, {-1,-1}, {0,5,1,7}, {-1,-1}, {-1,-1}},
      {{-1,-1}, {2,2,0,5}, {-1,-1}, {1,4,1,6}, {-1,-1}, {0,6,2,7}, {-1,-1}},
      {{3,1,0,4}, {-1,-1}, {2,3,1,5}, {-1,-1}, {1,5,2,6}, {-1,-1}, {0,7,3,7}},
      {{-1,-1}, {3,2,1,4}, {-1,-1}, {2,4,2,5}, {-1,-1}, {1,6,3,6}, {-1,-1}},
      {{4,1,1,3}, {-1,-1}, {3,3,2,4}, {-1,-1}, {2,5,3,5}, {-1,-1}, {1,7,4,6}},
      {{-1,-1}, {4,2,2,3}, {-1,-1}, {3,4,3,4}, {-1,-1}, {2,6,4,5}, {-1,-1}},
      {{5,1,2,2}, {-1,-1}, {4,3,3,3}, {-1,-1}, {3,5,4,4}, {-1,-1}, {2,7,5,5}},
      {{-1,-1}, {5,2,3,2}, {-1,-1}, {4,4,4,3}, {-1,-1}, {3,6,5,4}, {-1,-1}},
      {{6,1,3,1}, {-1,-1}, {5,3,4,2}, {-1,-1}, {4,5,5,3}, {-1,-1}, {3,7,6,4}},
      {{-1,-1}, {6,2,4,1}, {-1,-1}, {5,4,5,2}, {-1,-1}, {4,6,6,3}, {-1,-1}},
      {{-1,-1}, {-1,-1}, {6,3,5,1}, {-1,-1}, {5,5,6,2}, {-1,-1}, {-1,-1}},
      {{-1,-1}, {-1,-1}, {-1,-1}, {6,4,6,1}, {-1,-1}, {-1,-1}, {-1,-1}}
    };


    /**

     * Déplace un pion ou simule son déplacement

     * @param couleur couleur du pion à déplacer

     * @param lcSource case source Lc

     * @param lcDest case destination Lc

     * @param mode ModeMVT.REAL/SIMU selon qu'on réalise effectivement le déplacement ou qu'on le simule seulement.

     * @return enum {OK, BAD_COLOR, DEST_NOT_FREE, EMPTY_SRC, TOO_FAR, EXT_BOARD, EXIT} selon le déplacement

     */
  Result deplace(char couleur, String lcSource, String lcDest, ModeMvt mode) {
    // Traduction des Strings en Ints exploitables avec le tableau
    // System.out.println("TESTS Source colonne:" + idColSource + " Source ligne:" +
    // idLineSource + " Destination colonne :"+ idColDest + " Destination ligne:" +
    // idLineDest);
    if (lcSource.length() != 2 || lcDest.length() != 2) {
      return Result.EXIT;
    }

    int idLineSource = 55 - lcSource.charAt(1);
    int idLineDest = 55 - lcDest.charAt(1);

    int idColSource = (lcSource.charAt(1) - 48) + (lcSource.charAt(0) - 68);
    int idColDest = (lcDest.charAt(1) - 48) + (lcDest.charAt(0) - 68);

    if (idLineSource < 0 || idLineSource > 6 || idLineDest < 0 || idLineDest > 6 || idColSource < 1 || idColSource > 7
        || idColDest < 1 || idColDest > 7) {
      return Result.EXT_BOARD;
    }
    if (state[idLineSource][idColSource] == '.') {
      return Result.EMPTY_SRC;
    }
    if (state[idLineSource][idColSource] != couleur) {
      return Result.BAD_COLOR;
    }
    if (state[idLineDest][idColDest] != '.') {
      return Result.DEST_NOT_FREE;
    }
    String isTooFar = Arrays.toString(possibleDests(couleur, idLineSource, idColSource));
    // System.out.println(isTooFar);
    boolean boolTooFar = true;
    switch (couleur) {
      case 'B':
        if (isTooFar.charAt(1) == 'L' && idColDest + 1 == idColSource && idLineDest == idLineSource) {
          // System.out.println("Checkpoint1 B");
          boolTooFar = false;
        } else if (isTooFar.charAt(6) == 'T' && idLineDest + 1 == idLineSource && idColDest == idColSource) {
          // System.out.println("Checkpoint2 B");
          boolTooFar = false;
        } else if (isTooFar.charAt(11) == 'R' && idLineDest + 1 == idLineSource && idColDest - 1 == idColSource) {
          // System.out.println("Checkpoint3 B");
          boolTooFar = false;
        }
        if (boolTooFar == true) {
          return Result.TOO_FAR;
        }
        break;
      case 'R':
        if (isTooFar.charAt(1) == 'L' && (idColDest + 1 == idColSource && idLineDest - 1 == idLineSource)) {
          // System.out.println("Checkpoint1 R");
          boolTooFar = false;
        } else if (isTooFar.charAt(6) == 'D' && idLineDest - 1 == idLineSource && idColDest == idColSource) {
          // System.out.println("Checkpoint2 R");
          boolTooFar = false;
        } else if (isTooFar.charAt(11) == 'R' && idLineDest == idLineSource && idColDest - 1 == idColSource) {
          // System.out.println("Checkpoint3 R");
          boolTooFar = false;
        }
        if (boolTooFar == true) {
          return Result.TOO_FAR;
        }
        break;
      default:
        throw new java.lang.UnsupportedOperationException("Fonction deplace : Couleur incompatible");
    }
    state[idLineSource][idColSource] = '.';
    state[idLineDest][idColDest] = couleur;
    return Result.OK;
  }

  /**
   * 
   * Construit les trois chaînes représentant les positions accessibles
   * 
   * à partir de la position de départ [idLettre][idCol].
   * 
   * @param couleur  couleur du pion à jouer
   * 
   * @param idLettre id de la ligne du pion à jouer
   * 
   * @param idCol    id de la colonne du pion à jouer
   * 
   * @return tableau des trois positions jouables par le pion (redondance possible
   *         sur les bords)
   * 
   */

  String[] possibleDests(char couleur, int idLettre, int idCol) {

    if (idLettre > 6 || idLettre < 0 || idCol > 7 || idCol < 1) {
      String throwMsg = "Erreur, les valeurs entrées en paramètres de la fonction possibleDests ne correspondent à aucune case";
      throw new java.lang.UnsupportedOperationException(throwMsg);
    }

    String[] possibilites = new String[3];
    Arrays.fill(possibilites, "XXX");

    switch (couleur) {
      case 'B':
        if (idCol > 1) {
          if (state[idLettre][idCol - 1] == '.') {
            possibilites[0] = "LFT";
          }
        }
        if (idLettre > 0) {
          if (state[idLettre - 1][idCol] == '.') {
            possibilites[1] = "TOP";
          }
        }
        if (idLettre > 0 && idCol < 7) {
          if (state[idLettre - 1][idCol + 1] == '.') {
            possibilites[2] = "RGT";
          }
        }
        break;

      case 'R':
        if (idCol > 1 && idLettre < 6) {
          if (state[idLettre + 1][idCol - 1] == '.') {
            possibilites[0] = "LFT";
          }
        }
        if (idLettre < 6) {
          if (state[idLettre + 1][idCol] == '.') {
            possibilites[1] = "DWN";
          }
        }
        if (idCol < 7) {
          if (state[idLettre][idCol + 1] == '.') {
            possibilites[2] = "RGT";
          }
        }
        break;
    }

    // System.out.println(state[idLettre][idCol] + " " + possibilites[0] +
    // possibilites[1] + possibilites[2]);
    return possibilites;
  }

  /**
   * 
   * Affiche le plateau de jeu dans la configuration portée par
   * 
   * l'attribut d'état "state"
   *
   */
  void affiche() {
      //TESTS A DEGAGER
      //possibleDests('R', 0, 7);
      //System.out.println(deplace('B', "E3", "D4", ModeMvt.REAL));

    // Affichage console Dev
      for(int i = 0; i < state3.length; i++) {
        for(int it = 0; it < state3[i].length; it++){
          if (state3[i][it][0] == -1){
            System.out.print("  "+ConsoleColors.RESET);
      }
          else if ((state3[i][it][0] != -1) && (state[state3[i][it][0]][state3[i][it][1]]=='B')){
            System.out.print(ConsoleColors.BLUE_BACKGROUND+afficheLettre(state3[i][it][2])+state3[i][it][3]+ConsoleColors.RESET);
          }
          else if ((state3[i][it][0] != -1) && (state[state3[i][it][0]][state3[i][it][1]]=='R')){
            System.out.print(ConsoleColors.RED_BACKGROUND+afficheLettre(state3[i][it][2])+state3[i][it][3]+ConsoleColors.RESET);
          }
          else if ((state3[i][it][0] != -1) && (state[state3[i][it][0]][state3[i][it][1]]=='.')){
            System.out.print(ConsoleColors.WHITE_BACKGROUND+afficheLettre(state3[i][it][2])+state3[i][it][3]+ConsoleColors.RESET);
        }
      }
      System.out.println("");
      }
    }

    void afficheDev() {
    // TESTS A DEGAGER
    // possibleDests('R', 0, 7);
    // System.out.println(deplace('B', "E3", "D4", ModeMvt.REAL));

    // Affichage console Dev
    for (int it = 0; it < state.length; it++) {
      int letter = 65;
      if (it > 3) {
        letter += (it - 3);
      }
      for (int e = 1; e < state.length + 1; e++) {
        if (state[it][e] == 'B') {
          System.out.print(ConsoleColors.BLUE + (char) letter + (7 - it) + " ");
          letter += 1;
        } else if (state[it][e] == 'R') {
          System.out.print(ConsoleColors.RED + (char) letter + (7 - it) + " ");
          letter += 1;
        } else if (state[it][e] == '.') {
          System.out.print(ConsoleColors.RESET + (char) letter + (7 - it) + " ");
          letter += 1;
        } else {
          System.out.print(" " + ConsoleColors.RESET + state[it][e] + " ");
        }
      }
      System.out.println("");
      }
    }



    public static char afficheLettre(int ind) {
      switch(ind){
        case 0 :
          return 'A';
        case 1 :
          return 'B';
        case 2 :
          return 'C';
        case 3 :
          return 'D';
        case 4 :
          return 'E';
        case 5 :
          return 'F';
        case 6 :
          return 'G';
        default:
          return 'X';
      }
  }
      

  void affichageGraphique() {
    StdDraw.setXscale(-SCALE, SCALE);
    StdDraw.setYscale(-SCALE, SCALE);
    StdDraw.clear();

    for (int it = 0; it < state.length; it++) {
      int letter = 65;
      double hauteur = 5 - it * 0.85;
      double largeur = 0;
      if (it < 4) {
        largeur = 0 - it * 1.5;
      } else if (it < 5) {
        largeur = -4.5;
        hauteur = 7.52 - it * 1.7;
        letter += (it - 3);
      } else if (it < 6) {
        largeur = -4.5;
        hauteur = 7.52 - it * 1.709;
        letter += (it - 3);
      } else if (it < 7) {
        largeur = -4.5;
        hauteur = 7.505 - it * 1.71;
        letter += (it - 3);
      }
      for (int e = 1; e < state.length + 1; e++) {
        if (state[it][e] != '-') {
          StdDraw.setPenColor(StdDraw.BLACK);
          hexagon(largeur, hauteur, 1);

          if (state[it][e] == 'B') {
            StdDraw.setPenColor(StdDraw.BLUE);
          } else if (state[it][e] == 'R') {
            StdDraw.setPenColor(StdDraw.RED);
          } else {
            StdDraw.setPenColor(StdDraw.WHITE);
          }

          StdDraw.filledCircle(largeur, hauteur, 0.7);
          hauteur -= 0.9;
          largeur += 1.5;
          StdDraw.setPenColor(StdDraw.WHITE);

          if (state[it][e] == '.') {
            StdDraw.setPenColor(StdDraw.BLACK);
          }
          String nomCase = String.valueOf((char) letter) + String.valueOf(7 - it); // char et int a convertir en String
          StdDraw.text(largeur - 1.5, hauteur + 0.85, nomCase);
          letter += 1;
        }
      }
    }
  }

  /**
   * 
   * Dessine un héxagone
   * 
   * @param x    coordonnées X
   * @param y    coordonnées Y
   * @param size taille de l'héxagone
   * 
   */
  void hexagon(double x, double y, double size) {
    double theta = (2 * Math.PI) / 6;
    for (int i = 0; i <= 6; i++) {
      double x1 = x + size * Math.cos(i * theta);
      double y1 = y + size * Math.sin(i * theta);
      double x2 = x + size * Math.cos((i + 1) * theta);
      double y2 = y + size * Math.sin((i + 1) * theta);
      StdDraw.line(x1, y1, x2, y2);
    }
  }

  /**
   * 
   * Joue un tour
   * 
   * @param couleur couleur du pion à jouer
   * 
   * @return tableau contenant la position de départ et la destination du pion à
   *         jouer.
   * 
   */

  String[] jouerIA(char couleur) {
    final int MAXL = 8;
    final int MINL = 1;
    final int MAXH = 7;
    final int MINH = 0;
    String[] tabIa = new String[2];
    String lesPossibles = "";
    int hauteur;
    int largeur;

    do {
      hauteur = (int) (Math.random() * (MAXH - MINH)) + MINH;
      largeur = (int) (Math.random() * (MAXL - MINL)) + MINL;
      lesPossibles = Arrays.toString(possibleDests(couleur, hauteur, largeur));
    } while (lesPossibles.charAt(1) != 'L' &&
        lesPossibles.charAt(6) != 'D' &&
        lesPossibles.charAt(6) != 'T' && lesPossibles.charAt(11) != 'R' ||
        state[hauteur][largeur] != couleur);
    // System.out.println(largeur);
    int returnedHauteur = 7 - hauteur;
    char returnedLargeur = (char) (largeur + 61 + (7 - returnedHauteur));
    tabIa[0] = Character.toString(returnedLargeur) + Integer.toString(returnedHauteur);
    // System.out.println("TOUR DE L IA, couleur : " + couleur);
    // System.out.println(tabIa[0]);
    // System.out.println("couleur " + couleur);
    // System.out.println(Arrays.toString(possibleDests(couleur, hauteur,
    // largeur)));
    final int MAXR = 4;
    final int MINR = 1;
    boolean isValid;

    do {
      isValid = true;
      int choixMv = (int) (Math.random() * (MAXR - MINR)) + MINR;

      switch (couleur) {

        case 'R':
          if (lesPossibles.charAt(1) == 'L' && choixMv == 1) {
            tabIa[1] = Character.toString(returnedLargeur) + Integer.toString(returnedHauteur - 1);
            // System.out.println("OK GOT IT IM : " + couleur + " GOING LEFT");
            break;
          }
          if (lesPossibles.charAt(6) == 'D' && choixMv == 2) {
            returnedLargeur = (char) ((int) returnedLargeur + 1);
            tabIa[1] = Character.toString(returnedLargeur) + Integer.toString(returnedHauteur - 1);
            // System.out.println("OK GOT IT IM : " + couleur + " GOING DOWN");
            break;
          }
          if (lesPossibles.charAt(11) == 'R' && choixMv == 3) {
            returnedLargeur = (char) ((int) returnedLargeur + 1);
            tabIa[1] = Character.toString(returnedLargeur) + Integer.toString(returnedHauteur);
            // System.out.println("OK GOT IT IM : " + couleur + " GOING RIGHT");
            break;
          }
          isValid = false;
          break;

        case 'B':
          if (lesPossibles.charAt(1) == 'L' && choixMv == 1) {
            returnedLargeur = (char) ((int) returnedLargeur - 1);
            tabIa[1] = Character.toString(returnedLargeur) + Integer.toString(returnedHauteur);
            break;
          }
          if (lesPossibles.charAt(6) == 'T' && choixMv == 2) {
            returnedLargeur = (char) ((int) returnedLargeur - 1);
            tabIa[1] = Character.toString(returnedLargeur) + Integer.toString(returnedHauteur + 1);
            break;
          }
          if (lesPossibles.charAt(11) == 'R' && choixMv == 3) {
            tabIa[1] = Character.toString(returnedLargeur) + Integer.toString(returnedHauteur + 1);
            break;
          }
          isValid = false;
          break;
        default:
          String message = "Fonction JouerIA : pas de couleur valide";
          throw new java.lang.UnsupportedOperationException(message);
      }
    } while (isValid == false);

    if ("".equals(tabIa[0]) || "".equals(tabIa[1])) {
      String message = "Fonction JouerIA : tabIa vide";
      throw new java.lang.UnsupportedOperationException(message);
    }
    if (COLLECTING_DATA) {
      storeData(couleur);
    }
    return tabIa;
    // throw new java.lang.UnsupportedOperationException("à compléter");

  }

  /**
   * 
   * gère le jeu en fonction du joueur/couleur
   * 
   * @param couleur
   * 
   * @return tableau de deux chaînes {source,destination} du pion à jouer
   * 
   */

  String[] jouer(char couleur) {

    String src = "";

    String dst = "";

    String[] mvtIa;

    double coordsX;
    double coordsY;

    switch (couleur) {

      case 'B':

        System.out.println("Mouvement " + couleur);

        if (affichageG == 1) {
          do {
            coordsX = StdDraw.mouseX();
            coordsY = StdDraw.mouseY();
            // System.out.println(coordsX + " souris " + coordsY);
          } while (!StdDraw.isMousePressed());
          System.out.println(coordsX + " souris " + coordsY);
          // closestCoords(coordsTab, coordsX, coordsY, couleur);
          // src et dst a dégager
          src = input.next();
          dst = input.next();
        } else {
          src = input.next();
          dst = input.next();
        }

        System.out.println(src + "->" + dst);

        break;

      case 'R':

        System.out.println("Mouvement " + couleur);

        if (gamemode == 1) { // Si JoueurvsJoueur
          if (affichageG == 1) { // Si Affichage graphique activé
            do { // Prendre les coordonnées de la souris
              coordsX = StdDraw.mouseX();
              coordsY = StdDraw.mouseY();
              // System.out.println(coordsX + " souris " + coordsY);
            } while (!StdDraw.isMousePressed());
          } else {
            src = input.next();
            dst = input.next();
          }
        } else {
          mvtIa = jouerIA(couleur);
          src = mvtIa[0];
          dst = mvtIa[1];
        }

        System.out.println(src + "->" + dst);

        break;
    }
    if (COLLECTING_DATA) {
      storeData(couleur);
    }
    return new String[] { src, dst };
  }

  /**
   * 
   * retourne 'R' ou 'B' si vainqueur, 'N' si partie pas finie
   * 
   * @param couleur
   * 
   * @return 'R' ou 'B' si vainqueur, 'N' si partie pas finie
   * 
   */

  char finPartie(char couleur) {
    for (int it = 0; it < state.length; it++) {
      for (int e = 1; e < state.length + 1; e++) {
        if (state[it][e] == couleur) {
          String canMove = Arrays.toString(possibleDests(couleur, it, e));
          // System.out.println("fonction finPartie : " + canMove);
          if (!"[XXX, XXX, XXX]".equals(canMove)) {
            return 'N';
          }
        }
      }
    }
    return couleur;
    // throw new java.lang.UnsupportedOperationException("Fonction finPartie");
  }

  void afficheVainqueur(char couleur) {
    StdDraw.setXscale(-10, 10);
    StdDraw.setYscale(-10, 10);
    StdDraw.filledRectangle(-5, 5, 1, 1);
  }

  void createCoordsTab() {
    StdDraw.setXscale(-10, 10);
    StdDraw.setYscale(-10, 10);
    for (int it = 0; it < state.length; it++) {
      double largeur = -4.5;
      double hauteur = (it < 4) ? 7.7 - it * 1.75 : (it < 5) ? 7.53 - it * 1.7 : 7.565 - it * 1.71;
      // double hauteur = 7.7 - (it * 1.75) + ((it - 4) * 0.15);
      for (int e = 1; e < state.length + 1; e++) {
        if (state[it][e] != '-') {
          coordsTab[it][e][0] = hauteur;
          coordsTab[it][e][1] = largeur;
          // StdDraw.setPenColor(StdDraw.GREEN);
          // StdDraw.filledCircle(largeur, hauteur, 0.7);
        } else {
          // Set coordsTab to an impossible number to make tests later on
          coordsTab[it][e][0] = SCALE + 1;
          coordsTab[it][e][1] = SCALE + 1;
        }
        // System.out.println(coordsTab[it][e][0] + " " + coordsTab[it][e][1]);
        StdDraw.show();
        hauteur -= 0.9;
        largeur += 1.5;
        // StdDraw.text(largeur-1.5, hauteur+0.85, nomCase);
      }
    }
  }

  void closestCoords(double[][][] coordsTab, double x, double y, char couleur) {
    double radius = 1;
    for (int it = 0; it < state.length; it++) {
      for (int e = 1; e < state.length + 1; e++) {
        double x2 = coordsTab[it][e][1];
        double y2 = coordsTab[it][e][0];
        // System.out.println(coordsTab[it][e][1] + " " + coordsTab[it][e][0]);
        double distance = Math.sqrt(Math.pow(x2 - x, 2) + Math.pow(y2 - y, 2));
        if (distance <= radius) {
          // System.out.println("Dans un rayon " + distance);
          // StdDraw.setPenColor(StdDraw.WHITE);
          // StdDraw.filledCircle(coordsTab[it][e][1], coordsTab[it][e][0], 0.8);
          dragToken(it, e, couleur);
          break;
        } // else {
          // System.out.println("Pas dans un rayon " + distance);
          // continue;
          // }
          // StdDraw.setPenColor(StdDraw.YELLOW);
          // StdDraw.filledCircle(coordsTab[it][e][1], coordsTab[it][e][0], 0.7);
          // StdDraw.setPenColor(StdDraw.RED);
          // StdDraw.filledCircle(x, y, 0.7);
        StdDraw.show();
      }
    }
  }

  void dragToken(int it, int e, char couleur) {
    System.out.println("it, e " + it + " " + e);
    System.out.println(state[it][e]);
    if (state[it][e] == couleur) {
      while (StdDraw.isMousePressed()) {
        StdDraw.clear();
        double mouseX = StdDraw.mouseX();
        double mouseY = StdDraw.mouseY();
        affichageGraphique();
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.filledCircle(coordsTab[it][e][1], coordsTab[it][e][0], 0.8);
        if (state[it][e] == 'R') {
          StdDraw.setPenColor(StdDraw.RED);
        } else {
          StdDraw.setPenColor(StdDraw.BLUE);
        }
        StdDraw.filledCircle(mouseX, mouseY, 0.7);
        StdDraw.show();
      }
    }
  }

  int gamemodeSelect() {
    gamemode = -1;
    while (gamemode != 1 && gamemode != 2 && gamemode != 3) {
      System.out.println("Sélectionnez un mode de jeu : \n\t(1) PlayerVSPlayer \n\t(2) PlayerVSAI \n\t(3) AIVSAI");
      gamemode = input.nextInt();
    }
    System.out.println("Mode sélectionné : " + gamemode);
    return gamemode;
  }

  int nbPartiesSelect() {
    int nbParties = 0;
    while (nbParties < 1 || nbParties > 1000) {
      try {
        System.out.println("Entrez le nombre de parties désirés : ");
        nbParties = input.nextInt();
      } catch (InputMismatchException e) {
        System.out.println("Entrée invalide, réessayez");
      }
    }
    System.out.println("Nombre de parties : " + nbParties);
    return nbParties;
  }

  void introStuckWin() {
    int counter = 0;
    while (counter < 40 && !StdDraw.hasNextKeyTyped()) {
      StdDraw.picture(0.5, 0.5, "STUCKWIN_OPEN.gif", 1.5, 1);
      counter++;
    }
    while (!StdDraw.hasNextKeyTyped()) {
      StdDraw.picture(0.5, 0.5, "STUCKWIN_WAIT.gif", 1.5, 1);
    }
    StdDraw.clear();
    while (counter < 85) {
      StdDraw.picture(0.5, 0.5, "STUCKWIN_CLOSE.gif", 1.5, 1);
      counter++;
    }
    StdDraw.clear();
  }

  void storeData(char curPlayer) {
    String result = createStringFromTab(state);
    stateData.add(result);
    stateDataColor.add(curPlayer);
  }

  String createStringFromTab(char tab[][]) {
    String result = "";
    for (int i = 0; i < state.length; i++) {
      for (int j = 0; j < state[i].length; j++) {
        result += state[i][j];
      }
    }
    return result;
  }

  void createAndWriteCSV(char winner) {
    try {

      File csvFile = new File("data.csv");

      FileWriter fw = new FileWriter(csvFile, true);
      BufferedWriter bw = new BufferedWriter(fw);

      // If the file does not exist, create it and write the header row
      if (!csvFile.exists()) {
        bw.write("Board,NextWhoPlays,Winner");
        bw.newLine();
      }
      //stateData.putAll(StateData);
      int i = 0;
      for (String element : stateData) {
        bw.write(element + "," + stateDataColor.get(i) + "," + winner);
        bw.newLine();
        i++;
      }
      stateData.clear();
      stateDataColor.clear();
      bw.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  void dataPossibilities(char color){
    char[][] virtualState = state;
    String resultat = "";
    for (int i = 0; i < state.length; i++) {
      for (int j = 0; j < state[i].length; j++) {
        String[] mouv = possibleDests(color, i, j);

        switch (color){
          case 'B':
            if (!mouv[0].equals("XXX")) {
              //deplace un pion sur le plateau virtuel
              virtualState[i][j] = '.'; 
              virtualState[i][j-1] = 'B';
              resultat = createStringFromTab(virtualState);
              virtualStates.add(resultat);
              virtualState = state;
            }
            if (!mouv[1].equals("XXX")) {
              virtualState[i][j] = '.'; 
              virtualState[i+1][j-1] = 'B';
              resultat = createStringFromTab(virtualState);
              virtualStates.add(resultat);
              virtualState = state;
            }
            if (!mouv[2].equals("XXX")) {
              virtualState[i][j] = '.'; 
              virtualState[i+1][j] = 'B';
              resultat = createStringFromTab(virtualState);
              virtualStates.add(resultat);
              virtualState = state;
            }
            break;

          case 'R':
            if (!mouv[0].equals("XXX")) {
              virtualState[i][j] = '.'; 
              virtualState[i-1][j] = 'R';
              resultat = createStringFromTab(virtualState);
              virtualStates.add(resultat);
              virtualState = state;
            }
            if (!mouv[1].equals("XXX")) {
              virtualState[i][j] = '.'; 
              virtualState[i-1][j+1] = 'R';
              resultat = createStringFromTab(virtualState);
              virtualStates.add(resultat);
              virtualState = state;
            }
            if (!mouv[2].equals("XXX")) {
              virtualState[i][j] = '.'; 
              virtualState[i][j+1] = 'R';
              resultat = createStringFromTab(virtualState);
              virtualStates.add(resultat);
              virtualState = state;
            }
            break;
        }
      }
    }
  }

  void jouerStupidTurtle(char color){
    dataPossibilities(color);
    for (String element : virtualStates) {
      continue;
    }
  }

  void createHashmap(char color){
    Map<String, Integer> stringCounts = new HashMap<>();
    try {
      BufferedReader reader = new BufferedReader(new FileReader("hashmap.csv"));
    
      String line;
      while ((line = reader.readLine()) != null) {
        String[] values = line.split(",");
        // Do something with the values
      }
    
      reader.close();
    } catch (FileNotFoundException e) {
      // Handle the FileNotFoundException here
      System.err.println("Could not find file: " + e.getMessage());
    } catch (IOException e) {
      // Handle the IOException here
      System.err.println("Error reading file: " + e.getMessage());
    }
    
  }

  public static void main(String[] args) {
    StuckWin jeuInit = new StuckWin();
    jeuInit.createHashmap('R');
    int victoiresBleu = 0;
    int victoiresRouge = 0;
    int nombreDeParties = 1;

    while (StuckWin.affichageG != 1 && StuckWin.affichageG != 2) {
      System.out.println("Voulez-vous afficher les graphiques ? (1) oui (2) non");
      StuckWin.affichageG = input.nextInt();
    }

    if (StuckWin.affichageG == 1) {
      StdDraw.setXscale(-10, 10);
      StdDraw.setYscale(-10, 10);
      StdDraw.setCanvasSize(1080, 1080);

      jeuInit.introStuckWin();
      StdDraw.enableDoubleBuffering();
    }

    // StuckWin.createCoordsTab();
    int gamemode = jeuInit.gamemodeSelect();
    nombreDeParties = jeuInit.nbPartiesSelect();

    for (int i = 0; i < nombreDeParties; i++) {

      StuckWin jeu = new StuckWin();
      if (StuckWin.affichageG == 1) {
        jeu.createCoordsTab();
      }

      String src = "";

      String dest;

      String[] reponse;

      Result status;

      char partie = 'N';

      char curCouleur = jeu.joueurs[0];

      char nextCouleur = jeu.joueurs[1];

      char tmp;

      int cpt = 0;

      // version console

      do {

        // séquence pour Bleu ou rouge
        jeu.affiche();
        if (StuckWin.affichageG == 1) {
          jeu.affichageGraphique();
          StdDraw.show();
        }

        do {

          status = Result.EXIT;
          if (gamemode == 3) {
            reponse = jeu.jouerIA(curCouleur);
          } else {
            reponse = jeu.jouer(curCouleur);
          }
          // reponse = jeu.jouerIA(curCouleur);

          // reponse = jeu.jouer(curCouleur);

          src = reponse[0];

          dest = reponse[1];

          if ("q".equals(src))

            return;

          status = jeu.deplace(curCouleur, src, dest, ModeMvt.REAL);

          partie = jeu.finPartie(nextCouleur);

          System.out.println("status : " + status + " partie : " + partie);

        } while (status != Result.OK && partie == 'N');

        tmp = curCouleur;

        curCouleur = nextCouleur;

        nextCouleur = tmp;

        cpt++;

      } while (partie == 'N'); // TODO affiche vainqueur
      if (COLLECTING_DATA) {
        jeu.createAndWriteCSV(partie);
      }

      System.out.printf("Victoire : " + partie + " (" + (cpt / 2) + " coups)");
      System.out.println(" ");
      // jeu.afficheVainqueur(partie);
      if (partie == 'R') {
        victoiresRouge++;
      } else {
        victoiresBleu++;
      }
      // jeu.createCoordsTab();
    }
    if (affichageG == 1) {
      StdDraw.show();
    }
    System.out.println("Victoires Bleu :" + victoiresBleu + " Victoires Rouge :" + victoiresRouge);
    // System.out.println("Nombre de déplacements gauche des vainqueurs vs des
    // perdants");
  }
}
