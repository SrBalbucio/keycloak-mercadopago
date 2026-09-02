package balbucio.keycloak.mercadopago;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;

public class MercadoPagoIdentityProviderConfig extends OAuth2IdentityProviderConfig {

    public static final String DEFAULT_AUTH_URL = "https://auth.mercadopago.com/authorization";
    public static final String DEFAULT_TOKEN_URL = "https://api.mercadopago.com/oauth/token";
    public static final String DEFAULT_USER_INFO_URL = "https://api.mercadopago.com/users/me";
    public static final String DEFAULT_SCOPE = "offline_access";

    public MercadoPagoIdentityProviderConfig() {
        setAuthorizationUrl(DEFAULT_AUTH_URL);
        setTokenUrl(DEFAULT_TOKEN_URL);
        setUserInfoUrl(DEFAULT_USER_INFO_URL);
        setDefaultScope(DEFAULT_SCOPE);
        setStoreToken(true);
    }

    public MercadoPagoIdentityProviderConfig(IdentityProviderModel model) {
        super(model);
    }

    public String getUserInfoUrl() {
        return getConfig().getOrDefault("userInfoUrl", DEFAULT_USER_INFO_URL);
    }

    public void setUserInfoUrl(String userInfoUrl) {
        getConfig().put("userInfoUrl", userInfoUrl);
    }
}
