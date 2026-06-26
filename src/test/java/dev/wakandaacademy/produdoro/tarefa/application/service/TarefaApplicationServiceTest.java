package dev.wakandaacademy.produdoro.tarefa.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusAtivacaoTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

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
    void deveIncrementarPomodoroComSucesso() {
        Usuario usuario = DataHelper.createUsuarioComStatusFoco();
        Tarefa tarefa = DataHelper.createTarefa();
        UUID idTarefa = tarefa.getIdTarefa();

        when(tarefaRepository.buscaTarefaPorId(idTarefa)).thenReturn(Optional.of(tarefa));
        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.salva(tarefa)).thenReturn(tarefa);
        when(usuarioRepository.salva(usuario)).thenReturn(usuario);

        assertDoesNotThrow(() -> tarefaApplicationService.incrementaPomodoro(usuario.getEmail(), idTarefa));

        verify(tarefaRepository, times(1)).buscaTarefaPorId(idTarefa);
        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(usuario.getEmail());
        verify(tarefaRepository, times(1)).salva(tarefa);
        verify(usuarioRepository, times(1)).salva(usuario);
    }

    @Test
    void deveRetornarNotFoundQuandoTarefaNaoExiste() {
        Usuario usuario = DataHelper.createUsuario();
        UUID idTarefa = UUID.randomUUID();

        when(tarefaRepository.buscaTarefaPorId(idTarefa)).thenReturn(Optional.empty());

        APIException exception = assertThrows(APIException.class, () ->
                tarefaApplicationService.incrementaPomodoro(usuario.getEmail(), idTarefa));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusException());
        assertEquals("Tarefa não encontrada!", exception.getMessage());

        verify(tarefaRepository, times(1)).buscaTarefaPorId(idTarefa);
        verify(usuarioRepository, never()).buscaUsuarioPorEmail(any());
        verify(tarefaRepository, never()).salva(any());
        verify(usuarioRepository, never()).salva(any());
    }

    @Test
    void deveRetornarUnauthorizedQuandoTarefaNaoPertenceAoUsuario() {
        Usuario usuario = DataHelper.criaUsuarioSecundario();
        Tarefa tarefa = DataHelper.createTarefa();
        UUID idTarefa = tarefa.getIdTarefa();

        when(tarefaRepository.buscaTarefaPorId(idTarefa)).thenReturn(Optional.of(tarefa));
        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);

        APIException exception = assertThrows(APIException.class, () ->
                tarefaApplicationService.incrementaPomodoro(usuario.getEmail(), idTarefa));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusException());
        assertEquals("Usuário não é dono da Tarefa solicitada!", exception.getMessage());

        verify(tarefaRepository, times(1)).buscaTarefaPorId(idTarefa);
        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(usuario.getEmail());
        verify(tarefaRepository, never()).salva(any());
        verify(usuarioRepository, never()).salva(any());
    }

    @Test
    void deveDeletarTarefasConcluidasComSucesso() {
        Usuario usuario = DataHelper.createUsuario();
        Tarefa tarefaConcluida = criaTarefa(usuario.getIdUsuario(), StatusTarefa.CONCLUIDA);
        Tarefa outraTarefaConcluida = criaTarefa(usuario.getIdUsuario(), StatusTarefa.CONCLUIDA);
        List<Tarefa> tarefasConcluidas = List.of(tarefaConcluida, outraTarefaConcluida);

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefasConcluidasPorUsuario(usuario.getIdUsuario())).thenReturn(tarefasConcluidas);

        tarefaApplicationService.deletaTarefasConcluidas(usuario.getEmail());

        verify(tarefaRepository).deletaTodas(tarefasConcluidas);
    }

    @Test
    void naoDeveDeletarQuandoUsuarioNaoPossuiTarefasConcluidas() {
        Usuario usuario = DataHelper.createUsuario();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefasConcluidasPorUsuario(usuario.getIdUsuario())).thenReturn(List.of());

        APIException exception = assertThrows(APIException.class, () ->
                tarefaApplicationService.deletaTarefasConcluidas(usuario.getEmail())
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusException());
        assertEquals("Usuário não possui nenhuma tarefa concluída!", exception.getBodyException().getMessage());
        verify(tarefaRepository, never()).deletaTodas(anyList());
    }

    @Test
    void naoDeveDeletarQuandoUsuarioNaoForDonoDasTarefas() {
        Usuario usuario = DataHelper.createUsuario();
        Tarefa tarefaDeOutroUsuario = criaTarefa(UUID.randomUUID(), StatusTarefa.CONCLUIDA);

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefasConcluidasPorUsuario(usuario.getIdUsuario())).thenReturn(List.of(tarefaDeOutroUsuario));

        APIException exception = assertThrows(APIException.class, () ->
                tarefaApplicationService.deletaTarefasConcluidas(usuario.getEmail())
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusException());
        assertEquals("usuário(a) não autorizado(a) para a requisição solicitada!", exception.getBodyException().getMessage());
        verify(tarefaRepository, never()).deletaTodas(anyList());
    }

    @Test
    void naoDeveDeletarQuandoUsuarioNaoForEncontrado() {
        String email = "email@email.com";

        when(usuarioRepository.buscaUsuarioPorEmail(email))
                .thenThrow(APIException.build(HttpStatus.BAD_REQUEST, "Usuário não encontrado"));

        APIException exception = assertThrows(APIException.class, () ->
                tarefaApplicationService.deletaTarefasConcluidas(email)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusException());
        assertEquals("Usuário não encontrado", exception.getBodyException().getMessage());
        verify(tarefaRepository, never()).buscaTarefasConcluidasPorUsuario(any(UUID.class));
        verify(tarefaRepository, never()).deletaTodas(anyList());
    }


    public TarefaRequest getTarefaRequest() {
        TarefaRequest request = new TarefaRequest("tarefa 1", UUID.randomUUID(), null, null, 0);
        return request;
    }

    private Tarefa criaTarefa(UUID idUsuario, StatusTarefa status) {
        return Tarefa.builder()
                .idTarefa(UUID.randomUUID())
                .idUsuario(idUsuario)
                .descricao("descricao tarefa")
                .status(status)
                .statusAtivacao(StatusAtivacaoTarefa.INATIVA)
                .contagemPomodoro(1)
                .build();
    }
}
