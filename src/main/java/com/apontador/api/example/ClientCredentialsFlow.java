package com.apontador.api.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import net.smartam.leeloo.client.OAuthClient;
import net.smartam.leeloo.client.URLConnectionClient;
import net.smartam.leeloo.client.request.OAuthClientRequest;
import net.smartam.leeloo.client.response.OAuthJSONAccessTokenResponse;
import net.smartam.leeloo.common.exception.OAuthProblemException;
import net.smartam.leeloo.common.exception.OAuthSystemException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Simple example that shows how to get OAuth 2.0 access token from Apontador API
 * using Leeloo OAuth library
 */
public class ClientCredentialsFlow {

    public static void main(String[] args) throws OAuthSystemException, IOException {

        try {
        	

            OAuthClientRequest request = OAuthClientRequest
                    .tokenLocation("http://localhost:8080/api/oauth/token")
                    .setParameter("grant_type", "client_credentials")
                    .setClientId("my-client-with-secret")
                    .setClientSecret("secret")
                    .setScope("read")
                    .setRedirectURI("http://localhost:8080/")
                    .buildBodyMessage();
        	
            OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
            OAuthJSONAccessTokenResponse oAuthResponse = oAuthClient.accessToken(request);

            System.out.println("Access Token: " + oAuthResponse.getAccessToken() + 
            		", Refresh Token: " + oAuthResponse.getRefreshToken() + 
            		", Expires in: " + oAuthResponse.getExpiresIn());
 
            //get protected resource
            DefaultHttpClient httpClient = new DefaultHttpClient();
    		HttpGet getRequest = new HttpGet("http://localhost:8080/api/places/nottinghill");
    		getRequest.addHeader("accept", "application/json");
    		getRequest.addHeader("authorization", "Bearer " + oAuthResponse.getAccessToken());
     
    		HttpResponse response = httpClient.execute(getRequest);
            
    		if (response.getStatusLine().getStatusCode() != 200) {
    			throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
    		}
     
    		BufferedReader br  = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
     
    		String output;
    		System.out.println("Output from Resource Server .... \n");
    		while ((output = br.readLine()) != null) {
    			System.out.println(output);
    		}
     
    		httpClient.getConnectionManager().shutdown();
            
            
        } catch (OAuthProblemException e) {
            System.out.println("OAuth error: " + e.getError());
            System.out.println("OAuth error description: " + e.getDescription());
        }
        
    }

}
