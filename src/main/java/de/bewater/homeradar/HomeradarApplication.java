package de.bewater.homeradar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import de.bewater.homeradar.config.ScanConfig;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(ScanConfig.class)
public class HomeradarApplication {

	public static void main(String[] args) {
		SpringApplication.run(HomeradarApplication.class, args);
	}

}
