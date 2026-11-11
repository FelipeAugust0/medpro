package com.medpro.medpro.model.dto;

import jakarta.validation.constraints.*;

public record DadosAtualizacaoPaciente(
    @NotNull @Positive Long id,
    @Pattern(regexp = "(?s).*\\S.*", message = "não pode estar em branco") @Size(max = 100) String nome,
    @Pattern(regexp = "(?s).*\\S.*", message = "não pode estar em branco") @Size(max = 20) String telefone,
    // O endereço é parcial no PUT; a entidade valida somente os campos enviados.
    DadosEndereco endereco
) {
}
