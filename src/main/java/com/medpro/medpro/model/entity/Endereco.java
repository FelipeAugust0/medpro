package com.medpro.medpro.model.entity;

import com.medpro.medpro.model.dto.DadosEndereco;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class Endereco {
    private String logradouro;
    private String bairro;
    private String cep;
    private String cidade;
    private String uf;
    private String numero;
    private String complemento;

    public Endereco(DadosEndereco dados) {
        this.logradouro = dados.logradouro();
        this.bairro = dados.bairro();
        this.cep = dados.cep();
        this.cidade = dados.cidade();
        this.uf = dados.uf();
        this.numero = dados.numero();
        this.complemento = dados.complemento();

    }

    public void atualizarInformacoes(DadosEndereco dados) {
        validarTexto(dados.logradouro(), "logradouro", 100, true);
        validarTexto(dados.bairro(), "bairro", 100, true);
        validarTexto(dados.cep(), "cep", 8, true);
        validarTexto(dados.cidade(), "cidade", 100, true);
        validarTexto(dados.uf(), "uf", 2, true);
        validarTexto(dados.numero(), "numero", 20, false);
        validarTexto(dados.complemento(), "complemento", 100, false);

        if (dados.cep() != null && !dados.cep().matches("[0-9]{8}")) {
            throw new IllegalArgumentException("CEP deve conter 8 dígitos.");
        }
        if (dados.uf() != null && !dados.uf().matches("[A-Za-z]{2}")) {
            throw new IllegalArgumentException("UF deve conter 2 letras.");
        }
        if (dados.logradouro() != null) this.logradouro = dados.logradouro();
        if (dados.bairro() != null) this.bairro = dados.bairro();
        if (dados.cep() != null) this.cep = dados.cep();
        if (dados.cidade() != null) this.cidade = dados.cidade();
        if (dados.uf() != null) this.uf = dados.uf();
        if (dados.numero() != null) this.numero = dados.numero();
        if (dados.complemento() != null) this.complemento = dados.complemento();

    }

    private static void validarTexto(String valor, String campo, int limite, boolean obrigatorio) {
        if (valor != null && ((obrigatorio && valor.isBlank()) || valor.length() > limite)) {
            throw new IllegalArgumentException("Valor inválido para " + campo + ".");
        }
    }
}
