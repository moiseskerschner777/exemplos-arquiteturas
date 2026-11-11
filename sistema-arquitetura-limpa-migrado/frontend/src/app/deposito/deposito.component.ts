import { Component, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { mensagemDeErro } from '../resultado';

@Component({
  selector: 'app-deposito',
  imports: [FormsModule],
  template: `
    <h1>Depósito</h1>
    @if (mensagem()) { <p>{{ mensagem() }}</p> }
    @if (erro()) { <p>{{ erro() }}</p> }
    <form (ngSubmit)="depositar()">
      <label>Conta <input name="conta" [(ngModel)]="conta"></label>
      <label>Valor <input name="valor" [(ngModel)]="valor"></label>
      <label>Descrição <input name="descricao" [(ngModel)]="descricao"></label>
      <button type="submit">Depositar</button>
    </form>
  `
})
export class DepositoComponent {

  private readonly http = inject(HttpClient);
  conta = '';
  valor = '';
  descricao = '';
  mensagem = signal('');
  erro = signal('');

  depositar(): void {
    this.mensagem.set('');
    this.erro.set('');
    const corpo = { valor: this.valor, descricao: this.descricao };
    this.http.post<{ mensagem: string }>(`/api/contas/${encodeURIComponent(this.conta)}/depositos`, corpo).subscribe({
      next: (resposta) => this.mensagem.set(resposta.mensagem),
      error: (resposta: HttpErrorResponse) => this.erro.set(mensagemDeErro(resposta))
    });
  }

}
