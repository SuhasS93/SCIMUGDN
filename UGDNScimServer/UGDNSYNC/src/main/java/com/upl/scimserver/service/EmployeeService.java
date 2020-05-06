package com.upl.scimserver.service;

import java.io.IOException;
import java.util.List;

import com.upl.scimserver.model.EmployeeModel;
import com.upl.scimserver.model.EmployeeUpdateModel;
import com.upl.scimserver.model.UpdateResponse;

import retrofit2.Response;

public interface EmployeeService {
	 public List<EmployeeModel> getAllEmployees() throws IOException;	 
	 public List<EmployeeModel> getNewOnlyEmployees() throws IOException;	 
	 public List<EmployeeModel> getUpdateOnlyEmployees() throws IOException;	 
	 public List<EmployeeModel> getDeleteOnlyEmployees() throws IOException ;
	 public Response<List<EmployeeModel>> getPaginationDetail(String page, String filterquery) throws IOException;
	 public void updateSyncResponse(List<UpdateResponse> responses) throws IOException;
	 public List<EmployeeUpdateModel> updateOktaToUGDNResponse(List<EmployeeUpdateModel> employee) throws IOException;
	 public EmployeeUpdateModel addSFData(EmployeeModel employee) throws IOException;
	 public EmployeeUpdateModel updateSFData(EmployeeModel employee) throws IOException;
	 public void setBackwardSyncStatus(Integer status, String id);
}
