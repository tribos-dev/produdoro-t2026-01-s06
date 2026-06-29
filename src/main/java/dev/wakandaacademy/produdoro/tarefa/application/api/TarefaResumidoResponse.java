package dev.wakandaacademy.produdoro.tarefa.application.api;

import dev.wakandaacademy.produdoro.tarefa.domain.StatusAtivacaoTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import lombok.Getter;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class TarefaResumidoResponse {
    private UUID idTarefa;
    private String descricao;
    private UUID area;
    private UUID projeto;
    private StatusTarefa status;
    private StatusAtivacaoTarefa statusAtivacao;
    private int contagemPomodoro;

    public TarefaResumidoResponse(Tarefa tarefa) {
        this.idTarefa = tarefa.getIdTarefa();
        this.descricao = tarefa.getDescricao();
        this.projeto = tarefa.getIdProjeto();
        this.area = tarefa.getIdArea();
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
