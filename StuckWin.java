import java.nio.channels.NonReadableChannelException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import java.util.Scanner;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.InputMismatchException;
import java.util.PrimitiveIterator;
import java.util.HashMap;
import java.util.Map;

import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;

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

  private static final int BOARD_SIZE = 7;

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

  public static int gamemode;
  public static boolean COLLECTING_DATA = false;
  public static int niWkcutS = -1;
  public static int affichageG;
  public static int numberAI = 1;

  public static final String ENTRY_ERROR = "Entrée invalide, réessayez";
  public static String globalDeplace = "";

  ArrayList<String> storedData = new ArrayList<>(10);
  ArrayList<Character> storedDataColor = new ArrayList<>(10);

  public static HashMap<String, Data> dataMapRed = new HashMap<>();
  public static HashMap<String, Data> dataMapBlue = new HashMap<>();

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
        //le logger n'est pas en UTF-8, et rend la lecture du terminal difficile
        //toutefois vous pouvez l'utiliser : LOGGER.info(message);
        System.out.println(message);
      } else {
        System.out.print(message);
      }
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
    // On vérifie que les chaînes de caractères ont bien une longueur de 2.
    if (lcSource.length() != 2 || lcDest.length() != 2) {
      // Si ce n'est pas le cas, on retourne un résultat une erreur de format.
      return Result.EXIT;
    }

    // On calcule les indices de ligne et de colonne correspondant aux positions
    // de départ et de destination sur le plateau de jeu.
    int idLineSource = 55 - lcSource.charAt(1);
    int idLineDest = 55 - lcDest.charAt(1);

    int idColSource = (lcSource.charAt(1)-48) + (lcSource.charAt(0)-68);
    int idColDest = (lcDest.charAt(1)-48) + (lcDest.charAt(0)-68);

    // Si les indices dépassent les limites du plateau (0 à 6 pour les lignes
    // et 1 à 7 pour les colonnes)
    if (idLineSource < 0 || idLineSource > 6 || idLineDest < 0 || idLineDest > 6 
    || idColSource < 1 || idColSource > 7 || idColDest < 1 || idColDest > 7) {
      // on retourne un résultat indiquant
      // que les positions sont en dehors du plateau.
      return Result.EXT_BOARD;
    }

    // On vérifie que la case de départ ne soit pas vide, que le pion
    // à déplacer soit de la couleur indiquée et que la destination soit libre
    if (state[idLineSource][idColSource] == '.') {
      return Result.EMPTY_SRC;
    }
    if (state[idLineSource][idColSource] != couleur) {
      return Result.BAD_COLOR;
    }
    if (state[idLineDest][idColDest] != '.') {
      return Result.DEST_NOT_FREE;
    }

    //On vérifie que la destination est bien parmi les possiblités
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
        // Sinon on envoie une erreur
        String message = "Fonction deplace : Couleur incompatible";
        throw new java.lang.UnsupportedOperationException(message);
    }

    // Si on arrive là c'est que tout est bon, on déplace le pion
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
   * @return tableau des trois positions jouables par le pion 
   * 
   */

  String[] possibleDests(char couleur, int idLettre, int idCol) {
    // On vérifie que idLettre et idCol ne sont pas trop grand ou trop petits
    if (idLettre > 6 || idLettre < 0 || idCol > 7 || idCol < 1) {
      String throwMsg = "Erreur, les valeurs entrées en paramètres" 
      + "de la fonction possibleDests ne correspondent à aucune case";
      throw new java.lang.UnsupportedOperationException(throwMsg);
    }

    // On crée un tableau de String
    String[] possibilites = new String[3];
    Arrays.fill(possibilites, "XXX");

    // On remplis le tableau de String reprséntant les différentes possibilités
    switch (couleur) {
      case 'B':
        if ((idCol > 1) && (state[idLettre][idCol - 1] == '.')) {
          possibilites[0] = "LFT";
        }
        
        if ((idLettre > 0) && (state[idLettre - 1][idCol] == '.')) {
          possibilites[1] = "TOP";
        }
        if ((idLettre >0 && idCol <7) && (state[idLettre-1][idCol+1] == '.')){
          possibilites[2] = "RGT";
        }
        break;

      case 'R':
        if ((idCol > 1 && idLettre < 6) && (state[idLettre+1][idCol-1] == '.')){
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
        // Ceci n'arrivera jamais ok
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

    // On parcours le tableau state3 et on print les cases dans la console
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
    // Reset la couleur et sauter une ligne
    toPrint = ""+ConsoleColors.RESET;
    printMessage(toPrint, true);
    }
  toPrint = ""+ConsoleColors.RESET;
  printMessage(toPrint, true);
  }

    /**
     * 
     * Affiche le plateau de jeu dans la configuration portée par
     * l'attribut d'état "state" en mode développement
     * Cet affichage est seulement utile en développement et ne dois pas être 
     * utilisé dans la version de production
     * 
     */
    void afficheDev() {
    // Affichage console Dev
    String toPrint = "";

    // On parcours le tableau state et on print les cases
    for (int it = 0; it < BOARD_SIZE; it++) {
      int letter = 65;
      if (it > 3) {
        letter += (it - 3);
      }
      for (int e = 1; e < SIZE ; e++) {
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
      // Sauter une ligne
      toPrint = "";
      printMessage(toPrint, true);
      }
    }

  /**
   * @param ind Entier représentant une lettre
   *
   * Traduction partiel des INT en lettre
   * 
   * @return une lettre
   * 
   */
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
      
  /**
   *
   * Permet d'afficher les graphiques
   * 
   */
  void affichageGraphique() {
    // Initialisation
    StdDraw.setFont(new Font("", Font.PLAIN, 16));
    StdDraw.setXscale(-SCALE, SCALE);
    StdDraw.setYscale(-SCALE, SCALE);
    StdDraw.clear();

    // Création du tableau de jeu
    for (int it = 0; it < BOARD_SIZE; it++) {
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
      for (int e = 1; e < SIZE; e++) {
        if (state[it][e] != '-') {
          StdDraw.setPenColor(StdDraw.BLACK);
          // Création des hexagones
          hexagon(largeur, hauteur, 1);
          
          // Sélection de la couleur qui va être utilisé
          if (state[it][e] == 'B') {
            StdDraw.setPenColor(StdDraw.BLUE);
          } else if (state[it][e] == 'R') {
            StdDraw.setPenColor(StdDraw.RED);
          } else {
            StdDraw.setPenColor(StdDraw.WHITE);
          }

          // Création des pions
          StdDraw.filledCircle(largeur, hauteur, 0.7);
          hauteur -= 0.9;
          largeur += 1.5;

          // Ecriture des noms des pions (H6 par exemple)
          StdDraw.setPenColor(StdDraw.WHITE);
          if (state[it][e] == '.') {
            StdDraw.setPenColor(StdDraw.BLACK);
          }
          String nomCase = String.valueOf((char)letter) + String.valueOf(7-it);
          // char et int a convertis en String
          StdDraw.text(largeur - 1.5, hauteur + 0.85, nomCase);
          letter += 1;
        }
      }
    }
    StdDraw.setPenColor(StdDraw.BLACK);
    StdDraw.setFont(new Font("OCR A Extended", Font.PLAIN, 26));
    StdDraw.text(0, 7.5, globalDeplace);
  }

  /**
   * 
   * Dessine un héxagone
   * 
   * @param x coordonnées X
   * @param y coordonnées Y
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
    // Initialisation
    final int MAXL = 8;
    final int MINL = 1;
    final int MAXH = 7;
    final int MINH = 0;
    String[] tabIa = new String[2];
    String lesPossibles = "";
    int hauteur;
    int largeur;

    // Pendant que je n'ai pas un pion valide, j'en choisis 1 aléatoirement,
    // Sinon, j'en cherche une autre, et je choisi une possibilité de 
    // déplacement aléatoire

    // Sélection de la case
    do {
      hauteur = (int) (Math.random() * (MAXH - MINH)) + MINH;
      largeur = (int) (Math.random() * (MAXL - MINL)) + MINL;
      lesPossibles = Arrays.toString(possibleDests(couleur, hauteur, largeur));
    } while (lesPossibles.charAt(1) != 'L' 
        && lesPossibles.charAt(6) != 'D' 
        && lesPossibles.charAt(6) != 'T' 
        && lesPossibles.charAt(11) != 'R' 
        || state[hauteur][largeur] != couleur);
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

      // Sélection du déplacement
      switch (couleur) {

        case 'R':
          if (lesPossibles.charAt(1) == 'L' && choixMv == 1) {
            tabIa[1] = Character.toString(returnedLargeur) 
            + Integer.toString(returnedHauteur - 1);
            break;
          }
          if (lesPossibles.charAt(6) == 'D' && choixMv == 2) {
            returnedLargeur = (char) (returnedLargeur + 1);
            tabIa[1] = Character.toString(returnedLargeur) 
            + Integer.toString(returnedHauteur - 1);
            break;
          }
          if (lesPossibles.charAt(11) == 'R' && choixMv == 3) {
            returnedLargeur = (char) (returnedLargeur + 1);
            tabIa[1] = Character.toString(returnedLargeur) 
            + Integer.toString(returnedHauteur);
            break;
          }
          isValid = false;
          break;

        case 'B':
          if (lesPossibles.charAt(1) == 'L' && choixMv == 1) {
            returnedLargeur = (char) (returnedLargeur - 1);
            tabIa[1] = Character.toString(returnedLargeur) 
            + Integer.toString(returnedHauteur);
            break;
          }
          if (lesPossibles.charAt(6) == 'T' && choixMv == 2) {
            returnedLargeur = (char) (returnedLargeur - 1);
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
          // Normalement pas possible d'arriver là
          String message = "Fonction JouerIA : pas de couleur valide";
          throw new java.lang.UnsupportedOperationException(message);
      }
    } while (!isValid);

    if ("".equals(tabIa[0]) || "".equals(tabIa[1])) {
      // pareil
      String message = "Fonction JouerIA : tabIa vide";
      throw new java.lang.UnsupportedOperationException(message);
    }
    if (COLLECTING_DATA) {
      storeData(couleur);
    }
    // Quand tout est bon on revoi le résultat
    return tabIa;
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
  String[] jouerIA_StupidTurtle(char couleur) {
    // Initialisation
    String[] tabIa = new String[2];
    int bestScore = Integer.MIN_VALUE;
    String bestMove = "";
    String actualState = createStringFromTab(state, 'A');

    int sourceTokenNB = -1;
    int sourceTokenCHAR = 65;
    int destTokenNB = -1;
    int destTokenCHAR = 65;

    Data value = null;

    ArrayList<String> lesPossiblesList = new ArrayList<String>();
    lesPossiblesList = dataPossibilities(couleur);

    for (String element : lesPossiblesList) {
      int score = 10;
      
      if (couleur == 'R') {
        value = searchValue("dataMapRed.bin", element);
      } else {
        value = searchValue("dataMapBlue.bin", element);
      }

      if (value != null) {
        if (niWkcutS == 1) {
          if (couleur == 'R') {
            score = score(value.getWCountR(), value.getWCountB());
          } else {
            score = score(value.getWCountB(), value.getWCountR());
          }
        } else if (niWkcutS == 2) {
          // Si le mode niWcuts est sélectionné, alors j'inverse simplement les
          // valeurs envoyées a score() pour favoriser l'adversaire
          if (couleur == 'R') {
            score = score(value.getWCountR(), value.getWCountB());
          } else {
            score = score(value.getWCountB(), value.getWCountR());
          }
        }
      }

      if (score >= bestScore) {
        bestScore = score;
        bestMove = element;
      }
    }


      for (int i = 0; i < bestMove.length(); i++) {
        if (bestMove.charAt(i) != actualState.charAt(i)) {
          if (actualState.charAt(i) == couleur) {
            if (i < 4){
              sourceTokenNB = 7;
              sourceTokenCHAR = sourceTokenCHAR+i; 
            } else if (i < 9) {
              sourceTokenNB = 6;
              sourceTokenCHAR = sourceTokenCHAR+i-4;
            } else if (i < 15) {
              sourceTokenNB = 5;
              sourceTokenCHAR = sourceTokenCHAR+i-9;
            } else if (i < 22) {
              sourceTokenNB = 4;
              sourceTokenCHAR = sourceTokenCHAR+i-15;
            } else if (i < 28) {
              sourceTokenNB = 3;
              sourceTokenCHAR = sourceTokenCHAR+i-21;
            } else if (i < 33) {
              sourceTokenNB = 2;
              sourceTokenCHAR = sourceTokenCHAR+i-26;
            } else if (i < 37) {
              sourceTokenNB = 1;
              sourceTokenCHAR = sourceTokenCHAR+i-30;
            }
          tabIa[0] = (char)sourceTokenCHAR + "" + sourceTokenNB;
          } else if (actualState.charAt(i) == '.') {
            if (i < 4){
              destTokenNB = 7;
              destTokenCHAR = destTokenCHAR+i; 
            } else if (i < 9) {
              destTokenNB = 6;
              destTokenCHAR = destTokenCHAR+i-4;
            } else if (i < 15) {
              destTokenNB = 5;
              destTokenCHAR = destTokenCHAR+i-9;
            } else if (i < 22) {
              destTokenNB = 4;
              destTokenCHAR = destTokenCHAR+i-15;
            } else if (i < 28) {
              destTokenNB = 3;
              destTokenCHAR = destTokenCHAR+i-21;
            } else if (i < 33) {
              destTokenNB = 2;
              destTokenCHAR = destTokenCHAR+i-26;
            } else if (i < 37) {
              destTokenNB = 1;
              destTokenCHAR = destTokenCHAR+i-30;
            }
          tabIa[1] = (char)destTokenCHAR + "" + destTokenNB;
          }
        }
      }

    if ("".equals(tabIa[0]) || "".equals(tabIa[1])) {
      // pareil
      String message = "Fonction JouerIA : tabIa vide";
      throw new java.lang.UnsupportedOperationException(message);
    }
    if (COLLECTING_DATA) {
      storeData(couleur);
    }
    // Quand tout est bon on revoi le résultat
    return tabIa;
  }

  int score(int currentPlayerWins, int enemyWins) {
    int score = 0;
    // Give a higher score for a larger lead
    score += (currentPlayerWins - enemyWins) * 100;
    // Give a higher score for a larger number of total wins
    score += (currentPlayerWins + enemyWins) * 1.5;
    return score;
  }

  /**
   * 
   * Gère le jeu en fonction du joueur/couleur
   * 
   * @param couleur
   * 
   * @return tableau de deux chaînes {source,destination} du pion à jouer
   * 
   */
  String[] jouer(char couleur) {
    // Initialisation
    String src = "";

    String dst = "";

    String[] mvtIa;

    double coordsX;
    double coordsY;

    String message = "";

    // Selon les entrées de l'utilisateur, on gère le tour de chacun
    switch (couleur) {

      case 'B':
        message = "Mouvement " + couleur;
        printMessage(message, true);

        if (affichageG == 1) {
          do {
            coordsX = StdDraw.mouseX();
            coordsY = StdDraw.mouseY();

          } while (!StdDraw.isMousePressed());

          String[] mouseInput = 
          closestCoords(coordsTab, coordsX, coordsY, couleur);

          src = mouseInput[0];
          dst = mouseInput[1];

        } else {
          src = input.next();
          dst = input.next();
        }

        message = src + " -> " + dst;
        globalDeplace = message;

        printMessage(message, true);

        break;

      case 'R':
        message = "Mouvement " + couleur;
        printMessage(message, true);

        if (gamemode == 1) { // Si JoueurvsJoueur
          if (affichageG == 1) { // Si Affichage graphique activé
            do { // Prendre les coordonnées de la souris
              coordsX = StdDraw.mouseX();
              coordsY = StdDraw.mouseY();

            } while (!StdDraw.isMousePressed());

            String[] mouseInput = 
            closestCoords(coordsTab, coordsX, coordsY, couleur);

            src = mouseInput[0];
            dst = mouseInput[1];
          } else {
            src = input.next();
            dst = input.next();
          }
        } else {
          if (numberAI == 2) {
            mvtIa = jouerIA_StupidTurtle(couleur);
          } else {
            mvtIa = jouerIA(couleur);
          }
          src = mvtIa[0];
          dst = mvtIa[1];
        }
        
        message = src + "->" + dst;
        globalDeplace = message;

        printMessage(message, true);

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
    // On parcours le tableau et on test les possibilités
    for (int it = 0; it < BOARD_SIZE; it++) {
      for (int e = 1; e < SIZE; e++) {
        if (state[it][e] == couleur) {
          String canMove = Arrays.toString(possibleDests(couleur, it, e));
          if (!"[XXX, XXX, XXX]".equals(canMove)) {
            // Si on en trouve une c'est qu'on peut encore jouer
            return 'N';
          }
        }
      }
    }
    // Si le mode niWkcutS est choisis, on inverse le gagnant
    if (niWkcutS == 1) {
      return couleur;
    } else {
      if (couleur == 'B') {
        return 'R';
      } else {
        return 'B';
      }
    }
  }

  /**
   * 
   * Créer le tableau des coordonnées des points StdDraw
   * 
   * Il est nécessaire afin de pouvoir déplacer les pions à la souris
   * 
   */
  void createCoordsTab() {
    // Initialisation
    StdDraw.setXscale(-10, 10);
    StdDraw.setYscale(-10, 10);

    // On parcours le tableau state
    for (int it = 0; it < BOARD_SIZE; it++) {
      double largeur = -4.5;
      double hauteur;
      if (it < 4) {
        hauteur = 7.7 - it * 1.75;
      } else if (it < 5) {
        hauteur = 7.53 - it * 1.7;
      } else {
        hauteur = 7.565 - it * 1.71;
      }

      for (int e = 1; e < SIZE; e++) {
        if (state[it][e] != '-') {
          coordsTab[it][e][0] = hauteur;
          coordsTab[it][e][1] = largeur;
        } else {
          // Si ce point n'est pas valide on lui attribut une coordonnées 
          // en dehors du tableau (invalide)
          coordsTab[it][e][0] = SCALE + 1.0;
          coordsTab[it][e][1] = SCALE + 1.0;
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
   * 
   * @param x coordonnées x de la souris
   * 
   * @param y coordonnées x de la souris
   * 
   * @param couleur couleur du pion à rechercher
   * 
   * @return tableau contenant la position de départ et
   *  la destination du pion à jouer.
   * 
   */
  String[] closestCoords(double[][][] coordsTab,double x,double y,char couleur){
    // Initialisation
    double radius = 1;
    String source = "";
    String dest = "";
    String[] returned = new String[2]; 

    // On parcours le tableau coordsTab
    for (int it = 0; it < coordsTab.length; it++) {
      int letter = 65;
      letter += (it-3);

      for (int e = 1; e <= coordsTab.length; e++) {
        double x2 = coordsTab[it][e][1];
        double y2 = coordsTab[it][e][0];
        double distance = Math.sqrt(Math.pow(x2 - x, 2) + Math.pow(y2 - y, 2));

        if (distance <= radius) {
          // Si la click effectué est proche d'une coordonnées
          if (state[it][e] == 'R' || state[it][e] == 'B'){
            // Et qu'il est bleu ou rouge
            source = (char)letter + "" + (7-it);
            // On affiche une animation de déplacement de pion
            dest = dragToken(it, e, couleur, radius);
          }
          break;
        }
        letter++;
        StdDraw.show();
      }
    }

    // On renvoie les coordonnées de source et dest
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
    // Initialisation
    double mouseX = StdDraw.mouseX();
    double mouseY = StdDraw.mouseY();
    String returned = "";

    // Pendant que l'utilisateur n'a pas relaché le pion,
    // On affiche le pion attaché à sa souris
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

    // une fois qu'il à laché le pion, on parcours le tableau coordsTab
    for (int it2 = 0; it2 < coordsTab.length; it2++) {
      int letter = 65+it2-3;

      for (int e2 = 1; e2 < coordsTab.length+1; e2++) {
        double x2 = coordsTab[it2][e2][1];
        double y2 = coordsTab[it2][e2][0];
        double distance = Math.sqrt(Math.pow(x2 - mouseX, 2) 
        + Math.pow(y2 - mouseY, 2));

        // Si à l'endroit où à l'utilisateur à laché le pion
        // correspond a une case
        if (distance <= radius) {
          // On note la case
          returned = (char)letter + "" + (7-it2);
          break;
        }
        letter++;
        StdDraw.show();
      }
    }
    // On renvoie la case
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
    // Initialisation
    String message = "";

    // On demande jusqu'à ce qu'un input valide soit entrée
    while (gamemode < 1 || gamemode > 4) {
      message = "Sélectionnez un mode de jeu : \n"
      + "\t(1) PlayerVSPlayer \n"
      + "\t(2) PlayerVSAI \n"
      + "\t(3) AIVSAI \n"
      + "\t(4) AIVSAI configuration minimum";
      printMessage(message, true);

      try {
        gamemode = input.nextInt();
      } catch (InputMismatchException e) {
        printMessage(ENTRY_ERROR, true);
        input.nextLine();
      }
    }

    // On print est renvoie la sélection
    message = "Mode sélectionné : " + gamemode;
    printMessage(message, true);

    return gamemode;
  }

  /**
   * 
   * Permet la sélection d'un mode de jeu spécial
   * 
   * @return 1 ou 2 selon l'entrée utilisateur
   * 
   */
  int niWkcutSSelect() {
    // Initialisation
    String message = "";
    printMessage(message, true);

    // On demande jusqu'à ce qu'un input valide soit entrée
    while (niWkcutS < 1 || niWkcutS > 2) {
      message = "Mode Spécial ? : \n"
      + "\t(1) StuckWin \n"
      + "\t(2) niWkcutS \n";
      printMessage(message, true);

      try {
        niWkcutS = input.nextInt();
      } catch (InputMismatchException e) {
        printMessage(ENTRY_ERROR, true);
        input.nextLine();
      }
    }

    // On print est renvoie la sélection
    message = "Mode sélectionné : " + niWkcutS;
    printMessage(message, true);

    return niWkcutS;
  }

  /**
   * 
   * Permet la sélection du nombre de parties souhaitées
   * 
   * @return Entier correspondant à l'input de l'utilisateur
   * 
   */
  int nbPartiesSelect() {
    // Initialisation
    int nbParties = 0;
    String message = "";

    // On demande jusqu'à ce qu'un input valide soit entrée
    while (nbParties < 1) {
      message = "Entrez le nombre de parties désirés : ";
      printMessage(message, true);

      try {
        nbParties = input.nextInt();
      } catch (InputMismatchException e) {
        printMessage(ENTRY_ERROR, true);
        input.nextLine();
      }
    }

    // On print est renvoie la sélection
    message = "Nombre de parties : " + nbParties;
    printMessage(message, true);
    return nbParties;
  }

  /**
   * 
   * Permet la sélection du nombre de parties souhaitées
   * 
   * @return Entier correspondant à l'input de l'utilisateur
   * 
   */
  int rejouer() {
    // Initialisation
    int rejouer = 0;
    String message = "";

    // On demande jusqu'à ce qu'un input valide soit entrée
    while (rejouer < 1 || rejouer > 2) {
      message = "Partie fini, encore une petite dernière ? \n"
      + "\t(1) Oui \n"
      + "\t(2) Non";
      printMessage(message, true);

      try {
        rejouer = input.nextInt();
      } catch (InputMismatchException e) {
        printMessage(ENTRY_ERROR, true);
        input.nextLine();
      }
    }

    // On print est renvoie la sélection
    return rejouer;
  }

  /**
   * 
   * Affiche l'animation d'entrée StdDraw, permet aussi de la skip
   * 
   */
  void introStuckWin() {
    // Initialisation
    int counter = 0;

    // On affiche l'animation d'entrée tant qu'elle n'est pas fini et
    // qu'une touche n'a pas été entrée ou que la souris n'a pas été cliqué
    while (counter < 40 && !StdDraw.hasNextKeyTyped() 
    && !StdDraw.isMousePressed()) {
      StdDraw.picture(0.5, 0.5, "STUCKWIN_OPEN.gif", 1.5, 1);
      counter++;
    }
    // Pareil pour l'animation d'attente
    while (!StdDraw.hasNextKeyTyped() && !StdDraw.isMousePressed()) {
      StdDraw.picture(0.5, 0.5, "STUCKWIN_WAIT.gif", 1.5, 1);
    }

    // On réinitialise nextKeyTyped pour après
    while (StdDraw.hasNextKeyTyped()) {
      StdDraw.nextKeyTyped();
    }

    // Une pause pour évité de prendre en compte le clic 2x
    StdDraw.pause(250);

    // Pareil pour l'animation de fermeture
    while (counter < 80 && !StdDraw.hasNextKeyTyped() 
    && !StdDraw.isMousePressed()) {
      StdDraw.picture(0.5, 0.5, "STUCKWIN_CLOSE.gif", 1.5, 1);
      counter++;
    }
    StdDraw.clear();
  }

  /**
   * 
   * Permet de vérifié que la police d'écriture utilisé dans le programme 
   * est utilisable par le programme
   * 
   */
  void checkForFont(){
    // Initialisation
    String fontName = "OCR A Extended";
    String message = "";
    Font font = new Font(fontName, Font.PLAIN, 12);

    // On fait les tests, si c'est pas bon on essaye d'installer
    if (font.getFamily().equalsIgnoreCase(fontName)) {
      message = fontName + " is installed on the user's computer";
      printMessage(message, true);
    } else {
      message = fontName + " not installed on the user's computer";
      printMessage(message, true);

      message = fontName + "...trying to install Font...";
      printMessage(message, true);

      File fontFile = new File("OCRAEXT.TTF");
      try {
        Font fontInstall = Font.createFont(Font.TRUETYPE_FONT, fontFile);
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(fontInstall);
        //+ de 80 chars si on compte les espaces mais bon là...
      
        // use the font in your program
        new Font(fontName, Font.PLAIN, 26);
      } catch (FontFormatException | IOException e) {
          // handle the FontFormatException here
          message = "FontFormatException or IOException";
          printMessage(message, true);
      }
      if (font.getFamily().equalsIgnoreCase(fontName)) {
        String holdString = fontName 
        + " couln't be installed on user's computer, " 
        + "please install OCRAEXT.TTF using the provided "
        + "font file in the game files";
        printMessage(holdString, true);
      } else {
        String holdString = fontName + " installed on user's computer"; 
        printMessage(holdString, true);
      }
    }
  }

  /**
   * 
   * Permet l'affichage du gagnant, et de son nombre de coups
   * 
   */
  void drawWinningScreen(char color, int coups, int victoiresBleu, int victoiresRouge) {
    // Initialisation
    StdDraw.setFont(new Font("OCR A Extended", Font.PLAIN, 32));
    double wdh = 7.5;
    double hgt = 2;
    double hgtTxt = 0.4;

    // Création du rectangle
    StdDraw.setPenColor(StdDraw.DARK_GRAY);
    StdDraw.filledRectangle(0, 0, wdh+0.2, hgt+0.2);
    StdDraw.setPenColor(StdDraw.WHITE);
    StdDraw.filledRectangle(0, 0, wdh, hgt);

    // Ecriture du texte
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
    StdDraw.text(2, -hgtTxt-0.5, ""+victoiresRouge);
  }

  /**
   * 
   * Permet l'affichage et la sélection du mode de jeu
   * 
   */
  void drawGamemodeSelect() {
    // Initialisation
    StdDraw.setXscale(-10, 10);
    StdDraw.setYscale(-10, 10);
    StdDraw.picture(0,0, "STUCKWIN_GAMEMODE.jpg", 20,20);
    StdDraw.show();

    // Pause pour ne pas clic 2x par erreur
    StdDraw.pause(500);

    while (!StdDraw.isMousePressed()){
      //Attendre l'utilisateur
    }
    double mouseX = StdDraw.mouseX();
    double mouseY = StdDraw.mouseY();

    if (mouseY >= -1.68){
      if (mouseX < 0) {
        gamemode = 1;
      } else {
        gamemode = 2;
      }
    } else {
      if (mouseX <= 0) {
        gamemode = 3;
      } else {
        gamemode = 4;
      }
    }
  }

  /**
   * 
   * Permet l'affichage et la sélection du nombre de partie(s) voulu
   * 
   * @return le nombre de parties voulu
   * 
   */
  int drawPartiesSelect() {
    // Initialisation
    StdDraw.picture(0,0, "STUCKWIN_PARTIES.jpg", 20,20);
    StdDraw.show();

    // Pause pour ne pas clic 2x par erreur
    StdDraw.pause(500);

    while (!StdDraw.isMousePressed()){
      //Attendre l'utilisateur
    }
    double mouseX = StdDraw.mouseX();
    double mouseY = StdDraw.mouseY();

    if (mouseY >= -1.68){
      if (mouseX < 0) {
        return 1;
      } else {
        return 3;
      }
    } else {
      if (mouseX <= 0) {
        return 5;
      } else {
        return 15;
      }
    }
  }

  /**
   * 
   * Permet l'affichage et la sélection d'un mode de jeu spécial
   * 
   * @return le mode de jeu choisis (1 ou 2)
   * 
   */
  int drawSpecialSelect() {
    // Initialisation
    StdDraw.picture(0,0, "STUCKWIN_SPECIAL.jpg", 20,20);
    StdDraw.show();

    // Pause pour ne pas clic 2x par erreur
    StdDraw.pause(500);

    while (!StdDraw.isMousePressed()){
      //Attendre l'utilisateur
    }
    double mouseX = StdDraw.mouseX();
    if (mouseX <= 0){
      niWkcutS = 1;
      return 1;
    } else {
      niWkcutS = 2;
      return 2;
    }
  }

  /**
   * 
   * Transforme le tableau donnée en String
   * 
   * @return renvoie le String
   * 
   */
  String createStringFromTab(char tab[][], char color) {
    String result = "";
    for (int i = 0; i < BOARD_SIZE; i++) {
      for (int j = 1; j < SIZE; j++) {
        if (tab[i][j] != '-') {
          if (color == 'B' && tab[i][j] == 'R') {
            // Modifier result pour alterer l'IA
            result += '.'; //result += 'R';
          } else if (color == 'R' && tab[i][j] == 'B') {
            result += '.'; //result += 'B';
          } else {
            result += tab[i][j];
          }
        }
      }
    }
  return result;
  }

  /**
   * Permet de rechercher toutes les possibilités qu'un joueur peut jouer
   * 
   * @param color la couleur du joueur dont on veut connaitre les possibilités
   * 
   * @return Une liste de String représentant les différents états du tableau
   * qui seraient possibles
   */
  ArrayList<String> dataPossibilities(char color){
    ArrayList<String> virtualStates = new ArrayList<>();
    char[][] virtualState = new char[BOARD_SIZE][SIZE];
    String resultat = "";

    for (int i = 0; i < BOARD_SIZE; i++) {
      for (int j = 1; j < SIZE; j++) {

        if (state[i][j] == color){
          String[] mouv = possibleDests(color, i, j);

          switch (color){
            case 'B':
              if (!mouv[0].equals("XXX")) {
                //deplace un pion sur le plateau virtuel
                for (int lon = 0; lon < BOARD_SIZE; lon++) {
                    for (int col = 0; col < SIZE; col++) {
                      virtualState[lon][col] = state[lon][col];
                    }
                }
                virtualState[i][j] = '.'; 
                virtualState[i][j-1] = 'B';
                resultat = createStringFromTab(virtualState, color);
                virtualStates.add(resultat);
              }
              if (!mouv[1].equals("XXX")) {
                for (int lon = 0; lon < BOARD_SIZE; lon++) {
                    for (int col = 0; col < SIZE; col++) {
                      virtualState[lon][col] = state[lon][col];
                    }
                }
                virtualState[i][j] = '.'; 
                virtualState[i-1][j] = 'B';
                resultat = createStringFromTab(virtualState, color);
                virtualStates.add(resultat);
              }
              if (!mouv[2].equals("XXX")) {
                for (int lon = 0; lon < BOARD_SIZE; lon++) {
                    for (int col = 0; col < SIZE; col++) {
                      virtualState[lon][col] = state[lon][col];
                    }
                }
                virtualState[i][j] = '.'; 
                virtualState[i-1][j+1] = 'B';
                resultat = createStringFromTab(virtualState, color);
                virtualStates.add(resultat);
              }
              break;

            case 'R':
              if (!mouv[0].equals("XXX")) {
                for (int lon = 0; lon < BOARD_SIZE; lon++) {
                    for (int col = 0; col < SIZE; col++) {
                      virtualState[lon][col] = state[lon][col];
                    }
                }
                virtualState[i][j] = '.'; 
                virtualState[i+1][j-1] = 'R';
                resultat = createStringFromTab(virtualState, color);
                virtualStates.add(resultat);
              }
              if (!mouv[1].equals("XXX")) {
                for (int lon = 0; lon < BOARD_SIZE; lon++) {
                    for (int col = 0; col < SIZE; col++) {
                      virtualState[lon][col] = state[lon][col];
                    }
                }
                virtualState[i][j] = '.'; 
                virtualState[i+1][j] = 'R';
                resultat = createStringFromTab(virtualState, color);
                virtualStates.add(resultat);
              }
              if (!mouv[2].equals("XXX")) {
                for (int lon = 0; lon < BOARD_SIZE; lon++) {
                    for (int col = 0; col < SIZE; col++) {
                      virtualState[lon][col] = state[lon][col];
                    }
                }
                virtualState[i][j] = '.'; 
                virtualState[i][j+1] = 'R';
                resultat = createStringFromTab(virtualState, color);
                virtualStates.add(resultat);
              }
              break;
            }
        }
      }
    }
  return virtualStates;
  }

  /**
   * 
   * Permet de stocker les données du jeu à un instant donnée
   * Notament l'état du plateau (un String de 37 char), 
   * et le tour du joueur actuel ('R' ou 'B')
   * 
   * @param curPlayer le joueur qui doit jouer ('R' ou 'B')
   */
  void storeData(char curPlayer) {
    String result = createStringFromTab(state, curPlayer);
    storedData.add(result);
    storedDataColor.add(curPlayer);
  }

  /**
   * Enregistre les données de la partie joué dans les HashMap 
   * dataMapRed et dataMapBlue
   * 
   * @param winner le vainqueur de la partie
   */
  void takeData(char winner) {
    int i = 0;
    // Pour chaque set de données récupéré par storeData
    for (String stateString : storedData) {

      if (storedDataColor.get(i) == 'R') {
        if (dataMapRed.containsKey(stateString)) {
          int WCountB = dataMapRed.get(stateString).getWCountB();
          int WCountR = dataMapRed.get(stateString).getWCountR();
          Data data = dataMapRed.get(stateString);
          if (winner == 'R') {
            data.setWCountR(WCountR+1);
            data.setWCountB(WCountB);
          } else { // winner is equal to B
            data.setWCountR(WCountR);
            data.setWCountB(WCountB+1);
          }
          dataMapRed.put(stateString, data);
        } else { // first time situation
          if (winner == 'R') { // red win count to 1
            dataMapRed.put(stateString, new Data(1, 0));
          } else { // blue win count to 1
            dataMapRed.put(stateString, new Data(0, 1));
          }
        }
      } else { // storedDataColor == 'B'
        if (dataMapBlue.containsKey(stateString)) {
          int WCountB = dataMapBlue.get(stateString).getWCountB();
          int WCountR = dataMapBlue.get(stateString).getWCountR();
          Data data = dataMapBlue.get(stateString);
          if (winner == 'R') {
            data.setWCountR(WCountR+1);
            data.setWCountB(WCountB);
          } else { // winner is equal to B
            data.setWCountR(WCountR);
            data.setWCountB(WCountB+1);
          }
          dataMapBlue.put(stateString, data);
        } else { // first time situation
          if (winner == 'R') { // red win count to 1
            dataMapBlue.put(stateString, new Data(1, 0));
          } else { // blue win count to 1
            dataMapBlue.put(stateString, new Data(0, 1));
          }
        }
      }
    i++;
    }
    storedData.clear();
    storedDataColor.clear();
  }
  
  /**
   * Permet de sauvegarder les données des parties dans un fichier
   * 
   * @param namefile nom du fichier dans lequel on souhaite chercher une valeur
   * @param hashmap valeurs que l'on souhaite ajouter au fichier
   */
  public static void savingFiles(String namefile, HashMap<String, Data> hashMap){
    long numberOfKeys = hashMap.size();
    int bufferSize = (int) Math.min(500 * numberOfKeys + 1024, Integer.MAX_VALUE*0.75);
    
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(namefile, true);
      FileChannel fc = fos.getChannel();

      // Create a byte buffer and write the map to it
      ByteBuffer bb = ByteBuffer.allocate(bufferSize);
      for (Entry<String, Data> entry : hashMap.entrySet()) {
        // Compress the key
        String compressedKeyStr = compressData(entry.getKey());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(entry.getValue());
        oos.close();
        byte[] dataBytes = baos.toByteArray();
        bb.putInt(compressedKeyStr.length());
        bb.put(compressedKeyStr.getBytes());
        bb.putInt(dataBytes.length);
        bb.put(dataBytes);
        }
      bb.flip();
      fc.write(bb);

      // Close the file
      fos.close();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (fos != null) {
        try {
          fos.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Permet de chercher une valeur dans un fichier
   * 
   * @param namefile nom du fichier dans lequel on souhaite chercher une valeur
   * @param key valeur que l'on souhaite chercher
   * 
   * @return un object Data
   */
  public static Data searchValue(String namefile, String oldkey) {
    FileInputStream fis = null;
    String key = compressData(oldkey);
    try {
      fis = new FileInputStream(namefile);
      File file = new File(namefile);
      long fileSize = file.length();
      int bufferSize = (int) Math.min(fileSize, Integer.MAX_VALUE*0.75);
      byte[] buffer = new byte[bufferSize];
      int bytesRead;
      Data value = null;

      while ((bytesRead = fis.read(buffer)) != -1) {
        ByteBuffer bb = ByteBuffer.wrap(buffer, 0, bytesRead);
        while (bb.hasRemaining()) {
          if (bb.remaining() < 4) {
            System.out.println("Error: Not enough data in buffer to read the key length");
            value = searchValueRAF(namefile, key);
            return value;
          }
          int keyLength = bb.getInt();
          byte[] keyBytes = null;
          if (keyLength <= bb.remaining()) {
            keyBytes = new byte[keyLength];
            bb.get(keyBytes);
          } else {
            System.out.println("Error: Not enough data in buffer to read the key");
            value = searchValueRAF(namefile, key);
            return value;
          }
          String readKey = new String(keyBytes);
          if (readKey.equals(key)) {
            if (bb.remaining() < 4) {
              System.out.println("Error: Not enough data in buffer to read the value length");
              value = searchValueRAF(namefile, key);
              return value;
            }
            int valueLength = bb.getInt();
            byte[] valueBytes = null;
            if (valueLength <= bb.remaining()) {
              valueBytes = new byte[valueLength];
              bb.get(valueBytes);
            } else {
              System.out.println("Error: Not enough data in buffer to read the value");
              value = searchValueRAF(namefile, key);
              return value;
            }
            ByteArrayInputStream bais = new ByteArrayInputStream(valueBytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            value = (Data) ois.readObject();
            ois.close();
            return value;
            }
          if (bb.remaining() < 4) {
            System.out.println("Error: Not enough data in buffer to skip the value");
            value = searchValueRAF(namefile, key);
            return value;
          }
          int valueLength = bb.getInt();
          if (valueLength <= bb.remaining()) {
            bb.position(bb.position() + valueLength);
          } else {
            System.out.println("Error: Not enough data in buffer to skip the value");
            value = searchValueRAF(namefile, key);
            return value;
          }
        }
      }
      // Key not found in map
      return null;
      } catch (IOException | ClassNotFoundException e) {
        e.printStackTrace();
        return null;
      } finally {
        if (fis != null) {
      try {
        fis.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}

  /**
   * Fonction 'de secours' a searchValue qui prends plus de temps,
   * mais fonctionne sur de plus grand fichiers
   * 
   * @param namefile nom du fichier dans lequel on souhaite chercher une valeur
   * @param key valeur que l'on souhaite chercher
   * 
   * @return un object Data
   */
  public static Data searchValueRAF(String namefile, String key) {
    RandomAccessFile raf = null;
    try {
        raf = new RandomAccessFile(namefile, "r");
        // Go through the file looking for the key
        while (raf.getFilePointer() < raf.length()) {
            int keyLength = raf.readInt();
            byte[] keyBytes = new byte[keyLength];
            raf.read(keyBytes);
            String readKey = new String(keyBytes);
            if (readKey.equals(key)) {
                // Found the key, read the value
                int valueLength = raf.readInt();
                byte[] valueBytes = new byte[valueLength];
                raf.read(valueBytes);
                ByteArrayInputStream bais = new ByteArrayInputStream(valueBytes);
                ObjectInputStream ois = new ObjectInputStream(bais);
                Data value = (Data) ois.readObject();
                ois.close();
                return value;
            } else {
                // Key does not match, skip the value
                int valueLength = raf.readInt();
                raf.seek(raf.getFilePointer() + valueLength);
            }
        }
        System.out.println("Error: Key not found in map, key : " + key);
        return null;
    } catch (IOException | ClassNotFoundException e) {
        e.printStackTrace();
        return null;
    } finally {
        if (raf != null) {
            try {
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

  /**
   * Permet d'afficher un échantillon de données issue de HashMap
   * 
   * @param dataMap une HashMap
   */
  public static void printData(HashMap<String, Data> dataMap){
    // Print the values of the data objects in the HashMap
    int i = 0;
    for (String key : dataMap.keySet()) {
      System.out.println(key + ": " + dataMap.get(key).getWCountB() + " " + dataMap.get(key).getWCountR());
      if (i > 1000) {
        return;
      }
      i++;
    }
  }

  public static String compressData(String someData) {
    StringBuilder compressed = new StringBuilder();
    int count = 1;
    char last = someData.charAt(0);

    for (int i = 1; i < someData.length(); i++) {
        if (someData.charAt(i) == last) {
            count++;
        } else {
            if (count > 1) {
                compressed.append(count);
            }
            compressed.append(last);
            last = someData.charAt(i);
            count = 1;
        }
    }
    if (count > 1) {
        compressed.append(count);
    }
    compressed.append(last);

    return compressed.toString();
  }

  public static void main(String[] args) {
    // Initialisation
    int counter = 0;
    String message = "";
    StuckWin jeuInit = new StuckWin();
    
    try {
      if (args.length > 0) {
        Integer.parseInt(args[0]);
        numberAI = Integer.parseInt(args[0]);
      }
    } catch (NumberFormatException e) {
        // Handle the exception, for example by displaying an error message
        message = "L'argument entré lors de l'exécution du fichier" 
        + "n'est pas valide, valeur par défaut utilisée";
        jeuInit.printMessage(message, true);
        jeuInit.printMessage("", true);
    }

    // On vérifie que la police d'écriture utilisé est installée
    jeuInit.checkForFont();

    // On vérifie que les fichiers de sauvegarde n'ont pas été crées
    File file1 = new File("dataMapBlue.bin");
    File file2 = new File("dataMapRed.bin");
    if ((file1.exists() && COLLECTING_DATA == true) 
     || (file2.exists() && COLLECTING_DATA == true)) {
      message = "Des fichiers de sauvegarde de données existent déjà, " +
      "si vous continuez d'exécuter ce programme, il est possible qu'il " +
      "perde en performances";
      jeuInit.printMessage(message, true);

      message = "Attention : Il est conseiller de supprimer les fichiers .bin " +
      "ou vous pouvez désactiver la collecte de données";
      jeuInit.printMessage(message, true);

      jeuInit.printMessage("", true);
    }

    // On demande à l'utilisateur si il souhaite sauvegarder les données
    boolean choisi = false;
    while (!choisi) {
      jeuInit.printMessage("", true);
      message = "Voulez-vous sauvegarder les données ? (1) oui (2) non";
      jeuInit.printMessage(message, true);

      try {
        int nb = input.nextInt();
        if (nb == 1) {
          COLLECTING_DATA = true;
          choisi = true;
        } else if (nb == 2) {
          choisi = true;
        }

      } catch(InputMismatchException e){
        jeuInit.printMessage(ENTRY_ERROR, true);
        input.nextLine();
      }
    }

    int victoiresBleu = 0;
    int victoiresRouge = 0;
    int nombreDeParties = 1;

    
    // On demande à l'utilisateur la sélection de l'affichage
    while (StuckWin.affichageG != 1 && StuckWin.affichageG != 2) {
      jeuInit.printMessage("", true);
      message = "Voulez-vous afficher les graphiques ? (1) oui (2) non";
      jeuInit.printMessage(message, true);

      try {
        StuckWin.affichageG = input.nextInt();

      } catch(InputMismatchException e){
        jeuInit.printMessage(ENTRY_ERROR, true);
        input.nextLine();
      }
    }

    // Si l'affichage est activé, on utilise StdDraw
    if (StuckWin.affichageG == 1) {
      jeuInit.printMessage("", true);
      message = "Attention, les inputs dans le terminal sont maintenant "
      + "désactivés, veuillez utiliser l'affichage StdDraw pour intéragir "
      + "avec le jeu";
      jeuInit.printMessage(message, true);

      StdDraw.setXscale(-10, 10);
      StdDraw.setYscale(-10, 10);
      StdDraw.setCanvasSize(1000, 1000);

      jeuInit.introStuckWin();
      StdDraw.enableDoubleBuffering();
      jeuInit.drawGamemodeSelect();
      jeuInit.drawSpecialSelect();
      nombreDeParties = jeuInit.drawPartiesSelect();

    } else {
      // Sinon on écrit dans le terminal
      jeuInit.gamemodeSelect();
      jeuInit.niWkcutSSelect();
      nombreDeParties = jeuInit.nbPartiesSelect();
    }

    // On joue le nombre de parties voulu
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
        if (gamemode != 4){
          jeu.affiche();
        }
        if (StuckWin.affichageG == 1) {
          jeu.affichageGraphique();
          StdDraw.show();
        }

        do {
          
          // On joue l'IA ou le joueur dépendament du gamemode
          if (gamemode == 3 || gamemode == 4) {
            if (numberAI == 2 && cpt%2==0) {
              System.out.println("AI 1");
              reponse = jeu.jouerIA_StupidTurtle(curCouleur);
            } else {
              System.out.println("AI 2");
              reponse = jeu.jouerIA(curCouleur);
            }
          } else {
            reponse = jeu.jouer(curCouleur);
          }

          // On récupère la source et la destination du pion à joué
          src = reponse[0];

          dest = reponse[1];

          if ("q".equals(src))

            return;

          // On le déplace
          status = jeu.deplace(curCouleur, src, dest, ModeMvt.REAL);

          // Si l'affichage graphique est activé on le met à jour
          if (status != Result.OK && affichageG == 1) {
            StdDraw.clear();
            jeu.affichageGraphique();

            StdDraw.setPenColor(StdDraw.WHITE);
            StdDraw.filledRectangle(0, 7.5, 2, 0.5);

            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.setFont(new Font("OCR A Extended", Font.PLAIN, 26));
            message = "status : " + status + " partie : " + partie;
            StdDraw.text(0, 7.5, message);

            StdDraw.show();
          }

          partie = jeu.finPartie(nextCouleur);

          if (gamemode != 4){
            // On print le status actuel du jeu 
            message = "status : " + status + " partie : " + partie;
            jeu.printMessage(message, true);
          }
          // Pendant que l'entrée n'est pas valide et la partie n'est pas fini 
          //on re-demande le déplacement d'un pion
        } while (status != Result.OK && partie == 'N');

        tmp = curCouleur;

        curCouleur = nextCouleur;

        nextCouleur = tmp;

        cpt++;

        // Pendant que la partie n'est pas fini, on recommence
      } while (partie == 'N');

      if (affichageG == 1) {
        jeu.affichageGraphique();
        StdDraw.show();
      }
      jeu.takeData(partie);

      if (gamemode != 4){
        // On print le vainqueur et son nombre de coups utilisé
        System.out.printf("Victoire : " +partie+ " (" + (cpt / 2) + " coups)");
        System.out.println("");
        jeu.affiche();
      }
      
      // On incrémente les compteurs de victoire
      if (partie == 'R') {
        victoiresRouge++;
      } else {
        victoiresBleu++;
      }

      counter++;
      if (COLLECTING_DATA && counter >= 1000000) {
        message = "writing data into file, this operation can take a while";
        jeuInit.printMessage(message, true);
    
        savingFiles("dataMapBlue.bin", dataMapBlue);
        savingFiles("dataMapRed.bin", dataMapRed);
        dataMapBlue.clear();
        dataMapRed.clear();
    
        message = "writing data completed, resuming matches";
        jeuInit.printMessage(message, true);

        counter = 0;
      }

      if (affichageG ==1) {
        // On dessine le gagnant
        jeu.drawWinningScreen(partie, cpt/2, victoiresBleu, victoiresRouge);
        StdDraw.show();
      }

      if (i == nombreDeParties-1) {
        jeu.printMessage("", true);
        int rejouer = jeu.rejouer();
        if (rejouer == 1) {
          i -= 1;
        }
      }
    }

    // On print le total de victoires bleu et rouge
    message = "Victoires Bleu :" + victoiresBleu 
           + " Victoires Rouge :" + victoiresRouge;
    jeuInit.printMessage(message, true);

    if (COLLECTING_DATA) {
      message = "writing data into file, this operation can take a while";
      jeuInit.printMessage(message, true);

      savingFiles("dataMapBlue.bin", dataMapBlue);
      savingFiles("dataMapRed.bin", dataMapRed);

      message = "writing data completed";
      jeuInit.printMessage(message, true);
    }
  }
}
