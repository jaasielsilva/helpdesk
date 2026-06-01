package com.jaasielsilva.helpdesk.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jaasielsilva.helpdesk.dto.ApiResponse;
import com.jaasielsilva.helpdesk.dto.auth.LoginRequest;
import com.jaasielsilva.helpdesk.dto.auth.LoginResponse;
import com.jaasielsilva.helpdesk.dto.auth.UsuarioLogadoResponse;
import com.jaasielsilva.helpdesk.service.IAuthService;
import com.jaasielsilva.helpdesk.ratelimit.LoginRateLimiter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final IAuthService authService;
    private final LoginRateLimiter rateLimiter;

    public AuthController(IAuthService authService, LoginRateLimiter rateLimiter) {
        this.authService = authService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = obterIp(httpRequest);

        if (!rateLimiter.tryConsume(clientIp)) {
            log.warn("Rate limit atingido para IP={}", clientIp);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.erro("Muitas tentativas de login. Tente novamente em 1 minuto."));
        }

        log.debug("Tentativa de login para '{}' de IP={}", request.usuario(), clientIp);
        LoginResponse login = authService.autenticar(request);
        return ResponseEntity.ok(ApiResponse.sucesso("Login realizado com sucesso", login));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UsuarioLogadoResponse>> me(Authentication authentication) {
        UsuarioLogadoResponse usuario = authService.buscarUsuarioLogado(authentication);
        return ResponseEntity.ok(ApiResponse.sucesso("Usuário autenticado", usuario));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.sucesso("Logout realizado com sucesso"));
    }

    private String obterIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
