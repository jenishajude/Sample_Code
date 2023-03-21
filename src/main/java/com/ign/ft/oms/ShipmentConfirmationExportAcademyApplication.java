package com.ign.ft.oms;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.format.DateTimeFormatter;

import com.google.gson.internal.LinkedTreeMap;
import com.ign.ft.oms.config.CustomBeanToCSVMappingStrategy;
import com.ign.ft.oms.config.SFTPClientConfig.UploadGateway;
import com.ign.ft.oms.model.ShipmentConfirmation;
import com.kibocommerce.sdk.fulfillment.api.ShipmentControllerApi;
import com.kibocommerce.sdk.fulfillment.model.EntityModelOfShipment;
import com.kibocommerce.sdk.fulfillment.model.Item;
import com.kibocommerce.sdk.fulfillment.model.PagedModelOfEntityModelOfShipment;
import com.mozu.api.contracts.commerceruntime.orders.Order;
import com.mozu.api.contracts.commerceruntime.orders.OrderAttribute;
import com.mozu.api.contracts.commerceruntime.orders.OrderCollection;
import com.mozu.api.resources.commerce.OrderResource;
import com.mozu.api.resources.commerce.orders.OrderAttributeResource;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;

@SpringBootApplication
public class ShipmentConfirmationExportAcademyApplication {

	private static final Logger logger = LogManager.getLogger(ShipmentConfirmationExportAcademyApplication.class);

	@Autowired
	private OrderResource orderResource;

	@Autowired
	private OrderAttributeResource orderAttributeResource;

	@Autowired
	private ShipmentControllerApi shipmentControllerApi;
	
	@Autowired
	private UploadGateway uploadGateway;


	@Value("${client.tenantID}")
	private Integer tenantID;

	@Value("${client.siteID}")
	private Integer siteID;

	@Value("${file.prefix}")
	private String filePrefix; 

	@Value("${file.extension}")
	private String fileExtension; 
	
	@Value("${location.code}")
	private String locationCode; 
	
	@Value("${shipfrom.address}")
	private String shipFromAddress; 
	
	@Value("${shipfrom.city}")
	private String shipFromCity; 
	
	@Value("${shipfrom.region}")
	private String shipFromRegion; 
	
	@Value("${shipfrom.postal}")
	private String shipFromPostal; 
	
	@Value("${sftp.retries}")
	private int sftpRetries;
	
	private String shippedFilter="( attributes.name eq tenant~division and  attributes.value eq ACADEMY and attributes.value ne SHIP-CONFIRMATION-SENT  and fulfillmentStatus in ['PartiallyFulfilled','Fulfilled'])";
	
	//private String shippedFilter="(orderNumber eq 2005227)";
	
	//private String shippedFilter="(orderNumber eq '2005227' or orderNumber eq '2005228' or orderNumber eq '2005229' or orderNumber eq '2005230' and fulfillmentStatus in ['PartiallyFulfilled','Fulfilled'])" ;
	
	private static final String ORDER_ATTRIBUTE_SHIP_CONFIRMATION = "retailerShipmentConfirmation";
	
	private static final String ORDER_ATTRIBUTE_TENANT_SHIP_CONFIRMATION= "tenant~" + ORDER_ATTRIBUTE_SHIP_CONFIRMATION;
	
	public static void main(String[] args) {
		SpringApplication.run(ShipmentConfirmationExportAcademyApplication.class, args);
		System.exit(0);
	}

	@Component
	public class JobRunner implements CommandLineRunner {

		@Override
		public void run(String... args) throws Exception {
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			//processing the shipment confirmation
			processShipmentConfirmation();
			
			stopWatch.stop();
	        logger.info("Time taken to complete Shipment Confirmation Export Process in HH:MM:SS.SSS :  " + 
	            											DurationFormatUtils.formatDurationHMS(stopWatch.getTime()));
		}

		private void processShipmentConfirmation() throws Exception {

			logger.info("Shipment Confirmation Job Started.");
			
			String pattern = "yyyyMMddhhmmss";
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

			String filename = filePrefix+simpleDateFormat.format(new Date())+fileExtension;

			Writer writer = new FileWriter(filename);

			CustomBeanToCSVMappingStrategy<ShipmentConfirmation> mappingStrategy = new CustomBeanToCSVMappingStrategy<>();
			mappingStrategy.setType(ShipmentConfirmation.class);

			StatefulBeanToCsv<ShipmentConfirmation> shipmentConfirmationWriter = new StatefulBeanToCsvBuilder<ShipmentConfirmation>(writer)
					.withSeparator(',')
					.withMappingStrategy(mappingStrategy)
					.build();

			int pageSize = 40; 			
			List<Order> orders = retrieveAllNgOrders(shippedFilter , pageSize);

			for(Order order : orders) {
				
				String purchaseOrderNumber = "";
				List<OrderAttribute> orderattributes = order.getAttributes();
				for (OrderAttribute orderAttribute : orderattributes) {			
					if (orderAttribute.getFullyQualifiedName().equals("tenant~retailerPONumber")) {
						purchaseOrderNumber = ((String) orderAttribute.getValues().get(0));
						break;
					}
				}
				
				PagedModelOfEntityModelOfShipment shipmentsModel = shipmentControllerApi.getShipmentsUsingGET(tenantID,"orderNumber=eq="+order.getOrderNumber(), 
												null,null, null,null,null,null,siteID);
				
				if(shipmentsModel!=null && shipmentsModel.getEmbedded()!=null) {
					List<EntityModelOfShipment> shipments = shipmentsModel.getEmbedded().get("shipments");
					if(shipments.size()>0) {	
						for(EntityModelOfShipment shipmentval : shipments ){
							if(!shipmentval.getShipmentStatus().getValue().equalsIgnoreCase("READY") &&
									shipmentval.getFulfillmentLocationCode() != null && shipmentval.getFulfillmentLocationCode().equalsIgnoreCase("SET_A")){
								
								logger.info("Processing the Order Number:"+order.getOrderNumber());
								
								List<Item> itemsShipped = new ArrayList<Item>();
								itemsShipped.addAll(shipmentval.getItems());
								for(Item shippedItem : itemsShipped) {
									
									ShipmentConfirmation shipmentConfirmationRow = new ShipmentConfirmation();
									
									shipmentConfirmationRow.setPo_number(purchaseOrderNumber);
									int lineId = shippedItem.getLineId();
									shipmentConfirmationRow.setLine_item_line_number(lineId);
									
									String sku = shippedItem.getVariationProductCode().replace("_", " ");
									shipmentConfirmationRow.setLine_item_sku(sku);
									shipmentConfirmationRow.setLine_item_quantity(shippedItem.getQuantity());				
									shipmentConfirmationRow.setPackage_tracking_number(getShipmentTrackingNumber(shipmentval));
									shipmentConfirmationRow.setPackage_warehouse_code(locationCode);
									
									String shipCarrier = shipmentval.getShippingMethodCode();
									shipmentConfirmationRow.setPackage_ship_carrier(shipCarrier);
									String shipMethodName = shipmentval.getShippingMethodName();
									String shipMethodCode =	shipMethodName.substring(shipMethodName.lastIndexOf(":") + 1).trim();
									shipmentConfirmationRow.setPackage_ship_method(shipMethodCode);
									shipmentConfirmationRow.setShipping_service_level_code(getShipServiceLevelCode(shipmentval));
									
									BigDecimal handlingCost = shippedItem.getHandling();
									BigDecimal shippedCost = shippedItem.getShipping();
									BigDecimal packageShipCost = handlingCost.add(shippedCost);
									
									if (tenantID.equals(29587) && !(packageShipCost.compareTo(BigDecimal.ZERO) > 0)) {
										packageShipCost = new BigDecimal(10);
									}
									shipmentConfirmationRow.setPackage_ship_cost(packageShipCost);

									shipmentConfirmationRow.setPackage_ship_date(getPackageShipDateValue(shipmentval));
		
									shipmentConfirmationRow.setShip_from_address_1(shipFromAddress);
									shipmentConfirmationRow.setShip_from_city(shipFromCity);
									shipmentConfirmationRow.setShip_from_region(shipFromRegion);
									shipmentConfirmationRow.setShip_from_postal(shipFromPostal);
									shipmentConfirmationRow.setShip_from_location_code(locationCode);
													
									shipmentConfirmationWriter.write(shipmentConfirmationRow);
									
									
								}
								
								// update order level attribute
								updateOrderAttributeStatus(order);
							}
							
						}
						
					}
				}

			} 
			writer.close();
			
			//uploading the .csv file to SFTP
			logger.info("Uploading the file to SFTP started.");
            File shipmentConfirmationFile = new File(filename);
            int copyAttempts=0;
            for (int i = 0; i < sftpRetries; i++) {
	            try {
	            	uploadGateway.uploadDsco(shipmentConfirmationFile); 
	            	logger.info("File "+shipmentConfirmationFile+" uploaded to SFTP");
	            	break;
				} catch (Exception e) {
					copyAttempts++;
					if(copyAttempts<sftpRetries) {
						logger.warn("Academy Attempt #"+copyAttempts+" - Retrying to connect to SFTP ... "); 
						continue;
					}else {
						logger.error("Academy Attempt #"+copyAttempts+" Files were NOT moved to the SFTP. Exiting now...",e); 
						break;
					}
				}
            }   
            
            logger.info("Shipment Confirmation Job completed.");			
           
		}

		/** Method to get the shipment tracking number
		 * 
		 * @param shipmentval
		 * @return trackNumber
		 */
		private String getShipmentTrackingNumber(EntityModelOfShipment shipmentval) {
			
			String trackNumber = " ";
			if(shipmentval.getPackages()!=null && shipmentval.getPackages().size()>0) {
				if(shipmentval.getPackages().get(0).getTrackingNumbers()!=null && shipmentval.getPackages().get(0).getTrackingNumbers().size()>0) {
					trackNumber = shipmentval.getPackages().get(0).getTrackingNumbers().get(0);
				}
			}
			return trackNumber;
		}
		
		
		/** Method to get the Package shipped date
		 * 
		 * @param shipmentval
		 * @return packageShipDate
		 */
		private String getPackageShipDateValue(EntityModelOfShipment shipmentval) {
			
			String packageShipDate = "";

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
			String shipdate = shipmentval.getFulfillmentDate().toZonedDateTime().format(formatter);
			
		    OffsetDateTime utcDateTime = OffsetDateTime.parse(shipdate);
		    ZoneOffset estOffset = ZoneOffset.of("-05:00");
		    OffsetDateTime estDateTime = utcDateTime.withOffsetSameLocal(estOffset);
		    packageShipDate = estDateTime.toString();

			return packageShipDate;
		}
		
		/** Method to get the Radial Carrier Code
		 * 
		 * @param shipmentval
		 * @return shipServiceLevelCode
		 */
		private String getShipServiceLevelCode(EntityModelOfShipment shipmentval) {
			
			String shipServiceLevelCode = "";
			if(shipmentval.getAttributes() != null) {
				LinkedTreeMap<String,String> carrierCodeList=(LinkedTreeMap<String,String>)shipmentval.getAttributes().get("carrierCodeList");
				Set<String> carrierKeys = carrierCodeList.keySet();
				for (String key : carrierKeys) {
					String radialCarrierCode=carrierCodeList.get(key);
					shipServiceLevelCode = mapShipServiceLevelCode(radialCarrierCode);
					break;	
				}
			}
			return shipServiceLevelCode;
		}
	
		/** Method to map the ship service level code
		 * 
		 * @param radialCarrierCode
		 * @return ShipServiceLevelCode
		 */
		private String mapShipServiceLevelCode(String radialCarrierCode) {
	
			String value="FEDX_09";
		
			if("FH".equals(radialCarrierCode))	value="FEDX_09";
			if("F1".equals(radialCarrierCode))	value="FEDX_ND";
			if("F2".equals(radialCarrierCode))	value="FEDX_SE";
			if("F3".equals(radialCarrierCode))	value="FEDX_3D";
			
			return value;		
			
		}

		/** Method to update the order level attribute
		 * 
		 * @param order
		 * @throws Exception
		 */
		private void updateOrderAttributeStatus(Order order) throws Exception {
			Optional<OrderAttribute> attribute = order.getAttributes().stream().filter(orderAttribute -> orderAttribute
					.getFullyQualifiedName().endsWith("~" + ORDER_ATTRIBUTE_SHIP_CONFIRMATION)).findFirst();
			if (attribute.isPresent()) {
				OrderAttribute orderAttribute = attribute.get();
				orderAttribute.setValues(Collections.singletonList("SHIP-CONFIRMATION-SENT"));
				List<OrderAttribute> orderAttributeList = Collections.singletonList(orderAttribute);	                
			    orderAttributeResource.updateOrderAttributes(orderAttributeList, order.getId(),false);
				logger.info("Order Attribute " + ORDER_ATTRIBUTE_TENANT_SHIP_CONFIRMATION
					+ " was updated to 'SHIP-CONFIRMATION-SENT', but it was already present in order number"
					+ order.getOrderNumber());
			} else {
				OrderAttribute orderAttributeNew = new OrderAttribute();
				orderAttributeNew.setFullyQualifiedName(ORDER_ATTRIBUTE_TENANT_SHIP_CONFIRMATION);
				orderAttributeNew.setValues(Collections.singletonList("SHIP-CONFIRMATION-SENT"));
				List<OrderAttribute> orderAttributeList = Collections.singletonList(orderAttributeNew);
				logger.info("Updating " + ORDER_ATTRIBUTE_TENANT_SHIP_CONFIRMATION
					+ " attribute for order number " + order.getOrderNumber());
					orderAttributeResource.createOrderAttributes(orderAttributeList, order.getId());
			}
		}
		
		 

		/** Method to retieve all orders with shippedfilter
		 * 
		 * @param shippedFilter
		 * @param pageSize
		 * @return response
		 */
		private List<Order> retrieveAllNgOrders(String shippedFilter, int pageSize) {
			logger.debug("Retrieving shipped items orders.");
			
			List<Order> response = new ArrayList<>();
			try {
				int startIndex = 0;
				OrderCollection orderCollection = orderResource.getOrders(startIndex, pageSize, null, shippedFilter, null, null, null, "synthesized", null);

				if (orderCollection != null && orderCollection.getTotalCount() > 0) {
					Iterator<Order> orderIterator = orderCollection.getItems().iterator();
					while (orderIterator.hasNext()) {
						Order order = orderIterator.next();
						response.add(order);

						// Check if more pages to fetch. 
						if (!orderIterator.hasNext()) {
							startIndex += pageSize;
							orderCollection = orderResource.getOrders(startIndex, pageSize, null, shippedFilter, null, null, null,	"synthesized", null);
							orderIterator = orderCollection.getItems().iterator();
						}
					}
				}
			} catch (Exception e) {
				logger.error("Error in fetching orders using filter"+ e.getMessage());
			}
			logger.info("Shipped orders list size: " + response.size());
			return response;
		}
	}

}
