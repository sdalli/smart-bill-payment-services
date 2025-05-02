package com.sesami.smart_bill_payment_services.config;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class MaskingSensitiveDataConverter extends ClassicConverter {
    @Override
    public String convert(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        return message.replaceAll("(\"password\"\\s*:\\s*\")[^\"]+\"", "$1****\"")
                      .replaceAll("(\"username\"\\s*:\\s*\")[^\"]+\"", "$1****\"");
    }
}

