package com.ign.ft.oms.config;

import org.apache.commons.lang.StringUtils;

import com.opencsv.bean.BeanField;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

public class CustomBeanToCSVMappingStrategy<ShipmentConfirmation> extends ColumnPositionMappingStrategy<ShipmentConfirmation> {
	 @Override
	 public String[] generateHeader(ShipmentConfirmation bean) throws CsvRequiredFieldEmptyException {

	        String[] headersAsPerFieldName = getFieldMap().generateHeader(bean); // header name based on field name
	        String[] header = new String[headersAsPerFieldName.length];
	        for (int i = 0; i <= headersAsPerFieldName.length - 1; i++) {

	            BeanField beanField = findField(i);
	            String columnHeaderName = extractHeaderName(beanField); // header name based on @CsvBindByName annotation
	            if (columnHeaderName.isEmpty()) 
	                columnHeaderName = headersAsPerFieldName[i]; // defaults to header name based on field name
	            header[i] = columnHeaderName;
	        }

	        headerIndex.initializeHeaderIndex(header);
	        return header;
	   }

	    private String extractHeaderName(final BeanField beanField) {
	        if (beanField == null || beanField.getField() == null || beanField.getField().getDeclaredAnnotationsByType(CsvBindByName.class).length == 0) {
	            return StringUtils.EMPTY;
	        }

	        final CsvBindByName bindByNameAnnotation = beanField.getField().getDeclaredAnnotationsByType(CsvBindByName.class)[0];
	        return bindByNameAnnotation.column();
	    }

}
