package dev.wakandaacademy.produdoro.tarefa.infra;

import dev.wakandaacademy.produdoro.tarefa.domain.StatusAtivacaoTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TarefaSpringMongoDBRepository extends MongoRepository<Tarefa, UUID> {
    Optional<Tarefa> findByIdTarefa(UUID idTarefa);
    List<Tarefa> findByIdUsuario(UUID idUsuario);
    Optional<Tarefa> findByIdUsuarioAndStatusAtivacao(UUID idUsuario, StatusAtivacaoTarefa statusAtivacao);
    List<Tarefa> findAllByIdUsuarioAndStatus(UUID idUsuario, StatusTarefa status);
}
