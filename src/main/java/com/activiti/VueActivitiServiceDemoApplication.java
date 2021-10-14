package com.activiti;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;


/**
 * @author crh
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class })
public class VueActivitiServiceDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(VueActivitiServiceDemoApplication.class, args);
	}

}
