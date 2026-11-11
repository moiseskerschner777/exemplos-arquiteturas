import { HttpErrorResponse } from '@angular/common/http';

export function mensagemDeErro(resposta: HttpErrorResponse): string {
  return resposta.error?.erro ?? 'erro ao processar';
}
