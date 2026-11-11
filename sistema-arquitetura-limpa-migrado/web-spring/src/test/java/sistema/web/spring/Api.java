package sistema.web.spring;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

final class Api {

    private Api() {
    }

    static String abrirConta(MockMvc mvc, String titular) throws Exception {
        String corpo = mvc.perform(post("/api/contas").contentType(MediaType.APPLICATION_JSON)
                .content("{\"titular\":\"" + titular + "\"}")).andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.id");
    }

    static void depositar(MockMvc mvc, String conta, String valor, String descricao) throws Exception {
        mvc.perform(post("/api/contas/" + conta + "/depositos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"valor\":" + valor + ",\"descricao\":\"" + descricao + "\"}"));
    }

}