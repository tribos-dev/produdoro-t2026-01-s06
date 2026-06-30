package dev.wakandaacademy.produdoro.tarefa.application.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import javax.validation.constraints.PositiveOrZero;

@Getter
public class TarefaModificaOrdemRequest {

    @PositiveOrZero
    private final int novaPosicao;

    @JsonCreator
    public TarefaModificaOrdemRequest(@JsonProperty("novaPosicao") int novaPosicao) {
        this.novaPosicao = novaPosicao;
    }
}
