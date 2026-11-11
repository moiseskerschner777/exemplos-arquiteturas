<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Saque</title>
</head>
<body>
    <h1>Saque</h1>
    <% if (request.getAttribute("mensagem") != null) { %>
        <p><%= request.getAttribute("mensagem") %></p>
    <% } %>
    <% if (request.getAttribute("erro") != null) { %>
        <p><%= request.getAttribute("erro") %></p>
    <% } %>
    <form method="post" action="sacar">
        <label>Conta <input name="conta"></label>
        <label>Valor <input name="valor"></label>
        <label>Descrição <input name="descricao"></label>
        <button type="submit">Sacar</button>
    </form>
</body>
</html>
