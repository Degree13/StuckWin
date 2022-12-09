// Affichage console Dev
      for(int it = 0; it < state.length; it++) {
        int letter = 65;
        if (it>3){
          letter += (it-3);
        }
        for (int e = 1; e < state.length+1; e++){
          if (state[it][e] == 'B') {
            System.out.print(2 + (char)letter + (7-it) +" ");
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

    int [][][] state3 = {
      {{-1,-1}, {-1,-1}, {-1,-1}, {0,4}, {-1,-1}, {-1,-1}, {-1,-1}},
      {{-1,-1}, {-1,-1}, {1,3}, {-1,-1}, {0,5}, {-1,-1}, {-1,-1}},
      {{-1,-1}, {2,2}, {-1,-1}, {1,4}, {-1,-1}, {0,6}, {-1,-1}},
      {{3,1}, {-1,-1}, {2,3}, {-1,-1}, {1,5}, {-1,-1}, {0,7}},
      {{-1,-1}, {3,2}, {-1,-1}, {2,4}, {-1,-1}, {1,6}, {-1,-1}},
      {{4,1}, {-1,-1}, {3,3}, {-1,-1}, {2,5}, {-1,-1}, {1,7}},
      {{-1,-1}, {4,2}, {-1,-1}, {3,4}, {-1,-1}, {2,6}, {-1,-1}},
      {{5,1}, {-1,-1}, {4,3}, {-1,-1}, {3,5}, {-1,-1}, {2,7}},
      {{-1,-1}, {5,2}, {-1,-1}, {4,4}, {-1,-1}, {3,6}, {-1,-1}},
      {{6,1}, {-1,-1}, {5,3}, {-1,-1}, {4,5}, {-1,-1}, {3,7}},
      {{-1,-1}, {6,2}, {-1,-1}, {5,4}, {-1,-1}, {4,6}, {-1,-1}},
      {{-1,-1}, {-1,-1}, {6,3}, {-1,-1}, {5,5}, {-1,-1}, {-1,-1}},
      {{-1,-1}, {-1,-1}, {-1,-1}, {6,4}, {-1,-1}, {-1,-1}, {-1,-1}}
    };  

    for(int i = 7, int colonne = 1; i>0 , colonne>5; i--, colonne++) {
      for(int y = 0; y < 6; y++){
        if (state[i][y]=='B'){
          System.out.print("  "+ConsoleColors.BLUE_BACKGROUND+afficheLettre(i)+y+ConsoleColors.RESET+"  ");
        }
        else if (state[i][y]=='R'){
          System.out.print("  "+ConsoleColors.RED_BACKGROUND+afficheLettre(i)+y+ConsoleColors.RESET+"  ");
        }
        else if (state[i][y]=='.'){
          System.out.print("  "+ConsoleColors.WHITE_BACKGROUND+afficheLettre(i)+y+ConsoleColors.RESET+"  ");
        }
        else{
          System.out.print("  "+ConsoleColors.RESET);
        }
        
      }
      System.out.println("");
    }

  }