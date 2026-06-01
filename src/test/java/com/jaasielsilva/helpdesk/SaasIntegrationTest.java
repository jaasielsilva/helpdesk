package com.jaasielsilva.helpdesk;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SaasIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void superAdminCriaTenantEAdminFazLogin() throws Exception {
        String superToken = login("superadmin", "super@123", null);

        mockMvc.perform(post("/api/empresas")
                        .header("Authorization", "Bearer " + superToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "slug": "acme-test",
                                  "nome": "Acme Test",
                                  "adminUsuario": "acmeadmin",
                                  "adminSenha": "acme@123",
                                  "adminNome": "Admin Acme"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andExpect(jsonPath("$.dados.empresa.slug").value("acme-test"));

        mockMvc.perform(post("/api/empresas")
                        .header("Authorization", "Bearer " + superToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "slug": "acme-test",
                                  "nome": "Acme Duplicada",
                                  "adminUsuario": "outro",
                                  "adminSenha": "outro@123"
                                }
                                """))
                .andExpect(status().isConflict());

        String tenantToken = login("acmeadmin", "acme@123", "acme-test");

        mockMvc.perform(get("/api/chamados")
                        .header("Authorization", "Bearer " + tenantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true));
    }

    @Test
    void loginExigeEmpresaSlugQuandoAmbiguo() throws Exception {
        String superToken = login("superadmin", "super@123", null);

        criarTenant(superToken, "tenant-a", "Tenant A", "shareduser", "shared@123");
        criarTenant(superToken, "tenant-b", "Tenant B", "shareduser", "shared@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"usuario":"shareduser","senha":"shared@123"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("REQUIRES_EMPRESA_SLUG"));

        login("shareduser", "shared@123", "tenant-b");
    }

    private void criarTenant(
            String superToken,
            String slug,
            String nome,
            String adminUsuario,
            String adminSenha) throws Exception {
        mockMvc.perform(post("/api/empresas")
                        .header("Authorization", "Bearer " + superToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                  "slug": "%s",
                                  "nome": "%s",
                                  "adminUsuario": "%s",
                                  "adminSenha": "%s"
                                }
                                """, slug, nome, adminUsuario, adminSenha)))
                .andExpect(status().isCreated());
    }

    private String login(String usuario, String senha, String empresaSlug) throws Exception {
        String body = empresaSlug == null
                ? String.format("{\"usuario\":\"%s\",\"senha\":\"%s\"}", usuario, senha)
                : String.format("{\"usuario\":\"%s\",\"senha\":\"%s\",\"empresaSlug\":\"%s\"}", usuario, senha, empresaSlug);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sucesso").value(true))
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.get("dados").get("accessToken").asText();
    }
}
