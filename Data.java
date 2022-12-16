import java.util.ArrayList;
import java.io.*;

// A simple data class that we want to store in an array
class Data implements Serializable {
  private int value;

  public Data(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
