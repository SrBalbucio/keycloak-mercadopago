package balbucio.keycloak.mercadopago;

import com.google.auto.service.AutoService;
import org.keycloak.OAuth2Constants;
import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.List;

@AutoService(SocialIdentityProviderFactory.class)
public class MercadoPagoIdentityProviderFactory extends AbstractIdentityProviderFactory<MercadoPagoIdentityProvider>
        implements SocialIdentityProviderFactory<MercadoPagoIdentityProvider> {

    public static final String PROVIDER_ID = "mercadopago";

    @Override
    public String getName() {
        return "Mercado Pago";
    }

    @Override
    public MercadoPagoIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
        MercadoPagoIdentityProviderConfig config = new MercadoPagoIdentityProviderConfig(model);
        if (isBlank(config.getAuthorizationUrl())) {
            config.setAuthorizationUrl(MercadoPagoIdentityProviderConfig.DEFAULT_AUTH_URL);
        }
        if (isBlank(config.getTokenUrl())) {
            config.setTokenUrl(MercadoPagoIdentityProviderConfig.DEFAULT_TOKEN_URL);
        }
        if (isBlank(config.getUserInfoUrl())) {
            config.setUserInfoUrl(MercadoPagoIdentityProviderConfig.DEFAULT_USER_INFO_URL);
        }
        if (isBlank(config.getDefaultScope())) {
            config.setDefaultScope(MercadoPagoIdentityProviderConfig.DEFAULT_SCOPE);
        }
        if (config.isPkceEnabled() && isBlank(config.getPkceMethod())) {
            config.setPkceMethod(OAuth2Constants.PKCE_METHOD_S256);
        }
        return new MercadoPagoIdentityProvider(session, config);
    }

    @Override
    public MercadoPagoIdentityProviderConfig createConfig() {
        return new MercadoPagoIdentityProviderConfig();
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name("userInfoUrl")
                .type(ProviderConfigProperty.STRING_TYPE)
                .label("User Info URL")
                .defaultValue(MercadoPagoIdentityProviderConfig.DEFAULT_USER_INFO_URL)
                .helpText("Endpoint da API do Mercado Pago usado para carregar o perfil do usuario.")
                .add()
                .property()
                .name(OAuth2IdentityProviderConfig.PKCE_ENABLED)
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .label("PKCE")
                .defaultValue("false")
                .helpText("Ativa o PKCE (Proof Key for Code Exchange) S256 no fluxo de autorizacao. "
                        + "O PKCE tambem precisa estar habilitado na aplicacao no Mercado Pago.")
                .add()
                .property()
                .name(OAuth2IdentityProviderConfig.PKCE_METHOD)
                .type(ProviderConfigProperty.LIST_TYPE)
                .options(OAuth2Constants.PKCE_METHOD_S256, OAuth2Constants.PKCE_METHOD_PLAIN)
                .defaultValue(OAuth2Constants.PKCE_METHOD_S256)
                .label("PKCE Method")
                .helpText("Metodo usado para gerar o code_challenge quando o PKCE esta ativo.")
                .add()
                .build();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
