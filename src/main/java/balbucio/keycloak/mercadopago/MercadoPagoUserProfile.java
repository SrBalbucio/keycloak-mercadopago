package balbucio.keycloak.mercadopago;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MercadoPagoUserProfile {

    private Long id;

    private String nickname;

    private String email;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("country_id")
    private String countryId;

    @JsonProperty("site_id")
    private String siteId;

    @JsonProperty("user_type")
    private String userType;

    private String permalink;

    private String logo;
}
