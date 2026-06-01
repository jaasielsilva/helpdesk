import { Injectable, inject } from '@angular/core';
import { ToastrService } from 'ngx-toastr';

export type NotificationType = 'success' | 'error' | 'info' | 'warning';

/**
 * Serviço centralizado de notificações
 * Fornece métodos para exibir toast notifications ao usuário
 */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly toastr = inject(ToastrService);

  /**
   * Exibe notificação de sucesso
   */
  success(message: string, title = '✓ Sucesso'): void {
    this.toastr.success(message, title, {
      positionClass: 'toast-top-right',
      timeOut: 3000,
      progressBar: true,
      progressAnimation: 'increasing'
    });
  }

  /**
   * Exibe notificação de erro
   */
  error(message: string, title = '✗ Erro'): void {
    this.toastr.error(message, title, {
      positionClass: 'toast-top-right',
      timeOut: 5000,
      progressBar: true,
      progressAnimation: 'increasing',
      enableHtml: false
    });
  }

  /**
   * Exibe notificação de informação
   */
  info(message: string, title = 'ℹ Informação'): void {
    this.toastr.info(message, title, {
      positionClass: 'toast-top-right',
      timeOut: 3000,
      progressBar: true,
      progressAnimation: 'increasing'
    });
  }

  /**
   * Exibe notificação de aviso
   */
  warning(message: string, title = '⚠ Aviso'): void {
    this.toastr.warning(message, title, {
      positionClass: 'toast-top-right',
      timeOut: 4000,
      progressBar: true,
      progressAnimation: 'increasing'
    });
  }

  /**
   * Exibe notificação customizada
   */
  show(
    message: string,
    type: NotificationType = 'info',
    title = '',
    options: any = {}
  ): void {
    const defaults = {
      positionClass: 'toast-top-right',
      timeOut: 3000,
      progressBar: true,
      progressAnimation: 'increasing'
    };

    const config = { ...defaults, ...options };

    switch (type) {
      case 'success':
        this.toastr.success(message, title, config);
        break;
      case 'error':
        this.toastr.error(message, title, { ...config, timeOut: 5000 });
        break;
      case 'warning':
        this.toastr.warning(message, title, { ...config, timeOut: 4000 });
        break;
      case 'info':
      default:
        this.toastr.info(message, title, config);
        break;
    }
  }

  /**
   * Limpa todas as notificações ativas
   */
  clear(): void {
    this.toastr.clear();
  }
}
