package dev.wakandaacademy.produdoro.tarefa.infra;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusAtivacaoTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Log4j2
@RequiredArgsConstructor
public class TarefaInfraRepository implements TarefaRepository {

    private final TarefaSpringMongoDBRepository tarefaSpringMongoDBRepository;

    @Override
    public Tarefa salva(Tarefa tarefa) {
        log.info("[inicia] TarefaInfraRepository - salva");
        try {
            tarefaSpringMongoDBRepository.save(tarefa);
        } catch (DataIntegrityViolationException e) {
            throw APIException.build(HttpStatus.BAD_REQUEST, "Tarefa já cadastrada", e);
        }
        log.info("[finaliza] TarefaInfraRepository - salva");
        return tarefa;
    }

    @Override
    public Optional<Tarefa> buscaTarefaPorId(UUID idTarefa) {
        log.info("[inicia] TarefaInfraRepository - buscaTarefaPorId");
        Optional<Tarefa> tarefaPorId = tarefaSpringMongoDBRepository.findByIdTarefa(idTarefa);
        log.info("[finaliza] TarefaInfraRepository - buscaTarefaPorId");
        return tarefaPorId;
    }

    @Override
    public List<Tarefa> buscaTarefasPorIdUsuario(UUID idUsuario) {
        log.info("[inicia] TarefaInfraRepository - buscaTarefasPorIdUsuario");
        List<Tarefa> tarefas = tarefaSpringMongoDBRepository.findByIdUsuario(idUsuario);
        log.info("[finaliza] TarefaInfraRepository - buscaTarefasPorIdUsuario");
        return tarefas;
    }

    @Override
    public Optional<Tarefa> buscaTarefaAtivaPorUsuario(UUID idUsuario) {
        log.info("[inicia] TarefaInfraRepository - buscaTarefaAtivaPorUsuario");
        Optional<Tarefa> tarefaAtiva = tarefaSpringMongoDBRepository
                .findByIdUsuarioAndStatusAtivacao(idUsuario, StatusAtivacaoTarefa.ATIVA);
        log.info("[finaliza] TarefaInfraRepository - buscaTarefaAtivaPorUsuario");
        return tarefaAtiva;
    }

    @Override
    public List<Tarefa> buscaTarefasConcluidasPorUsuario(UUID idUsuario) {
        log.info("[inicia] TarefaInfraRepository - buscaTarefasConcluidasPorUsuario");
        List<Tarefa> tarefasConcluidas = tarefaSpringMongoDBRepository.findAllByIdUsuarioAndStatus(idUsuario, StatusTarefa.CONCLUIDA);
        log.info("[finaliza] TarefaInfraRepository - buscaTarefasConcluidasPorUsuario");
        return tarefasConcluidas;
    }

    @Override
    public void deletaTodas(List<Tarefa> tarefas) {
        log.info("[inicia] TarefaInfraRepository - deletaTodas");
        tarefaSpringMongoDBRepository.deleteAll(tarefas);
        log.info("[finaliza] TarefaInfraRepository - deletaTodas");
    }
}
