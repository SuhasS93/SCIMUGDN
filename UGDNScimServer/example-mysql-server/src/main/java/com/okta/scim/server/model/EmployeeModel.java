package com.okta.scim.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "employee")
public class EmployeeModel {
	private static final long serialVersionUID = 1L;
	private int empId;
	@Id
	@Column(name = "uid_no")
	private int uid_no;

	@Column(name = "old_ecode")
	private int old_ecode;
	
	@Column(name = "bus_name")
	private String bus_name;
	
	@Column(name = "first_name")
	private String first_name;
	
	@Column(name = "middle_name")
	private String middle_name;
	
	@Column(name = "last_name")
	private String last_name;
	
	@Column(name = "display_name")
	private String display_name;
	
	@Column(name = "desig")
	private String desig;
	
	@Column(name = "dept")
	private String dept;
	
	@Column(name = "phone")
	private String phone;
	
	@Column(name = "mobile")
	private String mobile;
	
	@Column(name = "mail_id")
	private String mail_id;
	
	@Column(name = "blood_group")
	private String blood_group;
	
	@Column(name = "date_birth")
	private String date_birth;
	
	@Column(name = "date_join")
	private String date_join;
	
	@Column(name = "date_left")
	private String date_left;
	
	@Column(name = "loc_name")
	private String loc_name;
	
	@Column(name = "country_name")
	private String country_name;
	
	@Column(name = "company_code")
	private String company_code;
	
	@Column(name = "emp_catg")
	private String emp_catg;
	
	@Column(name = "region_name")
	private String region_name;
	
	@Column(name = "sub_area")
	private String sub_area;
	
	@Column(name = "function")
	private String function;
	
	@Column(name = "sub_function")
	private String sub_function;
	
	@Column(name = "cost_centre")
	private String cost_centre;
	
	@Column(name = "hr_band")
	private String hr_band;
	
	@Column(name = "active_flag")
	private String active_flag;
	
	@Column(name = "hod_id")
	private int hod_id;
	
	@Column(name = "hod_name")
	private String hod_name;
	
	@Column(name = "hod_mail_id")
	private String hod_mail_id;
	
	@Column(name = "hod_desig")
	private String hod_desig;

	public int getEmpId() {
		return empId;
	}

	public void setEmpId(int empId) {
		this.empId = empId;
	}

	public int getUid_no() {
		return uid_no;
	}

	public void setUid_no(int uid_no) {
		this.uid_no = uid_no;
	}

	public int getOld_ecode() {
		return old_ecode;
	}

	public void setOld_ecode(int old_ecode) {
		this.old_ecode = old_ecode;
	}

	public String getBus_name() {
		return bus_name;
	}

	public void setBus_name(String bus_name) {
		this.bus_name = bus_name;
	}

	public String getFirst_name() {
		return first_name;
	}

	public void setFirst_name(String first_name) {
		this.first_name = first_name;
	}

	public String getMiddle_name() {
		return middle_name;
	}

	public void setMiddle_name(String middle_name) {
		this.middle_name = middle_name;
	}

	public String getLast_name() {
		return last_name;
	}

	public void setLast_name(String last_name) {
		this.last_name = last_name;
	}

	public String getDisplay_name() {
		return display_name;
	}

	public void setDisplay_name(String display_name) {
		this.display_name = display_name;
	}

	public String getDesig() {
		return desig;
	}

	public void setDesig(String desig) {
		this.desig = desig;
	}

	public String getDept() {
		return dept;
	}

	public void setDept(String dept) {
		this.dept = dept;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getMail_id() {
		return mail_id;
	}

	public void setMail_id(String mail_id) {
		this.mail_id = mail_id;
	}

	public String getBlood_group() {
		return blood_group;
	}

	public void setBlood_group(String blood_group) {
		this.blood_group = blood_group;
	}

	public String getDate_birth() {
		return date_birth;
	}

	public void setDate_birth(String date_birth) {
		this.date_birth = date_birth;
	}

	public String getDate_join() {
		return date_join;
	}

	public void setDate_join(String date_join) {
		this.date_join = date_join;
	}

	public String getDate_left() {
		return date_left;
	}

	public void setDate_left(String date_left) {
		this.date_left = date_left;
	}

	public String getLoc_name() {
		return loc_name;
	}

	public void setLoc_name(String loc_name) {
		this.loc_name = loc_name;
	}

	public String getCountry_name() {
		return country_name;
	}

	public void setCountry_name(String country_name) {
		this.country_name = country_name;
	}

	public String getCompany_code() {
		return company_code;
	}

	public void setCompany_code(String company_code) {
		this.company_code = company_code;
	}

	public String getEmp_catg() {
		return emp_catg;
	}

	public void setEmp_catg(String emp_catg) {
		this.emp_catg = emp_catg;
	}

	public String getRegion_name() {
		return region_name;
	}

	public void setRegion_name(String region_name) {
		this.region_name = region_name;
	}

	public String getSub_area() {
		return sub_area;
	}

	public void setSub_area(String sub_area) {
		this.sub_area = sub_area;
	}

	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public String getSub_function() {
		return sub_function;
	}

	public void setSub_function(String sub_function) {
		this.sub_function = sub_function;
	}

	public String getCost_centre() {
		return cost_centre;
	}

	public void setCost_centre(String cost_centre) {
		this.cost_centre = cost_centre;
	}

	public String getHr_band() {
		return hr_band;
	}

	public void setHr_band(String hr_band) {
		this.hr_band = hr_band;
	}

	public String getActive_flag() {
		return active_flag;
	}

	public void setActive_flag(String active_flag) {
		this.active_flag = active_flag;
	}

	public int getHod_id() {
		return hod_id;
	}

	public void setHod_id(int hod_id) {
		this.hod_id = hod_id;
	}

	public String getHod_name() {
		return hod_name;
	}

	public void setHod_name(String hod_name) {
		this.hod_name = hod_name;
	}

	public String getHod_mail_id() {
		return hod_mail_id;
	}

	public void setHod_mail_id(String hod_mail_id) {
		this.hod_mail_id = hod_mail_id;
	}

	public String getHod_desig() {
		return hod_desig;
	}

	public void setHod_desig(String hod_desig) {
		this.hod_desig = hod_desig;
	}

}
