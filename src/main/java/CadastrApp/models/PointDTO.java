package CadastrApp.models;

public class PointDTO {
    private int number;
    private double b;
    private double l;
    private double h;

    public PointDTO(){
    }

    public PointDTO(int number, double b, double l, double h) {
        this.number = number;
        this.b = b;
        this.l = l;
        this.h = h;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public double getL() {
        return l;
    }

    public void setL(double l) {
        this.l = l;
    }

    public double getH() {
        return h;
    }

    public void setH(double h) {
        this.h = h;
    }
}
