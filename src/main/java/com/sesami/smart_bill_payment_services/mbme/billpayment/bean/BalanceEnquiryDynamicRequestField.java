package com.sesami.smart_bill_payment_services.mbme.billpayment.bean;

import java.io.Serializable;

public class BalanceEnquiryDynamicRequestField implements Serializable {
	 /**
	 * 
	 */
	private static final long serialVersionUID = -3777515891446767212L;
	private String name;
    private String value;
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
	
    
    
    
    
    
}
