package dev.wakandaacademy.produdoro.tarefa.application.service;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaAtualizarRequest;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaModificaOrdemRequest;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaResumidoResponse;
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
        int novaPosicao = tarefaRepository.contaTarefasUsuario(tarefaRequest.getIdUsuario());
        Tarefa tarefaCriada = tarefaRepository.salva(new Tarefa(tarefaRequest, novaPosicao));
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

    public Tarefa atualizaTarefa(String usuario, UUID idTarefa, TarefaAtualizarRequest tarefaAtualizarRequest) {
        log.info("[inicia] TarefaApplicationService - atualizaTarefa");
        Tarefa tarefa = detalhaTarefa(usuario, idTarefa);
        tarefa.atualizaTarefa(tarefaAtualizarRequest.getDescricao());
        Tarefa tarefaAtualizada = tarefaRepository.salva(tarefa);
        log.debug("[finaliza] TarefaApplicationService - atualizaTarefa");
        return tarefaAtualizada;
    }

    @Override

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

    @Override
    public List<TarefaResumidoResponse> retornaTodasTarefas(String usuario, UUID idUsuario) {
        log.info("[inicia] TarefaApplicationService - retornaTodasTarefa");
        verificaUsuarioExistente(idUsuario);
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        usuarioPorEmail.idPertenceAoUsuario(idUsuario);
        log.info("[usuarioPorEmail] {}", usuarioPorEmail);
        List<Tarefa> tarefas = tarefaRepository.buscaTarefasPorIdUsuario(usuarioPorEmail.getIdUsuario());
        List<TarefaResumidoResponse> tarefasResumidos = TarefaResumidoResponse.converte(tarefas);
        log.info("[finaliza] TarefaApplicationService - retornaTodasTarefa");
        return tarefasResumidos;
    }

    @Override
    public void modificaOrdemTarefa(UUID idTarefa, String usuario, TarefaModificaOrdemRequest tarefaModificaOrdemRequest) {
        log.info("[inicia] TarefaApplicationService - modificaOrdemTarefa");
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        log.info("[usuarioPorEmail] {}", usuarioPorEmail);
        Tarefa tarefa = buscaTarefaPorId(idTarefa);
        tarefa.pertenceAoUsuario(usuarioPorEmail);
        int novaPosicao = tarefaModificaOrdemRequest.getNovaPosicao();
        modificaOrdemOutrasTarefas(usuarioPorEmail.getIdUsuario(), tarefa, novaPosicao);
        tarefa.modificaPosicao(novaPosicao);
        tarefaRepository.salva(tarefa);
        log.info("[finaliza] TarefaApplicationService - modificaOrdemTarefa");
    }

    private void modificaOrdemOutrasTarefas(UUID idUsuario, Tarefa tarefaMovida, int novaPosicao) {
        log.info("[inicia] TarefaApplicationService - modificaOrdemOutrasTarefas");
        int posicaoAntiga = tarefaMovida.getPosicao();
        List<Tarefa> tarefasDoUsuario = tarefaRepository.buscaTarefasPorIdUsuario(idUsuario);
        validaNovaPosicao(novaPosicao, tarefasDoUsuario.size());
        tarefasDoUsuario.stream()
                .filter(outraTarefa -> !outraTarefa.getIdTarefa().equals(tarefaMovida.getIdTarefa()))
                .forEach(outraTarefa -> {
                    if (reposicionaOutraTarefa(outraTarefa, posicaoAntiga, novaPosicao)) {
                        tarefaRepository.salva(outraTarefa);
                    }
                });
        log.info("[finaliza] TarefaApplicationService - modificaOrdemOutrasTarefas");
    }

    private boolean reposicionaOutraTarefa(Tarefa outraTarefa, int posicaoAntiga, int novaPosicao) {
        int posicaoAtual = outraTarefa.getPosicao();
        if (novaPosicao > posicaoAntiga) {
            if (posicaoAtual > posicaoAntiga && posicaoAtual <= novaPosicao) {
                outraTarefa.modificaPosicao(posicaoAtual - 1);
                return true;
            }
        } else if (novaPosicao < posicaoAntiga) {
            if (posicaoAtual >= novaPosicao && posicaoAtual < posicaoAntiga) {
                outraTarefa.modificaPosicao(posicaoAtual + 1);
                return true;
            }
        }
        return false;
    }

    private void validaNovaPosicao(int novaPosicao, int totalTarefas) {
        if (novaPosicao >= totalTarefas) {
            throw APIException.build(HttpStatus.BAD_REQUEST,
                    "Nova posição inválida! A posição deve estar entre 0 e " + (totalTarefas - 1) + ".");
        }
    }

    private void verificaUsuarioExistente(UUID idUsuario) {
        usuarioRepository.buscaUsuarioPorId(idUsuario);
    }

    public Tarefa buscaTarefaPorId(UUID idTarefa){
        Tarefa tarefa = tarefaRepository.buscaTarefaPorId(idTarefa).
                        orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Tarefa não encontrada!"));
        return tarefa;
    }

    private Tarefa getTarefa(UUID idTarefa) {
        Tarefa tarefa = tarefaRepository.buscaTarefaPorId(idTarefa)
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Tarefa não encontrada!"));
        return tarefa;
    }
}
