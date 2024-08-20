package CadastrApp.services;

import CadastrApp.models.ObjectPoint;
import CadastrApp.models.RealEstateObject;
import CadastrApp.models.UserRequest;

import java.util.LinkedHashMap;

public interface CadastralSearchInterface {

    public RealEstateObject checkingAvailabilityData (UserRequest request);

    public LinkedHashMap<Integer, ObjectPoint> getCoords (RealEstateObject object);
    public LinkedHashMap<Integer, ObjectPoint> getCommunicationCoords (RealEstateObject object);
}
