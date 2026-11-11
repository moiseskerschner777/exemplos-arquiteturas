<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Encerrar conta</title>
</head>
<body>
    <h1>Encerrar conta</h1>
    <% if (request.getAttribute("mensagem") != null) { %>
        <p><%= request.getAttribute("mensagem") %></p>
    <% } %>
    <% if (request.getAttribute("erro") != null) { %>
        <p><%= request.getAttribute("erro") %></p>
    <% } %>
    <form method="post" action="encerrar-conta">
        <label>Conta <input name="conta"></label>
        <button type="submit">Encerrar</button>
    </form>
</body>
</html>
