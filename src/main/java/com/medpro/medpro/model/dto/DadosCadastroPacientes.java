package com.medpro.medpro.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record DadosCadastroPacientes(
    @NotBlank @Size(max = 100) String nome,
    @NotBlank @Email @Size(max = 100) String email,
    @NotBlank @Pattern(regexp = "[0-9]{11}") String cpf,
    @NotBlank @Size(max = 20) String telefone,
    @NotNull @Valid DadosEndereco endereco
) {
}
