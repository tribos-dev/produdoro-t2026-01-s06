package dev.wakandaacademy.produdoro.tarefa.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaAtualizarRequest;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusTarefa;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class TarefaApplicationServiceTest {

    //	@Autowired
    @InjectMocks
    TarefaApplicationService tarefaApplicationService;

    //	@MockBean
    @Mock
    TarefaRepository tarefaRepository;

    @Mock
    UsuarioRepository usuarioRepository;

    @Test
    void deveRetornarIdTarefaNovaCriada() {
        TarefaRequest request = getTarefaRequest();
        when(tarefaRepository.salva(any())).thenReturn(new Tarefa(request));

        TarefaIdResponse response = tarefaApplicationService.criaNovaTarefa(request);

        assertNotNull(response);
        assertEquals(TarefaIdResponse.class, response.getClass());
        assertEquals(UUID.class, response.getIdTarefa().getClass());
    }

    @Test
    void deveConcluirTarefaComSucesso() {
        Usuario usuario = DataHelper.createUsuario();
        Tarefa tarefa = DataHelper.createTarefa();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(tarefa.getIdTarefa())).thenReturn(Optional.of(tarefa));

        tarefaApplicationService.concluiTarefa(usuario.getEmail(), tarefa.getIdTarefa());

        assertEquals(StatusTarefa.CONCLUIDA, tarefa.getStatus());
        verify(tarefaRepository).salva(tarefa);
    }

    @Test
    void naoDeveConcluirTarefaQuandoNaoEncontrada() {
        Usuario usuario = DataHelper.createUsuario();
        UUID idTarefa = UUID.randomUUID();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(idTarefa)).thenReturn(Optional.empty());

        APIException exception = assertThrows(APIException.class, () -> {
            tarefaApplicationService.concluiTarefa(usuario.getEmail(), idTarefa);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusException());
        assertEquals("Tarefa não encontrada!", exception.getBodyException().getMessage());
    }

    @Test
    @DisplayName("Tarefa é editada com sucesso")
    void deveEditarTarefaComSucesso() {
        Usuario usuario = DataHelper.createUsuario();
        Tarefa tarefa = DataHelper.createTarefa();
        TarefaAtualizarRequest tarefaAtualizarRequest = DataHelper.createAtualizarTarefaRequest();

        when(usuarioRepository.buscaUsuarioPorEmail(ArgumentMatchers.isA(String.class))).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(ArgumentMatchers.isA(UUID.class))).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.salva(tarefa)).thenReturn(tarefa);

        Tarefa tarefaAtualizada = tarefaApplicationService.atualizaTarefa(
                usuario.getIdUsuario().toString(),
                tarefa.getIdTarefa(),
                tarefaAtualizarRequest
        );

        assertNotNull(tarefaAtualizada);
        assertEquals(tarefaAtualizada.getDescricao(), tarefaAtualizarRequest.getDescricao());
    }

    @Test
    @DisplayName("Tarefa não encontrada, código 404")
    void naoDeveRetornarTarefaNaoEncontrada() {
        Usuario usuario = DataHelper.createUsuario();
        Tarefa tarefa = DataHelper.createTarefa();
        TarefaAtualizarRequest tarefaAtualizarRequest = DataHelper.createAtualizarTarefaRequest();

        when(usuarioRepository.buscaUsuarioPorEmail(ArgumentMatchers.isA(String.class))).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(ArgumentMatchers.isA(UUID.class))).thenReturn(Optional.empty());

        APIException exception
                = assertThrows(APIException.class, () -> tarefaApplicationService
                .atualizaTarefa(
                    usuario.getIdUsuario().toString(),
                    tarefa.getIdTarefa(),
                    tarefaAtualizarRequest
                ));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusException());
        assertEquals("Tarefa não encontrada!",  exception.getBodyException().getMessage());
    }

    @Test
    @DisplayName("Tarefa não pertence ao usuário, código 401")
    void tarefaNaoPertenceAoUsuario() {
        Usuario usuarioComIdDistinto = Usuario.builder().idUsuario(UUID.randomUUID()).build();
        Tarefa tarefa = DataHelper.createTarefa();
        TarefaAtualizarRequest tarefaAtualizarRequest = DataHelper.createAtualizarTarefaRequest();

        when(usuarioRepository.buscaUsuarioPorEmail(ArgumentMatchers.isA(String.class))).thenReturn(usuarioComIdDistinto);
        when(tarefaRepository.buscaTarefaPorId(ArgumentMatchers.isA(UUID.class))).thenReturn(Optional.of(tarefa));

        APIException exception
                = assertThrows(APIException.class, () -> tarefaApplicationService
                .atualizaTarefa(
                    usuarioComIdDistinto.getIdUsuario().toString(),
                    tarefa.getIdTarefa(),
                    tarefaAtualizarRequest
                ));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusException());
        assertEquals("Usuário não é dono da Tarefa solicitada!",  exception.getBodyException().getMessage());
    }

    public TarefaRequest getTarefaRequest() {
        TarefaRequest request = new TarefaRequest("tarefa 1", UUID.randomUUID(), null, null, 0);
        return request;
    }
}
