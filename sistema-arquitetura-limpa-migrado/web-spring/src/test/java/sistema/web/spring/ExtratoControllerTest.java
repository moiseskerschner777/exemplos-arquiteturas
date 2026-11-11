package sistema.web.spring;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ExtratoControllerTest extends PostgresNoContainer {

    @Autowired
    private MockMvc mvc;

    @Test
    void extratoComTresMovimentacoes() throws Exception {
        String conta = Api.abrirConta(mvc, "Extrato");
        Api.depositar(mvc, conta, "1", "m1");
        Api.depositar(mvc, conta, "2", "m2");
        Api.depositar(mvc, conta, "3", "m3");
        mvc.perform(get("/api/contas/" + conta + "/extrato").param("pagina", "0").param("tamanho", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens.length()").value(2))
                .andExpect(jsonPath("$.itens[0].descricao").value("m1"))
                .andExpect(jsonPath("$.total").value(3));
        mvc.perform(get("/api/contas/" + conta + "/extrato").param("pagina", "1").param("tamanho", "2"))
                .andExpect(jsonPath("$.itens.length()").value(1))
                .andExpect(jsonPath("$.itens[0].descricao").value("m3"));
    }

    @Test
    void paginaInvalidaDevolve422() throws Exception {
        mvc.perform(get("/api/contas/c1/extrato").param("pagina", "-1").param("tamanho", "2"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.erro").value("pagina e tamanho invalidos"));
    }

}