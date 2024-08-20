package CadastrApp.services;

import CadastrApp.models.ObjectPoint;
import org.opengis.referencing.FactoryException;

import java.util.LinkedHashMap;

public interface CoordinateConverterInterface {

    ObjectPoint convertToUTM(ObjectPoint point, String espg) throws FactoryException;
    LinkedHashMap<Integer, ObjectPoint> convertingPoints (LinkedHashMap<Integer, ObjectPoint> points, String espg) throws FactoryException;
}
