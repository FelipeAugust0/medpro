package com.medpro.medpro.model.dto;

public record DadosCadastroPacientes(
        String nome,
        String email,
        String cpf,
        String telefone,
        DadosEndereco endereco) {
}
