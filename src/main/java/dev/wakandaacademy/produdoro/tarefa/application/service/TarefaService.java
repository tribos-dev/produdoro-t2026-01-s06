package dev.wakandaacademy.produdoro.tarefa.application.service;

import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaAtualizarRequest;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaModificaOrdemRequest;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaResumidoResponse;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;

import java.util.List;
import java.util.UUID;

public interface TarefaService {
    TarefaIdResponse criaNovaTarefa(TarefaRequest tarefaRequest);

    Tarefa detalhaTarefa(String usuario, UUID idTarefa);

    void limparTodasTarefas(String usuario, UUID idUsuario);

    List<TarefaResumidoResponse> retornaTodasTarefas(String usuario, UUID idUsuario);

    void concluiTarefa(String usuario, UUID idTarefa);

    Tarefa atualizaTarefa(String usuario, UUID idTarefa, TarefaAtualizarRequest tarefaAtualizarRequest);

    void ativaTarefa(String usuario, UUID idTarefa);

    void incrementaPomodoro(String usuarioEmail, UUID idTarefa);

    void deletaTarefasConcluidas(String usuario);
    void modificaOrdemTarefa(UUID idTarefa, String usuario, TarefaModificaOrdemRequest novaPosicao);
}
