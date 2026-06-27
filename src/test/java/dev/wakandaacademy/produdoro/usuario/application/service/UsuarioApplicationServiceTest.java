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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioApplicationServiceTest {

        @InjectMocks
        private UsuarioApplicationService usuarioApplicationService;

        @Mock
        private UsuarioRepository usuarioRepository;

        @Test
        void IniciaPausaLongaComSucesso() {
                Usuario usuario = DataHelper.criaUsuarioSecundario();

                when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
                when(usuarioRepository.buscaUsuarioPorId(usuario.getIdUsuario())).thenReturn(usuario);
                when(usuarioRepository.salva(usuario)).thenReturn(usuario);

                usuarioApplicationService.iniciarPausaLonga(usuario.getIdUsuario(), usuario.getEmail());

                verify(usuarioRepository).salva(usuario);
                assertEquals(StatusUsuario.PAUSA_LONGA, usuario.getStatus());
        }

        @Test
        void deveRetornarErroQuandoUsuarioNaoAutorizado() {
                Usuario usuario = DataHelper.criaUsuarioSecundario();
                UUID idUsuarioInvalido = UUID.randomUUID();

                when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
                when(usuarioRepository.buscaUsuarioPorId(idUsuarioInvalido)).thenReturn(usuario);

                APIException exception = assertThrows(APIException.class,
                                () -> usuarioApplicationService.iniciarPausaLonga(idUsuarioInvalido,
                                                usuario.getEmail()));

                assertEquals("Credencial de autenticação não é válida!", exception.getMessage());

        }

        @Test
        void deveRetornarErroQuandoUsuarioNãoEncontrado() {
                Usuario usuario = DataHelper.criaUsuarioSecundario();

                when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
                when(usuarioRepository.buscaUsuarioPorId(usuario.getIdUsuario()))
                                .thenThrow(APIException.build(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

                APIException exception = assertThrows(APIException.class,
                                () -> usuarioApplicationService.iniciarPausaLonga(usuario.getIdUsuario(),
                                                usuario.getEmail()));

                assertEquals(HttpStatus.NOT_FOUND, exception.getStatusException());
                assertEquals("Usuário não encontrado", exception.getMessage());

        }

        @Test
        void deveRetornarErroQuandoUsuarioJaEstaEmPausaLonga() {
                Usuario usuario = DataHelper.createUsuario();

                when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
                when(usuarioRepository.buscaUsuarioPorId(usuario.getIdUsuario())).thenReturn(usuario);

                APIException exception = assertThrows(APIException.class,
                                () -> usuarioApplicationService.iniciarPausaLonga(usuario.getIdUsuario(),
                                                usuario.getEmail()));

                assertEquals(HttpStatus.CONFLICT, exception.getStatusException());
                assertEquals("Usúario já esta em PAUSA_LONGA!", exception.getMessage());

        }

        @Test
        void deveMudarStatusFocoComSucesso() {

                Usuario usuario = DataHelper.createUsuario();
                UUID idUsuario = usuario.getIdUsuario();

                when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
                when(usuarioRepository.salva(usuario)).thenReturn(usuario);

                usuarioApplicationService.iniciaFoco(usuario.getEmail(), idUsuario);
                assertEquals(StatusUsuario.FOCO, usuario.getStatus());

                verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(usuario.getEmail());
                verify(usuarioRepository, times(1)).salva(usuario);
        }

        @Test
        void deveRetornarUnauthorizedQuandoTokenNaoPertenceAoUsuario() {
                Usuario usuario = DataHelper.createUsuario();
                Usuario outroUsuario = DataHelper.criaUsuarioSecundario();

                when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail()))
                                .thenReturn(usuario);

                APIException exception = assertThrows(APIException.class, () -> usuarioApplicationService
                                .iniciaFoco(usuario.getEmail(), outroUsuario.getIdUsuario()));

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

                APIException exception = assertThrows(APIException.class,
                                () -> usuarioApplicationService.iniciaFoco(usuario.getEmail(), idUsuario));

		assertEquals(HttpStatus.CONFLICT, exception.getStatusException());
		assertEquals("Usuário já está em FOCO!", exception.getMessage());
		verify(usuarioRepository, never()).salva(any());
	}

	@Test
	void deveIniciarPausaCurtaComSucesso() {
		Usuario usuario = DataHelper.criaUsuarioSecundario();
		UUID idUsuario = usuario.getIdUsuario();

		when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
		when(usuarioRepository.salva(usuario)).thenReturn(usuario);

		usuarioApplicationService.iniciaPausaCurta(usuario.getEmail(), idUsuario);

		assertEquals(StatusUsuario.PAUSA_CURTA, usuario.getStatus());
		assertEquals(1, usuario.getQuantidadePomodorosPausaCurta());
		verify(usuarioRepository, times(1)).salva(usuario);
	}

	@Test
	void deveLancarUnauthorizedQuandoTokenNaoPertenceAoUsuario() {
		Usuario usuario = DataHelper.createUsuario();
		UUID idUsuarioInvalido = UUID.randomUUID();

		when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);

		APIException exception = assertThrows(APIException.class,
				() -> usuarioApplicationService.iniciaPausaCurta(usuario.getEmail(), idUsuarioInvalido));

		assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusException());
		assertEquals("Credencial de autenticação não é válida!", exception.getMessage());
		verify(usuarioRepository, never()).salva(any());
	}

	@Test
	void deveLancarBadRequestQuandoUsuarioJaEstaEmPausaCurta() {
		Usuario usuario = Usuario.builder()
				.idUsuario(UUID.randomUUID())
				.email("pausacurta@teste.com")
				.status(StatusUsuario.PAUSA_CURTA)
				.quantidadePomodorosPausaCurta(0)
				.build();
		UUID idUsuario = usuario.getIdUsuario();

		when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);

		APIException exception = assertThrows(APIException.class,
				() -> usuarioApplicationService.iniciaPausaCurta(usuario.getEmail(), idUsuario));

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusException());
		assertEquals("Usuário já esta em PAUSA CURTA!", exception.getMessage());
		verify(usuarioRepository, never()).salva(any());
	}
}
