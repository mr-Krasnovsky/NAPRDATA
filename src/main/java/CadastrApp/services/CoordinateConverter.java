package CadastrApp.services;

import CadastrApp.config.AppConstants;
import CadastrApp.models.ObjectPoint;
import org.geotools.geometry.jts.JTS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class CoordinateConverter  implements CoordinateConverterInterface {

//    public static double[] convertWGS84ToUTM38N(double latitude, double longitude, double altitude) {
//        try {
//            // Создание преобразования между WGS-84 и UTM Zone 38N
//            String wgs84 = "EPSG:4326";
//            String utm38n = "EPSG:32638";
//            String utm37n = "EPSG:32637";
//            MathTransform transform38 = (MathTransform) CRS.findMathTransform(CRS.decode(wgs84), CRS.decode(utm38n), true);
//            MathTransform transform37 = (MathTransform) CRS.findMathTransform(CRS.decode(wgs84), CRS.decode(utm38n), true);
//            // Создание геометрической фабрики для создания точек
//            GeometryFactory geometryFactory = new GeometryFactory();
//
//            // Создание точки с координатами WGS-84
//            Coordinate coord = new Coordinate(longitude, latitude, altitude);
//            org.locationtech.jts.geom.Point point = geometryFactory.createPoint(coord);
//
//            // Преобразование точки в UTM Zone 38N
//            org.locationtech.jts.geom.Point utmPoint = (org.locationtech.jts.geom.Point) JTS.transform(point, transform38);
//
//            // Возвращение результата с точностью до миллиметров
//            return new double[]{
//                    Math.round(utmPoint.getX() * 1000) / 1000.0,
//                    Math.round(utmPoint.getY() * 1000) / 1000.0,
//                    Math.round(utmPoint.getCoordinate().z * 1000) / 1000.0
//            };
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null; // В случае ошибки возвращается null
//        }
//    }

    @Override
    public ObjectPoint convertToUTM (ObjectPoint point, String espg) throws FactoryException {
        try {
        MathTransform transform = (MathTransform) CRS.findMathTransform(CRS.decode(AppConstants.WGS84), CRS.decode(espg), true);
            GeometryFactory geometryFactory = new GeometryFactory();
            Coordinate coord = new Coordinate(point.getL(), point.getB(), point.getH());
            org.locationtech.jts.geom.Point jtsPoint = geometryFactory.createPoint(coord);
            org.locationtech.jts.geom.Point utmPoint = (org.locationtech.jts.geom.Point) JTS.transform(jtsPoint, transform);

            return new ObjectPoint(
                Math.round(utmPoint.getY() * 1000) / 1000.0,
                    Math.round(utmPoint.getX() * 1000) / 1000.0,
                Math.round(utmPoint.getCoordinate().z * 1000) / 1000.0);
        } catch (Exception e) {
            System.out.println("Coordinate conversion error" + e.getMessage());
            return null;
        }
    }

    @Override
    public LinkedHashMap<Integer, ObjectPoint> convertingPoints(LinkedHashMap<Integer, ObjectPoint> points, String espg) throws FactoryException {
        LinkedHashMap<Integer, ObjectPoint> result = new LinkedHashMap<>();
        ObjectPoint point = null;
        for (Map.Entry<Integer, ObjectPoint> entry : points.entrySet()) {
            Integer index = entry.getKey();
            point = convertToUTM(entry.getValue(), espg);
            System.out.println(" p: " + point.getB() + " " + point.getL());
            result.put(index, point);
        }
        return result;
    }
}


