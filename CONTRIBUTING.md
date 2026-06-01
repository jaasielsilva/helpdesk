# Contribuindo para o Helpdesk

Obrigado por considerar contribuir para o Helpdesk! Este documento fornece diretrizes e instruções para contribuir.

## 📋 Código de Conduta

Este projeto e seus participantes estão comprometidos em fornecer um ambiente acolhedor e livre de assédio para todos.

## 🐛 Reportando Bugs

Antes de criar um relatório de bug, verifique se o problema já foi relatado.

**Para reportar um bug, crie uma Issue com os seguintes detalhes:**

- **Sumário claro e descritivo** para o título
- **Descrição detalhada** do comportamento observado
- **Passos específicos** para reproduzir o problema
- **Comportamento esperado** versus **real**
- **Screenshots** (se aplicável)
- **Seu ambiente** (OS, Java version, Maven version, etc)

## 🚀 Sugerindo Melhorias

Melhorias incluem funcionalidades novas, otimizações de código, e documentação melhorada.

**Para sugerir uma melhoria:**

- Use um título claro e descritivo
- Forneça uma descrição passo-a-passo da sugestão
- Explique por que essa melhoria seria útil
- Liste algumas aplicações/exemplos onde essa funcionalidade existente ajudaria

## ✍️ Pull Requests

- Siga os **guias de estilo** (veja abaixo)
- Siga o **modelo de commits convencionais**
- Inclua **testes apropriados**
- Atualize **documentação** conforme necessário
- Termine seus arquivos com uma **nova linha**

## 🎨 Guia de Estilo

### Java

```java
// ✅ BOM
public class UserService {
    private final UserRepository repository;
    
    public UserService(UserRepository repository) {
        this.repository = repository;
    }
    
    public User findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}

// ❌ RUIM
public class UserService {
    private UserRepository repository;
    
    public User findById(Long id) {
        User user = repository.findById(id).orElse(null);
        if (user == null) {
            throw new RuntimeException("Not found");
        }
        return user;
    }
}
```

### Convenções

- Use **PascalCase** para classes
- Use **camelCase** para variáveis e métodos
- Use **UPPER_SNAKE_CASE** para constantes
- Máximo **120 caracteres** por linha
- Use **4 espaços** para indentação (não tabs)

### Commits Convencionais

```bash
# Tipo: feat, fix, docs, style, refactor, test, chore

git commit -m "feat(auth): adicionar autenticação com JWT"
git commit -m "fix(chamado): corrigir bug de paginação"
git commit -m "docs: atualizar guia de instalação"
git commit -m "refactor(service): melhorar performance de queries"
git commit -m "test(controller): adicionar testes para login"
git commit -m "chore: atualizar dependências"
```

### Branches

```bash
# Feature
git checkout -b feature/nova-funcionalidade

# Bugfix
git checkout -b bugfix/descricao-do-bug

# Hotfix (para produção)
git checkout -b hotfix/versao
```

## 🔍 Checklist para PR

- [ ] Segui o guia de estilo
- [ ] Executei `mvn clean test`
- [ ] Atualizei documentação relevante
- [ ] Adicionei testes para novas features
- [ ] Minhas mudanças não quebram testes existentes
- [ ] Usei nomes descritivos em branches
- [ ] Minhas commits têm mensagens claras

## 📚 Estrutura para Novas Features

```
src/main/java/com/jaasielsilva/helpdesk/
├── model/
│   └── NovaEntidade.java
├── dto/
│   ├── NovaEntidadeCreateRequest.java
│   └── NovaEntidadeResponse.java
├── repository/
│   └── NovaEntidadeRepository.java
├── service/
│   ├── INovaEntidadeService.java
│   └── NovaEntidadeServiceImpl.java
└── controller/
    └── NovaEntidadeController.java
```

## 🧪 Testes

Toda feature deve incluir testes:

```java
@SpringBootTest
public class UserServiceTest {
    
    @MockBean
    private UserRepository repository;
    
    @InjectMocks
    private UserService service;
    
    @Test
    public void testFindById_Success() {
        // Arrange
        Long id = 1L;
        User expected = new User();
        when(repository.findById(id)).thenReturn(Optional.of(expected));
        
        // Act
        User result = service.findById(id);
        
        // Assert
        assertEquals(expected, result);
    }
}
```

## 📖 Documentação

Toda feature deve incluir documentação:

- **JavaDoc** para métodos públicos
- **README** se adicionar dependencies
- **CHANGELOG** para features relevantes

```java
/**
 * Busca um usuário pelo ID.
 * 
 * @param id o ID do usuário
 * @return o usuário encontrado
 * @throws EntityNotFoundException se não encontrado
 */
public User findById(Long id) {
    // ...
}
```

## 🎓 Recursos

- [Git Documentation](https://git-scm.com/doc)
- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [Angular Guide](https://angular.io/guide)
- [Conventional Commits](https://www.conventionalcommits.org/)

## ❓ Dúvidas?

- Abra uma **Discussion**
- Envie um **email**
- Crie uma **Issue** com a tag `question`

---

**Obrigado por contribuir! 🎉**
