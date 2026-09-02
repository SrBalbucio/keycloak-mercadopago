package balbucio.keycloak.mercadopago;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.jbosslog.JBossLog;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.events.EventBuilder;
import org.keycloak.models.KeycloakSession;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@JBossLog
public class MercadoPagoIdentityProvider extends AbstractOAuth2IdentityProvider<MercadoPagoIdentityProviderConfig>
        implements SocialIdentityProvider<MercadoPagoIdentityProviderConfig> {

    public MercadoPagoIdentityProvider(KeycloakSession session, MercadoPagoIdentityProviderConfig config) {
        super(session, config);
    }

    @Override
    protected String getDefaultScopes() {
        return MercadoPagoIdentityProviderConfig.DEFAULT_SCOPE;
    }

    @Override
    protected boolean supportsExternalExchange() {
        return true;
    }

    @Override
    protected String getProfileEndpointForValidation(EventBuilder event) {
        return getConfig().getUserInfoUrl();
    }

    @Override
    protected BrokeredIdentityContext extractIdentityFromProfile(EventBuilder event, JsonNode profile) {
        String id = getJsonProperty(profile, "id");
        if (isBlank(id)) {
            throw new IdentityBrokerException("A resposta da API do Mercado Pago nao inclui o identificador do usuario.");
        }

        BrokeredIdentityContext context = new BrokeredIdentityContext(id, getConfig());
        context.setBrokerUserId(id);
        context.setIdp(this);

        String nickname = getJsonProperty(profile, "nickname");
        String username = isBlank(nickname) ? id : nickname;
        context.setUsername(username);
        context.setModelUsername(username);

        context.setEmail(getJsonProperty(profile, "email"));
        context.setFirstName(getJsonProperty(profile, "first_name"));
        context.setLastName(getJsonProperty(profile, "last_name"));

        context.setUserAttribute("mercadopago.id", id);
        context.setUserAttribute("mercadopago.nickname", nullToEmpty(nickname));
        context.setUserAttribute("mercadopago.first_name", nullToEmpty(getJsonProperty(profile, "first_name")));
        context.setUserAttribute("mercadopago.last_name", nullToEmpty(getJsonProperty(profile, "last_name")));
        context.setUserAttribute("mercadopago.country_id", nullToEmpty(getJsonProperty(profile, "country_id")));
        context.setUserAttribute("mercadopago.site_id", nullToEmpty(getJsonProperty(profile, "site_id")));
        context.setUserAttribute("mercadopago.user_type", nullToEmpty(getJsonProperty(profile, "user_type")));
        context.setUserAttribute("mercadopago.permalink", nullToEmpty(getJsonProperty(profile, "permalink")));

        return context;
    }

    @Override
    protected BrokeredIdentityContext doGetFederatedIdentity(String accessToken) {
        try {
            String body = fetchJson(getConfig().getUserInfoUrl(), accessToken);
            JsonNode profile = JsonSerialization.readValue(body, JsonNode.class);
            return extractIdentityFromProfile(null, profile);
        } catch (IOException e) {
            throw new IdentityBrokerException("Nao foi possivel obter o perfil do usuario na API do Mercado Pago.", e);
        }
    }

    protected String fetchJson(String url, String accessToken) throws IOException {
        HttpGet request = new HttpGet(url);
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        request.addHeader(HttpHeaders.ACCEPT, "application/json");

        try (CloseableHttpResponse response = session.getProvider(HttpClientProvider.class)
                .getHttpClient()
                .execute(request)) {
            int status = response.getStatusLine().getStatusCode();
            String payload = response.getEntity() == null
                    ? ""
                    : EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            if (status != HttpStatus.SC_OK) {
                throw new IdentityBrokerException("Mercado Pago userinfo request failed with status " + status + ": " + payload);
            }
            return payload;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
