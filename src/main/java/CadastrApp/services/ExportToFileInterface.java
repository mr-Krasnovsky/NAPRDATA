package CadastrApp.services;

import CadastrApp.models.ObjectPoint;

import java.io.IOException;
import java.util.LinkedHashMap;

public interface ExportToFileInterface {

    byte[] exportToCSV (LinkedHashMap<Integer, ObjectPoint> map);
    byte[] exportToDXF (LinkedHashMap<Integer, ObjectPoint> map) throws IOException;
}
