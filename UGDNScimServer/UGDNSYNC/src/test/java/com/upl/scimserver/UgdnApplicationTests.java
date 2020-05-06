package com.upl.scimserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.upl.scimserver.model.EmployeeUpdateModel;
import com.upl.scimserver.service.EmployeeService;
import com.upl.scimserver.service.EmployeeServiceImpl;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("unittest")
public class UgdnApplicationTests {

	private static final Logger logger = LoggerFactory.getLogger(EmployeeServiceImpl.class);
	@Autowired
	EmployeeService employeeService;
	
	@Test
	public void contextLoads() 
	{
	}
	
	@Test
	public void employeeUpdateTest() throws IOException
	{
//		List<EmployeeUpdateModel> employee =new ArrayList<EmployeeUpdateModel>();
//		employee.add(new EmployeeUpdateModel("20000002", "rajesh.khanolkar123@uniphos.com"));
//		employee.add(new EmployeeUpdateModel("20000001", "kirit.macwan123@uniphos.com"));
//		
//		List<EmployeeUpdateModel> response=employeeService.updateOktaToUGDNResponse(employee);
//		logger.debug("Update OKTA to UGDN "+response);
		
		
	}

}
