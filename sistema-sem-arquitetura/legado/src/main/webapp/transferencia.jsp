<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Transferência</title>
</head>
<body>
    <h1>Transferência entre contas</h1>
    <% if (request.getAttribute("mensagem") != null) { %>
        <p><%= request.getAttribute("mensagem") %></p>
    <% } %>
    <% if (request.getAttribute("erro") != null) { %>
        <p><%= request.getAttribute("erro") %></p>
    <% } %>
    <form method="post" action="transferir">
        <label>Origem <input name="origem"></label>
        <label>Destino <input name="destino"></label>
        <label>Valor <input name="valor"></label>
        <label>Descrição <input name="descricao"></label>
        <button type="submit">Transferir</button>
    </form>
</body>
</html>
