# Estrutura e Convenções de Código - QUOD Biometria

## Estrutura do Projeto

O projeto QUOD Biometria segue uma arquitetura em camadas com clara separação de responsabilidades:

```
src/main/java/com/quodbiometria/
├── config/                # Configurações Spring, segurança, beans, etc.
├── controller/            # Controladores REST
├── exception/             # Tratamento de exceções e modelos de erro
├── model/                 # Modelos de dados
│   ├── dto/               # Objetos de transferência de dados
│   │   ├── request/       # DTOs para requests
│   │   └── response/      # DTOs para responses
│   ├── entity/            # Entidades persistentes
│   ├── enums/             # Enumerações
│   └── mappers/           # Conversores entre DTOs e entidades
├── repository/            # Repositórios para acesso ao MongoDB
├── service/               # Camada de serviços com lógica de negócios
│   ├── biometrics/        # Serviços específicos para biometria
│   ├── documents/         # Serviços específicos para documentos
│   ├── fraud/             # Serviços de detecção de fraude
│   └── notification/      # Serviços de notificação
├── util/                  # Classes utilitárias
└── QuodBiometriaApplication.java  # Classe principal
```

## Convenções de Código

### 1. Nomenclatura

- **Classes**: PascalCase (ex: `BiometricService`, `UserController`)
- **Métodos e variáveis**: camelCase (ex: `validateFace()`, `processImage()`)
- **Constantes**: UPPER_SNAKE_CASE (ex: `MAX_IMAGE_SIZE`, `DEFAULT_THRESHOLD`)
- **Pacotes**: lowercase com palavras separadas por pontos (ex: `com.quodbiometria.service`)
- **Arquivos de propriedades**: kebab-case (ex: `application-dev.properties`)

### 2. Estilo de Código

- Usar 4 espaços para indentação, não tabs
- Comprimento máximo de linha: 120 caracteres
- Chaves em nova linha para definições de classe e método
- Chaves na mesma linha para estruturas de controle (if, for, etc.)
- Sempre usar chaves, mesmo para blocos de uma única linha

### 3. JavaDoc

- Todas as classes públicas devem ter documentação JavaDoc
- Todos os métodos públicos devem ser documentados
- Parâmetros e retorno devem ser documentados com `@param` e `@return`
- Exceções devem ser documentadas com `@throws`

Exemplo:

```java
/**
 * Processa uma imagem biométrica facial e realiza validações de segurança.
 * 
 * @param imageData Os bytes da imagem a ser processada
 * @param userId O ID do usuário associado à biometria
 * @param metadata Metadados adicionais para processamento
 * @return Um objeto BiometricResult com o resultado do processamento
 */
public BiometricResult processFaceImage(byte[] imageData, String userId, Map<String, Object> metadata)
    throws InvalidImageException, ProcessingException {
    // Implementação
}
```

### 4. Testes

- Nomear classes de teste com o sufixo `Test` (ex: `BiometricServiceTest`)
- Nomear métodos de teste com o formato `should[Expected]When[Condition]` (ex: `shouldDetectFraudWhenImageIsDeepfake`)
- Organizar testes em seções: given, when, then (arrange, act, assert)
- Usar mocks para dependências externas
- Buscar cobertura mínima de 80% em classes de serviço

### 5. APIs REST

- URLs devem usar kebab-case (ex: `/api/biometric-data`)
- Usar substantivos, não verbos (ex: `/users`, não `/getUsers`)
- Utilizar corretamente os métodos HTTP:
    - GET: para obter recursos
    - POST: para criar recursos
    - PUT: para atualizar recursos completamente
    - PATCH: para atualizar recursos parcialmente
    - DELETE: para remover recursos
- Versionar APIs quando necessário (ex: `/api/v1/biometrics`)
- Retornar códigos HTTP apropriados

### 6. Mensagens de Commit

Seguir o padrão Conventional Commits:

```
<tipo>[escopo opcional]: <descrição>

[corpo opcional]

[rodapé opcional]
```

Tipos comuns:
- feat: Nova funcionalidade
- fix: Correção de bug
- docs: Documentação
- style: Mudanças que não afetam o código (formatação, etc)
- refactor: Refatoração de código
- test: Adição ou correção de testes
- chore: Manutenção (build, dependências, etc)

### 7. Segurança

- Nunca expor informações sensíveis em logs ou responses
- Validar cuidadosamente todos os inputs
- Não armazenar credenciais no código
- Sempre usar parametrização em consultas
- Utilizar sanitização para prevenir ataques de injeção

### 8. Tratamento de Exceções

- Criar exceções específicas ao domínio da aplicação
- Capturar exceções no nível apropriado
- Não retornar stacktraces para o cliente
- Registrar exceções com nível de log adequado
- Usar `@ExceptionHandler` centralizado para manipular exceções

### 9. Dependências

- Preferir injeção de dependências via construtor
- Usar `@RequiredArgsConstructor` do Lombok para injeção automática
- Minimizar o uso de dependências estáticas
- Documentar o propósito de cada dependência injetada

### 10. MongoDB

- Usar nomes descritivos para collections
- Criar índices para campos frequentemente pesquisados
- Evitar relacionamentos complexos (desnormalizar quando necessário)
- Utilizar agregações para consultas complexas
- Testar consultas com volumes de dados representativos