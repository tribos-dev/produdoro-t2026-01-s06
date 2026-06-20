package dev.wakandaacademy.produdoro.tarefa.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaResumidoResponse;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import feign.FeignException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    void deveRetornarListaTarefasQuandoExecutadoComSucesso() {
        List<Tarefa> tarefaResumidos = List.of(
                new Tarefa(getTarefaRequest()),
                new Tarefa(getTarefaRequest()),
                new Tarefa(getTarefaRequest())
        );

        UUID uuid = UUID.randomUUID();
        Usuario usuario = getUsuario(uuid);

        when(tarefaRepository.buscaTarefasPorIdUsuario(any())).thenReturn(tarefaResumidos);
        when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);

        List<TarefaResumidoResponse> tarefaResumidosResponse = tarefaApplicationService
                .retornaTodasTarefas(uuid.toString(), uuid);

        assertNotNull(tarefaResumidosResponse);
        assertFalse(tarefaResumidosResponse.isEmpty());
        assertEquals(TarefaResumidoResponse.class, tarefaResumidosResponse.get(0).getClass());
    }

    @Test
    void deveRetornarListaTarefasVaziaQuandoExecutadoComSucesso() {
        List<Tarefa> tarefaResumidos = List.of();

        UUID uuid = UUID.randomUUID();
        Usuario usuario = getUsuario(uuid);

        when(tarefaRepository.buscaTarefasPorIdUsuario(any())).thenReturn(tarefaResumidos);
        when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);

        List<TarefaResumidoResponse> tarefaResumidosResponse = tarefaApplicationService
                .retornaTodasTarefas(uuid.toString(), uuid);

        assertNotNull(tarefaResumidosResponse);
        assertTrue(tarefaResumidosResponse.isEmpty());
    }

    @Test
    void deveRetornarExececao400BadRequestQuandoUsuarioIdNaoExistente() {
        UUID uuid = UUID.randomUUID();

        when(usuarioRepository.buscaUsuarioPorId(any()))
                .thenThrow(APIException.build(HttpStatus.BAD_REQUEST, "Usuario não encontrado!"));

        Exception exception = assertThrows(APIException.class, () -> tarefaApplicationService
                .retornaTodasTarefas(uuid.toString(), uuid));

        assertEquals("Usuario não encontrado!", exception.getMessage());
        assertEquals(APIException.class, exception.getClass());
    }

    @Test
    void deveRetornarExececao400BadRequestQuandoUsuarioEmailNaoExistente() {
        UUID uuid = UUID.randomUUID();
        Usuario usuario = getUsuario(uuid);

        when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorEmail(any()))
                .thenThrow(APIException.build(HttpStatus.BAD_REQUEST, "Usuario não encontrado!"));

        Exception exception = assertThrows(APIException.class, () -> tarefaApplicationService
                .retornaTodasTarefas(uuid.toString(), uuid));

        assertEquals("Usuario não encontrado!", exception.getMessage());
        assertEquals(APIException.class, exception.getClass());
    }

    @Test
    void deveRetornarExececao403ForbiddenQuandoUsuarioEmailNaoExistente() {
        UUID uuid = UUID.randomUUID();
        Usuario usuario = getUsuario(uuid);

        when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);

        Exception exception = assertThrows(APIException.class, () -> tarefaApplicationService
                .retornaTodasTarefas(uuid.toString(), UUID.randomUUID()));

        assertEquals("O usuário não têm acesso às tarefas.", exception.getMessage());
        assertEquals(APIException.class, exception.getClass());
    }

    public TarefaRequest getTarefaRequest() {
        TarefaRequest request =
                new TarefaRequest("tarefa 1", UUID.randomUUID(), null, null, 0);
        return request;
    }

    public Usuario getUsuario(UUID uuid) {
        return Usuario.builder().idUsuario(uuid).build();
    }
}
