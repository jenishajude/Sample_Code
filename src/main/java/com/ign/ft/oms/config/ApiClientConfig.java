package com.ign.ft.oms.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kibocommerce.sdk.auth.ApiException;
import com.kibocommerce.sdk.auth.api.AppAuthTicketsApi;
import com.kibocommerce.sdk.auth.model.MozuAppDevContractsOAuthAccessTokenResponse;
import com.kibocommerce.sdk.auth.model.MozuAppDevContractsOauthAuthRequest;
import com.kibocommerce.sdk.fulfillment.ApiClient;
import com.kibocommerce.sdk.fulfillment.api.ShipmentControllerApi;
import com.mozu.api.MozuApiContext;
import com.mozu.api.contracts.appdev.AppAuthInfo;
import com.mozu.api.resources.commerce.OrderResource;
import com.mozu.api.resources.commerce.catalog.admin.ProductResource;
import com.mozu.api.resources.commerce.catalog.admin.products.ProductVariationResource;
import com.mozu.api.resources.commerce.orders.OrderAttributeResource;
import com.mozu.api.resources.commerce.orders.ShipmentResource;
import com.mozu.api.security.AppAuthenticator;

import lombok.Data;

@Configuration
@Data
public class ApiClientConfig {

    @Value("${url.auth.host}")
    private String authUrlHostBasePath;

    @Value("${url.auth.path}")
    private String authServicePath;

    @Value("${auth.clientId}")
    private String clientID;

    @Value("${auth.clientSecret}")
    private String clientSecret;

    @Value("${client.tenantID}")
    private Integer tenantID;

    @Value("${client.siteID}")
    private Integer siteID;
    
    @Value("${url.fulfillment.host}")
    private String	fulfillmentUrlHostBasePath;
    
    @Value("${url.fulfillment.path}")
    private String	fulfillmentServicePath;
    
    @Value("${auth.debug}")
    private boolean debugConnection;
 
    @Bean
    public com.kibocommerce.sdk.auth.ApiClient authApiClient() {
        com.kibocommerce.sdk.auth.ApiClient authApiClient = new com.kibocommerce.sdk.auth.ApiClient();

        authApiClient.setBasePath(authUrlHostBasePath + authServicePath);
        authApiClient.setDebugging(debugConnection);
        authApiClient.setReadTimeout(30000);

        return authApiClient;
    }

    //auth api
    @Bean
    public AppAuthTicketsApi appAuthTicketsApi() {
        return new AppAuthTicketsApi(authApiClient());
    }

    @Bean
    public MozuAppDevContractsOAuthAccessTokenResponse authAccessToken() {

        MozuAppDevContractsOauthAuthRequest mozuAppDevContractsOauthAuthRequest = new MozuAppDevContractsOauthAuthRequest();
        mozuAppDevContractsOauthAuthRequest.clientId(clientID);
        mozuAppDevContractsOauthAuthRequest.clientSecret(clientSecret);
        mozuAppDevContractsOauthAuthRequest.setGrantType("client_credentials");
        
        try {
            MozuAppDevContractsOAuthAccessTokenResponse response = appAuthTicketsApi().oauthAuthenticateApp(null,null, mozuAppDevContractsOauthAuthRequest);

            if (response != null) {
                return response;
            }
        }
        catch (ApiException ex) {
            System.out.println(ex.getResponseBody());
        }

        throw new RuntimeException("Could not acquire auth token");
    }
    
    @Bean
    public ApiClient fulfillmentApiClient() {
        ApiClient fulfillmentApiClient = new ApiClient();

        fulfillmentApiClient.setBasePath(fulfillmentUrlHostBasePath + fulfillmentServicePath);
        fulfillmentApiClient.setReadTimeout(30000);
        fulfillmentApiClient.setDebugging(debugConnection);
        fulfillmentApiClient.addDefaultHeader("Authorization", "Bearer " + authAccessToken().getAccessToken());

        return fulfillmentApiClient;
    }
    
    @Bean
    public ShipmentControllerApi shipmentControllerApi() {
        return new ShipmentControllerApi(fulfillmentApiClient());
    }

    
    @Bean
    public AppAuthInfo appAuthInfo() {
	    AppAuthInfo appAuthInfo = new AppAuthInfo();
		appAuthInfo.setApplicationId(clientID);
		appAuthInfo.setSharedSecret(clientSecret);
		return appAuthInfo;
    }
    
    @Bean
    public MozuApiContext apiContext() {
    	return new MozuApiContext(tenantID, siteID);
    }
    
    @Bean
    public OrderResource orderResourceApi() {
		AppAuthenticator.initialize(appAuthInfo());
        return new OrderResource(apiContext());
    }
    
    @Bean
    public ShipmentResource shipmentResourceApi() {
		AppAuthenticator.initialize(appAuthInfo());
        return new ShipmentResource(apiContext());
    }
    
    @Bean
    public OrderAttributeResource orderAttributeResourceApi() {
		AppAuthenticator.initialize(appAuthInfo());
        return new OrderAttributeResource(apiContext());
    }
    
    @Bean
    public ProductResource productResourceApi() {
		AppAuthenticator.initialize(appAuthInfo());
        return new ProductResource(apiContext());
    }
    
    @Bean
    public ProductVariationResource productVariationResourceApi() {
		AppAuthenticator.initialize(appAuthInfo());
        return new ProductVariationResource(apiContext());
    }


}