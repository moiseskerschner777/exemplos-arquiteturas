Class caracterizacao.SessaoFalsa Extends %RegisteredObject
{

Property Data As %String [ MultiDimensional ];

Property SessionId As %String [ InitialExpression = "caracterizacao" ];

Method Get(pChave As %String, pPadrao As %String = "") As %String
{
    Return $Get(..Data(pChave), pPadrao)
}

}
