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
class DepositoControllerTest extends OracleNoContainer {

    @Autowired
    private MockMvc mvc;

    @Test
    void depositaNaContaC1() throws Exception {
        mvc.perform(post("/api/contas/c1/depositos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"valor\":25,\"descricao\":\"x\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("operacao realizada"));
    }

    @Test
    void valorZeroDevolve422() throws Exception {
        mvc.perform(post("/api/contas/c1/depositos").contentType(MediaType.APPLICATION_JSON).content("{\"valor\":0}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.erro").value("valor deve ser maior que zero"));
    }

    @Test
    void contaInexistenteDevolve404() throws Exception {
        mvc.perform(post("/api/contas/zzz/depositos").contentType(MediaType.APPLICATION_JSON).content("{\"valor\":10}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("conta nao encontrada"));
    }

}