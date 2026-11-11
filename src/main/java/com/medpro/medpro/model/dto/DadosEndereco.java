package com.medpro.medpro.model.dto;

import jakarta.validation.constraints.*;

public record DadosEndereco(
    @NotBlank @Size(max = 100) String logradouro,
    @NotBlank @Size(max = 100) String bairro,
    @NotBlank @Pattern(regexp = "[0-9]{8}") String cep,
    @NotBlank @Size(max = 100) String cidade,
    @NotBlank @Pattern(regexp = "[A-Za-z]{2}") String uf,
    @Size(max = 20) String numero,
    @Size(max = 100) String complemento
) {
}
