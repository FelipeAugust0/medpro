package com.medpro.medpro.controller;

import com.medpro.medpro.model.dto.*;
import com.medpro.medpro.model.entity.Medico;
import com.medpro.medpro.repository.MedicoRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/medicos")
public class MedicoController {
    private final MedicoRepository repository;

    public MedicoController(MedicoRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    @Transactional
    @ResponseStatus(HttpStatus.CREATED)
    public DadosListagemMedico cadastrar(@RequestBody @Valid DadosCadastroMedicos dados) {
        return new DadosListagemMedico(repository.save(new Medico(dados)));
    }

    @GetMapping
    public Page<DadosListagemMedico> listar(@PageableDefault(size = 10, sort = "nome") Pageable paginacao) {
        return repository.findAllByAtivoTrue(paginacao).map(DadosListagemMedico::new);
    }

    @PutMapping
    @Transactional
    public DadosListagemMedico atualizar(@RequestBody @Valid DadosAtualizacaoMedico dados) {
        var medico = buscar(dados.id());
        medico.atualizarInformacoes(dados);
        return new DadosListagemMedico(medico);
    }

    @DeleteMapping("/{id}")
    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        buscar(id).excluir();
    }

    private Medico buscar(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Medico não encontrado."));
    }
}
