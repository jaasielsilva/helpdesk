package com.jaasielsilva.helpdesk.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.jaasielsilva.helpdesk.enums.PerfilUsuario;
import com.jaasielsilva.helpdesk.model.Empresa;
import com.jaasielsilva.helpdesk.model.Chamado;
import com.jaasielsilva.helpdesk.model.Empresa;
import com.jaasielsilva.helpdesk.model.Usuario;
import com.jaasielsilva.helpdesk.repository.ChamadoRepository;
import com.jaasielsilva.helpdesk.repository.EmpresaRepository;
import com.jaasielsilva.helpdesk.repository.UsuarioRepository;

@Configuration
public class DataInitializer {

    private static final String DEMO_EMPRESA_SLUG = "demo";
    private static final String DEMO_EMPRESA_NOME = "Empresa Demo";

    @Bean
    CommandLineRunner init(
            UsuarioRepository usuarioRepository,
            EmpresaRepository empresaRepository,
            ChamadoRepository chamadoRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            Empresa demoEmpresa = empresaRepository.findBySlugIgnoreCase(DEMO_EMPRESA_SLUG)
                    .orElseGet(() -> {
                        Empresa empresa = new Empresa();
                        empresa.setSlug(DEMO_EMPRESA_SLUG);
                        empresa.setNome(DEMO_EMPRESA_NOME);
                        empresa.setAtivo(true);
                        return empresaRepository.save(empresa);
                    });

            criarSuperAdminSeNecessario(usuarioRepository, passwordEncoder);
            migrarAdminLegado(usuarioRepository, demoEmpresa, passwordEncoder);
            criarUsuarioSeNecessario(usuarioRepository, demoEmpresa, passwordEncoder,
                    "admin", "Administrador", PerfilUsuario.ADMIN, "admin@123");
            criarUsuarioSeNecessario(usuarioRepository, demoEmpresa, passwordEncoder,
                    "suporte", "Analista de Suporte", PerfilUsuario.SUPORTE, "suporte@123");
            criarUsuarioSeNecessario(usuarioRepository, demoEmpresa, passwordEncoder,
                    "user", "Usuário Final", PerfilUsuario.USER, "user@123");
            migrarChamadosLegados(chamadoRepository, demoEmpresa);

            System.out.println("\n✅ Seed multi-tenant concluído");
            System.out.println("   Empresa demo: " + DEMO_EMPRESA_SLUG);
            System.out.println("   superadmin / super@123  (SUPER_ADMIN - plataforma)");
            System.out.println("   admin / admin@123       (ADMIN - tenant demo)");
            System.out.println("   suporte / suporte@123   (SUPORTE - tenant demo)");
            System.out.println("   user / user@123         (USER - tenant demo)\n");
        };
    }

    private void criarSuperAdminSeNecessario(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        usuarioRepository.findSuperAdminByUsuarioIgnoreCase("superadmin").ifPresentOrElse(
                superAdmin -> atualizarSenhaSeNecessario(superAdmin, passwordEncoder, "super@123", usuarioRepository),
                () -> {
                    Usuario superAdmin = new Usuario();
                    superAdmin.setUsuario("superadmin");
                    superAdmin.setNome("Dono da Plataforma");
                    superAdmin.setSenha(passwordEncoder.encode("super@123"));
                    superAdmin.setPerfil(PerfilUsuario.SUPER_ADMIN);
                    superAdmin.setEmpresa(null);
                    superAdmin.setAtivo(true);
                    usuarioRepository.save(superAdmin);
                });
    }

    private void migrarAdminLegado(
            UsuarioRepository usuarioRepository,
            Empresa demoEmpresa,
            PasswordEncoder passwordEncoder) {
        usuarioRepository.findByUsuarioIgnoreCase("admin").ifPresent(admin -> {
            if (admin.getEmpresa() == null && admin.getPerfil() != PerfilUsuario.SUPER_ADMIN) {
                admin.setEmpresa(demoEmpresa);
                admin.setPerfil(PerfilUsuario.ADMIN);
                atualizarSenhaSeNecessario(admin, passwordEncoder, "admin@123", usuarioRepository);
            }
        });
    }

    private void criarUsuarioSeNecessario(
            UsuarioRepository usuarioRepository,
            Empresa empresa,
            PasswordEncoder passwordEncoder,
            String login,
            String nome,
            PerfilUsuario perfil,
            String senhaPlana) {
        usuarioRepository.findByUsuarioIgnoreCaseAndEmpresaSlug(login, empresa.getSlug()).ifPresentOrElse(
                usuario -> atualizarSenhaSeNecessario(usuario, passwordEncoder, senhaPlana, usuarioRepository),
                () -> {
                    Usuario usuario = new Usuario();
                    usuario.setUsuario(login);
                    usuario.setNome(nome);
                    usuario.setSenha(passwordEncoder.encode(senhaPlana));
                    usuario.setPerfil(perfil);
                    usuario.setEmpresa(empresa);
                    usuario.setAtivo(true);
                    usuarioRepository.save(usuario);
                });
    }

    private void atualizarSenhaSeNecessario(
            Usuario usuario,
            PasswordEncoder passwordEncoder,
            String senhaPlana,
            UsuarioRepository usuarioRepository) {
        if (!usuario.getSenha().startsWith("$2a$") && !usuario.getSenha().startsWith("$2b$")) {
            usuario.setSenha(passwordEncoder.encode(senhaPlana));
            usuarioRepository.save(usuario);
        }
    }

    private void migrarChamadosLegados(ChamadoRepository chamadoRepository, Empresa demoEmpresa) {
        chamadoRepository.findAll().forEach(chamado -> {
            if (chamado.getEmpresa() != null) {
                return;
            }

            Empresa empresa = chamado.getUsuario().getEmpresa() != null
                    ? chamado.getUsuario().getEmpresa()
                    : demoEmpresa;
            chamado.setEmpresa(empresa);
            chamadoRepository.save(chamado);
        });
    }
}
