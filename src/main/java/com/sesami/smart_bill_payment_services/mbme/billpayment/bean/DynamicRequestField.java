package com.sesami.smart_bill_payment_services.mbme.billpayment.bean;

import java.io.Serializable;
import java.util.List;

public class DynamicRequestField implements Serializable {
	 /**
	 * 
	 */
	private static final long serialVersionUID = -3777515891446767212L;
	private String name;
    private String value;
    private List<SubDynamicRequestFields> subDynamicRequestFields;
	
    
    public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public List<SubDynamicRequestFields> getSubDynamicRequestFields() {
		return subDynamicRequestFields;
	}
	public void setSubDynamicRequestFields(List<SubDynamicRequestFields> subDynamicRequestFields) {
		this.subDynamicRequestFields = subDynamicRequestFields;
	}
	
    
    
    
    
    
}
