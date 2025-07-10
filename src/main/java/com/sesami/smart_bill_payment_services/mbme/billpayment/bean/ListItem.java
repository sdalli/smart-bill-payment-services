package com.sesami.smart_bill_payment_services.mbme.billpayment.bean;



import java.util.List;

/**
 * Represents an item in a list with various attributes.
 */
public class ListItem {

    private String name;
    private String value;
    private String label;
    private String type;
    private boolean usedForPayment;
    private String position;
    private int rowNumber;
    private String isPayable; // 0 for false, 1 for true
    private List<ListItem> list;

    // Getters and Setters
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

    public boolean isUsedForPayment() {
        return usedForPayment;
    }

    public void setUsedForPayment(boolean usedForPayment) {
        this.usedForPayment = usedForPayment;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public List<ListItem> getList() {
        return list;
    }

    public void setList(List<ListItem> list) {
        this.list = list;
    }

    @Override
    public String toString() {
        return "ListItem{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", label='" + label + '\'' +
                ", type='" + type + '\'' +
                ", usedForPayment=" + usedForPayment +
                ", position='" + position + '\'' +
                ", rowNumber=" + rowNumber +
                ", list=" + list +
                '}';
    }

	public String getIsPayable() {
		return isPayable;
	}

	public void setIsPayable(String isPayable) {
		this.isPayable = isPayable;
	}
}

