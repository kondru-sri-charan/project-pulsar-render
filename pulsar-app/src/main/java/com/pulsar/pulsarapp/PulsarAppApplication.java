package main.java.com.pulsar.pulsarapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication
@EnableScheduling
public class PulsarAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(PulsarAppApplication.class, args);
	}

}
