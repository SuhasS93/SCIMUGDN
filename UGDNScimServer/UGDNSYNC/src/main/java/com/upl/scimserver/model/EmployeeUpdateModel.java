package com.upl.scimserver.model;

public class EmployeeUpdateModel 
{
	private String uid_no;
	private String mail_id;
	private String ack;
	
	public String getAck() {
		return ack;
	}


	public void setAck(String ack) {
		this.ack = ack;
	}


	public EmployeeUpdateModel(String uid_no,String mail_id)
	{
		this.uid_no = uid_no;
		this.mail_id = mail_id;
	}
	
	
	public String getUid_no() {
		return uid_no;
	}
	public void setUid_no(String uid_no) {
		this.uid_no = uid_no;
	}
	public String getMail_id() {
		return mail_id;
	}
	public void setMail_id(String mail_id) {
		this.mail_id = mail_id;
	}


	@Override
	public String toString() {
		return "EmployeeUpdateModel [uid_no=" + uid_no + ", mail_id=" + mail_id + ", ack=" + ack + "]";
	}

	
}
