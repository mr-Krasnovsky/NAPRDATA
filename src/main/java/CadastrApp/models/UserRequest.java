package CadastrApp.models;

public class UserRequest {
    private String realEstateName;
    private double[] coords;

    public UserRequest (String realEstateName){
        this.realEstateName=realEstateName;
    }

    public double[] getCoords() {
        return coords;
    }

    public void setCoords(double[] coords) {
        this.coords = coords;
    }

    private UserRequest (double[] coords){
        this.coords = coords;
    }

    public String getRealEstateName() {
        return realEstateName;
    }

    public void setRealEstateName(String realEstateName) {
        this.realEstateName = realEstateName;
    }
}
