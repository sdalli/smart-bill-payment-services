package com.sesami.smart_bill_payment_services.mbme.billpayment.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BalanceEnquiryDynamicRequestField;
import com.sesami.smart_bill_payment_services.mbme.billpayment.bean.BalanceEnquiryRequest;

/**
 * Utility methods for creating dynamic bill inquiry requests.
 */
public final class DynamicBillInquiryRequestUtil {

    private static final Logger logger = LoggerFactory.getLogger(DynamicBillInquiryRequestUtil.class);

    private DynamicBillInquiryRequestUtil() {
        // Utility class, do not instantiate
    }

    /**
     * Generates a dynamic bill inquiry request JSON string based on the provided BalanceEnquiryRequest.
     *
     * @param balanceEnquiryRequest the request POJO
     * @return JSON string representing the bill inquiry request
     */
    public static String generateDynamicBillInquiryRequest(BalanceEnquiryRequest balanceEnquiryRequest) {
        Objects.requireNonNull(balanceEnquiryRequest, "balanceEnquiryRequest must not be null");

        var objectMapper = new ObjectMapper();
        var rootNode = objectMapper.createObjectNode();

        // Add static/common fields
        var staticFields = Map.of(
            "transactionId", balanceEnquiryRequest.getExternalTransactionId(),
            "merchantId", balanceEnquiryRequest.getMerchantId(),
            "merchantLocation", balanceEnquiryRequest.getMerchantLocation(),
            "serviceId", balanceEnquiryRequest.getServiceId(),
            "method", balanceEnquiryRequest.getServiceType(),
            "lang", balanceEnquiryRequest.getLanguage()
        );

        staticFields.forEach((key, value) -> {
            if (value != null) {
                rootNode.put(key, value);
            }
        });

        // Service-specific dynamic field mapping
        var serviceId = balanceEnquiryRequest.getServiceId();
        var dynamicFields = balanceEnquiryRequest.getDynamicRequestFields();

        if (dynamicFields == null || dynamicFields.isEmpty()) {
            logger.warn("No dynamic request fields found for request: {}", balanceEnquiryRequest);
            return rootNode.toString();
        }

        // Convert list to map for easier access
        var fieldMap = dynamicFields.stream()
            .filter(f -> f.getName() != null && f.getValue() != null)
            .collect(java.util.stream.Collectors.toMap(
                BalanceEnquiryDynamicRequestField::getName,
                BalanceEnquiryDynamicRequestField::getValue,
                (v1, v2) -> v1, // handle duplicate keys if any
                LinkedHashMap::new // maintain insertion order
            ));

        switch (serviceId) {
            case "103", "1" -> {
                putIfPresent(rootNode, "reqField1", fieldMap, "accountNumber");
            }
            case "14", "15" -> {
                putIfPresent(rootNode, "reqField1", fieldMap, "serviceName");
            }
            case "18" -> handleServiceId18(rootNode, fieldMap, balanceEnquiryRequest);
            case "19" -> {
                putIfPresent(rootNode, "reqField1", fieldMap, "accountNumber");
                putIfPresent(rootNode, "reqField2", fieldMap, "serviceType");
            }
            case "21" -> {
                putIfPresent(rootNode, "reqField1", fieldMap, "accountNumber");
                putIfPresent(rootNode, "reqField2", fieldMap, "pin");
            }
            case "42" -> {
                putIfPresent(rootNode, "reqField1", fieldMap, "cardnumber");
                putIfPresent(rootNode, "reqField2", fieldMap, "amount");
            }
            case "20150" -> {
                putIfPresent(rootNode, "reqField1", fieldMap, "transactionNumber");
            }
            case "112" -> {
                putIfPresent(rootNode, "reqField1", fieldMap, "phoneNumber");
            }
            default -> {
                // For all other serviceIds, add all dynamic fields using their names as keys
                fieldMap.forEach(rootNode::put);
            }
        }

        return rootNode.toString();
    }

    private static void handleServiceId18(ObjectNode rootNode, Map<String, String> fieldMap, BalanceEnquiryRequest request) {
    	var searchType = fieldMap.get("searchType"); // Fetch the value of searchType directly from the fieldMap
    	request.setServiceCode(searchType); // Set the service code based on searchType
    	if( request.getServiceType() == null  && request.getServiceType().equalsIgnoreCase("createTransaction")) {
    		
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

    private static void putIfPresent(ObjectNode node, String jsonField, Map<String, String> source, String sourceKey) {
        var value = source.get(sourceKey);
        if (value != null) {
            node.put(jsonField, value);
        } else {
            logger.warn("Expected dynamic field '{}' not found in request fields.", sourceKey);
        }
    }
}
