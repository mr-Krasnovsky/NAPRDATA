package CadastrApp;

import org.opengis.referencing.FactoryException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;


@SpringBootApplication
public class Main {

    public static void main(String[] args) throws FactoryException, IOException {

                SpringApplication.run(Main.class, args);

    }
}
