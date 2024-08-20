package CadastrApp.models;

public class RealEstateObject {
    private int id;
    private String name;
    private String descript;
    private String info_link;
    private String geometry_link;

    public RealEstateObject() {
    }

    public RealEstateObject(int id, String name, String descript, String info_link, String geometry_link) {
        this.id = id;
        this.name = name;
        this.descript = descript;
        this.info_link = info_link;
        this.geometry_link = geometry_link;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescript() {
        return descript;
    }

    public void setDescript(String descript) {
        this.descript = descript;
    }

    public String getInfo_link() {
        return info_link;
    }

    public void setInfo_link(String info_link) {
        this.info_link = info_link;
    }

    public String getGeometry_link() {
        return geometry_link;
    }

    public void setGeometry_link(String geometry_link) {
        this.geometry_link = geometry_link;
    }
}
