package dev.wakandaacademy.produdoro.tarefa.application.service;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaResumidoResponse;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
    @DisplayName("Retorna uma lista de tarefas do usuário")
    void deveRetornarListaTarefasQuandoExecutadoComSucesso() {
        List<Tarefa> tarefas = DataHelper.createListTarefa();

        Usuario usuario = DataHelper.createUsuario();

        when(tarefaRepository.buscaTarefasPorIdUsuario(ArgumentMatchers.isA(UUID.class))).thenReturn(tarefas);
        when(usuarioRepository.buscaUsuarioPorId(ArgumentMatchers.isA(UUID.class))).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorEmail(ArgumentMatchers.isA(String.class))).thenReturn(usuario);

        List<TarefaResumidoResponse> tarefaResumidosResponse = tarefaApplicationService
                .retornaTodasTarefas(usuario.getIdUsuario().toString(), usuario.getIdUsuario());

        assertNotNull(tarefaResumidosResponse);
        assertFalse(tarefaResumidosResponse.isEmpty());
        assertEquals(TarefaResumidoResponse.class, tarefaResumidosResponse.get(0).getClass());
    }

    @Test
    @DisplayName("Retorna lista vazia quando o usuario não possui tarefas")
    void deveRetornarListaTarefasVaziaQuandoExecutadoComSucesso() {
        List<Tarefa> tarefaResumidos = List.of();

        Usuario usuario = DataHelper.createUsuario();

        when(tarefaRepository.buscaTarefasPorIdUsuario(ArgumentMatchers.isA(UUID.class))).thenReturn(tarefaResumidos);
        when(usuarioRepository.buscaUsuarioPorId(ArgumentMatchers.isA(UUID.class))).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorEmail(ArgumentMatchers.isA(String.class))).thenReturn(usuario);

        List<TarefaResumidoResponse> tarefaResumidosResponse = tarefaApplicationService
                .retornaTodasTarefas(usuario.getIdUsuario().toString(), usuario.getIdUsuario());

        assertNotNull(tarefaResumidosResponse);
        assertTrue(tarefaResumidosResponse.isEmpty());
    }

    @Test
    @DisplayName("Retorna 400 quando o id passado por parâmetro não corresponde a nenhum usuário")
    void deveRetornarExececao400BadRequestQuandoUsuarioIdNaoExistente() {
        UUID uuid = UUID.randomUUID();

        when(usuarioRepository.buscaUsuarioPorId(ArgumentMatchers.isA(UUID.class)))
                .thenThrow(APIException.build(HttpStatus.BAD_REQUEST, "Usuario não encontrado!"));

        APIException exception = assertThrows(APIException.class, () -> tarefaApplicationService
                .retornaTodasTarefas(uuid.toString(), uuid));

        assertEquals("Usuario não encontrado!", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusException());
    }

    @Test
    @DisplayName("Retorna código 400 quando o email passado não corresponde a nenhum usuário")
    void deveRetornarExececao400BadRequestQuandoUsuarioEmailNaoExistente() {
        Usuario usuario = DataHelper.createUsuario();

        when(usuarioRepository.buscaUsuarioPorId(ArgumentMatchers.isA(UUID.class))).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorEmail(ArgumentMatchers.isA(String.class)))
                .thenThrow(APIException.build(HttpStatus.BAD_REQUEST, "Usuario não encontrado!"));

        APIException exception = assertThrows(APIException.class, () -> tarefaApplicationService
                .retornaTodasTarefas(usuario.getIdUsuario().toString(), usuario.getIdUsuario()));

        assertEquals("Usuario não encontrado!", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusException());
    }

    @Test
    @DisplayName(
            "Retorna códido 403 Forbidden quando passado por parâmetro não coincide com o usuário passado por token")
    void deveRetornarExececao403ForbiddenQuandoUsuarioPassadoPorParametroNaoCoincidirComOToken() {
        Usuario usuario = DataHelper.createUsuario();

        when(usuarioRepository.buscaUsuarioPorId(ArgumentMatchers.isA(UUID.class))).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorEmail(ArgumentMatchers.isA(String.class))).thenReturn(usuario);

        APIException exception = assertThrows(APIException.class, () -> tarefaApplicationService
                .retornaTodasTarefas(usuario.getIdUsuario().toString(), UUID.randomUUID()));

        assertEquals("O usuário não têm acesso às tarefas.", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusException());
    }

    public TarefaRequest getTarefaRequest() {
        TarefaRequest request =
                new TarefaRequest("tarefa 1", UUID.randomUUID(), null, null, 0);
        return request;
    }

}
