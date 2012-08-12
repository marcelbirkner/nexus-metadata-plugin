package de.mb;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.Secret;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.ws.rs.core.MediaType;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import com.sonatype.nexus.index.rest.model.CustomMetadata;
import com.sonatype.nexus.index.rest.model.CustomMetadataRequest;
import com.sonatype.nexus.index.rest.model.CustomMetadataResponse;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.Base64;

/**
 * {@link Builder} that add metadata to an artefact in the Nexus repository
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link NexusMetadataBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #key})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
 * method will be invoked. 
 *
 * @author Marcel Birkner
 */
public class NexusMetadataBuilder extends Builder {

    private final String key;
    private final String value;
    private final String namespace;
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String packaging;
    
    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public NexusMetadataBuilder(String key, String value, String namespace, String groupId, 
    		String artifactId, String version, String packaging) {
        this.key = key;
        this.value = value;
        this.namespace = namespace;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.packaging = packaging;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getKey() {
		return key;
	}
	public String getValue() {
		return value;
	}
	public String getNamespace() {
		return namespace;
	}
	public String getGroupId() {
		return groupId;
	}
	public String getArtifactId() {
		return artifactId;
	}
	public String getVersion() {
		return version;
	}
	public String getPackaging() {
		return packaging;
	}

	/**
	 * This is where you 'build' the project.
	 */
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
    	
    	final String url = getDescriptor().getNexusUrl();
    	final String user = getDescriptor().getNexusUser();
    	final Secret password = getDescriptor().getNexusPassword();
    	
		if( ! validatePluginConfiguration(url, user, Secret.toString( password ) ) ) {
			listener.getLogger().println("Please configure Nexus Metadata Plugin");
			return false;
		}
		
    	WebResource service = getService(url, user, password);
		
		listener.getLogger().println("GET Nexus Version");
		String nexusVersion = service.path("service").path("local").path("status").accept(MediaType.APPLICATION_JSON).get(String.class).toString();
		if( ! nexusVersion.contains( "nexus:metadata" ) ) {
			listener.getLogger().println("Please install the metadata plugin.");
			return false;
		}
		if( ! nexusVersion.contains( "Sonatype Nexus Professional" ) )  {
			listener.getLogger().println("Please install Sonatype Nexus Professional.");
			return false;
		}
		listener.getLogger().println("Installation seems to be correct.\n");

		String artefact = getNamespace()+"#"+getGroupId()+":"+getArtifactId()+":"+getVersion()+"::"+getPackaging()+"";
		listener.getLogger().println("GET metadata for artefact " + artefact);
		String encodedString = new String( Base64.encode( artefact.getBytes() ) );
		String metadataResult = service.path("service").path("local").path("index").path("custom_metadata").path("releases")
				.path(encodedString).accept(MediaType.APPLICATION_JSON).get(String.class).toString();
		listener.getLogger().println( metadataResult + "\n");

		CustomMetadataResponse metadataRes = service.path("service").path("local").path("index").path("custom_metadata").path("releases")
				.path(encodedString).accept(MediaType.APPLICATION_XML).get( CustomMetadataResponse.class );

		List<CustomMetadata> metaList = metadataRes.getData();
		CustomMetadataRequest customRequest = getCustomMetadataRequest( getNamespace(), getKey(), getValue() );
		for (CustomMetadata customMetadata : metaList) {
			if( ! customMetadata.getReadOnly() ) {
				customRequest.getData().add( customMetadata );
			}
		}
		
		listener.getLogger().println("POST: add new metadata to artefact " + artefact);
		service.path("service").path("local")
			.path("index").path("custom_metadata").path("releases")
			.path(encodedString).accept( MediaType.APPLICATION_JSON ).post( customRequest );
		metadataResult = service.path("service").path("local").path("index").path("custom_metadata").path("releases")
				.path(encodedString).accept(MediaType.APPLICATION_JSON).get(String.class).toString();
		listener.getLogger().println( metadataResult + "\n");

		return true;
    }

	private WebResource getService(final String url, final String user,
			final Secret password) {
		// setup REST-Client
    	ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		client.addFilter( new HTTPBasicAuthFilter(user, Secret.toString( password ) ) ); 
		WebResource service = client.resource( url );
		return service;
	}

	private boolean validatePluginConfiguration(
			final String url,
			final String user, 
			final String password) {
		
		if( url == null || user == null || password == null || 
			url.isEmpty() || user.isEmpty() || password.isEmpty() ) {
			return false;
		}
		return true;
	}

	private static CustomMetadataRequest getCustomMetadataRequest(String namespace, String key, String value) {
		CustomMetadataRequest request = new CustomMetadataRequest();
		List<CustomMetadata> list = new ArrayList<CustomMetadata>();
		CustomMetadata item = new CustomMetadata();
		item.setKey( key );
		item.setValue( value );
		item.setNamespace( namespace + "#");
		item.setReadOnly( false );
		list.add(item);
		request.setData( list );
		return request;
	}

    /**
     * Overridden for better type safety.
     * If your plugin doesn't really define any property on Descriptor,
     * you don't have to do this. 
     */
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }
    
    public FormValidation doTestConnection(
    		@QueryParameter("nexusUrl") final String nexusUrl,
    		@QueryParameter("nexusUser") final String nexusUser, 
            @QueryParameter("nexusPassword") final String nexusPassword) throws IOException, ServletException {

    	System.out.println("doTestConnection");
    	try {
        	WebResource service = getService(nexusUrl, nexusUser, Secret.fromString( nexusPassword ) );
    		String nexusStatus = service.path("service").path("local").path("status").accept(MediaType.APPLICATION_JSON).get(ClientResponse.class).toString();
        	System.out.println("doTestConnection " + nexusStatus);
    		if( ! nexusStatus.contains( "200 OK") ) {
    	    	System.out.println("Failed");
    			return FormValidation.error("Failed. Please check the configuration.");
    		}
        	
        	return FormValidation.ok("Success");
        } catch (Exception e) {
        	System.out.println("Exception " + e.getMessage() );
            return FormValidation.error("Client error : " + e.getMessage());
        }
    }

	/**
     * Descriptor for {@link NexusMetadataBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/NexusMetadataBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        private String nexusUrl;
        private String nexusUser;
        private Secret nexusPassword;
        
        /**
         * Performs on-the-fly validation of the form field 'key'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckKey(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a key");
            if (value.length() < 4)
                return FormValidation.warning("Isn't the key too short?");
            return FormValidation.ok();
        }
        public FormValidation doCheckValue(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a value");
            if (value.length() < 4)
                return FormValidation.warning("Isn't the key too short?");
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Add metadata to artefact in Nexus repostitory";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            nexusUrl = formData.getString("nexusUrl");
            nexusUser = formData.getString("nexusUser");
            nexusPassword = Secret.fromString( formData.getString("nexusPassword") );
            
            // Can also use req.bindJSON(this, formData);
            // (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            return super.configure(req,formData);
        }

        public String getNexusUrl() {
            return nexusUrl;
        }
        public String getNexusUser() {
            return nexusUser;
        }
        public Secret getNexusPassword() {
            return nexusPassword;
        }
    }
}

