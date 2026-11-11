import { MotivoRecusa } from './procedimento.model';

export const MENSAGENS_RECUSA: Record<MotivoRecusa, string> = {
  NAO_ENCONTRADO: 'Procedimento não encontrado.',
  SEXO_INCOMPATIVEL: 'O procedimento não é permitido para o sexo do paciente.',
  RESTRITO_NA_UNIDADE: 'O procedimento é restrito na unidade de coleta do atendimento.',
  INATIVO: 'O procedimento está inativo.',
  MATERIAL_DIVERSOS_PENDENTE: 'O material do procedimento é do tipo diversos: informe o material.',
  CATEGORIA_INCOMPATIVEL: 'O procedimento não é permitido para a categoria do atendimento.',
  FATURA_FECHADA: 'O atendimento tem fatura fechada nesta fonte pagadora.',
  FATURA_COMPLEMENTAR_FECHADA: 'O atendimento tem fatura fechada na fonte pagadora complementar.',
  PERCENTUAL_COBRANCA: 'A regra de percentual de cobrança da fonte pagadora impede a inclusão.',
  AGENDA_ESPECIFICA: 'O procedimento tem agenda específica e o atendimento é agendado. Confirme a inclusão.',
  ISOLADO_SEM_PRECO: 'O procedimento isolado da fonte pagadora não tem preço definido.',
  INATIVO_SEGUNDA_VERIFICACAO: 'O procedimento está inativo.',
  ANTIBIOGRAMA_SEM_CULTURA: 'O antibiograma do procedimento não está no atendimento.',
  CULTURA_SEM_ANTIBIOGRAMA: 'O antibiograma precisa da cultura correspondente no atendimento.',
  SEM_COBERTURA: 'A fonte pagadora não cobre o procedimento.',
  CODIGO_AMB_NAO_DEFINIDO: 'O procedimento não tem o código da tabela AMB exigido pela autorização online.',
  JA_INCLUIDO: 'O procedimento já está no atendimento.',
  JA_INCLUIDO_POR_PERFIL: 'O procedimento já está no atendimento dentro de um perfil.',
  ERRO_PRAZO: 'Não foi possível calcular o prazo de entrega do procedimento.',
  SEM_PRECO: 'O procedimento não tem preço na tabela da fonte pagadora.',
  TETO_ATENDIMENTO_EXCEDIDO: 'O valor ultrapassa o teto do atendimento na fonte pagadora.',
  TETO_FATURAMENTO_EXCEDIDO: 'O valor ultrapassa o teto de faturamento da fonte pagadora.',
  SALDO_EM_RECALCULO: 'O saldo do teto de faturamento da fonte pagadora está em recálculo.',
  REPETIDO_NO_RECIPIENTE: 'O procedimento já está no mesmo recipiente.',
  FORA_DO_RECORTE: 'Situação fora do recorte da migração.'
};

export const MOTIVOS_COM_MENSAGEM_DO_SERVIDOR: MotivoRecusa[] = [
  'SEM_COBERTURA',
  'TETO_ATENDIMENTO_EXCEDIDO',
  'TETO_FATURAMENTO_EXCEDIDO',
  'SALDO_EM_RECALCULO'
];
