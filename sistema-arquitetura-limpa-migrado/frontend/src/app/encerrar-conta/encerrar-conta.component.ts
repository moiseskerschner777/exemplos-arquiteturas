import { Component, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { mensagemDeErro } from '../resultado';

@Component({
  selector: 'app-encerrar-conta',
  imports: [FormsModule],
  template: `
    <h1>Encerrar conta</h1>
    @if (mensagem()) { <p>{{ mensagem() }}</p> }
    @if (erro()) { <p>{{ erro() }}</p> }
    <form (ngSubmit)="encerrar()">
      <label>Conta <input name="conta" [(ngModel)]="conta"></label>
      <button type="submit">Encerrar</button>
    </form>
  `
})
export class EncerrarContaComponent {

  private readonly http = inject(HttpClient);
  conta = '';
  mensagem = signal('');
  erro = signal('');

  encerrar(): void {
    this.mensagem.set('');
    this.erro.set('');
    this.http.post<{ mensagem: string }>(`/api/contas/${encodeURIComponent(this.conta)}/encerramento`, {}).subscribe({
      next: (resposta) => this.mensagem.set(resposta.mensagem),
      error: (resposta: HttpErrorResponse) => this.erro.set(mensagemDeErro(resposta))
    });
  }

}
