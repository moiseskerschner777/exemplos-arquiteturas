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
class AbrirContaControllerTest extends PostgresNoContainer {

    @Autowired
    private MockMvc mvc;

    @Test
    void abreContaComTitular() throws Exception {
        mvc.perform(post("/api/contas").contentType(MediaType.APPLICATION_JSON).content("{\"titular\":\"Fulano\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void titularVazioDevolve422() throws Exception {
        mvc.perform(post("/api/contas").contentType(MediaType.APPLICATION_JSON).content("{\"titular\":\"  \"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.erro").value("titular obrigatorio"));
    }

}