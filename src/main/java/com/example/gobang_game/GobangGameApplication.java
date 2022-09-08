package com.example.gobang_game;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class GobangGameApplication {
	public static ConfigurableApplicationContext context;
	public static void main(String[] args) {
		context = SpringApplication.run(GobangGameApplication.class, args);
	}

}
