package dev.wakandaacademy.produdoro.tarefa.application.api;

import dev.wakandaacademy.produdoro.config.security.service.TokenService;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.service.TarefaService;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@Log4j2
@RequiredArgsConstructor
public class TarefaRestController implements TarefaAPI {
    private final TarefaService tarefaService;
    private final TokenService tokenService;

    public TarefaIdResponse postNovaTarefa(TarefaRequest tarefaRequest) {
        log.info("[inicia]  TarefaRestController - postNovaTarefa  ");
        TarefaIdResponse tarefaCriada = tarefaService.criaNovaTarefa(tarefaRequest);
        log.info("[finaliza]  TarefaRestController - postNovaTarefa");
        return tarefaCriada;
    }

    @Override
    public TarefaDetalhadoResponse detalhaTarefa(String token, UUID idTarefa) {
        log.info("[inicia] TarefaRestController - detalhaTarefa");
        String usuario = getUsuarioByToken(token);
        Tarefa tarefa = tarefaService.detalhaTarefa(usuario, idTarefa);
        log.info("[finaliza] TarefaRestController - detalhaTarefa");
        return new TarefaDetalhadoResponse(tarefa);
    }

    @Override
    public void concluiTarefa(String token, UUID idTarefa) {
        log.info("[inicia] TarefaRestController - concluiTarefa");
        String usuario = getUsuarioByToken(token);
        tarefaService.concluiTarefa(usuario, idTarefa);
        log.info("[finaliza] TarefaRestController - concluiTarefa");
    }

    @Override
    public List<TarefaResumidoResponse> retornaTodasTarefas(String token, UUID idUsuario) {
        log.info("[inicia] TarefaRestController - retornaTodasTarefas");
        String usuario = getUsuarioByToken(token);
        var tarefas = tarefaService.retornaTodasTarefas(usuario, idUsuario);
        log.info("[finaliza] TarefaRestController - retornaTodasTarefas");
        return tarefas;
    }

    @Override
    public void ativaTarefa(String token, UUID idTarefa) {
        log.info("[inicia] TarefaRestController - ativaTarefa");
        String usuario = getUsuarioByToken(token);
        tarefaService.ativaTarefa(usuario, idTarefa);
        log.info("[finaliza] TarefaRestController - ativaTarefa");
    }

    @Override
    public void incrementaPomodoro(String token, UUID idTarefa) {
        log.info("[inicia] TarefaRestController - incrementaPomodoro");
        String usuario = getUsuarioByToken(token);
        tarefaService.incrementaPomodoro(usuario, idTarefa);
        log.info("[finaliza] TarefaRestController - incrementaPomodoro");
    }

    @Override
    public void limparTodasTarefas(String token, UUID idUsuario) {
        log.info("[inicia] TarefaRestController - limparTodasTarefas");
        String usuario = getUsuarioByToken(token);
        tarefaService.limparTodasTarefas(usuario, idUsuario);
        log.info("[finaliza] TarefaRestController - limparTodasTarefas");

    }

    @Override
    public void atualizaTarefa(String token, UUID idTarefa, TarefaAtualizarRequest tarefaAtualizarRequest) {
        log.info("[inicia] TarefaRestController - atualizaTarefa");
        String usuario = getUsuarioByToken(token);
        tarefaService.atualizaTarefa(usuario, idTarefa, tarefaAtualizarRequest);
        log.info("[finaliza] TarefaRestController - atualizaTarefa");
    }

    @Override
    public void modificaOrdemTarefa(String token, UUID idTarefa, TarefaModificaOrdemRequest novaPosicao) {
        log.info("[inicia] - TarefaRestController - modifcaOrdemTarefa");
		String usuario = getUsuarioByToken(token);
		tarefaService.modificaOrdemTarefa(idTarefa, usuario, novaPosicao);
		log.info("[finaliza] - TarefaRestController - modificaOrdemTarefa");
    }

    private String getUsuarioByToken(String token) {
		log.debug("[token] {}", token);
		String usuario = tokenService.getUsuarioByBearerToken(token).orElseThrow(() -> APIException.build(HttpStatus.UNAUTHORIZED, token));
		log.info("[usuario] {}", usuario);
		return usuario;
	}

    @Override
    public void deletaTarefasConcluidas(String token) {
        log.info("[inicia] TarefaRestController - deletaTarefasConcluidas");
        String usuario = getUsuarioByToken(token);
        tarefaService.deletaTarefasConcluidas(usuario);
        log.info("[finaliza] TarefaRestController - deletaTarefasConcluidas");
    }
}
