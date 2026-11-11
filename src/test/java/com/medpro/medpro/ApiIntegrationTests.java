package com.medpro.medpro;

import com.jayway.jsonpath.JsonPath;
import com.medpro.medpro.repository.MedicoRepository;
import com.medpro.medpro.repository.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired MedicoRepository medicos;
    @Autowired PacienteRepository pacientes;

    @BeforeEach
    void limpar() {
        medicos.deleteAll();
        pacientes.deleteAll();
    }

    @ParameterizedTest
    @ValueSource(strings = {"medicos", "pacientes"})
    void cadastrarAtualizarEPreservarDadosNaExclusaoLogica(String recurso) throws Exception {
        long id = cadastrar(recurso, "Ana", 1);
        mvc.perform(get("/" + recurso)).andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].id").value(id))
            .andExpect(jsonPath("$.content[0].endereco").doesNotExist());

        mvc.perform(put("/" + recurso).contentType(MediaType.APPLICATION_JSON).content("""
            {"id":%d,"nome":"Ana Atualizada","telefone":"16999990000",
             "endereco":{"numero":"42","complemento":"Sala 2"}}
            """.formatted(id))).andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Ana Atualizada"));
        if (recurso.equals("medicos")) {
            var medico = medicos.findById(id).orElseThrow();
            assertThat(medico.getTelefone()).isEqualTo("16999990000");
            assertThat(medico.getEndereco().getNumero()).isEqualTo("42");
            assertThat(medico.getEndereco().getCidade()).isEqualTo("São Carlos");
            assertThat(medico.getCrm()).isEqualTo("123401");
        } else {
            var paciente = pacientes.findById(id).orElseThrow();
            assertThat(paciente.getTelefone()).isEqualTo("16999990000");
            assertThat(paciente.getEndereco().getNumero()).isEqualTo("42");
            assertThat(paciente.getEndereco().getCidade()).isEqualTo("São Carlos");
            assertThat(paciente.getCpf()).isEqualTo("12345678901");
        }

        mvc.perform(delete("/" + recurso + "/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/" + recurso)).andExpect(jsonPath("$.totalElements").value(0));
        if (recurso.equals("medicos")) {
            assertThat(medicos.findById(id).orElseThrow().isAtivo()).isFalse();
        } else {
            assertThat(pacientes.findById(id).orElseThrow().isAtivo()).isFalse();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"medicos", "pacientes"})
    void paginarOrdenarEFiltrarInativosAntesDaPaginacao(String recurso) throws Exception {
        long id = cadastrar(recurso, "Ana", 1);
        cadastrar(recurso, "Zelia", 2);
        cadastrar(recurso, "Bruno", 3);
        mvc.perform(get("/" + recurso).param("size", "1").param("page", "1").param("sort", "nome,desc"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].nome").value("Bruno"))
            .andExpect(jsonPath("$.totalElements").value(3));
        mvc.perform(delete("/" + recurso + "/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/" + recurso).param("size", "1").param("sort", "nome"))
            .andExpect(jsonPath("$.content[0].nome").value("Bruno"))
            .andExpect(jsonPath("$.totalElements").value(2));
    }

    @ParameterizedTest
    @ValueSource(strings = {"medicos", "pacientes"})
    void rejeitarDadosInvalidosEDuplicados(String recurso) throws Exception {
        mvc.perform(post("/" + recurso).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
        for (String invalido : new String[]{
            cadastro(recurso, "Ana", 1).replace("13574000", "123"),
            cadastro(recurso, "Ana", 1).replace("ana1@example.com", "email-invalido"),
            cadastro(recurso, "Ana", 1).replace("123401", "12").replace("12345678901", "12"),
            cadastro(recurso, "Ana", 1).replace("16999999999", ""),
            cadastro(recurso, "Ana", 1).replace("Rua A", " ")}) {
            mvc.perform(post("/" + recurso).contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest());
        }
        cadastrar(recurso, "Ana", 1);
        mvc.perform(post("/" + recurso).contentType(MediaType.APPLICATION_JSON).content(cadastro(recurso, "Ana", 1)))
            .andExpect(status().isConflict());
    }

    @ParameterizedTest
    @ValueSource(strings = {"medicos", "pacientes"})
    void rejeitarAtualizacaoInvalidaSemPersistirAlteracoes(String recurso) throws Exception {
        long id = cadastrar(recurso, "Ana", 1);
        for (String campos : new String[]{"\"nome\":\" \"", "\"telefone\":\"\"",
            "\"endereco\":{\"cidade\":\" \"}", "\"endereco\":{\"cep\":\"123\"}",
            "\"endereco\":{\"uf\":\"SPP\"}"}) {
            mvc.perform(put("/" + recurso).contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":" + id + "," + campos + "}"))
                .andExpect(status().isBadRequest());
        }
        mvc.perform(put("/" + recurso).contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\":" + id + ",\"nome\":\"Alterado\",\"endereco\":{\"cep\":\"x\"}}"))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/" + recurso)).andExpect(jsonPath("$.content[0].nome").value("Ana"));
        mvc.perform(put("/" + recurso).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Sem ID\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(put("/" + recurso).contentType(MediaType.APPLICATION_JSON).content("{\"id\":999999}"))
            .andExpect(status().isNotFound());
        mvc.perform(delete("/" + recurso + "/999999")).andExpect(status().isNotFound());
    }

    private long cadastrar(String recurso, String nome, int numero) throws Exception {
        var resposta = mvc.perform(post("/" + recurso).contentType(MediaType.APPLICATION_JSON)
            .content(cadastro(recurso, nome, numero))).andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }

    private String cadastro(String recurso, String nome, int numero) {
        String documento = recurso.equals("medicos")
            ? "\"crm\":\"12340" + numero + "\",\"especialidade\":\"ORTOPEDIA\""
            : "\"cpf\":\"1234567890" + numero + "\"";
        return """
            {"nome":"%s","email":"ana%d@example.com",%s,"telefone":"16999999999",
             "endereco":{"logradouro":"Rua A","bairro":"Centro","cep":"13574000",
                         "cidade":"São Carlos","uf":"SP","numero":"10"}}
            """.formatted(nome, numero, documento);
    }
}
