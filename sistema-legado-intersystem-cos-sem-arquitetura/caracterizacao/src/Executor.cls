Class caracterizacao.Executor Extends %RegisteredObject
{

ClassMethod Executar(pCenario As %DynamicObject) As %DynamicObject
{
    Set resultado = {"id": (pCenario.id)}
    Set nivel = $TLevel
    New %session
    Try {
        TStart
        Set ajustes = pCenario.ajustes
        If $IsObject(ajustes) {
            Set iterador = ajustes.%GetIterator()
            While iterador.%GetNext(.indice, .sql) {
                Set rs = ##class(%SQL.Statement).%ExecDirect(, sql)
                Throw:(rs.%SQLCODE < 0) ##class(%Exception.SQL).CreateFromSQLCODE(rs.%SQLCODE, rs.%Message_" em: "_sql)
            }
        }
        Kill ^||IgnorarValidacoesOs
        Set:(+pCenario.ignorarValidacoes) ^||IgnorarValidacoesOs = 1
        Set %session = ##class(caracterizacao.SessaoFalsa).%New()
        Set %session.Data("UsuarioId") = $Select(pCenario.usuarioId'="": pCenario.usuarioId, 1: 1)
        Set osId = pCenario.osId
        Set os = ##class(dado.ArqOrdemServico).%OpenId(osId)
        Set exames = ##class(%ListOfDataTypes).%New()
        Set perfis = ##class(%ListOfDataTypes).%New()
        Set convenios = ##class(%ListOfDataTypes).%New()
        Set dados = ##class(%ListOfDataTypes).%New()
        Set dadosResposta = ##class(%ListOfDataTypes).%New()
        Set devDoc = ##class(%ListOfDataTypes).%New()
        Set conveniosExame = ##class(%ListOfDataTypes).%New()
        Set valores = ##class(%ListOfDataTypes).%New()
        Set rs = ##class(%SQL.Statement).%ExecDirect(, "SELECT Procedimento, Perfil, FontePagadoraId FROM dado.TblOsProcedimento WHERE Os = ? ORDER BY ID", osId)
        While rs.%Next() {
            Do exames.Insert(rs.Procedimento)
            Do perfis.Insert(rs.Perfil)
            Do conveniosExame.Insert(rs.FontePagadoraId)
            Do valores.Insert(0)
            If 'convenios.Find(rs.FontePagadoraId) {
                Do convenios.Insert(rs.FontePagadoraId)
                Do dados.Insert("")
                Do dadosResposta.Insert("")
            }
        }
        Set convenio = pCenario.convenio
        If 'convenios.Find(convenio) {
            Do convenios.Insert(convenio)
            Do dados.Insert("")
            Do dadosResposta.Insert("")
        }
        Do:(+pCenario.devDoc) devDoc.Insert(convenio)
        Set proxy = ##class(%ZEN.proxyObject).%New()
        Set proxy.Categoria = os.Categoria
        Set proxy.SexoProcedimento = pCenario.sexoPaciente
        Set extras = pCenario.proxy
        If $IsObject(extras) {
            Set iterador = extras.%GetIterator()
            While iterador.%GetNext(.chave, .valor) {
                Set $Property(proxy, chave) = valor
            }
        }
        Set posto = $Select(pCenario.posto'="": pCenario.posto, 1: os.PostoGetObjectId())
        Set retorno = ##class(s01.iu.codigo.atendimento.cadastro.OsProcedimento).AddProcedimento(
            pCenario.mnemonico, convenio, posto, +pCenario.urgente, pCenario.material, proxy,
            exames, perfis, convenios, dados, dadosResposta, devDoc, pCenario.materialDescricao, 0, "", osId,
            "", "", "antesVerificarDiversosMateriais", 1, conveniosExame, valores, "", "")
        Set resultado.retorno = +retorno
        Do proxy.%CopyToArray(.campos)
        Set saida = {}
        Set chave = ""
        For {
            Set chave = $Order(campos(chave))
            Quit:(chave = "")
            Set valor = campos(chave)
            Continue:($IsObject(valor))
            Do saida.%Set(chave, valor)
        }
        Set resultado.proxy = saida
    } Catch excecao {
        Set resultado.excecao = excecao.DisplayString()
    }
    TRollback:($TLevel > nivel) ($TLevel - nivel)
    Kill ^||IgnorarValidacoesOs
    Return resultado
}

ClassMethod Rodar(pEntrada As %String, pSaida As %String) As %Status
{
    Set arquivo = ##class(%Stream.FileCharacter).%New()
    Do arquivo.LinkToFile(pEntrada)
    Do arquivo.TranslateTableSet("UTF8")
    Set cenarios = ##class(%DynamicAbstractObject).%FromJSON(arquivo)
    Set resultados = []
    Set iterador = cenarios.%GetIterator()
    While iterador.%GetNext(.indice, .cenario) {
        Set resultado = ..Executar(cenario)
        Set esperado = cenario.esperado
        Set diferencas = []
        If $IsObject(esperado) {
            Set campos = esperado.%GetIterator()
            While campos.%GetNext(.campo, .valorEsperado) {
                If campo = "retorno" {
                    Set obtido = resultado.retorno
                } ElseIf campo = "excecao" {
                    Set obtido = resultado.excecao
                } Else {
                    Set obtido = $Select($IsObject(resultado.proxy): resultado.proxy.%Get(campo), 1: "")
                }
                Do:(obtido '= valorEsperado) diferencas.%Push({"campo": (campo), "esperado": (valorEsperado), "obtido": (obtido)})
            }
            Set resultado.situacao = $Select(diferencas.%Size() = 0: "passou", 1: "falhou")
            Set resultado.diferencas = diferencas
        } Else {
            Set resultado.situacao = "sem esperado"
        }
        Do resultados.%Push(resultado)
    }
    Set destino = ##class(%Stream.FileCharacter).%New()
    Do destino.LinkToFile(pSaida)
    Do destino.TranslateTableSet("UTF8")
    Do resultados.%ToJSON(destino)
    Return destino.%Save()
}

}
