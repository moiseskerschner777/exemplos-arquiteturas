import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-transferencia',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './transferencia.component.html',
})
export class TransferenciaComponent {
  origem = '';
  destino = '';
  valor = 0;
  descricao = '';
  mensagem = '';
  erro = '';

  constructor(private http: HttpClient) {}

  transferir(): void {
    this.mensagem = '';
    this.erro = '';
    this.http
      .post<{ mensagem: string }>('/api/transferencias', {
        origem: this.origem,
        destino: this.destino,
        valor: this.valor,
        descricao: this.descricao,
      })
      .subscribe({
        next: (resposta) => (this.mensagem = resposta.mensagem),
        error: (falha) => (this.erro = falha.error?.erro ?? 'erro ao transferir'),
      });
  }
}
