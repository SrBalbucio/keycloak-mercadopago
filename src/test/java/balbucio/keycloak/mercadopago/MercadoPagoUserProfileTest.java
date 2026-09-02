package balbucio.keycloak.mercadopago;

import org.junit.jupiter.api.Test;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MercadoPagoUserProfileTest {

    private static final String FULL_PROFILE = """
            {
              "address": {
                "city": null,
                "neighborhood": null,
                "municipality": null,
                "state": null,
                "zip_code": null,
                "street_name": null,
                "street_number": null
              },
              "first_name": "João",
              "last_name": "Silva",
              "id": 123456789,
              "identification": {
                "number": null,
                "type": "CPF"
              },
              "email": "joao.silva@email.com",
              "nickname": "JOAO.SILVA",
              "phone": {
                "area_code": null,
                "extension": null,
                "number": null
              },
              "user_type": "normal",
              "country_id": "BR",
              "tags": ["user_info_verified"],
              "logo": null,
              "points": {
                "consumed": 0.0,
                "total": 0.0
              },
              "site_id": "MLB",
              "permalink": "https://www.mercadolivre.com/jm/JOAO.SILVA"
            }
            """;

    @Test
    void shouldDeserializeFullProfile() throws IOException {
        MercadoPagoUserProfile profile = JsonSerialization.readValue(
                FULL_PROFILE.getBytes(StandardCharsets.UTF_8), MercadoPagoUserProfile.class);

        assertEquals(123456789L, profile.getId());
        assertEquals("JOAO.SILVA", profile.getNickname());
        assertEquals("joao.silva@email.com", profile.getEmail());
        assertEquals("João", profile.getFirstName());
        assertEquals("Silva", profile.getLastName());
        assertEquals("BR", profile.getCountryId());
        assertEquals("MLB", profile.getSiteId());
        assertEquals("normal", profile.getUserType());
        assertEquals("https://www.mercadolivre.com/jm/JOAO.SILVA", profile.getPermalink());
        assertNull(profile.getLogo());
    }

    @Test
    void shouldDeserializeNumericIdAsLong() throws IOException {
        MercadoPagoUserProfile profile = JsonSerialization.readValue(
                "{\"id\": 9876543210}", MercadoPagoUserProfile.class);

        assertEquals(9876543210L, profile.getId());
    }

    @Test
    void shouldHandleMinimalPayload() throws IOException {
        MercadoPagoUserProfile profile = JsonSerialization.readValue(
                "{\"id\": 42}", MercadoPagoUserProfile.class);

        assertEquals(42L, profile.getId());
        assertNull(profile.getNickname());
        assertNull(profile.getEmail());
        assertNull(profile.getCountryId());
    }

    @Test
    void shouldIgnoreUnknownFields() throws IOException {
        MercadoPagoUserProfile profile = JsonSerialization.readValue(
                "{\"id\": 1, \"unknown_field\": \"value\", \"future_nested\": {\"a\": 1}}",
                MercadoPagoUserProfile.class);

        assertEquals(1L, profile.getId());
        assertNull(profile.getNickname());
    }
}
