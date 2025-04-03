package com.sesami.smart_bill_payment_services.mbme.billpayment.bean;

import java.util.ArrayList;
import java.util.List;

public class BillingCommonDynamicResponseField {
	 private String name="";
     private String value="";
     private String label="";
     private String type="text";
     private boolean visible=Boolean.FALSE;
     private boolean export=Boolean.FALSE;
     private List<ListItem> list= new ArrayList<ListItem>();
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
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean isVisible() {
		return visible;
	}
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	public boolean isExport() {
		return export;
	}
	public void setExport(boolean export) {
		this.export = export;
	}
	public List<ListItem> getList() {
		return list;
	}
	public void setList(List<ListItem> list) {
		this.list = list;
	}
     
     
}
