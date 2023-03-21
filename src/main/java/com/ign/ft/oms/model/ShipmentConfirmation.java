package com.ign.ft.oms.model;

import java.math.BigDecimal;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

public class ShipmentConfirmation {

	@CsvBindByName(column = "po_number")
	@CsvBindByPosition(position = 0)
	private String po_number; 

	@CsvBindByName(column = "line_item_line_number ")
	@CsvBindByPosition(position = 1)
	private int line_item_line_number ;

	@CsvBindByName(column = "line_item_sku")
	@CsvBindByPosition(position = 2)
	private String line_item_sku; 

	@CsvBindByName(column = "line_item_quantity")
	@CsvBindByPosition(position = 3)
	private int line_item_quantity; 

	@CsvBindByName(column = "package_tracking_number")
	@CsvBindByPosition(position = 4)
	private String package_tracking_number;	  

	@CsvBindByName(column = "package_warehouse_code")
	@CsvBindByPosition(position = 5)
	private String package_warehouse_code; 

	@CsvBindByName(column = "package_ship_carrier")
	@CsvBindByPosition(position = 6)
	private String package_ship_carrier;

	@CsvBindByName(column = "package_ship_method")
	@CsvBindByPosition(position = 7)
	private String package_ship_method; 

	@CsvBindByName(column = "package_ship_cost ")
	@CsvBindByPosition(position = 8)
	private BigDecimal package_ship_cost ;

	@CsvBindByName(column = "package_ship_date")
	@CsvBindByPosition(position = 9)
	private String package_ship_date; 

	@CsvBindByName(column = "shipping_service_level_code")
	@CsvBindByPosition(position = 10)
	private String shipping_service_level_code; 

	@CsvBindByName(column = "ship_from_address_1")
	@CsvBindByPosition(position = 11)
	private String ship_from_address_1;	  
	
	@CsvBindByName(column = "ship_from_city")
	@CsvBindByPosition(position = 12)
	private String ship_from_city;	 

	@CsvBindByName(column = "ship_from_region")
	@CsvBindByPosition(position = 13)
	private String ship_from_region; 
	
	@CsvBindByName(column = "ship_from_postal")
	@CsvBindByPosition(position = 14)
	private String ship_from_postal; 

	@CsvBindByName(column = "ship_from_location_code")
	@CsvBindByPosition(position = 15)
	private String ship_from_location_code;
	

	public String getPo_number() {
		return po_number;
	}

	public void setPo_number(String po_number) {
		this.po_number = po_number;
	}

	public int getLine_item_line_number() {
		return line_item_line_number;
	}

	public void setLine_item_line_number(int line_item_line_number) {
		this.line_item_line_number = line_item_line_number;
	}

	public String getLine_item_sku() {
		return line_item_sku;
	}

	public void setLine_item_sku(String line_item_sku) {
		this.line_item_sku = line_item_sku;
	}

	public int getLine_item_quantity() {
		return line_item_quantity;
	}

	public void setLine_item_quantity(int line_item_quantity) {
		this.line_item_quantity = line_item_quantity;
	}

	public String getPackage_tracking_number() {
		return package_tracking_number;
	}

	public void setPackage_tracking_number(String package_tracking_number) {
		this.package_tracking_number = package_tracking_number;
	}

	public String getPackage_warehouse_code() {
		return package_warehouse_code;
	}

	public void setPackage_warehouse_code(String package_warehouse_code) {
		this.package_warehouse_code = package_warehouse_code;
	}

	public String getPackage_ship_carrier() {
		return package_ship_carrier;
	}

	public void setPackage_ship_carrier(String package_ship_carrier) {
		this.package_ship_carrier = package_ship_carrier;
	}

	public String getPackage_ship_method() {
		return package_ship_method;
	}

	public void setPackage_ship_method(String package_ship_method) {
		this.package_ship_method = package_ship_method;
	}

	public BigDecimal getPackage_ship_cost() {
		return package_ship_cost;
	}

	public void setPackage_ship_cost(BigDecimal packageShipCost) {
		this.package_ship_cost = packageShipCost;
	}

	public String getPackage_ship_date() {
		return package_ship_date;
	}

	public void setPackage_ship_date(String package_ship_date) {
		this.package_ship_date = package_ship_date;
	}

	public String getShipping_service_level_code() {
		return shipping_service_level_code;
	}

	public void setShipping_service_level_code(String shipping_service_level_code) {
		this.shipping_service_level_code = shipping_service_level_code;
	}

	public String getShip_from_address_1() {
		return ship_from_address_1;
	}

	public void setShip_from_address_1(String ship_from_address_1) {
		this.ship_from_address_1 = ship_from_address_1;
	}

	public String getShip_from_region() {
		return ship_from_region;
	}

	public void setShip_from_region(String ship_from_region) {
		this.ship_from_region = ship_from_region;
	}

	public String getShip_from_location_code() {
		return ship_from_location_code;
	}

	public void setShip_from_location_code(String ship_from_location_code) {
		this.ship_from_location_code = ship_from_location_code;
	}

	public String getShip_from_city() {
		return ship_from_city;
	}

	public void setShip_from_city(String ship_from_city) {
		this.ship_from_city = ship_from_city;
	}

	public String getShip_from_postal() {
		return ship_from_postal;
	}

	public void setShip_from_postal(String ship_from_postal) {
		this.ship_from_postal = ship_from_postal;
	}


}
