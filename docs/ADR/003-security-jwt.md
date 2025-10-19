# ADR 003: Spring Security com JWT

## Contexto

Sistema distribuído requer autenticação e autorização consistente entre múltiplos serviços. Precisamos de um mecanismo stateless que escale horizontalmente.

## Decisão

Implementar **Spring Security com JWT (JSON Web Tokens)** para autenticação e autorização.

## Arquitetura

### Fluxo de Autenticação

```
1. Cliente → POST /api/v1/auth/login
            ↓ (username + password)
2. Auth Controller valida credenciais
            ↓
3. Gera JWT com claims (username, roles, userId)
            ↓
4. Retorna token para cliente
            ↓
5. Cliente inclui token em todas as requisições
   Header: Authorization: Bearer <token>
            ↓
6. JwtAuthenticationFilter intercepta
            ↓
7. Valida token e extrai claims
            ↓
8. Popula SecurityContext
            ↓
9. Controller processa requisição
```

### Estrutura do JWT

```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "admin",
    "roles": ["ROLE_USER", "ROLE_ADMIN"],
    "userId": "user-123",
    "iat": 1697750400,
    "exp": 1697836800
  },
  "signature": "..."
}
```

## Componentes

### 1. JwtUtil (Shared)

Utilitário para geração e validação de tokens:
- `generateToken()`: Cria JWT com claims
- `validateToken()`: Valida assinatura e expiração
- `extractUsername()`: Extrai subject do token
- `extractClaims()`: Extrai claims customizados

### 2. JwtAuthenticationFilter (Shared)

Filtro Spring Security que:
- Intercepta todas as requisições
- Extrai JWT do header Authorization
- Valida token
- Popula SecurityContext com authorities

### 3. SecurityConfig (Por Serviço)

Configuração específica de cada serviço:

**order-command-service:**
- POST /orders: ROLE_USER ou ROLE_ADMIN
- PUT /orders: ROLE_USER ou ROLE_ADMIN
- DELETE /orders: ROLE_ADMIN

**order-query-service:**
- GET /orders: ROLE_USER ou ROLE_ADMIN

**saga-orchestrator:**
- GET /sagas: ROLE_ADMIN

### 4. AuthController

Endpoints de autenticação:
- `POST /api/v1/auth/login`: Login com credenciais
- `POST /api/v1/auth/test-token`: Gerar token de teste

## Vantagens

1. **Stateless**: Sem necessidade de session storage
2. **Escalabilidade**: Serviços podem escalar horizontalmente
3. **Distribuído**: Token válido em todos os serviços
4. **Performance**: Validação local sem chamadas externas
5. **Claims Customizáveis**: Informações adicionais no token
6. **Padrão da Indústria**: OAuth 2.0 / OpenID Connect compatível

## Desvantagens

1. **Token Revocation**: Difícil revogar tokens antes da expiração
2. **Token Size**: Maior que session ID
3. **Secret Management**: Precisa gerenciar chave secreta
4. **Token Theft**: Se roubado, válido até expirar

## Mitigações de Segurança

### 1. Expiração Curta
```yaml
jwt:
  expiration: 86400000  # 24 horas
```

### 2. HTTPS Obrigatório
```yaml
server:
  ssl:
    enabled: true
```

### 3. Secret Forte
- Mínimo 256 bits
- Armazenado em secrets manager (Vault, AWS Secrets)
- Rotação periódica

### 4. Refresh Tokens (Futuro)
- Token de acesso curto (15 min)
- Refresh token longo (7 dias)
- Stored no Redis com revogação

### 5. Token Blacklist (Futuro)
```java
@Component
public class TokenBlacklist {
    private final RedisTemplate<String, String> redis;
    
    public void blacklist(String token, Duration ttl) {
        redis.opsForValue().set("blacklist:" + token, "1", ttl);
    }
    
    public boolean isBlacklisted(String token) {
        return redis.hasKey("blacklist:" + token);
    }
}
```

## Configuração

### application.yml

```yaml
jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000  # 24 horas em ms
```

### Variável de Ambiente

```bash
export JWT_SECRET="your-super-secret-key-minimum-256-bits-change-in-production"
```

## Uso

### 1. Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**Resposta:**
```json
{
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "username": "admin",
    "roles": ["ROLE_USER", "ROLE_ADMIN"],
    "expiresAt": "2024-10-20T22:00:00Z"
  }
}
```

### 2. Usar Token

```bash
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{...}'
```

### 3. Token de Teste (Dev only)

```bash
curl -X POST http://localhost:8080/api/v1/auth/test-token
```

## Roles e Permissões

| Endpoint | Método | Roles Permitidas |
|----------|--------|------------------|
| `/api/v1/auth/**` | POST | Público |
| `/api/v1/orders` | POST | USER, ADMIN |
| `/api/v1/orders` | GET | USER, ADMIN |
| `/api/v1/orders/**` | PUT | USER, ADMIN |
| `/api/v1/orders/**` | DELETE | ADMIN |
| `/api/v1/sagas/**` | GET | ADMIN |
| `/actuator/**` | GET | Público |

## Próximas Melhorias

1. **User Service**: Serviço dedicado para gestão de usuários
2. **OAuth 2.0**: Integração com providers (Google, GitHub)
3. **Refresh Tokens**: Renovação automática
4. **MFA**: Autenticação multi-fator
5. **RBAC Avançado**: Permissões granulares
6. **Audit Log**: Registrar todas as ações autenticadas
7. **Rate Limiting**: Prevenir brute force

## Referências

- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [JWT.io](https://jwt.io/)
- [RFC 7519 - JSON Web Token](https://datatracker.ietf.org/doc/html/rfc7519)
- [OWASP JWT Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html)

