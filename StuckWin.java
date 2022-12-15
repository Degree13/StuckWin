import java.nio.channels.NonReadableChannelException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import java.util.Scanner;
import java.util.logging.Logger;
import java.util.InputMismatchException;
import java.util.HashMap;
import java.util.Map;
import java.util.PrimitiveIterator;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.FontFormatException;

import java.lang.ref.Cleaner.Cleanable;

import javax.print.FlavorException;
import javax.swing.plaf.basic.BasicBorders.RadioButtonBorder;
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

  public static int gamemode = 0;
  public static boolean COLLECTING_DATA = false;
  public static int affichageG = -1;

  private static final Logger LOGGER = Logger.getLogger(StuckWin.class.getName());

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
  double[][][] coordsTab = new double[7][8][2];

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

    public void printMessage(String message, boolean ligne) {
      if (ligne) {
          System.out.println(message);
      } else {
          System.out.print(message);
      }
      //LOGGER.info(message);
    }

    /**

     * Déplace un pion ou simule son déplacement

     * @param couleur couleur du pion à déplacer

     * @param lcSource case source Lc

     * @param lcDest case destination Lc

     * @param mode ModeMVT.REAL/SIMU selon qu'on réalise effectivement 
     * le déplacement ou qu'on le simule seulement.

     * @return enum {OK, BAD_COLOR, DEST_NOT_FREE, EMPTY_SRC, TOO_FAR, 
     * EXT_BOARD, EXIT} selon le déplacement

     */
  Result deplace(char couleur, String lcSource, String lcDest, ModeMvt mode) {
    if (lcSource.length() != 2 || lcDest.length() != 2) {
      return Result.EXIT;
    }

    int idLineSource = 55 - lcSource.charAt(1);
    int idLineDest = 55 - lcDest.charAt(1);

    int idColSource = (lcSource.charAt(1) - 48) + (lcSource.charAt(0) - 68);
    int idColDest = (lcDest.charAt(1) - 48) + (lcDest.charAt(0) - 68);

    if (idLineSource < 0 || idLineSource > 6 || idLineDest < 0 || idLineDest > 6 
    || idColSource < 1 || idColSource > 7 || idColDest < 1 || idColDest > 7) {
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
    String[] holdString = possibleDests(couleur, idLineSource, idColSource);
    String isTooFar = Arrays.toString(holdString);
    boolean boolTooFar = true;
    switch (couleur) {
      case 'B':
        if (isTooFar.charAt(1) == 'L' 
        && idColDest + 1 == idColSource 
        && idLineDest == idLineSource) {
          boolTooFar = false;
        } else if (isTooFar.charAt(6) == 'T' 
        && idLineDest + 1 == idLineSource 
        && idColDest == idColSource) {
          boolTooFar = false;
        } else if (isTooFar.charAt(11) == 'R' 
        && idLineDest + 1 == idLineSource 
        && idColDest - 1 == idColSource) {
          boolTooFar = false;
        }
        if (boolTooFar == true) {
          return Result.TOO_FAR;
        }
        break;
      case 'R':
        if (isTooFar.charAt(1) == 'L' 
        && (idColDest + 1 == idColSource 
        && idLineDest - 1 == idLineSource)) {
          boolTooFar = false;
        } else if (isTooFar.charAt(6) == 'D' 
        && idLineDest - 1 == idLineSource 
        && idColDest == idColSource) {
          boolTooFar = false;
        } else if (isTooFar.charAt(11) == 'R' 
        && idLineDest == idLineSource 
        && idColDest - 1 == idColSource) {
          boolTooFar = false;
        }
        if (boolTooFar) {
          return Result.TOO_FAR;
        }
        break;
      default:
        String message = "Fonction deplace : Couleur incompatible";
        throw new java.lang.UnsupportedOperationException(message);
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
      String throwMsg = "Erreur, les valeurs entrées en paramètres" 
      + "de la fonction possibleDests ne correspondent à aucune case";
      throw new java.lang.UnsupportedOperationException(throwMsg);
    }

    String[] possibilites = new String[3];
    Arrays.fill(possibilites, "XXX");

    switch (couleur) {
      case 'B':
        if ((idCol > 1) && (state[idLettre][idCol - 1] == '.')) {
          possibilites[0] = "LFT";
        }
        
        if ((idLettre > 0) && (state[idLettre - 1][idCol] == '.')) {
          possibilites[1] = "TOP";
        }
        if ((idLettre >0 && idCol <7)&&(state[idLettre-1][idCol+1] == '.')){
          possibilites[2] = "RGT";
        }
        break;

      case 'R':
        if ((idCol >1 && idLettre <6)&&(state[idLettre+1][idCol-1] == '.')){
          possibilites[0] = "LFT";
        }
        if ((idLettre < 6) && (state[idLettre + 1][idCol] == '.')) {
          possibilites[1] = "DWN";
        }
        if ((idCol < 7) && (state[idLettre][idCol + 1] == '.')) {
          possibilites[2] = "RGT";
        }
        break;

        default:
        String message = "Fonction PossibleDests : pas de couleur valide";
        throw new java.lang.UnsupportedOperationException(message);
          
    }
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
    // Affichage console
    String toPrint = "";
    for(int i = 0; i < state3.length; i++) {
      for(int it = 0; it < state3[i].length; it++){
        if (state3[i][it][0] == -1){
          toPrint = "  "+ConsoleColors.RESET;
          printMessage(toPrint, false);
        }
        else if ((state3[i][it][0] != -1) 
        && (state[state3[i][it][0]][state3[i][it][1]]=='B')){
          toPrint = (ConsoleColors.BLUE_BACKGROUND
          +afficheLettre(state3[i][it][2])
          +state3[i][it][3]+ConsoleColors.RESET);
          printMessage(toPrint, false);
        }
        else if ((state3[i][it][0] != -1) 
        && (state[state3[i][it][0]][state3[i][it][1]]=='R')){
          toPrint = (ConsoleColors.RED_BACKGROUND
          +afficheLettre(state3[i][it][2])
          +state3[i][it][3]+ConsoleColors.RESET);
          printMessage(toPrint, false);
        }
        else if ((state3[i][it][0] != -1) 
        && (state[state3[i][it][0]][state3[i][it][1]]=='.')){
          toPrint = (ConsoleColors.WHITE_BACKGROUND
          +afficheLettre(state3[i][it][2])+state3[i][it][3]
          +ConsoleColors.RESET);
          printMessage(toPrint, false);
        }
      }
    toPrint = "";
    printMessage(toPrint, true);
    }
  }

    /**
     * 
     * Affiche le plateau de jeu dans la configuration portée par
     * l'attribut d'état "state" en mode développement
     * Cet affichage est seulement utile en développement et ne dois pas être utilisé dans la version de production
     * 
     */
    void afficheDev() {
    // Affichage console Dev
    String toPrint = "";
    for (int it = 0; it < state.length; it++) {
      int letter = 65;
      if (it > 3) {
        letter += (it - 3);
      }
      for (int e = 1; e < state.length + 1; e++) {
        if (state[it][e] == 'B') {
          toPrint = (ConsoleColors.BLUE + (char) letter + (7 - it) + " ");
          printMessage(toPrint, false);
          letter += 1;
        } else if (state[it][e] == 'R') {
          toPrint = (ConsoleColors.RED + (char) letter + (7 - it) + " ");
          printMessage(toPrint, false);
          letter += 1;
        } else if (state[it][e] == '.') {
          toPrint = (ConsoleColors.RESET + (char) letter + (7 - it) + " ");
          printMessage(toPrint, false);
          letter += 1;
        } else {
          toPrint = (" " + ConsoleColors.RESET + state[it][e] + " ");
          printMessage(toPrint, false);
        }
      }
      toPrint = "";
      printMessage(toPrint, true);
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
    StdDraw.setFont(new Font("", Font.PLAIN, 16));
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
          String nomCase = String.valueOf((char) letter) + String.valueOf(7 - it); 
          // char et int a convertis en String
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
    int returnedHauteur = 7 - hauteur;
    char returnedLargeur = (char) (largeur + 61 + (7 - returnedHauteur));
    tabIa[0] = Character.toString(returnedLargeur) 
    + Integer.toString(returnedHauteur);
    final int MAXR = 4;
    final int MINR = 1;
    boolean isValid;

    do {
      isValid = true;
      int choixMv = (int) (Math.random() * (MAXR - MINR)) + MINR;

      switch (couleur) {

        case 'R':
          if (lesPossibles.charAt(1) == 'L' && choixMv == 1) {
            tabIa[1] = Character.toString(returnedLargeur) 
            + Integer.toString(returnedHauteur - 1);
            break;
          }
          if (lesPossibles.charAt(6) == 'D' && choixMv == 2) {
            returnedLargeur = (char) ((int) returnedLargeur + 1);
            tabIa[1] = Character.toString(returnedLargeur) 
            + Integer.toString(returnedHauteur - 1);
            break;
          }
          if (lesPossibles.charAt(11) == 'R' && choixMv == 3) {
            returnedLargeur = (char) ((int) returnedLargeur + 1);
            tabIa[1] = Character.toString(returnedLargeur) 
            + Integer.toString(returnedHauteur);
            break;
          }
          isValid = false;
          break;

        case 'B':
          if (lesPossibles.charAt(1) == 'L' && choixMv == 1) {
            returnedLargeur = (char) ((int) returnedLargeur - 1);
            tabIa[1] = Character.toString(returnedLargeur) 
            + Integer.toString(returnedHauteur);
            break;
          }
          if (lesPossibles.charAt(6) == 'T' && choixMv == 2) {
            returnedLargeur = (char) ((int) returnedLargeur - 1);
            tabIa[1] = Character.toString(returnedLargeur) 
            + Integer.toString(returnedHauteur + 1);
            break;
          }
          if (lesPossibles.charAt(11) == 'R' && choixMv == 3) {
            tabIa[1] = Character.toString(returnedLargeur) 
            + Integer.toString(returnedHauteur + 1);
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
          } while (!StdDraw.isMousePressed());
          System.out.println(coordsX + " souris " + coordsY);
          String[] mouseInput = new String[2];
          mouseInput = closestCoords(coordsTab, coordsX, coordsY, couleur);
          src = mouseInput[0];
          dst = mouseInput[1];
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
            } while (!StdDraw.isMousePressed());
            System.out.println(coordsX + " souris " + coordsY);
            String[] mouseInput = new String[2];
            mouseInput = closestCoords(coordsTab, coordsX, coordsY, couleur);
            src = mouseInput[0];
            dst = mouseInput[1];
          } else {
            src = input.next();
            dst = input.next();
          }
        } else {
          mvtIa = jouerIA(couleur);
          src = mvtIa[0];
          dst = mvtIa[1];
        }
        String message = src + "->" + dst;
        System.out.println(message);

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
          if (!"[XXX, XXX, XXX]".equals(canMove)) {
            return 'N';
          }
        }
      }
    }
    return couleur;
  }

  /**
   * 
   * Créer le tableau des coordonnées des points StdDraw
   * 
   */
  void createCoordsTab() {
    StdDraw.setXscale(-10, 10);
    StdDraw.setYscale(-10, 10);
    for (int it = 0; it < state.length; it++) {
      double largeur = -4.5;
      double hauteur = (it < 4) 
      ? 7.7 - it * 1.75 : (it < 5) 
      ? 7.53 - it * 1.7 : 7.565 - it * 1.71;
      for (int e = 1; e < state.length + 1; e++) {
        if (state[it][e] != '-') {
          coordsTab[it][e][0] = hauteur;
          coordsTab[it][e][1] = largeur;
        } else {
          // Set coordsTab to an impossible number to make tests later on
          coordsTab[it][e][0] = SCALE + 1;
          coordsTab[it][e][1] = SCALE + 1;
        }
        StdDraw.show();
        hauteur -= 0.9;
        largeur += 1.5;
      }
    }
  }

  /**
   * 
   * Cherche la coordonnées la plus proche du clic
   * 
   * @param coordsTab le tableau des coordonnées
   * @param x coordonnées x de la souris
   * @param y coordonnées x de la souris
   * @param couleur couleur du pion à rechercher
   * 
   * @return tableau contenant la position de départ et
   *  la destination du pion à jouer.
   * 
   */
  String[] closestCoords(double[][][] coordsTab,double x,double y,char couleur){
    double radius = 1;
    String source = "";
    String dest = "";
    String[] returned = new String[2]; 
    for (int it = 0; it < coordsTab.length; it++) {
      int letter = 65;
      letter += (it-3);
      for (int e = 1; e <= coordsTab.length; e++) {
        double x2 = coordsTab[it][e][1];
        double y2 = coordsTab[it][e][0];
        double distance = Math.sqrt(Math.pow(x2 - x, 2) + Math.pow(y2 - y, 2));
        if (distance <= radius) {
          if (state[it][e] == 'R' || state[it][e] == 'B'){
            source = (char)letter + "" + (7-it);
            dest = dragToken(it, e, couleur, radius);
          }
          break;
        }
        letter++;
        StdDraw.show();
      }
    }
    returned[0] = source;
    returned[1] = dest;
    return returned;
  }

  /**
   * 
   * Déplace le pion virtuellement et réellement
   * 
   * @param it coordonnées 1 du tableau
   * @param e coordonnées 2 du tableau
   * @param couleur couleur du pion à rechercher
   * @param radius rayon d'interaction des pions
   * 
   * @return String contenant la destination du pion à jouer.
   * 
   */
  String dragToken(int it, int e, char couleur, double radius) {
    double mouseX = StdDraw.mouseX();
    double mouseY = StdDraw.mouseY();
    String returned = "";

    if (state[it][e] != couleur) {
      System.out.println("Warning : Wrong color");
    }
    while (StdDraw.isMousePressed()) {
      StdDraw.clear();
      mouseX = StdDraw.mouseX();
      mouseY = StdDraw.mouseY();
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

    for (int it2 = 0; it2 < coordsTab.length; it2++) {
      int letter = 65+it2-3;
      for (int e2 = 1; e2 < coordsTab.length+1; e2++) {
        double x2 = coordsTab[it2][e2][1];
        double y2 = coordsTab[it2][e2][0];
        double distance = Math.sqrt(Math.pow(x2 - mouseX, 2) 
        + Math.pow(y2 - mouseY, 2));

        if (distance <= radius) {
          System.out.println("I found the token to be " + it2 + " " + e2);
          System.out.println("Representing " + (char)letter + " " + (7-it2));
          returned = (char)letter + "" + (7-it2);
          break;
        }
        letter++;
        StdDraw.show();
      }
    }
    return returned;
  }

  /**
   * 
   * Permet la sélection du mode de jeu
   * 
   * @return Entier (1,2 ou 3) correspondant à l'input de l'utilisateur
   * 
   */
  int gamemodeSelect() {
    gamemode = -1;
    while (gamemode != 1 && gamemode != 2 && gamemode != 3) {
      System.out.println("Sélectionnez un mode de jeu : \n"
      + "\t(1) PlayerVSPlayer \n"
      + "\t(2) PlayerVSAI \n"
      + "\t(3) AIVSAI");
      try {
        gamemode = input.nextInt();
      } catch (InputMismatchException e) {
        System.out.println("Entrée invalide, réessayez");
        input.nextLine();
      }
    }
    System.out.println("Mode sélectionné : " + gamemode);
    return gamemode;
  }

  /**
   * 
   * Permet la sélection du nombre de parties souhaitées
   * 
   * @return Entier correspondant à l'input de l'utilisateur
   * 
   */
  int nbPartiesSelect() {
    int nbParties = 0;
    while (nbParties < 1) {
      System.out.println("Entrez le nombre de parties désirés : ");
      try {
        nbParties = input.nextInt();
      } catch (InputMismatchException e) {
        System.out.println("Entrée invalide, réessayez");
        input.nextLine();
      }
    }
    System.out.println("Nombre de parties : " + nbParties);
    return nbParties;
  }

  /**
   * 
   * Affiche l'animation d'entrée StdDraw, permet aussi de la skip
   * 
   */
  void introStuckWin() {
    int counter = 0;
    while (counter < 40 && !StdDraw.hasNextKeyTyped() 
    && !StdDraw.isMousePressed()) {
      StdDraw.picture(0.5, 0.5, "STUCKWIN_OPEN.gif", 1.5, 1);
      counter++;
    }
    while (!StdDraw.hasNextKeyTyped() && !StdDraw.isMousePressed()) {
      StdDraw.picture(0.5, 0.5, "STUCKWIN_WAIT.gif", 1.5, 1);
    }

    //Flush nextKeyTyped
    while (StdDraw.hasNextKeyTyped()) {
      StdDraw.nextKeyTyped();
    }

    while (counter < 85 && !StdDraw.hasNextKeyTyped() 
    && !StdDraw.isMousePressed()) {
      StdDraw.picture(0.5, 0.5, "STUCKWIN_CLOSE.gif", 1.5, 1);
      counter++;
    }
    StdDraw.clear();
  }

  void checkForFont(){
    // check if the OCRAEXT.TTF font is installed on the user's computer
    String fontName = "OCR A Extended";
    Font font = new Font(fontName, Font.PLAIN, 12);
    if (font.getFamily().equalsIgnoreCase(fontName)) {
        System.out.println(fontName + " is installed on the user's computer");
    } else {
        System.out.println(fontName + " is not installed on the user's computer");
        System.out.println(fontName + "...trying to install Font...");
        File fontFile = new File("OCRAEXT.TTF");
        try {
          Font fontInstall = Font.createFont(Font.TRUETYPE_FONT, fontFile);
          GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fontInstall);
      
          // use the font in your program
            fontInstall = new Font(fontName, Font.PLAIN, 26);
        } catch (FontFormatException e) {
            // handle the FontFormatException here
        } catch (IOException e) {
            // handle the IOException here
        }
        if (font.getFamily().equalsIgnoreCase(fontName)) {
          String holdSting = fontName + " couln't be installed on user's comptuer, " 
          + "please install OCRAEXT.TTF using the provided font file in the game files";
          printMessage(holdSting, true);
      }
    }
  }
  /**
   * 
   * Permet l'affichage du gagnant, et de son nombre de coups
   * 
   */
  void drawWinningScreen(char color, int coups, int victoiresBleu, int victoiresRouge) {
    StdDraw.setFont(new Font("OCR A Extended", Font.PLAIN, 32));
    double wdh = 7;
    double hgt = 2;
    StdDraw.setPenColor(StdDraw.DARK_GRAY);
    StdDraw.filledRectangle(0, 0, wdh+0.2, hgt+0.2);
    StdDraw.setPenColor(StdDraw.WHITE);
    StdDraw.filledRectangle(0, 0, wdh, hgt);

    double hgtTxt = 0.4;
    if (color == 'R') {
      StdDraw.setPenColor();
      StdDraw.setPenColor(StdDraw.RED);
      StdDraw.text(0, hgtTxt, "Le joueur Rouge remporte cette manche!");
    } else if (color == 'B') {
      StdDraw.setPenColor(StdDraw.BLUE);
      StdDraw.text(0, hgtTxt, "Le joueur Bleu remporte cette manche!");
    }

    StdDraw.setFont(new Font("OCR A Extended", Font.PLAIN, 26));
    StdDraw.setPenColor(StdDraw.DARK_GRAY);
    StdDraw.text(0, -hgtTxt, "Coups joués : " + coups);

    StdDraw.text(-0.35, -hgtTxt-0.5, "Score(s) :  |" );
    StdDraw.setPenColor(StdDraw.BLUE);
    StdDraw.text(1, -hgtTxt-0.5, ""+victoiresBleu);
    StdDraw.setPenColor(StdDraw.RED);
    StdDraw.text(1.75, -hgtTxt-0.5, ""+victoiresRouge);
  }

  /**
   * 
   * Permet de stocker les données du jeu à un instant donnée
   * 
   */
  void storeData(char curPlayer) {
    String result = createStringFromTab(state);
    stateData.add(result);
    stateDataColor.add(curPlayer);
  }

  /**
   * 
   * Transforme le tableau donnée en String
   * 
   * @return renvoie le String
   * 
   */
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
    jeuInit.checkForFont();
    int victoiresBleu = 0;
    int victoiresRouge = 0;
    int nombreDeParties = 1;

    while (StuckWin.affichageG != 1 && StuckWin.affichageG != 2) {
      System.out.println("Voulez-vous afficher les graphiques ? (1) oui (2) non");
      try {
        StuckWin.affichageG = input.nextInt();
      } catch(InputMismatchException e){
        System.out.println("Entrée invalide, réessayez");
        input.nextLine();
      }
    }

    if (StuckWin.affichageG == 1) {
      StdDraw.setXscale(-10, 10);
      StdDraw.setYscale(-10, 10);
      StdDraw.setCanvasSize(1080, 1080);

      jeuInit.introStuckWin();
      StdDraw.enableDoubleBuffering();
    }

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

          src = reponse[0];

          dest = reponse[1];

          if ("q".equals(src))

            return;

          status = jeu.deplace(curCouleur, src, dest, ModeMvt.REAL);

          if (status != Result.OK && affichageG == 1) {
            StdDraw.clear();
            jeu.affichageGraphique();
            StdDraw.show();
          }

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

      System.out.printf("Victoire : " +partie+ " (" + (cpt / 2) + " coups)");
      System.out.println(" ");
      if (partie == 'R') {
        victoiresRouge++;
      } else {
        victoiresBleu++;
      }
      if (affichageG ==1) {
        jeu.drawWinningScreen(partie, cpt/2, victoiresBleu, victoiresRouge);
      }
    }
    if (affichageG == 1) {
      StdDraw.show();
    }
    System.out.println("Victoires Bleu :" + victoiresBleu 
    + " Victoires Rouge :" + victoiresRouge);
    // System.out.println("Nombre de déplacements gauche des vainqueurs vs 
    //des perdants");
  }
}
