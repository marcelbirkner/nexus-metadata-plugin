package de.mb;

import java.net.URI;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class RestTest {
	
	public static void main(String[] args) {
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		WebResource service = client.resource(getBaseURI());
		System.out.println(service.path("api").path("build").accept(MediaType.APPLICATION_JSON).get(ClientResponse.class).toString());

	}

	private static URI getBaseURI() {
		return UriBuilder.fromUri("http://jenkins:8081/artifactory/").build();
	}
}
