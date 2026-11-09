package sistema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class CenariosMigradoTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void propriedades(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    MockMvc mvc;
    @Autowired
    ContaRepository contas;
    @Autowired
    MovimentacaoRepository movimentacoes;

    @BeforeEach
    void preparar() {
        movimentacoes.deleteAll();
        contas.deleteAll();
        contas.save(new Conta("c1", "Ana", new BigDecimal("100.00"), "ATIVA"));
        contas.save(new Conta("c2", "Bruno", new BigDecimal("50.00"), "ATIVA"));
        contas.save(new Conta("c3", "Carla", new BigDecimal("0.00"), "ENCERRADA"));
    }

    private int transferir(String origem, String destino, String valor, String descricao) throws Exception {
        String json = "{\"origem\":\"" + origem + "\",\"destino\":\"" + destino + "\",\"valor\":" + valor
                + ",\"descricao\":\"" + descricao + "\"}";
        return mvc.perform(post("/api/transferencias").contentType(MediaType.APPLICATION_JSON).content(json))
                .andReturn().getResponse().getStatus();
    }

    private BigDecimal saldo(String id) {
        return contas.findById(id).orElseThrow().getSaldo();
    }

    @Test
    void aceitaTransferenciaComSaldo() throws Exception {
        assertThat(transferir("c1", "c2", "50", "pagamento")).isEqualTo(200);
        assertThat(saldo("c1")).isEqualByComparingTo("50");
        assertThat(saldo("c2")).isEqualByComparingTo("100");
        assertThat(movimentacoes.findByContaId("c1")).hasSize(1);
        assertThat(movimentacoes.findByContaId("c2")).hasSize(1);
    }

    @Test
    void recusaSaldoInsuficiente() throws Exception {
        assertThat(transferir("c1", "c2", "150", "x")).isEqualTo(422);
        assertThat(saldo("c1")).isEqualByComparingTo("100");
        assertThat(saldo("c2")).isEqualByComparingTo("50");
    }

    @Test
    void recusaValorZeroOuNegativo() throws Exception {
        assertThat(transferir("c1", "c2", "0", "x")).isEqualTo(422);
        assertThat(transferir("c1", "c2", "-5", "x")).isEqualTo(422);
        assertThat(saldo("c1")).isEqualByComparingTo("100");
    }

    @Test
    void recusaOrigemIgualAoDestino() throws Exception {
        assertThat(transferir("c1", "c1", "10", "x")).isEqualTo(422);
        assertThat(saldo("c1")).isEqualByComparingTo("100");
    }

    @Test
    void recusaContaEncerrada() throws Exception {
        assertThat(transferir("c1", "c3", "10", "x")).isEqualTo(422);
        assertThat(transferir("c3", "c1", "10", "x")).isEqualTo(422);
        assertThat(saldo("c1")).isEqualByComparingTo("100");
    }

    @Test
    void recusaDestinoInexistenteSemAlterarOrigem() throws Exception {
        assertThat(transferir("c1", "zzz", "10", "x")).isEqualTo(404);
        assertThat(saldo("c1")).isEqualByComparingTo("100");
    }

    @Test
    void mantemDescricaoVazia() throws Exception {
        assertThat(transferir("c1", "c2", "10", "")).isEqualTo(200);
        assertThat(movimentacoes.findByContaId("c1").get(0).getDescricao()).isEqualTo("");
    }

    private int enviar(String url, String json) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))
                .andReturn().getResponse().getStatus();
    }

    @Test
    void abreContaComSaldoZero() throws Exception {
        assertThat(enviar("/api/contas", "{\"titular\":\"Eva\"}")).isEqualTo(201);
        assertThat(contas.findAll()).anyMatch(c -> c.getTitular().equals("Eva")
                && c.getSaldo().compareTo(BigDecimal.ZERO) == 0 && c.getSituacao().equals("ATIVA"));
    }

    @Test
    void recusaAbrirContaSemTitular() throws Exception {
        assertThat(enviar("/api/contas", "{\"titular\":\" \"}")).isEqualTo(422);
    }

    @Test
    void deposita() throws Exception {
        assertThat(enviar("/api/contas/c1/depositos", "{\"valor\":25,\"descricao\":\"x\"}")).isEqualTo(200);
        assertThat(saldo("c1")).isEqualByComparingTo("125");
        assertThat(movimentacoes.findByContaId("c1")).hasSize(1);
    }

    @Test
    void recusaDepositoInvalido() throws Exception {
        assertThat(enviar("/api/contas/c1/depositos", "{\"valor\":0}")).isEqualTo(422);
        assertThat(enviar("/api/contas/c3/depositos", "{\"valor\":10}")).isEqualTo(422);
        assertThat(enviar("/api/contas/zzz/depositos", "{\"valor\":10}")).isEqualTo(404);
        assertThat(saldo("c1")).isEqualByComparingTo("100");
    }

    @Test
    void saca() throws Exception {
        assertThat(enviar("/api/contas/c1/saques", "{\"valor\":40,\"descricao\":\"x\"}")).isEqualTo(200);
        assertThat(saldo("c1")).isEqualByComparingTo("60");
    }

    @Test
    void recusaSaqueSemSaldoOuEmContaEncerrada() throws Exception {
        assertThat(enviar("/api/contas/c1/saques", "{\"valor\":150}")).isEqualTo(422);
        assertThat(enviar("/api/contas/c3/saques", "{\"valor\":1}")).isEqualTo(422);
        assertThat(saldo("c1")).isEqualByComparingTo("100");
    }

    @Test
    void encerraContaSemSaldo() throws Exception {
        contas.save(new Conta("c4", "Davi", new BigDecimal("0.00"), "ATIVA"));
        assertThat(enviar("/api/contas/c4/encerramento", "")).isEqualTo(200);
        assertThat(contas.findById("c4").orElseThrow().getSituacao()).isEqualTo("ENCERRADA");
    }

    @Test
    void recusaEncerrarContaComSaldoOuJaEncerrada() throws Exception {
        assertThat(enviar("/api/contas/c1/encerramento", "")).isEqualTo(422);
        assertThat(enviar("/api/contas/c3/encerramento", "")).isEqualTo(422);
        assertThat(contas.findById("c1").orElseThrow().getSituacao()).isEqualTo("ATIVA");
    }

    @Test
    void paginaOExtratoNaOrdemDeRegistro() throws Exception {
        for (int i = 1; i <= 3; i++) {
            enviar("/api/contas/c1/depositos", "{\"valor\":" + i + ",\"descricao\":\"m" + i + "\"}");
        }
        mvc.perform(get("/api/contas/c1/extrato?pagina=0&tamanho=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].descricao").value("m1"))
                .andExpect(jsonPath("$[1].descricao").value("m2"));
        mvc.perform(get("/api/contas/c1/extrato?pagina=1&tamanho=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].descricao").value("m3"));
    }
}
