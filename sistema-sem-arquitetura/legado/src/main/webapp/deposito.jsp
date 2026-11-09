<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Depósito</title>
</head>
<body>
    <h1>Depósito</h1>
    <% if (request.getAttribute("mensagem") != null) { %>
        <p><%= request.getAttribute("mensagem") %></p>
    <% } %>
    <% if (request.getAttribute("erro") != null) { %>
        <p><%= request.getAttribute("erro") %></p>
    <% } %>
    <form method="post" action="depositar">
        <label>Conta <input name="conta"></label>
        <label>Valor <input name="valor"></label>
        <label>Descrição <input name="descricao"></label>
        <button type="submit">Depositar</button>
    </form>
</body>
</html>
