import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Chamado } from '../../core/models/chamado';
import { AuthService } from '../../core/services/auth.service';
import { ChamadoService } from '../../core/services/chamado.service';

export interface StatusBar {
  label: string;
  count: number;
  pct: number;
  color: string;
}

export interface DayPoint {
  label: string;
  count: number;
  pct: number;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [DatePipe, DecimalPipe, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  private readonly chamadoService = inject(ChamadoService);
  readonly authService = inject(AuthService);

  chamados: Chamado[] = [];
  carregando = true;
  agora = new Date();

  // --- KPIs ---
  get total(): number { return this.chamados.length; }
  get abertos(): number { return this.por('ABERTO'); }
  get emAtendimento(): number { return this.por('EM_ATENDIMENTO'); }
  get fechados(): number { return this.por('FECHADO') + this.por('RESOLVIDO'); }
  get taxaResolucao(): number {
    return this.total ? Math.round((this.fechados / this.total) * 100) : 0;
  }

  // --- Gráfico de barras por status ---
  get statusBars(): StatusBar[] {
    const map: Record<string, { label: string; color: string }> = {
      ABERTO:          { label: 'Aberto',         color: '#7c3aed' },
      EM_ATENDIMENTO:  { label: 'Em atendimento',  color: '#f59e0b' },
      RESOLVIDO:       { label: 'Resolvido',       color: '#10b981' },
      FECHADO:         { label: 'Fechado',         color: '#94a3b8' },
    };
    const max = Math.max(...Object.keys(map).map(s => this.por(s)), 1);
    return Object.entries(map).map(([status, meta]) => ({
      label: meta.label,
      count: this.por(status),
      pct: Math.round((this.por(status) / max) * 100),
      color: meta.color,
    }));
  }

  // --- Gráfico de linha: chamados por dia (últimos 7 dias) ---
  get weekPoints(): DayPoint[] {
    const days: DayPoint[] = [];
    for (let i = 6; i >= 0; i--) {
      const d = new Date();
      d.setDate(d.getDate() - i);
      const key = d.toISOString().slice(0, 10);
      const count = this.chamados.filter(c => c.dataCriacao?.slice(0, 10) === key).length;
      days.push({
        label: d.toLocaleDateString('pt-BR', { weekday: 'short' }),
        count,
        pct: 0,
      });
    }
    const max = Math.max(...days.map(d => d.count), 1);
    return days.map(d => ({ ...d, pct: Math.round((d.count / max) * 100) }));
  }

  // --- Tabela recentes ---
  get recentes(): Chamado[] {
    return [...this.chamados]
      .sort((a, b) => new Date(b.dataCriacao).getTime() - new Date(a.dataCriacao).getTime())
      .slice(0, 5);
  }

  ngOnInit(): void {
    this.chamadoService.listar(0, 500).subscribe({
      next: (page) => {
        this.chamados = page.content;
        this.carregando = false;
      },
      error: () => { this.carregando = false; }
    });
  }

  private por(status: string): number {
    return this.chamados.filter(c => c.status === status).length;
  }

  statusClass(status: string): string {
    const map: Record<string, string> = {
      ABERTO: 'badge-blue',
      EM_ATENDIMENTO: 'badge-yellow',
      RESOLVIDO: 'badge-green',
      FECHADO: 'badge-gray',
    };
    return map[status] ?? 'badge-gray';
  }

  statusLabel(status: string): string {
    const map: Record<string, string> = {
      ABERTO: 'Aberto',
      EM_ATENDIMENTO: 'Em atendimento',
      RESOLVIDO: 'Resolvido',
      FECHADO: 'Fechado',
    };
    return map[status] ?? status;
  }
}
