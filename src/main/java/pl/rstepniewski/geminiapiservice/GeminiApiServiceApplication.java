package pl.rstepniewski.geminiapiservice;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GeminiApiServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GeminiApiServiceApplication.class, args);
	}

	@Bean
	public CommandLineRunner run(GeminiApiServiceRunner runner) {
		return args -> runner.execute();
	}
}