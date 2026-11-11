<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="sistema.core.dominio.Movimentacao, sistema.core.dominio.Pagina" %>
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
    <% Pagina<Movimentacao> extrato = (Pagina<Movimentacao>) request.getAttribute("extrato"); %>
    <% if (extrato != null) { %>
        <table>
            <tr><th>Data</th><th>Tipo</th><th>Valor</th><th>Descrição</th></tr>
            <% for (Movimentacao m : extrato.getItens()) { %>
                <tr>
                    <td><%= m.getDataHora() %></td>
                    <td><%= m.getTipo() %></td>
                    <td><%= m.getValor() %></td>
                    <td><%= m.getDescricao() == null ? "" : m.getDescricao() %></td>
                </tr>
            <% } %>
        </table>
        <p>total: <%= extrato.getTotal() %></p>
    <% } %>
    <form method="post" action="extrato">
        <label>Conta <input name="conta"></label>
        <label>Página <input name="pagina" value="0"></label>
        <label>Tamanho <input name="tamanho" value="10"></label>
        <button type="submit">Consultar</button>
    </form>
</body>
</html>
