package com.sesami.smart_bill_payment_services.mbme.billpayment.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BillPaymentRequest;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.DynamicRequestField;

/**
 * Utility methods for creating dynamic bill inquiry requests.
 */
public final class DynamicBillPaymentServiceRequestUtil {

    private static final Logger logger = LoggerFactory.getLogger(DynamicBillPaymentServiceRequestUtil.class);

    private DynamicBillPaymentServiceRequestUtil() {
        // Utility class, do not instantiate
    }

    public static String generateDynamicBillPaymentRequest(BillPaymentRequest billPaymentRequest) {
        Objects.requireNonNull(billPaymentRequest, "BillPaymentRequest must not be null");

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode rootNode = objectMapper.createObjectNode();

        // Add static/common fields


        var staticFields = Map.of(
                "transactionId", billPaymentRequest.getExternalTransactionId(),
                "merchantId", billPaymentRequest.getMerchantId(),
                "merchantLocation", billPaymentRequest.getMerchantLocation(),
                "method", billPaymentRequest.getServiceType(),
                "serviceId", billPaymentRequest.getServiceId(),
             // CH = CASH / CQ = Cheque // CC = Credit Card // PO = POS Machine // NE = NetBanking
                "paymentMode", switch (billPaymentRequest.getPaymentMode() == null ? "CH" : billPaymentRequest.getPaymentMode().toUpperCase()) {
                    case "CH" -> "Cash";
                    case "CQ" -> "Cheque";
                    case "CC" -> "Credit Card";
                    case "PO" -> "POS Machine";
                    case "NE" -> "NetBanking";
                    default -> billPaymentRequest.getPaymentMode();
                },
                "paidAmount", billPaymentRequest.getPaidAmount() != null ? billPaymentRequest.getPaidAmount().toString() : "0.00", 
                "lang", billPaymentRequest.getLanguage()
            );
        
        staticFields.forEach((key, value) -> {
            if (value != null) {
                rootNode.put(key, value);
            }
        });

        var serviceId = billPaymentRequest.getServiceId();
        var dynamicFields = billPaymentRequest.getDynamicRequestFields();

        // Service-specific dynamic field mapping
        if (serviceId == null || serviceId.isEmpty()) {
			logger.warn("Service ID is null or empty in request: {}", billPaymentRequest);
			return rootNode.toString();
		}
        if (dynamicFields == null || dynamicFields.isEmpty()) {
            logger.warn("No dynamic request fields found for request: {}", billPaymentRequest);
            return rootNode.toString();
        }

        // Convert list to map for easier access

        var fieldMap = dynamicFields.stream()
                .filter(f -> f.getName() != null && f.getValue() != null)
                .collect(java.util.stream.Collectors.toMap(
                		DynamicRequestField::getName,
                		DynamicRequestField::getValue,
                    (v1, v2) -> v1, // handle duplicate keys if any
                    LinkedHashMap::new // maintain insertion order
                ));


        switch (serviceId) {
        case "103", "1" -> {
            putIfPresent(rootNode, "reqField1", fieldMap, "accountNumber");
            putIfPresent(rootNode, "reqField2", fieldMap, "transactionType");
        }
        case "14", "15" -> {
            putIfPresent(rootNode, "reqField1", fieldMap, "serviceName");
        }
        case "18" -> handleServiceId18(rootNode, fieldMap, billPaymentRequest);
        case "19" -> {
           
        	//handleServiceId_Etislate_19(rootNode, fieldMap, billPaymentRequest);
        	 putIfPresent(rootNode, "reqField1", fieldMap, "accountNumber");
             putIfPresent(rootNode, "reqField2", fieldMap, "serviceType");
            putIfPresent(rootNode, "reqField3", fieldMap, "apiReturnTransactionId");
            putIfPresent(rootNode, "reqField4", fieldMap, "amount");
        }
        case "21" -> {
            putIfPresent(rootNode, "reqField1", fieldMap, "accountNumber");
            putIfPresent(rootNode, "reqField2", fieldMap, "pin");
            putIfPresent(rootNode, "reqField3", fieldMap, "apiReturnTransactionId");
        }
        case "42" -> {
            putIfPresent(rootNode, "reqField1", fieldMap, "cardNumber");
            putIfPresent(rootNode, "reqField2", fieldMap, "type");
        }
        case "20150" -> {
            putIfPresent(rootNode, "reqField1", fieldMap, "transactionNumber");
        }
        case "112" -> {
            putIfPresent(rootNode, "reqField1", fieldMap, "phoneNumber");
            putIfPresent(rootNode, "reqField2", fieldMap, "id");
            putIfPresent(rootNode, "reqField3", fieldMap, "foreignAmount");
        }
        default -> {
            // For all other serviceIds, add all dynamic fields using their names as keys
            fieldMap.forEach(rootNode::put);
        }
    }

    return rootNode.toString();
    }

    private static void handleServiceId18(ObjectNode rootNode, Map<String, String> fieldMap, BillPaymentRequest billPaymentRequest) {
    	var searchType = fieldMap.get("searchType"); // Fetch the value of searchType directly from the fieldMap
    	billPaymentRequest.setServiceCode(searchType); // Set the service code based on searchType
    	// Dubai Traffic Fines
    	if( billPaymentRequest.getServiceType() == null  && billPaymentRequest.getServiceType().equalsIgnoreCase("createTransaction")) {
    		
    	}
    	
        if ("byTrfNo".equalsIgnoreCase(searchType)) {
            putIfPresent(rootNode, "reqField1", fieldMap, "searchType");
            putIfPresent(rootNode, "reqField2", fieldMap, "trafficFileNo");
        } else if ("byPlateNo".equalsIgnoreCase(searchType)) {
            putIfPresent(rootNode, "reqField1", fieldMap, "searchType");
            putIfPresent(rootNode, "reqField2", fieldMap, "plateNo");
            putIfPresent(rootNode, "reqField3", fieldMap, "plateCode");
            putIfPresent(rootNode, "reqField4", fieldMap, "plateCategory");
            putIfPresent(rootNode, "reqField5", fieldMap, "plateSource");
        } else if ("byTicketNo".equalsIgnoreCase(searchType)) {
            putIfPresent(rootNode, "reqField1", fieldMap, "searchType");
            putIfPresent(rootNode, "reqField2", fieldMap, "ticketNo");
            putIfPresent(rootNode, "reqField3", fieldMap, "ticketYear");
            putIfPresent(rootNode, "reqField4", fieldMap, "beneficiaryCode");
            putIfPresent(rootNode, "reqField5", fieldMap, "beneficiaryName");
        } else if ("byLicenceNo".equalsIgnoreCase(searchType)) {
            putIfPresent(rootNode, "reqField1", fieldMap, "searchType");
            putIfPresent(rootNode, "reqField2", fieldMap, "licenceSourceCode");
            putIfPresent(rootNode, "reqField3", fieldMap, "licenceNumber");
            putIfPresent(rootNode, "reqField4", fieldMap, "licenceFrom");
        }
    }

    private static void handleServiceId_Etislate_19(ObjectNode rootNode, Map<String, String> fieldMap, BillPaymentRequest billPaymentRequest) {
    	if( billPaymentRequest.getServiceCode() == null  && billPaymentRequest.getServiceCode().equalsIgnoreCase("EtisalatTopUp")) {
    		
    	}
    	
        if ( billPaymentRequest.getServiceCode() == null  && billPaymentRequest.getServiceCode().equalsIgnoreCase("EtisalatTopUp")) {
        	 putIfPresent(rootNode, "reqField1", fieldMap, "accountNumber");
             putIfPresent(rootNode, "reqField2", fieldMap, "serviceType");
        } else if ( billPaymentRequest.getServiceCode() == null  && billPaymentRequest.getServiceCode().equalsIgnoreCase("EtisalatTopUp")) {
        	 putIfPresent(rootNode, "reqField1", fieldMap, "accountNumber");
             putIfPresent(rootNode, "reqField2", fieldMap, "serviceType");
            putIfPresent(rootNode, "reqField4", fieldMap, "plateCategory");
            putIfPresent(rootNode, "reqField5", fieldMap, "plateSource");
        } 
    }
    private static void putIfPresent(ObjectNode node, String jsonField, Map<String, String> source, String sourceKey) {
        var value = source.get(sourceKey);
        if (value != null) {
            node.put(jsonField, value);
        } else {
            logger.warn("Expected dynamic field '{}' not found in request fields.", sourceKey);
        }
    }
}
