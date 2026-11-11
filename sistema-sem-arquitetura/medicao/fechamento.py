import re,html,glob,os,sys,json,collections
raiz=sys.argv[1]; saida=sys.argv[2]
idx={}
supers={}
for f in glob.glob(raiz+'/*/*.cls.xml'):
    if '/test/' in f or '/node_modules/' in f: continue
    t=open(f,errors='ignore').read()
    cm=re.search(r'<Class name="([^"]+)"',t)
    if not cm: continue
    c=cm.group(1)
    s=re.search(r'<Super>([^<]*)</Super>',t)
    supers[c]=[x.strip() for x in s.group(1).split(',')] if s else []
    ms={}
    for m in re.finditer(r'<Method name="([^"]+)">(.*?)</Method>',t,re.S):
        corpo=html.unescape(m.group(2))
        lang=re.search(r'<Language>([^<]*)',corpo)
        impl=re.search(r'<Implementation><!\[CDATA\[(.*?)\]\]></Implementation>',m.group(2),re.S)
        codigo=html.unescape(impl.group(1)) if impl else ''
        ms[m.group(1).lower()]=(m.group(1),codigo,lang.group(1) if lang else 'cache',f)
    idx[c]=ms
def resolve(c,m):
    vistos=set(); fila=[c]
    while fila:
        x=fila.pop(0)
        if x in vistos or x not in idx: continue
        vistos.add(x)
        if m.lower() in idx[x]: return x
        fila+=supers.get(x,[])
    return None
alvo=('s01.iu.codigo.atendimento.cadastro.OsProcedimento','AddProcedimento')
visit={}; fila=[(alvo[0],alvo[1],0)]; nao_resolvidas=collections.Counter()
while fila:
    c,m,d=fila.pop(0)
    rc=resolve(c,m)
    if not rc: nao_resolvidas[c+'.'+m]+=1; continue
    k=(rc,idx[rc][m.lower()][0])
    if k in visit: continue
    nome,cod,lang,f=idx[rc][m.lower()]
    visit[k]=(d,cod,lang)
    if lang.lower()=='javascript': continue
    for mm in re.findall(r'(?<![\w.])\.\.(%?\w+)\s*\(',cod): fila.append((rc,mm,d+1))
    for cc,mm in re.findall(r'##class\(([\w.%]+)\)\.(%?\w+)\s*\(',cod):
        if cc.startswith('%'): continue
        fila.append((cc,mm,d+1))
    for cc in re.findall(r'#[Dd][Ii][Mm]\s+\w+\s+[Aa]s\s+([\w.]+)',cod): pass
linhas=sum(v[1].count('\n')+1 for v in visit.values())
classes=collections.Counter(k[0] for k in visit)
sql=set(); glob_=set(); persist=set()
for (c,m),(d,cod,lang) in visit.items():
    for t in re.findall(r'(?:FROM|JOIN|INTO|UPDATE)\s+([A-Za-z_][\w.]*)',cod,re.I):
        if t.upper() not in ('SELECT','SET','WHERE','THE','A','O'): sql.add(t)
    glob_.update(re.findall(r'\^(?!%)([A-Za-z][\w.]*)\s*\(',cod))
    persist.update(re.findall(r'##class\((dado\.[\w.]+)\)',cod))
prof=collections.Counter(v[0] for v in visit.values())
res={'metodos':len(visit),'linhas':linhas,'classes':len(classes),'profundidade':dict(sorted(prof.items())),
 'tabelas_sql':sorted(sql),'globais':sorted(glob_),'classes_dado':sorted(persist),
 'top_classes':classes.most_common(15),
 'maiores_metodos':sorted(((v[1].count('\n')+1,k[0]+'.'+k[1]) for k,v in visit.items()),reverse=True)[:20],
 'nao_resolvidas':len(nao_resolvidas)}
json.dump(res,open(saida,'w'),indent=1,ensure_ascii=False)
print(json.dumps({k:res[k] for k in ('metodos','linhas','classes','profundidade','nao_resolvidas')},ensure_ascii=False))
print('tabelas SQL',len(sql),'| globais',len(glob_),'| classes dado',len(persist))
