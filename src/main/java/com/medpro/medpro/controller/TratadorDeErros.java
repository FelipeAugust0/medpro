package com.medpro.medpro.controller;

import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class TratadorDeErros {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public List<ErroCampo> validar(MethodArgumentNotValidException erro) {
        return erro.getBindingResult().getFieldErrors().stream()
            .map(campo -> new ErroCampo(campo.getField(), campo.getDefaultMessage())).toList();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroMensagem argumento(IllegalArgumentException erro) {
        return new ErroMensagem(erro.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroMensagem jsonInvalido() {
        return new ErroMensagem("JSON inválido. Confira os campos e a especialidade informada.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroMensagem conflito() {
        return new ErroMensagem("Cadastro em conflito: email, CRM ou CPF já registrado, ou dados incompatíveis com o banco.");
    }

    public record ErroCampo(String campo, String mensagem) {}
    public record ErroMensagem(String mensagem) {}
}
