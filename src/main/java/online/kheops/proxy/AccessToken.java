package online.kheops.proxy;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlElement;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AccessToken {
    private static final Logger LOG = Logger.getLogger(AccessToken.class.getName());
    private static Client client = ClientBuilder.newClient();


    private String token;
    private String user;

    @SuppressWarnings("unused")
    static class TokenResponse {
        @XmlElement(name = "access_token")
        String accessToken;
        @XmlElement(name = "token_type")
        String tokenType;
        @XmlElement(name = "expires_in")
        String expiresIn;
        @XmlElement(name = "user")
        String user;
    }

    public static class AccessTokenBuilder {
        private String capability;
        URI authorizationServerRoot;

        private AccessTokenBuilder(URI authorizationServerRoot) {
            this.authorizationServerRoot = authorizationServerRoot;
        }

        public AccessTokenBuilder withCapability(String capability) {
            this.capability = capability;
            return this;
        }

        public AccessToken build() {
            if (capability == null) {
                throw new IllegalStateException("Capability is not set");
            }

            Form form = new Form().param("assertion", capability).param("grant_type", "urn:x-kheops:params:oauth:grant-type:capability");
            URI uri = UriBuilder.fromUri(authorizationServerRoot).path("token").build();

            LOG.info("About to get a token");

            final TokenResponse tokenResponse;
            try {
                tokenResponse = client.target(uri).request("application/json").post(Entity.form(form), TokenResponse.class);
            } catch (ResponseProcessingException e) {
                LOG.log(Level.WARNING,"Unable to obtain a token for capability token", e);
                throw new IllegalStateException("Unable to get a request token for the capability URL", e);
            } catch (Exception e) {
                LOG.log(Level.WARNING,"Other exception Unable to obtain a token for capability token", e);
                throw new IllegalStateException("Unable to get a request token for the capability URL", e);
            }

            return new AccessToken(tokenResponse.accessToken, tokenResponse.user);
        }
    }

    private AccessToken(String token, String user) {
        this.token = token;
        this.user = user;
    }

    public static AccessTokenBuilder createBuilder(URI authorizationServerRoot) {
        return new AccessTokenBuilder(authorizationServerRoot);
    }

    public String getToken() {
        return token;
    }

    public String getUser() {
        return user;
    }
}
