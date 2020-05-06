package com.upl.scimserver.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

@Entity
@Table(name = "employee")
public class EmployeeModel {
	public static final int STATUS_ACTIVE = 1;
	public static final int STATUS_INACTIVE = 0;
	
	public static final int SYNC_DONE=3;
	public static final int SYNC_PENDING_UPDATE=1;
	public static final int SYNC_PENDING_INSERT=2;
	
	private static final long serialVersionUID = 1L;
	//private int empId;

	@Id
	@Column(name = "uid_no")
	@SerializedName("uid_no")
	private String uidNo;

	@Column(name = "old_ecode")
	@SerializedName("old_ecode")
	private String oldEcode;

	@Column(name = "bus_name")
	@SerializedName("bus_name")
	private String busName;

	@Column(name = "first_name")
	@SerializedName("first_name")
	private String firstName;

	@Column(name = "middle_name")
	@SerializedName("middle_name")
	private String middleName;

	@Column(name = "last_name")
	@SerializedName("last_name")
	private String lastName;

	@Column(name = "display_name")
	@SerializedName("display_name")
	private String displayName;

	@Column(name = "desig")
	@SerializedName("desig")
	private String desig;

	@Column(name = "dept")
	@SerializedName("dept")
	private String dept;

	@Column(name = "phone")
	@SerializedName("phone")
	private String phone;

	@Column(name = "mobile")
	@SerializedName("mobile")
	private String mobile;

	@Column(name = "mail_id")
	@SerializedName("mail_id")
	private String mailId;

	@Column(name = "blood_group")
	@SerializedName("blood_group")
	private String bloodGroup;

	@Column(name = "date_birth")
	@SerializedName("date_birth")
	private String dateBirth;

	@Column(name = "date_join")
	@SerializedName("date_join")
	private String dateJoin;

	@Column(name = "date_left")
	@SerializedName("date_left")
	private String dateLeft;

	@Column(name = "loc_name")
	@SerializedName("loc_name")
	private String locName;

	@Column(name = "country_name")
	@SerializedName("country_name")
	private String countryName;

	@Column(name = "company_code")
	@SerializedName("company_code")
	private String companyCode;

	@Column(name = "emp_catg")
	@SerializedName("emp_catg")
	private String empCatg;

	@Column(name = "region_name")
	@SerializedName("region_name")
	private String regionName;

	@Column(name = "sub_area")
	@SerializedName("sub_area")
	private String subArea;

	@Column(name = "emp_function")
	@SerializedName("function")
	private String function;

	@Column(name = "sub_function")
	@SerializedName("sub_function")
	private String subFunction;

	@Column(name = "cost_centre")
	@SerializedName("cost_centre")
	private String costCentre;

	@Column(name = "hr_band")
	@SerializedName("hr_band")
	private String hrBand;

	@Column(name = "active_flag")
	@SerializedName("active_flag")
	private String activeFlag;

	@Column(name = "hod_id")
	@SerializedName("hod_id")
	private String hodId;

	@Column(name = "hod_name")
	@SerializedName("hod_name")
	private String hodName;

	@Column(name = "hod_mail_id")
	@SerializedName("hod_mail_id")	
	private String hodMailId;

	@Column(name = "hod_desig")
	@SerializedName("hod_desig")
	private String hodDesig;

	@Column(name="status")
	@SerializedName("status")
	private Integer status;
	
	@Column(name="backwardsync")
	@SerializedName("backwardsync")
	private Integer backwardSync;
	
	@Column(name="source")
	@SerializedName("source")
	private String source;
	
	@Column(name="hr_represent")
	@SerializedName("hr_represent")
	private String hrRepresent;
	
	@Column(name="business_unit")
	@SerializedName("bu")
	private String businessUnit;
	
	private String ack;
	
	@Column(name = "failedcount",columnDefinition="int(10) default 0")
	@SerializedName("failedcount")
	private Integer failedCount;

	@Column(name = "failreason")
	@SerializedName("failreason")
	private String failReason;

	
	public String getAck() {
		return ack;
	}

	public void setAck(String ack) {
		this.ack = ack;
	}
	
	@JsonProperty("uid_no")
	public String getUidNo() {
		return uidNo;
	}

	public void setUidNo(String uidNo) {
		this.uidNo = uidNo;
	}

	public Integer getBackwardSync() {
		return backwardSync;
	}

	public void setBackwardSync(Integer backwardSync) {
		this.backwardSync = backwardSync;
	}

	public String getSource() {
		return source;
	}

	@JsonProperty("source")
	public void setSource(String source) {
		this.source = source;
	}

	@JsonProperty("old_ecode")
	public String getOldEcode() {
		return oldEcode;
	}

	public void setOldEcode(String oldEcode) {
		this.oldEcode = oldEcode;
	}

	@JsonProperty("bus_name")
	public String getBusName() {
		return busName;
	}

	public void setBusName(String busName) {
		this.busName = busName;
	}

	@JsonProperty("first_name")
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@JsonProperty("middle_name")
	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	@JsonProperty("last_name")
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@JsonProperty("display_name")
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@JsonProperty("desig")
	public String getDesig() {
		return desig;
	}

	public void setDesig(String desig) {
		this.desig = desig;
	}

	@JsonProperty("dept")
	public String getDept() {
		return dept;
	}

	public void setDept(String dept) {
		this.dept = dept;
	}

	@JsonProperty("phone")
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	@JsonProperty("mobile")
	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	@JsonProperty("mail_id")
	public String getMailId() {
		return mailId;
	}

	public void setMailId(String mailId) {
		this.mailId = mailId;
	}
	
	@JsonProperty("blood_group")
	public String getBloodGroup() {
		return bloodGroup;
	}

	public void setBloodGroup(String bloodGroup) {
		this.bloodGroup = bloodGroup;
	}
	
	@JsonProperty("date_birth")
	public String getDateBirth() {
		return dateBirth;
	}

	public void setDateBirth(String dateBirth) {
		this.dateBirth = dateBirth;
	}
	
	@JsonProperty("date_join")
	public String getDateJoin() {
		return dateJoin;
	}

	public void setDateJoin(String dateJoin) {
		this.dateJoin = dateJoin;
	}

	@JsonProperty("date_left")
	public String getDateLeft() {
		return dateLeft;
	}

	public void setDateLeft(String dateLeft) {
		this.dateLeft = dateLeft;
	}

	@JsonProperty("loc_name")
	public String getLocName() {
		return locName;
	}

	public void setLocName(String locName) {
		this.locName = locName;
	}

	@JsonProperty("country_name")
	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	@JsonProperty("company_code")
	public String getCompanyCode() {
		return companyCode;
	}

	public void setCompanyCode(String companyCode) {
		this.companyCode = companyCode;
	}

	@JsonProperty("emp_catg")
	public String getEmpCatg() {
		return empCatg;
	}

	public void setEmpCatg(String empCatg) {
		this.empCatg = empCatg;
	}

	@JsonProperty("region_name")
	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	@JsonProperty("sub_area")
	public String getSubArea() {
		return subArea;
	}

	public void setSubArea(String subArea) {
		this.subArea = subArea;
	}

	@JsonProperty("function")
	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	@JsonProperty("sub_function")
	public String getSubFunction() {
		return subFunction;
	}

	public void setSubFunction(String subFunction) {
		this.subFunction = subFunction;
	}

	@JsonProperty("cost_centre")
	public String getCostCentre() {
		return costCentre;
	}

	public void setCostCentre(String costCentre) {
		this.costCentre = costCentre;
	}

	@JsonProperty("hr_band")
	public String getHrBand() {
		return hrBand;
	}

	public void setHrBand(String hrBand) {
		this.hrBand = hrBand;
	}

	@JsonProperty("active_flag")
	public String getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(String activeFlag) {
		this.activeFlag = activeFlag;
	}

	@JsonProperty("hod_id")
	public String getHodId() {
		return hodId;
	}

	public void setHodId(String hodId) {
		this.hodId = hodId;
	}

	public String getHodName() {
		return hodName;
	}

	public void setHodName(String hodName) {
		this.hodName = hodName;
	}

	public String getHodMailId() {
		return hodMailId;
	}

	public void setHodMailId(String hodMailId) {
		this.hodMailId = hodMailId;
	}

	public String getHodDesig() {
		return hodDesig;
	}

	public void setHodDesig(String hodDesig) {
		this.hodDesig = hodDesig;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public static int getStatusActive() {
		return STATUS_ACTIVE;
	}

	public static int getStatusInactive() {
		return STATUS_INACTIVE;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	@JsonProperty("hr_represent")
	public String getHrRepresent() {
		return hrRepresent;
	}

	public void setHrRepresent(String hrRepresent) {
		this.hrRepresent = hrRepresent;
	}

	@JsonProperty("bu")
	public String getBusinessUnit() {
		return businessUnit;
	}

	public void setBusinessUnit(String businessUnit) {
		this.businessUnit = businessUnit;
	}

	public Integer getFailedCount() {
		return failedCount;
	}

	public void setFailedCount(Integer failedCount) {
		this.failedCount = failedCount;
	}

	public String getFailReason() {
		return failReason;
	}

	public void setFailReason(String failReason) {
		this.failReason = failReason;
	}

	@Override
	public String toString() {
		return "EmployeeModel [uidNo=" + uidNo + ", oldEcode=" + oldEcode + ", busName=" + busName + ", firstName="
				+ firstName + ", middleName=" + middleName + ", lastName=" + lastName + ", displayName=" + displayName
				+ ", desig=" + desig + ", dept=" + dept + ", phone=" + phone + ", mobile=" + mobile + ", mailId="
				+ mailId + ", bloodGroup=" + bloodGroup + ", dateBirth=" + dateBirth + ", dateJoin=" + dateJoin
				+ ", dateLeft=" + dateLeft + ", locName=" + locName + ", countryName=" + countryName + ", companyCode="
				+ companyCode + ", empCatg=" + empCatg + ", regionName=" + regionName + ", subArea=" + subArea
				+ ", function=" + function + ", subFunction=" + subFunction + ", costCentre=" + costCentre + ", hrBand="
				+ hrBand + ", activeFlag=" + activeFlag + ", hodId=" + hodId + ", hodName=" + hodName + ", hodMailId="
				+ hodMailId + ", hodDesig=" + hodDesig + ", status=" + status + ", backwardSync=" + backwardSync
				+ ", source=" + source + ", ack=" + ack + ", hr_represent="+ hrRepresent+", business_unit="+businessUnit+"]";
	}

	
	
}
