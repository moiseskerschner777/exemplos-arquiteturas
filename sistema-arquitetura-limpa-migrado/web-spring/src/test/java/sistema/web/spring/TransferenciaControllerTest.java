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
class TransferenciaControllerTest extends PostgresNoContainer {

    @Autowired
    private MockMvc mvc;

    @Test
    void transfere50DeC1ParaC2() throws Exception {
        mvc.perform(post("/api/transferencias").contentType(MediaType.APPLICATION_JSON)
                .content("{\"origem\":\"c1\",\"destino\":\"c2\",\"valor\":50,\"descricao\":\"x\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("transferencia realizada"));
    }

    @Test
    void destinoInexistenteDevolve404() throws Exception {
        mvc.perform(post("/api/transferencias").contentType(MediaType.APPLICATION_JSON)
                .content("{\"origem\":\"c1\",\"destino\":\"zzz\",\"valor\":10,\"descricao\":\"x\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("conta de destino nao encontrada"));
    }

    @Test
    void saldoInsuficienteDevolve422() throws Exception {
        mvc.perform(post("/api/transferencias").contentType(MediaType.APPLICATION_JSON)
                .content("{\"origem\":\"c1\",\"destino\":\"c2\",\"valor\":100000,\"descricao\":\"x\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.erro").value("saldo insuficiente"));
    }

    @Test
    void valorInvalidoDevolve422() throws Exception {
        mvc.perform(post("/api/transferencias").contentType(MediaType.APPLICATION_JSON)
                .content("{\"origem\":\"c1\",\"destino\":\"c2\",\"valor\":\"abc\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.erro").value("valor invalido"));
    }

}