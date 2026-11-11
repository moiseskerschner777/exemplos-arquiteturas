import { Component, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { mensagemDeErro } from '../resultado';

@Component({
  selector: 'app-abrir-conta',
  imports: [FormsModule],
  template: `
    <h1>Abrir conta</h1>
    @if (mensagem()) { <p>{{ mensagem() }}</p> }
    @if (erro()) { <p>{{ erro() }}</p> }
    <form (ngSubmit)="abrir()">
      <label>Titular <input name="titular" [(ngModel)]="titular"></label>
      <button type="submit">Abrir</button>
    </form>
  `
})
export class AbrirContaComponent {

  private readonly http = inject(HttpClient);
  titular = '';
  mensagem = signal('');
  erro = signal('');

  abrir(): void {
    this.mensagem.set('');
    this.erro.set('');
    this.http.post<{ mensagem: string }>('/api/contas', { titular: this.titular }).subscribe({
      next: (resposta) => this.mensagem.set(resposta.mensagem),
      error: (resposta: HttpErrorResponse) => this.erro.set(mensagemDeErro(resposta))
    });
  }

}
