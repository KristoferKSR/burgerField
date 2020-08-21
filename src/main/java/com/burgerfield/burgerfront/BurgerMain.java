package com.burgerfield.burgerfront;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.stereotype.*;

@Controller
@SpringBootApplication
public class BurgerMain extends SpringBootServletInitializer {

	//http://www.jsonschema2pojo.org/ used for object generation

	public static void main(String[] args) {
		SpringApplication.run(BurgerMain.class, args);

	}
}