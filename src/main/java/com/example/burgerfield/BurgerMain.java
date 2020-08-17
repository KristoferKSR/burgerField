package com.example.burgerfield;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.*;

@Controller
@SpringBootApplication
public class BurgerMain {

	@RequestMapping("/")
	@ResponseBody
	String home() {
		return "Hello World xoxo";
	}

	public static void main(String[] args) {
		SpringApplication.run(BurgerMain.class, args);
	}
}