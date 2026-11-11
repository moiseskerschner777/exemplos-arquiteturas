import { Component, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { mensagemDeErro } from '../resultado';

@Component({
  selector: 'app-saque',
  imports: [FormsModule],
  template: `
    <h1>Saque</h1>
    @if (mensagem()) { <p>{{ mensagem() }}</p> }
    @if (erro()) { <p>{{ erro() }}</p> }
    <form (ngSubmit)="sacar()">
      <label>Conta <input name="conta" [(ngModel)]="conta"></label>
      <label>Valor <input name="valor" [(ngModel)]="valor"></label>
      <label>Descrição <input name="descricao" [(ngModel)]="descricao"></label>
      <button type="submit">Sacar</button>
    </form>
  `
})
export class SaqueComponent {

  private readonly http = inject(HttpClient);
  conta = '';
  valor = '';
  descricao = '';
  mensagem = signal('');
  erro = signal('');

  sacar(): void {
    this.mensagem.set('');
    this.erro.set('');
    const corpo = { valor: this.valor, descricao: this.descricao };
    this.http.post<{ mensagem: string }>(`/api/contas/${encodeURIComponent(this.conta)}/saques`, corpo).subscribe({
      next: (resposta) => this.mensagem.set(resposta.mensagem),
      error: (resposta: HttpErrorResponse) => this.erro.set(mensagemDeErro(resposta))
    });
  }

}
