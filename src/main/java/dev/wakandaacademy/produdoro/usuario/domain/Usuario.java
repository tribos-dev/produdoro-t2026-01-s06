package dev.wakandaacademy.produdoro.usuario.domain;

import java.util.UUID;

import javax.validation.constraints.Email;

import dev.wakandaacademy.produdoro.handler.APIException;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import dev.wakandaacademy.produdoro.pomodoro.domain.ConfiguracaoPadrao;
import dev.wakandaacademy.produdoro.usuario.application.api.UsuarioNovoRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
@Document(collection = "Usuario")
public class Usuario {
	@Id
	private UUID idUsuario;
	@Email
	@Indexed(unique = true)
	private String email;
	private ConfiguracaoUsuario configuracao;
	@Builder.Default
	private StatusUsuario status = StatusUsuario.FOCO;
	@Builder.Default
	private Integer quantidadePomodorosPausaCurta = 0;
	@Builder.Default
	private Integer quantidadePomodorosPausaLonga = 0;
	private Integer contador;

	public Usuario(UsuarioNovoRequest usuarioNovo, ConfiguracaoPadrao configuracaoPadrao) {
		this.idUsuario = UUID.randomUUID();
		this.email = usuarioNovo.getEmail();
		this.status = StatusUsuario.FOCO;
		this.configuracao = new ConfiguracaoUsuario(configuracaoPadrao);
	}

	public void alterarStatusParaPausaLonga() {
		validaStatusUsuario(StatusUsuario.PAUSA_LONGA);
		this.status = StatusUsuario.PAUSA_LONGA;
	}

	private void validaStatusUsuario(StatusUsuario status) {
		if (this.status.equals(status)) {
			throw APIException.build(HttpStatus.CONFLICT, "Usuário já está em " + status + "!");
		}
	}

	public void iniciaFoco() {
		validaSeJaEstaEmFoco();
		this.status = StatusUsuario.FOCO;
	}

	public void validaIdUsuario(UUID idUsuarioRequest) {
		if (!this.idUsuario.equals(idUsuarioRequest)) {
			throw APIException.build(HttpStatus.UNAUTHORIZED,
					"Credencial de autenticação não é válida!");
		}
	}

	public void validaSeJaEstaEmFoco() {
		if (isStatusFoco()) {
			throw APIException.build(HttpStatus.CONFLICT, "Usuário já está em FOCO!");}
	}

	private boolean isStatusFoco() {
		return this.status == StatusUsuario.FOCO;
	}

	public void validaSeEstaStatusFoco() {
		if (!isStatusFoco())  {
			throw APIException.build(HttpStatus.CONFLICT, "Usuário não está em FOCO!");}
	}

	public void iniciaPausaAposPomodoro(int contagemPomodoro) {
		if (contagemPomodoro % 4 == 0) {
			iniciaPausaLonga();
		} else {
			iniciaPausaCurta();
		}
	}

	public void iniciaPausaLonga() {
		iniciaPausa(StatusUsuario.PAUSA_LONGA, configuracao.getTempoMinutosPausaLonga());
		this.quantidadePomodorosPausaLonga++;
	}

	public void iniciaPausaCurta() {
		iniciaPausa(StatusUsuario.PAUSA_CURTA, configuracao.getTempoMinutosPausaCurta());
		this.quantidadePomodorosPausaCurta++;
	}

	private void iniciaPausa(StatusUsuario novoStatus, int tempoPausa) {
		validaSeEstaStatusFoco();
		this.status = novoStatus;
		this.contador = tempoPausa;
	}

}
