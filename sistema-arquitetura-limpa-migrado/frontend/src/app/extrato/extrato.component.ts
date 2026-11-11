import { Component, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { mensagemDeErro } from '../resultado';

interface Movimentacao {
  dataHora: string;
  tipo: string;
  valor: number;
  descricao: string | null;
}

interface Extrato {
  itens: Movimentacao[];
  total: number;
}

@Component({
  selector: 'app-extrato',
  imports: [FormsModule],
  template: `
    <h1>Extrato</h1>
    @if (erro()) { <p>{{ erro() }}</p> }
    @if (extrato(); as e) {
      <table>
        <tr><th>Data</th><th>Tipo</th><th>Valor</th><th>Descrição</th></tr>
        @for (m of e.itens; track $index) {
          <tr><td>{{ m.dataHora }}</td><td>{{ m.tipo }}</td><td>{{ m.valor }}</td><td>{{ m.descricao }}</td></tr>
        }
      </table>
      <p>total: {{ e.total }}</p>
    }
    <form (ngSubmit)="consultar()">
      <label>Conta <input name="conta" [(ngModel)]="conta"></label>
      <label>Página <input name="pagina" [(ngModel)]="pagina"></label>
      <label>Tamanho <input name="tamanho" [(ngModel)]="tamanho"></label>
      <button type="submit">Consultar</button>
    </form>
  `
})
export class ExtratoComponent {

  private readonly http = inject(HttpClient);
  conta = '';
  pagina = '0';
  tamanho = '10';
  extrato = signal<Extrato | null>(null);
  erro = signal('');

  consultar(): void {
    this.extrato.set(null);
    this.erro.set('');
    const params = { pagina: this.pagina, tamanho: this.tamanho };
    this.http.get<Extrato>(`/api/contas/${encodeURIComponent(this.conta)}/extrato`, { params }).subscribe({
      next: (resposta) => this.extrato.set(resposta),
      error: (resposta: HttpErrorResponse) => this.erro.set(mensagemDeErro(resposta))
    });
  }

}
