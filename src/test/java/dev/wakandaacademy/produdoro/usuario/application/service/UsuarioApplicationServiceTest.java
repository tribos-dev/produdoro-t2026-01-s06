package dev.wakandaacademy.produdoro.usuario.application.service;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.StatusUsuario;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;


@ExtendWith(MockitoExtension.class)
class UsuarioApplicationServiceTest {

    @InjectMocks
    UsuarioApplicationService usuarioApplicationService;

    @Mock
    UsuarioRepository usuarioRepository;

    @Test
    void deveMudarStatusFocoComSucesso() {

        Usuario usuario = DataHelper.createUsuario();
        UUID idUsuario = usuario.getIdUsuario();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail()))
                .thenReturn(usuario);

        usuarioApplicationService.iniciaFoco(usuario.getEmail(), idUsuario);
        assertEquals(StatusUsuario.FOCO, usuario.getStatus());

        verify(usuarioRepository).buscaUsuarioPorEmail(usuario.getEmail());
        verify(usuarioRepository).salva(usuario);
    }
    @Test
    void deveRetornarUnauthorizedQuandoTokenNaoPertenceAoUsuario() {
        Usuario usuario = DataHelper.createUsuario();
        Usuario outroUsuario = DataHelper.criaUsuarioSecundario();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        APIException exception = assertThrows(APIException.class, () ->
                usuarioApplicationService.iniciaFoco(usuario.getEmail(), outroUsuario.getIdUsuario()));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusException());
        assertEquals("Credencial de autenticação não é válida!", exception.getMessage());
        verify(usuarioRepository, never()).salva(any());
    }
    @Test
    void deveRetornarBadRequestQuandoUsuarioJaEstaEmFoco() {

        Usuario usuario = DataHelper.criaUsuarioSecundario();
        UUID idUsuario = usuario.getIdUsuario();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail()))
                .thenReturn(usuario);

        APIException exception = assertThrows(APIException.class, () ->
                usuarioApplicationService.iniciaFoco(usuario.getEmail(), idUsuario));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusException());
        assertEquals("Usuário já está em FOCO!", exception.getMessage());
        verify(usuarioRepository, never()).salva(any());
    }
}
