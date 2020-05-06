package com.upl.scimserver.service;

import java.util.List;

import com.upl.scimserver.model.EmployeeModel;
import com.upl.scimserver.model.EmployeeUpdateModel;
import com.upl.scimserver.model.UpdateResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface UGDNAPI {

	@Headers({ "Accept: application/json" })
	@POST("ugdnsync")
	Call<List<EmployeeModel>> getAllEmployees(@Query("fullList") String fullList,@Query("appCode") String appCode);

	@Headers({ "Accept: application/json" })
	@POST("ugdnsync")
	Call<List<EmployeeModel>> getNewEmployees(@Query("newOnly") String fullList,@Query("appCode") String appCode);

	@Headers({ "Accept: application/json" })
	@POST("ugdnsync")
	Call<List<EmployeeModel>> getUpdateEmployees(@Query("updateOnly") String fullList,@Query("appCode") String appCode);

	@Headers({ "Accept: application/json" })
	@POST("ugdnsync")
	Call<List<EmployeeModel>> getDeleteEmployees(@Query("deleteOnly") String fullList,@Query("appCode") String appCode);

	@Headers({ "Accept: application/json" })
	@POST("ugdnsync/pages")
	Call<List<EmployeeModel>> getPaginationDetail(@Query("page") String page,@Query("size") String size,@Query("filterquery") String filterquery,@Query("appcode") String appcode);

	@Headers({ "Accept: application/json" })
	@POST("ugdnsync/syncresponse")
	Call<ResponseBody> updateSyncResponse(@Query("appCode") String appcode, @Body List<UpdateResponse> response);
	
	@Headers({ "Accept: application/json" })
	@POST("syncoktatougdn")
	Call<List<EmployeeUpdateModel>> updateOktaToUGDNResponse(@Body List<EmployeeUpdateModel> employee);
	
	@Headers({ "Accept: application/json" })
	@POST("updateSFData")
	Call<EmployeeUpdateModel> updateSFData(@Body EmployeeModel employee);
	
	@Headers({ "Accept: application/json" })
	@POST("addSFData")
	Call<EmployeeUpdateModel> addSFData(@Body EmployeeModel employee);
}
