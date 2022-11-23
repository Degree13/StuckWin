import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import java.util.Scanner;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import com.oracle.webservices.internal.impl.internalspi.encoding.StreamDecoder;

public class StuckWin {

    static final Scanner input = new Scanner(System.in);

    private static final double BOARD_SIZE = 7;


    enum Result {OK, BAD_COLOR, DEST_NOT_FREE, EMPTY_SRC, TOO_FAR, EXT_BOARD, EXIT}

    enum ModeMvt {REAL, SIMU}

    final char[] joueurs = {'B', 'R'};

    final int SIZE = 8;

    final char VIDE = '.';

    // 'B'=bleu 'R'=rouge '.'=vide '-'=n'existe pas

    char[][] state = {

            {'-', '-', '-', '-', 'R', 'R', 'R', 'R'},

            {'-', '-', '-', 'R', 'R', 'R', 'R', '.'},

            {'-', '-', 'R', 'R', 'R', '.', '.', '.'},

            {'-', 'R', 'R', '.', '.', '.', 'B', 'B'},

            {'-', '.', '.', '.', 'B', 'B', 'B', '-'},

            {'-', '.', 'B', 'B', 'B', 'B', '-', '-'},

            {'-', 'B', 'B', 'B', 'B', '-', '-', '-'},

    };


    /**

     * Déplace un pion ou simule son déplacement

     * @param couleur couleur du pion à déplacer

     * @param lcSource case source Lc

     * @param lcDest case destination Lc

     * @param mode ModeMVT.REAL/SIMU selon qu'on réalise effectivement le déplacement ou qu'on le simule seulement.

     * @return enum {OK, BAD_COLOR, DEST_NOT_FREE, EMPTY_SRC, TOO_FAR, EXT_BOARD, EXIT} selon le déplacement

     */

    Result deplace(char couleur, String lcSource, String lcDest,  ModeMvt mode) {
      //ATTENTION !!!!!!!!!!!!!!!!!! IL MANQUE ENCORE EXT_BOARD ET EXIT
      // Traduction des Strings en Ints exploitables avec le tableau
      int idLineSource = 55-lcSource.charAt(1);
      int idLineDest = 55-lcDest.charAt(1);
      
      int idColSource = (lcSource.charAt(1)-48)+(lcSource.charAt(0)-68);
      int idColDest = (lcDest.charAt(1)-48)+(lcDest.charAt(0)-68);
      //System.out.println("TESTS Source colonne:" + idColSource + " Source ligne:" + idLineSource + " Destination colonne :"+ idColDest + " Destination ligne:" + idLineDest);
      if (lcSource.toUpperCase().equals("EXIT") || lcDest.toUpperCase().equals("EXIT")) {
        return Result.EXIT;
      }
      if (idLineSource < 0 || idLineSource > 6 || idLineDest < 0 || idLineDest > 6 || idColSource < 1 || idColSource > 7 || idColDest < 1 || idColDest > 7) {
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
      String isTooFar =  Arrays.toString(possibleDests(couleur, idLineSource, idColSource));
      //System.out.println(isTooFar);
      boolean boolTooFar = true;
      switch(couleur){
        case 'B':
          if (isTooFar.charAt(1) == 'L' && idColDest+1 == idColSource && idLineDest == idLineSource) {
            //System.out.println("Checkpoint1 B");
            boolTooFar = false;
          } else if (isTooFar.charAt(6) == 'T' && idLineDest+1 == idLineSource && idColDest == idColSource) {
            //System.out.println("Checkpoint2 B");
            boolTooFar = false;
          } else if (isTooFar.charAt(11) == 'R' && idLineDest+1 == idLineSource && idColDest-1 == idColSource) {
            //System.out.println("Checkpoint3 B");
            boolTooFar = false;
          }
          if (boolTooFar == true) {
            return Result.TOO_FAR;
          }
          break;
        case 'R':
          if (isTooFar.charAt(1) == 'L' && (idColDest+1 == idColSource && idLineDest-1 == idLineSource)) {
            //System.out.println("Checkpoint1 R");
            boolTooFar = false;
          } else if (isTooFar.charAt(6) == 'D' && idLineDest-1 == idLineSource && idColDest == idColSource) {
            //System.out.println("Checkpoint2 R");
            boolTooFar = false;
          } else if (isTooFar.charAt(11) == 'R' && idLineDest == idLineSource && idColDest-1 == idColSource) {
            //System.out.println("Checkpoint3 R");
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

     * Construit les trois chaînes représentant les positions accessibles

     * à partir de la position de départ [idLettre][idCol].

     * @param couleur couleur du pion à jouer

     * @param idLettre id de la ligne du pion à jouer

     * @param idCol id de la colonne du pion à jouer

     * @return tableau des trois positions jouables par le pion (redondance possible sur les bords)

     */

    String[] possibleDests(char couleur, int idLettre, int idCol){

      if (idLettre > 6 || idLettre < 0 || idCol > 7 || idCol < 1){
        String throwMsg = "Erreur, les valeurs entrées en paramètres de la fonction possibleDests ne correspondent à aucune case";
        throw new java.lang.UnsupportedOperationException(throwMsg);
      }

      String[] possibilites = new String[3];
      Arrays.fill(possibilites, "XXX");

      switch(couleur){

        case 'B': 
          if (idCol > 1) {
            if (state[idLettre][idCol-1] == '.') {
              possibilites[0] = "LFT";       
            }
          }
          if (idLettre > 0) {
            if (state[idLettre-1][idCol] == '.') {
              possibilites[1] = "TOP";       
            }
          }
          if (idLettre > 0 && idCol < 7) {
            if (state[idLettre-1][idCol+1] == '.') {
              possibilites[2] = "RGT";       
            }
          }
          break;
        
        case 'R':
          if (idCol > 1 && idLettre < 6) {
            if (state[idLettre+1][idCol-1] == '.') {
              possibilites[0] = "LFT";       
            }
          }
          if (idLettre < 6) {
            if (state[idLettre+1][idCol] == '.') {
              possibilites[1] = "DWN";       
            }
          }
          if (idCol < 7) {
            if (state[idLettre][idCol+1] == '.') {
              possibilites[2] = "RGT";       
            }
          }
          break;
      }

      //System.out.println(state[idLettre][idCol] + " " + possibilites[0] + possibilites[1] + possibilites[2]);
      return possibilites;
    }


    /**

     * Affiche le plateau de jeu dans la configuration portée par

     * l'attribut d'état "state"

     */

    void affiche() {
      //TESTS A DEGAGER
      //possibleDests('R', 0, 7);
      //System.out.println(deplace('B', "E3", "D4", ModeMvt.REAL));

      // Affichage console Dev
      for(int it = 0; it < state.length; it++) {
        int letter = 65;
        if (it>3){
          letter += (it-3);
        }
        for (int e = 1; e < state.length+1; e++){
          if (state[it][e] == 'B') {
            System.out.print(ConsoleColors.BLUE + (char)letter + (7-it) +" ");
            letter += 1;
          } else if (state[it][e] == 'R') {
            System.out.print(ConsoleColors.RED + (char)letter + (7-it) +" ");
            letter += 1;
          } else if (state[it][e] == '.') {
              System.out.print(ConsoleColors.RESET  + (char)letter + (7-it)+" ");
              letter += 1;
            } else {
              System.out.print(" " + ConsoleColors.RESET + state[it][e] + " ");
            }
        }
        System.out.println("");
    }

    // Affichage console Jeu

    // Affichage StdDraw
    StdDraw.setXscale(-10, 10);
    StdDraw.setYscale(-10, 10);
    StdDraw.clear();

    for(int it = 0; it < state.length; it++) {
      int letter = 65;
      double hauteur = 5-it*0.85;
      double largeur = 0;
      if (it<4){
        largeur = 0-it*1.5;
      } else {
         largeur = -4.5;
         hauteur = 7.5-it*1.7;
          letter += (it-3);
      }
      for (int e = 1; e < state.length+1; e++){
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
          String nomCase = String.valueOf((char)letter) + String.valueOf(7-it); //char et int a convertir en String
          StdDraw.text(largeur-1.5, hauteur+0.85, nomCase);
          letter += 1;
        }
      }
    }
    StdDraw.show();
  }

  /**

     * Dessin un héxagone

     * @param x coordonnées X
     * @param y coordonnées Y
     * @param size taille de l'héxagone

     */
  void hexagon(double x, double y, double size) {
    double theta = (2*Math.PI)/6;
    for (int i=0; i<=6 ; i++){
      StdDraw.line((Math.cos(i*theta)*size)+x, (Math.sin(i*theta)*size)+y, (Math.cos((i+1)*theta)*size)+x, (Math.sin((i+1)*theta)*size)+y);
    }     
  }

  /**

     * Créer une animation de déplacement de pion

     * @param coordSource coordonnées X
     * @param coordDest coordonnées Y
     * @param char couleur du joueur actuel

     */
    void animationArrow(char couleur, String coordSource, String coordDest){
      System.out.println("test");
    }


    /**

     * Joue un tour

     * @param couleur couleur du pion à jouer

     * @return tableau contenant la position de départ et la destination du pion à jouer.

     */

    String[] jouerIA(char couleur) {
      int maxL = 8;
      int minL = 1;
      int maxH = 7;
      int minH = 0;
      String[] tabIa = new String[2];
      String lesPossibles = "";
      int hauteur;
      int largeur;

      do {
        hauteur = (int) (Math.random()*(maxH-minH)) + minH;
        largeur = (int) (Math.random()*(maxL-minL)) + minL;
        lesPossibles =  Arrays.toString(possibleDests(couleur, hauteur, largeur));
      } while (lesPossibles.charAt(1) != 'L' && lesPossibles.charAt(6) != 'D' && lesPossibles.charAt(6) != 'T' && lesPossibles.charAt(11) != 'R' || state[hauteur][largeur] != couleur);
      //System.out.println(largeur);
      int returnedHauteur = 7-hauteur;
      char returnedLargeur = (char)(largeur+61+(7-returnedHauteur));
      tabIa[0] = Character.toString(returnedLargeur) + Integer.toString(returnedHauteur);
      //System.out.println("TOUR DE L IA, couleur : " + couleur);
      //System.out.println(tabIa[0]);
      //System.out.println("couleur " + couleur);
      //System.out.println(Arrays.toString(possibleDests(couleur, hauteur, largeur)));
      int maxR = 4;
      int minR = 1;
      boolean isValid;

      do {  
        isValid = true;
        int choixMv = (int) (Math.random()*(maxR-minR)) + minR;

        switch(couleur){

          case 'R':
            if (lesPossibles.charAt(1) == 'L' && choixMv == 1) {
              tabIa[1] = Character.toString(returnedLargeur) + Integer.toString(returnedHauteur-1);
              //System.out.println("OK GOT IT IM : " + couleur + " GOING LEFT");
              break;
            }
            if (lesPossibles.charAt(6) == 'D' && choixMv == 2) {
              returnedLargeur = (char)((int)returnedLargeur+1);
              tabIa[1] = Character.toString(returnedLargeur) + Integer.toString(returnedHauteur-1);
              //System.out.println("OK GOT IT IM : " + couleur + " GOING DOWN");
              break;
            }
            if (lesPossibles.charAt(11) == 'R' && choixMv == 3) {
              returnedLargeur = (char)((int)returnedLargeur+1);
              tabIa[1] = Character.toString(returnedLargeur) + Integer.toString(returnedHauteur);
              //System.out.println("OK GOT IT IM : " + couleur + " GOING RIGHT");
              break;
            }
            isValid = false;
            break;

          case 'B':
            if (lesPossibles.charAt(1) == 'L' && choixMv == 1) {
              returnedLargeur = (char)((int)returnedLargeur-1);
              tabIa[1] = Character.toString(returnedLargeur) + Integer.toString(returnedHauteur);
              break;
            }
            if (lesPossibles.charAt(6) == 'T' && choixMv == 2) {
              returnedLargeur = (char)((int)returnedLargeur-1);
              tabIa[1] = Character.toString(returnedLargeur) + Integer.toString(returnedHauteur+1);
              break;
            }
            if (lesPossibles.charAt(11) == 'R' && choixMv == 3) {
              tabIa[1] = Character.toString(returnedLargeur) + Integer.toString(returnedHauteur+1);
              break;
            }
            isValid = false;
            break;
          default:
            throw new java.lang.UnsupportedOperationException("Fonction JourIA : pas de couleur valide");
        }
      } while (isValid == false);
      
      if ("".equals(tabIa[0]) || "".equals(tabIa[1])) {
        throw new java.lang.UnsupportedOperationException("Fonction JouerIA : tabIa vide");
      }
      return tabIa;
      //throw new java.lang.UnsupportedOperationException("à compléter");

    }


    /**

     * gère le jeu en fonction du joueur/couleur

     * @param couleur

     * @return tableau de deux chaînes {source,destination} du pion à jouer

     */

    String[] jouer(char couleur){

        String src = "";

        String dst = "";

        String[] mvtIa;

        switch(couleur) {

            case 'B':

                System.out.println("Mouvement " + couleur);

                src = input.next();

                dst = input.next();

                System.out.println(src + "->" + dst);

                break;

            case 'R':

                System.out.println("Mouvement " + couleur);

                mvtIa = jouerIA(couleur);

                src = mvtIa[0];

                dst = mvtIa[1];

                System.out.println(src + "->" + dst);

                break;

        }

        return new String[]{src, dst};

    }


    /**

     * retourne 'R' ou 'B' si vainqueur, 'N' si partie pas finie

     * @param couleur

     * @return 'R' ou 'B' si vainqueur, 'N' si partie pas finie

     */

    char finPartie(char couleur){
      for(int it = 0; it < state.length; it++) {
        for (int e = 1; e < state.length+1; e++){
          if (state[it][e] == couleur) {
            String canMove =  Arrays.toString(possibleDests(couleur, it, e));
            //System.out.println("fonction finPartie : " + canMove);
            if (!"[XXX, XXX, XXX]".equals(canMove)){
              return 'N';
            }
          }
        }
      }
      return couleur;

      //throw new java.lang.UnsupportedOperationException("Fonction finPartie");

    }

    void afficheVainqueur(char couleur) {
      StdDraw.setXscale(-10, 10);
      StdDraw.setYscale(-10, 10);
      StdDraw.filledRectangle(-5, 5, 1, 1);
    }



    public static void main(String[] args) {

        StdDraw.enableDoubleBuffering();

        StuckWin jeu = new StuckWin();

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

              do {

                  status = Result.EXIT;

                  reponse = jeu.jouerIA(curCouleur);
                  //reponse = jeu.jouer(curCouleur);

                  src = reponse[0];

                  dest = reponse[1];

                  if("q".equals(src))

                      return;

                  status = jeu.deplace(curCouleur, src, dest, ModeMvt.REAL);

                  partie = jeu.finPartie(nextCouleur);

                  System.out.println("status : "+status + " partie : " + partie);

              } while(status != Result.OK && partie=='N');

              tmp = curCouleur;

              curCouleur = nextCouleur;

              nextCouleur = tmp;

              cpt ++;

        } while(partie =='N'); // TODO affiche vainqueur

        System.out.printf("Victoire : " + partie + " (" + (cpt/2) + " coups)");
        //jeu.afficheVainqueur(partie);

    }

}