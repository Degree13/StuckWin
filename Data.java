import java.io.*;

// A simple data class that we want to serialize
class Data implements Serializable {
  private String key;
  private int WCountR;
  private int WCountB;

  public Data(int WCountR, int WCountB) {
    this.WCountR = WCountR;
    this.WCountB = WCountB;
  }

  public int getWCountR() {
    return WCountR;
  }

  public void setWCountR(int WCountR) {
    this.WCountR = WCountR;
  }

  public int getWCountB() {
    return WCountB;
  }

  public void setWCountB(int WCountB) {
    this.WCountB = WCountB;
  }

  public String getKey() {
    // Return the key for this data object
    return key;
}
}