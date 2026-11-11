create table procedimento (
    id varchar(50) primary key,
    alerta_agendamento varchar(10000),
    alerta_cadastro varchar(10000),
    alerta_cadastro_web varchar(10000),
    alerta_fechamento_malote text,
    alerta_liberacao_pendencia varchar(10000),
    alerta_liberacao_pendencia_usa_alerta_cadastro boolean not null,
    alerta_liberacao_repeticao varchar(10000),
    alerta_liberacao_repeticao_usa_alerta_cadastro boolean not null,
    alerta_recebimento_setor text,
    alerta_recebimento_triagem text,
    amostra_encaminhada integer,
    antibiograma integer not null,
    bpa_i integer,
    campo_livre_descricao varchar(10000),
    campo_livre_label varchar(200),
    cobranca_variavel integer,
    composto_adicional text,
    composto_convenio text,
    composto_exame text,
    composto_oculto text,
    composto_variavel text,
    descricao varchar(200) not null,
    descricao_laudo varchar(200),
    disponivel_web integer not null,
    grade_armazenamento varchar(50),
    codigo varchar(50) not null,
    id_lis_antigo varchar(50),
    impressao_grupo bigint,
    impressao_ordem integer,
    imprime_dois_assinadores boolean,
    incluir_tsa_automaticamente integer,
    instrucao_jejum integer,
    last_modified timestamp,
    laudo integer not null,
    material varchar(50),
    mnemonico varchar(15) not null,
    necessario_laudador boolean,
    nro_etiqueta integer,
    obs_nao_realizado varchar(10000),
    obs_pendencia_frase varchar(50),
    obs_pendencia_liberacao integer not null,
    observacao_configuracao varchar(10000),
    observacao_interna varchar(10000),
    observacao_web varchar(10000),
    perfil text,
    perfil_tipo integer not null,
    pergunta text,
    permite_definir_quantidade_cadastro_os integer,
    permite_dupla_assinatura integer,
    permite_fracionamento integer not null,
    prazo_codigo_posto text,
    prazo_codigo_rotina varchar(50) not null,
    prazo_codigo_rotina_posto text,
    prazo_minimo varchar(50) not null,
    prazo_minimo_posto text,
    prazo_minimo_posto_tipo text,
    prazo_minimo_tipo integer not null,
    prazo_rotina_posto_tipo text,
    prazo_rotina_posto_utiliza_rotina text,
    prazo_rotina_tipo integer,
    prazo_rotina_utiliza_rotina integer not null,
    recipiente text,
    recipiente_lamina text,
    recipiente_unidade_recipiente text,
    recipiente_unidade_unidade text,
    resultado_parcial integer,
    segmento integer not null,
    setor_codigo_posto text,
    setor_complementar varchar(50),
    setor_exclusivo varchar(50),
    setor_padrao varchar(50),
    setor_por_posto text,
    sexo varchar(50) not null,
    site_disponivel text,
    status integer not null,
    sub_recipiente text,
    tipo_exame integer not null,
    tipo_microbiologia integer,
    tsa varchar(50),
    utiliza_dupla_digitacao boolean,
    utiliza_revisao_resultado boolean,
    utilizado_microbiologia integer,
    versao_check varchar(50),
    volume_total double precision
);
create index procedimento_idx_descricao on procedimento (upper(descricao));
create index procedimento_idx_disponivel_web on procedimento (disponivel_web);
create unique index procedimento_idx_id on procedimento (upper(codigo));
create unique index procedimento_idx_id_lis_antigo on procedimento (upper(id_lis_antigo));
create unique index procedimento_idx_mnemonico on procedimento (upper(mnemonico));
create index procedimento_idx_perfil_tipo on procedimento (perfil_tipo);
create index procedimento_idx_setor_complementar on procedimento (upper(setor_complementar));
create index procedimento_idx_setor_exclusivo on procedimento (upper(setor_exclusivo));
create index procedimento_idx_setor_padrao on procedimento (upper(setor_padrao));
create index procedimento_idx_sexo on procedimento (upper(sexo));
create index procedimento_idx_status on procedimento (status);
create index procedimento_idx_tipo_exame on procedimento (tipo_exame);
create index procedimento_idx_tsa on procedimento (upper(tsa));
create index procedimento_idx_utilizado_microbiologia on procedimento (utilizado_microbiologia);

create table procedimento_recipiente (
    tbl_exame varchar(50) not null,
    id varchar(512) primary key,
    recipiente text,
    element_key varchar(50) not null
);
create unique index procedimento_recipiente_idx_id on procedimento_recipiente (upper(tbl_exame), upper(element_key));

create table procedimento_material_permitido (
    id bigint primary key,
    material_id varchar(50) not null,
    procedimento_id varchar(50) not null,
    versao_check varchar(50)
);
create unique index procedimento_material_permitido_idkey on procedimento_material_permitido (id);
create index procedimento_material_permitido_idx_procedimento on procedimento_material_permitido (upper(procedimento_id));
create unique index procedimento_material_permitido_idx_procedimento_material on procedimento_material_permitido (upper(procedimento_id), upper(material_id));

create table status_procedimento (
    id varchar(50) primary key,
    cor_fonte varchar(50),
    cor_fundo varchar(50),
    descricao varchar(50),
    descricao_web varchar(50),
    frase_observacao_web text,
    grupo text not null,
    codigo varchar(50) not null,
    versao_check varchar(50)
);
create unique index status_procedimento_idx_id on status_procedimento (upper(codigo));

create table material (
    id varchar(50) primary key,
    classe_material_id bigint,
    complemento_obrigatorio boolean,
    descricao text not null,
    equipamento_chave text,
    equipamento_material text,
    grupo varchar(1),
    codigo varchar(50) not null,
    id_lis_antigo varchar(50),
    material_diversos integer,
    status boolean,
    topografia varchar(50),
    versao_check varchar(50)
);
create unique index material_idx_id on material (upper(codigo));
create unique index material_idx_id_lis_antigo on material (upper(id_lis_antigo));

create table recipiente (
    id bigint primary key,
    apelido varchar(10) not null,
    controlar_volume boolean,
    descricao varchar(50) not null,
    etiqueta_adicional varchar(50),
    gera_recipiente_externo integer,
    gera_recipiente_pendente integer,
    id_lis_antigo varchar(50),
    imprimir_copia_etiqueta boolean,
    imprimir_copia_etiqueta_na_liberacao_pendencia boolean,
    imprimir_copia_etiqueta_na_liberacao_repeticao boolean,
    imprimir_copia_etiqueta_na_os boolean,
    imprimir_copia_etiqueta_na_re_impressao_etiquetas boolean,
    imprimir_copia_etiqueta_no_monitor_de_coleta boolean,
    limite_inferior_temperatura_malote double precision,
    limite_superior_temperatura_malote double precision,
    mascara_cbarra varchar(50),
    material varchar(50),
    metrica varchar(50),
    padrao_etiqueta integer,
    peso_volume varchar(50),
    prioridade_coleta integer,
    quantidade_copias_etiqueta integer,
    recipiente_alternativo_id bigint,
    reserva_soroteca varchar(50),
    tipo integer,
    tipo_cbarra varchar(50),
    tipo_recipiente bigint,
    utilizar_quantidade_copias_etiqueta_variavel boolean,
    versao_check varchar(50),
    volume_util double precision
);
create unique index recipiente_idkey on recipiente (id);
create unique index recipiente_idx_apelido on recipiente (upper(apelido));
create unique index recipiente_idx_id_lis_antigo on recipiente (upper(id_lis_antigo));
create index recipiente_idx_prioridade_coleta on recipiente (prioridade_coleta);
create index recipiente_idx_recipiente_alternativo on recipiente (recipiente_alternativo_id);
create index recipiente_idx_tipo_recipiente on recipiente (tipo_recipiente);

create table atendimento (
    id bigint primary key,
    agendamento bigint,
    altura double precision,
    animal bigint,
    categoria integer,
    codigo_os varchar(50) not null,
    data date not null,
    destino varchar(50),
    disponivel_web boolean not null,
    dispositivo_localizacao varchar(50),
    endereco_hospitalar text,
    hora_final time not null,
    hora_inicial time not null,
    imc double precision,
    integracao varchar(50),
    last_modified timestamp,
    lote_caixa varchar(50),
    lote_fechamento bigint,
    nro_identificacao_os_lis_origem varchar(50),
    numero_protocolo varchar(50),
    obs_escritorio text,
    orcamento bigint,
    os_cod_barra text,
    os_origem_pre_impressa boolean not null,
    os_restrita boolean,
    os_status boolean,
    paciente bigint,
    paciente_lis_anterior text,
    pedido_lis varchar(50),
    peso double precision,
    posto bigint not null,
    proprietario bigint,
    recepcionista bigint,
    segmento integer not null,
    senha_atendimento varchar(50),
    sequencia_cbarra_repeticao varchar(50),
    sequencial integer not null,
    solicitante_livre varchar(100),
    tipo_atendimento bigint,
    versao_check varchar(50)
);
create index atendimento_$arq_ordem_servico on atendimento (id);
create unique index atendimento_idkey on atendimento (id);
create index atendimento_idx_agendamento on atendimento (agendamento);
create index atendimento_idx_animal on atendimento (animal);
create index atendimento_idx_categoria on atendimento (categoria);
create unique index atendimento_idx_codigo_os on atendimento (upper(codigo_os));
create index atendimento_idx_data on atendimento (data);
create index atendimento_idx_disponivel_web on atendimento (disponivel_web);
create unique index atendimento_idx_integracao_pedido on atendimento (upper(integracao), upper(nro_identificacao_os_lis_origem));
create index atendimento_idx_last_modified on atendimento (last_modified);
create index atendimento_idx_lote_caixa on atendimento (upper(lote_caixa));
create index atendimento_idx_lote_fechamento on atendimento (lote_fechamento);
create index atendimento_idx_nro_identificacao_os_lis_origem on atendimento (upper(nro_identificacao_os_lis_origem));
create index atendimento_idx_orcamento on atendimento (orcamento);
create index atendimento_idx_os_restrita on atendimento (os_restrita);
create index atendimento_idx_os_status on atendimento (os_status);
create index atendimento_idx_paciente on atendimento (paciente);
create unique index atendimento_idx_pedido_lis on atendimento (upper(pedido_lis));
create index atendimento_idx_posto on atendimento (posto);
create index atendimento_idx_proprietario on atendimento (proprietario);
create index atendimento_idx_recepcionista on atendimento (recepcionista);
create index atendimento_idx_segmento on atendimento (segmento);
create index atendimento_idx_senha_atendimento on atendimento (upper(senha_atendimento));
create index atendimento_idx_sequencial on atendimento (sequencial);
create index atendimento_idx_tipo_atendimento on atendimento (tipo_atendimento);

create table atendimento_procedimento (
    id bigint primary key,
    assinatura_data date,
    assinatura_hora time,
    assinatura_usuario_id bigint,
    data_hora_impressao timestamp,
    data_hora_liberacao timestamp,
    data_os date,
    data_promessa date,
    data_promessa_externa date,
    exportado integer,
    fonte_pagadora_id bigint not null,
    hora_promessa time,
    hora_promessa_externa time,
    last_modified timestamp,
    laudador1_id bigint,
    mascara bigint,
    notificacao_compulsoria boolean,
    os bigint not null,
    perfil varchar(50),
    prioridade varchar(50) not null,
    procedimento varchar(50) not null,
    recoleta boolean,
    requer_motivo_alteracao boolean,
    retificacao boolean,
    setor varchar(50) not null,
    status varchar(50) not null,
    template_laudo_procedimento_id bigint,
    versao_check varchar(50)
);
create index atendimento_procedimento_$tbl_os_procedimento on atendimento_procedimento (id);
create unique index atendimento_procedimento_idkey on atendimento_procedimento (id);
create index atendimento_procedimento_idx_assinatura_data on atendimento_procedimento (assinatura_data);
create index atendimento_procedimento_idx_assinatura_usuario_id on atendimento_procedimento (assinatura_usuario_id);
create index atendimento_procedimento_idx_data_hora_liberacao on atendimento_procedimento (data_hora_liberacao);
create index atendimento_procedimento_idx_data_os on atendimento_procedimento (data_os);
create index atendimento_procedimento_idx_data_promessa on atendimento_procedimento (data_promessa);
create index atendimento_procedimento_idx_data_promessa_externa on atendimento_procedimento (data_promessa_externa);
create index atendimento_procedimento_idx_exportado on atendimento_procedimento (exportado);
create index atendimento_procedimento_idx_fonte_pagadora on atendimento_procedimento (fonte_pagadora_id);
create index atendimento_procedimento_idx_hora_promessa on atendimento_procedimento (hora_promessa);
create index atendimento_procedimento_idx_hora_promessa_externa on atendimento_procedimento (hora_promessa_externa);
create index atendimento_procedimento_idx_last_modified on atendimento_procedimento (last_modified);
create index atendimento_procedimento_idx_laudador1_id on atendimento_procedimento (laudador1_id);
create index atendimento_procedimento_idx_mascara on atendimento_procedimento (mascara);
create index atendimento_procedimento_idx_notificacao_compulsoria on atendimento_procedimento (notificacao_compulsoria);
create index atendimento_procedimento_idx_os on atendimento_procedimento (os);
create index atendimento_procedimento_idx_os_perfil on atendimento_procedimento (os, upper(perfil));
create unique index atendimento_procedimento_idx_os_procedimento on atendimento_procedimento (os, upper(procedimento));
create index atendimento_procedimento_idx_os_status_proc_font on atendimento_procedimento (os, upper(status), upper(procedimento), fonte_pagadora_id);
create index atendimento_procedimento_idx_perfil on atendimento_procedimento (upper(perfil));
create index atendimento_procedimento_idx_prioridade on atendimento_procedimento (upper(prioridade));
create index atendimento_procedimento_idx_procedimento on atendimento_procedimento (upper(procedimento));
create index atendimento_procedimento_idx_setor on atendimento_procedimento (upper(setor));
create index atendimento_procedimento_idx_status on atendimento_procedimento (upper(status));
create index atendimento_procedimento_idx_status_font_data_os_proc_os on atendimento_procedimento (upper(status), fonte_pagadora_id, data_os, upper(procedimento), os);
create index atendimento_procedimento_idx_status_os on atendimento_procedimento (upper(status), os);
create index atendimento_procedimento_idx_status_setor_os on atendimento_procedimento (upper(status), upper(setor), os);
create index atendimento_procedimento_idx_status_setor_procedimento on atendimento_procedimento (upper(status), upper(setor), upper(procedimento));
create index atendimento_procedimento_idx_template_laudo_procedimento_id on atendimento_procedimento (template_laudo_procedimento_id);

create table atendimento_cobranca (
    id bigint primary key,
    adicional bigint,
    ch double precision,
    cobranca_variavel integer,
    composto varchar(50),
    conciliado double precision not null,
    data_conciliacao date,
    data_os date,
    desconto_balcao double precision not null,
    fator_reducao_acrescimo double precision,
    fatura varchar(50),
    fonte_pagadora bigint not null,
    guia bigint,
    guia_outras_despesas bigint,
    imposto_balcao_acrescimo double precision not null,
    imposto_balcao_desconto double precision not null,
    last_modified timestamp,
    liquido_balcao double precision not null,
    negociacao_especial integer,
    os bigint not null,
    os_procedimento_id bigint,
    procedimento varchar(50),
    procedimento_cobranca_variavel varchar(50),
    recebido_balcao double precision not null,
    recurso_conciliacao integer,
    status_conciliacao varchar(50),
    tipo_cobranca integer not null,
    tipo_conciliacao integer,
    vacina_id bigint,
    valor_bruto_balcao double precision not null,
    valor_bruto_fatura double precision not null,
    valor_bruto_fatura_sem_percentual_cobranca double precision,
    versao_check varchar(50)
);
create index atendimento_cobranca_$tbl_os_cobranca on atendimento_cobranca (id);
create unique index atendimento_cobranca_idkey on atendimento_cobranca (id);
create index atendimento_cobranca_idx_cobranca_variavel on atendimento_cobranca (cobranca_variavel);
create index atendimento_cobranca_idx_composto on atendimento_cobranca (upper(composto));
create index atendimento_cobranca_idx_data_conciliacao on atendimento_cobranca (data_conciliacao);
create index atendimento_cobranca_idx_data_os on atendimento_cobranca (data_os);
create index atendimento_cobranca_idx_fatura on atendimento_cobranca (upper(fatura));
create index atendimento_cobranca_idx_fonte_pagadora on atendimento_cobranca (fonte_pagadora);
create index atendimento_cobranca_idx_fonte_pagadora_data_os on atendimento_cobranca (fonte_pagadora, data_os);
create index atendimento_cobranca_idx_fonte_pagadora_fatura_data_os on atendimento_cobranca (fonte_pagadora, upper(fatura), data_os);
create index atendimento_cobranca_idx_guia on atendimento_cobranca (guia);
create index atendimento_cobranca_idx_guia_outras_despesas on atendimento_cobranca (guia_outras_despesas);
create index atendimento_cobranca_idx_last_modified on atendimento_cobranca (last_modified);
create index atendimento_cobranca_idx_negociacao_especial on atendimento_cobranca (negociacao_especial);
create index atendimento_cobranca_idx_os on atendimento_cobranca (os);
create index atendimento_cobranca_idx_os_proc_fonte_pagadora on atendimento_cobranca (fonte_pagadora, os, upper(procedimento));
create index atendimento_cobranca_idx_os_procedimento_id on atendimento_cobranca (os_procedimento_id);
create index atendimento_cobranca_idx_proc_os on atendimento_cobranca (os, upper(procedimento));
create index atendimento_cobranca_idx_procedimento on atendimento_cobranca (upper(procedimento));
create index atendimento_cobranca_idx_procedimento_cobranca_variavel on atendimento_cobranca (upper(procedimento_cobranca_variavel));
create index atendimento_cobranca_idx_status_conciliacao on atendimento_cobranca (upper(status_conciliacao));
create index atendimento_cobranca_idx_tipo_cobranca on atendimento_cobranca (tipo_cobranca);
create index atendimento_cobranca_idx_vacina on atendimento_cobranca (vacina_id);

create table atendimento_dado_cadastral (
    id bigint primary key,
    dado_cadastral bigint not null,
    data_vigencia date not null,
    fonte_pagadora bigint not null,
    os bigint not null,
    resposta text,
    versao_check varchar(50)
);
create index atendimento_dado_cadastral_$tbl_os_dado_cadastral on atendimento_dado_cadastral (id);
create unique index atendimento_dado_cadastral_idkey on atendimento_dado_cadastral (id);
create index atendimento_dado_cadastral_idx_dado_cadastral on atendimento_dado_cadastral (dado_cadastral);
create index atendimento_dado_cadastral_idx_fonte_pagadora on atendimento_dado_cadastral (fonte_pagadora);
create index atendimento_dado_cadastral_idx_os on atendimento_dado_cadastral (os);
create unique index atendimento_dado_cadastral_idx_os_dado_cadastral on atendimento_dado_cadastral (os, fonte_pagadora, dado_cadastral);
create index atendimento_dado_cadastral_idx_resposta on atendimento_dado_cadastral (upper(resposta));

create table paciente (
    id bigint primary key,
    anamnese text,
    autorizacao_resposta text,
    autorizacao_resultado text,
    emancipado integer,
    filler1 varchar(50),
    grau_parentesco varchar(50),
    id_lis_antigo varchar(50),
    id_paciente_lis_origem varchar(50),
    integracao bigint,
    last_modified timestamp,
    media_dias varchar(50),
    motivo varchar(250),
    numero_emancipacao text,
    observacao text,
    pessoa_fisica bigint not null,
    primeira_os varchar(50) not null,
    profissao varchar(50),
    reimpressao_data date,
    reimpressao_usuario varchar(50),
    responsavel bigint,
    status varchar(50) not null,
    total_os integer,
    ultima_os varchar(50) not null,
    versao_check varchar(50)
);
create unique index paciente_idkey on paciente (id);
create unique index paciente_idx_pessoa_fisica on paciente (pessoa_fisica);
create unique index paciente_idx_id_lis_antigo on paciente (upper(id_lis_antigo));
create index paciente_idx_id_paciente_lis_origem on paciente (upper(id_paciente_lis_origem));
create index paciente_idx_integracao on paciente (integracao);
create unique index paciente_idx_integracao_paciente on paciente (integracao, upper(id_paciente_lis_origem));
create index paciente_idx_last_modified on paciente (last_modified);
create index paciente_idx_responsavel on paciente (responsavel);

create table animal (
    id bigint primary key,
    anamnese text,
    data_nascimento date not null,
    familia varchar(50) not null,
    id_lis_antigo varchar(50),
    idade integer,
    integracao bigint,
    motivo varchar(250),
    nome varchar(50) not null,
    observacao text,
    proprietario bigint not null,
    raca bigint,
    sexo varchar(50) not null,
    status varchar(50) not null,
    versao_check varchar(50)
);
create unique index animal_idkey on animal (id);
create unique index animal_idx_id_lis_antigo on animal (upper(id_lis_antigo));
create index animal_idx_integracao on animal (integracao);
create index animal_idx_nome on animal (upper(nome));
create index animal_idx_proprietario on animal (proprietario);
create index animal_idx_status on animal (upper(status));

create table fatura (
    id varchar(50) primary key,
    animal_id bigint,
    convenio bigint not null,
    data_atendimento timestamp,
    data_emissao date,
    data_emissao_nf date,
    data_entrega date,
    data_envio_fatura date,
    data_fatura text,
    data_fechamento date,
    data_recebimento text,
    data_vencimento date,
    desconto text,
    desconto_id text,
    codigo varchar(50) not null,
    last_modified timestamp,
    log_dados_gerais text,
    log_data text,
    log_endereco_ip text,
    log_hora text,
    log_id text,
    log_usuario_id text,
    lotes_guias text,
    nota_fiscal_id text,
    numero_fatura varchar(50),
    origem integer,
    paciente_id bigint,
    protocolos_lotes_guias text,
    qtd_exame integer not null,
    qtd_os integer not null,
    segmento integer,
    situacao varchar(50) not null,
    tipo integer not null,
    total_ch double precision not null,
    total_exame varchar(50),
    total_os varchar(50),
    valor_bruto_ch double precision,
    valor_liquido double precision,
    valor_recebido text,
    valor_total double precision,
    versao_check varchar(50)
);
create index fatura_idx_animal_id on fatura (animal_id);
create index fatura_idx_convenio on fatura (convenio);
create index fatura_idx_convenio_situacao on fatura (convenio, upper(situacao));
create index fatura_idx_data_entrega on fatura (data_entrega);
create index fatura_idx_data_fechamento on fatura (data_fechamento);
create unique index fatura_idx_fatura_fechamento on fatura (convenio, data_fechamento, tipo, segmento, paciente_id, animal_id);
create unique index fatura_idx_id on fatura (upper(codigo));
create index fatura_idx_last_modified on fatura (last_modified);
create index fatura_idx_numero_fatura on fatura (upper(numero_fatura));
create index fatura_idx_paciente_id on fatura (paciente_id);
create index fatura_idx_segmento on fatura (segmento);
create index fatura_idx_situacao on fatura (upper(situacao));
create index fatura_idx_tipo on fatura (tipo);

create table fonte_pagadora (
    id bigint primary key,
    apelido varchar(100),
    autorizacao_agendamento varchar(50),
    bloquear_cadastro_triagem boolean,
    bloquear_expiracao integer,
    bloqueia_cadastro_externo integer,
    bloqueia_cadastro_teto_faturamento_atingido integer,
    categoria varchar(50),
    check_list text,
    classificacao varchar(50) not null,
    codigo_barras_tiss varchar(50),
    codigo_cliente bigint,
    codigo_credenciado varchar(20),
    codigo_guia integer,
    condicao_preenchimeno integer,
    considerar_data_status boolean,
    controla_guias integer,
    dado_cadastral_digito_verificador bigint,
    data_abertura date,
    data_expiracao date,
    data_valor varchar(50),
    desconto text,
    desconto_maximo double precision,
    descricao varchar(200) not null,
    disponivel_web integer,
    emite_estorno integer,
    empresa_id bigint,
    estorna_tsa integer,
    etiqueta_guia_por varchar(50),
    exame_por_guia integer,
    exibe_saldo_restante_os integer,
    fator_calculo_cbhpm double precision,
    fatura_aguardando_amostra integer,
    fatura_parcial integer,
    fatura_pendencia integer,
    faturamento_automatico boolean,
    faturamento_eletronico_autorizacao_online_id bigint,
    faturamento_eletronico_id bigint,
    forma_pagamento_id_controle_credito bigint,
    geracao_lote_guias integer,
    grupo varchar(50),
    grupo2 varchar(50),
    grupo_elegibilidade_interna_id bigint,
    id_lis_antigo varchar(50),
    informacao_atribuicao integer,
    last_modified timestamp,
    liberacao_pagamento varchar(50),
    logomarca bytea,
    lote_guias integer,
    modulo varchar(50),
    modulo_especifico integer,
    motivo varchar(50),
    nota_fiscal_pessoa_fisica_id bigint,
    nota_fiscal_tipo_pessoa integer,
    obrigatorio_caucao integer,
    obrigatorio_guia_operadora integer,
    obrigatorio_guia_principal integer,
    obrigatorio_senha_autorizacao integer,
    observacao text,
    observacao_agendamento text,
    observacao_externa text,
    observacao_faturamento text,
    omite_franquia integer,
    ordernar_procedimentos integer,
    parametros_autorizacao_online varchar(50),
    parametros_gerais_tiss varchar(50),
    primeiro_isolado varchar(50),
    qtd_dias_selecao_fatura integer,
    quantidade_caracteres varchar(50),
    quarto_isolado varchar(50),
    registro_ans varchar(50),
    restricao_guia_fatura integer,
    saldo_atual_controle_credito double precision,
    segundo_isolado varchar(50),
    separa_doc bigint,
    sequencial_guia varchar(50),
    site_disponivel text,
    status integer,
    status_fatura integer,
    tabela bigint,
    tabela_adicional_id bigint,
    tabela_deve_doc bigint,
    tabela_taxa_id bigint,
    template_etiqueta_tubo bigint,
    template_fatura bigint,
    template_fatura_adicionais text,
    template_fatura_glosa bigint,
    template_faturamento_eletronico text,
    template_laudo_id bigint,
    template_previa bigint,
    template_protocolo_entrega bigint,
    terceiro_isolado varchar(50),
    tipo_convenio varchar(50),
    tipo_data_faturamento varchar(50),
    tipo_data_fechamento varchar(50),
    tipo_data_vencimento varchar(50),
    tipo_identificacao_amostra varchar(50),
    tipo_lote_guias integer,
    tipo_periodo_faturamento integer,
    tipo_valor_fechamento text,
    tipo_valor_vencimento varchar(50),
    trilha varchar(50),
    trilha_final varchar(50),
    trilha_inicio varchar(50),
    unidade varchar(50),
    utiliza_controle_credito integer,
    utiliza_controle_teto_faturamento integer,
    utiliza_deve_doc integer,
    utiliza_tsa_adicional varchar(50) not null,
    valor_minimo_destaque_os double precision,
    valor_teto_os double precision,
    versao_check varchar(50)
);
create index fonte_pagadora_$tbl_convenio on fonte_pagadora (id);
create unique index fonte_pagadora_idkey on fonte_pagadora (id);
create index fonte_pagadora_idx_apelido on fonte_pagadora (upper(apelido));
create index fonte_pagadora_idx_codigo_cliente on fonte_pagadora (codigo_cliente);
create index fonte_pagadora_idx_disponivel_web on fonte_pagadora (disponivel_web);
create index fonte_pagadora_idx_faturamento_eletronico_autorizacao_online_id on fonte_pagadora (faturamento_eletronico_autorizacao_online_id);
create index fonte_pagadora_idx_faturamento_eletronico_id on fonte_pagadora (faturamento_eletronico_id);
create index fonte_pagadora_idx_grupo on fonte_pagadora (upper(grupo));
create index fonte_pagadora_idx_grupo2 on fonte_pagadora (upper(grupo2));
create index fonte_pagadora_idx_grupo_elegibilidade_interna_id on fonte_pagadora (grupo_elegibilidade_interna_id);
create unique index fonte_pagadora_idx_id_lis_antigo on fonte_pagadora (upper(id_lis_antigo));
create index fonte_pagadora_idx_nota_fiscal_pessoa_fisica_id on fonte_pagadora (nota_fiscal_pessoa_fisica_id);
create index fonte_pagadora_idx_status on fonte_pagadora (status);
create index fonte_pagadora_idx_tipo_convenio on fonte_pagadora (upper(tipo_convenio));
create index fonte_pagadora_ixd_empresa_id on fonte_pagadora (empresa_id);

create table fonte_pagadora_dado_cadastral (
    id varchar(50) primary key,
    cruzado_gestao_contrato text,
    dado_arquivar text not null,
    dado_cadastro text not null,
    dado_correlacao_tiss text,
    dado_cruzado text,
    dado_dupla_digitacao text,
    dado_impressao text not null,
    dado_obrigatorio text not null,
    dado_tamanho text not null,
    dado_tipo text not null,
    data_inicio text not null,
    codigo varchar(50) not null,
    versao_check varchar(50)
);
create unique index fonte_pagadora_dado_cadastral_idx_id on fonte_pagadora_dado_cadastral (upper(codigo));

create table fonte_pagadora_teto_faturamento (
    id bigint primary key,
    calculando_saldo_periodo boolean,
    dia_final integer,
    dia_inicio integer,
    fonte_pagadora_id bigint not null,
    periodo_sendo_utilizado boolean,
    valor_teto double precision,
    versao_check varchar(50)
);
create unique index fonte_pagadora_teto_faturamento_idkey on fonte_pagadora_teto_faturamento (id);

create table teto_faturamento (
    id bigint primary key,
    data_final date not null,
    data_inicio date not null,
    fonte_pagadora_id bigint not null,
    saldo double precision not null,
    versao_check varchar(50)
);
create unique index teto_faturamento_idkey on teto_faturamento (id);

create table fonte_pagadora_procedimento_isolado (
    id bigint primary key,
    convenio_id bigint not null,
    primeiro_isolado_id varchar(50),
    procedimento_id varchar(50) not null,
    quarto_isolado_id varchar(50),
    segundo_isolado_id varchar(50),
    terceiro_isolado_id varchar(50),
    versao_check varchar(50)
);
create unique index fonte_pagadora_procedimento_isolado_idkey on fonte_pagadora_procedimento_isolado (id);
create index fonte_pagadora_procedimento_isolado_idx_convenio_id on fonte_pagadora_procedimento_isolado (convenio_id);
create unique index fonte_pagadora_procedimento_isolado_idx_convenio_procedimento on fonte_pagadora_procedimento_isolado (convenio_id, upper(procedimento_id));
create index fonte_pagadora_procedimento_isolado_idx_primeiro_isolado_id on fonte_pagadora_procedimento_isolado (upper(primeiro_isolado_id));
create index fonte_pagadora_procedimento_isolado_idx_procedimento_id on fonte_pagadora_procedimento_isolado (upper(procedimento_id));
create index fonte_pagadora_procedimento_isolado_idx_quarto_isolado_id on fonte_pagadora_procedimento_isolado (upper(quarto_isolado_id));
create index fonte_pagadora_procedimento_isolado_idx_segundo_isolado_id on fonte_pagadora_procedimento_isolado (upper(segundo_isolado_id));
create index fonte_pagadora_procedimento_isolado_idx_terceiro_isolado_id on fonte_pagadora_procedimento_isolado (upper(terceiro_isolado_id));

create table gestao_contrato (
    id bigint primary key,
    cruzado_id bigint,
    fonte_pagadora_id bigint not null,
    tipo integer not null,
    versao_check integer
);
create unique index gestao_contrato_idkey on gestao_contrato (id);
create index gestao_contrato_idx_cruzado_id on gestao_contrato (cruzado_id);
create unique index gestao_contrato_idx_fonte_pagadora_id on gestao_contrato (fonte_pagadora_id);
create index gestao_contrato_idx_tipo on gestao_contrato (tipo);

create table gestao_contrato_dado_cadastral (
    id bigint primary key,
    cruzado_item_id bigint not null,
    fator_calculo_cbhpm double precision,
    franquia_tipo integer,
    franquia_valor double precision,
    franquia_valor_limite double precision,
    gestao_contrato_id bigint not null,
    tabela_preco_id bigint,
    unidade_financeira_id varchar(50),
    versao_check integer
);
create unique index gestao_contrato_dado_cadastral_idkey on gestao_contrato_dado_cadastral (id);
create unique index gestao_contrato_dado_cadastral_idx_gestao_contrato_cruzado_item on gestao_contrato_dado_cadastral (gestao_contrato_id, cruzado_item_id);
create index gestao_contrato_dado_cadastral_idx_gestao_contrato_id on gestao_contrato_dado_cadastral (gestao_contrato_id);

create table gestao_contrato_excecao (
    id bigint primary key,
    autorizacao varchar(500),
    fator_calculo_cbhpm double precision,
    gestao_contrato_dado_cadastral_id bigint,
    gestao_contrato_id bigint not null,
    motivo_nao_cobertura varchar(500),
    nao_coberto boolean not null,
    negociacao_especial boolean not null,
    procedimento_id varchar(50),
    tabela_preco_id bigint,
    unidade_financeira_id varchar(50),
    vacina_id bigint,
    versao_check integer
);
create unique index gestao_contrato_excecao_idkey on gestao_contrato_excecao (id);
create index gestao_contrato_excecao_idx_gestao_contrato_dado_cadastral_id on gestao_contrato_excecao (gestao_contrato_dado_cadastral_id);
create index gestao_contrato_excecao_idx_gestao_contrato_id on gestao_contrato_excecao (gestao_contrato_id);
create unique index gestao_contrato_excecao_idx_gestao_contrato_procedimento_gc_dado_cadastral on gestao_contrato_excecao (gestao_contrato_id, upper(procedimento_id), gestao_contrato_dado_cadastral_id);
create unique index gestao_contrato_excecao_idx_gestao_contrato_vacina_gc_dado_cadastral on gestao_contrato_excecao (gestao_contrato_id, vacina_id, gestao_contrato_dado_cadastral_id);
create index gestao_contrato_excecao_idx_nao_coberto on gestao_contrato_excecao (nao_coberto);
create index gestao_contrato_excecao_idx_negociacao_especial on gestao_contrato_excecao (negociacao_especial);

create table unidade_coleta (
    id bigint primary key,
    agendamento_confirmado boolean,
    agendamento_necessario_senha boolean,
    agendamento_presente_unidade boolean,
    amostra_encaminhada integer,
    amostra_encaminhada_triagem boolean,
    assina_eletronica varchar(50) not null,
    assina_manual varchar(50) not null,
    atendimento_ambulatorio integer not null,
    atendimento_internado integer not null,
    atendimento_laboratorio integer not null,
    atendimento_unico integer not null,
    autorizacao_paciente_obrigatoria integer,
    cnes varchar(50),
    cartao_web varchar(50) not null,
    categoria_alimento integer not null,
    categoria_ambiente integer not null,
    categoria_animal integer not null,
    categoria_humano integer not null,
    cns_obrigatorio boolean,
    controle_temperatura_malote integer,
    convenio_nao_permitido text,
    convenio_permitido text,
    cpf_obrigatorio integer not null,
    descricao varchar(500) not null,
    dia_entrega text,
    dia_producao text,
    disponivel_web integer,
    doc_estrangeiro_obrigatorio integer not null,
    email_obrigatorio integer not null,
    empresa_id bigint,
    endereco_bairro varchar(50),
    endereco_cep varchar(50),
    endereco_cidade varchar(50),
    endereco_complemento varchar(500),
    endereco_estado varchar(50),
    endereco_hospitalar boolean,
    endereco_logradouro text,
    endereco_numero varchar(50),
    endereco_obrigatorio integer not null,
    envelopamento varchar(50) not null,
    envia_laudo_por_email integer,
    filiacao_obrigatorio boolean,
    fracionamento boolean,
    gera_recipiente_pendente integer,
    gerar_recipiente_no_cadastro_os varchar(50) not null,
    grupo_resultado_anterior bigint,
    horario_entrega time,
    horario_limite_rotina time,
    id_lis_antigo varchar(50),
    identificacao_veiculo_transporte boolean,
    imagem_resultado bytea,
    imprime_resultado_debito integer not null,
    informacoes_atendimento text,
    integracao_id bigint,
    last_modified timestamp,
    liberar_resultado_com_deve_doc boolean,
    localizacao_google_maps text,
    maioridade integer,
    malote_agrupado_por varchar(50),
    obrigatorio_cadastro_paciente varchar(50) not null,
    obrigatorio_imprimir_nota integer not null,
    paciente_externo_obrigatorio boolean,
    pedido_externo boolean,
    percentual_max_desconto integer,
    percentual_min_sinal integer,
    percentual_min_sinal_caucao integer,
    permite_cadastro_pessoa_sem_cpf boolean,
    permite_desvio_fluxo integer,
    permite_emergencia integer,
    permite_selecionar_prazo_minimo integer,
    pesquisa_paciente_padrao integer not null,
    prazo_online varchar(50) not null,
    prontuario boolean,
    recalcular_prazo integer not null,
    recebe_recipiente_pendente integer not null,
    regiao_id bigint,
    regional varchar(50),
    registro_coleta boolean,
    requer_peso_altura_e_lis boolean,
    requer_peso_altura_lis boolean,
    responsavel_tecnico bigint,
    resultado_anterior_externo integer,
    resultado_anterior_interno integer,
    rg_obrigatorio integer not null,
    segmento_analise_clinica integer not null,
    segmento_analise_especial integer not null,
    segmento_dna integer not null,
    segmento_vacina integer not null,
    separar_material_por_tipo_malote boolean,
    site_disponivel text,
    status integer not null,
    status_inicial_procedimento varchar(50),
    telefone_ddd text,
    telefone_ddi text,
    telefone_numero text,
    telefone_observacao text,
    telefone_tipo text,
    template_atestado varchar(50),
    template_auto_pedido text,
    template_elis varchar(50),
    template_etiqueta_envelope varchar(50),
    template_etiqueta_tubo varchar(50),
    template_laudo_id bigint,
    template_os_escritorio varchar(50),
    template_os_laboratorio varchar(50),
    template_os_paciente varchar(50),
    template_recibo varchar(50),
    tempo_transporte integer not null,
    tipo_atendimento_default varchar(50) not null,
    tipo_atendimento_possivel text,
    tipo_fechamento_malote integer not null,
    tipo_geracao_malote integer not null,
    tipo_liberacao varchar(50) not null,
    transportar_amostras_em varchar(50) not null,
    unidade_atendimento_filas_id varchar(50),
    unidade_atendimento_id bigint not null,
    unidade_coleta_desvio_fluxo_id bigint,
    unidade_destino varchar(50) not null,
    unidade_processamento_destino_id bigint,
    utiliza_pre_impressa integer,
    versao_check varchar(50)
);
create index unidade_coleta_$tbl_posto on unidade_coleta (id);
create unique index unidade_coleta_idkey on unidade_coleta (id);
create index unidade_coleta_idx_descricao on unidade_coleta (upper(descricao));
create index unidade_coleta_idx_disponivel_web on unidade_coleta (disponivel_web);
create index unidade_coleta_idx_endereco_bairro on unidade_coleta (upper(endereco_bairro));
create index unidade_coleta_idx_endereco_cidade on unidade_coleta (upper(endereco_cidade));
create index unidade_coleta_idx_grupo_resultado_anterior on unidade_coleta (grupo_resultado_anterior);
create unique index unidade_coleta_idx_id_lis_antigo on unidade_coleta (upper(id_lis_antigo));
create index unidade_coleta_idx_integracao_id on unidade_coleta (integracao_id);
create index unidade_coleta_idx_regiao on unidade_coleta (regiao_id);
create index unidade_coleta_idx_regional on unidade_coleta (upper(regional));
create unique index unidade_coleta_idx_unidade_atendimento_filas_id on unidade_coleta (upper(unidade_atendimento_filas_id));
create index unidade_coleta_idx_unidade_atendimento_id on unidade_coleta (unidade_atendimento_id);

create table unidade_procedimento_restricao (
    id bigint primary key,
    procedimento_id varchar(50) not null,
    unidade_atendimento_id bigint,
    unidade_coleta_id bigint,
    versao_check varchar(50)
);
create unique index unidade_procedimento_restricao_idkey on unidade_procedimento_restricao (id);
create index unidade_procedimento_restricao_idx_procedimento_id on unidade_procedimento_restricao (upper(procedimento_id));
create unique index unidade_procedimento_restricao_idx_procedimento_id_unidade_atendimento on unidade_procedimento_restricao (upper(procedimento_id), unidade_atendimento_id);
create unique index unidade_procedimento_restricao_idx_procedimento_id_unidade_coleta on unidade_procedimento_restricao (upper(procedimento_id), unidade_coleta_id);
create index unidade_procedimento_restricao_idx_unidade_atendimento_id on unidade_procedimento_restricao (unidade_atendimento_id);
create index unidade_procedimento_restricao_idx_unidade_coleta_id on unidade_procedimento_restricao (unidade_coleta_id);

create table agenda (
    id bigint primary key,
    agrupar_itens boolean,
    descricao text not null,
    encaixe varchar(50) not null,
    exclusiva integer not null,
    execucao integer not null,
    id_lis_antigo varchar(50),
    integracao bigint,
    integracao_dispositivo_localizacao_id bigint,
    item_especifico boolean,
    limite_encaixe_dia integer,
    limite_encaixe_intervalo integer,
    limite_reserva_horario integer not null,
    peso_altura varchar(50),
    prioridade integer not null,
    sala_id bigint not null,
    sexo varchar(50) not null,
    status integer not null,
    tipo integer,
    versao_check integer,
    vigencia_final date,
    vigencia_inicio date
);
create unique index agenda_idkey on agenda (id);
create index agenda_idx_exclusiva on agenda (exclusiva);
create unique index agenda_idx_id_lis_antigo on agenda (upper(id_lis_antigo));
create index agenda_idx_integracao_dispositivo_localizacao_id on agenda (integracao_dispositivo_localizacao_id);
create index agenda_idx_item_especifico on agenda (item_especifico);
create index agenda_idx_pacs on agenda (integracao);
create index agenda_idx_sala_id on agenda (sala_id);
create index agenda_idx_sexo on agenda (upper(sexo));
create index agenda_idx_status on agenda (status);
create index agenda_idx_tipo on agenda (tipo);

create table agenda_procedimento (
    id bigint primary key,
    agenda_id bigint not null,
    prioridade integer,
    procedimento_id varchar(50),
    quantidade_horario integer,
    todos boolean,
    versao_check integer
);
create unique index agenda_procedimento_idkey on agenda_procedimento (id);
create index agenda_procedimento_idx_agenda_id on agenda_procedimento (agenda_id);
create index agenda_procedimento_idx_agenda_procedimento on agenda_procedimento (agenda_id, upper(procedimento_id));
create index agenda_procedimento_idx_agenda_todos on agenda_procedimento (agenda_id, todos);
create index agenda_procedimento_idx_prioridade on agenda_procedimento (prioridade);
create index agenda_procedimento_idx_procedimento_id on agenda_procedimento (upper(procedimento_id));

create table tabela_preco (
    id bigint primary key,
    descricao text not null,
    especificacao integer,
    grupo text,
    id_lis_antigo varchar(50),
    status integer not null,
    tipo integer,
    tipo_tabela integer not null,
    valor_grupo text,
    versao_cbhpm varchar(50),
    versao_check varchar(50)
);
create unique index tabela_preco_idkey on tabela_preco (id);
create index tabela_preco_idx_descricao on tabela_preco (upper(descricao));
create unique index tabela_preco_idx_id_lis_antigo on tabela_preco (upper(id_lis_antigo));
create index tabela_preco_idx_status on tabela_preco (status);

create table tabela_preco_item (
    id bigint primary key,
    ch varchar(50),
    codigo varchar(50),
    descricao_cbhpm varchar(50),
    nome_faturamento text,
    porcentagem_porte double precision,
    porcentagem_uco double precision,
    porte_id varchar(50),
    procedimento_id varchar(50),
    tabela_preco_id bigint not null,
    vacina_id bigint,
    versao_check varchar(50)
);
create unique index tabela_preco_item_idkey on tabela_preco_item (id);
create unique index tabela_preco_item_idx_proc_tabela on tabela_preco_item (tabela_preco_id, upper(procedimento_id));
create index tabela_preco_item_idx_procedimento_id on tabela_preco_item (upper(procedimento_id));
create index tabela_preco_item_idx_tabela_preco_id on tabela_preco_item (tabela_preco_id);
create index tabela_preco_item_idx_vacina_id on tabela_preco_item (vacina_id);
create unique index tabela_preco_item_idx_vacina_tabela on tabela_preco_item (tabela_preco_id, vacina_id);

create table tabela_preco_item_componente (
    id bigint primary key,
    componente_preco_id bigint not null,
    porcentagem double precision,
    preco_item_id bigint not null,
    quantidade double precision,
    unidade_financeira_id varchar(50),
    versao_check varchar(50)
);
create unique index tabela_preco_item_componente_idkey on tabela_preco_item_componente (id);
create index tabela_preco_item_componente_idx_componente_preco_id on tabela_preco_item_componente (componente_preco_id);
create unique index tabela_preco_item_componente_idx_preco_item_componente_preco on tabela_preco_item_componente (preco_item_id, componente_preco_id);
create index tabela_preco_item_componente_idx_preco_item_id on tabela_preco_item_componente (preco_item_id);

create table tabela_preco_item_mat_med (
    id bigint primary key,
    adicional_id bigint not null,
    codigo varchar(50),
    codigo_despesa bigint,
    tabela_preco_id bigint not null,
    tabela_tiss varchar(50),
    unidade_medida bigint,
    valor double precision,
    versao_check varchar(50)
);
create unique index tabela_preco_item_mat_med_idkey on tabela_preco_item_mat_med (id);
create unique index tabela_preco_item_mat_med_idx_tabela_adicional on tabela_preco_item_mat_med (tabela_preco_id, adicional_id);
create index tabela_preco_item_mat_med_idx_tabela_preco_id on tabela_preco_item_mat_med (tabela_preco_id);

create table codigo_cobranca_negociacao_especial (
    id bigint primary key,
    codigo varchar(50),
    os_cobranca bigint,
    versao_check varchar(50)
);
create unique index codigo_cobranca_negociacao_especial_idkey on codigo_cobranca_negociacao_especial (id);
create unique index codigo_cobranca_negociacao_especial_idx_os_cobranca on codigo_cobranca_negociacao_especial (os_cobranca);

create table cruzado_item (
    id bigint primary key,
    arquivo_cruzado bigint not null,
    descricao text not null,
    item_codigo varchar(15) not null,
    versao_check varchar(50)
);
create unique index cruzado_item_idkey on cruzado_item (id);
create index cruzado_item_idx_cruzado on cruzado_item (arquivo_cruzado);
create unique index cruzado_item_idx_cruzado_item_codigo on cruzado_item (arquivo_cruzado, upper(item_codigo));

create table adicional (
    id bigint primary key,
    convenio_id text,
    descricao varchar(50) not null,
    id_lis_antigo varchar(50),
    lote_obrigatorio boolean,
    permitir_fracionamento boolean,
    quantidade_obrigatorio boolean,
    status integer not null,
    tipo varchar(50) not null,
    unidade varchar(20),
    validade_obrigatorio boolean,
    valor_convenio text,
    valor_variavel boolean,
    versao_check varchar(50)
);
create unique index adicional_idkey on adicional (id);
create unique index adicional_idx_id_lis_antigo on adicional (upper(id_lis_antigo), upper(tipo));
create index adicional_idx_tipo on adicional (upper(tipo));

create table unidade_financeira (
    id varchar(50) primary key,
    data_alteracao text,
    data_novo_valor date not null,
    descricao varchar(50) not null,
    codigo varchar(50) not null,
    id_lis_antigo varchar(50),
    responsavel varchar(50) not null,
    responsavel_antigo text,
    status integer not null,
    valor_antigo text,
    valor_atual double precision not null,
    versao_check varchar(50)
);
create unique index unidade_financeira_idx_id on unidade_financeira (upper(codigo));
create unique index unidade_financeira_idx_id_lis_antigo on unidade_financeira (upper(id_lis_antigo));

create table rotina_prazo (
    id varchar(50) primary key,
    descricao varchar(50) not null,
    dias text,
    horario_entrega text,
    horario_limite text,
    codigo varchar(50) not null,
    id_lis_antigo varchar(50),
    num_dias_apos text,
    observacoes varchar(250),
    tipo_dias text,
    versao_check varchar(50)
);
create unique index rotina_prazo_idx_id on rotina_prazo (upper(codigo));
create unique index rotina_prazo_idx_id_lis_antigo on rotina_prazo (upper(id_lis_antigo));

create table setor (
    id varchar(50) primary key,
    assinatura_imagem bytea,
    assinatura_imagem_altura integer,
    assinatura_imagem_largura integer,
    assunto_padrao varchar(100),
    ativo integer not null,
    cnes varchar(30),
    cnes_execucao integer not null,
    descricao varchar(50) not null,
    desistencia_area_padrao_id bigint,
    desistencia_assunto_padrao varchar(100),
    dia_producao text,
    frase_setor text,
    grupo_visualizacao varchar(300),
    codigo varchar(50) not null,
    id_lis_antigo varchar(50),
    intercorrencia_area_padrao_id bigint,
    intercorrencia_assunto_padrao varchar(100),
    laboratorio varchar(50),
    laboratorio_apoio_id bigint,
    last_modified timestamp,
    parametro_amarelo varchar(300),
    parametro_vermelho varchar(300),
    processa_rotina integer not null,
    recebe_amostra varchar(50) not null,
    revisao varchar(50) not null,
    setor_laboratorial_id bigint not null,
    tempo_lock integer not null,
    tipo_liberacao integer not null,
    unidade_atendimento_id bigint not null,
    versao_check varchar(50)
);
create index setor_idx_ativo on setor (ativo);
create index setor_idx_descricao on setor (upper(descricao));
create unique index setor_idx_id on setor (upper(codigo));
create unique index setor_idx_id_lis_antigo on setor (upper(id_lis_antigo));
create index setor_idx_last_modified on setor (last_modified);

create table setor_unidade_processamento (
    id bigint primary key,
    dia_semana text,
    hora_final time,
    hora_inicial time,
    setor_destino_id varchar(50),
    setor_execucao_id varchar(50),
    unidade_coleta_id bigint,
    versao_check varchar(50)
);
create unique index setor_unidade_processamento_idkey on setor_unidade_processamento (id);
create index setor_unidade_processamento_idx_setor_execucao on setor_unidade_processamento (upper(setor_execucao_id));

create table regiao (
    id bigint primary key,
    descricao varchar(50) not null,
    fuso varchar(50) not null,
    status integer not null,
    utiliza_horario_verao boolean not null,
    versao_check varchar(50)
);
create unique index regiao_idkey on regiao (id);

create table regiao_item (
    id bigint primary key,
    data_final date not null,
    data_inicio date not null,
    fuso varchar(50) not null,
    regiao_id bigint not null,
    versao_check varchar(50)
);
create unique index regiao_item_idkey on regiao_item (id);
create index regiao_item_idx_regiao on regiao_item (regiao_id);
create unique index regiao_item_idx_regiao_periodo on regiao_item (regiao_id, data_inicio, data_final);

create table horario_verao (
    id bigint primary key,
    data_final date not null,
    data_inicio date not null,
    fuso varchar(50) not null,
    versao_check varchar(50)
);
create unique index horario_verao_idkey on horario_verao (id);
create unique index horario_verao_idx_periodo on horario_verao (data_inicio, data_final);

create table tuss (
    id bigint primary key,
    codigo_tuss varchar(50) not null,
    descricao_tuss varchar(500) not null,
    grupo varchar(500),
    tipo varchar(50),
    versao_check varchar(50)
);
create unique index tuss_idkey on tuss (id);
create unique index tuss_idx_codigo_tuss on tuss (upper(codigo_tuss));

create table tuss_correlacao (
    id bigint primary key,
    adicional_id bigint,
    fonte_pagadora_codigo_tuss varchar(50),
    fonte_pagadora_id bigint,
    procedimento_id varchar(50),
    procedimento_tuss_id bigint,
    tabela_id varchar(50),
    versao_check varchar(50)
);
create unique index tuss_correlacao_idkey on tuss_correlacao (id);
create unique index tuss_correlacao_idx_adicional_fonte_pagadora on tuss_correlacao (adicional_id, fonte_pagadora_id);
create unique index tuss_correlacao_idx_procedimento_fonte_pagadora on tuss_correlacao (upper(procedimento_id), fonte_pagadora_id);

create table tabela_tiss (
    id varchar(50) primary key,
    descricao varchar(100) not null,
    codigo varchar(50) not null,
    tuss boolean,
    versao_check varchar(50)
);
create unique index tabela_tiss_idx_id on tabela_tiss (upper(codigo));

create table parametros_tiss (
    id varchar(50) primary key,
    carater_atendimento varchar(50),
    criterio_data_fatura_conciliacao_automatica varchar(50),
    dado_cadastral_conciliacao_automatica varchar(50),
    digito_verificador varchar(50),
    geracao_fatura_conciliacao_automatica varchar(50),
    guia_automatica varchar(50) not null,
    guia_tiss_assinatura_beneficiario varchar(50),
    guia_tiss_assinatura_prestador varchar(50),
    guia_tiss_assinatura_solicitante varchar(50),
    codigo varchar(50) not null,
    id_imagem varchar(10),
    identificacao_solicitante varchar(50),
    imprime_codigo_os varchar(50),
    imprime_guia varchar(50),
    imprime_guia_outras_despesas varchar(50),
    numero_final varchar(20),
    numero_guia_recurso_glosa varchar(20),
    numero_inicial varchar(20),
    numero_limite varchar(20),
    quantidade_digitos integer,
    senha_prestador varchar(50),
    tabela varchar(50),
    tabela_material varchar(50),
    tabela_medicamento varchar(50),
    tabela_taxa varchar(50),
    tipo_atendimento varchar(50),
    tipo_saida varchar(50),
    usuario_prestador varchar(50),
    versao_check varchar(50)
);
create unique index parametros_tiss_idx_id on parametros_tiss (upper(codigo));

create table parametros_gerais_tiss (
    id varchar(50) primary key,
    codigo varchar(50) not null,
    id_lis_antigo varchar(50),
    profissional_responsavel bigint not null,
    versao_check varchar(50),
    versao_padrao varchar(50) not null
);
create unique index parametros_gerais_tiss_idx_id on parametros_gerais_tiss (upper(codigo));
create unique index parametros_gerais_tiss_idx_id_lis_antigo on parametros_gerais_tiss (upper(id_lis_antigo));
create index parametros_gerais_tiss_idx_profissional_responsavel on parametros_gerais_tiss (profissional_responsavel);

create table faturamento_eletronico (
    id bigint primary key,
    classe_componente varchar(500),
    codigo_faturamento varchar(50) not null,
    descricao varchar(50) not null,
    versao_check varchar(50)
);
create unique index faturamento_eletronico_idkey on faturamento_eletronico (id);
create unique index faturamento_eletronico_idx_codigo_faturamento on faturamento_eletronico (upper(codigo_faturamento));