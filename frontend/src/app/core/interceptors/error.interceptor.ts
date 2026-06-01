import { Injectable, inject } from '@angular/core';
import {
  HttpRequest,
  HttpHandlerFn,
  HttpEvent,
  HttpInterceptorFn,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { NotificationService } from '../services/notification.service';
import { AuthService } from '../services/auth.service';

/**
 * Interceptor de erro HTTP
 * Captura erros de requisições HTTP e exibe notificações ao usuário
 */
export const errorInterceptor: HttpInterceptorFn = (
  request: HttpRequest<any>,
  next: HttpHandlerFn
): Observable<HttpEvent<any>> => {
  const notificationService = inject(NotificationService);
  const authService = inject(AuthService);

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      handleHttpError(error, notificationService, authService, request);
      return throwError(() => error);
    })
  );
};

/**
 * Trata diferentes tipos de erros HTTP
 */
function handleHttpError(
  error: HttpErrorResponse,
  notificationService: NotificationService,
  authService: AuthService,
  request: HttpRequest<any>
): void {
  if (request.url.includes('/auth/logout')) {
    return;
  }

  const errorResponse = error.error as any;
  let mensagem = 'Erro ao processar requisição';

  // Se a resposta tem a estrutura ApiResponse
  if (errorResponse?.mensagem) {
    mensagem = errorResponse.mensagem;
  }

  switch (error.status) {
    case 400:
      // Bad Request - Validação
      notificationService.error(
        extrairMensagemValidacao(errorResponse) || mensagem,
        '❌ Dados Inválidos'
      );
      break;

    case 401:
      if (request.url.includes('/auth/login')) {
        const msg = errorResponse?.mensagem ?? '';
        if (msg !== 'REQUIRES_EMPRESA_SLUG') {
          notificationService.error(
            'Usuário ou senha incorretos. Verifique suas credenciais.',
            '🔐 Falha na Autenticação'
          );
        }
      } else if (request.url.includes('/auth/logout')) {
        break;
      } else {
        notificationService.error(
          'Sua sessão expirou. Faça login novamente.',
          '🔐 Não Autenticado'
        );
        authService.clearSession();
        window.location.href = '/login';
      }
      break;

    case 403:
      // Forbidden - Sem permissão
      notificationService.error(
        'Você não tem permissão para executar esta ação.',
        '🚫 Acesso Negado'
      );
      break;

    case 404:
      // Not Found - Recurso não encontrado
      notificationService.error(
        mensagem || 'Recurso não encontrado.',
        '❓ Não Encontrado'
      );
      break;

    case 409:
      // Conflict - Conflito de dados
      notificationService.error(
        mensagem || 'Conflito ao processar dados.',
        '⚠️ Conflito'
      );
      break;

    case 500:
      // Internal Server Error
      notificationService.error(
        'Erro interno do servidor. Tente novamente mais tarde.',
        '💥 Erro do Servidor'
      );
      console.error('Server error:', error);
      break;

    case 502:
    case 503:
    case 504:
      // Service Unavailable
      notificationService.error(
        'Serviço temporariamente indisponível. Tente novamente mais tarde.',
        '🔧 Serviço Indisponível'
      );
      break;

    case 0:
      // Erro de rede ou CORS
      notificationService.error(
        'Erro de conexão. Verifique sua internet.',
        '📡 Erro de Rede'
      );
      console.error('Network error:', error);
      break;

    default:
      notificationService.error(
        mensagem || `Erro ${error.status}: ${error.statusText}`,
        '⚠️ Erro'
      );
      break;
  }
}

/**
 * Extrai mensagens de validação da resposta de erro
 */
function extrairMensagemValidacao(errorResponse: any): string | null {
  if (!errorResponse) return null;

  // Se tem array de erros (formato customizado)
  if (Array.isArray(errorResponse.errors)) {
    return errorResponse.errors.join('; ');
  }

  // Se tem object com erros de campo
  if (typeof errorResponse.dados === 'object' && errorResponse.dados !== null) {
    const erros = Object.entries(errorResponse.dados)
      .map(([field, message]) => `${field}: ${message}`)
      .join('; ');
    return erros || null;
  }

  return null;
}
