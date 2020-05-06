package com.upl.scimserver.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.upl.scimserver.model.EmployeeModel;
import com.upl.scimserver.model.EmployeeUpdateModel;
import com.upl.scimserver.model.UpdateResponse;
import com.upl.scimserver.repository.EmployeeRepository;
import com.upl.scimserver.service.EmployeeService;

import retrofit2.Response;

@Component
public class MainController {

	private static final Logger logger = LoggerFactory.getLogger(MainController.class);
	private static final String STATUS_SUCCESS = "success";
	private static final String STATUS_PENDING = "pending";
	private static final String STATUS_FAILED = "failed";

	private static final String SOURCE_SF = "SuccessFactors";
	private static final String SOURCE_UGDN = "UGDN";

	@Autowired
	EmployeeService employeeService;

	@Autowired
	EmployeeRepository employeeRepository;

	@Scheduled(fixedDelay = 60000)
	public void allUGDNApicall() throws Exception {
		try {
			if (employeeRepository.count() == 0) {
				getAllUsers();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		try {getNewOnlyUsers();}catch (Exception e) {logger.error("Error while new User Sync",e);}
		try {updateUsersOnly();}catch (Exception e) {logger.error("Error while Update User Sync",e);}
		try {deleteUsersOnly();}catch (Exception e) {logger.error("Error while Delete User Sync",e);}

		try {updateSFUsers();}catch (Exception e) {logger.error("Error while SF Update User Sync",e);}
		try {addSFUsers();}catch (Exception e) {logger.error("Error while SF New User Sync",e);}
		try {updateUGDNUsersData();}catch (Exception e) {logger.error("Error while Update UGDN User Data",e);}
	}

	public void updateUGDNUsersData() throws IOException {
		logger.info("In Update Source_UGDN User");
		List<EmployeeModel> updateEmployeeList = employeeRepository.findBySourceAndBackwardSyncAndFailedCountLessThanEqual(SOURCE_UGDN,
				EmployeeModel.SYNC_PENDING_UPDATE,3);
		
		logger.info("Total {} users selected to be updated back email to ugdn ",updateEmployeeList.size());
		List<EmployeeUpdateModel> updateEmployeeModellist = new ArrayList<EmployeeUpdateModel>();

		updateEmployeeList.forEach(employee -> {
			updateEmployeeModellist.add(new EmployeeUpdateModel(employee.getUidNo(), employee.getMailId()));
		});
		List<EmployeeUpdateModel> updatedEmployeeList = new ArrayList<>();

		if (!updateEmployeeModellist.isEmpty()) {
			updatedEmployeeList = employeeService.updateOktaToUGDNResponse(updateEmployeeModellist);
		}

		for (EmployeeUpdateModel employeeUpdateModel : updatedEmployeeList) {
			if (employeeUpdateModel.getAck().equals(STATUS_SUCCESS)) {
				employeeRepository.setBackwardSyncStatus(EmployeeModel.SYNC_DONE, employeeUpdateModel.getUid_no());
				logger.info("Update Source_UGDN Status done..!!");
			}
			if (employeeUpdateModel.getAck().equals(STATUS_FAILED)) {
				Optional<EmployeeModel> failedEmployee = employeeRepository.findById(employeeUpdateModel.getUid_no());
				int failedStatus = failedEmployee.get().getFailedCount() + 1;
				String failedReson = employeeUpdateModel.getUid_no() + "," + employeeUpdateModel.getMail_id() + ", " + employeeUpdateModel.getAck();
				employeeRepository.setFailedStatusWithReson(failedStatus, failedReson,employeeUpdateModel.getUid_no());
				logger.info("Filed Source_SF updated..!!");
			}
		}
	}

	public void addSFUsers() {
		logger.info("In AddSF User");
		List<EmployeeModel> updateEmployeeList = employeeRepository.findBySourceAndBackwardSyncAndFailedCountLessThanEqual(SOURCE_SF,
				EmployeeModel.SYNC_PENDING_INSERT,3);

		updateEmployeeList.forEach(employee -> {
			try {
				if (employee.getFailedCount() <= 3) {
					employee.setBackwardSync(null);
					employee.setSource(null);
					employee.setHodName(null);
					employee.setHodMailId(null);
					employee.setHodDesig(null);
					employee.setStatus(null);
					employee.setFailedCount(null);
					employee.setFailReason(null);
					logger.debug("EmployeeModel: " + employee);
					EmployeeUpdateModel updatedEmployee = employeeService.addSFData(employee);
					if (updatedEmployee.getAck().equals(STATUS_SUCCESS)) {
						employeeRepository.setBackwardSyncStatus(EmployeeModel.SYNC_DONE, updatedEmployee.getUid_no());
						logger.info("Insert Source_SF Status done..!!");
					}
					if (updatedEmployee.getAck().equals(STATUS_FAILED)) {
						Optional<EmployeeModel> failedEmployee = employeeRepository.findById(updatedEmployee.getUid_no());
						int failedStatus = failedEmployee.get().getFailedCount() + 1;
						String failedReson = updatedEmployee.getUid_no() + "," + updatedEmployee.getMail_id() + ", " + updatedEmployee.getAck();
						employeeRepository.setFailedStatusWithReson(failedStatus, failedReson,updatedEmployee.getUid_no());
						logger.info("Filed Source_SF updated..!!");
					}
				}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}

		});
	}

	public void updateSFUsers() {
		logger.info("In UpdateSF User");
		List<EmployeeModel> updateEmployeeList = employeeRepository.findBySourceAndBackwardSyncAndFailedCountLessThanEqual(SOURCE_SF,
				EmployeeModel.SYNC_PENDING_UPDATE,3);

		logger.info("Total employees to update" + updateEmployeeList.size());
		for (EmployeeModel employee : updateEmployeeList) {
			try {
				employee.setBackwardSync(null);
				employee.setSource(null);
				employee.setHodName(null);
				employee.setHodMailId(null);
				employee.setHodDesig(null);
				employee.setStatus(null);
				employee.setFailedCount(null);
				employee.setFailReason(null);
				logger.info("employee: " + employee);
				EmployeeUpdateModel updatedEmployee = employeeService.updateSFData(employee);
				if (updatedEmployee.getAck().equals(STATUS_SUCCESS)) {
					employeeRepository.setBackwardSyncStatus(EmployeeModel.SYNC_DONE, updatedEmployee.getUid_no());
					logger.info("Update_Source_SF Status done..!!");
				}
				if (updatedEmployee.getAck().equals(STATUS_FAILED)) {
					Optional<EmployeeModel> failedEmployee = employeeRepository.findById(updatedEmployee.getUid_no());
					int failedStatus = failedEmployee.get().getFailedCount() + 1;
					String failedReson = updatedEmployee.getUid_no() + "," + updatedEmployee.getMail_id() + ", " + updatedEmployee.getAck();
					employeeRepository.setFailedStatusWithReson(failedStatus, failedReson,updatedEmployee.getUid_no());
					logger.info("Filed Source_SF updated..!!");
				}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	public void getAllUsers() throws Exception {
		logger.info("In getAllUsers Method.");
		List<EmployeeModel> employeeModel;
		employeeModel = employeeService.getAllEmployees();

		insertEmployees(employeeModel);

		int i = 2;

		while (i != 0) {
			Response<List<EmployeeModel>> responseBody;
			employeeModel = new ArrayList<>();
			responseBody = employeeService.getPaginationDetail(String.valueOf(i), "fullList");
			employeeModel = responseBody.body();
			String linkUrl = responseBody.headers().get("Link");

			if (linkUrl != null && linkUrl != "") {
				logger.info("-------------------------------");
				logger.info("Page Id of getAllUsers " + i);
				logger.info("-------------------------------");
				i = Integer.valueOf(linkUrl.split("=")[1].split("&")[0]);
				insertEmployees(employeeModel);
			} else {
				logger.info("Last Page Id of getAllUsers" + i);
				break;
			}
		}
		logger.info(" End GetAllUsers Method.");
	}

	public void getNewOnlyUsers() throws Exception {
		logger.info("In GetNewOnlyUsers Method.");
		List<EmployeeModel> employeeModel;
		employeeModel = employeeService.getNewOnlyEmployees();

		insertEmployees(employeeModel);

		int i = 2;
		while (i != 0) {
			Response<List<EmployeeModel>> responseBody;
			employeeModel = new ArrayList<>();
			responseBody = employeeService.getPaginationDetail(String.valueOf(i), "newOnly");
			employeeModel = responseBody.body();
			String linkUrl = responseBody.headers().get("Link");

			if (linkUrl != null && !linkUrl.equals("")) {
				logger.info("Page Id of New Users " + i);
				i = Integer.valueOf(linkUrl.split("=")[1].split("&")[0]);
				insertEmployees(employeeModel);
			} else {
				logger.info("Last Page Id of New Users" + i);
				break;
			}
		}
		logger.info(" End GetNewOnlyUsers Method.");
	}

	public void updateUsersOnly() throws Exception {
		logger.info("In UpdateUserOnly Method.");
		List<EmployeeModel> employeeModel;
		employeeModel = employeeService.getUpdateOnlyEmployees();
		updateEmployees(employeeModel);

		int i = 2;
		while (i != 0) {
			Response<List<EmployeeModel>> responseBody;
			responseBody = employeeService.getPaginationDetail(String.valueOf(i), "updateOnly");
			employeeModel = responseBody.body();
			String linkUrl = responseBody.headers().get("Link");

			if (linkUrl != null && !"".equals(linkUrl)) {
				logger.info("Page Id of Update Users " + i);
				i = Integer.valueOf(linkUrl.split("=")[1].split("&")[0]);
				updateEmployees(employeeModel);
			} else {
				logger.info("Last Page Id of Update Users" + i);
				break;
			}
		}
		logger.info("End UpdateUserOnly Method.");
	}

	public void deleteUsersOnly() throws Exception {
		List<EmployeeModel> employeeModel;
		employeeModel = employeeService.getDeleteOnlyEmployees();
		removeEmployees(employeeModel);

		int i = 2;
		while (i != 0) {
			Response<List<EmployeeModel>> responseBody;
			employeeModel = new ArrayList<>();
			responseBody = employeeService.getPaginationDetail(String.valueOf(i), "deleteOnly");
			employeeModel = responseBody.body();
			String linkUrl = responseBody.headers().get("Link");

			if (linkUrl != null && !linkUrl.equals("")) {
				logger.info("Page Id of Update Users " + i);
				i = Integer.valueOf(linkUrl.split("=")[1].split("&")[0]);
				removeEmployees(employeeModel);
			} else {
				logger.info("Last Page Id of Update Users" + i);
				break;
			}
		}
	}

	public void insertEmployees(List<EmployeeModel> employeelist) throws IOException {
		logger.info("In Save Employee Method");
		List<EmployeeModel> employeeModels = new ArrayList<>();

		for (EmployeeModel employeeModel : employeelist) {
			logger.debug(" " + employeeModel);
			if (!employeeRepository.existsById(employeeModel.getUidNo())) {
				employeeModel.setSource(SOURCE_UGDN);
				employeeModel.setStatus(EmployeeModel.STATUS_ACTIVE);
				employeeModels.add(employeeModel);
			}
		}
		List<EmployeeModel> savedList = employeeRepository.saveAll(employeeModels);
		updateSyncResponse(savedList);
	}

	public void updateEmployees(List<EmployeeModel> employeelist) throws IOException {
		logger.info("In Update Employee Method");

		for (EmployeeModel employeeModel : employeelist) {
			logger.debug(" " + employeeModel);
			EmployeeModel employeeModel2 = employeeRepository.findById(employeeModel.getUidNo()).get();
			employeeModel.setSource(employeeModel2.getSource());
		}
		List<EmployeeModel> savedList = employeeRepository.saveAll(employeelist);
		updateSyncResponse(savedList);
	}

	public void removeEmployees(List<EmployeeModel> employeeList) throws IOException {
		logger.info("In Update Employee Method");
		for (EmployeeModel employeeModel : employeeList) {
			logger.debug(" " + employeeModel);
			employeeModel.setStatus(EmployeeModel.STATUS_INACTIVE);
			employeeModel.setActiveFlag("NO");
		}
		List<EmployeeModel> savedList = employeeRepository.saveAll(employeeList);
		updateSyncResponse(savedList);
	}

	public void updateSyncResponse(List<EmployeeModel> savedList) throws IOException {
		List<UpdateResponse> responses = new ArrayList<>();
		savedList.forEach(emp -> {
			UpdateResponse response = new UpdateResponse();
			response.setUid_no(emp.getUidNo());
			response.setStatus(STATUS_SUCCESS);
			responses.add(response);
		});
		employeeService.updateSyncResponse(responses);
	}
}
