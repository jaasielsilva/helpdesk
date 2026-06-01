# 📱 Exemplos Visuais - Toast Notifications

## 1. Toast de Sucesso ✅

```
┌─────────────────────────────────────┐
│ ✓ Sucesso                       ✕   │
├─────────────────────────────────────┤
│ Chamado criado com sucesso!        │
│                                     │
│ [████████████░░░░░░░░░░░░░░░░] 2s │
└─────────────────────────────────────┘

Cor: Verde (#15803d)
Duração: 3s
Som: ✓ opcional
```

## 2. Toast de Erro ❌

```
┌─────────────────────────────────────┐
│ ✗ Erro                          ✕   │
├─────────────────────────────────────┤
│ Usuário ou senha inválidos         │
│                                     │
│ [██████████████░░░░░░░░░░░░░░░░] 4s│
└─────────────────────────────────────┘

Cor: Vermelho (#b91c1c)
Duração: 5s
Importância: Alta
```

## 3. Toast de Validação 400

```
┌─────────────────────────────────────┐
│ ❌ Dados Inválidos              ✕   │
├─────────────────────────────────────┤
│ titulo: Título é obrigatório       │
│ descricao: Min 10 caracteres       │
│                                     │
│ [██████████░░░░░░░░░░░░░░░░░░░░] 5s│
└─────────────────────────────────────┘

Cor: Laranja/Vermelho
HTTP Status: 400
Campos afetados: Mostrados
```

## 4. Toast de Autenticação 401

```
┌─────────────────────────────────────┐
│ 🔐 Não Autenticado              ✕   │
├─────────────────────────────────────┤
│ Sua sessão expirou.                │
│ Faça login novamente.              │
│                                     │
│ [██████░░░░░░░░░░░░░░░░░░░░░░░░░] 5s│
└─────────────────────────────────────┘

Cor: Azul (#2563eb)
HTTP Status: 401
Ação: Redireciona para /login
```

## 5. Toast de Autorização 403

```
┌─────────────────────────────────────┐
│ 🚫 Acesso Negado                ✕   │
├─────────────────────────────────────┤
│ Você não tem permissão para        │
│ executar esta ação.                │
│                                     │
│ [██████████░░░░░░░░░░░░░░░░░░░░] 5s│
└─────────────────────────────────────┘

Cor: Vermelho escuro
HTTP Status: 403
Motivo: Sem permissão
```

## 6. Toast de Não Encontrado 404

```
┌─────────────────────────────────────┐
│ ❓ Não Encontrado               ✕   │
├─────────────────────────────────────┤
│ Chamado não encontrado.            │
│                                     │
│ [████████████████░░░░░░░░░░░░░░] 2s│
└─────────────────────────────────────┘

Cor: Amarelo/Âmbar
HTTP Status: 404
Duração: 3s
```

## 7. Toast de Erro de Servidor 500

```
┌─────────────────────────────────────┐
│ 💥 Erro do Servidor             ✕   │
├─────────────────────────────────────┤
│ Erro interno do servidor.          │
│ Tente novamente mais tarde.        │
│                                     │
│ [████░░░░░░░░░░░░░░░░░░░░░░░░░░░] 5s│
└─────────────────────────────────────┘

Cor: Vermelho brilhante
HTTP Status: 500
Duração: 5s
Importante: Não reintentar automaticamente
```

## 8. Toast de Aviso ⚠️

```
┌─────────────────────────────────────┐
│ ⚠ Aviso                         ✕   │
├─────────────────────────────────────┤
│ Preencha todos os campos            │
│                                     │
│ [███████░░░░░░░░░░░░░░░░░░░░░░░░] 3s│
└─────────────────────────────────────┘

Cor: Amarelo (#f59e0b)
Duração: 4s
```

## 9. Toast de Informação ℹ️

```
┌─────────────────────────────────────┐
│ ℹ Informação                    ✕   │
├─────────────────────────────────────┤
│ Nenhum chamado encontrado.         │
│                                     │
│ [███████████░░░░░░░░░░░░░░░░░░░░] 2s│
└─────────────────────────────────────┘

Cor: Azul claro (#2563eb)
Duração: 3s
```

## Múltiplos Toasts Simultâneos

```
┌─────────────────────────────────────┐
│ ✗ Erro 1                       ✕   │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│ ⚠ Aviso 2                      ✕   │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│ ℹ Info 3                        ✕   │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│ ✓ Sucesso 4                    ✕   │
└─────────────────────────────────────┘

Máximo: 4 toasts simultâneos
Posição: Canto superior direito
Novo toast aparece no topo
```

## Cenários de Uso

### Cenário 1: Login Bem-sucedido

```
1. Usuário: admin
2. Senha: admin@123
3. Click: "Entrar"

Resultado:
┌─────────────────────────────────────┐
│ 🎉 Login Bem-sucedido           ✕   │
├─────────────────────────────────────┤
│ Bem-vindo! Redirecionando...       │
│                                     │
│ [████████████████░░░░░░░░░░░░░░░] 2s│
└─────────────────────────────────────┘

Ação: Redireciona para /dashboard
```

### Cenário 2: Login Falhado

```
1. Usuário: admin
2. Senha: errada
3. Click: "Entrar"

Resultado:
┌─────────────────────────────────────┐
│ 🔐 Não Autenticado              ✕   │
├─────────────────────────────────────┤
│ Usuário ou senha inválidos         │
│                                     │
│ [████░░░░░░░░░░░░░░░░░░░░░░░░░░░] 5s│
└─────────────────────────────────────┘

Ação: Limpa formulário
```

### Cenário 3: Validação de Formulário

```
1. Título: (vazio)
2. Descrição: (vazio)
3. Click: "Criar"

Resultado:
┌─────────────────────────────────────┐
│ ❌ Dados Inválidos              ✕   │
├─────────────────────────────────────┤
│ titulo: Título é obrigatório       │
│ descricao: Descrição é obrigatória │
│                                     │
│ [███████░░░░░░░░░░░░░░░░░░░░░░░░] 5s│
└─────────────────────────────────────┘

Ação: Marca campos com erro
```

### Cenário 4: Criação Bem-sucedida

```
1. Título: "Bug no sistema"
2. Descrição: "Erro ao fazer login"
3. Click: "Criar"

Resultado:
┌─────────────────────────────────────┐
│ ✓ Sucesso                       ✕   │
├─────────────────────────────────────┤
│ Chamado criado com sucesso!        │
│                                     │
│ [████████████████████░░░░░░░░░░░░] 1s│
└─────────────────────────────────────┘

Ação: Lista se atualiza com novo chamado
```

### Cenário 5: Sem Internet

```
1. Desligar conexão de internet
2. Click: "Entrar"

Resultado:
┌─────────────────────────────────────┐
│ 📡 Erro de Rede                 ✕   │
├─────────────────────────────────────┤
│ Erro de conexão.                   │
│ Verifique sua internet.            │
│                                     │
│ [█░░░░░░░░░░░░░░░░░░░░░░░░░░░░░] 5s│
└─────────────────────────────────────┘

Ação: Permite retry manualmente
```

## Posições Disponíveis

```
Top Right (Padrão)       Top Center           Top Left
┌─────┐                  ┌────────┐           ┌─────┐
│Toast│ █░░░          █░░│ Toast  │░░█     █░░│Toast│
└─────┘                  └────────┘           └─────┘

Bottom Right             Bottom Center         Bottom Left
┌─────┐                  ┌────────┐           ┌─────┐
└─────┘ █░░░          █░░└ Toast  ┘░░█     █░░└Toast┘
│Toast│                  │ Toast  │           │Toast│
```

## Cores por Tipo

```
Sucesso:      #15803d (Verde escuro)
              Gradiente: #f0fdf4 → #dcfce7

Erro:         #b91c1c (Vermelho escuro)
              Gradiente: #fef2f2 → #fee2e2

Info:         #2563eb (Azul)
              Gradiente: #eff6ff → #dbeafe

Aviso:        #f59e0b (Âmbar)
              Gradiente: #fef3c7 → #fde68a
```

## Tempos de Duração

```
Sucesso:      3 segundos (operação rápida)
Erro:         5 segundos (precisa ler)
Aviso:        4 segundos
Info:         3 segundos
```

## Animações

```
Entrada:  Fade in + slide left
Saída:    Fade out + slide right
Duração:  200ms

Progress bar:
├ animação linear
├ da esquerda para direita
└ acompanha timeout
```

## Responsividade

```
Desktop (≥ 860px):
┌─────────────────────────┐
│ ✓ Toast (400px width)  │
└─────────────────────────┘

Mobile (< 860px):
┌──────────────┐
│ ✓ Toast     │
│ (auto width)│
└──────────────┘
```

---

**Todos os toasts aparecem no canto superior direito com barra de progresso e botão fechar.**

