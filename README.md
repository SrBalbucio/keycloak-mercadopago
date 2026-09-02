# Keycloak Mercado Pago Identity Provider (SPI)

Extensão de **Social Login** do Keycloak para o **Mercado Pago**, permitindo que usuários façam login/cadastro no Keycloak usando sua conta Mercado Pago via OAuth 2.0 (Authorization Code Grant), conforme a documentação oficial: https://www.mercadopago.com.br/developers/pt/docs/security/oauth

Compatível com **Keycloak 26.7.2** (Java 17).

## Endpoints do Mercado Pago utilizados

| Endpoint | URL |
|----------|-----|
| Authorization | `https://auth.mercadopago.com/authorization` |
| Token | `https://api.mercadopago.com/oauth/token` |
| User Info | `https://api.mercadopago.com/users/me` |
| Scope padrão | `offline_access` |

> **Observações:**
> - A troca do código de autorização (`code`) pelo Access Token é feita com `application/x-www-form-urlencoded` (padrão do Keycloak) — o endpoint `/oauth/token` do Mercado Pago também aceita JSON; ambos foram validados.
> - O scope `offline_access` é necessário para receber um `refresh_token` (o Access Token do fluxo Authorization code expira em 180 dias). Scopes `read`/`write` dependem da configuração da sua aplicação no Mercado Pago.
> - O provider suporta **Token Exchange** externo via `/users/me` (`supportsExternalExchange`).

## Mapeamento de atributos

O perfil retornado por `GET https://api.mercadopago.com/users/me` é mapeado para o `BrokeredIdentityContext`:

| Campo Mercado Pago (API) | Destino no Keycloak |
|--------------------------|---------------------|
| `id` | ID federado / `brokerUserId` (convertido para String) |
| `nickname` | `username` (fallback: `id` se o nickname vier vazio) |
| `email` | `email` |
| `first_name` | `firstName` e atributo `mercadopago.first_name` |
| `last_name` | `lastName` e atributo `mercadopago.last_name` |
| `id` | atributo `mercadopago.id` |
| `nickname` | atributo `mercadopago.nickname` |
| `country_id` | atributo `mercadopago.country_id` |
| `site_id` | atributo `mercadopago.site_id` |
| `user_type` | atributo `mercadopago.user_type` |
| `permalink` | atributo `mercadopago.permalink` |

## Build

```bash
mvn clean package
```

O artefato final será gerado em `target/keycloak-mercadopago.jar` (fat jar).

## Deploy

1. Copie `target/keycloak-mercadopago.jar` para o diretório `providers/` do Keycloak:
   ```bash
   cp target/keycloak-mercadopago.jar $KEYCLOAK_HOME/providers/
   ```
2. Reinicie o Keycloak:
   ```bash
   $KEYCLOAK_HOME/bin/kc.sh build
   $KEYCLOAK_HOME/bin/kc.sh start
   # ou para dev: $KEYCLOAK_HOME/bin/kc.sh start-dev
   ```

## Configuração no Keycloak

### 1. Criar a aplicação no Mercado Pago

1. Acesse [Mercado Pago Developers](https://www.mercadopago.com.br/developers/panel/app) e crie uma nova aplicação.
2. Em **Detalhes da aplicação**, configure as **URLs de redirecionamento** com:
   ```
   https://<SEU_KEYCLOAK>/realms/<SEU_REALM>/broker/mercadopago/endpoint
   ```
   > A URL de redirecionamento deve ser **estática** (sem parâmetros extras).
3. (Opcional) Habilite o fluxo **Authorization code com PKCE** nos detalhes da aplicação, caso queira usá-lo no Keycloak.
4. Anote as credenciais:
   - **App ID** (número da aplicação) → será o *Client ID*.
   - **Secret Key** → será o *Client Secret*.

### 2. Configurar o provider no Keycloak

1. No admin console, vá em **Identity Providers → Add provider → Mercado Pago**.
2. Preencha:
   - **Client Id**: App ID da aplicação Mercado Pago.
   - **Client Secret**: Secret Key da aplicação Mercado Pago.
   - **Default Scopes**: `offline_access` (já é o padrão).
   - **User Info URL** *(opcional)*: endpoint da API (padrão `https://api.mercadopago.com/users/me`).
   - **Store Tokens**: habilitado por padrão (mantém access/refresh token para renovação automática).
   - **PKCE**: desligado por padrão. Ative apenas se o PKCE estiver **habilitado na aplicação do Mercado Pago** (campo "PKCE" + "PKCE Method" `S256`).
3. Salve.

### 3. (Opcional) Attribute Mapper

O provider registra o mapper **Mercado Pago Attribute Mapper**, que permite mapear qualquer atributo do contexto broker (ex: `mercadopago.nickname`, `mercadopago.country_id`) para um atributo de usuário do Keycloak, configurável via UI sem recompilar.

## Testes

```bash
mvn test
```

Os testes validam a desserialização do payload de `GET /users/me` do Mercado Pago, incluindo `id` numérico, campos aninhados, payload mínimo e campos desconhecidos.

## Licença

MIT
