package dev.wakandaacademy.produdoro.tarefa.application.service;

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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

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
    void deveAtivarTarefaComSucesso() {
        Usuario usuario = DataHelper.createUsuario();
        Tarefa tarefa = DataHelper.createTarefa();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(tarefa.getIdTarefa())).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.buscaTarefaAtivaPorUsuario(usuario.getIdUsuario())).thenReturn(Optional.empty());

        tarefaApplicationService.ativaTarefa(usuario.getEmail(), tarefa.getIdTarefa());

        assertEquals(StatusAtivacaoTarefa.ATIVA, tarefa.getStatusAtivacao());
        verify(tarefaRepository).salva(tarefa);
    }

    @Test
    void deveLancarExcecao_QuandoIdTarefaInvalido() {
        Usuario usuario = DataHelper.createUsuario();
        UUID idTarefa = UUID.randomUUID();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(idTarefa)).thenReturn(Optional.empty());

        APIException exception = assertThrows(APIException.class, () -> {
            tarefaApplicationService.ativaTarefa(usuario.getEmail(), idTarefa);
        });

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusException());
        assertEquals("Tarefa não encontrada!", exception.getBodyException().getMessage());
    }

    @Test
    void deveDesativarTarefaAnteriorAposAtivarNova() {
        Usuario usuario = DataHelper.createUsuario();
        Tarefa tarefaAntiga = Tarefa.builder()
                .idTarefa(UUID.randomUUID())
                .idUsuario(usuario.getIdUsuario())
                .descricao("tarefa antiga")
                .statusAtivacao(StatusAtivacaoTarefa.ATIVA)
                .build();
        Tarefa tarefaNova = DataHelper.createTarefa();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(tarefaNova.getIdTarefa())).thenReturn(Optional.of(tarefaNova));
        when(tarefaRepository.buscaTarefaAtivaPorUsuario(usuario.getIdUsuario())).thenReturn(Optional.of(tarefaAntiga));

        tarefaApplicationService.ativaTarefa(usuario.getEmail(), tarefaNova.getIdTarefa());

        assertEquals(StatusAtivacaoTarefa.INATIVA, tarefaAntiga.getStatusAtivacao());
        assertEquals(StatusAtivacaoTarefa.ATIVA, tarefaNova.getStatusAtivacao());
        verify(tarefaRepository, times(2)).salva(any());
    }

    @Test
    void deveLancarExcecao_QuandoTokenNaoPertenceAoTarefa() {
        Usuario usuarioLogado = DataHelper.criaUsuarioSecundario();
        Tarefa tarefa = DataHelper.createTarefa();

        when(usuarioRepository.buscaUsuarioPorEmail(usuarioLogado.getEmail())).thenReturn(usuarioLogado);
        when(tarefaRepository.buscaTarefaPorId(tarefa.getIdTarefa())).thenReturn(Optional.of(tarefa));

        APIException exception = assertThrows(APIException.class, () -> {
            tarefaApplicationService.ativaTarefa(usuarioLogado.getEmail(), tarefa.getIdTarefa());
        });

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusException());
        assertEquals("Usuário não é dono da Tarefa solicitada!", exception.getBodyException().getMessage());
    }

    @Test
    void deveLancarExcecao_QuandoTarefaJaEstaAtiva() {
        Usuario usuario = DataHelper.createUsuario();
        Tarefa tarefa = Tarefa.builder()
                .idTarefa(UUID.randomUUID())
                .idUsuario(usuario.getIdUsuario())
                .descricao("tarefa ativa")
                .statusAtivacao(StatusAtivacaoTarefa.ATIVA)
                .build();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(tarefa.getIdTarefa())).thenReturn(Optional.of(tarefa));

        APIException exception = assertThrows(APIException.class, () -> {
            tarefaApplicationService.ativaTarefa(usuario.getEmail(), tarefa.getIdTarefa());
        });

        assertEquals(HttpStatus.CONFLICT, exception.getStatusException());
        assertEquals("Tarefa já está ativa!", exception.getBodyException().getMessage());
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
