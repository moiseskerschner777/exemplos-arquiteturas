import { Component, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { mensagemDeErro } from '../resultado';

@Component({
  selector: 'app-transferencia',
  imports: [FormsModule],
  template: `
    <h1>Transferência entre contas</h1>
    @if (mensagem()) { <p>{{ mensagem() }}</p> }
    @if (erro()) { <p>{{ erro() }}</p> }
    <form (ngSubmit)="transferir()">
      <label>Origem <input name="origem" [(ngModel)]="origem"></label>
      <label>Destino <input name="destino" [(ngModel)]="destino"></label>
      <label>Valor <input name="valor" [(ngModel)]="valor"></label>
      <label>Descrição <input name="descricao" [(ngModel)]="descricao"></label>
      <button type="submit">Transferir</button>
    </form>
  `
})
export class TransferenciaComponent {

  private readonly http = inject(HttpClient);
  origem = '';
  destino = '';
  valor = '';
  descricao = '';
  mensagem = signal('');
  erro = signal('');

  transferir(): void {
    this.mensagem.set('');
    this.erro.set('');
    const corpo = { origem: this.origem, destino: this.destino, valor: this.valor, descricao: this.descricao };
    this.http.post<{ mensagem: string }>('/api/transferencias', corpo).subscribe({
      next: (resposta) => this.mensagem.set(resposta.mensagem),
      error: (resposta: HttpErrorResponse) => this.erro.set(mensagemDeErro(resposta))
    });
  }

}
