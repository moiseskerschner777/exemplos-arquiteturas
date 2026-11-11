import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { DatePipe, DecimalPipe } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { CheckboxModule } from 'primeng/checkbox';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { SelectModule } from 'primeng/select';
import { TagModule } from 'primeng/tag';
import { ProcedimentoService } from './procedimento.service';
import { MENSAGENS_RECUSA, MOTIVOS_COM_MENSAGEM_DO_SERVIDOR } from './mensagens-recusa';
import {
  AdicionarProcedimentoDtoRequest,
  InclusaoRecusadaDtoResponse,
  MotivoRecusa,
  Pendencia,
  ProcedimentoAdicionadoDtoResponse
} from './procedimento.model';

interface MaterialDiversos {
  materialId: string;
  materiaisPermitidos: string[];
  descricao: string;
  complemento: string;
}

@Component({
  selector: 'app-adicionar-procedimento',
  imports: [
    FormsModule,
    DatePipe,
    DecimalPipe,
    ButtonModule,
    CardModule,
    CheckboxModule,
    DialogModule,
    InputNumberModule,
    InputTextModule,
    MessageModule,
    SelectModule,
    TagModule
  ],
  templateUrl: './adicionar-procedimento.component.html',
  styleUrl: './adicionar-procedimento.component.css'
})
export class AdicionarProcedimentoComponent {
  private readonly service = inject(ProcedimentoService);

  protected readonly sexos = [
    { rotulo: 'Masculino', valor: 'M' },
    { rotulo: 'Feminino', valor: 'F' },
    { rotulo: 'Não informado', valor: '' }
  ];

  protected atendimentoId = 15;
  protected mnemonico = '';
  protected sexoPaciente = 'M';
  protected fontePagadoraId = 1;
  protected materialId = '';
  protected materialDescricao = '';
  protected complementoMaterial = '';
  protected ignorarValidacoes = false;

  protected readonly enviando = signal(false);
  protected readonly item = signal<ProcedimentoAdicionadoDtoResponse | null>(null);
  protected readonly recusa = signal<InclusaoRecusadaDtoResponse | null>(null);
  protected readonly falha = signal<string | null>(null);
  protected readonly materialDiversos = signal<MaterialDiversos | null>(null);
  protected readonly pedeConfirmacaoDeAgenda = computed(
    () => this.item()?.pendencias.some(pendencia => pendencia.motivo === 'AGENDA_ESPECIFICA') ?? false
  );

  private ultimoPedido: AdicionarProcedimentoDtoRequest | null = null;

  protected adicionar(): void {
    this.enviar({
      mnemonico: this.mnemonico.trim(),
      sexoPaciente: this.sexoPaciente,
      fontePagadoraId: this.fontePagadoraId,
      materialId: this.textoOuNulo(this.materialId),
      materialDescricao: this.textoOuNulo(this.materialDescricao),
      complementoMaterial: this.textoOuNulo(this.complementoMaterial),
      confirmouAgendamento: false,
      ignorarValidacoes: this.ignorarValidacoes
    });
  }

  protected confirmarAgenda(): void {
    if (this.ultimoPedido) {
      this.enviar({ ...this.ultimoPedido, confirmouAgendamento: true });
    }
  }

  protected informarMaterial(): void {
    const material = this.materialDiversos();
    if (!this.ultimoPedido || !material || !material.descricao.trim()) {
      return;
    }
    this.materialDiversos.set(null);
    this.enviar({
      ...this.ultimoPedido,
      materialId: material.materialId,
      materialDescricao: material.descricao.trim(),
      complementoMaterial: this.textoOuNulo(material.complemento)
    });
  }

  protected cancelarMaterial(): void {
    this.materialDiversos.set(null);
  }

  protected mensagemDoMotivo(motivo: MotivoRecusa): string {
    return MENSAGENS_RECUSA[motivo];
  }

  protected detalheDoServidor(motivo: MotivoRecusa, mensagem: string | null): string | null {
    return MOTIVOS_COM_MENSAGEM_DO_SERVIDOR.includes(motivo) && mensagem ? mensagem.trim() : null;
  }

  protected severidade(pendencia: Pendencia): 'warn' | 'error' {
    return pendencia.motivo.startsWith('TETO') || pendencia.motivo === 'SALDO_EM_RECALCULO' ? 'error' : 'warn';
  }

  private enviar(pedido: AdicionarProcedimentoDtoRequest): void {
    this.ultimoPedido = pedido;
    this.enviando.set(true);
    this.item.set(null);
    this.recusa.set(null);
    this.falha.set(null);
    this.service.adicionar(this.atendimentoId, pedido).subscribe({
      next: item => {
        this.item.set(item);
        this.enviando.set(false);
      },
      error: (erro: HttpErrorResponse) => {
        this.enviando.set(false);
        if (erro.status === 422) {
          this.tratarRecusa(erro.error as InclusaoRecusadaDtoResponse);
        } else if (erro.status === 404) {
          this.falha.set(erro.error?.mensagem ?? 'Dado não encontrado.');
        } else {
          this.falha.set('Não foi possível falar com a aplicação.');
        }
      }
    });
  }

  private tratarRecusa(recusa: InclusaoRecusadaDtoResponse): void {
    if (recusa.motivo === 'MATERIAL_DIVERSOS_PENDENTE' && recusa.materialId) {
      this.materialDiversos.set({
        materialId: recusa.materialId,
        materiaisPermitidos: [recusa.materialId, ...(recusa.materiaisPermitidos ?? []).filter(id => id !== recusa.materialId)],
        descricao: '',
        complemento: ''
      });
    }
    this.recusa.set(recusa);
  }

  private textoOuNulo(texto: string): string | null {
    return texto.trim() ? texto.trim() : null;
  }
}
