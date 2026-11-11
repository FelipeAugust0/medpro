package com.medpro.medpro.controller;

import com.medpro.medpro.model.dto.*;
import com.medpro.medpro.model.entity.Paciente;
import com.medpro.medpro.repository.PacienteRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {
    private final PacienteRepository repository;

    public PacienteController(PacienteRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    @Transactional
    @ResponseStatus(HttpStatus.CREATED)
    public DadosListagemPaciente cadastrar(@RequestBody @Valid DadosCadastroPacientes dados) {
        return new DadosListagemPaciente(repository.save(new Paciente(dados)));
    }

    @GetMapping
    public Page<DadosListagemPaciente> listar(@PageableDefault(size = 10, sort = "nome") Pageable paginacao) {
        return repository.findAllByAtivoTrue(paginacao).map(DadosListagemPaciente::new);
    }

    @PutMapping
    @Transactional
    public DadosListagemPaciente atualizar(@RequestBody @Valid DadosAtualizacaoPaciente dados) {
        var paciente = buscar(dados.id());
        paciente.atualizarInformacoes(dados);
        return new DadosListagemPaciente(paciente);
    }

    @DeleteMapping("/{id}")
    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        buscar(id).excluir();
    }

    private Paciente buscar(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente não encontrado."));
    }
}
