package fr.saintmichel.sirene;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO) // format JSON stable pour les pages de résultats
public class SireneApplication {

    public static void main(String[] args) {
        SpringApplication.run(SireneApplication.class, args);
    }
}
