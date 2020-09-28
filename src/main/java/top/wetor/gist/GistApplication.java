package top.wetor.gist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
public class GistApplication {
    public static void main(String[] args) {
        SpringApplication.run(GistApplication.class, args);
    }
}
