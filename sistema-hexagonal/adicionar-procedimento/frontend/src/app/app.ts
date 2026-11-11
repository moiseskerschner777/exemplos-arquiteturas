import { Component } from '@angular/core';
import { AdicionarProcedimentoComponent } from './procedimento/adicionar-procedimento.component';

@Component({
  selector: 'app-root',
  imports: [AdicionarProcedimentoComponent],
  template: '<app-adicionar-procedimento />'
})
export class App {}
