package ru.zaomurom.applicationzao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ApplicationZaoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApplicationZaoApplication.class, args);
	}

}
