package sistema.web.spring;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class EncerrarContaControllerTest extends PostgresNoContainer {

    @Autowired
    private MockMvc mvc;

    @Test
    void encerraContaSemSaldo() throws Exception {
        String conta = Api.abrirConta(mvc, "Encerrar");
        mvc.perform(post("/api/contas/" + conta + "/encerramento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("conta encerrada"));
    }

    @Test
    void contaComSaldoDevolve422() throws Exception {
        String conta = Api.abrirConta(mvc, "Encerrar");
        Api.depositar(mvc, conta, "10", "x");
        mvc.perform(post("/api/contas/" + conta + "/encerramento"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.erro").value("conta com saldo nao pode ser encerrada"));
    }

}