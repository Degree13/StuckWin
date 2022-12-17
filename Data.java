import java.io.*;
import java.util.ArrayList;

// A simple data class that we want to serialize
class Data implements Serializable {
  private int value;
  private char c;

  public Data(int value, char c) {
    this.value = value;
    this.c = c;
  }

  public int getValue() {
    return value;
  }

  public char getC() {
    return c;
  }
}