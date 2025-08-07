package com.sesami.smart_bill_payment_services.mbme.billpayment.bean;

import java.util.List;

public class InnerListItem {
	private List<InnerField> list;
	private String rowNumber;

	public List<InnerField> getList() {
		return list;
	}

	public void setList(List<InnerField> list) {
		this.list = list;
	}

	public String getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(String rowNumber) {
		this.rowNumber = rowNumber;
	}
}
