package dev.wakandaacademy.produdoro.tarefa.application.api;

import lombok.Value;

import javax.validation.constraints.PositiveOrZero;

@Value
public class TarefaModificaOrdemRequest {

    @PositiveOrZero
    int novaPosicao;
}
