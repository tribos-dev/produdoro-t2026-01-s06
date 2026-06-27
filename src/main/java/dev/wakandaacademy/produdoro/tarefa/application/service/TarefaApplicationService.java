package dev.wakandaacademy.produdoro.tarefa.application.service;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class TarefaApplicationService implements TarefaService {
    private final TarefaRepository tarefaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public TarefaIdResponse criaNovaTarefa(TarefaRequest tarefaRequest) {
        log.info("[inicia] TarefaApplicationService - criaNovaTarefa");
        Tarefa tarefaCriada = tarefaRepository.salva(new Tarefa(tarefaRequest));
        log.info("[finaliza] TarefaApplicationService - criaNovaTarefa");
        return TarefaIdResponse.builder().idTarefa(tarefaCriada.getIdTarefa()).build();
    }

    @Override
    public void ativaTarefa(String usuario, UUID idTarefa) {
        log.info("[inicia] TarefaApplicationService - ativaTarefa");
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        Tarefa tarefa = tarefaRepository.buscaTarefaPorId(idTarefa)
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Tarefa não encontrada!"));
        tarefa.pertenceAoUsuario(usuarioPorEmail);
        tarefa.validaNaoEstaAtiva();
        tarefaRepository.buscaTarefaAtivaPorUsuario(usuarioPorEmail.getIdUsuario())
                .ifPresent(tarefaAtiva -> {
                    tarefaAtiva.desativa();
                    tarefaRepository.salva(tarefaAtiva);
                });
        tarefa.ativa();
        tarefaRepository.salva(tarefa);
        log.info("[finaliza] TarefaApplicationService - ativaTarefa");
    }

    @Override
    public Tarefa detalhaTarefa(String usuario, UUID idTarefa) {
        log.info("[inicia] TarefaApplicationService - detalhaTarefa");
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        log.info("[usuarioPorEmail] {}", usuarioPorEmail);
        Tarefa tarefa = tarefaRepository.buscaTarefaPorId(idTarefa)
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Tarefa não encontrada!"));
        tarefa.pertenceAoUsuario(usuarioPorEmail);
        log.info("[finaliza] TarefaApplicationService - detalhaTarefa");
        return tarefa;
    }

    @Override
    public void concluiTarefa(String usuario, UUID idTarefa) {
        log.info("[inicia] TarefaApplicationService - concluiTarefa");
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        log.info("[usuarioPorEmail] {}", usuarioPorEmail);
        Tarefa tarefa = tarefaRepository.buscaTarefaPorId(idTarefa)
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Tarefa não encontrada!"));
        tarefa.pertenceAoUsuario(usuarioPorEmail);
        tarefa.concluir();
        tarefaRepository.salva(tarefa);
        log.info("[finaliza] TarefaApplicationService - concluiTarefa");
    }

    @Override

    public void limparTodasTarefas(String usuario, UUID idUsuario) {
        log.info("[inicia] TarefaApplicationService - limpaTodasTarefas");
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        usuarioRepository.buscaUsuarioPorId(idUsuario);
        usuarioPorEmail.validaIdUsuario(idUsuario);
        List<Tarefa> tarefas = tarefaRepository.buscaTarefasPorUsuario(idUsuario);
        validarTarefas(tarefas);
        tarefaRepository.removerTodasTarefas(tarefas);
        log.info("[finaliza] TarefaApplicationService - limpaTodasTarefas");

    }

    private void validarTarefas(List<Tarefa> tarefas) {
        if (tarefas.isEmpty()) {
            throw APIException.build(HttpStatus.CONFLICT, "Usuário não possui tarefa(as) cadastrada(as)");
        }
        if (tarefas.size() < 2) {
            throw APIException.build(HttpStatus.BAD_REQUEST,
                    "Deve existir pelo menos duas tarefas cadastradas no registro");
        }
    }

    public void incrementaPomodoro(String usuarioEmail, UUID idTarefa) {
        log.info("[inicia] TarefaApplicationService - incrementaPomodoro");
        Tarefa tarefa = getTarefa(idTarefa);
        Usuario usuario = usuarioRepository.buscaUsuarioPorEmail(usuarioEmail);
        tarefa.pertenceAoUsuario(usuario);
        usuario.validaSeEstaStatusFoco();
        tarefa.incrementaPomodoro();
        usuario.iniciaPausaAposPomodoro(tarefa.getContagemPomodoro());
        tarefaRepository.salva(tarefa);
        usuarioRepository.salva(usuario);
        log.info("[finaliza] TarefaApplicationService - incrementaPomodoro");
    }

    private Tarefa getTarefa(UUID idTarefa) {
        Tarefa tarefa = tarefaRepository.buscaTarefaPorId(idTarefa)
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Tarefa não encontrada!"));
        return tarefa;
    }

    @Override
    public void deletaTarefasConcluidas(String usuario) {
        log.info("[inicia] TarefaApplicationService - deletaTarefasConcluidas");
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        log.info("[usuarioPorEmail] {}", usuarioPorEmail);

        List<Tarefa> tarefasConcluidas = tarefaRepository
                .buscaTarefasConcluidasPorUsuario(usuarioPorEmail.getIdUsuario());

        if (tarefasConcluidas.isEmpty()) {
            throw APIException.build(HttpStatus.NOT_FOUND, "Usuário não possui nenhuma tarefa concluída!");
        }

        tarefasConcluidas.forEach(tarefa -> {
            if (!usuarioPorEmail.getIdUsuario().equals(tarefa.getIdUsuario())) {
                throw APIException.build(HttpStatus.UNAUTHORIZED,
                        "usuário(a) não autorizado(a) para a requisição solicitada!");
            }
        });

        tarefaRepository.deletaTodas(tarefasConcluidas);
        log.info("[finaliza] TarefaApplicationService - deletaTarefasConcluidas");
    }
}
