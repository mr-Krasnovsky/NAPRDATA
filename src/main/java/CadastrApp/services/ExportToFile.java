package CadastrApp.services;

import com.jsevy.jdxf.DXFDocument;
import com.jsevy.jdxf.DXFGraphics;
import CadastrApp.models.ObjectPoint;

import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.awt.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExportToFile implements ExportToFileInterface {
    private String fileName;

    @Override
    public byte[] exportToCSV(LinkedHashMap<Integer, ObjectPoint> map) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DecimalFormat df = new DecimalFormat("#.###"); // Форматирование до 3 знаков после запятой
        try {
            outputStream.write("Name,X,Y,H\n".getBytes());

            for (Map.Entry<Integer, ObjectPoint> entry : map.entrySet()) {
                outputStream.write((entry.getKey() + "," +
                        entry.getValue().getB() + "," +
                        entry.getValue().getL() + "," +
                        entry.getValue().getH() + "\n").getBytes());
            }

            System.out.println("CSV данные сгенерированы.");
        } catch (IOException e) {
            System.err.println("Ошибка при генерации CSV данных: " + e.getMessage());
        }
        return outputStream.toByteArray();
    }

    @Override
    public byte[] exportToDXF(LinkedHashMap<Integer, ObjectPoint> map) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            DXFDocument dxfDocument = new DXFDocument();
            dxfDocument.setUnits(6);  // Set units to mm (4) or m (6)
            dxfDocument.setPrecisionDigits(3); // Set precision digits

            DXFGraphics dxfGraphics = dxfDocument.getGraphics();
            dxfDocument.setViewportCenter(map.get(1).getL() - 250, map.get(1).getB());
            dxfDocument.setViewportScale(250);
            dxfDocument.setLayer("layerName");
            dxfGraphics.setColor(Color.RED);

            double[] x = new double[map.size()];
            double[] y = new double[map.size()];
            for (Map.Entry<Integer, ObjectPoint> entry : map.entrySet()) {
                x[entry.getKey() - 1] = entry.getValue().getL();
                y[entry.getKey() - 1] = -entry.getValue().getB();

            }
            for (int i = 0; i < x.length-1; i++){
                dxfGraphics.drawPoint(x[i], y[i]);
            }
            dxfGraphics.drawPolyline(x, y, map.size());

            String stringOutput = dxfDocument.toDXFString();
            outputStream.write(stringOutput.getBytes());

            System.out.println("DXF данные сгенерированы.");
        } catch (IOException e) {
            System.err.println("Ошибка при генерации DXF данных: " + e.getMessage());
        }
        return outputStream.toByteArray();
    }



    public byte[] getFileBytes() throws IOException {
        File file = new File(fileName);
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];

        try {
            for (int readNum; (readNum = fis.read(buf)) != -1;) {
                bos.write(buf, 0, readNum);
            }
        } catch (IOException ex) {
            throw ex;
        }

        byte[] bytes = bos.toByteArray();

        // Закрытие потоков
        try {
            fis.close();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bytes;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
