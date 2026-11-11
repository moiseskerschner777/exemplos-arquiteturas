export type MotivoRecusa =
  | 'NAO_ENCONTRADO'
  | 'SEXO_INCOMPATIVEL'
  | 'RESTRITO_NA_UNIDADE'
  | 'INATIVO'
  | 'MATERIAL_DIVERSOS_PENDENTE'
  | 'CATEGORIA_INCOMPATIVEL'
  | 'FATURA_FECHADA'
  | 'FATURA_COMPLEMENTAR_FECHADA'
  | 'PERCENTUAL_COBRANCA'
  | 'AGENDA_ESPECIFICA'
  | 'ISOLADO_SEM_PRECO'
  | 'INATIVO_SEGUNDA_VERIFICACAO'
  | 'ANTIBIOGRAMA_SEM_CULTURA'
  | 'CULTURA_SEM_ANTIBIOGRAMA'
  | 'SEM_COBERTURA'
  | 'CODIGO_AMB_NAO_DEFINIDO'
  | 'JA_INCLUIDO'
  | 'JA_INCLUIDO_POR_PERFIL'
  | 'ERRO_PRAZO'
  | 'SEM_PRECO'
  | 'TETO_ATENDIMENTO_EXCEDIDO'
  | 'TETO_FATURAMENTO_EXCEDIDO'
  | 'SALDO_EM_RECALCULO'
  | 'REPETIDO_NO_RECIPIENTE'
  | 'FORA_DO_RECORTE';

export interface AdicionarProcedimentoDtoRequest {
  mnemonico: string;
  sexoPaciente: string;
  fontePagadoraId: number;
  materialId: string | null;
  materialDescricao: string | null;
  complementoMaterial: string | null;
  confirmouAgendamento: boolean;
  ignorarValidacoes: boolean;
}

export interface Pendencia {
  motivo: MotivoRecusa;
  mensagem: string | null;
}

export interface ProcedimentoAdicionadoDtoResponse {
  procedimentoId: string;
  mnemonico: string;
  descricao: string;
  fontePagadoraId: number;
  valor: number;
  negociacaoEspecial: boolean;
  entrega: string | null;
  prazoMinimo: string | null;
  materialId: string | null;
  materialDescricao: string;
  materialComplementoObrigatorio: boolean;
  setorId: string | null;
  recipientes: string[];
  recipientesAmostraEncaminhada: string[];
  recipientesNaoPermitemAmostraEncaminhada: string[];
  tsaId: string | null;
  fraseAutorizacao: string | null;
  alertaCadastro: string | null;
  pendencias: Pendencia[];
}

export interface InclusaoRecusadaDtoResponse {
  motivo: MotivoRecusa;
  codigoCos: string;
  retornoCos: number;
  mensagem: string | null;
  materialId: string | null;
  materialDescricao: string | null;
  materiaisPermitidos: string[] | null;
}
