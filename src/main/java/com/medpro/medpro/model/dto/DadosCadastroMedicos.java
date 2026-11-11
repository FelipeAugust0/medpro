package com.medpro.medpro.model.dto;

import com.medpro.medpro.enums.Especialidade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record DadosCadastroMedicos(
    @NotBlank @Size(max = 100) String nome,
    @NotBlank @Email @Size(max = 100) String email,
    @NotBlank @Pattern(regexp = "[0-9]{4,6}") String crm,
    @NotBlank @Size(max = 20) String telefone,
    @NotNull Especialidade especialidade,
    @NotNull @Valid DadosEndereco endereco
) {
}
