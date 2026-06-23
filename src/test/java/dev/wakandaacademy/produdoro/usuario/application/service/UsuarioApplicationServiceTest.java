package dev.wakandaacademy.produdoro.usuario.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestExecutionListeners;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.StatusUsuario;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;

@ExtendWith(MockitoExtension.class)
class UsuarioApplicationServiceTest {

        @InjectMocks
        private UsuarioApplicationService usuarioApplicationService;

        @Mock
        private UsuarioRepository usuarioRepository;

        @Test
        void IniciaPausaLongaComSucesso() {
                // given preparação
                Usuario usuario = Usuario.builder()
                                .idUsuario(UUID.randomUUID())
                                .email("rita.teste.@gmail.com")
                                .status(StatusUsuario.FOCO)
                                .build();

                when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail()))
                                .thenReturn(usuario);

                when(usuarioRepository.buscaUsuarioPorId(usuario.getIdUsuario()))
                                .thenReturn(usuario);

                when(usuarioRepository.salva(usuario))
                                .thenReturn(usuario);

                // WHEN
                usuarioApplicationService.iniciarPausaLonga(
                                usuario.getIdUsuario(),
                                usuario.getEmail());
                // Then
                verify(usuarioRepository).salva(usuario);

                assertEquals(StatusUsuario.PAUSA_LONGA, usuario.getStatus());
        }

        @Test
        void deveRetornarErroQuandoUsuarioNaoAutorizado() {
                Usuario usuario = Usuario.builder()
                                .idUsuario(UUID.randomUUID())
                                .email("rita.teste.@gmail.com")
                                .status(StatusUsuario.FOCO)
                                .build();

                UUID idUsuarioInvalido = UUID.randomUUID();

                when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail()))
                                .thenReturn(usuario);

                when(usuarioRepository.buscaUsuarioPorId(idUsuarioInvalido))
                                .thenReturn(usuario);

                APIException exception = assertThrows(
                                APIException.class,
                                () -> usuarioApplicationService.iniciarPausaLonga(
                                                idUsuarioInvalido,
                                                usuario.getEmail()));

                assertEquals(
                                "Credencial de autenticação não é válida",
                                exception.getMessage());

        }

        @Test
        void deveRetornarErroQuandoUsuarioNãoEncontrado() {
                Usuario usuario = Usuario.builder()
                                .idUsuario(UUID.randomUUID())
                                .email("rita.teste.@gmail.com")
                                .status(StatusUsuario.FOCO)
                                .build();
                when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail()))
                                .thenReturn(usuario);

                when(usuarioRepository.buscaUsuarioPorId(usuario.getIdUsuario()))
                                .thenThrow(
                                                APIException.build(HttpStatus.NOT_FOUND,
                                                                "Usuário não encontrado"));
                APIException exception = assertThrows(
                                APIException.class,
                                () -> usuarioApplicationService.iniciarPausaLonga(
                                                usuario.getIdUsuario(),
                                                usuario.getEmail()

                                ));
                assertEquals(HttpStatus.NOT_FOUND, exception.getStatusException());

                assertEquals(
                                "Usuário não encontrado",
                                exception.getMessage());

        }

        @Test
        void deveRetornarErroQuandoUsuarioJaEstaEmPausaLonga() {
                Usuario usuario = Usuario.builder()
                                .idUsuario(UUID.randomUUID())
                                .email("rita.teste.@gmail.com")
                                .status(StatusUsuario.PAUSA_LONGA)
                                .build();
                when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail()))
                                .thenReturn(usuario);
                when(usuarioRepository.buscaUsuarioPorId(usuario.getIdUsuario()))
                                .thenReturn(usuario);

                APIException exception = assertThrows(
                                APIException.class,
                                () -> usuarioApplicationService.iniciarPausaLonga(
                                                usuario.getIdUsuario(),
                                                usuario.getEmail()

                                ));
                assertEquals(HttpStatus.CONFLICT, exception.getStatusException());

                assertEquals(
                                "Usúario já esta em PAUSA_LONGA!",
                                exception.getMessage());

        }

}