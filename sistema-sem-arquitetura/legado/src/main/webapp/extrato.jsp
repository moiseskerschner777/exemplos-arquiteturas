<%@ page contentType="text/html;charset=UTF-8" language="java" import="java.util.List" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Extrato</title>
</head>
<body>
    <h1>Extrato</h1>
    <% if (request.getAttribute("erro") != null) { %>
        <p><%= request.getAttribute("erro") %></p>
    <% } %>
    <form method="get" action="extrato">
        <label>Conta <input name="conta"></label>
        <label>Página <input name="pagina" value="0"></label>
        <label>Tamanho <input name="tamanho" value="10"></label>
        <button type="submit">Consultar</button>
    </form>
    <% List<String> linhas = (List<String>) request.getAttribute("linhas");
       if (linhas != null) { for (String linha : linhas) { %>
        <p><%= linha %></p>
    <% } } %>
</body>
</html>
