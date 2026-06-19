package dev.wakandaacademy.produdoro.tarefa.application.api;

import dev.wakandaacademy.produdoro.tarefa.domain.StatusAtivacaoTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TarefaResumidoResponse {
    private UUID idTarefa;
    private String descricao;
    private UUID idArea;
    private UUID idProjeto;
    private StatusTarefa status;
    private StatusAtivacaoTarefa statusAtivacao;
    private int contagemPomodoro;

    public TarefaResumidoResponse(Tarefa tarefa) {
        this.idTarefa = tarefa.getIdTarefa();
        this.descricao = tarefa.getDescricao();
        this.idArea = tarefa.getIdArea();
        this.idProjeto = tarefa.getIdProjeto();
        this.status = tarefa.getStatus();
        this.statusAtivacao = tarefa.getStatusAtivacao();
        this.contagemPomodoro = tarefa.getContagemPomodoro();
    }

    public static List<TarefaResumidoResponse> converte(List<Tarefa> tarefas) {
        return tarefas
                .stream()
                .map(TarefaResumidoResponse::new)
                .collect(Collectors.toList());
    }
}
