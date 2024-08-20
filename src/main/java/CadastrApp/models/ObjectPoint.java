package CadastrApp.models;

public class ObjectPoint {
    private double b;
    private double l;
    private double h;

    public ObjectPoint(double b, double l, double h) {
        this.b = b;
        this.l = l;
        this.h=h;
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

    @Override
    public String toString() {
        return "Point{" +
                "b=" + b +
                ", l=" + l +
                ", h=" + h +
                '}';
    }
}
