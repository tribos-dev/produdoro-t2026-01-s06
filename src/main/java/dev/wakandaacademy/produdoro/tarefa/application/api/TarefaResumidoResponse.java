package dev.wakandaacademy.produdoro.tarefa.application.api;

import dev.wakandaacademy.produdoro.tarefa.domain.StatusAtivacaoTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;

import java.util.UUID;

public class TarefaResumidoResponse {
    private UUID idTarefa;
    private String descricao;
    private StatusAtivacaoTarefa statusAtivacao;
    private int contagemPomodoro;

    public TarefaResumidoResponse(Tarefa tarefa) {
        this.idTarefa = tarefa.getIdTarefa();
        this.descricao = tarefa.getDescricao();
        this.statusAtivacao = tarefa.getStatusAtivacao();
        this.contagemPomodoro = tarefa.getContagemPomodoro();
    }
}
