package com.medpro.medpro.model.dto;

import com.medpro.medpro.enums.Especialidade;

public record DadosCadastroMedicos(
        String nome,
        String email,
        String crm,
        Especialidade especialidade,
        DadosEndereco endereco) {
}
