package org.sprain.ai;

import org.springframework.ai.model.ollama.autoconfigure.OllamaEmbeddingAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication(exclude = {OllamaEmbeddingAutoConfiguration.class})
@SpringBootApplication
public class SprainApplication {

    public static void main(String[] args) {
        SpringApplication.run(SprainApplication.class, args);
    }

}
