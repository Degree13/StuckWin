import java.util.ArrayList;

import java.util.List;

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

      // votre code ici. Supprimer la ligne ci-dessous.

      throw new java.lang.UnsupportedOperationException("à compléter");

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

      // votre code ici. Supprimer la ligne ci-dessous.

      throw new java.lang.UnsupportedOperationException("à compléter");

    }


    /**

     * Affiche le plateau de jeu dans la configuration portée par

     * l'attribut d'état "state"

     */

    void affiche() {

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

     * Joue un tour

     * @param couleur couleur du pion à jouer

     * @return tableau contenant la position de départ et la destination du pion à jouer.

     */

    String[] jouerIA(char couleur) {

      // votre code ici. Supprimer la ligne ci-dessous.

      throw new java.lang.UnsupportedOperationException("à compléter");

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

     * @return

     */

    char finPartie(char couleur){

      // votre code ici. Supprimer la ligne ci-dessous.

      throw new java.lang.UnsupportedOperationException("à compléter");

    }



    public static void main(String[] args) {

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

                  reponse = jeu.jouer(curCouleur);

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

    }

}