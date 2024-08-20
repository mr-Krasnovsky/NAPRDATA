package CadastrApp.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import CadastrApp.config.AppConstants;
import CadastrApp.models.ObjectPoint;
import CadastrApp.models.RealEstateObject;
import CadastrApp.models.UserRequest;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.util.Timeout;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class CadastralSearch implements CadastralSearchInterface {

    @Override
    public RealEstateObject checkingAvailabilityData(UserRequest request) {
        try {
            String jsonResponse = postResponse(request.getRealEstateName());
            return getObjectData(jsonResponse);
        } catch (NullPointerException e) {
            System.out.println("Проверьте формат номера участка_");
        }
        return null;
    }



    private String postResponse(String realEstateName) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(30))
                .setConnectionRequestTimeout(Timeout.ofSeconds(30))
                .setResponseTimeout(Timeout.ofSeconds(30))
                .build();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(AppConstants.mapUrl);
            request.addHeader("Content-Type", "application/x-www-form-urlencoded");

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("keyword", realEstateName)); // кадастровый номер
            params.add(new BasicNameValuePair("keyword_description[coords][]", "41.68098259150901"));
            params.add(new BasicNameValuePair("keyword_description[coords][]", "41.66475403922547"));
            params.add(new BasicNameValuePair("keyword_description[zoom]", "18.51761363598813"));
            params.add(new BasicNameValuePair("keyword_description[bbox][]", "4639702.613122467"));
            params.add(new BasicNameValuePair("keyword_description[bbox][]", "5110712.559933412"));
            params.add(new BasicNameValuePair("keyword_description[bbox][]", "4640108.902577698"));
            params.add(new BasicNameValuePair("keyword_description[bbox][]", "5111072.9645425705"));
            params.add(new BasicNameValuePair("keyword_description[screen_width]", "1536"));
            params.add(new BasicNameValuePair("keyword_description[screen_height]", "864"));
            params.add(new BasicNameValuePair("keyword_description[projection]", "EPSG:32638"));
            params.add(new BasicNameValuePair("keyword_description[orientation_angle]", "0"));
            params.add(new BasicNameValuePair("keyword_description[getinfo_type]", ""));
            params.add(new BasicNameValuePair("keyword_description[layers][]", "92"));
            params.add(new BasicNameValuePair("keyword_description[layers][]", "97"));
            params.add(new BasicNameValuePair("keyword_description[lang]", "en"));

            request.setEntity(new UrlEncodedFormEntity(params));

            // Построение URI с параметрами для вывода в консоль
            URIBuilder uriBuilder = new URIBuilder(AppConstants.mapUrl);
            for (NameValuePair param : params) {
                uriBuilder.addParameter(param.getName(), param.getValue());
            }
            URI uri = uriBuilder.build();
            System.out.println("Request URI: " + uri); // Вывод URI в консоль

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (response.getCode() == 200) {
                    String jsonResponse = new String(response.getEntity().getContent().readAllBytes());
                    return jsonResponse;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private RealEstateObject getObjectData(String jsonResponse) {
        // Парсинг JSON ответа с помощью Jackson
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonResponse);
            System.out.println("!!! " + rootNode);
            boolean status = rootNode.path("status").asBoolean();
            if (status) {
                JsonNode results = rootNode.path("result");
                if (results.isArray() && results.size() > 0) {
                    JsonNode firstResult = results.get(0);
                    JsonNode idNode = firstResult.path("id");
                    JsonNode nameNode = firstResult.path("name");
                    JsonNode descriptNode = firstResult.path("descript");
                    JsonNode detailsNode = firstResult.path("details");
                    JsonNode info_linkNode = detailsNode.path("info_link");
                    JsonNode geometry_linkNode = detailsNode.path("geometry_link");
                    RealEstateObject object = new RealEstateObject(
                            idNode.asInt(),
                            nameNode.toString(),
                            descriptNode.asText(),
                            AppConstants.mapUrlResult + info_linkNode.asText(),
                            AppConstants.dataUrl + geometry_linkNode.asText());
                    return object;
                }
            }
        } catch (JsonProcessingException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public LinkedHashMap<Integer, ObjectPoint> getCoords(RealEstateObject object) {
        String coordsResponse = getCoordsResponse(object.getGeometry_link());
        return parsingCoords(coordsResponse);
    }

    public LinkedHashMap<Integer, ObjectPoint> getCommunicationCoords(RealEstateObject object) {
        String coordsResponse = getCoordsResponse(object.getGeometry_link());
        return parsingCommunicationCoords(coordsResponse);
    }

    private String getCoordsResponse(String url) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (response.getCode() == 200) {
                    String jsonResponse = new String(response.getEntity().getContent().readAllBytes());
                    return jsonResponse;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private LinkedHashMap<Integer, ObjectPoint> parsingCoords(String jsonResponse) {
        LinkedHashMap<Integer, ObjectPoint> coordinates = new LinkedHashMap<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonResponse);
            JsonNode dataNode = rootNode.path("data");
            if (dataNode.isArray() && dataNode.size() > 0) {
                JsonNode firstItem = dataNode.get(0);
                String shape = firstItem.path("shape").asText();
                if (shape != null && !shape.isEmpty()) {
                    // Пример парсинга координат из строки shape
                    String polygonCoords = shape.replace("POLYGON ((", "").replace("))", "");
                    String[] coordsArray = polygonCoords.split(", ");
                    int index = 1;
                    for (String coord : coordsArray) {
                        String[] latLon = coord.split(" ");
                        double latitude = Double.parseDouble(latLon[0]);
                        double longitude = Double.parseDouble(latLon[1]);
                        ObjectPoint point = new ObjectPoint(latitude, longitude, 0); // Предполагаем, что z = 0
                        coordinates.put(index++, point);
                    }
                }
            }
                return coordinates;

    } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private LinkedHashMap<Integer, ObjectPoint> parsingCommunicationCoords(String jsonResponse) {
        LinkedHashMap<Integer, ObjectPoint> coordinates = new LinkedHashMap<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonResponse);
            JsonNode dataNode = rootNode.path("data");
            if (dataNode.isArray() && dataNode.size() > 0) {
                JsonNode firstItem = dataNode.get(0);
                String shape = firstItem.path("shape").asText();
                if (shape != null && !shape.isEmpty()) {
                    // Пример парсинга координат из строки shape
                    String polygonCoords = shape.replace("LINESTRING (", "").replace(")", "");
                    String[] coordsArray = polygonCoords.split(", ");
                    int index = 1;
                    for (String coord : coordsArray) {
                        String[] latLon = coord.split(" ");
                        double latitude = Double.parseDouble(latLon[0]);
                        double longitude = Double.parseDouble(latLon[1]);
                        ObjectPoint point = new ObjectPoint(latitude, longitude, 0); // Предполагаем, что z = 0
                        coordinates.put(index++, point);
                    }
                }
            }
            return coordinates;

        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    }