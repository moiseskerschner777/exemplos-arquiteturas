package sistema.web.spring;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SaqueControllerTest extends PostgresNoContainer {

    @Autowired
    private MockMvc mvc;

    @Test
    void sacaDeUmaContaComSaldo() throws Exception {
        String conta = Api.abrirConta(mvc, "Saque");
        Api.depositar(mvc, conta, "100", "x");
        mvc.perform(post("/api/contas/" + conta + "/saques").contentType(MediaType.APPLICATION_JSON)
                .content("{\"valor\":40,\"descricao\":\"x\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("operacao realizada"));
    }

    @Test
    void saldoInsuficienteDevolve422() throws Exception {
        String conta = Api.abrirConta(mvc, "Saque");
        mvc.perform(post("/api/contas/" + conta + "/saques").contentType(MediaType.APPLICATION_JSON)
                .content("{\"valor\":150}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.erro").value("saldo insuficiente"));
    }

}