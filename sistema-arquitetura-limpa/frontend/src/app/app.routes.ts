import { Routes } from '@angular/router';
import { AbrirContaComponent } from './abrir-conta/abrir-conta.component';
import { DepositoComponent } from './deposito/deposito.component';
import { EncerrarContaComponent } from './encerrar-conta/encerrar-conta.component';
import { ExtratoComponent } from './extrato/extrato.component';
import { SaqueComponent } from './saque/saque.component';
import { TransferenciaComponent } from './transferencia/transferencia.component';

export const rotas: Routes = [
  { path: 'abrir-conta', component: AbrirContaComponent },
  { path: 'depositar', component: DepositoComponent },
  { path: 'sacar', component: SaqueComponent },
  { path: 'transferir', component: TransferenciaComponent },
  { path: 'encerrar-conta', component: EncerrarContaComponent },
  { path: 'extrato', component: ExtratoComponent }
];
