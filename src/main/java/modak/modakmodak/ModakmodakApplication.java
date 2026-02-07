package modak.modakmodak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class ModakmodakApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModakmodakApplication.class, args);
	}

}
