package com.upl.scimserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upl.scimserver.model.EmployeeModel;
import com.upl.scimserver.model.EmployeeUpdateModel;
import com.upl.scimserver.repository.EmployeeRepository;
import com.upl.scimserver.service.EmployeeService;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties
@Configuration
public class UgdnApplication extends SpringBootServletInitializer {
	Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	@Autowired
	EmployeeService employeeService;
	
	@Autowired
	EmployeeRepository repository;
	
	@Value("${ugdn.api.url}")
	public String apiurl;
	
	public static void main(String[] args) {
		SpringApplication.run(UgdnApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void initializeSmartOrderRouterApp() throws IOException
	{
		LOGGER.debug("API BASE : "+apiurl);
		
	}

	public String getApiurl() {
		return apiurl;
	}

	public void setApiurl(String apiurl) {
		this.apiurl = apiurl;
	}

}
