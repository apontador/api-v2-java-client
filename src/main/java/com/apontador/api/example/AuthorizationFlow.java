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
import net.smartam.leeloo.common.message.types.GrantType;
import net.smartam.leeloo.common.message.types.ResponseType;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Simple example that shows how to get OAuth 2.0 access token from Apontador API
 * using Leeloo OAuth library
 */
public class AuthorizationFlow {

    public static void main(String[] args) throws OAuthSystemException, IOException {

        try {
        	
            OAuthClientRequest request = OAuthClientRequest
                .authorizationLocation("http://localhost:8080/api/oauth/authorize")
                .setClientId("my-trusted-client-with-secret")
                .setRedirectURI("http://localhost:8080/")
                .setScope("read")
                .setState("optional-csrf-token")
                .setResponseType(ResponseType.CODE.toString())
                .buildQueryMessage();

            //in web application you make redirection to uri:
            System.out.println("Visit:\n" + request.getLocationUri() + "\nand grant permission");

            System.out.print("Now enter the OAuth code you have received in redirect uri ");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String code = br.readLine();

            request = OAuthClientRequest
                .tokenLocation("http://localhost:8080/api/oauth/token")
                .setGrantType(GrantType.AUTHORIZATION_CODE)
                .setCode(code)
                .setClientId("my-trusted-client-with-secret")
                .setClientSecret("somesecret")
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
     
    		br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
     
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
