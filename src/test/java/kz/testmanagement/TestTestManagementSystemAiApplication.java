package kz.testmanagement;

import org.springframework.boot.SpringApplication;

public class TestTestManagementSystemAiApplication {

	public static void main(String[] args) {
		SpringApplication.from(TestManagementSystemAiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
