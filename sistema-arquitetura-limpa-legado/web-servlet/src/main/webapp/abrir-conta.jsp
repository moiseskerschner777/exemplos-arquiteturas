<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Abrir conta</title>
</head>
<body>
    <h1>Abrir conta</h1>
    <% if (request.getAttribute("mensagem") != null) { %>
        <p><%= request.getAttribute("mensagem") %></p>
    <% } %>
    <% if (request.getAttribute("erro") != null) { %>
        <p><%= request.getAttribute("erro") %></p>
    <% } %>
    <form method="post" action="abrir-conta">
        <label>Titular <input name="titular"></label>
        <button type="submit">Abrir</button>
    </form>
</body>
</html>
