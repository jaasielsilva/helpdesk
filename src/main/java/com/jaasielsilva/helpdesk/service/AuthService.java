package com.jaasielsilva.helpdesk.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.jaasielsilva.helpdesk.dto.auth.LoginRequest;
import com.jaasielsilva.helpdesk.dto.auth.LoginResponse;
import com.jaasielsilva.helpdesk.dto.auth.UsuarioLogadoResponse;
import com.jaasielsilva.helpdesk.model.Usuario;
import com.jaasielsilva.helpdesk.repository.UsuarioRepository;
import com.jaasielsilva.helpdesk.security.JwtService;
import com.jaasielsilva.helpdesk.security.UsuarioAutenticado;

@Service
public class AuthService implements IAuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final LoginResolver loginResolver;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public AuthService(
            LoginResolver loginResolver,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            UsuarioRepository usuarioRepository) {
        this.loginResolver = loginResolver;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public LoginResponse autenticar(LoginRequest request) {
        String senha = request.senha().trim();
        Usuario usuario = loginResolver.resolve(request);

        log.info("Tentativa de login para usuário '{}' (empresa={})", usuario.getUsuario(),
                usuario.getEmpresa() != null ? usuario.getEmpresa().getSlug() : "plataforma");

        if (!passwordEncoder.matches(senha, usuario.getSenha())) {
            throw new BadCredentialsException("Usuário ou senha inválidos");
        }

        if (!usuario.isAtivo()) {
            throw new BadCredentialsException("Usuário inativo");
        }

        if (usuario.getEmpresa() != null && !usuario.getEmpresa().isAtivo()) {
            throw new BadCredentialsException("Empresa inativa");
        }

        String token = jwtService.gerarToken(usuario);
        UsuarioAutenticado principal = new UsuarioAutenticado(usuario);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        log.info("Login bem-sucedido para usuário '{}'", usuario.getUsuario());
        return new LoginResponse(token, "Bearer", jwtService.getExpirationSeconds(), toResponse(usuario));
    }

    @Override
    public UsuarioLogadoResponse buscarUsuarioLogado(String username) {
        Usuario usuario = usuarioRepository.findByUsuarioIgnoreCase(username.trim())
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado nao encontrado"));
        return toResponse(usuario);
    }

    @Override
    public UsuarioLogadoResponse buscarUsuarioLogado(Authentication authentication) {
        UsuarioAutenticado autenticado = UsuarioDetailsService.requireUsuarioAutenticado(authentication);
        return toResponse(autenticado.getUsuario());
    }

    private UsuarioLogadoResponse toResponse(Usuario usuario) {
        return new UsuarioLogadoResponse(
                usuario.getId(),
                usuario.getUsuario(),
                usuario.getNome(),
                usuario.getPerfil().name(),
                usuario.getEmpresa() != null ? usuario.getEmpresa().getId() : null,
                usuario.getEmpresa() != null ? usuario.getEmpresa().getNome() : null,
                usuario.getEmpresa() != null ? usuario.getEmpresa().getSlug() : null);
    }
}
