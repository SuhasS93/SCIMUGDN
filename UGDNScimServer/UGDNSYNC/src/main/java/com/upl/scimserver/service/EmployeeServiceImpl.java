package com.upl.scimserver.service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.upl.scimserver.model.EmployeeModel;
import com.upl.scimserver.model.EmployeeUpdateModel;
import com.upl.scimserver.model.UpdateResponse;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Service
public class EmployeeServiceImpl implements EmployeeService {

	private static final Logger logger = LoggerFactory.getLogger(EmployeeServiceImpl.class);
	static final String JSON_CONTENT_TYPE = "application/json";
	private SimpleDateFormat sdf_out = new SimpleDateFormat("dd-MMM-yy");
	private SimpleDateFormat sdf_short = new SimpleDateFormat("yyyy-MM-dd");
	UGDNAPI api;

	public EmployeeServiceImpl(@Value("${ugdn.api.url}") String apiUrl) {

		HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
		interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
		OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
		
		Retrofit retrofit = new Retrofit.Builder().baseUrl(apiUrl).client(client)
				.addConverterFactory(GsonConverterFactory.create()).build();

		api = retrofit.create(UGDNAPI.class);

	}

	@Override
	public List<EmployeeModel> getAllEmployees() throws IOException {
		logger.info("In Service -> GetAllEmployees Method.");
		Call<List<EmployeeModel>> retrofitCall = api.getAllEmployees("YES", "OKTA");

		Response<List<EmployeeModel>> response = retrofitCall.execute();

		if (!response.isSuccessful()) {
			throw new IOException(response.errorBody() != null ? response.errorBody().string() : "Unknown error");
		}

		return response.body();

	}

	@Override
	public List<EmployeeModel> getNewOnlyEmployees() throws IOException {
		logger.info("In Service -> getNewOnlyEmployees Method.");
		
		Call<List<EmployeeModel>> retrofitCall = api.getNewEmployees("YES", "OKTA");

		Response<List<EmployeeModel>> response = retrofitCall.execute();

		if (!response.isSuccessful()) {
			throw new IOException(response.errorBody() != null ? response.errorBody().string() : "Unknown error");
		}

		return response.body();
	}

	@Override
	public List<EmployeeModel> getUpdateOnlyEmployees() throws IOException {
		logger.info("In Service -> getUpdateOnlyEmployees Method.");
		Call<List<EmployeeModel>> retrofitCall = api.getUpdateEmployees("YES", "OKTA");

		Response<List<EmployeeModel>> response = retrofitCall.execute();

		if (!response.isSuccessful()) {
			throw new IOException(response.errorBody() != null ? response.errorBody().string() : "Unknown error");
		}

		return response.body();

	}

	@Override
	public List<EmployeeModel> getDeleteOnlyEmployees() throws IOException {
		logger.info("In Service -> getDeleteOnlyEmployees Method.");
		Call<List<EmployeeModel>> retrofitCall = api.getDeleteEmployees("YES", "OKTA");

		Response<List<EmployeeModel>> response = retrofitCall.execute();

		if (!response.isSuccessful()) {
			throw new IOException(response.errorBody() != null ? response.errorBody().string() : "Unknown error");
		}

		return response.body();

	}

	@Override
	public Response<List<EmployeeModel>> getPaginationDetail(String page, String filterquery) throws IOException {
		logger.info("In Service -> getPaginationDetail Method. page: {} filterQuery: {}",page,filterquery);
		Call<List<EmployeeModel>> retrofitCall = api.getPaginationDetail(page, "10", filterquery, "OKTA");

		Response<List<EmployeeModel>> response = retrofitCall.execute();
		//String link = response.headers().get("Link");

		if (!response.isSuccessful()) {
			throw new IOException(response.errorBody() != null ? response.errorBody().string() : "Unknown error");
		}

		return response;
	}

	@Override
	public void updateSyncResponse(List<UpdateResponse> responses) throws IOException {
		Call<ResponseBody> retrofitCall = api.updateSyncResponse("OKTA",responses);
		Response<ResponseBody> response = retrofitCall.execute();
		
		if(!response.isSuccessful()) {
			throw new IOException(response.errorBody() != null ? response.errorBody().string() : "Unknown error");
		}
	}

	@Override
	public List<EmployeeUpdateModel> updateOktaToUGDNResponse(List<EmployeeUpdateModel> employee) throws IOException {
		Call<List<EmployeeUpdateModel>> retrofitCall = api.updateOktaToUGDNResponse(employee);
		Response<List<EmployeeUpdateModel>> response = retrofitCall.execute();
		
		if(!response.isSuccessful()) {
			throw new IOException(response.errorBody() != null ? response.errorBody().string() : "Unknown error");
		}
		return response.body();
	}

	@Override
	public EmployeeUpdateModel addSFData(EmployeeModel employee) throws IOException {
		formatDates(employee);
		Call<EmployeeUpdateModel> retrofitCall = api.addSFData(employee);
		Response<EmployeeUpdateModel> response = retrofitCall.execute();
		
		if(!response.isSuccessful()) {
			throw new IOException(response.errorBody() != null ? response.errorBody().string() : "Unknown error");
		}
		return response.body();
		
	}
	
	private void formatDates(EmployeeModel employee) {
		Date birthDate=null;
		Date joinDate=null;
		Date leftDate=null;
		try {
			if(employee.getDateBirth()!=null && !"".equals(employee.getDateBirth()))
				birthDate = sdf_short.parse(employee.getDateBirth());
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
		}

		try {
			if(employee.getDateJoin()!=null && !"".equals(employee.getDateJoin()))
				joinDate = sdf_short.parse(employee.getDateJoin());
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
		}
		
		try {
			if(employee.getDateLeft()!=null && !"".equals(employee.getDateLeft()))
				leftDate = sdf_short.parse(employee.getDateLeft());
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
		}
		if(birthDate!=null) 
			employee.setDateBirth(sdf_out.format(birthDate));
		
		if(joinDate!=null)
			employee.setDateJoin(sdf_out.format(joinDate));
		
		if(leftDate!=null)
			employee.setDateLeft(sdf_out.format(leftDate));
	}

	@Override
	public EmployeeUpdateModel updateSFData(EmployeeModel employee) throws IOException {
		formatDates(employee);
		Call<EmployeeUpdateModel> retrofitCall = api.updateSFData(employee);
		Response<EmployeeUpdateModel> response = retrofitCall.execute();
		
		if(!response.isSuccessful()) {
			throw new IOException(response.errorBody() != null ? response.errorBody().string() : "Unknown error");
		}
		return response.body();
	}

	@Override
	public void setBackwardSyncStatus(Integer status, String id) {
		// TODO Auto-generated method stub
		
	}

}
